package main;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import databasePart1.*;

/**
 * The AdminSetupPage class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
public class AdminSetupPage {

    private final DatabaseHelper databaseHelper;

    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        // Background Pane with True Red to Orange-Yellow Gradient
        Pane backgroundPane = new Pane();
        backgroundPane.setStyle("-fx-background-color: linear-gradient(to bottom, #4169E1, #87CEEB);"); // True Red to Warm Orange
        backgroundPane.setPrefSize(800, 400);

        // Main VBox Layout (Centered Content)
        VBox layout = new VBox(10);
        layout.setPrefSize(800, 400);
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        Label titleLabel = new Label("First User Administrator Setup");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Helvetica'; -fx-text-fill: white;");

        // Input fields for userName, password, full name, and email
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin userName");
        userNameField.setMaxWidth(250);
        userNameField.setStyle("-fx-font-family: 'Helvetica'; -fx-background-color: white; -fx-border-color: #696969;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        passwordField.setStyle("-fx-font-family: 'Helvetica'; -fx-background-color: white; -fx-border-color: #696969;");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Enter Full Name");
        fullNameField.setMaxWidth(250);
        fullNameField.setStyle("-fx-font-family: 'Helvetica'; -fx-background-color: white; -fx-border-color: #696969;");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email");
        emailField.setMaxWidth(250);
        emailField.setStyle("-fx-font-family: 'Helvetica'; -fx-background-color: white; -fx-border-color: #696969;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px");

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
            // Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String fullName = fullNameField.getText();
            String email = emailField.getText();
            try {
                // Validate input using static methods from UserAccountValidator.
                String errMessage = UserAccountValidator.UserNameRecognizer.checkForValidUserName(userName) + "\n";
                errMessage += UserAccountValidator.PasswordEvaluator.evaluatePassword(password) + "\n";
                errMessage += UserAccountValidator.UserProfileValidator.validateEmail(email) + "\n";
                errMessage += UserAccountValidator.UserProfileValidator.validateFullName(fullName) + "\n";

                // Check if any error messages exist.
                if (!errMessage.trim().isEmpty()) {
                    // If there are errors, display them on the screen.
                    errorLabel.setText(errMessage);
                } else {
                    // If no errors, register the admin user.
                    User user = new User(userName, password, fullName, email, new String[]{"admin"});
                    databaseHelper.register(user);
                    System.out.println("Administrator setup completed.");

                    // Navigate to the User Login Page only after successful registration.
                    new UserLoginPage(databaseHelper).show(primaryStage);
                }
            } catch (IllegalArgumentException e) {
                errorLabel.setText(e.getMessage());
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        layout.getChildren().addAll(
                titleLabel,
                userNameField,
                fullNameField,
                emailField,
                passwordField,
                setupButton,
                errorLabel
        );

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
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
