package it.mountaineering.gadria.ring.memory.scheduled.task;

import it.mountaineering.gadria.ring.memory.bean.WebcamProperty;
import it.mountaineering.gadria.ring.memory.util.DiskSpaceManager;

public class VlcLauncherBackupProperties {
	WebcamProperty webcamProperty;
	DiskSpaceManager diskSPaceManager;
	int videoLenght;
	int lenghtOffset;
	int webcamId;

	public VlcLauncherBackupProperties(DiskSpaceManager diskSPaceManager, int videoLenght, int webcamId,
			WebcamProperty webcamProperty, int lenghtOffset) {
		this.diskSPaceManager = diskSPaceManager;
		this.videoLenght = videoLenght;
		this.webcamId = webcamId;
		this.webcamProperty = webcamProperty;
		this.lenghtOffset = lenghtOffset;
	}

	public DiskSpaceManager getDiskSPaceManager() {
		return diskSPaceManager;
	}

	public void setDiskSPaceManager(DiskSpaceManager diskSPaceManager) {
		this.diskSPaceManager = diskSPaceManager;
	}

	public int getVideoLenght() {
		return videoLenght;
	}

	public void setVideoLenght(int videoLenght) {
		this.videoLenght = videoLenght;
	}

	public int getWebcamId() {
		return webcamId;
	}

	public void setWebcamId(int webcamId) {
		this.webcamId = webcamId;
	}

	public WebcamProperty getWebcamProperty() {
		return webcamProperty;
	}

	public void setWebcamProperty(WebcamProperty webcamProperty) {
		this.webcamProperty = webcamProperty;
	}

	public int getLenghtOffset() {
		return lenghtOffset;
	}

	public void setLenghtOffset(int lenghtOffset) {
		this.lenghtOffset = lenghtOffset;
	}

}
