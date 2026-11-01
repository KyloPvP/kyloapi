package hu.paperkit.hud;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Egyeni sidebar scoreboard (a jobb oldali lista), soronknt frissitheto.
 *
 * <p>A klasszikus trkk: minden sor egy egyedi entry vagy team, igy
 * hirt a mar mos tagok lna meg.</p>
 *
 * <pre>{@code
 * ScoreboardKit sb = ScoreboardKit.create("&6&lSZERVER")
 *         .lines("&eElso sor", "&aMasodik sor", "&7...")
 *         .show(player);
 *
 * sb.lines("&eUj bitter", "&aMasodik sor", "&7...");
 * sb.hide(player);
 * }</pre>
 */
public final class ScoreboardKit {

    private final Scoreboard scoreboard;
    private final Objective objective;
    private final List<Team> teams = new ArrayList<>();
    private final List<Player> viewers = new ArrayList<>();

    private ScoreboardKit(final Scoreboard scoreboard, final Objective objective) {
        this.scoreboard = scoreboard;
        this.objective = objective;
    }

    /**
     * Uj sidebar letrehozasa.
     *
     * @param title a cim (kodek formazassal)
     */
    public static ScoreboardKit create(final String title) {
        final Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        final Objective obj = sb.registerNewObjective("paperkit", "dummy",
                ChatColor.translateAlternateColorCodes('&', title == null ? "" : title));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        final ScoreboardKit kit = new ScoreboardKit(sb, obj);
        final ChatColor[] codes = ChatColor.values();
        for (int i = 0; i < 16; i++) {
            final Team team = sb.registerNewTeam("line_" + i);
            // egyedi, nem lathato entry minden sorhoz
            final String uniqueEntry = ChatColor.RESET.toString() + codes[(i * 2) % codes.length];
            team.addEntry(uniqueEntry);
            obj.getScore(uniqueEntry).setScore(15 - i);
            kit.teams.add(team);
        }
        return kit;
    }

    /**
     * Sorok beallitasa (maximum 15 l?that?).
     *
     * @param lines uj tartalom (felulrol lefele)
     */
    public ScoreboardKit lines(final List<String> lines) {
        final List<String> safe = lines == null ? List.of() : lines;
        for (int i = 0; i < 16 && i < safe.size() + 16; i++) {
            if (i >= 16 || i >= safe.size()) {
                setLine(i, "");
            } else {
                setLine(i, safe.get(i));
            }
        }
        return this;
    }

    /**
     * Sorok beallitasa (varargs).
     */
    public ScoreboardKit lines(final String... lines) {
        return lines(List.of(lines == null ? new String[0] : lines));
    }

    /**
     * Egy sor frissitese (0 = legfelso).
     */
    public ScoreboardKit setLine(final int index, final String text) {
        if (index < 0 || index >= teams.size()) {
            return this;
        }
        final Team team = teams.get(index);
        final String formatted = ChatColor.translateAlternateColorCodes('&',
                text == null ? "" : text);
        // prefix/suffix vagnas 16 karaktern kivul felosztva
        final int cut = Math.min(formatted.length(), 16);
        team.setPrefix(formatted.substring(0, cut));
        if (formatted.length() > 16) {
            final String rest = formatted.substring(cut);
            team.setSuffix(rest.substring(0, Math.min(rest.length(), 16)));
        } else {
            team.setSuffix("");
        }
        return this;
    }

    /**
     * Megjelenites egy jatekosnak.
     */
    public ScoreboardKit show(final Player player) {
        if (player != null) {
            player.setScoreboard(scoreboard);
            if (!viewers.contains(player)) {
                viewers.add(player);
            }
        }
        return this;
    }

    /**
     * Elrejtes es feloldasa a jatekos tablaarol.
     */
    public ScoreboardKit hide(final Player player) {
        if (player != null) {
            try {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            } catch (final Exception ignored) {
            }
            viewers.remove(player);
        }
        return this;
    }

    /**
     * Minden nzo eltzatsoja es a scoreboard bombajt?r.
     */
    public void delete() {
        for (final Player player : new ArrayList<>(viewers)) {
            hide(player);
        }
        viewers.clear();
        try {
            objective.unregister();
        } catch (final IllegalStateException ignored) {
        }
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
    }

    /**
     * A nzo listja.
     */
    public List<Player> viewers() {
        return viewers;
    }
}
