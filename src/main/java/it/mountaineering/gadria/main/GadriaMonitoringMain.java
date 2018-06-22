package it.mountaineering.gadria.main;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import it.mountaineering.gadria.ring.memory.main.Main;

public class GadriaMonitoringMain implements ServletContextListener {

	Main ringMemoryMain;
	public static String installationPath = "";
	
	public GadriaMonitoringMain() {
		ringMemoryMain = new Main();
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
        System.out.println("contextInitialized");
		installationPath = sce.getServletContext().getInitParameter("InstallationPath");
        System.out.println("installationPath ==> "+installationPath);
		ringMemoryMain.start(new String[] {installationPath});
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		
	}
}