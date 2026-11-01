package hu.paperkit.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * A Text legacy->MiniMessage atalakitasanak tesztjei.
 * (Ezek nem hasznaljak a szervert, csak konyvtart.)
 */
public final class TextTest {

    @Test
    public void legacyColorCodesTranslate() {
        assertEquals("<aqua>Hello", Text.translate("&bHello"));
        assertEquals("<green>B<aqua>old<reset>!", Text.translate("&aB&bold<reset>!"));
    }

    @Test
    public void sectionSignTranslated() {
        assertEquals("<red>t", Text.translate("\u00a7ct"));
    }

    @Test
    public void hexHashTranslated() {
        assertEquals("<#FF8800>x", Text.translate("&#FF8800x"));
    }

    @Test
    public void bukkitHexFormTranslated() {
        assertEquals("<#FF8800>x", Text.translate("&x&F&F&8&8&0&0x"));
    }

    @Test
    public void unknownAmpersandKept() {
        assertEquals("a & z", Text.translate("a & z"));
    }

    @Test
    public void parseProducesComponent() {
        assertTrue(Text.parse("<green>ok") != null);
    }

    @Test
    public void placeholdersRegistered() {
        final net.kyori.adventure.text.Component c = Text.parse("Szia <name>!", Text.res("name", "Pete"));
        assertEquals("Szia Pete!",
                net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(c));
    }
}
