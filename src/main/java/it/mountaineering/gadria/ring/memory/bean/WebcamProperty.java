package it.mountaineering.gadria.ring.memory.bean;

import it.mountaineering.gadria.ring.memory.bean.WebcamProperty;

public class WebcamProperty {

	String iD;
	boolean enabled;
	String ip;
	String videoRelativeStorageFolder;
	String pictureRelativeStorageFolder;
	
	public String getiD() {
		return iD;
	}

	public void setiD(String iD) {
		this.iD = iD;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getVideoRelativeStorageFolder() {
		return videoRelativeStorageFolder;
	}

	public void setVideoRelativeStorageFolder(String videoRelativeStorageFolder) {
		this.videoRelativeStorageFolder = videoRelativeStorageFolder;
	}

	public String getPictureRelativeStorageFolder() {
		return pictureRelativeStorageFolder;
	}

	public void setPictureRelativeStorageFolder(String pictureRelativeStorageFolder) {
		this.pictureRelativeStorageFolder = pictureRelativeStorageFolder;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((iD == null) ? 0 : iD.hashCode());
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result
				+ ((pictureRelativeStorageFolder == null) ? 0 : pictureRelativeStorageFolder.hashCode());
		result = prime * result + ((videoRelativeStorageFolder == null) ? 0 : videoRelativeStorageFolder.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WebcamProperty other = (WebcamProperty) obj;
		if (enabled != other.enabled)
			return false;
		if (iD == null) {
			if (other.iD != null)
				return false;
		} else if (!iD.equals(other.iD))
			return false;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (pictureRelativeStorageFolder == null) {
			if (other.pictureRelativeStorageFolder != null)
				return false;
		} else if (!pictureRelativeStorageFolder.equals(other.pictureRelativeStorageFolder))
			return false;
		if (videoRelativeStorageFolder == null) {
			if (other.videoRelativeStorageFolder != null)
				return false;
		} else if (!videoRelativeStorageFolder.equals(other.videoRelativeStorageFolder))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WebcamProperty [iD=" + iD + ", enabled=" + enabled + ", ip=" + ip + ", videoRelativeStorageFolder="
				+ videoRelativeStorageFolder + ", pictureRelativeStorageFolder=" + pictureRelativeStorageFolder + "]";
	}
}
