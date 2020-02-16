package me.asu.util;

/**
 * @author suk
 */
public abstract class Threads
{

    /**
     * 对Thread.sleep(long)的简单封装,不抛出任何异常
     *
     * @param millisecond 休眠时间
     */
    public static void quiteSleep(long millisecond)
    {
        try {
            if (millisecond > 0) {
                Thread.sleep(millisecond);
            }
        } catch (Exception e) {
        }
    }

    /**
     * 一个便利的方法，将当前线程睡眠一段时间
     *
     * @param ms 要睡眠的时间 ms
     */
    public static void sleep(long ms)
    {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw Exceptions.wrapThrow(e);
        }
    }

    /**
     * 一个便利的等待方法同步一个对象
     *
     * @param lock 锁对象
     * @param ms   要等待的时间 ms
     */
    public static void wait(Object lock, long ms)
    {
        if (null != lock) {
            synchronized (lock) {
                try {
                    lock.wait(ms);
                } catch (InterruptedException e) {
                    throw Exceptions.wrapThrow(e);
                }
            }
        }
    }

    /**
     * 通知对象的同步锁
     *
     * @param lock 锁对象
     */
    public static void notifyAll(Object lock)
    {
        if (null != lock) {
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }


    /**
     * @return 返回当前程序运行的根目录
     */
    public static String runRootPath()
    {
        String cp = Threads.class.getClassLoader().getResource("").toExternalForm();
        if (cp.startsWith("file:")) {
            cp = cp.substring("file:".length());
        }
        return cp;
    }
}
