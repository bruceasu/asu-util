package me.asu.util;
/**
 * ExtClasspathLoader.
 *
 * @author Suk Honzeon
 * @since 2012-9-13 下午7:04:00
 * @version V1.0
 */
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public final class ExtClasspathLoader {
  /** URLClassLoader的addURL方法 */
  private final Method addURL = initAddMethod();

  private final URLClassLoader classloader = getClassLoader();

  /**
   * 通过filepath加载文件到classpath。
   *
   * @param file 文件路径
   * @return URL
   * @throws Exception 异常
   */
  public void addURL(File file) {
    try {
      file = file.isDirectory() ? file : file.getParentFile();
      addURL(file.toURI().toURL());
    } catch (Exception e) {
    }

  }

  public void addURL(final String path) {
    File file = new File(path);
    addURL(file);
  }

  public void addURL(final URL url) {
    try {
      addURL.invoke(classloader, new Object[]{url});
    } catch (Exception e) {
    }
  }

  public void loadJarToClasspath(final String filepath) {
    File file = new File(filepath);
    loopFiles(file);
  }

  public void loadResourceDirToClasspath(final String filepath) {
    File file = new File(filepath);
    loopDirs(file);
  }

  private URLClassLoader getClassLoader() {
    return (URLClassLoader) ClassLoader.getSystemClassLoader();
  }

  /**
   * 初始化addUrl 方法.
   *
   * @return 可访问addUrl方法的Method对象
   */
  private Method initAddMethod() {
    try {
      Method add = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
      add.setAccessible(true);
      return add;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 循环遍历目录，找出所有的资源路径。
   *
   * @param file 当前遍历文件
   */
  private void loopDirs(final File file) {
    // 资源文件只加载路径
    if (file.isDirectory()) {
      addURL(file);
      File[] tmps = file.listFiles();
      for (File tmp : tmps) {
        loopDirs(tmp);
      }
    }
  }

  /**
   * 循环遍历目录，找出所有的jar包。
   *
   * @param file 当前遍历文件
   */
  private void loopFiles(final File file) {
    if (file.isDirectory()) {
      File[] tmps = file.listFiles();
      for (File tmp : tmps) {
        loopFiles(tmp);
      }
    } else {
      if (file.getAbsolutePath().endsWith(".jar") || file.getAbsolutePath().endsWith(".zip")) {
        addURL(file);
      }
    }
  }
}
