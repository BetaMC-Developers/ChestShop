package com.LRFLEW.register.payment.forChestShop;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.HashSet;
import java.util.Set;

/**
 * The <code>Methods</code> initializes Methods that utilize the Method interface
 * based on a "first come, first served" basis.
 * <p/>
 * Allowing you to check whether a payment method exists or not.
 * <p/>
 * Methods also allows you to set a preferred method of payment before it captures
 * payment plugins in the initialization process.
 * <p/>
 * in server.properties:
 * <blockquote><pre>
 *  economy=iConomy
 * </pre></blockquote>
 *
 * @author: Nijikokun <nijikokun@shortmail.com> (@nijikokun)
 * @copyright: Copyright (C) 2011
 * @license: AOL license <http://aol.nexua.org>
 */
public class Methods {
    private static String version = null;
    private static boolean self = false;
    private static Method Method = null;
    public static String preferred = "";
    private static Set<Method> Methods = new HashSet<Method>();
    private static Set<String> Dependencies = new HashSet<String>();
    private static Set<Method> Attachables = new HashSet<Method>();

    static { _init(); }

    /**
     * Implement all methods along with their respective name & class.
     */
    private static void _init() {
//        addMethod("iConomy", new com.LRFLEW.register.payment.forChestShop.methods.iCo6());
//        addMethod("iConomy", new com.LRFLEW.register.payment.forChestShop.methods.iCo5());
//        addMethod("iConomy", new com.LRFLEW.register.payment.forChestShop.methods.iCo4());
//        addMethod("BOSEconomy", new com.LRFLEW.register.payment.forChestShop.methods.BOSE6());
//        addMethod("BOSEconomy", new com.LRFLEW.register.payment.forChestShop.methods.BOSE7());
        addMethod("ZCore", new com.LRFLEW.register.payment.forChestShop.methods.ZCoreEco());
        addMethod("Essentials", new com.LRFLEW.register.payment.forChestShop.methods.EE17());
//        addMethod("Currency", new com.LRFLEW.register.payment.forChestShop.methods.MCUR());
        Dependencies.add("MultiCurrency");
    }

    /**
     * Used by the plugin to setup version
     *
     * @param v version
     */
    public static void setVersion(String v) {
        version = v;
    }

    /**
     * Use to reset methods during disable
     */
    public static void reset() {
        version = null;
        self = false;
        Method = null;
        preferred = "";
        Attachables.clear();
    }

    /**
     * Use to get version of Register plugin
     *
     * @return version
     */
    public static String getVersion() {
        return version;
    }

    /**
     * Returns an array of payment method names that have been loaded
     * through the <code>_init</code> method.
     *
     * @return <code>Set<String></code> - Array of payment methods that are loaded.
     */
    public static Set<String> getDependencies() {
        return Dependencies;
    }

    /**
     * Interprets Plugin class data to verify whether it is compatible with an existing payment
     * method to use for payments and other various economic activity.
     *
     * @param plugin Plugin data from bukkit, Internal Class file.
     * @return Method <em>or</em> Null
     */
    public static Method createMethod(Plugin plugin) {
        for (Method method : Methods) {
            if (method.isCompatible(plugin)) {
                method.setPlugin(plugin);
                return method;
            }
        }

        return null;
    }

    private static void addMethod(String name, Method method) {
        Dependencies.add(name);
        Methods.add(method);
    }

    /**
     * Verifies if Register has set a payment method for usage yet.
     *
     * @return <code>boolean</code>
     * @see #checkDisabled(org.bukkit.plugin.Plugin)
     */
    public static boolean hasMethod() {
        return (Method != null);
    }

    /**
     * Checks Plugin Class against a multitude of checks to verify it's usability
     * as a payment method.
     *
     * @return <code>boolean</code> True on success, False on failure.
     */
    public static boolean setMethod(PluginManager manager) {
        if (hasMethod()) return true;
        if (self) { self = false; return false; }

        int count = 0;
        boolean match = false;
        Plugin plugin;

        for (String name : Dependencies) {
            if (hasMethod()) break;
            plugin = manager.getPlugin(name);
            if (plugin == null) continue;

            Method current = createMethod(plugin);
            if (current == null) continue;

            if (preferred.isEmpty())
                Method = current;
            else {
                Attachables.add(current);
            }
        }

        if (!preferred.isEmpty()) {
            do {
                if (hasMethod()) {
                    match = true;
                } else {
                    for (Method attached : Attachables) {
                        if (attached == null) continue;

                        if (hasMethod()) {
                            match = true;
                            break;
                        }

                        if (preferred.isEmpty()) Method = attached;

                        if (count == 0) {
                            if (preferred.equalsIgnoreCase(attached.getName()))
                                Method = attached;
                        } else {
                            Method = attached;
                        }
                    }

                    count++;
                }
            } while (!match);
        }

        return hasMethod();
    }

    /**
     * Grab the existing and initialized (hopefully) Method Class.
     *
     * @return <code>Method</code> <em>or</em> <code>Null</code>
     */
    public static Method getMethod() {
        return Method;
    }

    /**
     * Verify is a plugin is disabled, only does this if we there is an existing payment
     * method initialized in Register.
     *
     * @param method Plugin data from bukkit, Internal Class file.
     * @return <code>boolean</code>
     */
    public static boolean checkDisabled(Plugin method) {
        if (!hasMethod()) return true;
        if (Method.isCompatible(method)) Method = null;
        return (Method == null);
    }
}
