package main;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * The RoleSelectionPage class allows users with multiple roles to select which role they want to play.
 * After selection, users are directed to the appropriate role-specific home page.
 */
public class RoleSelectionPage {
    private final User user;
    private final DatabaseHelper databaseHelper;

    public RoleSelectionPage(User user, DatabaseHelper databaseHelper) {
        this.user = user;
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

        // Title label
        Label titleLabel = new Label("Select Role");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: black;");

        // Role selection ComboBox
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(user.getRoles());
        roleComboBox.setPromptText("Select a role");
        roleComboBox.setMaxWidth(250);
        roleComboBox.setStyle("-fx-font-family: 'Helvetica'; -fx-background-color: white; -fx-border-color: #696969;");

        // Updated Button with Ash-Themed Styling
        Button continueButton = new Button("Continue");
        continueButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: black; "
                + "-fx-background-color: #A9A9A9; " // Dark Grey Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #696969; " // Dark Grey Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");
        
        continueButton.setOnAction(e -> {
            String selectedRole = roleComboBox.getValue();
            if (selectedRole != null) {
                navigateToRolePage(primaryStage, selectedRole);
            }
        });

        layout.getChildren().addAll(titleLabel, roleComboBox, continueButton);

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

        Scene roleSelectionScene = new Scene(root, 1000, 600);
        primaryStage.setScene(roleSelectionScene);
        primaryStage.setTitle("Role Selection");
        primaryStage.show();
    }

    public void navigateToRolePage(Stage primaryStage, String role) {
        switch (role.toLowerCase()) {
            case "admin":
                new AdminHomePage(databaseHelper, user.getUserName()).show(primaryStage);
                break;
            case "student":
                new StudentHomePage(databaseHelper, user.getUserName()).show(primaryStage);
                break;
            case "instructor":
                new InstructorHomePage(databaseHelper, user.getUserName()).show(primaryStage);
                break;
            case "staff":
                new StaffHomePage(databaseHelper).show(primaryStage);
                break;
            case "reviewer":
                new ReviewerHomePage(databaseHelper, user.getUserName()).show(primaryStage);
                break;
        }
    }
}
