import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeSet;
import java.util.Vector;

import org.json.JSONObject;
import org.json.JSONArray;

public class GetData {

    static String prefix = "project3.";

    // You must use the following variable as the JDBC connection
    Connection oracleConnection = null;

    // You must refer to the following variables for the corresponding 
    // tables in your database
    String userTableName = null;
    String friendsTableName = null;
    String cityTableName = null;
    String currentCityTableName = null;
    String hometownCityTableName = null;

    // DO NOT modify this constructor
    public GetData(String u, Connection c) {
        super();
        String dataType = u;
        oracleConnection = c;
        userTableName = prefix + dataType + "_USERS";
        friendsTableName = prefix + dataType + "_FRIENDS";
        cityTableName = prefix + dataType + "_CITIES";
        currentCityTableName = prefix + dataType + "_USER_CURRENT_CITIES";
        hometownCityTableName = prefix + dataType + "_USER_HOMETOWN_CITIES";
    }

    // TODO: Implement this function
    @SuppressWarnings("unchecked")
    public JSONArray toJSON() throws SQLException {

        // This is the data structure to store all users' information
        JSONArray users_info = new JSONArray();
        
        try (Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            
            String query = "SELECT user_id, first_name, last_name, gender, year_of_birth, month_of_birth, day_of_birth, gender FROM " + userTableName;
            ResultSet rst = stmt.executeQuery(query);

            while(rst.next()){

                JSONObject newUser = new JSONObject();
                int user_id = rst.getInt(1);
                String first_name = rst.getString(2);
                String last_name = rst.getString(3);
                String gender = rst.getString(4);
                int YOB = rst.getInt(5);
                int MOB = rst.getInt(6);
                int DOB = rst.getInt(7);

                JSONArray friends = new JSONArray();
                JSONObject current = new JSONObject();
                JSONObject hometown = new JSONObject();

                newUser.put("user_id", user_id);
                newUser.put("first_name", first_name);
                newUser.put("last_name", last_name);
                newUser.put("gender", gender);
                newUser.put("YOB", YOB);
                newUser.put("MOB", MOB);
                newUser.put("DOB", DOB);
                try (Statement stmt1 = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)){

                    String queryFriends = "SELECT user2_id FROM " + friendsTableName +
                    " WHERE user1_id = " + user_id + "AND user2_id > user1_id"; 

                    ResultSet friendRST = stmt1.executeQuery(queryFriends);
                    while(friendRST.next()){
                        friends.put(friendRST.getInt(1));
                    }
                    newUser.put("friends", friends);
                    
                }
                catch(SQLException e) {
                    System.err.println(e.getMessage());
                }

                try (Statement stmt2 = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)){
                    String currentQuery = "SELECT c.CITY_NAME, c.STATE_NAME, c.COUNTRY_NAME FROM " + cityTableName +
                    " c JOIN " + currentCityTableName + " cc on cc.CURRENT_CITY_ID = c.CITY_ID " + 
                    " WHERE cc.USER_ID = " + user_id;
                    ResultSet currentRST = stmt2.executeQuery(currentQuery);

                    while(currentRST.next()){
                        String city = currentRST.getString(1);
                        String state = currentRST.getString(2);
                        String country = currentRST.getString(3);
                        current.put("country", country);
                        current.put("city", city);
                        current.put("state", state);
                    }
                    newUser.put("current", current);

                }
                catch(SQLException e) {
                    System.err.println(e.getMessage());
                }


                try (Statement stmt3 = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)){

                    String hometownQuery = "SELECT c.CITY_NAME, c.STATE_NAME, c.COUNTRY_NAME FROM " + cityTableName +
                    " c JOIN " + hometownCityTableName + " ht on ht.HOMETOWN_CITY_ID = c.CITY_ID " + 
                    " WHERE ht.USER_ID = " + user_id;
                    ResultSet hometownRST = stmt3.executeQuery(hometownQuery);

                    while(hometownRST.next()){
                        String city = hometownRST.getString(1);
                        String state = hometownRST.getString(2);
                        String country = hometownRST.getString(3);
                        hometown.put("country", country);
                        hometown.put("city", city);
                        hometown.put("state", state);
                    }
                    newUser.put("hometown", hometown);

                }
                catch(SQLException e) {
                    System.err.println(e.getMessage());
                }
                users_info.put(newUser);
            }
            
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return users_info;
    }

    // This outputs to a file "output.json"
    // DO NOT MODIFY this function
    public void writeJSON(JSONArray users_info) {
        try {
            FileWriter file = new FileWriter(System.getProperty("user.dir") + "/output.json");
            file.write(users_info.toString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
