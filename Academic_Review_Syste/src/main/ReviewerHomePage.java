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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * ReviewerHomePage class represents the home page for users with the reviewer role.
 * This page displays a welcome message, a search section, a questions section, an inbox button, and a logout button.
 */
public class ReviewerHomePage {
    private final DatabaseHelper databaseHelper;
    private final DatabaseHelper2 databaseHelper2;
    private final DatabaseHelper3 databaseHelper3;
    private final String currentUsername;
    private Questions questionsManager;
    private Answers answersManager;
    private TableView<Question> questionTable;
    private static final java.text.SimpleDateFormat DATE_FORMAT = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss");

    public ReviewerHomePage(DatabaseHelper databaseHelper, String username) {
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
        // Background Pane with Blue Gradient
        Pane backgroundPane = new Pane();
        backgroundPane.setStyle("-fx-background-color: linear-gradient(to bottom, #4169E1, #87CEEB);");
        backgroundPane.setPrefSize(800, 500);

        // Main VBox Layout (Centered Content)
        VBox layout = new VBox(10);
        layout.setPrefSize(800, 550);
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        // Welcome Label
        Label welcomeLabel = new Label("Welcome, " + currentUsername + "!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Search Section
        VBox searchSection = createSearchSection();

        // Questions Section
        VBox questionsSection = createQuestionsSection();

        // Profile Button
        Button profileButton = new Button("My Profile");
        styleButton(profileButton, "#9370DB", "#8A2BE2"); // More purple color
        profileButton.setOnAction(e -> showProfileDialog());

        // Inbox Button
        Button inboxButton = new Button("Inbox");
        styleButton(inboxButton, "#ADD8E6", "#4682B4");
        inboxButton.setOnAction(e -> showInboxDialog());

        // Logout Button
        Button logoutButton = LogoutHelper.createLogoutButton(primaryStage, databaseHelper);
        styleButton(logoutButton, "#FF6B6B", "#CD5C5C"); // Red color with black text

        layout.getChildren().addAll(
            welcomeLabel,
            searchSection,
            questionsSection,
            profileButton,
            inboxButton,
            logoutButton
        );

        // Create a container for the back button at bottom right
        HBox backButtonContainer = new HBox();
        backButtonContainer.setAlignment(javafx.geometry.Pos.BOTTOM_RIGHT);
        backButtonContainer.setPadding(new javafx.geometry.Insets(0, 20, 20, 0)); // Add more padding to position from edges
        
        // Back button to return to WelcomeLoginPage
        Button backButton = new Button("Back");
        styleButton(backButton, "#E0E0E0", "#C0C0C0"); // Light gray with black text
        backButton.setOnAction(e -> {
            new WelcomeLoginPage(databaseHelper).show(primaryStage, new User(currentUsername, "", "", "", new String[]{"reviewer"}));
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

        Scene reviewerScene = new Scene(root, 1200, 600);
        primaryStage.setScene(reviewerScene);
        primaryStage.setTitle("Reviewer Dashboard");
        
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

        // Create action buttons row
        HBox actionButtonsRow = new HBox(10);
        actionButtonsRow.setAlignment(Pos.CENTER);
        
        // Answer button
        Button answerBtn = new Button("View Answers");
        styleButton(answerBtn, "#90EE90", "#228B22");
        answerBtn.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion == null) {
                showError("Selection Error", "Please select a question to answer.");
                return;
            }
            showAnswerManagementDialog(selectedQuestion);
        });
        
        // Add Review button
        Button addReviewBtn = new Button("Add Review");
        styleButton(addReviewBtn, "#FFD700", "#DAA520");
        addReviewBtn.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion == null) {
                showError("Selection Error", "Please select a question to review.");
                return;
            }
            showReviewDialog(selectedQuestion);
        });
        
        // View Reviews button
        Button viewReviewsBtn = new Button("View Reviews");
        styleButton(viewReviewsBtn, "#87CEEB", "#4169E1");
        viewReviewsBtn.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion == null) {
                showError("Selection Error", "Please select a question to view reviews.");
                return;
            }
            showReviewsDialog(selectedQuestion);
        });
        
        // Message Author button
        Button messageBtn = new Button("Message Author");
        styleButton(messageBtn, "#ADD8E6", "#4682B4");
        messageBtn.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion == null) {
                showError("Selection Error", "Please select a question to message its author.");
                return;
            }
            if (!selectedQuestion.getAuthor().equals(currentUsername)) { // Don't message yourself
                showMessageForQuestion(selectedQuestion.getAuthor(), selectedQuestion.getQuestionId());
            }
        });
        
        actionButtonsRow.getChildren().addAll(answerBtn, addReviewBtn, viewReviewsBtn, messageBtn);

        section.getChildren().addAll(titleLabel, questionTable, actionButtonsRow);
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
    }

    private void showAnswerManagementDialog(Question question) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Answer Management");
        dialog.setHeaderText("Question: " + question.getContent());

        // Set the dialog size
        dialog.getDialogPane().setPrefSize(1000, 700);

        // Create the dialog content
        VBox contentPane = new VBox(10);
        contentPane.setPadding(new Insets(10));

        // Existing Answers Section
        Label existingAnswersLabel = new Label("Existing Answers:");
        TableView<Answer> answersTable = new TableView<>();
        
        // Answer Content Column
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
        answersTable.setPrefHeight(200);

        // Create action buttons row
        HBox actionButtonsRow = new HBox(10);
        actionButtonsRow.setAlignment(Pos.CENTER);

        // Add Review button
        Button addReviewBtn = new Button("Add Review");
        styleButton(addReviewBtn, "#FFD700", "#DAA520");
        addReviewBtn.setStyle(addReviewBtn.getStyle() + "; -fx-text-fill: black;");
        addReviewBtn.setDisable(true);

        // View Reviews button
        Button viewReviewsBtn = new Button("View Reviews");
        styleButton(viewReviewsBtn, "#87CEEB", "#4169E1");
        viewReviewsBtn.setStyle(viewReviewsBtn.getStyle() + "; -fx-text-fill: black;");
        viewReviewsBtn.setDisable(true);

        // Message Author button
        Button messageBtn = new Button("Message Author");
        styleButton(messageBtn, "#ADD8E6", "#4682B4");
        messageBtn.setStyle(messageBtn.getStyle() + "; -fx-text-fill: black;");
        messageBtn.setDisable(true);

        // Add selection listener to enable/disable buttons based on selection
        answersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            addReviewBtn.setDisable(!hasSelection);
            viewReviewsBtn.setDisable(!hasSelection);
            messageBtn.setDisable(!hasSelection || newSelection.getAuthor().equals(currentUsername));

            if (hasSelection) {
                // Set up button actions
                addReviewBtn.setOnAction(e -> showAnswerReviewDialog(newSelection, question));
                viewReviewsBtn.setOnAction(e -> showAnswerReviewsDialog(newSelection, question));
                messageBtn.setOnAction(e -> showMessageForAnswer(newSelection.getAuthor(), question.getQuestionId(), newSelection.getAnswerId()));
            }
        });

        actionButtonsRow.getChildren().addAll(addReviewBtn, viewReviewsBtn, messageBtn);

        // Load existing answers for this question
        try {
            answersTable.setItems(FXCollections.observableArrayList(
                answersManager.getAnswersForQuestion(question.getQuestionId())
            ));
        } catch (SQLException e) {
            showError("Error", "Failed to load answers: " + e.getMessage());
        }

        contentPane.getChildren().addAll(existingAnswersLabel, answersTable, actionButtonsRow);
        dialog.getDialogPane().setContent(contentPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
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
        
        // Add Reviewer Rating Column
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

        reviewsTable.getColumns().setAll(reviewerCol, reviewCol, dateCol, ratingCol);

        // Create action buttons row
        HBox actionButtonsRow = new HBox(10);
        actionButtonsRow.setAlignment(Pos.CENTER);

        // Update button
        Button updateBtn = new Button("Update");
        styleButton(updateBtn, "#90EE90", "#228B22");
        updateBtn.setDisable(true);

        // Delete button
        Button deleteBtn = new Button("Delete");
        styleButton(deleteBtn, "#FFB6C1", "#FF0000");
        deleteBtn.setDisable(true);

        // Message button
        Button messageBtn = new Button("Message");
        styleButton(messageBtn, "#ADD8E6", "#4682B4");
        messageBtn.setDisable(true);

        // View Profile button
        Button viewProfileBtn = new Button("View Profile");
        styleButton(viewProfileBtn, "#90EE90", "#228B22");
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
        
        // Add Reviewer Rating Column
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

        // Action Column for Update/Delete
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
                    
                    HBox box = new HBox(5);
                    
                    // Don't show update/delete buttons for other reviewers' reviews
                    if (reviewer.equals(currentUsername)) {
                        Button updateBtn = new Button("Update");
                        styleButton(updateBtn, "#90EE90", "#228B22");
                        updateBtn.setOnAction(e -> showUpdateReviewDialog(review, reviewsTable));
                        
                        Button deleteBtn = new Button("Delete");
                        styleButton(deleteBtn, "#FFB6C1", "#FF0000");
                        deleteBtn.setOnAction(e -> handleReviewDeletion(review, reviewsTable));
                        
                        box.getChildren().addAll(updateBtn, deleteBtn);
                    } else {
                        // Add message button for other reviewers
                        Button messageBtn = new Button("Message");
                        styleButton(messageBtn, "#ADD8E6", "#4682B4");
                        messageBtn.setOnAction(e -> showMessageForReview(reviewer, question.getQuestionId(), review.getReviewId()));
                        
                        box.getChildren().add(messageBtn);
                    }
                    
                    setGraphic(box);
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

    private void showReviewDialog(Question question) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add Review");
        dialog.setHeaderText("Review for Question #" + question.getQuestionId());

        // Create the content area
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Question content display
        Label questionLabel = new Label("Question: " + question.getContent());
        questionLabel.setWrapText(true);

        // Review input
        Label reviewLabel = new Label("Your Review:");
        TextArea reviewArea = new TextArea();
        reviewArea.setPromptText("Type your review here...");
        reviewArea.setPrefRowCount(5);

        content.getChildren().addAll(questionLabel, reviewLabel, reviewArea);
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
        dialog.showAndWait().ifPresent(reviewContent -> {
            if (reviewContent == null || reviewContent.trim().isEmpty()) {
                showError("Error", "Review cannot be empty or just spaces.");
                return;
            }

            try {
                // Create a new review for the question
                Review newReview = new Review(
                    0,
                    currentUsername,
                    reviewContent,
                    new Date(),
                    question.getQuestionId(),
                    null
                );

                databaseHelper3.addReview(newReview);
                showSuccess("Success", "Review added successfully!");
            } catch (SQLException ex) {
                showError("Error", "Failed to add review: " + ex.getMessage());
            }
        });
    }

    private void showMessageForQuestion(String receiver, int questionId) {
        // Don't message yourself
        if (receiver.equals(currentUsername)) {
            return;
        }
        MessageHelper.showMessageForQuestion(currentUsername, receiver, questionId, 
                                           databaseHelper2, questionsManager);
    }

    private void showMessageForAnswer(String receiver, int questionId, int answerId) {
        // Don't message yourself
        if (receiver.equals(currentUsername)) {
            return;
        }
        MessageHelper.showMessageForAnswer(currentUsername, receiver, questionId, answerId,
                                         databaseHelper2, questionsManager, answersManager);
    }

    private void showMessageForReview(String receiver, int questionId, int reviewId) {
        // Don't message yourself
        if (receiver.equals(currentUsername)) {
            return;
        }
        MessageHelper.showMessageForReview(currentUsername, receiver, questionId, reviewId,
                                         databaseHelper2, databaseHelper3, questionsManager);
    }

    private void showInboxDialog() {
        MessageHelper.showInboxDialog(currentUsername, databaseHelper2, databaseHelper3, 
                                    questionsManager, answersManager);
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
     * Helper method to truncate long strings for display
     */
    private String truncateIfNeeded(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Shows a dialog for adding a review to an answer
     */
    private void showAnswerReviewDialog(Answer answer, Question question) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add Review");
        dialog.setHeaderText("Review for Answer by " + answer.getAuthor());

        // Create the content area
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Answer content display
        Label answerLabel = new Label("Answer: " + answer.getContent());
        answerLabel.setWrapText(true);

        // Review input
        Label reviewLabel = new Label("Your Review:");
        TextArea reviewArea = new TextArea();
        reviewArea.setPromptText("Type your review here...");
        reviewArea.setPrefRowCount(5);

        content.getChildren().addAll(answerLabel, reviewLabel, reviewArea);
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
        dialog.showAndWait().ifPresent(reviewContent -> {
            if (reviewContent == null || reviewContent.trim().isEmpty()) {
                showError("Error", "Review cannot be empty or just spaces.");
                return;
            }

            try {
                // Create a new review for the answer
                Review newReview = new Review(
                    0,
                    currentUsername,
                    reviewContent,
                    new Date(),
                    question.getQuestionId(),
                    answer.getAnswerId()
                );

                databaseHelper3.addReview(newReview);
                showSuccess("Success", "Review added successfully!");
            } catch (SQLException ex) {
                showError("Error", "Failed to add review: " + ex.getMessage());
            }
        });
    }

    private void showProfileDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Reviewer Profile");
        dialog.setHeaderText("Profile for " + currentUsername);

        // Create the dialog content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Profile Information Section
        VBox profileSection = new VBox(5);
        Label profileLabel = new Label("Profile Information");
        profileLabel.setStyle("-fx-font-weight: bold;");

        TextArea aboutArea = new TextArea();
        aboutArea.setPromptText("About me...");
        aboutArea.setPrefRowCount(3);

        TextArea experienceArea = new TextArea();
        experienceArea.setPromptText("My experience...");
        experienceArea.setPrefRowCount(3);

        TextArea specialtiesArea = new TextArea();
        specialtiesArea.setPromptText("My specialties...");
        specialtiesArea.setPrefRowCount(3);

        // Load existing profile data
        try {
            Map<String, Object> profile = databaseHelper3.getReviewerProfile(currentUsername);
            if (profile != null) {
                aboutArea.setText((String) profile.get("about"));
                experienceArea.setText((String) profile.get("experience"));
                specialtiesArea.setText((String) profile.get("specialties"));
            }
        } catch (SQLException e) {
            showError("Error", "Failed to load profile: " + e.getMessage());
        }

        Button saveButton = new Button("Save Profile");
        styleButton(saveButton, "#90EE90", "#228B22");
        saveButton.setOnAction(e -> {
            try {
                databaseHelper3.updateReviewerProfile(
                    currentUsername,
                    aboutArea.getText(),
                    experienceArea.getText(),
                    specialtiesArea.getText()
                );
                showSuccess("Success", "Profile updated successfully!");
            } catch (SQLException ex) {
                showError("Error", "Failed to update profile: " + ex.getMessage());
            }
        });

        profileSection.getChildren().addAll(
            profileLabel,
            new Label("About:"),
            aboutArea,
            new Label("Experience:"),
            experienceArea,
            new Label("Specialties:"),
            specialtiesArea,
            saveButton
        );

        // Reviews Section
        VBox reviewsSection = new VBox(5);
        Label reviewsLabel = new Label("My Reviews");
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
                databaseHelper3.getReviewsByReviewer(currentUsername)
            ));
        } catch (SQLException e) {
            showError("Error", "Failed to load reviews: " + e.getMessage());
        }

        reviewsSection.getChildren().addAll(reviewsLabel, reviewsTable);

        // Feedback Section
        VBox feedbackSection = new VBox(5);
        Label feedbackLabel = new Label("Feedback and Messages");
        feedbackLabel.setStyle("-fx-font-weight: bold;");

        TableView<Map<String, Object>> feedbackTable = new TableView<>();
        
        // Type Column to distinguish between feedback and messages
        TableColumn<Map<String, Object>, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("type")));
        typeCol.setPrefWidth(100);

        // Student Column
        TableColumn<Map<String, Object>, String> studentCol = new TableColumn<>("From Student");
        studentCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("student_username")));
        studentCol.setPrefWidth(120);

        // Content Column
        TableColumn<Map<String, Object>, String> feedbackContentCol = new TableColumn<>("Message");
        feedbackContentCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("content")));
        feedbackContentCol.setPrefWidth(350);

        // Rating Column with Stars (only for feedback)
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

        feedbackTable.getColumns().setAll(typeCol, studentCol, feedbackContentCol, ratingCol, feedbackDateCol);
        feedbackTable.setPlaceholder(new Label("No feedback or messages received yet"));

        // Load both feedback and messages
        try {
            List<Map<String, Object>> allFeedback = new ArrayList<>();
            
            // Load feedback from reviewer_feedback table
            List<Map<String, Object>> feedbackData = databaseHelper3.getReviewerFeedback(currentUsername);
            for (Map<String, Object> feedback : feedbackData) {
                feedback.put("type", "Feedback");
                allFeedback.add(feedback);
            }
            
            // Load messages from Feedback table
            List<Feedback> messages = databaseHelper2.getFeedbackForUser(currentUsername);
            for (Feedback message : messages) {
                Map<String, Object> messageMap = new HashMap<>();
                messageMap.put("type", "Message");
                messageMap.put("student_username", message.getSender());
                messageMap.put("content", message.getContent());
                messageMap.put("timestamp", message.getTimestamp());
                messageMap.put("rating", null); // Messages don't have ratings
                allFeedback.add(messageMap);
            }
            
            // Sort by timestamp (most recent first)
            allFeedback.sort((a, b) -> {
                Date dateA = (Date) a.get("timestamp");
                Date dateB = (Date) b.get("timestamp");
                return dateB.compareTo(dateA);
            });
            
            feedbackTable.setItems(FXCollections.observableArrayList(allFeedback));
        } catch (SQLException e) {
            showError("Error", "Failed to load feedback and messages: " + e.getMessage());
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