package me.asu.lang.unsafe;

import java.lang.reflect.Field;
import sun.misc.Unsafe;

/**
 * jupiter
 * org.jupiter.common.util.internal
 *
 * @author jiachun.fjc
 */
@SuppressWarnings("unchecked")
public final class UnsafeReferenceFieldUpdater<U, W> {
    private final long offset;
    private final Unsafe unsafe;

    UnsafeReferenceFieldUpdater(Unsafe unsafe, Class<? super U> tClass, String fieldName) throws NoSuchFieldException {
        Field field = tClass.getDeclaredField(fieldName);
        if (unsafe == null) {
            throw new NullPointerException("unsafe");
        }
        this.unsafe = unsafe;
        offset = unsafe.objectFieldOffset(field);
    }

    public void set(U obj, W newValue) {
        unsafe.putObject(obj, offset, newValue);
    }

    public W get(U obj) {
        return (W) unsafe.getObject(obj, offset);
    }
}
