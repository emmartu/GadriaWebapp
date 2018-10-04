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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import it.mountaineering.gadria.ring.memory.bean.FileWithCreationTime;
import it.mountaineering.gadria.ring.memory.bean.WebcamProperty;
import it.mountaineering.gadria.ring.memory.scheduled.task.CurrentPictureTakerTask;
import it.mountaineering.gadria.ring.memory.scheduled.task.VlcLauncherScheduledTask;
import it.mountaineering.gadria.ring.memory.util.PropertiesManager;

@Controller
public class MainPagesController {

	public static final SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmm");

	@RequestMapping("/download")
	public ModelAndView download() {
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


	@RequestMapping(method = RequestMethod.GET, value = "/freezing/video/from/{from}/to/{to}")
	public @ResponseBody
	List<String> getVideoFromDateToDate(HttpServletRequest request, HttpServletResponse response, HttpSession session,
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

		return freezingVideoLog;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/properties/videoabsolutestoragefolder")
	public @ResponseBody
	String getVideoAbsoluteStorageFolder(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

		String videoAbsoluteStorageFolder = PropertiesManager.getVideoAbsoluteStorageFolder();

		return videoAbsoluteStorageFolder;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/properties/freezedvideoabsolutestoragefolder")
	public @ResponseBody
	String getFreezedVideoAbsoluteStorageFolder(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

		String freezedVideoAbsoluteStorageFolder = PropertiesManager.getFreezedVideoAbsoluteStorageFolder();
		
		return freezedVideoAbsoluteStorageFolder;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/properties/videomaxdiskspace")
	public @ResponseBody
	Long getVideoMaxDiskSpace(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

		Long videoMaxDiskSpace = PropertiesManager.getVideoMaxDiskSpace();

		return videoMaxDiskSpace;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/properties/videolength")
	public @ResponseBody
	Long getVideoLength(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

		Long videoLength = PropertiesManager.getVideoLength();

		return videoLength;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/properties/overlap")
	public @ResponseBody
	Long getOverlap(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

		Long overlap = PropertiesManager.getOverlap();

		return overlap;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/properties/pictureinterval")
	public @ResponseBody
	Long getPictureInterval(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

		Long pictureInterval = PropertiesManager.getPictureInterval();

		return pictureInterval;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/properties/pictureabsolutestoragefolder")
	public @ResponseBody
	String getPictureAbsoluteStorageFolder(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

		String pictureAbsoluteStorageFolder = PropertiesManager.getPictureAbsoluteStorageFolder();
		
		return pictureAbsoluteStorageFolder;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/properties/picturemaxdiskspace")
	public @ResponseBody
	Long getPictureMaxDiskSpace(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

		Long pictureMaxDiskSpace = PropertiesManager.getPictureMaxDiskSpace();

		return pictureMaxDiskSpace;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/properties/videoLanexepath")
	public @ResponseBody
	String getVideoLanExePath(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

		String videoLanExePath = PropertiesManager.getVideoLanExePath();
		
		return videoLanExePath;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/properties/WebCams")
	public @ResponseBody
	List<String> getWebCams(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

		Map<String, WebcamProperty> enabledWebcamPropertiesMap = PropertiesManager.getEnabledWebcamPropertiesMap();
		
		List<String> webcamIdList = new ArrayList<String>();
		webcamIdList.addAll(enabledWebcamPropertiesMap.keySet());

		return webcamIdList;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/properties/enabledwebcams")
	public @ResponseBody
	Map<String, WebcamProperty> getEnabledWebcams(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

		Map<String, WebcamProperty> enabledWebcamPropertiesMap = PropertiesManager.getEnabledWebcamPropertiesMap();

		return enabledWebcamPropertiesMap;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/properties/webcamIdList")
	public @ResponseBody
	List<String> getWebcamIdList(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

		Map<String, WebcamProperty> enabledWebcamPropertiesMap = PropertiesManager.getEnabledWebcamPropertiesMap();
		
		List<String> webcamIdList = new ArrayList<String>();
		webcamIdList.addAll(enabledWebcamPropertiesMap.keySet());

		return webcamIdList;
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
