/*
 Emmanuel Lee
 April 8, 2019
 Final Project
 Csi410 Intro to Databases
 */
package csi410_restaurants;

import java.sql.*;
import java.util.Scanner;

public class Restaurant_test {

    public static void main(String[] args) throws Exception {
        Csi410_Restaurants res = new Csi410_Restaurants();
        Connection conn = res.get_Connection();
        Statement stmt = conn.createStatement();

        String breakfast_seats = "";
        String lunch_seats = "";
        String dinner_seats = "";
        try {
            String time; //used to hold the time value the user enters 
            String size; //used to hold the size of the party 
            String command; //used to hold answers from the user 
            String loop = ""; //used to hold a return value so that the do-while loop continues 

            Restaurant_test test = new Restaurant_test(); //object of the Restairant_test class
            Scanner keyboard = new Scanner(System.in); //Scanner obeject used to get user input

            System.out.println("Reservation: no password press [ENTER] \nAdministrator: type [admin]"); //enter admin mode or continue as new user

            String user = keyboard.nextLine().toLowerCase().trim();

            switch (user) {
                case "admin":
                    //perform crud operations on database
                    System.out.println("*****Administrator Mode***** \nDisplay reservations: type read\n"
                            + "Change reservations: type update\nRemove reservations: type del\nExit application: type exit");
                    do {
                        command = keyboard.nextLine().toLowerCase().trim(); //read in command for administrative action
                        switch (command) {
                            case "read":
                                loop = Read(); //method for reading tables 
                                break;
                            case "update":
                                loop = Update(); //method for updating fields 
                                break;
                            case "del":
                                loop = delete(); //method for deleting fields 
                                break;
                            case "exit":
                                loop = "exit";
                                System.out.println("Bye!"); //exits program 
                                break;
                            default:
                                System.out.println("invalid command. try again");

                        }
                        if (loop.equals("")) {
                            System.out.println("*****Administrator Mode***** \nDisplay reservations: type read\n"
                                    + "Change reservations: type update\nRemove reservations: type del\nExit application: type exit");
                            System.out.print("Command: ");
                        }

                    } while (loop.equals("")); //iterate admin actions until terminated 
                    break;

                case "": //new reservation 
                    do {
                        System.out.println("-----New Reservation-----");

                        System.out.println("What is your first name?");
                        String first_name = keyboard.nextLine().toLowerCase();

                        System.out.println("What is your last name?");
                        String last_name = keyboard.nextLine().toLowerCase();

                        System.out.println("What time do you prefer to eat? (please enter in x:xx pm/am format)");
                        time = keyboard.nextLine().toLowerCase();

                        System.out.println("What party size do you have arriving?");
                        size = keyboard.nextLine();

                        if (time.matches("(1[0-2]|0?[1-9]):([0-5][0-9]) ?([AaPp][Mm])") && size.matches("\\d+")) {
                            //method to calculate and update remaining seats left for reservations
                            String seats = removeSeats(time, size);

                            if (seats.equals("invalid")) {
                                System.out.println("Sorry, there are not enough seats available for your party at this time");
                                break;
                            } else {
                                System.out.println("We have " + seats + " seats left for this meal time");
                                create(last_name, first_name, time, size);

                                System.out.print("Do you want to make another reservation? [yes/no]\nAnswer: ");
                                loop = keyboard.nextLine().toLowerCase().trim();
                                switch (loop) {
                                    case "yes":
                                        loop = "yes";
                                    case "no":
                                        System.out.println("Bye!");
                                        break;
                                }
                            }
                        } else {
                            System.out.println("Invalid time or size format. Please enter information again to complete reservation\n");
                        }

                    } while (!time.matches("((1[0-2]|0?[1-9]):([0-5][0-9]) ?([AaPp][Mm]))") | !size.matches("\\d+") | loop.equals("yes"));
                    break;
                default:
                    System.out.println("invalid command");
                    break;
            }

        } catch (Exception e) {
            System.out.println("Somethid bad happened");
        }
    }

    //-----------------------------------------------------------------------------
    //-----checks if remaining seats are available for new reservation update-----
    public static boolean checkSeats(String id, String table, String size, String table_seats) throws Exception {
        Csi410_Restaurants res = new Csi410_Restaurants();
        Connection conn = res.get_Connection();
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT party_size FROM " + table + " where id = " + id);
        ResultSetMetaData rsmd = rs.getMetaData();

        String party_size = "";

        //get the row associated with the update 
        try {
            while (rs.next()) {
                for (int i = 0; i < 1; i++) {
                    party_size = rs.getString(i + 1); //party size from the row selected
                }
            }

            int row_size = Integer.parseInt(party_size); //size found in the current entry
            int new_size = Integer.parseInt(size); //size to be updated
            int seats = Integer.parseInt(table_seats); //seats left in database for this table

            return (row_size + seats) >= new_size;

        } catch (SQLException e) {
            System.out.println("Something happened trying to get the party size");
        }

        return true;
    }
//------------------------------------------------------------------------------
    //-----add the number of seats reviously removed back in a reservation-----

    public static boolean addSeats(String id, String table) throws Exception {
        Csi410_Restaurants res = new Csi410_Restaurants();
        Connection conn = res.get_Connection();
        Statement stmt = conn.createStatement();
        PreparedStatement ps;

        int party_size = 0;
        int seat_size = 0;

        try {
            //get the size of the reservation party with the given id
            ResultSet rs = stmt.executeQuery("SELECT party_size FROM " + table + " where id = " + id);
            while (rs.next()) {
                for (int i = 0; i < 1; i++) {
                    String temp = rs.getString(i + 1);
                    party_size = Integer.parseInt(temp); //convert party size from desired row to an int
                }
            }
            //get the number of remaining seats in desired column from seats
            rs = stmt.executeQuery("select " + table + " from seats");
            while (rs.next()) {
                for (int i = 0; i < 1; i++) {
                    String temp = rs.getString(i + 1);
                    seat_size = Integer.parseInt(temp); //convert number of availablilty in seats to an int

                }
            }
            //add the number of seats that were taken back to the reservation column it came from
            seat_size = party_size + seat_size;
            String limit = Integer.toString(seat_size);
            switch (table) {
                case "breakfast":
                    String update = "update seats set breakfast = ?";
                    ps = conn.prepareStatement(update);
                    ps.setString(1, limit);

                    //System.out.println(ps);
                    //System.out.println("New " + table + " seating is " + limit);
                    ps.executeUpdate();
                    break;

                case "lunch":
                    update = "update seats set lunch = ?";
                    ps = conn.prepareStatement(update);
                    ps.setString(1, limit);

                    //System.out.println(ps);
                    //System.out.println("New " + table + " seating is " + limit);
                    ps.executeUpdate();
                    break;

                case "dinner":
                    update = "update seats set dinner = ?";
                    ps = conn.prepareStatement(update);
                    ps.setString(1, limit);

                    //System.out.println(ps);
                    //System.out.println("New " + table + " seating is " + limit);
                    ps.executeUpdate();
                    break;

                default:
                    System.out.println("Something went wrong");
                    return false;

            }
        } catch (NumberFormatException | SQLException e) {
            System.out.println("Something bad happened");
        }
        return true;
    }

    //-----------------------------------------------------------------------------
    //*****updates and returns number of seats remaining*****
    public static String removeSeats(String s_time, String party_size) throws Exception {

        int num_party = Integer.parseInt(party_size); //convert party size to an int
        int num_seats; //number of seats for each table 
        int new_seat_count; //updated seat count 
        int time;
        int columnsNumber;
        String seats_left;
        String breakfast_seats = "";
        String lunch_seats = "";
        String dinner_seats = "";

        Csi410_Restaurants res = new Csi410_Restaurants();
        Connection conn = res.get_Connection();
        Statement stmt = conn.createStatement();
        PreparedStatement ps;

        String temp_time = s_time.replaceAll("[^0-9.]", "").trim(); //remove all besides numbers from time input
        time = Integer.parseInt(temp_time); //convert time string to int

        String[] arr = new String[100];
        //calculate current number of seats in seats table
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM seats"); //select all fields from table seats
            ResultSetMetaData rsmd = rs.getMetaData();
            columnsNumber = rsmd.getColumnCount();

            while (rs.next()) {
                for (int i = 0; i < columnsNumber; i++) {
                    String temp = rs.getString(i + 1);
                    arr[i] = temp;
                }
            }
            breakfast_seats = arr[0];
            lunch_seats = arr[1];
            dinner_seats = arr[2];

        } catch (SQLException e) {
            System.out.println("Something happened while trying to access seats");
        }
        int num_b = Integer.parseInt(breakfast_seats); //convert breakfast count to an int
        int num_l = Integer.parseInt(lunch_seats); //convert lunch count to an int
        int num_d = Integer.parseInt(dinner_seats); //convert dinner count to an int 

        //breakfast time
        if (s_time.contains("am") && time >= 700 && time < 1200) {
            if (num_party > num_b) {
                return "invalid";
            }

            new_seat_count = num_b - num_party;

            seats_left = Integer.toString(new_seat_count);

            String command = "update seats set breakfast = ?";
            ps = conn.prepareStatement(command);
            ps.setString(1, seats_left);

            ps.executeUpdate();

        } //lunch time
        else if (s_time.contains("pm") && (time <= 1200 && time < 500)) {
            if (num_party > num_l) {
                return "invalid";
            }
            new_seat_count = num_l - num_party;

            seats_left = Integer.toString(new_seat_count);

            String command = "update seats set lunch = ?";
            ps = conn.prepareStatement(command);
            ps.setString(1, seats_left);

            ps.executeUpdate();

        } //dinner time
        else if (s_time.contains("pm") && (time >= 500 && time < 1000)) {
            if (num_party > num_d) {
                return "invalid";
            }

            new_seat_count = num_d - num_party;

            seats_left = Integer.toString(new_seat_count);

            String command = "update seats set dinner = ?";
            ps = conn.prepareStatement(command);
            ps.setString(1, seats_left);

            ps.executeUpdate();

        } else {
            return "invalid";
        }

        return seats_left;
    }
 //-----------------------------------------------------------------------------
                    //method for creating a new rservation
    public static String create(String last_name, String first_name, String s_time, String size) throws Exception {

        int time;
        String loop;

        String temp = s_time.replaceAll("[^0-9.]", "").trim();
        time = Integer.parseInt(temp);

        if (s_time.contains("am") && time >= 700 && time < 1200) {
            loop = createBreakfast(last_name, first_name, s_time, size);
            return "";
        } else if (s_time.contains("pm") && (time <= 1200 && time < 500)) {
            loop = createLunch(last_name, first_name, s_time, size);
            return "";
        } else if (s_time.contains("pm") && (time >= 500 && time < 1000)) {
            loop = createDinner(last_name, first_name, s_time, size);
            return "";
        } else {
            System.out.println("Sorry the store is closed during this time. Our hours of operation are 7:00am - 10:00pm. Please try again later.");
        }
        return "";
    }
//------------------------------------------------------------------------------
               //-----method for creating breakfast reservation-----
    public static String createBreakfast(String last_name, String first_name, String s_time, String size) throws Exception {

        int number = 0;

        Csi410_Restaurants res = new Csi410_Restaurants();
        Connection conn = res.get_Connection();
        Statement command = conn.createStatement();

        PreparedStatement ps;

        try {
            //get the number of rows from the database to update the number of reservations per client
            ResultSet rs = command.executeQuery("SELECT count(*) FROM breakfast");
            ResultSetMetaData rsmd = rs.getMetaData();

            if (rs.next()) {
                number = Integer.parseInt(rs.getString(1));
            }

            String current_id = Integer.toString(number);
            number++;
            String next_id = Integer.toString(number);

            //update row with new reservation 
            String stmt = "update breakfast set last_name = ?, first_name = ?, time = ?, party_size = ? where id = ?";
            ps = conn.prepareStatement(stmt);
            ps.setString(1, last_name);
            ps.setString(2, first_name);
            ps.setString(3, s_time);
            ps.setString(4, size);
            ps.setString(5, current_id);

            System.out.println(ps);
            ps.executeUpdate();

            //insert a new row
            stmt = "insert into breakfast(id) values(?)";
            ps = conn.prepareStatement(stmt);
            ps.setString(1, next_id);

            ps.executeUpdate();

            //change NULL values to "null".
            stmt = "update breakfast set last_name = ?, first_name = ?, time = ?, party_size = ? where id = ?";
            ps = conn.prepareStatement(stmt);
            ps.setString(1, "null");
            ps.setString(2, "null");
            ps.setString(3, "null");
            ps.setString(4, "null");
            ps.setString(5, next_id);

            ps.executeUpdate();

            System.out.println("\n-----Thank you for your reservation-----");

        } catch (NumberFormatException | SQLException e) {
            System.out.println(e);
        }
        return "";
    }
    //--------------------------------------------------------------------------
                   //-----method for creating lunch reservation-----
    public static String createLunch(String last_name, String first_name, String s_time, String size) throws Exception {
        int number = 0;

        Csi410_Restaurants res = new Csi410_Restaurants();
        Connection conn = res.get_Connection();
        Statement command = conn.createStatement();

        PreparedStatement ps;

        try {
            //get the number of rows from the database to update the number of reservations per client
            ResultSet rs = command.executeQuery("SELECT count(*) FROM lunch");
            ResultSetMetaData rsmd = rs.getMetaData();

            if (rs.next()) {
                number = Integer.parseInt(rs.getString(1));
            }

            String current_id = Integer.toString(number);
            number++;
            String next_id = Integer.toString(number);

            //update row with new reservation 
            String stmt = "update lunch set last_name = ?, first_name = ?, time = ?, party_size = ? where id = ?";
            ps = conn.prepareStatement(stmt);
            ps.setString(1, last_name);
            ps.setString(2, first_name);
            ps.setString(3, s_time);
            ps.setString(4, size);
            ps.setString(5, current_id);

            System.out.println(ps);
            ps.executeUpdate();

            //insert a new row
            stmt = "insert into lunch(id) values(?)";
            ps = conn.prepareStatement(stmt);
            ps.setString(1, next_id);

            ps.executeUpdate();

            //change NULL values to "null".
            stmt = "update lunch set last_name = ?, first_name = ?, time = ?, party_size = ? where id = ?";
            ps = conn.prepareStatement(stmt);
            ps.setString(1, "null");
            ps.setString(2, "null");
            ps.setString(3, "null");
            ps.setString(4, "null");
            ps.setString(5, next_id);

            ps.executeUpdate();

            System.out.println("\n-----Thank you for your reservation-----");

        } catch (NumberFormatException | SQLException e) {
            System.out.println(e);
        }
        return "";

    }
//------------------------------------------------------------------------------
                 //------method for creating dinner reservation-----
    public static String createDinner(String last_name, String first_name, String s_time, String size) throws Exception {
        int number = 0;

        Csi410_Restaurants res = new Csi410_Restaurants();
        Connection conn = res.get_Connection();
        Statement command = conn.createStatement();

        PreparedStatement ps;

        try {
            //get the number of rows from the database to update the number of reservations per client
            ResultSet rs = command.executeQuery("SELECT count(*) FROM dinner");
            ResultSetMetaData rsmd = rs.getMetaData();

            if (rs.next()) {
                number = Integer.parseInt(rs.getString(1));
            }

            String current_id = Integer.toString(number);
            number++;
            String next_id = Integer.toString(number);

            //update row with new reservation 
            String stmt = "update dinner set last_name = ?, first_name = ?, time = ?, party_size = ? where id = ?";
            ps = conn.prepareStatement(stmt);
            ps.setString(1, last_name);
            ps.setString(2, first_name);
            ps.setString(3, s_time);
            ps.setString(4, size);
            ps.setString(5, current_id);

            System.out.println(ps);
            ps.executeUpdate();

            //insert a new row
            stmt = "insert into dinner(id) values(?)";
            ps = conn.prepareStatement(stmt);
            ps.setString(1, next_id);

            ps.executeUpdate();

            //change NULL values to "null".
            stmt = "update dinner set last_name = ?, first_name = ?, time = ?, party_size = ? where id = ?";
            ps = conn.prepareStatement(stmt);
            ps.setString(1, "null");
            ps.setString(2, "null");
            ps.setString(3, "null");
            ps.setString(4, "null");
            ps.setString(5, next_id);

            ps.executeUpdate();

            System.out.println("\n-----Thank you for your reservation-----");

        } catch (NumberFormatException | SQLException e) {
            System.out.println(e);
        }
        return "";

    }

 //-----------------------------------------------------------------------------
                //-----Read from the database-----
    public static String Read() throws Exception {
        Csi410_Restaurants res = new Csi410_Restaurants();
        Connection conn = res.get_Connection();
        Statement stmt = conn.createStatement();

        int columnsNumber; //get number of columns in the table
        Scanner keyboard = new Scanner(System.in);
        String table;
        do {
            System.out.println("Which reservation table would you like to see? [breakfast, lunch, dinner, seats]\ntype [../] to cancel");
            System.out.print("Table: ");
            table = keyboard.nextLine().toLowerCase();

            switch (table) {

                case "breakfast":
                    ResultSet rs = stmt.executeQuery("SELECT * FROM breakfast"); //select all entries in breakfast table

                    ResultSetMetaData rsmd = rs.getMetaData();

                    columnsNumber = rsmd.getColumnCount();

                    String[] arr = new String[columnsNumber];
                    System.out.println("-----List of Reservations[breakfast]-----");
                    System.out.println("ID.----------------------------------------------------------------------------------------");
                    //print entries from breakfast table
                    while (rs.next()) {
                        for (int i = 0; i < columnsNumber; i++) {
                            String temp = rs.getString(i + 1);
                            arr[i] = temp.trim();
                            System.out.print(arr[i].replace(" ", "").trim() + "\t\t");
                        }
                        System.out.println();
                    }
                    break;

                case "lunch":
                    ResultSet rs2 = stmt.executeQuery("SELECT * FROM lunch"); //select all entries in lunch table

                    ResultSetMetaData rsmd2 = rs2.getMetaData();

                    columnsNumber = rsmd2.getColumnCount();

                    String[] arr2 = new String[columnsNumber];
                    System.out.println("-----List of Reservations[lunch]-----");
                    System.out.println("ID.---------------------------------------------------------------------------------------");
                    //print entries of lunch table
                    while (rs2.next()) {
                        for (int i = 0; i < columnsNumber; i++) {
                            String temp = rs2.getString(i + 1);
                            arr2[i] = temp.trim();
                            System.out.print(arr2[i].replace(" ", "").trim() + "\t\t");
                        }
                        System.out.println();
                    }
                    break;

                case "dinner":
                    ResultSet rs3 = stmt.executeQuery("SELECT * FROM dinner"); //select all from dinner table

                    ResultSetMetaData rsmd3 = rs3.getMetaData();
                    columnsNumber = rsmd3.getColumnCount();

                    String[] arr3 = new String[columnsNumber];
                    System.out.println("-----List of Reservations[dinner]-----");
                    System.out.println("ID.---------------------------------------------------------------------------------------");
                    //print all entries of dinner table
                    while (rs3.next()) {
                        for (int i = 0; i < columnsNumber; i++) {
                            String temp = rs3.getString(i + 1);
                            arr3[i] = temp.trim();
                            System.out.print(arr3[i].replace(" ", "").trim() + "\t\t");
                        }
                        System.out.println();
                    }
                    break;

                case "seats":
                    ResultSet rs4 = stmt.executeQuery("SELECT * FROM seats");

                    ResultSetMetaData rsmd4 = rs4.getMetaData();
                    columnsNumber = rsmd4.getColumnCount();

                    String[] arr4 = new String[columnsNumber];
                    System.out.println("-----List of Reservations[dinner]-----");
                    System.out.println("Breakfast\tLunch\tDinner");

                    while (rs4.next()) {
                        for (int i = 0; i < columnsNumber; i++) {
                            String temp = rs4.getString(i + 1);
                            arr4[i] = temp.trim();
                            System.out.print(arr4[i].replace(" ", "").trim() + "\t\t");
                        }
                        System.out.println();
                    }
                    break;

                case "../":
                    return "";

                default:
                    System.out.println("Invalid table. Please try again.");

            }
        } while (!table.equals("../"));

        return "";
    }
    //----------------------------------------------------------------------------- 
                //-----method for updating the database-----  
    public static String Update() throws Exception {

        String size = "";
        String s_time = "";
        String first_name = "";
        String last_name = "";
        String breakfast_seats; //holds remaining seats in breakfast table
        String lunch_seats; //holds remaining seats in lunch table
        String dinner_seats; //holds remaining seats in dinner table
        String table = ""; //used to select which table to update
        String id = ""; //used to pick a row to update
        String check = ""; //used to check the boolean select sql statement for if a row exists
        String check_null; //check if an entry is a null entry 
        int int_time; //used to check if time meets requirements 
        int int_rows = 0; //used to hold int value of party size in a specified row 
        String[] arr = new String[10]; //used to print out contents of the database
        boolean flag; //used to check if checkSeats method returns valid open space or not

        Csi410_Restaurants res = new Csi410_Restaurants();
        Scanner keyboard = new Scanner(System.in);
        PreparedStatement ps;
        Connection conn = res.get_Connection();
        Statement stmt = conn.createStatement();

        do {
            //pick a table to update
            System.out.println("-----Updating Resrvation-----\nWhich table do you wish to update?\nType [../] to cancel");
            System.out.print("Table name: ");
            table = keyboard.nextLine().toLowerCase().trim();

            switch (table) {
                //if breakfast is selected
                case "breakfast":
                    ResultSet rs = stmt.executeQuery("SELECT * FROM " + table);
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnNumber = rsmd.getColumnCount();
                    //display contents of breakfast table
                    while (rs.next()) {
                        for (int i = 0; i < columnNumber; i++) {
                            String temp = rs.getString(i + 1);
                            arr[i] = temp;
                            System.out.print(arr[i].replace(" ", "").trim() + "\t\t");
                        }
                        System.out.println();
                    }
                    break;
                //if lunch is selected
                case "lunch":
                    rs = stmt.executeQuery("SELECT * FROM " + table);
                    rsmd = rs.getMetaData();
                    columnNumber = rsmd.getColumnCount();
                    //display contents of lunch table
                    while (rs.next()) {
                        for (int i = 0; i < columnNumber; i++) {
                            String temp = rs.getString(i + 1);
                            arr[i] = temp;
                            System.out.print(arr[i].replace(" ", "").trim() + "\t\t");
                        }
                        System.out.println();
                    }
                    break;
                //if dinner is selected
                case "dinner":
                    rs = stmt.executeQuery("SELECT * FROM " + table);
                    rsmd = rs.getMetaData();
                    columnNumber = rsmd.getColumnCount();
                    //display contents of dinner table
                    while (rs.next()) {
                        for (int i = 0; i < columnNumber; i++) {
                            String temp = rs.getString(i + 1);
                            arr[i] = temp;
                            System.out.print(arr[i].replace(" ", "").trim() + "\t\t");
                        }
                        System.out.println();
                    }
                    break;
                case "../":
                    return "";
                default:
                    System.out.println("Invalid table name");
                    break;
            }
        } while (!table.equals("breakfast") && !table.equals("lunch") && !table.equals("dinner") && !table.equals("../"));

        do {
            System.out.print("Which reservation would you like to update?\nType [../] to cancel\nEnter ID: ");
            id = keyboard.nextLine().toLowerCase().trim(); //get the id of the reservation to update

            if (id.equals("../")) {
                return "";
            }
            try {
                //check if the entry exists within the table
                ResultSet rs = stmt.executeQuery("SELECT EXISTS(SELECT * FROM " + table + " WHERE id = " + id + ")");

                while (rs.next()) {
                    for (int i = 0; i < 1; i++) {
                        check = rs.getString(i + 1);
                    }
                }
            } catch (SQLException e) {
                System.out.println("Something bad happened while trying to check if entry exists");
                return "";
            }

            //check if the selected row is a null entry
            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + table + " where id = " + id);
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnsCount = rsmd.getColumnCount();
                while (rs.next()) {
                    for (int i = 0; i < columnsCount; i++) {
                        arr[i] = rs.getString(i + 1);
                    }
                }
                check_null = arr[1];
            } catch (SQLException e) {
                System.out.println("Something bad happened");
                return "";
            }

            //if the entry was not found
            if (check.equals("0") | check_null.equals("null")) {
                System.out.println("This entry does not exist. Try again");
            }
        } while (check.equals("0") | check_null.equals("null"));

        try {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table); //count number of rows in table
            while (rs.next()) {
                for (int i = 0; i < 1; i++) {
                    int_rows = Integer.parseInt(rs.getString(i + 1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Something bad happened trying to count rows in " + table + " table");
            return "";
        }
        do {
            if (int_rows > 1) {
                //enter new data
                System.out.print("First name: ");
                first_name = keyboard.nextLine().toLowerCase().trim();

                System.out.print("Last name: ");
                last_name = keyboard.nextLine().toLowerCase().trim();

                System.out.print("Time(please enter in x:xx pm/am format): ");
                s_time = keyboard.nextLine().toLowerCase().trim();

                System.out.print("Party size: ");
                size = keyboard.nextLine().trim();
            }
            if (!s_time.matches("(1[0-2]|0?[1-9]):([0-5][0-9]) ?([AaPp][Mm])") | !size.matches("\\d+")) {
                System.out.println("Invalid Time or size format. Try again");
            }

        } while (!s_time.matches("(1[0-2]|0?[1-9]):([0-5][0-9]) ?([AaPp][Mm])") | !size.matches("\\d+"));

        //calculate current number of seats in seats table
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM seats"); //select all fields from table seats
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();

            while (rs.next()) {
                for (int i = 0; i < columnsNumber; i++) {
                    arr[i] = rs.getString(i + 1);
                }
            }
            breakfast_seats = arr[0];
            lunch_seats = arr[1];
            dinner_seats = arr[2];

        } catch (SQLException e) {
            System.out.println("Something happened while trying to access Seats table");
            return "";
        }
        System.out.print("-----Update-----\nID\tLast Name\tFirst Name\tTime\tParty_size\n");
        System.out.println(id + "\t" + last_name + "\t" + first_name + "\t" + s_time + "\t" + size);
        System.out.print("Are you sure you want to update this entry? [y/n]\nAnswer: ");
        String answer = keyboard.nextLine().toLowerCase().trim();
        do {
            switch (answer) {
                case "y":
                    break;
                case "n":
                    return "";
                default:
                    System.out.println("Invalid answer. Try again");
            }
        } while (!answer.equals("y") && !answer.equals("y"));

        String temp = s_time.replaceAll("[^0-9.]", "").trim();
        int_time = Integer.parseInt(temp); //convert string time to an int

        if (table.equals("breakfast") && s_time.contains("am") && int_time >= 700 && int_time < 1200) {
            flag = checkSeats(id, table, size, breakfast_seats);

            if (flag) {
                boolean add = addSeats(id, table); //add previous seats back to table
                String seats = removeSeats(s_time, size); //remove new seat count from table
                System.out.println("There are now " + seats + " seats available in Breakfast table");
            }
        } else if (table.equals("lunch") && s_time.contains("pm") && (int_time <= 1200 && int_time < 500)) {
            flag = checkSeats(id, table, size, lunch_seats);

            if (flag) {
                boolean add = addSeats(id, table); //add previous seats back to table
                String seats = removeSeats(s_time, size); //remove new seat count from table
                System.out.println("There are now " + seats + " seats available in Lunch table");
            }
        } else if (table.equals("dinner") && s_time.contains("pm") && (int_time >= 500 && int_time < 1000)) {
            flag = checkSeats(id, table, size, dinner_seats);

            if (flag) {
                boolean add = addSeats(id, table); //add previous seats back to table
                String seats = removeSeats(s_time, size); //remove new seat count from table
                System.out.println("There are now " + seats + " seats available in Dinner table");
            }
        } else {
            System.out.println("Time mismatch");
            return "";
        }
        //update table selected
        switch (table) {
            case "breakfast":
                String update = "update breakfast set last_name = ?, first_name = ?, time = ?, party_size = ? where id = ?";
                ps = conn.prepareStatement(update);
                ps.setString(1, last_name);
                ps.setString(2, first_name);
                ps.setString(3, s_time);
                ps.setString(4, size);
                ps.setString(5, id);

                System.out.println(ps);
                ps.executeUpdate();
                break;

            case "lunch":
                update = "update lunch set last_name = ?, first_name = ?, time = ?, party_size = ? where id = ?";
                ps = conn.prepareStatement(update);
                ps.setString(1, last_name);
                ps.setString(2, first_name);
                ps.setString(3, s_time);
                ps.setString(4, size);
                ps.setString(5, id);

                System.out.println(ps);
                ps.executeUpdate();
                break;
            case "dinner":
                update = "update dinner set last_ name = ?, first_name = ?, time = ?, party_size = ? where id = ?";
                ps = conn.prepareStatement(update);
                ps.setString(1, last_name);
                ps.setString(2, first_name);
                ps.setString(3, s_time);
                ps.setString(4, size);
                ps.setString(5, id);

                System.out.println(ps);
                ps.executeUpdate();
                break;
            default:
                System.out.println("invalid table");
        }

        return "";
    }

    //-----------------------------------------------------------------------------
                //----Delete method----
    public static String delete() throws Exception {

        Scanner keyboard = new Scanner(System.in);
        Csi410_Restaurants res = new Csi410_Restaurants();
        PreparedStatement ps;
        Connection conn = res.get_Connection();
        Statement stmt = conn.createStatement();

        String[] arr = new String[5]; //used to display database records 
        String table;
        String id;
        String check_null = "";
        String check = "";
        int int_rows = 0;
        int columnsCount;

        do {
            System.out.println("Select a table to delete entries from [breakfast, lunch, dinner]\ntype [../] to cancel");
            table = keyboard.nextLine().toLowerCase().trim();

            if (table.equals("../")) {
                return "";
            } else if (!table.equals("breakfast") && !table.equals("lunch") && !table.equals("dinner")) {
                System.out.println("invalid table. Try again");
            }

        } while (!table.equals("breakfast") && !table.equals("lunch") && !table.equals("dinner") && !table.equals("../"));

        do {
            System.out.println("What is the ID of the entry?\ntype [../] to cancel?");
            System.out.print("ID: ");
            id = keyboard.nextLine().toLowerCase().trim();

            if (id.equals("../")) {
                return "";
            } else if (id.matches("^[1-9]*$")) {

                //check if the selected row is a null entry
                try {
                    ResultSet rs = stmt.executeQuery("SELECT * FROM " + table + " where id = " + id);
                    ResultSetMetaData rsmd = rs.getMetaData();
                    columnsCount = rsmd.getColumnCount();
                    while (rs.next()) {
                        for (int i = 0; i < columnsCount; i++) {
                            arr[i] = rs.getString(i + 1);
                        }
                    }
                    check_null = arr[1];
                } catch (SQLException e) {
                    System.out.println("Something bad happened");
                    return "";
                }

                //if the entry was not found
                if (check.equals("0") | check_null.equals("null")) {
                    System.out.println("This entry does not exist. Try again");
                }
            } else {
                System.out.println("Invalid ID. Try again");
            }

        } while (check.equals("0") | check_null.equals("null") | !id.matches("^[1-9]*$"));

        try {
            //get the number of records in the selected table
            ResultSet rs = stmt.executeQuery("select COUNT(*) FROM " + table); //count number of rows in table
            while (rs.next()) {
                for (int i = 0; i < 1; i++) {
                    String temp_rows = rs.getString(i + 1);
                    int_rows = Integer.parseInt(temp_rows);
                }
            }
        } catch (SQLException e) {
            System.out.println("Something bad ");
            return "";
        }
        try {

            //grab the data that is associated with the selected id
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + table + " where id = " + id);
            ResultSetMetaData rsmd = rs.getMetaData();
            columnsCount = rsmd.getColumnCount();

            System.out.println("-----Selected Reservation from[" + table + "]-----");
            System.out.println("ID------------------------------------------------------------------------------------");
            //Print the row that is associated with the selected id
            while (rs.next()) {
                for (int i = 0; i < columnsCount; i++) {
                    String temp = rs.getString(i + 1);
                    arr[i] = temp.trim();
                    System.out.print(arr[i].replace(" ", "").trim() + "\t\t");
                }
                System.out.println();
            }
            if (arr[1].equals("null")) {
                System.out.println("This entry does not exist. Try again");
                id = "null";
            }
        } catch (NumberFormatException | SQLException e) {
            System.out.println("");
        }

        try {
            //confirm delete 
            System.out.println("Are you sure you want to delete this entry? [y/n]");
            System.out.print("Answer: ");
            String command = keyboard.nextLine().toLowerCase().trim();

            switch (command) {
                case "y":
                    String last_row = "";

                    //delete from table with only 2 entries remaining
                    if (int_rows == 2) {
                        //find the last id number in the table
                        ResultSet rs = stmt.executeQuery("select id FROM " + table);
                        while (rs.next()) {
                            for (int i = 0; i < 1; i++) {
                                last_row = rs.getString(i + 1);
                            }
                        }
                        boolean flag = addSeats(id, table);
                        command = "delete from " + table + " where id in (?,?)";
                        ps = conn.prepareStatement(command);
                        ps.setString(1, id);
                        ps.setString(2, last_row);
                        System.out.println(ps);
                        ps.executeUpdate();

                        //add new id after all is removed.
                        command = "insert into " + table + " (id) values (?)";
                        ps = conn.prepareStatement(command);
                        ps.setString(1, "1");
                        System.out.println(ps);
                        ps.executeUpdate();

                        //change null values to "NULL".
                        command = "update " + table + " set last_name = ?, first_name = ?, time = ?, party_size = ? where id = ?";
                        ps = conn.prepareStatement(command);
                        ps.setString(1, "null");
                        ps.setString(2, "null");
                        ps.setString(3, "null");
                        ps.setString(4, "null");
                        ps.setString(5, "1");
                        System.out.println(ps);
                        ps.executeUpdate();

                    } //delete from table with more than 2 entries remaining
                    else if (int_rows > 2) {
                        boolean flag = addSeats(id, table);
                        command = "delete from " + table + " where id = ?";
                        ps = conn.prepareStatement(command);
                        ps.setString(1, id);
                        System.out.println(ps);
                        ps.executeUpdate();
                    } else //trying to delete from table with only 1 entry remaining
                    {
                        System.out.println("Entry does not exist");
                    }
                    break;
                case "n":
                    break;
                default:
                    System.out.println("Invalid entry");
                    break;
            }
        } catch (SQLException e) {
            System.out.println("Something bad happened");
            return "";
        }
        return "";
    }
}
