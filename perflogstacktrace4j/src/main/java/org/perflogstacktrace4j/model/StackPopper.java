package org.perflogstacktrace4j.model;

import java.io.Closeable;

/**
 * Helper object to pop CallStackElt from its corresponding parent CallStack
 * 
 * Implementation note: object are allocated only once for fast performance / no GC overhead 
 * ... but you are not allowed to keed reference to it!
 * 
 */
public final class StackPopper implements Closeable {

	private final CallStackElt callStackElt;
	
	public StackPopper(CallStackElt callStackElt) {
		this.callStackElt = callStackElt;
	}

	@Override
	public void close() {
		callStackElt.ownerStack.doPop(callStackElt);
	}

	public StackPopper progressStep(int incr, String progressMessage) {
		callStackElt.onProgressStep(incr, progressMessage);
		callStackElt.ownerStack.doProgressStep(callStackElt, incr, progressMessage);
		return this;
	}

	public StackPopper withParamValue(String paramName, Object value) {
		callStackElt.putParam(paramName, value);
		return this;
	}

	/** alias for withParamValue("reurn", value) */
	public StackPopper withReturnValue(Object value) {
		return withParamValue("return", value);
	}

	/**
	 * usefull for returning value and adding it as param "return" to current CallStackElt 
	 * cf also LocalCallStack.pushPopParentReturn() ... 
	 */
	public <T> T returnParamValue(String paramName, T value) {
		withParamValue(paramName, value);
		return value;
	}
	
	public <T> T returnValue(T value) {
		withParamValue("return", value);
		return value;
	}
	
	/** idem returnValue(Object) with primitive value to avoid boxing/unboxing*/
	public boolean returnValue(boolean value) {
		withParamValue("return", value);
		return value;
	}
	
	/** idem returnValue(Object) with primitive value to avoid boxing/unboxing*/
	public int returnValue(int value) {
		return returnParamValue("return", value);
	}
	
	/** idem returnValue(Object) with primitive value to avoid boxing/unboxing*/
	public long returnValue(long value) {
		return returnParamValue("return", value);
	}


	
    public <T extends Throwable> T returnException(T ex) {
        String eltName = "exception-" + ex.getClass().getSimpleName();
        returnException(eltName, ex);
        return ex;
    }

    /** idem pushPopParentException() but using custom exception eltName instead of default (<code>eltName= "exception-" + ex.getClass().getSimpleName()</code>) */
    public void returnException(String eltName, Throwable ex) {
		String className = callStackElt.className;
        StackPopper toPop = callStackElt.pusher(className, eltName).withParam("ex", ex).pushWithParentStartTime();
        toPop.close();
    }

}
