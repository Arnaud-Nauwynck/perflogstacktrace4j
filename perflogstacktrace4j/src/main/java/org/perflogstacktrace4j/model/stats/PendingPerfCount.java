package org.perflogstacktrace4j.model.stats;

import org.perflogstacktrace4j.model.CallStackElt;
import org.perflogstacktrace4j.utils.ThreadTimeUtils;
import org.perflogstacktrace4j.utils.UnsafeUtils;
import org.perflogstacktrace4j.utils.UnsafeUtils.IntVolatileFieldAccessor;
import org.perflogstacktrace4j.utils.UnsafeUtils.LongVolatileFieldAccessor;

/**
 * perf counter for pending thread in a section
 * 
 * this class is multi-thread safe, and lock-FREE!
 */
public class PendingPerfCount {

	// internal for UNSAFE volatile access
	private static final IntVolatileFieldAccessor<PendingPerfCount> pendingCountFieldAccessor = UnsafeUtils.intVolatileFieldAccessor(PendingPerfCount.class, "pendingCount");
	private static final LongVolatileFieldAccessor<PendingPerfCount> pendingSumStartTimeFieldAccessor = UnsafeUtils.longVolatileFieldAccessor(PendingPerfCount.class, "pendingSumStartTime");
	private static final LongVolatileFieldAccessor<PendingPerfCount> pendingSumStartCpuTimeFieldAccessor = UnsafeUtils.longVolatileFieldAccessor(PendingPerfCount.class, "pendingSumStartCpuTime");

	/**
	 * count of currently pending threads
	 */
	private int pendingCount;

	/**
	 * sum of startTime in nanos for all currently pending threads.
	 * notice that will probably overflow long (2^64 bits), so the value is "correct modulo 2^64"
	 * 
	 * to compute average until given timeNow, use <code>(pendingCount * timeNow - pendingSumStartTime) / pendingCount</code>
	 * see getPendingAverageTimeNanosUntilTime(timeNow)
	 */
	private long pendingSumStartTime;

	/**
	 * sum of startCpuTime in nanos for all currently pending threads.
	 * notice that will probably overflow long (2^64 bits), so the value is "correct modulo 2^64"
	 * 
	 * to compute average until given timeNow, use <code>(pendingCount * timeNow - pendingSumCpuStartTime) / pendingCount</code>
	 * see getPendingAverageCpuTimeNanosUntilTime(timeNow)
	 */
	private long pendingSumStartCpuTime;

	// ------------------------------------------------------------------------

	public PendingPerfCount() {
	}

	public PendingPerfCount(PendingPerfCount src) {
		set(src);
	}

	// ------------------------------------------------------------------------

	public int getPendingCount() {
		return pendingCountFieldAccessor.get(this);
	}

	public long getPendingSumStartTime() {
		return pendingSumStartTimeFieldAccessor.get(this);
	}

	public long getPendingSumStartCpuTime() {
		return pendingSumStartCpuTimeFieldAccessor.get(this);
	}

	public long getPendingAverageTimeNanosUntilTime(long timeNanos) {
		int count = getPendingCount();
		if (count == 0) {
			return 0;
		}
		long sumStart = getPendingSumStartTime();
		long avg = (count * timeNanos - sumStart) / count;
		return avg;
	}

	public long getPendingAverageTimeMillisUntilTime(long timeNanos) {
		long avgNanos = getPendingAverageTimeNanosUntilTime(timeNanos);
		return ThreadTimeUtils.nanosToMillis(avgNanos);
	}
	
	@Override /* java.lang.Object */
	public PendingPerfCount clone() {
		return copy();
	}
	
	public PendingPerfCount copy() {
		return new PendingPerfCount(this);
	}

	public void set(PendingPerfCount src) {
		this.pendingCount = src.getPendingCount();
		this.pendingSumStartTime = src.getPendingSumStartTime();
		this.pendingSumStartCpuTime = src.getPendingSumStartCpuTime();
	}

	public void clear() {
		pendingCountFieldAccessor.set(this, 0);
		pendingSumStartTimeFieldAccessor.set(this, 0L);
		pendingSumStartCpuTimeFieldAccessor.set(this, 0L);
	}

	public void incr(PendingPerfCount src) {
		int incrCount = src.getPendingCount();
		long incrPendingSum = src.getPendingSumStartTime();
		long incrPendingCpuSum = src.getPendingSumStartCpuTime();
		pendingCountFieldAccessor.add(this, incrCount);  
		pendingSumStartTimeFieldAccessor.add(this, incrPendingSum); 
		pendingSumStartCpuTimeFieldAccessor.add(this, incrPendingCpuSum); 
	}

	// ------------------------------------------------------------------------
	
	public void addPending(long startTimeMillis, long startCpuTimeMillis) {
		pendingCountFieldAccessor.add(this, 1);  
		pendingSumStartTimeFieldAccessor.add(this, startTimeMillis); 
		pendingSumStartCpuTimeFieldAccessor.add(this, startCpuTimeMillis); 
	}

	public void removePending(long startTimeMillis, long startCpuTimeMillis) {
		pendingCountFieldAccessor.add(this, -1);  
		pendingSumStartTimeFieldAccessor.add(this, -startTimeMillis); 
		pendingSumStartCpuTimeFieldAccessor.add(this, -startCpuTimeMillis); 
	}

	// Helper method using StackElt start/end times
	// ------------------------------------------------------------------------
	
	public void addPending(CallStackElt stackElt) {
		addPending(stackElt.getStartTime(), stackElt.getThreadCpuStartTime());
	}

	public void removePending(CallStackElt stackElt) {
		removePending(stackElt.getStartTime(), stackElt.getThreadCpuStartTime());		
	}

	// ------------------------------------------------------------------------
	
	@Override
	public String toString() {
		final int count = pendingCount;
		if (count == 0) return "PendingPerfCounts[]";
		final long sum = this.pendingSumStartTime;
		final long sumCpu = this.pendingSumStartCpuTime;
		
		long timeNow = ThreadTimeUtils.getTime();
		long avgMillisUntilNow = getPendingAverageTimeMillisUntilTime(timeNow);
		
		return "PendingPerfCounts[" 
				+ "count:" + count + "sum:" + sum + " pendingSumCpuTime:" + sumCpu 
				+ ", avgMillis:" + avgMillisUntilNow + " ms until now:" + timeNow
				+ "]";
	}

}
