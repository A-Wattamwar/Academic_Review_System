package main;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import databasePart1.*;

/**
 * The SetupLoginSelectionPage class allows users to choose between setting up a new account
 * or logging into an existing account. It provides two buttons for navigation to the respective pages.
 */
public class SetupLoginSelectionPage {

    private final DatabaseHelper databaseHelper;

    public SetupLoginSelectionPage(DatabaseHelper databaseHelper) {
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

        // Instruction label prompting user action
        Label instructionLabel = new Label("Click 'Setup' to create an account,'Login' to sign in, or 'Exit' to quit.");
        instructionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");

        // Updated Button with Blue Styling
        Button setupButton = new Button("SetUp");
        setupButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: #1a4b78; " // Dark Blue Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #4169E1; " // Royal Blue Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");
        setupButton.setOnAction(a -> {
            new SetupAccountPage(databaseHelper).show(primaryStage);
        });

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
            new UserLoginPage(databaseHelper).show(primaryStage);
        });

        // Exit Button to quit the program
        Button exitButton = new Button("Exit");
        exitButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: #1a4b78; " // Dark Blue Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #4169E1; " // Royal Blue Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");
        exitButton.setOnAction(e -> {
            primaryStage.close();
        });

        layout.getChildren().addAll(instructionLabel, setupButton, loginButton, exitButton);

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

        Scene setupSelectionScene = new Scene(root, 1000, 600);
        primaryStage.setScene(setupSelectionScene);
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}
