package main;

import java.util.Date;

/**
 * Represents a feedback message in the system.
 * This class contains all the properties of a feedback message including its ID,
 * sender, receiver, content, timestamp, question ID, answer ID, review ID, and parent message ID.
 * 
 * <p> Copyright: Team 60 CSE 360 </p>
 *
 * @version 1.00    2025-04-01    Feedback class implementation
 */
public class Feedback {
    private int feedbackId;
    private String sender;
    private String receiver;
    private String content;
    private Date timestamp;
    private int questionId;
    
    // New fields for private messaging
    private Integer answerId;      // Reference to an answer (nullable)
    private Integer reviewId;      // Reference to a review (nullable)
    private Integer parentMessageId; // For threaded conversations (nullable)

    /**
     * Constructs a new Feedback object with the specified properties.
     * 
     * @param feedbackId The ID of the feedback message
     * @param sender The username of the sender
     * @param receiver The username of the receiver 
     * @param content The content of the feedback message
     * @param timestamp The timestamp of when the feedback was created
     * @param questionId The ID of the question this feedback belongs to
     * @param answerId The ID of the answer this feedback belongs to (nullable)
     * @param reviewId The ID of the review this feedback belongs to (nullable)
     * @param parentMessageId The ID of the parent message in a threaded conversation (nullable)
     */
    public Feedback(int feedbackId, String sender, String receiver, String content, Date timestamp, 
                   int questionId, Integer answerId, Integer reviewId, Integer parentMessageId) {
        this.feedbackId = feedbackId;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;
        this.questionId = questionId;
        this.answerId = answerId;
        this.reviewId = reviewId;
        this.parentMessageId = parentMessageId;
    }

    /**
     * Constructs a new Feedback object with the specified properties.
     * 
     * @param feedbackId The ID of the feedback message
     * @param sender The username of the sender
     * @param receiver The username of the receiver 
     * @param content The content of the feedback message
     * @param timestamp The timestamp of when the feedback was created
     * @param questionId The ID of the question this feedback belongs to
     */
    // Original constructor for backward compatibility
    public Feedback(int feedbackId, String sender, String receiver, String content, Date timestamp, int questionId) {
        this(feedbackId, sender, receiver, content, timestamp, questionId, null, null, null);
    }

    /**
     * Gets the ID of the feedback message.
     * 
     * @return The ID of the feedback message
     */
    public int getFeedbackId() {
        return feedbackId;
    }

    /**
     * Gets the username of the sender.
     * 
     * @return The username of the sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * Gets the username of the receiver.
     * 
     * @return The username of the receiver
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     * Gets the content of the feedback message.
     * 
     * @return The content of the feedback message
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the timestamp of when the feedback was created.
     * 
     * @return The timestamp of the feedback
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the ID of the question this feedback belongs to.
     * 
     * @return The ID of the question
     */
    public int getQuestionId() {
        return questionId;
    }

    /**
     * Gets the ID of the answer this feedback belongs to.
     * 
     * @return The ID of the answer
     */
    public Integer getAnswerId() {
        return answerId;
    }

    /**
     * Gets the ID of the review this feedback belongs to.
     * 
     * @return The ID of the review
     */
    public Integer getReviewId() {
        return reviewId;
    }

    /**
     * Gets the ID of the parent message in a threaded conversation.
     * 
     * @return The ID of the parent message
     */
    public Integer getParentMessageId() {
        return parentMessageId;
    }

    /**
     * Sets the ID of the feedback message.
     * 
     * @param feedbackId The new ID of the feedback message
     */
    public void setFeedbackId(int feedbackId) {
        this.feedbackId = feedbackId;
    }

    /**
     * Sets the username of the sender.
     * 
     * @param sender The new username of the sender
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * Sets the username of the receiver.
     * 
     * @param receiver The new username of the receiver
     */
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    /**
     * Sets the content of the feedback message.
     * 
     * @param content The new content of the feedback message
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Sets the timestamp of when the feedback was created.
     * 
     * @param timestamp The new timestamp of the feedback
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Sets the ID of the question this feedback belongs to.
     * 
     * @param questionId The new ID of the question
     */
    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    /**
     * Sets the ID of the answer this feedback belongs to.
     * 
     * @param answerId The new ID of the answer
     */
    public void setAnswerId(Integer answerId) {
        this.answerId = answerId;
    }

    /**
     * Sets the ID of the review this feedback belongs to.
     * 
     * @param reviewId The new ID of the review
     */
    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }

    /**
     * Sets the ID of the parent message in a threaded conversation.
     * 
     * @param parentMessageId The new ID of the parent message
     */
    public void setParentMessageId(Integer parentMessageId) {
        this.parentMessageId = parentMessageId;
    }
} 