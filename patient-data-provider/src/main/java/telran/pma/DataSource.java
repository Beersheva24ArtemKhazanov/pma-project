package telran.pma;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


import telran.pma.api.PatientData;
import telran.pma.logger.Logger;

public class DataSource {
    private static final String DEFAULT_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    private static final String AGE = "age";
    private static final String WEIGHT = "weight";
    private static final String CHILD_PUGH = "child_pugh";
    private static final String GFR = "gfr";
    private static final String PLT = "plt";
    private static final String WBC = "wbc";
    private static final String SAT = "sat";
    private static final String SODIUM = "sodium";
    private static final String SENSETIVITY = "sensetivity";
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

    public DataSource(String connectionStr, String username, String password, Logger logger) {
        DataSource.logger = logger;
        logger.log("info", "driver class name: " + driverClassName);
        try {
            con = DriverManager.getConnection(connectionStr, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public PatientData getPatientData(long patientId) {
        String query = "SELECT * FROM patients WHERE id = ?";
        try {
            pstmt = con.prepareStatement(query);
            pstmt.setLong(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            String[] contraindications = getContraindications(patientId);
            if (rs.next()) {
                return new PatientData(
                    patientId,
                    rs.getInt(AGE),
                    rs.getInt(WEIGHT),
                    rs.getString(CHILD_PUGH),
                    rs.getInt(GFR),
                    rs.getInt(PLT),
                    rs.getDouble(WBC),
                    rs.getInt(SAT),
                    rs.getInt(SODIUM),
                    rs.getString(SENSETIVITY),
                    contraindications);
            } else {
                throw new NoSuchElementException(String.format("Patient with id %d not found", patientId));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getContraindications(long patientId) {
        String query = "SELECT * FROM patients_contraindications WHERE patient_id = ?";
        try {
            pstmt = con.prepareStatement(query);
            pstmt.setLong(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            List<String> contraindications = new ArrayList<>();
            while (rs.next()) {
                contraindications.add(rs.getString("contraindication"));
            }
            return !contraindications.isEmpty() ? contraindications.toArray(new String[0]) : null;
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
