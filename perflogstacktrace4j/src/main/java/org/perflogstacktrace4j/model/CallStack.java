package org.perflogstacktrace4j.model;

import org.perflogstacktrace4j.spi.CallStackListener;
import org.perflogstacktrace4j.spi.CallStackListenerSupport;

/**
 * Applicative CallStack (~ StackTrace) = push/pop Stack of CallStack Element (~java.lang.StackTraceElement) 
 * 
 * NOTE: contrarily to java, a CallStack can be detached-from/attached-to a Thread 
 * 
 * It corresponds to a "Trace" in Google Spanner / Zipkin / OppenTracing, and CallStackElement corresponds to a "Span".
 * 
 * <PRE>
 *                         <-\
 *                             push()
 *   +------------------+         
 *   | callStackElt Curr|    <-- curr stack position
 *   +------------------+       
 *   |                         pop()
 *   |  ..                 <-/
 *   |  ..
 *   +------------------+
 *   | callStackElt 2   |
 *   +------------------+
 *   | callStackElt 1   |
 *   +------------------+
 * </PRE>
 * 
 */
public class CallStack {

	private static final int DEFAULT_ALLOC_INCR_STACK_LEN = 5;
	
	private CallStackElt curr;
	private CallStackElt[] stackElts;
	
	private final CallStackListenerSupport callStackListeners = new CallStackListenerSupport(); 

	// ------------------------------------------------------------------------
	
	public CallStack() {
		this.stackElts = new CallStackElt[1];
		this.stackElts[0] = new CallStackElt(this, 0, null);
		reallocStackEltArray(10);
		this.curr = stackElts[0];
	}
	

	// ------------------------------------------------------------------------
	
	public CallStackElt curr() {
		return curr;
	}
	
	public void addCallStackListener(CallStackListener l) {
		callStackListeners.addListener(l);
	}

	public void removeCallStackListener(CallStackListener l) {
		callStackListeners.removeListener(l);
	}

	// ------------------------------------------------------------------------
	
	/*pp*/ void onAttachToThread(Thread thread) {
		callStackListeners.fireOnAttachToThread(this, thread);
	}

	/*pp*/ void onAttachFromThread(Thread thread) {
		callStackListeners.fireOnDetachFromThread(this, thread);
	}

	// internal
	// ------------------------------------------------------------------------
	
	private void reallocStackEltArray(int stackLen) {
		CallStackElt[] prevStackElts = stackElts;
		CallStackElt[] newStackElts = new CallStackElt[stackLen];
		System.arraycopy(prevStackElts, 0, newStackElts, 0, prevStackElts.length);
		for(int i = prevStackElts.length; i < stackLen; i++) {
			newStackElts[i] = new CallStackElt(this, i, newStackElts[i-1]);
			newStackElts[i - 1].pusher = new StackPusher(newStackElts[i]);
		}
		this.stackElts = newStackElts;
	}


	/*pp*/ StackPopper doPush(CallStackElt pushedElt) {
		if (pushedElt.pusher == null) {
			reallocStackEltArray(this.stackElts.length + DEFAULT_ALLOC_INCR_STACK_LEN);
		}
		this.curr = pushedElt;
		pushedElt.onPushSetStartTime();
		callStackListeners.fireOnPush(pushedElt);
		return pushedElt.popper;
	}

   /*pp*/ StackPopper doPushWithParentStartTime(CallStackElt pushedElt) {
        if (pushedElt.pusher == null) {
            reallocStackEltArray(this.stackElts.length + DEFAULT_ALLOC_INCR_STACK_LEN);
        }
        this.curr = pushedElt;
        pushedElt.onPushSetParentStartTime();
		callStackListeners.fireOnPush(pushedElt);
        return pushedElt.popper;
    }

	/*pp*/ void doPop(CallStackElt poppedElt) {
		this.curr = poppedElt.getParentCallStackElt();
		poppedElt.onPopSetEndTime();
		callStackListeners.fireOnPop(poppedElt);
	}

	/*pp*/ void doProgressStep(CallStackElt currElt, int incr, String progressMessage) {
		//assert currElt == this.curr;
		callStackListeners.fireOnProgressStep(currElt, incr, progressMessage);
	}

}
