package com.bluemoon.controllers;

import com.bluemoon.models.User;
import com.bluemoon.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import java.io.IOException;

public class DashboardController {

    @FXML
    private Label lblWelcome;
    
    @FXML
    private BorderPane mainBorderPane;

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblWelcome.setText("Xin chào, " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        }
    }
    
    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/" + fxml));
            Node view = loader.load();
            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleHoKhau(ActionEvent event) {
        loadView("hokhau.fxml");
    }

    @FXML
    void handleNhanKhau(ActionEvent event) {
        // Todo: Implement Nhân khẩu feature
    }

    @FXML
    void handleKhoanThu(ActionEvent event) {
        loadView("khoanthu.fxml");
    }

    @FXML
    void handleNopTien(ActionEvent event) {
        loadView("thanhtoan.fxml");
    }

    @FXML
    void handleThongKe(ActionEvent event) {
        loadView("thongke.fxml");
    }
}
