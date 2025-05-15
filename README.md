# Academic Review System (ARS)

## Overview
The Academic Review System (ARS) is a comprehensive JavaFX application designed to facilitate academic paper reviews and feedback among students, reviewers, and administrators. This platform streamlines the review process, maintains quality control through reviewer scorecards, and provides administrators with tools to manage the entire system.

The system features a robust User Account Management System with role-based access control and secure authentication to ensure proper permissions and data security throughout the application.

## Author
Ayush Sachin Wattamwar

## Key Features

### 1. User Account Management System
- Role-based access control (Admin, Student, Reviewer, Instructor, Staff roles)
- Secure authentication with password hashing
- One-time password generation for password resets
- Invitation code system for new user registration
- User profile management and editing
- Account status tracking and management
- Session management and secure logout

### 2. Reviewer Profile Management
- Reviewer profiles with about, experience, and specialties sections
- Feedback system for students to rate and comment on reviews
- Review history tracking with detailed analytics
- Profile editing capabilities with validation
- Profile visibility settings

### 3. Reviewer Scorecard System
- Automated scoring system based on multiple parameters
- Parameters include:
  - Friendliness (0-5)
  - Accuracy (0-5)
  - Judgement (0-5)
  - Communication (0-5)
- Auto-computed overall score with weighted calculations
- Score display in UI with visual indicators
- Trend analysis for score changes over time
- Comparative metrics against other reviewers

### 4. Admin Request Management
- Request creation and tracking with unique identifiers
- Status management (open/in progress/closed)
- Request reopening capability with history preservation
- Documentation of actions taken for auditing purposes
- Link between reopened and original requests
- Priority levels and assignment capabilities
- Notification system for status changes

## Technical Architecture

### System Architecture
- **Design Pattern**: MVC (Model-View-Controller) architecture
- **Database**: H2 embedded database with comprehensive schema
- **UI**: JavaFX-based interface with responsive design and dynamic gradient backgrounds
- **Security**: Password protection and role-based access control
- **Modularity**: Component-based design allowing for easy extension

### Database Design
The system utilizes multiple database helpers to manage different aspects:
- **DatabaseHelper**: User accounts, authentication, and invitation code management
- **DatabaseHelper2**: Questions, answers, and educational content
- **DatabaseHelper3**: Reviews, feedback, and reviewer profiles
- **DatabaseHelper4**: Admin requests and system management

### Class Structure
The application includes several key object models:
- **User**: Base class with role-specific extensions
- **Review**: Manages paper review content and metadata
- **Question/Answer**: Educational content management
- **Feedback**: Student responses to reviewer performance
- **Request**: Admin-level system management requests

### UML Diagrams
Detailed UML diagrams are available in the Design_Documents directory:
- Flow diagrams showing system interaction
- Database helper class diagrams
- Entity relationship diagrams for key components
- Class hierarchies and dependencies

## Technologies Used
- Java (JDK 11+)
- JavaFX 17 - GUI framework
- H2 Database - Embedded database
- JUnit 5 - Testing framework
- CSS - UI styling

## Testing Framework
- Comprehensive JUnit tests organized in test packages:
  1. `src/test/Phase1AutomatedTests.java`
  2. `src/test/Phase2AutomatedTests.java`
  3. `src/test/Phase3AutomatedTests.java`
  4. `src/test/Phase4AutomatedTests.java`
- Manual testing of UI components with documented test cases
- Database operation validation with data integrity checks
- Integration testing between components
- Performance testing for database operations
- Edge case testing for error handling

## Installation and Setup

### Prerequisites
- Eclipse IDE (2021 or newer recommended)
- Java Development Kit (JDK) 11 or higher
- JavaFX SDK 17 or higher
- JUnit 5
- H2 Database

### Setup Instructions

1. Clone the Repository:
```bash
git clone https://github.com/A-Wattamwar/Academic_Review_System.git
```

2. Import the Academic_Review_System project

3. Configure Build Path:
   - Right-click on the project
   - Select "Build Path" -> "Configure Build Path"
   - Navigate to "Libraries" tab
   - Under "Modulepath":
     - Add JRE System Library
     - Add JavaFX
     - Add JUnit 5
   - Under "Classpath":
     - Add H2 Database

4. Clean the Project:
   - In Eclipse menu: Project -> Clean

5. Configure Run Settings:
   - Go to Run -> Run Configurations
   - Ensure main class is set to `main.StartCSE360`
   - For MacOS users:
     - Uncheck "Use the -XstartOnFirstThread argument when launching with SWT"
   - Click Apply and Close

6. Reset Database:
   - Open `src/databasePart1/DatabaseHelper.java`
   - Uncomment line 50:
     ```java
     statement.execute("DROP ALL OBJECTS");
     ```
   - Save and run the file
   - Close the file
   - Comment the line again and save

7. Run the Application:
   - Select `src/main/StartCSE360.java`
   - Click Run

8. Run Automated Tests:
   - Right-click on `src/test/Phase4AutomatedTests.java`
   - Select "Run As" -> "JUnit Test"
   - Note: Ensure database is reset (Step 6) before running tests
   - The same way you can also run other PhaseAutomationTests.

## System Requirements
- Minimum 4GB RAM
- 100MB free disk space
- 1280x720 screen resolution or higher
- Internet connection not required (uses embedded database)

## Recent Updates
- Added reviewer profile management system
- Implemented automated reviewer scorecard
- Created admin request tracking system
- Enhanced database schema
- Added new UI components
- Implemented comprehensive testing
- Integrated user account management system

## Documentation
To access the comprehensive JavaDoc documentation, open `index.html` at `Academic_Review_System/doc/index.html`.



