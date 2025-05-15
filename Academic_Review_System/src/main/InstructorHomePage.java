package main;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import databasePart1.DatabaseHelper;
import databasePart1.DatabaseHelper2;
import databasePart1.DatabaseHelper3;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.HashMap;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import databasePart1.DatabaseHelper4;

/**
 * InstructorHomePage class represents the home page for users with the instructor role.
 */
public class InstructorHomePage {
    private final DatabaseHelper databaseHelper;
    private final DatabaseHelper2 databaseHelper2;
    private final DatabaseHelper3 databaseHelper3;
    private final DatabaseHelper4 databaseHelper4;
    private final String currentUsername;
    private Questions questionsManager;
    private static final java.text.SimpleDateFormat DATE_FORMAT = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss");

    private AdminRequestsPage adminRequestsPage;

    public InstructorHomePage(DatabaseHelper databaseHelper, String currentUsername) {
        this.databaseHelper = databaseHelper;
        this.currentUsername = currentUsername;
        
        // Initialize DatabaseHelper2 and DatabaseHelper3
        this.databaseHelper2 = new DatabaseHelper2();
        this.databaseHelper3 = new DatabaseHelper3();
        this.databaseHelper4 = new DatabaseHelper4();
        try {
            this.databaseHelper2.connectToDatabase();
            this.databaseHelper3.connectToDatabase();
            this.databaseHelper4.connectToDatabase();
            this.questionsManager = new Questions(databaseHelper2, databaseHelper2.connection);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Fatal Error", "Could not connect to required databases. Please restart the application.");
        }
    }

    public void show(Stage primaryStage) {
        // Background Pane with Blue Gradient
        Pane backgroundPane = new Pane();
        backgroundPane.setStyle("-fx-background-color: linear-gradient(to bottom, #4169E1, #87CEEB);"); 
        backgroundPane.setPrefSize(1000, 600);

        // Main VBox Layout
        VBox layout = new VBox(20);
        layout.setPrefSize(1000, 600);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        Label instructorLabel = new Label("Welcome, " + currentUsername + "!");
        instructorLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Create vertical button column (replacing horizontal button row)
        VBox buttonColumn = new VBox(10);
        buttonColumn.setAlignment(Pos.CENTER);
        buttonColumn.setPadding(new Insets(50, 0, 0, 0)); // Add top padding to move buttons down

        // Add Dashboard button
        Button dashboardButton = new Button("Dashboard");
        styleButton(dashboardButton, "#1a4b78", "#4169E1");
        dashboardButton.setOnAction(e -> showDashboard(primaryStage));

        // Add Reviewer Permissions Management button
        Button reviewerButton = new Button("Manage Reviewer Requests");
        styleButton(reviewerButton, "#1a4b78", "#4169E1");
        reviewerButton.setOnAction(e -> {
            new ReviewerPermissionsPage(databaseHelper, currentUsername, true).show(primaryStage);
        });

        // --- Add New Admin Request Buttons --- 
        Button createAdminRequestButton = new Button("Request Admin Action");
        styleButton(createAdminRequestButton, "#FFD700", "#DAA520"); // Gold/Yellow style
        createAdminRequestButton.setStyle(createAdminRequestButton.getStyle() + "; -fx-text-fill: black;");
        createAdminRequestButton.setOnAction(e -> {
            if (adminRequestsPage == null) {
                adminRequestsPage = new AdminRequestsPage(databaseHelper4, databaseHelper, currentUsername, primaryStage, primaryStage.getScene());
            }
            adminRequestsPage.showCreateRequestDialog(); 
        });

        Button viewAdminRequestsButton = new Button("View Admin Requests");
        styleButton(viewAdminRequestsButton, "#FFD700", "#DAA520"); // Gold/Yellow style
        viewAdminRequestsButton.setStyle(viewAdminRequestsButton.getStyle() + "; -fx-text-fill: black;");
        viewAdminRequestsButton.setOnAction(e -> {
             if (adminRequestsPage == null) {
                adminRequestsPage = new AdminRequestsPage(databaseHelper4, databaseHelper, currentUsername, primaryStage, primaryStage.getScene());
            }
            adminRequestsPage.show(); // Show the full request management page
        });
        // --- End New Admin Request Buttons --- 

        // Add Reported Content button
        Button reportedContentButton = new Button("Reported Content");
        styleButton(reportedContentButton, "#B22222", "#FF6347");
        reportedContentButton.setOnAction(e -> showReportedContentDialog());
        
        // Add Banned Students button
        Button bannedStudentsButton = new Button("Banned Students");
        styleButton(bannedStudentsButton, "#800000", "#A52A2A"); // Dark red
        bannedStudentsButton.setOnAction(e -> showBannedStudentsDialog());

        // Add Inbox button
        Button inboxButton = new Button("Inbox");
        styleButton(inboxButton, "#ADD8E6", "#4682B4");
        inboxButton.setStyle(inboxButton.getStyle() + "; -fx-text-fill: black;");
        inboxButton.setOnAction(e -> {
            if (databaseHelper2 != null) {
                try {
                    Answers answersManager = new Answers(databaseHelper2);
                    MessageHelper.showInboxDialog(currentUsername, databaseHelper2, databaseHelper3, 
                                                questionsManager, answersManager);
                } catch (SQLException ex) {
                    showError("Error", "Failed to open inbox: " + ex.getMessage());
                }
            }
        });

        // Add Logout button
        Button logoutButton = LogoutHelper.createLogoutButton(primaryStage, databaseHelper);
        styleButton(logoutButton, "#FF6B6B", "#CD5C5C"); // Red color scheme
        logoutButton.setStyle(logoutButton.getStyle() + "; -fx-text-fill: black;"); // Add black text color

        // Add all buttons to the vertical column
        buttonColumn.getChildren().addAll(
            dashboardButton, 
            reviewerButton,
            createAdminRequestButton, 
            viewAdminRequestsButton,
            reportedContentButton, 
            bannedStudentsButton,
            inboxButton, 
            logoutButton
        );
        
        // Add buttons to main layout
        layout.getChildren().addAll(instructorLabel, buttonColumn);
        
        // Add notifications section
        try {
            int pendingReportCount = databaseHelper3.getPendingReportedContent().size();
            
            if (pendingReportCount > 0) {
                HBox notificationBox = new HBox(10);
                notificationBox.setAlignment(Pos.CENTER);
                notificationBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); "
                        + "-fx-padding: 10px; "
                        + "-fx-background-radius: 10px;");
                
                Label notificationLabel = new Label("You have " + pendingReportCount + " pending reported content items to review");
                notificationLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #B22222;");
                
                Button viewButton = new Button("View Now");
                styleButton(viewButton, "#B22222", "#FF6347");
                viewButton.setOnAction(e -> showReportedContentDialog());
                
                notificationBox.getChildren().addAll(notificationLabel, viewButton);
                layout.getChildren().add(notificationBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

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

        Scene instructorScene = new Scene(root, 1000, 600);
        primaryStage.setScene(instructorScene);
        primaryStage.setTitle("Instructor Dashboard");
        
        // Add window close handler to clean up database connections
        primaryStage.setOnCloseRequest(event -> {
            closeDatabaseConnections();
        });
        
        primaryStage.show();
    }
    
    /**
     * Shows a dialog displaying reported content that needs review
     */
    private void showReportedContentDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Reported Content");
        dialog.setHeaderText("Review Reported Content");
        dialog.getDialogPane().setPrefSize(900, 600);

        VBox content = new VBox(15);
        content.setPadding(new Insets(10));

        try {
            // Fetch ALL reported content, not just pending
            List<Map<String, Object>> reportedContent = databaseHelper3.getAllReportedContent();
            
            if (reportedContent.isEmpty()) {
                Label emptyLabel = new Label("No reported content found.");
                emptyLabel.setStyle("-fx-font-style: italic;");
                content.getChildren().add(emptyLabel);
            } else {
                // Create a table view for reported content
                TableView<Map<String, Object>> reportTable = new TableView<>();
                reportTable.setPrefHeight(400);
                
                // Setup columns
                TableColumn<Map<String, Object>, String> reporterCol = new TableColumn<>("Reported By");
                reporterCol.setCellValueFactory(data -> 
                    new javafx.beans.property.SimpleStringProperty((String)data.getValue().get("reporterUsername")));
                reporterCol.setPrefWidth(120);
                
                TableColumn<Map<String, Object>, String> typeCol = new TableColumn<>("Content Type");
                typeCol.setCellValueFactory(data -> 
                    new javafx.beans.property.SimpleStringProperty((String)data.getValue().get("contentType")));
                typeCol.setPrefWidth(100);
                
                TableColumn<Map<String, Object>, String> idCol = new TableColumn<>("Content ID");
                idCol.setCellValueFactory(data -> 
                    new javafx.beans.property.SimpleStringProperty(data.getValue().get("contentId").toString()));
                idCol.setPrefWidth(80);
                
                TableColumn<Map<String, Object>, Date> dateCol = new TableColumn<>("Report Date");
                dateCol.setCellValueFactory(data -> 
                    new javafx.beans.property.SimpleObjectProperty<>((Date)data.getValue().get("timestamp")));
                dateCol.setCellFactory(col -> new TableCell<>() {
                    @Override
                    protected void updateItem(Date item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(DATE_FORMAT.format(item));
                        }
                    }
                });
                dateCol.setPrefWidth(150);
                
                // Add Status Column
                TableColumn<Map<String, Object>, String> statusCol = new TableColumn<>("Status");
                statusCol.setCellValueFactory(data -> {
                    String status = (String)data.getValue().get("status");
                    // If status is null or "pending", show as "Pending"
                    if (status == null || status.equals("pending")) {
                        return new javafx.beans.property.SimpleStringProperty("Pending");
                    } else {
                        return new javafx.beans.property.SimpleStringProperty(
                            status.substring(0, 1).toUpperCase() + status.substring(1));
                    }
                });
                statusCol.setCellFactory(col -> new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            if (item.equals("Pending")) {
                                setStyle("-fx-text-fill: #B22222; -fx-font-weight: bold;"); // Red for pending
                            } else if (item.equals("Reviewed")) {
                                setStyle("-fx-text-fill: #228B22;"); // Green for reviewed
                            } else {
                                setStyle("-fx-text-fill: #666666;"); // Grey for other statuses
                            }
                        }
                    }
                });
                statusCol.setPrefWidth(80);
                
                TableColumn<Map<String, Object>, Void> actionCol = new TableColumn<>("Action");
                actionCol.setCellFactory(col -> new TableCell<>() {
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Map<String, Object> report = getTableView().getItems().get(getIndex());
                            
                            HBox box = new HBox(5);
                            
                            Button viewBtn = new Button("View Details");
                            styleButton(viewBtn, "#1a4b78", "#4169E1");
                            viewBtn.setOnAction(e -> showReportDetails(report, reportTable));
                            
                            box.getChildren().add(viewBtn);
                            setGraphic(box);
                        }
                    }
                });
                actionCol.setPrefWidth(120);
                
                reportTable.getColumns().addAll(reporterCol, typeCol, idCol, dateCol, statusCol, actionCol);
                reportTable.setItems(FXCollections.observableArrayList(reportedContent));
                
                // Add a pending filter toggle
                CheckBox showPendingOnly = new CheckBox("Show Pending Reports Only");
                showPendingOnly.setSelected(true);  // Initially show only pending reports
                showPendingOnly.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
                
                // Filter handler
                showPendingOnly.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    try {
                        if (newVal) {
                            // Only show pending
                            List<Map<String, Object>> pendingOnly = reportedContent.stream()
                                .filter(map -> {
                                    String status = (String)map.get("status");
                                    return status == null || status.equals("pending");
                                })
                                .collect(java.util.stream.Collectors.toList());
                            reportTable.setItems(FXCollections.observableArrayList(pendingOnly));
                        } else {
                            // Show all
                            reportTable.setItems(FXCollections.observableArrayList(reportedContent));
                        }
                    } catch (Exception e) {
                        showError("Error", "Failed to filter reports: " + e.getMessage());
                    }
                });
                
                // Trigger initial filter
                showPendingOnly.fire();
                
                content.getChildren().addAll(showPendingOnly, reportTable);
            }
        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading reported content: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            content.getChildren().add(errorLabel);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    /**
     * Shows detailed information about a reported content item and options to act on it
     * 
     * @param report The report data to display
     * @param reportTable The table to refresh after action
     */
    private void showReportDetails(Map<String, Object> report, TableView<Map<String, Object>> reportTable) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Report Details");
        dialog.setHeaderText("Review Reported Content #" + report.get("reportId"));

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        try {
            // Display report information
            Label reporterLabel = new Label("Reported by: " + report.get("reporterUsername"));
            Label typeLabel = new Label("Content type: " + report.get("contentType"));
            Label dateLabel = new Label("Report date: " + DATE_FORMAT.format((Date)report.get("timestamp")));
            Label statusLabel = new Label("Status: " + (report.get("status") == null ? "Pending" : 
                report.get("status").toString().substring(0, 1).toUpperCase() + 
                report.get("status").toString().substring(1)));
            
            // Style status label based on status
            if (report.get("status") == null || report.get("status").equals("pending")) {
                statusLabel.setStyle("-fx-text-fill: #B22222; -fx-font-weight: bold;"); // Red for pending
            } else if (report.get("status").equals("reviewed")) {
                statusLabel.setStyle("-fx-text-fill: #228B22; -fx-font-weight: bold;"); // Green for reviewed
            }
            
            // Display content information
            String contentType = (String)report.get("contentType");
            int contentId = (Integer)report.get("contentId");
            
            VBox contentBox = new VBox(5);
            contentBox.setStyle("-fx-background-color: #f8f8f8; -fx-padding: 10; -fx-background-radius: 5;");
            
            if ("question".equals(contentType)) {
                Question question = questionsManager.getQuestionById(contentId);
                if (question != null) {
                    Label contentLabel = new Label("Question: " + question.getContent());
                    contentLabel.setWrapText(true);
                    Label authorLabel = new Label("Asked by: " + question.getAuthor());
                    
                    contentBox.getChildren().addAll(
                        new Label("Question #" + contentId + ":"),
                        contentLabel,
                        authorLabel
                    );
                } else {
                    contentBox.getChildren().add(new Label("Question not found"));
                }
            } else if ("answer".equals(contentType)) {
                // Fetch and display answer details
                // For brevity, we'll just show a placeholder
                contentBox.getChildren().add(new Label("Answer ID: " + contentId));
            }
            
            // Display reason for reporting
            Label reasonTitle = new Label("Reason for reporting:");
            reasonTitle.setStyle("-fx-font-weight: bold;");
            
            TextArea reasonArea = new TextArea((String)report.get("reason"));
            reasonArea.setEditable(false);
            reasonArea.setWrapText(true);
            reasonArea.setPrefRowCount(4);
            
            // If report already has notes, show them
            Label notesDisplayLabel = null;
            TextArea notesDisplayArea = null;
            if (report.get("notes") != null && !((String)report.get("notes")).isEmpty()) {
                notesDisplayLabel = new Label("Review Notes:");
                notesDisplayLabel.setStyle("-fx-font-weight: bold;");
                
                notesDisplayArea = new TextArea((String)report.get("notes"));
                notesDisplayArea.setEditable(false);
                notesDisplayArea.setWrapText(true);
                notesDisplayArea.setPrefRowCount(3);
            }
            
            // Only show decision controls if status is pending
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER);
            
            if (report.get("status") == null || report.get("status").equals("pending")) {
                // Add decision controls
                Label decisionLabel = new Label("Your decision:");
                decisionLabel.setStyle("-fx-font-weight: bold;");
                
                TextArea notesArea = new TextArea();
                notesArea.setPromptText("Enter your review notes here...");
                notesArea.setWrapText(true);
                notesArea.setPrefRowCount(3);
                
                Button reviewedBtn = new Button("Mark as Reviewed");
                styleButton(reviewedBtn, "#90EE90", "#228B22");
                reviewedBtn.setOnAction(e -> {
                    try {
                        handleReportDecision((Integer)report.get("reportId"), "reviewed", notesArea.getText(), reportTable);
                        dialog.close();
                    } catch (SQLException ex) {
                        showError("Error", "Failed to update report status: " + ex.getMessage());
                    }
                });
                
                Button messageBtn = new Button("Message Reporter");
                styleButton(messageBtn, "#ADD8E6", "#4682B4");
                messageBtn.setOnAction(e -> {
                    String reporter = (String)report.get("reporterUsername");
                    
                    // Create a message dialog
                    Dialog<String> msgDialog = new Dialog<>();
                    msgDialog.setTitle("Message Reporter");
                    msgDialog.setHeaderText("Send message to " + reporter);
                    
                    VBox msgContent = new VBox(10);
                    msgContent.setPadding(new Insets(10));
                    
                    Label msgLabel = new Label("Message:");
                    TextArea msgArea = new TextArea();
                    msgArea.setPromptText("Type your message here...");
                    msgArea.setPrefRowCount(5);
                    
                    msgContent.getChildren().addAll(msgLabel, msgArea);
                    msgDialog.getDialogPane().setContent(msgContent);
                    msgDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                    
                    msgDialog.setResultConverter(dialogButton -> {
                        if (dialogButton == ButtonType.OK) {
                            return msgArea.getText();
                        }
                        return null;
                    });
                    
                    msgDialog.showAndWait().ifPresent(message -> {
                        if (message != null && !message.trim().isEmpty()) {
                            try {
                                // Create message
                                Feedback feedback = new Feedback(
                                    0, 
                                    currentUsername, 
                                    reporter, 
                                    message, 
                                    new Date(), 
                                    contentType.equals("question") ? contentId : 0,
                                    contentType.equals("answer") ? contentId : null,
                                    null,  // No review reference
                                    null   // Not a reply
                                );
                                databaseHelper2.addFeedback(feedback);
                                showSuccess("Message Sent", "Your message has been sent to " + reporter);
                            } catch (SQLException ex) {
                                showError("Error", "Failed to send message: " + ex.getMessage());
                            }
                        } else {
                            showError("Error", "Message cannot be empty");
                        }
                    });
                });
                
                buttonBox.getChildren().addAll(reviewedBtn, messageBtn);
                
                content.getChildren().addAll(
                    reporterLabel, typeLabel, dateLabel, statusLabel,
                    new Separator(),
                    contentBox,
                    new Separator(),
                    reasonTitle, reasonArea
                );
                
                if (notesDisplayLabel != null && notesDisplayArea != null) {
                    content.getChildren().addAll(
                        new Separator(),
                        notesDisplayLabel, notesDisplayArea
                    );
                }
                
                content.getChildren().addAll(
                    new Separator(),
                    decisionLabel, notesArea,
                    buttonBox
                );
            } else {
                // If already reviewed, just show a message button
                Button messageBtn = new Button("Message Reporter");
                styleButton(messageBtn, "#ADD8E6", "#4682B4");
                messageBtn.setOnAction(e -> {
                    String reporter = (String)report.get("reporterUsername");
                    
                    // Create a message dialog (same as above)
                    Dialog<String> msgDialog = new Dialog<>();
                    msgDialog.setTitle("Message Reporter");
                    msgDialog.setHeaderText("Send message to " + reporter);
                    
                    VBox msgContent = new VBox(10);
                    msgContent.setPadding(new Insets(10));
                    
                    Label msgLabel = new Label("Message:");
                    TextArea msgArea = new TextArea();
                    msgArea.setPromptText("Type your message here...");
                    msgArea.setPrefRowCount(5);
                    
                    msgContent.getChildren().addAll(msgLabel, msgArea);
                    msgDialog.getDialogPane().setContent(msgContent);
                    msgDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                    
                    msgDialog.setResultConverter(dialogButton -> {
                        if (dialogButton == ButtonType.OK) {
                            return msgArea.getText();
                        }
                        return null;
                    });
                    
                    msgDialog.showAndWait().ifPresent(message -> {
                        if (message != null && !message.trim().isEmpty()) {
                            try {
                                // Create message
                                Feedback feedback = new Feedback(
                                    0, 
                                    currentUsername, 
                                    reporter, 
                                    message, 
                                    new Date(), 
                                    contentType.equals("question") ? contentId : 0,
                                    contentType.equals("answer") ? contentId : null,
                                    null,  // No review reference
                                    null   // Not a reply
                                );
                                databaseHelper2.addFeedback(feedback);
                                showSuccess("Message Sent", "Your message has been sent to " + reporter);
                            } catch (SQLException ex) {
                                showError("Error", "Failed to send message: " + ex.getMessage());
                            }
                        } else {
                            showError("Error", "Message cannot be empty");
                        }
                    });
                });
                
                buttonBox.getChildren().add(messageBtn);
                
                // Show who reviewed it if available
                if (report.get("reviewedBy") != null) {
                    Label reviewedByLabel = new Label("Reviewed by: " + report.get("reviewedBy") + 
                                                   " on " + DATE_FORMAT.format((Date)report.get("reviewTimestamp")));
                    reviewedByLabel.setStyle("-fx-font-style: italic;");
                    
                    content.getChildren().addAll(
                        reporterLabel, typeLabel, dateLabel, statusLabel, reviewedByLabel,
                        new Separator(),
                        contentBox,
                        new Separator(),
                        reasonTitle, reasonArea
                    );
                } else {
                    content.getChildren().addAll(
                        reporterLabel, typeLabel, dateLabel, statusLabel,
                        new Separator(),
                        contentBox,
                        new Separator(),
                        reasonTitle, reasonArea
                    );
                }
                
                if (notesDisplayLabel != null && notesDisplayArea != null) {
                    content.getChildren().addAll(
                        new Separator(),
                        notesDisplayLabel, notesDisplayArea
                    );
                }
                
                content.getChildren().add(buttonBox);
            }
            
        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading content details: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            content.getChildren().add(errorLabel);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    /**
     * Handles the instructor's decision on a reported content item
     * 
     * @param reportId The ID of the report
     * @param status The new status (reviewed or dismissed)
     * @param notes Notes from the instructor about their decision
     * @param reportTable The table to refresh after updating the status
     */
    private void handleReportDecision(int reportId, String status, String notes, TableView<Map<String, Object>> reportTable) throws SQLException {
        // Update the report status in the database
        databaseHelper3.updateReportStatus(reportId, status, currentUsername, notes);
        
        // Refresh the table with updated data
        List<Map<String, Object>> allReports = databaseHelper3.getAllReportedContent();
        
        // Check if we're filtering to show only pending
        boolean showPendingOnly = false;
        for (Node node : reportTable.getParent().getChildrenUnmodifiable()) {
            if (node instanceof CheckBox && ((CheckBox) node).getText().equals("Show Pending Reports Only")) {
                showPendingOnly = ((CheckBox) node).isSelected();
                break;
            }
        }
        
        if (showPendingOnly) {
            // Only show pending reports
            List<Map<String, Object>> pendingOnly = allReports.stream()
                .filter(map -> {
                    String reportStatus = (String)map.get("status");
                    return reportStatus == null || reportStatus.equals("pending");
                })
                .collect(java.util.stream.Collectors.toList());
            reportTable.setItems(FXCollections.observableArrayList(pendingOnly));
        } else {
            // Show all reports
            reportTable.setItems(FXCollections.observableArrayList(allReports));
        }
        
        showSuccess("Report Updated", "The report has been marked as " + status);
    }
    
    private void closeDatabaseConnections() {
        if (databaseHelper2 != null) {
            databaseHelper2.closeConnection();
        }
        if (databaseHelper3 != null) {
            databaseHelper3.closeConnection();
        }
        if (databaseHelper4 != null) { 
            databaseHelper4.closeConnection();
        }
    }
    
    private void styleButton(Button button, String bgColor, String borderColor) {
        button.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: " + bgColor + "; "
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: " + borderColor + "; "
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Shows the instructor dashboard with questions and answers
     * 
     * @param primaryStage The primary stage to show the dashboard on
     */
    private void showDashboard(Stage primaryStage) {
        // Save current stage state
        Scene previousScene = primaryStage.getScene();
        String previousTitle = primaryStage.getTitle();

        // Background Pane with Blue Gradient
        Pane backgroundPane = new Pane();
        backgroundPane.setStyle("-fx-background-color: linear-gradient(to bottom, #4169E1, #87CEEB);");
        backgroundPane.setPrefSize(1000, 600);

        // Main VBox Layout
        VBox layout = new VBox(20);
        layout.setPrefSize(1000, 600);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        // Create a container for the back button at bottom right
        HBox backButtonContainer = new HBox();
        backButtonContainer.setAlignment(Pos.BOTTOM_RIGHT);
        backButtonContainer.setPadding(new Insets(0, 20, 20, 0)); // Add padding to position from edges
        
        // Back button to return to previous page
        Button backButton = new Button("Back to Home");
        styleButton(backButton, "#E0E0E0", "#C0C0C0"); // Light gray with black text
        backButton.setStyle(backButton.getStyle() + "; -fx-text-fill: black;");
        backButton.setOnAction(e -> {
            primaryStage.setScene(previousScene);
            primaryStage.setTitle(previousTitle);
        });
        
        // Add back button to its container
        backButtonContainer.getChildren().add(backButton);

        // Create a VBox to hold the main layout and back button
        VBox mainContainer = new VBox();
        mainContainer.getChildren().addAll(layout, backButtonContainer);
        VBox.setVgrow(layout, Priority.ALWAYS);

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

        // Add search section
        layout.getChildren().add(createDashboardSearchSection());

        // Add questions section
        layout.getChildren().add(createDashboardQuestionsSection());

        // Update the scene
        Scene dashboardScene = new Scene(root, 1000, 600);
        primaryStage.setScene(dashboardScene);
        primaryStage.setTitle("Dashboard");

        // Initial load of questions
        refreshDashboardQuestions();
    }
    
    private VBox createDashboardSearchSection() {
        VBox searchSection = new VBox(10);
        searchSection.setAlignment(Pos.CENTER);

        // Search row
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER);

        TextField searchField = new TextField();
        searchField.setPromptText("Search questions...");
        searchField.setPrefWidth(300);

        Button searchButton = new Button("Search");
        styleButton(searchButton, "#1a4b78", "#1a4b78");
        searchButton.setOnAction(e -> {
            try {
                String keyword = searchField.getText();
                if (!keyword.isEmpty()) {
                    dashboardQuestionTable.setItems(FXCollections.observableArrayList(
                        questionsManager.searchQuestions(keyword)
                    ));
                } else {
                    refreshDashboardQuestions();
                }
            } catch (SQLException ex) {
                showError("Search Error", ex.getMessage());
            }
        });

        // Add Manage Reviewers button
        Button manageReviewersButton = new Button("Manage Reviewer Scorecards");
        styleButton(manageReviewersButton, "#1a4b78", "#1a4b78");
        manageReviewersButton.setOnAction(e -> showReviewerScorecardDialog());

        searchRow.getChildren().addAll(searchField, searchButton, manageReviewersButton);
        searchSection.getChildren().add(searchRow);
        return searchSection;
    }
    
    // Add instance variable at the class level
    private TableView<Question> dashboardQuestionTable;
    
    private VBox createDashboardQuestionsSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));

        Label titleLabel = new Label("Questions");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        dashboardQuestionTable = new TableView<>();
        setupDashboardQuestionTable();

        // Create action buttons row
        HBox actionButtonsRow = new HBox(10);
        actionButtonsRow.setAlignment(Pos.CENTER);
        
        // View Answers button
        Button viewAnswersBtn = new Button("View Answers");
        styleButton(viewAnswersBtn, "#90EE90", "#228B22");
        viewAnswersBtn.setStyle(viewAnswersBtn.getStyle() + "; -fx-text-fill: black;");
        viewAnswersBtn.setOnAction(e -> {
            Question selectedQuestion = dashboardQuestionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion == null) {
                showError("Selection Error", "Please select a question to view answers.");
                return;
            }
            showAnswersDialog(selectedQuestion);
        });
        
        // View Reviews button
        Button viewReviewsBtn = new Button("View Reviews");
        styleButton(viewReviewsBtn, "#87CEEB", "#4169E1");
        viewReviewsBtn.setStyle(viewReviewsBtn.getStyle() + "; -fx-text-fill: black;");
        viewReviewsBtn.setOnAction(e -> {
            Question selectedQuestion = dashboardQuestionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion == null) {
                showError("Selection Error", "Please select a question to view reviews.");
                return;
            }
            showQuestionReviewsDialog(selectedQuestion);
        });
        
        // Message Author button
        Button messageBtn = new Button("Message Author");
        styleButton(messageBtn, "#ADD8E6", "#4682B4");
        messageBtn.setStyle(messageBtn.getStyle() + "; -fx-text-fill: black;");
        messageBtn.setOnAction(e -> {
            Question selectedQuestion = dashboardQuestionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion == null) {
                showError("Selection Error", "Please select a question to message its author.");
                return;
            }
            if (!selectedQuestion.getAuthor().equals(currentUsername)) {
                showMessageForQuestion(selectedQuestion.getAuthor(), selectedQuestion.getQuestionId());
            }
        });
        
        // Send Warning button
        Button warningBtn = new Button("Send Warning");
        styleButton(warningBtn, "#FFB6C1", "#FF0000");
        warningBtn.setStyle(warningBtn.getStyle() + "; -fx-text-fill: black;");
        warningBtn.setOnAction(e -> {
            Question selectedQuestion = dashboardQuestionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion == null) {
                showError("Selection Error", "Please select a question to send a warning.");
                return;
            }
            showQuestionWarningDialog(selectedQuestion);
        });
        
        actionButtonsRow.getChildren().addAll(viewAnswersBtn, viewReviewsBtn, messageBtn, warningBtn);

        section.getChildren().addAll(titleLabel, dashboardQuestionTable, actionButtonsRow);
        return section;
    }
    
    private void setupDashboardQuestionTable() {
        // Content Column
        TableColumn<Question, String> contentCol = new TableColumn<>("Question");
        contentCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContent()));
        contentCol.setPrefWidth(500);

        // ID Column
        TableColumn<Question, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("questionId"));
        idCol.setPrefWidth(60);

        // Author Column
        TableColumn<Question, String> authorCol = new TableColumn<>("Asked By");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(120);

        // Date Column
        TableColumn<Question, Date> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(DATE_FORMAT.format(item));
                }
            }
        });
        dateCol.setPrefWidth(170);

        // Status Column
        TableColumn<Question, Boolean> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("answered"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item ? "Resolved" : "Unresolved");
                }
            }
        });
        statusCol.setPrefWidth(100);

        dashboardQuestionTable.getColumns().setAll(contentCol, idCol, authorCol, dateCol, statusCol);
        
        // Set row selection behavior
        dashboardQuestionTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }
    
    private void refreshDashboardQuestions() {
        try {
            dashboardQuestionTable.setItems(FXCollections.observableArrayList(
                questionsManager.getAllQuestions()
            ));
        } catch (SQLException e) {
            showError("Error", "Failed to load questions: " + e.getMessage());
        }
    }
    
    // Filter questions by status (resolved/unresolved)
    private void filterDashboardByStatus(boolean isResolved) {
        try {
            List<Question> filteredQuestions = questionsManager.getAllQuestions();
            filteredQuestions.removeIf(q -> q.isAnswered() != isResolved);
            dashboardQuestionTable.setItems(FXCollections.observableArrayList(filteredQuestions));
        } catch (SQLException e) {
            showError("Filter Error", e.getMessage());
        }
    }

    // Filter questions by recency (most recent first)
    private void filterDashboardByRecent() {
        try {
            List<Question> recentQuestions = questionsManager.getAllQuestions();
            recentQuestions.sort((q1, q2) -> q2.getTimestamp().compareTo(q1.getTimestamp()));
            dashboardQuestionTable.setItems(FXCollections.observableArrayList(recentQuestions));
        } catch (SQLException e) {
            showError("Filter Error", e.getMessage());
        }
    }
    
    private void showAnswersDialog(Question question) {
        try {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Question Answers");
            dialog.setHeaderText("Answers for Question #" + question.getQuestionId());
            dialog.getDialogPane().setPrefSize(900, 600);

            VBox content = new VBox(10);
            content.setPadding(new Insets(10));

            // Question content display
            Label questionLabel = new Label("Question: " + question.getContent());
            questionLabel.setWrapText(true);
            questionLabel.setStyle("-fx-font-weight: bold;");
            
            // Author and date information
            Label infoLabel = new Label("Asked by: " + question.getAuthor() + " on " + 
                                       DATE_FORMAT.format(question.getTimestamp()));
            
            TableView<Answer> answersTable = new TableView<>();
            
            // Answer Content Column
            TableColumn<Answer, String> contentCol = new TableColumn<>("Answer");
            contentCol.setCellValueFactory(cellData -> {
                Answer answer = cellData.getValue();
                String answerContent = answer.getContent();
                
                // Handle referenced answers
                if (answer.hasReference()) {
                    try {
                        Answer referencedAnswer = databaseHelper2.getAnswerById(answer.getReferenceAnswerId());
                        if (referencedAnswer != null) {
                            String referenceDisplay = String.format(
                                "↪ Re: \"%s\" (by %s)\n%s",
                                truncateString(referencedAnswer.getContent(), 50),
                                referencedAnswer.getAuthor(),
                                answerContent
                            );
                            return new javafx.beans.property.SimpleStringProperty(referenceDisplay);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                
                return new javafx.beans.property.SimpleStringProperty(answerContent);
            });
            contentCol.setPrefWidth(400);

            // Answered By Column
            TableColumn<Answer, String> authorCol = new TableColumn<>("Answered By");
            authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
            authorCol.setPrefWidth(120);

            // Date Column
            TableColumn<Answer, Date> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
            dateCol.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(DATE_FORMAT.format(item));
                    }
                }
            });
            dateCol.setPrefWidth(150);

            // Accepted Answer Column
            TableColumn<Answer, Boolean> acceptedCol = new TableColumn<>("Accepted");
            acceptedCol.setCellValueFactory(new PropertyValueFactory<>("accepted"));
            acceptedCol.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        Answer answer = getTableView().getItems().get(getIndex());
                        setText(answer.isAccepted() ? "Yes" : "No");
                    }
                }
            });
            acceptedCol.setPrefWidth(80);
            acceptedCol.setStyle("-fx-alignment: CENTER;");

            // Set the columns (without action column)
            answersTable.getColumns().setAll(contentCol, authorCol, dateCol, acceptedCol);
            answersTable.setPrefHeight(300);
            
            // Set row selection behavior
            answersTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

            // Load existing answers for this question
            Answers answersManager = new Answers(databaseHelper2);
            answersTable.setItems(FXCollections.observableArrayList(
                answersManager.getAnswersForQuestion(question.getQuestionId())
            ));
            
            // Create action buttons section below the table
            Label selectionLabel = new Label("Select an answer and choose an action:");
            selectionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            
            HBox actionButtonsBox = new HBox(10);
            actionButtonsBox.setAlignment(Pos.CENTER);
            
            // View Reviews button
            Button viewReviewsBtn = new Button("View Reviews");
            styleButton(viewReviewsBtn, "#87CEEB", "#4169E1");
            viewReviewsBtn.setStyle(viewReviewsBtn.getStyle() + "; -fx-text-fill: black;");
            viewReviewsBtn.setOnAction(e -> {
                Answer selectedAnswer = answersTable.getSelectionModel().getSelectedItem();
                if (selectedAnswer != null) {
                    showAnswerReviewsDialog(selectedAnswer, question);
                } else {
                    showError("Selection Error", "Please select an answer first.");
                }
            });
            
            // Message Author button
            Button messageBtn = new Button("Message Author");
            styleButton(messageBtn, "#ADD8E6", "#4682B4");
            messageBtn.setStyle(messageBtn.getStyle() + "; -fx-text-fill: black;");
            messageBtn.setOnAction(e -> {
                Answer selectedAnswer = answersTable.getSelectionModel().getSelectedItem();
                if (selectedAnswer != null) {
                    if (!selectedAnswer.getAuthor().equals(currentUsername)) {
                        try {
                            MessageHelper.showMessageForAnswer(
                                currentUsername, 
                                selectedAnswer.getAuthor(), 
                                question.getQuestionId(), 
                                selectedAnswer.getAnswerId(),
                                databaseHelper2, 
                                questionsManager, 
                                new Answers(databaseHelper2)
                            );
                        } catch (Exception ex) {
                            showError("Message Error", "Failed to send message: " + ex.getMessage());
                        }
                    } else {
                        showError("Message Error", "You cannot message yourself.");
                    }
                } else {
                    showError("Selection Error", "Please select an answer first.");
                }
            });
            
            // Warning button for answers
            Button warningBtn = new Button("Send Warning");
            styleButton(warningBtn, "#FFA500", "#FF8C00"); // Orange color for warning
            warningBtn.setStyle(warningBtn.getStyle() + "; -fx-text-fill: black;");
            warningBtn.setOnAction(e -> {
                Answer selectedAnswer = answersTable.getSelectionModel().getSelectedItem();
                if (selectedAnswer != null) {
                    showAnswerWarningDialog(selectedAnswer, question);
                } else {
                    showError("Selection Error", "Please select an answer first.");
                }
            });
            
            actionButtonsBox.getChildren().addAll(viewReviewsBtn, messageBtn, warningBtn);
            
            VBox actionSection = new VBox(5);
            actionSection.setAlignment(Pos.CENTER);
            actionSection.getChildren().addAll(selectionLabel, actionButtonsBox);

            content.getChildren().addAll(questionLabel, infoLabel, answersTable, actionSection);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
        } catch (SQLException e) {
            showError("Error", "Failed to load answers: " + e.getMessage());
        }
    }
    
    private void showQuestionReviewsDialog(Question question) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Question Reviews");
        dialog.setHeaderText("Reviews for Question #" + question.getQuestionId());

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Question content display
        Label questionLabel = new Label("Question: " + question.getContent());
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-font-weight: bold;");

        TableView<Review> reviewsTable = new TableView<>();

        // Reviewer Column
        TableColumn<Review, String> reviewerCol = new TableColumn<>("Reviewed By");
        reviewerCol.setCellValueFactory(new PropertyValueFactory<>("reviewer"));
        reviewerCol.setPrefWidth(150);

        // Review Content Column
        TableColumn<Review, String> reviewCol = new TableColumn<>("Review");
        reviewCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        reviewCol.setPrefWidth(300);

        // Date Column
        TableColumn<Review, Date> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(DATE_FORMAT.format(item));
                }
            }
        });
        dateCol.setPrefWidth(150);

        reviewsTable.getColumns().addAll(reviewerCol, reviewCol, dateCol);

        // Create action buttons row
        HBox actionButtonsRow = new HBox(10);
        actionButtonsRow.setAlignment(Pos.CENTER);

        // Message button
        Button messageBtn = new Button("Message Reviewer");
        styleButton(messageBtn, "#ADD8E6", "#4682B4");
        messageBtn.setStyle(messageBtn.getStyle() + "; -fx-text-fill: black;");
        messageBtn.setDisable(true);

        // Add selection listener to enable/disable buttons based on selection
        reviewsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String reviewer = newSelection.getReviewer();
                messageBtn.setDisable(reviewer.equals(currentUsername));
                
                // Set up button action
                messageBtn.setOnAction(e -> {
                    try {
                        MessageHelper.showMessageForReview(
                            currentUsername, 
                            reviewer, 
                            question.getQuestionId(), 
                            newSelection.getReviewId(),
                            databaseHelper2, 
                            databaseHelper3, 
                            questionsManager
                        );
                    } catch (Exception ex) {
                        showError("Message Error", "Failed to send message: " + ex.getMessage());
                    }
                });
            } else {
                messageBtn.setDisable(true);
            }
        });

        actionButtonsRow.getChildren().add(messageBtn);

        try {
            List<Review> reviews = databaseHelper3.getReviewsForQuestion(question.getQuestionId());
            reviewsTable.setItems(FXCollections.observableArrayList(reviews));
        } catch (SQLException e) {
            showError("Error", "Failed to load reviews: " + e.getMessage());
        }

        content.getChildren().addAll(questionLabel, reviewsTable, actionButtonsRow);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    private void showAnswerReviewsDialog(Answer answer, Question question) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Answer Reviews");
        dialog.setHeaderText("Reviews for Answer by " + answer.getAuthor());

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Answer content display
        Label answerLabel = new Label("Answer: " + answer.getContent());
        answerLabel.setWrapText(true);
        answerLabel.setStyle("-fx-font-weight: bold;");

        TableView<Review> reviewsTable = new TableView<>();

        // Reviewer Column
        TableColumn<Review, String> reviewerCol = new TableColumn<>("Reviewed By");
        reviewerCol.setCellValueFactory(new PropertyValueFactory<>("reviewer"));
        reviewerCol.setPrefWidth(150);

        // Review Content Column
        TableColumn<Review, String> reviewCol = new TableColumn<>("Review");
        reviewCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        reviewCol.setPrefWidth(300);

        // Date Column
        TableColumn<Review, Date> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(DATE_FORMAT.format(item));
                }
            }
        });
        dateCol.setPrefWidth(150);

        // Action Column for messaging
        TableColumn<Review, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Review review = getTableView().getItems().get(getIndex());
                    String reviewer = review.getReviewer();
                    
                    // Only show message button if not your own review
                    if (!reviewer.equals(currentUsername)) {
                        Button messageBtn = new Button("Message Reviewer");
                        styleButton(messageBtn, "#ADD8E6", "#4682B4");
                        messageBtn.setOnAction(e -> {
                            try {
                                MessageHelper.showMessageForReview(
                                    currentUsername, 
                                    reviewer, 
                                    question.getQuestionId(), 
                                    review.getReviewId(),
                                    databaseHelper2, 
                                    databaseHelper3, 
                                    questionsManager
                                );
                            } catch (Exception ex) {
                                showError("Message Error", "Failed to send message: " + ex.getMessage());
                            }
                        });
                        
                        HBox box = new HBox(5);
                        box.getChildren().add(messageBtn);
                        setGraphic(box);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        actionCol.setPrefWidth(150);

        reviewsTable.getColumns().addAll(reviewerCol, reviewCol, dateCol, actionCol);

        try {
            List<Review> reviews = databaseHelper3.getReviewsForAnswer(answer.getAnswerId());
            reviewsTable.setItems(FXCollections.observableArrayList(reviews));
        } catch (SQLException e) {
            showError("Error", "Failed to load reviews: " + e.getMessage());
        }

        content.getChildren().addAll(answerLabel, reviewsTable);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    // Helper to truncate strings
    private String truncateString(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Shows a confirmation dialog for issuing a formal warning for a question
     * 
     * @param question The question for which a warning is being issued
     */
    private void showQuestionWarningDialog(Question question) {
        // Don't send warnings for instructor's own questions
        if (question.getAuthor().equals(currentUsername)) {
            showError("Error", "You cannot issue warnings for your own content.");
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Issue Formal Warning");
        confirmDialog.setHeaderText("Issue a formal warning to " + question.getAuthor());
        confirmDialog.setContentText("Are you sure you want to issue a formal warning for Question #" + 
                                   question.getQuestionId() + "?\n\nThis action will send an official warning " +
                                   "to the student's inbox informing them of their misconduct.");

        confirmDialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    // Send the warning
                    sendQuestionWarning(question);
                    showSuccess("Warning Sent", "A formal warning has been sent to " + question.getAuthor() + ".");
                } catch (SQLException ex) {
                    showError("Error", "Failed to send warning: " + ex.getMessage());
                }
            }
        });
    }
    
    /**
     * Shows a confirmation dialog for issuing a formal warning for an answer
     * 
     * @param answer The answer for which a warning is being issued
     * @param question The associated question
     */
    private void showAnswerWarningDialog(Answer answer, Question question) {
        // Don't send warnings for instructor's own answers
        if (answer.getAuthor().equals(currentUsername)) {
            showError("Error", "You cannot issue warnings for your own content.");
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Issue Formal Warning");
        confirmDialog.setHeaderText("Issue a formal warning to " + answer.getAuthor());
        confirmDialog.setContentText("Are you sure you want to issue a formal warning for Answer #" + 
                                   answer.getAnswerId() + "?\n\nThis action will send an official warning " +
                                   "to the student's inbox informing them of their misconduct.");

        confirmDialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    // Send the warning
                    sendAnswerWarning(answer, question);
                    showSuccess("Warning Sent", "A formal warning has been sent to " + answer.getAuthor() + ".");
                } catch (SQLException ex) {
                    showError("Error", "Failed to send warning: " + ex.getMessage());
                }
            }
        });
    }
    
    /**
     * Sends a formal warning to a student about a question
     * 
     * @param question The question related to the warning
     */
    private void sendQuestionWarning(Question question) throws SQLException {
        // Prepare warning message
        String warningMessage = String.format(
            "FORMAL WARNING\n\n" +
            "This is an official warning regarding your question (ID #%d):\n\n" +
            "\"%s\"\n\n" +
            "This content has been flagged for review by an instructor. This type of content may violate " +
            "our community guidelines. Please review the guidelines and ensure all future contributions " +
            "comply with our standards.\n\n" +
            "Note: Repeated violations may result in restrictions to your account privileges or academic penalties. " +
            "If you believe this warning was issued in error, please contact the instructor directly.",
            question.getQuestionId(),
            question.getContent()
        );
        
        // Create a feedback/message to the student
        Feedback warning = new Feedback(
            0,
            "SYSTEM WARNING", // Special sender name to highlight importance
            question.getAuthor(), // Receiver is the question author
            warningMessage,
            new Date(),
            question.getQuestionId(),
            null,  // No answer reference
            null,  // No review reference
            null   // Not a reply
        );
        
        // Add the warning to the database
        databaseHelper2.addFeedback(warning);
        
        // Check if student has received 4 or more warnings
        int warningCount = databaseHelper2.getSystemWarningCount(question.getAuthor());
        if (warningCount >= 4) {
            // Auto-ban after 4 warnings
            String banReason = "Automatic ban after receiving 4 system warnings for violating community guidelines.";
            databaseHelper3.banStudent(question.getAuthor(), currentUsername, banReason);
            
            // Notify the student about the ban
            String banMessage = String.format(
                "ACCOUNT BANNED\n\n" +
                "Your account has been automatically banned after receiving %d warnings for violating community guidelines.\n\n" +
                "You can still access the system to view content and messages, but most interactive features have been disabled.\n\n" +
                "If you believe this action was taken in error, please contact the administration.",
                warningCount
            );
            
            Feedback banNotification = new Feedback(
                0,
                "SYSTEM NOTIFICATION", 
                question.getAuthor(),
                banMessage,
                new Date(),
                0,  // No specific question reference
                null,  // No answer reference
                null,  // No review reference
                null   // Not a reply
            );
            
            databaseHelper2.addFeedback(banNotification);
            
            // Show success message to instructor
            showSuccess("Student Banned", question.getAuthor() + " has been automatically banned after receiving " + warningCount + " warnings.");
        }
    }
    
    /**
     * Sends a formal warning to a student about an answer
     * 
     * @param answer The answer related to the warning
     * @param question The associated question
     */
    private void sendAnswerWarning(Answer answer, Question question) throws SQLException {
        // Prepare warning message
        String warningMessage = String.format(
            "FORMAL WARNING\n\n" +
            "This is an official warning regarding your answer (ID #%d) to question #%d:\n\n" +
            "\"%s\"\n\n" +
            "This content has been flagged for review by an instructor. This type of content may violate " +
            "our community guidelines. Please review the guidelines and ensure all future contributions " +
            "comply with our standards.\n\n" +
            "Note: Repeated violations may result in restrictions to your account privileges or academic penalties. " +
            "If you believe this warning was issued in error, please contact the instructor directly.",
            answer.getAnswerId(),
            question.getQuestionId(),
            answer.getContent()
        );
        
        // Create a feedback/message to the student
        Feedback warning = new Feedback(
            0,
            "SYSTEM WARNING", // Special sender name to highlight importance
            answer.getAuthor(), // Receiver is the answer author
            warningMessage,
            new Date(),
            question.getQuestionId(),
            answer.getAnswerId(),  // Reference to the answer
            null,  // No review reference
            null   // Not a reply
        );
        
        // Add the warning to the database
        databaseHelper2.addFeedback(warning);
        
        // Check if student has received 4 or more warnings
        int warningCount = databaseHelper2.getSystemWarningCount(answer.getAuthor());
        if (warningCount >= 4) {
            // Auto-ban after 4 warnings
            String banReason = "Automatic ban after receiving 4 system warnings for violating community guidelines.";
            databaseHelper3.banStudent(answer.getAuthor(), currentUsername, banReason);
            
            // Notify the student about the ban
            String banMessage = String.format(
                "ACCOUNT BANNED\n\n" +
                "Your account has been automatically banned after receiving %d warnings for violating community guidelines.\n\n" +
                "You can still access the system to view content and messages, but most interactive features have been disabled.\n\n" +
                "If you believe this action was taken in error, please contact the administration.",
                warningCount
            );
            
            Feedback banNotification = new Feedback(
                0,
                "SYSTEM NOTIFICATION", 
                answer.getAuthor(),
                banMessage,
                new Date(),
                0,  // No specific question reference
                null,  // No answer reference
                null,  // No review reference
                null   // Not a reply
            );
            
            databaseHelper2.addFeedback(banNotification);
            
            // Show success message to instructor
            showSuccess("Student Banned", answer.getAuthor() + " has been automatically banned after receiving " + warningCount + " warnings.");
        }
    }

    /**
     * Shows a dialog to manage banned students
     */
    private void showBannedStudentsDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Banned Students Management");
        dialog.setHeaderText("Manage Banned Students");
        dialog.getDialogPane().setPrefSize(800, 600);

        VBox content = new VBox(15);
        content.setPadding(new Insets(10));

        // Student list section
        Label allStudentsLabel = new Label("Student List");
        allStudentsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Table for displaying students
        TableView<User> studentTable = new TableView<>();
        
        // Username column
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        usernameCol.setPrefWidth(150);
        
        // Full name column
        TableColumn<User, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(200);
        
        // Email column
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(220);
        
        // Ban status column with checkboxes
        TableColumn<User, Boolean> banStatusCol = new TableColumn<>("Banned");
        banStatusCol.setCellFactory(col -> new TableCell<User, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            
            {
                checkBox.setOnAction(event -> {
                    User student = getTableView().getItems().get(getIndex());
                    if (checkBox.isSelected()) {
                        // Show confirmation dialog for banning
                        showBanConfirmationDialog(student);
                    } else {
                        // Show confirmation dialog for unbanning
                        showUnbanConfirmationDialog(student);
                    }
                });
            }
            
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User student = getTableView().getItems().get(getIndex());
                    try {
                        boolean isBanned = databaseHelper3.isStudentBanned(student.getUserName());
                        checkBox.setSelected(isBanned);
                        setGraphic(checkBox);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        setGraphic(null);
                    }
                }
            }
        });
        banStatusCol.setPrefWidth(100);
        
        studentTable.getColumns().addAll(usernameCol, nameCol, emailCol, banStatusCol);
        
        // Load students
        try {
            List<User> students = databaseHelper.getUsersByRole("student");
            studentTable.setItems(FXCollections.observableArrayList(students));
        } catch (SQLException e) {
            showError("Error", "Failed to load students: " + e.getMessage());
        }
        
        // Add tab view to show currently banned students in a separate tab
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // All Students Tab
        Tab allStudentsTab = new Tab("All Students");
        VBox allStudentsBox = new VBox(10);
        allStudentsBox.getChildren().addAll(allStudentsLabel, studentTable);
        allStudentsTab.setContent(allStudentsBox);
        
        // Banned Students Tab
        Tab bannedStudentsTab = new Tab("Currently Banned");
        VBox bannedStudentsBox = new VBox(10);
        TableView<Map<String, Object>> bannedTable = new TableView<>();
        
        // Set up banned students table columns
        TableColumn<Map<String, Object>, String> bannedUsernameCol = new TableColumn<>("Username");
        bannedUsernameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            (String)data.getValue().get("username")
        ));
        bannedUsernameCol.setPrefWidth(150);
        
        TableColumn<Map<String, Object>, String> bannedByCol = new TableColumn<>("Banned By");
        bannedByCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            (String)data.getValue().get("bannedBy")
        ));
        bannedByCol.setPrefWidth(150);
        
        TableColumn<Map<String, Object>, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            (String)data.getValue().get("reason")
        ));
        reasonCol.setPrefWidth(250);
        
        TableColumn<Map<String, Object>, Date> banDateCol = new TableColumn<>("Ban Date");
        banDateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(
            (Date)data.getValue().get("banDate")
        ));
        banDateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DATE_FORMAT.format(item));
                }
            }
        });
        banDateCol.setPrefWidth(170);
        
        // Action column for unbanning
        TableColumn<Map<String, Object>, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button unbanBtn = new Button("Unban");
            
            {
                styleButton(unbanBtn, "#90EE90", "#228B22"); // Green for unban
                unbanBtn.setOnAction(e -> {
                    Map<String, Object> student = getTableView().getItems().get(getIndex());
                    String username = (String)student.get("username");
                    
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Unban Student");
                    confirm.setHeaderText("Unban " + username);
                    confirm.setContentText("Are you sure you want to unban this student?");
                    
                    confirm.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            try {
                                databaseHelper3.unbanStudent(username);
                                
                                // Send notification to student
                                String unbanMessage = String.format(
                                    "ACCOUNT RESTRICTION REMOVED\n\n" +
                                    "Your account ban has been removed by instructor %s.\n\n" +
                                    "You now have full access to all system features again.\n\n" +
                                    "Please continue to follow community guidelines to avoid future restrictions.",
                                    currentUsername
                                );
                                
                                Feedback unbanNotification = new Feedback(
                                    0,
                                    "SYSTEM NOTIFICATION", 
                                    username,
                                    unbanMessage,
                                    new Date(),
                                    0,  // No specific question reference
                                    null,  // No answer reference
                                    null,  // No review reference
                                    null   // Not a reply
                                );
                                
                                databaseHelper2.addFeedback(unbanNotification);
                                
                                refreshBannedTable(bannedTable);
                                studentTable.refresh();
                                showSuccess("Student Unbanned", username + " has been unbanned and notified via inbox.");
                            } catch (SQLException ex) {
                                showError("Error", "Failed to unban student: " + ex.getMessage());
                            }
                        }
                    });
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(unbanBtn);
                }
            }
        });
        actionCol.setPrefWidth(100);
        
        bannedTable.getColumns().addAll(bannedUsernameCol, bannedByCol, reasonCol, banDateCol, actionCol);
        
        // Load banned students
        refreshBannedTable(bannedTable);
        
        bannedStudentsBox.getChildren().add(bannedTable);
        bannedStudentsTab.setContent(bannedStudentsBox);
        
        tabPane.getTabs().addAll(allStudentsTab, bannedStudentsTab);
        
        content.getChildren().add(tabPane);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    /**
     * Shows a confirmation dialog for banning a student
     * 
     * @param student The student to ban
     */
    private void showBanConfirmationDialog(User student) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Ban Student");
        dialog.setHeaderText("Ban " + student.getUserName());
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label promptLabel = new Label("Please provide a reason for banning this student:");
        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Enter reason for ban...");
        reasonArea.setPrefRowCount(3);
        
        content.getChildren().addAll(promptLabel, reasonArea);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return reasonArea.getText();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(reason -> {
            if (reason != null && !reason.trim().isEmpty()) {
                try {
                    databaseHelper3.banStudent(student.getUserName(), currentUsername, reason);
                    
                    // Send notification to student
                    String banMessage = String.format(
                        "ACCOUNT BANNED\n\n" +
                        "Your account has been banned by instructor %s for the following reason:\n\n" +
                        "\"%s\"\n\n" +
                        "You can still access the system to view content and messages, but most interactive features have been disabled.\n\n" +
                        "If you believe this action was taken in error, please contact the administration.",
                        currentUsername,
                        reason
                    );
                    
                    Feedback banNotification = new Feedback(
                        0,
                        "SYSTEM NOTIFICATION", 
                        student.getUserName(),
                        banMessage,
                        new Date(),
                        0,  // No specific question reference
                        null,  // No answer reference
                        null,  // No review reference
                        null   // Not a reply
                    );
                    
                    databaseHelper2.addFeedback(banNotification);
                    
                    showSuccess("Student Banned", student.getUserName() + " has been banned and notified via inbox.");
                } catch (SQLException e) {
                    showError("Error", "Failed to ban student: " + e.getMessage());
                }
            } else {
                showError("Error", "A reason is required to ban a student");
                // Reset the checkbox since the ban was not applied
                try {
                    databaseHelper3.unbanStudent(student.getUserName());
                } catch (SQLException e) {
                    // Ignore
                }
            }
        });
    }
    
    /**
     * Shows a confirmation dialog for unbanning a student
     * 
     * @param student The student to unban
     */
    private void showUnbanConfirmationDialog(User student) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Unban Student");
        confirm.setHeaderText("Unban " + student.getUserName());
        confirm.setContentText("Are you sure you want to unban this student?");
        
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    databaseHelper3.unbanStudent(student.getUserName());
                    
                    // Send notification to student
                    String unbanMessage = String.format(
                        "ACCOUNT RESTRICTION REMOVED\n\n" +
                        "Your account ban has been removed by instructor %s.\n\n" +
                        "You now have full access to all system features again.\n\n" +
                        "Please continue to follow community guidelines to avoid future restrictions.",
                        currentUsername
                    );
                    
                    Feedback unbanNotification = new Feedback(
                        0,
                        "SYSTEM NOTIFICATION", 
                        student.getUserName(),
                        unbanMessage,
                        new Date(),
                        0,  // No specific question reference
                        null,  // No answer reference
                        null,  // No review reference
                        null   // Not a reply
                    );
                    
                    databaseHelper2.addFeedback(unbanNotification);
                    
                    showSuccess("Student Unbanned", student.getUserName() + " has been unbanned and notified via inbox.");
                } catch (SQLException e) {
                    showError("Error", "Failed to unban student: " + e.getMessage());
                }
            } else {
                // Just refresh the table that should be visible in the dialog
                for (Node node : confirm.getDialogPane().getScene().getRoot().lookupAll(".table-view")) {
                    if (node instanceof TableView) {
                        ((TableView<?>) node).refresh();
                        break;
                    }
                }
            }
        });
    }
    
    /**
     * Refreshes the banned students table
     * 
     * @param bannedTable The table to refresh
     */
    private void refreshBannedTable(TableView<Map<String, Object>> bannedTable) {
        try {
            List<Map<String, Object>> bannedStudents = databaseHelper3.getBannedStudents();
            bannedTable.setItems(FXCollections.observableArrayList(bannedStudents));
        } catch (SQLException e) {
            showError("Error", "Failed to load banned students: " + e.getMessage());
        }
    }

    private void showReviewerScorecardDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage Reviewer Scorecards");
        dialog.setHeaderText("Set and update reviewer scorecard parameters");

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Create table for reviewer scorecards
        TableView<Map<String, Object>> scorecardTable = new TableView<>();
        
        // Reviewer Column
        TableColumn<Map<String, Object>, String> reviewerCol = new TableColumn<>("Reviewer");
        reviewerCol.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("reviewer_username")));
        reviewerCol.setPrefWidth(150);

        // Friendliness Column
        TableColumn<Map<String, Object>, Integer> friendlinessCol = new TableColumn<>("Friendliness");
        friendlinessCol.setCellValueFactory(data -> new SimpleObjectProperty<>((Integer) data.getValue().get("friendliness")));
        friendlinessCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
        friendlinessCol.setPrefWidth(100);

        // Accuracy Column
        TableColumn<Map<String, Object>, Integer> accuracyCol = new TableColumn<>("Accuracy");
        accuracyCol.setCellValueFactory(data -> new SimpleObjectProperty<>((Integer) data.getValue().get("accuracy")));
        accuracyCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
        accuracyCol.setPrefWidth(100);

        // Judgement Column
        TableColumn<Map<String, Object>, Integer> judgementCol = new TableColumn<>("Judgement");
        judgementCol.setCellValueFactory(data -> new SimpleObjectProperty<>((Integer) data.getValue().get("judgement")));
        judgementCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
        judgementCol.setPrefWidth(100);

        // Communication Column
        TableColumn<Map<String, Object>, Integer> communicationCol = new TableColumn<>("Communication");
        communicationCol.setCellValueFactory(data -> new SimpleObjectProperty<>((Integer) data.getValue().get("communication")));
        communicationCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
        communicationCol.setPrefWidth(100);

        // Overall Score Column
        TableColumn<Map<String, Object>, Double> overallCol = new TableColumn<>("Overall Score");
        overallCol.setCellValueFactory(data -> new SimpleObjectProperty<>((Double) data.getValue().get("overall_score")));
        overallCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        overallCol.setPrefWidth(100);

        // Action Column
        TableColumn<Map<String, Object>, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            {
                editButton.setOnAction(e -> {
                    Map<String, Object> scorecard = getTableView().getItems().get(getIndex());
                    showEditScorecardDialog(scorecard, scorecardTable);
                });
                styleButton(editButton, "#90EE90", "#228B22");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });
        actionCol.setPrefWidth(100);

        scorecardTable.getColumns().addAll(reviewerCol, friendlinessCol, accuracyCol, 
                                         judgementCol, communicationCol, overallCol, actionCol);

        // Add button to create new scorecard
        Button addButton = new Button("Add New Scorecard");
        styleButton(addButton, "#90EE90", "#228B22");
        addButton.setOnAction(e -> showEditScorecardDialog(null, scorecardTable));

        // Load existing scorecards
        try {
            scorecardTable.setItems(FXCollections.observableArrayList(databaseHelper3.getAllReviewerScorecards()));
        } catch (SQLException e) {
            showError("Error", "Failed to load reviewer scorecards: " + e.getMessage());
        }

        content.getChildren().addAll(scorecardTable, addButton);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showEditScorecardDialog(Map<String, Object> existingScorecard, TableView<Map<String, Object>> scorecardTable) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle(existingScorecard == null ? "Add New Scorecard" : "Edit Scorecard");

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Reviewer selection dropdown
        ComboBox<String> reviewerComboBox = new ComboBox<>();
        reviewerComboBox.setPromptText("Select Reviewer");
        reviewerComboBox.setPrefWidth(200);

        try {
            List<User> reviewers = databaseHelper.getUsersByRole("reviewer");
            List<String> reviewerUsernames = reviewers.stream()
                                                    .map(User::getUserName)
                                                    .collect(java.util.stream.Collectors.toList());
            reviewerComboBox.setItems(FXCollections.observableArrayList(reviewerUsernames));
        } catch (SQLException e) {
            showError("Error", "Failed to load reviewers: " + e.getMessage());
            // Optionally disable the dialog or show an error message within it
        }

        if (existingScorecard != null) {
            reviewerComboBox.setValue((String) existingScorecard.get("reviewer_username"));
            reviewerComboBox.setDisable(true); // Disable editing the reviewer for existing scorecards
        }

        // Sliders for each parameter
        Slider friendlinessSlider = createParameterSlider("Friendliness", existingScorecard != null ? (Integer) existingScorecard.get("friendliness") : 3);
        Slider accuracySlider = createParameterSlider("Accuracy", existingScorecard != null ? (Integer) existingScorecard.get("accuracy") : 3);
        Slider judgementSlider = createParameterSlider("Judgement", existingScorecard != null ? (Integer) existingScorecard.get("judgement") : 3);
        Slider communicationSlider = createParameterSlider("Communication", existingScorecard != null ? (Integer) existingScorecard.get("communication") : 3);

        content.getChildren().addAll(
            new Label("Reviewer:"), reviewerComboBox,
            new Label("Friendliness:"), friendlinessSlider,
            new Label("Accuracy:"), accuracySlider,
            new Label("Judgement:"), judgementSlider,
            new Label("Communication:"), communicationSlider
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Ensure a reviewer is selected before proceeding
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (reviewerComboBox.getValue() == null || reviewerComboBox.getValue().isEmpty()) {
                showError("Validation Error", "Please select a reviewer.");
                event.consume(); // Prevent the dialog from closing
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                Map<String, Object> result = new HashMap<>();
                result.put("reviewer_username", reviewerComboBox.getValue()); // Get value from ComboBox
                result.put("friendliness", (int) friendlinessSlider.getValue());
                result.put("accuracy", (int) accuracySlider.getValue());
                result.put("judgement", (int) judgementSlider.getValue());
                result.put("communication", (int) communicationSlider.getValue());
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
                databaseHelper3.updateReviewerScorecard(
                    (String) result.get("reviewer_username"),
                    (Integer) result.get("friendliness"),
                    (Integer) result.get("accuracy"),
                    (Integer) result.get("judgement"),
                    (Integer) result.get("communication")
                );
                showSuccess("Success", "Scorecard " + (existingScorecard == null ? "added" : "updated") + " successfully!");
                // Refresh the table after add/update
                scorecardTable.setItems(FXCollections.observableArrayList(databaseHelper3.getAllReviewerScorecards()));
            } catch (SQLException e) {
                showError("Error", "Failed to save scorecard: " + e.getMessage());
            }
        });
    }

    private Slider createParameterSlider(String label, int initialValue) {
        Slider slider = new Slider(0, 5, initialValue);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setPrefWidth(300);
        return slider;
    }

    private void showMessageForQuestion(String receiver, int questionId) {
        // Don't message yourself
        if (receiver.equals(currentUsername)) {
            showError("Message Error", "You cannot message yourself.");
            return;
        }
        try {
            MessageHelper.showMessageForQuestion(currentUsername, receiver, questionId, 
                                               databaseHelper2, questionsManager);
        } catch (Exception ex) {
            showError("Message Error", "Failed to send message: " + ex.getMessage());
        }
    }
}
