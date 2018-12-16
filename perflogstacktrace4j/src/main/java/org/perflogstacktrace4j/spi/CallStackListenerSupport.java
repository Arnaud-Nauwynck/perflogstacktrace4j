package org.perflogstacktrace4j.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.perflogstacktrace4j.model.CallStack;
import org.perflogstacktrace4j.model.CallStackElt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallStackListenerSupport {
	
	private static final Logger LOG = LoggerFactory.getLogger(CallStackListenerSupport.class);

	private Object lock = new Object();
	
	// copy on write
	private CallStackListener[] callStackListeners = new CallStackListener[0];

	public CallStackListenerSupport() {
	}
	
	public void addListener(CallStackListener l) {
		synchronized(lock) {
			CallStackListener[] prev = callStackListeners;
			int len = prev.length;
			CallStackListener[] chg = new CallStackListener[len + 1];
			System.arraycopy(prev, 0, chg, 0, len);
			chg[len] = l;
			this.callStackListeners = chg;
		}
	}

	public void removeListener(CallStackListener l) {
		synchronized(lock) {
			List<CallStackListener> ls = new ArrayList<>(Arrays.asList(callStackListeners));
			ls.remove(l);
			this.callStackListeners = ls.toArray(new CallStackListener[ls.size()]);
		}
	}

	public void fireOnPush(CallStackElt pushedElt) {
		final CallStackListener[] listeners = callStackListeners;
		if (listeners.length != 0) {
			for (CallStackListener listener : listeners) {
				try {
					listener.onPush(pushedElt);
				} catch(Exception ex) {
					LOG.error("Failed to fire event onPush()! .. ignore, no rethrow", ex);
				}
			}
		}
	}

	public void fireOnPop(CallStackElt poppedElt) {
		final CallStackListener[] listeners = callStackListeners;
		if (listeners.length != 0) {
			for (CallStackListener listener : listeners) {
				try {
					listener.onPop(poppedElt);
				} catch(Exception ex) {
					LOG.error("Failed to fire event onPop()! .. ignore, no rethrow", ex);
				}
			}
		}
	}

	public void fireOnProgressStep(CallStackElt curr, int incr, String progressMessage) {
		final CallStackListener[] listeners = callStackListeners;
		if (listeners.length != 0) {
			for (CallStackListener listener : listeners) {
				try {
					listener.onProgressStep(curr, incr, progressMessage);
				} catch(Exception ex) {
					LOG.error("Failed to fire event onPop()! .. ignore, no rethrow", ex);
				}
			}
		}
	}

	public void fireOnAttachToThread(CallStack callStack, Thread thread) {
		final CallStackListener[] listeners = callStackListeners;
		if (listeners.length != 0) {
			for (CallStackListener listener : listeners) {
				try {
					listener.onAttachCallStackToThread(callStack, thread);
				} catch(Exception ex) {
					LOG.error("Failed to fire event onAttachToThread()! .. ignore, no rethrow", ex);
				}
			}
		}
	}

	public void fireOnDetachFromThread(CallStack callStack, Thread thread) {
		final CallStackListener[] listeners = callStackListeners;
		if (listeners.length != 0) {
			for (CallStackListener listener : listeners) {
				try {
					listener.onDetachCallStackFromThread(callStack, thread);
				} catch(Exception ex) {
					LOG.error("Failed to fire event onDettachFromThread()! .. ignore, no rethrow", ex);
				}
			}
		}
	}

}
