package me.asu.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * StartupShutdownMessage
 * @author suk
 * @version 1.0.0
 * @since 2017-10-16 17:42
 */
public class StartupShutdownMessage {
    private static String toStartupShutdownString(String prefix, String[] msg) {
        StringBuilder b = new StringBuilder(prefix);
        b.append("\n/************************************************************");
        String[] var3 = msg;
        int var4 = msg.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String s = var3[var5];
            b.append("\n" + prefix + s);
        }

        b.append("\n************************************************************/");
        return b.toString();
    }

    public static void startupShutdownMessage(Class<?> clazz, String[] args, String version) {
        final String hostname = getHostname();
        final String classname = clazz.getSimpleName();
        System.out.println(toStartupShutdownString("启动信息: ", new String[]{"  启动 " + classname,
                "  主机 = " + hostname,
                "  参数 = " + Arrays.asList(args),
                "  版本 = " + version ,
                "  classpath = " + System.getProperty("java.class.path"),
                "  java = " + System.getProperty("java.version")}));
        ShutdownHookManager.get().addShutdownHook(new Runnable() {
            @Override
            public void run() {
                System.out.println(toStartupShutdownString("关闭信息: ", new String[]{"Shutting down " + classname + " at " + hostname}));
            }
        }, 0);
    }

    public static String getHostname() {
        try {
            return "" + InetAddress.getLocalHost();
        } catch (UnknownHostException var1) {
            return "" + var1;
        }
    }
}
