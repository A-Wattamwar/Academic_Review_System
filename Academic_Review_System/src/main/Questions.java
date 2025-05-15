package main;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import databasePart1.DatabaseHelper2;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;

/**
 * Questions class manages all question-related operations in the Question and Answer system.
 * This class handles the creation, retrieval, updating, and deletion of questions,
 * interfacing with the database through DatabaseHelper.
 * 
 * <p> Copyright: Team 60 CSE 360 </p>
 *
 * @version 1.00    2025-04-01    Questions class implementation
 */
public class Questions {
    private List<Question> questionList;
    private DatabaseHelper2 dbHelper;
    private Connection connection;

    /**
     * Creates a new Questions manager with database connection.
     * @param dbHelper Database helper instance for database operations
     * @param connection Database connection for direct queries
     * @throws SQLException if database connection fails
     */
    public Questions(DatabaseHelper2 dbHelper, Connection connection) throws SQLException {
        this.questionList = dbHelper.getAllQuestions();
        this.dbHelper = dbHelper;
        this.connection = connection;
    }

    /**
     * Adds a new question to the system.
     * @param q The question to be added
     * @throws SQLException if database operation fails
     */
    public void addQuestion(Question q) throws SQLException {
        dbHelper.addQuestion(q);
        questionList.add(q);
    }

    /**
     * Removes a question from the system.
     * @param questionId The ID of the question to be removed
     * @throws SQLException if database operation fails
     */
    public void removeQuestion(int questionId) throws SQLException {
        dbHelper.deleteQuestion(questionId);
        questionList.removeIf(q -> q.getQuestionId() == questionId);
    }

    /**
     * Updates a question in the system.
     * @param q The question to be updated
     * @throws SQLException if database operation fails
     */
    public void updateQuestion(Question q) throws SQLException {
        dbHelper.updateQuestion(q);
        for (int i = 0; i < questionList.size(); i++) {
            if (questionList.get(i).getQuestionId() == q.getQuestionId()) {
                questionList.set(i, q);
                break;
            }
        }
    }

    /**
     * Retrieves a question by its ID.
     * @param questionId The ID of the question to retrieve
     * @return The question object, or null if not found
     * @throws SQLException if database operation fails
     */
    public Question getQuestionById(int questionId) throws SQLException {
        String query = "SELECT questionId, content, author, timestamp, answered FROM Questions WHERE questionId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Question(
                    rs.getInt("questionId"),
                    rs.getString("content"),
                    rs.getString("author"),
                    rs.getTimestamp("timestamp"),
                    rs.getBoolean("answered")
                );
            }
        }
        return null;
    }

    /**
     * Retrieves all questions from the database.
     * @return List of all questions
     * @throws SQLException if database operation fails
     */
    public List<Question> getAllQuestions() throws SQLException {
        return dbHelper.getAllQuestions();
    }

    /**
     * Searches for questions containing the specified keyword.
     * @param keyword The search term to look for in questions
     * @return List of questions matching the search term
     * @throws SQLException if database operation fails
     */
    public List<Question> searchQuestions(String keyword) throws SQLException {
        String query = "SELECT questionId, content, author, timestamp, answered FROM Questions WHERE LOWER(content) LIKE LOWER(?) OR LOWER(author) LIKE LOWER(?)";
        List<Question> results = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Question question = new Question(
                    rs.getInt("questionId"),
                    rs.getString("content"),
                    rs.getString("author"),
                    rs.getTimestamp("timestamp"),
                    rs.getBoolean("answered")
                );
                results.add(question);
            }
        }
        
        return results;
    }

    /**
     * Searches for questions by a specific username.
     * @param username The username to search for
     * @return List of questions by the specified user
     * @throws SQLException if database operation fails
     */
    public List<Question> searchQuestionsByUser(String username) throws SQLException {
        String query = "SELECT questionId, content, author, timestamp, answered, referenceQuestionId FROM Questions WHERE LOWER(author) LIKE LOWER(?)";
        List<Question> results = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            String searchPattern = "%" + username + "%";
            pstmt.setString(1, searchPattern);
            
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
                
                results.add(question);
            }
        }
        
        return results;
    }
}
