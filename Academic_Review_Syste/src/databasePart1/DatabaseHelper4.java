package databasePart1;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import main.Request;

/**
 * Database helper class for managing Admin Requests.
 */
public class DatabaseHelper4 {
    public Connection connection;
    // Use H2 database details, consistent with other helpers
    private static final String JDBC_DRIVER = "org.h2.Driver";   
    private static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  
    private static final String USER = "sa"; 
    private static final String PASS = ""; 

    // --- Database Connection --- 

    public void connectToDatabase() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Use the correct H2 driver
                Class.forName(JDBC_DRIVER); 
                // Use H2 URL and credentials
                connection = DriverManager.getConnection(DB_URL, USER, PASS); 
                System.out.println("DatabaseHelper4: Connection to H2 database has been established.");
                initializeDatabase(); // Ensure table exists
            } catch (ClassNotFoundException e) {
                // Update error message for H2
                System.err.println("DatabaseHelper4: H2 JDBC driver not found."); 
                throw new SQLException("H2 JDBC driver not found.", e);
            } catch (SQLException e) {
                System.err.println("DatabaseHelper4: Failed to connect to the H2 database.");
                throw e;
            }
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("DatabaseHelper4: Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("DatabaseHelper4: Error closing database connection.");
            e.printStackTrace();
        }
    }

    // --- Database Initialization --- 

    private void initializeDatabase() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS AdminRequests (
                request_id INTEGER PRIMARY KEY AUTO_INCREMENT,
                requester_username TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'open', -- 'open', 'closed'
                creation_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                last_update_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                closed_timestamp DATETIME NULL, 
                closed_by_username TEXT NULL,
                admin_notes TEXT NULL,
                reopened_from_id INTEGER NULL, -- Link to original request if this is a reopened one
                has_been_reopened BOOLEAN DEFAULT FALSE, -- Flag if the original request was reopened
                FOREIGN KEY (requester_username) REFERENCES cse360users(userName),
                FOREIGN KEY (closed_by_username) REFERENCES cse360users(userName),
                FOREIGN KEY (reopened_from_id) REFERENCES AdminRequests(request_id)
            );
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("DatabaseHelper4: AdminRequests table initialized or already exists.");
        } catch (SQLException e) {
            System.err.println("DatabaseHelper4: Error creating/checking AdminRequests table.");
            throw e;
        }
    }

    // --- Request CRUD Operations --- 

    /**
     * Creates a new admin request.
     *
     * @param requesterUsername Username of the instructor making the request.
     * @param title             Title of the request.
     * @param description       Detailed description of the request.
     * @return The newly created Request object, or null if creation failed.
     * @throws SQLException If a database error occurs.
     */
    public Request createRequest(String requesterUsername, String title, String description) throws SQLException {
        String sql = "INSERT INTO AdminRequests (requester_username, title, description, last_update_timestamp) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        connectToDatabase(); // Ensure connection

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, requesterUsername);
            pstmt.setString(2, title);
            pstmt.setString(3, description);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        return getRequestById(newId); // Retrieve the full request object
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("DatabaseHelper4: Error creating request for user " + requesterUsername);
            throw e;
        }
        return null; // Indicate failure
    }
    
    /**
     * Retrieves a specific request by its ID.
     *
     * @param requestId The ID of the request.
     * @return The Request object, or null if not found.
     * @throws SQLException If a database error occurs.
     */
    public Request getRequestById(int requestId) throws SQLException {
        String sql = "SELECT * FROM AdminRequests WHERE request_id = ?";
        connectToDatabase();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, requestId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRequest(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("DatabaseHelper4: Error retrieving request ID " + requestId);
            throw e;
        }
        return null;
    }

    /**
     * Retrieves all requests with 'open' status.
     *
     * @return A list of open Request objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Request> getAllOpenRequests() throws SQLException {
        return getRequestsByStatus("open");
    }

    /**
     * Retrieves all requests with 'closed' status.
     *
     * @return A list of closed Request objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Request> getAllClosedRequests() throws SQLException {
        return getRequestsByStatus("closed");
    }
    
    /**
     * Retrieves closed requests that have NOT been reopened.
     *
     * @return A list of closed, non-reopened Request objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Request> getClosedRequestsNotReopened() throws SQLException {
        String sql = "SELECT * FROM AdminRequests WHERE status = 'closed' AND has_been_reopened = FALSE ORDER BY closed_timestamp DESC";
        return executeRequestQuery(sql);
    }
    
    /**
     * Retrieves closed requests that HAVE been reopened (i.e., they are the original request that led to a new one).
     *
     * @return A list of closed, reopened Request objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Request> getClosedRequestsThatWereReopened() throws SQLException {
        String sql = "SELECT * FROM AdminRequests WHERE status = 'closed' AND has_been_reopened = TRUE ORDER BY closed_timestamp DESC";
        return executeRequestQuery(sql);
    }

    private List<Request> getRequestsByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM AdminRequests WHERE status = ? ORDER BY last_update_timestamp DESC";
        connectToDatabase();
        List<Request> requests = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapResultSetToRequest(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("DatabaseHelper4: Error retrieving requests with status " + status);
            throw e;
        }
        return requests;
    }
    
    private List<Request> executeRequestQuery(String sql) throws SQLException {
        connectToDatabase();
        List<Request> requests = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                requests.add(mapResultSetToRequest(rs));
            }
        } catch (SQLException e) {
            System.err.println("DatabaseHelper4: Error executing request query.");
            throw e;
        }
        return requests;
    }

    /**
     * Closes an open admin request.
     *
     * @param requestId      The ID of the request to close.
     * @param adminUsername  The username of the admin closing the request.
     * @param adminNotes     Notes from the admin about the resolution.
     * @return true if the request was closed successfully, false otherwise.
     * @throws SQLException If a database error occurs.
     */
    public boolean closeRequest(int requestId, String adminUsername, String adminNotes) throws SQLException {
        String sql = "UPDATE AdminRequests SET status = 'closed', closed_by_username = ?, admin_notes = ?, closed_timestamp = CURRENT_TIMESTAMP, last_update_timestamp = CURRENT_TIMESTAMP WHERE request_id = ? AND status = 'open'";
        connectToDatabase();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, adminUsername);
            pstmt.setString(2, adminNotes);
            pstmt.setInt(3, requestId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("DatabaseHelper4: Error closing request ID " + requestId);
            throw e;
        }
    }

    /**
     * Reopens a closed admin request by creating a new request linked to the original.
     * Updates the original request to mark it as 'has_been_reopened'.
     *
     * @param originalRequestId The ID of the closed request to reopen.
     * @param reopeningUsername The username of the instructor reopening the request.
     * @param updatedDescription The new or updated description for the reopened request.
     * @return The newly created (reopened) Request object, or null if reopening failed.
     * @throws SQLException If a database error occurs or the original request cannot be reopened.
     */
    public Request reopenRequest(int originalRequestId, String reopeningUsername, String updatedDescription) throws SQLException {
        connectToDatabase();
        Request originalRequest = getRequestById(originalRequestId);

        // Validation
        if (originalRequest == null) {
            throw new SQLException("Original request ID " + originalRequestId + " not found.");
        }
        if (originalRequest.isOpen()) {
            throw new SQLException("Request ID " + originalRequestId + " is already open.");
        }
        if (originalRequest.hasBeenReopened()) {
             throw new SQLException("Request ID " + originalRequestId + " has already been reopened.");
        }
        if (!originalRequest.getRequesterUsername().equals(reopeningUsername)) {
             // Optional: Only allow original requester to reopen? Or any instructor?
             // For now, allowing any instructor as per general prompt.
             // Consider adding role check if needed.
        }

        // 1. Create the new 'reopened' request
        String insertSql = "INSERT INTO AdminRequests (requester_username, title, description, reopened_from_id, last_update_timestamp) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        String newTitle = "[Reopened] " + originalRequest.getTitle();
        int newRequestId = -1;

        try (PreparedStatement pstmtInsert = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmtInsert.setString(1, reopeningUsername); // Could be original or reopening user
            pstmtInsert.setString(2, newTitle);
            pstmtInsert.setString(3, updatedDescription); // Use the new description
            pstmtInsert.setInt(4, originalRequestId);

            int affectedRows = pstmtInsert.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmtInsert.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newRequestId = generatedKeys.getInt(1);
                    }
                }
            } else {
                 throw new SQLException("Failed to create the new reopened request entry.");
            }
        } catch (SQLException e) {
            System.err.println("DatabaseHelper4: Error creating reopened request based on ID " + originalRequestId);
            throw e;
        }

        // 2. Update the original request to mark it as reopened
        String updateSql = "UPDATE AdminRequests SET has_been_reopened = TRUE, last_update_timestamp = CURRENT_TIMESTAMP WHERE request_id = ?";
        try (PreparedStatement pstmtUpdate = connection.prepareStatement(updateSql)) {
            pstmtUpdate.setInt(1, originalRequestId);
            int updateAffectedRows = pstmtUpdate.executeUpdate();
            if (updateAffectedRows == 0) {
                 // This is unlikely if the first part succeeded, but good to check
                 System.err.println("DatabaseHelper4: Failed to mark original request ID " + originalRequestId + " as reopened after creating new one.");
                 // Consider rollback logic here if atomicity is critical
            }
        } catch (SQLException e) {
            System.err.println("DatabaseHelper4: Error marking original request ID " + originalRequestId + " as reopened.");
            // Consider rollback logic here
            throw e;
        }

        // 3. Return the newly created request object
        return getRequestById(newRequestId);
    }
    
    /**
     * Updates the description of an open request.
     *
     * @param requestId         The ID of the request to update.
     * @param newDescription    The new description text.
     * @param updatingUsername  The username of the user performing the update (for validation).
     * @return true if the update was successful, false otherwise.
     * @throws SQLException If a database error occurs or validation fails.
     */
    public boolean updateRequestDescription(int requestId, String newDescription, String updatingUsername) throws SQLException {
        connectToDatabase();
        Request request = getRequestById(requestId);

        if (request == null) {
            throw new SQLException("Request ID " + requestId + " not found.");
        }
        if (!request.isOpen()) {
            throw new SQLException("Cannot update description of a closed request (ID: " + requestId + "). Reopen it first.");
        }
        // Optional: Add check if updatingUsername must be the original requester
        // if (!request.getRequesterUsername().equals(updatingUsername)) {
        //     throw new SQLException("Only the original requester can update the description.");
        // }

        String sql = "UPDATE AdminRequests SET description = ?, last_update_timestamp = CURRENT_TIMESTAMP WHERE request_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newDescription);
            pstmt.setInt(2, requestId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("DatabaseHelper4: Error updating description for request ID " + requestId);
            throw e;
        }
    }


    // --- Helper Methods --- 

    private Request mapResultSetToRequest(ResultSet rs) throws SQLException {
        int id = rs.getInt("request_id");
        String requester = rs.getString("requester_username");
        String title = rs.getString("title");
        String description = rs.getString("description");
        String status = rs.getString("status");
        Timestamp creationTs = rs.getTimestamp("creation_timestamp");
        Timestamp lastUpdateTs = rs.getTimestamp("last_update_timestamp");
        Timestamp closedTs = rs.getTimestamp("closed_timestamp");
        String closedBy = rs.getString("closed_by_username");
        String adminNotes = rs.getString("admin_notes");
        int reopenedFromIdInt = rs.getInt("reopened_from_id");
        Integer reopenedFromId = rs.wasNull() ? null : reopenedFromIdInt;
        boolean hasBeenReopened = rs.getBoolean("has_been_reopened");

        return new Request(
            id,
            requester,
            title,
            description,
            status,
            creationTs == null ? null : new Date(creationTs.getTime()),
            lastUpdateTs == null ? null : new Date(lastUpdateTs.getTime()),
            closedTs == null ? null : new Date(closedTs.getTime()),
            closedBy,
            adminNotes,
            reopenedFromId,
            hasBeenReopened
        );
    }
} 