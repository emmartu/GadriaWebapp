package it.mountaineering.gadria.ring.memory.scheduled.task;

import it.mountaineering.gadria.ring.memory.bean.FileWithCreationTime;
import it.mountaineering.gadria.ring.memory.bean.WebcamProperty;
import it.mountaineering.gadria.ring.memory.util.DiskSpaceManager;

public class VlcLauncherBackupProperties {
	String vlcLauncerPID;
	WebcamProperty webcamProperty;
	DiskSpaceManager diskSPaceManager;
	Long videoLengthSeconds;
	Long startMillis;
	FileWithCreationTime fileWithCreationTime;

	public VlcLauncherBackupProperties(DiskSpaceManager diskSPaceManager, Long videoLengthSeconds,
			WebcamProperty webcamProperty, Long startMillis, String vlcLauncerPID,
			FileWithCreationTime fileWithCreationTime) {
		this.diskSPaceManager = diskSPaceManager;
		this.videoLengthSeconds = videoLengthSeconds;
		this.webcamProperty = webcamProperty;
		this.startMillis = startMillis;
		this.vlcLauncerPID = vlcLauncerPID;
		this.fileWithCreationTime = fileWithCreationTime;
	}

	public DiskSpaceManager getDiskSPaceManager() {
		return diskSPaceManager;
	}

	public void setDiskSPaceManager(DiskSpaceManager diskSPaceManager) {
		this.diskSPaceManager = diskSPaceManager;
	}

	public Long getVideoLengthSeconds() {
		return videoLengthSeconds;
	}

	public void setVideoLengthSeconds(Long videoLengthSeconds) {
		this.videoLengthSeconds = videoLengthSeconds;
	}

	public WebcamProperty getWebcamProperty() {
		return webcamProperty;
	}

	public void setWebcamProperty(WebcamProperty webcamProperty) {
		this.webcamProperty = webcamProperty;
	}

	public Long getStartMillis() {
		return startMillis;
	}

	public void setStartMillis(Long startMillis) {
		this.startMillis = startMillis;
	}

	public String getVlcLauncerPID() {
		return vlcLauncerPID;
	}

	public void setVlcLauncerPID(String vlcLauncerPID) {
		this.vlcLauncerPID = vlcLauncerPID;
	}

	public FileWithCreationTime getFileWithCreationTime() {
		return fileWithCreationTime;
	}

	public void setFileWithCreationTime(FileWithCreationTime fileWithCreationTime) {
		this.fileWithCreationTime = fileWithCreationTime;
	}

}
