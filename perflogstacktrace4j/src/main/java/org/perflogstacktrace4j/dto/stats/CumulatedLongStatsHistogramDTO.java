package org.perflogstacktrace4j.dto.stats;

import org.perflogstacktrace4j.model.stats.LongStatsHistogram;
import org.perflogstacktrace4j.utils.ThreadTimeUtils;


/**
 * DTO for LongStatsHistogram
 * <br/>
 * values are slightly transformed, by applying partial cumulated sums, and converting nanos to millis
 * <br/>
 * for total count, use cumulated[N]
 * cumulated[i] = sum for all calls faster than i-th slot
 * 
 * cumulated[0] = src.count[0]
 * cumulated[1] = src.count[1] + cumulated[0]
 * cumulated[2] = src.count[2] + cumulated[1]  = src.count[0] + src.count[1] + src.count[2]
 * ..
 * cumulated[N] = src.count[N] + cumulated[N-1]
 * 
 * 
 * see BasicTimeStatsLogHistogram
 */
public final class CumulatedLongStatsHistogramDTO {

	/**
	 * slot count
	 */
	public static final int SLOT_LEN = 10;
	public static final int MAX_SLOT_INDEX = SLOT_LEN - 1;
	

	/**
	 * cumulated occurrence count per elapsed time using histogram slots
	 * cumulated[0] = src.count[0]
	 * cumulated[1] = src.count[1] + cumulated[0]
	 * cumulated[2] = src.count[2] + cumulated[1]  = src.count[0] + src.count[1] + src.count[2]
	 * ..
	 * cumulated[N] = src.count[N] + cumulated[N-1]
	 */
	private int[] cumulatedCountSlots = new int[SLOT_LEN];

	/**
	 * cumulated sum of elapsed time in millis using histogram slots
	 * 
	 * cumulated[0] = src.count[0]
	 * cumulated[1] = src.count[1] + cumulated[0]
	 * cumulated[2] = src.count[2] + cumulated[1]  = src.count[0] + src.count[1] + src.count[2]
	 * ..
	 * cumulated[N] = src.count[N] + cumulated[N-1]
	 * 
	 */
	private long[] cumulatedSumSlots = new long[SLOT_LEN];
	
	
	// ------------------------------------------------------------------------

	public CumulatedLongStatsHistogramDTO() {
	}

	public CumulatedLongStatsHistogramDTO(CumulatedLongStatsHistogramDTO src) {
		set(src);
	}

	public CumulatedLongStatsHistogramDTO(LongStatsHistogram src) {
		incr(src);
	}

	// ------------------------------------------------------------------------

	/** @return sum of values in all slots */
	public long totalSum() {
		return cumulatedSumSlots[MAX_SLOT_INDEX];
	}

	/** @return sum of counts in all slots */
	public int totalCount() {
		return cumulatedCountSlots[MAX_SLOT_INDEX];
	}

	public long cumulatedSumAt(int i) {
		return cumulatedSumSlots[i];
	}

	public int cumulatedCountAt(int i) {
		return cumulatedCountSlots[i];
	}
		
	public int[] getCumulatedCountSlots() {
		return cumulatedCountSlots;
	}

	public long[] getCumulatedSumSlots() {
		return cumulatedSumSlots;
	}
	
	public void incr(LongStatsHistogram src) {
		int cumulCount = 0;
		long cumulSum = 0;
		for (int i = 0; i < SLOT_LEN; i++) {
			cumulCount += src.getCount(i);
			cumulSum += ThreadTimeUtils.nanosToMillis(src.getSum(i));
			cumulatedCountSlots[i] = cumulCount;
			cumulatedSumSlots[i] = cumulSum;
		}
	}

	public void incr(CumulatedLongStatsHistogramDTO src) {
		for (int i = 0; i < SLOT_LEN; i++) {
			cumulatedCountSlots[i] += src.cumulatedCountSlots[i];
			cumulatedSumSlots[i] += src.cumulatedSumSlots[i];
		}
	}

	@Override /* java.lang.Object */
	public CumulatedLongStatsHistogramDTO clone() {
		return copy();
	}
	
	public CumulatedLongStatsHistogramDTO copy() {
		return new CumulatedLongStatsHistogramDTO(this);
	}

	public void set(CumulatedLongStatsHistogramDTO src) {
		System.arraycopy(src.cumulatedCountSlots, 0, cumulatedCountSlots, 0, SLOT_LEN);
		System.arraycopy(src.cumulatedSumSlots, 0, cumulatedSumSlots, 0, SLOT_LEN);
	}

	public boolean compareHasChangeCount(CumulatedLongStatsHistogramDTO cmp) {
		// heuristic: compare total instead of comparing all values
		return totalCount() != cmp.totalCount();
	}

	@Override
	public String toString() {
		long totalCount = totalCount();
		long avg = (totalCount != 0)? totalSum()/totalCount : 0;
		return "CumulatedPerfStatsHistogram ["
				+ "total count:" + totalCount
				+ ", avg:" + avg
				+ "]";
	}

}
