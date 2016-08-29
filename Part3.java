//Michael Roytman
//COSI127B: Database Management Systems
//Professor Cherniack
//Programming Assignment 2
//5.3 Task #3 - Generating Purchase Orders

import java.sql.*;
import edu.brandeis.cs127b.pa2.latex.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Scanner;
import java.util.Set;
import java.util.*;

public class Part3 {
    static final String JDBC_DRIVER = "com.postgresql.jdbc.Driver";
    static final String DB_TYPE = "postgresql";
    static final String DB_DRIVER = "jdbc";
    static final String DB_NAME = System.getenv("PGDATABASE");
    static final String DB_HOST = System.getenv("PGHOST");
    static final String DB_URL = String.format("%s:%s://%s/%s", DB_DRIVER,
            DB_TYPE, DB_HOST, DB_NAME);
    static final String DB_USER = System.getenv("PGUSER");
    static final String DB_PASSWORD = System.getenv("PGPASSWORD");
    static Connection conn;

    public static void main(String[] args) throws SQLException {
        conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        Scanner in = new Scanner(System.in);
        Document doc = new Document();
        
        String query;

        //for each order in the file
        while (in.hasNextLine()) {

            String[] arr = in.nextLine().split(":");
            String purchaseNumber = arr[0]; //order number/purchase number (each line in the file)

            //TreeMap from Supplier to a Set of Parts that they sell; changed to TreeMap because HashMap was problematic in that 
            //Suppliers with the same state did not have the same hashcode, so retrieval did not work
            Map<Supplier, Set<Part>> suppliers = new TreeMap<Supplier, Set<Part>>();
            
            Purchase p = new Purchase(purchaseNumber); //new Purchase for the order number

            String[] partsAndAmounts = arr[1].split(","); //an array where each cell represents an order in the format "AxB", where A is the 
                                                          //part quantity and B is the part number

            //for each part number and quantity in the order
            for (int i = 0; i < partsAndAmounts.length; i++) {

                //part quantity and part number
                String quantityAndNumber = partsAndAmounts[i];

                //split over "x" to get a two cell array with part quantity and part number
                String[] quantitiesAndNumbers = quantityAndNumber.split("x");

                int partQuantity = Integer.parseInt(quantitiesAndNumbers[0]);
                String partNumber = quantitiesAndNumbers[1];

                //query that selects the cheapest price (ps_supplycost) from a supplier (s_suppkey) for the given partNumber
                query = "SELECT A1.s_suppkey, A1.ps_supplycost FROM (supplier INNER JOIN partsupp ON s_suppkey = ps_suppkey INNER JOIN part "
                        + "ON p_partkey = ps_partkey) AS A1 WHERE p_partkey = "
                        + partNumber
                        + " AND NOT EXISTS (SELECT * FROM (supplier INNER JOIN partsupp "
                        + "ON s_suppkey = ps_suppkey INNER JOIN part ON p_partkey = ps_partkey) AS A2 WHERE"
                        + " A2.ps_supplycost < A1.ps_supplycost AND A2.p_partkey = "
                        + partNumber + ");";

                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(query);

                while (rs.next()) {

                    String supplierKey = rs.getInt(1) + ""; //supplierKey as a String
                    double supplyCost = rs.getDouble(2);

                    Part part = new Part(partNumber, partQuantity); //constructs Part for the described part
                    part.setCost(supplyCost); //sets cost

                    Supplier supplier = new Supplier(supplierKey); //creates new Supplier
                    Set<Part> parts;

                    //gets the set of parts mapped to by the Supplier or creates a new TreeSet for the supplier
                    //and adds it to the TreeMap
                    if (suppliers.containsKey(supplier)) {
                        parts = suppliers.get(supplier);
                    } 
                    else {
                        parts = new TreeSet<Part>();
                        suppliers.put(supplier, parts);
                    }

                    parts.add(part); //adds Part to the set of Parts
                }

            }

            //for each Supplier in the keys of the TreeMap, create a suborder for them and add all the parts they supply
            //to the suborder; add each suborder to the order, and then add the order to the document
            for (Supplier supp : suppliers.keySet()) {

                Suborder o = new Suborder(supp);

                p.add(o);

                for (Part part : suppliers.get(supp)) {
                    o.add(part);
                }
            }
            doc.add(p);
        }
        System.out.println(doc);
    }
}