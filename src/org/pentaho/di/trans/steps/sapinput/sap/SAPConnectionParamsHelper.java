package org.pentaho.di.trans.steps.sapinput.sap;

import org.pentaho.di.core.database.DatabaseMeta;

public class SAPConnectionParamsHelper {
	
	public static SAPConnectionParams getFromDatabaseMeta(DatabaseMeta sapConnection) {
		String name = sapConnection.getName();
		String host = sapConnection.getHostname();
		String sysnr = sapConnection.getAttributes().getProperty(
				"SAPSystemNumber");
		String client = sapConnection.getAttributes().getProperty("SAPClient");
		String user = sapConnection.getUsername();
		String password = sapConnection.getPassword();
		String lang = "";
		return new SAPConnectionParams(name, host, sysnr, client, user,
				password, lang);
	}
}
