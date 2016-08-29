//Michael Roytman
//COSI127B: Database Management Systems
//Professor Cherniack
//Programming Assignment 2
//5.1 Task #1 - Visualizing Supplier Sales by Region

import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;

import edu.brandeis.cs127b.pa2.gnuplot.*;
public class Part1 {
    static final String JDBC_DRIVER = "com.postgresql.jdbc.Driver";
    static final String DB_TYPE = "postgresql";
    static final String DB_DRIVER = "jdbc";
    static final String DB_NAME = System.getenv("PGDATABASE");
    static final String DB_HOST = System.getenv("PGHOST");
    static final String DB_URL = String.format("%s:%s://%s/%s",DB_DRIVER, 
        DB_TYPE, DB_HOST, DB_NAME);
    static final String DB_USER = System.getenv("PGUSER");
    static final String DB_PASSWORD = System.getenv("PGPASSWORD");
    static Connection conn;

    //generates each suppliers sales total by region for each month of each year represented in the database
    static final String QUERY = "SELECT r_name, SUM(l_extendedprice * (1+l_tax) * (1-l_discount)), EXTRACT(MONTH from l_shipdate) AS month, "
    + "EXTRACT(YEAR from l_shipdate) AS year FROM region INNER JOIN nation ON r_regionkey = n_regionkey INNER JOIN supplier ON "
    + "n_nationkey = s_nationkey INNER JOIN lineitem ON s_suppkey = l_suppkey GROUP BY r_name, month, year ORDER BY r_name, month, year;";

    public static void main(String[] args) throws SQLException {
    
        conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASSWORD);

        //x-axis, y-axis, and title labels described in the assignment
        final String title = "Monthly TPC-H Order Sales Total by region";
        final String xlabel = "Year";
        final String ylabel = "Order Total (Thousands)";
    
        TimeSeriesPlot plot = new TimeSeriesPlot(title, xlabel, ylabel);
    
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(QUERY);

        //HashMap between the String representation of a region (e.g. Asia) and its associated DateLine
        Map<String, DateLine> regionDateLines = new HashMap<String, DateLine>();
       
        //SimpleDateFormat to create Date objects represented by the pattern "MM/yyyy"
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/yyyy");

        while (rs.next()) {

            String regionName = rs.getString(1);
            DateLine line;

            //gets the DateLine associated with the region or creates a new DateLine for that region if it has not yet
            //been created
            if (regionDateLines.containsKey(regionName)) {
                line = regionDateLines.get(regionName);
            }
            else {
                line = new DateLine(regionName);
                regionDateLines.put(regionName, line);
            }

            //gets the sales for a region; divides by 1000.0 to get in thousands (per the y-axis) 
            double sales = rs.getDouble(2)/1000.0;

            int month = rs.getInt(3);
            int year = rs.getInt(4);
            String date = month + "/" + year;
            java.util.Date dateObject = null;
            
            //creates Date object from SimpleDateFormat
            try {
                dateObject = dateFormatter.parse(date);
            }
            catch (Exception e) {
            
            };
        
            //creates a new DatePoint with the Date and sales
            DatePoint datePoint = new DatePoint(dateObject, sales);

            //adds DatePoint to appropriate DateLine for a region
            line.add(datePoint);
        }

        //adds all the DateLines in the HashMap to the plot    
        for (DateLine line : regionDateLines.values()) {
            plot.add(line);
        }

        System.out.println(plot);
    }
}