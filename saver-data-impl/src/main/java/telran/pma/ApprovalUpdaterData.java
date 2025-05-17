package telran.pma;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Map;

import telran.pma.logger.Logger;
import telran.pma.logger.LoggerStandard;

public class ApprovalUpdaterData extends ApprovalSaverData {
        private static final String DEFAULT_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    private static final String DEFAULT_USER_NAME = "postgres";
    private static final String DEFAULT_DB_CONNECTION_STRING = "jdbc:postgresql://postgres-pma.cul4ey8o8fp4.us-east-1.rds.amazonaws.com:5432/postgres";
    Map<String, String> env = System.getenv();
    String connectionStr = getConnectionString();
    String username = getUsername();
    String password = getPassword();
    Logger logger = new LoggerStandard("approval-updater-data");
    Connection con;
    PreparedStatement pstmt;
    static String driverClassName;
    static {
        driverClassName = getDriverClassName();
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver class not found: " + driverClassName, e);
        }
    }

    public ApprovalUpdaterData() {
        try {
            con = DriverManager.getConnection(connectionStr, username, password);
            pstmt = con.prepareStatement(
                    "UPDATE approvals SET type = ? WHERE id = ?");
        } catch (Exception e) {
            logger.log("severe", "error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveData(Map<String, Object> data) {
        try {
            pstmt.setString(1, data.get("type").toString());
            pstmt.setLong(2, Long.parseLong(data.get("id").toString()));
            pstmt.executeUpdate();
            logger.log("info", "Data saved successfully: " + data.toString());
        } catch (Exception e) {
            logger.log("severe", "error: " + e.getMessage());
            throw new RuntimeException(e);

        }
    }

    private static String getDriverClassName() {
        String driverClassName = System.getenv("DRIVER_CLASS_NAME");
        if (driverClassName == null) {
            driverClassName = DEFAULT_DRIVER_CLASS_NAME;
        }
        return driverClassName;
    }

    private String getPassword() {
        String password = env.get("DB_PASSWORD");
        if (password == null) {
            throw new RuntimeException("password must be specified in environment variable");
        }
        return password;
    }

    private String getUsername() {
        String username = env.getOrDefault("USERNAME", DEFAULT_USER_NAME);
        return username;
    }

    private String getConnectionString() {
        String connectionString = env.getOrDefault("DB_CONNECTION_STRING", DEFAULT_DB_CONNECTION_STRING);
        return connectionString;
    }
}
