package org.lee.InfobotREST;

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
public class postgres {
	private static final Logger logger = Logger.getLogger(Notes.class);
	String url = "jdbc:postgresql://localhost:5432/infostg";
	String user = "postgres";
	String passwd = "post";

	private Connection conn = null;
	private Statement stmt = null;

	public postgres(/*@Context UriInfo uriInfo,
            @PathParam("num") int pathNum*/) {
		logger.info("create postgres object...");
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
	
	public void finalize(){  //just want to see GC in action.
		logger.info("after postgres object GC ...");
	}

	@GET
	@Path("getStgContentByID/{id}/{seq}/{ver}")
	//@Path("getByID/{id}")
	//@Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSTGByID(@PathParam("id") int sid, 
			@PathParam("seq") long seq,
			@PathParam("ver") int ver) {
		//System.out.println("to getSTGByID: " + sid + ", " + seq + "," + ver);
		System.out.println("to getSTGByID: " + sid + ", " );
		//String rslt = getNoteBySeq(id, ver);
		//String sql = "select name, val,template_name,meta from node where seq = " + id +" and ver="+ver;
//TODO 2021.03.08
		String sql = "select src_id, seq, name, version, meta, content from info_stg "
				+ "where src_id= " + sid
				+ " and seq = " + seq   //1010000 
				+ " and version = " + ver
				;
		String rslt = sqlToJsonArrayString(sql);
		
		//return rslt;
		System.out.println(rslt);
		return Response.ok(rslt).build();
	}

	@POST
	@Path("srcSaveNewVersion")
	@Produces(MediaType.APPLICATION_JSON)
	public Response srcSaveNewVersion(InputStream incomingData) {
		System.out.println("crtSrcText() ");
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

			JSONObject jsonSrc = (JSONObject) jsonObj.get("src");
			
			String sname, tname, template;
			// save source
			sname =(String)jsonSrc.get("name");
			System.out.println(sname);
			//template = (String) jsonSrc.get("template");
			int srcId=Integer.parseInt((String) jsonSrc.get("srcID"));
			long seq = Long.parseLong( (String) jsonSrc.get("seq"));
			String a=jsonSrc.get("meta").toString();
			String b=jsonSrc.get("srcContent").toString();
			saveSrcStgAsNewVer(srcId, seq, sname, a, b);

//			return Response.created(URI.create("/note_quill.html")).build();
			return Response.ok("done!", "text/plain").build();
			// return true;
		}  catch(ParseException pe){
	         return Response.status(Response.Status.PRECONDITION_FAILED).build();
	      }catch (IOException e) {
			e.printStackTrace();
			return Response.status(Response.Status.PRECONDITION_FAILED).build();
		}
	}

	@POST
	@Path("srcReplaceVersion0")
	@Produces(MediaType.APPLICATION_JSON)
	public Response srcReplaceVersion0(InputStream incomingData) {
		System.out.println("crtSrcText() ");
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

			JSONObject jsonSrc = (JSONObject) jsonObj.get("src");
			
			String sname, tname, template;
			// save source
			sname =(String)jsonSrc.get("name");
			System.out.println(sname);
			//template = (String) jsonSrc.get("template");
			int srcId=Integer.parseInt((String) jsonSrc.get("srcID"));
			long seq = Long.parseLong( (String) jsonSrc.get("seq"));
			String a=jsonSrc.get("meta").toString();
			String b=jsonSrc.get("srcContent").toString();
			saveSrcStgReplaceVer0(srcId, seq, sname, a, b);

//			return Response.created(URI.create("/note_quill.html")).build();
			return Response.ok("done!", "text/plain").build();
			// return true;
		}  catch(ParseException pe){
	         return Response.status(Response.Status.PRECONDITION_FAILED).build();
	      }catch (IOException e) {
			e.printStackTrace();
			return Response.status(Response.Status.PRECONDITION_FAILED).build();
		}
	}
	

	@GET
	//@Path("getSeqPrev/{sid}/{seq}")
	@Path("getSeqPrev")
	//@Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSrcPrev(@QueryParam("sid") int sid,
			@QueryParam("seq") long seq) {
		String sql = "select max(seq) prevSeq from info_stg "
				+ "where src_id= " + sid
				+ " and version=0" 
				+ " and seq< " + seq;
		String rslt = sqlToJsonArrayString(sql);
		
		//return rslt;
		System.out.println(rslt);
		return Response.ok(rslt).build();
	}
	@GET
	//@Path("getSeqNext/{sid}/{seq}")
	@Path("getSeqNext")
	//@Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSrcNext(@QueryParam("sid") int sid,
			@QueryParam("seq") long seq) {
		String sql = "select min(seq) nextSeq from info_stg "
				+ "where src_id= " + sid
				+ " and version=0" 
				+ " and seq>" + seq;
		String rslt = sqlToJsonArrayString(sql);
		
		//return rslt;
		System.out.println(sql);
		System.out.println(rslt);
		return Response.ok(rslt).build();
	}

	
	@GET
	@Path("getSTGRegTEXT/{id}")
	//@Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSTGRegExTEXT(@PathParam("id") int sid) {
		String sql = "select regex_text from info_src "
				+ "where id= " + sid
				;
		String rslt = sqlToJsonArrayString(sql);
		
		//return rslt;
		System.out.println(sql);
		System.out.println(rslt);
		return Response.ok(rslt).build();
	}

	@GET
	@Path("getSTGRegHTML/{id}")
	//@Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSTGRegExHTML(@PathParam("id") int sid) {
		String sql = "select regex_html from info_src "
				+ "where id= " + sid
				;
		String rslt = sqlToJsonArrayString(sql);
		
		//return rslt;
		System.out.println(rslt);
		return Response.ok(rslt).build();
	}


	public boolean logSearchTerm(String term) {
		//\c notes
		//CREATE TABLE qry_terms(
	    //   term varchar(255) ,
	    //   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP);

		String sql = "INSERT INTO qry_terms(term) VALUES "
				+"('" + term + "')";
		try {
			stmt.execute(sql);
			conn.commit();
		} catch (SQLException ex) {
			logger.error(ex);
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
	public String getNoteBySeq(int seq, int ver) {
		String sql = "select name, val,template_name,meta from node where seq = " + seq +" and ver="+ver;
		return sqlToJsonArrayString(sql);
	}
	//TODO[2021.03.04]: rename it to addBot ...
	private boolean addNode(String name, String tmplt, String mt, String sb) {
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
	
	private boolean saveSrcStgAsNewVer(int srcId, long seq, String name, String mt, String sb) {
		String sql; 
		try {
			sql = "update info_stg set version = version-1 where seq="+seq+ " and src_id="+srcId;
			stmt.execute(sql);
			sql = "INSERT INTO info_stg(src_id, seq, name, version, meta, content) VALUES "
					+"(" + srcId + "," + seq + ", '" + name + "', 0, '" + mt +"','"+ sb+ "')";
			stmt.execute(sql);
			//conn.commit();
		} catch (SQLException ex) {
			// insert into duplicate table
			System.out.println(
				"Caught SQLException " + ex.getErrorCode() + "/" + ex.getSQLState() + " " + ex.getMessage());
		} 
		return true;
	}
	private boolean saveSrcStgReplaceVer0(int srcId, long seq, String name, String mt, String sb) {
		String sql; 
		try {
			sql = "delete from info_stg where seq="+seq+ " and src_id=" + srcId + " and version=0";
			System.out.println(sql);
			stmt.execute(sql);
			sql = "INSERT INTO info_stg(src_id, seq, name, version, meta, content) VALUES "
					+"(" + srcId + "," + seq + ", '" + name + "', 0, '" + mt +"','"+ sb+ "')";
			System.out.println(sql);
			stmt.execute(sql);
			System.out.println(sb);
		
			//conn.commit();
		} catch (SQLException ex) {
			// insert into duplicate table
			System.out.println(
				"Caught SQLException " + ex.getErrorCode() + "/" + ex.getSQLState() + " " + ex.getMessage());
		} 
		return true;
	}

	@POST
	@Path("/STG/addGeneralNote")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addGeneralNote(InputStream incomingData) {
		// public boolean addNote(InputStream incomingData) {
		// StringBuilder sb = new StringBuilder();
		String sb = "";
		String line = null;
		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
			while ((line = in.readLine()) != null) {
				// sb.append(line);
				sb = sb + "\n" + line;
			}
			System.out.print("add phrase: " + sb);
			// return Response.status(200).entity(inJson.toString()).build();
			// appendToFile("/tmp/test.txt", sb.toString());
			//appendToFile("/tmp/gen.txt", sb);

			//psSQL.addNode(sb);

			return Response.created(URI.create("/note.html")).build();
			// return true;

		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Response.Status.PRECONDITION_FAILED).build();
			// return false;
		}
	}

	private void appendToFile(String oFile, String val) {
		BufferedWriter bw = null;
		try {
			// APPEND MODE SET HERE
			bw = new BufferedWriter(new FileWriter(oFile, true));
			bw.write(val);
			bw.newLine();
			bw.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally { // always close the file
			if (bw != null)
				try {
					bw.close();
				} catch (IOException ioe2) {
					// just ignore it
				}
		} // end try/catch/finally

	} // end test()
	
	/********************/
	@PUT
	@Path("/<add method name here>")
    @Produces(MediaType.TEXT_PLAIN)
	public String putSomething(@FormParam("request") String request ,  @DefaultValue("1") @FormParam("version") int version) {
		if (logger.isDebugEnabled()) {
			logger.debug("Start putSomething");
			logger.debug("data: '" + request + "'");
			logger.debug("version: '" + version + "'");
		}

		String response = null;

        try{			
            switch(version){
	            case 1:
	                if(logger.isDebugEnabled()) logger.debug("in version 1");

	                response = "Response from Jersey Restful Webservice : " + request;
                    break;
                default: throw new Exception("Unsupported version: " + version);
            }
        }
        catch(Exception e){
        	response = e.getMessage().toString();
        }
        
        if(logger.isDebugEnabled()){
            logger.debug("result: '"+response+"'");
            logger.debug("End putSomething");
        }
        return response;	
	}

	@DELETE
	@Path("/<add method name here>")
	public void deleteSomething(@FormParam("request") String request ,  @DefaultValue("1") @FormParam("version") int version) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("Start deleteSomething");
			logger.debug("data: '" + request + "'");
			logger.debug("version: '" + version + "'");
		}

        try{			
            switch(version){
	            case 1:
	                if(logger.isDebugEnabled()) logger.debug("in version 1");

                    break;
                default: throw new Exception("Unsupported version: " + version);
            }
        }
        catch(Exception e){
        	e.printStackTrace();
        }
        
        if(logger.isDebugEnabled()){
            logger.debug("End deleteSomething");
        }
	}

}
