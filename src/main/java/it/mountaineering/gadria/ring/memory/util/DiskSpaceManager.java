package it.mountaineering.gadria.ring.memory.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import it.mountaineering.gadria.ring.memory.bean.DiskSpaceProperties;
import it.mountaineering.gadria.ring.memory.bean.FileWithCreationTime;

public class DiskSpaceManager {

	private static final java.util.logging.Logger log = Logger.getLogger(DiskSpaceManager.class.getName());
	public static final String _ZIPFILE = "freezed_video_zip_file.zip";
	private DiskSpaceProperties diskSpaceProperties;
	public ZipOutputStream out;

	String storageFolder;
	Long maxDiskSpace;
	public Long size = 0L;

	public DiskSpaceManager(String storageFolder, Long maxDiskSpace) {
		this.storageFolder = storageFolder;
		this.maxDiskSpace = maxDiskSpace;
	}

	public boolean hasEnoughMemory() {
		log.fine("hasEnoughMemory()");
		File storageFile = new File(storageFolder);

		if (diskSpaceProperties == null || diskSpaceProperties.getFileNumber() == 0L
				|| diskSpaceProperties.getFolderSize() == 0L) {
			diskSpaceProperties = getDiskSpaceProperties(storageFile);
		}

		Long safetythreshold = calculateSafetyThreshold(diskSpaceProperties);
		Long freeSpace = maxDiskSpace - diskSpaceProperties.getFolderSize();
		log.finer("freeSpace: " + maxDiskSpace + " - " + diskSpaceProperties.getFolderSize() + " = " + freeSpace);

		if (freeSpace >= safetythreshold) {
			log.finer("OK --> freeSpace >= safetythreshold");

			return true;
		}

		log.finer("NO OK --> freeSpace < safetythreshold");
		return false;
	}

	public void deleteOldestFilesFromMemory() {
		log.fine("Delete Oldest Files From Memory");

		Collection<Long> unsortedEpochList = diskSpaceProperties.getFileMap().keySet();
		List<Long> sorted = asSortedList(unsortedEpochList);
		Long firstItem = sorted.get(0);
		log.finer("sorted time file map, first key: " + firstItem);
		File file = diskSpaceProperties.getFileMap().get(firstItem);
		log.finer("sorted time file map, first file to remove: " + file.getAbsolutePath() + ", size: " + file.length());

		if (file.isFile()) {
			Long size = file.length();
			diskSpaceProperties.removeFolderSize(size);
			diskSpaceProperties.removeFileNumber(1L);
			diskSpaceProperties.getFileMap().remove(firstItem);
			boolean deleted = file.delete();
			log.finer("file deleted: " + deleted);
		} else {
			log.warning("invalid file to remove!" + file.getName());
		}
	}

	public List<String> freezeFilesFromDateToDateFromMemory(Date fromDateTime, Date toDateTime) {
		log.fine("Delete Files FromDateTime: " + fromDateTime.toString() + " toDateTime: " + toDateTime.toString()
				+ " From Memory");

		Collection<Long> unsortedEpochList = diskSpaceProperties.getFileMap().keySet();
		List<Long> sorted = asSortedList(unsortedEpochList);

		int fromKey = 0;
		int toKey = 0;

		List<Long> filesToMove = new ArrayList<Long>();

		for (int i = 0; i < sorted.size(); i++) {
			Long currKey = sorted.get(i);
			System.out.println(currKey);
			if (sorted.get(i) >= fromDateTime.getTime() && fromKey == 0L) {
				fromKey = i;
			}

			if (sorted.get(i) > toDateTime.getTime() && toKey == 0L) {
				toKey = i - 1;
				break;
			}

			if (fromKey > 0 && toKey == 0) {
				Long fileToFreezeDateKey = sorted.get(i);
				filesToMove.add(fileToFreezeDateKey);
			}
		}

		List<String> freezingVideoList = freezeFiles(filesToMove);
		
		return freezingVideoList;
	}

	private List<String> freezeFiles(List<Long> fileToMove) {
		log.fine("freezeFiles");

		List<String> freezingVideoList = new ArrayList<String>();
		for (int i = 0; i < fileToMove.size(); i++) {
			Long fileToFreezeDateKey = fileToMove.get(i);

			File fileToFreeze = diskSpaceProperties.getFileMap().get(fileToFreezeDateKey);
			Long size = fileToFreeze.length();
			diskSpaceProperties.removeFolderSize(size);
			diskSpaceProperties.removeFileNumber(1L);

			String fileToFreezeNewPath = PropertiesManager.getFreezedVideoAbsoluteStorageFolder() + "//" + fileToFreeze.getName();

			log.fine("file to freeze path: "+fileToFreeze.getAbsolutePath()+" - new path: "+fileToFreezeNewPath);
			fileToFreeze.renameTo(new File(fileToFreezeNewPath));
			File freezedfile = new File(fileToFreezeNewPath);
			freezingVideoList.add(freezedfile.getName());
			
			diskSpaceProperties.getFileMap().remove(fileToFreezeDateKey);
		}
		
		return freezingVideoList;
	}

	private <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list);
		return list;
	}

	protected Long calculateSafetyThreshold(DiskSpaceProperties diskSpaceProperties) {
		log.fine("calculateSafetyThreshold");

		Double safetythreshold = new Double(0);
		Double folderSize = new Double(diskSpaceProperties.getFolderSize());

		safetythreshold = (folderSize / diskSpaceProperties.getFileNumber());
		Double fiftyPercent = (safetythreshold / 100) * 50;
		safetythreshold = safetythreshold + fiftyPercent;

		Long longSafetythreshold = (new Double(safetythreshold)).longValue();
		log.finer("calculateSafetyThreshold: " + longSafetythreshold);

		return longSafetythreshold;
	}

	protected DiskSpaceProperties getDiskSpaceProperties(File directory) {
		log.info("*************  init DiskSpaceProperties  ***************");
		DiskSpaceProperties diskSpaceFile = new DiskSpaceProperties();

		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				log.info("-- getDiskSpaceProperties -- add file: " + file + " on DiskSpaceProperties");

				diskSpaceFile.addFolderSize(file.length());
				diskSpaceFile.addFileNumber(1L);
				FileWithCreationTime fileWithCreationTime = new FileWithCreationTime(file.getAbsolutePath(),
						getFileCreationEpoch(file));
				diskSpaceFile.putFileInMap(fileWithCreationTime);
			} else {
				log.info("-- getDiskSpaceProperties -- get subfolder properties: " + file);

				DiskSpaceProperties diskSpaceFileTemp = getDiskSpaceProperties(file);
				diskSpaceFile.addFolderSize(diskSpaceFileTemp.getFolderSize());
				diskSpaceFile.addFileNumber(diskSpaceFileTemp.getFileNumber());
				diskSpaceFile.mergeFileMap(diskSpaceFileTemp.getFileMap());
			}
		}

		log.info("return diskSpaceFile for folder: " + directory + " FolderSize: " + diskSpaceFile.getFolderSize()
				+ ", FileNumber: " + diskSpaceFile.getFileNumber());
		log.info("********  END DiskSpaceProperties  **********");

		return diskSpaceFile;
	}

	public void addLatestFile(FileWithCreationTime fileWithCreationTime) {
		log.fine("add Latest File name: " + fileWithCreationTime.getFile().getName() + ", size: "
				+ fileWithCreationTime.getFile().length() + ", creation time: "
				+ fileWithCreationTime.getCreationTime());

		if (diskSpaceProperties == null) {
			diskSpaceProperties = new DiskSpaceProperties();
		}

		diskSpaceProperties.addFileNumber(1L);
		Long size = fileWithCreationTime.getFile().length();
		diskSpaceProperties.addFolderSize(size);
		diskSpaceProperties.putFileInMap(fileWithCreationTime);
	}

	private long getFileCreationEpoch(File file) {
		try {
			BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			return attr.creationTime().toInstant().toEpochMilli();
		} catch (IOException e) {
			throw new RuntimeException(file.getAbsolutePath(), e);
		}
	}
	
	
	public void addToZipFile(File file) throws IOException {
		String installationPath = PropertiesManager.getVideoAbsoluteStorageFolder();
		File zipFile = new File(installationPath+_ZIPFILE);
		String zipFilePath = "VIDEO\\";

		if(out==null) {
			out = new ZipOutputStream(new FileOutputStream(zipFile));
		}
		
		ZipEntry e = new ZipEntry(zipFilePath+file.getName());
		out.putNextEntry(e);

		InputStream inputStream = null;
		try {
			inputStream = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		byte[] media = getByteArray(inputStream);

		out.write(media, 0, media.length);
	}

	public byte[] getByteArray(InputStream inputStream) {
		byte[] media = null;

		try {
			media = IOUtils.toByteArray(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return media;
	}
		
	public DiskSpaceProperties getFileCounter() {
		DiskSpaceProperties diskSpaceProperties = this.diskSpaceProperties;
		
		return diskSpaceProperties;		
	}

	public File getDownloadZipFile() {
		String freezedFilesDirPath = PropertiesManager.getFreezedVideoAbsoluteStorageFolder();
		File freezedFilesDirectory = new File(freezedFilesDirPath);
		
		for (File file : freezedFilesDirectory.listFiles()) {
			try {
				addToZipFile(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		String installationPath = PropertiesManager.getVideoAbsoluteStorageFolder();
		File zipFile = new File(installationPath+_ZIPFILE);
		
		return zipFile;
	}

}
