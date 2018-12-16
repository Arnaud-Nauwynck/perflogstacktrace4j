package org.perflogstacktrace4j;

import org.perflogstacktrace4j.model.CallStack;
import org.perflogstacktrace4j.model.CallStackElt;
import org.perflogstacktrace4j.model.StackPopper;
import org.perflogstacktrace4j.model.StackPusher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facade extension of slf4j Logger, for Trace
 * 
 * This is a helper wrapper class for currying <code>clazz</code> parameter in ThreadLocalCallStack.push() /pop(), Slf4J.Logger, etc..
 * 
 * similar to a Slf4J Logger, used as:
 * <PRE>
 * private static final CallStackTracer TRACER = CallStackTracer.getTracer(SomeClass.class);
 * 
 * try (StackPopper pop = TRACER.push("someMethod")) {  // => call ThreadLocalCallStack.push(SomeClass.class, "someMethod");
 * 	  ..
 *    TRACER.info("some message");
 *    TRACER.info("some message with detected indexed parameters..  a:{}, b:{}", a, b);
 *    TRACER.infoNV("some message with detected named parameter.. a:", a, " b:", b);
 *    
 * }  // => call AppThreadLocalCallStack.pop()
 * 
 * </PRE>
 * 
 */
public final class CallStackTracer {

	private final Class<?> clazz;
	
	private final Logger slf4jLogger;

	private CallStackTracer(Class<?> clazz) {
		this.clazz = clazz;
		this.slf4jLogger = LoggerFactory.getLogger(clazz);
	}
	
	public static CallStackTracer getTracer(Class<?> clazz) {
		return new CallStackTracer(clazz);
	}
	
	
	/**
	 * sample usage:
	 * <PRE>
	 * try (StackPopper pop = TRACER.meth("someMethod")
	 * 		.withParam("param1", value1)
	 * 		.withParam("param2", value2)
	 * 		.withInheritedProp("prop3", value3)
	 * 		.withLogger(LOG, LogLevel.INFO, LogLevel.INFO)  // <= log INFO on push() and INFO on pop()
	 * 		.push()) { // => call ThreadLocalCallStack.push(SomeClass.class.getName(), "someMethod");
	 *   ..
	 * } // => call ThreadLocalCallStack.pop()
	 * </PRE>
	 */
	public StackPusher meth(String methodName) {
		return ThreadLocalCallStack.meth(clazz.getName(), methodName);
	}

	/**
	 * sample usage:
	 * <PRE>
	 * try (StackPopper pop = TRACER.push("someMethod")) {  // => call ThreadLocalCallStack.push(SomeClass.class.getName(), "someMethod");
	 *   ..
	 * } // => call ThreadLocalCallStack.pop()
	 * </PRE>
	 */
	public StackPopper push(String methodName) {
		return ThreadLocalCallStack.push(clazz.getName(), methodName);
	}

	public CallStack currCallStack() {
		return ThreadLocalCallStack.currCallStack();
	}

	public CallStackElt currStackElt() {
		return ThreadLocalCallStack.currStackElt();
	}

	public Logger logger() {
		return slf4jLogger;
	}

	public void info(String msg) {
		slf4jLogger.info(msg);
	}

}
