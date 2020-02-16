package me.asu.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 文件操作的帮助函数
 *
 * @author amos(amosleaf@gmail.com)
 * @author zozoh(zozohtnt@gmail.com)
 * @author wendal(wendal1985@gmail.com)
 * @author bonyfish(mc02cxj@gmail.com)
 */
public abstract class Files {

    /**
     * 读取 UTF-8 文件全部内容
     *
     * @param path 文件路径
     * @return 文件内容
     */
    public static String read(String path) {
        File f = new File(path);
        if (null == f) {
            throw Exceptions.makeThrow("Can not find file '%s'", path);
        }
        return read(f);
    }

    /**
     * 读取 UTF-8 文件全部内容
     *
     * @param f 文件
     * @return 文件内容
     */
    public static String read(File f) {
        return Streams.readAll(Streams.fileInr(f));
    }

    public static String read(File f, String charest) {
        return Streams.readAll(Streams.fileInr(f, charest));
    }

    /**
     * 读取文件全部字节，并关闭文件
     *
     * @param path 文件路径
     * @return 文件的字节内容
     */
    public static byte[] readBytes(String path) {
        File f = new File(path);
        if (null == f) {
            throw Exceptions.makeThrow("Can not find file '%s'", path);
        }
        return readBytes(f);
    }

    /**
     * 读取文件全部字节，并关闭文件
     *
     * @param f 文件
     * @return 文件的字节内容
     */
    public static byte[] readBytes(File f) {
        return Streams.readBytesAndClose(Streams.buff(Streams.fileIn(f)));
    }

    /**
     * 将内容写到一个文件内，内容对象可以是：
     * <ul>
     * <li>InputStream - 按二进制方式写入
     * <li>byte[] - 按二进制方式写入
     * <li>Reader - 按 UTF-8 方式写入
     * <li>其他对象被 toString() 后按照 UTF-8 方式写入
     * </ul>
     *
     * @param path 文件路径，如果不存在，则创建
     * @param obj  内容对象
     */
    public static void write(String path, Object obj) {
        if (null == path || null == obj) {
            return;
        }
        try {
            write(Files.createFileIfNoExists(path), obj);
        } catch (IOException e) {
            throw Exceptions.wrapThrow(e);
        }
    }

    /**
     * 将内容写到一个文件内，内容对象可以是：
     *
     * <ul>
     * <li>InputStream - 按二进制方式写入
     * <li>byte[] - 按二进制方式写入
     * <li>Reader - 按 UTF-8 方式写入
     * <li>其他对象被 toString() 后按照 UTF-8 方式写入
     * </ul>
     *
     * @param f   文件
     * @param obj 内容
     */
    public static void write(File f, Object obj) {
        if (null == f || null == obj) {
            return;
        }
        if (f.isDirectory()) {
            throw Exceptions.makeThrow("Directory '%s' can not be write as File", f);
        }

        try {
            // 保证文件存在
            if (!f.exists()) {
                Files.createNewFile(f);
            }
            // 输入流
            if (obj instanceof InputStream) {
                Streams.writeAndClose(Streams.fileOut(f), (InputStream) obj);
            }
            // 字节数组
            else if (obj instanceof byte[]) {
                Streams.writeAndClose(Streams.fileOut(f), (byte[]) obj);
            }
            // 文本输入流
            else if (obj instanceof Reader) {
                Streams.writeAndClose(Streams.fileOutw(f), (Reader) obj);
            }
            // 其他对象
            else {
                Streams.writeAndClose(Streams.fileOutw(f), obj.toString());
            }
        } catch (IOException e) {
            throw Exceptions.wrapThrow(e);
        }
    }

    /**
     * 将内容写到一个文件末尾
     * <ul>
     * <li>InputStream - 按二进制方式写入
     * <li>byte[] - 按二进制方式写入
     * <li>Reader - 按 UTF-8 方式写入
     * <li>其他对象被 toString() 后按照 UTF-8 方式写入
     * </ul>
     *
     * @param f   文件
     * @param obj 内容
     */
    public static void appendWrite(File f, Object obj) {
        if (null == f || null == obj) {
            return;
        }
        if (f.isDirectory()) {
            throw Exceptions.makeThrow("Directory '%s' can not be write as File", f);
        }

        try {
            // 保证文件存在
            if (!f.exists()) {
                Files.createNewFile(f);
            }
            // 输入流
            if (obj instanceof InputStream) {
                // TODO
                throw Exceptions.noImplement();
            }
            // 字节数组
            else if (obj instanceof byte[]) {
                // TODO
                throw Exceptions.noImplement();
            }
            // 文本输入流
            else if (obj instanceof Reader) {
                // TODO
                throw Exceptions.noImplement();
            }
            // 其他对象
            else {
                Streams.appendWriteAndClose(f, obj.toString());
            }
        } catch (IOException e) {
            throw Exceptions.wrapThrow(e);
        }
    }

    /**
     * 将文件后缀改名，从而生成一个新的文件对象。但是并不在磁盘上创建它
     *
     * @param f      文件
     * @param suffix 新后缀， 比如 ".gif" 或者 ".jpg"
     * @return 新文件对象
     */
    public static File renameSuffix(File f, String suffix) {
        if (null == f || null == suffix || suffix.length() == 0) {
            return f;
        }
        return new File(renameSuffix(f.getAbsolutePath(), suffix));
    }

    /**
     * 将文件路径后缀改名，从而生成一个新的文件路径。
     *
     * @param path   文件路径
     * @param suffix 新后缀， 比如 ".gif" 或者 ".jpg"
     * @return 新文件后缀
     */
    public static String renameSuffix(String path, String suffix) {
        int pos = path.length();
        for (--pos; pos > 0; pos--) {
            if (path.charAt(pos) == '.') {
                break;
            }
            if (path.charAt(pos) == '/' || path.charAt(pos) == '\\') {
                pos = -1;
                break;
            }
        }
        if (0 >= pos) {
            return path + suffix;
        }
        return path.substring(0, pos) + suffix;
    }

    /**
     * 获取文件主名。 即去掉后缀的名称
     *
     * @param path 文件路径
     * @return 文件主名
     */
    public static String getMajorName(String path) {
        int len = path.length();
        int l = 0;
        int r = len;
        for (int i = r - 1; i > 0; i--) {
            if (r == len) {
                if (path.charAt(i) == '.') {
                    r = i;
                }
            }
            if (path.charAt(i) == '/' || path.charAt(i) == '\\') {
                l = i + 1;
                break;
            }
        }
        return path.substring(l, r);
    }

    /**
     * 获取文件主名。 即去掉后缀的名称
     *
     * @param f 文件
     * @return 文件主名
     */
    public static String getMajorName(File f) {
        return getMajorName(f.getAbsolutePath());
    }

    /**
     * 获取文件后缀名，不包括 '.'，如 'abc.gif','，则返回 'gif'
     *
     * @param f 文件
     * @return 文件后缀名
     */
    public static String getSuffixName(File f) {
        if (null == f) {
            return null;
        }
        return getSuffixName(f.getAbsolutePath());
    }

    /**
     * 获取文件后缀名，不包括 '.'，如 'abc.gif','，则返回 'gif'
     *
     * @param path 文件路径
     * @return 文件后缀名
     */
    public static String getSuffixName(String path) {
        if (null == path) {
            return null;
        }
        int p0 = path.lastIndexOf('.');
        int p1 = path.lastIndexOf('/');
        if (-1 == p0 || p0 < p1) {
            return "";
        }
        return path.substring(p0 + 1);
    }

    /**
     * 根据正则式，从压缩文件中获取文件
     *
     * @param zip   压缩文件
     * @param regex 正则式，用来匹配文件名
     * @return 数组
     */
    public static ZipEntry[] findEntryInZip(ZipFile zip, String regex) {
        List<ZipEntry> list = new LinkedList<ZipEntry>();
        Enumeration<? extends ZipEntry> en = zip.entries();
        while (en.hasMoreElements()) {
            ZipEntry ze = en.nextElement();
            if (null == regex || ze.getName().matches(regex)) {
                list.add(ze);
            }
        }
        return list.toArray(new ZipEntry[list.size()]);
    }

    /**
     * 试图生成一个文件对象，如果文件不存在则创建它。 如果给出的 PATH 是相对路径 则会在 CLASSPATH
     * 中寻找，如果未找到，则会在用户主目录中创建这个文件
     *
     * @param path 文件路径，可以以 ~ 开头，也可以是 CLASSPATH 下面的路径
     * @return 文件对象
     * @throws java.io.IOException 创建失败
     */
    public static File createFileIfNoExists(String path) throws IOException {
        File f = new File(path);
        if (!f.exists()) {
            Files.createNewFile(f);
        }
        if (!f.isFile()) {
            throw Exceptions.makeThrow("'%s' should be a file!", path);
        }
        return f;
    }

    /**
     * 如果文件对象不存在，则创建它
     *
     * @param f 文件对象
     * @return 传入的文件对象，以便为调用者省略一行代码
     */
    public static File createFileIfNoExists(File f) {
        if (null == f) {
            return f;
        }
        if (!f.exists()) {
            try {
                Files.createNewFile(f);
            } catch (IOException e) {
                throw Exceptions.wrapThrow(e);
            }
        }
        if (!f.isFile()) {
            throw Exceptions.makeThrow("'%s' should be a file!", f);
        }
        return f;
    }

    /**
     * 试图生成一个目录对象，如果文件不存在则创建它。 如果给出的 PATH 是相对路径 则会在 CLASSPATH
     * 中寻找，如果未找到，则会在用户主目录中创建这个目录
     *
     * @param path 文件路径，可以以 ~ 开头，也可以是 CLASSPATH 下面的路径
     * @return 文件对象
     */
    public static File createDirIfNoExists(String path) {
        File f = new File(path);
        if (!f.exists()) {
            Files.makeDir(f);
        }
        if (!f.isDirectory()) {
            throw Exceptions.makeThrow("'%s' should be a directory!", path);
        }
        return f;
    }

    /**
     * 传入一个目录对象，如果目录不存在，则创建目录
     *
     * @param d 文件目录对象
     * @return 文件目录对象，以便调用者省略一行代码
     */
    public static File createDirIfNoExists(File d) {
        if (null == d) {
            return d;
        }
        if (!d.exists()) {
            Files.makeDir(d);
        }
        if (!d.isDirectory()) {
            throw Exceptions.makeThrow("'%s' should be a directory!", d);
        }
        return d;
    }

    /**
     * each 函数的参数类型
     */
    public enum LsMode {
        /**
         * 仅文件
         */
        FILE, /**
         * 仅目录
         */
        DIR, /**
         * 文件和目录
         */
        ALL
    }

    /**
     * 在一个目录里列出所有的子文件或者目录
     *
     * @param d       目录
     * @param p       正则表达式对象，如果为空，则是全部正则表达式
     * @param exclude true 正则表达式匹配的文件会被忽略，false 正则表达式匹配的文件会被包含
     * @param mode    请参看 LsMode 枚举类说明, null 表示 LsMode.ALL
     * @return 得到文件对象数组
     * @see Files.LsMode
     */
    public static File[] ls(File d, final Pattern p, final boolean exclude, LsMode mode) {
        if (null == p) {
            return d.listFiles();
        }
        // 全部
        else if (null == mode || LsMode.ALL == mode) {
            return d.listFiles(new FileFilter() {
                public boolean accept(File f) {
                    return p.matcher(f.getName()).find() ^ exclude;
                }
            });
        }
        // 仅文件
        else if (LsMode.FILE == mode) {
            return d.listFiles(new FileFilter() {
                public boolean accept(File f) {
                    if (!f.isFile()) {
                        return false;
                    }
                    return p.matcher(f.getName()).find() ^ exclude;
                }
            });
        }
        // 仅目录
        else if (LsMode.DIR == mode) {
            return d.listFiles(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (!f.isDirectory()) {
                        return false;
                    }
                    return p.matcher(f.getName()).find() ^ exclude;
                }
            });
        }
        // 不可能
        throw Exceptions.impossible();
    }

    /**
     * 列文件
     *
     * @param d     目录对象
     * @param regex 正则表达式
     * @param mode  模式
     * @return 文件列表对象
     * @see #ls(java.io.File, java.util.regex.Pattern, boolean, Files.LsMode)
     */
    public static File[] ls(File d, String regex, LsMode mode) {
        boolean exclude = false;
        Pattern p = null;
        if (!Strings.isBlank(regex)) {
            exclude = regex.startsWith("!");
            if (exclude) {
                regex = Strings.trim(regex.substring(1));
            }
            p = Pattern.compile(regex);
        }
        return ls(d, p, exclude, mode);
    }


    /**
     * 获取输出流
     *
     * @param path  文件路径
     * @param klass 参考的类， -- 会用这个类的 ClassLoader
     * @param enc   文件路径编码
     * @return 输出流
     */
    public static InputStream findFileAsStream(String path, Class<?> klass, String enc) {
        File f = new File(path);
        if (f.exists()) {
            try {
                return new FileInputStream(f);
            } catch (FileNotFoundException e1) {
                return null;
            }
        }
        if (null != klass) {
            InputStream ins = klass.getClassLoader().getResourceAsStream(path);
            if (null == ins) {
                ins = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
            }
            if (null != ins) {
                return ins;
            }
        }
        return ClassLoader.getSystemResourceAsStream(path);
    }

    /**
     * 获取输出流
     *
     * @param path 文件路径
     * @param enc  文件路径编码
     * @return 输出流
     */
    public static InputStream findFileAsStream(String path, String enc) {
        return findFileAsStream(path, Files.class, enc);
    }

    /**
     * 获取输出流
     *
     * @param path  文件路径
     * @param klass 参考的类， -- 会用这个类的 ClassLoader
     * @return 输出流
     */
    public static InputStream findFileAsStream(String path, Class<?> klass) {
        return findFileAsStream(path, klass, "utf-8");
    }

    /**
     * 获取输出流
     *
     * @param path 文件路径
     * @return 输出流
     */
    public static InputStream findFileAsStream(String path) {
        return findFileAsStream(path, Files.class, "utf-8");
    }

    /**
     * 从 CLASSPATH 下或从指定的本机器路径下寻找一个文件
     *
     * @param path        文件路径
     * @param klassLoader 参考 ClassLoader
     * @param enc         文件路径编码
     * @return 文件对象，如果不存在，则为 null
     */
    public static File findFile(String path, ClassLoader klassLoader, String enc) {
        path = Disks.absolute(path, klassLoader, enc);
        if (null == path) {
            return null;
        }
        return new File(path);
    }

    /**
     * 从 CLASSPATH 下或从指定的本机器路径下寻找一个文件
     *
     * @param path 文件路径
     * @param enc  文件路径编码
     * @return 文件对象，如果不存在，则为 null
     */
    public static File findFile(String path, String enc) {
        return findFile(path, ClassUtils.getDefaultClassLoader(), enc);
    }

    /**
     * 从 CLASSPATH 下或从指定的本机器路径下寻找一个文件
     *
     * @param path        文件路径
     * @param klassLoader 使用该 ClassLoader进行查找
     * @return 文件对象，如果不存在，则为 null
     */
    public static File findFile(String path, ClassLoader klassLoader) {
        return findFile(path, klassLoader, Charset.defaultCharset().name());
    }

    /**
     * 从 CLASSPATH 下或从指定的本机器路径下寻找一个文件
     *
     * @param path 文件路径
     * @return 文件对象，如果不存在，则为 null
     */
    public static File findFile(String path) {
        return findFile(path, ClassUtils.getDefaultClassLoader(), Charset.defaultCharset().name());
    }

    /**
     * 文件对象是否是目录，可接受 null
     */
    public static boolean isDirectory(File f) {
        if (null == f) {
            return false;
        }
        if (!f.exists()) {
            return false;
        }
        if (!f.isDirectory()) {
            return false;
        }
        return true;
    }

    /**
     * 文件对象是否是文件，可接受 null
     */
    public static boolean isFile(File f) {
        return null != f && f.exists() && f.isFile();
    }

    /**
     * 创建新文件，如果父目录不存在，也一并创建。可接受 null 参数
     *
     * @param f 文件对象
     * @return false，如果文件已存在。 true 创建成功
     */
    public static boolean createNewFile(File f) throws IOException {
        if (null == f || f.exists()) {
            return false;
        }
        makeDir(f.getParentFile());
        return f.createNewFile();
    }

    /**
     * 创建新目录，如果父目录不存在，也一并创建。可接受 null 参数
     *
     * @param dir 目录对象
     * @return false，如果目录已存在。 true 创建成功
     */
    public static boolean makeDir(File dir) {
        if (null == dir || dir.exists()) {
            return false;
        }
        return dir.mkdirs();
    }

    /**
     * 强行删除一个目录，包括这个目录下所有的子目录和文件
     *
     * @param dir 目录
     * @return 是否删除成功
     */
    public static boolean deleteDir(File dir) {
        if (null == dir || !dir.exists()) {
            return false;
        }
        if (!dir.isDirectory()) {
            throw new RuntimeException("\"" + dir.getAbsolutePath() + "\" should be a directory!");
        }
        File[] files = dir.listFiles();
        boolean re = false;
        if (null != files) {
            if (files.length == 0) {
                return dir.delete();
            }
            for (File f : files) {
                if (f.isDirectory()) {
                    re |= deleteDir(f);
                } else {
                    re |= deleteFile(f);
                }
            }
            re |= dir.delete();
        }
        return re;
    }

    public static void deleteFileOrFolder(Path start) throws IOException {
        if (java.nio.file.Files.notExists(start)) {
            throw new IOException(start + " is not exists.");
        }
        if (java.nio.file.Files.isRegularFile(start)) {
            java.nio.file.Files.delete(start);
        } else if (java.nio.file.Files.isDirectory(start)) {
            // walkFileTree is depth-first
            java.nio.file.Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
                    java.nio.file.Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e)
                throws IOException {
                    if (e == null) {
                        java.nio.file.Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        throw e;
                    }
                }
            });
        } else {
            throw new IOException(start + " is not exists.");
        }

    }

    /**
     * 删除一个文件
     *
     * @param f 文件
     * @return 是否删除成功
     */
    public static boolean deleteFile(File f) {
        if (null == f) {
            return false;
        }
        return f.delete();
    }

    /**
     * 清除一个目录里所有的内容
     *
     * @param dir 目录
     * @return 是否清除成功
     */
    public static boolean clearDir(File dir) {
        if (null == dir) {
            return false;
        }
        if (!dir.exists()) {
            return false;
        }
        File[] fs = dir.listFiles();
        for (File f : fs) {
            if (f.isFile()) {
                Files.deleteFile(f);
            } else if (f.isDirectory()) {
                Files.deleteDir(f);
            }
        }
        return false;
    }

    /**
     * 拷贝一个文件
     *
     * @param src    原始文件
     * @param target 新文件
     * @return 是否拷贝成功
     */
    public static boolean copyFile(File src, File target) throws IOException {
        if (src == null || target == null || !src.exists()) {
            return false;
        }
        if (!target.exists()) {
            if (!createNewFile(target)) {
                return false;
            }
        }
        InputStream ins = new BufferedInputStream(new FileInputStream(src));
        OutputStream ops = new BufferedOutputStream(new FileOutputStream(target));

        Streams.write(ops, ins);

        Streams.safeClose(ins);
        Streams.safeFlush(ops);
        Streams.safeClose(ops);
        return target.setLastModified(src.lastModified());
    }

    /**
     * 自动决定是 copy 文件还是目录
     *
     * @param src    源
     * @param target 目标
     * @return 是否 copy 成功
     */
    public static boolean copy(File src, File target) {
        try {
            if (src.isDirectory()) {
                return copyDir(src, target);
            }
            return copyFile(src, target);
        } catch (IOException e) {
            throw Exceptions.wrapThrow(e);
        }
    }

    /**
     * 拷贝一个目录
     *
     * @param src    原始目录
     * @param target 新目录
     * @return 是否拷贝成功
     */
    public static boolean copyDir(File src, File target) throws IOException {
        if (src == null || target == null || !src.exists()) {
            return false;
        }
        if (!src.isDirectory()) {
            throw new IOException(src.getAbsolutePath() + " should be a directory!");
        }
        if (!target.exists()) {
            if (!makeDir(target)) {
                return false;
            }
        }
        boolean re = true;
        File[] files = src.listFiles();
        if (null != files) {
            for (File f : files) {
                if (f.isFile()) {
                    re &= copyFile(f, new File(target.getAbsolutePath() + "/" + f.getName()));
                } else {
                    re &= copyDir(f, new File(target.getAbsolutePath() + "/" + f.getName()));
                }
            }
        }
        return re;
    }

    public static void copyFolder(final Path srcPath, final Path destPath) throws IOException {
        long startTime = System.currentTimeMillis();
        if (java.nio.file.Files.notExists(srcPath)) {
            throw new IOException(srcPath + " is not exists.");
        }
        if (java.nio.file.Files.notExists(destPath)) {
            java.nio.file.Files.createDirectories(destPath);
        }
        java.nio.file.Files.walkFileTree(srcPath, new SimpleFileVisitor<Path>() {
            @Override
            /** 文件处理，将文件夹也一并处理，简洁些 */ public FileVisitResult visitFile(Path file,
                                                                       BasicFileAttributes attrs)
            throws IOException {
                Path dest = destPath.resolve(srcPath.relativize(file));
                if (java.nio.file.Files.notExists(dest.getParent())) {
                    java.nio.file.Files.createDirectories(dest.getParent());
                }
                java.nio.file.Files.copy(file, dest);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * 将文件移动到新的位置
     *
     * @param src    原始文件
     * @param target 新文件
     * @return 移动是否成功
     */
    public static boolean move(File src, File target) throws IOException {
        if (src == null || target == null) {
            return false;
        }
        makeDir(target.getParentFile());
        if (src.isDirectory()) {
            src = new File(src.getCanonicalPath() + File.separator);
            target = new File(target.getCanonicalPath() + File.separator);
        }
        return src.renameTo(target);
    }

    /**
     * 将文件改名
     *
     * @param src     文件
     * @param newName 新名称
     * @return 改名是否成功
     */
    public static boolean rename(File src, String newName) {
        if (src == null || newName == null) {
            return false;
        }
        if (src.exists()) {
            File newFile = new File(src.getParent() + "/" + newName);
            if (newFile.exists()) {
                return false;
            }
            Files.makeDir(newFile.getParentFile());
            return src.renameTo(newFile);
        }
        return false;
    }

    /**
     * 修改路径
     *
     * @param path    路径
     * @param newName 新名称
     * @return 新路径
     */
    public static String renamePath(String path, String newName) {
        if (!Strings.isBlank(path)) {
            int pos = path.replace('\\', '/').lastIndexOf('/');
            if (pos > 0) {
                return path.substring(0, pos) + "/" + newName;
            }
        }
        return newName;
    }

    /**
     * @param path 路径
     * @return 父路径
     */
    public static String getParent(String path) {
        if (Strings.isBlank(path)) {
            return path;
        }
        int pos = path.replace('\\', '/').lastIndexOf('/');
        if (pos > 0) {
            return path.substring(0, pos);
        }
        return "/";
    }

    /**
     * @param f 文件对象
     * @return 文件或者目录名
     */
    public static String getName(File f) {
        return getName(f.getPath());
    }

    /**
     * @param path 全路径
     * @return 文件或者目录名
     */
    public static String getName(String path) {
        if (!Strings.isBlank(path)) {
            int pos = path.replace('\\', '/').lastIndexOf('/');
            if (pos != -1) {
                return path.substring(pos + 1);
            }
        }
        return path;
    }

    /**
     * 将一个目录下的特殊名称的目录彻底删除，比如 '.svn' 或者 '.cvs'
     *
     * @param dir  目录
     * @param name 要清除的目录名
     */
    public static void cleanAllFolderInSubFolderes(File dir, String name) throws IOException {
        File[] files = dir.listFiles();
        for (File d : files) {
            if (d.isDirectory()) {
                if (d.getName().equalsIgnoreCase(name)) {
                    deleteDir(d);
                } else {
                    cleanAllFolderInSubFolderes(d, name);
                }
            }
        }
    }

    /**
     * 精确比较两个文件是否相等
     *
     * @param f1 文件1
     * @param f2 文件2
     * @return <ul> <li>true: 两个文件内容完全相等 <li>false: 任何一个文件对象为 null，不存在 或内容不相等 </ul>
     */
    public static boolean isEquals(File f1, File f2) {
        if (null == f1 || null == f2 || !f1.isFile() || !f2.isFile()) {
            return false;
        }
        InputStream ins1 = null;
        InputStream ins2 = null;
        try {
            ins1 = Streams.fileIn(f1);
            ins2 = Streams.fileIn(f2);
            return Streams.equals(ins1, ins2);
        } catch (IOException e) {
            return false;
        } finally {
            Streams.safeClose(ins1);
            Streams.safeClose(ins2);
        }
    }

    /**
     * 在一个目录下，获取一个文件对象
     *
     * @param dir  目录
     * @param path 文件相对路径
     * @return 文件
     */
    public static File getFile(File dir, String path) {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                return new File(dir.getAbsolutePath() + "/" + path);
            }
            return new File(dir.getParent() + "/" + path);
        }
        return new File(path);
    }

    /**
     * 获取一个目录下所有子目录。子目录如果以 '.' 开头，将被忽略
     *
     * @param dir 目录
     * @return 子目录数组
     */
    public static File[] dirs(File dir) {
        return dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return !f.isHidden() && f.isDirectory() && !f.getName().startsWith(".");
            }
        });
    }

    /**
     * 递归查找获取一个目录下所有子目录(及子目录的子目录)。子目录如果以 '.' 开头，将被忽略
     * <p/>
     * <b>包含传入的目录</b>
     *
     * @param dir 目录
     * @return 子目录数组
     */
    public static File[] scanDirs(File dir) {
        ArrayList<File> list = new ArrayList<File>();
        list.add(dir);
        scanDirs(dir, list);
        return list.toArray(new File[list.size()]);
    }

    private static void scanDirs(File rootDir, List<File> list) {
        File[] dirs = rootDir.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return !f.isHidden() && f.isDirectory() && !f.getName().startsWith(".");
            }
        });
        if (dirs != null) {
            for (File dir : dirs) {
                scanDirs(dir, list);
                list.add(dir);
            }
        }
    }

    /**
     * 获取一个目录下所有的文件(不递归，仅仅一层)。隐藏文件会被忽略。
     *
     * @param dir    目录
     * @param suffix 文件后缀名。如果为 null，则获取全部文件
     * @return 文件数组
     */
    public static File[] files(File dir, final String suffix) {
        return dir.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return !f.isHidden() && f.isFile() && (null == suffix || f.getName()
                                                                          .endsWith(suffix));
            }
        });
    }

    /**
     * 判断两个文件内容是否相等
     *
     * @param f1 文件对象
     * @param f2 文件对象
     * @return <ul> <li>true: 两个文件内容完全相等 <li>false: 任何一个文件对象为 null，不存在 或内容不相等 </ul>
     */
    public static boolean equals(File f1, File f2) {
        return isEquals(f1, f2);
    }

    public static boolean copyOnWrite(File f, Object obj) {
        File tmp = new File(f.getAbsolutePath() + ".new");
        File tmp2 = new File(f.getAbsolutePath() + ".old");
        tmp2.delete();
        try {
            write(tmp, obj);
            boolean flag = false;
            if (f.exists()) {
                flag = f.renameTo(tmp2);
            }
            if (tmp.renameTo(f)) {
                tmp2.delete();
                return true;
            } else if (flag) {
                tmp2.renameTo(f); // 如果这里也失败的话,起码.old还在...
            }
            return false;
        } finally {
            tmp.delete();
        }
    }

    public static boolean copyOnWrite(String path, Object obj) {
        return copyOnWrite(new File(path), obj);
    }

    public static List<String> readLines(File f) {
        List<String> lines = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = Streams.buffr(Streams.fileInr(f));
            while (br.ready()) {
                lines.add(br.readLine());
            }
        } catch (IOException e) {
            throw Exceptions.wrapThrow(e);
        } finally {
            Streams.safeClose(br);
        }
        return lines;
    }

    public static List<String> readLines(File f, String charset) {
        List<String> lines = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = Streams.buffr(Streams.fileInr(f, charset));
            while (br.ready()) {
                lines.add(br.readLine());
            }
        } catch (IOException e) {
            throw Exceptions.wrapThrow(e);
        } finally {
            Streams.safeClose(br);
        }
        return lines;
    }

    public static LineIterator lineIterator(File f, String charset) {
        return new LineIterator(f, charset);
    }

    public static LineIterator lineIterator(File f) {
        return new LineIterator(f);
    }

    public static class LineIterator implements Iterator<String> {

        BufferedReader br = null;
        String currentLine;
        boolean hasData;

        public LineIterator(File f) {
            br = Streams.buffr(Streams.fileInr(f));
        }

        public LineIterator(File f, String charset) {
            br = Streams.buffr(Streams.fileInr(f, charset));
        }

        @Override
        public boolean hasNext() {
            try {
                currentLine = br.readLine();
                hasData = true;
            } catch (IOException e) {
                Streams.safeClose(br);
                hasData = false;
            }
            return hasData;
        }

        @Override
        public String next() {
            if (hasData) {
                hasData = false;
                return currentLine;
            } else {
                throw new RuntimeException("No Data");
            }
        }
    }
}
