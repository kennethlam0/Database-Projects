package project2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;

/*
    The StudentFakebookOracle class is derived from the FakebookOracle class and implements
    the abstract query functions that investigate the database provided via the <connection>
    parameter of the constructor to discover specific information.
*/
public final class StudentFakebookOracle extends FakebookOracle {
    // [Constructor]
    // REQUIRES: <connection> is a valid JDBC connection
    public StudentFakebookOracle(Connection connection) {
        oracle = connection;
    }

    @Override
    // Query 0
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the total number of users for which a birth month is listed
    //        (B) Find the birth month in which the most users were born
    //        (C) Find the birth month in which the fewest users (at least one) were born
    //        (D) Find the IDs, first names, and last names of users born in the month
    //            identified in (B)
    //        (E) Find the IDs, first names, and last name of users born in the month
    //            identified in (C)
    //
    // This query is provided to you completed for reference. Below you will find the appropriate
    // mechanisms for opening up a statement, executing a query, walking through results, extracting
    // data, and more things that you will need to do for the remaining nine queries
    public BirthMonthInfo findMonthOfBirthInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            // Step 1
            // ------------
            // * Find the total number of users with birth month info
            // * Find the month in which the most users were born
            // * Find the month in which the fewest (but at least 1) users were born
            ResultSet rst = stmt.executeQuery(
                    "SELECT COUNT(*) AS Birthed, Month_of_Birth " + // select birth months and number of uses with that birth month
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth IS NOT NULL " + // for which a birth month is available
                            "GROUP BY Month_of_Birth " + // group into buckets by birth month
                            "ORDER BY Birthed DESC, Month_of_Birth ASC"); // sort by users born in that month, descending; break ties by birth month

            int mostMonth = 0;
            int leastMonth = 0;
            int total = 0;
            while (rst.next()) { // step through result rows/records one by one
                if (rst.isFirst()) { // if first record
                    mostMonth = rst.getInt(2); //   it is the month with the most
                }
                if (rst.isLast()) { // if last record
                    leastMonth = rst.getInt(2); //   it is the month with the least
                }
                total += rst.getInt(1); // get the first field's value as an integer
            }
            BirthMonthInfo info = new BirthMonthInfo(total, mostMonth, leastMonth);

            // Step 2
            // ------------
            // * Get the names of users born in the most popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name " + // select ID, first name, and last name
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth = " + mostMonth + " " + // born in the most popular birth month
                            "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addMostPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 3
            // ------------
            // * Get the names of users born in the least popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name " + // select ID, first name, and last name
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth = " + leastMonth + " " + // born in the least popular birth month
                            "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addLeastPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 4
            // ------------
            // * Close resources being used
            rst.close();
            stmt.close(); // if you close the statement first, the result set gets closed automatically

            return info;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new BirthMonthInfo(-1, -1, -1);
        }
    }

    @Override
    // Query 1
    // -----------------------------------------------------------------------------------
    // GOALS: (A) The first name(s) with the most letters
    //        (B) The first name(s) with the fewest letters
    //        (C) The first name held by the most users
    //        (D) The number of users whose first name is that identified in (C)
    public FirstNameInfo findNameInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            //Find first name(S) with most letters
            ResultSet rstLongNames = stmt.executeQuery(
                "SELECT DISTINCT first_name AS FirstNameLength " + 
                    "FROM " + UsersTable + " " + 
                    "WHERE LENGTH(first_name) = (SELECT MAX(LENGTH(first_name)) FROM " + UsersTable + ") " + 
                    "ORDER BY first_name ASC"
            );

            FirstNameInfo info = new FirstNameInfo();
            while(rstLongNames.next()){
                info.addLongName(rstLongNames.getString(1));
            }
            //Find first name(s) with fewest letters    
            ResultSet rstShortNames = stmt.executeQuery(
                "SELECT DISTINCT first_name AS FirstNameLength " + 
                    "FROM " + UsersTable + " " + 
                    "WHERE LENGTH(first_name) = (SELECT MIN(LENGTH(first_name)) FROM " + UsersTable + ") " + 
                    "ORDER BY first_name ASC"
            );
            while(rstShortNames.next()){
                info.addShortName(rstShortNames.getString(1));
            }    
            //FIND MOST COMMON NAMES
            ResultSet rstCommonNames = stmt.executeQuery(
                    "SELECT first_name, COUNT(first_name) AS counter FROM " + UsersTable + " " + "GROUP BY first_name " +
                    "HAVING COUNT(first_name) = (SELECT MAX(counter) FROM (SELECT first_name, COUNT(first_name) AS counter FROM " + UsersTable + " " + "GROUP BY first_name))" + " ORDER BY first_name ASC");

            while(rstCommonNames.next()){
                info.addCommonName(rstCommonNames.getString(1));
                info.setCommonNameCount(rstCommonNames.getLong(2));
            }
                                

            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                FirstNameInfo info = new FirstNameInfo();
                info.addLongName("Aristophanes");
                info.addLongName("Michelangelo");
                info.addLongName("Peisistratos");
                info.addShortName("Bob");
                info.addShortName("Sue");
                info.addCommonName("Harold");
                info.addCommonName("Jessica");
                info.setCommonNameCount(42);
                return info;
            */
            rstLongNames.close();
            rstShortNames.close();
            rstCommonNames.close();
            stmt.close();
            return info; // placeholder for compilation
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new FirstNameInfo();
        }
    }

    @Override
    // Query 2
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users without any friends
    //
    // Be careful! Remember that if two users are friends, the Friends table only contains
    // the one entry (U1, U2) where U1 < U2.
    public FakebookArrayList<UserInfo> lonelyUsers() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
                ResultSet rst = stmt.executeQuery(
                    "SELECT u.user_id, u.first_name, u.last_name " + 
                    "FROM " + UsersTable + " u " +
                    "WHERE u.user_id NOT IN (" +
                    "SELECT f.user1_id FROM " + FriendsTable + " f " +
                    "UNION " + 
                    "SELECT f.user2_id FROM " + FriendsTable + " f " +
                    ")" + 
                    "ORDER BY u.user_id ASC"
                );

                while(rst.next()){
                    UserInfo u = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3));
                    results.add(u);
                }
                rst.close();
                stmt.close();

            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(15, "Abraham", "Lincoln");
                UserInfo u2 = new UserInfo(39, "Margaret", "Thatcher");
                results.add(u1);
                results.add(u2);
            */
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 3
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users who no longer live
    //            in their hometown (i.e. their current city and their hometown are different)
    public FakebookArrayList<UserInfo> liveAwayFromHome() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
                ResultSet rst = stmt.executeQuery(
                    "SELECT uc.user_id, u.first_name, u.last_name FROM " + CurrentCitiesTable + " uc " +
                    "JOIN " + HometownCitiesTable + " uh ON " + 
                    "uc.user_id = uh.user_id AND uc.current_city_id != uh.hometown_city_id " +
                    "JOIN " + UsersTable + " u ON " +
                    "uc.user_id = u.user_id " + 
                    "ORDER BY uc.user_id"  
                );

                while (rst.next()){
                    UserInfo u = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3));
                    results.add(u);
                }

                rst.close();
                stmt.close();
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(9, "Meryl", "Streep");
                UserInfo u2 = new UserInfo(104, "Tom", "Hanks");
                results.add(u1);
                results.add(u2);
            */
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 4
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, links, and IDs and names of the containing album of the top
    //            <num> photos with the most tagged users
    //        (B) For each photo identified in (A), find the IDs, first names, and last names
    //            of the users therein tagged
    public FakebookArrayList<TaggedPhotoInfo> findPhotosWithMostTags(int num) throws SQLException {
        FakebookArrayList<TaggedPhotoInfo> results = new FakebookArrayList<TaggedPhotoInfo>("\n");

        try (Statement stmtOuter = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {

                ResultSet rstOuter = stmtOuter.executeQuery(
                    "SELECT p.photo_id, p.album_id, p.photo_link, a.album_name FROM " + PhotosTable + " p " + 
                    "JOIN " + AlbumsTable + " a " + 
                    "ON p.album_id = a.album_id " + 
                    "WHERE p.photo_id IN " + 
                    "(SELECT p.photo_id FROM " + PhotosTable + " p " +
                    "JOIN " + TagsTable + " t " +
                    "ON p.photo_id = t.tag_photo_id " + 
                    "GROUP BY p.photo_id " + 
                    "ORDER BY COUNT(*) DESC, p.photo_id " + 
                    "FETCH FIRST " + num + " ROWS ONLY)"
                );

                while(rstOuter.next()){
                    PhotoInfo p = new PhotoInfo(rstOuter.getInt(1),rstOuter.getInt(2), rstOuter.getString(3),rstOuter.getString(4));
                    TaggedPhotoInfo tp = new TaggedPhotoInfo(p);
                    try(Statement stmtInner = oracle.createStatement(FakebookOracleConstants.AllScroll,FakebookOracleConstants.ReadOnly)){
                        ResultSet taggedUsersResultSet = stmtInner.executeQuery("SELECT u.user_id,u.first_name,u.last_name FROM " + UsersTable + " u " +
                        "JOIN " + TagsTable + " t " + "ON u.user_id=t.tag_subject_id " + 
                        "WHERE t.tag_photo_id = " + rstOuter.getInt(1) +
                        " ORDER BY u.user_id ASC"
                    );
                    while(taggedUsersResultSet.next()){
                        UserInfo u = new UserInfo(taggedUsersResultSet.getInt(1),taggedUsersResultSet.getString(2),taggedUsersResultSet.getString(3));
                        tp.addTaggedUser(u);
                    }
                }
                results.add(tp);
            }
                
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                PhotoInfo p = new PhotoInfo(80, 5, "www.photolink.net", "Winterfell S1");
                UserInfo u1 = new UserInfo(3901, "Jon", "Snow");
                UserInfo u2 = new UserInfo(3902, "Arya", "Stark");
                UserInfo u3 = new UserInfo(3903, "Sansa", "Stark");
                TaggedPhotoInfo tp = new TaggedPhotoInfo(p);
                tp.addTaggedUser(u1);
                tp.addTaggedUser(u2);
                tp.addTaggedUser(u3);
                results.add(tp);
            */
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 5
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, last names, and birth years of each of the two
    //            users in the top <num> pairs of users that meet each of the following
    //            criteria:
    //              (i) same gender
    //              (ii) tagged in at least one common photo
    //              (iii) difference in birth years is no more than <yearDiff>
    //              (iv) not friends
    //        (B) For each pair identified in (A), find the IDs, links, and IDs and names of
    //            the containing album of each photo in which they are tagged together
    public FakebookArrayList<MatchPair> matchMaker(int num, int yearDiff) throws SQLException {
        FakebookArrayList<MatchPair> results = new FakebookArrayList<MatchPair>("\n");
    
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly);
             ResultSet innerRST = stmt.executeQuery(
                // PART A 
                "SELECT U1.USER_ID, U1.FIRST_NAME, U1.LAST_NAME, U1.YEAR_OF_BIRTH, "
                + "U2.USER_ID, U2.FIRST_NAME, U2.LAST_NAME, U2.YEAR_OF_BIRTH, "
                + "COUNT(DISTINCT T.TAG_PHOTO_ID) AS shared "
                + "FROM " + UsersTable + " U1 "
                + "JOIN " + UsersTable + " U2 ON U1.GENDER = U2.GENDER "
                + "JOIN " + TagsTable + " T ON U1.USER_ID = T.TAG_SUBJECT_ID "
                + "JOIN " + TagsTable + " T2 ON T.TAG_PHOTO_ID = T2.TAG_PHOTO_ID AND T.TAG_SUBJECT_ID < T2.TAG_SUBJECT_ID AND T2.TAG_SUBJECT_ID = U2.USER_ID "
                + "LEFT JOIN " + FriendsTable + " F ON (U1.USER_ID = F.USER1_ID AND U2.USER_ID = F.USER2_ID) OR (U1.USER_ID = F.USER2_ID AND U2.USER_ID = F.USER1_ID) "
                + "WHERE ABS(U1.YEAR_OF_BIRTH - U2.YEAR_OF_BIRTH) <= " + yearDiff + " AND F.USER1_ID IS NULL "
                + "GROUP BY U1.USER_ID, U1.FIRST_NAME, U1.LAST_NAME, U1.YEAR_OF_BIRTH, U2.USER_ID, U2.FIRST_NAME, U2.LAST_NAME, U2.YEAR_OF_BIRTH "
                + "ORDER BY shared DESC, U1.USER_ID, U2.USER_ID "
                + "FETCH FIRST " + num + " ROWS ONLY"
                );
             ) {
            
            while (innerRST.next()) {
                UserInfo u1 = new UserInfo(innerRST.getLong(1), innerRST.getString(2), innerRST.getString(3));
                UserInfo u2 = new UserInfo(innerRST.getLong(5), innerRST.getString(6), innerRST.getString(7));
                MatchPair mp = new MatchPair(u1, innerRST.getInt(4), u2, innerRST.getInt(8));
    
                try (Statement stmt2 = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly);
                     ResultSet outteRST = stmt2.executeQuery(
                        // part b
                    "SELECT P.PHOTO_ID, P.PHOTO_LINK, A.ALBUM_ID, A.ALBUM_NAME "
                     + "FROM " + PhotosTable + " P JOIN " + AlbumsTable + " A ON P.ALBUM_ID = A.ALBUM_ID "
                     + "JOIN " + TagsTable + " T1 ON P.PHOTO_ID = T1.TAG_PHOTO_ID "
                     + "JOIN " + TagsTable + " T2 ON P.PHOTO_ID = T2.TAG_PHOTO_ID "
                     + "WHERE T1.TAG_SUBJECT_ID = " + innerRST.getLong(1) + " AND T2.TAG_SUBJECT_ID = " + innerRST.getLong(5) + " "
                     + "GROUP BY P.PHOTO_ID, P.PHOTO_LINK, A.ALBUM_ID, A.ALBUM_NAME "
                     + "ORDER BY P.PHOTO_ID");
                     ) {
                    
                    while (outteRST.next()) {
                        PhotoInfo p = new PhotoInfo(outteRST.getLong(1), outteRST.getLong(3), outteRST.getString(2), outteRST.getString(4));
                        mp.addSharedPhoto(p);
                    }
                }
                results.add(mp);
            }

            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(93103, "Romeo", "Montague");
                UserInfo u2 = new UserInfo(93113, "Juliet", "Capulet");
                MatchPair mp = new MatchPair(u1, 1597, u2, 1597);
                PhotoInfo p = new PhotoInfo(167, 309, "www.photolink.net", "Tragedy");
                mp.addSharedPhoto(p);
                results.add(mp);
            */
        } catch (SQLException e) {
            System.err.println("SQL exception: " + e.getMessage());
        }
        return results;
    }
    
    
    
    @Override
    // Query 6
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of each of the two users in
    //            the top <num> pairs of users who are not friends but have a lot of
    //            common friends
    //        (B) For each pair identified in (A), find the IDs, first names, and last names
    //            of all the two users' common friends
    public FakebookArrayList<UsersPair> suggestFriends(int num) throws SQLException {
        FakebookArrayList<UsersPair> results = new FakebookArrayList<UsersPair>("\n");
    
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {

            stmt.executeUpdate("CREATE OR REPLACE VIEW Bidirectional AS " + 
            "SELECT USER1_ID AS USER1, USER2_ID AS USER2 FROM " + FriendsTable + " " +
            "UNION ALL " +
            "SELECT USER2_ID, USER1_ID FROM " + FriendsTable
            );
            
            ResultSet rst = stmt.executeQuery(
            "WITH MutualCounts AS (" +
            "SELECT a.USER1 AS USER1, b.USER1 AS USER2, COUNT(*) AS MutualFriends " +
            "FROM Bidirectional a JOIN Bidirectional b ON a.USER2 = b.USER2 AND a.USER1 < b.USER1 " +
            "GROUP BY a.USER1, b.USER1), " +
            "FilteredPairs AS (" +
            "SELECT USER1, USER2, MutualFriends FROM MutualCounts " +
            "WHERE NOT EXISTS (" +
            "SELECT 1 FROM " + FriendsTable + " f " +
            "WHERE (f.USER1_ID = MutualCounts.USER1 AND f.USER2_ID = MutualCounts.USER2) " +
            "OR (f.USER1_ID = MutualCounts.USER2 AND f.USER2_ID = MutualCounts.USER1)) " +
            "ORDER BY MutualFriends DESC, USER1, USER2 FETCH FIRST " + num + " ROWS ONLY) " +
            "SELECT f.USER1, f.USER2, f.MutualFriends, u1.FIRST_NAME AS USER1_FIRST, u1.LAST_NAME AS USER1_LAST, " +
            "u2.FIRST_NAME AS USER2_FIRST, u2.LAST_NAME AS USER2_LAST " +
            "FROM FilteredPairs f " +
            "JOIN " + UsersTable + " u1 ON f.USER1 = u1.USER_ID " +
            "JOIN " + UsersTable + " u2 ON f.USER2 = u2.USER_ID");

            while (rst.next()) {
                long userId1 = rst.getLong(1);
                long userId2 = rst.getLong(2);
                UserInfo user1 = new UserInfo(userId1, rst.getString(4), rst.getString(5));
                UserInfo user2 = new UserInfo(userId2, rst.getString(6), rst.getString(7));
                UsersPair up = new UsersPair(user1, user2);
                // FIND MUTUAL FRIENDS? IDK
                Statement stmt2 = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly);
                ResultSet rst2 = stmt2.executeQuery(
                    "SELECT u.USER_ID, u.FIRST_NAME, u.LAST_NAME FROM Bidirectional f " +  
                    "JOIN Bidirectional f1 ON f.USER1 < f1.USER1 " +
                    "JOIN " + UsersTable + " u ON f.USER2 = u.USER_ID " + 
                    "WHERE f.USER1 = " + userId1 + " AND f1.USER1 = " + userId2 + " " +
                    "AND f.USER2 = f1.USER2 " + 
                    "ORDER BY u.USER_ID ASC"
                );

                while(rst2.next()){
                    UserInfo u3 = new UserInfo(rst2.getInt(1), rst2.getString(2), rst2.getString(3));
                    up.addSharedFriend(u3);
                }

                rst2.close();
                stmt2.close();
                results.add(up);
            }
            rst.close();
            stmt.executeUpdate("DROP VIEW Bidirectional");
            stmt.close();
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(16, "The", "Hacker");
                UserInfo u2 = new UserInfo(80, "Dr.", "Marbles");
                UserInfo u3 = new UserInfo(192, "Digit", "Le Boid");
                UsersPair up = new UsersPair(u1, u2);
                up.addSharedFriend(u3);
                results.add(up);
            */
        } catch (SQLException e) {
            System.err.println("SQL exception: " + e.getMessage());
        }
    
        return results;
    }
    
    
    @Override
    // Query 7
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the name of the state or states in which the most events are held
    //        (B) Find the number of events held in the states identified in (A)
    public EventStateInfo findEventStates() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
                ResultSet rst = stmt.executeQuery(
                    "WITH EventCounts AS (" +
                    "SELECT c.state_name, COUNT(*) AS NumEvents FROM " + CitiesTable + " c " +
                    "JOIN " + EventsTable + " e ON c.city_id = e.event_city_id " +
                    "GROUP BY c.state_name), " +
                    "MaxEventCount AS ( " +
                    "SELECT MAX(NumEvents) AS MaxEvents FROM EventCounts) " +
                    "SELECT ec.state_name, ec.NumEvents FROM EventCounts ec " +
                    "JOIN MaxEventCount mec ON ec.NumEvents = mec.MaxEvents " +
                    "ORDER BY ec.state_name ASC"

                );
                EventStateInfo info = null;
                int numEvents = 0;
                if (rst.next()) {
                    numEvents = rst.getInt(2);
                    info = new EventStateInfo(numEvents);
                
                    info.addState(rst.getString(1));
                    while (rst.next() && rst.getInt(2) == numEvents) {
                        info.addState(rst.getString(1));
                    }
                
                }          
                rst.close();
                stmt.close();      
                return info;
                
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                EventStateInfo info = new EventStateInfo(50);
                info.addState("Kentucky");
                info.addState("Hawaii");
                info.addState("New Hampshire");
                return info;
            */
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new EventStateInfo(-1);
        }
    }

    @Override
    // Query 8
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the ID, first name, and last name of the oldest friend of the user
    //            with User ID <userID>
    //        (B) Find the ID, first name, and last name of the youngest friend of the user
    //            with User ID <userID>
    public AgeInfo findAgeInfo(long userID) throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
                //first name, last name oldest
                 ResultSet rstOldest = stmt.executeQuery(
                    "SELECT user_id, first_name, last_name" +
                    " FROM " + UsersTable + " WHERE user_id IN" + 
                    "(SELECT F1.user1_id FROM " + FriendsTable + " F1 " +
                    "WHERE F1.user2_id=" + userID + " UNION " + 
                    "SELECT F2.user2_id FROM " + FriendsTable + " F2 " + 
                    "WHERE F2.user1_id= " + userID + ")" + 
                    " ORDER BY year_of_birth ASC, month_of_birth ASC, day_of_birth ASC, user_id DESC " +
                    "FETCH FIRST 1 ROWS ONLY"
                 );
                 rstOldest.next();
                 UserInfo old = new UserInfo(rstOldest.getLong(1),rstOldest.getString(2),rstOldest.getString(3));
                 //find first name, last name youngest
                 ResultSet rstYoungest = stmt.executeQuery(
                    "SELECT user_id, first_name, last_name" +
                    " FROM " + UsersTable + " WHERE user_id IN" + 
                    "(SELECT F1.user1_id FROM " + FriendsTable + " F1 " +
                    "WHERE F1.user2_id=" + userID + " UNION " + 
                    "SELECT F2.user2_id FROM " + FriendsTable + " F2 " + 
                    "WHERE F2.user1_id= " + userID + ")" + 
                    " ORDER BY year_of_birth DESC, month_of_birth DESC, day_of_birth DESC, user_id DESC " +
                    "FETCH FIRST 1 ROWS ONLY"
                 );
                 rstYoungest.next();
                 UserInfo young = new UserInfo(rstYoungest.getLong(1),rstYoungest.getString(2),rstYoungest.getString(3));
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo old = new UserInfo(12000000, "Galileo", "Galilei");
                UserInfo young = new UserInfo(80000000, "Neil", "deGrasse Tyson");
                return new AgeInfo(old, young);
            */
            rstOldest.close();
            rstYoungest.close();
            stmt.close();
            return new AgeInfo(old, young); // placeholder for compilation
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new AgeInfo(new UserInfo(-1, "ERROR", "ERROR"), new UserInfo(-1, "ERROR", "ERROR"));
        }
    }

    @Override
    // Query 9
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find all pairs of users that meet each of the following criteria
    //              (i) same last name
    //              (ii) same hometown
    //              (iii) are friends
    //              (iv) less than 10 birth years apart
    public FakebookArrayList<SiblingInfo> findPotentialSiblings() throws SQLException {
        FakebookArrayList<SiblingInfo> results = new FakebookArrayList<SiblingInfo>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {

                ResultSet rst = stmt.executeQuery(
                    "SELECT U1.USER_ID AS User1_ID, U1.FIRST_NAME AS User1_FirstName, U1.LAST_NAME AS User1_LastName, U2.USER_ID AS User2_ID, U2.FIRST_NAME AS User2_FirstName, U2.LAST_NAME AS User2_LastName FROM " + UsersTable + " U1 " +
                    "JOIN " + UsersTable + " U2 ON U1.LAST_NAME = U2.LAST_NAME AND U1.USER_ID < U2.USER_ID AND ABS(U1.YEAR_OF_BIRTH - U2.YEAR_OF_BIRTH) < 10 " +
                    "JOIN " + FriendsTable + " F ON (U1.USER_ID = F.USER1_ID AND U2.USER_ID = F.USER2_ID) OR (U1.USER_ID = F.USER2_ID AND U2.USER_ID = F.USER1_ID) " +
                    "JOIN " + HometownCitiesTable + " HC1 ON U1.USER_ID = HC1.USER_ID " +
                    "JOIN " + HometownCitiesTable + " HC2 ON U2.USER_ID = HC2.USER_ID AND HC1.HOMETOWN_CITY_ID = HC2.HOMETOWN_CITY_ID " +
                    "ORDER BY U1.USER_ID ASC, U2.USER_ID ASC"
                );

                while(rst.next()){
                    UserInfo u1 = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3));
                    UserInfo u2 = new UserInfo(rst.getLong(4), rst.getString(5), rst.getString(6));
                    SiblingInfo si = new SiblingInfo(u1, u2);
                    results.add(si);
                }

                rst.close();
                stmt.close();

                
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(81023, "Kim", "Kardashian");
                UserInfo u2 = new UserInfo(17231, "Kourtney", "Kardashian");
                SiblingInfo si = new SiblingInfo(u1, u2);
                results.add(si);
            */
        
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    // Member Variables
    private Connection oracle;
    private final String UsersTable = FakebookOracleConstants.UsersTable;
    private final String CitiesTable = FakebookOracleConstants.CitiesTable;
    private final String FriendsTable = FakebookOracleConstants.FriendsTable;
    private final String CurrentCitiesTable = FakebookOracleConstants.CurrentCitiesTable;
    private final String HometownCitiesTable = FakebookOracleConstants.HometownCitiesTable;
    private final String ProgramsTable = FakebookOracleConstants.ProgramsTable;
    private final String EducationTable = FakebookOracleConstants.EducationTable;
    private final String EventsTable = FakebookOracleConstants.EventsTable;
    private final String AlbumsTable = FakebookOracleConstants.AlbumsTable;
    private final String PhotosTable = FakebookOracleConstants.PhotosTable;
    private final String TagsTable = FakebookOracleConstants.TagsTable;
}