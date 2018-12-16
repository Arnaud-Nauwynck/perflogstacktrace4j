package org.perflogstacktrace4j.model;

import java.util.Map;

/**
 * Helper object to configure then push new CallStackElt on its corresponding parent CallStack
 *  
 * This is the builder pattern for configuring the information passed to CallStack.push()
 *
 * Implementation note: object are allocated only once for fast performance / no GC overhead 
 * ... but you are not allowed to keed reference to it!
 */
public final class StackPusher {
	
	private final CallStackElt pushedElt;
	
	/*pp*/ StackPusher(CallStackElt pushedElt) {
		this.pushedElt = pushedElt;
	}
	
	public StackPopper push() {
		return pushedElt.ownerStack.doPush(pushedElt);
	}

	public StackPopper pushWithParentStartTime() {
	    return pushedElt.ownerStack.doPushWithParentStartTime(pushedElt);
    }

	public StackPusher withName(String className, String name) {
		pushedElt.className = className;
		pushedElt.name = name;
		return this;
	}
	
	/** alias for withParam() */
	public StackPusher p(String paramName, Object value) {
		return withParam(paramName, value);
	}

	public StackPusher withParam(String paramName, Object value) {
		pushedElt.putParam(paramName, value);
		return this;
	}

	public StackPusher withParams(Map<String,Object> p) {
		pushedElt.putAllParams(p);
		return this;
	}

	public StackPusher withInheritableProp(String paramName, Object value) {
		pushedElt.putInheritableProp(paramName, value);
		return this;
	}

	public StackPusher withAllInheritableProps(Map<String,Object> p) {
		pushedElt.putAllInheritableProps(p);
		return this;
	}

	public StackPusher withProgressExpectedCount(int p) {
		pushedElt.progressExpectedCount = p;
		return this;
	}

}