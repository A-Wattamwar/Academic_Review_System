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
import javafx.collections.FXCollections;
import java.util.List;
import java.util.Map;
import javafx.scene.Node;

/**
 * StudentHomePage class represents the home page for users with the student role.
 * This page displays a welcome message, a search section, a questions section, an inbox button, and a logout button.
 */
public class StudentHomePage {
    private final DatabaseHelper databaseHelper;
    private final DatabaseHelper2 databaseHelper2;
    private final DatabaseHelper3 databaseHelper3;
    private final String currentUsername;
    private Questions questionsManager;
    private Answers answersManager;
    private TableView<Question> questionTable;
    private TableView<Answer> answersTable; // Table for managing answers
    private static final java.text.SimpleDateFormat DATE_FORMAT = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss");

    public StudentHomePage(DatabaseHelper databaseHelper, String username) {
        this.databaseHelper = databaseHelper;
        this.currentUsername = username;
        
        // Initialize DatabaseHelper2 and DatabaseHelper3
        this.databaseHelper2 = new DatabaseHelper2();
        this.databaseHelper3 = new DatabaseHelper3();
        try {
            this.databaseHelper2.connectToDatabase();
            this.databaseHelper3.connectToDatabase();
            this.questionsManager = new Questions(databaseHelper2, databaseHelper2.connection);
            this.answersManager = new Answers(databaseHelper2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20; " +
                "-fx-background-color: linear-gradient(to bottom, #4169E1, #87CEEB);"); // Royal blue to sky blue

        // Welcome Label
        Label welcomeLabel = new Label("Welcome, " + currentUsername + "!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Search Section
        VBox searchSection = createSearchSection();

        // Questions Section
        VBox questionsSection = createQuestionsSection();

        // Add Reviewer Request button
        Button reviewerRequestButton = new Button("Request Reviewer Permission");
        styleButton(reviewerRequestButton, "#FFB6C1", "#FF69B4"); // Light Pink to Hot Pink gradient
        reviewerRequestButton.setOnAction(e -> handleReviewerRequest());

        // Ask to be Reviewer Button
        Button reviewerPermissionsButton = new Button("See Permission Status");
        styleButton(reviewerPermissionsButton, "#00CED1", "#008B8B"); // Turquoise to Dark Turquoise gradient
        reviewerPermissionsButton.setOnAction(e -> {
            new ReviewerPermissionsPage(databaseHelper, currentUsername, false).show(primaryStage);
        });

        // Inbox Button
        Button inboxButton = new Button("Inbox");
        styleButton(inboxButton, "#ADD8E6", "#4682B4"); // Light blue for message
        inboxButton.setOnAction(e -> {
            if (databaseHelper2 != null) {
                try {
                    showInboxDialog();
                } catch (Exception ex) {
                    showError("Error", "Failed to open inbox: " + ex.getMessage());
                }
            }
        });

        // Logout Button
        Button logoutButton = LogoutHelper.createLogoutButton(primaryStage, databaseHelper);
        styleButton(logoutButton, "#FF6B6B", "#CD5C5C"); // Lighter Red to Coral Red gradient

        layout.getChildren().addAll(
            welcomeLabel,
            searchSection,
            questionsSection,
            reviewerRequestButton,
            reviewerPermissionsButton,
            inboxButton,
            logoutButton
        );

        // Check if student is banned
        boolean isUserBanned = false;
        try {
            isUserBanned = databaseHelper3.isStudentBanned(currentUsername);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (isUserBanned) {
            // Add a warning banner at the top
            HBox bannerBox = new HBox(10);
            bannerBox.setAlignment(Pos.CENTER);
            bannerBox.setStyle("-fx-background-color: #FF6B6B; -fx-padding: 10px;");
            
            Label bannerLabel = new Label("YOUR ACCOUNT HAS BEEN BANNED");
            bannerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
            
            bannerBox.getChildren().add(bannerLabel);
            
            // Insert the banner at the top, right after the welcome label
            layout.getChildren().add(1, bannerBox);
            
            // Disable all action buttons 
            disableAllButtons(questionsSection);
            reviewerRequestButton.setDisable(true);
            reviewerPermissionsButton.setDisable(true);
            
            // Only allow viewing inbox and logout
            inboxButton.setDisable(false);
            logoutButton.setDisable(false);
        }

        Scene studentScene = new Scene(layout, 1200, 600);
        primaryStage.setScene(studentScene);
        primaryStage.setTitle("Student Dashboard");
        primaryStage.show();

        // Initial load of questions
        refreshQuestions();
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
                + "-fx-background-color: #1a4b78; " // Dark Blue Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #4169E1; " // Royal Blue Border
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
        
        // User search components
        TextField userSearchField = new TextField();
        userSearchField.setPromptText("Search by username...");
        userSearchField.setPrefWidth(300);
        
        Button userSearchButton = new Button("Search by User");
        userSearchButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: #1a4b78; " // Dark Blue Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #4169E1; " // Royal Blue Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");
        
        userSearchButton.setOnAction(e -> {
            try {
                String username = userSearchField.getText();
                if (!username.isEmpty()) {
                    questionTable.setItems(FXCollections.observableArrayList(
                        questionsManager.searchQuestionsByUser(username)
                    ));
                } else {
                    refreshQuestions();
                }
            } catch (SQLException ex) {
                showError("User Search Error", ex.getMessage());
            }
        });
        
        searchRow.getChildren().addAll(searchField, searchButton, userSearchField, userSearchButton);
        
        // Filter row
        HBox filterRow = new HBox(10);
        filterRow.setAlignment(Pos.CENTER);
        
        Label filterLabel = new Label("Filter by:");
        filterLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;-fx-font-size: 14px;");
        
        Button allButton = new Button("All Questions");
        allButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: #1a4b78; " // Dark Blue Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #4169E1; " // Royal Blue Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");
        
        Button unresolvedButton = new Button("Unresolved");
        unresolvedButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: #1a4b78; " // Dark Blue Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #4169E1; " // Royal Blue Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");
        
        Button resolvedButton = new Button("Resolved");
        resolvedButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: #1a4b78; " // Dark Blue Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #4169E1; " // Royal Blue Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");
        
        Button recentButton = new Button("Most Recent");
        recentButton.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: #1a4b78; " // Dark Blue Background
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: #4169E1; " // Royal Blue Border
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");
        
        allButton.setOnAction(e -> refreshQuestions());
        unresolvedButton.setOnAction(e -> filterByStatus(false));
        resolvedButton.setOnAction(e -> filterByStatus(true));
        recentButton.setOnAction(e -> filterByRecent());
        
        filterRow.getChildren().addAll(
            filterLabel,
            allButton,
            unresolvedButton,
            resolvedButton,
            recentButton
        );
        
        searchSection.getChildren().addAll(searchRow, filterRow);
        return searchSection;
    }

    // Filter questions by status (resolved/unresolved)
    private void filterByStatus(boolean isResolved) {
        try {
            List<Question> filteredQuestions = questionsManager.getAllQuestions();
            filteredQuestions.removeIf(q -> q.isAnswered() != isResolved);
            questionTable.setItems(FXCollections.observableArrayList(filteredQuestions));
        } catch (SQLException e) {
            showError("Filter Error", e.getMessage());
        }
    }

    // Filter questions by recency (most recent first)
    private void filterByRecent() {
        try {
            List<Question> recentQuestions = questionsManager.getAllQuestions();
            recentQuestions.sort((q1, q2) -> q2.getTimestamp().compareTo(q1.getTimestamp()));
            questionTable.setItems(FXCollections.observableArrayList(recentQuestions));
        } catch (SQLException e) {
            showError("Filter Error", e.getMessage());
        }
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

        Button askQuestionButton = createAskQuestionButton();
        Button answerBtn = new Button("Answer");
        Button referenceBtn = new Button("Reference");
        Button messageBtn = new Button("Message Author");
        Button readReviewsBtn = new Button("Read Reviews");
        Button trustedReviewersButton = new Button("Trusted Reviewers");
        Button deleteBtn = new Button("Delete");

        styleButton(askQuestionButton, "#FF8C00", "#FF4500"); // Dark Orange to Orange Red gradient
        styleButton(answerBtn, "#90EE90", "#228B22"); // Light green for success
        styleButton(referenceBtn, "#B19CD9", "#9370DB"); // Light Purple to Medium Purple gradient
        styleButton(messageBtn, "#ADD8E6", "#4682B4"); // Light blue for message
        styleButton(readReviewsBtn, "#87CEEB", "#4169E1"); // Light sky blue for reviews
        styleButton(trustedReviewersButton, "#FFD700", "#DAA520"); // Gold color for trust
        styleButton(deleteBtn, "#FF6B6B", "#CD5C5C"); // Lighter Red to Coral Red gradient

        // Add action handlers
        answerBtn.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                showAnswerManagementDialog(selectedQuestion);
            } else {
                showError("Selection Required", "Please select a question first.");
            }
        });

        referenceBtn.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                showReferenceQuestionDialog(selectedQuestion);
            } else {
                showError("Selection Required", "Please select a question first.");
            }
        });

        readReviewsBtn.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                showReviewsDialog(selectedQuestion);
            } else {
                showError("Selection Required", "Please select a question first.");
            }
        });

        messageBtn.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                if (!selectedQuestion.getAuthor().equals(currentUsername)) {
                    MessageHelper.showMessageForQuestion(currentUsername, selectedQuestion.getAuthor(), 
                                                       selectedQuestion.getQuestionId(), databaseHelper2, questionsManager);
                } else {
                    showError("Invalid Action", "You cannot message yourself.");
                }
            } else {
                showError("Selection Required", "Please select a question first.");
            }
        });

        deleteBtn.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                if (selectedQuestion.getAuthor().equals(currentUsername)) {
                    handleQuestionDeletion(selectedQuestion);
                } else {
                    showError("Permission Denied", "You can only delete your own questions.");
                }
            } else {
                showError("Selection Required", "Please select a question first.");
            }
        });

        trustedReviewersButton.setOnAction(e -> showTrustedReviewersDialog());

        actionButtons.getChildren().addAll(
            askQuestionButton,
            answerBtn,
            referenceBtn,
            messageBtn,
            readReviewsBtn,
            trustedReviewersButton,
            deleteBtn
        );

        section.getChildren().addAll(titleLabel, questionTable, actionButtons);
        return section;
    }

    private void setupQuestionTable() {
        // Content Column
        TableColumn<Question, String> contentCol = new TableColumn<>("Question");
        contentCol.setCellValueFactory(cellData -> {
            Question question = cellData.getValue();
            String content = question.getContent();
            
            // If this question references another question, show the reference
            if (question.hasReference()) {
                try {
                    Question referencedQuestion = databaseHelper2.getQuestionById(question.getReferenceQuestionId());
                    if (referencedQuestion != null) {
                        // Format: Show referenced question in italics above the actual question
                        String referenceDisplay = String.format(
                            "↪ Re: \"%s\" (by %s)\n%s",
                            truncateIfNeeded(referencedQuestion.getContent(), 50),
                            referencedQuestion.getAuthor(),
                            content
                        );
                        return new javafx.beans.property.SimpleStringProperty(referenceDisplay);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            
            return new javafx.beans.property.SimpleStringProperty(content);
        });
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

        // Unread Answers Count Column
        TableColumn<Question, Integer> unreadCol = new TableColumn<>("Unread Answers");
        unreadCol.setCellValueFactory(cellData -> {
            Question question = cellData.getValue();
            try {
                int unreadCount = getUnreadAnswersCount(question.getQuestionId());
                return new javafx.beans.property.SimpleIntegerProperty(unreadCount).asObject();
            } catch (SQLException e) {
                e.printStackTrace();
                return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
            }
        });
        unreadCol.setPrefWidth(120);
        unreadCol.setStyle("-fx-alignment: CENTER;");

        questionTable.getColumns().setAll(contentCol, idCol, authorCol, dateCol, statusCol, unreadCol);
        questionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Enable row selection
        questionTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private Button createAskQuestionButton() {
        Button askQuestionButton = new Button("Ask a Question");
        styleButton(askQuestionButton, "#FF8C00", "#FF4500"); // Dark Orange to Orange Red gradient

        askQuestionButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Ask a Question");
            dialog.setHeaderText("Enter your question below:");
            dialog.setContentText("Question:");

            dialog.showAndWait().ifPresent(questionContent -> {
                if (questionContent == null || questionContent.trim().isEmpty()) {
                    showError("Error", "Question cannot be empty or just spaces.");
                    return;
                }
                try {
                    Question newQuestion = new Question(0, questionContent, currentUsername, new Date());
                    questionsManager.addQuestion(newQuestion);
                    refreshQuestions();
                } catch (SQLException ex) {
                    showError("Error", "Failed to add question: " + ex.getMessage());
                }
            });
        });

        return askQuestionButton;
    }

    private void showAnswerManagementDialog(Question question) {
        // Mark answers as read when dialog is opened
        markAnswersAsRead(question.getQuestionId());
        
        Stage dialog = new Stage();
        dialog.setTitle("Answers for Question #" + question.getQuestionId());
        
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.setStyle("-fx-background-color: white;");

        // Question details
        Label questionLabel = new Label("Question: " + question.getContent());
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-font-weight: bold;");

        // New Answer Section
        Label newAnswerLabel = new Label("Your Answer:");
        TextArea answerArea = new TextArea();
        answerArea.setPromptText("Type your answer here...");
        answerArea.setPrefRowCount(3);
        
        // Button container for submitting a new answer
        HBox submitBox = new HBox(10);
        submitBox.setAlignment(Pos.CENTER);
        Button submitAnswerButton = new Button("Submit Answer");
        styleButton(submitAnswerButton, "#90EE90", "#228B22"); // Light green for success
        submitBox.getChildren().add(submitAnswerButton);

        // Existing Answers Section
        Label existingAnswersLabel = new Label("Existing Answers:");
        answersTable = new TableView<>();
        setupAnswersTable();

        // Create action buttons for answers
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);

        Button replyBtn = new Button("Reply");
        Button messageBtn = new Button("Message Author");
        Button readReviewsBtn = new Button("Read Reviews");
        Button updateButton = new Button("Update Answer");
        Button deleteButton = new Button("Delete");
        Button acceptButton = new Button("Accept");

        styleButton(replyBtn, "#E6E6FA", "#9370DB"); // Lavender for reference
        styleButton(messageBtn, "#ADD8E6", "#4682B4"); // Light blue for message
        styleButton(readReviewsBtn, "#87CEEB", "#4169E1"); // Light sky blue for reviews
        styleButton(updateButton, "#90EE90", "#228B22"); // Light green for success
        styleButton(deleteButton, "#FF6B6B", "#CD5C5C"); // Lighter Red to Coral Red gradient
        styleButton(acceptButton, "#FFD700", "#DAA520"); // Gold color for trust

        // Add action handlers
        replyBtn.setOnAction(e -> {
            Answer selectedAnswer = answersTable.getSelectionModel().getSelectedItem();
            if (selectedAnswer != null) {
                showReplyToAnswerDialog(selectedAnswer, question);
            } else {
                showError("Selection Required", "Please select an answer first.");
            }
        });

        messageBtn.setOnAction(e -> {
            Answer selectedAnswer = answersTable.getSelectionModel().getSelectedItem();
            if (selectedAnswer != null) {
                if (!selectedAnswer.getAuthor().equals(currentUsername)) {
                    showMessageForAnswer(selectedAnswer.getAuthor(), question.getQuestionId(), selectedAnswer.getAnswerId());
                } else {
                    showError("Invalid Action", "You cannot message yourself.");
                }
            } else {
                showError("Selection Required", "Please select an answer first.");
            }
        });

        readReviewsBtn.setOnAction(e -> {
            Answer selectedAnswer = answersTable.getSelectionModel().getSelectedItem();
            if (selectedAnswer != null) {
                showAnswerReviewsDialog(selectedAnswer, question);
            } else {
                showError("Selection Required", "Please select an answer first.");
            }
        });

        updateButton.setOnAction(e -> {
            Answer selectedAnswer = answersTable.getSelectionModel().getSelectedItem();
            if (selectedAnswer != null) {
                if (selectedAnswer.getAuthor().equals(currentUsername)) {
                    showUpdateAnswerDialog(selectedAnswer, answersTable, question);
                } else {
                    showError("Permission Denied", "You can only update your own answers.");
                }
            } else {
                showError("Selection Required", "Please select an answer first.");
            }
        });

        deleteButton.setOnAction(e -> {
            Answer selectedAnswer = answersTable.getSelectionModel().getSelectedItem();
            if (selectedAnswer != null) {
                if (selectedAnswer.getAuthor().equals(currentUsername)) {
                    handleAnswerDeletion(selectedAnswer, answersTable, question);
                } else {
                    showError("Permission Denied", "You can only delete your own answers.");
                }
            } else {
                showError("Selection Required", "Please select an answer first.");
            }
        });

        acceptButton.setOnAction(e -> {
            Answer selectedAnswer = answersTable.getSelectionModel().getSelectedItem();
            if (selectedAnswer != null) {
                if (question.getAuthor().equals(currentUsername) && !selectedAnswer.isAccepted()) {
                    handleAcceptAnswer(selectedAnswer, question);
                } else if (!question.getAuthor().equals(currentUsername)) {
                    showError("Permission Denied", "Only the question author can accept answers.");
                } else {
                    showError("Invalid Action", "This answer is already accepted.");
                }
            } else {
                showError("Selection Required", "Please select an answer first.");
            }
        });

        actionButtons.getChildren().addAll(replyBtn, messageBtn, readReviewsBtn, updateButton, deleteButton, acceptButton);

        // Submit Answer Button action
        submitAnswerButton.setOnAction(e -> {
            String answerContent = answerArea.getText();
            if (answerContent == null || answerContent.trim().isEmpty()) {
                showError("Error", "Answer cannot be empty or just spaces.");
                return;
            }
            try {
                Answer newAnswer = new Answer(0, question.getQuestionId(), answerContent, currentUsername, new Date());
                answersManager.addAnswer(newAnswer);
                answersTable.setItems(FXCollections.observableArrayList(
                    answersManager.getAnswersForQuestion(question.getQuestionId())
                ));
                answerArea.clear();
                refreshQuestions();
            } catch (SQLException ex) {
                showError("Error", "Failed to add answer: " + ex.getMessage());
            }
        });

        // Load existing answers
        try {
            answersTable.setItems(FXCollections.observableArrayList(
                answersManager.getAnswersForQuestion(question.getQuestionId())
            ));
        } catch (SQLException e) {
            showError("Error", "Failed to load answers: " + e.getMessage());
        }

        // Feedback Section
        Button sendFeedbackButton = new Button("Send Feedback");
        styleButton(sendFeedbackButton, "#ADD8E6", "#4682B4"); // Light blue for message
        sendFeedbackButton.setOnAction(e -> showFeedbackDialog(question.getAuthor(), question.getQuestionId()));

        layout.getChildren().addAll(
            questionLabel,
            newAnswerLabel,
            answerArea,
            submitBox,
            existingAnswersLabel,
            answersTable,
            actionButtons,
            sendFeedbackButton
        );

        Scene dialogScene = new Scene(layout, 800, 600);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void setupAnswersTable() {
        // Content Column
        TableColumn<Answer, String> contentCol = new TableColumn<>("Answer");
        contentCol.setCellValueFactory(cellData -> {
            Answer answer = cellData.getValue();
            String answerContent = answer.getContent();
            
            // If this answer references another answer, show the reference
            if (answer.hasReference()) {
                try {
                    Answer referencedAnswer = databaseHelper2.getAnswerById(answer.getReferenceAnswerId());
                    if (referencedAnswer != null) {
                        String referenceDisplay = String.format(
                            "↪ Re: \"%s\" (by %s)\n%s",
                            truncateIfNeeded(referencedAnswer.getContent(), 50),
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
        contentCol.setPrefWidth(300);

        // Author Column
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

        // Accepted Column
        TableColumn<Answer, Boolean> acceptedCol = new TableColumn<>("Accepted");
        acceptedCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("accepted"));
        acceptedCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item ? "Yes" : "No");
                }
            }
        });
        acceptedCol.setPrefWidth(80);
        acceptedCol.setStyle("-fx-alignment: CENTER;");

        answersTable.getColumns().setAll(contentCol, authorCol, dateCol, acceptedCol);
        answersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Enable row selection
        answersTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void showUpdateAnswerDialog(Answer answer, TableView<Answer> answersTable, Question question) {
        // Only allow updating if the current user is the answer author
        if (!answer.getAuthor().equals(currentUsername)) {
            showError("Error", "You can only update your own answers.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Update Answer");
        dialog.setHeaderText("Update your answer");

        TextArea updateArea = new TextArea(answer.getContent());
        updateArea.setPrefRowCount(3);

        dialog.getDialogPane().setContent(updateArea);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return updateArea.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newContent -> {
            if (!newContent.trim().isEmpty()) {
                try {
                    answer.setContent(newContent);
                    answersManager.updateAnswer(answer);
                    answersTable.setItems(FXCollections.observableArrayList(
                        answersManager.getAnswersForQuestion(question.getQuestionId())
                    ));
                } catch (SQLException e) {
                    showError("Error", "Failed to update answer: " + e.getMessage());
                }
            }
        });
    }

    private void handleAnswerDeletion(Answer answer, TableView<Answer> answersTable, Question question) {
        // Only allow deletion if the current user is the answer author
        if (!answer.getAuthor().equals(currentUsername)) {
            showError("Error", "You can only delete your own answers.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Answer");
        alert.setHeaderText("Delete Answer");
        alert.setContentText("Are you sure you want to delete this answer?");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    answersManager.removeAnswer(answer.getAnswerId());
                    answersTable.setItems(FXCollections.observableArrayList(
                        answersManager.getAnswersForQuestion(question.getQuestionId())
                    ));
                    // If no answers remain, update the question status to unresolved
                    List<Answer> remainingAnswers = answersManager.getAnswersForQuestion(question.getQuestionId());
                    if (remainingAnswers.isEmpty()) {
                        question.setAnswered(false);
                        questionsManager.updateQuestion(question);
                        refreshQuestions();
                    }
                } catch (SQLException e) {
                    showError("Error", "Failed to delete answer: " + e.getMessage());
                }
            }
        });
    }

    private void handleQuestionDeletion(Question question) {
        // Only allow deletion if the current user is the question author
        if (!question.getAuthor().equals(currentUsername)) {
            showError("Error", "You can only delete your own questions.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Question");
        alert.setHeaderText("Delete Question");
        alert.setContentText("Are you sure you want to delete this question? All associated answers will also be deleted.");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    questionsManager.removeQuestion(question.getQuestionId());
                    refreshQuestions();
                } catch (SQLException e) {
                    showError("Error", "Failed to delete question: " + e.getMessage());
                }
            }
        });
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

    // Mark answers as read in the database
    private void markAnswersAsRead(int questionId) {
        try {
            databaseHelper2.markAnswersAsRead(questionId, currentUsername);
            refreshQuestions();
        } catch (SQLException e) {
            showError("Error", "Failed to mark answers as read: " + e.getMessage());
        }
    }

    // Get the unread answers count from the database
    private int getUnreadAnswersCount(int questionId) throws SQLException {
        return databaseHelper2.getUnreadAnswersCount(questionId, currentUsername);
    }

    // When the Accept button is clicked, update the answer's accepted status
    private void handleAcceptAnswer(Answer answer, Question question) {
        try {
            // Set the answer as accepted so that its Accepted column changes from "No" to "Yes"
            answer.setAccepted(true);
            answersManager.updateAnswer(answer);
            
            // Mark the question as resolved if it isn't already
            if (!question.isAnswered()) {
                question.setAnswered(true);
                questionsManager.updateQuestion(question);
            }
            
            // Refresh both questions and answers views
            refreshQuestions();
            answersTable.setItems(FXCollections.observableArrayList(
                answersManager.getAnswersForQuestion(question.getQuestionId())
            ));
            answersTable.refresh();
        } catch (SQLException e) {
            showError("Error", "Failed to accept answer: " + e.getMessage());
        }
    }

    /**
     * Shows a dialog to create a new question that references an existing question
     */
    private void showReferenceQuestionDialog(Question referencedQuestion) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Ask a Related Question");
        dialog.setHeaderText("Create a question referencing: \n" + referencedQuestion.getContent());
        
        // Create the content area
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Referenced question display
        Label referenceLabel = new Label("Referencing question #" + referencedQuestion.getQuestionId() + 
                                        " by " + referencedQuestion.getAuthor());
        referenceLabel.setStyle("-fx-font-style: italic;");
        
        // New question input
        Label questionLabel = new Label("Your question:");
        TextArea questionArea = new TextArea();
        questionArea.setPromptText("Type your related question here...");
        questionArea.setPrefRowCount(5);
        
        content.getChildren().addAll(referenceLabel, questionLabel, questionArea);
        dialog.getDialogPane().setContent(content);
        
        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return questionArea.getText();
            }
            return null;
        });
        
        // Handle the result
        dialog.showAndWait().ifPresent(questionContent -> {
            if (questionContent == null || questionContent.trim().isEmpty()) {
                showError("Error", "Question cannot be empty or just spaces.");
                return;
            }
            
            try {
                // Create a new question with reference to the selected question
                Question newQuestion = new Question(
                    0, 
                    questionContent, 
                    currentUsername, 
                    new Date(), 
                    referencedQuestion.getQuestionId()
                );
                
                questionsManager.addQuestion(newQuestion);
                refreshQuestions();
            } catch (SQLException ex) {
                showError("Error", "Failed to add question: " + ex.getMessage());
            }
        });
    }

    /**
     * Shows a dialog to create a new answer that replies to an existing answer
     */
    private void showReplyToAnswerDialog(Answer referencedAnswer, Question question) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reply to Answer");
        dialog.setHeaderText("Create a reply to: \n" + referencedAnswer.getContent());
        
        // Create the content area
        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(10));
        
        // Referenced answer display
        Label referenceLabel = new Label("Replying to answer by " + referencedAnswer.getAuthor());
        referenceLabel.setStyle("-fx-font-style: italic;");
        
        // New answer input
        Label answerLabel = new Label("Your reply:");
        TextArea answerArea = new TextArea();
        answerArea.setPromptText("Type your reply here...");
        answerArea.setPrefRowCount(5);
        
        dialogContent.getChildren().addAll(referenceLabel, answerLabel, answerArea);
        dialog.getDialogPane().setContent(dialogContent);
        
        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return answerArea.getText();
            }
            return null;
        });
        
        // Handle the result
        dialog.showAndWait().ifPresent(replyContent -> {
            if (replyContent == null || replyContent.trim().isEmpty()) {
                showError("Error", "Reply cannot be empty or just spaces.");
                return;
            }
            
            try {
                // Create a new answer with reference to the selected answer
                Answer newAnswer = new Answer(
                    0, 
                    question.getQuestionId(), 
                    replyContent, 
                    currentUsername, 
                    new Date(), 
                    referencedAnswer.getAnswerId()
                );
                
                answersManager.addAnswer(newAnswer);
                answersTable.setItems(FXCollections.observableArrayList(
                    answersManager.getAnswersForQuestion(question.getQuestionId())
                ));
            } catch (SQLException ex) {
                showError("Error", "Failed to add reply: " + ex.getMessage());
            }
        });
    }

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

        // Rating Column
        TableColumn<Review, Double> ratingCol = new TableColumn<>("Rating");
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
        ratingCol.setPrefWidth(100);

        reviewsTable.getColumns().addAll(reviewerCol, reviewCol, dateCol, ratingCol);

        // Action Buttons Row
        HBox actionButtonsRow = new HBox(10);
        actionButtonsRow.setAlignment(Pos.CENTER);
        actionButtonsRow.setPadding(new Insets(10));

        // Trust Reviewer Button
        Button trustButton = new Button("Trust Reviewer");
        styleButton(trustButton, "#90EE90", "#228B22"); // Green color
        trustButton.setDisable(true); // Initially disabled

        // Remove Trust Button
        Button removeTrustButton = new Button("Remove Trust");
        styleButton(removeTrustButton, "#FFB6C1", "#FF0000"); // Light red color
        removeTrustButton.setDisable(true); // Initially disabled

        // Message Reviewer Button
        Button messageButton = new Button("Message Reviewer");
        styleButton(messageButton, "#ADD8E6", "#4682B4"); // Light blue color
        messageButton.setDisable(true); // Initially disabled

        // View Profile Button
        Button viewProfileButton = new Button("View Profile");
        styleButton(viewProfileButton, "#FFD700", "#DAA520"); // Gold color
        viewProfileButton.setDisable(true); // Initially disabled

        // Add buttons to the row
        actionButtonsRow.getChildren().addAll(trustButton, removeTrustButton, messageButton, viewProfileButton);

        // Handle review selection
        reviewsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String reviewer = newSelection.getReviewer();
                
                // Enable all buttons when a review is selected
                trustButton.setDisable(false);
                removeTrustButton.setDisable(false);
                messageButton.setDisable(false);
                viewProfileButton.setDisable(false);

                // Check if reviewer is already trusted
                try {
                    boolean isTrusted = databaseHelper2.isTrustedReviewer(currentUsername, reviewer);
                    trustButton.setDisable(isTrusted);
                    removeTrustButton.setDisable(!isTrusted);
                } catch (SQLException e) {
                    showError("Error", "Failed to check trust status: " + e.getMessage());
                }

                // Set up button actions
                trustButton.setOnAction(e -> {
                    try {
                        databaseHelper2.addTrustedReviewer(currentUsername, reviewer, 1);
                        trustButton.setDisable(true);
                        removeTrustButton.setDisable(false);
                        showSuccess("Success", "Reviewer trusted successfully!");
                    } catch (SQLException ex) {
                        showError("Error", "Failed to trust reviewer: " + ex.getMessage());
                    }
                });

                removeTrustButton.setOnAction(e -> {
                    try {
                        databaseHelper2.removeTrustedReviewer(currentUsername, reviewer);
                        trustButton.setDisable(false);
                        removeTrustButton.setDisable(true);
                        showSuccess("Success", "Reviewer trust removed successfully!");
                    } catch (SQLException ex) {
                        showError("Error", "Failed to remove trust: " + ex.getMessage());
                    }
                });

                messageButton.setOnAction(e -> showMessageForReview(reviewer, question.getQuestionId(), newSelection.getReviewId()));
                viewProfileButton.setOnAction(e -> showReviewerProfileDialog(reviewer));
            } else {
                // Disable all buttons when no review is selected
                trustButton.setDisable(true);
                removeTrustButton.setDisable(true);
                messageButton.setDisable(true);
                viewProfileButton.setDisable(true);
            }
        });

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

        // Search section for trusted reviewers
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);
        
        Label searchLabel = new Label("Filter by trusted reviewers:");
        CheckBox trustedOnlyCheck = new CheckBox("Show only trusted reviewers");
        trustedOnlyCheck.setSelected(false);
        
        searchBox.getChildren().addAll(searchLabel, trustedOnlyCheck);

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
                Map<String, Object> scorecard = databaseHelper3.getReviewerScorecard(data.getValue().getReviewer());
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

        // NEW: Add action column for Trust button and messaging
        TableColumn<Review, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button trustButton = new Button("Trust Reviewer");
            private final Button removeTrustButton = new Button("Remove Trust");
            private final Button messageButton = new Button("Message Reviewer");
            private final Button viewProfileButton = new Button("View Profile");
            
            {
                styleButton(trustButton, "#FFD700", "#DAA520"); // More saturated gold
                styleButton(removeTrustButton, "#FF6B6B", "#CD5C5C"); // More saturated red
                styleButton(messageButton, "#00CED1", "#008B8B"); // Darker cyan colors
                styleButton(viewProfileButton, "#90EE90", "#228B22"); // Green colors
                
                messageButton.setOnAction(e -> {
                    Review review = getTableView().getItems().get(getIndex());
                    String reviewer = review.getReviewer();
                    
                    if (!reviewer.equals(currentUsername)) { // Don't message yourself
                        showMessageForReview(reviewer, question.getQuestionId(), review.getReviewId());
                    }
                });
                
                trustButton.setOnAction(e -> {
                    Review review = getTableView().getItems().get(getIndex());
                    String reviewer = review.getReviewer();
                    
                    try {
                        databaseHelper2.addTrustedReviewer(currentUsername, reviewer, 1);
                        getTableView().refresh();
                        showSuccess("Success", reviewer + " added to your trusted reviewers!");
                    } catch (SQLException ex) {
                        showError("Error", "Failed to add trusted reviewer: " + ex.getMessage());
                    }
                });
                
                removeTrustButton.setOnAction(e -> {
                    Review review = getTableView().getItems().get(getIndex());
                    String reviewer = review.getReviewer();
                    
                    try {
                        databaseHelper2.removeTrustedReviewer(currentUsername, reviewer);
                        getTableView().refresh();
                        showSuccess("Success", reviewer + " removed from your trusted reviewers!");
                    } catch (SQLException ex) {
                        showError("Error", "Failed to remove trusted reviewer: " + ex.getMessage());
                    }
                });
                
                viewProfileButton.setOnAction(e -> {
                    Review review = getTableView().getItems().get(getIndex());
                    String reviewer = review.getReviewer();
                    showReviewerProfileDialog(reviewer);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Review review = getTableView().getItems().get(getIndex());
                    String reviewer = review.getReviewer();
                    
                    HBox box = new HBox(5);
                    
                    // Add message button for all reviews except your own
                    if (!reviewer.equals(currentUsername)) {
                        box.getChildren().add(messageButton);
                    }
                    
                    // Add trust/remove trust buttons based on current trust status
                    try {
                        boolean isTrusted = databaseHelper2.isTrustedReviewer(currentUsername, reviewer);
                        if (isTrusted) {
                            box.getChildren().add(removeTrustButton);
                        } else {
                            box.getChildren().add(trustButton);
                        }
                    } catch (SQLException ex) {
                        // Handle exception silently
                    }
                    
                    // Add view profile button for all reviewers
                    box.getChildren().add(viewProfileButton);
                    
                    setGraphic(box);
                }
            }
        });
        actionCol.setPrefWidth(150);

        reviewsTable.getColumns().setAll(
            reviewerCol, 
            reviewCol, 
            dateCol, 
            ratingCol,
            actionCol
        );

        // Load all reviews initially
        try {
            List<Review> allReviews = databaseHelper3.getReviewsForAnswer(answer.getAnswerId());
            reviewsTable.setItems(FXCollections.observableArrayList(allReviews));
        } catch (SQLException e) {
            showError("Error", "Failed to load reviews: " + e.getMessage());
        }

        // Add listener to checkbox to filter reviews
        trustedOnlyCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            try {
                List<Review> allReviews = databaseHelper3.getReviewsForAnswer(answer.getAnswerId());
                
                // Sort the reviews based on trusted reviewer weightage
                allReviews.sort((r1, r2) -> {
                    try {
                        // Get weightage for each reviewer
                        int w1 = databaseHelper2.getReviewerWeightage(currentUsername, r1.getReviewer());
                        int w2 = databaseHelper2.getReviewerWeightage(currentUsername, r2.getReviewer());
                        
                        // Sort in descending order of weightage (higher weightage first)
                        return Integer.compare(w2, w1);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return 0;
                    }
                });
                
                if (newVal) {
                    // Filter to show only trusted reviewers
                    List<Review> trustedReviews = allReviews.stream()
                        .filter(review -> {
                            try {
                                return databaseHelper2.isTrustedReviewer(currentUsername, review.getReviewer());
                            } catch (SQLException e) {
                                e.printStackTrace();
                                return false;
                            }
                        })
                        .collect(java.util.stream.Collectors.toList());
                    reviewsTable.setItems(FXCollections.observableArrayList(trustedReviews));
                } else {
                    // Show all reviews, but still sorted by weightage for trusted ones
                    reviewsTable.setItems(FXCollections.observableArrayList(allReviews));
                }
            } catch (SQLException e) {
                showError("Error", "Failed to filter reviews: " + e.getMessage());
            }
        });

        content.getChildren().addAll(answerLabel, searchBox, reviewsTable);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showTrustedReviewersDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Trusted Reviewers");
        dialog.setHeaderText("Manage Your Trusted Reviewers");

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Create the table with columns for reviewer username and trusted status
        TableView<String> reviewersTable = new TableView<>();
        
        // Username column
        TableColumn<String, String> usernameCol = new TableColumn<>("Reviewer");
        usernameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()));
        usernameCol.setPrefWidth(200);

        // Trusted status column with checkboxes
        TableColumn<String, Boolean> trustedCol = new TableColumn<>("Trusted");
        trustedCol.setCellFactory(col -> new TrustedStatusCell());
        trustedCol.setCellValueFactory(data -> {
            String reviewer = data.getValue();
            try {
                return new javafx.beans.property.SimpleBooleanProperty(
                    databaseHelper2.isTrustedReviewer(currentUsername, reviewer));
            } catch (SQLException e) {
                e.printStackTrace();
                return new javafx.beans.property.SimpleBooleanProperty(false);
            }
        });
        trustedCol.setPrefWidth(100);
        
        // Add weightage column with spinners
        TableColumn<String, Integer> weightageCol = new TableColumn<>("Weightage");
        weightageCol.setCellFactory(col -> new WeightageCell());
        weightageCol.setCellValueFactory(data -> {
            String reviewer = data.getValue();
            try {
                int weightage = databaseHelper2.getReviewerWeightage(currentUsername, reviewer);
                return new javafx.beans.property.SimpleIntegerProperty(weightage).asObject();
            } catch (SQLException e) {
                e.printStackTrace();
                return new javafx.beans.property.SimpleIntegerProperty(1).asObject();
            }
        });
        weightageCol.setPrefWidth(100);
        
        reviewersTable.getColumns().addAll(usernameCol, trustedCol, weightageCol);
        
        // Load all reviewers
        try {
            List<String> reviewers = databaseHelper2.getAllReviewers();
            reviewersTable.setItems(FXCollections.observableArrayList(reviewers));
        } catch (SQLException e) {
            showError("Error", "Failed to load reviewers: " + e.getMessage());
        }

        content.getChildren().add(reviewersTable);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Custom cell for displaying and editing the trusted status of reviewers
     */
    private class TrustedStatusCell extends TableCell<String, Boolean> {
            private final CheckBox checkBox = new CheckBox();
        
        public TrustedStatusCell() {
            checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (isEditing()) {
                    commitEdit(newVal);
                }
            });
            
            checkBox.setOnAction(event -> {
                    String reviewer = getTableView().getItems().get(getIndex());
                    try {
                        if (checkBox.isSelected()) {
                        // Add as trusted reviewer with default weightage of 1
                        databaseHelper2.addTrustedReviewer(currentUsername, reviewer, 1);
                        } else {
                        // Remove from trusted reviewers
                            databaseHelper2.removeTrustedReviewer(currentUsername, reviewer);
                        }
                    getTableView().refresh();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Error", "Failed to update trusted reviewer status: " + e.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                checkBox.setSelected(item != null && item);
                    setGraphic(checkBox);
                }
            }
    }
    
    /**
     * Custom cell for displaying and editing the weightage of trusted reviewers
     */
    private class WeightageCell extends TableCell<String, Integer> {
        private final Spinner<Integer> spinner = new Spinner<>(1, 10, 1);
        
        public WeightageCell() {
            spinner.setEditable(true);
            spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (isEditing()) {
                    commitEdit(newVal);
                }
                
                String reviewer = getTableView().getItems().get(getIndex());
                try {
                    if (databaseHelper2.isTrustedReviewer(currentUsername, reviewer)) {
                        databaseHelper2.updateReviewerWeightage(currentUsername, reviewer, newVal);
                    }
        } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Error", "Failed to update weightage: " + e.getMessage());
                }
            });
            
            spinner.setPrefWidth(80);
        }
        
        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                String reviewer = getTableView().getItems().get(getIndex());
                try {
                    if (databaseHelper2.isTrustedReviewer(currentUsername, reviewer)) {
                        spinner.getValueFactory().setValue(item);
                        setGraphic(spinner);
                    } else {
                        setGraphic(null);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    setGraphic(null);
                }
            }
        }
    }

    /**
     * Updates to use the centralized MessageHelper
     */
    private void showInboxDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Messages");
        dialog.setHeaderText("Your Messages");
        dialog.getDialogPane().setPrefSize(900, 600);

        // Split pane for conversations and messages
        SplitPane splitPane = new SplitPane();
        
        // Left pane: Conversations list
        VBox conversationsPane = new VBox(10);
        conversationsPane.setPadding(new Insets(10));
        
        Label conversationsLabel = new Label("Conversations");
        conversationsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        ListView<String> conversationsList = new ListView<>();
        
        try {
            List<String> conversations = databaseHelper2.getUserConversations(currentUsername);
            Map<String, Integer> unreadCounts = databaseHelper2.getUnreadMessageCountsByConversation(currentUsername);
            
            // Setup cell factory to show unread message indicators and instructor labels
            conversationsList.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox cell = new HBox(10);
                        cell.setAlignment(Pos.CENTER_LEFT);
                        
                        // Check if the user is an instructor
                        boolean isInstructor = false;
                        try {
                            String[] roles = databaseHelper.getUserRoles(item);
                            for (String role : roles) {
                                if (role.equals("instructor")) {
                                    isInstructor = true;
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            // If we can't check roles, just proceed without the label
                        }
                        
                        // Create the label with instructor tag if needed
                        String displayName = item + (isInstructor ? " (Instructor)" : "");
                        Label nameLabel = new Label(displayName);
                        if (isInstructor) {
                            nameLabel.setStyle("-fx-font-weight: bold;");
                        }
                        nameLabel.setMaxWidth(Double.MAX_VALUE);
                        HBox.setHgrow(nameLabel, Priority.ALWAYS);
                        
                        cell.getChildren().add(nameLabel);
                        
                        // Add unread indicator if there are unread messages
                        if (unreadCounts.containsKey(item) && unreadCounts.get(item) > 0) {
                            Label unreadLabel = new Label(unreadCounts.get(item).toString());
                            unreadLabel.setStyle(
                                "-fx-background-color: #ff6b6b; " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 2 6; " +
                                "-fx-background-radius: 10;"
                            );
                            cell.getChildren().add(unreadLabel);
                        }
                        
                        setGraphic(cell);
                    }
                }
            });
            
            conversationsList.getItems().addAll(conversations);
        } catch (SQLException e) {
            showError("Error", "Failed to load conversations: " + e.getMessage());
        }
        
        // Right pane: Messages view
        VBox messagesPane = new VBox(10);
        messagesPane.setPadding(new Insets(10));
        messagesPane.setAlignment(Pos.CENTER);
        
        Label selectConversationLabel = new Label("Select a conversation to view messages");
        selectConversationLabel.setStyle("-fx-font-style: italic;");
        messagesPane.getChildren().add(selectConversationLabel);
        
        // Handle conversation selection
        conversationsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    // Get conversation messages
                    List<Feedback> messages = databaseHelper2.getConversation(currentUsername, newVal);
                    
                    // Check if other user is an instructor
                    boolean isInstructor = false;
                    try {
                        String[] roles = databaseHelper.getUserRoles(newVal);
                        for (String role : roles) {
                            if (role.equals("instructor")) {
                                isInstructor = true;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        // If we can't check roles, just proceed without the label
                    }
                    
                    // Conversation header with instructor tag if needed
                    String headerText = "Conversation with " + newVal + (isInstructor ? " (Instructor)" : "");
                    
                    messagesPane.getChildren().clear();
                    
                    Label headerLabel = new Label(headerText);
                    headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    
                    // Rest of the conversation display (same as MessageHelper)
                    ScrollPane scrollPane = new ScrollPane();
                    scrollPane.setFitToWidth(true);
                    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
                    
                    VBox messagesBox = new VBox(10);
                    messagesBox.setPadding(new Insets(10));
                    
                    // Render messages
                    for (Feedback message : messages) {
                        HBox messageBox = new HBox(10);
                        
                        // Position messages based on sender
                        boolean isFromCurrentUser = message.getSender().equals(currentUsername);
                        messageBox.setAlignment(isFromCurrentUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                        
                        VBox messageContent = new VBox(5);
                        messageContent.setMaxWidth(400);
                        messageContent.setStyle(
                            "-fx-background-color: " + (isFromCurrentUser ? "#ddecf8" : "#f1f1f1") + "; " +
                            "-fx-padding: 10; " +
                            "-fx-background-radius: 10;"
                        );
                        
                        // Add reference information
                        if (message.getQuestionId() > 0) {
                            try {
                                Question question = questionsManager.getQuestionById(message.getQuestionId());
                                if (question != null) {
                                    Label refLabel = new Label("Re: Question #" + question.getQuestionId());
                                    refLabel.setStyle("-fx-font-style: italic; -fx-font-size: 11px;");
                                    messageContent.getChildren().add(refLabel);
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        
                        if (message.getAnswerId() != null) {
                            try {
                                Answer answer = answersManager.getAnswerById(message.getAnswerId());
                                if (answer != null) {
                                    Label refLabel = new Label("Re: Answer by " + answer.getAuthor());
                                    refLabel.setStyle("-fx-font-style: italic; -fx-font-size: 11px;");
                                    messageContent.getChildren().add(refLabel);
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        
                        if (message.getReviewId() != null) {
                            try {
                                Review review = databaseHelper3.getReviewById(message.getReviewId());
                                if (review != null) {
                                    Label refLabel = new Label("Re: Review by " + review.getReviewer());
                                    refLabel.setStyle("-fx-font-style: italic; -fx-font-size: 11px;");
                                    messageContent.getChildren().add(refLabel);
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        
                        // Message text and timestamp
                        Label contentLabel = new Label(message.getContent());
                        contentLabel.setWrapText(true);
                        
                        Label timeLabel = new Label(new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(message.getTimestamp()));
                        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
                        
                        messageContent.getChildren().addAll(contentLabel, timeLabel);
                        messageBox.getChildren().add(messageContent);
                        messagesBox.getChildren().add(messageBox);
                        
                    }
                    
                    scrollPane.setContent(messagesBox);
                    
                    // Reply box
                    HBox replyBox = new HBox(10);
                    replyBox.setAlignment(Pos.CENTER);
                    
                    TextField replyField = new TextField();
                    replyField.setPromptText("Type a message...");
                    replyField.setPrefWidth(400);
                    HBox.setHgrow(replyField, Priority.ALWAYS);
                    
                    Button sendButton = new Button("Send");
                    sendButton.setStyle("-fx-font-size: 13px; " +
                                     "-fx-text-fill: white; " +
                                     "-fx-background-color: #90EE90; " +
                                     "-fx-padding: 6px 12px; " +
                                     "-fx-border-color: #228B22; " +
                                     "-fx-border-width: 2px; " +
                                     "-fx-border-radius: 12px; " +
                                     "-fx-background-radius: 12px;");
                    
                    sendButton.setOnAction(e -> {
                        String content = replyField.getText().trim();
                        if (!content.isEmpty()) {
                            try {
                                // Create and send reply
                                Feedback reply = new Feedback(
                                    0,
                                    currentUsername,
                                    newVal,
                                    content,
                                    new Date(),
                                    0, // No specific question reference for direct replies
                                    null,
                                    null,
                                    null
                                );
                                databaseHelper2.addFeedback(reply);
                                
                                // Refresh conversation - call this method again with the same other user
                                conversationsList.getSelectionModel().select(newVal);
                                
                                // Clear reply field
                                replyField.clear();
                            } catch (SQLException ex) {
                                showError("Error", "Failed to send message: " + ex.getMessage());
                            }
                        }
                    });
                    
                    replyBox.getChildren().addAll(replyField, sendButton);
                    
                    // Add all components to the messages pane
                    messagesPane.getChildren().addAll(headerLabel, scrollPane, replyBox);
                    
                } catch (SQLException e) {
                    showError("Error", "Failed to load conversation: " + e.getMessage());
                }
            }
        });
        
        conversationsPane.getChildren().addAll(conversationsLabel, conversationsList);
        
        splitPane.getItems().addAll(conversationsPane, messagesPane);
        splitPane.setDividerPositions(0.3);
        
        dialog.getDialogPane().setContent(splitPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
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

    /**
     * Shows a dialog for sending feedback about a question
     * This method delegates to the MessageHelper for consistency
     */
    private void showFeedbackDialog(String receiver, int questionId) {
        // Don't message yourself
        if (receiver.equals(currentUsername)) {
            return;
        }
        MessageHelper.showMessageForQuestion(currentUsername, receiver, questionId, 
                                            databaseHelper2, questionsManager);
    }

    /**
     * Shows a dialog for sending a message about an answer
     * This method delegates to the MessageHelper for consistency
     */
    private void showMessageForAnswer(String receiver, int questionId, int answerId) {
        // Don't message yourself
        if (receiver.equals(currentUsername)) {
            return;
        }
        MessageHelper.showMessageForAnswer(currentUsername, receiver, questionId, answerId, 
                                          databaseHelper2, questionsManager, answersManager);
    }

    /**
     * Shows a dialog for sending a message about a review
     * This method delegates to the MessageHelper for consistency
     */
    private void showMessageForReview(String receiver, int questionId, int reviewId) {
        // Don't message yourself
        if (receiver.equals(currentUsername)) {
            return;
        }
        MessageHelper.showMessageForReview(currentUsername, receiver, questionId, reviewId,
                                          databaseHelper2, databaseHelper3, questionsManager);
    }

    private void handleReviewerRequest() {
        try {
            // Check if user already has a pending request
            if (databaseHelper3.hasPendingReviewerRequest(currentUsername)) {
                showAlert(Alert.AlertType.INFORMATION, "Request Pending", 
                        "You already have a pending request for reviewer permission. Please wait for an instructor to review it.");
                return;
            }
            
            // Check if user is already a reviewer
            String[] roles = databaseHelper.getUserRoles(currentUsername);
            for (String role : roles) {
                if (role.equals("reviewer")) {
                    showAlert(Alert.AlertType.INFORMATION, "Already a Reviewer", 
                            "You already have reviewer permissions.");
                    return;
                }
            }
            
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
                    } catch (SQLException ex) {
                        showAlert(Alert.AlertType.ERROR, "Error", 
                                "Failed to submit request: " + ex.getMessage());
                    }
                }
            });
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to check request status: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Helper method to disable all buttons in a container
    private void disableAllButtons(Pane container) {
        for (Node node : container.getChildren()) {
            if (node instanceof Button) {
                ((Button) node).setDisable(true);
            } else if (node instanceof Pane) {
                disableAllButtons((Pane) node);
            }
        }
    }

    /**
     * Shows a dialog displaying a reviewer's profile in an uneditable format
     * @param reviewerUsername The username of the reviewer whose profile to display
     */
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

        // Create read-only text areas for profile information
        TextArea aboutArea = new TextArea();
        aboutArea.setEditable(false);
        aboutArea.setPrefRowCount(3);
        aboutArea.setWrapText(true);

        TextArea experienceArea = new TextArea();
        experienceArea.setEditable(false);
        experienceArea.setPrefRowCount(3);
        experienceArea.setWrapText(true);

        TextArea specialtiesArea = new TextArea();
        specialtiesArea.setEditable(false);
        specialtiesArea.setPrefRowCount(3);
        specialtiesArea.setWrapText(true);

        // Load profile data
        try {
            Map<String, Object> profile = databaseHelper3.getReviewerProfile(reviewerUsername);
            if (profile != null) {
                aboutArea.setText((String) profile.get("about"));
                experienceArea.setText((String) profile.get("experience"));
                specialtiesArea.setText((String) profile.get("specialties"));
            } else {
                aboutArea.setText("No information available");
                experienceArea.setText("No information available");
                specialtiesArea.setText("No information available");
            }
        } catch (SQLException e) {
            showError("Error", "Failed to load profile: " + e.getMessage());
        }

        // Add labels and text areas to profile section
        profileSection.getChildren().addAll(
            profileLabel,
            new Label("About:"),
            aboutArea,
            new Label("Experience:"),
            experienceArea,
            new Label("Specialties:"),
            specialtiesArea
        );

        // Reviews Section
        VBox reviewsSection = new VBox(5);
        Label reviewsLabel = new Label("Reviews");
        reviewsLabel.setStyle("-fx-font-weight: bold;");

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

        reviewsTable.getColumns().addAll(reviewerCol, reviewCol, dateCol);

        // Load reviews
        try {
            List<Review> reviews = databaseHelper3.getReviewsByReviewer(reviewerUsername);
            reviewsTable.setItems(FXCollections.observableArrayList(reviews));
        } catch (SQLException e) {
            showError("Error", "Failed to load reviews: " + e.getMessage());
        }

        reviewsSection.getChildren().addAll(reviewsLabel, reviewsTable);

        // Feedback Section
        VBox feedbackSection = new VBox(5);
        Label feedbackLabel = new Label("Feedback");
        feedbackLabel.setStyle("-fx-font-weight: bold;");

        TableView<Map<String, Object>> feedbackTable = new TableView<>();
        
        // Student Column
        TableColumn<Map<String, Object>, String> studentCol = new TableColumn<>("From");
        studentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("student_username")));
        studentCol.setPrefWidth(150);

        // Content Column
        TableColumn<Map<String, Object>, String> contentCol = new TableColumn<>("Feedback");
        contentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("content")));
        contentCol.setPrefWidth(300);

        // Rating Column
        TableColumn<Map<String, Object>, Integer> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty((Integer) data.getValue().get("rating")).asObject());
        ratingCol.setPrefWidth(100);

        // Date Column
        TableColumn<Map<String, Object>, Date> feedbackDateCol = new TableColumn<>("Date");
        feedbackDateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>((Date) data.getValue().get("timestamp")));
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

        feedbackTable.getColumns().addAll(studentCol, contentCol, ratingCol, feedbackDateCol);

        // Load feedback
        try {
            List<Map<String, Object>> feedback = databaseHelper3.getReviewerFeedback(reviewerUsername);
            feedbackTable.setItems(FXCollections.observableArrayList(feedback));
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

