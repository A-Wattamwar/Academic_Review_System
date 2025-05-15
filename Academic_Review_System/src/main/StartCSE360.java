package main;

import javafx.application.Application;
import javafx.stage.Stage;
import java.sql.SQLException;

import databasePart1.DatabaseHelper;
import databasePart1.DatabaseHelper2;
import databasePart1.DatabaseHelper3;

/**
 * StartCSE360 class is the main entry point for the CSE360 application.
 * It extends the Application class and implements the start method.
 */
public class StartCSE360 extends Application {
	private static final DatabaseHelper databaseHelper = new DatabaseHelper();
    private static final DatabaseHelper2 databaseHelper2 = new DatabaseHelper2();
    private static final DatabaseHelper3 databaseHelper3 = new DatabaseHelper3();
	
	public static void main( String[] args )
	{
		 launch(args);
	}
	
	@Override
    public void start(Stage primaryStage) {
        try {
            databaseHelper.connectToDatabase(); // Connect to the database
            databaseHelper2.connectToDatabase();
            databaseHelper3.connectToDatabase();
            if (databaseHelper.isDatabaseEmpty()) {            	
            	new FirstPage(databaseHelper).show(primaryStage);
            } else {
            	new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
                
            }
        } catch (SQLException e) {
        	System.out.println(e.getMessage());
        }
    }
	

}
