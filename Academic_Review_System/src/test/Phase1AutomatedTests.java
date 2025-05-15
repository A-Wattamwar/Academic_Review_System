package test;

import main.UserAccountValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

import databasePart1.DatabaseHelper;
import main.User;
import java.sql.SQLException;

/**
 * Phase1AutomatedTests.java
 *
 * This class contains comprehensive test suites for validating user account data including:
 *   - Username validation tests using FSM approach
 *   - Password evaluation tests using directed-graph approach
 *   - Email validation tests
 *   - Full name validation tests
 *
 * Each test suite is organized in nested classes for better organization and readability.
 * The tests use JUnit Jupiter (JUnit 5) framework.
 *
 * <p> Copyright: Team 60 CSE 360 </p>
 *
 * @version 1.00    2025-04-01    Initial implementation of all validation test suites
 */
public class Phase1AutomatedTests {

    private DatabaseHelper databaseHelper;

    @BeforeEach
    public void setUp() throws SQLException {
        databaseHelper = new DatabaseHelper();
        databaseHelper.connectToDatabase();
    }

    /**
     * Test suite for username validation.
     * Tests various aspects of username requirements including:
     *   - Length constraints (4-16 characters)
     *   - Character type restrictions
     *   - Special character placement rules
     */
    @Nested
    @DisplayName("Username Validation Tests")
    public class UsernameValidationTests {
        
        /**
         * Tests that an empty username is rejected by the validator.
         */
        @Test
        @DisplayName("Empty username should be rejected")
        public void testEmptyUsername() {
            String result = UserAccountValidator.UserNameRecognizer.checkForValidUserName("");
            assertTrue(result.contains("empty"));
        }

        /**
         * Tests that usernames shorter than the minimum length (4 characters) are rejected.
         */
        @Test
        @DisplayName("Username shorter than 4 characters should be rejected")
        public void testShortUsername() {
            String result = UserAccountValidator.UserNameRecognizer.checkForValidUserName("ab1");
            assertTrue(result.contains("must have at least 4 characters"));
        }

        /**
         * Tests that usernames exceeding the maximum length (16 characters) are rejected.
         */
        @Test
        @DisplayName("Username longer than 16 characters should be rejected")
        public void testLongUsername() {
            String result = UserAccountValidator.UserNameRecognizer.checkForValidUserName("ReallyLongUsernameThatExceedsLimit");
            assertTrue(result.contains("no more than 16 characters"));
        }

        /**
         * Tests that a username meeting all requirements is accepted.
         */
        @Test
        @DisplayName("Valid username should be accepted")
        public void testValidUsername() {
            String result = UserAccountValidator.UserNameRecognizer.checkForValidUserName("IronMan99");
            assertTrue(result.isEmpty());
        }

        /**
         * Tests that usernames with valid special characters are accepted.
         * Valid special characters include hyphens and underscores when properly placed.
         */
        @Test
        @DisplayName("Username with valid special characters should be accepted")
        public void testValidSpecialCharacters() {
            String result = UserAccountValidator.UserNameRecognizer.checkForValidUserName("Thor-Warrior_99");
            assertTrue(result.isEmpty());
        }

        /**
         * Tests that usernames containing invalid special characters are rejected.
         */
        @Test
        @DisplayName("Username with invalid characters should be rejected")
        public void testInvalidCharacters() {
            String result = UserAccountValidator.UserNameRecognizer.checkForValidUserName("Doctor&Strange");
            assertFalse(result.isEmpty());
        }

        /**
         * Tests that usernames beginning with special characters are rejected.
         */
        @Test
        @DisplayName("Username starting with special character should be rejected")
        public void testStartingWithSpecialChar() {
            String result = UserAccountValidator.UserNameRecognizer.checkForValidUserName("-LokiLord");
            assertTrue(result.contains("must start with A-Z or a-z"));
        }

        /**
         * Tests that usernames containing consecutive special characters are rejected.
         */
        @Test
        @DisplayName("Username with consecutive special characters should be rejected")
        public void testConsecutiveSpecialChars() {
            String result = UserAccountValidator.UserNameRecognizer.checkForValidUserName("E..dith");
            assertFalse(result.isEmpty());
        }

        /**
         * Tests that usernames containing consecutive hyphens are rejected.
         */
        @Test
        @DisplayName("Username with consecutive hyphens should be rejected")
        public void testConsecutiveHyphens() {
            String result = UserAccountValidator.UserNameRecognizer.checkForValidUserName("Spider--man");
            assertFalse(result.isEmpty());
        }
    }

    /**
     * Test suite for password validation.
     * Tests various password requirements including:
     *   - Presence of uppercase letters
     *   - Presence of lowercase letters
     *   - Presence of numeric digits
     *   - Presence of special characters
     *   - Minimum length requirement
     */
    @Nested
    @DisplayName("Password Validation Tests")
    public class PasswordValidationTests {
        
        /**
         * Tests that passwords without any uppercase letters are rejected.
         */
        @Test
        @DisplayName("Password missing uppercase should be rejected")
        public void testMissingUppercase() {
            String result = UserAccountValidator.PasswordEvaluator.evaluatePassword("abc123@");
            assertTrue(result.contains("Upper case"));
        }

        /**
         * Tests that passwords without any lowercase letters are rejected.
         */
        @Test
        @DisplayName("Password missing lowercase should be rejected")
        public void testMissingLowercase() {
            String result = UserAccountValidator.PasswordEvaluator.evaluatePassword("XYZ@789");
            assertTrue(result.contains("Lower case"));
        }

        /**
         * Tests that passwords without any special characters are rejected.
         */
        @Test
        @DisplayName("Password missing special character should be rejected")
        public void testMissingSpecialChar() {
            String result = UserAccountValidator.PasswordEvaluator.evaluatePassword("MightyPass77");
            assertTrue(result.contains("Special character"));
        }

        /**
         * Tests that passwords meeting all requirements are accepted.
         */
        @Test
        @DisplayName("Valid password should be accepted")
        public void testValidPassword() {
            String result = UserAccountValidator.PasswordEvaluator.evaluatePassword("SturdyP@ss456");
            assertTrue(result.isEmpty());
        }

        /**
         * Tests that passwords without any numeric digits are rejected.
         */
        @Test
        @DisplayName("Password missing digits should be rejected")
        public void testMissingDigits() {
            String result = UserAccountValidator.PasswordEvaluator.evaluatePassword("Ab-cd-efg");
            assertTrue(result.contains("Numeric digits"));
        }
    }

    /**
     * Test suite for email validation.
     * Tests various email format requirements including:
     *   - Non-empty requirement
     *   - Presence and position of @ symbol
     *   - Presence and position of domain separator (.)
     */
    @Nested
    @DisplayName("Email Validation Tests")
    public class EmailValidationTests {
        
        /**
         * Tests that empty email addresses are rejected.
         */
        @Test
        @DisplayName("Empty email should be rejected")
        public void testEmptyEmail() {
            String result = UserAccountValidator.UserProfileValidator.validateEmail("");
            assertTrue(result.contains("required"));
        }

        /**
         * Tests that email addresses without @ symbol and domain separator are rejected.
         */
        @Test
        @DisplayName("Email without @ and . should be rejected")
        public void testEmailWithoutAt() {
            String result = UserAccountValidator.UserProfileValidator.validateEmail("testemail");
            assertTrue(result.contains("@"));
        }

        /**
         * Tests that properly formatted email addresses are accepted.
         */
        @Test
        @DisplayName("Valid email should be accepted")
        public void testValidEmail() {
            String result = UserAccountValidator.UserProfileValidator.validateEmail("hello.world@mail.com");
            assertTrue(result.isEmpty());
        }

        /**
         * Tests that email addresses without a domain part are rejected.
         */
        @Test
        @DisplayName("Email without domain should be rejected")
        public void testEmailWithoutDomain() {
            String result = UserAccountValidator.UserProfileValidator.validateEmail("contact@domainname");
            assertFalse(result.isEmpty());
        }

        /**
         * Tests that email addresses with invalid domain format are rejected.
         */
        @Test
        @DisplayName("Email with invalid domain format should be rejected")
        public void testEmailInvalidDomainFormat() {
            String result = UserAccountValidator.UserProfileValidator.validateEmail("contact@.com");
            assertFalse(result.isEmpty());
        }
    }

    /**
     * Test suite for full name validation.
     * Tests various full name requirements including:
     *   - Non-empty requirement
     *   - Character restrictions (letters and spaces only)
     */
    @Nested
    @DisplayName("Full Name Validation Tests")
    public class FullNameValidationTests {
        
        /**
         * Tests that empty full names are rejected.
         */
        @Test
        @DisplayName("Empty full name should be rejected")
        public void testEmptyFullName() {
            String result = UserAccountValidator.UserProfileValidator.validateFullName("");
            assertTrue(result.contains("required"));
        }

        /**
         * Tests that properly formatted full names are accepted.
         */
        @Test
        @DisplayName("Valid full name should be accepted")
        public void testValidFullName() {
            String result = UserAccountValidator.UserProfileValidator.validateFullName("Anna Marie");
            assertTrue(result.isEmpty());
        }

        /**
         * Tests that full names containing numbers are rejected.
         */
        @Test
        @DisplayName("Full name with numbers should be rejected")
        public void testFullNameWithNumbers() {
            String result = UserAccountValidator.UserProfileValidator.validateFullName("Anna Marie 1");
            assertFalse(result.isEmpty());
        }
    }

    /**
     * Test suite for database operations.
     * Tests various database functionalities including:
     *   - User creation and admin role assignment
     *   - User login verification
     *   - Role assignment and retrieval
     *   - User existence checks
     *   - Database state verification
     */
    @Nested
    @DisplayName("Database Operation Tests")
    public class DatabaseOperationTests {
        
        /**
         * Tests that the first user created in the system is automatically
         * assigned the admin role.
         * @throws SQLException if database operation fails
         */
        @Test
        @DisplayName("First user should be created as admin")
        public void testFirstUserCreation() throws SQLException {
            User user = new User("BlackPanther1", "StrongP@ss123", "T'Challa", "tchalla@wakanda.com", 
                new String[]{"admin"});
            databaseHelper.register(user);
            String[] roles = databaseHelper.getUserRoles("BlackPanther1");
            assertTrue(roles[0].equals("admin"));
        }

        /**
         * Verifies that the login verification process works correctly
         * for a registered user with valid credentials.
         * @throws SQLException if database operation fails
         */
        @Test
        @DisplayName("User login verification")
        public void testUserLoginVerification() throws SQLException {
            User user = new User("StarLordUser99", "SpaceP@ss123", "Peter Quill", "starlord@guardians.com", 
                new String[]{"admin"});
            databaseHelper.register(user);
            boolean loginSuccess = databaseHelper.login(user);
            assertTrue(loginSuccess);
        }

        /**
         * Tests that role assignment works correctly for multiple roles.
         * Verifies that a user can be assigned and retrieve multiple distinct roles.
         * @throws SQLException if database operation fails
         */
        @Test
        @DisplayName("User should be assigned multiple roles")
        public void testRoleAssignment() throws SQLException {
            User user = new User("HawkeyeLeader", "ArrowP@ss123", "Clint Barton", "hawkeye@shield.com", 
                new String[]{"instructor", "reviewer"});
            databaseHelper.register(user);
            String[] roles = databaseHelper.getUserRoles("HawkeyeLeader");
            assertEquals(2, roles.length);
        }

        /**
         * Tests that user existence check works correctly.
         * Verifies that the system can accurately detect registered users.
         * @throws SQLException if database operation fails
         */
        @Test
        @DisplayName("User existence check")
        public void testUserExistence() throws SQLException {
            User user = new User("IronManTest", "StarkP@ss123", "Tony Stark", "tony@stark.com", 
                new String[]{"admin"});
            databaseHelper.register(user);
            assertTrue(databaseHelper.doesUserExist("IronManTest"));
        }

        /**
         * Tests that the database starts in an empty state.
         * Verifies that no users exist in the database upon initialization.
         * @throws SQLException if database operation fails
         */
        @Test
        @DisplayName("Database should start empty")
        public void testDatabaseEmpty() throws SQLException {
            assertTrue(databaseHelper.isDatabaseEmpty());
        }
    }

    /**
     * Test suite for invitation code functionality.
     * Tests various aspects of invitation system including:
     *   - Generation of invitation codes
     *   - Validation of invitation codes
     *   - Role assignment through invitation codes
     *   - Expiration handling
     */
    @Nested
    @DisplayName("Invite Code Tests")
    public class InviteCodeTests {
        
        /**
         * Tests that invitation codes are properly generated and validated.
         * Verifies that newly generated codes are immediately valid in the system.
         * @throws SQLException if database operation fails
         */
        @Test
        @DisplayName("Should generate and validate invitation code")
        public void testInviteCodeValidation() throws SQLException {
            String inviteCode = databaseHelper.generateInvitationCode(
                new String[]{"student"}, 
                new java.sql.Timestamp(System.currentTimeMillis() + 86400000)
            );
            assertTrue(databaseHelper.validateInvitationCode(inviteCode));
        }

        /**
         * Tests that invitation codes correctly store and retrieve role information.
         * Verifies that roles assigned during code generation can be retrieved accurately.
         * @throws SQLException if database operation fails
         */
        @Test
        @DisplayName("Should retrieve correct roles for invite code")
        public void testInviteCodeRoles() throws SQLException {
            String inviteCode = databaseHelper.generateInvitationCode(
                new String[]{"student"}, 
                new java.sql.Timestamp(System.currentTimeMillis() + 86400000)
            );
            String[] roles = databaseHelper.getInvitationRoles(inviteCode);
            assertEquals("student", roles[0]);
        }
    }
}
