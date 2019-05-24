package it.mountaineering.gadria.ring.memory.process;

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

	public CheckProcessTask(String pID, VlcLauncherBackupProperties vlcLauncherBackupProperties) {
		this.PID = pID;
		this.vlcLauncherBackupProperties = vlcLauncherBackupProperties;
		endTimeMillis = vlcLauncherBackupProperties.getStartMillis() + (vlcLauncherBackupProperties.getVideoLengthSeconds()*1000);
		log.info("CheckProcessTask - startMillis:"+vlcLauncherBackupProperties.getStartMillis()+" - vlenmillis: "+vlcLauncherBackupProperties.getVideoLengthSeconds()*1000);
	}

	private static void addSaveFileProcessTask(DiskSpaceManager diskSPaceManager, FileWithCreationTime fileWithCreationTime, Long videoLengthSeconds) {
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.schedule(new VlcLauncherBackupSaveVideoTask(diskSPaceManager, fileWithCreationTime), videoLengthSeconds+1, TimeUnit.SECONDS);
		scheduler.shutdown();
	}

	private Long getNewVideoLength(Long actualTimeMillis) {
		log.info("CheckProcessTask - PID:"+PID+" - !isRunning: offsetMillis= "+actualTimeMillis+" - startMillis: "+vlcLauncherBackupProperties.getStartMillis());
		Long offsetMillis = actualTimeMillis - vlcLauncherBackupProperties.getStartMillis();
		log.info("CheckProcessTask - PID:"+PID+" - !isRunning: videoLengthSeconds: "+vlcLauncherBackupProperties.getVideoLengthSeconds()+" *1000 - offsetMillis: "+offsetMillis);
		Long videoLengthSeconds = ((vlcLauncherBackupProperties.getVideoLengthSeconds()*1000) - offsetMillis)/1000;
		log.info("CheckProcessTask - PID:"+PID+" - !isRunning: videoLengthSeconds: "+videoLengthSeconds);
		
		return videoLengthSeconds;
	}
	
	@Override
	public void run() {
		Date date = new Date();
		Long actualTimeMillis = date.getTime();
		log.info("CheckProcessTask run - PID:"+PID+" - time: "+format.format(date));
		
		if (Long.compare(endTimeMillis, actualTimeMillis)>=0) {
			boolean isRunning = ProcessManager.getRunningProcess(PID);
			log.info("CheckProcessTask - PID:"+PID+" - isRunning: "+isRunning);
			if(!isRunning) {
				Long newVideoLengthSeconds = getNewVideoLength(actualTimeMillis);
				String execString = ExecStringBuilder.getExecString(vlcLauncherBackupProperties.getWebcamProperty(), newVideoLengthSeconds, true);

				long latestFileCreationTime = System.currentTimeMillis();
				String storageFileFullPath = ExecStringBuilder.getStorageFileFullPath(vlcLauncherBackupProperties.getWebcamProperty(), true);
				FileWithCreationTime fileWithCreationTime = new FileWithCreationTime(storageFileFullPath, latestFileCreationTime);
				vlcLauncherBackupProperties.setFileWithCreationTime(fileWithCreationTime);

				try {
					ProcessManager.createProcess(newVideoLengthSeconds, execString, vlcLauncherBackupProperties);
					addSaveFileProcessTask(vlcLauncherBackupProperties.getDiskSPaceManager(), fileWithCreationTime, newVideoLengthSeconds);;
				} catch (IOException e) {
					e.printStackTrace();
				}

				log.info("CheckProcessTask !Running - new process created - PID:"+PID+" - cancel!");
				cancel();
			}
		}else {
			log.info("CheckProcessTask - endTimeMillis<date.getTime() - PID:"+PID+" - cancel!");
			cancel();
		}
	}
}