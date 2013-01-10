/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

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
