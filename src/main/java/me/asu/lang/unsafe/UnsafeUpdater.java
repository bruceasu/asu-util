package me.asu.lang.unsafe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * jupiter
 * org.jupiter.common.util.internal
 *
 * @author jiachun.fjc
 */
public class UnsafeUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnsafeUpdater.class);
    private static final sun.misc.Unsafe UNSAFE;

    static {
        sun.misc.Unsafe unsafe;
        try {
            Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (sun.misc.Unsafe) unsafeField.get(null);
        } catch (Throwable t) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("sun.misc.Unsafe.theUnsafe: unavailable, {}.", stackTrace(t));
            }

            unsafe = null;
        }

        UNSAFE = unsafe;
    }

    public static String stackTrace(Throwable t) {
        if (t == null) {
            return "null";
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);
        t.printStackTrace(ps);
        ps.flush();
        try {
            return new String(out.toByteArray());
        } finally {
            try {
                out.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Returns the {@link sun.misc.Unsafe}'s instance.
     */
    public static sun.misc.Unsafe getUnsafe() {
        return UNSAFE;
    }

    /**
     * Creates and returns an updater for objects with the given field.
     *
     * @param tClass    the class of the objects holding the field.
     * @param fieldName the name of the field to be updated.
     */
    public static <U> UnsafeIntegerFieldUpdater<U> newIntegerFieldUpdater(Class<? super U> tClass, String fieldName) {
        try {
            return new UnsafeIntegerFieldUpdater<U>(getUnsafe(), tClass, fieldName);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Creates and returns an updater for objects with the given field.
     *
     * @param tClass    the class of the objects holding the field.
     * @param fieldName the name of the field to be updated.
     */
    public static <U> UnsafeLongFieldUpdater<U> newLongFieldUpdater(Class<? super U> tClass, String fieldName) {
        try {
            return new UnsafeLongFieldUpdater<U>(getUnsafe(), tClass, fieldName);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Creates and returns an updater for objects with the given field.
     *
     * @param tClass    the class of the objects holding the field.
     * @param fieldName the name of the field to be updated.
     */
    public static <U, W> UnsafeReferenceFieldUpdater<U, W> newReferenceFieldUpdater(Class<? super U> tClass, String fieldName) {
        try {
            return new UnsafeReferenceFieldUpdater<U, W>(getUnsafe(), tClass, fieldName);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Creates and returns an updater for objects with the given field.
     *
     * @param tClass    the class of the objects holding the field.
     * @param vClass    the class of the field
     * @param fieldName the name of the field to be updated.
     */
    public static <U, W> AtomicReferenceFieldUpdater<U, W> newAtomicReferenceFieldUpdater(Class<U> tClass,
                                                                                          Class<W> vClass,
                                                                                          String fieldName) {
        try {
            return new UnsafeAtomicReferenceFieldUpdater<U, W>(getUnsafe(), tClass,
                    fieldName);
        } catch (Throwable t) {
            return AtomicReferenceFieldUpdater.newUpdater(tClass, vClass, fieldName);
        }
    }
}
