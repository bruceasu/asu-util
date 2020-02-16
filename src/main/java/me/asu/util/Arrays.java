package me.asu.util;

import java.lang.reflect.Array;
import java.util.*;

public class Arrays
{

    /**
     * 判断两个对象是否相等。 这个函数用处是:
     * <ul>
     * <li>可以容忍 null
     * <li>可以容忍不同类型的 Number
     * <li>对数组，集合， Map 会深层比较
     * </ul>
     * 当然，如果你重写的 equals 方法会优先
     *
     * @param a0 比较对象1
     * @param a1 比较对象2
     * @return 是否相等
     */
    public static boolean equals(Object a0, Object a1)
    {
        return Objects.equals(a0, a1);
    }

    /**
     * 判断一个数组内是否包括某一个对象。 它的比较将通过 equals(Object,Object) 方法
     *
     * @param array 数组
     * @param ele   对象
     * @return true 包含 false 不包含
     */
    public static <T> boolean contains(T[] array, T ele)
    {
        if (null == array) {
            return false;
        }
        for (T e : array) {
            if (equals(e, ele)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 判断一个对象是否为空。它支持如下对象类型：
     * <ul>
     * <li>null : 一定为空
     * <li>数组
     * <li>集合
     * <li>Map
     * <li>其他对象 : 一定不为空
     * </ul>
     *
     * @param obj 任意对象
     * @return 是否为空
     */
    public static boolean isEmpty(Object obj)
    {
        if (obj == null) {
            return true;
        }
        if (obj instanceof Collection<?>) {
            return ((Collection<?>) obj).isEmpty();
        }
        if (obj instanceof Map<?, ?>) {
            return ((Map<?, ?>) obj).isEmpty();
        }
        if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        }

        return false;
    }

    /**
     * 判断一个数组是否是空数组
     *
     * @param ary 数组
     * @return null 或者空数组都为 true 否则为 false
     */
    public static <T> boolean isEmptyArray(T[] ary)
    {
        return null == ary || ary.length == 0;
    }

    /**
     * 将多个数组，合并成一个数组。如果这些数组为空，则返回 null
     *
     * @param arys 数组对象
     * @return 合并后的数组对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] merge(T[]... arys)
    {
        Queue<T> list = new LinkedList<T>();
        for (T[] ary : arys) {
            if (null != ary) {
                for (T e : ary) {
                    if (null != e) {
                        list.add(e);
                    }
                }
            }
        }
        if (list.isEmpty()) {
            return null;
        }
        Class<T> type = (Class<T>) list.peek().getClass();
        return list.toArray((T[]) Array.newInstance(type, list.size()));
    }

    /**
     * 将一个对象添加成为一个数组的第一个元素，从而生成一个新的数组
     *
     * @param e    对象
     * @param eles 数组
     * @return 新数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayAddFirst(T e, T[] eles)
    {
        try {
            if (null == eles || eles.length == 0) {
                T[] arr = (T[]) Array.newInstance(e.getClass(), 1);
                arr[0] = e;
                return arr;
            }
            T[] arr = (T[]) Array.newInstance(eles.getClass().getComponentType(), eles.length + 1);
            arr[0] = e;
            //            for (int i = 0; i < eles.length; i++) {
            //                arr[i + 1] = eles[i];
            //            }
            System.arraycopy(eles, 0, arr, 1, eles.length);
            return arr;
        } catch (NegativeArraySizeException e1) {
            throw Exceptions.wrapThrow(e1);
        }
    }

    /**
     * 将一个对象添加成为一个数组的最后一个元素，从而生成一个新的数组
     *
     * @param e    对象
     * @param eles 数组
     * @return 新数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayAddLast(T[] eles, T e)
    {
        try {
            if (null == eles || eles.length == 0) {
                T[] arr = (T[]) Array.newInstance(e.getClass(), 1);
                arr[0] = e;
                return arr;
            }
            T[] arr = (T[]) Array.newInstance(eles.getClass().getComponentType(), eles.length + 1);
            //            for (int i = 0; i < eles.length; i++) {
            //                arr[i] = eles[i];
            //            }
            System.arraycopy(eles, 0, arr, 0, eles.length);
            arr[eles.length] = e;
            return arr;
        } catch (NegativeArraySizeException e1) {
            throw Exceptions.wrapThrow(e1);
        }
    }

    /**
     * 安全的从一个数组获取一个元素，容忍 null 数组，以及支持负数的 index
     * <p>
     * 如果该下标越界，则返回 null
     *
     * @param array 数组，如果为 null 则直接返回 null
     * @param index 下标，-1 表示倒数第一个， -2 表示倒数第二个，以此类推
     * @return 数组元素
     */
    public static <T> T get(T[] array, int index)
    {
        if (null == array) {
            return null;
        }
        int i = index < 0 ? array.length + index : index;
        if (i < 0 || i >= array.length) {
            return null;
        }
        return array[i];
    }
}
