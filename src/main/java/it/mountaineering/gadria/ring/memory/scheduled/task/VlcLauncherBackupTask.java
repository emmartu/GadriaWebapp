package it.mountaineering.gadria.ring.memory.scheduled.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import it.mountaineering.gadria.ring.memory.bean.FileWithCreationTime;
import it.mountaineering.gadria.ring.memory.bean.WebcamProperty;
import it.mountaineering.gadria.ring.memory.process.ProcessManager;
import it.mountaineering.gadria.ring.memory.util.CrunchifyThread;
import it.mountaineering.gadria.ring.memory.util.DiskSpaceManager;
import it.mountaineering.gadria.ring.memory.util.PropertiesManager;

public class VlcLauncherBackupTask implements Runnable{

	private static final java.util.logging.Logger log = Logger.getLogger(VlcLauncherScheduledTask.class.getName());
	private static final String VLC_VIDEO_RECORDER_BAT = PropertiesManager.getInstallationPath()+"VlcVideoRecorder.bat";
	private static final String TEST_BAT = PropertiesManager.getInstallationPath()+"test_write_and_sleep.bat";

	Date now;
	private boolean hasStarted = false;
	DiskSpaceManager diskSPaceManager;
	int webcamId;
	int videoLength;
	int lenghtOffset;
	WebcamProperty webcamProperty;
	
	public VlcLauncherBackupTask(VlcLauncherBackupProperties vlcLauncherBackupProperties) {
		this.diskSPaceManager = vlcLauncherBackupProperties.getDiskSPaceManager();
		this.webcamId = vlcLauncherBackupProperties.getWebcamId();
		this.videoLength = vlcLauncherBackupProperties.getVideoLenght();
		this.webcamProperty = vlcLauncherBackupProperties.getWebcamProperty();
		this.lenghtOffset = vlcLauncherBackupProperties.getLenghtOffset();
	}
	
	public void run() {
		now = new Date(); // initialize date
		String tName = Thread.currentThread().getName();
		log.info("Vlc Launcher Backup Scheduled Task start! Date: "+now);

		String absoluteStorageFolder = PropertiesManager.getVideoAbsoluteStorageFolder();			

			log.info("Time is :" + now+ " webcam "+webcamId+" - enabled: "+webcamProperty.isEnabled()+" - IP: "+webcamProperty.getIp()+" - folder: "+webcamProperty.getVideoRelativeStorageFolder());

			String relativeStorageFolder = webcamProperty.getVideoRelativeStorageFolder();
			absoluteStorageFolder = checkSlashesOnPath(absoluteStorageFolder);
			relativeStorageFolder = checkSlashesOnPath(relativeStorageFolder);
			String timeStamp = new SimpleDateFormat("yyyy-MM-dd@HH-mm-ss.S").format(new Date());
			String fileName = webcamId+"_"+timeStamp+"_backup.mp4";

			String storageFolderFullPath = absoluteStorageFolder + relativeStorageFolder;
			checkFolder(storageFolderFullPath);

			String storageFileFullPath = absoluteStorageFolder + relativeStorageFolder + fileName;

			long latestFileCreationTime = System.currentTimeMillis();
			
			String videoLanExePath = PropertiesManager.getVideoLanExePath();
			String exec_1 = "cmd /c start /B \"\" "+VLC_VIDEO_RECORDER_BAT+" "+webcamProperty.getiD()+" "+webcamProperty.getIp()+" "+storageFileFullPath+" "+videoLength+" \""+videoLanExePath+"\" ";
			
			try {
				ProcessManager.createProcess(tName, CrunchifyThread.timeOut, exec_1);				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			FileWithCreationTime fileWithCreationTime = new FileWithCreationTime(storageFileFullPath, latestFileCreationTime);
	}


	private String checkSlashesOnPath(String folderPath) {
		if (!folderPath.endsWith("\\")) {
			folderPath += "\\";
		}
		
		return folderPath;
	}

	private void checkFolder(String storageFolderFullPath) {
		File directory = new File(storageFolderFullPath);
		if (!directory.exists()||!directory.isDirectory()) {
			directory.mkdir();	
		}
	}

	public boolean isHasStarted() {
		return hasStarted;
	}

	public void setHasStarted(boolean hasStarted) {
		this.hasStarted = hasStarted;
	}
}
