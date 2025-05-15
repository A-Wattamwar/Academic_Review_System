package main;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import databasePart1.*;

/**
 * SetupAccountPage class handles the account setup process for new users.
 * Users provide their userName, password, and a valid invitation code to register.
 */
public class SetupAccountPage {

    private final DatabaseHelper databaseHelper;

    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the Setup Account page in the provided stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
        // Background Pane with True Red to Orange-Yellow Gradient
        Pane backgroundPane = new Pane();
        backgroundPane.setStyle("-fx-background-color: linear-gradient(to bottom, #4169E1, #87CEEB);"); // True Red to Warm Orange
        backgroundPane.setPrefSize(800, 500);

        // Main VBox Layout (Centered Content)
        VBox layout = new VBox(10);
        layout.setPrefSize(800, 500);
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        // Title label.
        Label titleLabel = new Label("Account Setup");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Input fields for userName, password, invitation code, full name, and email.
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);
        userNameField.setStyle("-fx-font-family: 'Helvetica'; -fx-background-color: white; -fx-border-color: #696969;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        passwordField.setStyle("-fx-font-family: 'Helvetica'; -fx-background-color: white; -fx-border-color: #696969;");

        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter Invitation Code");
        inviteCodeField.setMaxWidth(250);
        inviteCodeField.setStyle("-fx-font-family: 'Helvetica'; -fx-background-color: white; -fx-border-color: #696969;");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Enter Full Name");
        fullNameField.setMaxWidth(250);
        fullNameField.setStyle("-fx-font-family: 'Helvetica'; -fx-background-color: white; -fx-border-color: #696969;");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email");
        emailField.setMaxWidth(250);
        emailField.setStyle("-fx-font-family: 'Helvetica'; -fx-background-color: white; -fx-border-color: #696969;");

        // Label to display error messages for invalid input or registration issues.
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        // Updated Button with Blue Styling
        Button setupButton = new Button("Setup");
        setupButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: #1a4b78; " // Dark Blue Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #4169E1; " // Royal Blue Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");

        setupButton.setOnAction(a -> {
            // Retrieve user input.
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String code = inviteCodeField.getText();
            String fullName = fullNameField.getText();
            String email = emailField.getText();

            try {
                // Check if the user already exists.
                if (!databaseHelper.doesUserExist(userName)) {
                    // Validate the input using static methods from UserAccountValidator.
                    String errMessage = UserAccountValidator.UserNameRecognizer.checkForValidUserName(userName) + "\n"
                                      + UserAccountValidator.PasswordEvaluator.evaluatePassword(password) + "\n"
                                      + UserAccountValidator.UserProfileValidator.validateEmail(email) + "\n"
                                      + UserAccountValidator.UserProfileValidator.validateFullName(fullName) + "\n";

                    if (!errMessage.trim().isEmpty()) {
                        errorLabel.setText(errMessage);
                    } else {
                        // Validate the invitation code.
                        if (databaseHelper.validateInvitationCode(code)) {
                            String[] availableRoles = databaseHelper.getInvitationRoles(code);
                            if (availableRoles.length > 0) {
                                // Create a new user and register them in the database.
                                User user = new User(userName, password, fullName, email, availableRoles);
                                databaseHelper.register(user);

                                // Navigate to the Welcome Login Page.
                                new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                            } else {
                                errorLabel.setText("Invalid invitation code: No roles available");
                            }
                        } else {
                            errorLabel.setText("Please enter a valid invitation code");
                        }
                    }
                } else {
                    errorLabel.setText("This username is taken! Please use another to set up an account.");
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Back button.
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: #1a4b78; " // Dark Blue Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #4169E1; " // Royal Blue Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");
        backButton.setOnAction(e -> {
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });

        layout.getChildren().addAll(titleLabel, backButton, userNameField, fullNameField, emailField, 
                                    passwordField, inviteCodeField, setupButton, errorLabel);

        // StackPane to layer background and content
        StackPane root = new StackPane();
        root.getChildren().addAll(backgroundPane, layout);

        // Mouse-responsive gradient effect (Blue Gradient)
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

        Scene setupScene = new Scene(root, 1000, 600);
        primaryStage.setScene(setupScene);
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}
