package main;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import databasePart1.DatabaseHelper2;

/**
 * Answers class represents the manager for all answer-related operations.
 * This class handles the creation, retrieval, updating, and deletion of answers,
 * interfacing with the database through DatabaseHelper.
 * 
 * <p> Copyright: Team 60 CSE 360 </p>
 *
 * @version 1.00    2025-04-01    Answers class implementation
 */
public class Answers {
    private List<Answer> answers;
    private DatabaseHelper2 dbHelper;

    /**
     * Creates a new Answers manager with database connection.
     * @param dbHelper Database helper instance for database operations
     * @throws SQLException if database connection fails
     */
    public Answers(DatabaseHelper2 dbHelper) throws SQLException {
        this.answers = dbHelper.getAllAnswers();
        this.dbHelper = dbHelper;
    }

    /**
     * Adds a new answer to the system.
     * @param a The answer to be added
     * @throws SQLException if database operation fails
     */
    public void addAnswer(Answer a) throws SQLException {
        dbHelper.addAnswer(a);
        answers.add(a);
    }

    /**
     * Removes an answer from the system.
     * @param answerId ID of the answer to remove
     * @throws SQLException if database operation fails
     */
    public void removeAnswer(int answerId) throws SQLException {
        dbHelper.deleteAnswer(answerId);
        answers.removeIf(a -> a.getAnswerId() == answerId);
    }

    /**
     * Updates an existing answer in the system.
     * @param a The answer with updated information
     * @throws SQLException if database operation fails
     */
    public void updateAnswer(Answer a) throws SQLException {
        dbHelper.updateAnswer(a);
        for (int i = 0; i < answers.size(); i++) {
            if (answers.get(i).getAnswerId() == a.getAnswerId()) {
                answers.set(i, a);
                break;
            }
        }
    }

    /**
     * Retrieves an answer by its ID.
     * @param answerId ID of the answer to retrieve
     * @return The answer with the specified ID
     * @throws SQLException if database operation fails
     */
    public Answer getAnswerById(int answerId) throws SQLException {
        return dbHelper.getAnswerById(answerId);
    }

    /**
     * Retrieves all answers from the system.
     * @return List of all answers
     * @throws SQLException if database operation fails
     */
    public List<Answer> getAllAnswers() throws SQLException {
        return dbHelper.getAllAnswers();
    }

    /**
     * Retrieves all answers for a specific question.
     * @param questionId ID of the question to get answers for
     * @return List of answers for the specified question
     * @throws SQLException if database operation fails
     */
    public List<Answer> getAnswersForQuestion(int questionId) throws SQLException {
        return dbHelper.getAnswersForQuestion(questionId);
    }
}
