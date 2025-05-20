package telran.pma.dataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.NoSuchElementException;

import telran.pma.logger.Logger;

public class EmployeesDataSource {
    private static final String DEFAULT_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String ROLE = "role";
    private static final String EMAIL = "email";
    PreparedStatement pstmt;
    static Logger logger;
    static String driverClassName;
    static {
        driverClassName = getDriverClassName();
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver class not found: " + driverClassName, e);
        }
    }
    Connection con;

    public EmployeesDataSource(String connectionStr, String username, String password, Logger logger) {
        EmployeesDataSource.logger = logger;
        logger.log("info", "driver class name: " + driverClassName);
        try {
            con = DriverManager.getConnection(connectionStr, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, Object> getEmployee(String role) {
        String query = "SELECT * from employees where role = ?";
        HashMap<String, Object> empl = new HashMap<>();
        try {
            pstmt = con.prepareStatement(query);
            pstmt.setString(1, role);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                empl.put("id", rs.getLong(ID));
                empl.put("name", rs.getString(NAME));
                empl.put("role", rs.getString(ROLE));
                empl.put("email", rs.getString(EMAIL));
            } else {
                throw new NoSuchElementException("Settings on found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return empl;
    }

    private static String getDriverClassName() {
        String driverClassName = System.getenv("DRIVER_CLASS_NAME");
        if (driverClassName == null) {
            driverClassName = DEFAULT_DRIVER_CLASS_NAME;
        }
        return driverClassName;
    }
}
