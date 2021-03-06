package it.mountaineering.gadria.ring.memory.scheduled.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.logging.Logger;

import it.mountaineering.gadria.ring.memory.bean.FileWithCreationTime;
import it.mountaineering.gadria.ring.memory.bean.WebcamProperty;
import it.mountaineering.gadria.ring.memory.process.ProcessManager;
import it.mountaineering.gadria.ring.memory.util.DiskSpaceManager;
import it.mountaineering.gadria.ring.memory.util.ExecStringBuilder;
import it.mountaineering.gadria.ring.memory.util.PropertiesManager;

public class VlcLauncherScheduledTask extends TimerTask {

	private static final java.util.logging.Logger log = Logger.getLogger(VlcLauncherScheduledTask.class.getName());
	public static DiskSpaceManager diskSPaceManager;

	{
		diskSPaceManager = new DiskSpaceManager(PropertiesManager.getVideoAbsoluteStorageFolder(),
				PropertiesManager.getVideoMaxDiskSpace(), "Video Recorder");
		log.info("init Vlc Launcher Scheduled Task");
		checkMemory();
	}

	Date now;
	private boolean hasStarted = false;
	private static List<FileWithCreationTime> latestFileList;

	public void run() {
		String vlcLauncerThreadName = Thread.currentThread().getName();

		now = new Date(); // initialize date
		log.info("Vlc Launcher Scheduled Task start! Date: " + now.getTime());
		this.hasStarted = true;
		initLatestFileList();

		Map<String, WebcamProperty> enabledWebcamPropertiesMap = PropertiesManager.getEnabledWebcamPropertiesMap();

		Long videoLengthSeconds = 0L;
		videoLengthSeconds = PropertiesManager.getVideoLength();
		
		int fileAddLimit = enabledWebcamPropertiesMap.keySet().size();
		if(enabledWebcamPropertiesMap.keySet().size()==1) {
			fileAddLimit = 2;
		}

		for (String webcamId : enabledWebcamPropertiesMap.keySet()) {

			String vlcLauncerName = vlcLauncerThreadName + "_" + webcamId;

			if (latestFileList.size() == fileAddLimit) {
				FileWithCreationTime fileWithCreationTime = latestFileList.remove(0);
				log.info("VlcLauncherScheduledTask add fileWithCreationTime: "+fileWithCreationTime.getFile().getName()+" - size: "+fileWithCreationTime.getFile().length());
				diskSPaceManager.addLatestFile(fileWithCreationTime);

				checkMemory();
			}

			WebcamProperty webcamProperty = enabledWebcamPropertiesMap.get(webcamId);
			log.info("Time is :" + now + " webcam " + webcamId + " - enabled: " + webcamProperty.isEnabled() + " - IP: "
					+ webcamProperty.getIp() + " - folder: " + webcamProperty.getVideoRelativeStorageFolder());

			String storageFileFullPath = ExecStringBuilder.getStorageFileFullPath(webcamProperty, false);

			long latestFileCreationTime = System.currentTimeMillis();

			String exec_1 = ExecStringBuilder.getExecStringWithStorageFileFullPath(webcamProperty, videoLengthSeconds,
					false, storageFileFullPath);

			FileWithCreationTime fileWithCreationTime = new FileWithCreationTime(storageFileFullPath,
					latestFileCreationTime);
			Long startTimeMillis = new Date().getTime();
			VlcLauncherBackupProperties vlcLauncherBackupProperties = new VlcLauncherBackupProperties(diskSPaceManager,
					videoLengthSeconds, webcamProperty, startTimeMillis, vlcLauncerName, fileWithCreationTime);

			try {
				ProcessManager.createProcess(videoLengthSeconds, exec_1, vlcLauncherBackupProperties, 0);
			} catch (IOException e) {
				e.printStackTrace();
			}

			latestFileList.add(fileWithCreationTime);
		}
	}

	private void checkMemory() {
		while (!diskSPaceManager.hasEnoughMemory()) {
			diskSPaceManager.deleteOldestFilesFromMemory();
		}
	}

	private static void initLatestFileList() {
		if (latestFileList == null) {
			latestFileList = new ArrayList<FileWithCreationTime>();
		}
	}

	public boolean isHasStarted() {
		return hasStarted;
	}

	public void setHasStarted(boolean hasStarted) {
		this.hasStarted = hasStarted;
	}

}