package org.perflogstacktrace4j.model.stats;

import org.perflogstacktrace4j.model.CallStackElt;

/**
 * class for aggregating PendingPerfCount + BasicTimeStatsLogHistogram (elapsed,threadUser,threadCpu)
 * 
 * this class is thread-safe, and lock-FREE !
 */
public final class PerfTimeStats {
	
	private PendingPerfCount pendingCounts = new PendingPerfCount();
	
	private LongStatsHistogram elapsedTimeStats = new LongStatsHistogram();
	private LongStatsHistogram threadUserTimeStats = new LongStatsHistogram();
	private LongStatsHistogram threadCpuTimeStats = new LongStatsHistogram();
	
	// ------------------------------------------------------------------------

	public PerfTimeStats() {
	}

	public PerfTimeStats(PerfTimeStats src) {
		set(src);
	}

	// ------------------------------------------------------------------------

	public PendingPerfCount getPendingCounts() {
		return pendingCounts;
	}

	public LongStatsHistogram getElapsedTimeStats() {
		return elapsedTimeStats;
	}

	public LongStatsHistogram getThreadUserTimeStats() {
		return threadUserTimeStats;
	}

	public LongStatsHistogram getThreadCpuTimeStats() {
		return threadCpuTimeStats;
	}

	public int getPendingCount() {
		return pendingCounts.getPendingCount();
	}

	public long getPendingSumStartTime() {
		return pendingCounts.getPendingSumStartTime();
	}

	@Override /* java.lang.Object */
	public PerfTimeStats clone() {
		return copy();
	}
	
	public PerfTimeStats copy() {
		return new PerfTimeStats(this);
	}

	// ------------------------------------------------------------------------
	
	public void clear() {
		this.elapsedTimeStats.clear();
		this.threadUserTimeStats.clear();
		this.threadCpuTimeStats.clear();

		this.pendingCounts.clear();
	}

	public void clearAndCopyTo(PerfTimeStats dest) {
		// TODO
		dest.set(this);
		clear();
	}

	public void copyTo(PerfTimeStats dest) {
		dest.set(this);
	}

	public void set(PerfTimeStats src) {
		this.elapsedTimeStats.set(src.elapsedTimeStats);
		this.threadUserTimeStats.set(src.threadUserTimeStats);
		this.threadCpuTimeStats.set(src.threadCpuTimeStats);

		this.pendingCounts.set(src.pendingCounts);		
	}

	
	public void addPending(long currTime, long currThreadCpuTime) {
		pendingCounts.addPending(currTime, currThreadCpuTime);
	}

	public void removePending(long startedTime, long startedThreadCpuTime) {
		pendingCounts.removePending(startedTime, startedThreadCpuTime);
	}

	public void incrAndRemovePending(
			long startTime, long threadUserStartTime, long threadCpuStartTime,
			long endTime, long threadUserEndTime, long threadCpuEndTime) {
		incr(endTime-startTime, threadUserEndTime - threadUserStartTime, threadCpuEndTime - threadCpuStartTime);
		pendingCounts.removePending(startTime, threadCpuStartTime);
	}
	
	public void incr(long elapsedTime, long elapsedThreadUserTime, long elapsedThreadCpuTime) {
		elapsedTimeStats.incr(elapsedTime);
		threadUserTimeStats.incr(elapsedThreadUserTime);
		threadCpuTimeStats.incr(elapsedThreadCpuTime);
	}

	public void incr(PerfTimeStats src) {
		pendingCounts.incr(src.pendingCounts);
		elapsedTimeStats.incr(src.elapsedTimeStats);
		threadUserTimeStats.incr(src.threadUserTimeStats);
		threadCpuTimeStats.incr(src.threadCpuTimeStats);
	}
	
	// Helper method using StackElt start/end times
	// ------------------------------------------------------------------------
	
	public void addPending(CallStackElt stackElt) {
		pendingCounts.addPending(stackElt);
	}

	public void incrAndRemovePending(CallStackElt stackElt) {
		long elapsedTime = stackElt.getEndTime() - stackElt.getStartTime();
		long elapsedThreadUserTime = stackElt.getThreadUserEndTime() - stackElt.getThreadUserStartTime();
		long elapsedThreadCpuTime = stackElt.getThreadCpuEndTime() - stackElt.getThreadCpuStartTime();
		incr(elapsedTime, elapsedThreadUserTime, elapsedThreadCpuTime);

		pendingCounts.removePending(stackElt);		
	}

	// ------------------------------------------------------------------------
	
	@Override
	public String toString() {
		int pendingCount = pendingCounts.getPendingCount();
		return "PerfTimeStats [" 
				+ ((pendingCount != 0)? "pending:" + pendingCount + ", ": "")
				+ "count:" + elapsedTimeStats.getCount()
				+ ", cumulated ms elapsed: " + elapsedTimeStats.getSum()
				+ ", cpu:" + threadCpuTimeStats.getSum()
				+ ", user:" + threadUserTimeStats.getSum()
				+ "]";
	}

}
