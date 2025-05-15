package main;

import java.util.Date;

/**
 * Review class represents a review in the Question and Answer system.
 * This class contains all the properties of a review including its ID,
 * content, reviewer information, and references to questions and answers.
 * 
 * <p> Copyright: Team 60 CSE 360 </p>
 *
 * @version 1.00    2025-04-01    Review class implementation
 */
public class Review {
    private int reviewId;
    private String reviewer;
    private String content;
    private Date timestamp;
    private int questionId;
    private Integer answerId; // Nullable, since a review can be for a question or answer

    /**
     * Creates a new Review with specified properties.
     * 
     * @param reviewId Unique identifier for the review
     * @param reviewer Username of the review author
     * @param content The review text content
     * @param timestamp Time when the review was created
     * @param questionId ID of the question this review belongs to
     * @param answerId ID of the answer this review belongs to, or null if it's for a question
     */
    public Review(int reviewId, String reviewer, String content, Date timestamp, int questionId, Integer answerId) {
        this.reviewId = reviewId;
        this.reviewer = reviewer;
        this.content = content;
        this.timestamp = timestamp;
        this.questionId = questionId;
        this.answerId = answerId;
    }

    /**
     * Gets the unique identifier of this review.
     * @return The review ID
     */
    public int getReviewId() {
        return reviewId;
    }

    /**
     * Gets the username of the reviewer.
     * @return The reviewer's username
     */
    public String getReviewer() {
        return reviewer;
    }

    /**
     * Gets the content of this review.
     * @return The review text content
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the timestamp of when this review was created.
     * @return The timestamp of the review
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the ID of the question this review belongs to.
     * @return The question ID
     */
    public int getQuestionId() {
        return questionId;
    }

    /**
     * Gets the ID of the answer this review belongs to.
     * @return The answer ID, or null if it's for a question
     */
    public Integer getAnswerId() {
        return answerId;
    }

    /**
     * Sets the unique identifier of this review.
     * @param reviewId The new review ID
     */
    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    /**
     * Sets the username of the reviewer.
     * @param reviewer The new reviewer's username
     */
    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    /**
     * Sets the content of this review.
     * @param content The new review text content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Sets the timestamp of when this review was created.
     * @param timestamp The new timestamp of the review
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Sets the ID of the question this review belongs to.
     * @param questionId The new question ID
     */
    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    /**
     * Sets the ID of the answer this review belongs to.
     * @param answerId The new answer ID
     */
    public void setAnswerId(Integer answerId) {
        this.answerId = answerId;
    }
} 