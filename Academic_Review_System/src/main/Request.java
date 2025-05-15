package main;

import java.util.Date;

/**
 * Represents an administrative action request made by an instructor.
 * 
 * <p> Copyright: Team 60 CSE 360 </p>
 *
 * @version 1.00    2025-04-18    Request class implementation
 */
public class Request {
    private int requestId;
    private String requesterUsername;
    private String title;
    private String description;
    private String status; // "open", "closed"
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private Date closedTimestamp; // Null if not closed
    private String closedByUsername; // Null if not closed
    private String adminNotes; // Null if no notes or not closed
    private Integer reopenedFromId; // Null if not a reopened request
    private boolean hasBeenReopened; // True if this request led to a new reopened request

    /**
     * Constructor for creating a new request object from database data
     * @param requestId The ID of the request
     * @param requesterUsername The username of the requester
     * @param title The title of the request
     * @param description The description of the request
     * @param status The status of the request
     * @param creationTimestamp The timestamp when the request was created
     * @param lastUpdateTimestamp The timestamp when the request was last updated
     * @param closedTimestamp The timestamp when the request was closed
     * @param closedByUsername The username of the user who closed the request
     * @param adminNotes The notes of the admin who closed the request
     * @param reopenedFromId The ID of the request that was reopened
     * @param hasBeenReopened True if this request led to a new reopened request
     */
    public Request(int requestId, String requesterUsername, String title, String description,
                   String status, Date creationTimestamp, Date lastUpdateTimestamp,
                   Date closedTimestamp, String closedByUsername, String adminNotes,
                   Integer reopenedFromId, boolean hasBeenReopened) {
        this.requestId = requestId;
        this.requesterUsername = requesterUsername;
        this.title = title;
        this.description = description;
        this.status = status;
        this.creationTimestamp = creationTimestamp;
        this.closedTimestamp = closedTimestamp;
        this.closedByUsername = closedByUsername;
        this.adminNotes = adminNotes;
        this.reopenedFromId = reopenedFromId;
        this.hasBeenReopened = hasBeenReopened;
    }

    /**
     * Getter for the request ID
     * @return The ID of the request
     */
    public int getRequestId() {
        return requestId;
    }

    /**
     * Getter for the requester username
     * @return The username of the requester
     */
    public String getRequesterUsername() {
        return requesterUsername;
    }

    /**
     * Getter for the title of the request
     * @return The title of the request
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter for the description of the request
     * @return The description of the request
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter for the status of the request
     * @return The status of the request
     */
    public String getStatus() {
        return status;
    }

    /**
     * Getter for the creation timestamp of the request
     * @return The timestamp when the request was created
     */
    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    /**
     * Getter for the last update timestamp of the request
     * @return The timestamp when the request was last updated
     */
    public Date getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    /**
     * Getter for the closed timestamp of the request
     * @return The timestamp when the request was closed
     */
    public Date getClosedTimestamp() {
        return closedTimestamp;
    }

    /**
     * Getter for the username of the user who closed the request
     * @return The username of the user who closed the request
     */
    public String getClosedByUsername() {
        return closedByUsername;
    }

    /**
     * Getter for the admin notes of the request
     * @return The notes of the admin who closed the request
     */
    public String getAdminNotes() {
        return adminNotes;
    }

    /**
     * Getter for the ID of the request that was reopened
     * @return The ID of the request that was reopened
     */
    public Integer getReopenedFromId() {
        return reopenedFromId;
    }

    /**
     * Getter for whether the request has been reopened
     * @return True if the request has been reopened
     */
    public boolean hasBeenReopened() {
        return hasBeenReopened;
    }
    
    /**
     * Helper method to check if the request is open
     * @return True if the request is open
     */
    public boolean isOpen() {
        return "open".equalsIgnoreCase(status);
    }

    /**
     * Helper method to check if the request is closed
     * @return True if the request is closed
     */
    public boolean isClosed() {
        return "closed".equalsIgnoreCase(status);
    }

    /**
     * Helper method to convert the request to a string
     * @return The string representation of the request
     */
    @Override
    public String toString() {
        return "Request{" +
               "requestId=" + requestId +
               ", requesterUsername='" + requesterUsername + "'" +
               ", title='" + title + "'" +
               ", status='" + status + "'" +
               ", creationTimestamp=" + creationTimestamp +
               ", lastUpdateTimestamp=" + lastUpdateTimestamp +
               ", closedTimestamp=" + closedTimestamp +
               ", closedByUsername='" + closedByUsername + "'" +
               ", reopenedFromId=" + reopenedFromId +
               ", hasBeenReopened=" + hasBeenReopened +
               '}';
    }
}
