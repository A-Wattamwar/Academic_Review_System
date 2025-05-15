package main;

import databasePart1.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;
import java.util.UUID;
import databasePart1.DatabaseHelper4;

/**
 * AdminPage class represents the user interface for the admin user.
 * This page displays a simple welcome message for the admin as well as a table of all users.
 */

public class AdminHomePage {
    private final DatabaseHelper databaseHelper;
    private final DatabaseHelper4 databaseHelper4;
    private final String currentAdminUserName;
    private AdminRequestsPage adminRequestsPage;
    private TableView<User> userTable; // Make table a class field for access in action handlers

    public AdminHomePage(DatabaseHelper databaseHelper, String currentAdminUserName) {
        this.databaseHelper = databaseHelper;
        this.currentAdminUserName = currentAdminUserName;
        this.databaseHelper4 = new DatabaseHelper4();
        try {
            this.databaseHelper4.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Fatal Error", "Could not connect to Admin Requests database.");
        }
    }

    public void show(Stage primaryStage) {
        // Background Pane with Blue Gradient
        Pane backgroundPane = new Pane();
        backgroundPane.setStyle("-fx-background-color: linear-gradient(to bottom, #4169E1, #87CEEB);"); 
        backgroundPane.setPrefSize(800, 500);

        // Main VBox Layout (Centered Content)
        VBox layout = new VBox(15);
        layout.setPrefSize(800, 550);
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        // Label to display the welcome message for the admin
        Label adminLabel = new Label("Hello, Admin!");
        adminLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        // TableView for users
        userTable = new TableView<>();
        userTable.setMaxWidth(700);
        userTable.setMaxHeight(300);
        userTable.setStyle("-fx-background-color: white; -fx-border-color: #696969;");

        // Apply row striping based on roles
        userTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (!empty && user != null) {
                    String[] roles = user.getRoles();
                    if (roles.length > 0) {
                        if (containsRole(roles, "admin")) setStyle("-fx-background-color: #FFCCCC;"); // Very Light Red
                        else if (containsRole(roles, "student")) setStyle("-fx-background-color: #FFE5CC;"); // Very Light Orange
                        else if (containsRole(roles, "reviewer")) setStyle("-fx-background-color: #CCFFCC;"); // Very Light Green
                        else if (containsRole(roles, "instructor")) setStyle("-fx-background-color: #E6CCFF;"); // Very Light Purple
                        else if (containsRole(roles, "staff")) setStyle("-fx-background-color: #FFFFCC;"); // Very Light Yellow
                        else setStyle(""); // Default
                    }
                }
            }
        });

        // Columns
        TableColumn<User, String> userNameCol = new TableColumn<>("Username");
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userNameCol.setPrefWidth(100);

        TableColumn<User, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(150);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<User, String> rolesCol = new TableColumn<>("Roles");
        rolesCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.join(", ", cellData.getValue().getRoles())));

        // Add columns to table (removed action columns)
        userTable.getColumns().addAll(userNameCol, nameCol, emailCol, rolesCol);

        // Load users
        try {
            userTable.getItems().addAll(databaseHelper.getAllUsers());
        } catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
            showError("Database Error", "Could not load user list: " + e.getMessage());
        }

        // --- Action Buttons Row ---
        HBox actionButtonsRow = new HBox(10);
        actionButtonsRow.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Manage User Button
        Button manageButton = new Button("Manage User");
        styleButton(manageButton, "#1a4b78", "#4169E1", true);
        manageButton.setOnAction(e -> handleManageUser(primaryStage));
        
        // Reset Password Button
        Button resetButton = new Button("Reset Password");
        styleButton(resetButton, "#1a4b78", "#4169E1", true);
        resetButton.setOnAction(e -> handleResetPassword(primaryStage));
        
        // Delete User Button
        Button deleteButton = new Button("Delete User");
        styleButton(deleteButton, "#1a4b78", "#4169E1", true);
        deleteButton.setOnAction(e -> handleDeleteUser(primaryStage));
        
        // Add action buttons to the row
        actionButtonsRow.getChildren().addAll(manageButton, resetButton, deleteButton);

        // --- Navigation Buttons Row --- 
        HBox navigationButtonsRow = new HBox(20);
        navigationButtonsRow.setAlignment(javafx.geometry.Pos.CENTER);
        
        // --- Add New View Admin Requests Button --- 
        Button viewAdminRequestsButton = new Button("View Admin Requests");
        styleButton(viewAdminRequestsButton, "#FFD700", "#DAA520"); // Gold/Yellow style
        viewAdminRequestsButton.setOnAction(e -> {
             if (adminRequestsPage == null) {
                adminRequestsPage = new AdminRequestsPage(databaseHelper4, databaseHelper, currentAdminUserName, primaryStage, primaryStage.getScene());
            }
            adminRequestsPage.show(); // Show the full request management page
        });
        
        // Logout button with Red Styling
        Button logoutButton = LogoutHelper.createLogoutButton(primaryStage, databaseHelper);
        styleButton(logoutButton, "#FF6B6B", "#CD5C5C", false); // Red color with black text
        
        // Add navigation buttons to the row
        navigationButtonsRow.getChildren().addAll(viewAdminRequestsButton, logoutButton);

        // Add all components to the layout
        layout.getChildren().addAll(adminLabel, userTable, actionButtonsRow, navigationButtonsRow);

        // Create a container for the back button at bottom right
        HBox backButtonContainer = new HBox();
        backButtonContainer.setAlignment(javafx.geometry.Pos.BOTTOM_RIGHT);
        backButtonContainer.setPadding(new javafx.geometry.Insets(0, 20, 20, 0)); // Add more padding to position from edges
        
        // Back button to return to WelcomeLoginPage
        Button backButton = new Button("Back");
        styleButton(backButton, "#E0E0E0", "#C0C0C0", false); // Light gray with black text
        backButton.setOnAction(e -> {
            new WelcomeLoginPage(databaseHelper).show(primaryStage, new User(currentAdminUserName, "", "", "", new String[]{"admin"}));
        });
        
        // Add back button to its container
        backButtonContainer.getChildren().add(backButton);

        // Create a VBox to hold the main layout and back button
        VBox mainContainer = new VBox();
        mainContainer.getChildren().addAll(layout, backButtonContainer);
        VBox.setVgrow(layout, javafx.scene.layout.Priority.ALWAYS);

        // Create main stack pane to layer content
        StackPane root = new StackPane();
        root.getChildren().addAll(backgroundPane, mainContainer);

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

        // Add window close handler to clean up database connections
        primaryStage.setOnCloseRequest(event -> {
            closeDatabaseConnections();
        });

        Scene adminScene = new Scene(root, 1000, 600);
        primaryStage.setScene(adminScene);
        primaryStage.setTitle("Admin Page");
        primaryStage.show();
    }

    // Handler for Manage User button
    private void handleManageUser(Stage primaryStage) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Selection Error", "Please select a user to manage.");
            return;
        }
        
        // Show user management dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage User: " + selectedUser.getUserName());
        dialog.setHeaderText("Manage user roles and permissions");
        
        // Create dialog content
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(10));
        
        // Role selection
        Label roleLabel = new Label("User Roles:");
        CheckBox adminCheck = new CheckBox("Admin");
        CheckBox studentCheck = new CheckBox("Student");
        CheckBox reviewerCheck = new CheckBox("Reviewer");
        CheckBox instructorCheck = new CheckBox("Instructor");
        CheckBox staffCheck = new CheckBox("Staff");
        
        // Set initial values based on current roles
        String[] currentRoles = selectedUser.getRoles();
        for (String role : currentRoles) {
            if (role.equalsIgnoreCase("admin")) adminCheck.setSelected(true);
            if (role.equalsIgnoreCase("student")) studentCheck.setSelected(true);
            if (role.equalsIgnoreCase("reviewer")) reviewerCheck.setSelected(true);
            if (role.equalsIgnoreCase("instructor")) instructorCheck.setSelected(true);
            if (role.equalsIgnoreCase("staff")) staffCheck.setSelected(true);
        }
        
        // Add role checkboxes to content
        content.getChildren().addAll(roleLabel, adminCheck, studentCheck, reviewerCheck, instructorCheck, staffCheck);
        
        // Add buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Set content
        dialog.getDialogPane().setContent(content);
        
        // Handle save button
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Collect selected roles
                java.util.List<String> newRoles = new java.util.ArrayList<>();
                if (adminCheck.isSelected()) newRoles.add("admin");
                if (studentCheck.isSelected()) newRoles.add("student");
                if (reviewerCheck.isSelected()) newRoles.add("reviewer");
                if (instructorCheck.isSelected()) newRoles.add("instructor");
                if (staffCheck.isSelected()) newRoles.add("staff");
                
                // Update user roles in database
                try {
                    databaseHelper.updateUserRoles(selectedUser.getUserName(), newRoles.toArray(new String[0]), currentAdminUserName);
                    showSuccess("Success", "User roles updated successfully.");
                    refreshUserTable(); // Refresh the table to show updated roles
                } catch (SQLException e) {
                    showError("Database Error", "Failed to update user roles: " + e.getMessage());
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    // Handler for Reset Password button
    private void handleResetPassword(Stage primaryStage) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Selection Error", "Please select a user to reset password.");
            return;
        }
        
        // Generate a random password
        String newPassword = UUID.randomUUID().toString().substring(0, 8);
        
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Reset Password");
        confirmDialog.setHeaderText("Reset password for " + selectedUser.getUserName());
        confirmDialog.setContentText("A new password will be generated. Continue?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Update password in database
                    databaseHelper.updatePasswordAndClearOneTime(selectedUser.getUserName(), newPassword);
                    
                    // Show success dialog with new password
                    Alert successDialog = new Alert(Alert.AlertType.INFORMATION);
                    successDialog.setTitle("Password Reset");
                    successDialog.setHeaderText("Password reset successful");
                    successDialog.setContentText("New password: " + newPassword + "\n\nPlease inform the user of their new password.");
                    successDialog.showAndWait();
                } catch (SQLException e) {
                    showError("Database Error", "Failed to reset password: " + e.getMessage());
                }
            }
        });
    }
    
    // Handler for Delete User button
    private void handleDeleteUser(Stage primaryStage) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Selection Error", "Please select a user to delete.");
            return;
        }

        // Prevent admin from deleting their own account
        if (selectedUser.getUserName().equals(currentAdminUserName)) {
            showError("Operation Not Allowed", "You cannot delete your own admin account.");
            return;
        }
        
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete User");
        confirmDialog.setHeaderText("Delete user: " + selectedUser.getUserName());
        confirmDialog.setContentText("Are you sure you want to delete this user? This action cannot be undone.");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Delete user from database
                    databaseHelper.deleteUser(selectedUser.getUserName(), currentAdminUserName);
                    showSuccess("Success", "User deleted successfully.");
                    refreshUserTable(); // Refresh the table to remove the deleted user
                } catch (SQLException e) {
                    showError("Database Error", "Failed to delete user: " + e.getMessage());
                }
            }
        });
    }
    
    // Helper method to refresh the user table
    private void refreshUserTable() {
        try {
            userTable.getItems().clear();
            userTable.getItems().addAll(databaseHelper.getAllUsers());
        } catch (SQLException e) {
            showError("Database Error", "Failed to refresh user list: " + e.getMessage());
        }
    }
    
    // Helper method to show success message
    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeDatabaseConnections() {
        if (databaseHelper != null) {
            // databaseHelper.closeConnection(); // Assuming LogoutHelper handles this
        }
        if (databaseHelper4 != null) { 
            databaseHelper4.closeConnection();
        }
    }

    // Utility Method for Role Check
    private boolean containsRole(String[] roles, String role) {
        for (String r : roles) {
            if (r.equalsIgnoreCase(role)) return true;
        }
        return false;
    }

    // Utility method to style buttons (can be adapted from Instructor/Staff page if needed)
    private void styleButton(Button button, String bgColor, String borderColor, boolean whiteText) {
         button.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: " + (whiteText ? "white" : "black") + "; " // Use white or black text based on parameter
                + "-fx-background-color: " + bgColor + "; "
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: " + borderColor + "; "
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");
        // Add hover effect (optional)
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-background-color: derive(" + bgColor + ", -10%);"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("-fx-background-color: derive(" + bgColor + ", -10%);", "")));
    }

    // Overloaded styleButton method for backward compatibility
    private void styleButton(Button button, String bgColor, String borderColor) {
        styleButton(button, bgColor, borderColor, false);
    }

    // Basic Error Dialog (copy from other pages or adapt)
     private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        // alert.initOwner(primaryStage); // Need primaryStage accessible here if used
        alert.showAndWait();
    }
}
