package it.mountaineering.gadria.ring.memory.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import it.mountaineering.gadria.ring.memory.exception.PropertiesException;
import it.mountaineering.gadria.ring.memory.scheduled.task.CurrentPictureTakerTask;
import it.mountaineering.gadria.ring.memory.scheduled.task.VlcLauncherScheduledTask;
import it.mountaineering.gadria.ring.memory.util.PropertiesManager;


public class RingMemoryMain {

	private static final java.util.logging.Logger log = Logger.getLogger(RingMemoryMain.class.getName());
	private static final String LOGGING_PROPERTIES = "logging.properties";

	private static final Object _START = "start";
	private static final Object _STOP = "stop";
	private static final String _CONF = "conf\\";
	private static boolean stop = false;

	public static Timer vlcTimer;
	public static VlcLauncherScheduledTask vlcLauncher;

	public static Timer pictureTakerTimer;
	public static CurrentPictureTakerTask pictureTakerLauncher;
	private static String installationPath;

	PropertiesManager prop = new PropertiesManager();
	OutputStream output = null;

    public static void stop(String[] args) {
        log.info("Service has been stopped");
        vlcTimer.cancel();
        pictureTakerTimer.cancel();
        stop = true;
    }
	
    public static void start(String[] args) {
        if(!stop) {
        	installationPath = args[0]+_CONF;
			RingMemoryMain main = new RingMemoryMain();
			try {
				main.setUpLogger();
				log.info("Service is starting");
			} catch (SecurityException se) {
	        	log.info("problem encuntered reading properties file "+se.getMessage());
	        	RingMemoryMain.stop(new String[] {});
				return;
			} catch (FileNotFoundException e) {
	        	log.info("problem encuntered reading properties file "+e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
	        	log.info("problem encuntered reading properties file "+e.getMessage());
				e.printStackTrace();
			}

			try {
				main.setupProperties();
			} catch (PropertiesException e) {
				log.severe(e.getMessage()+" **** the application ecountered a properties exception *** ");
				RingMemoryMain.stop(new String[] {});
				return;
			}
	
			pictureTakerTimer = new Timer();
			pictureTakerLauncher = new CurrentPictureTakerTask();

			if(PropertiesManager.isPictureCaptureEnabled()) {
				main.launchPictureTakerScheduledTasks();			
			}

			vlcTimer = new Timer();
			vlcLauncher = new VlcLauncherScheduledTask();

			if(PropertiesManager.isVideoCaptureEnabled()) {
				main.launchVlcScheduledTasks();			
			}
        }
	}
	
	private void setUpLogger() throws SecurityException, FileNotFoundException, IOException {
		checkSlashesOnPath(installationPath);
        LogManager.getLogManager().readConfiguration(new FileInputStream(installationPath+LOGGING_PROPERTIES));
        Level level = log.getLevel();
		log.info("log level: "+level);
	}
	
	private void setupProperties() throws PropertiesException {
		log.info("****** WEBCAM VIDEO RECORDER MANAGER SETUP PROPERTIES ******");
		PropertiesManager.setConfigFilePath(installationPath);
		PropertiesManager.setupConfigProperties();
	}

	private void launchVlcScheduledTasks() {
		Long videoLength = 0L;
		videoLength = PropertiesManager.getVideoLength();
		Long overlap = PropertiesManager.getOverlap();
		
		Long taskTimePeriod = videoLength-overlap;
		Long millisTaskTimePeriod = 1000*taskTimePeriod;
		vlcTimer.schedule(vlcLauncher, 0, millisTaskTimePeriod);
		vlcTimer.purge();
	}
	
	private void launchPictureTakerScheduledTasks() {
		Long pictureInterval = 0L;
		pictureInterval = PropertiesManager.getPictureInterval();
		
		Long millisTaskTimePeriod = 1000*pictureInterval;
		pictureTakerTimer.schedule(pictureTakerLauncher, 0, millisTaskTimePeriod);
		pictureTakerTimer.purge();
	}

	private String checkSlashesOnPath(String folderPath) {
		if (!folderPath.endsWith("\\")) {
			folderPath += "\\";
		}
		
		return folderPath;
	}

	
	public static void main(String[] args) {
		RingMemoryMain rmm = new RingMemoryMain();
		installationPath = "C:\\Users\\Lele\\Documents\\LavoroWebCamMobotix\\TEST\\";
		rmm.start(new String[] {installationPath});
	}
}
