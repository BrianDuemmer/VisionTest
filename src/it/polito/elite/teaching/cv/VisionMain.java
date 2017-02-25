package it.polito.elite.teaching.cv;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

/**
 * The main class for a JavaFX application. It creates and handle the main
 * window with its resources (style, graphics, etc.).
 */
public class VisionMain 
{
	
	
	public static void main(String[] args)
	{
		//Print a test line to make sure the program has
		System.out.println("testing123");

		
		//Create an instance of the controller and launch it.
		VisionController controller = new VisionController();
		controller.startCamera;
		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

	}
}
