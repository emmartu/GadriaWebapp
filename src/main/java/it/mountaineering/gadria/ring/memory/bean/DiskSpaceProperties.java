package it.mountaineering.gadria.ring.memory.bean;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;


public class DiskSpaceProperties {

	Long folderSize;
	Long fileNumber;
	Map<Long,File> fileMap;
	
	public DiskSpaceProperties() {
		folderSize = 0L;
		fileNumber = 0L;
		fileMap = new LinkedHashMap<Long,File>();
	}

	public Long getFolderSize() {
		return folderSize;
	}

	public void setFolderSize(Long folderSize) {
		this.folderSize = folderSize;
	}

	public Long getFileNumber() {
		return fileNumber;
	}

	public void setFileNumber(Long fileNumber) {
		this.fileNumber = fileNumber;
	}

	public void addFolderSize(long length) {
		this.folderSize += length;		
	}

	public void removeFolderSize(long length) {
		this.folderSize -= length;		
	}

	public void addFileNumber(Long fileCount) {
		this.fileNumber += fileCount;		
	}

	public void removeFileNumber(Long fileCount) {
		this.fileNumber -= fileCount;		
	}

	public void putFileInMap(FileWithCreationTime fileWithCreationTime) {
		if(fileMap==null) {
			fileMap = new LinkedHashMap<Long, File>();
		}
		
		fileMap.put(fileWithCreationTime.getCreationTime(), fileWithCreationTime.getFile());
	}

	public Map<Long,File> getFileMap() {
		return fileMap;
	}

	public void mergeFileMap(Map<Long, File> fileMapToMerge) {
		if(fileMap==null) {
			fileMap = new LinkedHashMap<Long, File>();
		}

		fileMap.putAll(fileMapToMerge);
	}
}
