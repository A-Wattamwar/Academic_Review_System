package test;

import main.*;
import databasePart1.*;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Date;

/**
 * Phase2AutomatedTests.java
 *
 * This class contains test suites for validating the Question and Answer system functionality including:
 *   - Question creation and management
 *   - Answer creation and management
 *   - Feedback system
 *   - Question and Answer database operations
 *
 * <p> Copyright: Team 60 CSE 360 </p>
 *
 * @version 1.00    2025-04-01    Tests for Question and Answer system functionality
 */
public class Phase2AutomatedTests {

    private DatabaseHelper2 dbHelper;
    private Questions questions;
    private Answers answers;

    @BeforeEach
    public void setUp() throws SQLException {
        dbHelper = new DatabaseHelper2();
        dbHelper.connectToDatabase();
        questions = new Questions(dbHelper, dbHelper.connection);
        answers = new Answers(dbHelper);
    }

    /**
     * Test suite for Question class functionality.
     */
    @Nested
    @DisplayName("Question Tests")
    public class QuestionTests {
        
        @Test
        @DisplayName("Correct question should be valid")
        public void testQuestionValidation() {
            Question question = new Question(1, "How do I use Java streams?", "testUser", new Date());
            assertTrue(question.validate());
        }

        @Test
        @DisplayName("Empty question should be invalid")
        public void testQuestionValidationEmptyContent() {
            Question question = new Question(1, "", "testUser", new Date());
            assertFalse(question.validate());
        }

        @Test
        @DisplayName("Question status should track resolved/unresolved state")
        public void testMarkQuestionAsAnswered() {
            Question question = new Question(1, "Content", "testUser", new Date());
            assertFalse(question.isAnswered());
            question.setAnswered(true);
            assertTrue(question.isAnswered());
        }

        @Test
        @DisplayName("Question reference system should support follow-up questions")
        public void testQuestionReference() {
            Question question = new Question(1, "Follow-up question", "testUser", new Date(), 5);
            assertTrue(question.hasReference());
            assertEquals(Integer.valueOf(5), question.getReferenceQuestionId());
        }
    }
    
    /**
     * Test suite for Questions manager functionality.
     */
    @Nested
    @DisplayName("Questions Manager Tests")
    public class QuestionsManagerTests {
        
        @Test
        @DisplayName("Add and retrieve question")
        public void testAddQuestion() throws SQLException {
            String uniqueContent = "Test content " + System.currentTimeMillis();
            Question question = new Question(0, uniqueContent, "testUser", new Date());
            questions.addQuestion(question);
            
            List<Question> allQuestions = questions.getAllQuestions();
            boolean found = false;
            for (Question q : allQuestions) {
                if (q.getContent().equals(uniqueContent)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        @Test
        @DisplayName("Search questions by keyword")
        public void testSearchQuestions() throws SQLException {
            String uniqueContent = "UniqueKeyword" + System.currentTimeMillis();
            Question question = new Question(0, uniqueContent, "testUser", new Date());
            questions.addQuestion(question);
            
            List<Question> results = questions.searchQuestions(uniqueContent);
            assertFalse(results.isEmpty());
        }

        @Test
        @DisplayName("Search questions by user")
        public void testSearchByUser() throws SQLException {
            String uniqueUser = "uniqueUser" + System.currentTimeMillis();
            Question question = new Question(0, "Test content", uniqueUser, new Date());
            questions.addQuestion(question);
            
            List<Question> results = questions.searchQuestionsByUser(uniqueUser);
            assertFalse(results.isEmpty());
        }

        @Test
        @DisplayName("Update question content")
        public void testUpdateQuestion() throws SQLException {
            // Add a question
            Question question = new Question(0, "Original content", "testUser", new Date());
            questions.addQuestion(question);
            
            // Find the added question
            List<Question> allQuestions = questions.getAllQuestions();
            Question added = null;
            for (Question q : allQuestions) {
                if (q.getContent().equals("Original content")) {
                    added = q;
                    break;
                }
            }
            
            assertNotNull(added);
            
            // Update the question
            added.setContent("Updated content");
            questions.updateQuestion(added);
            
            // Verify the update
            Question updated = questions.getQuestionById(added.getQuestionId());
            assertEquals("Updated content", updated.getContent());
        }

        @Test
        @DisplayName("Remove question")
        public void testRemoveQuestion() throws SQLException {
            // Add a question
            String uniqueContent = "DeleteTest" + System.currentTimeMillis();
            Question question = new Question(0, uniqueContent, "testUser", new Date());
            questions.addQuestion(question);
            
            // Find the added question
            List<Question> allQuestions = questions.getAllQuestions();
            Question added = null;
            for (Question q : allQuestions) {
                if (q.getContent().equals(uniqueContent)) {
                    added = q;
                    break;
                }
            }
            
            assertNotNull(added);
            int initialCount = questions.getAllQuestions().size();
            
            // Remove the question
            questions.removeQuestion(added.getQuestionId());
            
            // Verify it was removed
            int afterCount = questions.getAllQuestions().size();
            assertEquals(initialCount - 1, afterCount);
        }
    }
    
    /**
     * Test suite for Answer class functionality.
     */
    @Nested
    @DisplayName("Answer Tests")
    public class AnswerTests {
        
        @Test
        @DisplayName("Answer validation should enforce content requirements")
        public void testAnswerValidation() {
            Answer answer = new Answer(1, 1, "This is a test answer", "testUser", new Date());
            assertTrue(answer.validate());
        }

        @Test
        @DisplayName("Answer validation should enforce non-empty content requirement")
        public void testAnswerValidationEmptyContent() {
            Answer answer = new Answer(1, 1, "", "testUser", new Date());
            assertFalse(answer.validate());
        }

        @Test
        @DisplayName("Answer status should track accepted/unaccepted state")
        public void testMarkAnswerAsAccepted() {
            Answer answer = new Answer(1, 1, "Content", "testUser", new Date());
            assertFalse(answer.isAccepted());
            answer.setAccepted(true);
            assertTrue(answer.isAccepted());
        }

        @Test
        @DisplayName("Answer reference system should support reply functionality")
        public void testAnswerReference() {
            Answer answer = new Answer(1, 1, "Follow-up answer", "testUser", new Date(), 5);
            assertTrue(answer.hasReference());
            assertEquals(Integer.valueOf(5), answer.getReferenceAnswerId());
        }
    }
    
    /**
     * Test suite for Answers manager functionality.
     */
    @Nested
    @DisplayName("Answers Manager Tests")
    public class AnswersManagerTests {
        
        @Test
        @DisplayName("Add and retrieve answers for question")
        public void testAddAnswer() throws SQLException {
            // Add a question
            Question question = new Question(0, "Test question", "testUser", new Date());
            questions.addQuestion(question);
            
            // Find the added question
            List<Question> allQuestions = questions.getAllQuestions();
            Question added = null;
            for (Question q : allQuestions) {
                if (q.getContent().equals("Test question")) {
                    added = q;
                    break;
                }
            }
            
            assertNotNull(added);
            
            // Add an answer
            Answer answer = new Answer(0, added.getQuestionId(), "Test answer", "answerUser", new Date());
            answers.addAnswer(answer);
            
            // Verify it was added
            List<Answer> questionAnswers = answers.getAnswersForQuestion(added.getQuestionId());
            assertFalse(questionAnswers.isEmpty());
        }

        @Test
        @DisplayName("Remove answer")
        public void testRemoveAnswer() throws SQLException {
            // Add question and answer
            Question question = new Question(0, "Test question", "testUser", new Date());
            questions.addQuestion(question);
            
            List<Question> allQuestions = questions.getAllQuestions();
            Question added = null;
            for (Question q : allQuestions) {
                if (q.getContent().equals("Test question")) {
                    added = q;
                    break;
                }
            }
            
            assertNotNull(added);
            
            Answer answer = new Answer(0, added.getQuestionId(), "Test answer", "answerUser", new Date());
            answers.addAnswer(answer);
            
            // Find the added answer
            List<Answer> answerList = answers.getAnswersForQuestion(added.getQuestionId());
            assertFalse(answerList.isEmpty());
            Answer addedAnswer = answerList.get(0);
            
            int initialCount = answers.getAllAnswers().size();
            
            // Remove the answer
            answers.removeAnswer(addedAnswer.getAnswerId());
            
            // Verify it was removed
            int afterCount = answers.getAllAnswers().size();
            assertEquals(initialCount - 1, afterCount);
        }

        @Test
        @DisplayName("Update answer content")
        public void testUpdateAnswer() throws SQLException {
            // Add question and answer
            Question question = new Question(0, "Test question", "testUser", new Date());
            questions.addQuestion(question);
            
            List<Question> allQuestions = questions.getAllQuestions();
            Question added = null;
            for (Question q : allQuestions) {
                if (q.getContent().equals("Test question")) {
                    added = q;
                    break;
                }
            }
            
            assertNotNull(added);
            
            Answer answer = new Answer(0, added.getQuestionId(), "Original answer", "answerUser", new Date());
            answers.addAnswer(answer);
            
            // Find the added answer
            List<Answer> answerList = answers.getAnswersForQuestion(added.getQuestionId());
            Answer addedAnswer = answerList.get(0);
            
            // Update the answer
            addedAnswer.setContent("Updated answer");
            answers.updateAnswer(addedAnswer);
            
            // Verify the update
            Answer updated = answers.getAnswerById(addedAnswer.getAnswerId());
            assertEquals("Updated answer", updated.getContent());
        }
    }
    
    /**
     * Test suite for DatabaseHelper2 functionality.
     */
    @Nested
    @DisplayName("DatabaseHelper2 Tests")
    public class DatabaseHelper2Tests {
        
        @Test
        @DisplayName("Database connection should be established")
        public void testDatabaseConnection() {
            assertNotNull(dbHelper.connection);
        }

        @Test
        @DisplayName("Get question by ID")
        public void testGetQuestionById() throws SQLException {
            // Add a question
            Question question = new Question(0, "Test for retrieval", "testUser", new Date());
            questions.addQuestion(question);
            
            // Find the added question
            List<Question> allQuestions = questions.getAllQuestions();
            Question added = null;
            for (Question q : allQuestions) {
                if (q.getContent().equals("Test for retrieval")) {
                    added = q;
                    break;
                }
            }
            
            assertNotNull(added);
            int id = added.getQuestionId();
            
            // Retrieve by ID
            Question retrieved = dbHelper.getQuestionById(id);
            assertNotNull(retrieved);
            assertEquals(id, retrieved.getQuestionId());
        }

        @Test
        @DisplayName("Get answer by ID")
        public void testGetAnswerById() throws SQLException {
            // Add question and answer
            Question question = new Question(0, "Test question", "testUser", new Date());
            questions.addQuestion(question);
            
            List<Question> allQuestions = questions.getAllQuestions();
            Question added = null;
            for (Question q : allQuestions) {
                if (q.getContent().equals("Test question")) {
                    added = q;
                    break;
                }
            }
            
            assertNotNull(added);
            
            String uniqueContent = "Unique answer content " + System.currentTimeMillis();
            Answer answer = new Answer(0, added.getQuestionId(), uniqueContent, "answerUser", new Date());
            answers.addAnswer(answer);
            
            // Find the added answer
            List<Answer> answerList = answers.getAnswersForQuestion(added.getQuestionId());
            Answer addedAnswer = null;
            for (Answer a : answerList) {
                if (a.getContent().equals(uniqueContent)) {
                    addedAnswer = a;
                    break;
                }
            }
            
            assertNotNull(addedAnswer);
            int answerId = addedAnswer.getAnswerId();
            
            // Retrieve by ID
            Answer retrieved = dbHelper.getAnswerById(answerId);
            assertNotNull(retrieved);
            assertEquals(answerId, retrieved.getAnswerId());
        }

        @Test
        @DisplayName("Mark answers as read")
        public void testMarkAnswersAsRead() throws SQLException {
            // Add question and answer
            Question question = new Question(0, "Read test question", "testUser", new Date());
            questions.addQuestion(question);
            
            List<Question> allQuestions = questions.getAllQuestions();
            Question added = null;
            for (Question q : allQuestions) {
                if (q.getContent().equals("Read test question")) {
                    added = q;
                    break;
                }
            }
            
            assertNotNull(added);
            
            Answer answer = new Answer(0, added.getQuestionId(), "Test answer", "answerUser", new Date());
            answers.addAnswer(answer);
            
            // Check unread count, mark as read, check again
            String testUser = "readTestUser" + System.currentTimeMillis();
            int beforeCount = dbHelper.getUnreadAnswersCount(added.getQuestionId(), testUser);
            dbHelper.markAnswersAsRead(added.getQuestionId(), testUser);
            int afterCount = dbHelper.getUnreadAnswersCount(added.getQuestionId(), testUser);
            
            // After marking as read, count should be less than or equal to before
            assertTrue(afterCount <= beforeCount);
        }

        @Test
        @DisplayName("Add and retrieve feedback")
        public void testFeedback() throws SQLException {
            // Add a question
            Question question = new Question(0, "Test question", "testUser", new Date());
            questions.addQuestion(question);
            
            // Find the added question
            List<Question> allQuestions = questions.getAllQuestions();
            Question added = null;
            for (Question q : allQuestions) {
                if (q.getContent().equals("Test question")) {
                    added = q;
                    break;
                }
            }
            
            assertNotNull(added);
            
            // Add feedback
            String uniqueContent = "Feedback " + System.currentTimeMillis();
            String uniqueReceiver = "receiver" + System.currentTimeMillis();
            Feedback feedback = new Feedback(0, "sender", uniqueReceiver, uniqueContent, new Date(), added.getQuestionId());
            dbHelper.addFeedback(feedback);
            
            // Verify feedback was added
            List<Feedback> feedbacks = dbHelper.getFeedbackForUser(uniqueReceiver);
            boolean found = false;
            for (Feedback f : feedbacks) {
                if (f.getContent().equals(uniqueContent)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        @Test
        @DisplayName("Delete feedback")
        public void testDeleteFeedback() throws SQLException {
            // Add a question
            Question question = new Question(0, "Test question", "testUser", new Date());
            questions.addQuestion(question);
            
            // Find the added question
            List<Question> allQuestions = questions.getAllQuestions();
            Question added = null;
            for (Question q : allQuestions) {
                if (q.getContent().equals("Test question")) {
                    added = q;
                    break;
                }
            }
            
            assertNotNull(added);
            
            // Add feedback
            String uniqueReceiver = "receiver" + System.currentTimeMillis();
            Feedback feedback = new Feedback(0, "sender", uniqueReceiver, "Test feedback", new Date(), added.getQuestionId());
            dbHelper.addFeedback(feedback);
            
            // Find the added feedback
            List<Feedback> feedbacks = dbHelper.getFeedbackForUser(uniqueReceiver);
            assertFalse(feedbacks.isEmpty());
            Feedback addedFeedback = feedbacks.get(0);
            
            // Delete the feedback
            dbHelper.deleteFeedback(addedFeedback.getFeedbackId());
            
            // Verify it was deleted
            List<Feedback> afterDelete = dbHelper.getFeedbackForUser(uniqueReceiver);
            assertTrue(afterDelete.isEmpty() || !afterDelete.contains(addedFeedback));
        }
    }
    
    /**
     * Test suite for Feedback class functionality.
     */
    @Nested
    @DisplayName("Feedback Tests")
    public class FeedbackTests {
        
        @Test
        @DisplayName("Create feedback with valid data")
        public void testCreateFeedback() {
            Date now = new Date();
            Feedback feedback = new Feedback(1, "sender1", "receiver1", "Great answer!", now, 1);
            
            assertEquals(1, feedback.getFeedbackId());
            assertEquals("sender1", feedback.getSender());
            assertEquals("receiver1", feedback.getReceiver());
            assertEquals("Great answer!", feedback.getContent());
            assertEquals(now, feedback.getTimestamp());
            assertEquals(1, feedback.getQuestionId());
        }

        @Test
        @DisplayName("Update feedback properties")
        public void testUpdateFeedback() {
            Date initialDate = new Date();
            Feedback feedback = new Feedback(1, "sender1", "receiver1", "Initial content", initialDate, 1);
            
            Date newDate = new Date();
            feedback.setFeedbackId(2);
            feedback.setSender("newSender");
            feedback.setReceiver("newReceiver");
            feedback.setContent("Updated content");
            feedback.setTimestamp(newDate);
            feedback.setQuestionId(2);
            
            assertEquals(2, feedback.getFeedbackId());
            assertEquals("newSender", feedback.getSender());
            assertEquals("newReceiver", feedback.getReceiver());
            assertEquals("Updated content", feedback.getContent());
            assertEquals(newDate, feedback.getTimestamp());
            assertEquals(2, feedback.getQuestionId());
        }
    }
    
    @AfterEach
    public void tearDown() throws SQLException {
        if (dbHelper != null && dbHelper.connection != null) {
            dbHelper.connection.close();
        }
    }
}