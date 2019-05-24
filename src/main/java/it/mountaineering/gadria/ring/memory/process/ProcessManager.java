package it.mountaineering.gadria.ring.memory.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import it.mountaineering.gadria.ring.memory.scheduled.task.VlcLauncherBackupProperties;

public class ProcessManager {

	private static final java.util.logging.Logger log = Logger.getLogger(ProcessManager.class.getName());
	private static final int checkProcessSecondsTimeout = 5;
	private static final String TASKLIST_CMD = "tasklist.exe /FI \"IMAGENAME eq vlc*\" /fo csv /nh";
	private static final String TASKLIST_PID_CMD = "tasklist.exe /FI ";
	private static final String TASKLIST_PID_CMD_TAIL = " /fo csv /nh";
	private static final CharSequence NO_TASK = "No tasks";
	private static final String TASKKILL_CMD = "taskkill.exe /F /FI ";
	private static Map<String, Set<String>> processThreadNameToPidMap = new HashMap<String, Set<String>>();
	private static Map<String, String> processPidToVlcThreadIdMap = new HashMap<String, String>();
	private static List<String> processList = new ArrayList<String>();
	private static Map<String, VlcLauncherBackupProperties> vlcLauncherBackupPropertiesMap = new HashMap<String, VlcLauncherBackupProperties>();

	static ReadWriteLock lock = new ReentrantReadWriteLock();
	static Lock writeLock = lock.writeLock();

	static DateFormat format = new SimpleDateFormat("HH:mm:ss");

	public static void createProcess(Long videoLengthSeconds, String execString,
			VlcLauncherBackupProperties vlcLauncherBackupProperties) throws IOException {

		String vlcLauncerName = vlcLauncherBackupProperties.getVlcLauncerPID();

		try {
			writeLock.tryLock(1, TimeUnit.SECONDS);

			launchCmdLineProcess(execString, vlcLauncerName);
			Set<String> processVlcNameToPidSet = getProcessPidSetByVlcLauncherName(vlcLauncerName);
			log.info("createProcess processVlcNameToPidSet: "+processVlcNameToPidSet.toString());
			String PID = getPidFromRunningProcesses();

			if (PID != "") {
				processVlcNameToPidSet.add(PID);
				processPidToVlcThreadIdMap.put(PID, vlcLauncerName);
				vlcLauncherBackupPropertiesMap.put(vlcLauncerName, vlcLauncherBackupProperties);
				addKillProcessTask(PID, vlcLauncerName, Integer.parseInt(videoLengthSeconds.toString()));
				
				addCheckProcessTask(PID, vlcLauncherBackupProperties);
				
				writeLock.unlock();
			} else {
				writeLock.unlock();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void addCheckProcessTask(String pID, VlcLauncherBackupProperties vlcLauncherBackupProperties) {
		log.info("addCheckProcessTask PID: "+pID);
		Timer checkProcessTimer = new Timer();
		CheckProcessTask checkProcessTask = new CheckProcessTask(pID, vlcLauncherBackupProperties);
		checkProcessTimer.schedule(checkProcessTask, 5000, 3000);
	}

	private static void addKillProcessTask(String PID, String vlcLauncerName, int videoLengthSeconds) {
		log.info("addKillProcessTask PID: "+PID+" - videoLengthSeconds:"+videoLengthSeconds+" - vlcLauncerName: "+vlcLauncerName);
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.schedule(new KillProcessTask(PID, vlcLauncerName), videoLengthSeconds + 5, TimeUnit.SECONDS);
		scheduler.shutdown();
	}

	private static Set<String> getProcessPidSetByVlcLauncherName(String vlcLauncerName) {
		Set<String> processPidSetByVlcName = null;

		if (processThreadNameToPidMap.get(vlcLauncerName) == null) {
			processPidSetByVlcName = new HashSet<String>();
			processThreadNameToPidMap.put(vlcLauncerName, processPidSetByVlcName);
		} else {
			processPidSetByVlcName = processThreadNameToPidMap.get(vlcLauncerName);
		}

		return processPidSetByVlcName;
	}

	public static void launchCmdLineProcess(String execString, String vlcLauncerName) {
		log.info("addProcess, exec: " + execString);

		Process process = null;
		try {
			process = Runtime.getRuntime().exec(execString);
		} catch (IOException e) {
			e.printStackTrace();
		}

		launchStreamGlobbler(process, vlcLauncerName);
	}

	private static void launchStreamGlobbler(Process proc, String vlcLauncerName) {
		StreamGobbler err = new StreamGobbler(proc.getErrorStream(), "ERROR_" + vlcLauncerName);
		StreamGobbler in = new StreamGobbler(proc.getInputStream(), "INPUT_" + vlcLauncerName);
		err.start();
		in.start();
	}

	public static void clearMap(String PID, String vlcLauncerPID) {
		log.info("clearMap Start, vlcPID: " + vlcLauncerPID + " - PID: "+PID+" actual map: " + processThreadNameToPidMap.toString());
		
		if (!processThreadNameToPidMap.get(vlcLauncerPID).isEmpty()
				&& processThreadNameToPidMap.get(vlcLauncerPID).contains(PID)) {

			processThreadNameToPidMap.get(vlcLauncerPID).remove(PID);
			log.fine("clearMap vlcLauncerPID: " + vlcLauncerPID + " - PID: "+PID+" removed");
		}

		if (processThreadNameToPidMap.get(vlcLauncerPID).isEmpty()) {
			processThreadNameToPidMap.remove(vlcLauncerPID);
			log.fine("clearMap vlcLauncerPID: " + vlcLauncerPID + " - vlcLauncerPID removed");
		}

		if (!processPidToVlcThreadIdMap.get(PID).isEmpty()
				&& processPidToVlcThreadIdMap.get(PID).contains(vlcLauncerPID)) {
			log.fine("Pid to Thread map: " + processPidToVlcThreadIdMap.toString() + " to be removed");

			processPidToVlcThreadIdMap.remove(PID);
			log.fine("clearMap vlcLauncerPID: " + vlcLauncerPID + " - PID removed, map: "+processPidToVlcThreadIdMap.toString());
		}

		processList.remove(PID);
		log.info("processList: " + processList.toString());
		log.info("clearMap end, vlcPID: " + vlcLauncerPID + " actual map: " + processThreadNameToPidMap.toString());
	}

	public static void killPId(String vlcLauncerPID, String PID) {
		log.info("killPId Start, vlcPID: " + vlcLauncerPID + " - PID: "+PID);

		try {
			writeLock.tryLock(1, TimeUnit.SECONDS);
			String line;
			System.out.println("Sono il processo: " + vlcLauncerPID + " ammazzo il PID: " + PID);

			Process p = Runtime.getRuntime().exec(TASKKILL_CMD + "\"PID eq " + PID + "\"");
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

			while ((line = input.readLine()) != null) {
				if (!line.trim().equals("")) {
					log.info("Running processes line: " + line);
				}
			}

			input.close();

			clearMap(PID, vlcLauncerPID);
			writeLock.unlock();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	public static boolean getRunningProcess(String pID) {

		log.info("getRunningProcess - PID:"+pID);

		try {
			String line;
			Process p = Runtime.getRuntime()
					.exec(TASKLIST_PID_CMD + "\"PID eq " + pID + " \" " + TASKLIST_PID_CMD_TAIL);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

			while ((line = input.readLine()) != null) {
				if (!line.trim().equals("")) {
					log.info("Running processes line: " + line);

					if (!line.contains(NO_TASK)) {
						return true;
					}
				}
			}

			input.close();
		} catch (Exception err) {
			err.printStackTrace();
		}

		return false;
	}

	public static String getPidFromRunningProcesses() {
		log.info("getPidFromRunningProcesses");
		log.info("processList: "+processList.toString());

		String PID = "";
		try {
			String line;
			Process p = Runtime.getRuntime().exec(TASKLIST_CMD);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

			while ((line = input.readLine()) != null) {
				if (!line.trim().equals("")) {
					log.info("Running processes line: " + line);
					String[] processItems = null;

					if (!line.contains(NO_TASK)) {
						processItems = line.split(",");
					}

					//clearDeadProcesses(processItems[1]);

					if (processItems != null &&
						!processList.contains(processItems[1])) {
						log.info("getPidFromRunningProcesses add new process: "+processItems[1]);
						processList.add(processItems[1]);
						return processItems[1];
					}
				}
			}

			input.close();
		} catch (Exception err) {
			err.printStackTrace();
		}

		return PID;
	}

	private static void clearDeadProcesses(String deadProcessPid) {
		log.info("clearDeadProcesses deadProcessPid: "+deadProcessPid);
		if (processList.contains(deadProcessPid)) {
			String vlcProcessId = processPidToVlcThreadIdMap.get(deadProcessPid);
			processThreadNameToPidMap.get(vlcProcessId).remove(deadProcessPid);
			processPidToVlcThreadIdMap.remove(deadProcessPid);
			processList.remove(deadProcessPid);
		}
	}

	/*
	 * public static void clearMap() {
	 * 
	 * for (String pid : processPidMap.keySet()) { Map<String, Integer>
	 * processMap = processPidMap.get(pid);
	 * 
	 * if (processMap.isEmpty()) { processPidMap.remove(pid); } } }
	 */

	public static void main(String[] args) {
		// killPId("1820");
	}

}

class StreamGobbler extends Thread {
	private static final java.util.logging.Logger log = Logger.getLogger(StreamGobbler.class.getName());

	public StreamGobbler(InputStream in, String type) {
		this.in = in;
		this.type = type;
	}

	private InputStream in;
	private String type;

	public void run() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while ((line = reader.readLine()) != null) {
				//log.info(type + ": " + line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
