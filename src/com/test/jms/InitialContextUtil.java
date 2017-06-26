package com.test.jms;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class InitialContextUtil {

	public final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";
	public final static String URL = "t3://localhost:80";

	public static InitialContext getInitialContext() {
		
		Hashtable<String, String> properties = new Hashtable<String, String>();
		properties.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
		properties.put(Context.PROVIDER_URL, URL);

		try {
			return new InitialContext(properties);
		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
