package org.perflogstacktrace4j.model.stats;

import java.util.Date;

import org.perflogstacktrace4j.utils.ExUtils;
import org.perflogstacktrace4j.utils.UnsafeUtils;
import org.perflogstacktrace4j.utils.UnsafeUtils.IntArrayVolatileFieldAccessor;
import org.perflogstacktrace4j.utils.UnsafeUtils.LongArrayVolatileFieldAccessor;
import org.perflogstacktrace4j.utils.UnsafeUtils.LongVolatileFieldAccessor;


/**
 * statistics histogram on Long values, using Logarithmic-based for basic performance ranges
 * <BR/>
 * This class is multi-thread safe, and lock FREE !!
 * <BR/>
 * 
 * Ranges are hard-coded with 10 buckets, using this logarithmic range: 
 * <ul>
 * <li> [0]: 0           millis</li>
 * <li> [1]: 1    - 31   millis</li>
 * <li> [2]: 32   - 63   millis</li>
 * <li> [3]: 64   - 127  millis</li>
 * <li> [4]: 128  - 255  millis</li>
 * <li> [5]: 256  - 511  millis</li>
 * <li> [6]: 512  - 1023 millis</li>
 * <li> [7]: 1024 - 2047 millis</li>
 * <li> [8]: 2048 - 4095 millis</li>
 * <li> [9]: more than 4096 millis</li>
 * </ul> 
 */
public final class LongStatsHistogram {

	private static final IntArrayVolatileFieldAccessor<LongStatsHistogram> countSlotsAccessor = 
			UnsafeUtils.intArrayVolatileFieldAccessor(LongStatsHistogram.class, "countSlots");
	private static final LongArrayVolatileFieldAccessor<LongStatsHistogram> sumSlotsAccessor = 
			UnsafeUtils.longArrayVolatileFieldAccessor(LongStatsHistogram.class, "sumSlots");

	private static final LongVolatileFieldAccessor<LongStatsHistogram> minValueAccessor = 
			UnsafeUtils.longVolatileFieldAccessor(LongStatsHistogram.class, "minValue");
	private static final LongVolatileFieldAccessor<LongStatsHistogram> maxValueAccessor = 
			UnsafeUtils.longVolatileFieldAccessor(LongStatsHistogram.class, "maxValue");
	private static final LongVolatileFieldAccessor<LongStatsHistogram> timeReachingMaxValueAccessor = 
			UnsafeUtils.longVolatileFieldAccessor(LongStatsHistogram.class, "timeReachingMaxValue");
	
	/**
	 * slot count
	 */
	public static final int SLOT_LEN = 10;
	

	/**
	 * occurrence count per elapsed time using histogram slots
	 * 
	 * (values updated atomically using code similar to AtomicIntegerArray using UNSAFE.getAndAddInt() / .getIntVolatile()
	 * but optimized: without using wrapper class + extra array index bound checking
	 * )
	 */
	private int[] countSlots = new int[SLOT_LEN];

	/**
	 * sum of calls elapsed time in nanos, using histogram slots
	 * 
	 * (values updated atomically using code similar to AtomicLongArray using UNSAFE.getAndAddLong() / .getLongVolatile() 
	 * but optimized: without using wrapper class + extra array index bound checking
	 * )
	 */
	private long[] sumSlots = new long[SLOT_LEN];
	
	/**
	 * min value
	 */
	private long minValue = Long.MAX_VALUE;

	/**
	 * max value
	 */
	private long maxValue = Long.MIN_VALUE;

	/**
	 * Date when value has reached max value
	 */
	private long timeReachingMaxValue;

	/**
     * StackTrace when value has reached max value
     */
	private String stackReachingMaxValue;

	
	// ------------------------------------------------------------------------

	public LongStatsHistogram() {
	}

	public LongStatsHistogram(LongStatsHistogram src) {
		set(src);
	}
	
	// ------------------------------------------------------------------------

	public int getCount(int index) {
		assert index >= 0 && index < SLOT_LEN;
		return countSlotsAccessor.getAt(this, index);
	}
	
	public long getSum(int index) {
		assert index >= 0 && index < SLOT_LEN;
		return sumSlotsAccessor.getAt(this, index);
	}

	public long getMinValue() {
		return minValueAccessor.get(this);
	}

	public long getMaxValue() {
		return maxValueAccessor.get(this);
	}

	public long getTimeReachingMaxValue() {
		return timeReachingMaxValueAccessor.get(this);
	}

	public String getStackReachingMaxValue() {
		return stackReachingMaxValue;
	}
	
	// --------------------------------------------------------------------------------------------

	public void clear() {
		for (int i = 0; i < SLOT_LEN; i++) {
			countSlotsAccessor.setAt(this, i, 0);
			sumSlotsAccessor.setAt(this, i, 0);
		}
		minValueAccessor.set(this, Long.MAX_VALUE);
		maxValueAccessor.set(this, Long.MIN_VALUE);
		timeReachingMaxValueAccessor.set(this, 0);
		stackReachingMaxValue = null;
	}


	public void incr(long value) {
		int index = valueToSlotIndex(value);
		countSlotsAccessor.addAt(this, index, 1);
		sumSlotsAccessor.addAt(this, index, value);
		
		long prevMin = minValueAccessor.get(this);
		if (value < prevMin) {
			minValueAccessor.compareAndSwap(this, prevMin, value);
			timeReachingMaxValueAccessor.set(this, System.currentTimeMillis());
			stackReachingMaxValue = ExUtils.currentStackTraceShortPath();
		}
		long prevMax = maxValueAccessor.get(this);
		if (value > prevMax) {
			maxValueAccessor.compareAndSwap(this, prevMax, value);
		}
	}

	public void incr(LongStatsHistogram src) {
		for (int i = 0; i < SLOT_LEN; i++) {
			countSlotsAccessor.addAt(this, i, src.getCount(i));
			sumSlotsAccessor.addAt(this, i, src.getSum(i));
		}
	}

	public void set(LongStatsHistogram src) {
		for (int i = 0; i < SLOT_LEN; i++) {
			countSlotsAccessor.setAt(this, i, src.getCount(i));
			sumSlotsAccessor.setAt(this, i, src.getSum(i));
		}
		minValueAccessor.set(this, src.getMinValue());
		maxValueAccessor.set(this, src.getMaxValue());
		timeReachingMaxValueAccessor.set(this, src.getTimeReachingMaxValue());
		stackReachingMaxValue  = src.getStackReachingMaxValue();
	}
	
	// ------------------------------------------------------------------------

	/** @return sum of values in all slots */
	public long getSum() {
		long res = 0;
		for (int i = 0; i < SLOT_LEN; i++) {
			res += getSum(i);
		}
		return res;
	}

	/** @return sum of counts in all slots */
	public int getCount() {
		int res = 0;
		for (int i = 0; i < SLOT_LEN; i++) {
			res += getCount(i);
		}
		return res;
	}

	public double getAverage() {
		int count = getCount();
		if (count == 0) return 0.0;
		long sum = getSum();
		return (double)sum / count;
	}

	/** @return copy of all slots */
	public LongStatsHistogramSlotInfo[] getSlotInfoCopy() {
		LongStatsHistogramSlotInfo[] res = new LongStatsHistogramSlotInfo[SLOT_LEN];
		for (int i = 0; i < SLOT_LEN; i++) {
			LongStatsHistogramSlotInfo slotInfo = SLOT_INFOS[i];
			res[i] = new LongStatsHistogramSlotInfo(slotInfo.getFrom(), slotInfo.getTo(), getCount(i), getSum(i));
		}
		return res;
	}

	/** @return copy of nth-slot */
	public LongStatsHistogramSlotInfo getSlotInfoCopyAt(int i) {
		if (i < 0 || i >= SLOT_LEN) throw new ArrayIndexOutOfBoundsException();
		LongStatsHistogramSlotInfo slotInfo = SLOT_INFOS[i];
		return new LongStatsHistogramSlotInfo(slotInfo.getFrom(), slotInfo.getTo(), getCount(i), getSum(i));
	}
	
	@Override /* java.lang.Object */
	public LongStatsHistogram clone() {
		return copy();
	}
	
	public LongStatsHistogram copy() {
		return new LongStatsHistogram(this);
	}

	public boolean compareHasChangeCount(LongStatsHistogram cmp) {
		for (int i = 0; i < SLOT_LEN; i++) {
			if (getCount(i) != cmp.getCount(i)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		long count = getCount();
		if (count == 0) {
			return "PerfStatsHistogram[]";
		}
		StringBuilder sb = new StringBuilder();
		long avg = (count != 0)? getSum()/count : 0;
		sb.append("PerfStatsHistogram["
				+ "cumul count:" + count + ", avg:" + avg + "\n");
		for(int i = 0; i < SLOT_LEN; i++) {
			sb.append("slot[" + i+ "]: { count:" + countSlots[i] + ", sum:" + sumSlots[i] + "}\n");
		}
		sb.append("minValue:" + minValue + ", maxValue:" + maxValue 
				+ " at " + new Date(timeReachingMaxValue)
				+ " stackReachingMaxValue: " + stackReachingMaxValue);
		sb.append("]");
		return sb.toString();
	}
	
	// internal utilities for log-based index
	// ------------------------------------------------------------------------

	private static final LongStatsHistogramSlotInfo[] SLOT_INFOS;
	private static final int MAX_SLOT_VALUE = 4096;
	private static final int[] VALUE_DIV32_TO_SLOT_INDEX; 

	static {
		int[] breaks = new int[] { 
				1, 32, 64, 128, 256, 512, 1024, 2048, 4096 
		};

		LongStatsHistogramSlotInfo[] tmp = new LongStatsHistogramSlotInfo[SLOT_LEN];
		int[] tmpValueToSlotIndex = new int[MAX_SLOT_VALUE/32];
		
		tmp[0] = new LongStatsHistogramSlotInfo(-Long.MAX_VALUE, 0, 0, 0);
		int index = 1;
		int from = 1;
		for(int i = 1; i <= MAX_SLOT_VALUE; i++) {
			if (breaks[index] == i) {
				tmp[index] = new LongStatsHistogramSlotInfo(from, i-1, 0, 0);
				index++;
				from = i;
			}
			if (i == MAX_SLOT_VALUE) break;
			int iDiv32 = i >>> 5;
			tmpValueToSlotIndex[iDiv32] = index;
		}
		tmp[SLOT_LEN-1] = new LongStatsHistogramSlotInfo(from, Long.MAX_VALUE, 0, 0);
		
		SLOT_INFOS = tmp;
		VALUE_DIV32_TO_SLOT_INDEX = tmpValueToSlotIndex;
		
		// check..
		if (1 != valueToSlotIndex(30)) throw new IllegalStateException();
		if (1 != valueToSlotIndex(31)) throw new IllegalStateException();
		if (2 != valueToSlotIndex(32)) throw new IllegalStateException();
		if (2 != valueToSlotIndex(33)) throw new IllegalStateException();
		
		if (SLOT_LEN-2 != valueToSlotIndex(4095)) throw new IllegalStateException();
		if (SLOT_LEN-1 != valueToSlotIndex(4096)) throw new IllegalStateException();
		
		index = 1;
		for(int i = 1; i < MAX_SLOT_VALUE; i++) {
			if (breaks[index] == i) {
				index++;
			}
			int checkSlot = valueToSlotIndex(i);
			if (checkSlot != index) {
				throw new IllegalStateException("ERROR " + i + " => " + checkSlot + " != " + index);
			}
		}
		
	}
	
	/**
	 * index using logarithm / linear by parts 
	 */
	public static int valueToSlotIndex(long value) {
		if (value <= 0) return 0;
		else if (value >= MAX_SLOT_VALUE) return SLOT_LEN-1;
		int v = (int) value;
		if (v < 32) return 1;
		else return VALUE_DIV32_TO_SLOT_INDEX[v >>> 5];
	}

}
