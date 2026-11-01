package hu.paperkit.config;

import java.util.Locale;
import java.util.Map;

/**
 * Egyszeru matematikai kifejezes-e evaluator, a klasszikus
 * plugin-config minta alapjan: a szerverekpen konfiguralhato
 * szintszamitas, arlepcso, jutalom-kurva stb.
 *
 * <p>Tamogatott nyelvtan:</p>
 * <ul>
 *   <li>szamok (egesz es tizedes: {@code 2, 3.5})</li>
 *   <li>valtozok: {@code %level%}, {@code %prestige%} stb.</li>
 *   <li>muveletek: {@code + - * / ^}, zarojelek, egyoperandusu minusz</li>
 * </ul>
 *
 * <p>Config-minta (AxHoes-fele "formula/manual" stilus):</p>
 *
 * <pre>{@code
 * xp-calculation:
 *   type: "formula"              # vagy "manual"
 *   formula: "%level% ^ 2 * 1000"
 *   manual:                      # ha type: manual
 *     1: "100"
 *     2: "500"
 * }</pre>
 *
 * <pre>{@code
 * Formula f = Formula.compile("%level% ^ 2 * 1000");
 * int xp = f.evalInt(Map.of("level", 5)); // 25000
 *
 * // configbol (a beallitas a path-hoz tartozo szekcio):
 * int level = Formula.calculate(config, "xp-calculation", "level", 3);
 * }</pre>
 */
public final class Formula {

    /** A gyoker kifejezes-csomopont (null, ha ures). */
    private final Node root;

    private Formula(final Node root) {
        this.root = root;
    }

    /**
     * Kifejezes "leforditasa" egyszer futtatando AST-ra.
     *
     * @param expression a kifejezes, pl. {@code "%level% ^ 2 * 1000"}
     * @return a Formula peldany
     * @throws IllegalArgumentException szintaktikai hiba eseten
     */
    public static Formula compile(final String expression) {
        if (expression == null || expression.isBlank()) {
            return new Formula(new Num(0));
        }
        return new Formula(new Parser(expression).parse());
    }

    /**
     * Kiertekel adott valtozokkal.
     *
     * @param vars valtozok neve -> ertek (nev a % jelek NELKUL: "level")
     * @return az eredmeny
     */
    public double eval(final Map<String, Number> vars) {
        return root.value(vars == null ? Map.of() : vars);
    }

    /**
     * Kiertekel threshold egessz kerekitesi szaballyal.
     *
     * @param vars valtozok
     * @return az eredmeny egeszkent
     */
    public int evalInt(final Map<String, Number> vars) {
        final double value = eval(vars);
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0;
        }
        return (int) Math.floor(value + 0.5);
    }

    /**
     * Kiertekel egy valtozoval (a leggyakoribb eset: %level%).
     *
     * @param varName a valtozo neve (pl. "level")
     * @param value   erteke
     * @return az eredmeny
     */
    public double eval(final String varName, final Number value) {
        return eval(Map.of(varName, value));
    }

    /**
     * Kiertekel egy valtozoval, egesz kerekitessel.
     */
    public int evalInt(final String varName, final Number value) {
        return evalInt(Map.of(varName, value));
    }

    /**
     * "formula/manual" config szekcio feloldasa.
     *
     * <p>Ha {@code type: formula}, a {@code formula} kulcs kifejezese
     * fut le a megadott valtozoval; ha {@code type: manual}, akkor a
     * {@code manual.<value>} kulcs erteke (pl. manual.5 -> 123) es
     * fallback: ha nincs kozel level, a kifejezes lesz hasznalva.</p>
     *
     * @param config  a betoltott ConfigFile
     * @param path    a szekcio utvonala, pl. {@code "xp-calculation"}
     * @param varName a valtozo neve a kifejezesben (pl. "level")
     * @param varValue a valtozo erteke (pl. a jatekos szintje)
     * @return a szamolt ertek egeszkent
     */
    public static int calculate(final ConfigFile config, final String path,
            final String varName, final int varValue) {
        final String type = config.getString(path + ".type", "formula").toLowerCase(Locale.ROOT);
        if ("manual".equals(type)) {
            final String raw = config.getString(path + ".manual." + varValue, null);
            if (raw != null) {
                try {
                    return (int) Math.round(Double.parseDouble(raw.trim().replace(',', '.')));
                } catch (final NumberFormatException e) {
                    return 0;
                }
            }
        }
        final String expression = config.getString(path + ".formula", null);
        if (expression == null) {
            return varValue;
        }
        try {
            return compile(expression).evalInt(varName, varValue);
        } catch (final IllegalArgumentException e) {
            return 0;
        }
    }

    // ---------- belso AST ----------

    private interface Node {
        double value(Map<String, Number> vars);
    }

    private record Num(double value) implements Node {
        @Override
        public double value(final Map<String, Number> vars) {
            return value;
        }
    }

    private record Var(String name) implements Node {
        @Override
        public double value(final Map<String, Number> vars) {
            final Number number = vars.get(name);
            // hianyzo valtozo = 0 (a config Center hibaja nem gyutja le a szervert)
            return number == null ? 0.0 : number.doubleValue();
        }
    }

    private record Bin(char op, Node left, Node right) implements Node {
        @Override
        public double value(final Map<String, Number> vars) {
            final double lv = left.value(vars);
            final double rv = right.value(vars);
            return switch (op) {
                case '+' -> lv + rv;
                case '-' -> lv - rv;
                case '*' -> lv * rv;
                case '/' -> lv / rv;
                case '^' -> Math.pow(lv, rv);
                default -> throw new IllegalStateException("Ismeretlen operator: " + op);
            };
        }
    }

    private record Neg(Node inner) implements Node {
        @Override
        public double value(final Map<String, Number> vars) {
            return -inner.value(vars);
        }
    }

    // ---------- tokenez + parser ----------

    private static final class Parser {

        private final String src;
        private int pos;

        private Parser(final String src) {
            this.src = src;
        }

        Node parse() {
            final Node result = expression();
            skipWhitespace();
            if (pos < src.length()) {
                throw new IllegalArgumentException(
                        "Hibas matematikai kifejezes (a(z) " + pos + ". karaktertol): " + src);
            }
            return result;
        }

        private Node expression() {
            Node node = term();
            while (true) {
                final char op = peekOp('+', '-');
                if (op == 0) {
                    return node;
                }
                next();
                node = new Bin(op, node, term());
            }
        }

        private Node term() {
            Node node = power();
            while (true) {
                final char op = peekOp('*', '/');
                if (op == 0) {
                    return node;
                }
                next();
                node = new Bin(op, node, power());
            }
        }

        private Node power() {
            final Node base = unary();
            final char op = peekOp('^');
            if (op == 0) {
                return base;
            }
            next();
            // jobbra asszociativ: 2^3^2 == 512
            return new Bin(op, base, power());
        }

        private Node unary() {
            skipWhitespace();
            if (peek() == '-') {
                next();
                return new Neg(unary());
            }
            if (peek() == '+') {
                next();
                return unary();
            }
            return primary();
        }

        private Node primary() {
            skipWhitespace();
            final char c = peek();
            if (c == '(') {
                next();
                final Node inner = expression();
                skipWhitespace();
                if (peek() != ')') {
                    throw new IllegalArgumentException("Hianyzo zarojel, kifejezes: " + src);
                }
                next();
                return inner;
            }
            if (c == '%') {
                return variable();
            }
            return number();
        }

        private Node variable() {
            next(); // % elnyelo
            final int start = pos;
            while (pos < src.length() && src.charAt(pos) != '%') {
                pos++;
            }
            if (pos >= src.length()) {
                throw new IllegalArgumentException("Lezaratlan valtozo (%nev%), kifejezes: " + src);
            }
            final String name = src.substring(start, pos).trim();
            next(); // lezar % elnyelo
            if (name.isBlank()) {
                throw new IllegalArgumentException("Ures valtozonev, kifejezes: " + src);
            }
            return new Var(name.toLowerCase(Locale.ROOT));
        }

        private Node number() {
            skipWhitespace();
            final int start = pos;
            while (pos < src.length()
                    && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) {
                pos++;
            }
            final String token = src.substring(start, pos);
            if (token.isEmpty()) {
                throw new IllegalArgumentException(
                        "Szamot vartam a(z) " + start + ". pozicion, kifejezes: " + src);
            }
            try {
                return new Num(Double.parseDouble(token));
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException("Hibas szam: '" + token + "' (" + src + ")");
            }
        }

        private char peek() {
            return pos < src.length() ? src.charAt(pos) : '\0';
        }

        private char peekOp(final char... ops) {
            skipWhitespace();
            final char c = peek();
            for (final char op : ops) {
                if (c == op) {
                    return op;
                }
            }
            return 0;
        }

        private void next() {
            pos++;
        }

        private void skipWhitespace() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
                pos++;
            }
        }
    }
}
