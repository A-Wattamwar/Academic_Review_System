package main;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import databasePart1.*;

/**
 * The UserLoginPage class provides a login interface for users to access their accounts.
 * It validates the user's credentials and navigates to the appropriate page upon successful login.
 */
public class UserLoginPage {
    
    private final DatabaseHelper databaseHelper;

    public UserLoginPage(DatabaseHelper databaseHelper) {
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

        // Title label for the login page
        Label titleLabel = new Label("User Login");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Input field for the user's userName, password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);
        userNameField.setStyle("-fx-font-family: 'Helvetica'; -fx-background-color: white; -fx-border-color: #696969;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        passwordField.setStyle("-fx-font-family: 'Helvetica'; -fx-background-color: white; -fx-border-color: #696969;");

        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        // Updated Button with Blue Styling
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: #1a4b78; " // Dark Blue Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #4169E1; " // Royal Blue Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");

        loginButton.setOnAction(a -> {
            // Retrieve user inputs
            String userName = userNameField.getText();
            String password = passwordField.getText();

            if (userName.length() < 4) {
                errorLabel.setText("Username too short");
            }
            else if (userName.length() > 16) {
                errorLabel.setText("Username too long");
            }
            else if (password.length() < 8) {
                errorLabel.setText("Password too short");
            }
            else {
                try {                    
                    if (databaseHelper.doesUserExist(userName)) {
                        String[] roles = databaseHelper.getUserRoles(userName);

                        if (roles.length > 0) {
                            User user = new User(userName, password, "", "", roles);
                            if (databaseHelper.login(user)) {
                                // User is admin
                                if (user.hasRole("admin")) {
                                    new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                                } 
                                // User has multiple roles
                                else if (roles.length > 1) {
                                    new RoleSelectionPage(user, databaseHelper).show(primaryStage);
                                } 
                                // User has single role
                                else {
                                    new RoleSelectionPage(user, databaseHelper).navigateToRolePage(primaryStage, roles[0]);
                                }
                            } else {
                                errorLabel.setText("Error logging in, make sure your username/password is correct");
                            }
                        }
                        else {
                            // Display an error if the account has no roles
                            errorLabel.setText("A user must have at least one role");
                        }
                    } else {
                        // Display an error if the account does not exist
                        errorLabel.setText("User account doesn't exist");
                    }                
                } catch (SQLException e) {
                    System.err.println("Database error: " + e.getMessage());
                    e.printStackTrace();
                } 
            }
        });

        // Back button after the title label
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

        // Reset Password Link
        Hyperlink resetPasswordLink = new Hyperlink("Reset Password");
        resetPasswordLink.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        resetPasswordLink.setOnAction(e -> {
            new ResetPasswordPage(databaseHelper).show(primaryStage);
        });

        // Add all elements to layout
        layout.getChildren().addAll(titleLabel, backButton, userNameField, passwordField, loginButton, 
                                    resetPasswordLink, errorLabel);

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

        Scene loginScene = new Scene(root, 1000, 600);
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }
}
