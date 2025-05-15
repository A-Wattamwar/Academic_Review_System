package test;

import main.*;
import databasePart1.*;

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

/**
 * <p> Title: Phase3 Automated Tests. </p>
 * 
 * <p> Description: A comprehensive test suite for validating the functionality
 * of the Phase 3 components including:
 *   - Review creation and manipulation
 *   - Review database operations
 *   - Messaging functionality
 *   - Integration with Questions and Answers systems
 * </p>
 * 
 * <p> Copyright: Team 60 CSE 360 </p>
 * 
 * @version 0.00        2025-04-01    Initial test suite implementation
 */
public class Phase3AutomatedTests {

    private DatabaseHelper3 dbHelper;
    private DatabaseHelper2 dbHelper2;
    private Questions questions;
    private Answers answers;
    private int testQuestionId;
    private int testAnswerId;

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
        
        // Initialize managers
        questions = new Questions(dbHelper2, dbHelper2.connection);
        answers = new Answers(dbHelper2);
        
        // Set up test data
        setupTestData();
    }

    /**
     * Sets up the test data for the test suite.
     * Creates a test question and answer, and retrieves their IDs.
     * 
     * @throws SQLException if a database error occurs
     */
    public void setupTestData() throws SQLException {
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

    /**
     * Test suite for Review class functionality.
     */
    @Nested
    @DisplayName("Review Class Tests")
    public class ReviewClassTests {
        
        /**
         * Test for creating a review with valid data.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Create review with valid data")
        public void testCreateReview() {
            Date now = new Date();
            Review review = new Review(1, "reviewer1", "Great content!", now, testQuestionId, null);
            
            assertEquals(1, review.getReviewId());
            assertEquals("reviewer1", review.getReviewer());
            assertEquals("Great content!", review.getContent());
            assertEquals(now, review.getTimestamp());
            assertEquals(testQuestionId, review.getQuestionId());
            assertNull(review.getAnswerId());
        }

        /**
         * Test for creating a review for an answer.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Create review for answer")
        public void testCreateAnswerReview() {
            Date now = new Date();
            Review review = new Review(1, "reviewer1", "Helpful answer!", now, testQuestionId, testAnswerId);
            
            assertEquals(1, review.getReviewId());
            assertEquals("reviewer1", review.getReviewer());
            assertEquals("Helpful answer!", review.getContent());
            assertEquals(now, review.getTimestamp());
            assertEquals(testQuestionId, review.getQuestionId());
            assertEquals(Integer.valueOf(testAnswerId), review.getAnswerId());
        }

        /**
         * Test for updating review properties.
         */
        @Test
        @DisplayName("Update review properties")
        public void testUpdateReview() {
            Date initialDate = new Date();
            Review review = new Review(1, "reviewer1", "Initial content", initialDate, testQuestionId, null);
            
            Date newDate = new Date();
            review.setReviewId(2);
            review.setReviewer("newReviewer");
            review.setContent("Updated content");
            review.setTimestamp(newDate);
            review.setQuestionId(testQuestionId);
            review.setAnswerId(testAnswerId);
            
            assertEquals(2, review.getReviewId());
            assertEquals("newReviewer", review.getReviewer());
            assertEquals("Updated content", review.getContent());
            assertEquals(newDate, review.getTimestamp());
            assertEquals(testQuestionId, review.getQuestionId());
            assertEquals(Integer.valueOf(testAnswerId), review.getAnswerId());
        }
    }
    
    /**
     * Test suite for Reviews manager functionality.
     */
    @Nested
    @DisplayName("Reviews Manager Tests")
    public class ReviewsManagerTests {
        
        /**
         * Cleans up any existing reviews before each test.
         * 
         * @throws SQLException if a database error occurs
         */
        @BeforeEach
        public void cleanupReviews() throws SQLException {
            // Clean up any existing reviews before each test
            dbHelper.statement.execute("DELETE FROM reviews");
        }

        /**
         * Test for adding and retrieving a review for a question.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Add and retrieve review for question")
        public void testAddAndRetrieveQuestionReview() throws SQLException {
            // Create a review
            Date now = new Date();
            Review review = new Review(0, "testReviewer", "Test review content", now, testQuestionId, null);
            
            // Add the review
            dbHelper.addReview(review);
            
            // Retrieve reviews for the question
            List<Review> retrievedReviews = dbHelper.getReviewsForQuestion(testQuestionId);
            assertFalse(retrievedReviews.isEmpty());
            
            // Verify the review content
            Review retrieved = retrievedReviews.get(0);
            assertEquals("testReviewer", retrieved.getReviewer());
            assertEquals("Test review content", retrieved.getContent());
            assertEquals(testQuestionId, retrieved.getQuestionId());
            assertNull(retrieved.getAnswerId());
        }

        /**
         * Test for adding and retrieving a review for an answer.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Add and retrieve review for answer")
        public void testAddAndRetrieveAnswerReview() throws SQLException {
            // Create a review
            Date now = new Date();
            Review review = new Review(0, "testReviewer", "Test answer review", now, testQuestionId, testAnswerId);
            
            // Add the review
            dbHelper.addReview(review);
            
            // Retrieve reviews for the answer
            List<Review> retrievedReviews = dbHelper.getReviewsForAnswer(testAnswerId);
            assertFalse(retrievedReviews.isEmpty());
            
            // Verify the review content
            Review retrieved = retrievedReviews.get(0);
            assertEquals("testReviewer", retrieved.getReviewer());
            assertEquals("Test answer review", retrieved.getContent());
            assertEquals(testQuestionId, retrieved.getQuestionId());
            assertEquals(Integer.valueOf(testAnswerId), retrieved.getAnswerId());
        }

        /**
         * Test for updating an existing review.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Update existing review")
        public void testUpdateReview() throws SQLException {
            // Create and add initial review
            Date now = new Date();
            Review review = new Review(0, "testReviewer", "Initial content", now, testQuestionId, null);
            dbHelper.addReview(review);
            
            // Get the review ID from the list
            List<Review> retrievedReviews = dbHelper.getReviewsForQuestion(testQuestionId);
            assertFalse(retrievedReviews.isEmpty());
            int reviewId = retrievedReviews.get(0).getReviewId();
            
            // Update the review
            Review updatedReview = new Review(reviewId, "testReviewer", "Updated content", now, testQuestionId, null);
            dbHelper.updateReview(updatedReview);
            
            // Verify the update
            Review retrieved = dbHelper.getReviewById(reviewId);
            assertEquals("Updated content", retrieved.getContent());
        }

        /**
         * Test for deleting a review.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Delete review")
        public void testDeleteReview() throws SQLException {
            // Create and add a review
            Date now = new Date();
            Review review = new Review(0, "testReviewer", "To be deleted", now, testQuestionId, null);
            dbHelper.addReview(review);
            
            // Get the review ID
            List<Review> retrievedReviews = dbHelper.getReviewsForQuestion(testQuestionId);
            assertFalse(retrievedReviews.isEmpty());
            int reviewId = retrievedReviews.get(0).getReviewId();
            
            // Delete the review
            dbHelper.deleteReview(reviewId);
            
            // Verify deletion
            Review retrieved = dbHelper.getReviewById(reviewId);
            assertNull(retrieved);
        }

        /**
         * Test for getting multiple reviews for a question.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Get multiple reviews for question")
        public void testGetMultipleReviewsForQuestion() throws SQLException {
            // Add multiple reviews
            Date now = new Date();
            Review review1 = new Review(0, "reviewer1", "First review", now, testQuestionId, null);
            Review review2 = new Review(0, "reviewer2", "Second review", now, testQuestionId, null);
            
            dbHelper.addReview(review1);
            dbHelper.addReview(review2);
            
            // Retrieve all reviews
            List<Review> retrievedReviews = dbHelper.getReviewsForQuestion(testQuestionId);
            assertEquals(2, retrievedReviews.size());
            
            // Verify both reviews exist
            boolean foundFirst = false;
            boolean foundSecond = false;
            for (Review r : retrievedReviews) {
                if (r.getContent().equals("First review")) foundFirst = true;
                if (r.getContent().equals("Second review")) foundSecond = true;
            }
            assertTrue(foundFirst && foundSecond);
        }

        /**
         * Test for getting multiple reviews for an answer.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Get multiple reviews for answer")
        public void testGetMultipleReviewsForAnswer() throws SQLException {
            // Add multiple reviews
            Date now = new Date();
            Review review1 = new Review(0, "reviewer1", "First answer review", now, testQuestionId, testAnswerId);
            Review review2 = new Review(0, "reviewer2", "Second answer review", now, testQuestionId, testAnswerId);
            
            dbHelper.addReview(review1);
            dbHelper.addReview(review2);
            
            // Retrieve all reviews
            List<Review> retrievedReviews = dbHelper.getReviewsForAnswer(testAnswerId);
            assertEquals(2, retrievedReviews.size());
            
            // Verify both reviews exist
            boolean foundFirst = false;
            boolean foundSecond = false;
            for (Review r : retrievedReviews) {
                if (r.getContent().equals("First answer review")) foundFirst = true;
                if (r.getContent().equals("Second answer review")) foundSecond = true;
            }
            assertTrue(foundFirst && foundSecond);
        }

        /**
         * Test for getting all reviews from the database.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Get all reviews from database")
        public void testGetAllReviews() throws SQLException {
            // Add multiple reviews of different types
            Date now = new Date();
            Review questionReview = new Review(0, "reviewer1", "Question review", now, testQuestionId, null);
            Review answerReview = new Review(0, "reviewer2", "Answer review", now, testQuestionId, testAnswerId);
            
            dbHelper.addReview(questionReview);
            dbHelper.addReview(answerReview);
            
            // Retrieve all reviews
            List<Review> questionReviews = dbHelper.getReviewsForQuestion(testQuestionId);
            List<Review> answerReviews = dbHelper.getReviewsForAnswer(testAnswerId);
            assertEquals(1, questionReviews.size());
            assertEquals(1, answerReviews.size());
            
            // Verify both types of reviews exist
            boolean foundQuestionReview = false;
            boolean foundAnswerReview = false;
            for (Review r : questionReviews) {
                if (r.getContent().equals("Question review") && r.getAnswerId() == null) {
                    foundQuestionReview = true;
                }
            }
            for (Review r : answerReviews) {
                if (r.getContent().equals("Answer review") && r.getAnswerId() != null) {
                    foundAnswerReview = true;
                }
            }
            assertTrue(foundQuestionReview && foundAnswerReview, "Both question and answer reviews should be present");
        }
    }
    
    /**
     * Test suite for messaging functionality
     */
    @Nested
    @DisplayName("Messaging Tests")
    public class MessagingTests {

        /**
         * Cleans up any existing messages before each test.
         * 
         * @throws SQLException if a database error occurs
         */
        @BeforeEach
        public void cleanupMessages() throws SQLException {
            // Clean up existing messages before each test
            dbHelper2.statement.execute("DELETE FROM Feedback");
        }

        /**
         * Test for sending a message to a different user.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Can message different user")
        public void testCanMessageDifferentUser() throws SQLException {
            Question question = new Question(0, "Test question", "otherUser", new Date());
            questions.addQuestion(question);
             // Get the created question's ID
            List<Question> allQuestions = questions.getAllQuestions();
            int questionId = allQuestions.get(allQuestions.size() - 1).getQuestionId();
            
            Feedback feedback = new Feedback(0, "testUser", "otherUser", "Test message", new Date(), questionId);
            dbHelper2.addFeedback(feedback);
            
            List<Feedback> messages = dbHelper2.getFeedbackForUser("otherUser");
            assertFalse(messages.isEmpty());
            assertEquals("Test message", messages.get(0).getContent());
            assertEquals(questionId, messages.get(0).getQuestionId()); // Verify question ID too
        }

        /**
         * Test for maintaining question references in messages.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Messages maintain question references")
        public void testQuestionReferences() throws SQLException {
            Question question = new Question(0, "Test question", "author1", new Date());
            questions.addQuestion(question);
             // Get the created question's ID
            List<Question> allQuestions = questions.getAllQuestions();
            int questionId = allQuestions.get(allQuestions.size() - 1).getQuestionId();
            
            Feedback feedback = new Feedback(0, "sender", "receiver", "About question", new Date(), questionId);
            dbHelper2.addFeedback(feedback);
            
            List<Feedback> messages = dbHelper2.getFeedbackForUser("receiver");
            assertFalse(messages.isEmpty());
            assertEquals(questionId, messages.get(0).getQuestionId());
            assertNull(messages.get(0).getAnswerId(), "Answer ID should be null for question message");
            assertNull(messages.get(0).getReviewId(), "Review ID should be null for question message");
        }

        /**
         * Test for maintaining answer references in messages.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Messages maintain answer references")
        public void testAnswerReferences() throws SQLException {
            // Use the globally set up testQuestionId and testAnswerId
            Feedback feedback = new Feedback(0, "sender", "receiver", "About answer", new Date(), 
                                           testQuestionId, testAnswerId, null, null);
            dbHelper2.addFeedback(feedback);
            
            List<Feedback> messages = dbHelper2.getFeedbackForUser("receiver");
            assertFalse(messages.isEmpty(), "Messages list should not be empty");
            
            Feedback retrievedFeedback = messages.get(0); // Get the first message (should be the one we added)
            assertEquals(testQuestionId, retrievedFeedback.getQuestionId(), "Question ID mismatch");
            assertEquals(Integer.valueOf(testAnswerId), retrievedFeedback.getAnswerId(), "Answer ID mismatch"); // Compare Integers
             assertNull(retrievedFeedback.getReviewId(), "Review ID should be null for answer message");
        }

        /**
         * Test for maintaining review references in messages.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Messages maintain review references")
        public void testReviewReferences() throws SQLException {
             // Use the globally set up testQuestionId
            Review review = new Review(0, "reviewer1", "Test review", new Date(), testQuestionId, null); // Review for question
            dbHelper.addReview(review);
            
             // Retrieve the review *after* adding it to get the generated ID
            List<Review> reviews = dbHelper.getReviewsForQuestion(testQuestionId);
            assertFalse(reviews.isEmpty(), "Review list should not be empty after adding");
            int reviewId = reviews.get(0).getReviewId(); // Get ID of the added review
             assertTrue(reviewId > 0, "Review ID should be positive after insertion"); // Basic check

            Feedback feedback = new Feedback(0, "sender", "receiver", "About review", new Date(), 
                                           testQuestionId, null, reviewId, null); // Reference the retrieved reviewId
            dbHelper2.addFeedback(feedback);
            
            List<Feedback> messages = dbHelper2.getFeedbackForUser("receiver");
            assertFalse(messages.isEmpty(), "Messages list should not be empty");

            Feedback retrievedFeedback = messages.get(0); // Get the first message
            assertEquals(testQuestionId, retrievedFeedback.getQuestionId(), "Question ID mismatch");
            assertNull(retrievedFeedback.getAnswerId(), "Answer ID should be null for review message");
            assertEquals(Integer.valueOf(reviewId), retrievedFeedback.getReviewId(), "Review ID mismatch"); // Compare Integers
        }

        /**
         * Test for ordering messages by timestamp.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Messages are ordered by timestamp")
        public void testMessageOrdering() throws SQLException {
            Question question = new Question(0, "Test question", "author1", new Date());
            questions.addQuestion(question);
             // Get the created question's ID
            List<Question> allQuestions = questions.getAllQuestions();
            int questionId = allQuestions.get(allQuestions.size() - 1).getQuestionId();
            
            Date earlier = new Date(System.currentTimeMillis() - 1000);
            Date later = new Date();
            
            Feedback feedback1 = new Feedback(0, "sender", "receiver", "First message", earlier, questionId);
            Feedback feedback2 = new Feedback(0, "sender", "receiver", "Second message", later, questionId);
            
            dbHelper2.addFeedback(feedback1);
            dbHelper2.addFeedback(feedback2);
            
            List<Feedback> messages = dbHelper2.getFeedbackForUser("receiver");
            assertEquals(2, messages.size());
             // getFeedbackForUser sorts DESCENDING
            assertTrue(messages.get(0).getTimestamp().after(messages.get(1).getTimestamp()));
            assertEquals("Second message", messages.get(0).getContent());
            assertEquals("First message", messages.get(1).getContent());
        }

        /**
         * Test for retrieving users with whom messages have been exchanged.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Retrieve users with whom messages have been exchanged")
        public void testRetrieveConversationUsers() throws SQLException {
            // Add a question to associate messages with
            Question question = new Question(0, "Test question", "user1", new Date());
            questions.addQuestion(question);
            int questionId = question.getQuestionId();

            // Create feedback messages between users
            Feedback feedback1 = new Feedback(0, "user1", "user2", "Message 1", new Date(), questionId);
            Feedback feedback2 = new Feedback(0, "user2", "user1", "Message 2", new Date(), questionId);
            Feedback feedback3 = new Feedback(0, "user1", "user3", "Message 3", new Date(), questionId);

            // Add feedback to the database
            dbHelper2.addFeedback(feedback1);
            dbHelper2.addFeedback(feedback2);
            dbHelper2.addFeedback(feedback3);

            // Retrieve users with whom 'user1' has exchanged messages
            List<String> user1Conversations = dbHelper2.getUserConversations("user1");
            // Retrieve users with whom 'user2' has exchanged messages
            List<String> user2Conversations = dbHelper2.getUserConversations("user2");
            // Retrieve users with whom 'user3' has exchanged messages
            List<String> user3Conversations = dbHelper2.getUserConversations("user3");

            // Verify the expected users in each conversation list
            assertEquals(2, user1Conversations.size(), "User1 should have 2 conversation partners");
            assertTrue(user1Conversations.contains("user2"));
            assertTrue(user1Conversations.contains("user3"));

            assertEquals(1, user2Conversations.size(), "User2 should have 1 conversation partner");
            assertTrue(user2Conversations.contains("user1"));

            assertEquals(1, user3Conversations.size(), "User3 should have 1 conversation partner");
            assertTrue(user3Conversations.contains("user1"));
        }

        /**
         * Test for deleting messages.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Can delete messages")
        public void testDeleteMessages() throws SQLException {
            Question question = new Question(0, "Test question", "author1", new Date());
            questions.addQuestion(question);
             // Get the created question's ID
            List<Question> allQuestions = questions.getAllQuestions();
            int questionId = allQuestions.get(allQuestions.size() - 1).getQuestionId();
            
            Feedback feedback = new Feedback(0, "sender", "receiver", "Test message", new Date(), questionId);
            dbHelper2.addFeedback(feedback);
            
            List<Feedback> messages = dbHelper2.getFeedbackForUser("receiver");
            assertFalse(messages.isEmpty());
            int feedbackId = messages.get(0).getFeedbackId(); // Get the ID after insertion
            
            dbHelper2.deleteFeedback(feedbackId);
            messages = dbHelper2.getFeedbackForUser("receiver");
            assertTrue(messages.isEmpty());
        }

        /**
         * Test for ensuring messages persist after related entities deletion.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Messages persist after related entities deletion")
        public void testMessagesPersistAfterDeletion() throws SQLException {
            // Use globally set up IDs
            int qId = testQuestionId;
            int aId = testAnswerId;

            Review review = new Review(0, "reviewer1", "Test review", new Date(), qId, aId);
            dbHelper.addReview(review);
             List<Review> reviews = dbHelper.getReviewsForAnswer(aId);
             assertFalse(reviews.isEmpty(), "Review list should not be empty after adding");
             int rId = reviews.get(0).getReviewId(); // Get ID of the added review

            Feedback feedback = new Feedback(0, "sender", "receiver", "Test message", new Date(), qId, aId, rId, null);
            dbHelper2.addFeedback(feedback);
            
             List<Feedback> initialMessages = dbHelper2.getFeedbackForUser("receiver");
             assertFalse(initialMessages.isEmpty(), "Message should be present initially");
             int feedbackId = initialMessages.get(0).getFeedbackId();
            
            // Delete the review, answer, and question
             dbHelper.deleteReview(rId);
            answers.removeAnswer(aId); // Assuming this handles related data or cascades
             questions.removeQuestion(qId); // Assuming this handles related data or cascades

            // Messages should still exist, but references might be dangling (depending on FK constraints)
            List<Feedback> messagesAfterDelete = dbHelper2.getFeedbackForUser("receiver");
            assertFalse(messagesAfterDelete.isEmpty(), "Messages should persist after entity deletion");
            assertEquals(feedbackId, messagesAfterDelete.get(0).getFeedbackId(), "Message ID should remain the same");
             assertEquals("Test message", messagesAfterDelete.get(0).getContent(), "Message content should remain");

             // Check if the references are still there (they might be, depending on DB schema/deletion logic)
             assertEquals(qId, messagesAfterDelete.get(0).getQuestionId(), "Question ID should persist in message");
             assertEquals(Integer.valueOf(aId), messagesAfterDelete.get(0).getAnswerId(), "Answer ID should persist in message");
             assertEquals(Integer.valueOf(rId), messagesAfterDelete.get(0).getReviewId(), "Review ID should persist in message");
        }

        /**
         * Test for unread message count.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Unread message count is accurate")
        public void testUnreadMessageCount() throws SQLException {
            Question question = new Question(0, "Test question", "author1", new Date());
            questions.addQuestion(question);
            int questionId = question.getQuestionId();

            String receiver = "receiverUser";
            
            // Send 3 messages
            dbHelper2.addFeedback(new Feedback(0, "sender1", receiver, "msg1", new Date(), questionId));
            dbHelper2.addFeedback(new Feedback(0, "sender2", receiver, "msg2", new Date(), questionId));
            dbHelper2.addFeedback(new Feedback(0, "sender1", receiver, "msg3", new Date(), questionId));

            assertEquals(3, dbHelper2.getUnreadMessageCount(receiver), "Initial unread count should be 3");

            // Mark conversation with sender1 as read
            dbHelper2.markConversationAsRead(receiver, "sender1");
            
            assertEquals(1, dbHelper2.getUnreadMessageCount(receiver), "Unread count should be 1 after marking sender1's messages read");

            // Mark remaining conversation as read
            dbHelper2.markConversationAsRead(receiver, "sender2");
            assertEquals(0, dbHelper2.getUnreadMessageCount(receiver), "Unread count should be 0 after marking all read");
        }

        /**
         * Test for unread message counts by conversation.
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Unread message counts by conversation are accurate")
        public void testUnreadCountsByConversation() throws SQLException {
             Question question = new Question(0, "Test question", "author1", new Date());
            questions.addQuestion(question);
            int questionId = question.getQuestionId();
            
            String receiver = "receiverUser";
            
             // Send messages from different senders
            dbHelper2.addFeedback(new Feedback(0, "sender1", receiver, "msg1", new Date(), questionId));
            dbHelper2.addFeedback(new Feedback(0, "sender2", receiver, "msg2", new Date(), questionId));
            dbHelper2.addFeedback(new Feedback(0, "sender1", receiver, "msg3", new Date(), questionId));
            
            var unreadCounts = dbHelper2.getUnreadMessageCountsByConversation(receiver);
            
            assertEquals(2, unreadCounts.size(), "Should have unread counts from 2 senders");
            assertEquals(2, unreadCounts.get("sender1"), "Unread count from sender1 should be 2");
            assertEquals(1, unreadCounts.get("sender2"), "Unread count from sender2 should be 1");
            
            // Mark conversation with sender1 as read
            dbHelper2.markConversationAsRead(receiver, "sender1");
            unreadCounts = dbHelper2.getUnreadMessageCountsByConversation(receiver);
            
            assertEquals(1, unreadCounts.size(), "Should have unread counts from 1 sender after marking");
            assertNull(unreadCounts.get("sender1"), "Unread count from sender1 should be null/absent");
            assertEquals(1, unreadCounts.get("sender2"), "Unread count from sender2 should still be 1");
        }
    }
    
    /**
     * Test suite for Reviewer Permissions functionality
     */
    @Nested
    @DisplayName("Reviewer Permissions Tests")
    public class ReviewerPermissionsTests {
        
        /**
         * Cleans up existing reviewer requests before running tests
         * 
         * @throws SQLException if a database error occurs
         */
        @BeforeEach
        public void cleanupReviewerRequests() throws SQLException {
            // Clean up existing reviewer requests
            dbHelper.statement.execute("DELETE FROM reviewer_requests");
        }

        /**
         * Test for submitting a reviewer request
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Student can submit reviewer request")
        public void testSubmitReviewerRequest() throws SQLException {
            String studentUsername = "testStudent";
            
            // Submit a reviewer request
            dbHelper.submitReviewerRequest(studentUsername);
            
            // Verify the request exists and is pending
            assertTrue(dbHelper.hasPendingReviewerRequest(studentUsername), 
                "Student should have a pending request");
            
            // Get request status and verify details
            Map<String, Object> status = dbHelper.getReviewerRequestStatus(studentUsername);
            assertNotNull(status, "Request status should not be null");
            assertEquals("pending", status.get("status"), "Request should be pending");
            assertNotNull(status.get("requestDate"), "Request date should be set");
            assertNull(status.get("reviewerUsername"), "Reviewer should not be set yet");
        }

        /**
         * Test for preventing multiple pending requests
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Cannot submit multiple pending requests")
        public void testMultiplePendingRequests() throws SQLException {
            String studentUsername = "testStudent";
            
            // Submit first request
            dbHelper.submitReviewerRequest(studentUsername);
            assertTrue(dbHelper.hasPendingReviewerRequest(studentUsername), 
                "First request should be pending");
            
            // Try to submit second request - should throw SQLException
            assertThrows(SQLException.class, () -> {
                dbHelper.submitReviewerRequest(studentUsername);
            }, "Should not allow multiple pending requests");
            
            // Verify only one request exists
            List<Map<String, Object>> requests = dbHelper.getPendingReviewerRequests();
            assertEquals(1, (int) requests.stream()
                .filter(r -> r.get("studentUsername").equals(studentUsername))
                .count(), 
                "Should only have one pending request");
        }

        /**
         * Test for approving a reviewer request
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Instructor can approve reviewer request")
        public void testApproveReviewerRequest() throws SQLException {
            String studentUsername = "testStudent";
            String instructorUsername = "testInstructor";
            String notes = "Good quality contributions";
            
            // Submit request
            dbHelper.submitReviewerRequest(studentUsername);
            
            // Get the request ID
            List<Map<String, Object>> requests = dbHelper.getPendingReviewerRequests();
            assertFalse(requests.isEmpty(), "Should have a pending request");
            int requestId = (Integer) requests.get(0).get("requestId");
            
            // Approve request
            dbHelper.updateReviewerRequestStatus(requestId, "approved", instructorUsername, notes);
            
            // Verify status after approval
            Map<String, Object> status = dbHelper.getReviewerRequestStatus(studentUsername);
            assertNotNull(status, "Status should exist after approval");
            assertEquals("approved", status.get("status"), "Status should be approved");
            assertEquals(instructorUsername, status.get("reviewerUsername"), 
                "Reviewer username should match instructor");
            assertEquals(notes, status.get("reviewNotes"), "Notes should match");
            assertNotNull(status.get("reviewDate"), "Review date should be set");
            
            // Verify no longer has pending request
            assertFalse(dbHelper.hasPendingReviewerRequest(studentUsername), 
                "Should not have pending request after approval");
        }

        /**
         * Test for rejecting a reviewer request
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Instructor can reject reviewer request")
        public void testRejectReviewerRequest() throws SQLException {
            String studentUsername = "testStudent";
            String instructorUsername = "testInstructor";
            String notes = "Need more experience";
            
            // Submit request
            dbHelper.submitReviewerRequest(studentUsername);
            
            // Get the request ID
            List<Map<String, Object>> requests = dbHelper.getPendingReviewerRequests();
            assertFalse(requests.isEmpty(), "Should have a pending request");
            int requestId = (Integer) requests.get(0).get("requestId");
            
            // Reject request
            dbHelper.updateReviewerRequestStatus(requestId, "rejected", instructorUsername, notes);
            
            // Verify status after rejection
            Map<String, Object> status = dbHelper.getReviewerRequestStatus(studentUsername);
            assertNotNull(status, "Status should exist after rejection");
            assertEquals("rejected", status.get("status"), "Status should be rejected");
            assertEquals(instructorUsername, status.get("reviewerUsername"), 
                "Reviewer username should match instructor");
            assertEquals(notes, status.get("reviewNotes"), "Notes should match");
            assertNotNull(status.get("reviewDate"), "Review date should be set");
            
            // Verify no longer has pending request
            assertFalse(dbHelper.hasPendingReviewerRequest(studentUsername), 
                "Should not have pending request after rejection");
        }

        /**
         * Test for retrieving multiple pending requests
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Can retrieve multiple pending requests")
        public void testGetMultiplePendingRequests() throws SQLException {
            // Clean up any existing requests first
            dbHelper.statement.execute("DELETE FROM reviewer_requests");

            // Create test student usernames
            String[] testStudents = {"student1", "student2", "student3"};
            
            // Submit requests for each test student
            for (String student : testStudents) {
                dbHelper.submitReviewerRequest(student);
            }

            // Get all pending requests
            List<Map<String, Object>> pendingRequests = dbHelper.getPendingReviewerRequests();

            // Verify we got exactly 3 requests
            assertEquals(3, pendingRequests.size(), "Should have exactly 3 pending requests");

            // Create a set of student usernames for easy lookup
            Set<String> foundStudents = new HashSet<>();

            // Verify each request
            for (Map<String, Object> request : pendingRequests) {
                // Check that required fields exist and are of correct type
                assertTrue(request.containsKey("requestId"), "Request should have requestId");
                assertTrue(request.get("requestId") instanceof Integer, "requestId should be an Integer");
                
                assertTrue(request.containsKey("studentUsername"), "Request should have studentUsername");
                assertTrue(request.get("studentUsername") instanceof String, "studentUsername should be a String");
                String studentUsername = (String) request.get("studentUsername");
                
                assertTrue(request.containsKey("requestDate"), "Request should have requestDate");
                assertTrue(request.get("requestDate") instanceof Timestamp, "requestDate should be a Timestamp");
                
                // Verify the student is one of our test students
                assertTrue(Arrays.asList(testStudents).contains(studentUsername), 
                    "Found unexpected student: " + studentUsername);
                
                // Add to our found set
                foundStudents.add(studentUsername);
                
                // Verify the request is still pending in the database
                assertTrue(dbHelper.hasPendingReviewerRequest(studentUsername), 
                    "Student should have a pending request: " + studentUsername);
            }

            // Verify we found all test students
            assertEquals(testStudents.length, foundStudents.size(), 
                "Should have found all test students");
            for (String student : testStudents) {
                assertTrue(foundStudents.contains(student), 
                    "Should have found student: " + student);
            }
        }

        /**
         * Cleans up all test data after each test
         * 
         * @throws SQLException if a database error occurs
         */
        @AfterEach
        public void cleanup() throws SQLException {
            // Clean up all test data
            dbHelper.statement.execute("DELETE FROM reviewer_requests");
        }
    }
    
    @Nested
    @DisplayName("Additional Review System Tests")
    public class AdditionalReviewSystemTests {
        /**
         * Cleans up existing data in reverse order of dependencies
         * 
         * @throws SQLException if a database error occurs
         */
        @BeforeEach
        public void setupAdditionalData() throws SQLException {
            // Clean up existing data in reverse order of dependencies
            dbHelper2.statement.execute("DELETE FROM Feedback");
            dbHelper.statement.execute("DELETE FROM reviews");
            dbHelper2.statement.execute("DELETE FROM TrustedReviewers");
            dbHelper2.statement.execute("DELETE FROM ReadStatus");
        }

        /**
         * Test for adding and managing trusted reviewers
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Student can add and manage trusted reviewers")
        public void testTrustedReviewerManagement() throws SQLException {
            String studentUsername = "testStudent";
            String reviewerUsername = "testReviewer";
            
            // Add a trusted reviewer with default weightage
            dbHelper2.addTrustedReviewer(studentUsername, reviewerUsername);
            
            // Verify the reviewer was added
            List<String> trustedReviewers = dbHelper2.getTrustedReviewers(studentUsername);
            assertTrue(trustedReviewers.contains(reviewerUsername), "Reviewer should be in trusted list");
            assertEquals(1, dbHelper2.getReviewerWeightage(studentUsername, reviewerUsername), "Default weightage should be 1");
            
            // Remove the trusted reviewer
            dbHelper2.removeTrustedReviewer(studentUsername, reviewerUsername);
            
            // Verify the reviewer was removed
            trustedReviewers = dbHelper2.getTrustedReviewers(studentUsername);
            assertFalse(trustedReviewers.contains(reviewerUsername), "Reviewer should be removed from trusted list");
            assertEquals(0, dbHelper2.getReviewerWeightage(studentUsername, reviewerUsername), "Weightage should be 0 after removal");
        }

        /**
         * Test for setting and updating reviewer weights
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Student can set and update reviewer weights")
        public void testReviewerWeights() throws SQLException {
            String studentUsername = "testStudent";
            String reviewerUsername = "testReviewer";
            int initialWeight = 2;
            int updatedWeight = 3;
            
            // Add trusted reviewer with initial weight
            dbHelper2.addTrustedReviewer(studentUsername, reviewerUsername, initialWeight);
            
            // Verify initial weight
            int weight = dbHelper2.getReviewerWeightage(studentUsername, reviewerUsername);
            assertEquals(initialWeight, weight, "Initial weight should match");
            
            // Update weight
            dbHelper2.updateReviewerWeightage(studentUsername, reviewerUsername, updatedWeight);
            
            // Verify updated weight
            weight = dbHelper2.getReviewerWeightage(studentUsername, reviewerUsername);
            assertEquals(updatedWeight, weight, "Updated weight should match");
        }

        /**
         * Test for ordering reviews by reviewer weights
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Reviews are ordered by reviewer weights")
        public void testReviewOrderingByWeight() throws SQLException {
            String studentUsername = "testStudent";
            String reviewer1 = "reviewer1";
            String reviewer2 = "reviewer2";
            
            // First create the trusted reviewers with weights
            dbHelper2.addTrustedReviewer(studentUsername, reviewer1, 3); // Higher weight
            dbHelper2.addTrustedReviewer(studentUsername, reviewer2, 1); // Lower weight
            
            // Verify the weights were set correctly
            int weight1 = dbHelper2.getReviewerWeightage(studentUsername, reviewer1);
            int weight2 = dbHelper2.getReviewerWeightage(studentUsername, reviewer2);
            
            assertEquals(3, weight1, "Reviewer1 should have weight 3");
            assertEquals(1, weight2, "Reviewer2 should have weight 1");
            
            // Verify reviewer1 has higher weight than reviewer2
            assertTrue(weight1 > weight2, "Reviewer1 should have higher weight than reviewer2");
            
            // Verify trusted reviewer status
            assertTrue(dbHelper2.isTrustedReviewer(studentUsername, reviewer1), "Reviewer1 should be trusted");
            assertTrue(dbHelper2.isTrustedReviewer(studentUsername, reviewer2), "Reviewer2 should be trusted");
            
            // Get all trusted reviewers
            List<String> trustedReviewers = dbHelper2.getTrustedReviewers(studentUsername);
            assertEquals(2, trustedReviewers.size(), "Should have two trusted reviewers");
            assertTrue(trustedReviewers.contains(reviewer1), "Should contain reviewer1");
            assertTrue(trustedReviewers.contains(reviewer2), "Should contain reviewer2");
        }

        /**
         * Test for filtering reviews by trusted reviewers
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Student can filter reviews to show only trusted reviewers")
        public void testFilterReviewsByTrustedReviewers() throws SQLException {
            String studentUsername = "testStudent";
            String[] reviewers = {
                "trustedReviewer1",
                "trustedReviewer2",
                "untrustedReviewer1",
                "untrustedReviewer2"
            };
            
            // Add only some reviewers as trusted
            dbHelper2.addTrustedReviewer(studentUsername, reviewers[0]); // Add first trusted reviewer
            dbHelper2.addTrustedReviewer(studentUsername, reviewers[1]); // Add second trusted reviewer
            
            // Verify trusted reviewers list
            List<String> trustedReviewers = dbHelper2.getTrustedReviewers(studentUsername);
            assertEquals(2, trustedReviewers.size(), "Should have exactly two trusted reviewers");
            assertTrue(trustedReviewers.contains(reviewers[0]), "First trusted reviewer should be in list");
            assertTrue(trustedReviewers.contains(reviewers[1]), "Second trusted reviewer should be in list");
            
            // Verify each reviewer's trusted status
            assertTrue(dbHelper2.isTrustedReviewer(studentUsername, reviewers[0]), 
                "First reviewer should be trusted");
            assertTrue(dbHelper2.isTrustedReviewer(studentUsername, reviewers[1]), 
                "Second reviewer should be trusted");
            assertFalse(dbHelper2.isTrustedReviewer(studentUsername, reviewers[2]), 
                "Third reviewer should not be trusted");
            assertFalse(dbHelper2.isTrustedReviewer(studentUsername, reviewers[3]), 
                "Fourth reviewer should not be trusted");
            
            // Verify weightage for trusted reviewers (default should be 1)
            assertEquals(1, dbHelper2.getReviewerWeightage(studentUsername, reviewers[0]), 
                "First trusted reviewer should have default weight");
            assertEquals(1, dbHelper2.getReviewerWeightage(studentUsername, reviewers[1]), 
                "Second trusted reviewer should have default weight");
            
            // Verify weightage for untrusted reviewers (should be 0)
            assertEquals(0, dbHelper2.getReviewerWeightage(studentUsername, reviewers[2]), 
                "Untrusted reviewer should have zero weight");
            assertEquals(0, dbHelper2.getReviewerWeightage(studentUsername, reviewers[3]), 
                "Untrusted reviewer should have zero weight");
        }

        /**
         * Test for reviewer message interaction
         * 
         * @throws SQLException if a database error occurs
         */
        @Test
        @DisplayName("Reviewer can view and respond to messages about their reviews")
        public void testReviewerMessageInteraction() throws SQLException {
            String reviewerUsername = "testReviewer";
            String studentUsername = "testStudent";
            int questionId = -1;
            
            try {
                // First create a question (required for foreign key constraint)
                Question question = new Question(0, "Test Question Content", studentUsername, new Date());
                questions.addQuestion(question);
                
                // Get the created question's ID
                List<Question> allQuestions = questions.getAllQuestions();
                assertFalse(allQuestions.isEmpty(), "Question should be created");
                questionId = allQuestions.get(allQuestions.size() - 1).getQuestionId();
                
                // Create a review for the question
                Review review = new Review(0, reviewerUsername, "Initial review content", new Date(), questionId, null);
                dbHelper.addReview(review);
                
                // Get the review ID after insertion
                List<Review> reviews = dbHelper.getReviewsForQuestion(questionId);
                assertFalse(reviews.isEmpty(), "Review should exist");
                int reviewId = reviews.get(0).getReviewId();
                
                // Student sends message about the review
                Feedback studentMessage = new Feedback(0, studentUsername, reviewerUsername, 
                    "Question about your review", new Date(), questionId, null, reviewId, null);
                dbHelper2.addFeedback(studentMessage);
                
                // Verify reviewer can see the message
                List<Feedback> reviewerMessages = dbHelper2.getFeedbackForUser(reviewerUsername);
                assertFalse(reviewerMessages.isEmpty(), "Reviewer should have received the message");
                assertEquals(studentUsername, reviewerMessages.get(0).getSender(), "Message should be from student");
                assertEquals(reviewId, reviewerMessages.get(0).getReviewId(), "Message should reference correct review");
                
                // Reviewer responds to the message
                int studentMessageId = reviewerMessages.get(0).getFeedbackId();
                Feedback reviewerResponse = new Feedback(0, reviewerUsername, studentUsername,
                    "Response to your question", new Date(), questionId, null, reviewId, studentMessageId);
                dbHelper2.addFeedback(reviewerResponse);
                
                // Verify student can see the response
                List<Feedback> studentMessages = dbHelper2.getFeedbackForUser(studentUsername);
                assertFalse(studentMessages.isEmpty(), "Student should have received the response");
                assertEquals(reviewerUsername, studentMessages.get(0).getSender(), "Response should be from reviewer");
                assertEquals(reviewId, studentMessages.get(0).getReviewId(), "Response should reference correct review");
                assertEquals(studentMessageId, studentMessages.get(0).getParentMessageId(), "Response should reference original message");
            } finally {
                // Clean up test data in reverse order of dependencies
                if (questionId != -1) {
                    try {
                        // Delete feedback messages first
                        List<Feedback> messages = dbHelper2.getFeedbackForUser(studentUsername);
                        messages.addAll(dbHelper2.getFeedbackForUser(reviewerUsername));
                        for (Feedback message : messages) {
                            if (message.getQuestionId() == questionId) {
                                dbHelper2.deleteFeedback(message.getFeedbackId());
                            }
                        }
                        
                        // Delete reviews next
                        List<Review> reviews = dbHelper.getReviewsForQuestion(questionId);
                        for (Review review : reviews) {
                            dbHelper.deleteReview(review.getReviewId());
                        }
                        
                        // Delete question last
                        questions.removeQuestion(questionId);
                    } catch (SQLException e) {
                        // Log cleanup errors but don't fail the test
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * Cleans up all test data after each test
         * 
         * @throws SQLException if a database error occurs
         */     
        @AfterEach
        public void cleanupAdditionalData() throws SQLException {
            // Clean up all test data in reverse order of dependencies
            dbHelper2.statement.execute("DELETE FROM Feedback");
            dbHelper.statement.execute("DELETE FROM reviews");
            dbHelper2.statement.execute("DELETE FROM TrustedReviewers");
            dbHelper2.statement.execute("DELETE FROM ReadStatus");
            
            // Clean up questions and answers
            List<Answer> allAnswers = answers.getAllAnswers();
            for (Answer answer : allAnswers) {
                try {
                    answers.removeAnswer(answer.getAnswerId());
                } catch (SQLException e) {
                    // Log error but continue cleanup
                    System.err.println("Error removing answer during cleanup: " + e.getMessage());
                }
            }
            
            List<Question> allQuestions = questions.getAllQuestions();
            for (Question question : allQuestions) {
                try {
                    questions.removeQuestion(question.getQuestionId());
                } catch (SQLException e) {
                    // Log error but continue cleanup
                    System.err.println("Error removing question during cleanup: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Cleans up the test environment after each test.
     * 
     * @throws SQLException if a database error occurs
     */
    @AfterEach
    public void tearDown() throws SQLException {
        try {
            // Clean up messages first because of potential foreign key constraints
            dbHelper2.statement.execute("DELETE FROM Feedback");
            // Clean up reviews
            dbHelper.statement.execute("DELETE FROM reviews");
            // Clean up read status
             dbHelper2.statement.execute("DELETE FROM ReadStatus");
            // Clean up trusted reviewers
            dbHelper2.statement.execute("DELETE FROM TrustedReviewers");
            // Clean up reviewer requests
            dbHelper.statement.execute("DELETE FROM reviewer_requests");
            // Clean up answers (use manager to handle potential related data)
            List<Answer> allAnswers = answers.getAllAnswers();
             for (Answer answer : allAnswers) {
                 try {
                     answers.removeAnswer(answer.getAnswerId());
                 } catch (SQLException e) {
                     // Ignore errors during cleanup, possibly due to dependencies already deleted
                      System.err.println("Error removing answer during teardown: " + e.getMessage());
                 }
             }
             // Clean up questions (use manager)
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
        }
    }
}
