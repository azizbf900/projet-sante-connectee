package tn.esprit.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import tn.esprit.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import tn.esprit.utils.Database;
import tn.esprit.utils.UserActivityLogger;

public class ProfilePageController {
    @FXML
    private Label usernameLabel;
    @FXML
    private Label emailLabel;

    private User loggedUser;

    public void setUser(User user) {
        this.loggedUser = user;
        usernameLabel.setText(user.getUsername());
        emailLabel.setText(user.getEmail());
    }

    @FXML
    private void handleChatbotButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/chatbot.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogoutButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateButton(ActionEvent event) {
        if (loggedUser == null) return;
        TextInputDialog usernameDialog = new TextInputDialog(loggedUser.getUsername());
        usernameDialog.setTitle("Mettre à jour le profil");
        usernameDialog.setHeaderText("Modifier le nom d'utilisateur");
        usernameDialog.setContentText("Nom d'utilisateur:");
        Optional<String> usernameResult = usernameDialog.showAndWait();
        if (!usernameResult.isPresent()) return;
        String newUsername = usernameResult.get().trim();
        if (newUsername.isEmpty()) return;

        TextInputDialog emailDialog = new TextInputDialog(loggedUser.getEmail());
        emailDialog.setTitle("Mettre à jour le profil");
        emailDialog.setHeaderText("Modifier l'email");
        emailDialog.setContentText("Email:");
        Optional<String> emailResult = emailDialog.showAndWait();
        if (!emailResult.isPresent()) return;
        String newEmail = emailResult.get().trim();
        if (newEmail.isEmpty()) return;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE users SET username=?, email=? WHERE id=?")) {
            stmt.setString(1, newUsername);
            stmt.setString(2, newEmail);
            stmt.setInt(3, loggedUser.getId());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                loggedUser.setUsername(newUsername);
                loggedUser.setEmail(newEmail);
                usernameLabel.setText(newUsername);
                emailLabel.setText(newEmail);
                UserActivityLogger.logActivity(loggedUser.getId(), "updated profile", "Profile updated: " + newUsername + ", " + newEmail);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdatePasswordButton(ActionEvent event) {
        if (loggedUser == null) return;
        // Ask for current password
        TextInputDialog currentPwdDialog = new TextInputDialog();
        currentPwdDialog.setTitle("Mettre à jour le mot de passe");
        currentPwdDialog.setHeaderText("Confirmez votre mot de passe actuel");
        currentPwdDialog.setContentText("Mot de passe actuel:");
        Optional<String> currentPwdResult = currentPwdDialog.showAndWait();
        if (!currentPwdResult.isPresent()) return;
        String currentPwd = currentPwdResult.get();
        if (currentPwd.isEmpty()) return;

        // Validate current password
        String hashedCurrent = hashPassword(currentPwd);
        boolean valid = false;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT password FROM users WHERE id=?")) {
            stmt.setInt(1, loggedUser.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String dbPwd = rs.getString("password");
                if (dbPwd.equals(hashedCurrent)) valid = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!valid) {
            showAlert(AlertType.ERROR, "Mot de passe actuel incorrect.");
            return;
        }

        // Ask for new password
        TextInputDialog newPwdDialog = new TextInputDialog();
        newPwdDialog.setTitle("Mettre à jour le mot de passe");
        newPwdDialog.setHeaderText("Entrez le nouveau mot de passe");
        newPwdDialog.setContentText("Nouveau mot de passe:");
        Optional<String> newPwdResult = newPwdDialog.showAndWait();
        if (!newPwdResult.isPresent()) return;
        String newPwd = newPwdResult.get();
        if (newPwd.isEmpty()) return;

        // Confirm new password
        TextInputDialog confirmPwdDialog = new TextInputDialog();
        confirmPwdDialog.setTitle("Mettre à jour le mot de passe");
        confirmPwdDialog.setHeaderText("Confirmez le nouveau mot de passe");
        confirmPwdDialog.setContentText("Confirmez le mot de passe:");
        Optional<String> confirmPwdResult = confirmPwdDialog.showAndWait();
        if (!confirmPwdResult.isPresent()) return;
        String confirmPwd = confirmPwdResult.get();
        if (!newPwd.equals(confirmPwd)) {
            showAlert(AlertType.ERROR, "Les mots de passe ne correspondent pas.");
            return;
        }

        // Update password in DB
        String hashedNew = hashPassword(newPwd);
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE users SET password=? WHERE id=?")) {
            stmt.setString(1, hashedNew);
            stmt.setInt(2, loggedUser.getId());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert(AlertType.INFORMATION, "Mot de passe mis à jour avec succès.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showAlert(AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(type == AlertType.ERROR ? "Erreur" : "Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
