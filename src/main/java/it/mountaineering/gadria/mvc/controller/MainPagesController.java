package it.mountaineering.gadria.mvc.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import it.mountaineering.gadria.ring.memory.bean.FileWithCreationTime;
import it.mountaineering.gadria.ring.memory.scheduled.task.CurrentPictureTakerTask;
import it.mountaineering.gadria.ring.memory.scheduled.task.VlcLauncherScheduledTask;

@Controller
public class MainPagesController {

	public static final SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmm");

	@RequestMapping("/welcome")
	public ModelAndView helloWorld() {

		String message = "<br><div style='text-align:center;'>"
				+ "<h3>********** Hello World, Spring MVC Tutorial</h3>This message is coming from CrunchifyHelloWorld.java **********</div><br><br>";
		return new ModelAndView("welcome", "message", message);
	}

	@RequestMapping("/download")
	public ModelAndView download() {

		//DiskSpaceProperties diskSpaceProperties = VlcLauncherScheduledTask.diskSPaceManager.getFileCounter();

		return new ModelAndView("download", "diskSpaceProperties", null);
	}

	@RequestMapping(value = "/downloadFreezed", method = RequestMethod.GET)
	public ResponseEntity<byte[]> downloadZipFile(HttpServletRequest request,
        HttpServletResponse response) throws IOException {
		HttpHeaders headers = new HttpHeaders();
	
		File downloadZipFile = VlcLauncherScheduledTask.diskSPaceManager.getDownloadZipFile();
		String downloadZipFilePath = downloadZipFile.getAbsolutePath();
		InputStream inputStream = null;
		
		try {
			inputStream = new BufferedInputStream(new FileInputStream(downloadZipFilePath));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		byte[] media = getByteArray(inputStream);

		headers.setCacheControl("no-cache, no-store, must-revalidate");
		headers.setPragma("no-cache");
		headers.setExpires(0);

		ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>(media, headers, HttpStatus.OK);
		return responseEntity;
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

		byte[] media = getByteArray(inputStream);

		headers.setCacheControl("no-cache, no-store, must-revalidate");
		headers.setPragma("no-cache");
		headers.setExpires(0);

		ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>(media, headers, HttpStatus.OK);
		return responseEntity;
	}


	@RequestMapping(method = RequestMethod.GET, value = "/testListC")
	public @ResponseBody
	void getStops(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		List<String> strList = new ArrayList<String>();
		strList.add("test1");
		strList.add("test2");
		strList.add("test3");
		strList.add("test4");

		try {

			response.setContentType("application/json; charset=utf-8");
			response.getWriter().write(strList.toString());
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
	}


	@Path("/video/{from}/{to}")
	@RequestMapping(method = RequestMethod.GET, value = "/freezing/video/{from}/{to}")
	public @ResponseBody
	void getVideoFromDateToDate(HttpServletRequest request, HttpServletResponse response, HttpSession session,
										   @PathVariable("from") String fromDatetime, @PathVariable("to") String toDatetime) {

		String output = "from : " + fromDatetime + " --> to: " + toDatetime;
		System.out.println("output: " + output);

		Date fromDateTimeClass = null;
		Date toDateTimeClass = null;

		try {
			fromDateTimeClass = format.parse(fromDatetime);
			toDateTimeClass = format.parse(toDatetime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<String> freezingVideoLog = VlcLauncherScheduledTask.diskSPaceManager.freezeFilesFromDateToDateFromMemory(fromDateTimeClass,
				toDateTimeClass);

		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().write(freezingVideoLog.toString());
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
	}

	private byte[] getByteArray(InputStream inputStream) {
		byte[] media = null;

		try {
			media = IOUtils.toByteArray(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return media;
	}


}
