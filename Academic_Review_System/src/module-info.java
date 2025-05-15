module FoundationCode {
	requires javafx.controls;
	requires java.sql;
	requires org.junit.jupiter.api;
	
	opens main to javafx.graphics, javafx.fxml, javafx.base;
	opens test to org.junit.platform.commons;
	exports main;
	exports databasePart1;
	exports test;
}