/* Copyright (c) 2010 Aschauer EDV GmbH.  All rights reserved. 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * This software was developed by Aschauer EDV GmbH and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 * 
 * Please contact Aschauer EDV GmbH www.aschauer-edv.at if you need additional
 * information or have any questions.
 * 
 * @author  Robert Wintner robert.wintner@aschauer-edv.at
 * @since   PDI 4.0
 */

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
