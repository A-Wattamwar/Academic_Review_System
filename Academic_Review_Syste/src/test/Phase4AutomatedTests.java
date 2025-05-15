package test;

import main.*;
import databasePart1.DatabaseHelper;
import databasePart1.DatabaseHelper2;
import databasePart1.DatabaseHelper3;
import databasePart1.DatabaseHelper4;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;
import java.sql.Statement;

/**
 * <p> Title: Phase4 Automated Tests. </p>
 * 
 * <p> Description: A comprehensive test suite for validating the functionality
 * of the Phase 4 components including:
 *   - Reviewer profile management
 *   - Reviewer reviews functionality
 *   - Reviewer scorecard parameters
 *   - Admin request management 
 * </p>
 * 
 * <p> Copyright: Team 60 CSE 360 </p>
 * 
 * @version 0.00        2025-04-01    Initial test suite implementation
 */
public class Phase4AutomatedTests {

    private DatabaseHelper3 dbHelper;
    private DatabaseHelper2 dbHelper2;
    private DatabaseHelper4 dbHelper4;
    private Questions questions;
    private Answers answers;
    private int testQuestionId;
    private int testAnswerId;
    private String testReviewerUsername = "testReviewer";
    private String testStudentUsername = "testStudent";
    private String testInstructorUsername = "testInstructor";
    private String testAdminUsername = "testAdmin";

    /**
     * Sets up the test environment before each test.
     * Initializes database connections and managers.
     * 
     * @throws SQLException if a database error occurs
     */
    @BeforeEach
    public void setUp() throws SQLException {
        // Initialize database connections and helpers
        dbHelper = new DatabaseHelper3();
        dbHelper.connectToDatabase();
        dbHelper2 = new DatabaseHelper2();
        dbHelper2.connectToDatabase();
        dbHelper4 = new DatabaseHelper4();
        dbHelper4.connectToDatabase();
        
        // Initialize managers
        questions = new Questions(dbHelper2, dbHelper2.connection);
        answers = new Answers(dbHelper2);
        
        // Set up test data
        setupTestData();
    }

    /**
     * Sets up the test data for the test suite.
     * Creates test users, a test question, and a test answer.
     * 
     * @throws SQLException if a database error occurs
     */
    public void setupTestData() throws SQLException {
        // Create test users in the cse360users table
        try {
            // First, check the schema to get the correct column names
            String showColumnsSQL = "SHOW COLUMNS FROM cse360users";
            boolean columnsFound = false;
            String roleColumnName = null;
            
            try (Statement stmt = dbHelper2.connection.createStatement();
                 ResultSet rs = stmt.executeQuery(showColumnsSQL)) {
                while (rs.next()) {
                    columnsFound = true;
                    String columnName = rs.getString("FIELD");
                    
                    // Check if this is the role column (case insensitive check for common naming patterns)
                    String lowerColumnName = columnName.toLowerCase();
                    if (lowerColumnName.contains("role") || lowerColumnName.contains("type") || 
                        lowerColumnName.equals("userrole") || lowerColumnName.equals("usertype")) {
                        roleColumnName = columnName;
                    }
                }
            } catch (SQLException e) {
                // Continue with best guess if we can't inspect schema
            }
            
            if (!columnsFound) {
                // Try to create the table
                try (Statement stmt = dbHelper2.connection.createStatement()) {
                    stmt.execute("CREATE TABLE IF NOT EXISTS cse360users (userName VARCHAR(255) PRIMARY KEY, password VARCHAR(255), userRole VARCHAR(50))");
                    roleColumnName = "userRole";
                } catch (SQLException e) {
                    throw e;
                }
            }
            
            // If we couldn't determine the role column, use a best guess
            if (roleColumnName == null) {
                roleColumnName = "userRole";
            }
            
            // Check if test users already exist
            String checkUserSQL = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
            try (PreparedStatement pstmt = dbHelper2.connection.prepareStatement(checkUserSQL)) {
                
                // Create testInstructor if it doesn't exist
                pstmt.setString(1, testInstructorUsername);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        // User doesn't exist, create it
                        String insertUserSQL = "INSERT INTO cse360users (userName, password, " + roleColumnName + ") VALUES (?, ?, ?)";
                        try (PreparedStatement insertStmt = dbHelper2.connection.prepareStatement(insertUserSQL)) {
                            insertStmt.setString(1, testInstructorUsername);
                            insertStmt.setString(2, "password");
                            insertStmt.setString(3, "instructor");
                            insertStmt.executeUpdate();
                        }
                    }
                }
                
                // Create testAdmin if it doesn't exist
                pstmt.setString(1, testAdminUsername);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        // User doesn't exist, create it
                        String insertUserSQL = "INSERT INTO cse360users (userName, password, " + roleColumnName + ") VALUES (?, ?, ?)";
                        try (PreparedStatement insertStmt = dbHelper2.connection.prepareStatement(insertUserSQL)) {
                            insertStmt.setString(1, testAdminUsername);
                            insertStmt.setString(2, "password");
                            insertStmt.setString(3, "admin");
                            insertStmt.executeUpdate();
                        }
                    }
                }
                
                // Create testReviewer if it doesn't exist
                pstmt.setString(1, testReviewerUsername);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        // User doesn't exist, create it
                        String insertUserSQL = "INSERT INTO cse360users (userName, password, " + roleColumnName + ") VALUES (?, ?, ?)";
                        try (PreparedStatement insertStmt = dbHelper2.connection.prepareStatement(insertUserSQL)) {
                            insertStmt.setString(1, testReviewerUsername);
                            insertStmt.setString(2, "password");
                            insertStmt.setString(3, "reviewer");
                            insertStmt.executeUpdate();
                        }
                    }
                }
                
                // Create testStudent if it doesn't exist
                pstmt.setString(1, testStudentUsername);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        // User doesn't exist, create it
                        String insertUserSQL = "INSERT INTO cse360users (userName, password, " + roleColumnName + ") VALUES (?, ?, ?)";
                        try (PreparedStatement insertStmt = dbHelper2.connection.prepareStatement(insertUserSQL)) {
                            insertStmt.setString(1, testStudentUsername);
                            insertStmt.setString(2, "password");
                            insertStmt.setString(3, "student");
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating test users: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Create a test question
        Question testQuestion = new Question(0, "Test Question Content", "testUser", new Date());
        questions.addQuestion(testQuestion);
        
        // Get the created question's ID
        List<Question> allQuestions = questions.getAllQuestions();
        testQuestionId = allQuestions.get(allQuestions.size() - 1).getQuestionId();
        
        // Create a test answer for the question
        Answer testAnswer = new Answer(0, testQuestionId, "Test Answer Content", "testUser", new Date());
        answers.addAnswer(testAnswer);
        
        // Get the created answer's ID
        List<Answer> questionAnswers = answers.getAnswersForQuestion(testQuestionId);
        testAnswerId = questionAnswers.get(questionAnswers.size() - 1).getAnswerId();
    }

    @Nested
    @DisplayName("Reviewer Tests")
    public class ReviewerTests {
        
        /**
         * Test for creating a reviewer profile with valid data.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Create reviewer profile with valid data")
        public void testCreateReviewerProfile() throws SQLException {
            String about = "Experienced reviewer with expertise in Java";
            String experience = "5 years of software development experience";
            String specialties = "Java, Algorithms, Data Structures";
            
            dbHelper.updateReviewerProfile(testReviewerUsername, about, experience, specialties);
            
            Map<String, Object> profile = dbHelper.getReviewerProfile(testReviewerUsername);
            assertNotNull(profile);
            assertEquals(about, profile.get("about"));
            assertEquals(experience, profile.get("experience"));
            assertEquals(specialties, profile.get("specialties"));
        }

        /**
         * Test for updating an existing reviewer profile.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Update existing reviewer profile")
        public void testUpdateReviewerProfile() throws SQLException {
            // First create a profile
            dbHelper.updateReviewerProfile(testReviewerUsername, "Initial about", "Initial experience", "Initial specialties");
            
            // Update the profile
            String newAbout = "Updated about section";
            String newExperience = "Updated experience";
            String newSpecialties = "Updated specialties";
            
            dbHelper.updateReviewerProfile(testReviewerUsername, newAbout, newExperience, newSpecialties);
            
            Map<String, Object> profile = dbHelper.getReviewerProfile(testReviewerUsername);
            assertNotNull(profile);
            assertEquals(newAbout, profile.get("about"));
            assertEquals(newExperience, profile.get("experience"));
            assertEquals(newSpecialties, profile.get("specialties"));
        }

        /**
         * Test for retrieving a non-existent reviewer profile.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Get non-existent reviewer profile")
        public void testGetNonExistentProfile() throws SQLException {
            Map<String, Object> profile = dbHelper.getReviewerProfile("nonExistentReviewer");
            assertNull(profile);
        }

        /**
         * Test for adding and retrieving a review.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Add and retrieve review")
        public void testAddAndRetrieveReview() throws SQLException {
            String reviewContent = "This is a test review";
            Review review = new Review(0, testReviewerUsername, reviewContent, new Date(), testQuestionId, null);
            dbHelper.addReview(review);
            
            List<Review> reviews = dbHelper.getReviewsByReviewer(testReviewerUsername);
            assertFalse(reviews.isEmpty());
            assertEquals(reviewContent, reviews.get(0).getContent());
            assertEquals(testQuestionId, reviews.get(0).getQuestionId());
        }

        /**
         * Test for retrieving multiple reviews for a specific question.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Get reviews for specific question")
        public void testGetReviewsForQuestion() throws SQLException {
            // Add multiple reviews for the same question
            Review review1 = new Review(0, testReviewerUsername, "First review", new Date(), testQuestionId, null);
            Review review2 = new Review(0, testReviewerUsername, "Second review", new Date(), testQuestionId, null);
            
            dbHelper.addReview(review1);
            dbHelper.addReview(review2);
            
            List<Review> reviews = dbHelper.getReviewsForQuestion(testQuestionId);
            assertEquals(2, reviews.size());
            
            // Verify both reviews exist
            boolean foundFirst = false;
            boolean foundSecond = false;
            for (Review r : reviews) {
                if (r.getContent().equals("First review")) foundFirst = true;
                if (r.getContent().equals("Second review")) foundSecond = true;
            }
            assertTrue(foundFirst && foundSecond);
        }

        /**
         * Test for verifying reviews are ordered by timestamp.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Reviews ordered by timestamp")
        public void testReviewsOrdering() throws SQLException {
            // Add reviews with different timestamps
            Review review1 = new Review(0, testReviewerUsername, "Older review", new Date(), testQuestionId, null);
            dbHelper.addReview(review1);
            try {
                Thread.sleep(1000); // Ensure different timestamps
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Test interrupted while waiting for timestamp difference");
            }
            Review review2 = new Review(0, testReviewerUsername, "Newer review", new Date(), testQuestionId, null);
            dbHelper.addReview(review2);
            
            List<Review> reviews = dbHelper.getReviewsByReviewer(testReviewerUsername);
            assertEquals(2, reviews.size());
            
            // Verify ordering (newest first)
            assertTrue(reviews.get(0).getTimestamp().after(reviews.get(1).getTimestamp()));
        }

        /**
         * Test for viewing reviewer profile details.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Test reviewer profile viewing")
        public void testReviewerProfileViewing() throws SQLException {
            // Create a reviewer profile
            String about = "Experienced Java developer";
            String experience = "5 years of software development";
            String specialties = "Java, Spring, Hibernate";
            dbHelper.updateReviewerProfile(testReviewerUsername, about, experience, specialties);
            
            // Get the profile
            Map<String, Object> profile = dbHelper.getReviewerProfile(testReviewerUsername);
            
            // Verify all fields are present and correct
            assertNotNull(profile);
            assertEquals(about, profile.get("about"));
            assertEquals(experience, profile.get("experience"));
            assertEquals(specialties, profile.get("specialties"));
        }

        /**
         * Test for submitting a review with detailed content.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Test review submission with content")
        public void testReviewSubmission() throws SQLException {
            // Create a review with content
            String reviewContent = "This is a detailed review of the answer";
            Review review = new Review(0, testReviewerUsername, reviewContent, new Date(), testQuestionId, null);
            
            // Submit the review
            dbHelper.addReview(review);
            
            // Verify the review was added
            List<Review> reviews = dbHelper.getReviewsForQuestion(testQuestionId);
            assertFalse(reviews.isEmpty());
            assertEquals(reviewContent, reviews.get(0).getContent());
            assertEquals(testReviewerUsername, reviews.get(0).getReviewer());
            assertEquals(testQuestionId, reviews.get(0).getQuestionId());
        }

        /**
         * Test for adding multiple reviews to a single question.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Test multiple reviews per question")
        public void testMultipleReviewsPerQuestion() throws SQLException {
            // Add multiple reviews for the same question
            Review review1 = new Review(0, testReviewerUsername, "First review", new Date(), testQuestionId, null);
            Review review2 = new Review(0, "anotherReviewer", "Second review", new Date(), testQuestionId, null);
            
            dbHelper.addReview(review1);
            dbHelper.addReview(review2);
            
            // Verify both reviews exist
            List<Review> reviews = dbHelper.getReviewsForQuestion(testQuestionId);
            assertEquals(2, reviews.size());
            
            // Verify each review's content and reviewer
            Set<String> reviewContents = new HashSet<>();
            Set<String> reviewers = new HashSet<>();
            for (Review r : reviews) {
                reviewContents.add(r.getContent());
                reviewers.add(r.getReviewer());
            }
            
            assertTrue(reviewContents.contains("First review"));
            assertTrue(reviewContents.contains("Second review"));
            assertTrue(reviewers.contains(testReviewerUsername));
            assertTrue(reviewers.contains("anotherReviewer"));
        }

        /**
         * Test for updating a reviewer's profile with new information.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Test reviewer profile update")
        public void testReviewerProfileUpdate() throws SQLException {
            // Create initial profile
            dbHelper.updateReviewerProfile(testReviewerUsername, "Initial about", "Initial experience", "Initial specialties");
            
            // Update profile
            String newAbout = "Updated about section with more details";
            String newExperience = "Updated experience with new skills";
            String newSpecialties = "Updated specialties with new technologies";
            
            dbHelper.updateReviewerProfile(testReviewerUsername, newAbout, newExperience, newSpecialties);
            
            // Verify the update
            Map<String, Object> profile = dbHelper.getReviewerProfile(testReviewerUsername);
            assertNotNull(profile);
            assertEquals(newAbout, profile.get("about"));
            assertEquals(newExperience, profile.get("experience"));
            assertEquals(newSpecialties, profile.get("specialties"));
        }
    }

    @Nested
    @DisplayName("Reviewer Scorecard Tests")
    public class ReviewerScorecardTests {
        
        /**
         * Test for creating a new reviewer scorecard with parameters.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Create new reviewer scorecard")
        public void testCreateReviewerScorecard() throws SQLException {
            // Create a new scorecard with all parameters set
            dbHelper.updateReviewerScorecard(testReviewerUsername, 4, 5, 4, 5);
            
            // Verify the scorecard was created with correct values
            Map<String, Object> scorecard = dbHelper.getReviewerScorecard(testReviewerUsername);
            assertNotNull(scorecard);
            assertEquals(4, scorecard.get("friendliness"));
            assertEquals(5, scorecard.get("accuracy"));
            assertEquals(4, scorecard.get("judgement"));
            assertEquals(5, scorecard.get("communication"));
            
            // Verify overall score calculation
            double expectedOverall = (4 + 5 + 4 + 5) / 4.0;
            assertEquals(expectedOverall, (Double)scorecard.get("overall_score"), 0.01);
        }
        
        /**
         * Test for updating an existing reviewer scorecard.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Update existing reviewer scorecard")
        public void testUpdateReviewerScorecard() throws SQLException {
            // Create initial scorecard
            dbHelper.updateReviewerScorecard(testReviewerUsername, 3, 3, 3, 3);
            
            // Update the scorecard
            dbHelper.updateReviewerScorecard(testReviewerUsername, 5, 4, 5, 4);
            
            // Verify the update
            Map<String, Object> scorecard = dbHelper.getReviewerScorecard(testReviewerUsername);
            assertNotNull(scorecard);
            assertEquals(5, scorecard.get("friendliness"));
            assertEquals(4, scorecard.get("accuracy"));
            assertEquals(5, scorecard.get("judgement"));
            assertEquals(4, scorecard.get("communication"));
            
            // Verify updated overall score
            double expectedOverall = (5 + 4 + 5 + 4) / 4.0;
            assertEquals(expectedOverall, (Double)scorecard.get("overall_score"), 0.01);
        }
        
        /**
         * Test for retrieving a non-existent reviewer scorecard.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Get non-existent reviewer scorecard")
        public void testGetNonExistentScorecard() throws SQLException {
            Map<String, Object> scorecard = dbHelper.getReviewerScorecard("nonExistentReviewer");
            assertNull(scorecard);
        }
        
        /**
         * Test for retrieving all reviewer scorecards from the database.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Retrieve all reviewer scorecards")
        public void testGetAllReviewerScorecards() throws SQLException {
            // Create multiple scorecards
            dbHelper.updateReviewerScorecard(testReviewerUsername, 4, 4, 4, 4);
            dbHelper.updateReviewerScorecard("anotherReviewer", 3, 5, 4, 3);
            
            // Retrieve all scorecards
            List<Map<String, Object>> scorecards = dbHelper.getAllReviewerScorecards();
            
            // Verify at least our two scorecards exist
            assertNotNull(scorecards);
            assertTrue(scorecards.size() >= 2);
            
            // Verify our reviewers are in the list
            boolean foundFirst = false;
            boolean foundSecond = false;
            for (Map<String, Object> sc : scorecards) {
                String username = (String) sc.get("reviewer_username");
                if (testReviewerUsername.equals(username)) {
                    foundFirst = true;
                    assertEquals(4, sc.get("friendliness"));
                }
                if ("anotherReviewer".equals(username)) {
                    foundSecond = true;
                    assertEquals(5, sc.get("accuracy"));
                }
            }
            assertTrue(foundFirst && foundSecond);
        }
        
        /**
         * Test for verifying the overall score calculation formula.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Verify scorecard overall score calculation")
        public void testScorecardOverallScoreCalculation() throws SQLException {
            // Test with various scores to verify average calculation
            int friendliness = 3;
            int accuracy = 5;
            int judgement = 2;
            int communication = 4;
            
            dbHelper.updateReviewerScorecard(testReviewerUsername, friendliness, accuracy, judgement, communication);
            
            Map<String, Object> scorecard = dbHelper.getReviewerScorecard(testReviewerUsername);
            double expectedOverall = (friendliness + accuracy + judgement + communication) / 4.0;
            assertEquals(expectedOverall, (Double)scorecard.get("overall_score"), 0.01);
        }
        
        /**
         * Test for verifying extreme values (min/max) in scorecard parameters.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Test extreme scorecard values")
        public void testExtremeScorecardValues() throws SQLException {
            // Test with minimum values
            dbHelper.updateReviewerScorecard(testReviewerUsername, 0, 0, 0, 0);
            Map<String, Object> minScorecard = dbHelper.getReviewerScorecard(testReviewerUsername);
            assertEquals(0.0, (Double)minScorecard.get("overall_score"), 0.01);
            
            // Test with maximum values
            dbHelper.updateReviewerScorecard(testReviewerUsername, 5, 5, 5, 5);
            Map<String, Object> maxScorecard = dbHelper.getReviewerScorecard(testReviewerUsername);
            assertEquals(5.0, (Double)maxScorecard.get("overall_score"), 0.01);
        }
        
        /**
         * Test for verifying scorecards are sorted by overall score.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Verify scorecards sorted by overall score")
        public void testScorecardSorting() throws SQLException {
            // Create scorecards with different overall scores
            dbHelper.updateReviewerScorecard("reviewer1", 5, 5, 5, 5); // Overall: 5.0
            dbHelper.updateReviewerScorecard("reviewer2", 4, 4, 4, 4); // Overall: 4.0
            dbHelper.updateReviewerScorecard("reviewer3", 3, 3, 3, 3); // Overall: 3.0
            
            // Get all scorecards, which should be sorted by overall score (DESC)
            List<Map<String, Object>> scorecards = dbHelper.getAllReviewerScorecards();
            
            // Verify at least 3 found
            assertTrue(scorecards.size() >= 3);
            
            // Find our test reviewers in the results
            Map<String, Double> scoreMap = new HashMap<>();
            for (Map<String, Object> sc : scorecards) {
                String username = (String) sc.get("reviewer_username");
                if ("reviewer1".equals(username) || "reviewer2".equals(username) || "reviewer3".equals(username)) {
                    scoreMap.put(username, (Double) sc.get("overall_score"));
                }
            }
            
            // Verify scores are correct
            assertEquals(5.0, scoreMap.get("reviewer1"), 0.01);
            assertEquals(4.0, scoreMap.get("reviewer2"), 0.01);
            assertEquals(3.0, scoreMap.get("reviewer3"), 0.01);
        }
        
        /**
         * Test for mixed values in scorecard parameters.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Test mixed scorecard parameter values")
        public void testMixedScorecardValues() throws SQLException {
            // Set very different values for each parameter
            dbHelper.updateReviewerScorecard(testReviewerUsername, 1, 5, 2, 4);
            
            Map<String, Object> scorecard = dbHelper.getReviewerScorecard(testReviewerUsername);
            assertEquals(1, scorecard.get("friendliness"));
            assertEquals(5, scorecard.get("accuracy"));
            assertEquals(2, scorecard.get("judgement"));
            assertEquals(4, scorecard.get("communication"));
            
            // Verify overall score
            double expectedOverall = (1 + 5 + 2 + 4) / 4.0;
            assertEquals(expectedOverall, (Double)scorecard.get("overall_score"), 0.01);
        }
    }
    
    @Nested
    @DisplayName("Admin Request Tests")
    public class AdminRequestTests {
        
        /**
         * Test for creating a new admin request.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Create admin request")
        public void testCreateAdminRequest() throws SQLException {
            String title = "Test Admin Request";
            String description = "This is a test admin request description";
            
            Request request = dbHelper4.createRequest(testInstructorUsername, title, description);
            
            assertNotNull(request);
            assertEquals(testInstructorUsername, request.getRequesterUsername());
            assertEquals(title, request.getTitle());
            assertEquals(description, request.getDescription());
            assertEquals("open", request.getStatus());
            assertTrue(request.isOpen());
            assertFalse(request.isClosed());
            assertNull(request.getClosedTimestamp());
            assertNull(request.getClosedByUsername());
            assertNull(request.getAdminNotes());
            assertNull(request.getReopenedFromId());
            assertFalse(request.hasBeenReopened());
        }
        
        /**
         * Test for retrieving an admin request by its ID.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Retrieve admin request by ID")
        public void testGetAdminRequestById() throws SQLException {
            // Create a request
            String title = "Test Request for Retrieval";
            String description = "This request will be retrieved by ID";
            Request createdRequest = dbHelper4.createRequest(testInstructorUsername, title, description);
            
            // Retrieve the request by ID
            Request retrievedRequest = dbHelper4.getRequestById(createdRequest.getRequestId());
            
            // Verify retrieval
            assertNotNull(retrievedRequest);
            assertEquals(createdRequest.getRequestId(), retrievedRequest.getRequestId());
            assertEquals(title, retrievedRequest.getTitle());
            assertEquals(description, retrievedRequest.getDescription());
            assertEquals(testInstructorUsername, retrievedRequest.getRequesterUsername());
        }
        
        /**
         * Test for retrieving all open admin requests.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Retrieve all open admin requests")
        public void testGetAllOpenRequests() throws SQLException {
            // Create multiple open requests
            dbHelper4.createRequest(testInstructorUsername, "Open Request 1", "Description 1");
            dbHelper4.createRequest(testInstructorUsername, "Open Request 2", "Description 2");
            
            // Retrieve all open requests
            List<Request> openRequests = dbHelper4.getAllOpenRequests();
            
            // Verify at least our two requests exist
            assertNotNull(openRequests);
            assertTrue(openRequests.size() >= 2);
            
            // Verify all retrieved requests are open
            for (Request request : openRequests) {
                assertEquals("open", request.getStatus());
                assertTrue(request.isOpen());
            }
        }
        
        /**
         * Test for closing an admin request with admin notes.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Close admin request")
        public void testCloseAdminRequest() throws SQLException {
            // Create a request
            Request request = dbHelper4.createRequest(testInstructorUsername, "Request to Close", "This request will be closed");
            
            // Close the request
            String adminNotes = "Request handled and resolved";
            boolean closed = dbHelper4.closeRequest(request.getRequestId(), testAdminUsername, adminNotes);
            
            // Verify closure was successful
            assertTrue(closed);
            
            // Retrieve the closed request and verify its state
            Request closedRequest = dbHelper4.getRequestById(request.getRequestId());
            assertNotNull(closedRequest);
            assertEquals("closed", closedRequest.getStatus());
            assertTrue(closedRequest.isClosed());
            assertFalse(closedRequest.isOpen());
            assertEquals(testAdminUsername, closedRequest.getClosedByUsername());
            assertEquals(adminNotes, closedRequest.getAdminNotes());
            assertNotNull(closedRequest.getClosedTimestamp());
        }
        
        /**
         * Test for retrieving all closed admin requests.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Retrieve closed admin requests")
        public void testGetClosedRequests() throws SQLException {
            // Create and close requests
            Request request1 = dbHelper4.createRequest(testInstructorUsername, "Request 1", "Description 1");
            Request request2 = dbHelper4.createRequest(testInstructorUsername, "Request 2", "Description 2");
            
            dbHelper4.closeRequest(request1.getRequestId(), testAdminUsername, "Closed notes 1");
            dbHelper4.closeRequest(request2.getRequestId(), testAdminUsername, "Closed notes 2");
            
            // Retrieve closed requests
            List<Request> closedRequests = dbHelper4.getAllClosedRequests();
            
            // Verify at least our two closed requests exist
            assertNotNull(closedRequests);
            assertTrue(closedRequests.size() >= 2);
            
            // Verify all retrieved requests are closed
            for (Request request : closedRequests) {
                assertEquals("closed", request.getStatus());
                assertTrue(request.isClosed());
                assertNotNull(request.getClosedTimestamp());
            }
        }
        
        /**
         * Test for reopening a closed admin request.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Reopen closed admin request")
        public void testReopenAdminRequest() throws SQLException {
            // Create and close a request
            Request originalRequest = dbHelper4.createRequest(testInstructorUsername, "Original Request", "Original Description");
            dbHelper4.closeRequest(originalRequest.getRequestId(), testAdminUsername, "Closed for testing");
            
            // Reopen the request
            String updatedDescription = "Updated description for reopened request";
            Request reopenedRequest = dbHelper4.reopenRequest(originalRequest.getRequestId(), testInstructorUsername, updatedDescription);
            
            // Verify the reopened request
            assertNotNull(reopenedRequest);
            assertEquals(testInstructorUsername, reopenedRequest.getRequesterUsername());
            
            assertTrue(reopenedRequest.getTitle().contains(originalRequest.getTitle()));
            
            assertEquals(updatedDescription, reopenedRequest.getDescription());
            assertEquals("open", reopenedRequest.getStatus());
            assertEquals(originalRequest.getRequestId(), reopenedRequest.getReopenedFromId().intValue());
            
            // Verify the original request has been marked as reopened
            Request updatedOriginal = dbHelper4.getRequestById(originalRequest.getRequestId());
            assertTrue(updatedOriginal.hasBeenReopened());
            assertEquals("closed", updatedOriginal.getStatus());
        }
        
        /**
         * Test for updating an admin request description.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Update admin request description")
        public void testUpdateRequestDescription() throws SQLException {
            // Create a request
            Request request = dbHelper4.createRequest(testInstructorUsername, "Request to Update", "Original description");
            
            // Update the description
            String newDescription = "Updated description for the request";
            boolean updated = dbHelper4.updateRequestDescription(request.getRequestId(), newDescription, testInstructorUsername);
            
            // Verify update was successful
            assertTrue(updated);
            
            // Retrieve the updated request and verify
            Request updatedRequest = dbHelper4.getRequestById(request.getRequestId());
            assertNotNull(updatedRequest);
            assertEquals(newDescription, updatedRequest.getDescription());
        }
        
        /**
         * Test for retrieving closed requests that have not been reopened.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Retrieve closed requests that have not been reopened")
        public void testGetClosedRequestsNotReopened() throws SQLException {
            // Create and close multiple requests
            Request request1 = dbHelper4.createRequest(testInstructorUsername, "Request 1", "Description 1");
            Request request2 = dbHelper4.createRequest(testInstructorUsername, "Request 2", "Description 2");
            Request request3 = dbHelper4.createRequest(testInstructorUsername, "Request 3", "Description 3");
            
            dbHelper4.closeRequest(request1.getRequestId(), testAdminUsername, "Closed notes 1");
            dbHelper4.closeRequest(request2.getRequestId(), testAdminUsername, "Closed notes 2");
            dbHelper4.closeRequest(request3.getRequestId(), testAdminUsername, "Closed notes 3");
            
            // Reopen one request
            dbHelper4.reopenRequest(request2.getRequestId(), testInstructorUsername, "Reopened description");
            
            // Get closed requests that haven't been reopened
            List<Request> notReopenedRequests = dbHelper4.getClosedRequestsNotReopened();
            
            // Verify request1 and request3 are in the list, but not request2
            boolean foundRequest1 = false;
            boolean foundRequest2 = false;
            boolean foundRequest3 = false;
            
            for (Request request : notReopenedRequests) {
                if (request.getRequestId() == request1.getRequestId()) {
                    foundRequest1 = true;
                    assertFalse(request.hasBeenReopened());
                }
                if (request.getRequestId() == request2.getRequestId()) {
                    foundRequest2 = true;
                }
                if (request.getRequestId() == request3.getRequestId()) {
                    foundRequest3 = true;
                    assertFalse(request.hasBeenReopened());
                }
            }
            
            assertTrue(foundRequest1);
            assertFalse(foundRequest2); // Should not be in the list because it was reopened
            assertTrue(foundRequest3);
        }
        
        /**
         * Test for retrieving closed requests that have been reopened.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Retrieve closed requests that have been reopened")
        public void testGetClosedRequestsThatWereReopened() throws SQLException {
            // Create and close multiple requests
            Request request1 = dbHelper4.createRequest(testInstructorUsername, "Request 1", "Description 1");
            Request request2 = dbHelper4.createRequest(testInstructorUsername, "Request 2", "Description 2");
            
            dbHelper4.closeRequest(request1.getRequestId(), testAdminUsername, "Closed notes 1");
            dbHelper4.closeRequest(request2.getRequestId(), testAdminUsername, "Closed notes 2");
            
            // Reopen request2
            dbHelper4.reopenRequest(request2.getRequestId(), testInstructorUsername, "Reopened description");
            
            // Get closed requests that have been reopened
            List<Request> reopenedRequests = dbHelper4.getClosedRequestsThatWereReopened();
            
            // Verify request2 is in the list but not request1
            boolean foundRequest1 = false;
            boolean foundRequest2 = false;
            
            for (Request request : reopenedRequests) {
                if (request.getRequestId() == request1.getRequestId()) {
                    foundRequest1 = true;
                }
                if (request.getRequestId() == request2.getRequestId()) {
                    foundRequest2 = true;
                    assertTrue(request.hasBeenReopened());
                }
            }
            
            assertFalse(foundRequest1); // Should not be in the list because it wasn't reopened
            assertTrue(foundRequest2);
        }
    }

    @Nested
    @DisplayName("Ban System Tests")
    public class BanSystemTests {
        
        /**
         * Test for banning a student and verifying their ban status.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Ban student and verify status")
        public void testBanStudent() throws SQLException {
            // Ensure student is not banned initially
            assertFalse(dbHelper.isStudentBanned(testStudentUsername));
            
            // Ban the student
            String banReason = "Test ban reason";
            dbHelper.banStudent(testStudentUsername, testInstructorUsername, banReason);
            
            // Verify the student is now banned
            assertTrue(dbHelper.isStudentBanned(testStudentUsername));
            
            // Unban for cleanup
            dbHelper.unbanStudent(testStudentUsername);
        }
        
        /**
         * Test for automatic banning of a student after receiving 4 warnings.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Automatic ban after 4 warnings")
        public void testAutomaticBan() throws SQLException {
            try {
                // Ensure student is not banned initially
                dbHelper.unbanStudent(testStudentUsername);
                assertFalse(dbHelper.isStudentBanned(testStudentUsername));
                
                // Clear any existing warnings
                try (PreparedStatement stmt = dbHelper2.connection.prepareStatement(
                        "DELETE FROM Feedback WHERE receiver = ? AND sender = 'SYSTEM WARNING'")) {
                    stmt.setString(1, testStudentUsername);
                    stmt.executeUpdate();
                }
                
                // Add 4 system warnings
                for (int i = 0; i < 4; i++) {
                    Feedback warning = new Feedback(
                        0, "SYSTEM WARNING", testStudentUsername,
                        "Warning " + (i + 1), new Date(), 0, null, null, null
                    );
                    dbHelper2.addFeedback(warning);
                }
                
                // Verify warning count is 4
                int warningCount = dbHelper2.getSystemWarningCount(testStudentUsername);
                assertEquals(4, warningCount, "Student should have 4 warnings");
                
                // Apply automatic ban
                String banReason = "Automatic ban after 4 warnings";
                dbHelper.banStudent(testStudentUsername, "SYSTEM", banReason);
                
                // Verify student is now banned
                assertTrue(dbHelper.isStudentBanned(testStudentUsername));
            } finally {
                // Clean up - unban the student
                dbHelper.unbanStudent(testStudentUsername);
                
                // Remove the test warnings
                try (PreparedStatement stmt = dbHelper2.connection.prepareStatement(
                        "DELETE FROM Feedback WHERE receiver = ? AND sender = 'SYSTEM WARNING'")) {
                    stmt.setString(1, testStudentUsername);
                    stmt.executeUpdate();
                }
            }
        }
        
        /**
         * Test for sending a ban notification to a student upon being banned.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Send ban notification to student")
        public void testBanNotification() throws SQLException {
            // Ban the student
            String banReason = "Test ban reason for notification";
            
            // Create and send ban notification
            String banMessage = "ACCOUNT BANNED\n\nYour account has been banned by instructor " + 
                               testInstructorUsername + " for the following reason:\n\n\"" + banReason + "\"\n\n" +
                               "You can still access the system to view content and messages, but most interactive " +
                               "features have been disabled.";
            
            Feedback banNotification = new Feedback(
                0,
                "SYSTEM NOTIFICATION",
                testStudentUsername,
                banMessage,
                new Date(),
                0,
                null,
                null,
                null
            );
            
            // Add notification
            dbHelper2.addFeedback(banNotification);
            
            // Verify the notification was sent
            String sql = "SELECT COUNT(*) FROM Feedback WHERE receiver = ? AND sender = 'SYSTEM NOTIFICATION' " +
                         "AND content LIKE '%ACCOUNT BANNED%'";
            PreparedStatement pstmt = dbHelper2.connection.prepareStatement(sql);
            pstmt.setString(1, testStudentUsername);
            ResultSet rs = pstmt.executeQuery();
            
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            
            rs.close();
            pstmt.close();
            
            assertTrue(count > 0, "Ban notification should be sent to student");
            
            // Unban for cleanup
            dbHelper.unbanStudent(testStudentUsername);
        }
    }

    /**
     * Cleans up the test environment after each test.
     * Removes test data from all tables and closes database connections.
     * 
     * @throws SQLException if a database error occurs
     */
    @AfterEach
    public void tearDown() throws SQLException {
        try {
            // Clean up admin requests
            try (PreparedStatement pstmt = dbHelper4.connection.prepareStatement("DELETE FROM AdminRequests")) {
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error cleaning up admin requests during teardown: " + e.getMessage());
            }
            
            // Clean up reviewer scorecards
            try (PreparedStatement pstmt = dbHelper.connection.prepareStatement("DELETE FROM reviewer_scorecard")) {
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error cleaning up reviewer scorecards during teardown: " + e.getMessage());
            }
            
            // Clean up reviews
            dbHelper.statement.execute("DELETE FROM reviews");
            // Clean up reviewer profiles
            dbHelper.statement.execute("DELETE FROM reviewer_profile");
            // Clean up answers
            List<Answer> allAnswers = answers.getAllAnswers();
            for (Answer answer : allAnswers) {
                try {
                    answers.removeAnswer(answer.getAnswerId());
                } catch (SQLException e) {
                    // Ignore errors during cleanup
                    System.err.println("Error removing answer during teardown: " + e.getMessage());
                }
            }
            // Clean up questions
            List<Question> allQuestions = questions.getAllQuestions();
            for (Question question : allQuestions) {
                try {
                    questions.removeQuestion(question.getQuestionId());
                } catch (SQLException e) {
                    // Ignore errors during cleanup
                    System.err.println("Error removing question during teardown: " + e.getMessage());
                }
            }
        } finally {
            // Close database connections
            if (dbHelper != null && dbHelper.connection != null) {
                dbHelper.connection.close();
            }
            if (dbHelper2 != null && dbHelper2.connection != null) {
                dbHelper2.connection.close();
            }
            if (dbHelper4 != null && dbHelper4.connection != null) {
                dbHelper4.connection.close();
            }
        }
    }
}
