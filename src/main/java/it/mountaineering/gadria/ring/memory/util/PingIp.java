package it.mountaineering.gadria.ring.memory.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PingIp {

	
	public static boolean runSystemCommand(String command) {

		try {
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader inputStream = new BufferedReader(
					new InputStreamReader(p.getInputStream()));

			String s = "";
			// reading output stream of the command
			while ((s = inputStream.readLine()) != null) {
				//System.out.println(s);
				if(s.contains("Request timed out")) {
					return false;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	public static boolean isPingReachable(String ip) {
		boolean result = runSystemCommand("ping " + ip);
		
		return result;
	}
	
}