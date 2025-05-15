package main;

/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as userName, password, and role.
 * Valid roles are: admin, student, instructor, staff, and reviewer.
 * 
 * <p> Copyright: Team 60 CSE 360 </p>
 *
 * @version 1.00    2025-04-01    User class implementation
 */
public class User {
    private String userName;
    private String password;
    private String[] roles;
    private String fullName;
    private String email;

    // Constructor for creating a new user with full details
    public User(String userName, String password, String fullName, String email, String[] roles) {
        this.userName = userName;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.roles = roles;
    }

    // Sets the roles of the user
    public void setRoles(String[] roles) {
        if (areValidRoles(roles)) {
            this.roles = roles;
        } else {
            throw new IllegalArgumentException("Invalid role(s). Must be one of: admin, student, instructor, staff, reviewer");
        }
    }

    // Validates if all given roles are allowed
    private boolean areValidRoles(String[] roles) {
        for (String role : roles) {
            if (!isValidRole(role)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidRole(String role) {
        return role.equals("admin") || 
               role.equals("student") || 
               role.equals("instructor") || 
               role.equals("staff") || 
               role.equals("reviewer");
    }

    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public String[] getRoles() { return roles; }
    public boolean hasRole(String role) {
        for (String userRole : roles) {
            if (userRole.equals(role)) return true;
        }
        return false;
    }

    // Add getters for new fields
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }

//    private boolean isValidEmail(String email) {
//        if (email == null || email.trim().isEmpty()) {
//            return false;
//        }
//        // Basic email validation: must contain @ and at least one . after @
//        int atIndex = email.indexOf('@');
//        if (atIndex == -1 || atIndex == 0) {
//            return false;
//        }
//        int dotIndex = email.indexOf('.', atIndex);
//        return dotIndex > atIndex + 1 && dotIndex < email.length() - 1;
//    }
//
//    private boolean isValidFullName(String fullName) {
//        if (fullName == null || fullName.trim().isEmpty()) {
//            return false;
//        }
//        // Full name should be between 2 and 50 characters and contain only letters and spaces
//        String trimmedName = fullName.trim();
//        return trimmedName.length() >= 2 && 
//               trimmedName.length() <= 50 && 
//               trimmedName.matches("[a-zA-Z\\s]+");
//    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}