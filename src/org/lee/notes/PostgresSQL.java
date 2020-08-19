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

	private Connection conn = null;
	private Statement stmt = null;

	PostgresSQL() {
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

	public boolean addNode(String name, String tmplt, String mt, String sb) {
		String sql = "INSERT INTO node(name, template_name, meta, val) VALUES "
				+"('" + name + "', '"+ tmplt + "', '" + mt +"','"+ sb+ "')";
		try {
			stmt.execute(sql);
			conn.commit();
		} catch (SQLException ex) {
			// insert into duplicate table
			System.out.println(
				"Caught SQLException " + ex.getErrorCode() + "/" + ex.getSQLState() + " " + ex.getMessage());
			if (ex.getSQLState().equals("23505") ) {
				// Handle Here
				try {
					System.out.println(mt);
					sql = "insert into node_dup select * from node where name='"+name+"'";
					stmt.execute(sql);
					//stmt.executeUpdate("delete from node where name='"+name+"'");
					sql = "update node set val = '"+sb+"', meta= '" + mt + "' where name = '" + name +"'";	
					stmt.execute(sql);
					conn.commit();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} 

		return true;
	}

	// public JSONObject getNote(String name) {
	public String getNote(String name) {
		String sql = "select val,template_name,meta from node where name = '" + name +"'";
		JSONObject json = new JSONObject();

		try {
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				json.put("meta", rs.getString(3));
				json.put("template", rs.getString(2));
				json.put("val", rs.getString(1));
				// return rs.getString(1);
				return json.toString();
				// return json;
			}
		} catch (SQLException ex) {
			// insert into duplicate table
			System.out.println(
					"Caught SQLException " + ex.getErrorCode() + "/" + ex.getSQLState() + " " + ex.getMessage());
		} finally {
		}
		return null;
	}

	public String getNoteBySeq(int seq) {
		String sql = "select name, meta, val from node where seq = " + seq;
		JSONObject json = new JSONObject();

		try {
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				json.put("name", rs.getString(1));
				json.put("meta", rs.getString(2));
				json.put("val", rs.getString(3));
				// return rs.getString(1);
				return json.toString();
				// return json;
			}
		} catch (SQLException ex) {
			// insert into duplicate table
			System.out.println(
				"Caught SQLException " + ex.getErrorCode() + "/" + ex.getSQLState() + " " + ex.getMessage());
		} finally {
		}
		return null;
	}

	
	public boolean addRelation(String sname, String tname, String rel, String info) {
		String sql = "INSERT into relation(s_name, t_name, relation, val) VALUES "
				+ "('"+sname+"', '" + tname + "', '"+rel+",'"+info+"')";

		try {
			stmt.execute(sql);
			conn.commit();
		}catch (SQLException ex) {
			// insert into duplicate table
			System.out.println(
					"Caught SQLException " + ex.getErrorCode() + "/" + ex.getSQLState() + " " + ex.getMessage());
			try {
				sql = "INSERT into relation_dup(s_name, t_name, relation, val) VALUES "
						+ "('"+sname+"', '" + tname + "', '"+rel+",'"+info+"')";
				stmt.execute(sql);
				// conn.commit();

				// also add to node, if not exist yet:
				sql = "INSERT INTO node(name) VALUES('"+sname+"')";
				stmt = conn.prepareStatement(sql);
				sql = "INSERT INTO node(name) VALUES('"+tname+"')";
				stmt = conn.prepareStatement(sql);
				conn.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} 

		return true;
	}

	public String getRelationList() {
		String sql = "select array_to_json(array_agg(t)) from relation_list t";

		try {
			ResultSet rs = stmt.executeQuery(sql);
			rs.next();

			return rs.getObject(1).toString();
		} catch (SQLException ex) {
			// insert into duplicate table
			System.out.println(
					"Caught SQLException " + ex.getErrorCode() + "/" + ex.getSQLState() + " " + ex.getMessage());
		}
		
		return null;
	}

	public String getTemplateList() {
		String sql = "select array_to_json(array_agg(t)) from (select name from template) t";

		try {
			ResultSet rs = stmt.executeQuery(sql);
			rs.next();

			return rs.getObject(1).toString();
		} catch (SQLException ex) {
			// insert into duplicate table
			System.out.println(
					"Caught SQLException " + ex.getErrorCode() + "/" + ex.getSQLState() + " " + ex.getMessage());
		} 

		return null;
	}

	public String getTemplate(String name) {
		String sql = "select template from template where name = '"+name+"'";

		try {
			ResultSet rs = stmt.executeQuery(sql);
			rs.next();

			return rs.getString(1);
		}catch (SQLException ex) {
			// insert into duplicate table
			System.out.println(
					"Caught SQLException " + ex.getErrorCode() + "/" + ex.getSQLState() + " " + ex.getMessage());
		} 

		return null;
	}
	
	// read 百病主治药 and to be displayed in a tree.
	// not an ideal place for that, but what the heck !
	public String readBaiBin() {
		String sql = "select regexp_split_to_array(name, ' \\* ') from node where name like '%百病主治药%' order by seq";
		JSONObject json = new JSONObject();

		try {
			ResultSet rs = stmt.executeQuery(sql);
			
			int mxI=0, l=0, lvl=0, c[];
			String[] pC = new String[]{"","","","","","","","","",""};
			String cC[];
			
			//String bVal="", cVal="";
			//json.put("name", "百病");
			
			String rslt="";
			String test="";
			boolean firstC=true;
			while (rs.next()) {
				cC = (String[])rs.getArray(1).getArray();
				
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
		}catch (SQLException ex) {
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