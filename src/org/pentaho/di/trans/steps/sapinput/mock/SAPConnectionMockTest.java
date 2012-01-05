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

package org.pentaho.di.trans.steps.sapinput.mock;

import java.util.Collection;
import java.util.Vector;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.SAPR3DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.sapinput.sap.SAPConnection;
import org.pentaho.di.trans.steps.sapinput.sap.SAPException;
import org.pentaho.di.trans.steps.sapinput.sap.SAPField;
import org.pentaho.di.trans.steps.sapinput.sap.SAPFunction;
import org.pentaho.di.trans.steps.sapinput.sap.SAPFunctionSignature;
import org.pentaho.di.trans.steps.sapinput.sap.SAPResultSet;
import org.pentaho.di.trans.steps.sapinput.sap.SAPRow;

public class SAPConnectionMockTest {

	/**
	 * How to use a SAPConnection
	 * @throws KettleException 
	 */
	public static void main(String[] args) throws SAPException {

		// how to obtain a connection
		SAPConnection sc = SAPConnectionFactoryMock.create();

		// how to open a connection
		// @Matt:
		// please show us how to retrieve the connection params from the
		// pentaho environment
		DatabaseMeta cp = new DatabaseMeta("SAP", "SAPR3", "Plugin", "192.168.9.50", null, null, "USER", "PASSWORT");
		cp.getAttributes().setProperty(SAPR3DatabaseMeta.ATTRIBUTE_SAP_SYSTEM_NUMBER, "00");
		cp.getAttributes().setProperty(SAPR3DatabaseMeta.ATTRIBUTE_SAP_CLIENT, "100");
		cp.getAttributes().setProperty(SAPR3DatabaseMeta.ATTRIBUTE_SAP_LANGUAGE, "DE");
		sc.open(cp);

		// how to query all functions
		System.out.println("how to query all functions");
		Collection<SAPFunction> csf1 = sc.getFunctions("");
		for (SAPFunction sapFunction : csf1) {
			System.out.println(sapFunction);
		}
		System.out.println();

		// how to query functions
		System.out.println("how to query functions");
		Collection<SAPFunction> csf2 = sc.getFunctions("1");
		for (SAPFunction sapFunction : csf2) {
			System.out.println(sapFunction);
		}
		System.out.println();

		// how to get a function
		System.out.println("how to get a function");
		SAPFunction sf = sc.getFunction("SearchCustomer");
		System.out.println(sf);
		System.out.println();

		// how to get function signature
		System.out.println("how to get function signature");
		SAPFunctionSignature sfs = sc.getFunctionSignature(sf);
		System.out.println("input:");
		for (SAPField field : sfs.getInput()) {
			System.out.println(field);
		}
		System.out.println("output:");
		for (SAPField field : sfs.getOutput()) {
			System.out.println(field);
		}
		System.out.println();

		// how to execute a function
		System.out.println("how to execute a function");
		Collection<SAPField> input = new Vector<SAPField>();
		input.add(new SAPField("Name", "", "input_single", "Casters"));
		Collection<SAPField> output = new Vector<SAPField>();
		output.add(new SAPField("Name", "", "output_single"));
		output.add(new SAPField("Firstname", "", "output_single"));
		output.add(new SAPField("Adress", "", "output_single"));
		output.add(new SAPField("Zipcode", "", "output_single"));
		output.add(new SAPField("CustomerGroup", "", "output_single"));
		SAPResultSet sfr = sc.executeFunctionUncursored(sf, input, output);
		for (SAPRow row : sfr.getRows()) {
			System.out.println(row);
		}
		System.out.println();

		// Close the connection
		//
		sc.close();
	}

}
