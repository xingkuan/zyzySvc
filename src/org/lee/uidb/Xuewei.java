package org.lee.uidb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.lee.notes.Notes;
import org.lee.notes.NotesDB;

@Path("/")
public class Xuewei {
	private static final Logger logger = Logger.getLogger(Notes.class);
	String url = "jdbc:postgresql://localhost:5432/uidb";
	String user = "postgres";
	String passwd = "post";

	private Connection conn = null;
	private Statement stmt = null;

	public Xuewei(/*@Context UriInfo uriInfo,
            @PathParam("num") int pathNum*/) {
		logger.info("create postgres object...");
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(url, user, passwd);
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void finalize(){  //just want to see GC in action.
		logger.info("after postgres object GC ...");
	}

	@GET
	@Path("getPointByName/{mName}/{jName}/{pName}")
	//@Path("getByID/{id}")
	//@Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPointByName(
			@PathParam("mName") String mname,
			@PathParam("jName") String jname,
			@PathParam("pName") String pname) {
		System.out.println("to getPointByName: " + jname + ", " + pname);
		String sql = "select id,name,line_name,coor,seq,isxw,model_name,array_to_string(sub_lines,',') as sub_lines from points "
				+ "where line_name= '" + jname + "' and "
				+ "name='"+pname+"' and model_name='"+mname+"'"
				;
		String rslt = sqlToJsonArrayString(sql);
		
		//return rslt;
		System.out.println(rslt);
		return Response.ok(rslt).build();
	}
	
/* not needed	
	@GET
	@Path("getPointByID/{id}")
	//@Path("getByID/{id}")
	//@Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPointByID(@PathParam("id") int id) {
		System.out.println("to getPointByID: " + id + ", " );
		String sql = "select * from points "
				+ "where id= " + id
				;
		String rslt = sqlToJsonArrayString(sql);
		
		//return rslt;
		System.out.println(rslt);
		return Response.ok(rslt).build();
	}

	//TODO 20210728: moved to postgres.java 

	//TODO:2021.08.02
	@GET
	@Path("getSubLinesByJL/{modelName}/{jl}")
	//@Path("getByID/{id}")
	//@Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSubLinesByJL(@PathParam("modelName") String mn,
			@PathParam("jl") String jl) {
		//System.out.println("to getPointsByJL: " + jl + ", " );
		String sql = "select distinct unnest(sub_lines) from points "
				+ "where model_name='"+mn+"' and line_name= '" + jl +"' "
				+ "order by seq asc"
				;
		String rslt = sqlToJsonArrayString(sql);
		
		//return rslt;
		//System.out.println(rslt);
		return Response.ok(rslt).build();
	}
*/
	//Only 3D coordinates are stored in JL; the r (moved to postgres.java 
	@GET
	@Path("getPointsOfJL/{modelName}/{jl}")
	//@Path("getByID/{id}")
	//@Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPointsByJL(@PathParam("modelName") String mn,
			@PathParam("jl") String jl) {
		//System.out.println("to getPointsByJL: " + jl + ", " );
		String sql = "select name, unnest(sub_lines) as sLine, seq, coor, facing, isxw from points "
				+ "where model_name='"+mn+"' and line_name= '" + jl +"' "
				+ "order by sLine asc, seq asc"
				;
		String rslt = sqlToJsonArrayString(sql);
		
		//return rslt;
		//System.out.println(rslt);
		return Response.ok(rslt).build();
	}

/*20210803: moved to InfoboREST	
	@GET
	@Path("getJLs")
	//@Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJLs() {
		String sql = "select distinct line_name from points ";
		String rslt = sqlToJsonArrayString(sql);
		
		//return rslt;
		//System.out.println(rslt);
		return Response.ok(rslt).build();
	}
*/
	
	@POST
	@Path("upsertPoint")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updatePoint(InputStream incomingData) {
		System.out.println("updatePoint() ");
		String sb = "";
		String line = null;

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
			while ((line = in.readLine()) != null) {
				sb = sb + line;
			}
			//System.out.print("...: " + sb);
			JSONParser parser = new JSONParser();
			JSONObject jsonObj = (JSONObject)parser.parse(sb);
			
			String mname, lname, name, subNum;
			int seq=0;
			long id;
			JSONObject coor = new JSONObject();

			mname =(String)jsonObj.get("model_name");
			lname =(String)jsonObj.get("line_name");
			name =(String)jsonObj.get("name");
			id =(long) jsonObj.get("id");
			if (jsonObj.get("uiSeq")!=null)
				seq =Integer.parseInt((String)jsonObj.get("uiSeq"));
			subNum =(String)jsonObj.get("subNum");

			//coor.put("x", Double.parseDouble( (String) jsonObj.get("x")));
			//coor.put("y", Double.parseDouble( (String) jsonObj.get("y")));
			//coor.put("z", Double.parseDouble( (String) jsonObj.get("z")));
			coor=(JSONObject) jsonObj.get("coor");
			JSONObject facing=(JSONObject) jsonObj.get("facing");
			
			String sqlStr="insert into points (id, model_name, line_name, name, seq, coor, facing, isxw, sub_lines) values ("
					+ id + ",'"+mname+"', '"+lname+"', '"+name+"', " + seq + ", '" +coor + "', '"+facing+"',false, '{"+subNum+"}') "
					+ "on conflict on constraint points_pkey do "
					+ "update set id="+id+", coor='" + coor +  "', facing='" + facing + "', seq=" +seq + ", sub_lines='{"+subNum+"}'";
			System.out.println(sqlStr);
			runDML(sqlStr);

			return Response.ok("done!", "text/plain").build();
			//return true;
		}  catch(ParseException pe){
	         return Response.status(Response.Status.PRECONDITION_FAILED).build();
	      }catch (IOException e) {
			e.printStackTrace();
			return Response.status(Response.Status.PRECONDITION_FAILED).build();
		}
	}

	@POST
	@Path("updateCoor")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateCoor(InputStream incomingData) {
		String sb = "";
		String line = null;

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
			while ((line = in.readLine()) != null) {
				sb = sb + line;
			}
			System.out.print("...: " + sb);
			JSONParser parser = new JSONParser();
			JSONObject jsonObj = (JSONObject)parser.parse(sb);
			
			String mname, lname, name, subNum;
			int seq=0;
			//JSONObject coor = new JSONObject();
			JSONObject coor;

			mname =(String)jsonObj.get("model_name");
			lname =(String)jsonObj.get("line_name");
			name =(String)jsonObj.get("name");
			coor=(JSONObject) jsonObj.get("coor");
			String sqlStr="update points set coor='" + coor +  "' "
					+ "where model_name='" + mname + "' "
					+ "and line_name='" +lname + "' "
					+ "and name='"+name+"'";
			System.out.print(sqlStr);
			runDML(sqlStr);

			return Response.ok("done!", "text/plain").build();
			//return true;
		}  catch(ParseException pe){
	         return Response.status(Response.Status.PRECONDITION_FAILED).build();
	      }catch (IOException e) {
			e.printStackTrace();
			return Response.status(Response.Status.PRECONDITION_FAILED).build();
		}
	}
	
	public boolean runDML(String sql) {
		try {
			stmt.execute(sql);
			conn.commit();
		} catch (SQLException ex) {
			// insert into duplicate table
			System.out.println(
				"Caught SQLException " + ex.getErrorCode() + "/" + ex.getSQLState() + " " + ex.getMessage());
		} 
		return true;
	}

	private String sqlToJsonArrayString(String sql) {
	    JSONArray jsonArray = new JSONArray();
	   
		try {
			ResultSet rs;
				rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int columns = rs.getMetaData().getColumnCount();
				JSONObject obj = new JSONObject();
	 			for (int i = 0; i < columns; i++)
	 				obj.put(rs.getMetaData().getColumnLabel(i + 1).toLowerCase(),  rs.getObject(i + 1));
	 			
	 			jsonArray.add(obj);
			}
 			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    return jsonArray.toString();
	}
	
}
