package org.lee.jingluo;

import java.io.IOException;

import javax.ws.rs.PathParam;

public class Test {

	public static void main(String[] args) throws IOException {
		//JingLuoDB psSQL = new JingLuoDB();
		//String rslt = psSQL.getLine("手厥阴心包经");
		//System.out.println(rslt);
		//rslt = psSQL.getPoint("合谷");
		//System.out.println(rslt);
		
		JLRestSvc svc = new JLRestSvc();
		String rslt = svc.getLine("手厥阴心包经");
		System.out.println(rslt);
		rslt = svc.getPoint("合谷");
		System.out.println(rslt);
	}
}
