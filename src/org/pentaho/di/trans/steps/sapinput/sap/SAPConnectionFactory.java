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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseFactoryInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.sapinput.SapInputMeta;
import org.pentaho.di.trans.steps.sapinput.sap.impl.SAPConnectionImpl;

public class SAPConnectionFactory implements DatabaseFactoryInterface {

	public static SAPConnection create() throws SAPException {
		if (!SAPLibraryTester.isJCoLibAvailable()) {
			String message = BaseMessages.getString(SapInputMeta.class, "SapInputDialog.JCoLibNotFound");
			throw new SAPException(message);
		}
		if (!SAPLibraryTester.isJCoImplAvailable()) {
			String message = BaseMessages.getString(SapInputMeta.class, "SapInputDialog.JCoImplNotFound");
			throw new SAPException(message);
		}
		return new SAPConnectionImpl();
	}

	/**
	 * The SAP connection to test
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
			report.append("Connecting to SAP ERP server [").append(
					databaseMeta.getName()).append(
					"] succeeded without a problem.").append(Const.CR);

		} catch (SAPException e) {
			report.append("Unable to connect to the SAP ERP server: ").append(
					e.getMessage()).append(Const.CR);
			report.append(Const.getStackTracker(e));
		} catch (Throwable e) {
			report.append("Unable to connect to the SAP ERP server: ").append(
					e.getMessage()).append(Const.CR);
			report.append(Const.getStackTracker(e));
		} finally {
			if (sc != null)
				sc.close();
		}

		return report.toString();
	}
}
