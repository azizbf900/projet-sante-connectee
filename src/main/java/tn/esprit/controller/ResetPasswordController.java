package tn.esprit.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.Random;
import tn.esprit.utils.MailUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ResetPasswordController {
    @FXML private TextField codeField;
    @FXML private Button verifyCodeBtn;
    @FXML private Label codeMessageLabel;
    @FXML private VBox resetFormPane;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmNewPasswordField;
    @FXML private Button resetPasswordBtn;
    @FXML private Label resetMessageLabel;

    private String sentCode;
    private String userEmail;

    public void setUserEmail(String email) {
        this.userEmail = email;
        sendCode(email);
    }

    private void sendCode(String email) {
        // Generate a simple 4-digit code
        sentCode = String.format("%04d", new java.util.Random().nextInt(10000));
        System.out.println("[DEBUG] Sending code to " + email + ": " + sentCode);

        // Send the email using MailUtil
        String subject = "Password Reset Code";
        String htmlContent = "<p>Your password reset code is: <b>" + sentCode + "</b></p>"
                + "<p>Please open the application and enter this code to reset your password.</p>";
        try {
            boolean sent = MailUtil.sendHtmlMail(email, subject, htmlContent);
            if (!sent) {
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Email Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to send reset code. Please try again.");
                    alert.showAndWait();
                });
                return;
            }
        } catch (Exception e) {
            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Email Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to send reset code. Please check your email or try again later.");
                alert.showAndWait();
            });
            e.printStackTrace();
            return;
        }

        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Email Sent");
            alert.setHeaderText(null);
            alert.setContentText("A 4-digit code was sent to your email. Please check your inbox.");
            alert.showAndWait();
            // After user clicks OK, show the code entry form (if hidden)
            if (resetFormPane != null) resetFormPane.setVisible(true);
            if (codeField != null) codeField.clear();
        });
    }

    @FXML
    public void initialize() {
        // Restrict codeField to 4 digits only
        codeField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() > 4) {
                codeField.setText(newText.substring(0, 4));
            } else if (!newText.matches("\\d*")) {
                codeField.setText(newText.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    private void handleVerifyCode() {
        String enteredCode = codeField.getText().trim();
        if (!enteredCode.equals(sentCode)) {
            codeMessageLabel.setText("Invalid code. Please try again.");
            return;
        }
        codeMessageLabel.setText("");
        if (resetFormPane != null) resetFormPane.setVisible(true);
        // Optionally, disable codeField and verifyCodeBtn after successful verification
        codeField.setDisable(true);
        verifyCodeBtn.setDisable(true);
    }

    @FXML
    private void handleResetPassword() {
        String newPass = newPasswordField.getText();
        String confirmPass = confirmNewPasswordField.getText();
        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            resetMessageLabel.setText("All fields are required!");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            resetMessageLabel.setText("Passwords do not match!");
            return;
        }
        // Hash the new password
        String hashedPassword = hashPassword(newPass);
        // Update password in database for userEmail
        try (Connection conn = tn.esprit.utils.Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE users SET password=? WHERE email=?")) {
            stmt.setString(1, hashedPassword);
            stmt.setString(2, userEmail);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                resetMessageLabel.setText("Password reset successful! Redirecting to login...");
                // Redirect to login page after a short delay
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(1300);
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/tn/esprit/view/login.fxml"));
                        javafx.scene.Parent loginRoot = loader.load();
                        javafx.stage.Stage stage = (javafx.stage.Stage) resetPasswordBtn.getScene().getWindow();
                        stage.setScene(new javafx.scene.Scene(loginRoot, 900, 600));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                resetMessageLabel.setText("Failed to update password. Please try again.");
            }
        } catch (Exception e) {
            resetMessageLabel.setText("Database error. Please try again.");
            e.printStackTrace();
        }
    }

    // Password hashing (same as in LoginController/SignupController)
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
}
