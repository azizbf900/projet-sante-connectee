package tn.esprit.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import tn.esprit.models.User;
import tn.esprit.models.UserActivityLog;
import tn.esprit.utils.Database;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;

public class DashboardController {
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> createdAtCol;
    @FXML private TableColumn<User, Void> actionsCol;
    @FXML private TextField searchField;
    @FXML private Pagination pagination;
    @FXML private Button statsBtn;
    @FXML private VBox userTablePane;
    @FXML private VBox statsPane;
    @FXML private StackPane mainContentPane;
    @FXML private LineChart<String, Number> userLineChart;
    @FXML private CategoryAxis dateAxis;
    @FXML private NumberAxis countAxis;
    @FXML private Button usersBtn;
    @FXML private Button logoutBtn;
    @FXML private Button chatbotBtn;
    @FXML private TableView<UserActivityLog> activityLogTable;
    @FXML private TableColumn<UserActivityLog, Integer> logIdCol;
    @FXML private TableColumn<UserActivityLog, Integer> logUserIdCol;
    @FXML private TableColumn<UserActivityLog, String> logActionCol;
    @FXML private TableColumn<UserActivityLog, String> logDetailsCol;
    @FXML private TableColumn<UserActivityLog, String> logTimestampCol;
    @FXML private VBox activityLogPane;
    @FXML private Button activityLogBtn;

    private ObservableList<User> allUsers = FXCollections.observableArrayList();
    private static final int ROWS_PER_PAGE = 10;

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        createdAtCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        addEditButtonToTable();
        setupSearchAndPagination();
        loadAllUsers();
        setupStatsButton();
        setupUsersButton();
        setupLogoutButton();
        setupChatbotButton();
        // Setup activity log table columns
        if (activityLogTable != null) {
            logIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            logUserIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
            logActionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
            logDetailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));
            logTimestampCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTimestamp().toString()));
        }
        // Hide activity log pane by default
        if (activityLogPane != null) {
            activityLogPane.setVisible(false);
            activityLogPane.setManaged(false);
        }
        if (userTablePane != null) {
            userTablePane.setVisible(true);
            userTablePane.setManaged(true);
        }
        setupActivityLogButton();
    }

    private void setupSearchAndPagination() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePagination();
        });
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            updateTable();
        });
    }

    private void loadAllUsers() {
        allUsers.clear();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = Database.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT id, username, email, created_at FROM users");
            while (rs.next()) {
                allUsers.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        null, // password not shown/used
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        updatePagination();
    }

    private void updatePagination() {
        ObservableList<User> filtered = getFilteredUsers();
        int pageCount = (int) Math.ceil((double) filtered.size() / ROWS_PER_PAGE);
        pagination.setPageCount(Math.max(pageCount, 1));
        pagination.setCurrentPageIndex(0);
        updateTable();
    }

    private void updateTable() {
        ObservableList<User> filtered = getFilteredUsers();
        int pageIndex = pagination.getCurrentPageIndex();
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filtered.size());
        if (fromIndex < toIndex) {
            userTable.getItems().clear();
            userTable.setItems(FXCollections.observableArrayList(filtered.subList(fromIndex, toIndex)));
        } else {
            userTable.getItems().clear();
            userTable.setItems(FXCollections.observableArrayList());
        }
        // Force refresh of actions column cell factory to fix disappearing buttons
        actionsCol.setVisible(false);
        actionsCol.setVisible(true);
        // Disable vertical scroll bar
        userTable.setFixedCellSize(32); // adjust as needed for your row height
        userTable.setPrefHeight(32 * ROWS_PER_PAGE + 28); // 28 for header, adjust if needed
        userTable.setMaxHeight(32 * ROWS_PER_PAGE + 28);
    }

    private ObservableList<User> getFilteredUsers() {
        String filter = searchField.getText();
        if (filter == null || filter.trim().isEmpty()) {
            return allUsers;
        }
        String lower = filter.toLowerCase();
        return allUsers.filtered(user ->
            user.getUsername().toLowerCase().contains(lower) ||
            user.getEmail().toLowerCase().contains(lower)
        );
    }

    private void addEditButtonToTable() {
        actionsCol.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<User, Void>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final HBox btnBox = new HBox(10, editBtn, deleteBtn);
                    {
                        editBtn.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white; -fx-font-weight: bold;");
                        deleteBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");
                        editBtn.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            showEditDialog(user);
                        });
                        deleteBtn.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            deleteUser(user);
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btnBox);
                    }
                };
            }
        });
    }

    private void deleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete user '" + user.getUsername() + "'?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Connection conn = null;
                Statement stmt = null;
                try {
                    conn = Database.getConnection();
                    stmt = conn.createStatement();
                    String delete = String.format("DELETE FROM users WHERE id=%d", user.getId());
                    stmt.executeUpdate(delete);
                    loadAllUsers();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
                    try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
                }
            }
        });
    }

    private void showEditDialog(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user details (password cannot be changed)");

        TextField usernameField = new TextField(user.getUsername());
        TextField emailField = new TextField(user.getEmail());

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Username:"),
                usernameField,
                new Label("Email:"),
                emailField
        );
        dialog.getDialogPane().setContent(content);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new User(
                        user.getId(),
                        usernameField.getText(),
                        emailField.getText(),
                        null,
                        user.getCreatedAt()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newUser -> {
            Connection conn = null;
            Statement stmt = null;
            try {
                conn = Database.getConnection();
                stmt = conn.createStatement();
                String update = String.format("UPDATE users SET username='%s', email='%s' WHERE id=%d", newUser.getUsername(), newUser.getEmail(), newUser.getId());
                stmt.executeUpdate(update);
                loadAllUsers();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
                try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    private void setupStatsButton() {
        statsBtn.setOnAction(e -> showStatsPane());
    }
    private void setupUsersButton() {
        if (usersBtn != null) {
            usersBtn.setOnAction(e -> showUserTable());
        }
    }

    private void setupLogoutButton() {
        if (logoutBtn != null) {
            logoutBtn.setOnAction(e -> handleLogout());
        }
    }

    private void setupChatbotButton() {
        if (chatbotBtn != null) {
            chatbotBtn.setOnAction(e -> openChatbot());
        }
    }

    private void openChatbot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/chatbot.fxml"));
            Parent chatbotRoot = loader.load();
            Stage chatbotStage = new Stage();
            chatbotStage.setTitle("Chatbot Santé");
            chatbotStage.setScene(new Scene(chatbotRoot, 500, 500));
            chatbotStage.setResizable(false);
            chatbotStage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleLogout() {
        // Call logout method from LoginController
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/login.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(loginRoot, 900, 600));
            stage.setTitle("Login");
            stage.setResizable(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void showUserTable() {
        if (userTablePane != null) {
            userTablePane.setVisible(true);
            userTablePane.setManaged(true);
        }
        if (activityLogPane != null) {
            activityLogPane.setVisible(false);
            activityLogPane.setManaged(false);
        }
        if (statsPane != null) {
            statsPane.setVisible(false);
            statsPane.setManaged(false);
        }
    }

    @FXML
    private void showActivityLog() {
        if (userTablePane != null) {
            userTablePane.setVisible(false);
            userTablePane.setManaged(false);
        }
        if (activityLogPane != null) {
            activityLogPane.setVisible(true);
            activityLogPane.setManaged(true);
        }
        if (statsPane != null) {
            statsPane.setVisible(false);
            statsPane.setManaged(false);
        }
        loadActivityLogs();
    }

    @FXML
    private void showStatsPane() {
        if (userTablePane != null) {
            userTablePane.setVisible(false);
            userTablePane.setManaged(false);
        }
        if (activityLogPane != null) {
            activityLogPane.setVisible(false);
            activityLogPane.setManaged(false);
        }
        if (statsPane != null) {
            statsPane.setVisible(true);
            statsPane.setManaged(true);
        }
        loadUserStats();
    }

    private void loadUserStats() {
        // Query DB for user registrations grouped by date
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Users Joined");
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DATE(created_at) as reg_date, COUNT(*) as count FROM users GROUP BY reg_date ORDER BY reg_date ASC")) {
            while (rs.next()) {
                String date = rs.getString("reg_date");
                int count = rs.getInt("count");
                series.getData().add(new XYChart.Data<>(date, count));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        userLineChart.getData().clear();
        userLineChart.getData().add(series);
    }

    private void loadActivityLogs() {
        List<UserActivityLog> logs = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM user_activity_log ORDER BY timestamp DESC LIMIT 100")) {
            while (rs.next()) {
                logs.add(new UserActivityLog(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("action"),
                    rs.getString("details"),
                    rs.getTimestamp("timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (activityLogTable != null) {
            activityLogTable.getItems().setAll(logs);
        }
    }

    private void setupActivityLogButton() {
        activityLogBtn.setOnAction(e -> showActivityLog());
    }
}
