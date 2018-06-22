package it.mountaineering.gadria.ring.memory.exception;

import it.mountaineering.gadria.ring.memory.exception.PropertiesException;

public class CSVFormatPropertiesException extends PropertiesException {

	public CSVFormatPropertiesException(String exception) {
		super();
		mailSender.sendPropertiesExceptionEmail(exceptionPrefixString, exception, exceptionSuffixString);
	}

}
