package hu.paperkit.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adatbazis-kapcsolati segito: SQLite (alap, fajl a plugin mappajaban)
 * es MySQL is tamogatott — a driver mar benne van a Paperben, igy nincs
 * uj dependency.
 *
 * <p>Felhasznalas:</p>
 *
 * <pre>{@code
 * Database db = Database.open(this, "adatok.db");      // SQLite
 * // Database db = Database.mysql("jdbc:mysql://127.0.0.1/adat", "user", "jelszo");
 *
 * db.table("kik", "uuid TEXT", "money REAL", "kills INTEGER");
 * int sorok = db.update("UPDATE kik SET money = ? WHERE uuid = ?", 150.0, uuid);
 * List<Map<String, Object>> lista = db.query(
 *     "SELECT * FROM kik WHERE money > ? ORDER BY money DESC LIMIT 10", 0);
 * }</pre>
 *
 * <p>Egy megosztott kapcsolat + szal-biztos hozzaferes; a nagy mennyisegu
 * levelel a fodott admin Thread-erkekbol async technikus legyen.</p>
 */
public final class DatabaseKit {

    private Connection connection;
    private final boolean mysql;
    private final Object lock = new Object();

    private DatabaseKit(final Connection connection, final boolean mysql) {
        this.connection = connection;
        this.mysql = mysql;
    }

    /**
     * SQLite adatbazis megnyitasa a plugin mappajaban.
     *
     * @param plugin a hivo plugin (adatmappa a db-nek)
     * @param fileName a fajl neve, pl. "adatok.db"
     * @return a kapcsolat-keze unus
     */
    public static DatabaseKit open(final org.bukkit.plugin.Plugin plugin, final String fileName) {
        final File file = new File(plugin.getDataFolder(), fileName);
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        return open("jdbc:sqlite:" + file.getAbsolutePath());
    }

    /**
     * Kapcsolat megnyitasa URL-bol (MySQLhez:
     * "jdbc:mysql://127.0.0.1:3306/adat" + user + jelszo).
     *
     * @param url a JDBC url
     * @return a kapcsolat-keze
     */
    public static DatabaseKit open(final String url) {
        return open(url, null, null);
    }

    /**
     * Kapcsolat megnyitasa user/jelszoval (a mysql-kesz loginokhoz).
     */
    public static DatabaseKit open(final String url, final String user, final String password) {
        final boolean isMysql = url.startsWith("jdbc:mysql");
        final Connection connection;
        try {
            if (isMysql) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = (user == null)
                        ? DriverManager.getConnection(url)
                        : DriverManager.getConnection(url, user, password);
            } else {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(url);
            }
        } catch (final Exception e) {
            throw new IllegalStateException("Adatbazis-kapcsolat sikertelen: " + url, e);
        }
        return new DatabaseKit(connection, isMysql);
    }

    /**
     * Tabla letrehozasa ha meg nincs (CREATE TABLE IF NOT EXISTS).
     *
     * @param name a tabla neve
     * @param columns oszlopot k parameterben, pl. "uuid TEXT", "money REAL"
     */
    public void table(final String name, final String... columns) {
        final StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(mysql ? "`" + name + "`" : name).append(" (");
        for (int i = 0; i < columns.length; i++) {
            sql.append(columns[i]);
            if (i < columns.length - 1) {
                sql.append(", ");
            }
        }
        sql.append(")");
        update(sql.toString());
    }

    /**
     * Frissito SQL lefuttatasa (UPDATE / INSERT / DELETE / DDL).
     *
     * @param sql az sql (a parameterek pointja biró "?")
     * @param params a parameter-ertfek
     * @return az erintett sorok szama
     */
    public int update(final String sql, final Object... params) {
        synchronized (lock) {
            try (final PreparedStatement ps = connection.prepareStatement(sql)) {
                bind(ps, params);
                return ps.executeUpdate();
            } catch (final Exception e) {
                throw new IllegalStateException("Update hiba: " + sql, e);
            }
        }
    }

    /**
     * Lekerdezes lefuttatasa: sor -> oszlopnev-ertek map-je lista.
     *
     * @param sql az sql (a parameterek "?")
     * @param params parameter-ertfek
     * @return sorok map-lista
     */
    public List<Map<String, Object>> query(final String sql, final Object... params) {
        synchronized (lock) {
            try (final PreparedStatement ps = connection.prepareStatement(sql)) {
                bind(ps, params);
                try (final ResultSet rs = ps.executeQuery()) {
                    final ResultSetMetaData meta = rs.getMetaData();
                    final int columnCount = meta.getColumnCount();
                    final List<Map<String, Object>> rows = new ArrayList<>();
                    while (rs.next()) {
                        final Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(meta.getColumnLabel(i), rs.getObject(i));
                        }
                        rows.add(row);
                    }
                    return rows;
                }
            } catch (final Exception e) {
                throw new IllegalStateException("Query hiba: " + sql, e);
            }
        }
    }

    /**
     * Egyert felSimt adott mezo ertek (vagy null).
     */
    public Object firstValue(final String sql, final String column, final Object... params) {
        final List<Map<String, Object>> rows = query(sql, params);
        if (rows.isEmpty()) {
            return null;
        }
        return rows.get(0).get(column);
    }

    /**
     * Kapcsolat zarasa (onDisable-ben).
     */
    public void close() {
        synchronized (lock) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (final Exception ignored) {
                // closed mar
            }
            connection = null;
        }
    }

    /**
     * @return nyitva van-e a kapcsolat
     */
    public boolean isOpen() {
        synchronized (lock) {
            try {
                return connection != null && !connection.isClosed();
            } catch (final Exception e) {
                return false;
            }
        }
    }

    private static void bind(final PreparedStatement ps, final Object... params) throws Exception {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
