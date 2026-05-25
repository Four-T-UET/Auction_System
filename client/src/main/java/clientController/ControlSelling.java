package clientController;

import auction.logic.enums.ItemCategory;
import auction.logic.manager.AuctionManager;
import auction.logic.model.Auction;
import auction.logic.model.Item;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.AuctionService;
import service.ItemService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import static Utils.AlertShow.showAlert;

public class ControlSelling implements Initializable {
    private static final int MAX_HOURS = 100;
    private static final int MAX_MINUTES = 59;
    private static final String DEFAULT_IMAGE_PATH = "image/loginImage.jpg";

    @FXML private TextField nameSelling;
    @FXML private TextField descriptionSelling;
    @FXML private TextField firstPrice;
    @FXML private TextField step;
    @FXML private TextField hours;
    @FXML private TextField minutes;
    @FXML private ComboBox<ItemCategory> categoryBox;
    @FXML private Button imageChoose;
    @FXML private Label StatusFile;
    @FXML private ProgressIndicator progressSelling;
    @FXML private Button createButton;

    private File selectFile;

    private record SellingFormData(
            String name,
            String description,
            double startPrice,
            double stepPrice,
            int hours,
            int minutes,
            ItemCategory category
    ) {
        int totalMinutes() {
            return hours * 60 + minutes;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        StatusFile.setText("Not Found");
        categoryBox.getItems().setAll(ItemCategory.values());
        categoryBox.getSelectionModel().select(ItemCategory.REAL_ESTATE);
        imageChoose.setOnAction(this::getImageChoose);
        hours.setPromptText("0-100");
        minutes.setPromptText("0-59");
        setBusy(false);
    }

    @FXML
    public void getImageChoose(ActionEvent event) {
        event.consume();

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );

        Stage stage = (Stage) imageChoose.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectFile = file;
            StatusFile.setText(file.getName());
        }
    }

    @FXML
    public void setSelling() {
        if (!hasRequiredFields()) {
            return;
        }

        final SellingFormData formData;
        try {
            formData = readFormData();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi định dạng", "Giá, bước giá, giờ hoặc phút phải là số hợp lệ!");
            return;
        }

        if (formData.hours() < 0 || formData.hours() > MAX_HOURS) {
            showAlert(Alert.AlertType.ERROR, "Lỗi định dạng", "Giờ phải nằm trong khoảng 0-100!");
            return;
        }
        if (formData.minutes() < 0 || formData.minutes() > MAX_MINUTES) {
            showAlert(Alert.AlertType.ERROR, "Lỗi định dạng", "Phút phải nằm trong khoảng 0-59!");
            return;
        }
        if (formData.totalMinutes() <= 0) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Thời gian phải lớn hơn 0 phút!");
            return;
        }

        Task<Auction> task = createSellingTask(formData);
        task.setOnRunning(event -> {
            event.consume();
            setBusy(true);
        });
        task.setOnSucceeded(event -> {
            event.consume();
            setBusy(false);
            handleSuccess(task.getValue(), formData);
        });
        task.setOnFailed(event -> {
            event.consume();
            setBusy(false);
            Throwable ex = task.getException();
            showAlert(Alert.AlertType.ERROR, "Lỗi", ex == null ? "Có lỗi xảy ra" : ex.getMessage());
        });

        Thread thread = new Thread(task, "selling-task");
        thread.setDaemon(true);
        thread.start();
    }

    private boolean hasRequiredFields() {
        if (nameSelling.getText().isBlank() || firstPrice.getText().isBlank() || categoryBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all the fields!");
            return false;
        }
        return true;
    }

    private SellingFormData readFormData() {
        return new SellingFormData(
                nameSelling.getText().trim(),
                descriptionSelling.getText().trim(),
                Double.parseDouble(firstPrice.getText().trim()),
                Double.parseDouble(step.getText().trim()),
                Integer.parseInt(hours.getText().isBlank() ? "0" : hours.getText().trim()),
                Integer.parseInt(minutes.getText().isBlank() ? "0" : minutes.getText().trim()),
                categoryBox.getValue()
        );
    }

    private Task<Auction> createSellingTask(SellingFormData formData) {
        return new Task<>() {
            @Override
            protected Auction call() throws Exception {
                byte[] imageBytes = resolveImageBytes();
                String sellerId = UserSession.getCurrentUser().getId();
                Item newItem = (Item) ItemService.addItem(formData.name(), formData.category(), formData.description(), imageBytes);
                if (newItem == null) {
                    throw new IllegalStateException("Không tạo được sản phẩm!");
                }

                return (Auction) AuctionService.addAuction(
                        newItem,
                        formData.startPrice(),
                        formData.stepPrice(),
                        formData.totalMinutes(),
                        UserSession.getCurrentUser().getId()
                );
            }
        };
    }

    private byte[] resolveImageBytes() throws IOException {
        if (selectFile != null) {
            return Files.readAllBytes(selectFile.toPath());
        }

        Path defaultPath = Paths.get(DEFAULT_IMAGE_PATH);
        return Files.exists(defaultPath) ? Files.readAllBytes(defaultPath) : new byte[0];
    }

    private void handleSuccess(Auction response, SellingFormData formData) {
        System.out.println("[ControlSelling] Server response class: " + (response == null ? "null" : response.getClass().getName()));
        System.out.println("[ControlSelling] Server response toString: " + (response == null ? "null" : response.toString()));

        if (response == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Server không thể tạo phiên đấu giá. Vui lòng thử lại.");
            return;
        }

        AuctionManager.getInstance().addOrUpdate(response);
        showAlert(Alert.AlertType.INFORMATION, "Thành công",
                "Đã tạo phiên đấu giá thành công cho mục: " + response.getItem().getName() +
                        " - Thời gian: " + formData.hours() + " giờ " + formData.minutes() + " phút");
    }

    private void setBusy(boolean busy) {
        if (progressSelling != null) {
            progressSelling.setVisible(busy);
        }
        setInputsDisabled(busy);
    }

    private void setInputsDisabled(boolean disabled) {
        nameSelling.setDisable(disabled);
        descriptionSelling.setDisable(disabled);
        firstPrice.setDisable(disabled);
        step.setDisable(disabled);
        hours.setDisable(disabled);
        minutes.setDisable(disabled);
        categoryBox.setDisable(disabled);
        imageChoose.setDisable(disabled);
        if (createButton != null) {
            createButton.setDisable(disabled);
        }
    }
}

