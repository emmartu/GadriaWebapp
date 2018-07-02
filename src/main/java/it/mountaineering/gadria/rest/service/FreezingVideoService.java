package it.mountaineering.gadria.rest.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import it.mountaineering.gadria.ring.memory.scheduled.task.VlcLauncherScheduledTask;

@Path("/freezing")
public class FreezingVideoService {

	public static final SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmm");

	@GET
	@Path("/video/{from}/{to}")
	@Produces("application/json")
	public List<String> getVideoFromDateToDate(@PathParam("from") String fromDatetime, @PathParam("to") String toDatetime) {

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

		//return Response.status(200).entity(freezingVideoLog.toString()).build();
		return freezingVideoLog;
	}

}