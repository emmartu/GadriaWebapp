package it.mountaineering.gadria.ring.memory.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import it.mountaineering.gadria.ring.memory.bean.WebcamProperty;
import it.mountaineering.gadria.ring.memory.exception.BooleanStringPropertyException;
import it.mountaineering.gadria.ring.memory.exception.CSVFormatPropertiesException;
import it.mountaineering.gadria.ring.memory.exception.NumberFormatPropertiesException;
import it.mountaineering.gadria.ring.memory.exception.PropertiesException;
import it.mountaineering.gadria.ring.memory.exception.UnreachableIpException;
import it.mountaineering.gadria.ring.memory.exception.WebcamPropertyIDException;

public class PropertiesManager {

	private static final java.util.logging.Logger log = Logger.getLogger(PropertiesManager.class.getName());

	private static final String CONFIG_PROPERTIES = "config.properties";
	private static final String VIDEO_ABSOLUTE_STORAGE_FOLDER = "VideoAbsoluteStorageFolder";
	private static final String FREEZED_VIDEO_ABSOLUTE_STORAGE_FOLDER = "FreezedVideoAbsoluteStorageFolder";
	private static final String PICTURE_ABSOLUTE_STORAGE_FOLDER = "PictureAbsoluteStorageFolder";
	private static final String VIDEO_MAX_DISK_SPACE = "VideoMaxDiskSpace";
	private static final String PICTURE_MAX_DISK_SPACE = "PictureMaxDiskSpace";
	private static final String WEBCAM_ID = "WebCams";
	private static final String VIDEO_LENGTH = "VideoLength";
	private static final String OVERLAP = "Overlap";
	private static final String PICTURE_INTERVAL = "PictureInterval";
	private static final String _ENABLED = "_enabled";
	private static final String _VIDEO_RELATIVE_STORE = "_videoRelativeStore";
	private static final String _PICTURE_RELATIVE_STORE = "_pictureRelativeStore";
	private static final String _IP = "_ip";
	private static final String TRUE_STRING = "true";
	private static final String FALSE_STRING = "false";
	private static final String VIDEO_CAPTURE_ENABLED = "VideoCaptureEnabled";
	private static final String PICTURE_CAPTURE_ENABLED = "PictureCaptureEnabled";
	private static final String VIDEOLAN_EXE_PATH = "VideoLanExePath";

	protected static Map<String, String> propertiesMap = new HashMap<String, String>();
	protected static Map<String, WebcamProperty> webcamPropertiesMap = new HashMap<String, WebcamProperty>();
	protected static Map<String, WebcamProperty> enabledWebcamPropertiesMap = new HashMap<String, WebcamProperty>();
	protected static Map<String, Boolean> webcamErrorMap = new HashMap<String, Boolean>();
	protected static String[] webcamArray;
	protected static boolean checkIp = true;

	private static Properties prop = new Properties();
	private static InputStream input = null;
	private static String installationPath = "";


	protected static void setConfigFile(String configFile) {
		try {
			input = new FileInputStream(configFile);
			prop.clear();
			prop.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void setConfigFilePath(String installationPath) {
		try {
			setInstallationPath(installationPath);
			input = new FileInputStream(installationPath + CONFIG_PROPERTIES);
			prop.clear();
			prop.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void setupConfigProperties() throws PropertiesException {
		log.info("**** Get Properties From config.properties ****");

		String videoCaptureEnabled = getVideoCaptureEnabledFromConfigProperties();
		log.info("VideoCaptureEnabled: " + videoCaptureEnabled);
		propertiesMap.put(VIDEO_CAPTURE_ENABLED, videoCaptureEnabled);

		String videoLanExePath = getVideoLanExePathFromConfigProperties();
		log.info("videoLanExePath: " + videoLanExePath);
		propertiesMap.put(VIDEOLAN_EXE_PATH, videoLanExePath);

		String pictureCaptureEnabled = getPictureCaptureEnabledFromConfigProperties();
		log.info("PictureCaptureEnabled: " + pictureCaptureEnabled);
		propertiesMap.put(PICTURE_CAPTURE_ENABLED, pictureCaptureEnabled);

		String videoAbsoluteStorageFolder = getVideoAbsoluteStorageFolderFromConfigProperties();
		log.info("VideoAbsoluteStorageFolder: " + videoAbsoluteStorageFolder);
		propertiesMap.put(VIDEO_ABSOLUTE_STORAGE_FOLDER, videoAbsoluteStorageFolder);

		String freezedVideoAbsoluteStorageFolder = getFreezedVideoAbsoluteStorageFolderFromConfigProperties();
		log.info("FreezedVideoAbsoluteStorageFolder: " + freezedVideoAbsoluteStorageFolder);
		propertiesMap.put(FREEZED_VIDEO_ABSOLUTE_STORAGE_FOLDER, freezedVideoAbsoluteStorageFolder);

		String pictureAbsoluteStorageFolder = getPictureAbsoluteStorageFolderFromConfigProperties();
		log.info("PictureAbsoluteStorageFolder: " + pictureAbsoluteStorageFolder);
		propertiesMap.put(PICTURE_ABSOLUTE_STORAGE_FOLDER, pictureAbsoluteStorageFolder);

		Long videoMaxDiskSpace = getVideoMaxDiskSpaceFromConfigProperties();
		log.info("VideoMaxDiskSpace: " + videoMaxDiskSpace);
		propertiesMap.put(VIDEO_MAX_DISK_SPACE, String.valueOf(videoMaxDiskSpace));

		Long pictureMaxDiskSpace = getPictureMaxDiskSpaceFromConfigProperties();
		log.info("PictureMaxDiskSpace: " + pictureMaxDiskSpace);
		propertiesMap.put(PICTURE_MAX_DISK_SPACE, String.valueOf(pictureMaxDiskSpace));

		Long videoLength = getVideoLengthFromConfigProperties();
		log.info("VideoLength: " + videoLength);
		propertiesMap.put(VIDEO_LENGTH, String.valueOf(videoLength));

		Long overlap = getOverlapFromConfigProperties();
		log.info("Overlap: " + overlap);
		propertiesMap.put(OVERLAP, String.valueOf(overlap));

		Long pictureInterval = getPictureIntervalFromConfigProperties();
		log.info("PictureInterval: " + pictureInterval);
		propertiesMap.put(PICTURE_INTERVAL, String.valueOf(pictureInterval));

		try {
			webcamArray = getWebcamNamesFromConfigProperties();
		} catch (CSVFormatPropertiesException e1) {
			log.severe("error occured reading webcam names property");
			e1.printStackTrace();
		}

		log.info("webcamArray: " + Arrays.toString(webcamArray));

		for (int i = 0; i < webcamArray.length; i++) {
			WebcamProperty webcamProperty = null;
			try {
				webcamProperty = getWebcamPropertyFromConfigPropertiesById(webcamArray[i]);
			} catch (WebcamPropertyIDException e) {
				webcamErrorMap.put(webcamArray[i], true);
				log.severe("error occured reading webcam " + webcamArray[i] + " property ");
				continue;
			}

			webcamPropertiesMap.put(webcamArray[i], webcamProperty);
			if (webcamProperty.isEnabled()) {
				enabledWebcamPropertiesMap.put(webcamArray[i], webcamProperty);
			}
		}

		if (!isStartAllowed()) {
			log.severe("error occured reading webcam properties on each of the enabled webcams");
			throw new PropertiesException("Cannot Read Properties correctly from each Webcams");
		}

		log.info("********* Properties setup Complete ***********");
	}

	private static boolean isStartAllowed() {
		for (String webcamName : enabledWebcamPropertiesMap.keySet()) {
			if (!(webcamErrorMap.get(webcamName) != null && webcamErrorMap.get(webcamName) == true)) {
				return true;
			}
		}

		return false;
	}

	private static String getStringPropertyByName(String propertyName) throws PropertiesException {
		String propertyStr = "";
		propertyStr = prop.getProperty(propertyName);

		if (propertyStr == null || propertyStr.equalsIgnoreCase("")) {
			throw new PropertiesException("Cannot Read Property " + propertyName);
		}

		return propertyStr;
	}

	private static long getNumberPropertyByName(String propertyName)
			throws PropertiesException, NumberFormatPropertiesException {
		String propertyStr;

		propertyStr = prop.getProperty(propertyName);

		if (propertyStr == null || propertyStr.equalsIgnoreCase("")) {
			throw new PropertiesException("Cannot Read Property " + propertyName + ", property is null");
		}

		long propertyNumber;
		try {
			propertyNumber = Long.parseLong(propertyStr);
		} catch (NumberFormatException e) {
			throw new NumberFormatPropertiesException("Cannot Read Property " + propertyName + ", malformed number");
		}

		return propertyNumber;
	}

	private static String getVideoLanExePathFromConfigProperties() throws PropertiesException {
		String videoLanExePath = "";

		videoLanExePath = getStringPropertyByName(VIDEOLAN_EXE_PATH);

		return videoLanExePath;
	}

	protected static String getVideoAbsoluteStorageFolderFromConfigProperties() throws PropertiesException {
		String storageFolder = "";

		storageFolder = getStringPropertyByName(VIDEO_ABSOLUTE_STORAGE_FOLDER);

		File storageDirectory = new File(storageFolder);

		if (!storageDirectory.exists() || !storageDirectory.isDirectory()) {
			throw new PropertiesException(
					"VideoAbsoluteStorageFolder property cannot access Directory " + storageFolder);
		}

		return storageFolder;
	}

	protected static String getFreezedVideoAbsoluteStorageFolderFromConfigProperties() throws PropertiesException {
		String storageFolder = "";

		storageFolder = getStringPropertyByName(FREEZED_VIDEO_ABSOLUTE_STORAGE_FOLDER);

		File storageDirectory = new File(storageFolder);

		if (!storageDirectory.exists() || !storageDirectory.isDirectory()) {
			throw new PropertiesException(
					"VideoAbsoluteStorageFolder property cannot access Directory " + storageFolder);
		}

		return storageFolder;
	}


	private static String getPictureAbsoluteStorageFolderFromConfigProperties() throws PropertiesException {
		String storageFolder = "";

		storageFolder = getStringPropertyByName(PICTURE_ABSOLUTE_STORAGE_FOLDER);

		File storageDirectory = new File(storageFolder);

		if (!storageDirectory.exists() || !storageDirectory.isDirectory()) {
			throw new PropertiesException(
					"PictureAbsoluteStorageFolder property cannot access Directory " + storageFolder);
		}

		return storageFolder;
	}

	public static String getVideoLanExePath() {
		return propertiesMap.get(VIDEOLAN_EXE_PATH);
	}

	public static String getVideoAbsoluteStorageFolder() {
		return propertiesMap.get(VIDEO_ABSOLUTE_STORAGE_FOLDER);
	}

	public static String getFreezedVideoAbsoluteStorageFolder() {
		return propertiesMap.get(FREEZED_VIDEO_ABSOLUTE_STORAGE_FOLDER);
	}

	public static String getPictureAbsoluteStorageFolder() {
		return propertiesMap.get(PICTURE_ABSOLUTE_STORAGE_FOLDER);
	}

	protected static Long getVideoMaxDiskSpaceFromConfigProperties()
			throws PropertiesException, NumberFormatPropertiesException {
		long diskSpace = 0L;

		diskSpace = getNumberPropertyByName(VIDEO_MAX_DISK_SPACE);

		return diskSpace;
	}

	protected static Long getPictureMaxDiskSpaceFromConfigProperties()
			throws PropertiesException, NumberFormatPropertiesException {
		long diskSpace = 0L;

		diskSpace = getNumberPropertyByName(PICTURE_MAX_DISK_SPACE);

		return diskSpace;
	}

	public static Long getPictureMaxDiskSpace() {
		String pictureMaxDiskSpaceStr = propertiesMap.get(PICTURE_MAX_DISK_SPACE);

		Long diskSpace = Long.parseLong(pictureMaxDiskSpaceStr);

		return diskSpace;
	}

	public static Long getVideoMaxDiskSpace() {
		String videoMaxDiskSpaceStr = propertiesMap.get(VIDEO_MAX_DISK_SPACE);

		Long diskSpace = Long.parseLong(videoMaxDiskSpaceStr);

		return diskSpace;
	}

	protected static String[] getWebcamNamesFromConfigProperties()
			throws PropertiesException, CSVFormatPropertiesException {
		String webcams = "";

		webcams = getStringPropertyByName(WEBCAM_ID);

		String[] webcamArray = null;

		webcamArray = webcams.split(",", -1);
		for (String webcamName : webcamArray) {
			if (webcamName == null || webcamName.trim() == "" || webcamName.isEmpty()) {
				throw new CSVFormatPropertiesException("Cannot Read correct CSV on Property WebCams");
			}
		}

		return webcamArray;
	}

	public static Map<String, WebcamProperty> getEnabledWebcamPropertiesMap() {
		return enabledWebcamPropertiesMap;
	}

	public static void setEnabledWebcamPropertiesMap(Map<String, WebcamProperty> enabledWebcamPropertiesMap) {
		PropertiesManager.enabledWebcamPropertiesMap = enabledWebcamPropertiesMap;
	}

	public static String[] getWebcamNames() {
		return webcamArray;
	}

	protected static Long getVideoLengthFromConfigProperties()
			throws NumberFormatPropertiesException, PropertiesException {
		long videoLength = 0L;

		videoLength = getNumberPropertyByName(VIDEO_LENGTH);

		return videoLength;
	}

	public static Long getVideoLength() {
		String videoLengthStr = propertiesMap.get(VIDEO_LENGTH);

		Long videoLength = Long.parseLong(videoLengthStr);

		return videoLength;
	}

	protected static Long getOverlapFromConfigProperties() throws NumberFormatPropertiesException, PropertiesException {
		long overlap = 0L;

		overlap = getNumberPropertyByName(OVERLAP);

		return overlap;
	}

	public static Long getOverlap() {
		String overlapStr = propertiesMap.get(OVERLAP);

		Long overlap = Long.parseLong(overlapStr);

		return overlap;
	}

	protected static Long getPictureIntervalFromConfigProperties()
			throws NumberFormatPropertiesException, PropertiesException {
		long pictureInterval = 0L;

		pictureInterval = getNumberPropertyByName(PICTURE_INTERVAL);

		return pictureInterval;
	}

	public static Long getPictureInterval() {
		String pictureIntervalStr = propertiesMap.get(PICTURE_INTERVAL);

		Long pictureInterval = Long.parseLong(pictureIntervalStr);

		return pictureInterval;
	}

	private static String getPictureCaptureEnabledFromConfigProperties() throws WebcamPropertyIDException {
		String enabledStr;

		enabledStr = prop.getProperty(PICTURE_CAPTURE_ENABLED);
		if (enabledStr == null || enabledStr.equalsIgnoreCase("")) {
			throw new WebcamPropertyIDException(
					"Cannot Read Property " + PICTURE_CAPTURE_ENABLED + ", property is null");
		}

		if (!isValidBooleanString(enabledStr)) {
			throw new BooleanStringPropertyException("");
		}

		return enabledStr;
	}

	public static boolean isPictureCaptureEnabled() {
		String pictureCaptureEnabledStr = propertiesMap.get(PICTURE_CAPTURE_ENABLED);

		boolean pictureCaptureEnabled = Boolean.parseBoolean(pictureCaptureEnabledStr);

		return pictureCaptureEnabled;
	}

	private static String getVideoCaptureEnabledFromConfigProperties() throws WebcamPropertyIDException {
		String enabledStr;

		enabledStr = prop.getProperty(VIDEO_CAPTURE_ENABLED);
		if (enabledStr == null || enabledStr.equalsIgnoreCase("")) {
			throw new WebcamPropertyIDException("Cannot Read Property " + VIDEO_CAPTURE_ENABLED + ", property is null");
		}

		if (!isValidBooleanString(enabledStr)) {
			throw new BooleanStringPropertyException("");
		}

		return enabledStr;
	}

	public static boolean isVideoCaptureEnabled() {
		String videoCaptureEnabledStr = propertiesMap.get(VIDEO_CAPTURE_ENABLED);

		boolean videoCaptureEnabled = Boolean.parseBoolean(videoCaptureEnabledStr);

		return videoCaptureEnabled;
	}

	public static WebcamProperty getWebcamPropertyById(String webcamId) {
		WebcamProperty webcamProperty = webcamPropertiesMap.get(webcamId);

		return webcamProperty;
	}

	public static Map<String, WebcamProperty> getEnabledWebcam() {

		return enabledWebcamPropertiesMap;
	}

	protected static WebcamProperty getWebcamPropertyFromConfigPropertiesById(String webcamId)
			throws WebcamPropertyIDException, UnreachableIpException {
		WebcamProperty webcamProperty = new WebcamProperty();

		webcamProperty.setiD(webcamId);

		String enabledStr;

		enabledStr = prop.getProperty(webcamId + _ENABLED);
		if (enabledStr == null || enabledStr.equalsIgnoreCase("")) {
			throw new WebcamPropertyIDException("Cannot Read Property " + webcamId + _ENABLED + ", property is null");
		}

		boolean enabled = false;

		if (!isValidBooleanString(enabledStr)) {
			throw new BooleanStringPropertyException("");
		}

		enabled = Boolean.parseBoolean(enabledStr);
		webcamProperty.setEnabled(enabled);

		String videoRelativeStore = "";
		videoRelativeStore = prop.getProperty(webcamId + _VIDEO_RELATIVE_STORE);
		if (videoRelativeStore == null || videoRelativeStore.equalsIgnoreCase("")) {
			throw new WebcamPropertyIDException(
					"Cannot read Property " + webcamId + _VIDEO_RELATIVE_STORE + ", property is null");
		}

		webcamProperty.setVideoRelativeStorageFolder(videoRelativeStore);

		String pictureRelativeStore = "";
		pictureRelativeStore = prop.getProperty(webcamId + _PICTURE_RELATIVE_STORE);
		if (pictureRelativeStore == null || pictureRelativeStore.equalsIgnoreCase("")) {
			throw new WebcamPropertyIDException(
					"Cannot read Property " + webcamId + _PICTURE_RELATIVE_STORE + ", property is null");
		}

		webcamProperty.setPictureRelativeStorageFolder(pictureRelativeStore);

		String webcamIP = "";
		webcamIP = prop.getProperty(webcamId + _IP);

		if (webcamIP == null || webcamIP.equalsIgnoreCase("")) {
			throw new WebcamPropertyIDException("Cannot read Property " + webcamId + _IP + ", property is null");
		}

		if (checkIp && enabled && !isIPOnline(webcamIP)) {
			throw new UnreachableIpException("Unable to reach " + webcamIP + " for webcam " + webcamId);
		}

		webcamProperty.setIp(webcamIP);

		return webcamProperty;
	}

	public static String getInstallationPath() {
		return installationPath;
	}

	public static void setInstallationPath(String installationPath) {
		PropertiesManager.installationPath = installationPath;
	}

	private static boolean isIPOnline(String webcamIP) {

		if (PingIp.isPingReachable(webcamIP)) {
			return true;
		}

		return false;
	}

	private static boolean isValidBooleanString(String enabledStr) {
		if (enabledStr.equalsIgnoreCase(TRUE_STRING) || enabledStr.equalsIgnoreCase(FALSE_STRING)) {
			return true;
		}

		return false;
	}

	public static boolean isCheckIp() {
		return checkIp;
	}

	public static void setCheckIp(boolean checkIp) {
		PropertiesManager.checkIp = checkIp;
	}
}