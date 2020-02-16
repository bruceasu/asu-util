package me.asu.util.sys;


import java.lang.management.*;
import java.util.*;
import me.asu.util.sys.OsUtils;

/**
 * @author suk
 */
public class JvmTools {

    /**
     * Returns java stack traces of java threads for the current java process.
     */
    public static List<String> jStack() throws Exception {
        List<String> stackList = new LinkedList<String>();
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        for (Map.Entry<Thread, StackTraceElement[]> entry : allStackTraces.entrySet()) {
            Thread thread = entry.getKey();
            StackTraceElement[] stackTraces = entry.getValue();

            stackList.add(String.format("\"%s\" tid=%s isDaemon=%s priority=%s" + OsUtils.LINE_SEPARATOR,
                    thread.getName(), thread.getId(), thread.isDaemon(), thread.getPriority()));

            stackList.add("java.lang.Thread.State: " + thread.getState() + OsUtils.LINE_SEPARATOR);

            if (stackTraces != null) {
                for (StackTraceElement s : stackTraces) {
                    stackList.add("    " + s.toString() + OsUtils.LINE_SEPARATOR);
                }
            }
        }
        return stackList;
    }

    /**
     * Returns memory usage for the current java process.
     */
    public static List<String> memoryUsage() throws Exception {
        MemoryUsage heapMemoryUsage = MXBeanHolder.MEMORY_MX_BEAN.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = MXBeanHolder.MEMORY_MX_BEAN.getNonHeapMemoryUsage();

        List<String> memoryUsageList = new LinkedList<String>();
        memoryUsageList
                .add("********************************** Memory Usage **********************************"
                        + OsUtils.LINE_SEPARATOR);
        memoryUsageList.add("Heap Memory Usage: " + heapMemoryUsage.toString() + OsUtils.LINE_SEPARATOR);
        memoryUsageList
                .add("NonHeap Memory Usage: " + nonHeapMemoryUsage.toString() + OsUtils.LINE_SEPARATOR);

        return memoryUsageList;
    }

    /**
     * Returns the heap memory used for the current java process.
     */
    public static double memoryUsed() throws Exception {
        MemoryUsage heapMemoryUsage = MXBeanHolder.MEMORY_MX_BEAN.getHeapMemoryUsage();
        return (double) (heapMemoryUsage.getUsed()) / heapMemoryUsage.getMax();
    }


    private static class MXBeanHolder {

        static final MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();
    }
}
