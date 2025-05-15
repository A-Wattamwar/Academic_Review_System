package main;

import databasePart1.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * FirstPage class represents the initial screen for the first user.
 * It prompts the user to set up administrator access and navigate to the setup screen.
 */
public class FirstPage {

    // Reference to the DatabaseHelper for database interactions
    private final DatabaseHelper databaseHelper;

    public FirstPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the first page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
        // Background Pane with True Red to Orange-Yellow Gradient
        Pane backgroundPane = new Pane();
        backgroundPane.setStyle("-fx-background-color: linear-gradient(to bottom, #4169E1, #87CEEB);"); 
        backgroundPane.setPrefSize(800, 400);

        // Main VBox Layout (Centered Content)
        VBox layout = new VBox(20);
        layout.setPrefSize(800, 400);
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        // Label to display the welcome message for the first user
        Label userLabel = new Label("Hello!\nYou are the first person here. \nPlease select continue to setup administrator access");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-family: 'Helvetica'; -fx-font-weight: bold; -fx-text-fill: white;");

        // Updated Button with Darker Grey Styling
        Button continueButton = new Button("Continue");
        continueButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: black; "
                + "-fx-background-color: #A9A9A9; "  // Darker Grey Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #696969; "  // Dark Grey Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "  // Keeping the same button shape
                + "-fx-background-radius: 12px; ");

        continueButton.setOnAction(a -> new AdminSetupPage(databaseHelper).show(primaryStage));

        // Add components to layout
        layout.getChildren().addAll(userLabel, continueButton);

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

        Scene firstPageScene = new Scene(root, 1000, 600);
        primaryStage.setScene(firstPageScene);
        primaryStage.setTitle("First Page");
        primaryStage.show();
    }
}
