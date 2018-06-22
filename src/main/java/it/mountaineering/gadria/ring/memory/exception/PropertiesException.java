package it.mountaineering.gadria.ring.memory.exception;

import it.mountaineering.gadria.ring.memory.mail.sender.MailSender;

public class PropertiesException extends Exception {

	MailSender mailSender = new MailSender();
	
	protected String exceptionPrefixString = "Exception occured reading properties File.";
	protected String exceptionSuffixString = "Timer Task Has Been Stopped";

	public PropertiesException() {
		exceptionPrefixString = "Exception occured reading properties File.";
		exceptionSuffixString = "Timer Task Has Been Stopped";

		/*
		if(Main.vlcLauncher.isHasStarted()) {
			Main.timer.cancel();
			Main.timer.purge();
			
			exceptionSuffixString = "";
		}
		*/
	}

	public PropertiesException(String exception) {		
		mailSender.sendPropertiesExceptionEmail(exceptionPrefixString, exception, exceptionSuffixString);
	}

}
