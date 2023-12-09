package iot.unipi.it.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MysqlManager {
    private final static String databaseIP = "localhost";
    private final static String databasePort = "3306";
    private final static String databaseUsername = "root";
    private final static String databasePassword = "PASSWORD";
    private final static String databaseName = "smart_agricolture";

    private static Connection makeJDBCConnection() {
        Connection databaseConnection = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");// checks if the Driver class exists (correctly available)
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            // DriverManager: The basic service for managing a set of JDBC drivers.
            databaseConnection = DriverManager.getConnection(
                    "jdbc:mysql://" + databaseIP + ":" + databasePort +
                            "/" + databaseName + "?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=CET",
                    databaseUsername,
                    databasePassword);
            // The Driver Manager provides the connection specified in the parameter string
            if (databaseConnection == null) {
                System.err.println("Connection to Db failed");
            }
        } catch (SQLException e) {
            System.err.println("MySQL Connection Failed!\n");
            e.printStackTrace();
        }
        return databaseConnection;
    }

    public static void insertTemperatureAndUmidity(String nodeId, double temperature, double umidity) {
        String insertQueryStatementTemperature = "INSERT INTO temperature (nodeId, value) VALUES (?, ?)";
        String insertQueryStatementUmidity = "INSERT INTO umidity (nodeId,value) VALUES (?, ?)";
        
        try (Connection IrrigationConnection = makeJDBCConnection();
                PreparedStatement prepareStatementTemp = IrrigationConnection
                        .prepareStatement(insertQueryStatementTemperature);
                PreparedStatement prepareStatementUmid = IrrigationConnection
                        .prepareStatement(insertQueryStatementUmidity);) {
            prepareStatementTemp.setString(1, nodeId);
            prepareStatementTemp.setDouble(2, temperature);
            prepareStatementTemp.executeUpdate();
            prepareStatementUmid.setString(1, nodeId);
            prepareStatementUmid.setDouble(2, umidity);
            prepareStatementUmid.executeUpdate();
        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
    }

    public static void insertSoilMoistureValue(String nodeId, double soilValue) {
        String insertQueryStatement = "INSERT INTO soilUmidity (nodeId, value) VALUES (?, ?);";

        try (Connection IrrigationConnection = makeJDBCConnection();
                PreparedStatement prepareStatement = IrrigationConnection.prepareStatement(insertQueryStatement);) {
            prepareStatement.setString(1, nodeId);
            prepareStatement.setDouble(2, soilValue);
            prepareStatement.executeUpdate();

        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
    }

}