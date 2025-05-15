package main;

import java.util.Date;

/**
 * Question class represents a question in the Question and Answer system.
 * This class contains all the properties of a question including its ID,
 * content, author information, and status.
 * 
 * <p> Copyright: Team 60 CSE 360 </p>
 *
 * @version 1.00    2025-04-01    Question class implementation
 */
public class Question {
    private int questionId;
    private String content;
    private String author;
    private Date timestamp;
    private boolean answered;
    private Integer referenceQuestionId;

    /**
     * Creates a new Question with all properties.
     * @param id Unique identifier for the question
     * @param content The question text content
     * @param author Username of the question author
     * @param timestamp Time when the question was created
     * @param answered Whether the question has been marked as resolved
     */
    public Question(int id, String content, String author, Date timestamp, boolean answered) {
        this.questionId = id;
        this.content = content;
        this.author = author;
        this.timestamp = timestamp;
        this.answered = answered;
        this.referenceQuestionId = null;
    }

    /**
     * Creates a new Question marked as unresolved.
     * @param id Unique identifier for the question
     * @param content The question text content
     * @param author Username of the question author
     * @param timestamp Time when the question was created
     */
    public Question(int id, String content, String author, Date timestamp) {
        this(id, content, author, timestamp, false);
    }
    
    /**
     * Creates a new Question with reference to another question.
     * @param id Unique identifier for the question
     * @param content The question text content
     * @param author Username of the question author
     * @param timestamp Time when the question was created
     * @param referenceQuestionId ID of the question being referenced
     */
    public Question(int id, String content, String author, Date timestamp, Integer referenceQuestionId) {
        this(id, content, author, timestamp, false);
        this.referenceQuestionId = referenceQuestionId;
    }

    /**
     * Gets the ID of the question.
     * @return The ID of the question
     */
    public int getQuestionId() {
        return questionId;
    }

    /**
     * Sets the ID of the question.
     * @param id The new ID of the question
     */
    public void setQuestionId(int id) {
        this.questionId = id;
    }

    /**
     * Gets the content of the question.
     * @return The content of the question
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of the question.
     * @param newContent The new content of the question
     */
    public void setContent(String newContent) {
        this.content = newContent;
    }

    /**
     * Gets the author of the question.
     * @return The author of the question
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author of the question.
     * @param newAuthor The new author of the question
     */
    public void setAuthor(String newAuthor) {
        this.author = newAuthor;
    }

    /**
     * Gets the timestamp of when the question was created.
     * @return The timestamp of the question
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of when the question was created.
     * @param timestamp The new timestamp of the question
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Checks if the question has been marked as resolved.
     * @return true if the question has been resolved, false otherwise
     */
    public boolean isAnswered() {
        return answered;
    }

    /**
     * Sets whether the question has been marked as resolved.
     * @param answered true if the question has been resolved, false otherwise
     */
    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    /**
     * Gets the ID of the question being referenced.
     * @return The ID of the question being referenced
     */
    public Integer getReferenceQuestionId() {
        return referenceQuestionId;
    }

    /**
     * Sets the ID of the question being referenced.
     * @param referenceQuestionId The new ID of the question being referenced
     */
    public void setReferenceQuestionId(Integer referenceQuestionId) {
        this.referenceQuestionId = referenceQuestionId;
    }

    /**
     * Checks if the question has a reference to another question.
     * @return true if the question has a reference, false otherwise
     */
    public boolean hasReference() {
        return referenceQuestionId != null;
    }

    /**
     * Validates the question content and author.
     * @return true if the question has valid content and author, false otherwise
     */
    public boolean validate() {
        // Ensures content, etc. are valid
        return content != null && !content.isEmpty() && author != null && !author.isEmpty();
    }
}
