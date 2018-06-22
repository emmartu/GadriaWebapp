package it.mountaineering.gadria.ring.memory.exception;

import it.mountaineering.gadria.ring.memory.exception.WebcamPropertyIDException;

public class UnreachableIpException extends WebcamPropertyIDException {

	public UnreachableIpException(String exception) {
		super();
		mailSender.sendPropertiesExceptionEmail(exceptionPrefixString, exception, exceptionSuffixString);
	}

}
