package org.perflogstacktrace4j;

import org.perflogstacktrace4j.model.CallStack;
import org.perflogstacktrace4j.model.CallStackElt;
import org.perflogstacktrace4j.model.StackPopper;
import org.perflogstacktrace4j.model.StackPusher;
import org.slf4j.Logger;


/**
 * Facade entry point to org.perflogstacktrace4j
 * 
 * This is the association between a Thread and an Applicative CallStack, using ThreadLocal
 */
public final class ThreadLocalCallStack {

	private static final ThreadLocal<CallStack> threadLocal = new ThreadLocal<CallStack>() {
		@Override
		protected CallStack initialValue() {
			return new CallStack();
		}
	};
	
	public static CallStack currCallStack() {
		return threadLocal.get();
	}

	public static CallStackElt currStackElt() {
		return currCallStack().curr();
	}


	/**
	 * This is the main entry point for calling push()/pop() on the current thread call stack
	 * 
	 * sample code (with jdk version >= 8):
	 * <code>
	 * try (StackPopper toPop = LocalCallStack.meth("methodName").push()) {
	 * 
	 * }
	 * </code>
	 * 
	 * using plain old java style (jdk version < 8)
	 * <code>
	 * StackPopper toPop = LocalCallStack.meth("methodName").push();
	 * try {
	 * 
	 * } finally {
	 * 	toPop.close();
	 * }
	 * </code>
	 * 
	 * Advanced example using StackPusher (~ Builder design-pattern):
	 * <code>
	 * try (StackPopper toPop = ThreadLocalCallStack.meth("methodName")
	 * 		.withParam("param1", value1)
	 * 		.withParam("param2", value2)
	 * 		.withInheritedProp("prop3", value3)
	 * 		.withLogger(LOG, LogLevel.INFO, LogLevel.INFO)  // <= log INFO on push() and INFO on pop()
	 * 		// .withLogger(LOG, LogLevel.INFO, LogLevel.DEBUG, 500)  // <= log INFO on push(), and DEBUG on pop(), but INFO when time exceed 500 ms
	 * 		.push()) {
	 * 
	 * }
	 * </code>
	 * 
	 * @param name
	 * @return
	 */
	public static StackPusher meth(String className, String methodName) {
		CallStackElt currStackElt = currStackElt();
		return currStackElt.pusher(className, methodName);
	}

	/** alias for <code>meth(logger.getName(), String methodName)</code> */
	public static StackPusher meth(Logger logger, String methodName) {
        String className = logger.getName(); 
        CallStackElt currStackElt = currStackElt();
        return currStackElt.pusher(className, methodName);
    }
   
	/** alias for <code>meth(className, name).push()</code> 
	 * using this shor tsyntax, it is not possible to configure StackElt with parameters,properties,logger...
	 * sample code:
	 * <code>
	 * try (StackPopper toPop = LocalCallStack.push(getClass().getName(), "methodName")) {
	 * 
	 * }
	 * </code>
	 */
	public static StackPopper push(String className, String methodName) {
		return meth(className, methodName).push();
	}
    
}
