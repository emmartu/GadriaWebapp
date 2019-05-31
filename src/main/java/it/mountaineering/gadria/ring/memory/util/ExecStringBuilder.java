package it.mountaineering.gadria.ring.memory.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import it.mountaineering.gadria.ring.memory.bean.WebcamProperty;

public class ExecStringBuilder {
	private static final String VLC_VIDEO_RECORDER_BAT = PropertiesManager.getInstallationPath()
			+ "VlcVideoRecorder.bat";
	private static final String TEST_BAT = PropertiesManager.getInstallationPath() + "test_write_and_sleep.bat";

	public static String getStorageFileFullPath(WebcamProperty webcamProperty, boolean isBackup) {

		String absoluteStorageFolder = PropertiesManager.getVideoAbsoluteStorageFolder();
		String relativeStorageFolder = webcamProperty.getVideoRelativeStorageFolder();
		absoluteStorageFolder = checkSlashesOnPath(absoluteStorageFolder);
		relativeStorageFolder = checkSlashesOnPath(relativeStorageFolder);
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd@HH-mm-ss.S").format(new Date());

		if (isBackup) {
			timeStamp = timeStamp + "_backup";
		}

		String fileName = webcamProperty.getiD() + "_" + timeStamp + ".mp4";

		String storageFolderFullPath = absoluteStorageFolder + relativeStorageFolder;
		checkFolder(storageFolderFullPath);

		String storageFileFullPath = absoluteStorageFolder + relativeStorageFolder + fileName;

		return storageFileFullPath;
	}

	private static String checkSlashesOnPath(String folderPath) {
		if (!folderPath.endsWith("\\")) {
			folderPath += "\\";
		}

		return folderPath;
	}

	private static void checkFolder(String storageFolderFullPath) {
		File directory = new File(storageFolderFullPath);
		if (!directory.exists() || !directory.isDirectory()) {
			directory.mkdir();
		}
	}

	public static String getExecStringWithStorageFileFullPath(WebcamProperty webcamProperty, Long videoLengthSeconds,
			boolean isBackup, String storageFileFullPath) {

		String videoLanExePath = PropertiesManager.getVideoLanExePath();

		String exec = "cmd /c start /B \"\" " + VLC_VIDEO_RECORDER_BAT + " " + webcamProperty.getiD() + " "
				+ webcamProperty.getIp() + " " + storageFileFullPath + " " + videoLengthSeconds + " \""
				+ videoLanExePath + "\" ";

		return exec;
	}
}
