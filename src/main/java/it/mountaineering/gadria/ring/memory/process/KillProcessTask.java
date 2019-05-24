package it.mountaineering.gadria.ring.memory.process;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

class KillProcessTask implements Runnable {
	private static final java.util.logging.Logger log = Logger.getLogger(KillProcessTask.class.getName());

	static DateFormat format = new SimpleDateFormat("HH:mm:ss");
	String PID;
	String vlcLauncerPID;

	public KillProcessTask(String pID, String vlcLauncerPID) {
		this.PID = pID;
		this.vlcLauncerPID = vlcLauncerPID;
	}

	@Override
	public void run() {
		Date date = new Date();
		log.info("KillProcessTask vlcLauncerPID: "+vlcLauncerPID+" - PID:"+PID+" - time: "+format.format(date));
		ProcessManager.killPId(vlcLauncerPID, PID);
	}
	
}