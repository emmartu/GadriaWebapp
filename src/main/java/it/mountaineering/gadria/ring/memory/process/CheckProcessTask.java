package it.mountaineering.gadria.ring.memory.process;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import it.mountaineering.gadria.ring.memory.bean.FileWithCreationTime;
import it.mountaineering.gadria.ring.memory.scheduled.task.VlcLauncherBackupProperties;
import it.mountaineering.gadria.ring.memory.util.DiskSpaceManager;
import it.mountaineering.gadria.ring.memory.util.ExecStringBuilder;

public class CheckProcessTask extends TimerTask {

	private static final java.util.logging.Logger log = Logger.getLogger(CheckProcessTask.class.getName());
	static DateFormat format = new SimpleDateFormat("HH:mm:ss");
	String PID;
	VlcLauncherBackupProperties vlcLauncherBackupProperties;
	Long endTimeMillis = 0L;
	private static final Long UNDERSIZED_FILE_LIMIT_PER_SECONDS = 12000L;

	public CheckProcessTask(String pID, VlcLauncherBackupProperties vlcLauncherBackupProperties) {
		this.PID = pID;
		this.vlcLauncherBackupProperties = vlcLauncherBackupProperties;
		endTimeMillis = vlcLauncherBackupProperties.getStartMillis()
				+ (vlcLauncherBackupProperties.getVideoLengthSeconds() * 1000);
		log.fine("CheckProcessTask - startMillis:" + vlcLauncherBackupProperties.getStartMillis() + " - vlenmillis: "
				+ vlcLauncherBackupProperties.getVideoLengthSeconds() * 1000);
	}

	private void addSaveFileProcessTask(DiskSpaceManager diskSPaceManager, FileWithCreationTime fileWithCreationTime,
			Long videoLengthSeconds) {
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.schedule(new VlcLauncherBackupSaveVideoTask(diskSPaceManager, fileWithCreationTime),
				(vlcLauncherBackupProperties.getVideoLengthSeconds() * 2), TimeUnit.SECONDS);
		scheduler.shutdown();
	}

	private Long getNewVideoLength(Long actualTimeMillis) {
		Long offsetMillis = actualTimeMillis - vlcLauncherBackupProperties.getStartMillis();
		Long videoLengthSeconds = ((vlcLauncherBackupProperties.getVideoLengthSeconds() * 1000) - offsetMillis) / 1000;

		return videoLengthSeconds;
	}

	private Long getPastSecondsFromStart(Long actualTimeMillis) {
		Long offsetInSeconds = (actualTimeMillis - vlcLauncherBackupProperties.getStartMillis()) / 1000;

		return offsetInSeconds;
	}

	@Override
	public void run() {
		Date date = new Date();
		Long actualTimeMillis = date.getTime();
		boolean isRunning = ProcessManager.getRunningProcess(PID);
		log.fine("CheckProcessTask - PID:" + PID + " - isRunning: " + isRunning);

		if (isProcessOverEndTime(actualTimeMillis)) {
			log.fine("isProcessOverEndTimed true! - PID:" + PID);

			if (isRunning) {
				ProcessManager.killPId(vlcLauncherBackupProperties.getVlcLauncerPID(), PID);
			}else {
				ProcessManager.clearMap(PID, vlcLauncherBackupProperties.getVlcLauncerPID());
			}

			cancel();
			return;
		}

		if (isStreamUnderSized(actualTimeMillis)) {
			log.info("isStreamUnderSized true! - PID:" + PID + " - filename: "
					+ vlcLauncherBackupProperties.getFileWithCreationTime().getFile().getName());

			if (isRunning) {
				ProcessManager.killPId(vlcLauncherBackupProperties.getVlcLauncerPID(), PID);
			}else {
				ProcessManager.clearMap(PID, vlcLauncherBackupProperties.getVlcLauncerPID());
			}

			launchBackupVlcTask(actualTimeMillis);

			cancel();
			return;
		}

		if (!isRunning) {
			log.info("isRunning false! - PID:" + PID);
			ProcessManager.clearMap(PID, vlcLauncherBackupProperties.getVlcLauncerPID());

			launchBackupVlcTask(actualTimeMillis);

			cancel();
			return;
		}

	}

	private void launchBackupVlcTask(Long actualTimeMillis) {

		Long newVideoLengthSeconds = getNewVideoLength(actualTimeMillis);
		String storageFileFullPath = ExecStringBuilder
				.getStorageFileFullPath(vlcLauncherBackupProperties.getWebcamProperty(), true);
		String execString = ExecStringBuilder.getExecStringWithStorageFileFullPath(
				vlcLauncherBackupProperties.getWebcamProperty(), newVideoLengthSeconds, true, storageFileFullPath);

		long latestFileCreationTime = System.currentTimeMillis();
		FileWithCreationTime fileWithCreationTime = new FileWithCreationTime(storageFileFullPath,
				latestFileCreationTime);
		vlcLauncherBackupProperties.setFileWithCreationTime(fileWithCreationTime);
		vlcLauncherBackupProperties.setStartMillis(actualTimeMillis);
		vlcLauncherBackupProperties.setVideoLengthSeconds(newVideoLengthSeconds);

		try {
			ProcessManager.createProcess(newVideoLengthSeconds, execString, vlcLauncherBackupProperties, 0);
			addSaveFileProcessTask(vlcLauncherBackupProperties.getDiskSPaceManager(), fileWithCreationTime,
					newVideoLengthSeconds);
		} catch (IOException e) {
			e.printStackTrace();
		}

		log.info("CheckProcessTask !Running - new process created - PID:" + PID + " - cancel!");
	}

	private boolean isStreamUnderSized(long actualTimeMillis) {
		FileWithCreationTime fileWithCreationTime = vlcLauncherBackupProperties.getFileWithCreationTime();
		File streamFile = fileWithCreationTime.getFile();
		Long pastSecondsFromStart = getPastSecondsFromStart(actualTimeMillis);
		log.fine("isStreamUnderSized pastSecondsFromStart:" + pastSecondsFromStart);
		Long fileSizeLimit = pastSecondsFromStart * UNDERSIZED_FILE_LIMIT_PER_SECONDS;
		log.fine("isStreamUnderSized fileSizeLimit:" + fileSizeLimit + " - Stream file size:" + streamFile.length());

		if ((streamFile.length() > 0 && streamFile.length() < fileSizeLimit && streamFile.length() != 40)
				|| (streamFile.length() == 0 && pastSecondsFromStart > 20L)) {
			return true;
		}

		return false;
	}

	private boolean isProcessOverEndTime(long actualTimeMillis) {
		log.fine("isProcessOverEndTime endTimeMillis:" + endTimeMillis + " - actualTimeMillis: "+actualTimeMillis);
		boolean isOverEndTime = Long.compare(endTimeMillis, actualTimeMillis) < 0;

		log.fine("isOverEndTime: " + isOverEndTime);

		return isOverEndTime;
	}
	
	
	public static void main(String[] args) {
		Long endTimeMillis = 1559294831330L;
		Long actualTimeMillis = 1559294833519L;
		int cmp = Long.compare(endTimeMillis, actualTimeMillis);
		System.out.println("cmp: "+cmp);
		
		boolean bcmp = cmp < 0;
		
		System.out.println("bcmp: "+bcmp);
	}
}