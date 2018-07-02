package it.mountaineering.gadria.mvc.controller;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import it.mountaineering.gadria.ring.memory.bean.DiskSpaceProperties;
import it.mountaineering.gadria.ring.memory.bean.FileWithCreationTime;
import it.mountaineering.gadria.ring.memory.scheduled.task.CurrentPictureTakerTask;
import it.mountaineering.gadria.ring.memory.scheduled.task.VlcLauncherScheduledTask;

@Controller
public class MainPagesController {

	/**
     * Size of a byte buffer to read/write file
     */
    private static final int BUFFER_SIZE = 4096;
             
    /**
     * Path of the file to be downloaded, relative to application's directory
     */
    private String filePath = "/downloads/SpringProject.zip";
  
	@RequestMapping("/welcome")
	public ModelAndView helloWorld() {
 
		String message = "<br><div style='text-align:center;'>"
				+ "<h3>********** Hello World, Spring MVC Tutorial</h3>This message is coming from CrunchifyHelloWorld.java **********</div><br><br>";
		return new ModelAndView("welcome", "message", message);
	}

	@RequestMapping("/download")
	public ModelAndView download() {
 
		DiskSpaceProperties diskSpaceProperties = VlcLauncherScheduledTask.diskSPaceManager.getFileCounter();
		
		return new ModelAndView("download", "diskSpaceProperties", diskSpaceProperties);
	}
	
	@RequestMapping(value = "/latest-image/{webcamId}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getImageAsResponseEntity(@PathVariable("webcamId") String webcamId) {
	    HttpHeaders headers = new HttpHeaders();
	    FileWithCreationTime latestPictureFile = CurrentPictureTakerTask.getLatestPicture(webcamId);
	    String latestPictureFilePath = latestPictureFile.getFile().getAbsolutePath();
	    InputStream inputStream = null;
		try {
			inputStream = new BufferedInputStream(new FileInputStream(latestPictureFilePath));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
	    byte[] media = getByteArrayImage(inputStream);
		
		headers.setCacheControl("no-cache, no-store, must-revalidate");
	    headers.setPragma("no-cache");
	    headers.setExpires(0);
	    
	    ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>(media, headers, HttpStatus.OK);
	    return responseEntity;
	}
	
	@RequestMapping(value = "/image-void-response", method = RequestMethod.GET)
	public void getImageAsByteArray(HttpServletResponse response) throws IOException {
	    InputStream in = getImageInputStream();
	    response.setContentType(MediaType.IMAGE_JPEG_VALUE);
	    IOUtils.copy(in, response.getOutputStream());
	}

	private byte[] getByteArrayImage(InputStream inputStream) {
	    byte[] media = null;		
	    
	    try {
			media = IOUtils.toByteArray(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

	    return media;
	}

	private byte[] getByteArrayImage() {
	    byte[] media = null;		
	    
	    try {
			InputStream in = getImageInputStream();
			media = IOUtils.toByteArray(in);
		} catch (IOException e) {
			e.printStackTrace();
		}

	    return media;
	}

	public InputStream getImageInputStream() {
	    InputStream in = null;
	    
		try {
			in = new BufferedInputStream(new FileInputStream("C:\\Users\\Lele\\Documents\\LavoroWebCamMobotix\\TEST\\w1_2018-06-27@19-27-44.jpg"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return in;
	}
	
	
}
