package org.perflogstacktrace4j.utils;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class UnsafeUtils {

	private static final Logger LOG = LoggerFactory.getLogger(UnsafeUtils.class);
	
	public static final sun.misc.Unsafe UNSAFE = getUnsafe();

    // Cached array base offset
	private static final long ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(byte[].class); // = 16...
	private static final int IDX_SHIFT_INT_ARR; // = 2 ...
	private static final int IDX_SHIFT_LONG_ARR;  // = 3 ...

	static {
		int scaleInt = UNSAFE.arrayIndexScale(int[].class);  // =4... size of an "int" in an int[] array
        IDX_SHIFT_INT_ARR = 31 - Integer.numberOfLeadingZeros(scaleInt);
        
        int scaleLong = UNSAFE.arrayIndexScale(long[].class); // =8... size of a "long" in a long[] array
        IDX_SHIFT_LONG_ARR = 31 - Integer.numberOfLeadingZeros(scaleLong);
	}
	
	/*pp*/ static sun.misc.Unsafe getUnsafe() {
        return AccessController.doPrivileged(new PrivilegedAction<sun.misc.Unsafe>() {
            public sun.misc.Unsafe run() {
		    	try {
		            java.lang.reflect.Field singleoneInstanceField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
		            singleoneInstanceField.setAccessible(true);
		            sun.misc.Unsafe ret =  (sun.misc.Unsafe)singleoneInstanceField.get(null);
		            return ret;
		        } catch (Throwable e) {
		            LOG.error("Could not instanciate sun.miscUnsafe. should use java.nio DirectByteBuffer ?",e);
		            return null;
		        }
            }
        });
    }

	

	protected static Field lookupField(Class<?> clss, String name) {
		Field[] fields = clss.getDeclaredFields();
		for(Field f : fields) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		return null;
	}
	
    
    public static long objectFieldOffset(Class<?> clazz, String fieldName) {
        Field field = lookupField(clazz, fieldName);
        return UNSAFE.objectFieldOffset(field);
	}

    
    public static final class IntVolatileFieldAccessor<T> {
    	private final long fieldOffset;
    	
    	public IntVolatileFieldAccessor(long fieldOffset) {
			this.fieldOffset = fieldOffset;
		}
		
    	public int get(T object) {
    		return UNSAFE.getIntVolatile(object, fieldOffset);
    	}
    	public int set(T object, int value) {
    		return UNSAFE.getAndSetInt(object, fieldOffset, value);
    	}
		public int add(T object, int incrValue) {
			return UNSAFE.getAndAddInt(object, fieldOffset, incrValue);
		}
		public boolean compareAndSwap(T object, int expected, int update) {
			return UNSAFE.compareAndSwapInt(object, fieldOffset, expected, update);
		}
    }

    public static final class LongVolatileFieldAccessor<T> {
    	private final long fieldOffset;
    	
    	public LongVolatileFieldAccessor(long fieldOffset) {
			this.fieldOffset = fieldOffset;
		}
		
    	public long get(T object) {
    		return UNSAFE.getLongVolatile(object, fieldOffset);
    	}
    	public long set(T object, long value) {
    		return UNSAFE.getAndSetLong(object, fieldOffset, value);
    	}
		public long add(T object, long incrValue) {
			return UNSAFE.getAndAddLong(object, fieldOffset, incrValue);
		}
		public boolean compareAndSwap(T object, long expected, long update) {
			return UNSAFE.compareAndSwapLong(object, fieldOffset, expected, update);
		}
    }

    public static <T> IntVolatileFieldAccessor<T> intVolatileFieldAccessor(Class<T> clazz, String fieldName) {
    	long offset = objectFieldOffset(clazz, fieldName);
    	return new IntVolatileFieldAccessor<T>(offset); 
    }

    public static <T> LongVolatileFieldAccessor<T> longVolatileFieldAccessor(Class<T> clazz, String fieldName) {
    	long offset = objectFieldOffset(clazz, fieldName);
    	return new LongVolatileFieldAccessor<T>(offset); 
    }

	public static int fieldIntVolatile(Object obj, long fieldOffset) {
		return UNSAFE.getIntVolatile(obj, fieldOffset);
	}

	public static long fieldLongVolatile(Object obj, long fieldOffset) {
		return UNSAFE.getLongVolatile(obj, fieldOffset);
	}


	
    private static long intArrayElementOffset(int i) {
        return ((long) i << IDX_SHIFT_INT_ARR) + ARRAY_BASE_OFFSET;
    }
    
    private static long longArrayElementOffset(int i) {
        return ((long) i << IDX_SHIFT_LONG_ARR) + ARRAY_BASE_OFFSET;
    }

    public static final class IntArrayVolatileFieldAccessor<T> {
    	private final long fieldOffset;
    	
    	public IntArrayVolatileFieldAccessor(long fieldOffset) {
			this.fieldOffset = fieldOffset;
		}
		private long offsetAt(int index) {
			return fieldOffset + intArrayElementOffset(index);
		}
    	public int getAt(T object, int index) {
    		return UNSAFE.getIntVolatile(object, offsetAt(index));
    	}
    	public int setAt(T object, int index, int value) {
    		return UNSAFE.getAndSetInt(object, offsetAt(index), value);
    	}
		public int addAt(T object, int index, int incrValue) {
			return UNSAFE.getAndAddInt(object, offsetAt(index), incrValue);
		}
    }

    public static final class LongArrayVolatileFieldAccessor<T> {
    	private final long fieldOffset;
    	
    	public LongArrayVolatileFieldAccessor(long fieldOffset) {
			this.fieldOffset = fieldOffset;
		}
		
		private long offsetAt(int index) {
			return fieldOffset + longArrayElementOffset(index);
		}
    	public long getAt(T object, int index) {
    		return UNSAFE.getLongVolatile(object, offsetAt(index));
    	}
    	public long setAt(T object, int index, long value) {
    		return UNSAFE.getAndSetLong(object, offsetAt(index), value);
    	}
		public long addAt(T object, int index, long incrValue) {
			return UNSAFE.getAndAddLong(object, offsetAt(index), incrValue);
		}
    }

    public static <T> IntArrayVolatileFieldAccessor<T> intArrayVolatileFieldAccessor(Class<T> clazz, String fieldName) {
    	long offset = objectFieldOffset(clazz, fieldName);
    	return new IntArrayVolatileFieldAccessor<T>(offset); 
    }

    public static <T> LongArrayVolatileFieldAccessor<T> longArrayVolatileFieldAccessor(Class<T> clazz, String fieldName) {
    	long offset = objectFieldOffset(clazz, fieldName);
    	return new LongArrayVolatileFieldAccessor<T>(offset); 
    }

}
