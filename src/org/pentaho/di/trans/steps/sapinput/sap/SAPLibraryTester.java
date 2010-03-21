package org.pentaho.di.trans.steps.sapinput.sap;

public class SAPLibraryTester {
	
	private static final String JCO_LIB_EXISTENCE_TEST_CLASS = "com.sap.conn.jco.JCoDestinationManager";

	private static final String JCO_IMPL_EXISTENCE_TEST_CLASS = "com.sap.conn.jco.JCo";

	public static boolean isJCoLibAvailable() {
		try {
			Class.forName(JCO_LIB_EXISTENCE_TEST_CLASS);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static boolean isJCoImplAvailable() {
		try {
			Class.forName(JCO_IMPL_EXISTENCE_TEST_CLASS);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

}
