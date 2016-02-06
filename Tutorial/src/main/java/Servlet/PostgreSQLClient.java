package Servlet;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author JVO
 */
import Bean.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public final class PostgreSQLClient {

    public PostgreSQLClient(){
        try {
            createTable();
        } catch (Exception ex) {
            Logger.getLogger(PostgreSQLClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /**
     * Grab text from PostgreSQL
     *
     * @param username
     * @param pass
     * @return List of Strings of text from PostgreSQL
     * @throws Exception
     */
    public User getResults(String username, String pass) throws Exception {
        String sql = "SELECT * FROM Account WHERE username = '" + username + "' and password = '" + pass + "';";
        System.out.println(sql);
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            rs = statement.executeQuery();

            User u = new User();
            u.setCheck(0);
            if (rs.next()) {
                u.setCheck(1);
                u.setFirstname(rs.getString(3));
                u.setLastname(rs.getString(4));
                u.setUsername(rs.getString(1));
                u.setPass(rs.getString(2));
            }
            return u;

        } finally {
            if (rs != null) {
                rs.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * Insert text into PostgreSQL
     *
     * param posts List of Strings of text to insert
     *
     * @return number of rows affected
     * @throws Exception
     * @throws Exception
     */
    public int addPosts(List<String> posts) throws Exception {
        String sql = "INSERT INTO Account VALUES (?)";
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(sql);

            for (String s : posts) {
                statement.setString(1, s);
                statement.addBatch();
            }
            int[] rows = statement.executeBatch();
            connection.commit();

            return rows.length;
        } catch (SQLException e) {
            SQLException next = e.getNextException();

            if (next != null) {
                throw next;
            }

            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }

            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * Delete all rows from PostgreSQL
     *
     * @return number of rows affected
     * @throws Exception
     */
    public int deleteAll() throws Exception {
        String sql = "DELETE FROM Account WHERE TRUE";
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            return statement.executeUpdate();
        } finally {
            if (statement != null) {
                statement.close();
            }

            if (connection != null) {
                connection.close();
            }
        }
    }

    private static Connection getConnection() throws Exception {
        Map<String, String> env = System.getenv();

        if (env.containsKey("VCAP_SERVICES")) {
            // we are running on cloud foundry, let's grab the service details from vcap_services
            JSONParser parser = new JSONParser();
            JSONObject vcap = (JSONObject) parser.parse(env.get("VCAP_SERVICES"));
            JSONObject service = null;

            // We don't know exactly what the service is called, but it will contain "postgresql"
            for (Object key : vcap.keySet()) {
                String keyStr = (String) key;
                if (keyStr.toLowerCase().contains("postgresql")) {
                    service = (JSONObject) ((JSONArray) vcap.get(keyStr)).get(0);
                    break;
                }
            }

            if (service != null) {
                JSONObject creds = (JSONObject) service.get("credentials");
                String name = (String) creds.get("name");
                String host = (String) creds.get("host");
                Long port = (Long) creds.get("port");
                String user = (String) creds.get("user");
                String password = (String) creds.get("password");

                String url = "jdbc:postgresql://" + host + ":" + port + "/" + name;

                return DriverManager.getConnection(url, user, password);
            }
        }

        throw new Exception("No PostgreSQL service URL found. Make sure you have bound the correct services to your app.");
    }

    /**
     * Create the posts table if it doesn't already exist
     *
     * @throws Exception
     */
    public void createTable() throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS Account "
                + "(username varchar(45) NOT NULL PRIMARY KEY, "
                + "password varchar(45) NOT NULL, "
                + "firstname varchar(45), "
                + "lastname varchar(45))"
                + ";";
        String sql2 = "INSERT INTO Account "
                + "(username, password, firstname, lastname) "
                + "VALUES ('admin', 'password', 'Jarrette', 'Ong'), "
                + "('user1', 'password', 'Lance', 'Del Valle');";
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement = connection.prepareStatement(sql2);
            statement.executeUpdate();
        } catch (Exception e){
            System.out.println("Initialization already completed.");
        } finally {
            if (statement != null) {
                statement.close();
            }

            if (connection != null) {
                connection.close();
            }
        }
    }
    /*
    public void insertUser() throws Exception {
        String sql = "";
        Connection connection = null;
        PreparedStatement statement = null;
        deleteAll();
        
        try {
            connection = getConnection();
            statement = connection.prepareStatement("INSERT INTO Account (username, password, firstname, lastname) VALUES ('jarrette', 'ong', 'Jarrette', 'Ong'), ('lance', 'del valle', 'Lance', 'Del Valle');");
            statement.executeUpdate();
        } finally {
            if (statement != null) {
                statement.close();
            }

            if (connection != null) {
                connection.close();
            }
        }
    }
    */
    public User editUser(String firstname, String lastname, String username) throws Exception {
        String sql = "";
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement("UPDATE Account SET firstname= '" + firstname + "', lastname= '" + lastname + "' WHERE username = '" + username + "';");
            statement.executeUpdate();
            statement = connection.prepareStatement("SELECT * FROM Account WHERE username = '" + username + "';");
            ResultSet rs = statement.executeQuery();
            User u = new User();
            if (rs.next()) {
                u.setCheck(1);
                u.setFirstname(rs.getString(3));
                u.setLastname(rs.getString(4));
                u.setUsername(rs.getString(1));
                u.setPass(rs.getString(2));
            }
            return u;
        } finally {
            if (statement != null) {
                statement.close();
            }

            if (connection != null) {
                connection.close();
            }
        }
    }
}
