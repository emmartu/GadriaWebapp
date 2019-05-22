package it.mountaineering.gadria.ring.memory.process;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CheckProcessTask implements Runnable {

	static DateFormat format = new SimpleDateFormat("HH:mm:ss");
	String PID;
	String vlcLauncerPID;

	public CheckProcessTask(String pID, String vlcLauncerPID) {
		this.PID = pID;
		this.vlcLauncerPID = vlcLauncerPID;
	}

	@Override
	public void run() {
		Date date = new Date();
		System.out.println("KillProcessTask vlcLauncerPID: "+vlcLauncerPID+" - PID:"+PID+" - time: "+format.format(date));
		boolean isRunning = ProcessManager.getRunningProcess(PID);
	}
	
	public static void main(String[] args) {
		Date date = new Date();
		System.out.println("KillProcessTask main: - time: "+format.format(date));
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		int delay = 5;
        scheduler.schedule(new KillProcessTask("1","T1"), delay, TimeUnit.SECONDS);
        scheduler.shutdown();
	}


}