package com.bluemoon.controllers;

import com.bluemoon.models.User;
import com.bluemoon.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML
    private Label lblWelcome;

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblWelcome.setText("Xin chào, " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        }
    }

    @FXML
    void handleHoKhau(ActionEvent event) {
        // Todo: Implement Hộ khẩu feature
    }

    @FXML
    void handleNhanKhau(ActionEvent event) {
        // Todo: Implement Nhân khẩu feature
    }

    @FXML
    void handleKhoanThu(ActionEvent event) {
        // Todo: Implement Khoản thu feature
    }

    @FXML
    void handleNopTien(ActionEvent event) {
        // Todo: Implement Nộp tiền feature
    }

    @FXML
    void handleThongKe(ActionEvent event) {
        // Todo: Implement Thống kê feature
    }
}
