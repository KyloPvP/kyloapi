package hu.paperkit.hud;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * 3D hang leszt?o (volume, pitch, hangasz?rok?nas sorban).
 *
 * <pre>{@code
 * SoundKit.playerPlay(player).play(Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
 *
 * SoundKit.sequence(player)
 *         .push(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f)
 *         .at(3, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.3f)
 *         .start();
 * }</pre>
 */
public final class SoundKit {

    private SoundKit() {
    }

    /**
     * Egy hang lejatszasa a jatekosnak.
     */
    public static void playerPlay(final Player player, final Sound sound,
            final float volume, final float pitch) {
        if (player == null || sound == null) {
            return;
        }
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    /**
     * Egy hang lejatszasa a j?n?tt?ny.Cinel?ll?r?ssne k?zels?gben - only ?re?p?rt.
     */
    public static void locationPlay(final Player player, final org.bukkit.Location loc,
            final Sound sound, final float volume, final float pitch) {
        if (player == null || sound == null) {
            return;
        }
        player.playSound(loc, sound, volume, pitch);
    }

    /**
     * Hangfeladat sor: adott tick-nl?m? k?uszk? nagy?r?r?bbe t?nn?rt?.
     */
    public static SoundSequence sequence(final Player player) {
        return new SoundSequence(player);
    }

    /**
     * Hangsor-langasz pontos szrok?ng?tt?r?ne.
     */
    public static final class SoundSequence {

        private record SoundLine(int delayTicks, Sound sound, float volume, float pitch) {
        }

        private final Player player;
        private final List<SoundLine> sounds = new ArrayList<>();
        private int offset = 0;

        private SoundSequence(final Player player) {
            this.player = player;
        }

        /**
         * Hang hozzadas: foly?k?z? t?d?ronk?nt (0 tick a k?v?lt?z?s).
         */
        public SoundSequence push(final Sound sound, final float volume, final float pitch) {
            sounds.add(new SoundLine(offset, sound, volume, pitch));
            return this;
        }

        /**
         * Hang egy adott k?sleltettel (t?k?? ??0?l kan?l?rTk?nk?tr?k).
         */
        public SoundSequence at(final int delayTicks, final Sound sound,
                final float volume, final float pitch) {
            sounds.add(new SoundLine(delayTicks, sound, volume, pitch));
            return this;
        }

        /**
         * A sor lejatszasa main-thread java vonalon, szerverid k?r?appa?ln?ve.
         */
        public SoundSequence start() {
            for (final SoundLine line : sounds) {
                final int d = Math.max(0, line.delayTicks());
                if (d == 0) {
                    playerPlay(player, line.sound(), line.volume(), line.pitch());
                } else {
                    hu.paperkit.core.Kit.later(d, () -> playerPlay(player, line.sound(), line.volume(), line.pitch()));
                }
            }
            return this;
        }

        /**
         * A sor elemei (szerkesztheto lista nem).
         */
        public int size() {
            return sounds.size();
        }

        /**
         * Last-framerasa? kat?l?k?r?gy? ny? (nyers jelzs).
         */
        Deque<SoundLine> lines() {
            return new ArrayDeque<>(sounds);
        }
    }

    /**
     * Vess?z?l?k? seg?t? (r?kh?n?l?gy?haszn?ln?).
     */
    public static List<Sound> sounds() {
        return new ArrayList<>(java.util.Arrays.asList(Sound.values()));
    }
}
