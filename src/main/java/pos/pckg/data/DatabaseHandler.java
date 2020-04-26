package pos.pckg.data;

import java.sql.*;

public class DatabaseHandler {
    private Connection dbConnection = null;
    private Statement dbStatement = null;
    private ResultSet dbResultSet = null;
    private PreparedStatement dbPreparedStatement = null;
    private String dbUrl = null;
    private String dbUname = null;
    private String dbPass = null;

    public DatabaseHandler(String dbUrl, String dbUname, String dbPass) {
        this.dbUrl = dbUrl;
        this.dbUname = dbUname;
        this.dbPass = dbPass;
        startConnection();
    }

    /**
     * The purpose of this method is to start a thread for the Database Connection
     */
    public void startConnection(){
        try {
            dbConnection = DriverManager.getConnection(this.dbUrl,this.dbUname,this.dbPass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Used to execute a Data Definition Language such as 'Select' Statement
     * @param sql - DDL
     * @return - The row and column result of the statement
     */
    public ResultSet execQuery(String sql){
        try {
            dbStatement = dbConnection.createStatement();
            dbResultSet = dbStatement.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dbResultSet;
    }

    /**
     * Used to execute a Data Manipulation Language such as 'Update','Insert' etc. Statement
     * @param sql - DML
     * @return - affected rows
     */
    public int execUpdate(String sql){
        int returnedRow = 0;
        try {
            dbStatement = dbConnection.createStatement();
            returnedRow = dbStatement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnedRow;

    }

    /**
     * An important Database method for closing the connection to free some resources
     */
    public void closeConnection(){
        try {
            if (!dbConnection.isClosed())
                dbConnection.close();
            if (!dbStatement.isClosed())
                dbStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}