package it.mountaineering.gadria.rest.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.mountaineering.gadria.ring.memory.scheduled.task.VlcLauncherScheduledTask;

@Path("/freezing")
public class FreezingVideoService {

	public static final SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmm");

	@GET
	@Path("/video/{from}/{to}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVideoFromDateToDate(@PathParam("from") String fromDatetime, @PathParam("to") String toDatetime) {

		String output = "from : " + fromDatetime + " --> to: " + toDatetime;
		System.out.println("output: " + output);

		Date fromDateTimeClass = null;
		Date toDateTimeClass = null;

		try {
			fromDateTimeClass = format.parse(fromDatetime);
			toDateTimeClass = format.parse(toDatetime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<String> freezingVideoLog = VlcLauncherScheduledTask.diskSPaceManager.freezeFilesFromDateToDateFromMemory(fromDateTimeClass,
				toDateTimeClass);

		GenericEntity<List<String>> list = new GenericEntity<List<String>>(freezingVideoLog) {};
	    return Response.ok(list).build();	    
		//return Response.status(200).entity(freezingVideoLog.toString()).build();
	}
	
	@GET
	@Path("/testList")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTestList() {

		List<String> strList = new ArrayList<String>();
		strList.add("test1");
		strList.add("test2");
		strList.add("test3");
		strList.add("test4");

		GenericEntity<List<String>> list = new GenericEntity<List<String>>(strList) {};

		return Response.ok(list).build();	    
	}

	@GET
	@Path("/testListB")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getTestListB() {

		List<String> strList = new ArrayList<String>();
		strList.add("test1");
		strList.add("test2");
		strList.add("test3");
		strList.add("test4");

		return strList;
	}


}