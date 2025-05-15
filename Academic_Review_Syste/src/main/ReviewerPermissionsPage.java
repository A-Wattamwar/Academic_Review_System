package main;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;
import java.util.Map;
import java.util.List;
import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.util.Pair;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.collections.FXCollections;
import java.util.ArrayList;
import java.util.Arrays;
import databasePart1.DatabaseHelper2;
import databasePart1.DatabaseHelper3;

/**
 * ReviewerPermissionsPage class represents the page for managing reviewer permissions.
 * This page allows instructors to review and approve or reject reviewer requests.
 * Students can also view their own reviewer permission status.
 */
public class ReviewerPermissionsPage {
    private final DatabaseHelper databaseHelper;
    private final String currentUsername;
    private final boolean isInstructor;
    private final DatabaseHelper2 databaseHelper2;
    private final DatabaseHelper3 databaseHelper3;
    private TableView<Map<String, Object>> requestsTable;

    public ReviewerPermissionsPage(DatabaseHelper databaseHelper, String currentUsername, boolean isInstructor) {
        this.databaseHelper = databaseHelper;
        this.currentUsername = currentUsername;
        this.isInstructor = isInstructor;
        
        // Initialize DatabaseHelper2 for access to questions and answers
        this.databaseHelper2 = new DatabaseHelper2();
        this.databaseHelper3 = new DatabaseHelper3();
        try {
            this.databaseHelper2.connectToDatabase();
            this.databaseHelper3.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void show(Stage primaryStage) {
        // Background Pane with Blue Gradient
        Pane backgroundPane = new Pane();
        backgroundPane.setStyle("-fx-background-color: linear-gradient(to bottom, #4169E1, #87CEEB);"); 
        backgroundPane.setPrefSize(800, 500);

        // Main VBox Layout (Centered Content)
        VBox layout = new VBox(10);
        layout.setPrefSize(800, 500);
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        // Title label
        Label titleLabel = new Label(isInstructor ? "Reviewer Permission Requests" : "Reviewer Permission Status");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        if (isInstructor) {
            // Instructor view - Table of pending requests
            requestsTable = new TableView<>(); // Assign to class field
            requestsTable.setMaxWidth(700);
            requestsTable.setMaxHeight(300);
            requestsTable.setStyle("-fx-background-color: white; -fx-border-color: #696969;");

            // Columns
            TableColumn<Map<String, Object>, String> studentCol = new TableColumn<>("Student");
            studentCol.setCellValueFactory(cellData -> new SimpleStringProperty((String) cellData.getValue().get("studentUsername")));

            TableColumn<Map<String, Object>, String> dateCol = new TableColumn<>("Request Date");
            dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get("requestDate").toString()));

            // Action buttons column
            TableColumn<Map<String, Object>, Void> actionCol = new TableColumn<>("Action");
            actionCol.setCellFactory(param -> new TableCell<>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Map<String, Object> request = getTableView().getItems().get(getIndex());
                        
                        HBox container = new HBox(5);
                        
                        // Review Student History button
                        Button reviewHistoryBtn = new Button("Review History");
                        reviewHistoryBtn.setStyle("-fx-font-size: 12px; -fx-background-color: #90EE90; -fx-border-color: #228B22;");
                        reviewHistoryBtn.setOnAction(e -> {
                            String studentUsername = (String) request.get("studentUsername");
                            showStudentHistory(studentUsername, (Integer) request.get("requestId"));
                        });
                        
                        // Approve button
                        Button approveBtn = new Button("Approve");
                        approveBtn.setStyle("-fx-font-size: 12px; -fx-background-color: #90EE90; -fx-border-color: #228B22;");
                        approveBtn.setOnAction(e -> {
                            String studentUsername = (String) request.get("studentUsername");
                            showApprovalDialog(studentUsername, (Integer) request.get("requestId"), true);
                        });
                        
                        // Reject button
                        Button rejectBtn = new Button("Reject");
                        rejectBtn.setStyle("-fx-font-size: 12px; -fx-background-color: #FFB6C1; -fx-border-color: #FF0000;");
                        rejectBtn.setOnAction(e -> {
                            String studentUsername = (String) request.get("studentUsername");
                            showApprovalDialog(studentUsername, (Integer) request.get("requestId"), false);
                        });
                        
                        container.getChildren().addAll(reviewHistoryBtn, approveBtn, rejectBtn);
                        setGraphic(container);
                    }
                }
            });

            requestsTable.getColumns().addAll(studentCol, dateCol, actionCol);

            // Load pending requests
            try {
                List<Map<String, Object>> requests = databaseHelper3.getPendingReviewerRequests();
                requestsTable.getItems().addAll(requests);
            } catch (SQLException e) {
                System.err.println("Error loading requests: " + e.getMessage());
            }

            layout.getChildren().addAll(titleLabel, requestsTable);
        } else {
            // Student view - Status display
            VBox statusBox = new VBox(10);
            statusBox.setStyle("-fx-background-color: white; -fx-padding: 20px; -fx-background-radius: 10px;");

            try {
                Map<String, Object> status = databaseHelper3.getReviewerRequestStatus(currentUsername);
                if (status != null) {
                    Label statusLabel = new Label("Status: " + status.get("status").toString().toUpperCase());
                    statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                    Label dateLabel = new Label("Request Date: " + status.get("requestDate").toString());
                    
                    VBox notesBox = new VBox(5);
                    notesBox.setVisible(!"pending".equals(status.get("status")));
                    
                    if (!"pending".equals(status.get("status"))) {
                        Label reviewerLabel = new Label("Reviewed by: " + status.get("reviewerUsername"));
                        Label reviewDateLabel = new Label("Review Date: " + status.get("reviewDate").toString());
                        
                        Label notesLabel = new Label("Notes:");
                        TextArea notesArea = new TextArea(status.get("reviewNotes") != null ? 
                                status.get("reviewNotes").toString() : "");
                        notesArea.setEditable(false);
                        notesArea.setPrefRowCount(4);
                        notesArea.setPrefWidth(400);
                        
                        notesBox.getChildren().addAll(reviewerLabel, reviewDateLabel, notesLabel, notesArea);
                    }
                    
                    statusBox.getChildren().addAll(statusLabel, dateLabel, notesBox);
                } else {
                    // No request has been made yet
                    Label noRequestLabel = new Label("You haven't requested reviewer permissions yet.");
                    
                    Button requestButton = new Button("Request Reviewer Permission");
                    requestButton.setStyle("-fx-font-size: 13px; "
                            + "-fx-text-fill: white; "
                            + "-fx-background-color: #4682B4; "
                            + "-fx-padding: 6px 12px; "
                            + "-fx-border-color: #1E90FF; "
                            + "-fx-border-width: 2px; "
                            + "-fx-border-radius: 12px; "
                            + "-fx-background-radius: 12px;");
                    
                    requestButton.setOnAction(e -> {
                        try {
                            // Confirm request
                            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                            confirmAlert.setTitle("Request Reviewer Permission");
                            confirmAlert.setHeaderText("Request to become a reviewer");
                            confirmAlert.setContentText("Are you sure you want to request permission to be a reviewer? An instructor will review your questions and answers before approving.");
                            
                            confirmAlert.showAndWait().ifPresent(response -> {
                                if (response == ButtonType.OK) {
                                    try {
                                        databaseHelper3.submitReviewerRequest(currentUsername);
                                        showAlert(Alert.AlertType.INFORMATION, "Request Submitted", 
                                                "Your request has been submitted. An instructor will review it shortly.");
                                        // Refresh the page to show the new status
                                        show(primaryStage);
                                    } catch (SQLException ex) {
                                        showAlert(Alert.AlertType.ERROR, "Error", 
                                                "Failed to submit request: " + ex.getMessage());
                                    }
                                }
                            });
                        } catch (Exception ex) {
                            showAlert(Alert.AlertType.ERROR, "Error", 
                                    "Failed to process request: " + ex.getMessage());
                        }
                    });
                    
                    statusBox.getChildren().addAll(noRequestLabel, requestButton);
                }
            } catch (SQLException e) {
                Label errorLabel = new Label("Error loading request status: " + e.getMessage());
                errorLabel.setStyle("-fx-text-fill: red;");
                statusBox.getChildren().add(errorLabel);
            }
            
            layout.getChildren().addAll(titleLabel, statusBox);
        }

        // Back button
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-font-size: 13px; -fx-text-fill: white; -fx-background-color: #1a4b78; "
                + "-fx-padding: 6px 12px; -fx-border-color: #4169E1; -fx-border-width: 2px; "
                + "-fx-border-radius: 12px; -fx-background-radius: 12px;");
        backButton.setOnAction(e -> {
            if (isInstructor) {
                new InstructorHomePage(databaseHelper, currentUsername).show(primaryStage);
            } else {
                new StudentHomePage(databaseHelper, currentUsername).show(primaryStage);
            }
        });

        layout.getChildren().add(backButton);

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

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle(isInstructor ? "Reviewer Permission Requests" : "Reviewer Permission Status");
        primaryStage.show();

        // Close database connection when the window is closed
        primaryStage.setOnCloseRequest(event -> {
            if (databaseHelper2 != null) {
                databaseHelper2.closeConnection();
            }
        });
    }

    private void showStudentHistory(String studentUsername, int requestId) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Student History Review");
        dialog.setHeaderText("Review questions and answers by " + studentUsername);
        
        // Create a tab pane for questions and answers
        TabPane tabPane = new TabPane();
        
        // Questions tab
        Tab questionsTab = new Tab("Questions");
        questionsTab.setClosable(false);
        VBox questionsBox = new VBox(10);
        questionsBox.setPadding(new Insets(10));
        
        // Table for questions
        TableView<Question> questionsTable = new TableView<>();
        
        // Question content column
        TableColumn<Question, String> contentCol = new TableColumn<>("Content");
        contentCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getContent()));
        contentCol.setPrefWidth(350);
        
        // Date column
        TableColumn<Question, Date> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        dateCol.setCellFactory(col -> new TableCell<>() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
            
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(item));
                }
            }
        });
        dateCol.setPrefWidth(150);
        
        // Status column
        TableColumn<Question, Boolean> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("answered"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Answered" : "Unanswered");
                }
            }
        });
        statusCol.setPrefWidth(100);
        
        questionsTable.getColumns().addAll(contentCol, dateCol, statusCol);
        
        try {
            // Load questions by this student
            Questions questionsManager = new Questions(databaseHelper2, databaseHelper2.connection);
            List<Question> studentQuestions = new ArrayList<>();
            
            for (Question q : questionsManager.getAllQuestions()) {
                if (q.getAuthor().equals(studentUsername)) {
                    studentQuestions.add(q);
                }
            }
            
            questionsTable.setItems(FXCollections.observableArrayList(studentQuestions));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load student questions: " + e.getMessage());
        }
        
        questionsBox.getChildren().add(questionsTable);
        questionsTab.setContent(questionsBox);
        
        // Answers tab
        Tab answersTab = new Tab("Answers");
        answersTab.setClosable(false);
        VBox answersBox = new VBox(10);
        answersBox.setPadding(new Insets(10));
        
        // Table for answers
        TableView<Answer> answersTable = new TableView<>();
        
        // Answer content column
        TableColumn<Answer, String> answerContentCol = new TableColumn<>("Content");
        answerContentCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getContent()));
        answerContentCol.setPrefWidth(350);
        
        // Question ID column
        TableColumn<Answer, Integer> questionIdCol = new TableColumn<>("Question ID");
        questionIdCol.setCellValueFactory(new PropertyValueFactory<>("questionId"));
        questionIdCol.setPrefWidth(80);
        
        // Date column
        TableColumn<Answer, Date> answerDateCol = new TableColumn<>("Date");
        answerDateCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        answerDateCol.setCellFactory(col -> new TableCell<>() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
            
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(item));
                }
            }
        });
        answerDateCol.setPrefWidth(150);
        
        // Accepted column
        TableColumn<Answer, Boolean> acceptedCol = new TableColumn<>("Accepted");
        acceptedCol.setCellValueFactory(new PropertyValueFactory<>("accepted"));
        acceptedCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Yes" : "No");
                }
            }
        });
        acceptedCol.setPrefWidth(80);
        
        answersTable.getColumns().addAll(answerContentCol, questionIdCol, answerDateCol, acceptedCol);
        
        try {
            // Load answers by this student
            Answers answersManager = new Answers(databaseHelper2);
            List<Answer> studentAnswers = new ArrayList<>();
            
            for (Answer a : answersManager.getAllAnswers()) {
                if (a.getAuthor().equals(studentUsername)) {
                    studentAnswers.add(a);
                }
            }
            
            answersTable.setItems(FXCollections.observableArrayList(studentAnswers));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load student answers: " + e.getMessage());
        }
        
        // Add buttons for approving/rejecting based on history review
        Button approveBtn = new Button("Approve Request");
        approveBtn.setStyle("-fx-font-size: 12px; -fx-background-color: #90EE90; -fx-border-color: #228B22;");
        approveBtn.setOnAction(e -> {
            dialog.close();
            showApprovalDialog(studentUsername, requestId, true);
        });
        
        Button rejectBtn = new Button("Reject Request");
        rejectBtn.setStyle("-fx-font-size: 12px; -fx-background-color: #FFB6C1; -fx-border-color: #FF0000;");
        rejectBtn.setOnAction(e -> {
            dialog.close();
            showApprovalDialog(studentUsername, requestId, false);
        });
        
        HBox buttonBox = new HBox(10, approveBtn, rejectBtn);
        buttonBox.setAlignment(Pos.CENTER);
        
        answersBox.getChildren().addAll(answersTable, buttonBox);
        answersTab.setContent(answersBox);
        
        tabPane.getTabs().addAll(questionsTab, answersTab);
        
        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().setPrefSize(700, 500);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }

    private void showApprovalDialog(String studentUsername, int requestId, boolean isApproved) {
        Dialog<Pair<String, Boolean>> dialog = new Dialog<>();
        dialog.setTitle(isApproved ? "Approve Request" : "Reject Request");
        dialog.setHeaderText(isApproved ? 
                "Approve reviewer permission for " + studentUsername : 
                "Reject reviewer permission for " + studentUsername);
        
        // Set button types
        ButtonType confirmButton = new ButtonType(isApproved ? "Approve" : "Reject", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, ButtonType.CANCEL);
        
        // Create the content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Notes field
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Enter notes about this decision...");
        notesArea.setPrefRowCount(5);
        
        grid.add(new Label("Notes:"), 0, 0);
        grid.add(notesArea, 1, 0);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButton) {
                return new Pair<>(notesArea.getText(), isApproved);
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            try {
                // Update the request status in the database
                String status = result.getValue() ? "approved" : "rejected";
                String notes = result.getKey();
                
                // First, update the request status
                databaseHelper3.updateReviewerRequestStatus(requestId, status, currentUsername, notes);
                
                // If approved, also update the user's roles to include "reviewer"
                if (result.getValue()) {
                    String[] currentRoles = databaseHelper.getUserRoles(studentUsername);
                    List<String> rolesList = new ArrayList<>(Arrays.asList(currentRoles));
                    
                    // Add reviewer role if not already present
                    if (!rolesList.contains("reviewer")) {
                        rolesList.add("reviewer");
                        databaseHelper.updateUserRoles(studentUsername, rolesList.toArray(new String[0]), currentUsername);
                    }
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Request has been " + status + " successfully.");
                
                // Refresh the table
                refreshRequests();
                
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to process request: " + e.getMessage());
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void refreshRequests() {
        try {
            if (requestsTable != null) {
                requestsTable.getItems().clear();
                requestsTable.getItems().addAll(databaseHelper3.getPendingReviewerRequests());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to refresh requests: " + e.getMessage());
        }
    }

    @Override
    public void finalize() {
        if (databaseHelper2 != null) {
            databaseHelper2.closeConnection();
        }
    }
} 