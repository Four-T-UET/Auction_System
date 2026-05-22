package controller;

import auction.logic.enums.ItemCategory;
import auction.logic.manager.AuctionManager;
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
import java.util.ResourceBundle;
import service.AuctionService;
import service.ItemService;

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
    private TextField hours;
    @FXML
    private TextField minutes;
    @FXML
    private ComboBox<ItemCategory> categoryBox;
    @FXML
    private Button imageChoose;
    @FXML
    private Label StatusFile;
    private File selectFile;



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
        imageChoose.setOnAction(this::getImageChoose);

        // Set prompt text for hours and minutes
        hours.setPromptText("0-100");
        minutes.setPromptText("0-59");
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
    public void setSelling() {
        // Kiểm tra dữ liệu đầu vào (Validation)
        if (!checkValid()) {
            return;
        }

        // Kiểm tra kết nối tới server trước khi tiếp tục

        try {
            // Thu thập dữ liệu từ các TextField
            String name = nameSelling.getText().trim();
            String description = descriptionSelling.getText().trim();
            double startPriceValue = Double.parseDouble(firstPrice.getText().trim());
            double minStepValue = Double.parseDouble(step.getText().trim());

            // Lấy giờ và phút từ TextField
            int hoursValue = Integer.parseInt(hours.getText().isEmpty() ? "0" : hours.getText().trim());
            int minutesValue = Integer.parseInt(minutes.getText().isEmpty() ? "0" : minutes.getText().trim());

            // Validate values
            if (hoursValue < 0 || hoursValue > 100) {
                showAlert(Alert.AlertType.ERROR, "Lỗi định dạng", "Giờ phải nằm trong khoảng 0-100!");
                return;
            }
            if (minutesValue < 0 || minutesValue > 59) {
                showAlert(Alert.AlertType.ERROR, "Lỗi định dạng", "Phút phải nằm trong khoảng 0-59!");
                return;
            }

            ItemCategory category = categoryBox.getValue();

            // Xử lý hình ảnh
            byte[] imageBytes;
            if (selectFile != null) {
                imageBytes = Files.readAllBytes(selectFile.toPath());
            } else {
                Path defaultPath = Paths.get("loginImage.jpg");
                imageBytes = Files.exists(defaultPath) ? Files.readAllBytes(defaultPath) : new byte[0];
            }
            Item newItem = (Item)ItemService.addItem(name, category, description, imageBytes);
            if (newItem == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tạo được sản phẩm!");
                return;
            }

            // Tính tổng số phút (hours * 60 + minutes)
            int totalMinutes = hoursValue * 60 + minutesValue;
            if (totalMinutes <= 0) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Thời gian phải lớn hơn 0 phút!");
                return;
            }
            //Gọi Service để gửi dữ liệu lên Server
            Object response = AuctionService.addAuction(newItem, startPriceValue, minStepValue, totalMinutes);
            // Debug: log response class and content
            System.out.println("[ControlSelling] Server response class: " + (response == null ? "null" : response.getClass().getName()));
            System.out.println("[ControlSelling] Server response toString: " + (response == null ? "null" : response.toString()));
            // Xử lý kết quả trả về từ Server
            if (response instanceof Auction finalAuction) {
                AuctionManager.getInstance().addOrUpdate(finalAuction);
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Đã tạo phiên đấu giá thành công cho mục: " + finalAuction.getItem().getName() +
                        " - Thời gian: " + hoursValue + " giờ " + minutesValue + " phút");

                // Tùy chọn: Reset form sau khi thành công
                // clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Server không thể tạo phiên đấu giá. Vui lòng thử lại.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi định dạng", "Giá, bước giá phải là số hợp lệ!");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi file", "Không thể xử lý hình ảnh: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Có lỗi xảy ra: " + e.getMessage());
        }
    }


//    public Auction getData() {
//        if (!checkValid()) {
//            return null;
//        }
//
//        String name = nameSelling.getText().trim();
//        String description = descriptionSelling.getText().trim();
//        double startPriceValue = Double.parseDouble(firstPrice.getText().trim());
//        double minStepValue = Double.parseDouble(step.getText().trim());
//        int durationDays = Integer.parseInt(timeBid.getText().trim());
//        ItemCategory category = categoryBox.getValue();
//        LocalDateTime customStartTime = parseStartTime(startTime.getText().trim());
//
//        ItemFactory factory;
//        switch (category) {
//            case VEHICLE -> factory = new RealEstateFactory();
//            case ARTS ->  factory = new ArtFactory();
//            case ELECTRONICS -> factory = new ElectronicsFactory();
//            default -> factory = new RealEstateFactory();
//        }
//        Item newItem = factory.createItem(name,description);
//        Object auction = AuctionService.addAuction(newItem, startPriceValue,minStepValue, durationDays);
//
//    }
//    @FXML
//    public void setSelling(ActionEvent event) {
//        try {
//            Auction auction = getData();
//            if (auction == null) {
//                // Dừng hàm nếu nhập liệu không hợp lệ
//                return;
//            }
//            if (selectFile != null) {
//                try {
//                    byte[] imageBytes = Files.readAllBytes(selectFile.toPath());
//                    auction.getItem().setImageBytes(imageBytes);
//                } catch (IOException e) {
//                    showAlert(Alert.AlertType.ERROR, "Error", "Image error");
//                    return;
//                }
//            } else {
//                Path path = Paths.get("loginImage.jpg");
//                byte[] imageBytes = Files.readAllBytes(path);
//                auction.getItem().setImageBytes(imageBytes);
//                StatusFile.setText("Not Found");
//            }
//
//            // Kiểm tra và đưa Auction lên mainDashboard
//            if(ItemService.addItem() instanceof  Item){
//                Object auction1 = AuctionService.addAuction();
//
//
//            }
//            //TODO: Lưu thông tin vào database
//        }catch (IOException e){
//            showAlert(Alert.AlertType.ERROR, "Error", "Khong the tao phien");
//        }
}
