package org.perflogstacktrace4j.spi;

import java.util.Map;

import org.perflogstacktrace4j.model.CallStack;
import org.perflogstacktrace4j.model.CallStackElt;

/**
 * Listener class to attach to a CallStack to listen to push-pop / detach-attach / progressStep / log .. events
 */
public abstract class CallStackListener {

	public abstract void onPush(CallStackElt stackElt);
	
	public abstract void onPop(CallStackElt stackElt);

	public abstract void onProgressStep(CallStackElt stackElt, int incr, String progressMessage);

	public abstract void onLog(String msg, Map<String,Object> namedValues);

	public abstract void onAttachCallStackToThread(CallStack stack, Thread thread);
	public abstract void onDetachCallStackFromThread(CallStack stack, Thread thread);

}
