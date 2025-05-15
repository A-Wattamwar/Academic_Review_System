package databasePart1;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import main.Question;
import main.Answer;
import main.Feedback;
import main.Review;

/**
 * DatabaseHelper2 class provides database operations for the Question and Answer system.
 * This class handles question and answer management, including creation, retrieval, updating, and deletion.
 * It also supports feedback and review systems.

 * <p> Copyright: Team 60 CSE 360 </p>
 */ 
public class DatabaseHelper2 {
    // JDBC driver name and database URL 
    static final String JDBC_DRIVER = "org.h2.Driver";   
    static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

    //  Database credentials 
    static final String USER = "sa"; 
    static final String PASS = ""; 

    public Connection connection = null;
    public Statement statement = null;

    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
            createTables();
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        // Create the questions table
        String questionsTable = "CREATE TABLE IF NOT EXISTS Questions ("
                + "questionId INT AUTO_INCREMENT PRIMARY KEY, "
                + "content TEXT, "
                + "author VARCHAR(255), "
                + "timestamp TIMESTAMP, "
                + "answered BOOLEAN, "
                + "referenceQuestionId INT, "
                + "FOREIGN KEY (referenceQuestionId) REFERENCES Questions(questionId))";
        statement.execute(questionsTable);

        // Create the answers table with referenceAnswerId column
        String answersTable = "CREATE TABLE IF NOT EXISTS Answers ("
                + "answerId INT AUTO_INCREMENT PRIMARY KEY, "
                + "questionId INT, "
                + "content TEXT, "
                + "author VARCHAR(255), "
                + "timestamp TIMESTAMP, "
                + "accepted BOOLEAN DEFAULT FALSE, "
                + "referenceAnswerId INT, "
                + "FOREIGN KEY (questionId) REFERENCES Questions(questionId), "
                + "FOREIGN KEY (referenceAnswerId) REFERENCES Answers(answerId))";
        statement.execute(answersTable);

        // Create the read_status table
        String readStatusTable = "CREATE TABLE IF NOT EXISTS ReadStatus ("
                + "answerId INT, "
                + "userId VARCHAR(255), "
                + "readTimestamp TIMESTAMP, "
                + "PRIMARY KEY (answerId, userId), "
                + "FOREIGN KEY (answerId) REFERENCES Answers(answerId))";
        statement.execute(readStatusTable);

        // Create the feedback table
        String feedbackTable = "CREATE TABLE IF NOT EXISTS Feedback ("
                + "feedbackId INT AUTO_INCREMENT PRIMARY KEY, "
                + "sender VARCHAR(255), "
                + "receiver VARCHAR(255), "
                + "content TEXT, "
                + "timestamp TIMESTAMP, "
                + "questionId INT, "
                + "answerId INT, "
                + "reviewId INT, "
                + "parentMessageId INT, "
                + "isRead BOOLEAN DEFAULT FALSE)";
        statement.execute(feedbackTable);

        // Create the trusted reviewers table
        String trustedReviewersTable = "CREATE TABLE IF NOT EXISTS TrustedReviewers ("
                + "studentUsername VARCHAR(255), "
                + "reviewerUsername VARCHAR(255), "
                + "timestamp TIMESTAMP, "
                + "weightage INT DEFAULT 1, "
                + "PRIMARY KEY (studentUsername, reviewerUsername))";
        statement.execute(trustedReviewersTable);
    }

    // Question Management Methods
    public void addQuestion(Question question) throws SQLException {
        String query = "INSERT INTO Questions (content, author, timestamp, answered, referenceQuestionId) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, question.getContent());
            pstmt.setString(2, question.getAuthor());
            pstmt.setTimestamp(3, new Timestamp(question.getTimestamp().getTime()));
            pstmt.setBoolean(4, question.isAnswered());
            
            // Handle the reference question ID (may be null)
            if (question.getReferenceQuestionId() != null) {
                pstmt.setInt(5, question.getReferenceQuestionId());
            } else {
                pstmt.setNull(5, java.sql.Types.INTEGER);
            }
            
            pstmt.executeUpdate();
        }
    }

    public void updateQuestion(Question question) throws SQLException {
        String query = "UPDATE Questions SET content = ?, author = ?, timestamp = ?, answered = ?, referenceQuestionId = ? WHERE questionId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, question.getContent());
            pstmt.setString(2, question.getAuthor());
            pstmt.setTimestamp(3, new Timestamp(question.getTimestamp().getTime()));
            pstmt.setBoolean(4, question.isAnswered());
            
            // Handle the reference question ID (may be null)
            if (question.getReferenceQuestionId() != null) {
                pstmt.setInt(5, question.getReferenceQuestionId());
            } else {
                pstmt.setNull(5, java.sql.Types.INTEGER);
            }
            
            pstmt.setInt(6, question.getQuestionId());
            pstmt.executeUpdate();
        }
    }

    public void deleteQuestion(int questionId) throws SQLException {
        String query = "DELETE FROM Questions WHERE questionId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            pstmt.executeUpdate();
        }
    }

    public Question getQuestionById(int questionId) throws SQLException {
        String query = "SELECT * FROM Questions WHERE questionId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Question question = new Question(
                    rs.getInt("questionId"),
                    rs.getString("content"),
                    rs.getString("author"),
                    rs.getTimestamp("timestamp"),
                    rs.getBoolean("answered")
                );
                
                // Get the reference question ID if it exists
                int refQuestionId = rs.getInt("referenceQuestionId");
                if (!rs.wasNull()) {
                    question.setReferenceQuestionId(refQuestionId);
                }
                
                return question;
            }
        }
        return null;
    }

    public List<Question> getAllQuestions() throws SQLException {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM Questions";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Question question = new Question(
                    rs.getInt("questionId"),
                    rs.getString("content"),
                    rs.getString("author"),
                    rs.getTimestamp("timestamp"),
                    rs.getBoolean("answered")
                );
                
                // Get the reference question ID if it exists
                int refQuestionId = rs.getInt("referenceQuestionId");
                if (!rs.wasNull()) {
                    question.setReferenceQuestionId(refQuestionId);
                }
                
                questions.add(question);
            }
        }
        return questions;
    }

    public List<Question> searchQuestions(String keyword) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM Questions WHERE content LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                questions.add(new Question(
                    rs.getInt("questionId"),
                    rs.getString("content"),
                    rs.getString("author"),
                    rs.getTimestamp("timestamp"),
                    rs.getBoolean("answered")
                ));
            }
        }
        return questions;
    }

    // Answer Management Methods
    public void addAnswer(Answer answer) throws SQLException {
        String query = "INSERT INTO Answers (questionId, content, author, timestamp, accepted, referenceAnswerId) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, answer.getQuestionId());
            pstmt.setString(2, answer.getContent());
            pstmt.setString(3, answer.getAuthor());
            pstmt.setTimestamp(4, new Timestamp(answer.getTimestamp().getTime()));
            pstmt.setBoolean(5, answer.isAccepted());
            
            // Handle the reference answer ID (may be null)
            if (answer.getReferenceAnswerId() != null) {
                pstmt.setInt(6, answer.getReferenceAnswerId());
            } else {
                pstmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            pstmt.executeUpdate();
        }
    }

    public void updateAnswer(Answer answer) throws SQLException {
        String query = "UPDATE Answers SET content = ?, author = ?, timestamp = ?, accepted = ?, referenceAnswerId = ? WHERE answerId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, answer.getContent());
            pstmt.setString(2, answer.getAuthor());
            pstmt.setTimestamp(3, new Timestamp(answer.getTimestamp().getTime()));
            pstmt.setBoolean(4, answer.isAccepted());
            
            // Handle the reference answer ID (may be null)
            if (answer.getReferenceAnswerId() != null) {
                pstmt.setInt(5, answer.getReferenceAnswerId());
            } else {
                pstmt.setNull(5, java.sql.Types.INTEGER);
            }
            
            pstmt.setInt(6, answer.getAnswerId());
            pstmt.executeUpdate();
        }
    }

    public void deleteAnswer(int answerId) throws SQLException {
        // First delete related records from ReadStatus table
        String deleteReadStatus = "DELETE FROM ReadStatus WHERE answerId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteReadStatus)) {
            pstmt.setInt(1, answerId);
            pstmt.executeUpdate();
        }

        // Then delete the answer
        String deleteAnswer = "DELETE FROM Answers WHERE answerId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteAnswer)) {
            pstmt.setInt(1, answerId);
            pstmt.executeUpdate();
        }
    }

    public Answer getAnswerById(int answerId) throws SQLException {
        String query = "SELECT * FROM Answers WHERE answerId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, answerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Answer answer = new Answer(
                    rs.getInt("answerId"),
                    rs.getInt("questionId"),
                    rs.getString("content"),
                    rs.getString("author"),
                    rs.getTimestamp("timestamp")
                );
                answer.setAccepted(rs.getBoolean("accepted"));
                
                // Get the reference answer ID if it exists
                int refAnswerId = rs.getInt("referenceAnswerId");
                if (!rs.wasNull()) {
                    answer.setReferenceAnswerId(refAnswerId);
                }
                
                return answer;
            }
        }
        return null;
    }

    public List<Answer> getAllAnswers() throws SQLException {
        List<Answer> answers = new ArrayList<>();
        String query = "SELECT * FROM Answers";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Answer answer = new Answer(
                    rs.getInt("answerId"),
                    rs.getInt("questionId"),
                    rs.getString("content"),
                    rs.getString("author"),
                    rs.getTimestamp("timestamp")
                );
                answer.setAccepted(rs.getBoolean("accepted"));
                
                // Get the reference answer ID if it exists
                int refAnswerId = rs.getInt("referenceAnswerId");
                if (!rs.wasNull()) {
                    answer.setReferenceAnswerId(refAnswerId);
                }
                
                answers.add(answer);
            }
        }
        return answers;
    }

    public List<Answer> getAnswersForQuestion(int questionId) throws SQLException {
        List<Answer> answers = new ArrayList<>();
        String query = "SELECT * FROM Answers WHERE questionId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Answer answer = new Answer(
                    rs.getInt("answerId"),
                    rs.getInt("questionId"),
                    rs.getString("content"),
                    rs.getString("author"),
                    rs.getTimestamp("timestamp")
                );
                answer.setAccepted(rs.getBoolean("accepted"));
                
                // Get the reference answer ID if it exists
                int refAnswerId = rs.getInt("referenceAnswerId");
                if (!rs.wasNull()) {
                    answer.setReferenceAnswerId(refAnswerId);
                }
                
                answers.add(answer);
            }
        }
        return answers;
    }

    public void markAnswersAsRead(int questionId, String username) throws SQLException {
        String query = "INSERT INTO ReadStatus (answerId, userId, readTimestamp) "
                + "SELECT a.answerId, ?, CURRENT_TIMESTAMP "
                + "FROM Answers a "
                + "WHERE a.questionId = ? "
                + "AND NOT EXISTS (SELECT 1 FROM ReadStatus rs WHERE rs.answerId = a.answerId AND rs.userId = ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, questionId);
            pstmt.setString(3, username);
            pstmt.executeUpdate();
        }
    }

    public int getUnreadAnswersCount(int questionId, String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM Answers a "
                + "WHERE a.questionId = ? "
                + "AND NOT EXISTS (SELECT 1 FROM ReadStatus rs WHERE rs.answerId = a.answerId AND rs.userId = ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public void addFeedback(Feedback feedback) throws SQLException {
        String query = "INSERT INTO Feedback (sender, receiver, content, timestamp, questionId, answerId, reviewId, parentMessageId, isRead) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, FALSE)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, feedback.getSender());
            pstmt.setString(2, feedback.getReceiver());
            pstmt.setString(3, feedback.getContent());
            pstmt.setTimestamp(4, new Timestamp(feedback.getTimestamp().getTime()));
            pstmt.setInt(5, feedback.getQuestionId());
            
            // Handle nullable fields
            if (feedback.getAnswerId() != null) {
                pstmt.setInt(6, feedback.getAnswerId());
            } else {
                pstmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            if (feedback.getReviewId() != null) {
                pstmt.setInt(7, feedback.getReviewId());
            } else {
                pstmt.setNull(7, java.sql.Types.INTEGER);
            }
            
            if (feedback.getParentMessageId() != null) {
                pstmt.setInt(8, feedback.getParentMessageId());
            } else {
                pstmt.setNull(8, java.sql.Types.INTEGER);
            }
            
            pstmt.executeUpdate();
        }
    }

    public List<Feedback> getFeedbackForUser(String username) throws SQLException {
        List<Feedback> feedbacks = new ArrayList<>();
        String query = "SELECT * FROM Feedback WHERE receiver = ? ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                feedbacks.add(new Feedback(
                    rs.getInt("feedbackId"),
                    rs.getString("sender"),
                    rs.getString("receiver"),
                    rs.getString("content"),
                    rs.getTimestamp("timestamp"),
                    rs.getInt("questionId"),
                    getIntegerOrNull(rs, "answerId"),
                    getIntegerOrNull(rs, "reviewId"),
                    getIntegerOrNull(rs, "parentMessageId")
                ));
            }
        }
        return feedbacks;
    }

    // Helper method to handle nullable integers from ResultSet
    private Integer getIntegerOrNull(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Gets all conversations for a user (both sent and received messages)
     * @param username The username to get conversations for
     * @return List of distinct users the specified user has conversed with
     */
    public List<String> getUserConversations(String username) throws SQLException {
        List<String> partners = new ArrayList<>();
        
        // Simplified query to get all unique conversation partners
        String query = "SELECT DISTINCT CASE " +
                       "WHEN sender = ? THEN receiver " +
                       "ELSE sender END as partner " +
                       "FROM Feedback " +
                       "WHERE sender = ? OR receiver = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, username);
            pstmt.setString(3, username);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                partners.add(rs.getString("partner"));
            }
        }
        
        // Now sort the partners by most recent message (in a separate query)
        if (!partners.isEmpty()) {
            Map<String, Long> lastMessageTime = new HashMap<>();
            
            for (String partner : partners) {
                String timeQuery = "SELECT MAX(timestamp) as latest " +
                                   "FROM Feedback " +
                                   "WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?)";
                
                try (PreparedStatement timePstmt = connection.prepareStatement(timeQuery)) {
                    timePstmt.setString(1, username);
                    timePstmt.setString(2, partner);
                    timePstmt.setString(3, partner);
                    timePstmt.setString(4, username);
                    
                    ResultSet timeRs = timePstmt.executeQuery();
                    if (timeRs.next() && timeRs.getTimestamp("latest") != null) {
                        lastMessageTime.put(partner, timeRs.getTimestamp("latest").getTime());
                    } else {
                        lastMessageTime.put(partner, 0L);
                    }
                }
            }
            
            // Sort partners by most recent message time
            partners.sort((a, b) -> Long.compare(
                lastMessageTime.getOrDefault(b, 0L), 
                lastMessageTime.getOrDefault(a, 0L)
            ));
        }
        
        return partners;
    }

    /**
     * Gets the conversation between two users
     * @param user1 First user in conversation
     * @param user2 Second user in conversation
     * @return List of feedback messages between the two users, ordered by timestamp
     */
    public List<Feedback> getConversation(String user1, String user2) throws SQLException {
        List<Feedback> conversation = new ArrayList<>();
        String query = "SELECT * FROM Feedback "
                    + "WHERE (sender = ? AND receiver = ?) "
                    + "OR (sender = ? AND receiver = ?) "
                    + "ORDER BY timestamp ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                conversation.add(new Feedback(
                    rs.getInt("feedbackId"),
                    rs.getString("sender"),
                    rs.getString("receiver"),
                    rs.getString("content"),
                    rs.getTimestamp("timestamp"),
                    rs.getInt("questionId"),
                    getIntegerOrNull(rs, "answerId"),
                    getIntegerOrNull(rs, "reviewId"),
                    getIntegerOrNull(rs, "parentMessageId")
                ));
            }
        }
        
        // Mark messages as read if user1 is the receiver
        markConversationAsRead(user1, user2);
        
        return conversation;
    }

    /**
     * Gets the number of unread messages for a user
     * @param username The username to count unread messages for
     * @return Count of unread messages
     */
    public int getUnreadMessageCount(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM Feedback WHERE receiver = ? AND isRead = FALSE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Marks messages as read when user1 views messages from user2
     */
    public void markConversationAsRead(String user1, String user2) throws SQLException {
        String query = "UPDATE Feedback SET isRead = TRUE WHERE receiver = ? AND sender = ? AND isRead = FALSE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.executeUpdate();
        }
    }

    /**
     * Gets the count of unread messages for each conversation
     * @param username The username to get unread message counts for
     * @return Map of conversation partner to unread message count
     */
    public Map<String, Integer> getUnreadMessageCountsByConversation(String username) throws SQLException {
        Map<String, Integer> unreadCounts = new HashMap<>();
        String query = "SELECT sender, COUNT(*) as unreadCount FROM Feedback "
                    + "WHERE receiver = ? AND isRead = FALSE "
                    + "GROUP BY sender";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                unreadCounts.put(rs.getString("sender"), rs.getInt("unreadCount"));
            }
        }
        return unreadCounts;
    }

    /**
     * Gets messages related to a specific question
     */
    public List<Feedback> getMessagesForQuestion(int questionId) throws SQLException {
        List<Feedback> messages = new ArrayList<>();
        String query = "SELECT * FROM Feedback WHERE questionId = ? ORDER BY timestamp ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(new Feedback(
                    rs.getInt("feedbackId"),
                    rs.getString("sender"),
                    rs.getString("receiver"),
                    rs.getString("content"),
                    rs.getTimestamp("timestamp"),
                    rs.getInt("questionId"),
                    getIntegerOrNull(rs, "answerId"),
                    getIntegerOrNull(rs, "reviewId"),
                    getIntegerOrNull(rs, "parentMessageId")
                ));
            }
        }
        return messages;
    }

    /**
     * Gets messages related to a specific answer
     */
    public List<Feedback> getMessagesForAnswer(int answerId) throws SQLException {
        List<Feedback> messages = new ArrayList<>();
        String query = "SELECT * FROM Feedback WHERE answerId = ? ORDER BY timestamp ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, answerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(new Feedback(
                    rs.getInt("feedbackId"),
                    rs.getString("sender"),
                    rs.getString("receiver"),
                    rs.getString("content"),
                    rs.getTimestamp("timestamp"),
                    rs.getInt("questionId"),
                    getIntegerOrNull(rs, "answerId"),
                    getIntegerOrNull(rs, "reviewId"),
                    getIntegerOrNull(rs, "parentMessageId")
                ));
            }
        }
        return messages;
    }

    /**
     * Gets messages related to a specific review
     */
    public List<Feedback> getMessagesForReview(int reviewId) throws SQLException {
        List<Feedback> messages = new ArrayList<>();
        String query = "SELECT * FROM Feedback WHERE reviewId = ? ORDER BY timestamp ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reviewId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(new Feedback(
                    rs.getInt("feedbackId"),
                    rs.getString("sender"),
                    rs.getString("receiver"),
                    rs.getString("content"),
                    rs.getTimestamp("timestamp"),
                    rs.getInt("questionId"),
                    getIntegerOrNull(rs, "answerId"),
                    getIntegerOrNull(rs, "reviewId"),
                    getIntegerOrNull(rs, "parentMessageId")
                ));
            }
        }
        return messages;
    }

    public void deleteFeedback(int feedbackId) throws SQLException {
        String query = "DELETE FROM Feedback WHERE feedbackId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, feedbackId);
            pstmt.executeUpdate();
        }
    }

    // Trusted Reviewers Management Methods
    public void addTrustedReviewer(String studentUsername, String reviewerUsername, int weightage) throws SQLException {
        String query = "INSERT INTO TrustedReviewers (studentUsername, reviewerUsername, timestamp, weightage) VALUES (?, ?, CURRENT_TIMESTAMP, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentUsername);
            pstmt.setString(2, reviewerUsername);
            pstmt.setInt(3, weightage);
            pstmt.executeUpdate();
        }
    }

    // Keep the original method for backward compatibility
    public void addTrustedReviewer(String studentUsername, String reviewerUsername) throws SQLException {
        addTrustedReviewer(studentUsername, reviewerUsername, 1);
    }

    public void updateReviewerWeightage(String studentUsername, String reviewerUsername, int weightage) throws SQLException {
        String query = "UPDATE TrustedReviewers SET weightage = ? WHERE studentUsername = ? AND reviewerUsername = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, weightage);
            pstmt.setString(2, studentUsername);
            pstmt.setString(3, reviewerUsername);
            pstmt.executeUpdate();
        }
    }

    public int getReviewerWeightage(String studentUsername, String reviewerUsername) throws SQLException {
        String query = "SELECT weightage FROM TrustedReviewers WHERE studentUsername = ? AND reviewerUsername = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentUsername);
            pstmt.setString(2, reviewerUsername);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("weightage");
            }
        }
        return 0; // Return 0 if not a trusted reviewer
    }

    public void removeTrustedReviewer(String studentUsername, String reviewerUsername) throws SQLException {
        String query = "DELETE FROM TrustedReviewers WHERE studentUsername = ? AND reviewerUsername = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentUsername);
            pstmt.setString(2, reviewerUsername);
            pstmt.executeUpdate();
        }
    }

    public List<String> getTrustedReviewers(String studentUsername) throws SQLException {
        List<String> trustedReviewers = new ArrayList<>();
        String query = "SELECT reviewerUsername FROM TrustedReviewers WHERE studentUsername = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                trustedReviewers.add(rs.getString("reviewerUsername"));
            }
        }
        return trustedReviewers;
    }

    public List<String> getAllReviewers() throws SQLException {
        List<String> reviewers = new ArrayList<>();
        String query = "SELECT DISTINCT userName FROM cse360users WHERE roles LIKE '%reviewer%'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                reviewers.add(rs.getString("userName"));
            }
        }
        return reviewers;
    }

    public boolean isTrustedReviewer(String studentUsername, String reviewerUsername) throws SQLException {
        String query = "SELECT COUNT(*) FROM TrustedReviewers WHERE studentUsername = ? AND reviewerUsername = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentUsername);
            pstmt.setString(2, reviewerUsername);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Counts the number of system warnings a student has received
     * 
     * @param username The username of the student
     * @return The number of system warnings the student has received
     * @throws SQLException if a database access error occurs
     */
    public int getSystemWarningCount(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Feedback WHERE receiver = ? AND sender = 'SYSTEM WARNING'";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

    public void closeConnection() {
        try{ 
            if(statement!=null) statement.close(); 
        } catch(SQLException se2) { 
            se2.printStackTrace();
        } 
        try { 
            if(connection!=null) connection.close(); 
        } catch(SQLException se){ 
            se.printStackTrace(); 
        } 
    }
}
