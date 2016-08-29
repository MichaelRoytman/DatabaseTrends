
//Michael Roytman
//COSI127B: Database Management Systems
//Professor Cherniack
//Programming Assignment 2
//5.2 Task #2 - Visualizing Trade Relationships by Region

//NOTE: all code except for the query on line 25 and the modification to the initialization of the weight on line 42
//was the existing starter code, so I did not comment extensively on code that was not written by me.

import java.sql.*;
import edu.brandeis.cs127b.pa2.graphviz.*;
public class Part2 {
	static final String JDBC_DRIVER = "com.postgresql.jdbc.Driver";
	static final String DB_TYPE = "postgresql";
	static final String DB_DRIVER = "jdbc";
	static final String DB_NAME = System.getenv("PGDATABASE");
	static final String DB_HOST = System.getenv("PGHOST");
	static final String DB_URL = String.format("%s:%s://%s/%s",DB_DRIVER, DB_TYPE, DB_HOST, DB_NAME);
	static final String DB_USER = System.getenv("PGUSER");
	static final String DB_PASSWORD = System.getenv("PGPASSWORD");

	//query to generate the flow of currency between regions; all other code, except the weight modification on line 39
	//was the existing starter code
	static final String QUERY = "SELECT R1.r_name, R2.r_name, SUM(l_extendedprice * (1+l_tax) * (1-l_discount)) FROM region AS R1" 
	+ " INNER JOIN nation ON R1.r_regionkey = n_regionkey INNER JOIN customer ON n_nationkey = c_nationkey INNER JOIN orders ON c_custkey = o_custkey"
	+ " INNER JOIN lineitem ON o_orderkey = l_orderkey INNER JOIN supplier ON l_suppkey = s_suppkey INNER JOIN nation AS N2 ON"
	+ " s_nationkey = N2.n_nationkey INNER JOIN region AS R2 ON N2.n_regionkey = R2.r_regionkey GROUP BY R1.r_name, R2.r_name;";
    
	public static void main(String[] args) throws SQLException{
		DirectedGraph g = new DirectedGraph();
		try {
			Connection conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASSWORD);
			Statement st = conn.createStatement();
     		ResultSet rs = st.executeQuery(QUERY);
			String fromLabel;
			String toLabel;
			String weight;
			while ( rs.next() ) {
				fromLabel = rs.getString(1).trim();
				toLabel = rs.getString(2).trim();

				//gets the weight as an int from the ResultSet, divides the integer by 1,000,000 (as edges are represented in millions), concatenate
				//to String "$", and concatenates the letter "M" to represent millions
				weight = ("$"+(rs.getInt(3)/1000000)).trim() + "M";
				
				Node from = new Node(fromLabel);
				Node to = new Node(toLabel);
				DirectedEdge e = new DirectedEdge(from, to);
				e.addLabel(weight);
				g.add(e);
			}
			System.out.println(g);
		} catch (SQLException s) {
			throw s;
		}
	}
}