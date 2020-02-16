package me.asu.util;

import static java.lang.String.format;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import me.asu.lang.Linked.LinkedCharArray;
import repo.io.spring.IResource;
import repo.io.spring.PathMatchingResourcePatternResolver;
import repo.io.spring.ResourcePatternResolver;

/**
 * Created by bruce on 2015/11/15/015.
 */
@Slf4j
public class FileSqlManager {
    static class InnerStack {

        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        LinkedCharArray list = new LinkedCharArray();
        LinkedCharArray cmts = new LinkedCharArray();
        String key = null;
        boolean inNormalComment;

        void eat(int c) {
            if (inNormalComment) {
                if (cmts.push(c).endsWith("*/")) {
                    cmts.clear();
                    inNormalComment = false;
                }
            } else if (key != null) {
                if (list.push(c).endsWith("\n/*")) {
                    list.popLast(3);
                    addOne();
                    list.push("\n/*");
                } else if (list.endsWith("/*")) {
                    list.popLast(2);
                    inNormalComment = true;
                }
            } else {
                if (list.size() < 3) {
                    if (!"\n/*".startsWith(list.push(c).toString())) {
                        list.clear();
                    }
                } else {
                    if (list.push(c).endsWith("*/")) {
                        Matcher matcher = ptn.matcher(list.popAll());
                        if (matcher.find()) {
                            key = Strings.trim(matcher.group());
                        }
                    }
                }
            }
        }

        void addOne() {
            String value =Strings.trim(list.popAll());
            if (!Strings.isBlank(value)) {
                map.put(key, value);
            }
            key = null;
        }

    }

    static class SqlFileBuilder {
        LinkedHashMap<String, String> map;

        SqlFileBuilder(BufferedReader reader) throws IOException {
            InnerStack stack = new InnerStack();
            int c;
            stack.eat('\n');
            while (-1 != (c = reader.read())) {
                stack.eat(c);
            }
            if (stack.key != null) {
                stack.addOne();
            }
            map = stack.map;
            Streams.safeClose(reader);
        }

        Set<String> keys() {
            return map.keySet();
        }

        String get(String key) {
            return map.get(key);
        }

        Set<Map.Entry<String, String>> entrySet() {
            return map.entrySet();
        }
    }

    private Map<String, String> _sql_map;

    private List<String> _sql_keys;

    private Object lock = new Object();

    private String[] paths;

    private boolean allowDuplicate = true;

    protected static final Pattern ptn = Pattern.compile("(?<=^\n/[*])(.*)(?=[*]/)");

    public FileSqlManager(String... paths) {
        this.paths = paths;
    }

    public void refresh() {
        synchronized (lock) {
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            for (String path : paths) {
                try {
                    IResource[] list = resourcePatternResolver.getResources(path);
                    initSqlMap();
                    for (IResource ins : list) {
                        try {
                            loadSQL(new InputStreamReader(ins.getInputStream()));
                        } catch (IOException e) {
                            throw Exceptions.wrapThrow(e);
                        }
                    }
                } catch (IOException e) {
                    log.error("", e);
                }
            }

        }
    }

    private void initSqlMap()
    {
        _sql_map = new HashMap<>();
    }

    public boolean contains(String key) {
        return map().containsKey(key);
    }

    public void saveAs(File f) throws IOException {
        Writer w = Streams.fileOutw(f);
        for (String key : keylist()) {
            w.append("/*").append(Strings.dup('-', 60)).append("*/\n");
            String sql = map().get(key);
            w.append(format("/*%s*/\n", key));
            w.append(sql).append('\n');
        }
        w.flush();
        w.close();
    }

    public String get(String key) {
        String sql = map().get(key);
        if (null == sql) {
            throw Exceptions.makeThrow(String.format("fail to find SQL '%s'!", key));
        }
        return sql;
    }

    public int count() {
        return map().size();
    }

    public String[] keys() {
        return keylist().toArray(new String[keylist().size()]);
    }

    public void addSql(String key, String value) {
        if (map().containsKey(key) && !allowDuplicate) {
            throw Exceptions.makeThrow("duplicate key '%s'", key);
        }
        key =Strings.trim(key);
        map().put(key, value);
        keylist().add(key);
    }

    public void remove(String key) {
        this.keylist().remove(key);
        this.map().remove(key);
    }

    public void setAllowDuplicate(boolean allowDuplicate) {
        this.allowDuplicate = allowDuplicate;
    }

    public boolean getAllowDuplicate() {
        return this.allowDuplicate;
    }


    private List<String> keylist() {
        if (null == _sql_keys) {
            this.refresh();
        }
        return _sql_keys;
    }

    private Map<String, String> map() {
        if (null == _sql_map) {
            this.refresh();
        }
        return _sql_map;
    }

    /**
     * 执行根据字符流来加载sql内容的操作
     *
     * @param reader
     * @throws IOException
     */
    protected void loadSQL(Reader reader) throws IOException {
        BufferedReader bufferedReader = null;
        try {
            if(reader instanceof BufferedReader) {
                bufferedReader = (BufferedReader)reader;
            }  else {
                bufferedReader = new BufferedReader(reader);
            }
            SqlFileBuilder p = new SqlFileBuilder(bufferedReader);
            _sql_keys = new ArrayList<String>(p.map.size());
            for (Map.Entry<String, String> en : p.entrySet()) {
                addSql(en.getKey(),Strings.trim(en.getValue()));
            }
        }
        finally {
            Streams.safeClose(bufferedReader);
        }

    }

}
