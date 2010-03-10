package org.pentaho.di.trans.steps.sapinput.sap;

import java.util.Collection;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;

public interface SAPConnection {

	/**
	 * Open a connection to SAP R/3 Note: method for init()
	 * 
	 * 
	 * @param sapConnection
	 *            The SAP Connection to use, needs to be of type SAP R/3
	 * @throws KettleException
	 *             in case something went wrong during the connection phase.
	 */
	void open(DatabaseMeta sapConnection) throws SAPException;

	/**
	 * Close the connection
	 */
	void close();

	// methods for UI
	Collection<SAPFunction> getFunctions(String query) throws SAPException;

	SAPFunction getFunction(String name) throws SAPException;

	SAPFunctionSignature getFunctionSignature(SAPFunction function) throws SAPException;

	// methods for data
	SAPResultSet executeFunction(SAPFunction function,
			Collection<SAPField> input, Collection<SAPField> output) throws SAPException;
}
