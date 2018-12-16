package org.perflogstacktrace4j.dto.stats;

import org.perflogstacktrace4j.model.stats.PerfTimeStats;

/**
 * DTO for PerfTimeStats
 */
public final class PerfStatsDTO {
	
	private PendingPerfCountDTO pendingCounts = new PendingPerfCountDTO();
	
	private CumulatedLongStatsHistogramDTO elapsedTimeStats = new CumulatedLongStatsHistogramDTO();
	private CumulatedLongStatsHistogramDTO threadUserTimeStats = new CumulatedLongStatsHistogramDTO();
	private CumulatedLongStatsHistogramDTO threadCpuTimeStats = new CumulatedLongStatsHistogramDTO();
	
	// ------------------------------------------------------------------------

	public PerfStatsDTO() {
	}

	public PerfStatsDTO(PerfStatsDTO src) {
		set(src);
	}

	public PerfStatsDTO(PerfTimeStats src) {
		incr(src);
	}

	// ------------------------------------------------------------------------

	public PendingPerfCountDTO getPendingCounts() {
		return pendingCounts;
	}

	public CumulatedLongStatsHistogramDTO getElapsedTimeStats() {
		return elapsedTimeStats;
	}

	public CumulatedLongStatsHistogramDTO getThreadUserTimeStats() {
		return threadUserTimeStats;
	}

	public CumulatedLongStatsHistogramDTO getThreadCpuTimeStats() {
		return threadCpuTimeStats;
	}

	public int getPendingCount() {
		return pendingCounts.getPendingCount();
	}

	public long getPendingSumStartTime() {
		return pendingCounts.getPendingSumStartTime();
	}

	public void set(PerfStatsDTO src) {
		elapsedTimeStats.set(src.elapsedTimeStats);
		threadUserTimeStats.set(src.threadUserTimeStats);
		threadCpuTimeStats.set(src.threadCpuTimeStats);

		pendingCounts.set(src.pendingCounts);
	}

	public void incr(PerfTimeStats src) {
		elapsedTimeStats.incr(src.getElapsedTimeStats());
		threadUserTimeStats.incr(src.getThreadUserTimeStats());
		threadCpuTimeStats.incr(src.getThreadCpuTimeStats());

		pendingCounts.incr(src.getPendingCounts());
	}


	@Override /* java.lang.Object */
	public PerfStatsDTO clone() {
		return copy();
	}
	
	public PerfStatsDTO copy() {
		return new PerfStatsDTO(this);
	}

	// ------------------------------------------------------------------------
	
	@Override
	public String toString() {
		int pendingCount = pendingCounts.getPendingCount();
		return "PerfStats [" 
				+ ((pendingCount != 0)? ", pending:" + pendingCount : "")
				+ "count:" + elapsedTimeStats.totalCount()
				+ ", sum ms elapsed: " + elapsedTimeStats.totalSum()
				+ ", cpu:" + threadCpuTimeStats.totalSum()
				+ ", user:" + threadUserTimeStats.totalSum()
				+ "]";
	}

}
