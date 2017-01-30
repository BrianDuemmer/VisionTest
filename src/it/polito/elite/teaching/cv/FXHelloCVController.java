package it.polito.elite.teaching.cv;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import it.polito.elite.teaching.cv.utils.Utils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * The controller for our application, where the application logic is
 * implemented. It handles the button for starting/stopping the camera,
 * video processing, and the acquired video stream.
 */
public class FXHelloCVController
{
	// the FXML button
	@FXML
	private Button button;
	// the FXML image view
	@FXML
	private ImageView currentFrame;
	
	//Integers for thresholding values, used a little later
	int hueStart;
	int satStart;
	int valStart;
	int hueStop;
	int satStop;
	int valStop;
	
	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	// Does two things: Creates a videocatpure instance and designates it "capture".
	private VideoCapture capture = new VideoCapture();
	// a flag to change the button behavior
	private boolean cameraActive = false;
	
	//create properties of HSV values !Not implemented because I don't need to display HSV values atm!
	/**
	 * Method for Converting Mats back to frames.
	 * @param frame
	 * 			The {@link Mat} that represents the image to show in OpenCV
	 * @return The actual {@link Image} to show
	 */
	private Image mat2Image (Mat frame)
	{
		MatOfByte buffer = new MatOfByte();
		Highgui.imencode(".png", frame, buffer);
		return new Image (new ByteArrayInputStream(buffer.toArray()));
	}
	//Used in converting Mats back into images
	private <T> void onFXThread(final ObjectProperty<T> property, final T value)
	{
		Platform.runLater(new Runnable() {
			
			@Override
			public void run()
			{
				property.set(value);
			}
		});
	}
	
	
	/**
	 *Activates the camera feed and begins to display the image(s)
	 */
	@FXML
	protected void startCamera(ActionEvent event)
	{
		if (!this.cameraActive)
		{
			// start the video capture
//			this.capture.open("C:/test/test-mjpeg.mov");
			this.capture.open("http://10.2.23.26/mjpg/video.mjpg");
			
			
			// is the video stream available?
			if (this.capture.isOpened())
			{
				this.cameraActive = true;
				
				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = new Runnable() {
					
					@Override
					public void run()
					{
						// effectively grab and process a single frame
						Image imageToShow = grabFrame();
						// convert and show the frame
						updateImageView(currentFrame, imageToShow);
					}
				};
				
				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 50, TimeUnit.MILLISECONDS);
				
				// update the button content
				this.button.setText("Stop Camera");
			}
			else
			{
				// log the error
				System.err.println("Camera is unreachable");
			}
		}
		else
		{
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.button.setText("Start Camera");
			
			// stop the timer
			this.stopAcquisition();
		}
	}
	
	/**
	 * Get a frame from the opened video stream (if any)
	 *
	 * @return the {@link Mat} to show
	 */
	private Image grabFrame()
	{
		Image imageToShow = null;
		// create Mats
		Mat frame = new Mat();
		
		// check if the capture is open
		if (this.capture.isOpened())
		{
			try
			{
				// read the current frame
				this.capture.read(frame);
				if (!frame.empty())
				{
					Mat mask = new Mat();
				
				
				// if the frame is not empty, process it
				// the imgproc refers to what is to be done with the image, In this case convert color formats.
				//mat refers to the source image,
				//mat1 refers to the destination image. 
				//Mat1 and Mat2 must be frames that have been converted into matrices.
				//and the next imgproc states more specifically what is to be done to the image. 
				//In this case, change BGR color formated frames to an HSV format.
				//Syntax: Imgproc.cvtColor(mat1, mat2, int)
				Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2HSV);
				
				/**Get threshold values 
				 * Right now this is going to be static in the code
				 * Later I will add a function to change them externally, or
				 * even a piece of code to automatically generate them.
				 */
				Scalar min = new Scalar(hueStart = 85, satStart = 40, valStart = 180, 0);
				Scalar max = new Scalar(hueStop = 135, satStop = 255, valStop = 255, 255);
				
				//Implement the thresholding
				Core.inRange(frame, min, max, mask);
				//Show the mask
				this.onFXThread(this.currentFrame.imageProperty(), this.mat2Image(mask));
				//Convert a mat back to an image
				imageToShow = mat2Image(mask);
				}
			}
			catch (Exception e)
			{
				// log the error
				System.err.println("Exception during the image elaboration: " + e);
			}
		}
		
		return imageToShow;
	}
	
	/**
	 * Stop the acquisition from the camera and release all the resources
	 */
	private void stopAcquisition()
	{
		if (this.timer!=null && !this.timer.isShutdown())
		{
			try
			{
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}
		
		if (this.capture.isOpened())
		{
			// release the camera
			this.capture.release();
		}
	}
	
	/**
	 * Update the {@link ImageView} in the JavaFX main thread
	 * 
	 * @param view
	 *            the {@link ImageView} to update
	 * @param image
	 *            the {@link Image} to show
	 */
	private void updateImageView(ImageView view, Image image)
	{
		Utils.onFXThread(view.imageProperty(), image);
	}
	
	/**
	 * On application close, stop the acquisition from the camera
	 */
	protected void setClosed()
	{
		this.stopAcquisition();
	}
	
}
