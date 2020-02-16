package me.asu.util;

/**
 * 获取源代码行号
 *
 * @author suk
 * @version 1.0.0
 * @date 2016/3/7 14:20
 */
public class LineNo {

    public static int getLineNumber() {
        return Thread.currentThread().getStackTrace()[3].getLineNumber();
    }

    public static String getFileName() {
        return Thread.currentThread().getStackTrace()[3].getFileName();
    }

    public static void logFileLineNo() {
        System.out.println("run to [" + getFileName() + "：" + getLineNumber() + "]");
    }

    public static void logFileLineNo(String msg) {
        System.out.println("[" + getFileName() + "：" + getLineNumber() + "] " + msg);
    }

    public static void main(String[] args) {
        System.out.println("[" + getFileName() + "：" + getLineNumber() + "]" + "Hello World!");
    }
}
