package org.lee.InfobotREST;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import static org.neo4j.driver.Values.parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

@Path("/")
public class neo4j {
	private final Driver driver = GraphDatabase.driver( 
			"bolt://localhost:7687", 
			AuthTokens.basic( "neo4j", "lisa" ) );
	//https://neo4j.com/docs/api/java-driver/current/
	@GET
	@Path("/graphSearch/{text}")
	//@Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response search(@PathParam("text") String text ) {
		System.out.println( "got a request: " + text );
		Map<Long, Map> nodes = new HashMap();
		Map<Long, Map> links = new HashMap();
		
		JSONArray nodeJSONArray = new JSONArray();
		JSONArray linkJSONArray = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		Map a = null;
	//    List<Map> list = new ArrayList<Map>();
	      
		try ( Session session = driver.session() ) {
			Result ne4jResult = session.run( "MATCH p=(e1)-[r:RELATIONSHIP]->(e2) "
                		//	+ "where e1.name starts with $message "
                			+ "RETURN p LIMIT 5",
                            parameters( "message", text ) );

              while (ne4jResult.hasNext())
              {
                  Record record = ne4jResult.next();
                   org.neo4j.driver.types.Path t = record.get(0).asPath();
                  Iterable<Node> k = t.nodes();
                  Iterable<Relationship> v = t.relationships();
                  for( Node i : k) { 
                	  //System.out.println(i.id());
                	  //System.out.println(i.labels());
                	  //System.out.println(i.asMap());
                	  if(!nodes.containsKey(i.id())) {
                		  a = new HashMap<>();
                    	  a.put("id", i.id());
                    	  a.put("label", ((ArrayList) i.labels()).get(0));
                    	  //i.asMap().forEach((key, value) -> a.merge( key, value)
                    		//	);
                    	  a.putAll(i.asMap());
                		  nodes.put(i.id(), a);
                	  //System.out.println(i.labels());
                	  //System.out.println(i.values());
                	  }
                  }
                  for( Relationship i : v) {
                	  //System.out.println(i.id());
                	  //System.out.println(i.type());
                	  //System.out.println(i.startNodeId());
                	  //System.out.println(i.endNodeId());
                	  //System.out.println(i.asMap());
                	  //System.out.println(i.values());
                	  if(!links.containsKey(i.id())) {
                		  a = new HashMap<>();
                		a.put("id", i.id());
                	  	a.put("Type", i.type());
                	  	a.put("source", i.startNodeId());
                	  	a.put("target", i.endNodeId());
              		  a.putAll(i.asMap());
               		  links.put(i.id(), a);}
                  }
                  //Value t = record.get(0);
                //List<Object> t = record.get(0).asList();
                  System.out.println(t);
                  // Values can be extracted from a record by index or name.
              }
              
              nodes.forEach((k, v) -> 
              	nodeJSONArray.add(v)
              );
              //links.forEach((k, v) -> System.out.println((k + ":" + v)));
              links.forEach((k, v) -> 
            	linkJSONArray.add(v)
            );
              jsonObj.put("nodes", nodeJSONArray);
              jsonObj.put("links", linkJSONArray);
             
		}
		//return jsonObj;
		return Response.ok(jsonObj.toJSONString()).build();
	}	
	
	@GET
	@Path("/test")
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		System.out.println( "got a request" );
		return "result";
	}	
}

