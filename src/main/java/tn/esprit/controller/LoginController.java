package tn.esprit.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.User;
import tn.esprit.utils.Database;
import tn.esprit.utils.MailUtil;
import tn.esprit.utils.UserActivityLogger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.UUID;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Hyperlink signupLink;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private ImageView illustrationImage;
    @FXML private HBox mainContainer;
    @FXML private StackPane formStack;
    @FXML private VBox signInPane;
    @FXML private StackPane overlayPane;
    @FXML private VBox overlayLeft;
    @FXML private VBox overlayRight;
    @FXML private Button showSignUpBtn;
    @FXML private Button showSignUpBtnOverlay;
    @FXML private Button showSignInBtnOverlay;
    // --- SIGN-UP FIELDS (must match FXML exactly) ---
    @FXML private TextField signupNameField;
    @FXML private TextField signupEmailField;
    @FXML private PasswordField signupPasswordField;
    @FXML private PasswordField signupConfirmPasswordField;
    @FXML private Button signupBtn;
    @FXML private VBox signUpPane;
    @FXML private Button showSignInBtn;
    @FXML private Label signupMessageLabel;
    private boolean rightPanelActive = false;

    @FXML
    private void initialize() {
        // Animate the image (fade in/out)
        if (illustrationImage != null) {
            FadeTransition fade = new FadeTransition(Duration.seconds(2), illustrationImage);
            fade.setFromValue(0.3);
            fade.setToValue(1.0);
            fade.setCycleCount(FadeTransition.INDEFINITE);
            fade.setAutoReverse(true);
            fade.play();
        }
        // Initial state
        showSignUpPanel(false);
        // Overlay buttons
        if (showSignUpBtn != null) showSignUpBtn.setOnAction(e -> showSignUpPanel(true));
        if (showSignInBtn != null) showSignInBtn.setOnAction(e -> showSignUpPanel(false));
        if (showSignUpBtnOverlay != null) showSignUpBtnOverlay.setOnAction(e -> showSignUpPanelTrue());
        if (showSignInBtnOverlay != null) showSignInBtnOverlay.setOnAction(e -> showSignUpPanel(false));
        if (forgotPasswordLink != null) {
            forgotPasswordLink.setOnAction(e -> handleForgotPassword());
        }
        if (signupBtn != null) signupBtn.setOnAction(e -> handleSignup());
    }

    private void showForgotPasswordDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mot de passe oublié");
        dialog.setHeaderText("Entrez votre email pour réinitialiser votre mot de passe");
        dialog.setContentText("Email :");
        dialog.showAndWait().ifPresent(email -> {
            if (!isValidEmail(email)) {
                showAlert(Alert.AlertType.ERROR, "Format d'email invalide.");
                return;
            }
            // Check if email exists in DB
            boolean exists = false;
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE email=?")) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    exists = true;
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur de base de données.");
                ex.printStackTrace();
                return;
            }
            if (!exists) {
                showAlert(Alert.AlertType.ERROR, "Aucun compte trouvé pour cette adresse email.");
                return;
            }
            // Generate reset token (for demo, just UUID)
            String token = UUID.randomUUID().toString();

            // Save token to DB (lookup user_id from email)
            try (Connection conn = Database.getConnection()) {
                int userId = -1;
                try (PreparedStatement userStmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
                    userStmt.setString(1, email);
                    ResultSet rs = userStmt.executeQuery();
                    if (rs.next()) {
                        userId = rs.getInt("id");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Aucun compte trouvé pour cette adresse email.");
                        return;
                    }
                }
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO password_reset_tokens (user_id, token, expires_at) VALUES (?, ?, NOW() + INTERVAL 1 HOUR)")) {
                    stmt.setInt(1, userId);
                    stmt.setString(2, token);
                    stmt.executeUpdate();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Échec de l'enregistrement du jeton de réinitialisation.");
                return;
            }

            // Send email
            boolean sent = sendResetEmail(email, token);
            if (sent) {
                showAlert(Alert.AlertType.INFORMATION, "Un code de réinitialisation a été envoyé à votre email.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec de l'envoi de l'email de réinitialisation.");
            }
        });
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(emailRegex);
    }

    private boolean sendResetEmail(String email, String token) {
        String subject = "Réinitialisation du mot de passe";
        String htmlContent = "<p>Votre code de réinitialisation est : <b>" + token + "</b></p>"
                + "<p>Veuillez ouvrir l'application et entrer ce code pour réinitialiser votre mot de passe. Si vous n'avez pas demandé de réinitialisation, vous pouvez ignorer cet email.</p>";
        return MailUtil.sendHtmlMail(email, subject, htmlContent);
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Erreur" : "Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.trim().isEmpty()) {
            messageLabel.setText("Nom d'utilisateur ou email requis !");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            messageLabel.setText("Mot de passe requis !");
            return;
        }

        // Hash the entered password before checking the DB
        String hashedPassword = hashPassword(password);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE (username=? OR email=?) AND password=?")) {
            stmt.setString(1, username);
            stmt.setString(2, username);
            stmt.setString(3, hashedPassword);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getTimestamp("created_at")
                );
                // Log user connection
                UserActivityLogger.logActivity(user.getId(), "connected", "User logged in");
                if ("admin".equalsIgnoreCase(username)) {
                    // Show dashboard for admin
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/dashboard.fxml"));
                    Parent dashboardRoot = loader.load();
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.setScene(new Scene(dashboardRoot, 1000, 700));
                    stage.setTitle("Tableau de bord Admin");
                } else {
                    // Show profile page for normal users
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/ProfilePage.fxml"));
                    Parent profileRoot = loader.load();
                    tn.esprit.controller.ProfilePageController profileController = loader.getController();
                    profileController.setUser(user);
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.setScene(new Scene(profileRoot, 1200, 900));
                    stage.setTitle("Profil Utilisateur");
                }
            } else {
                messageLabel.setText("Identifiants invalides.");
            }
        } catch (Exception e) {
            messageLabel.setText("Erreur de base de données.");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/signup.fxml"));
            Parent signupRoot = loader.load();
            Stage stage = (Stage) signupLink.getScene().getWindow();
            stage.setScene(new Scene(signupRoot, 900, 600));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSignUpPanel(boolean showSignUp) {
        if (showSignUp) {
            animatePane(signInPane, false);
            animatePane(signUpPane, true);
            animatePane(overlayRight, false);
            animatePane(overlayLeft, true);
        } else {
            animatePane(signInPane, true);
            animatePane(signUpPane, false);
            animatePane(overlayRight, true);
            animatePane(overlayLeft, false);
        }
        rightPanelActive = showSignUp;
    }

    @FXML
    private void showSignUpPanelTrue() {
        showSignUpPanel(true);
    }

    private void animatePane(javafx.scene.layout.Pane pane, boolean show) {
        if (show) {
            pane.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), pane);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } else {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), pane);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> pane.setVisible(false));
            fadeOut.play();
        }
    }

    @FXML
    private void handleSignup() {
        String username = signupNameField.getText().trim();
        String email = signupEmailField.getText().trim();
        String password = signupPasswordField.getText();
        String confirmPassword = signupConfirmPasswordField.getText();

        // Validate input
        if (username.isEmpty()) {
            signupMessageLabel.setText("Nom d'utilisateur requis !");
            return;
        }
        if (email.isEmpty()) {
            signupMessageLabel.setText("Email requis !");
            return;
        }
        if (password.isEmpty()) {
            signupMessageLabel.setText("Mot de passe requis !");
            return;
        }
        if (confirmPassword.isEmpty()) {
            signupMessageLabel.setText("Confirmer le mot de passe requis !");
            return;
        }
        if (!isValidEmail(email)) {
            signupMessageLabel.setText("Format d'email invalide !");
            return;
        }
        if (username.length() < 3) {
            signupMessageLabel.setText("Le nom d'utilisateur doit contenir au moins 3 caractères !");
            return;
        }
        if (!password.equals(confirmPassword)) {
            signupMessageLabel.setText("Les mots de passe ne correspondent pas !");
            return;
        }

        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            signupMessageLabel.setText("Erreur de hachage du mot de passe.");
            return;
        }

        // Check if username or email already exists
        try (Connection conn = tn.esprit.utils.Database.getConnection()) {
            try (PreparedStatement checkUser = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
                checkUser.setString(1, username);
                ResultSet rs = checkUser.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    signupMessageLabel.setText("Nom d'utilisateur déjà existant !");
                    return;
                }
            }
            try (PreparedStatement checkEmail = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE email = ?")) {
                checkEmail.setString(1, email);
                ResultSet rs = checkEmail.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    signupMessageLabel.setText("Email déjà existant !");
                    return;
                }
            }
            // Save to DB
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO users (username, email, password) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, username);
                stmt.setString(2, email);
                stmt.setString(3, hashedPassword);
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    int userId = -1;
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getInt(1);
                    }
                    // Log user added
                    if (userId != -1) {
                        UserActivityLogger.logActivity(userId, "added", "User registered: " + email);
                    }
                    signupMessageLabel.setText("Inscription réussie ! Vous pouvez maintenant vous connecter.");
                    showSignUpPanel(false); // Move to Sign In
                }
            }
        } catch (Exception e) {
            signupMessageLabel.setText("Échec de l'inscription : " + e.getMessage());
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

    @FXML
    private void handleForgotPassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mot de passe oublié");
        dialog.setHeaderText("Entrez votre email pour réinitialiser votre mot de passe");
        dialog.setContentText("Email :");
        dialog.showAndWait().ifPresent(email -> {
            if (!isValidEmail(email)) {
                showAlert(Alert.AlertType.ERROR, "Format d'email invalide.");
                return;
            }
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/reset_code.fxml"));
                    Parent root = loader.load();
                    ResetPasswordController controller = loader.getController();
                    controller.setUserEmail(email);
                    Stage stage = new Stage();
                    stage.setTitle("Réinitialisation du mot de passe");
                    stage.setScene(new Scene(root));
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.show();
                    // Log password reset request
                    UserActivityLogger.logActivity(userId, "reset password requested", "Reset password requested for: " + email);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Aucun utilisateur trouvé avec cet email.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void logout(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/login.fxml"));
            Parent loginRoot = loader.load();
            stage.setScene(new Scene(loginRoot, 900, 600));
            stage.setTitle("Connexion");
            stage.setResizable(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
