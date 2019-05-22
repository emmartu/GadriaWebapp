package it.mountaineering.gadria.ring.memory.util;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import it.mountaineering.gadria.ring.memory.process.ProcessManager;

public class CrunchifyThread {
	
	static DateFormat format = new SimpleDateFormat("HH:mm:ss");
	static String WEBCAM_IP_1="217.92.174.208";
	static String WEBCAM_IP_2="194.126.117.99";
	static String WEBCAM_IP_3="217.92.174.208";
	static String WEBCAM_IP_4="87.118.253.232";
	public static final int time = 15;
	public static final int timeOut = 20000;
	public static final String VIDEOLAN_EXE_PATH="C:\\Program Files\\VideoLAN\\VLC\\vlc.exe";
	public static final String VLC_VIDEO_RECORDER_BAT_1 = "C:\\Users\\Lele\\Documents\\LavoroWebCamMobotix\\TEST\\conf\\VlcVideoRecorder.bat";
	String storageFileFullPath = "C:\\Users\\Lele\\Documents\\LavoroWebCamMobotix\\TEST\\TEST_ONLINE_CAM\\";
	String exec_1 = "cmd /c start /B \"\" "+VLC_VIDEO_RECORDER_BAT_1+" TEST "+WEBCAM_IP_4+" "+storageFileFullPath+" "+time+" \""+VIDEOLAN_EXE_PATH+"\" ";
	
 
	public static void main(String args[]) {
		for (int i = 0; i < 2; i++) {
			int time = 30;
			if(i==1) {
				time=15;
			}
			if(i==1) {
				time=10;
			}
			
			//ThreadTest t1 = new ThreadTest(WEBCAM_IP_1, "W1", time);
			//t1.start();
			ThreadTest t2 = new ThreadTest(WEBCAM_IP_2, "W2", time);
			t2.start();			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//ThreadTest t3 = new ThreadTest(WEBCAM_IP_3, "W3");
		//t3.start();

		/*
		try {
			Thread.sleep(500000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		System.out.println("END");
	}
}
 
class ThreadTest extends Thread {
	String webcam_ip;
	String webcam_id;
	int time;
	public ThreadTest(String str, String id, int time) {
		webcam_ip = str;
		webcam_id = id;
		this.time = time;
	}
 
	public void run() {
		for (int i = 0; i < 1; i++) {
			
			long threadPid = getId();
			String tName = getName();
			System.out.println("Thred name " + tName + "- id " + threadPid + " starting");
			String storageFileFullPath = "C:\\Users\\Lele\\Documents\\LavoroWebCamMobotix\\TEST\\TEST_ONLINE_CAM\\test_"+webcam_id+"_"+webcam_ip+"_loop_"+i+".mp4";
			//String storageFileFullPath = "C:\\Users\\Lele\\Documents\\LavoroWebCamMobotix\\TEST\\TEST_ONLINE_CAM\\test_"+webcam_id+"_"+webcam_ip+".mp4";
			String exec_1 = "cmd /c start /B \"\" "+CrunchifyThread.VLC_VIDEO_RECORDER_BAT_1+" TEST "+webcam_ip+" "+storageFileFullPath+" "+time+" \""+CrunchifyThread.VIDEOLAN_EXE_PATH+"\" ";
			
			//System.out.println("exec_1: "+exec_1);
			try {
				ProcessManager.createProcess(tName, CrunchifyThread.timeOut, exec_1);				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("Test Finished for: " + getName());
	}

}