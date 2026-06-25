import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class FileStore {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/foodie_palace?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    public void ensureDefaults() throws IOException {
        try (Connection conn = openConnection()) {
            validateConnection(conn);
            ensureSchema(conn);
            ensureDefaultUsers(conn);
            ensureDefaultMenuItems(conn);
        } catch (SQLException ex) {
            throw new IOException("Failed to initialize database: " + ex.getMessage(), ex);
        }
    }

    public UserList loadUsers() throws IOException {
        UserList users = new UserList(200);
        try (Connection conn = openConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT username, password, role FROM `users`"
             );
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(new User(rs.getString("username"), rs.getString("password"), rs.getString("role")));
            }
        } catch (SQLException ex) {
            throw new IOException("Failed to load users from database: " + ex.getMessage(), ex);
        }
        return users;
    }

    public MenuItemList loadMenuItems() throws IOException {
        MenuItemList items = new MenuItemList(200);
        try (Connection conn = openConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT name, price, category FROM `menu_items`"
             );
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                items.add(new MenuItem(rs.getString("name"), rs.getDouble("price"), rs.getString("category")));
            }
        } catch (SQLException ex) {
            throw new IOException("Failed to load menu items from database: " + ex.getMessage(), ex);
        }
        return items;
    }

    public void saveUsers(UserList users) throws IOException {
        Connection conn = null;
        try {
            conn = openConnection();
            conn.setAutoCommit(false);
            try (Statement deleteStmt = conn.createStatement()) {
                deleteStmt.executeUpdate("DELETE FROM `users`");
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO `users` (username, password, role) VALUES (?, ?, ?)"
            )) {
                int index = 0;
                while (index < users.size()) {
                    User user = users.get(index);
                    stmt.setString(1, user.getUsername());
                    stmt.setString(2, user.getPassword());
                    stmt.setString(3, user.getRole());
                    stmt.addBatch();
                    index += 1;
                }
                stmt.executeBatch();
            }
            conn.commit();
        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                    // Ignore rollback failures.
                }
            }
            throw new IOException("Failed to save users to database: " + ex.getMessage(), ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                    // Ignore close failures.
                }
            }
        }
    }

    public void saveMenuItems(MenuItemList items) throws IOException {
        Connection conn = null;
        try {
            conn = openConnection();
            conn.setAutoCommit(false);
            try (Statement deleteStmt = conn.createStatement()) {
                deleteStmt.executeUpdate("DELETE FROM `menu_items`");
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO `menu_items` (name, price, category) VALUES (?, ?, ?)"
            )) {
                int index = 0;
                while (index < items.size()) {
                    MenuItem item = items.get(index);
                    stmt.setString(1, item.getName());
                    stmt.setDouble(2, item.getPrice());
                    stmt.setString(3, item.getCategory());
                    stmt.addBatch();
                    index += 1;
                }
                stmt.executeBatch();
            }
            conn.commit();
        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                    // Ignore rollback failures.
                }
            }
            throw new IOException("Failed to save menu items to database: " + ex.getMessage(), ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                    // Ignore close failures.
                }
            }
        }
    }

    private Connection openConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("MySQL JDBC driver not found. Check the classpath.", ex);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void ensureSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `users` ("
                    + "username VARCHAR(64) PRIMARY KEY, "
                    + "password VARCHAR(128) NOT NULL, "
                    + "role VARCHAR(32) NOT NULL"
                + ")"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `menu_items` ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(128) NOT NULL, "
                    + "price DECIMAL(10,2) NOT NULL, "
                    + "category VARCHAR(64) NOT NULL"
                + ")"
            );
        }
        dropMenuItemNameUniqueIfExists(conn);
    }

    private void validateConnection(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            if (!rs.next()) {
                throw new SQLException("Database connectivity check failed.");
            }
        }
        System.out.println("DB connection OK.");
    }

    private void dropMenuItemNameUniqueIfExists(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM information_schema.statistics "
            + "WHERE table_schema = DATABASE() AND table_name = 'menu_items' "
            + "AND index_name = 'name' AND non_unique = 0";
        try (PreparedStatement stmt = conn.prepareStatement(checkSql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                try (Statement dropStmt = conn.createStatement()) {
                    dropStmt.executeUpdate("DROP INDEX `name` ON `menu_items`");
                }
            }
        }
    }

    private boolean isTableEmpty(Connection conn, String table) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM `" + table + "`")) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
            return true;
        }
    }

    private void ensureDefaultUsers(Connection conn) throws SQLException {
        if (!isTableEmpty(conn, "users")) {
            return;
        }
        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO `users` (username, password, role) VALUES (?, ?, ?)"
        )) {
            stmt.setString(1, "admin");
            stmt.setString(2, "admin");
            stmt.setString(3, "ADMIN");
            stmt.executeUpdate();
        }
    }

    private void ensureDefaultMenuItems(Connection conn) throws SQLException {
        if (!isTableEmpty(conn, "menu_items")) {
            return;
        }
        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO `menu_items` (name, price, category) VALUES (?, ?, ?)"
        )) {
            stmt.setString(1, "White Bread Loaf");
            stmt.setDouble(2, 3.25);
            stmt.setString(3, "Breads");
            stmt.addBatch();

            stmt.setString(1, "Whole Wheat Loaf");
            stmt.setDouble(2, 3.50);
            stmt.setString(3, "Breads");
            stmt.addBatch();

            stmt.setString(1, "Banana Bread");
            stmt.setDouble(2, 4.00);
            stmt.setString(3, "Breads");
            stmt.addBatch();

            stmt.setString(1, "Sourdough Loaf");
            stmt.setDouble(2, 4.25);
            stmt.setString(3, "Breads");
            stmt.addBatch();

            stmt.setString(1, "Sponge Cake Slice");
            stmt.setDouble(2, 3.75);
            stmt.setString(3, "Cakes");
            stmt.addBatch();

            stmt.setString(1, "Chocolate Cake Slice");
            stmt.setDouble(2, 4.25);
            stmt.setString(3, "Cakes");
            stmt.addBatch();

            stmt.setString(1, "Cheesecake Slice");
            stmt.setDouble(2, 4.50);
            stmt.setString(3, "Cakes");
            stmt.addBatch();

            stmt.setString(1, "Custom Birthday Cake");
            stmt.setDouble(2, 28.00);
            stmt.setString(3, "Cakes");
            stmt.addBatch();

            stmt.setString(1, "Vanilla Cupcake");
            stmt.setDouble(2, 2.50);
            stmt.setString(3, "Cupcakes");
            stmt.addBatch();

            stmt.setString(1, "Chocolate Cupcake");
            stmt.setDouble(2, 2.75);
            stmt.setString(3, "Cupcakes");
            stmt.addBatch();

            stmt.setString(1, "Red Velvet Cupcake");
            stmt.setDouble(2, 2.95);
            stmt.setString(3, "Cupcakes");
            stmt.addBatch();

            stmt.setString(1, "Seasonal Special Cupcake");
            stmt.setDouble(2, 3.15);
            stmt.setString(3, "Cupcakes");
            stmt.addBatch();

            stmt.setString(1, "Chocolate Chip Cookie");
            stmt.setDouble(2, 1.75);
            stmt.setString(3, "Cookies");
            stmt.addBatch();

            stmt.setString(1, "Sugar Cookie");
            stmt.setDouble(2, 1.50);
            stmt.setString(3, "Cookies");
            stmt.addBatch();

            stmt.setString(1, "Oatmeal Cookie");
            stmt.setDouble(2, 1.65);
            stmt.setString(3, "Cookies");
            stmt.addBatch();

            stmt.setString(1, "Decorated Holiday Cookie");
            stmt.setDouble(2, 2.25);
            stmt.setString(3, "Cookies");
            stmt.addBatch();

            stmt.setString(1, "Butter Croissant");
            stmt.setDouble(2, 3.00);
            stmt.setString(3, "Pastries");
            stmt.addBatch();

            stmt.setString(1, "Puff Pastry Turnover");
            stmt.setDouble(2, 3.50);
            stmt.setString(3, "Pastries");
            stmt.addBatch();

            stmt.setString(1, "Fruit Danish");
            stmt.setDouble(2, 3.75);
            stmt.setString(3, "Pastries");
            stmt.addBatch();

            stmt.setString(1, "Cinnamon Roll");
            stmt.setDouble(2, 3.25);
            stmt.setString(3, "Pastries");
            stmt.addBatch();

            stmt.setString(1, "Apple Pie Slice");
            stmt.setDouble(2, 3.95);
            stmt.setString(3, "Pies & Tarts");
            stmt.addBatch();

            stmt.setString(1, "Lemon Tart");
            stmt.setDouble(2, 4.10);
            stmt.setString(3, "Pies & Tarts");
            stmt.addBatch();

            stmt.setString(1, "Fruit Tart");
            stmt.setDouble(2, 4.35);
            stmt.setString(3, "Pies & Tarts");
            stmt.addBatch();

            stmt.setString(1, "Pecan Pie Slice");
            stmt.setDouble(2, 4.45);
            stmt.setString(3, "Pies & Tarts");
            stmt.addBatch();

            stmt.setString(1, "Fudgy Brownie");
            stmt.setDouble(2, 2.95);
            stmt.setString(3, "Brownies & Bars");
            stmt.addBatch();

            stmt.setString(1, "Blondie Bar");
            stmt.setDouble(2, 2.85);
            stmt.setString(3, "Brownies & Bars");
            stmt.addBatch();

            stmt.setString(1, "Layered Dessert Bar");
            stmt.setDouble(2, 3.15);
            stmt.setString(3, "Brownies & Bars");
            stmt.addBatch();

            stmt.executeBatch();
        }
    }
}
