package main;

/*
 * UserAccountValidator.java
 *
 * This file provides methods to validate user account data. It combines:
 *   - UserName recognition via a finite state machine (FSM)
 *   - Password evaluation via a directed-graph approach
 *   - Email and full name validation for user profiles
 *
 * Each section of functionality is encapsulated within its own static inner class.
 */
public class UserAccountValidator {

    // **********************************************
    // Inner class: UserNameRecognizer (FSM-based)
    // **********************************************
    public static class UserNameRecognizer {
        /**
         * <p> Title: FSM-translated UserNameRecognizer. </p>
         * 
         * <p> Description: A demonstration of the mechanical translation of a Finite State Machine 
         * diagram into an executable Java program using the UserName Recognizer. The code 
         * detailed design is based on a while loop with a select list.</p>
         * 
         * <p> Copyright: Team 60 CSE 360 </p>
         * 
         * @version 1.00        2024-09-13    Initial baseline derived from the Even Recognizer
         * @version 1.01        2024-09-17    Correction to address UNChar coding error, improper error
         *                                  message, and improve internal documentation
         */
    
        public static String userNameRecognizerErrorMessage = "";   // The error message text
        public static String userNameRecognizerInput = "";          // The input being processed
        public static int userNameRecognizerIndexofError = -1;       // The index of error location
        private static int state = 0;                                 // The current state value
        private static int nextState = 0;                             // The next state value
        private static boolean finalState = false;                  // Is this state a final state?
        private static String inputLine = "";                       // The input line
        private static char currentChar;                            // The current character in the line
        private static int currentCharNdx;                          // The index of the current character
        private static boolean running;                             // The flag that specifies if the FSM is running
        private static int userNameSize = 0;                        // A numeric value may not exceed 16 characters
    
        // Private method to display debugging data
        private static void displayDebuggingInfo() {
            // For clarity, we determine the spacing based on nextState value.
            String spacing;
            if (nextState > 99) {
                spacing = "";
            } else if (nextState > 9 || nextState == -1) {
                spacing = "   ";
            } else {
                spacing = "    ";
            }
    
            if (currentCharNdx >= inputLine.length())
                // Display the line with the current state numbers aligned
                System.out.println(((state > 99) ? " " : (state > 9) ? "  " : "   ") + state + 
                        ((finalState) ? "       F   " : "           ") + "None");
            else
                System.out.println(((state > 99) ? " " : (state > 9) ? "  " : "   ") + state + 
                    ((finalState) ? "       F   " : "           ") + "  " + currentChar + " " +
                    spacing + nextState + "     " + userNameSize);
        }
        
        // Private method to move to the next character within the limits of the input line
        private static void moveToNextCharacter() {
            currentCharNdx++;
            if (currentCharNdx < inputLine.length())
                currentChar = inputLine.charAt(currentCharNdx);
            else {
                currentChar = ' ';
                running = false;
            }
        }
    
        /**
         * Checks for a valid user name using a finite state machine approach.
         *
         * @param input  The input string for the Finite State Machine
         * @return       An empty string if valid or an error message otherwise.
         */
        public static String checkForValidUserName(String input) {
            if (input.length() <= 0) {
                userNameRecognizerIndexofError = 0;
                return "\n*** ERROR *** The input is empty";
            }
            
            // Initialize FSM variables
            state = 0;
            inputLine = input;
            currentCharNdx = 0;
            currentChar = input.charAt(0);
            userNameRecognizerInput = input;
            running = true;
            nextState = -1;
            System.out.println("\nCurrent Final Input  Next  Date\nState   State Char  State  Size");
            userNameSize = 0;
    
            // Process the input via the FSM
            while (running) {
                switch (state) {
                    case 0: 
                        // State 0: Accepts only A-Z or a-z; transitions to State 1 if valid.
                        if ((currentChar >= 'A' && currentChar <= 'Z') ||
                            (currentChar >= 'a' && currentChar <= 'z')) {
                            nextState = 1;
                            userNameSize++;
                        } else {
                            running = false;
                        }
                        break;
    
                    case 1:
                        // State 1: Accepts A-Z, a-z, 0-9 (remains in State 1) or period, underscore, minus (goes to State 2).
                        if ((currentChar >= 'A' && currentChar <= 'Z') ||
                            (currentChar >= 'a' && currentChar <= 'z') ||
                            (currentChar >= '0' && currentChar <= '9')) {
                            nextState = 1;
                            userNameSize++;
                        } else if (currentChar == '.' || currentChar == '_' || currentChar == '-') {
                            nextState = 2;
                            userNameSize++;
                        } else {
                            running = false;
                        }
                        if (userNameSize > 16)
                            running = false;
                        break;
    
                    case 2:
                        // State 2: Expects A-Z, a-z, 0-9; transitions back to State 1.
                        if ((currentChar >= 'A' && currentChar <= 'Z') ||
                            (currentChar >= 'a' && currentChar <= 'z') ||
                            (currentChar >= '0' && currentChar <= '9')) {
                            nextState = 1;
                            userNameSize++;
                        } else {
                            running = false;
                        }
                        if (userNameSize > 16)
                            running = false;
                        break;
                }
    
                if (running) {
                    displayDebuggingInfo();
                    moveToNextCharacter();
                    state = nextState;
                    if (state == 1)
                        finalState = true;
                    nextState = -1;
                }
            }
            displayDebuggingInfo();
            System.out.println("The loop has ended.");
    
            userNameRecognizerIndexofError = currentCharNdx;
            userNameRecognizerErrorMessage = "\n*** ERROR *** ";
            switch (state) {
                case 0:
                    userNameRecognizerErrorMessage += "A UserName must start with A-Z or a-z.";
                    return userNameRecognizerErrorMessage;
    
                case 1:
                    if (userNameSize < 4) {
                        userNameRecognizerErrorMessage += "A UserName must have at least 4 characters.";
                        return userNameRecognizerErrorMessage;
                    } else if (userNameSize > 16) {
                        userNameRecognizerErrorMessage += "A UserName must have no more than 16 characters.";
                        return userNameRecognizerErrorMessage;
                    } else if (currentCharNdx < input.length()) {
                        userNameRecognizerErrorMessage += "A UserName may only contain A-Z, a-z, 0-9.";
                        return userNameRecognizerErrorMessage;
                    } else {
                        userNameRecognizerIndexofError = -1;
                        userNameRecognizerErrorMessage = "";
                        return userNameRecognizerErrorMessage;
                    }
    
                case 2:
                    userNameRecognizerErrorMessage += "A character after a period, underscore, or minus sign must be A-Z, a-z, or 0-9.";
                    return userNameRecognizerErrorMessage;
    
                default:
                    return "";
            }
        }
    }
    
    // *********************************************
    // Inner class: PasswordEvaluator (Directed Graph)
    // *********************************************
    public static class PasswordEvaluator {
        /**
         * <p> Title: Directed Graph-translated Password Assessor. </p>
         * 
         * <p> Description: A demonstration of the mechanical translation of a Directed Graph 
         * diagram into an executable Java program using the Password Evaluator. The code design 
         * is based on a while loop with a cascade of if statements.</p>
         * 
         * <p> Copyright: Lynn Robert Carter © 2022 </p>
         * 
         * @version 0.00        2018-02-22    Initial baseline 
         */
    
        public static String passwordErrorMessage = "";       // The error message text
        public static String passwordInput = "";              // The input being processed
        public static int passwordIndexofError = -1;           // The index where the error was located
        public static boolean foundUpperCase = false;
        public static boolean foundLowerCase = false;
        public static boolean foundNumericDigit = false;
        public static boolean foundSpecialChar = false;
        public static boolean foundLongEnough = false;
        private static String inputLine = "";                 // The input line
        private static char currentChar;                      // The current character in the line
        private static int currentCharNdx;                    // The index of the current character
        private static boolean running;                       // The flag that specifies if the FSM is running
    
        // Private method to display input state for debugging
        private static void displayInputState() {
            System.out.println(inputLine);
            System.out.println(inputLine.substring(0, currentCharNdx) + "?");
            System.out.println("Password size: " + inputLine.length() +
                               " | Current index: " + currentCharNdx +
                               " | Current char: \"" + currentChar + "\"");
        }
    
        /**
         * Evaluates the password based on several conditions (e.g., character types and minimum length).
         *
         * @param input  The password string to be evaluated.
         * @return       An empty string if valid or an error message if not.
         */
        public static String evaluatePassword(String input) {
            passwordErrorMessage = "";
            passwordIndexofError = 0;
            inputLine = input;
            currentCharNdx = 0;
    
            if (input.length() <= 0)
                return "*** Error *** The password is empty!";
    
            currentChar = input.charAt(0);
            passwordInput = input;
            foundUpperCase = false;
            foundLowerCase = false;
            foundNumericDigit = false;
            foundSpecialChar = false;
            foundLongEnough = false;
            running = true;
    
            while (running) {
                displayInputState();
                if (currentChar >= 'A' && currentChar <= 'Z') {
                    System.out.println("Upper case letter found");
                    foundUpperCase = true;
                } else if (currentChar >= 'a' && currentChar <= 'z') {
                    System.out.println("Lower case letter found");
                    foundLowerCase = true;
                } else if (currentChar >= '0' && currentChar <= '9') {
                    System.out.println("Digit found");
                    foundNumericDigit = true;
                } else if ("~`!@#$%^&*()_-+{}[]|:,.?/".indexOf(currentChar) >= 0) {
                    System.out.println("Special character found");
                    foundSpecialChar = true;
                } else {
                    passwordIndexofError = currentCharNdx;
                    return "*** Error *** An invalid character has been found!";
                }
    
                if (currentCharNdx >= 7) {
                    System.out.println("At least 8 characters found");
                    foundLongEnough = true;
                }
    
                currentCharNdx++;
                if (currentCharNdx >= inputLine.length())
                    running = false;
                else
                    currentChar = input.charAt(currentCharNdx);
    
                System.out.println();
            }
    
            String errMessage = "";
            if (!foundUpperCase)
                errMessage += "Upper case; ";
            if (!foundLowerCase)
                errMessage += "Lower case; ";
            if (!foundNumericDigit)
                errMessage += "Numeric digits; ";
            if (!foundSpecialChar)
                errMessage += "Special character; ";
            if (!foundLongEnough)
                errMessage += "Long Enough; ";
    
            if (errMessage.isEmpty())
                return "";
    
            passwordIndexofError = currentCharNdx;
            return errMessage + "conditions were not satisfied for password";
        }
    }
    
 // *****************************************************
 // Inner class: UserProfileValidator (Email and Full Name)
 // *****************************************************
 public static class UserProfileValidator {

     /**
      * Validates an email address. The email must not be empty, must contain an '@' 
      * that is not the first character, and must include at least one '.' after the '@'
      * (with the '.' not being the last character).
      *
      * @param email The email address to validate.
      * @return      An empty string if the email is valid; otherwise, a detailed error message.
      */
     public static String validateEmail(String email) {
         if (email == null || email.trim().isEmpty()) {
             return "*** ERROR *** Email address is required.";
         }
         int atIndex = email.indexOf('@');
         if (atIndex == -1 || atIndex == 0) {
             return "*** ERROR *** Email must contain an '@' symbol and it cannot be the first character.";
         }
         int dotIndex = email.indexOf('.', atIndex);
         if (dotIndex <= atIndex + 1 || dotIndex >= email.length() - 1) {
             return "*** ERROR *** Email must contain a '.' after the '@' and it cannot be the last character.";
         }
         return "";
     }

     /**
      * Validates a full name. The full name must not be empty, must be between 2 and 50 characters in length,
      * and may contain only letters and spaces.
      *
      * @param fullName The full name to validate.
      * @return         An empty string if the full name is valid; otherwise, a detailed error message.
      */
     public static String validateFullName(String fullName) {
         if (fullName == null || fullName.trim().isEmpty()) {
             return "*** ERROR *** Full name is required.";
         }
         String trimmedName = fullName.trim();
         if (trimmedName.length() < 2 || trimmedName.length() > 50) {
             return "*** ERROR *** Full name must be between 2 and 50 characters in length.";
         }
         if (!trimmedName.matches("[a-zA-Z\\s]+")) {
             return "*** ERROR *** Full name may only contain letters and spaces.";
         }
         return "";
     }
 }
}

