package main;

import java.util.Date;

/**
 * Answer class represents an answer in the Question and Answer system.
 * This class contains all the properties of an answer including its ID,
 * content, author information, and status.
 * 
 * <p> Copyright: Team 60 CSE 360 </p>
 *
 * @version 1.00    2025-04-01    Answer class implementation
 */

public class Answer {
    private int answerId;
    private int questionId;
    private String content;
    private String author;
    private Date timestamp;
    private boolean accepted;
    private Integer referenceAnswerId; // New field to store reference to another answer

    /**
     * Creates a new Answer with specified properties.
     * @param answerId Unique identifier for the answer
     * @param questionId ID of the question this answer belongs to
     * @param content The answer text content
     * @param author Username of the answer author
     * @param timestamp Time when the answer was created
     */
    public Answer(int answerId, int questionId, String content, String author, Date timestamp) {
        this.answerId = answerId;
        this.questionId = questionId;
        this.content = content;
        this.author = author;
        this.timestamp = timestamp;
        this.accepted = false;
        this.referenceAnswerId = null;
    }
    
    /**
     * Creates a new Answer that references another answer.
     * @param answerId Unique identifier for the answer
     * @param questionId ID of the question this answer belongs to
     * @param content The answer text content
     * @param author Username of the answer author
     * @param timestamp Time when the answer was created
     * @param referenceAnswerId ID of the answer being referenced
     */
    public Answer(int answerId, int questionId, String content, String author, Date timestamp, Integer referenceAnswerId) {
        this(answerId, questionId, content, author, timestamp);
        this.referenceAnswerId = referenceAnswerId;
    }

    /**
     * Gets the unique identifier of this answer.
     * @return The answer ID
     */
    public int getAnswerId() {
        return answerId;
    }

    /**
     * Gets the ID of the question this answer belongs to.
     * @return The question ID
     */
    public int getQuestionId() {
        return questionId;
    }

    /**
     * Gets the content of this answer.
     * @return The answer text content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of this answer.
     * @param newContent The new answer text content
     */
    public void setContent(String newContent) {
        this.content = newContent;
    }

    /**
     * Gets the author of this answer.
     * @return The author's username
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Gets the timestamp of when this answer was created.
     * @return The timestamp of the answer
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Checks if this answer has been accepted.
     * @return true if the answer has been accepted, false otherwise
     */
    public boolean isAccepted() {
        return accepted;
    }

    /**
     * Sets whether this answer has been accepted.
     * @param accepted true if the answer has been accepted, false otherwise
     */
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
    
    /**
     * Gets the ID of the referenced answer, if any.
     * @return The ID of the referenced answer, or null if this answer doesn't reference another
     */
    public Integer getReferenceAnswerId() {
        return referenceAnswerId;
    }
    
    /**
     * Sets the ID of the referenced answer.
     * @param referenceAnswerId The ID of the answer being referenced
     */
    public void setReferenceAnswerId(Integer referenceAnswerId) {
        this.referenceAnswerId = referenceAnswerId;
    }
    
    /**
     * Checks if this answer references another answer.
     * @return true if this answer references another answer, false otherwise
     */
    public boolean hasReference() {
        return referenceAnswerId != null;
    }

    /**
     * Validates the answer content and author.
     * @return true if the answer has valid content and author, false otherwise
     */
    public boolean validate() {
        return content != null && !content.isEmpty() && author != null && !author.isEmpty();
    }
}
