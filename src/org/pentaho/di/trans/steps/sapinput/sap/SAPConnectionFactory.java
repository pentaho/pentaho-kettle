package org.pentaho.di.trans.steps.sapinput.sap;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseFactoryInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.trans.steps.sapinput.sap.impl.SAPConnectionImpl;

public class SAPConnectionFactory implements DatabaseFactoryInterface {

	public static SAPConnection create() {
		return new SAPConnectionImpl();
	}

	/**
	 * The SAP connection to test
	 */
	public String getConnectionTestReport(DatabaseMeta databaseMeta)
			throws KettleDatabaseException {

		StringBuffer report = new StringBuffer();

		SAPConnection sc = create();

		try {
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
