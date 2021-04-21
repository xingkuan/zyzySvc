package org.lee.InfobotREST;


import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Request;
//import javax.ws.rs.core.Response;

import org.apache.http.HttpHost;
import org.apache.http.RequestLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
//import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.Header;
import org.json.simple.JSONObject;


@Path("/")
public class es {
    RestClient restClient;
    postgres ps;
    
    public es() {
    	restClient = RestClient.builder(
    	    new HttpHost("localhost", 9200, "http"),
    	    new HttpHost("localhost", 9201, "http")).build();
    	ps = new postgres();
    }
    
	@GET
	@Path("/esSearchHi/{text}")
	//@Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public javax.ws.rs.core.Response search(@PathParam("text") String text ) throws IOException {
		System.out.print("ES search" + text);
		ps.logSearchTerm(text);
		
		//restClient = RestClient.builder(
	    //	    new HttpHost("localhost", 9200, "http"),
	    //	    new HttpHost("localhost", 9201, "http")).build();

		Request request = new Request("POST", "/infobot/_search");
		request.addParameter("pretty", "true");
		request.setEntity(new StringEntity(
		        "{\n"
		        + "   \"query\": {\n"
		        + "      \"match_phrase\": {\n"
		        + "         \"content\": {\n"
		        + "            \"query\": \"" + text + "\",\n"
		        + "            \"slop\":  50 \n"
		        + "         }\n"
		        + "      }\n"
		        + "   },\n"
		        + "  \"highlight\" : {\n"
		        + "    \"pre_tags\" : [\"<i>\"],\n"
		        + "    \"post_tags\" : [\"</i>\"],\n"
		        + "    \"fields\" : {\n"
		        + "      \"content\": {\"fragment_size\" : 150, \"number_of_fragments\" : 3}\n"
		        + "    }\n"
		        + "  }\n"
//		        + " \"fields\": [\"_id\", \"highlight\"], \n"
//		        + " \"_source\": false \n"
		        + "}",
		       ContentType.APPLICATION_JSON));
		
        Response response = restClient.performRequest(request);
        RequestLine requestLine = response.getRequestLine(); 
        HttpHost host = response.getHost(); 
        int statusCode = response.getStatusLine().getStatusCode(); 
        org.apache.http.Header[] headers = response.getHeaders(); 
        String responseBody = EntityUtils.toString(response.getEntity());
		
		//JSONObject jsonObj = new JSONObject();

		//restClient.close();

		//return javax.ws.rs.core.Response.ok(jsonObj.toJSONString()).build();
		return javax.ws.rs.core.Response.ok(responseBody).build();
	}
	
	@GET
	@Path("/esDocByID/{id}")
	//@Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public javax.ws.rs.core.Response searchByID(@PathParam("id") String id ) throws IOException {
		System.out.print("ES get by id: " + id);
		
		Request request = new Request("POST", "/infobot/_search");
		request.addParameter("pretty", "true");
		request.setEntity(new StringEntity(
		        "{\n"
		        + "   \"query\" : {\n"
		        + "     \"match\":{\n"
		        + "        \"_id\": \"" + id + "\"\n"
		        + "     }\n"
		        + "   }\n"
		        + "}",
		       ContentType.APPLICATION_JSON));
		
        Response response = restClient.performRequest(request);
        RequestLine requestLine = response.getRequestLine(); 
        HttpHost host = response.getHost(); 
        int statusCode = response.getStatusLine().getStatusCode(); 
        org.apache.http.Header[] headers = response.getHeaders(); 
        String responseBody = EntityUtils.toString(response.getEntity());
		
		//JSONObject jsonObj = new JSONObject();

		//restClient.close();

		//return javax.ws.rs.core.Response.ok(jsonObj.toJSONString()).build();
		return javax.ws.rs.core.Response.ok(responseBody).build();
	}

	@GET
	@Path("/esSrcsByTitle/{srcTitle}")
	//@Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public javax.ws.rs.core.Response searchSrcByTitle(@PathParam("srcTitle") String title ) throws IOException {
		System.out.print("ES get by title: " + title);
		
		Request request = new Request("POST", "/infobot/_search");
		//request.addParameter("pretty", "true");
		request.setEntity(new StringEntity(
		        "{\n"
		        + "  \"query\": {\n"
		        + "    \"match\": {\n"
		        + "      \"meta\":\"" + title + "\"\n"
		        + "    }\n"
		        + "  },\n"
		        + "  \"sort\" : [\n"
		        + "    { \"seq\" : {\"order\" : \"asc\"}}\n"
		        + "  ],\n"
		        + " \"size\":100 \n"
		        + "}",
		       ContentType.APPLICATION_JSON));
		
        Response response = restClient.performRequest(request);
        RequestLine requestLine = response.getRequestLine(); 
        HttpHost host = response.getHost(); 
        int statusCode = response.getStatusLine().getStatusCode(); 
        org.apache.http.Header[] headers = response.getHeaders(); 
        String responseBody = EntityUtils.toString(response.getEntity());
		
		return javax.ws.rs.core.Response.ok(responseBody).build();
	}
	
	
	void close() throws IOException{
		restClient.close();
	}
	
	public List<JSONObject> searchES(String text) throws IOException {
		Request request = new Request("GET", "/zhongyi/_search");
		request.addParameter("pretty", "true");
        Response response = restClient.performRequest(request);
        RequestLine requestLine = response.getRequestLine(); 
        HttpHost host = response.getHost(); 
        int statusCode = response.getStatusLine().getStatusCode(); 
        org.apache.http.Header[] headers = response.getHeaders(); 
        String responseBody = EntityUtils.toString(response.getEntity());
		return null; 

	}
	public void addDoc(List<JSONObject> items) {
		  items.stream().forEach(e-> {
		    Request request = new Request("PUT", 
		            String.format("/zhongyi/_doc/%d",1));
		    try {
		        request.setEntity(new StringEntity(
		                "{\"json\":\"text\"}",
		                ContentType.APPLICATION_JSON));
		        restClient.performRequest(request);
		      } catch (IOException ex) {
		        
		      }
		  });
		}
	
	
}
