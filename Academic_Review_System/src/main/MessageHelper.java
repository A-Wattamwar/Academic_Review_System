package main;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import databasePart1.DatabaseHelper2;
import databasePart1.DatabaseHelper3;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Utility class to centralize messaging functionality
 */
public class MessageHelper {

    /**
     * Creates a message dialog for sending a message about a question
     */
    public static void showMessageForQuestion(String currentUsername, String receiver, int questionId, 
                                             DatabaseHelper2 databaseHelper2, Questions questionsManager) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Message About Question");
        dialog.setHeaderText("Send a message to " + receiver);

        // Create the form content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        try {
            // Get question details
            Question question = questionsManager.getQuestionById(questionId);
            if (question != null) {
                // Display question details
                Label questionLabel = new Label("Regarding Question: " + 
                                               truncateIfNeeded(question.getContent(), 100));
                questionLabel.setWrapText(true);
                questionLabel.setStyle("-fx-font-style: italic;");
                content.getChildren().add(questionLabel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Message input
        Label contentLabel = new Label("Message:");
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Type your message here...");
        contentArea.setPrefRowCount(5);

        content.getChildren().addAll(contentLabel, contentArea);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return contentArea.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(message -> {
            if (message != null && !message.trim().isEmpty()) {
                try {
                    // Create message
                    Feedback feedback = new Feedback(
                        0, 
                        currentUsername, 
                        receiver, 
                        message, 
                        new Date(), 
                        questionId,
                        null,  // No answer reference
                        null,  // No review reference
                        null   // Not a reply
                    );
                    databaseHelper2.addFeedback(feedback);
                    showSuccess("Message Sent", "Your message has been sent to " + receiver);
                } catch (SQLException e) {
                    showError("Error", "Failed to send message: " + e.getMessage());
                }
            } else {
                showError("Error", "Message cannot be empty");
            }
        });
    }

    /**
     * Creates a message dialog for sending a message about an answer
     */
    public static void showMessageForAnswer(String currentUsername, String receiver, int questionId, int answerId,
                                           DatabaseHelper2 databaseHelper2, Questions questionsManager, Answers answersManager) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Message About Answer");
        dialog.setHeaderText("Send a message to " + receiver);

        // Create the form content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        try {
            // Get question and answer details
            Question question = questionsManager.getQuestionById(questionId);
            Answer answer = answersManager.getAnswerById(answerId);
            
            if (question != null && answer != null) {
                // Display question and answer details
                Label questionLabel = new Label("Regarding Question: " + 
                                               truncateIfNeeded(question.getContent(), 100));
                questionLabel.setWrapText(true);
                questionLabel.setStyle("-fx-font-style: italic;");
                
                Label answerLabel = new Label("Answer by " + answer.getAuthor() + ": " + 
                                              truncateIfNeeded(answer.getContent(), 100));
                answerLabel.setWrapText(true);
                answerLabel.setStyle("-fx-font-style: italic;");
                
                content.getChildren().addAll(questionLabel, answerLabel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Message input
        Label contentLabel = new Label("Message:");
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Type your message here...");
        contentArea.setPrefRowCount(5);

        content.getChildren().addAll(contentLabel, contentArea);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return contentArea.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(message -> {
            if (message != null && !message.trim().isEmpty()) {
                try {
                    // Create message
                    Feedback feedback = new Feedback(
                        0, 
                        currentUsername, 
                        receiver, 
                        message, 
                        new Date(), 
                        questionId,
                        answerId,  // Reference to the answer
                        null,      // No review reference
                        null       // Not a reply
                    );
                    databaseHelper2.addFeedback(feedback);
                    showSuccess("Message Sent", "Your message has been sent to " + receiver);
                } catch (SQLException e) {
                    showError("Error", "Failed to send message: " + e.getMessage());
                }
            } else {
                showError("Error", "Message cannot be empty");
            }
        });
    }

    /**
     * Creates a message dialog for sending a message about a review
     */
    public static void showMessageForReview(String currentUsername, String receiver, int questionId, int reviewId,
                                           DatabaseHelper2 databaseHelper2, DatabaseHelper3 databaseHelper3, 
                                           Questions questionsManager) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Message About Review");
        dialog.setHeaderText("Send a message to " + receiver);

        // Create the form content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        try {
            // Get question and review details
            Question question = questionsManager.getQuestionById(questionId);
            Review review = databaseHelper3.getReviewById(reviewId);
            
            if (question != null && review != null) {
                // Display question and review details
                Label questionLabel = new Label("Regarding Question: " + 
                                               truncateIfNeeded(question.getContent(), 100));
                questionLabel.setWrapText(true);
                questionLabel.setStyle("-fx-font-style: italic;");
                
                Label reviewLabel = new Label("Review by " + review.getReviewer() + ": " + 
                                             truncateIfNeeded(review.getContent(), 100));
                reviewLabel.setWrapText(true);
                reviewLabel.setStyle("-fx-font-style: italic;");
                
                content.getChildren().addAll(questionLabel, reviewLabel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Message input
        Label contentLabel = new Label("Message:");
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Type your message here...");
        contentArea.setPrefRowCount(5);

        content.getChildren().addAll(contentLabel, contentArea);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return contentArea.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(message -> {
            if (message != null && !message.trim().isEmpty()) {
                try {
                    // Create message
                    Feedback feedback = new Feedback(
                        0, 
                        currentUsername, 
                        receiver, 
                        message, 
                        new Date(), 
                        questionId,
                        null,      // No answer reference
                        reviewId,  // Reference to the review
                        null       // Not a reply
                    );
                    databaseHelper2.addFeedback(feedback);
                    showSuccess("Message Sent", "Your message has been sent to " + receiver);
                } catch (SQLException e) {
                    showError("Error", "Failed to send message: " + e.getMessage());
                }
            } else {
                showError("Error", "Message cannot be empty");
            }
        });
    }

    /**
     * Shows Inbox dialog with conversations and messaging
     */
    public static void showInboxDialog(String currentUsername, DatabaseHelper2 databaseHelper2, 
                                      DatabaseHelper3 databaseHelper3, Questions questionsManager, 
                                      Answers answersManager) {
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
            
            // Setup cell factory to show unread message indicators
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
                        
                        Label nameLabel = new Label(item);
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
                showConversation(newVal, messagesPane, currentUsername, databaseHelper2, 
                                questionsManager, answersManager, databaseHelper3);
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
     * Displays conversation between users
     */
    private static void showConversation(String otherUser, VBox messagesPane, String currentUsername,
                                       DatabaseHelper2 databaseHelper2, Questions questionsManager,
                                       Answers answersManager, DatabaseHelper3 databaseHelper3) {
        messagesPane.getChildren().clear();
        
        try {
            // Get conversation messages
            List<Feedback> messages = databaseHelper2.getConversation(currentUsername, otherUser);
            
            // Conversation header
            Label headerLabel = new Label("Conversation with " + otherUser);
            headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            // Scrollable message area
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
            styleButton(sendButton, "#90EE90", "#228B22");
            
            sendButton.setOnAction(e -> {
                String content = replyField.getText().trim();
                if (!content.isEmpty()) {
                    try {
                        // Create and send reply
                        Feedback reply = new Feedback(
                            0,
                            currentUsername,
                            otherUser,
                            content,
                            new Date(),
                            0, // No specific question reference for direct replies
                            null,
                            null,
                            null
                        );
                        databaseHelper2.addFeedback(reply);
                        
                        // Refresh conversation
                        showConversation(otherUser, messagesPane, currentUsername, databaseHelper2, 
                                        questionsManager, answersManager, databaseHelper3);
                        
                        // Clear reply field
                        replyField.clear();
                    } catch (SQLException ex) {
                        showError("Error", "Failed to send message: " + ex.getMessage());
                    }
                }
            });
            
            replyBox.getChildren().addAll(replyField, sendButton);
            
            messagesPane.getChildren().addAll(headerLabel, scrollPane, replyBox);
            
            // Auto-scroll to bottom of conversation
            scrollPane.setVvalue(1.0);
        } catch (SQLException e) {
            showError("Error", "Failed to load conversation: " + e.getMessage());
        }
    }

    // Helper methods
    private static void styleButton(Button button, String bgColor, String borderColor) {
        button.setStyle("-fx-font-size: 13px; "
                + "-fx-text-fill: black; "
                + "-fx-background-color: " + bgColor + "; "
                + "-fx-padding: 6px 12px; "
                + "-fx-border-color: " + borderColor + "; "
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px;");
    }

    private static void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static String truncateIfNeeded(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
} 