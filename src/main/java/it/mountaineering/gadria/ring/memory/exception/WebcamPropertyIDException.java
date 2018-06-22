package it.mountaineering.gadria.ring.memory.exception;

import it.mountaineering.gadria.ring.memory.exception.PropertiesException;

public class WebcamPropertyIDException extends PropertiesException {

	public WebcamPropertyIDException() {
		super();
	}

	public WebcamPropertyIDException(String exception) {
		super();
		mailSender.sendPropertiesExceptionEmail(exceptionPrefixString, exception, exceptionSuffixString);
	}
}