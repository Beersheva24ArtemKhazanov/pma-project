package telran.pma.dataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

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

    public ArrayList<HashMap<String, Object>> getApprovals(String type) {
        String query = "SELECT * FROM approvals WHERE is_approved = false AND type = ?";
        ArrayList<HashMap<String, Object>> approvals = new ArrayList<>();
        try {
            pstmt = con.prepareStatement(query);
            pstmt.setString(1, type);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                HashMap<String, Object> approvalMap = new HashMap<>();
                approvalMap.put("id", rs.getLong(ID));
                approvalMap.put("patientCallId", rs.getLong(PATIENT_CALL_ID));
                approvalMap.put("type", rs.getString(TYPE));
                approvalMap.put("isApproved", rs.getBoolean(IS_APPROVED));
                approvalMap.put("timestamp", rs.getLong(TIMESTAMP));
                approvals.add(approvalMap);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return approvals;
    }

    private static String getDriverClassName() {
        String driverClassName = System.getenv("DRIVER_CLASS_NAME");
        if (driverClassName == null) {
            driverClassName = DEFAULT_DRIVER_CLASS_NAME;
        }
        return driverClassName;
    }

}
