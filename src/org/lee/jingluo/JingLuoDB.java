package org.lee.jingluo;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.sql.ResultSetMetaData;


public class JingLuoDB {
	String url = "jdbc:postgresql://localhost:5432/body";
	String user = "postgres";
	String passwd = "post";

	JingLuoDB() {

	}


	public String getLine(String name) {
		// String query = "select json_extract_path( info,'note') from node where name =
		// ?";
		String query = "select to_json(array_agg(t)) from v2.lines t where pingying=?";
		Connection conn = null;
		PreparedStatement stmt = null;
		JSONObject json = new JSONObject();

		try {
			Class.forName("org.postgresql.Driver"); // otherwise, "can't find suitable driver..."

			conn = DriverManager.getConnection(url, user, passwd);
			stmt = conn.prepareStatement(query);
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
			return rs.getString(1);
			// return json;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException ex) {
			// insert into duplicate table
			System.out.println(
					"Caught SQLException " + ex.getErrorCode() + "/" + ex.getSQLState() + " " + ex.getMessage());
		} finally {
		}
		return null;
	}

	public String getPoint(String name) {
		// String query = "select json_extract_path( info,'note') from node where name =
		// ?";
		String query = "select to_json(array_agg(t)) from v2.points t where name=?";
		Connection conn = null;
		PreparedStatement stmt = null;
		JSONObject json = new JSONObject();

		try {
			Class.forName("org.postgresql.Driver"); // otherwise, "can't find suitable driver..."

			conn = DriverManager.getConnection(url, user, passwd);
			stmt = conn.prepareStatement(query);
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
			return rs.getString(1);
			// return json;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException ex) {
			// insert into duplicate table
			System.out.println(
					"Caught SQLException " + ex.getErrorCode() + "/" + ex.getSQLState() + " " + ex.getMessage());
		} finally {
		}
		return null;
	}

}


