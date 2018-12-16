package org.perflogstacktrace4j.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public final class ExUtils {

	/* private to force all static */
	private ExUtils() {}
	
	public static String currentStackTraceShortPath() {
		return stackTraceToString(new Exception());
	}
	
	public static String stackTraceToShortPath(Throwable ex) {
		// TODO .. shorten..
		return stackTraceToString(ex);
	}

	/** @return ex.printStackTrace() as String */
	public static String stackTraceToString(Throwable ex) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(buffer);
		ex.printStackTrace(out);
		return buffer.toString();
	}
	
}
