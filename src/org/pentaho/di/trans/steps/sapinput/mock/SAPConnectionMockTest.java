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
