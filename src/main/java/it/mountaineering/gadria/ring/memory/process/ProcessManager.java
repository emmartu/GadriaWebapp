package it.mountaineering.gadria.ring.memory.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import it.mountaineering.gadria.ring.memory.util.CrunchifyThread;


public class ProcessManager {

	private static final String TASKLIST_CMD = "tasklist.exe /FI \"IMAGENAME eq vlc*\" /fo csv /nh";
	private static final String TASKLIST_PID_CMD = "tasklist.exe /FI ";
	private static final String TASKLIST_PID_CMD_TAIL = " /fo csv /nh";	
	private static final CharSequence NO_TASK = "No tasks";
	private static final String TASKKILL_CMD = "taskkill.exe /F /FI ";
	private static Map<String, Set<String>> processThreadIdToPidMap = new HashMap<String, Set<String>>();
	private static Map<String,String> processPidToVlcThreadIdMap = new HashMap<String,String>();
	private static List<String> processList = new ArrayList<String>();

	static ReadWriteLock lock = new ReentrantReadWriteLock();
	static Lock writeLock = lock.writeLock();
	
	static DateFormat format = new SimpleDateFormat("HH:mm:ss");
	
	
	public static void createProcess(String vlcLauncerName, int timeout, String execString) throws IOException {
		try {
			writeLock.tryLock(1, TimeUnit.SECONDS);
			Process proc = 
			Runtime.
			   getRuntime().
			   exec(execString);
			
			addProcess(CrunchifyThread.timeOut, vlcLauncerName);
			System.out.println("name :"+vlcLauncerName);				
			  
			StreamGobbler err = new StreamGobbler( proc.getErrorStream(), "ERROR_"+vlcLauncerName);
			StreamGobbler in = new StreamGobbler( proc.getInputStream(), "INPUT_"+vlcLauncerName);
			err.start();
			in.start();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			writeLock.unlock();
		}		
	}
	
	public static String addProcess(int timeout, String vlcLauncerName) {
		Date date = new Date();
		System.out.println("addProcess, vlcPID: "+vlcLauncerName+" - to: "+timeout+" time: "+format.format(date));
		String PID = "";
		Set<String> processVlcIdToPidSet = null;

		try {
			if (processThreadIdToPidMap.get(vlcLauncerName) == null) {
				processVlcIdToPidSet = new HashSet<String>();
				processThreadIdToPidMap.put(vlcLauncerName, processVlcIdToPidSet);
			} else {
				processVlcIdToPidSet = processThreadIdToPidMap.get(vlcLauncerName);
			}

			PID = getPidFromRunningProcesses(processVlcIdToPidSet);
			System.out.println("addProcess, vlcLauncerName: "+vlcLauncerName+" get PID: "+PID);

			if (PID != "") {
				processVlcIdToPidSet.add(PID);
				processPidToVlcThreadIdMap.put(PID, vlcLauncerName);
				writeLock.unlock();
				ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		        scheduler.schedule(new KillProcessTask(PID,vlcLauncerName), timeout, TimeUnit.MILLISECONDS);
		        scheduler.shutdown();
			}else {
				writeLock.unlock();
			}
		} finally {
			System.out.println("addProcess, vlcLauncerName: "+vlcLauncerName+" actual map: "+processVlcIdToPidSet.toString());
		}

		return PID;
	}

	public static void clearMap(String PID, String vlcLauncerPID) {
		
		try {
			if (!processThreadIdToPidMap.get(vlcLauncerPID).isEmpty() && 
				 processThreadIdToPidMap.get(vlcLauncerPID).contains(PID)) {
				
				processThreadIdToPidMap.get(vlcLauncerPID).remove(PID);
				System.out.println("clearMap vlcLauncerPID: "+vlcLauncerPID+" - PID removed");
			}
	
			if (processThreadIdToPidMap.get(vlcLauncerPID).isEmpty()) {
				processThreadIdToPidMap.remove(vlcLauncerPID);
				System.out.println("clearMap vlcLauncerPID: "+vlcLauncerPID+" - vlcLauncerPID removed");
			}

			if (!processPidToVlcThreadIdMap.get(PID).isEmpty() && 
				 processPidToVlcThreadIdMap.get(PID).contains(vlcLauncerPID)) {
					
				processPidToVlcThreadIdMap.remove(PID);
				System.out.println("clearMap vlcLauncerPID: "+vlcLauncerPID+" - PID removed");
			}
		
			processList.remove(PID);
		}finally {
			writeLock.unlock();
			System.out.println("clearMap end, vlcPID: "+vlcLauncerPID+" actual map: "+processThreadIdToPidMap.toString());
		}
	}

	public static void killPId(String vlcLauncerPID, String PID) {

		try {
			writeLock.tryLock(1, TimeUnit.SECONDS);
			String line;
			System.out.println("Sono il processo: " + vlcLauncerPID +" ammazzo il PID: "+PID);

			Process p = Runtime.getRuntime().exec(TASKKILL_CMD +"\"PID eq "+ PID +"\"");
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

			while ((line = input.readLine()) != null) {
				if (!line.trim().equals("")) {
					System.out.println("Running processes line: " + line);
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

		try {
			String line;
			Process p = Runtime.getRuntime().exec(TASKLIST_PID_CMD+"\"PID eq "+pID+" \" "+TASKLIST_PID_CMD_TAIL);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			while ((line = input.readLine()) != null) {
				if (!line.trim().equals("")) {
					System.out.println("Running processes line: " + line);

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
	
	public static String getPidFromRunningProcesses(Set<String> processVlcIdToPidSet) {
		String PID = "";
		try {
			String line;
			Process p = Runtime.getRuntime().exec(TASKLIST_CMD);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			while ((line = input.readLine()) != null) {
				if (!line.trim().equals("")) {
					System.out.println("Running processes line: " + line);
					String[] processItems = null;
										
					if (!line.contains(NO_TASK)) {
						processItems = line.split(",");
					}

					clearDeadProcesses(processItems[1]);

					if (processItems != null) {
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
		if(processList.contains(deadProcessPid)) {
			String vlcProcessId = processPidToVlcThreadIdMap.get(deadProcessPid);
			processThreadIdToPidMap.get(vlcProcessId).remove(deadProcessPid);
			processPidToVlcThreadIdMap.remove(deadProcessPid);
			processList.remove(deadProcessPid);
		}		
	}


	/*
	public static void clearMap() {

		for (String pid : processPidMap.keySet()) {
			Map<String, Integer> processMap = processPidMap.get(pid);

			if (processMap.isEmpty()) {
				processPidMap.remove(pid);
			}
		}
	}
	 */
	

	public static void main(String[] args) {
		// killPId("1820");
	}

}

class StreamGobbler extends Thread {
	public StreamGobbler( InputStream in, String type) {
		this.in = in;
		this.type = type;
	}
	private InputStream in;
	private String type;

	public void run() {
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader(in));
			String line = null;
			while (( line = reader.readLine()) != null) {
				System.out.println( type + ": " + line);
			}
		} catch ( IOException e) {
			e.printStackTrace();
		}
	}
}
