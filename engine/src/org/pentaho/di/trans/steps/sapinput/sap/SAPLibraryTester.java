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

public class SAPLibraryTester {
	
	private static final String JCO_LIB_EXISTENCE_TEST_CLASS = "com.sap.conn.jco.JCoDestinationManager";

	private static final String JCO_IMPL_EXISTENCE_TEST_CLASS = "com.sap.conn.rfc.driver.CpicDriver";

	public static boolean isJCoLibAvailable() {
		try {
			Object c = Class.forName(JCO_LIB_EXISTENCE_TEST_CLASS);
			if (c == null)
				return false;
			return true;
		} catch (NoClassDefFoundError e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean isJCoImplAvailable() {
		// we simply skip the test because it does not work for sapjco3 >=3.0.2
		return true;
	}

	
	public static boolean isJCoImplAvailableNotUsed() {
		try {
			Object c = Class.forName(JCO_IMPL_EXISTENCE_TEST_CLASS);
			if (c == null)
				return false;
			return true;
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			return false;
		} catch (NoClassDefFoundError e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}

}
