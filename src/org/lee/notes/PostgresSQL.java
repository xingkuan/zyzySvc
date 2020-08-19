package org.lee.notes;

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

public class PostgresSQL {
	String url = "jdbc:postgresql://localhost:5432/notes";
	String user = "postgres";
	String passwd = "post";

	PostgresSQL() {

	}

	public boolean addNode(String name, String tmplt, String mt, String sb) {
		// String query = "INSERT INTO node(name, template_name, val) VALUES(?, ?,
		// to_json(?::json))";
		// String queryD = "INSERT INTO node_dup(name, template_name, val) VALUES(?, ?,
		// to_json(?::json))";
		String query = "INSERT INTO node(name, template_name, meta, val) VALUES(?, ?, ?, ?)";
		String queryD = "INSERT INTO node_dup(name, template_name, meta, val) VALUES(?, ?, ?, ?)";
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			Class.forName("org.postgresql.Driver"); // otherwise, "can't find suitable driver..."

			conn = DriverManager.getConnection(url, user, passwd);
			stmt = conn.prepareStatement(query);
			stmt.setString(1, name);
			stmt.setString(2, tmplt);
			stmt.setString(3, mt);
			stmt.setString(4, sb);
			stmt.execute();
			// conn.commit();
			// System.out.print("add to PostGress: \n" + sb );
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException ex) {
			// insert into duplicate table
			System.out.println(
					"Caught SQLException " + ex.getErrorCode() + "/" + ex.getSQLState() + " " + ex.getMessage());
			if (ex.getSQLState().equals("23505") ) {
				// Handle Here
				try {
					System.out.println(mt);
//				conn = DriverManager.getConnection(url, user, passwd);
					Statement stmtx = conn.createStatement();
					stmtx.executeUpdate("insert into node_dup select * from node where name='"+name+"'");
					//stmtx.executeUpdate("delete from node where name='"+name+"'");
				String sqlU = "update node set val = ?, meta= ? where name = ?";	
				    stmt = conn.prepareStatement(sqlU);
					stmt.setString(1, sb);
					stmt.setString(2, mt);
					stmt.setString(3, name);
					stmt.execute();
					// conn.commit();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		} finally {
			try {
				if (stmt != null)
					conn.close();
			} catch (SQLException se) {
			} // do nothing
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		return true;
	}

	// public JSONObject getNote(String name) {
	public String getNote(String name) {
		// String query = "select json_extract_path( info,'note') from node where name =
		// ?";
		String query = "select val,template_name,meta from node where name = ?";
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
				json.put("meta", rs.getString(3));
			json.put("template", rs.getString(2));
			json.put("val", rs.getString(1));
			// return rs.getString(1);
			return json.toString();
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

	public String getNoteBySeq(int seq) {
		String query = "select name, meta, val from node where seq = ?";
		Connection conn = null;
		PreparedStatement stmt = null;
		JSONObject json = new JSONObject();

		try {
			Class.forName("org.postgresql.Driver"); // otherwise, "can't find suitable driver..."

			conn = DriverManager.getConnection(url, user, passwd);
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, seq);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				json.put("name", rs.getString(1));
				json.put("meta", rs.getString(2));
				json.put("val", rs.getString(3));
				// return rs.getString(1);
				return json.toString();
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

	
	public boolean addRelation(String sname, String tname, String rel, String info) {
		// System.out.print("add relation to PostGres: \n" + sb );
		// String query = "INSERT into relation(s_name, t_name, relation, val) VALUES(?,
		// ?, ?, to_json(?::json))";
		// String queryD = "INSERT into relation_dup(s_name, t_name, relation, val)
		// VALUES(?, ?, ?, to_json(?::json))";
		String query = "INSERT into relation(s_name, t_name, relation, val) VALUES(?, ?, ?, ?)";
		String queryD = "INSERT into relation_dup(s_name, t_name, relation, val) VALUES(?, ?, ?, ?)";
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			Class.forName("org.postgresql.Driver"); // otherwise, "can't find suitable driver..."

			conn = DriverManager.getConnection(url, user, passwd);
			stmt = conn.prepareStatement(query);
			stmt.setString(1, sname);
			stmt.setString(2, tname);
			stmt.setString(3, rel);
			stmt.setString(4, info);
			stmt.execute();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException ex) {
			// insert into duplicate table
			System.out.println(
					"Caught SQLException " + ex.getErrorCode() + "/" + ex.getSQLState() + " " + ex.getMessage());
			try {
				conn = DriverManager.getConnection(url, user, passwd);
				stmt = conn.prepareStatement(queryD);
				stmt.setString(1, sname);
				stmt.setString(2, tname);
				stmt.setString(3, rel);
				stmt.setString(4, info);
				stmt.execute();
				// conn.commit();

				// also add to node, if not exist yet:
				String st = "INSERT INTO node(name) VALUES(?)";
				stmt = conn.prepareStatement(st);
				stmt.setString(1, sname);
				stmt.execute();
				stmt.setString(1, tname);
				stmt.execute();

			} catch (SQLException e) {
				e.printStackTrace();
			}
		} finally {
			try {
				if (stmt != null)
					conn.close();
			} catch (SQLException se) {
			} // do nothing
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		return true;
	}

	public String getRelationList() {
		String query = "select array_to_json(array_agg(t)) from relation_list t";
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			Class.forName("org.postgresql.Driver"); // otherwise, "can't find suitable driver..."

			conn = DriverManager.getConnection(url, user, passwd);
			stmt = conn.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();
			rs.next();

			// System.out.println(rs.getObject(1).toString());
			// JSONParser parser = new JSONParser();
			// JSONArray arrayObj = (JSONArray) parser.parse(rs.getObject(1).toString());
			// JSONArray jsonArray = (JSONArray)parser.parse(rs.getObject(1).toString());
			// return jsonArray;
			return rs.getObject(1).toString();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException ex) {
			// insert into duplicate table
			System.out.println(
					"Caught SQLException " + ex.getErrorCode() + "/" + ex.getSQLState() + " " + ex.getMessage());
		} finally {
		}
		return null;
	}

	public String getTemplateList() {
		String query = "select array_to_json(array_agg(t)) from (select name from template) t";
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			Class.forName("org.postgresql.Driver"); // otherwise, "can't find suitable driver..."

			conn = DriverManager.getConnection(url, user, passwd);
			stmt = conn.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();
			rs.next();

			return rs.getObject(1).toString();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException ex) {
			// insert into duplicate table
			System.out.println(
					"Caught SQLException " + ex.getErrorCode() + "/" + ex.getSQLState() + " " + ex.getMessage());
		} finally {
		}
		return "{\"nae\", 1}";
	}

	public String getTemplate(String name) {
		String query = "select template from template where name = ?";
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			Class.forName("org.postgresql.Driver"); // otherwise, "can't find suitable driver..."

			conn = DriverManager.getConnection(url, user, passwd);
			stmt = conn.prepareStatement(query);
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			rs.next();

			return rs.getString(1);
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
	
	// read 百病主治药 and to be displayed in a tree.
	// not an ideal place for that, but what the heck !
	public String readBaiBin() {
		String query = "select regexp_split_to_array(name, ' \\* ') from node where name like '%百病主治药%' order by seq";
		Connection conn = null;
		PreparedStatement stmt = null;
		JSONObject json = new JSONObject();

		String url = "jdbc:postgresql://localhost:5432/notes";
		String user = "postgres";
		String passwd = "post";
		try {
			Class.forName("org.postgresql.Driver"); // otherwise, "can't find suitable driver..."

			conn = DriverManager.getConnection(url, user, passwd);
			stmt = conn.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();
			
			int mxI=0, l=0, lvl=0, c[];
			String[] pC = new String[]{"","","","","","","","","",""};
			String cC[];
			
			//String bVal="", cVal="";
			//json.put("name", "百病");
			
			String rslt="";
			String test="";
			boolean firstC=true;
			while (rs.next()) {
//				System.out.println(l);
				cC = (String[])rs.getArray(1).getArray();
				
			/*	//test
				for (int i=3; i < cC.length; i++ ) {
					test=test+cC[i]+" ";
				}
				System.out.println(test);
*/
				//devl
				if(l==0) {
					for (int i=3; i < cC.length - 1; i++ ) {
							rslt = rslt + "{\"name\": \"" + cC[i] + "\", \"children\": [";
							pC[i] = cC[i];
					}
					firstC=false;
					mxI = cC.length - 1;
					rslt = rslt + "{\"name\": \"" + cC[mxI] + "\"";
					pC[mxI] = cC[mxI];
				}else{
					if(cC.length ==4) {   //changed on the first field, and the only field
						for(int j=0; j<mxI - 3; j++) {
							rslt = rslt + "}";
						}
						rslt = rslt + "] }, {\"name\": \"" + cC[3] + "\"";
						firstC=true;
						mxI=3;
					} else {
					for (int i=3; i < cC.length - 1; i++ ) {
						if (  cC[i].equals(pC[i])) {
							lvl++;
						}else { 
							if(i==mxI){
								rslt = rslt + "} , {\"name\": \"" + cC[i] + "\", \"children\": [";
								pC[i] = cC[i];
							}else {
								for(int j=0; j<mxI - i; j++) {
									rslt = rslt + "}";
								}
								rslt = rslt + "] }, {\"name\": \"" + cC[i] + "\", \"children\": [";
								firstC=true;
							}
						}
						pC[i] = cC[i];
					}
					mxI = cC.length - 1;
					if (firstC) {
						rslt = rslt + " {\"name\": \"" + cC[mxI] + "\"";
						firstC=false;
					} else {
						rslt = rslt + "}, {\"name\": \"" + cC[mxI] + "\"";
					}
					pC[mxI] = cC[mxI];
					}
				}
				rslt = rslt+'\n';
				
				test="";
				l++;
				lvl=0;
				//System.out.println(Arrays.toString(cC));	
			}
			
			//System.out.println(mxI);
			for (int i=3; i<mxI; i++)
				rslt = rslt + "}]}";    //may not that simple !!!
			
			rslt="{\"name\": \"诸病\", \"children\": [" + rslt + "]}";
			System.out.println(rslt);
			return rslt;
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
	
	public String parseHerbNames() {
		String query = "select name, count(1), string_agg(distinct cat, ',') from (select distinct name[4] ||'|'||name[5] cat, trim(both unnest(regexp_split_to_array(val, ':|,'))) as name from (select regexp_split_to_array(name, ' \\* ') as name,unnest(REGEXP_MATCHES(val,'<span class=\"sec-title\">(.*?)</span>', 'g')) val from node where name like '%百病主治药%' ) as t ) as p where name !='' group by name ";
		Connection conn = null;
		PreparedStatement stmt = null;
		JSONObject json = new JSONObject();

		String url = "jdbc:postgresql://localhost:5432/notes";
		String user = "postgres";
		String passwd = "post";
		try {
			Class.forName("org.postgresql.Driver"); // otherwise, "can't find suitable driver..."

			conn = DriverManager.getConnection(url, user, passwd);
			stmt = conn.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();

			String rslt="[";
			while (rs.next()) {
				rslt = rslt + 
						"{\"name\": \"" + rs.getString(1) + "\", \"cnt\": " + rs.getInt(2) + ", \"cats\": \"" + rs.getString(3) + "\"},\n";
			}
			rslt=rslt.replaceAll(",$", "]");
			System.out.println(rslt);
			return rslt;
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