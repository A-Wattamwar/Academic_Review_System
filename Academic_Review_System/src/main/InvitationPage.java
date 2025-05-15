package main;

import databasePart1.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * InvitationPage class represents the page where an admin can generate an invitation code.
 * The invitation code is displayed upon clicking a button.
 */

public class InvitationPage {

    /**
     * Displays the Invite Page in the provided primary stage.
     * 
     * @param databaseHelper An instance of DatabaseHelper to handle database operations.
     * @param primaryStage   The primary stage where the scene will be displayed.
     * @param user           The user object to pass to the WelcomeLoginPage.
     */
    public void show(DatabaseHelper databaseHelper, Stage primaryStage, User user) {
        // Background Pane with Blue Gradient
        Pane backgroundPane = new Pane();
        backgroundPane.setStyle("-fx-background-color: linear-gradient(to bottom, #4169E1, #87CEEB);"); 
        backgroundPane.setPrefSize(800, 400);

        // Main VBox Layout (Centered Content)
        VBox layout = new VBox(10);
        layout.setPrefSize(800, 400);
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        // Label to display the title of the page
        Label titleLabel = new Label("Generate Invitation Code");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Back button to return to welcome page
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
            new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
        });

        // Role selection checkboxes
        VBox rolesBox = new VBox(5);
        rolesBox.setStyle("-fx-padding: 10;");
        Label rolesLabel = new Label("Select Roles:");
        rolesLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        CheckBox adminCheck = new CheckBox("Admin");
        CheckBox studentCheck = new CheckBox("Student");
        CheckBox instructorCheck = new CheckBox("Instructor");
        CheckBox staffCheck = new CheckBox("Staff");
        CheckBox reviewerCheck = new CheckBox("Reviewer");

        // Style checkboxes
        String checkboxStyle = "-fx-text-fill: white; -fx-font-size: 13px;";
        adminCheck.setStyle(checkboxStyle);
        studentCheck.setStyle(checkboxStyle);
        instructorCheck.setStyle(checkboxStyle);
        staffCheck.setStyle(checkboxStyle);
        reviewerCheck.setStyle(checkboxStyle);

        rolesBox.getChildren().addAll(rolesLabel, adminCheck, studentCheck, instructorCheck, staffCheck, reviewerCheck);

        // Deadline selection
        Label deadlineLabel = new Label("Select Deadline (days):");
        deadlineLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        ComboBox<Integer> deadlineDays = new ComboBox<>();
        deadlineDays.getItems().addAll(1, 3, 7, 14, 30);
        deadlineDays.setValue(7); // Default 7 days
        deadlineDays.setStyle("-fx-background-color: white; -fx-border-color: #4169E1; -fx-font-family: 'Helvetica';");

        // Button to generate the invitation code
        Button showCodeButton = new Button("Generate Invitation Code");
        showCodeButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: #1a4b78; " // Dark Blue Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #4169E1; " // Royal Blue Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");

        // Label to display the generated invitation code
        Label inviteCodeLabel = new Label("");
        inviteCodeLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic; -fx-text-fill: white;");
        
        showCodeButton.setOnAction(e -> {
            // Collect selected roles
            java.util.List<String> selectedRoles = new java.util.ArrayList<>();
            if (adminCheck.isSelected()) selectedRoles.add("admin");
            if (studentCheck.isSelected()) selectedRoles.add("student");
            if (instructorCheck.isSelected()) selectedRoles.add("instructor");
            if (staffCheck.isSelected()) selectedRoles.add("staff");
            if (reviewerCheck.isSelected()) selectedRoles.add("reviewer");

            if (selectedRoles.isEmpty()) {
                inviteCodeLabel.setText("Please select at least one role");
                return;
            }

            // Calculate deadline
            java.sql.Timestamp deadline = new java.sql.Timestamp(
                System.currentTimeMillis() + (deadlineDays.getValue() * 24L * 60L * 60L * 1000L)
            );

            // Generate the code
            String code = databaseHelper.generateInvitationCode(
                selectedRoles.toArray(new String[0]),
                deadline
            );
            inviteCodeLabel.setText("Invitation Code: " + code + "\nExpires in " + deadlineDays.getValue() + " days");
        });

        layout.getChildren().addAll(titleLabel, backButton, rolesBox, deadlineLabel, deadlineDays, showCodeButton, inviteCodeLabel);

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

        Scene inviteScene = new Scene(root, 1000, 600);
        primaryStage.setScene(inviteScene);
        primaryStage.setTitle("Generate Invitation");
    }
}
