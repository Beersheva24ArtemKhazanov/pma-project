package telran.pma;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import telran.pma.logger.Logger;

public class ApprovalsDataSource {
    private static final String DEFAULT_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    private static final String ID = "id";
    private static final String PATIENT_CALL_ID = "patient_call_id";
    private static final String TYPE = "type";
    private static final String IS_APPROVED = "is_approved";
    private static final String TIMESTAMP = "timestamp";
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

    public ApprovalsDataSource(String connectionStr, String username, String password, Logger logger) {
        ApprovalsDataSource.logger = logger;
        logger.log("info", "driver class name: " + driverClassName);
        try {
            con = DriverManager.getConnection(connectionStr, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object>[] getApprovals() {
        String query = "SELECT * FROM approvals WHERE is_approved = false AND type = 'toDoctor'";
        ArrayList<Map<String, Object>> approvals = new ArrayList<>();
        try {
            pstmt = con.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                approvals.add(Map.of(
                    "id", rs.getLong(ID),
                    "patientCallId", rs.getLong(PATIENT_CALL_ID),
                    "type", rs.getString(TYPE),
                    "isApproved", rs.getBoolean(IS_APPROVED),
                    "timestamp", rs.getLong(TIMESTAMP)
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return approvals.toArray(new Map[0]);
    }

    private static String getDriverClassName() {
        String driverClassName = System.getenv("DRIVER_CLASS_NAME");
        if (driverClassName == null) {
            driverClassName = DEFAULT_DRIVER_CLASS_NAME;
        }
        return driverClassName;
    }

}
