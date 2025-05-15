package databasePart1;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import main.User;
import main.Question;
import main.Answer;


/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 * 
 * <p> Copyright: Team 60 CSE 360 </p>
 * 
 * @author Team 60
 * @version 1.0
 * @since 2025-04-01
 */	

public class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	public Connection connection = null;
	public Statement statement = null; 
	//	PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
	//		 statement.execute("DROP ALL OBJECTS");  

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	private void createTables() throws SQLException {
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "fullName VARCHAR(255), "
				+ "email VARCHAR(255), "
				+ "roles VARCHAR(255), "
				+ "oneTimePassword VARCHAR(255))";
		statement.execute(userTable);
		
		// Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	            + "isUsed BOOLEAN DEFAULT FALSE, "
	            + "roles VARCHAR(255), "
	            + "deadline TIMESTAMP)";
	    statement.execute(invitationCodesTable);
	}


	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database.
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, password, fullName, email, roles) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getFullName());
			pstmt.setString(4, user.getEmail());
			pstmt.setString(5, String.join(",", user.getRoles()));
			pstmt.executeUpdate();
		}
	}

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		String query = "SELECT fullName, email FROM cse360users WHERE userName = ? AND password = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					// Update user with fullName and email from database
					String fullName = rs.getString("fullName");
					String email = rs.getString("email");
					user.setFullName(fullName);
					user.setEmail(email);
					return true;
				}
			}
		}
		return false;
	}
	
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}
	
	// Retrieves the role of a user from the database using their UserName.
	public String[] getUserRoles(String userName) {
	    String query = "SELECT roles FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            String roles = rs.getString("roles");
	            return roles.split(",");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return new String[0];
	}
	
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode(String[] roles, java.sql.Timestamp deadline) {
	    String code = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code
	    String query = "INSERT INTO InvitationCodes (code, roles, deadline) VALUES (?, ?, ?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.setString(2, String.join(",", roles));
	        pstmt.setTimestamp(3, deadline);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return code;
	}
	
	// Validates an invitation code to check if it is unused.
	public boolean validateInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE AND deadline > CURRENT_TIMESTAMP";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            // Mark the code as used
	            markInvitationCodeAsUsed(code);
	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	// Gets available roles for an invitation code
	public String[] getInvitationRoles(String code) {
	    String query = "SELECT roles FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            String roles = rs.getString("roles");
	            return roles.split(",");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return new String[0];
	}

	// Closes the database connection and statement.
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

	// Add new method to get all users
	public java.util.List<User> getAllUsers() throws SQLException {
	    java.util.List<User> users = new java.util.ArrayList<>();
	    String query = "SELECT userName, fullName, email, roles FROM cse360users";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            String userName = rs.getString("userName");
	            String fullName = rs.getString("fullName");
	            String email = rs.getString("email");
	            String[] roles = rs.getString("roles").split(",");
	            users.add(new User(userName, "", fullName, email, roles));
	        }
	    }
	    return users;
	}

	/**
	 * Retrieves all users with a specific role from the database.
	 * 
	 * @param role The role to filter users by (e.g., "instructor", "admin", etc.)
	 * @return A list of users who have the specified role
	 * @throws SQLException if a database access error occurs
	 */
	public java.util.List<User> getUsersByRole(String role) throws SQLException {
	    java.util.List<User> users = new java.util.ArrayList<>();
	    String query = "SELECT userName, fullName, email, roles FROM cse360users WHERE roles LIKE ?";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, "%" + role + "%");
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            String userName = rs.getString("userName");
	            String fullName = rs.getString("fullName");
	            String email = rs.getString("email");
	            String[] roles = rs.getString("roles").split(",");
	            
	            // Verify the user actually has this role (not just a substring match)
	            boolean hasRole = false;
	            for (String userRole : roles) {
	                if (userRole.equals(role)) {
	                    hasRole = true;
	                    break;
	                }
	            }
	            
	            if (hasRole) {
	                users.add(new User(userName, "", fullName, email, roles));
	            }
	        }
	    }
	    return users;
	}

	public boolean deleteUser(String userName, String currentAdminUserName) throws SQLException {
	    // Don't allow admin to delete themselves
	    if (userName.equals(currentAdminUserName)) {
	        return false;
	    }
	    
	    String query = "DELETE FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        int rowsAffected = pstmt.executeUpdate();
	        return rowsAffected > 0;
	    }
	}

	public boolean updateUserRoles(String userName, String[] newRoles, String currentAdminUserName) throws SQLException {
	    // Don't allow removing admin role from current admin
	    if (userName.equals(currentAdminUserName)) {
	        boolean hasAdmin = false;
	        for (String role : newRoles) {
	            if (role.equals("admin")) {
	                hasAdmin = true;
	                break;
	            }
	        }
	        if (!hasAdmin) return false;
	    }

	    // Check if this would remove the last admin
	    if (!userName.equals(currentAdminUserName)) {
	        String query = "SELECT COUNT(*) as adminCount FROM cse360users WHERE roles LIKE '%admin%' AND userName != ?";
	        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	            pstmt.setString(1, userName);
	            ResultSet rs = pstmt.executeQuery();
	            if (rs.next()) {
	                int adminCount = rs.getInt("adminCount");
	                boolean willHaveAdmin = false;
	                for (String role : newRoles) {
	                    if (role.equals("admin")) {
	                        willHaveAdmin = true;
	                        break;
	                    }
	                }
	                if (adminCount == 0 && !willHaveAdmin) {
	                    return false;
	                }
	            }
	        }
	    }

	    // Update the roles
	    String updateQuery = "UPDATE cse360users SET roles = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
	        pstmt.setString(1, String.join(",", newRoles));
	        pstmt.setString(2, userName);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	public void setOneTimePassword(String userName, String oneTimePassword) throws SQLException {
	    String query = "UPDATE cse360users SET oneTimePassword = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, oneTimePassword);
	        pstmt.setString(2, userName);
	        pstmt.executeUpdate();
	    }
	}
	
	// Checks if the one time password entered by the user is correct
	public boolean validateOneTimePassword(String userName, String oneTimePassword) throws SQLException {
	    String query = "SELECT oneTimePassword FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            String storedOneTime = rs.getString("oneTimePassword");
	            return storedOneTime != null && !storedOneTime.isEmpty() && storedOneTime.equals(oneTimePassword);
	        }
	    }
	    return false;
	}

	// Updates a user's password and then clears one time password
	public void updatePasswordAndClearOneTime(String userName, String newPassword) throws SQLException {
	    String query = "UPDATE cse360users SET password = ?, oneTimePassword = NULL WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newPassword);
	        pstmt.setString(2, userName);
	        pstmt.executeUpdate();
	    }
	}
}
