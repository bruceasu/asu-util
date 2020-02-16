package me.asu.lang.map;

import java.util.*;

public interface MultiValueMap<K, V> {

    /**
     * 添加Key-Value。
     *
     * @param key   key.
     * @param value value.
     */
    void add(K key, V value);

    /**
     * 添加Key-List<Value>。
     *
     * @param key    key.
     * @param values values.
     */
    void add(K key, List<V> values);

    /**
     * 设置一个Key-Value，如果这个Key存在就被替换，不存在则被添加。
     *
     * @param key   key.
     * @param value values.
     */
    void set(K key, V value);

    /**
     * 设置Key-List<Value>，如果这个Key存在就被替换，不存在则被添加。
     *
     * @param key    key.
     * @param values values.
     * @see #set(Object, Object)
     */
    void set(K key, List<V> values);

    /**
     * 替换所有的Key-List<Value>。
     *
     * @param values values.
     */
    void set(Map<K, List<V>> values);

    /**
     * 移除某一个Key，对应的所有值也将被移除。
     *
     * @param key key.
     * @return value.
     */
    List<V> remove(K key);

    /**
     * 移除所有的值。 Remove all key-value.
     */
    void clear();

    /**
     * 拿到Key的集合。
     *
     * @return Set.
     */
    Set<K> keySet();

    /**
     * 拿到所有的值的集合。
     *
     * @return List.
     */
    List<V> values();

    /**
     * 拿到某一个Key下的某一个值。
     *
     * @param key   key.
     * @param index index value.
     * @return The value.
     */
    V getValue(K key, int index);

    default V getValue(K key) {
        return getValue(key, 0);
    }
    /**
     * 拿到某一个Key的所有值。
     *
     * @param key key.
     * @return values.
     */
    List<V> getValues(K key);

    /**
     * 拿到MultiValueMap的大小.
     *
     * @return size.
     */
    int size();

    /**
     * 判断MultiValueMap是否为null.
     *
     * @return True: empty, false: not empty.
     */
    boolean isEmpty();

    /**
     * 判断MultiValueMap是否包含某个Key.
     *
     * @param key key.
     * @return True: contain, false: none.
     */
    boolean containsKey(K key);

    static <K, V> MultiValueMap<K, V> create() {
        return new LinkedMultiValueMap<K, V>();
    }

    public static class LinkedMultiValueMap<K, V> implements MultiValueMap<K, V> {

        protected Map<K, List<V>> mSource = new LinkedHashMap<K, List<V>>();

        public LinkedMultiValueMap() {
        }

        @Override
        public void add(K key, V value) {
            if (key != null) {
                // 如果有这个Key就继续添加Value，没有就创建一个List并添加Value
                if (!mSource.containsKey(key)) {
                    mSource.put(key, new ArrayList<V>(2));
                }
                mSource.get(key).add(value);
            }
        }

        @Override
        public void add(K key, List<V> values) {
            // 便利添加进来的List的Value，调用上面的add(K, V)方法添加
            for (V value : values) {
                add(key, value);
            }
        }

        @Override
        public void set(K key, V value) {
            // 移除这个Key，添加新的Key-Value
            mSource.remove(key);
            add(key, value);
        }

        @Override
        public void set(K key, List<V> values) {
            // 移除Key，添加List<V>
            mSource.remove(key);
            add(key, values);
        }

        @Override
        public void set(Map<K, List<V>> map) {
            // 移除所有值，便利Map里的所有值添加进来
            mSource.clear();
            mSource.putAll(map);
        }

        @Override
        public List<V> remove(K key) {
            return mSource.remove(key);
        }

        @Override
        public void clear() {
            mSource.clear();
        }

        @Override
        public Set<K> keySet() {
            return mSource.keySet();
        }

        @Override
        public List<V> values() {
            // 创建一个临时List保存所有的Value
            List<V> allValues = new ArrayList<V>();

            // 便利所有的Key的Value添加到临时List
            Set<K> keySet = mSource.keySet();
            for (K key : keySet) {
                allValues.addAll(mSource.get(key));
            }
            return allValues;
        }

        @Override
        public List<V> getValues(K key) {
            return mSource.get(key);
        }

        @Override
        public V getValue(K key, int index) {
            List<V> values = mSource.get(key);
            if (values != null && index < values.size()) {
                return values.get(index);
            }
            return null;
        }

        @Override
        public int size() {
            return mSource.size();
        }

        @Override
        public boolean isEmpty() {
            return mSource.isEmpty();
        }

        @Override
        public boolean containsKey(K key) {
            return mSource.containsKey(key);
        }

    }

}
