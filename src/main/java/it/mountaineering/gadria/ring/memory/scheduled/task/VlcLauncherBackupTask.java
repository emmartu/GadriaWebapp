package it.mountaineering.gadria.ring.memory.scheduled.task;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import it.mountaineering.gadria.ring.memory.bean.FileWithCreationTime;
import it.mountaineering.gadria.ring.memory.bean.WebcamProperty;
import it.mountaineering.gadria.ring.memory.process.ProcessManager;
import it.mountaineering.gadria.ring.memory.util.DiskSpaceManager;
import it.mountaineering.gadria.ring.memory.util.ExecStringBuilder;
import it.mountaineering.gadria.ring.memory.util.PropertiesManager;

public class VlcLauncherBackupTask implements Runnable{

	private static final java.util.logging.Logger log = Logger.getLogger(VlcLauncherScheduledTask.class.getName());
	private static final String VLC_VIDEO_RECORDER_BAT = PropertiesManager.getInstallationPath()+"VlcVideoRecorder.bat";

	Date now;
	private boolean hasStarted = false;
	DiskSpaceManager diskSPaceManager;
	String webcamId;
	Long videoLengthSeconds;
	WebcamProperty webcamProperty;
	
	public VlcLauncherBackupTask(VlcLauncherBackupProperties vlcLauncherBackupProperties) {
		this.diskSPaceManager = vlcLauncherBackupProperties.getDiskSPaceManager();
		this.webcamId = vlcLauncherBackupProperties.getWebcamProperty().getiD();
		this.videoLengthSeconds = vlcLauncherBackupProperties.getVideoLengthSeconds();
		this.webcamProperty = vlcLauncherBackupProperties.getWebcamProperty();
	}
	
	public void run() {
		now = new Date(); // initialize date
		String vlcLauncerName = Thread.currentThread().getName();
		log.info("Vlc Launcher Backup Scheduled Task start! Date: "+now);

		String storageFileFullPath = ExecStringBuilder.getStorageFileFullPath(webcamProperty, true);

		long latestFileCreationTime = System.currentTimeMillis();
		
		String exec_1 = ExecStringBuilder.getExecString(webcamProperty, videoLengthSeconds, true);

		FileWithCreationTime fileWithCreationTime = new FileWithCreationTime(storageFileFullPath, latestFileCreationTime);
		VlcLauncherBackupProperties vlcLauncherBackupProperties = new VlcLauncherBackupProperties(diskSPaceManager, videoLengthSeconds, webcamProperty, latestFileCreationTime, vlcLauncerName, fileWithCreationTime);

		try {
			ProcessManager.createProcess(videoLengthSeconds, exec_1, vlcLauncherBackupProperties);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isHasStarted() {
		return hasStarted;
	}

	public void setHasStarted(boolean hasStarted) {
		this.hasStarted = hasStarted;
	}
}
