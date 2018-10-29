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
import java.util.TimerTask;
import java.util.logging.Logger;

import it.mountaineering.gadria.ring.memory.bean.FileWithCreationTime;
import it.mountaineering.gadria.ring.memory.bean.WebcamProperty;
import it.mountaineering.gadria.ring.memory.util.DiskSpaceManager;
import it.mountaineering.gadria.ring.memory.util.PropertiesManager;

public class VlcLauncherScheduledTask extends TimerTask {

	private static final java.util.logging.Logger log = Logger.getLogger(VlcLauncherScheduledTask.class.getName());
	private static final String VLC_VIDEO_RECORDER_BAT = PropertiesManager.getInstallationPath()+"VlcVideoRecorder.bat";
	private static final String TEST_BAT = PropertiesManager.getInstallationPath()+"test_write_and_sleep.bat";
	public static DiskSpaceManager diskSPaceManager;
	
	{
		diskSPaceManager = new DiskSpaceManager(PropertiesManager.getVideoAbsoluteStorageFolder(), PropertiesManager.getVideoMaxDiskSpace(), "Video Recorder");
		log.info("init Vlc Launcher Scheduled Task");
		checkMemory();		
	}

	Date now;
	private boolean hasStarted = false;
	private static List<FileWithCreationTime> latestFileList;
	
	public void run() {
		now = new Date(); // initialize date
		log.info("Vlc Launcher Scheduled Task start! Date: "+now);
		this.hasStarted = true;
		initLatestFileList();

		Map<String,WebcamProperty> enabledWebcamPropertiesMap = PropertiesManager.getEnabledWebcamPropertiesMap();

		String absoluteStorageFolder = PropertiesManager.getVideoAbsoluteStorageFolder();
		Long videoLength = 0L;
		videoLength = PropertiesManager.getVideoLength();

		for (String webcamId : enabledWebcamPropertiesMap.keySet()){
			
			if(latestFileList.size()==2) {
				FileWithCreationTime fileWithCreationTime = latestFileList.remove(0);
				diskSPaceManager.addLatestFile(fileWithCreationTime);

				checkMemory();
			}
			

			WebcamProperty webcamProperty = enabledWebcamPropertiesMap.get(webcamId);
			log.info("Time is :" + now+ " webcam "+webcamId+" - enabled: "+webcamProperty.isEnabled()+" - IP: "+webcamProperty.getIp()+" - folder: "+webcamProperty.getVideoRelativeStorageFolder());

			String relativeStorageFolder = webcamProperty.getVideoRelativeStorageFolder();
			absoluteStorageFolder = checkSlashesOnPath(absoluteStorageFolder);
			relativeStorageFolder = checkSlashesOnPath(relativeStorageFolder);
			String timeStamp = new SimpleDateFormat("yyyy-MM-dd@HH-mm-ss.S").format(new Date());
			String fileName = webcamId+"_"+timeStamp+".mp4";

			String storageFolderFullPath = absoluteStorageFolder + relativeStorageFolder;
			checkFolder(storageFolderFullPath);

			String storageFileFullPath = absoluteStorageFolder + relativeStorageFolder + fileName;

			long latestFileCreationTime = System.currentTimeMillis();
			
			String videoLanExePath = PropertiesManager.getVideoLanExePath();
			String exec_1 = "cmd /c start /B \"\" "+VLC_VIDEO_RECORDER_BAT+" "+webcamProperty.getiD()+" "+webcamProperty.getIp()+" "+storageFileFullPath+" "+videoLength+" \""+videoLanExePath+"\" ";
			String exec_2 = "cmd /c start /B \"\" "+TEST_BAT+" "+webcamProperty.getiD()+" "+webcamProperty.getIp()+" "+storageFileFullPath+" "+videoLength+" \""+videoLanExePath+"\" ";
			
			try {
				Process proc = 
						Runtime.
				   getRuntime().
				   exec(exec_1);
				
				StreamGobbler err = new StreamGobbler( proc.getErrorStream(), "err");
				StreamGobbler out = new StreamGobbler( proc.getInputStream(), "out");
				err.start();
				out.start();

			} catch (Exception e) {
				log.severe("exception occurred grabbing video");
				e.printStackTrace();
			}

			FileWithCreationTime fileWithCreationTime = new FileWithCreationTime(storageFileFullPath, latestFileCreationTime);
			latestFileList.add(fileWithCreationTime);

		}
	}

	private void checkMemory() {
		while(!diskSPaceManager.hasEnoughMemory()) {
			diskSPaceManager.deleteOldestFilesFromMemory();
		}
	}

	private static void initLatestFileList() {
		if (latestFileList==null) {
			latestFileList = new ArrayList<FileWithCreationTime>();
		}
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

class StreamGobbler extends Thread {
	private static final java.util.logging.Logger log = Logger.getLogger(StreamGobbler.class.getName());
	public StreamGobbler( InputStream in, String type) {
		this.in = in;
		this.type = type;
	}
	private InputStream in;
	private String type;

	public void run() {
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader( in));
			String line = null;
			while (( line = reader.readLine()) != null) {
				log.fine( type + ": " + line);
			}
		} catch ( IOException e) {
			e.printStackTrace();
		}
	}
}

