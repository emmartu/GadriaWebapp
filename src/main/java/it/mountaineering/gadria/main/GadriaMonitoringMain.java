package it.mountaineering.gadria.main;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import it.mountaineering.gadria.ring.memory.main.RingMemoryMain;

public class GadriaMonitoringMain implements ServletContextListener {

	public static RingMemoryMain ringMemoryMain;
	public static String installationPath = "";
	public static ServletContext servletContext;
	
	public GadriaMonitoringMain() {
		ringMemoryMain = new RingMemoryMain();
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
        System.out.println("contextInitialized");
        servletContext = sce.getServletContext();
		installationPath = servletContext.getInitParameter("InstallationPath");
        System.out.println("installationPath ==> "+installationPath);
		ringMemoryMain.start(new String[] {installationPath});
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ringMemoryMain.vlcTimer.cancel();
		ringMemoryMain.pictureTakerTimer.cancel();		
	}
}