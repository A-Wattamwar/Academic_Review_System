package main;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import databasePart1.*;
import java.sql.SQLException;
import java.util.Date;
import javafx.collections.FXCollections;
import java.util.List;
import java.util.Map;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import javafx.scene.Node;

/**
 * StaffHomePage class represents the home page for users with the staff role.
 * This page displays a welcome message, a search section, a questions section, an inbox button, and a logout button.
 */
public class StaffHomePage {
    private final DatabaseHelper databaseHelper;
    private final DatabaseHelper2 databaseHelper2;
    private final DatabaseHelper3 databaseHelper3;
    private final DatabaseHelper4 databaseHelper4;
    private final String currentUsername;
    private Questions questionsManager;
    private Answers answersManager;
    private TableView<Question> questionTable;
    private static final java.text.SimpleDateFormat DATE_FORMAT = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss");

    private AdminRequestsPage adminRequestsPage;

    public StaffHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.currentUsername = "Staff"; // Default username for staff
        
        // Initialize DatabaseHelper2 and DatabaseHelper3
        this.databaseHelper2 = new DatabaseHelper2();
        this.databaseHelper3 = new DatabaseHelper3();
        this.databaseHelper4 = new DatabaseHelper4();
        try {
            this.databaseHelper2.connectToDatabase();
            this.databaseHelper3.connectToDatabase();
            this.databaseHelper4.connectToDatabase();
            this.questionsManager = new Questions(databaseHelper2, databaseHelper2.connection);
            this.answersManager = new Answers(databaseHelper2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20; " +
                "-fx-background-color: linear-gradient(to bottom, #4169E1, #87CEEB);");

        // Welcome Label
        Label welcomeLabel = new Label("Welcome, " + currentUsername + "!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Search Section
        VBox searchSection = createSearchSection();

        // Questions Section
        VBox questionsSection = createQuestionsSection();

        // Inbox Button
        Button inboxButton = new Button("Inbox");
        styleButton(inboxButton, "#ADD8E6", "#4682B4");
        inboxButton.setOnAction(e -> showInboxDialog());

        // Banned Students Button
        Button bannedStudentsButton = new Button("Banned Students");
        styleButton(bannedStudentsButton, "#800000", "#A52A2A"); // Dark red
        bannedStudentsButton.setStyle(bannedStudentsButton.getStyle() + "; -fx-text-fill: white;");
        bannedStudentsButton.setOnAction(e -> showBannedStudentsDialog());

        // View Admin Requests Button
        Button viewAdminRequestsButton = new Button("View Admin Requests");
        styleButton(viewAdminRequestsButton, "#FFD700", "#DAA520"); // Gold/Yellow style
        viewAdminRequestsButton.setOnAction(e -> {
            if (adminRequestsPage == null) {
                adminRequestsPage = new AdminRequestsPage(databaseHelper4, databaseHelper, currentUsername, primaryStage, primaryStage.getScene());
            }
            adminRequestsPage.show();
        });

        // Logout Button
        Button logoutButton = LogoutHelper.createLogoutButton(primaryStage, databaseHelper);
        styleButton(logoutButton, "#FFB6C1", "#FF0000");

        layout.getChildren().addAll(
            welcomeLabel,
            searchSection,
            questionsSection,
            inboxButton,
            bannedStudentsButton,
            viewAdminRequestsButton,
            logoutButton
        );

        Scene staffScene = new Scene(layout, 1200, 600);
        primaryStage.setScene(staffScene);
        primaryStage.setTitle("Staff Dashboard");
        
        // Add window close handler to clean up database connections
        primaryStage.setOnCloseRequest(event -> {
            closeDatabaseConnections();
        });
        
        primaryStage.show();

        // Initial load of questions
        refreshQuestions();
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

    private VBox createSearchSection() {
        VBox searchSection = new VBox(10);
        searchSection.setAlignment(Pos.CENTER);

        // Search row
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER);

        TextField searchField = new TextField();
        searchField.setPromptText("Search questions...");
        searchField.setPrefWidth(300);

        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: #1a4b78; "
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #1a4b78; "
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");

        searchButton.setOnAction(e -> {
            try {
                String keyword = searchField.getText();
                if (!keyword.isEmpty()) {
                    questionTable.setItems(FXCollections.observableArrayList(
                        questionsManager.searchQuestions(keyword)
                    ));
                } else {
                    refreshQuestions();
                }
            } catch (SQLException ex) {
                showError("Search Error", ex.getMessage());
            }
        });
        
        searchRow.getChildren().addAll(searchField, searchButton);
        searchSection.getChildren().add(searchRow);
        return searchSection;
    }

    private VBox createQuestionsSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));

        Label titleLabel = new Label("Questions");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        questionTable = new TableView<>();
        setupQuestionTable();

        // Create action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);

        Button viewBtn = new Button("View");
        Button reportBtn = new Button("Report");
        Button warningBtn = new Button("Send Warning");

        styleButton(viewBtn, "#90EE90", "#228B22"); // Light green for view
        styleButton(reportBtn, "#FF6347", "#B22222"); // Tomato color for report
        styleButton(warningBtn, "#FFA500", "#FF8C00"); // Orange color for warning

        // Add action handlers
        viewBtn.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                showQuestionDetails(selectedQuestion);
            } else {
                showError("Selection Required", "Please select a question first.");
            }
        });

        reportBtn.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                showReportDialog(selectedQuestion);
            } else {
                showError("Selection Required", "Please select a question to report.");
            }
        });
        
        warningBtn.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                showWarningConfirmationDialog(selectedQuestion);
            } else {
                showError("Selection Required", "Please select a question to issue a warning for.");
            }
        });

        actionButtons.getChildren().addAll(viewBtn, reportBtn, warningBtn);
        section.getChildren().addAll(titleLabel, questionTable, actionButtons);
        return section;
    }

    private void setupQuestionTable() {
        // Content Column
        TableColumn<Question, String> contentCol = new TableColumn<>("Question");
        contentCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContent()));
        contentCol.setPrefWidth(400);

        // ID Column
        TableColumn<Question, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("questionId"));
        idCol.setPrefWidth(60);

        // Author Column
        TableColumn<Question, String> authorCol = new TableColumn<>("Asked By");
        authorCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(120);

        // Date Column
        TableColumn<Question, Date> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("timestamp"));
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
        statusCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("answered"));
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

        questionTable.getColumns().setAll(contentCol, idCol, authorCol, dateCol, statusCol);
        
        // Enable row selection
        questionTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void showQuestionDetails(Question question) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Question Details");
        dialog.setHeaderText("Question #" + question.getQuestionId());

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Question content display
        Label questionLabel = new Label("Question: " + question.getContent());
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-font-weight: bold;");

        // Asked by and date information
        Label infoLabel = new Label("Asked by: " + question.getAuthor() + " on " + DATE_FORMAT.format(question.getTimestamp()));
        
        // Add button to view answers and reviews
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);
        
        Button viewAnswersBtn = new Button("View Answers");
        styleButton(viewAnswersBtn, "#90EE90", "#228B22");
        viewAnswersBtn.setOnAction(e -> showAnswersDialog(question));
        
        Button viewReviewsBtn = new Button("View Reviews");
        styleButton(viewReviewsBtn, "#87CEEB", "#4169E1");
        viewReviewsBtn.setOnAction(e -> showReviewsDialog(question));
        
        actionButtons.getChildren().addAll(viewAnswersBtn, viewReviewsBtn);
        
        content.getChildren().addAll(questionLabel, infoLabel, actionButtons);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    /**
     * Shows a dialog displaying reviews for a question with reviewer ratings
     * 
     * @param question The question to show reviews for
     */
    private void showReviewsDialog(Question question) {
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
        reviewerCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("reviewer"));
        reviewerCol.setPrefWidth(150);

        // Review Content Column
        TableColumn<Review, String> reviewCol = new TableColumn<>("Review");
        reviewCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("content"));
        reviewCol.setPrefWidth(300);

        // Date Column
        TableColumn<Review, Date> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("timestamp"));
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

        reviewsTable.getColumns().setAll(reviewerCol, reviewCol, dateCol);

        // Create action buttons row
        HBox actionButtonsRow = new HBox(10);
        actionButtonsRow.setAlignment(Pos.CENTER);
        actionButtonsRow.setPadding(new Insets(10, 0, 0, 0));

        // Update button
        Button updateBtn = new Button("Update");
        styleButton(updateBtn, "#90EE90", "#228B22");
        updateBtn.setStyle(updateBtn.getStyle() + "; -fx-text-fill: black;");
        updateBtn.setDisable(true);

        // Delete button
        Button deleteBtn = new Button("Delete");
        styleButton(deleteBtn, "#FFB6C1", "#FF0000");
        deleteBtn.setStyle(deleteBtn.getStyle() + "; -fx-text-fill: black;");
        deleteBtn.setDisable(true);

        // Message button
        Button messageBtn = new Button("Message");
        styleButton(messageBtn, "#ADD8E6", "#4682B4");
        messageBtn.setStyle(messageBtn.getStyle() + "; -fx-text-fill: black;");
        messageBtn.setDisable(true);

        // View Profile button
        Button viewProfileBtn = new Button("View Profile");
        styleButton(viewProfileBtn, "#90EE90", "#228B22");
        viewProfileBtn.setStyle(viewProfileBtn.getStyle() + "; -fx-text-fill: black;");
        viewProfileBtn.setDisable(true);

        // Add selection listener to enable/disable buttons based on selection
        reviewsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String reviewer = newSelection.getReviewer();
                boolean isOwnReview = reviewer.equals(currentUsername);
                
                updateBtn.setDisable(!isOwnReview);
                deleteBtn.setDisable(!isOwnReview);
                messageBtn.setDisable(reviewer.equals(currentUsername));
                viewProfileBtn.setDisable(false);

                // Set up button actions
                updateBtn.setOnAction(e -> showUpdateReviewDialog(newSelection, reviewsTable));
                deleteBtn.setOnAction(e -> handleReviewDeletion(newSelection, reviewsTable));
                messageBtn.setOnAction(e -> showMessageForReview(reviewer, question.getQuestionId(), newSelection.getReviewId()));
                viewProfileBtn.setOnAction(e -> showReviewerProfileDialog(reviewer));
            } else {
                updateBtn.setDisable(true);
                deleteBtn.setDisable(true);
                messageBtn.setDisable(true);
                viewProfileBtn.setDisable(true);
            }
        });

        actionButtonsRow.getChildren().addAll(updateBtn, deleteBtn, messageBtn, viewProfileBtn);

        try {
            List<Review> reviews = databaseHelper3.getReviewsForQuestion(question.getQuestionId());
            reviewsTable.setItems(FXCollections.observableArrayList(reviews));
        } catch (SQLException e) {
            showError("Error", "Failed to load reviews: " + e.getMessage());
        }

        content.getChildren().addAll(questionLabel, reviewsTable, actionButtonsRow);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(800, 600);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    /**
     * Shows a dialog displaying answers for a question
     * 
     * @param question The question to show answers for
     */
    private void showAnswersDialog(Question question) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Question Answers");
        dialog.setHeaderText("Answers for Question #" + question.getQuestionId());

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
        contentCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContent()));
        contentCol.setPrefWidth(300);

        // Answered By Column
        TableColumn<Answer, String> authorCol = new TableColumn<>("Answered By");
        authorCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(100);

        // Date Column
        TableColumn<Answer, Date> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("timestamp"));
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
        acceptedCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("accepted"));
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

        answersTable.getColumns().setAll(contentCol, authorCol, dateCol, acceptedCol);

        // Create action buttons row
        HBox actionButtonsRow = new HBox(10);
        actionButtonsRow.setAlignment(Pos.CENTER);
        actionButtonsRow.setPadding(new Insets(10, 0, 0, 0));

        // View Reviews button
        Button reviewsBtn = new Button("View Reviews");
        styleButton(reviewsBtn, "#87CEEB", "#4169E1");
        reviewsBtn.setStyle(reviewsBtn.getStyle() + "; -fx-text-fill: black;");
        reviewsBtn.setDisable(true);

        // Report button
        Button reportBtn = new Button("Report");
        styleButton(reportBtn, "#FF6347", "#B22222");
        reportBtn.setStyle(reportBtn.getStyle() + "; -fx-text-fill: black;");
        reportBtn.setDisable(true);

        // Add selection listener to enable/disable buttons based on selection
        answersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            reviewsBtn.setDisable(!hasSelection);
            reportBtn.setDisable(!hasSelection);

            if (hasSelection) {
                // Set up button actions
                reviewsBtn.setOnAction(e -> showAnswerReviewsDialog(newSelection, question));
                reportBtn.setOnAction(e -> showReportAnswerDialog(newSelection, question));
            }
        });

        actionButtonsRow.getChildren().addAll(reviewsBtn, reportBtn);

        // Load existing answers for this question
        try {
            answersTable.setItems(FXCollections.observableArrayList(
                answersManager.getAnswersForQuestion(question.getQuestionId())
            ));
        } catch (SQLException e) {
            showError("Error", "Failed to load answers: " + e.getMessage());
        }

        content.getChildren().addAll(questionLabel, infoLabel, answersTable, actionButtonsRow);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    /**
     * Shows a dialog displaying reviews for an answer with reviewer ratings
     * 
     * @param answer The answer to show reviews for
     * @param question The parent question
     */
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
        reviewerCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("reviewer"));
        reviewerCol.setPrefWidth(150);

        // Review Content Column
        TableColumn<Review, String> reviewCol = new TableColumn<>("Review");
        reviewCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("content"));
        reviewCol.setPrefWidth(300);

        // Date Column
        TableColumn<Review, Date> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("timestamp"));
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
        
        // Add Reviewer Rating Column (Overall Score only)
        TableColumn<Review, Double> ratingCol = new TableColumn<>("Reviewer Rating");
        ratingCol.setCellValueFactory(data -> {
            try {
                String reviewer = data.getValue().getReviewer();
                Map<String, Object> scorecard = databaseHelper3.getReviewerScorecard(reviewer);
                if (scorecard != null && scorecard.get("overall_score") != null) {
                    return new javafx.beans.property.SimpleDoubleProperty((Double)scorecard.get("overall_score")).asObject();
                }
                return new javafx.beans.property.SimpleDoubleProperty(0.0).asObject();
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleDoubleProperty(0.0).asObject();
            }
        });
        ratingCol.setCellFactory(col -> new TableCell<Review, Double>() {
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
        ratingCol.setPrefWidth(120);

        // Add action column for messaging reviewers
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
                    
                    Button messageBtn = new Button("Message Reviewer");
                    styleButton(messageBtn, "#ADD8E6", "#4682B4");
                    messageBtn.setOnAction(e -> showMessageForReview(reviewer, question.getQuestionId(), review.getReviewId()));
                    
                    setGraphic(messageBtn);
                }
            }
        });
        actionCol.setPrefWidth(150);

        reviewsTable.getColumns().setAll(reviewerCol, reviewCol, dateCol, ratingCol, actionCol);

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
    
    /**
     * Shows a dialog for reporting an inappropriate answer
     * 
     * @param answer The answer to be reported
     * @param question The parent question
     */
    private void showReportAnswerDialog(Answer answer, Question question) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Report Inappropriate Content");
        dialog.setHeaderText("Report Answer by " + answer.getAuthor());

        // Create the content area
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Answer content display
        Label answerLabel = new Label("Answer: " + answer.getContent());
        answerLabel.setWrapText(true);
        answerLabel.setStyle("-fx-font-weight: bold;");
        
        // Answer metadata
        Label infoLabel = new Label("Answered by: " + answer.getAuthor() + " on " + DATE_FORMAT.format(answer.getTimestamp()));
        
        // Report reason input
        Label reasonLabel = new Label("Reason for reporting:");
        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Please explain why this content is inappropriate or concerning...");
        reasonArea.setPrefRowCount(5);
        reasonArea.setPrefWidth(400);

        content.getChildren().addAll(answerLabel, infoLabel, reasonLabel, reasonArea);
        dialog.getDialogPane().setContent(content);

        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return reasonArea.getText();
            }
            return null;
        });

        // Handle the result
        dialog.showAndWait().ifPresent(reason -> {
            if (reason == null || reason.trim().isEmpty()) {
                showError("Error", "Please provide a reason for reporting this content.");
                return;
            }

            try {
                // Report the content in the database
                databaseHelper3.reportContent(currentUsername, "answer", answer.getAnswerId(), reason);
                
                // Send notification to all instructors
                sendAnswerReportNotifications(answer, question, reason);
                
                showSuccess("Report Submitted", "The content has been reported and instructors have been notified.");
            } catch (SQLException ex) {
                showError("Error", "Failed to report content: " + ex.getMessage());
            }
        });
    }
    
    /**
     * Sends notifications to all instructors about a reported answer
     * 
     * @param answer The answer that was reported
     * @param question The parent question
     * @param reason The reason for reporting
     */
    private void sendAnswerReportNotifications(Answer answer, Question question, String reason) throws SQLException {
        // Get all instructors
        List<User> instructors = databaseHelper.getUsersByRole("instructor");
        
        // Prepare report message
        String message = String.format(
            "CONTENT REPORTED: Answer #%d by %s has been reported by staff member %s.\n\n" +
            "For Question #%d: %s\n\n" +
            "Reported Answer: %s\n\n" +
            "Reason: %s",
            answer.getAnswerId(),
            answer.getAuthor(),
            currentUsername,
            question.getQuestionId(),
            question.getContent(),
            answer.getContent(),
            reason
        );
        
        // Send message to each instructor
        for (User instructor : instructors) {
            // Create a feedback/message for each instructor
            Feedback feedback = new Feedback(
                0,
                currentUsername,  // sender
                instructor.getUserName(),  // receiver
                message,
                new Date(),
                question.getQuestionId(),
                answer.getAnswerId(),  // Answer reference
                null,  // No review reference
                null   // Not a reply
            );
            
            // Add the feedback to the database
            databaseHelper2.addFeedback(feedback);
        }
    }
    
    /**
     * Shows a dialog for sending a message about a review
     * 
     * @param receiver The recipient of the message
     * @param questionId The ID of the question
     * @param reviewId The ID of the review
     */
    private void showMessageForReview(String receiver, int questionId, int reviewId) {
        // Don't message yourself
        if (receiver.equals(currentUsername)) {
            return;
        }
        MessageHelper.showMessageForReview(currentUsername, receiver, questionId, reviewId,
                                         databaseHelper2, databaseHelper3, questionsManager);
    }
    
    /**
     * Helper method to truncate long strings for display
     */
    private String truncateIfNeeded(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private void showInboxDialog() {
        try {
            Answers answersManager = new Answers(databaseHelper2);
            MessageHelper.showInboxDialog(currentUsername, databaseHelper2, databaseHelper3, 
                                        questionsManager, answersManager);
        } catch (SQLException e) {
            showError("Error", "Failed to open inbox: " + e.getMessage());
        }
    }

    /**
     * Shows a dialog for reporting inappropriate content
     * 
     * @param question The question to be reported
     */
    private void showReportDialog(Question question) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Report Inappropriate Content");
        dialog.setHeaderText("Report Question #" + question.getQuestionId());

        // Create the content area
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Question content display
        Label questionLabel = new Label("Question: " + question.getContent());
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-font-weight: bold;");
        
        // Asked by and date information
        Label infoLabel = new Label("Asked by: " + question.getAuthor() + " on " + DATE_FORMAT.format(question.getTimestamp()));
        
        // Report reason input
        Label reasonLabel = new Label("Reason for reporting:");
        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Please explain why this content is inappropriate or concerning...");
        reasonArea.setPrefRowCount(5);
        reasonArea.setPrefWidth(400);

        content.getChildren().addAll(questionLabel, infoLabel, reasonLabel, reasonArea);
        dialog.getDialogPane().setContent(content);

        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return reasonArea.getText();
            }
            return null;
        });

        // Handle the result
        dialog.showAndWait().ifPresent(reason -> {
            if (reason == null || reason.trim().isEmpty()) {
                showError("Error", "Please provide a reason for reporting this content.");
                return;
            }

            try {
                // Report the content in the database
                databaseHelper3.reportContent(currentUsername, "question", question.getQuestionId(), reason);
                
                // Send notification to all instructors
                sendReportNotifications(question, reason);
                
                showSuccess("Report Submitted", "The content has been reported and instructors have been notified.");
            } catch (SQLException ex) {
                showError("Error", "Failed to report content: " + ex.getMessage());
            }
        });
    }
    
    /**
     * Sends notifications to all instructors about reported content
     * 
     * @param question The question that was reported
     * @param reason The reason for reporting
     */
    private void sendReportNotifications(Question question, String reason) throws SQLException {
        // Get all instructors
        List<User> instructors = databaseHelper.getUsersByRole("instructor");
        
        // Prepare report message
        String message = String.format(
            "CONTENT REPORTED: Question #%d by %s has been reported by staff member %s.\n\nReported Question: %s\n\nReason: %s",
            question.getQuestionId(),
            question.getAuthor(),
            currentUsername,
            question.getContent(),
            reason
        );
        
        // Send message to each instructor
        for (User instructor : instructors) {
            // Create a feedback/message for each instructor
            Feedback feedback = new Feedback(
                0,
                currentUsername,  // sender
                instructor.getUserName(),  // receiver
                message,
                new Date(),
                question.getQuestionId(),
                null,  // No answer reference
                null,  // No review reference
                null   // Not a reply
            );
            
            // Add the feedback to the database
            databaseHelper2.addFeedback(feedback);
        }
    }

    private void refreshQuestions() {
        try {
            questionTable.setItems(FXCollections.observableArrayList(
                questionsManager.getAllQuestions()
            ));
        } catch (SQLException e) {
            showError("Error", "Failed to load questions: " + e.getMessage());
        }
    }

    private void styleButton(Button button, String bgColor, String borderColor) {
        button.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: black; "
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
     * Shows a confirmation dialog for issuing a formal warning to a student
     * 
     * @param question The question for which a warning is being issued
     */
    private void showWarningConfirmationDialog(Question question) {
        // Don't send warnings for staff's own questions
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
                    sendWarning(question);
                    showSuccess("Warning Sent", "A formal warning has been sent to " + question.getAuthor() + ".");
                } catch (SQLException ex) {
                    showError("Error", "Failed to send warning: " + ex.getMessage());
                }
            }
        });
    }
    
    /**
     * Sends a formal warning to a student
     * 
     * @param question The question related to the warning
     */
    private void sendWarning(Question question) throws SQLException {
        // Prepare warning message
        String warningMessage = String.format(
            "FORMAL WARNING\n\n" +
            "This is an official warning regarding your question (ID #%d):\n\n" +
            "\"%s\"\n\n" +
            "This content has been flagged for review by a staff member. This type of content may violate " +
            "our community guidelines. Please review the guidelines and ensure all future contributions " +
            "comply with our standards.\n\n" +
            "Note: Repeated violations may result in restrictions to your account privileges. " +
            "If you believe this warning was issued in error, please contact the administration.",
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
            
            // Show success message to staff
            showSuccess("Student Banned", question.getAuthor() + " has been automatically banned after receiving " + warningCount + " warnings.");
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
        usernameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("userName"));
        usernameCol.setPrefWidth(150);
        
        // Full name column
        TableColumn<User, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(200);
        
        // Email column
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("email"));
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
                                    "Your account ban has been removed by staff member %s.\n\n" +
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
                        "Your account has been banned by staff member %s for the following reason:\n\n" +
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
                        "Your account ban has been removed by staff member %s.\n\n" +
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

    private void showUpdateReviewDialog(Review review, TableView<Review> reviewsTable) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Update Review");
        dialog.setHeaderText("Update your review");

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Review input
        Label reviewLabel = new Label("Your Review:");
        TextArea reviewArea = new TextArea(review.getContent());
        reviewArea.setPromptText("Type your review here...");
        reviewArea.setPrefRowCount(5);

        content.getChildren().addAll(reviewLabel, reviewArea);
        dialog.getDialogPane().setContent(content);

        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return reviewArea.getText();
            }
            return null;
        });

        // Handle the result
        dialog.showAndWait().ifPresent(newContent -> {
            if (newContent == null || newContent.trim().isEmpty()) {
                showError("Error", "Review cannot be empty or just spaces.");
                return;
            }

            try {
                // Create new review instead of updating the existing one
                Review updatedReview = new Review(
                    0, // new review ID will be assigned by database
                    currentUsername,
                    "- updated. " + newContent,
                    new Date(),
                    review.getQuestionId(),
                    review.getAnswerId()
                );
                
                // Add the new review to the database
                databaseHelper3.addReview(updatedReview);
                
                // Refresh the reviews table
                if (review.getAnswerId() != null) {
                    // For answer reviews
                    try {
                        List<Review> reviews = databaseHelper3.getReviewsForAnswer(review.getAnswerId());
                        reviewsTable.setItems(FXCollections.observableArrayList(reviews));
                    } catch (SQLException ex) {
                        showError("Error", "Failed to refresh reviews: " + ex.getMessage());
                    }
                } else {
                    // For question reviews
                    try {
                        List<Review> reviews = databaseHelper3.getReviewsForQuestion(review.getQuestionId());
                        reviewsTable.setItems(FXCollections.observableArrayList(reviews));
                    } catch (SQLException ex) {
                        showError("Error", "Failed to refresh reviews: " + ex.getMessage());
                    }
                }
                
                showSuccess("Success", "Review updated successfully!");
            } catch (SQLException ex) {
                showError("Error", "Failed to update review: " + ex.getMessage());
            }
        });
    }

    private void handleReviewDeletion(Review review, TableView<Review> reviewsTable) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Review");
        alert.setHeaderText("Delete Review");
        alert.setContentText("Are you sure you want to delete this review?");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    databaseHelper3.deleteReview(review.getReviewId());
                    reviewsTable.getItems().remove(review);
                    showSuccess("Success", "Review deleted successfully!");
                } catch (SQLException e) {
                    showError("Error", "Failed to delete review: " + e.getMessage());
                }
            }
        });
    }

    private void showReviewerProfileDialog(String reviewerUsername) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Reviewer Profile");
        dialog.setHeaderText("Profile for " + reviewerUsername);

        // Create the dialog content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Profile Information Section
        VBox profileSection = new VBox(5);
        Label profileLabel = new Label("Profile Information");
        profileLabel.setStyle("-fx-font-weight: bold;");

        // About, Experience, and Specialties sections (read-only)
        Label aboutLabel = new Label("About:");
        TextArea aboutArea = new TextArea();
        aboutArea.setEditable(false);
        aboutArea.setWrapText(true);
        aboutArea.setPrefRowCount(3);

        Label experienceLabel = new Label("Experience:");
        TextArea experienceArea = new TextArea();
        experienceArea.setEditable(false);
        experienceArea.setWrapText(true);
        experienceArea.setPrefRowCount(3);

        Label specialtiesLabel = new Label("Specialties:");
        TextArea specialtiesArea = new TextArea();
        specialtiesArea.setEditable(false);
        specialtiesArea.setWrapText(true);
        specialtiesArea.setPrefRowCount(3);

        // Load profile data
        try {
            Map<String, Object> profile = databaseHelper3.getReviewerProfile(reviewerUsername);
            if (profile != null) {
                aboutArea.setText((String) profile.get("about"));
                experienceArea.setText((String) profile.get("experience"));
                specialtiesArea.setText((String) profile.get("specialties"));
            }
        } catch (SQLException e) {
            showError("Error", "Failed to load profile: " + e.getMessage());
        }

        profileSection.getChildren().addAll(
            profileLabel,
            aboutLabel, aboutArea,
            experienceLabel, experienceArea,
            specialtiesLabel, specialtiesArea
        );

        // Reviews Section
        VBox reviewsSection = new VBox(5);
        Label reviewsLabel = new Label("Reviews by " + reviewerUsername);
        reviewsLabel.setStyle("-fx-font-weight: bold;");

        TableView<Review> reviewsTable = new TableView<>();
        
        // Review Content Column
        TableColumn<Review, String> contentCol = new TableColumn<>("Review");
        contentCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContent()));
        contentCol.setPrefWidth(300);

        // Date Column
        TableColumn<Review, Date> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("timestamp"));
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

        reviewsTable.getColumns().setAll(contentCol, dateCol);

        // Load reviews
        try {
            reviewsTable.setItems(FXCollections.observableArrayList(
                databaseHelper3.getReviewsByReviewer(reviewerUsername)
            ));
        } catch (SQLException e) {
            showError("Error", "Failed to load reviews: " + e.getMessage());
        }

        reviewsSection.getChildren().addAll(reviewsLabel, reviewsTable);

        // Feedback Section
        VBox feedbackSection = new VBox(5);
        Label feedbackLabel = new Label("Feedback from Students");
        feedbackLabel.setStyle("-fx-font-weight: bold;");

        TableView<Map<String, Object>> feedbackTable = new TableView<>();
        
        // Student Column
        TableColumn<Map<String, Object>, String> studentCol = new TableColumn<>("From Student");
        studentCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("student_username")));
        studentCol.setPrefWidth(120);

        // Content Column
        TableColumn<Map<String, Object>, String> feedbackContentCol = new TableColumn<>("Feedback");
        feedbackContentCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("content")));
        feedbackContentCol.setPrefWidth(350);

        // Rating Column with Stars
        TableColumn<Map<String, Object>, Integer> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(data -> {
            Integer rating = (Integer) data.getValue().get("rating");
            return new javafx.beans.property.SimpleIntegerProperty(rating != null ? rating : 0).asObject();
        });
        ratingCol.setCellFactory(col -> new TableCell<Map<String, Object>, Integer>() {
            @Override
            protected void updateItem(Integer rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null || rating == 0) {
                    setText(null);
                } else {
                    // Convert rating to stars (★)
                    setText("★".repeat(rating) + "☆".repeat(5 - rating));
                }
            }
        });
        ratingCol.setPrefWidth(100);

        // Date Column
        TableColumn<Map<String, Object>, Date> feedbackDateCol = new TableColumn<>("Date Received");
        feedbackDateCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>((Date) data.getValue().get("timestamp")));
        feedbackDateCol.setCellFactory(col -> new TableCell<>() {
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
        feedbackDateCol.setPrefWidth(150);

        feedbackTable.getColumns().setAll(studentCol, feedbackContentCol, ratingCol, feedbackDateCol);
        feedbackTable.setPlaceholder(new Label("No feedback received yet"));

        // Load feedback
        try {
            List<Map<String, Object>> feedbackData = databaseHelper3.getReviewerFeedback(reviewerUsername);
            feedbackTable.setItems(FXCollections.observableArrayList(feedbackData));
        } catch (SQLException e) {
            showError("Error", "Failed to load feedback: " + e.getMessage());
        }

        feedbackSection.getChildren().addAll(feedbackLabel, feedbackTable);

        // Add all sections to the content
        content.getChildren().addAll(profileSection, reviewsSection, feedbackSection);

        // Set the dialog content and size
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(800, 600);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}
