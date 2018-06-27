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
	@Path("/video/{from}/{to}")
	public Response getVideoFromDateToDate(@PathParam("from") String fromDatetime, @PathParam("to") String toDatetime) {
 
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
		
		GadriaMonitoringMain.ringMemoryMain.vlcLauncher.diskSPaceManager.freezeFilesFromDateToDateFromMemory(fromDateTimeClass, toDateTimeClass);

		return Response.status(200).entity(output).build();
	}

}