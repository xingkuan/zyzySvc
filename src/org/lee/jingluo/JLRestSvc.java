package org.lee.jingluo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@Path("/")
//public class NoteServiceImpl implements NoteService{
public class JLRestSvc {
	JingLuoDB psSQL = new JingLuoDB();

	@GET
	@Path("/line/get/{name}")
	@Produces(MediaType.TEXT_PLAIN)
	//@Produces(MediaType.APPLICATION_JSON)
	public String getLine(@PathParam("name") String msg) {
	//public Response getNote(@PathParam("name") String msg) {
		System.out.println("to get line: " + msg);
		String rslt = psSQL.getLine(msg);
		return rslt;
	}
	
	@GET
	@Path("/point/get/{name}")
	@Produces(MediaType.TEXT_PLAIN)
	//@Produces(MediaType.APPLICATION_JSON)
	public String getPoint(@PathParam("name") String msg) {
	//public Response getNote(@PathParam("name") String msg) {
		System.out.println("to get point: " + msg);
		String rslt = psSQL.getPoint(msg);
		return rslt;
	}

}
