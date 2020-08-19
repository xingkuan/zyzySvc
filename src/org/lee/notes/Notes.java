package org.lee.notes;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@Path("/")
public class Notes {
	private static final Logger logger = Logger.getLogger(Notes.class);

	PostgresSQL psSQL = new PostgresSQL();
	
	@POST
	@Path("/addNote")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addNote(InputStream incomingData) {
		String sb = "";
		String line = null;

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
			while ((line = in.readLine()) != null) {
				sb = sb + line;
			}
			System.out.print("add note: " + sb);
			JSONParser parser = new JSONParser();
			JSONObject jsonObj = (JSONObject)parser.parse(sb);

			JSONObject jsonSrc = (JSONObject) jsonObj.get("src");
			
			String sname, tname, template;
			// save source
			sname =(String)jsonSrc.get("name");
			System.out.println(sname);
			template = (String) jsonSrc.get("template");

			psSQL.addNode(sname, template, jsonSrc.get("meta").toString(), jsonSrc.get("note").toString());

			// if come from a parent node, that a relation is included:
			JSONObject objRel = (JSONObject)jsonObj.get("rel");
			if(objRel != null) {
				String strSrc = objRel.get("names").toString();
				String strRel = objRel.get("relation").toString();
				String strTgt = objRel.get("namet").toString();
				System.out.println(strRel);
				psSQL.addRelation(strSrc, strTgt, strRel, null);
			}
			return Response.created(URI.create("/note.html")).build();
			// return true;

		}  catch(ParseException pe){
			
	         return Response.status(Response.Status.PRECONDITION_FAILED).build();
	      }catch (IOException e) {
			e.printStackTrace();
			return Response.status(Response.Status.PRECONDITION_FAILED).build();
		}
	}
	
	
	@GET
	@Path("/note/get/{name}")
	@Produces(MediaType.TEXT_PLAIN)
	//@Produces(MediaType.APPLICATION_JSON)
	public String getNote(@PathParam("name") String msg) {
	//public Response getNote(@PathParam("name") String msg) {
		System.out.println("to get node: " + msg);
		String rslt = psSQL.getNote(msg);
		return rslt;
	}
	
	@GET
	@Path("/note/getByID/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	//@Produces(MediaType.APPLICATION_JSON)
	public String getNoteByID(@PathParam("id") int id) {
		System.out.println("to getNodeByID: " + id);
		String rslt = psSQL.getNoteBySeq(id);
		return rslt;
	}
	
	@POST
	@Path("/addPhrase")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addPhrase(InputStream incomingData) {
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
			//appendToFile("/tmp/terms.txt", sb);
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(sb);
			JSONArray array = (JSONArray)obj;

			System.out.println(array.get(1));
			System.out.println(array.get(2));
			//psSQL.addNode(sb);

			return Response.created(URI.create("/note.html")).build();
			// return true;

		} catch(ParseException pe){
			
	         System.out.println("position: " + pe.getPosition());
	         System.out.println(pe);
	         return Response.status(Response.Status.PRECONDITION_FAILED).build();
	      }catch (IOException e) {
			e.printStackTrace();
			return Response.status(Response.Status.PRECONDITION_FAILED).build();
			// return false;
		}
	}

	@POST
	@Path("/addGeneralNote")
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

	@GET
	@Path("/test/{name}")
	public Response sayHi(@PathParam("name") String msg) {
		String output = "Hello, " + msg + "!";
		return Response.status(200).entity(output).build();
	}

	
	@POST
	@Path("/addRelation")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addRelation(InputStream incomingData) {
		// public boolean addNote(InputStream incomingData) {
		// StringBuffer sb = new StringBuffer();
		String sb = "";
		String line = null;

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
			while ((line = in.readLine()) != null) {
				// sb.append(line);
				sb = sb + line;
			}
			System.out.print("add relation: " + sb);
			// return Response.status(200).entity(inJson.toString()).build();
			//appendToFile("/tmp/test.txt", sb);
			JSONParser parser = new JSONParser();
			JSONObject jsonObj = (JSONObject)parser.parse(sb);

			String name1, name2, relation, meta;

			name1 =(String)jsonObj.get("name1");
			name2 =(String)jsonObj.get("name2");
			relation =(String)jsonObj.get("relation");
			meta =(String)jsonObj.get("meta");
			System.out.println(name1 + " " + relation + " " + name2 + " " + meta);
			psSQL.addRelation(name1, name2, relation, meta);

			return Response.created(URI.create("/note.html")).build();
			// return true;

		}  catch(ParseException pe){
			
	         System.out.println("position: " + pe.getPosition());
	         System.out.println(pe);
	         return Response.status(Response.Status.PRECONDITION_FAILED).build();
	      }catch (IOException e) {
			e.printStackTrace();
			return Response.status(Response.Status.PRECONDITION_FAILED).build();
			// return false;
		}
	}

	
	
	@GET
	@Path("/relation/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	//public Response getRelation(@PathParam("name") String msg) {
	public Response getRelation() {
		String relLst = psSQL.getRelationList();
		return Response.status(200).entity(relLst).build();
	}
	
	@GET
	@Path("/template/getList")
	@Produces(MediaType.APPLICATION_JSON)
	public Response  getTemplateList() {
		String relLst = psSQL.getTemplateList();
		return Response.ok(relLst).build();
		//return relLst;
	}
	@GET
	@Path("/template/get/{name}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getTemplate(@PathParam("name") String msg) {
		System.out.println("to get template: " + msg);
		String rslt = psSQL.getTemplate(msg);
		System.out.println("    : " + rslt);
		//return Response.ok(rslt).build();
		return rslt;
	}
	
	@GET
	@Path("/bbList/getBB")
	@Produces(MediaType.TEXT_PLAIN)
	public String readBB() {
		System.out.println("retrieve BB list as JSON tree in text ");
		String rslt = psSQL.readBaiBin();
		//return Response.status(200).entity(rslt).build();
		return rslt;
	}
	@GET
	@Path("/hbList/getHB")
	@Produces(MediaType.TEXT_PLAIN)
	public String getHB() {
		System.out.println("retrieve BB list as JSON tree in text ");
		String rslt = psSQL.parseHerbNames();
		//return Response.status(200).entity(rslt).build();
		return rslt;
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
