package main;

import javafx.scene.control.Button;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * LogoutHelper class provides consistent logout functionality across all pages.
 */
public class LogoutHelper {
    
    /**
     * Creates a logout button with consistent styling and functionality.
     * @param primaryStage The primary stage for navigation
     * @param databaseHelper The database helper instance
     * @return A styled logout button
     */
    public static Button createLogoutButton(Stage primaryStage, DatabaseHelper databaseHelper) {
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-font-size: 12px;");
        
        logoutButton.setOnAction(e -> {
            // Navigate back to the login page
            new UserLoginPage(databaseHelper).show(primaryStage);
        });
        
        return logoutButton;
    }
}
