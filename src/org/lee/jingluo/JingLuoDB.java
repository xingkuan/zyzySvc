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

	private Connection conn;
	private Statement stmt;
	
	JingLuoDB() {
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(url, user, passwd);
			stmt = conn.createStatement();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String sqlToJSONString(String sql) {
		try {
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next())
			return rs.getString(1);
			// return json;
		} catch (SQLException ex) {
			// insert into duplicate table
			System.out.println(
					"Caught SQLException " + ex.getErrorCode() + "/" + ex.getSQLState() + " " + ex.getMessage());
		} finally {
		}
		return null;
	}
	public String getLine(String name) {
		String sql = "select to_json(array_agg(t)) from v2.lines t "
				+ " where pingying='"+name+"'";
		return sqlToJSONString(sql);
	}

	public String getPoint(String name) {
		String sql = "select to_json(array_agg(t)) from v2.points t "
				+ " where name='"+name+"'";
		return sqlToJSONString(sql);
	}
}


