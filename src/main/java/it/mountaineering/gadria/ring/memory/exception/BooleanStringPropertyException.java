package it.mountaineering.gadria.ring.memory.exception;

import it.mountaineering.gadria.ring.memory.exception.WebcamPropertyIDException;

public class BooleanStringPropertyException extends WebcamPropertyIDException {

	public BooleanStringPropertyException(String exception) {
		super();
		mailSender.sendPropertiesExceptionEmail(exceptionPrefixString, exception, exceptionSuffixString);
	}

}
