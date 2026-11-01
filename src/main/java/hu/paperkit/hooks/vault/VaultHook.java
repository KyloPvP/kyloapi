package hu.paperkit.hooks.vault;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

import hu.paperkit.hooks.Hooks;

/**
 * Vault economy hook (reflection-nel: compile idejű Vault
 * dependency nelkkelee mukodik).
 *
 * <pre>{@code
 * if (Hooks.isPluginEnabled("Vault") && VaultHook.hooked()) {
 *     double money = VaultHook.balance(player);
 *     VaultHook.give(player, 100);
 *     VaultHook.take(player, 50);
 * }
 * }</pre>
 */
public final class VaultHook {

    private static Object economy;
    private static Method balanceMethod;
    private static Method withdrawMethod;
    private static Method depositMethod;
    private static Method hasMethod;
    private static boolean attempted;

    private VaultHook() {
    }

    /**
     * Megprobalja megszerezni a Vault economy szolgaltatast.
     *
     * @return true, ha sikerult
     */
    public static boolean hooked() {
        if (attempted) {
            return economy != null;
        }
        attempted = true;
        if (!Hooks.isPluginEnabled("Vault")) {
            return false;
        }
        try {
            final Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            final ServicesManager sm = Bukkit.getServicesManager();
            @SuppressWarnings("unchecked")
            final RegisteredServiceProvider<Object> registration =
                    (RegisteredServiceProvider<Object>) sm.getRegistration((Class) economyClass);
            if (registration == null) {
                return false;
            }
            economy = registration.getProvider();
            balanceMethod = economyClass.getMethod("getBalance", OfflinePlayer.class);
            withdrawMethod = economyClass.getMethod("withdrawPlayer", OfflinePlayer.class, double.class);
            depositMethod = economyClass.getMethod("depositPlayer", OfflinePlayer.class, double.class);
            hasMethod = economyClass.getMethod("has", OfflinePlayer.class, double.class);
            return economy != null;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * A jatekos penze.
     */
    public static double balance(final OfflinePlayer player) {
        if (!hooked()) {
            return 0;
        }
        try {
            return (double) balanceMethod.invoke(economy, player);
        } catch (final Exception e) {
            return 0;
        }
    }

    /**
     * Van-e eleg penze.
     */
    public static boolean has(final OfflinePlayer player, final double amount) {
        if (!hooked()) {
            return false;
        }
        try {
            return (boolean) hasMethod.invoke(economy, player, amount);
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Penz hozzáadasa.
     */
    public static void give(final OfflinePlayer player, final double amount) {
        if (!hooked()) {
            return;
        }
        try {
            depositMethod.invoke(economy, player, amount);
        } catch (final Exception e) {
            // eltuntincs
        }
    }

    /**
     * Penz elvetel�odakul.  (amount vissza gy port)
     */
    public static void take(final OfflinePlayer player, final double amount) {
        if (!hooked()) {
            return;
        }
        try {
            withdrawMethod.invoke(economy, player, amount);
        } catch (final Exception e) {
            // eltuntincs
        }
    }
}
