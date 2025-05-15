package databasePart1;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import main.Review;

/**
 * DatabaseHelper3 class provides database operations for the review system.
 * This class handles review creation, retrieval, updating, and deletion,
 * as well as reviewer request management.
 * 
 * <p> Copyright: Team 60 CSE 360 </p>
 * 
 * @author Team60
 * @version 1.0
 * @since 2025-04-01
 */
public class DatabaseHelper3 {
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
        // Create the reviews table
        String reviewsTable = "CREATE TABLE IF NOT EXISTS reviews ("
                + "review_id INT AUTO_INCREMENT PRIMARY KEY, "
                + "reviewer VARCHAR(255), "
                + "content TEXT, "
                + "timestamp TIMESTAMP, "
                + "question_id INT, "
                + "answer_id INT, "
                + "FOREIGN KEY (question_id) REFERENCES Questions(questionId), "
                + "FOREIGN KEY (answer_id) REFERENCES Answers(answerId))";
        statement.execute(reviewsTable);

        // Create the reviewer_profile table
        String reviewerProfileTable = "CREATE TABLE IF NOT EXISTS reviewer_profile ("
                + "username VARCHAR(255) PRIMARY KEY, "
                + "about TEXT, "
                + "experience TEXT, "
                + "specialties TEXT, "
                + "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        statement.execute(reviewerProfileTable);

        // Create the reviewer_feedback table
        String reviewerFeedbackTable = "CREATE TABLE IF NOT EXISTS reviewer_feedback ("
                + "feedback_id INT AUTO_INCREMENT PRIMARY KEY, "
                + "reviewer_username VARCHAR(255), "
                + "student_username VARCHAR(255), "
                + "content TEXT, "
                + "rating INT CHECK (rating >= 1 AND rating <= 5), "
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (reviewer_username) REFERENCES reviewer_profile(username))";
        statement.execute(reviewerFeedbackTable);

        // Create the reviewer_requests table
        String reviewerRequestsTable = "CREATE TABLE IF NOT EXISTS reviewer_requests ("
                + "request_id INT AUTO_INCREMENT PRIMARY KEY, "
                + "student_username VARCHAR(255), "
                + "status VARCHAR(20) DEFAULT 'pending', " // pending, approved, rejected
                + "request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "review_date TIMESTAMP, "
                + "reviewer_username VARCHAR(255), " // instructor who reviewed
                + "review_notes TEXT)";
        statement.execute(reviewerRequestsTable);
        
        // Create the reported_content table
        String reportedContentTable = "CREATE TABLE IF NOT EXISTS reported_content ("
                + "report_id INT AUTO_INCREMENT PRIMARY KEY, "
                + "reporter_username VARCHAR(255), " // staff member who reported
                + "content_type VARCHAR(20), " // question, answer
                + "content_id INT, " // ID of the question or answer
                + "reason TEXT, " // Reason for reporting
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "status VARCHAR(20) DEFAULT 'pending', " // pending, reviewed, dismissed
                + "reviewer_username VARCHAR(255), " // instructor who reviewed 
                + "review_notes TEXT, "
                + "review_date TIMESTAMP)";
        statement.execute(reportedContentTable);
        
        // Create the banned_students table
        String bannedStudentsTable = "CREATE TABLE IF NOT EXISTS banned_students ("
                + "ban_id INT AUTO_INCREMENT PRIMARY KEY, "
                + "student_username VARCHAR(255) UNIQUE, "
                + "banned_by VARCHAR(255), "
                + "ban_reason TEXT, "
                + "ban_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        statement.execute(bannedStudentsTable);

        // Create the reviewer_scorecard table
        String reviewerScorecardTable = "CREATE TABLE IF NOT EXISTS reviewer_scorecard ("
                + "scorecard_id INT AUTO_INCREMENT PRIMARY KEY, "
                + "reviewer_username VARCHAR(255) UNIQUE, "
                + "friendliness INT CHECK (friendliness >= 0 AND friendliness <= 5), "
                + "accuracy INT CHECK (accuracy >= 0 AND accuracy <= 5), "
                + "judgement INT CHECK (judgement >= 0 AND judgement <= 5), "
                + "communication INT CHECK (communication >= 0 AND communication <= 5), "
                + "overall_score DECIMAL(3,2) AS ((friendliness + accuracy + judgement + communication) / 4.0))";
        statement.execute(reviewerScorecardTable);
    }

    /**
     * Adds a new review to the database.
     * 
     * The method inserts a review record with the provided reviewer, content,
     * timestamp, question ID, and optional answer ID.
     *
     * @param review The review object to be added to the database
     * @throws SQLException if a database access error occurs
     */
    public void addReview(Review review) throws SQLException {
        String sql = "INSERT INTO reviews (reviewer, content, timestamp, question_id, answer_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, review.getReviewer());
            pstmt.setString(2, review.getContent());
            pstmt.setTimestamp(3, new java.sql.Timestamp(review.getTimestamp().getTime()));
            pstmt.setInt(4, review.getQuestionId());
            if (review.getAnswerId() != null) {
                pstmt.setInt(5, review.getAnswerId());
            } else {
                pstmt.setNull(5, java.sql.Types.INTEGER);
            }
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves all reviews for a specific question from the database.
     * 
     * This method queries the reviews table for all records where the question_id
     * matches the provided questionId and the answer_id is NULL (indicating the review
     * is for a question, not an answer).
     *
     * @param questionId The ID of the question to retrieve reviews for
     */
    public List<Review> getReviewsForQuestion(int questionId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE question_id = ? AND answer_id IS NULL ORDER BY timestamp ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new Review(
                        rs.getInt("review_id"),
                        rs.getString("reviewer"),
                        rs.getString("content"),
                        rs.getTimestamp("timestamp"),
                        rs.getInt("question_id"),
                        null
                    ));
                }
            }
        }
        return reviews;
    }

    /**
     * Retrieves all reviews for a specific answer from the database.
     * 
     * This method queries the reviews table for all records where the answer_id
     * matches the provided answerId.
     *
     * @param answerId The ID of the answer to retrieve reviews for
     */
    public List<Review> getReviewsForAnswer(int answerId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE answer_id = ? ORDER BY timestamp ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, answerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new Review(
                        rs.getInt("review_id"),
                        rs.getString("reviewer"),
                        rs.getString("content"),
                        rs.getTimestamp("timestamp"),
                        rs.getInt("question_id"),
                        rs.getInt("answer_id")
                    ));
                }
            }
        }
        return reviews;
    }

    /**
     * Updates an existing review in the database.
     * 
     * This method updates the content and timestamp of a review record in the reviews table.
     *
     * @param review The review object containing the updated content and timestamp
     * @throws SQLException if a database access error occurs
     */
    public void updateReview(Review review) throws SQLException {
        String sql = "UPDATE reviews SET content = ?, timestamp = ? WHERE review_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, review.getContent());
            pstmt.setTimestamp(2, new java.sql.Timestamp(review.getTimestamp().getTime()));
            pstmt.setInt(3, review.getReviewId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Deletes a review from the database.
     * 
     * This method deletes a review record from the reviews table based on the provided
     * reviewId.
     *
     * @param reviewId The ID of the review to be deleted
     * @throws SQLException if a database access error occurs
     */
    public void deleteReview(int reviewId) throws SQLException {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, reviewId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves a review by its ID from the database.
     * 
     * This method queries the reviews table for a record where the review_id matches
     * the provided reviewId. It then constructs a Review object with the retrieved data.
     *
     * @param reviewId The ID of the review to retrieve
     * @return The review object, or null if the review is not found
     */
    public Review getReviewById(int reviewId) throws SQLException {
        Review review = null;
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, reviewId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Integer answerId = rs.getInt("answer_id");
                    if (rs.wasNull()) {
                        answerId = null;
                    }
                    
                    review = new Review(
                        rs.getInt("review_id"),
                        rs.getString("reviewer"),
                        rs.getString("content"),
                        rs.getTimestamp("timestamp"),
                        rs.getInt("question_id"),
                        answerId
                    );
                }
            }
        }
        return review;
    }

    /**
     * Submits a request for a student to become a reviewer in the database.
     * 
     * This method inserts a new record into the reviewer_requests table with the provided
     * studentUsername.
     *
     * @param studentUsername The username of the student requesting to become a reviewer
     * @throws SQLException if a database access error occurs
     */
    public void submitReviewerRequest(String studentUsername) throws SQLException {
        // Check if user already has a pending request
        if (hasPendingReviewerRequest(studentUsername)) {
            throw new SQLException("User already has a pending reviewer request.");
        }
        
        String sql = "INSERT INTO reviewer_requests (student_username) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, studentUsername);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves all pending reviewer requests from the database.
     * 
     * This method queries the reviewer_requests table for all records where the status
     * is 'pending' and orders them by the request_date in descending order.
     *
     * @return A list of maps containing the request details
     * @throws SQLException if a database access error occurs
     */
    public List<Map<String, Object>> getPendingReviewerRequests() throws SQLException {
        List<Map<String, Object>> requests = new ArrayList<>();
        String sql = "SELECT * FROM reviewer_requests WHERE status = 'pending' ORDER BY request_date DESC";
        try (ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> request = new HashMap<>();
                request.put("requestId", rs.getInt("request_id"));
                request.put("studentUsername", rs.getString("student_username"));
                request.put("requestDate", rs.getTimestamp("request_date"));
                requests.add(request);
            }
        }
        return requests;
    }

    /**
     * Updates the status of a reviewer request in the database.
     * 
     * This method updates the status of a reviewer request record in the reviewer_requests table.
     *
     * @param requestId The ID of the request to update
     * @param status The new status for the request
     * @param reviewerUsername The username of the reviewer
     */
    public void updateReviewerRequestStatus(int requestId, String status, String reviewerUsername, String notes) throws SQLException {
        String sql = "UPDATE reviewer_requests SET status = ?, review_date = CURRENT_TIMESTAMP, reviewer_username = ?, review_notes = ? WHERE request_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, reviewerUsername);
            pstmt.setString(3, notes);
            pstmt.setInt(4, requestId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Checks if a student has a pending reviewer request in the database.
     * 
     * This method queries the reviewer_requests table for a record where the student_username
     * matches the provided studentUsername and the status is 'pending'.
     *
     * @param studentUsername The username of the student to check for a pending request
     * @return true if the student has a pending request, false otherwise
     */
    public boolean hasPendingReviewerRequest(String studentUsername) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reviewer_requests WHERE student_username = ? AND status = 'pending'";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, studentUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    /**
     * Retrieves the status of a student's reviewer request from the database.
     * 
     * This method queries the reviewer_requests table for a record where the student_username
     * matches the provided studentUsername and orders the results by the request_date in descending order.
     *
     * @param studentUsername The username of the student to retrieve the request status for
     * @return A map containing the request status details
     */
    public Map<String, Object> getReviewerRequestStatus(String studentUsername) throws SQLException {
        String sql = "SELECT * FROM reviewer_requests WHERE student_username = ? ORDER BY request_date DESC LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, studentUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> status = new HashMap<>();
                    status.put("status", rs.getString("status"));
                    status.put("requestDate", rs.getTimestamp("request_date"));
                    status.put("reviewDate", rs.getTimestamp("review_date"));
                    status.put("reviewerUsername", rs.getString("reviewer_username"));
                    status.put("reviewNotes", rs.getString("review_notes"));
                    return status;
                }
                return null;
            }
        }
    }

    /**
     * Reports inappropriate content in the system.
     * 
     * This method inserts a new record into the reported_content table with details about
     * the reported content including the reporter, content type, content ID, and reason.
     * 
     * @param reporterUsername The username of the staff member reporting the content
     * @param contentType The type of content being reported ("question" or "answer")
     * @param contentId The ID of the question or answer being reported
     * @param reason The reason for reporting the content
     * @throws SQLException if a database access error occurs
     */
    public void reportContent(String reporterUsername, String contentType, int contentId, String reason) throws SQLException {
        String sql = "INSERT INTO reported_content (reporter_username, content_type, content_id, reason) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, reporterUsername);
            pstmt.setString(2, contentType);
            pstmt.setInt(3, contentId);
            pstmt.setString(4, reason);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Retrieves all pending reported content.
     * 
     * This method queries the reported_content table for all records where status is 'pending'
     * and orders them by timestamp in descending order.
     * 
     * @return A list of maps containing the report details
     * @throws SQLException if a database access error occurs
     */
    public List<Map<String, Object>> getPendingReportedContent() throws SQLException {
        List<Map<String, Object>> reports = new ArrayList<>();
        String sql = "SELECT * FROM reported_content WHERE status = 'pending' ORDER BY timestamp DESC";
        try (ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> report = new HashMap<>();
                report.put("reportId", rs.getInt("report_id"));
                report.put("reporterUsername", rs.getString("reporter_username"));
                report.put("contentType", rs.getString("content_type"));
                report.put("contentId", rs.getInt("content_id"));
                report.put("reason", rs.getString("reason"));
                report.put("timestamp", rs.getTimestamp("timestamp"));
                reports.add(report);
            }
        }
        return reports;
    }
    
    /**
     * Updates the status of a reported content item.
     * 
     * This method updates the status, reviewer, review notes, and review date of a
     * reported content record in the reported_content table.
     * 
     * @param reportId The ID of the report to update
     * @param status The new status for the report ("reviewed" or "dismissed")
     * @param reviewerUsername The username of the instructor who reviewed the report
     * @param reviewNotes Notes about the review decision
     * @throws SQLException if a database access error occurs
     */
    public void updateReportStatus(int reportId, String status, String reviewerUsername, String reviewNotes) throws SQLException {
        String sql = "UPDATE reported_content SET status = ?, reviewer_username = ?, review_notes = ?, review_date = CURRENT_TIMESTAMP WHERE report_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, reviewerUsername);
            pstmt.setString(3, reviewNotes);
            pstmt.setInt(4, reportId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Gets all reported content items from the database, including both pending and reviewed items.
     * 
     * This method retrieves all reported content records from the reported_content table
     * and orders them by timestamp in descending order.
     * 
     * @return A list of maps containing the report details
     * @throws SQLException if a database access error occurs
     */
    public List<Map<String, Object>> getAllReportedContent() throws SQLException {
        List<Map<String, Object>> reports = new ArrayList<>();
        String sql = "SELECT * FROM reported_content ORDER BY timestamp DESC";
        try (ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> report = new HashMap<>();
                report.put("reportId", rs.getInt("report_id"));
                report.put("reporterUsername", rs.getString("reporter_username"));
                report.put("contentType", rs.getString("content_type"));
                report.put("contentId", rs.getInt("content_id"));
                report.put("reason", rs.getString("reason"));
                report.put("timestamp", rs.getTimestamp("timestamp"));
                report.put("status", rs.getString("status"));
                report.put("reviewedBy", rs.getString("reviewer_username"));
                report.put("notes", rs.getString("review_notes"));
                report.put("reviewTimestamp", rs.getTimestamp("review_date"));
                reports.add(report);
            }
        }
        return reports;
    }

    /**
     * Bans a student in the system
     * 
     * @param studentUsername The username of the student to ban
     * @param bannedBy The username of the staff/instructor who issued the ban
     * @param reason The reason for the ban
     * @throws SQLException if a database access error occurs
     */
    public void banStudent(String studentUsername, String bannedBy, String reason) throws SQLException {
        // First check if the student is already banned
        String checkSql = "SELECT COUNT(*) FROM banned_students WHERE student_username = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, studentUsername);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Student is already banned, update the record
                    String updateSql = "UPDATE banned_students SET banned_by = ?, ban_reason = ?, ban_date = CURRENT_TIMESTAMP WHERE student_username = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        updateStmt.setString(1, bannedBy);
                        updateStmt.setString(2, reason);
                        updateStmt.setString(3, studentUsername);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Student is not banned yet, insert a new record
                    String insertSql = "INSERT INTO banned_students (student_username, banned_by, ban_reason) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setString(1, studentUsername);
                        insertStmt.setString(2, bannedBy);
                        insertStmt.setString(3, reason);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }
    
    /**
     * Unbans a student in the system
     * 
     * @param studentUsername The username of the student to unban
     * @throws SQLException if a database access error occurs
     */
    public void unbanStudent(String studentUsername) throws SQLException {
        String sql = "DELETE FROM banned_students WHERE student_username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, studentUsername);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Checks if a student is banned
     * 
     * @param studentUsername The username of the student to check
     * @return true if the student is banned, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean isStudentBanned(String studentUsername) throws SQLException {
        String sql = "SELECT COUNT(*) FROM banned_students WHERE student_username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, studentUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }
    
    /**
     * Gets a list of all banned students
     * 
     * @return List of banned student usernames
     * @throws SQLException if a database access error occurs
     */
    public List<Map<String, Object>> getBannedStudents() throws SQLException {
        List<Map<String, Object>> bannedStudents = new ArrayList<>();
        String sql = "SELECT * FROM banned_students ORDER BY ban_date DESC";
        try (ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> student = new HashMap<>();
                student.put("banId", rs.getInt("ban_id"));
                student.put("username", rs.getString("student_username"));
                student.put("bannedBy", rs.getString("banned_by"));
                student.put("reason", rs.getString("ban_reason"));
                student.put("banDate", rs.getTimestamp("ban_date"));
                bannedStudents.add(student);
            }
        }
        return bannedStudents;
    }

    /**
     * Updates or inserts a reviewer's scorecard parameters
     * 
     * @param reviewerUsername The username of the reviewer
     * @param friendliness Score from 0-5
     * @param accuracy Score from 0-5
     * @param judgement Score from 0-5
     * @param communication Score from 0-5
     * @throws SQLException if a database access error occurs
     */
    public void updateReviewerScorecard(String reviewerUsername, int friendliness, int accuracy, 
                                      int judgement, int communication) throws SQLException {
        String sql = "MERGE INTO reviewer_scorecard (reviewer_username, friendliness, accuracy, judgement, communication) "
                   + "KEY (reviewer_username) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, reviewerUsername);
            pstmt.setInt(2, friendliness);
            pstmt.setInt(3, accuracy);
            pstmt.setInt(4, judgement);
            pstmt.setInt(5, communication);
            pstmt.executeUpdate();
        }
    }

    /**
     * Gets a reviewer's scorecard
     * 
     * @param reviewerUsername The username of the reviewer
     * @return Map containing the scorecard data or null if not found
     * @throws SQLException if a database access error occurs
     */
    public Map<String, Object> getReviewerScorecard(String reviewerUsername) throws SQLException {
        String sql = "SELECT * FROM reviewer_scorecard WHERE reviewer_username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, reviewerUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> scorecard = new HashMap<>();
                    scorecard.put("friendliness", rs.getInt("friendliness"));
                    scorecard.put("accuracy", rs.getInt("accuracy"));
                    scorecard.put("judgement", rs.getInt("judgement"));
                    scorecard.put("communication", rs.getInt("communication"));
                    scorecard.put("overall_score", rs.getDouble("overall_score"));
                    return scorecard;
                }
                return null;
            }
        }
    }

    /**
     * Gets all reviewer scorecards
     * 
     * @return List of maps containing all reviewer scorecards
     * @throws SQLException if a database access error occurs
     */
    public List<Map<String, Object>> getAllReviewerScorecards() throws SQLException {
        List<Map<String, Object>> scorecards = new ArrayList<>();
        String sql = "SELECT * FROM reviewer_scorecard ORDER BY overall_score DESC";
        try (ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> scorecard = new HashMap<>();
                scorecard.put("reviewer_username", rs.getString("reviewer_username"));
                scorecard.put("friendliness", rs.getInt("friendliness"));
                scorecard.put("accuracy", rs.getInt("accuracy"));
                scorecard.put("judgement", rs.getInt("judgement"));
                scorecard.put("communication", rs.getInt("communication"));
                scorecard.put("overall_score", rs.getDouble("overall_score"));
                scorecards.add(scorecard);
            }
        }
        return scorecards;
    }

    /**
     * Updates a reviewer's profile information
     * 
     * @param username The username of the reviewer
     * @param about Information about the reviewer
     * @param experience The reviewer's experience
     * @param specialties The reviewer's specialties
     * @throws SQLException if a database access error occurs
     */
    public void updateReviewerProfile(String username, String about, String experience, String specialties) throws SQLException {
        // Check if profile exists
        String checkSql = "SELECT COUNT(*) FROM reviewer_profile WHERE username = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, username);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Update existing profile
                    String updateSql = "UPDATE reviewer_profile SET about = ?, experience = ?, specialties = ? WHERE username = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        updateStmt.setString(1, about);
                        updateStmt.setString(2, experience);
                        updateStmt.setString(3, specialties);
                        updateStmt.setString(4, username);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Insert new profile
                    String insertSql = "INSERT INTO reviewer_profile (username, about, experience, specialties) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setString(1, username);
                        insertStmt.setString(2, about);
                        insertStmt.setString(3, experience);
                        insertStmt.setString(4, specialties);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }
    
    /**
     * Gets a reviewer's profile information
     * 
     * @param username The username of the reviewer
     * @return Map containing the reviewer's profile information or null if not found
     * @throws SQLException if a database access error occurs
     */
    public Map<String, Object> getReviewerProfile(String username) throws SQLException {
        String sql = "SELECT * FROM reviewer_profile WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("about", rs.getString("about"));
                    profile.put("experience", rs.getString("experience"));
                    profile.put("specialties", rs.getString("specialties"));
                    return profile;
                }
                return null;
            }
        }
    }

    /**
     * Adds feedback for a reviewer
     * 
     * @param reviewerUsername The username of the reviewer
     * @param studentUsername The username of the student providing feedback
     * @param content The feedback content
     * @param rating The rating (1-5)
     * @throws SQLException if a database access error occurs
     */
    public void addReviewerFeedback(String reviewerUsername, String studentUsername, String content, int rating) throws SQLException {
        String sql = "INSERT INTO reviewer_feedback (reviewer_username, student_username, content, rating) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, reviewerUsername);
            pstmt.setString(2, studentUsername);
            pstmt.setString(3, content);
            pstmt.setInt(4, rating);
            pstmt.executeUpdate();
        }
    }

    /**
     * Gets all feedback for a reviewer
     * 
     * @param reviewerUsername The username of the reviewer
     * @return List of maps containing the feedback
     * @throws SQLException if a database access error occurs
     */
    public List<Map<String, Object>> getReviewerFeedback(String reviewerUsername) throws SQLException {
        List<Map<String, Object>> feedbackList = new ArrayList<>();
        String sql = "SELECT * FROM reviewer_feedback WHERE reviewer_username = ? ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, reviewerUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> feedback = new HashMap<>();
                    feedback.put("feedback_id", rs.getInt("feedback_id"));
                    feedback.put("student_username", rs.getString("student_username"));
                    feedback.put("content", rs.getString("content"));
                    feedback.put("rating", rs.getInt("rating"));
                    feedback.put("timestamp", rs.getTimestamp("timestamp"));
                    feedbackList.add(feedback);
                }
            }
        }
        return feedbackList;
    }

    /**
     * Gets all reviews by a specific reviewer
     * 
     * @param reviewerUsername The username of the reviewer
     * @return List of Review objects
     * @throws SQLException if a database access error occurs
     */
    public List<Review> getReviewsByReviewer(String reviewerUsername) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE reviewer = ? ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, reviewerUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Integer answerId = rs.getInt("answer_id");
                    if (rs.wasNull()) {
                        answerId = null;
                    }
                    
                    reviews.add(new Review(
                        rs.getInt("review_id"),
                        rs.getString("reviewer"),
                        rs.getString("content"),
                        rs.getTimestamp("timestamp"),
                        rs.getInt("question_id"),
                        answerId
                    ));
                }
            }
        }
        return reviews;
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

