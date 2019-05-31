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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import it.mountaineering.gadria.ring.memory.bean.DiskSpaceProperties;
import it.mountaineering.gadria.ring.memory.bean.FileWithCreationTime;

public class DiskSpaceManager {

	private static final java.util.logging.Logger log = Logger.getLogger(DiskSpaceManager.class.getName());
	public static final String _ZIPFILE = "freezed_videos";
	private DiskSpaceProperties diskSpaceProperties;
	public ZipOutputStream out;

	String storageFolder;
	Long maxDiskSpace;
	public Long size = 0L;
	String type;

	private ReadWriteLock lock = new ReentrantReadWriteLock();
	private ReadWriteLock corruptedCounterLock = new ReentrantReadWriteLock();
	private Lock writeLock = lock.writeLock();
	private Lock writeCorruptedCounterLock = corruptedCounterLock.writeLock();

	public DiskSpaceManager(String storageFolder, Long maxDiskSpace, String type) {
		this.storageFolder = storageFolder;
		this.maxDiskSpace = maxDiskSpace;
		this.type = type;
	}

	public boolean hasEnoughMemory() {
		log.fine("Thread: " + Thread.currentThread().getName() + "type: " + type + "hasEnoughMemory()");
		File storageFile = new File(storageFolder);

		if (diskSpaceProperties == null || diskSpaceProperties.getFileNumber() == 0L
				|| diskSpaceProperties.getFolderSize() == 0L) {
			diskSpaceProperties = getDiskSpaceProperties(storageFile);
		}

		Long safetythreshold = calculateSafetyThreshold(diskSpaceProperties);
		Long freeSpace = maxDiskSpace - diskSpaceProperties.getFolderSize();
		log.finer("maxDiskSpace: " + maxDiskSpace + " - " + diskSpaceProperties.getFolderSize() + " = " + freeSpace);

		if (freeSpace >= safetythreshold) {
			log.finer("OK --> freeSpace >= safetythreshold");

			return true;
		}

		log.finer("NO OK --> freeSpace < safetythreshold");
		return false;
	}

	public void deleteOldestFilesFromMemory() {
		log.fine(
				"Thread: " + Thread.currentThread().getName() + "type: " + type + " - Delete Oldest Files From Memory");

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
			diskSpaceProperties.removeFolderSize(size);
			diskSpaceProperties.removeFileNumber(1L);
			diskSpaceProperties.getFileMap().remove(firstItem);
			log.warning("invalid real file to remove!" + file.getName() + "- logic file removed");
		}
	}

	public List<String> freezeFilesFromDateToDateFromMemory(Date fromDateTime, Date toDateTime) {
		log.fine("type: " + type + " - Delete Files FromDateTime: " + fromDateTime.toString() + " toDateTime: "
				+ toDateTime.toString() + " From Memory");

		Collection<Long> unsortedEpochList = diskSpaceProperties.getFileMap().keySet();
		List<Long> sorted = asSortedList(unsortedEpochList);

		int fromKey = -1;
		int toKey = -1;

		List<Long> filesToMove = new ArrayList<Long>();

		for (int i = 0; i < sorted.size(); i++) {
			if (sorted.get(i) >= fromDateTime.getTime() && fromKey == -1) {
				fromKey = i;
			}

			if (sorted.get(i) > toDateTime.getTime() && toKey == -1) {
				toKey = i - 1;
				break;
			}

			if (fromKey >= 0 && toKey == -1) {
				Long fileToFreezeDateKey = sorted.get(i);
				filesToMove.add(fileToFreezeDateKey);
			}
		}

		List<String> freezingVideoList = freezeFiles(filesToMove);

		return freezingVideoList;
	}

	private List<String> freezeFiles(List<Long> fileToMove) {
		log.fine("type: " + type + " - freezeFiles");

		List<String> freezingVideoList = new ArrayList<String>();
		for (int i = 0; i < fileToMove.size(); i++) {
			Long fileToFreezeDateKey = fileToMove.get(i);

			File fileToFreeze = diskSpaceProperties.getFileMap().get(fileToFreezeDateKey);
			Long size = fileToFreeze.length();
			diskSpaceProperties.removeFolderSize(size);
			diskSpaceProperties.removeFileNumber(1L);

			String fileToFreezeNewPath = PropertiesManager.getFreezedVideoAbsoluteStorageFolder() + "//"
					+ fileToFreeze.getName();

			log.fine("type: " + type + " - file to freeze path: " + fileToFreeze.getAbsolutePath() + " - new path: "
					+ fileToFreezeNewPath);
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
		log.fine("Thread: " + Thread.currentThread().getName() + "type: " + type + " - calculateSafetyThreshold");

		Double safetythreshold = new Double(0);
		Double folderSize = new Double(diskSpaceProperties.getFolderSize());

		safetythreshold = (folderSize / diskSpaceProperties.getFileNumber());
		Double fiftyPercent = (safetythreshold / 100) * 50;
		safetythreshold = safetythreshold + fiftyPercent;

		Long longSafetythreshold = (new Double(safetythreshold)).longValue();
		log.finer("type: " + type + " - calculateSafetyThreshold: " + longSafetythreshold);

		return longSafetythreshold;
	}

	protected DiskSpaceProperties getDiskSpaceProperties(File directory) {
		log.info("type: " + type + " - *************  init DiskSpaceProperties  ***************");
		DiskSpaceProperties diskSpaceFile = new DiskSpaceProperties();

		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				log.info("type: " + type + " - -- getDiskSpaceProperties -- add file: " + file
						+ " on DiskSpaceProperties");

				diskSpaceFile.addFolderSize(file.length());
				diskSpaceFile.addFileNumber(1L);
				FileWithCreationTime fileWithCreationTime = new FileWithCreationTime(file.getAbsolutePath(),
						getFileCreationEpoch(file));
				diskSpaceFile.putFileInMap(fileWithCreationTime);
			} else {
				log.info("type: " + type + " - -- getDiskSpaceProperties -- get subfolder properties: " + file);

				DiskSpaceProperties diskSpaceFileTemp = getDiskSpaceProperties(file);
				diskSpaceFile.addFolderSize(diskSpaceFileTemp.getFolderSize());
				diskSpaceFile.addFileNumber(diskSpaceFileTemp.getFileNumber());
				diskSpaceFile.mergeFileMap(diskSpaceFileTemp.getFileMap());
			}
		}

		log.info("type: " + type + " - return diskSpaceFile for folder: " + directory + " FolderSize: "
				+ diskSpaceFile.getFolderSize() + ", FileNumber: " + diskSpaceFile.getFileNumber());
		log.info("type: " + type + " - ********  END DiskSpaceProperties  **********");

		return diskSpaceFile;
	}

	public void addLatestFile(FileWithCreationTime fileWithCreationTime) {
		log.fine("Thread: " + Thread.currentThread().getName() + "add Latest File name: "
				+ fileWithCreationTime.getFile().getName() + ", size: " + fileWithCreationTime.getFile().length()
				+ ", creation time: " + fileWithCreationTime.getCreationTime());

		if (!isFileCorrupted(fileWithCreationTime)) {

			try {
				writeLock.tryLock(1, TimeUnit.SECONDS);

				if (diskSpaceProperties == null) {
					diskSpaceProperties = new DiskSpaceProperties();
				}

				diskSpaceProperties.addFileNumber(1L);
				Long size = fileWithCreationTime.getFile().length();
				diskSpaceProperties.addFolderSize(size);
				diskSpaceProperties.putFileInMap(fileWithCreationTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				writeLock.unlock();
			}
		}
	}

	private boolean isFileCorrupted(FileWithCreationTime fileWithCreationTime) {

		for (int i = 0; i < 20; i++) {

			if (isFileOpen(fileWithCreationTime)) {
				log.fine("Thread: " + Thread.currentThread().getName() + "isFileOpen true iter: " + i);

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				log.fine("Thread: " + Thread.currentThread().getName() + "isFileOpen false iter: " + i);
				return false;
			}
		}

		log.fine("Thread: " + Thread.currentThread().getName() + "isFileCorrupted CORRUPTED!!");
		return true;
	}

	private boolean isFileOpen(FileWithCreationTime fileWithCreationTime) {
		File file = fileWithCreationTime.getFile();
		File sameFileName = fileWithCreationTime.getFile();
		writeCorruptedCounterLock.lock();

		if (file.renameTo(sameFileName)) {
			log.fine("Thread: " + Thread.currentThread().getName() + "isFileOpen: file renamed!");
			writeCorruptedCounterLock.unlock();
			return false;
		} else {
			log.fine("Thread: " + Thread.currentThread().getName() + "isFileOpen: file NOT renamed!");
			writeCorruptedCounterLock.unlock();
			return true;
		}
	}

	private long getFileCreationEpoch(File file) {
		try {
			BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			return attr.creationTime().toInstant().toEpochMilli();
		} catch (IOException e) {
			throw new RuntimeException(file.getAbsolutePath(), e);
		}
	}

	public void addToZipFile(File zipFile, File file) throws IOException {
		String zipFilePath = "VIDEO\\";

		if (out == null) {
			out = new ZipOutputStream(new FileOutputStream(zipFile));
		}

		ZipEntry zipEntry = new ZipEntry(zipFilePath + file.getName());
		out.putNextEntry(zipEntry);

		InputStream inputStream = null;
		try {
			inputStream = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		byte[] buffer = new byte[1024];

		int len;
		while ((len = inputStream.read(buffer)) > 0) {
			out.write(buffer, 0, len);
		}

		inputStream.close();
	}

	public File getDownloadZipFile() {
		String freezedFilesDirPath = PropertiesManager.getFreezedVideoAbsoluteStorageFolder();
		File freezedFilesDirectory = new File(freezedFilesDirPath);

		String installationPath = PropertiesManager.getInstallationPath();

		String timeStamp = new SimpleDateFormat("yyyy-MM-dd@HH-mm-ss.S").format(new Date());
		String fileName = _ZIPFILE + "_" + timeStamp + ".zip";
		File zipFile = new File(installationPath + fileName);

		for (File file : freezedFilesDirectory.listFiles()) {
			try {
				addToZipFile(zipFile, file);
				System.out.println("added to zip file: " + file.getName());

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			out.closeEntry();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return zipFile;
	}

	public void deleteFreezedFiles() {
		String freezedFilesDirPath = PropertiesManager.getFreezedVideoAbsoluteStorageFolder();
		File freezedFilesDirectory = new File(freezedFilesDirPath);

		for (File file : freezedFilesDirectory.listFiles()) {
			boolean deleted = file.delete();
			System.out.println("deleted: " + deleted);
		}
	}

}