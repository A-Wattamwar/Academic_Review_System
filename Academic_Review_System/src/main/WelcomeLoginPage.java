package main;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import databasePart1.*;

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or
 * quit the application.
 */
public class WelcomeLoginPage {

	private final DatabaseHelper databaseHelper;

	public WelcomeLoginPage(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	public void show(Stage primaryStage, User user) {
		// Background Pane with Blue Gradient
        Pane backgroundPane = new Pane();
        backgroundPane.setStyle("-fx-background-color: linear-gradient(to bottom, #4169E1, #87CEEB);"); 
        backgroundPane.setPrefSize(800, 400);

        // Main VBox Layout (Centered Content)
        VBox layout = new VBox(10);
        layout.setPrefSize(800, 400);
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        Label welcomeLabel = new Label("Welcome!!");
        welcomeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        // "Invite" button for admin to generate invitation codes
        if (user.hasRole("admin")) {
            Button inviteButton = new Button("Invite");
            inviteButton.setStyle("-fx-font-size: 13px; "
                    + "-fx-text-fill: white; "
                    + "-fx-background-color: #1a4b78; " // Dark Blue Background
                    + "-fx-padding: 6px 12px; "
                    + "-fx-border-color: #4169E1; " // Royal Blue Border
                    + "-fx-border-width: 2px; "
                    + "-fx-border-radius: 12px; "
                    + "-fx-background-radius: 12px;");
            inviteButton.setOnAction(a -> {
                new InvitationPage().show(databaseHelper, primaryStage, user);
            });
            layout.getChildren().add(inviteButton);
        }

        // Button to navigate to the user's respective page based on their role
        Button continueButton = new Button("Continue to your Page");
        continueButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: #1a4b78; " // Dark Blue Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #4169E1; " // Royal Blue Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");
        continueButton.setOnAction(a -> {
            String[] roles = user.getRoles();
            if (roles.length == 1) {
                // If user has only one role, go directly to that role's page
                new RoleSelectionPage(user, databaseHelper).navigateToRolePage(primaryStage, roles[0]);
            } else {
                // If user has multiple roles, show role selection page
                new RoleSelectionPage(user, databaseHelper).show(primaryStage);
            }
        });

        // Button to quit the application
        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: #1a4b78; " // Dark Blue Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #4169E1; " // Royal Blue Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");
        quitButton.setOnAction(a -> {
            databaseHelper.closeConnection();
            Platform.exit(); // Exit the JavaFX application
        });

        // Logout button
        Button logoutButton = LogoutHelper.createLogoutButton(primaryStage, databaseHelper);
        logoutButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: #1a4b78; " // Dark Blue Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #4169E1; " // Royal Blue Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");

        // Add all elements to layout
        layout.getChildren().addAll(welcomeLabel, continueButton, logoutButton, quitButton);

        // StackPane to layer background and content
        StackPane root = new StackPane();
        root.getChildren().addAll(backgroundPane, layout);

        // Mouse-responsive gradient effect (Blue Gradient)
        root.setOnMouseMoved(event -> {
            double mouseX = event.getSceneX() / root.getWidth();
            double mouseY = event.getSceneY() / root.getHeight();

            int red1 = (int) (25 + (20 * mouseX));
            int green1 = (int) (75 + (20 * mouseX));
            int blue1 = (int) (225 - (20 * mouseY));

            int red2 = (int) (135 + (30 * mouseY));
            int green2 = (int) (206 + (20 * mouseY));
            int blue2 = (int) (235 - (20 * mouseX));

            backgroundPane.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom, rgb(%d,%d,%d), rgb(%d,%d,%d));",
                red1, green1, blue1, red2, green2, blue2
            ));
        });

        Scene welcomeScene = new Scene(root, 1000, 600);
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("Welcome");
        primaryStage.show();
	}
}
