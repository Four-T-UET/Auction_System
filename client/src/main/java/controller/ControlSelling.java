package controller;

import auction.logic.enums.ItemCategory;
import auction.logic.factory.ArtFactory;
import auction.logic.factory.ElectronicsFactory;
import auction.logic.factory.ItemFactory;
import auction.logic.factory.RealEstateFactory;
import auction.logic.factory.VehicleFactory;
import auction.logic.model.Auction;
import auction.logic.model.Item;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

import static controller.AlertShow.showAlert;

public class ControlSelling implements Initializable {
    @FXML
    private TextField nameSelling;
    @FXML
    private TextField descriptionSelling;
    @FXML
    private TextField firstPrice;
    @FXML
    private TextField step;
    @FXML
    private TextField startTime;
    @FXML
    private TextField timeBid;
    @FXML
    private ComboBox<ItemCategory> categoryBox;
    @FXML
    private Button imageChoose;
    @FXML
    private Label StatusFile;
    private File selectFile;

    public Auction getData() {
        if (!checkValid()) {
            return null;
        }

        String name = nameSelling.getText().trim();
        String description = descriptionSelling.getText().trim();
        double startPriceValue = Double.parseDouble(firstPrice.getText().trim());
        double minStepValue = Double.parseDouble(step.getText().trim());
        int durationDays = Integer.parseInt(timeBid.getText().trim());
        ItemCategory category = categoryBox.getValue();
        LocalDateTime customStartTime = parseStartTime(startTime.getText().trim());

        ItemFactory factory;
        switch (category) {
            case VEHICLE -> factory = new RealEstateFactory();
            case ARTS ->  factory = new ArtFactory();
            case REAL_ESTATE ->  factory = new RealEstateFactory();
            case ELECTRONICS -> factory = new ElectronicsFactory();
            default -> factory = new RealEstateFactory();
        }
        Item newItem = factory.createItem(name,description);
        return new Auction(newItem, startPriceValue, minStepValue, durationDays);
    }

    private LocalDateTime parseStartTime(String timeStart) {
        if (!timeStart.isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy H:m:s");
                LocalDateTime time = LocalDateTime.parse(timeStart, formatter);
                return time;
            } catch (DateTimeParseException e) {
                showAlert(Alert.AlertType.ERROR,"Error","Invaid input for date!");
            }
        }
        return null;
    }


    private boolean checkValid(){
        // Logic kiểm tra rỗng / null / kiểu dữ liệu cho phiên bản đầy đủ
        // Sẽ được hoàn thiện ở bước tiếp theo để an toàn hơn
        if (nameSelling.getText().isEmpty() || firstPrice.getText().isEmpty() || categoryBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR,"Error","Please fill all the fields!");
            return false;
        }

        return true;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        StatusFile.setText("Not Found");
        categoryBox.getItems().setAll(ItemCategory.values());
        categoryBox.getSelectionModel().select(ItemCategory.REAL_ESTATE);
        imageChoose.setOnAction(e -> {
            getImageChoose(e);
        });

    }

    @FXML
    public void getImageChoose(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );

        Stage stage = (Stage) imageChoose.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            this.selectFile = file;
            StatusFile.setText(file.getName());
        }
    }
    @FXML
    public void setSelling(ActionEvent event) {
        try {
            Auction auction = getData();
            if (auction == null) {
                // Dừng hàm nếu nhập liệu không hợp lệ
                return;
            }
            if (selectFile != null) {
                try {
                    byte[] imageBytes = Files.readAllBytes(selectFile.toPath());
                    auction.getItem().setImageBytes(imageBytes);
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Image error");
                    return;
                }
            } else {
                Path path = Paths.get("loginImage.jpg");
                byte[] imageBytes = Files.readAllBytes(path);
                auction.getItem().setImageBytes(imageBytes);
                StatusFile.setText("Not Found");
            }
            System.out.println(auction.getItem().getName());
            System.out.println(auction.getCurrentPrice());
            System.out.println(auction.getItem().getImageBytes().length);
            //TODO: Lưu thông tin vào database
        }catch (IOException e){
            showAlert(Alert.AlertType.ERROR, "Error", "Khong the tao phien");
        }
    }


}
