
package me.asu.util;

import java.io.*;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import repo.io.spring.IResource;
import repo.io.spring.PathMatchingResourcePatternResolver;
import repo.io.spring.ResourcePatternResolver;


@Slf4j
public class PropertiesProxy {

    // 是否为UTF8格式的Properties文件
    private final boolean utf8;
    // 是否忽略无法加载的文件
    private boolean ignoreResourceNotFound = false;

    private MultiLineProperties mp = new MultiLineProperties();

    public PropertiesProxy() {
        this(true);
    }

    public PropertiesProxy(boolean utf8) {
        this.utf8 = utf8;
    }

    public PropertiesProxy(String... paths) {
        this(true);
        this.setPaths(paths);
    }

    public PropertiesProxy(InputStream in) {
        this(true);
        setInputStream(in);
    }

    /**
     * @param r 文本输入流
     * @since 1.b.50
     */
    public PropertiesProxy(Reader r) {
        this(true);
        setReader(r);
    }

    /**
     * 加载指定文件/文件夹的Properties文件,合并成一个Properties对象
     * <p>
     * <b style=color:red>如果有重复的key,请务必注意加载的顺序!!<b/>
     *
     * @param paths 需要加载的Properties文件路径
     */
    public void setPaths(String... paths) {
        mp = new MultiLineProperties();

        try {
            List<IResource> resources = getResources(paths);
            if (utf8) {
                for (IResource nr : resources) {
                    Reader r = new InputStreamReader(nr.getInputStream());
                    try {
                        mp.load(r, false);
                    } finally {
                        Streams.safeClose(r);
                    }
                }
            } else {
                Properties p = new Properties();
                for (IResource nr : resources) {
                    InputStream in = nr.getInputStream();
                    try {
                        p.load(nr.getInputStream());
                    } finally {
                        Streams.safeClose(in);
                    }
                }
                mp.putAll(p);
            }
        } catch (IOException e) {
            throw Exceptions.wrapThrow(e);
        }
    }

    public void setReader(Reader r) {
        try {
            mp = new MultiLineProperties();
            mp.load(r);
        } catch (IOException e) {
            throw Exceptions.wrapThrow(e);
        }
    }

    public void setInputStream(InputStream in) {
        try {
            mp = new MultiLineProperties();
            mp.load(new InputStreamReader(in));
        } catch (IOException e) {
            throw Exceptions.wrapThrow(e);
        }
    }

    /**
     * 加载指定文件/文件夹的Properties文件
     *
     * @param paths 需要加载的Properties文件路径
     * @return 加载到的Properties文件Resource列表
     */
    private List<IResource> getResources(String... paths)
    {
        List<IResource> list = new ArrayList<IResource>();
        for (String path : paths) {
            try {
                ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
                IResource[] resources = resourceResolver.getResources("^.+[.]properties$");

                list.addAll(java.util.Arrays.asList(resources));
            } catch (Exception e) {
                if (ignoreResourceNotFound) {
                    if (log.isWarnEnabled()) {
                        log.warn("Could not load resource from " + path + ": " + e.getMessage());
                    }
                } else {
                    throw Exceptions.wrapThrow(e);
                }
            }
        }
        return list;
    }

    public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
        this.ignoreResourceNotFound = ignoreResourceNotFound;
    }

    /**
     * @param key 键
     * @return 是否包括这个键
     * @since 1.b.50
     */
    public boolean has(String key) {
        return mp.containsKey(key);
    }

    public void put(String key, String value) {
        mp.put(key, value);
    }

    public PropertiesProxy set(String key, String val) {
        put(key, val);
        return this;
    }

    public String check(String key) {
        String val = get(key);
        if (null == val) {
            throw Exceptions.makeThrow("Ioc.$conf expect property '%s'", key);
        }
        return val;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean dfval) {
        String val = get(key);
        if (me.asu.util.Strings.isBlank(val)) {
            return dfval;
        }
        return Boolean.parseBoolean(val);
    }

    public String get(String key) {
        return mp.get(key);
    }

    public String get(String key, String defaultValue) {
        return me.asu.util.Strings.sNull(mp.get(key), defaultValue);
    }

    public List<String> getList(String key) {
        List<String> re = new ArrayList<String>();
        String keyVal = get(key);
        if (me.asu.util.Strings.isNotBlank(keyVal)) {
            String[] vlist = me.asu.util.Strings.splitIgnoreBlank(keyVal, "\n");
            for (String v : vlist) {
                re.add(v);
            }
        }
        return re;
    }

    public String trim(String key) {
        return me.asu.util.Strings.trim(get(key));
    }

    public String trim(String key, String defaultValue) {
        return me.asu.util.Strings.trim(get(key, defaultValue));
    }

    public int getInt(String key) {
        return getInt(key, -1);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public long getLong(String key) {
        return getLong(key, -1);
    }

    public long getLong(String key, long dfval) {
        try {
            return Long.parseLong(get(key));
        } catch (NumberFormatException e) {
            return dfval;
        }
    }

    public String getTrim(String key) {
        return me.asu.util.Strings.trim(get(key));
    }

    public String getTrim(String key, String defaultValue) {
        return me.asu.util.Strings.trim(get(key, defaultValue));
    }

    public List<String> getKeys() {
        return mp.keys();
    }

    public Collection<String> getValues() {
        return mp.values();
    }

    public Properties toProperties() {
        Properties p = new Properties();
        for (String key : mp.keySet()) {
            p.put(key, mp.get(key));
        }
        return p;
    }

    /**
     * 将另外一个 Properties 文本加入本散列表
     *
     * @param r 文本输入流
     * @return 自身
     */
    public PropertiesProxy joinAndClose(Reader r) {
        MultiLineProperties mp = new MultiLineProperties();
        try {
            mp.load(r);
        } catch (IOException e) {
            throw Exceptions.wrapThrow(e);
        } finally {
            Streams.safeClose(r);
        }
        this.mp.putAll(mp);
        return this;
    }

    public Map<String, String> toMap() {
        return new HashMap<String, String>(mp);
    }

    /**
     * 可支持直接书写多行文本的 Properties 文件
     *
     * @author zozoh(zozohtnt @ gmail.com)
     */
    public static class MultiLineProperties implements Map<String, String>
    {

        public MultiLineProperties(Reader reader) throws IOException
        {
            this();
            load(reader);
        }

        public MultiLineProperties()
        {
            maps = new LinkedHashMap<String, String>();
        }

        protected Map<String, String> maps;

        /**
         * <b>载入并销毁之前的记录</b>
         */
        public synchronized void load(Reader reader) throws IOException
        {
            load(reader, false);
        }

        public synchronized void load(Reader reader, boolean clear) throws IOException
        {
            if (clear) {
                this.clear();
            }
            BufferedReader tr = null;
            if (reader instanceof BufferedReader) {
                tr = (BufferedReader) reader;
            } else {
                tr = new BufferedReader(reader);
            }
            String s;
            // FIXME: 代码有问题
            while (null != (s = tr.readLine())) {
                if (Strings.isBlank(s)) {
                    continue;
                }
                if (s.length() > 0 && s.trim().charAt(0) == '#') // 只要第一个非空白字符是#,就认为是注释
                {
                    continue;
                }
                int  pos;
                char c = '0';
                for (pos = 0; pos < s.length(); pos++) {
                    c = s.charAt(pos);
                    if (c == '=' || c == ':') {
                        break;
                    }
                }
                if (c == '=') {
                    String name = s.substring(0, pos);
                    maps.put(Strings.trim(name), s.substring(pos + 1));
                } else if (c == ':') {
                    String       name = s.substring(0, pos);
                    StringBuffer sb   = new StringBuffer();
                    sb.append(s.substring(pos + 1));
                    String ss;
                    while (null != (ss = tr.readLine())) {
                        if (ss.length() > 0 && ss.charAt(0) == '#') {
                            break;
                        }
                        sb.append("\r\n" + ss);
                    }
                    maps.put(Strings.trim(name), sb.toString());
                    if (null == ss) {
                        return;
                    }
                } else {
                    maps.put(Strings.trim(s), null);
                }
            }
        }

        @Override
        public synchronized void clear()
        {
            maps.clear();
        }

        @Override
        public boolean containsKey(Object key)
        {
            return maps.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value)
        {
            return maps.containsValue(value);
        }

        @Override
        public Set<Entry<String, String>> entrySet()
        {
            return maps.entrySet();
        }

        @Override
        public boolean equals(Object o)
        {
            return maps.equals(o);
        }

        @Override
        public int hashCode()
        {
            return maps.hashCode();
        }

        @Override
        public boolean isEmpty()
        {
            return maps.isEmpty();
        }

        @Override
        public Set<String> keySet()
        {
            return maps.keySet();
        }

        public List<String> keys()
        {
            return new ArrayList<String>(maps.keySet());
        }

        @Override
        public synchronized String put(String key, String value)
        {
            return maps.put(key, value);
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public synchronized void putAll(Map t)
        {
            maps.putAll(t);
        }

        @Override
        public synchronized String remove(Object key)
        {
            return maps.remove(key);
        }

        @Override
        public int size()
        {
            return maps.size();
        }

        @Override
        public Collection<String> values()
        {
            return maps.values();
        }

        @Override
        public String get(Object key)
        {
            return maps.get(key);
        }

    }
}