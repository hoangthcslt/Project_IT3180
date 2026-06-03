package com.bluemoon.controllers;

import com.bluemoon.models.Permission;
import com.bluemoon.models.User;
import com.bluemoon.models.UserGroup;
import com.bluemoon.models.UserGroupAssignment;
import com.bluemoon.services.PermissionService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PermissionController {
    @FXML
    private TextField txtSearchGroup;
    @FXML
    private TableView<UserGroup> tblGroups;
    @FXML
    private TableColumn<UserGroup, Number> colGroupStt;
    @FXML
    private TableColumn<UserGroup, String> colGroupName;
    @FXML
    private TableColumn<UserGroup, String> colGroupDesc;
    @FXML
    private TableColumn<UserGroup, Void> colPermission;
    @FXML
    private TableColumn<UserGroup, Void> colGroupAction;

    @FXML
    private TextField txtSearchUser;
    @FXML
    private TableView<UserGroupAssignment> tblUserGroups;
    @FXML
    private TableColumn<UserGroupAssignment, Number> colUserStt;
    @FXML
    private TableColumn<UserGroupAssignment, String> colUsername;
    @FXML
    private TableColumn<UserGroupAssignment, String> colUserGroup;
    @FXML
    private TableColumn<UserGroupAssignment, String> colUserRole;
    @FXML
    private TableColumn<UserGroupAssignment, Void> colUserAction;

    private final PermissionService permissionService = new PermissionService();

    @FXML
    public void initialize() {
        setupGroupTable();
        setupUserGroupTable();
        loadGroups();
        loadUserAssignments();
    }

    @FXML
    void handleSearchGroup() {
        loadGroups();
    }

    @FXML
    void handleAddGroup() {
        showGroupDialog(null);
    }

    @FXML
    void handleSearchUser() {
        loadUserAssignments();
    }

    @FXML
    void handleAssignUser() {
        UserGroupAssignment selected = tblUserGroups.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thong bao");
            alert.setHeaderText(null);
            alert.setContentText("Vui long chon mot user trong bang de chinh sua.");
            alert.showAndWait();
            return;
        }
        showAssignUserDialog(selected);
    }

    @FXML
    void handleAddUser() {
        showAddUserDialog();
    }

    @FXML
    void handleExportUserExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Xuất Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("Users.xlsx");
        File file = fileChooser.showSaveDialog(tblUserGroups.getScene().getWindow());

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Users");
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Username");
                headerRow.createCell(1).setCellValue("Password");

                List<User> users = permissionService.findAllUsers();
                int rowNum = 1;
                for (User user : users) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(user.getUsername());
                    String pass = user.getPassword();
                    if (pass != null && pass.startsWith("$2a$")) {
                        row.createCell(1).setCellValue("[ENCRYPTED]");
                    } else {
                        row.createCell(1).setCellValue(pass);
                    }
                }

                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Xuất file Excel thành công!");
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText("Không thể xuất file");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void setupGroupTable() {
        tblGroups.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colGroupStt.setCellValueFactory(
                cell -> new ReadOnlyObjectWrapper<>(tblGroups.getItems().indexOf(cell.getValue()) + 1));
        colGroupName.setCellValueFactory(new PropertyValueFactory<>("tenNhom"));
        colGroupDesc.setCellValueFactory(new PropertyValueFactory<>("moTa"));
        colPermission.setCellFactory(col -> actionCell("🔐", group -> showPermissionDialog(group)));
        colGroupAction.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = createIconButton("✎");
            private final Button deleteButton = createIconButton("🗑");
            private final HBox box = new HBox(6, editButton, deleteButton);

            {
                box.setAlignment(Pos.CENTER);
                editButton.setOnAction(event -> showGroupDialog(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> confirmDeleteGroup(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupUserGroupTable() {
        tblUserGroups.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colUserStt.setCellValueFactory(
                cell -> new ReadOnlyObjectWrapper<>(tblUserGroups.getItems().indexOf(cell.getValue()) + 1));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUserGroup.setCellValueFactory(new PropertyValueFactory<>("groupName"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colUserAction.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = createIconButton("✎");
            private final Button deleteButton = createIconButton("🗑");
            private final HBox box = new HBox(6, editButton, deleteButton);

            {
                box.setAlignment(Pos.CENTER);
                editButton.setOnAction(event -> showAssignUserDialog(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> confirmDeleteUser(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private TableCell<UserGroup, Void> actionCell(String icon, java.util.function.Consumer<UserGroup> action) {
        return new TableCell<>() {
            private final Button button = createIconButton(icon);

            {
                button.setOnAction(event -> action.accept(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : button);
                setAlignment(Pos.CENTER);
            }
        };
    }

    private Button createIconButton(String text) {
        Button button = new Button(text);
        button.setMinSize(34, 30);
        button.setStyle(
                "-fx-background-color: #eef3f8; -fx-background-radius: 7; -fx-font-weight: bold; -fx-cursor: hand;");
        button.setOnMouseEntered(event -> button.setStyle(
                "-fx-background-color: #d9e7f5; -fx-background-radius: 7; -fx-font-weight: bold; -fx-cursor: hand;"));
        button.setOnMouseExited(event -> button.setStyle(
                "-fx-background-color: #eef3f8; -fx-background-radius: 7; -fx-font-weight: bold; -fx-cursor: hand;"));
        return button;
    }

    private void loadGroups() {
        tblGroups.setItems(FXCollections.observableArrayList(permissionService.findGroups(txtSearchGroup.getText())));
    }

    private void loadUserAssignments() {
        tblUserGroups.setItems(
                FXCollections.observableArrayList(permissionService.findUserAssignments(txtSearchUser.getText())));
    }

    private void showGroupDialog(UserGroup group) {
        boolean isEdit = group != null;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Sửa nhóm người dùng" : "Thêm mới nhóm người dùng");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);

        TextField txtName = new TextField(isEdit ? group.getTenNhom() : "");
        TextArea txtDescription = new TextArea(isEdit ? group.getMoTa() : "");
        txtDescription.setPrefRowCount(3);
        Label error = new Label();
        error.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");

        Button saveButton = primaryButton("Lưu");
        Button closeButton = secondaryButton("Đóng");
        HBox actions = new HBox(10, saveButton, closeButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(16));
        form.add(new Label("Tên nhóm *"), 0, 0);
        form.add(txtName, 1, 0);
        form.add(new Label("Mô tả *"), 0, 1);
        form.add(txtDescription, 1, 1);
        form.add(error, 1, 2);
        form.add(actions, 1, 3);
        dialog.getDialogPane().setContent(form);

        saveButton.setOnAction(event -> {
            try {
                UserGroup target = isEdit ? group : new UserGroup();
                target.setTenNhom(txtName.getText());
                target.setMoTa(txtDescription.getText());
                if (permissionService.saveGroup(target)) {
                    loadGroups();
                    dialog.close();
                } else {
                    error.setText("Không thể lưu nhóm người dùng.");
                }
            } catch (IllegalArgumentException ex) {
                error.setText(ex.getMessage());
            }
        });
        closeButton.setOnAction(event -> dialog.close());
        dialog.showAndWait();
    }

    private void confirmDeleteGroup(UserGroup group) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn xóa không?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            permissionService.deleteGroup(group.getId());
            loadGroups();
            loadUserAssignments();
        }
    }

    private void showPermissionDialog(UserGroup group) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Phân quyền nhóm: " + group.getTenNhom());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);

        Set<String> selected = permissionService.getPermissionCodesByGroup(group.getId());
        List<Permission> permissions = permissionService.getSystemPermissions();
        VBox list = new VBox(10);
        list.setPadding(new Insets(16));
        Set<CheckBox> checkBoxes = new HashSet<>();
        for (Permission permission : permissions) {
            CheckBox checkBox = new CheckBox(permission.getName());
            checkBox.setUserData(permission.getCode());
            checkBox.setSelected(selected.contains(permission.getCode()));
            checkBox.setStyle("-fx-font-size: 14px;");
            checkBoxes.add(checkBox);
            list.getChildren().add(checkBox);
        }

        Button saveButton = primaryButton("Lưu");
        Button closeButton = secondaryButton("Đóng");
        HBox actions = new HBox(10, saveButton, closeButton);
        actions.setAlignment(Pos.CENTER_RIGHT);
        list.getChildren().add(actions);
        dialog.getDialogPane().setContent(list);

        saveButton.setOnAction(event -> {
            Set<String> codes = new HashSet<>();
            for (CheckBox checkBox : checkBoxes) {
                if (checkBox.isSelected()) {
                    codes.add((String) checkBox.getUserData());
                }
            }
            permissionService.saveGroupPermissions(group.getId(), codes);
            dialog.close();
        });
        closeButton.setOnAction(event -> dialog.close());
        dialog.showAndWait();
    }

    private void showAssignUserDialog(UserGroupAssignment assignment) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Quản trị nhóm người dùng");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);

        TextField txtUsername = new TextField(assignment == null ? "" : assignment.getUsername());
        ComboBox<UserGroup> cbGroups = new ComboBox<>(
                FXCollections.observableArrayList(permissionService.findGroups(null)));
        TextField txtRole = new TextField(assignment == null ? "" : assignment.getRole());
        txtUsername.setPrefWidth(280);
        cbGroups.setPrefWidth(280);
        txtRole.setPrefWidth(280);
        cbGroups.setConverter(new StringConverter<>() {
            @Override
            public String toString(UserGroup group) {
                return group == null ? "" : group.getTenNhom();
            }

            @Override
            public UserGroup fromString(String string) {
                return null;
            }
        });
        cbGroups.getSelectionModel().selectedItemProperty().addListener((obs, oldGroup, newGroup) -> {
            if (newGroup != null) {
                txtRole.setText(newGroup.getTenNhom());
            }
        });
        selectInitialGroup(cbGroups, assignment);

        Label error = new Label();
        error.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
        Button saveButton = primaryButton("Lưu");
        Button closeButton = secondaryButton("Đóng");
        HBox actions = new HBox(10, saveButton, closeButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(16));
        form.add(new Label("Username"), 0, 0);
        form.add(txtUsername, 1, 0);
        form.add(new Label("Nhóm"), 0, 1);
        form.add(cbGroups, 1, 1);
        form.add(new Label("Vai tro"), 0, 2);
        form.add(txtRole, 1, 2);
        form.add(error, 1, 3);
        form.add(actions, 1, 4);
        dialog.getDialogPane().setContent(form);

        saveButton.setOnAction(event -> {
            if (assignment == null) {
                error.setText("Vui lòng chọn người dùng và nhóm.");
                return;
            }
            UserGroup selectedGroup = cbGroups.getValue();
            if (selectedGroup == null) {
                selectedGroup = findDefaultGroup(cbGroups.getItems());
                cbGroups.setValue(selectedGroup);
            }
            if (selectedGroup == null) {
                error.setText("Khong tim thay nhom mac dinh.");
                return;
            }
            try {
                permissionService.updateUserAndGroup(assignment.getUserId(), txtUsername.getText(), txtRole.getText().toUpperCase(),
                        selectedGroup.getId());
                loadUserAssignments();
                dialog.close();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                error.setText(ex.getMessage());
            }
        });
        closeButton.setOnAction(event -> dialog.close());
        dialog.showAndWait();
    }

    private void showAddUserDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Thêm người dùng mới");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);

        TextField txtUsername = new TextField();
        PasswordField txtPassword = new PasswordField();
        ComboBox<UserGroup> cbGroups = new ComboBox<>(
                FXCollections.observableArrayList(permissionService.findGroups(null)));
        txtUsername.setPrefWidth(280);
        txtPassword.setPrefWidth(280);
        cbGroups.setPrefWidth(280);

        cbGroups.setConverter(new StringConverter<>() {
            @Override
            public String toString(UserGroup group) {
                return group == null ? "" : group.getTenNhom();
            }

            @Override
            public UserGroup fromString(String string) {
                return null;
            }
        });
        selectInitialGroup(cbGroups, null);

        Label error = new Label();
        error.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
        Button saveButton = primaryButton("Lưu");
        Button closeButton = secondaryButton("Đóng");
        HBox actions = new HBox(10, saveButton, closeButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(16));
        form.add(new Label("Username *"), 0, 0);
        form.add(txtUsername, 1, 0);
        form.add(new Label("Password *"), 0, 1);
        form.add(txtPassword, 1, 1);
        form.add(new Label("Nhóm *"), 0, 2);
        form.add(cbGroups, 1, 2);
        form.add(error, 1, 3);
        form.add(actions, 1, 4);
        dialog.getDialogPane().setContent(form);

        saveButton.setOnAction(event -> {
            String username = txtUsername.getText();
            String password = txtPassword.getText();
            UserGroup selectedGroup = cbGroups.getValue();

            if (username == null || username.trim().isEmpty()) {
                error.setText("Username không được để trống.");
                return;
            }
            if (password == null || password.trim().isEmpty()) {
                error.setText("Password không được để trống.");
                return;
            }
            if (selectedGroup == null) {
                error.setText("Vui lòng chọn nhóm.");
                return;
            }

            try {
                permissionService.insertUserAndGroup(username, password, selectedGroup.getTenNhom().toUpperCase(),
                        selectedGroup.getId());
                loadUserAssignments();
                dialog.close();
            } catch (Exception ex) {
                error.setText("Lỗi: " + ex.getMessage());
            }
        });
        closeButton.setOnAction(event -> dialog.close());
        dialog.showAndWait();
    }

    private void selectInitialGroup(ComboBox<UserGroup> cbGroups, UserGroupAssignment assignment) {
        if (assignment != null && assignment.getGroupId() > 0) {
            cbGroups.getItems().stream()
                    .filter(group -> group.getId() == assignment.getGroupId())
                    .findFirst()
                    .ifPresent(cbGroups::setValue);
        }
        if (cbGroups.getValue() == null) {
            cbGroups.setValue(findDefaultGroup(cbGroups.getItems()));
        }
    }

    private UserGroup findDefaultGroup(List<UserGroup> groups) {
        return groups.stream()
                .filter(group -> "Trống".equalsIgnoreCase(group.getTenNhom()))
                .findFirst()
                .orElse(groups.isEmpty() ? null : groups.get(0));
    }

    private void confirmDeleteUser(UserGroupAssignment assignment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn xóa tài khoản này không?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            permissionService.deleteUser(assignment.getUserId());
            loadUserAssignments();
        }
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: #2f80ed; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 9 18; -fx-background-radius: 8;");
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-padding: 9 18; -fx-background-radius: 8;");
        return button;
    }
}
