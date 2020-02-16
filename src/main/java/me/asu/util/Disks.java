package me.asu.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.LinkedList;


/**
 * 磁盘操作的帮助函数集合
 *
 * @author zozoh(zozohtnt@gmail.com)
 * @author bonyfish(mc02cxj@gmail.com)
 */
public abstract class Disks {

    /**
     * 将两个文件对象比较，得出相对路径
     *
     * @param base 基础文件对象
     * @param file 相对文件对象
     * @return 相对于基础文件对象的相对路径
     */
    public static String getRelativePath(File base, File file) {
        String pathBase = base.getAbsolutePath();
        if (base.isDirectory()) {
            pathBase += "/";
        }

        String pathFile = file.getAbsolutePath();
        if (file.isDirectory()) {
            pathFile += "/";
        }

        return getRelativePath(pathBase, pathFile);
    }

    /**
     * 将两个路径比较，得出相对路径
     *
     * @param base 基础路径，以 '/' 结束，表示目录
     * @param path 相对文件路径，以 '/' 结束，表示目录
     * @return 相对于基础路径对象的相对路径
     */
    public static String getRelativePath(String base, String path) {
        String[] bb = me.asu.util.Strings.splitIgnoreBlank(getCanonicalPath(base),
                                                        "[\\\\/]");
        String[] ff = me.asu.util.Strings.splitIgnoreBlank(getCanonicalPath(path),
                                                        "[\\\\/]");
        int len = Math.min(bb.length, ff.length);
        int pos = 0;
        for (; pos < len; pos++) {
            if (!bb[pos].equals(ff[pos])) {
                break;
            }
        }

        if (len == pos && bb.length == ff.length) {
            return "./";
        }

        int dir = 1;
        if (base.endsWith("/")) {
            dir = 0;
        }

        StringBuilder sb = new StringBuilder(me.asu.util.Strings.dup("../", bb.length
                - pos
                - dir));
        return sb.append(Strings.concat(pos, ff.length - pos, '/', ff)).toString();
    }

    /**
     * 整理路径。 将会合并路径中的 ".."
     *
     * @param path 路径
     * @return 整理后的路径
     */
    public static String getCanonicalPath(String path) {
        if (me.asu.util.Strings.isBlank(path)) {
            return path;
        }
        String[] pa = me.asu.util.Strings.splitIgnoreBlank(path, "[\\\\/]");
        LinkedList<String> paths = new LinkedList<String>();
        for (String s : pa) {
            if ("..".equals(s)) {
                if (paths.size() > 0) {
                    paths.removeLast();
                }
                continue;
            } else {
                paths.add(s);
            }
        }
        if (path.charAt(0) == '/') {
            return Strings.concat("/", paths).insert(0, '/').toString();
        }
        return Strings.concat("/", paths).toString();
    }

    /**
     * @return 当前账户的主目录全路径
     */
    public static String home() {
        return System.getProperty("user.home");
    }

    /**
     * @param path 相对用户主目录的路径
     * @return 相对用户主目录的全路径
     */
    public static String home(String path) {
        return home() + path;
    }

    /**
     * 获取一个路径的绝对路径。如果该路径不存在，则返回null
     *
     * @param path 路径
     * @return 绝对路径
     */
    public static String absolute(String path) {
        return absolute(path, ClassUtils.getDefaultClassLoader(),
                        Charset.defaultCharset().name());
    }

    /**
     * 获取一个路径的绝对路径。如果该路径不存在，则返回null
     *
     * @param path 路径
     * @param klassLoader 参考 ClassLoader
     * @param enc 路径编码方式
     * @return 绝对路径
     */
    public static String absolute(String path,
                                  ClassLoader klassLoader,
                                  String enc) {
        path = normalize(path, enc);
        if (me.asu.util.Strings.isEmpty(path)) {
            return null;
        }

        File f = new File(path);
        if (!f.exists()) {
            URL url = null;
            try {
                url = klassLoader.getResource(path);
                if (null == url) {
                    url = Thread.currentThread()
                                .getContextClassLoader()
                                .getResource(path);
                }
                if (null == url) {
                    url = ClassLoader.getSystemResource(path);
                }
            } catch (Throwable e) {
            }
            if (null != url) {
                return normalize(url.getPath(), "UTF-8");// 通过URL获取String,一律使用UTF-8编码进行解码
            }
            return null;
        }
        return path;
    }

    /**
     * 让路径变成正常路径，将 ~ 替换成用户主目录
     *
     * @param path 路径
     * @return 正常化后的路径
     */
    public static String normalize(String path) {
        return normalize(path, Charset.defaultCharset().name());
    }

    /**
     * 让路径变成正常路径，将 ~ 替换成用户主目录
     *
     * @param path 路径
     * @param enc 路径编码方式
     * @return 正常化后的路径
     */
    public static String normalize(String path, String enc) {
        if (me.asu.util.Strings.isEmpty(path)) {
            return null;
        }
        if (path.charAt(0) == '~') {
            path = Disks.home() + path.substring(1);
        }
        try {
            return URLDecoder.decode(path, enc);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

}
