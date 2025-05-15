package main;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * This page displays a simple welcome message for the user.
 */

public class UserHomePage {
    private final DatabaseHelper databaseHelper;
    private final String currentUsername;

    public UserHomePage(DatabaseHelper databaseHelper, String currentUsername) {
        this.databaseHelper = databaseHelper;
        this.currentUsername = currentUsername;
    }

    public void show(Stage primaryStage) {
        // Background Pane with True Red to Orange-Yellow Gradient
        Pane backgroundPane = new Pane();
        backgroundPane.setStyle("-fx-background-color: linear-gradient(to bottom, #4169E1, #87CEEB);"); 
        backgroundPane.setPrefSize(800, 400);

        // Main VBox Layout (Centered Content)
        VBox layout = new VBox();
        layout.setPrefSize(800, 400);
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        // Label to display Hello user
        Label userLabel = new Label("Hello, User!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        layout.getChildren().add(userLabel);

        // Add Reviewer Permissions button for students
        if (currentUsername != null && !currentUsername.isEmpty()) {
            Button reviewerButton = new Button("Reviewer Permissions");
            reviewerButton.setStyle("-fx-font-size: 13px; -fx-text-fill: white; -fx-background-color: #1a4b78; "
                    + "-fx-padding: 6px 12px; -fx-border-color: #4169E1; -fx-border-width: 2px; "
                    + "-fx-border-radius: 12px; -fx-background-radius: 12px;");
            reviewerButton.setOnAction(e -> {
                ReviewerPermissionsPage reviewerPage = new ReviewerPermissionsPage(databaseHelper, currentUsername, false);
                Stage stage = new Stage();
                reviewerPage.show(stage);
            });
            layout.getChildren().add(reviewerButton);
        }

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

        Scene userScene = new Scene(root, 1000, 600);

        // Set the scene to primary stage
        primaryStage.setScene(userScene);
        primaryStage.setTitle("User Page");
        primaryStage.show();
    }
}
