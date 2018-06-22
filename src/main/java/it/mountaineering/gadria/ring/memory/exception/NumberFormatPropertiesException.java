package it.mountaineering.gadria.ring.memory.exception;

import it.mountaineering.gadria.ring.memory.exception.PropertiesException;

public class NumberFormatPropertiesException extends PropertiesException {

	public NumberFormatPropertiesException(String exception) {
		super();
		mailSender.sendPropertiesExceptionEmail(exceptionPrefixString, exception, exceptionSuffixString);
	}
}