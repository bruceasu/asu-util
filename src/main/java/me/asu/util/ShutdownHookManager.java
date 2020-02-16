package me.asu.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Suk
 */
public class ShutdownHookManager {

    private static final ShutdownHookManager MGR                = new ShutdownHookManager();
    private static final Logger              LOG                = LoggerFactory.getLogger(ShutdownHookManager.class);
    private              Set<HookEntry>      hooks              = Collections.synchronizedSet(new HashSet());
    private              AtomicBoolean       shutdownInProgress = new AtomicBoolean(false);

    public static ShutdownHookManager get() {
        return MGR;
    }

    private ShutdownHookManager() {
    }

    List<Runnable> getShutdownHooksInOrder() {
        Set ordered = MGR.hooks;
        ArrayList list;
        synchronized (MGR.hooks) {
            list = new ArrayList(MGR.hooks);
        }

        Collections.sort(list, new Comparator<HookEntry>() {
            @Override
            public int compare(ShutdownHookManager.HookEntry o1, ShutdownHookManager.HookEntry o2) {
                return o2.priority - o1.priority;
            }
        });
        ArrayList ordered1 = new ArrayList();
        Iterator var3 = list.iterator();

        while (var3.hasNext()) {
            ShutdownHookManager.HookEntry entry = (ShutdownHookManager.HookEntry) var3.next();
            ordered1.add(entry.hook);
        }

        return ordered1;
    }

    public void addShutdownHook(Runnable shutdownHook, int priority) {
        if (shutdownHook == null) {
            throw new IllegalArgumentException("shutdownHook cannot be NULL");
        } else if (this.shutdownInProgress.get()) {
            throw new IllegalStateException("Shutdown in progress, cannot add a shutdownHook");
        } else {
            this.hooks.add(new ShutdownHookManager.HookEntry(shutdownHook, priority));
        }
    }

    public boolean removeShutdownHook(Runnable shutdownHook) {
        if (this.shutdownInProgress.get()) {
            throw new IllegalStateException("Shutdown in progress, cannot remove a shutdownHook");
        } else {
            return this.hooks.remove(new ShutdownHookManager.HookEntry(shutdownHook, 0));
        }
    }

    public boolean hasShutdownHook(Runnable shutdownHook) {
        return this.hooks.contains(new ShutdownHookManager.HookEntry(shutdownHook, 0));
    }

    public boolean isShutdownInProgress() {
        return this.shutdownInProgress.get();
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                ShutdownHookManager.MGR.shutdownInProgress.set(true);
                Iterator var1 = ShutdownHookManager.MGR.getShutdownHooksInOrder().iterator();

                while (var1.hasNext()) {
                    Runnable hook = (Runnable) var1.next();
                    try {
                        hook.run();
                    } catch (Throwable var4) {
                        ShutdownHookManager.LOG
                                .warn("ShutdownHook \'" + hook.getClass().getSimpleName()
                                              + "\' failed, " + var4.toString(), var4);
                    }
                }

            }
        });
    }

    private static class HookEntry {

        Runnable hook;
        int      priority;

        public HookEntry(Runnable hook, int priority) {
            this.hook = hook;
            this.priority = priority;
        }

        @Override
        public int hashCode() {
            return this.hook.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            boolean eq = false;
            if (obj != null && obj instanceof ShutdownHookManager.HookEntry) {
                eq = this.hook == ((ShutdownHookManager.HookEntry) obj).hook;
            }

            return eq;
        }
    }
}
