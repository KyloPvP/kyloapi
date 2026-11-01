package hu.paperkit.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * A Formula kifejezes-evaluator szervermentes unit tesztjei.
 */
class FormulaTest {

    @Test
    void egyszeruMuveletek() {
        assertEquals(50.0, Formula.compile("2 + 3 * (4 ^ 2)").eval(Map.of()));
        assertEquals(2.5, Formula.compile("10 / 4").eval(Map.of()));
        assertEquals(-1.0, Formula.compile("3 - 4").eval(Map.of()));
    }

    @Test
    void hatvanyJobbraAsszociativ() {
        assertEquals(512.0, Formula.compile("2 ^ 3 ^ 2").eval(Map.of()));
    }

    @Test
    void valtozoHelyettesites() {
        final Formula formula = Formula.compile("%level% ^ 2 * 1000");
        assertEquals(25000.0, formula.eval(Map.of("level", 5)));
        assertEquals(9000.0, formula.eval(Map.of("level", 3)));
    }

    @Test
    void valtozokEsZarojelek() {
        final Formula formula = Formula.compile("(%level% + 1) * (%level% + 1)");
        assertEquals(16.0, formula.eval(Map.of("level", 3)));
    }

    @Test
    void hianyzoValtozoNulla() {
        assertEquals(0.0, Formula.compile("%level% * 5").eval(Map.of()));
    }

    @Test
    void egyOperandususMinusz() {
        assertEquals(-5.0, Formula.compile("-%level% + 2").eval(Map.of("level", 7)));
    }

    @Test
    void egeszKerekit() {
        final Formula formula = Formula.compile("%a% / %b%");
        assertEquals(3, formula.evalInt(Map.of("a", 10, "b", 3)));
    }

    @Test
    void tizedesSzamok() {
        assertEquals(0.6, Formula.compile("%level% * 0.2").eval(Map.of("level", 3)), 1e-9);
    }

    @Test
    void hibasKifejezesDob() {
        assertThrows(IllegalArgumentException.class, () -> Formula.compile("2 + * 3"));
        assertThrows(IllegalArgumentException.class, () -> Formula.compile("(2 + 3"));
        assertThrows(IllegalArgumentException.class, () -> Formula.compile("%nev% + "));
    }
}
