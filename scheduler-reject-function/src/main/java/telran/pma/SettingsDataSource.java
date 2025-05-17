package telran.pma;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import telran.pma.api.SettingsData;
import telran.pma.logger.Logger;

public class SettingsDataSource {
    private static final String DEFAULT_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    private static final String INTERVAL_FOR_DOCTOR = "interval_for_doctor";
    private static final String INTERVAL_FOR_NURSE = "interval_for_nurse";
    PreparedStatement pstmt;
    static Logger logger;
    static String driverClassName;
    static {
        driverClassName = getDriverClassName();
        try {
            Class.forName(driverClassName);
        } catch (Exception e) {
            throw new RuntimeException("Driver class not found: " + driverClassName, e);
        }
    }
    Connection con;

    public SettingsDataSource(String connectionStr, String username, String password, Logger logger) {
        SettingsDataSource.logger = logger;
        logger.log("info", "driver class name: " + driverClassName);
        try {
            con = DriverManager.getConnection(connectionStr, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public SettingsData getSettings() {
        String query = "SELECT * from settings";
        try {
            pstmt = con.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new SettingsData(
                        rs.getLong(INTERVAL_FOR_DOCTOR),
                        rs.getLong(INTERVAL_FOR_NURSE));
            } else {
                throw new NoSuchElementException("Settings on found");
            }
        } catch (SQLException e) {
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
}
