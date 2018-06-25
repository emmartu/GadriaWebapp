package it.mountaineering.gadria.rest.service;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import it.mountaineering.gadria.main.GadriaMonitoringMain;
import it.mountaineering.gadria.ring.memory.util.PropertiesManager;

@Path("/freezing")
public class FreezingVideoService {

	public static final SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyhhmm");
	
	@GET
	@Path("/{from}/{to}")
	public Response getMsg(@PathParam("from") String fromDatetime, @PathParam("to") String toDatetime) {
 
		String output = "from : " + fromDatetime+" --> to: "+toDatetime;
		System.out.println("output: "+output);
 		
		Date fromDateTimeClass = null;
		Date toDateTimeClass = null;
		
		try {
			fromDateTimeClass = format.parse(fromDatetime);
			toDateTimeClass = format.parse(toDatetime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("fromClass: "+fromDateTimeClass);
		System.out.println("toClass: "+toDateTimeClass);
		
		GadriaMonitoringMain.ringMemoryMain.vlcLauncher.diskSPaceManager.freezeFilesFromDateToDateFromMemory(fromDateTimeClass, toDateTimeClass);

		return Response.status(200).entity(output).build();
	}

	public static void main(String[] args) {
		File fileToFreeze = new File("C:\\Users\\Lele\\Documents\\LavoroWebCamMobotix\\TEST\\TEST_FOLDER_VIDEO\\W1\\w1_2018-06-25@19-21-37.530.mp4");

		fileToFreeze.renameTo(new File("C:\\Users\\Lele\\Documents\\LavoroWebCamMobotix\\TEST\\TEST_FOLDER_FREEZED_VIDEO\\"+fileToFreeze.getName()));
	}
}