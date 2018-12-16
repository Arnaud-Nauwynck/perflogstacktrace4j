package org.perflogstacktrace4j.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.perflogstacktrace4j.utils.ThreadTimeUtils;

/**
 * Element of a CallStack
 * 
 * <PRE>
 *                          "try { toPop = pusher
 *                                        .param("p1, "v1").param("p2", "v2")
 *                                        .inheritedProp("prop1", "p1"). ...
 *                                        .push()"   
 *                          when entering new "method" : push new element on stack
 *                                <--
 *                                   \
 *                                   /
 *   +-------------------------+
 *   | callElt Curr            |    <-- curr stack position
 *   |   - clss, meth          |
 *   |   - startTimes          |
 *   |      (elapsed,cpu,user) |
 *   |   - params              |
 *   |   - inheritableProps    |
 *   |   - inheritedProps      |
 *   |   - pushPopHandlers     |  
 *   +-------------------------+     \
 *   |                               /
 *   |                            <--  
 *   |                      "} finally { toPop.close(); }"
 *   |                      when exiting curr "method" : pop element on stack
 *   |  ..                
 *   |  ..
 *   +-------------------------+
 *   | callElt 2               |
 *   +-------------------------+
 *   |  callElt 1              |
 *   +-------------------------+
 * </PRE>
 */
public final class CallStackElt {

	/*pp*/ final CallStack ownerStack;
	private final CallStackElt parentCallStackElt;
	private final int stackEltIndex;
	
	// TODO
	/*pp*/ String className;
	/*pp*/ String name;

	private Map<String,Object> params;
	private Map<String,Object> inheritableProps;
	
	/* lazily computed from inheritableProps + parentCallStackElt...  **/
	private Map<String,Object> inheritedProps;
	
	/*pp*/ StackPusher pusher;
	/*pp*/ StackPopper popper;

	private long startTime;
	private long threadCpuStartTime;
	private long threadUserStartTime;
	
	private long endTime;
	private long threadCpuEndTime;
	private long threadUserEndTime;
	
	/*pp*/ int progressExpectedCount;
	private int progressIndex;
	private String progressMessage;
	
	// ------------------------------------------------------------------------
	
	public CallStackElt(CallStack ownerStack, int stackEltIndex, CallStackElt parentCallStackElt) {
		super();
		this.ownerStack = ownerStack;
		this.stackEltIndex = stackEltIndex;
		this.parentCallStackElt = parentCallStackElt;
		// this.pusher .. initialized in parent stack with next stack elt on stack
		this.popper = new StackPopper(this);
	}	
	
	// ------------------------------------------------------------------------
	
	public StackPusher pusher(String className, String name) {
		return pusher.withName(className, name);
	}

	/*pp*/ void onPushSetStartTime() {
		this.startTime = ThreadTimeUtils.getTime();
		this.threadUserStartTime = ThreadTimeUtils.getCurrentThreadUserTime();
		this.threadCpuStartTime = ThreadTimeUtils.getCurrentThreadCpuTime();
	}

    /*pp*/ void onPushSetParentStartTime() {
        CallStackElt parent = parentCallStackElt;
        this.startTime = parent.startTime;
        this.threadUserStartTime = parent.threadUserStartTime;
        this.threadCpuStartTime = parent.threadCpuStartTime;
    }
	   
	/*pp*/ void onPopSetEndTime() {
		this.threadCpuEndTime = ThreadTimeUtils.getCurrentThreadCpuTime();
		this.threadUserEndTime = ThreadTimeUtils.getCurrentThreadUserTime();
		this.endTime = ThreadTimeUtils.getTime();
		
		this.progressExpectedCount = 0;
		this.progressIndex = 0;
		this.progressMessage = null;		
	}

	/*pp*/ void onProgressStep(int incr, String progressMessage) {
		this.progressIndex += incr;
		this.progressMessage = progressMessage;
	}
	
	// public getter (value are immutable after push(), until pop() is called)
	// private accessor, cf corresponding Pusher
	// ------------------------------------------------------------------------

	public CallStack getOwnerStack() {
		return ownerStack;
	}
	
	public CallStackElt getParentCallStackElt() {
		return parentCallStackElt;
	}
	
	public int getStackEltIndex() {
		return stackEltIndex;
	}

	public String getClassName() {
		return className;
	}

	public String getName() {
		return name;
	}

	public String[] getPath() {
		String[] res = new String[stackEltIndex+1];
		CallStackElt curr = this;
		for (int i = stackEltIndex; i >= 0; i--, curr = curr.getParentCallStackElt()) {
			res[i] = curr.getClassName() + ":" + curr.getName();
		}
		return res;
	}
	
	// return pointer, should return unmodifiable ref
	public Map<String, Object> getParams() {
		if (params == null) return Collections.emptyMap();
		return params;
	}
	
	public Map<String, Object> getInheritableProps() {
		if (inheritableProps == null) return Collections.emptyMap();
		return inheritableProps;
	}
	
	public Map<String, Object> getInheritedProps() {
		if (inheritedProps == null) {
			Map<String,Object> tmpres = new HashMap<String,Object>();
			if (parentCallStackElt != null) {
				tmpres.putAll(parentCallStackElt.getInheritedProps()); // **recurse ***
			}
			if (inheritableProps != null) {
				tmpres.putAll(inheritableProps);
			}
			inheritedProps = tmpres;
		}
		return inheritedProps;
	}
	
	public long getStartTime() {
		return startTime;
	}

	public long getStartTimeApproxMillis() {
		return ThreadTimeUtils.nanosToApproxMillis(startTime);
	}

	public long getThreadCpuStartTime() {
		return threadCpuStartTime;
	}
	
	public long getThreadUserStartTime() {
		return threadUserStartTime;
	}
	
	public long getEndTime() {
		return endTime;
	}
	
	public long getElapsedTime() {
		return endTime - startTime;
	}
	
	public long getThreadCpuEndTime() {
		return threadCpuEndTime;
	}
	
	public long getThreadUserEndTime() {
		return threadUserEndTime;
	}
	
	public int getProgressExpectedCount() {
		return progressExpectedCount;
	}
	
	public int getProgressIndex() {
		return progressIndex;
	}
	
	public String getProgressMessage() {
		return progressMessage;
	}
	
	// ------------------------------------------------------------------------

	/** called from Pusher */
	void putParam(String paramName, Object value) {
		if (params == null) params = new HashMap<String,Object>();
		params.put(paramName, value);
	}

	/** called from Pusher */
	void putAllParams(Map<String,Object> p) {
		if (params == null) params = new HashMap<String,Object>();
		params.putAll(p);
	}

	/** called from Pusher */
	void putInheritableProp(String paramName, Object value) {
		if (inheritableProps == null) inheritableProps = new HashMap<String,Object>();
		inheritableProps.put(paramName, value);
	}

	/** called from Pusher */
	void putAllInheritableProps(Map<String,Object> p) {
		if (inheritableProps == null) inheritableProps = new HashMap<String,Object>();
		inheritableProps.putAll(p);
	}


}
