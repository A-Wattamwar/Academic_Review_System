package main;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import main.UserAccountValidator.PasswordEvaluator;
import databasePart1.DatabaseHelper;
import java.sql.SQLException;

/**
 * The ResetPasswordPage class provides an interface for users to reset their account password.
 * It takes a username and one-time password and validates the new password to reset the user's password.
 */
public class ResetPasswordPage {
    private final DatabaseHelper databaseHelper;
    private static final PasswordEvaluator passwordEvaluator = new PasswordEvaluator();

    public ResetPasswordPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        // Background Pane with True Red to Orange-Yellow Gradient
        Pane backgroundPane = new Pane();
        backgroundPane.setStyle("-fx-background-color: linear-gradient(to bottom, #4169E1, #87CEEB);"); 
        backgroundPane.setPrefSize(800, 400);

        // Main VBox Layout (Centered Content)
        VBox layout = new VBox(10);
        layout.setPrefSize(800, 400);
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        Label titleLabel = new Label("Reset Password");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: black;");

        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(250);
        userNameField.setStyle("-fx-font-family: 'Helvetica'; -fx-background-color: white; -fx-border-color: #696969;");

        PasswordField oneTimePasswordField = new PasswordField();
        oneTimePasswordField.setPromptText("Enter One-Time Password");
        oneTimePasswordField.setMaxWidth(250);
        oneTimePasswordField.setStyle("-fx-font-family: 'Helvetica'; -fx-background-color: white; -fx-border-color: #696969;");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter New Password");
        newPasswordField.setMaxWidth(250);
        newPasswordField.setStyle("-fx-font-family: 'Helvetica'; -fx-background-color: white; -fx-border-color: #696969;");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");
        confirmPasswordField.setMaxWidth(250);
        confirmPasswordField.setStyle("-fx-font-family: 'Helvetica'; -fx-background-color: white; -fx-border-color: #696969;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        // Updated Button with Ash-Themed Styling
        Button resetButton = new Button("Reset Password");
        resetButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: black; "
                + "-fx-background-color: #A9A9A9; " // Dark Grey Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #696969; " // Dark Grey Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");

        resetButton.setOnAction(e -> {
            String userName = userNameField.getText();
            String oneTimePassword = oneTimePasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            try {
                if (!databaseHelper.doesUserExist(userName)) {
                    errorLabel.setText("User does not exist");
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    errorLabel.setText("Passwords do not match");
                    return;
                }

                String errMessage = UserAccountValidator.PasswordEvaluator.evaluatePassword(newPassword);
                if (!errMessage.trim().isEmpty()) {
                    errorLabel.setText(errMessage);
                    return;
                }

                if (databaseHelper.validateOneTimePassword(userName, oneTimePassword)) {
                    // Update the new password and clear the one-time password
                    databaseHelper.updatePasswordAndClearOneTime(userName, newPassword);
                    new UserLoginPage(databaseHelper).show(primaryStage);
                } else {
                    errorLabel.setText("Invalid one-time password");
                }
            } catch (SQLException ex) {
                errorLabel.setText("Error resetting password");
                ex.printStackTrace();
            }
        });

        Button backButton = new Button("Back");
        backButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: black; "
                + "-fx-background-color: #A9A9A9; " // Dark Grey Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #696969; " // Dark Grey Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");
        backButton.setOnAction(e -> {
            new UserLoginPage(databaseHelper).show(primaryStage);
        });

        layout.getChildren().addAll(titleLabel, backButton, userNameField, oneTimePasswordField, 
                                  newPasswordField, confirmPasswordField, resetButton, errorLabel);

        // StackPane to layer background and content
        StackPane root = new StackPane();
        root.getChildren().addAll(backgroundPane, layout);

        // Mouse-responsive gradient effect (True Red-Orange without Pink Shift)
        root.setOnMouseMoved(event -> {
            double mouseX = event.getSceneX() / root.getWidth(); // Normalized X (0 to 1)
            double mouseY = event.getSceneY() / root.getHeight(); // Normalized Y (0 to 1)

            // Dynamic blue gradient based on mouse position
            int red1 = (int) (25 + (20 * mouseX));    // Darker royal blue base red component
            int green1 = (int) (75 + (20 * mouseX));  // Darker royal blue base green component
            int blue1 = (int) (225 - (20 * mouseY));  // Darker royal blue base blue component

            int red2 = (int) (135 + (30 * mouseY));   // Sky blue base red component
            int green2 = (int) (206 + (20 * mouseY)); // Sky blue base green component
            int blue2 = (int) (235 - (20 * mouseX));  // Sky blue base blue component

            backgroundPane.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom, rgb(%d,%d,%d), rgb(%d,%d,%d));",
                red1, green1, blue1, red2, green2, blue2
            ));
        });

        Scene resetScene = new Scene(root, 1000, 600);
        primaryStage.setScene(resetScene);
        primaryStage.setTitle("Reset Password");
        primaryStage.show();
    }
}
