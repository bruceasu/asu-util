package me.asu.util.sys;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Enumeration;
import sun.security.action.GetPropertyAction;

/**
 * OsUtils.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-09-12 11:35
 */
public class OsUtils {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String LINUX          = "Linux";
    public static final String windows        = "Windows";

    public static boolean isWindows() {
        PrivilegedAction pa = new GetPropertyAction("os.name");
        String osname = (String) AccessController.doPrivileged(pa);
        return (osname.startsWith(windows));
    }

    public static boolean isLinux() {
        PrivilegedAction pa = new GetPropertyAction("os.name");
        String osname = (String) AccessController.doPrivileged(pa);
        return ("Linux".equals(osname));
    }

    public static boolean isSupportEPoll() {
        PrivilegedAction pa = new GetPropertyAction("os.name");
        String osname = (String) AccessController.doPrivileged(pa);
        // use EPollSelectorProvider for Linux kernels >= 2.6

        if (LINUX.equals(osname)) {
            pa = new GetPropertyAction("os.version");
            String osversion = (String) AccessController.doPrivileged(pa);
            String[] vers = osversion.split("\\.", 0);
            if (vers.length >= 2) {
                try {
                    int major = Integer.parseInt(vers[0]);
                    int minor = Integer.parseInt(vers[1]);
                    if (major > 2 || (major == 2 && minor >= 6)) {
                        return true;
                    }
                } catch (NumberFormatException x) {
                    // format not recognized
                }
            }
        }

        return false;
    }

    /*
     *
     * InetAddress 继承自 java.lang.Object类
     * 它有两个子类：Inet4Address 和 Inet6Address
     * 此类表示互联网协议 (IP) 地址。
     *
     * IP 地址是 IP 使用的 32 位或 128 位无符号数字，
     * 它是一种低级协议，UDP 和 TCP 协议都是在它的基础上构建的。
     *
     * ************************************************
     * 主机名就是计算机的名字（计算机名），网上邻居就是根据主机名来识别的。
     * 这个名字可以随时更改，从我的电脑属性的计算机名就可更改。
     *  用户登陆时候用的是操作系统的个人用户帐号，这个也可以更改，
     *  从控制面板的用户界面里改就可以了。这个用户名和计算机名无关。
     */

    /**
     * 获取本机的IP
     *
     * @return Ip地址
     */
    public static String getLocalHostIP() {
        String ip;
        try {
            /**返回本地主机。*/
            InetAddress addr = InetAddress.getLocalHost();
            /**返回 IP 地址字符串（以文本表现形式）*/
            ip = addr.getHostAddress();
        } catch (Exception ex) {
            ip = "";
        }

        return ip;
    }

    /**
     * @return 主机名
     */
    public static String getHostName() {
        if (System.getenv("COMPUTERNAME") != null) {
            return System.getenv("COMPUTERNAME");
        } else {
            return getHostNameFromJava();
        }
    }

    public static String getHostNameFromJava() {
        String hostName = "UnknownHost";
        try {
            hostName = getHostNameFromInetAddress(hostName);
        } catch (Exception ex) {
            // host = "hostname: hostname"
            String host = ex.getMessage();
            if (host != null) {
                int colon = host.indexOf(':');
                if (colon > 0) {
                    hostName = host.substring(0, colon);
                }
            } else {
                hostName = getHostNameFromMXBean();
            }
        }

        return hostName;
    }

    private static String getHostNameFromInetAddress(String hostName) throws UnknownHostException {
        /**返回本地主机。*/
        InetAddress addr = InetAddress.getLocalHost();
        /**获取此 IP 地址的主机名。*/
        hostName = addr.getHostName();
        return hostName;
    }

    public static String getHostNameFromMXBean() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName();
        int index = name.indexOf("@");
        if (index != -1) {
            return name.substring(index + 1);
        } else {
            return "localhost";
        }
    }


    /**
     * 获得本地所有的IP地址
     *
     * @return all ip addresses
     */
    public static String[] getAllLocalHostIP() {
        String[] ret = null;
        try {
            /*获得主机名*/
            String hostName = getHostNameFromJava();
            if (hostName.length() > 0) {
                /*在给定主机名的情况下，根据系统上配置的名称服务返回其 IP 地址所组成的数组。*/
                InetAddress[] addrs = InetAddress.getAllByName(hostName);
                if (addrs.length > 0) {
                    ret = new String[addrs.length];
                    for (int i = 0; i < addrs.length; i++) {
                        /*.getHostAddress()   返回 IP 地址字符串（以文本表现形式）。*/
                        ret[i] = addrs[i].getHostAddress();
                    }
                }
            }

        } catch (Exception ex) {
            ret = null;
        }

        return ret;
    }

    /**
     * Creates the process identifier.  This does not have to be unique per class loader because
     * NEXT_COUNTER will provide the uniqueness.
     */
    public static int createProcessIdentifier() {
        int processId = -1;
        try {
            processId = getPid();
        } catch (Throwable t) {
            processId = new SecureRandom().nextInt();
            System.err.println(
                    "Failed to get process identifier from JMX, using random number instead");
        }
        return processId;
    }

    public static int getPid() {
        try {
            return getPidFromMXBean();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int getPidFromMXBean() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName();
        if (name.contains("@")) {
            int index = name.indexOf("@");
            return Integer.parseInt(name.substring(0, index));
        } else {
            return name.hashCode();
        }

    }

    public static int createMachineIdentifier() {
        // build a 2-byte machine piece based on NICs info
        int machinePiece;
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                sb.append(ni.toString());
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    ByteBuffer bb = ByteBuffer.wrap(mac);
                    try {
                        sb.append(bb.getChar());
                        sb.append(bb.getChar());
                        sb.append(bb.getChar());
                    } catch (BufferUnderflowException shortHardwareAddressException) { //NOPMD
                        // mac with less than 6 bytes. continue
                    }
                }
            }
            machinePiece = sb.toString().hashCode();
        } catch (Throwable t) {
            // exception sometimes happens with IBM JVM, use random
            machinePiece = (new SecureRandom().nextInt());
            System.err.println(
                    "Failed to get machine identifier from network interface, using random number instead");
        }
        return machinePiece;
    }

    public static boolean checkPidInProcess(int pid) {
        InputStream in = null;
        BufferedReader b = null;
        String line = null;
        try {
            in = Runtime.getRuntime().exec("jps").getInputStream();
            b = new BufferedReader(new InputStreamReader(in));
            while ((line = b.readLine()) != null) {
                if (line.startsWith(pid + " ")) {
                    return true;
                }
            }
        } catch (IOException e) {
            // ignore
        } finally {
            try {
                if (b != null) {
                    b.close();
                }
            } catch (Exception e) {
                // ignore
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println("是否支持epoll：" + isSupportEPoll());
        System.out.println("当前进程的PID为：" + getPidFromMXBean());
        long s = System.nanoTime();
        System.out.println("本机机器名：" + getHostName());
        long e = System.nanoTime();
        System.out.println("cost: " + (e - s));
        System.out.println("------------------------------------");
        System.out.println("本机IP地址：" + getLocalHostIP());
        long s2 = System.nanoTime();
        System.out.println("本机机器名：" + getHostNameFromJava());
        long e2 = System.nanoTime();
        System.out.println("cost: " + (e2 - s2));
        String[] allLocalHostIP = getAllLocalHostIP();
        for (String host : allLocalHostIP) {
            System.out.println("host: " + host);
        }
    }
}
