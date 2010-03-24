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

package org.pentaho.di.trans.steps.sapinput.mock;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseFactoryInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.trans.steps.sapinput.sap.SAPConnection;
import org.pentaho.di.trans.steps.sapinput.sap.SAPException;

public class SAPConnectionFactory implements DatabaseFactoryInterface {

	public static SAPConnection create() {
		return new SAPConnectionMock();
	}

	/**
	 * The SAP connection to test, links to the TEST button in the database
	 * dialog.
	 */
	public String getConnectionTestReport(DatabaseMeta databaseMeta)
			throws KettleDatabaseException {

		StringBuffer report = new StringBuffer();

		SAPConnection sc = null;

		try {

			sc = create();

			sc.open(databaseMeta);

			// If the connection was successful
			//
			report.append("Connecting to SAP R/3 server [").append(
					databaseMeta.getName()).append(
					"] succeeded without a problem.").append(Const.CR);

		} catch (SAPException e) {
			report.append("Unable to connect to the SAP R/3 server: ").append(
					e.getMessage()).append(Const.CR);
			report.append(Const.getStackTracker(e));
		} finally {
			sc.close();
		}

		return report.toString();
	}
}
