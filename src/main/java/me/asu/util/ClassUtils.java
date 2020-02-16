package me.asu.util;

import java.io.*;
import java.lang.reflect.*;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * @author suk
 */
public class ClassUtils {

    private static ExtClasspathLoader extClasspathLoader = new ExtClasspathLoader();

    private static ClassLoader cacheClassLoader;


    static {
        cacheClassLoader = ClassUtils.class.getClassLoader();
        //当使用JavaSE是,如果通过bootClassLoader加载,那么就会为null
        if (cacheClassLoader == null) {
            try {
                cacheClassLoader = ClassLoader.getSystemClassLoader();
            } catch (Throwable e) {
            }
        }
    }

    /**
     * Returns the system {@link ClassLoader}.
     */
    public static ClassLoader getSystemClassLoader() {
        if (System.getSecurityManager() == null) {
            return ClassLoader.getSystemClassLoader();
        } else {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

                @Override
                public ClassLoader run() {
                    return ClassLoader.getSystemClassLoader();
                }
            });
        }
    }

    public static ClassLoader getDefaultClassLoader() {
        return cacheClassLoader;
    }

    public static void addToClasspath(final File path) {
        extClasspathLoader.addURL(path);
    }

    public static void addToClasspath(final String path) {
        extClasspathLoader.addURL(path);
    }

    public static void addToClasspath(final URL path) {
        extClasspathLoader.addURL(path);
    }

    public static void addToClasspathRecursion(final String path, final boolean isDir) {
        if (isDir) {
            extClasspathLoader.loadResourceDirToClasspath(path);
        } else {
            extClasspathLoader.loadJarToClasspath(path);
        }

    }

    /**
     * 以文件的形式来获取包下的所有Class.
     *
     * @param packageName String
     * @param packagePath String
     * @param recursive boolean
     * @param classes Set
     */
    public static void findAndAddClassesInPackageByFile(final String packageName, final String packagePath,
                                                        final boolean recursive, final Set<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            // log.warn("用户定义包名 " + packageName + " 下没有任何文件");
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            @Override
            public boolean accept(final File file) {
                return recursive && file.isDirectory() || file.getName().endsWith(".class");
            }
        });
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(),
                        file.getAbsolutePath(), recursive, classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    // 添加到集合中去
                    // classes.add(Class.forName(packageName + '.' +
                    // className));
                    // 经过回复同学的提醒，这里用forName有一些不好，会触发static方法，没有使用classLoader的load干净
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                } catch (Exception e) {
                    // System.err.println("添加类错误 找不到此类的.class文件");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 从包package中获取所有的Class.
     *
     * @param pack String
     * @return Set
     */
    public static Set<Class<?>> getClasses(final String pack) {
        // 第一个class类的集合
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageName    = pack;
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    System.err.println("file类型的扫描");
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    System.err.println("jar类型的扫描");
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String   name  = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if (idx != -1 || recursive) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1,
                                                name.length() - 6);
                                        try {
                                            // 添加到classes
                                            classes.add(Class.forName(packageName + '.' + className));
                                        } catch (Exception e) {
                                            // log
                                            // .error("添加用户自定义视图类错误 找不到此类的.class文件");
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        // System.err.println("在扫描用户定义视图时从jar包获取文件出错");
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    /**
     * 获取jar的ClassLoader的方法
     */
    public static ClassLoader getClassLoader() {
        ClassLoader classLoader = getContextClassLoader();
        if (classLoader != null) {
            return classLoader;
        }
        return cacheClassLoader;
    }

    /**
     * 获取当前线程的{@link ClassLoader}
     *
     * @return 当前线程的class loader
     * @see Thread#getContextClassLoader()
     */
    public static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 从输入流中读取Class的名字,输入流必须是Class文件格式
     */
    public static String getClassName(InputStream in) {
        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(in));
            Map<Integer, String>  strs    = new HashMap<Integer, String>();
            Map<Integer, Integer> classes = new HashMap<Integer, Integer>();
            dis.skipBytes(4);//Magic
            dis.skipBytes(2);//副版本号
            dis.skipBytes(2);//主版本号

            //读取常量池
            int constant_pool_count = dis.readUnsignedShort();
            for (int i = 0; i < (constant_pool_count - 1); i++) {
                byte flag = dis.readByte();
                switch (flag) {
                case 7://CONSTANT_Class:
                    int index = dis.readUnsignedShort();
                    classes.put(i + 1, index);
                    break;
                case 9://CONSTANT_Fieldref:
                case 10://CONSTANT_Methodref:
                case 11://CONSTANT_InterfaceMethodref:
                    dis.skipBytes(2);
                    dis.skipBytes(2);
                    break;
                case 8://CONSTANT_String:
                    dis.skipBytes(2);
                    break;
                case 3://CONSTANT_Integer:
                case 4://CONSTANT_Float:
                    dis.skipBytes(4);
                    break;
                case 5://CONSTANT_Long:
                case 6://CONSTANT_Double:
                    dis.skipBytes(8);
                    i++;//必须跳过一个,这是class文件设计的一个缺陷,历史遗留问题
                    break;
                case 12://CONSTANT_NameAndType:
                    dis.skipBytes(2);
                    dis.skipBytes(2);
                    break;
                case 1://CONSTANT_Utf8:
                    int len = dis.readUnsignedShort();
                    byte[] data = new byte[len];
                    dis.readFully(data);
                    strs.put(i + 1, new String(data, "UTF-8"));//必然是UTF8的
                    break;
                case 15://CONSTANT_MethodHandle:
                    dis.skipBytes(1);
                    dis.skipBytes(2);
                    break;
                case 16://CONSTANT_MethodType:
                    dis.skipBytes(2);
                    break;
                case 18://CONSTANT_InvokeDynamic:
                    dis.skipBytes(2);
                    dis.skipBytes(2);
                    break;
                default:
                    throw new RuntimeException("Impossible!! flag=" + flag);
                }
            }

            dis.skipBytes(2);//版本控制符
            int    pos  = dis.readUnsignedShort();
            String name = strs.get(classes.get(pos));
            if (name != null) {
                name = name.replace('/', '.');
            }
            dis.close();
            return name;
        } catch (Throwable e) {
            System.err.println("Fail to read ClassName from class InputStream");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从包package中获取所有的符合的文件
     */
    public static Map<String, byte[]> getFiles(final String pack, final String fileType) {
        // fileType = ".class";
        // 第一个class类的集合
        Map<String, byte[]> files = new HashMap<String, byte[]>();

        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageName    = pack;
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上

                if ("file".equals(protocol)) {
                    System.err.println("file类型的扫描");
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    getFromFileSystem(packageName, filePath, fileType, recursive, files);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    getFromJar(url, packageDirName, packageName, fileType, recursive, files);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return files;
    }

    /**
     * 以文件的形式来获取包下的所有Class.
     *
     * @param packageName String
     * @param packagePath String
     * @param fileType String
     * @param recursive boolean
     * @param files Map
     */
    public static void getFromFileSystem(final String packageName,
                                         final String packagePath,
                                         final String fileType,
                                         final boolean recursive,
                                         final Map<String, byte[]> files) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            // log.warn("用户定义包名 " + packageName + " 下没有任何文件");
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            @Override
            public boolean accept(final File file) {
                return recursive && file.isDirectory() || file.getName().endsWith(fileType);
            }
        });
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                getFromFileSystem(packageName + "." + file.getName(), file.getAbsolutePath(), fileType, recursive, files);
            } else {
                // 添加到集合中去
                String name = file.getName();
                name = name.substring(0, name.length() - fileType.length());
                try {
                    files.put(packageName + "." + name, read(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 以JAR的形式来获取包下的所有Class.
     *
     * @param url URL
     * @param packageDirName String
     * @param packageName String
     * @param fileType String
     * @param recursive boolean
     * @param files Map
     */
    public static void getFromJar(final URL url,
                                  final String packageDirName,
                                  String packageName,
                                  final String fileType,
                                  final boolean recursive,
                                  final Map<String, byte[]> files) {
        System.err.println("jar类型的扫描");
        JarFile jar;
        int     length = fileType.length();
        try {
            // 获取jar
            jar = ((JarURLConnection) url.openConnection()).getJarFile();
            // 从此jar包 得到一个枚举类
            Enumeration<JarEntry> entries = jar.entries();
            // 同样的进行循环迭代
            while (entries.hasMoreElements()) {
                // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                JarEntry entry = entries.nextElement();
                String   name  = entry.getName();
                // 如果是以/开头的
                if (name.charAt(0) == '/') {
                    // 获取后面的字符串
                    name = name.substring(1);
                }
                // 如果前半部分和定义的包名相同
                if (name.startsWith(packageDirName)) {
                    int idx = name.lastIndexOf('/');
                    // 如果以"/"结尾 是一个包
                    if (idx != -1) {
                        // 获取包名 把"/"替换成"."
                        packageName = name.substring(0, idx).replace('/', '.');
                    }
                    // 如果可以迭代下去 并且是一个包
                    if (idx != -1 || recursive) {
                        // 如果是一个.class文件 而且不是目录
                        if (name.endsWith(fileType) && !entry.isDirectory()) {
                            String className = name.substring(packageName.length() + 1, name.length()
                                    - length);
                            // get content
                            InputStream input = jar.getInputStream(entry);
                            files.put(packageName + '.' + className, toByteArray(input));
                            closeQuietly(input);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // System.err.println("在扫描用户定义视图时从jar包获取文件出错");
            e.printStackTrace();
        }
    }


    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadClass(ClassLoader currentCL, final String className) {
        if (currentCL == null) {
            currentCL = Thread.currentThread().getContextClassLoader();
        }
        for (; ; ) {
            // Loop through the classloader hierarchy trying to find
            // a viable classloader.
            System.err.println("Trying to load '" + className + "' from classloader " + objectId(currentCL));
            try {
                {
                    // Show the location of the first occurrence of the .class
                    // file
                    // in the classpath. This is the location that
                    // ClassLoader.loadClass
                    // will load the class from -- unless the classloader is
                    // doing
                    // something weird.
                    URL url;
                    String resourceName = className.replace('.', '/') + ".class";
                    if (currentCL != null) {
                        url = currentCL.getResource(resourceName);
                    } else {
                        url = ClassLoader.getSystemResource(resourceName + ".class");
                    }

                    if (url == null) {
                        System.err.println("Class '" + className + "' [" + resourceName + "] cannot be found.");
                    } else {
                        System.err.println("Class '" + className + "' was found at '" + url + "'");
                    }
                }
                Class<T> c = null;
                try {
                    c = (Class<T>) Class.forName(className, true, currentCL);
                } catch (ClassNotFoundException originalClassNotFoundException) {
                    // The current classloader was unable to find the log
                    // adapter
                    // in this or any ancestor classloader. There's no point in
                    // trying higher up in the hierarchy in this case..
                    String msg = "" + originalClassNotFoundException.getMessage();
                    System.err.println("The log adapter '" + className + "' is not available via classloader "
                            + objectId(currentCL) + ": " + msg.trim());
                    try {
                        // Try the class classloader.
                        // This may work in cases where the TCCL
                        // does not contain the code executed or JCL.
                        // This behaviour indicates that the application
                        // classloading strategy is not consistent with the
                        // Java 1.2 classloading guidelines but JCL can
                        // and so should handle this case.
                        c = (Class<T>) Class.forName(className);
                    } catch (ClassNotFoundException secondaryClassNotFoundException) {
                        // no point continuing: this adapter isn't available
                        msg = "" + secondaryClassNotFoundException.getMessage();
                        System.err.println("The log adapter '" + className + "' is not available via the LogFactoryImpl class classloader: "
                                + msg.trim());
                        break;
                    }
                }
                return c;
            } catch (Throwable t) {
                String msg = "" + t.getMessage();
                System.err.println("The log adapter '" + className + "' is missing dependencies when loaded via classloader "
                        + objectId(currentCL) + ": " + msg.trim());
            }

            if (currentCL == null) {
                break;
            }

            // try the parent classloader
            // currentCL = currentCL.getParent();
            currentCL = getParentClassLoader(currentCL);
        }
        return null;
    }

    public static <T> Class<T> loadClass(final ClassLoader currentCL, final String... classNames) {
        if (classNames == null || classNames.length == 0) {
            return null;
        }
        for (String className : classNames) {
            Class<T> t = loadClass(currentCL, className);
            if (t != null) {
                return t;
            }
        }

        return null;
    }

    public static <T> Class<T> loadClass(final String... classNames) {
        return loadClass((ClassLoader) null, classNames);
    }

    /**
     * 通获取类声明对象
     *
     * @param name 全限定类名
     * @return 获取类声明对象，如果对应的类文件不在，则返回null
     * @since 1.0.0
     */
    public static <T> Class<T> loadClass(final String name) {
        return loadClass(cacheClassLoader, name);

    }

    /**
     * 通过全限定类名获取类声明对象
     *
     * @param className 全限定类名
     * @param clazz 父类声明类
     * @return 获取类声明对象的父类，如果对应的类文件不在，则返回null
     * @since 1.0.0
     */
    public static <T> Class<? extends T> loadClass(final String className, final Class<? extends T> clazz) {
        Class<?> clazzClass = loadClass(className);
        if (clazzClass == null) {
            return null;
        }

        try {
            return clazzClass.asSubclass(clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ----------------------------------------------------------------------------------- isPresent

    /**
     * 指定类是否被提供，使用默认ClassLoader<br>
     * 通过调用{@link #loadClass(ClassLoader, String)}方法尝试加载指定类名的类，如果加载失败返回false<br>
     * 加载失败的原因可能是此类不存在或其关联引用类不存在
     *
     * @param className 类名
     * @return 是否被提供
     */
    public static boolean isPresent(String className) {
        return isPresent(className, null);
    }

    /**
     * 指定类是否被提供<br>
     * 通过调用{@link #loadClass(ClassLoader, String)}方法尝试加载指定类名的类，如果加载失败返回false<br>
     * 加载失败的原因可能是此类不存在或其关联引用类不存在
     *
     * @param className 类名
     * @param classLoader {@link ClassLoader}
     * @return 是否被提供
     */
    public static boolean isPresent(String className, ClassLoader classLoader) {
        try {
            loadClass(classLoader, className);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    public static String objectId(final Object o) {
        if (o == null) {
            return "null";
        } else {
            return o.getClass().getName() + "@" + System.identityHashCode(o);
        }
    }

    /**
     * Find a jar that contains a class of the same name, if any.
     * It will return a jar file, even if that is not the first thing
     * on the class path that has a class with the same name.
     *
     * @param clazz the class to find.
     * @return a jar file that contains the class, or null.
     */
    public static String findContainingJar(Class<?> clazz) {
        ClassLoader loader    = clazz.getClassLoader();
        String      classFile = clazz.getName().replaceAll("\\.", "/") + ".class";
        try {
            for (final Enumeration<URL> itr = loader.getResources(classFile); itr.hasMoreElements(); ) {
                final URL url = itr.nextElement();
                if ("jar".equals(url.getProtocol())) {
                    String toReturn = url.getPath();
                    if (toReturn.startsWith("file:")) {
                        toReturn = toReturn.substring("file:".length());
                    }
                    toReturn = URLDecoder.decode(toReturn, "UTF-8");
                    return toReturn.replaceAll("!.*$", "");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * 获取一个类的class文件所在的绝对路径。 这个类可以是JDK自身的类，
     * 也可以是用户自定义的类，或者是第三方开发包里的类。
     * 只要是在本程序中可以被加载的类，都可以定位到它的class文件的绝对路径。
     *
     * @param cls 一个对象的Class属性
     * @return 这个类的class文件位置的绝对路径。 如果没有这个类的定义，则返回null。
     */
    public static String getPathFromClass(Class<?> cls) throws IOException {
        String path = null;
        if (cls == null) {
            throw new NullPointerException();
        }
        URL url = getClassLocationURL(cls);
        if (url != null) {
            path = url.getPath();
            if ("jar".equalsIgnoreCase(url.getProtocol())) {
                try {
                    path = new URL(path).getPath();
                } catch (MalformedURLException e) {
                }
                int location = path.indexOf("!/");
                if (location != -1) {
                    path = path.substring(0, location);
                }
            }
            File file = new File(path);
            path = file.getCanonicalPath();
        }
        return path;
    }

    /**
     * 这个方法可以通过与某个类的class文件的相对路径来获取文件或目录的绝对路径。
     * 通常在程序中很难定位某个相对路径，特别是在B/S应用中。
     * 通过这个方法，我们可以根据我们程序自身的类文件的位置来定位某个相对路径。
     * 比如：某个txt文件相对于程序的Test类文件的路径是../../resource/test.txt，
     * 那么使用本方法Path.getFullPathRelateClass("../../resource/test.txt",Test.class)
     * 得到的结果是txt文件的在系统中的绝对路径。
     *
     * @param relatedPath 相对路径
     * @param cls 用来定位的类
     * @return 相对路径所对应的绝对路径
     * @throws IOException 因为本方法将查询文件系统，所以可能抛出IO异常
     */
    public static String getFullPathRelateClass(String relatedPath, Class<?> cls) throws IOException {
        String path = null;
        if (relatedPath == null) {
            throw new NullPointerException();
        }
        String clsPath  = getPathFromClass(cls);
        File   clsFile  = new File(clsPath);
        String tempPath = clsFile.getParent() + File.separator + relatedPath;
        File   file     = new File(tempPath);
        path = file.getCanonicalPath();
        return path;
    }

    /**
     * 获取类的class文件位置的URL。这个方法是本类最基础的方法，供其它方法调用。
     */
    private static URL getClassLocationURL(final Class<?> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("null input: cls");
        }
        URL result = null;
        final String clsAsResource = cls.getName().replace('.', '/').concat(".class");
        final ProtectionDomain pd = cls.getProtectionDomain();
        // java.lang.Class contract does not specify
        // if 'pd' can ever be null;
        // it is not the case for Sun's implementations,
        // but guard against null
        // just in case:
        if (pd != null) {
            final CodeSource cs = pd.getCodeSource();
            // 'cs' can be null depending on
            // the classloader behavior:
            if (cs != null) {
                result = cs.getLocation();
            }

            if (result != null) {
                // Convert a code source location into
                // a full class file location
                // for some common cases:
                if ("file".equals(result.getProtocol())) {
                    try {
                        if (result.toExternalForm().endsWith(".jar") || result.toExternalForm().endsWith(".zip")) {
                            result = new URL("jar:".concat(result.toExternalForm()).concat("!/").concat(clsAsResource));
                        } else if (new File(result.getFile()).isDirectory()) {
                            result = new URL(result, clsAsResource);
                        }
                    } catch (MalformedURLException ignore) {
                    }
                }
            }
        }

        if (result == null) {
            // Try to find 'cls' definition as a resource;
            // this is not
            // document．d to be legal, but Sun's
            // implementations seem to //allow this:
            final ClassLoader clsLoader = cls.getClassLoader();
            result = clsLoader != null ? clsLoader.getResource(clsAsResource) : ClassLoader.getSystemResource(clsAsResource);
        }
        return result;
    }


    private static byte[] read(File file) throws IOException {
        FileInputStream input = new FileInputStream(file);
        byte[]          data  = null;
        try {
            data = toByteArray(input);
        } catch (IOException e) {
            closeQuietly(input);
            throw e;
        }
        return data;
    }

    private static byte[] toByteArray(InputStream input) throws IOException {
        if (input == null || input.available() == 0) {
            return null;
        }
        ByteArrayOutputStream baos   = new ByteArrayOutputStream();
        byte[]                buffer = new byte[8192];
        int                   n      = 0;
        while (-1 != (n = input.read(buffer))) {
            baos.write(buffer, 0, n);
        }

        return baos.toByteArray();
    }

    private static void closeQuietly(Closeable input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
            }
        }

    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ClassLoader getParentClassLoader(final ClassLoader cl) {
        try {
            return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                @Override
                public Object run() {
                    return cl.getParent();
                }
            });
        } catch (SecurityException ex) {
            System.err.println("[ERROR] Unable to obtain parent classloader");
            return null;
        }

    }


    private static final Class<?>[] EMPTY_ARRAY = new Class[]{};

    /**
     * Cache of constructors for each class. Pins the classes so they
     * can't be garbage collected until ReflectionUtils can be collected.
     */
    private static final Map<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<Class<?>, Constructor<?>>();


    /**
     * Create an object for the given class and initialize it from conf
     *
     * @param theClass class of which an object is created
     * @return a new object
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> theClass) {
        T result;
        try {
            Constructor<T> meth = (Constructor<T>) CONSTRUCTOR_CACHE.get(theClass);
            if (meth == null) {
                meth = theClass.getDeclaredConstructor(EMPTY_ARRAY);
                meth.setAccessible(true);
                CONSTRUCTOR_CACHE.put(theClass, meth);
            }
            result = meth.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 通过全限定类名获取实例.
     *
     * @param className 全限定类名
     * @return T
     * @since 1.0.0
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> T newInstance(final String className) {
        try {
            Class c = Class.forName(className);
            return (T) newInstance(c);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取一个 Type 类型实际对应的Class
     *
     * @param type 类型
     * @return 与Type类型实际对应的Class
     */
    @SuppressWarnings("rawtypes")
    public static Class<?> getTypeClass(Type type)
    {
        Class<?> clazz = null;
        if (type instanceof Class<?>) {
            clazz = (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            clazz = (Class<?>) pt.getRawType();
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gat       = (GenericArrayType) type;
            Class<?>         typeClass = getTypeClass(gat.getGenericComponentType());
            return Array.newInstance(typeClass, 0).getClass();
        } else if (type instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) type;
            Type[]       ts = tv.getBounds();
            if (ts != null && ts.length > 0) {
                return getTypeClass(ts[0]);
            }
        } else if (type instanceof WildcardType) {
            WildcardType wt    = (WildcardType) type;
            Type[]       t_low = wt.getLowerBounds();// 取其下界
            if (t_low.length > 0) {
                return getTypeClass(t_low[0]);
            }
            Type[] t_up = wt.getUpperBounds(); // 没有下界?取其上界
            return getTypeClass(t_up[0]);// 最起码有Object作为上界
        }
        return clazz;
    }

    /**
     * 返回一个 Type 的泛型数组, 如果没有, 则直接返回null
     *
     * @param type 类型
     * @return 一个 Type 的泛型数组, 如果没有, 则直接返回null
     */
    public static Type[] getGenericsTypes(Type type)
    {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            return pt.getActualTypeArguments();
        }
        return null;
    }
}
