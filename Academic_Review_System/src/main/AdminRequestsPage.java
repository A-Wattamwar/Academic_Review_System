package main;

import databasePart1.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A page for viewing and managing Admin Requests.
 * Accessible by Instructor, Staff, and Admin roles with varying permissions.
 */
public class AdminRequestsPage {

    private final DatabaseHelper4 databaseHelper4;
    private final DatabaseHelper databaseHelper; // For user role checking
    private final String currentUsername;
    private final String currentUserRole; // "instructor", "staff", "admin"
    private Stage primaryStage;
    private Scene previousScene;

    private TableView<Request> openRequestsTable;
    private TableView<Request> closedRequestsTable;
    private TabPane tabPane;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");

    public AdminRequestsPage(DatabaseHelper4 databaseHelper4, DatabaseHelper databaseHelper, String currentUsername, Stage primaryStage, Scene previousScene) {
        this.databaseHelper4 = databaseHelper4;
        this.databaseHelper = databaseHelper;
        this.currentUsername = currentUsername;
        this.primaryStage = primaryStage;
        this.previousScene = previousScene;
        this.currentUserRole = getCurrentUserRole(); // Determine role for permissions
        try {
            this.databaseHelper4.connectToDatabase(); // Ensure connection is active
        } catch (SQLException e) {
            showError("Database Error", "Failed to connect to the requests database: " + e.getMessage());
            // Handle connection failure gracefully, maybe disable the page
        }
        
        // Initialize tables here to prevent NullPointerException if show() hasn't been called yet
        this.openRequestsTable = createRequestsTable(); 
        this.closedRequestsTable = createRequestsTable();
    }

    private String getCurrentUserRole() {
        try {
            // Fetch all users and find the current one
            List<User> allUsers = databaseHelper.getAllUsers(); 
            User currentUser = null;
            for (User user : allUsers) {
                if (user.getUserName().equals(currentUsername)) {
                    currentUser = user;
                    break;
                }
            }

            if (currentUser != null && currentUser.getRoles().length > 0) {
                 // Prioritize roles: Admin > Staff > Instructor
                if (currentUser.hasRole("admin")) return "admin";
                if (currentUser.hasRole("staff")) return "staff";
                if (currentUser.hasRole("instructor")) return "instructor";
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user role: " + e.getMessage());
        } catch (NullPointerException e) {
             System.err.println("Error: DatabaseHelper might not be initialized correctly when fetching roles.");
        } 
        return "unknown"; // Default or handle error appropriately
    }

    public void show() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #4169E1, #87CEEB);");

        Label titleLabel = new Label("Admin Action Requests");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Tab Pane for Open and Closed Requests
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // --- Open Requests Tab ---
        Tab openTab = new Tab("Open Requests");
        VBox openLayout = new VBox(10);
        openLayout.setPadding(new Insets(10));
        HBox openActionButtons = createOpenActionButtons();
        openLayout.getChildren().addAll(new Label("Currently Open Requests:"), openRequestsTable, openActionButtons);
        openTab.setContent(openLayout);

        // --- Closed Requests Tab ---
        Tab closedTab = new Tab("Closed Requests");
        VBox closedLayout = new VBox(10);
        closedLayout.setPadding(new Insets(10));
        HBox closedActionButtons = createClosedActionButtons(); 
        closedLayout.getChildren().addAll(new Label("Completed/Closed Requests:"), closedRequestsTable, closedActionButtons);
        closedTab.setContent(closedLayout);

        tabPane.getTabs().addAll(openTab, closedTab);

        // Back Button
        Button backButton = new Button("Back");
        styleButton(backButton, "#FFB6C1", "#FF0000");
        backButton.setOnAction(e -> primaryStage.setScene(previousScene));

        layout.getChildren().addAll(titleLabel, tabPane, backButton);

        refreshTables(); // Initial data load

        Scene adminRequestsScene = new Scene(layout, 900, 650);
        primaryStage.setScene(adminRequestsScene);
        primaryStage.setTitle("Admin Requests");
        primaryStage.show();
    }

    private TableView<Request> createRequestsTable() {
        TableView<Request> table = new TableView<>();
        table.setPrefHeight(400);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Columns
        TableColumn<Request, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        idCol.setPrefWidth(60);

        TableColumn<Request, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(250);

        TableColumn<Request, String> requesterCol = new TableColumn<>("Requester");
        requesterCol.setCellValueFactory(new PropertyValueFactory<>("requesterUsername"));
        requesterCol.setPrefWidth(120);

        TableColumn<Request, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(80);
        statusCol.setStyle("-fx-alignment: CENTER;");
         statusCol.setCellFactory(column -> new TableCell<Request, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.substring(0, 1).toUpperCase() + item.substring(1));
                    Request request = getTableView().getItems().get(getIndex());
                    if (request != null) {
                        if (request.isOpen()) {
                            setStyle("-fx-alignment: CENTER; -fx-text-fill: orange; -fx-font-weight: bold;");
                        } else if (request.isClosed()) {
                             if (request.hasBeenReopened()) {
                                setStyle("-fx-alignment: CENTER; -fx-text-fill: grey; -fx-font-style: italic;"); 
                             } else {
                                setStyle("-fx-alignment: CENTER; -fx-text-fill: green; -fx-font-weight: bold;");
                             }
                        } else {
                            setStyle("-fx-alignment: CENTER;");
                        }
                        // Indicate if reopened
                        if (request.getReopenedFromId() != null) {
                            setText(getText() + " (Reopened)");
                        } else if (request.hasBeenReopened()) {
                             setText(getText() + " (Original)");
                        }
                    } else {
                         setStyle("-fx-alignment: CENTER;");
                    }
                }
            }
        });

        TableColumn<Request, Date> createdCol = new TableColumn<>("Created");
        createdCol.setCellValueFactory(new PropertyValueFactory<>("creationTimestamp"));
        createdCol.setCellFactory(createDateCellFactory());
        createdCol.setPrefWidth(150);

        TableColumn<Request, Date> updatedCol = new TableColumn<>("Last Updated");
        updatedCol.setCellValueFactory(new PropertyValueFactory<>("lastUpdateTimestamp"));
        updatedCol.setCellFactory(createDateCellFactory());
        updatedCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, titleCol, requesterCol, statusCol, createdCol, updatedCol);
        return table;
    }

    private HBox createOpenActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button viewButton = new Button("View Details");
        styleButton(viewButton, "#ADD8E6", "#4682B4");
        viewButton.setOnAction(e -> {
            Request selected = openRequestsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showRequestDetailsDialog(selected);
            } else {
                showError("Selection Error", "Please select an open request to view.");
            }
        });

        buttonBox.getChildren().add(viewButton);

        // Admin-specific actions
        if ("admin".equals(currentUserRole)) {
            Button closeButton = new Button("Close Request");
            styleButton(closeButton, "#90EE90", "#228B22");
            closeButton.setOnAction(e -> {
                Request selected = openRequestsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showCloseRequestDialog(selected);
                } else {
                    showError("Selection Error", "Please select an open request to close.");
                }
            });
            buttonBox.getChildren().add(closeButton);
        }
        
         // Instructor-specific actions (only original requester can edit)
        if ("instructor".equals(currentUserRole)) {
            Button editButton = new Button("Edit Description");
            styleButton(editButton, "#FFA500", "#FF8C00"); 
            editButton.setOnAction(e -> {
                Request selected = openRequestsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    if (selected.getRequesterUsername().equals(currentUsername)) {
                       showEditDescriptionDialog(selected);
                    } else {
                         showError("Permission Denied", "Only the original requester can edit the description.");
                    }
                } else {
                    showError("Selection Error", "Please select an open request to edit.");
                }
            });
             buttonBox.getChildren().add(editButton);
        }

        return buttonBox;
    }

    private HBox createClosedActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button viewButton = new Button("View Details");
        styleButton(viewButton, "#ADD8E6", "#4682B4");
        viewButton.setOnAction(e -> {
            Request selected = closedRequestsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showRequestDetailsDialog(selected);
            } else {
                showError("Selection Error", "Please select a closed request to view.");
            }
        });
        buttonBox.getChildren().add(viewButton);

        // Instructor-specific actions
        if ("instructor".equals(currentUserRole)) {
            Button reopenButton = new Button("Reopen Request");
            styleButton(reopenButton, "#FF6347", "#B22222");
            reopenButton.setOnAction(e -> {
                Request selected = closedRequestsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    if (selected.hasBeenReopened()) {
                         showError("Action Denied", "This request has already been reopened.");
                    } else if (!selected.getRequesterUsername().equals(currentUsername)){
                         showError("Permission Denied", "Only the original requester can reopen this request.");
                    } else {
                         showReopenRequestDialog(selected);
                    }
                } else {
                    showError("Selection Error", "Please select a closed request to reopen.");
                }
            });
            buttonBox.getChildren().add(reopenButton);
        }

        return buttonBox;
    }

    private void refreshTables() {
        try {
            List<Request> openRequests = databaseHelper4.getAllOpenRequests();
            openRequestsTable.setItems(FXCollections.observableArrayList(openRequests));

            // Show closed requests that have NOT been reopened in the main closed tab
            List<Request> closedRequests = databaseHelper4.getClosedRequestsNotReopened();
            closedRequestsTable.setItems(FXCollections.observableArrayList(closedRequests));
             
        } catch (SQLException e) {
            showError("Database Error", "Failed to load requests: " + e.getMessage());
            openRequestsTable.getItems().clear();
            closedRequestsTable.getItems().clear();
        }
        openRequestsTable.refresh();
        closedRequestsTable.refresh();
    }

    // --- Dialogs --- 

    public void showCreateRequestDialog() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Request Admin Action");
        dialog.setHeaderText("Submit a new request for an administrator.");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label titleLabel = new Label("Request Title:");
        TextField titleField = new TextField();
        titleField.setPromptText("Brief summary of the request");

        Label descLabel = new Label("Description:");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Detailed description of the action needed...");
        descArea.setWrapText(true);
        descArea.setPrefRowCount(6);

        content.getChildren().addAll(titleLabel, titleField, descLabel, descArea);
        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Submit Request");
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (titleField.getText().trim().isEmpty() || descArea.getText().trim().isEmpty()) {
                showError("Input Error", "Title and Description cannot be empty.");
                event.consume(); // Prevent dialog closing
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return Map.of("title", titleField.getText().trim(), "description", descArea.getText().trim());
            }
            return null;
        });

        Optional<Map<String, String>> result = dialog.showAndWait();
        result.ifPresent(details -> {
            try {
                Request newRequest = databaseHelper4.createRequest(currentUsername, details.get("title"), details.get("description"));
                if (newRequest != null) {
                    showSuccess("Request Submitted", "Your request (ID: " + newRequest.getRequestId() + ") has been submitted.");
                    refreshTables(); // Update the open requests table
                    tabPane.getSelectionModel().selectFirst(); // Switch to open tab
                } else {
                    showError("Submission Failed", "Failed to create the request in the database.");
                }
            } catch (SQLException e) {
                showError("Database Error", "Failed to submit request: " + e.getMessage());
            }
        });
    }

    private void showRequestDetailsDialog(Request request) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Request Details - ID: " + request.getRequestId());
        dialog.setHeaderText(request.getTitle());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.getDialogPane().setPrefWidth(500);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        // Basic Info Grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.add(new Label("Status:"), 0, 0);
        Label statusLabel = new Label(request.getStatus().toUpperCase());
        statusLabel.setStyle(request.isOpen() ? "-fx-text-fill: orange; -fx-font-weight: bold;" : "-fx-text-fill: green; -fx-font-weight: bold;");
        if (request.hasBeenReopened()) statusLabel.setStyle("-fx-text-fill: grey; -fx-font-style: italic;");
        grid.add(statusLabel, 1, 0);
        
        grid.add(new Label("Requester:"), 0, 1);
        grid.add(new Label(request.getRequesterUsername()), 1, 1);
        
        grid.add(new Label("Created:"), 0, 2);
        grid.add(new Label(formatDate(request.getCreationTimestamp())), 1, 2);
        
        grid.add(new Label("Last Updated:"), 0, 3);
        grid.add(new Label(formatDate(request.getLastUpdateTimestamp())), 1, 3);
        
        // Reopen Info
        if (request.getReopenedFromId() != null) {
            grid.add(new Label("Reopened From:"), 0, 4);
            Hyperlink originalLink = new Hyperlink("Request ID: " + request.getReopenedFromId());
            originalLink.setOnAction(e -> {
                try {
                    Request original = databaseHelper4.getRequestById(request.getReopenedFromId());
                    if (original != null) {
                        dialog.close(); // Close current dialog
                        showRequestDetailsDialog(original); // Show original
                    } else {
                        showError("Not Found", "Original request (ID: " + request.getReopenedFromId() + ") not found.");
                    }
                } catch (SQLException ex) {
                    showError("Database Error", "Failed to load original request: " + ex.getMessage());
                }
            });
            grid.add(originalLink, 1, 4);
        }
        
         if (request.hasBeenReopened()) {
             grid.add(new Label("Status Note:"), 0, 4);
             grid.add(new Label("This request was later reopened."), 1, 4);
              // Optional: Add link to the new request if needed
         }

        // Description
        Label descLabel = new Label("Description:");
        descLabel.setStyle("-fx-font-weight: bold;");
        TextArea descArea = new TextArea(request.getDescription());
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(5);

        layout.getChildren().addAll(grid, new Separator(), descLabel, descArea);

        // Admin Notes (if closed)
        if (request.isClosed()) {
            Label adminNotesLabel = new Label("Admin Resolution Notes:");
            adminNotesLabel.setStyle("-fx-font-weight: bold;");
            TextArea adminNotesArea = new TextArea(request.getAdminNotes() != null ? request.getAdminNotes() : "(No notes provided)");
            adminNotesArea.setEditable(false);
            adminNotesArea.setWrapText(true);
            adminNotesArea.setPrefRowCount(4);
            
            Label closedInfoLabel = new Label(String.format("Closed by %s on %s", 
                                          request.getClosedByUsername(), 
                                          formatDate(request.getClosedTimestamp())));
            closedInfoLabel.setStyle("-fx-font-style: italic;");
            
            layout.getChildren().addAll(new Separator(), adminNotesLabel, adminNotesArea, closedInfoLabel);
        }

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
     private void showEditDescriptionDialog(Request request) {
         Dialog<String> dialog = new Dialog<>();
         dialog.setTitle("Edit Request Description");
         dialog.setHeaderText("Update the description for Request ID: " + request.getRequestId());
         dialog.initModality(Modality.APPLICATION_MODAL);
         dialog.initOwner(primaryStage);

         VBox content = new VBox(10);
         content.setPadding(new Insets(15));

         Label descLabel = new Label("New Description:");
         TextArea descArea = new TextArea(request.getDescription()); // Pre-fill with current
         descArea.setWrapText(true);
         descArea.setPrefRowCount(8);

         content.getChildren().addAll(descLabel, descArea);
         dialog.getDialogPane().setContent(content);

         dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
         final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
         okButton.setText("Save Changes");
         okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
             if (descArea.getText().trim().isEmpty()) {
                 showError("Input Error", "Description cannot be empty.");
                 event.consume();
             }
         });

         dialog.setResultConverter(dialogButton -> {
             if (dialogButton == ButtonType.OK) {
                 return descArea.getText().trim();
             }
             return null;
         });

         Optional<String> result = dialog.showAndWait();
         result.ifPresent(newDescription -> {
             try {
                 boolean updated = databaseHelper4.updateRequestDescription(request.getRequestId(), newDescription, currentUsername);
                 if (updated) {
                     showSuccess("Update Successful", "Request description updated.");
                     refreshTables();
                 } else {
                     showError("Update Failed", "Could not update the request description in the database.");
                 }
             } catch (SQLException e) {
                 showError("Database Error", "Failed to update description: " + e.getMessage());
             }
         });
     }

    private void showCloseRequestDialog(Request request) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Close Admin Request");
        dialog.setHeaderText("Close Request ID: " + request.getRequestId());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label notesLabel = new Label("Admin Notes (Optional):");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Enter notes about the resolution or action taken...");
        notesArea.setWrapText(true);
        notesArea.setPrefRowCount(5);

        content.getChildren().addAll(notesLabel, notesArea);
        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Confirm Close");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return notesArea.getText().trim(); // Return notes (can be empty)
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(adminNotes -> {
            try {
                boolean closed = databaseHelper4.closeRequest(request.getRequestId(), currentUsername, adminNotes);
                if (closed) {
                    showSuccess("Request Closed", "Request ID: " + request.getRequestId() + " has been closed.");
                    refreshTables();
                    tabPane.getSelectionModel().selectLast(); // Switch to closed tab
                } else {
                    showError("Closure Failed", "Could not close the request. It might already be closed or an error occurred.");
                }
            } catch (SQLException e) {
                showError("Database Error", "Failed to close request: " + e.getMessage());
            }
        });
    }

    private void showReopenRequestDialog(Request request) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reopen Request");
        dialog.setHeaderText("Reopen Request ID: " + request.getRequestId());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label infoLabel = new Label("This will create a new request linked to the original.");
        Label descLabel = new Label("Updated Description (Required):");
        TextArea descArea = new TextArea(request.getDescription()); // Pre-fill with original
        descArea.setPromptText("Explain why this needs to be reopened or provide updated details...");
        descArea.setWrapText(true);
        descArea.setPrefRowCount(6);

        content.getChildren().addAll(infoLabel, descLabel, descArea);
        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Confirm Reopen");
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (descArea.getText().trim().isEmpty()) {
                showError("Input Error", "An updated description is required to reopen.");
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return descArea.getText().trim();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(updatedDescription -> {
            try {
                Request reopenedRequest = databaseHelper4.reopenRequest(request.getRequestId(), currentUsername, updatedDescription);
                if (reopenedRequest != null) {
                    showSuccess("Request Reopened", "Request ID: " + request.getRequestId() + " has been reopened as new Request ID: " + reopenedRequest.getRequestId());
                    refreshTables(); // Refresh both tabs
                    tabPane.getSelectionModel().selectFirst(); // Go to open tab to see the new one
                } else {
                    showError("Reopen Failed", "Could not reopen the request.");
                }
            } catch (SQLException e) {
                showError("Database Error", "Failed to reopen request: " + e.getMessage());
            }
        });
    }

    // --- Helper Methods ---

    private void styleButton(Button button, String bgColor, String borderColor) {
        button.setStyle("-fx-font-size: 12px; " +
                "-fx-text-fill: black; " +
                "-fx-background-color: " + bgColor + "; " +
                "-fx-padding: 5px 10px; " +
                "-fx-border-color: " + borderColor + "; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px;");
        // Add hover effect (optional)
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-background-color: derive(" + bgColor + ", -10%);"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("-fx-background-color: derive(" + bgColor + ", -10%);", "")));
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "N/A";
        }
        return DATE_FORMAT.format(date);
    }

    private <T> javafx.util.Callback<TableColumn<Request, T>, TableCell<Request, T>> createDateCellFactory() {
        return column -> new TableCell<Request, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item instanceof Date) {
                        setText(DATE_FORMAT.format((Date) item));
                    } else {
                        setText("Invalid Date");
                    }
                }
            }
        };
    }
} 