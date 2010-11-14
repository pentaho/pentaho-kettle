/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/

package org.pentaho.di.trans.steps.salesforceinput;

import org.pentaho.di.i18n.BaseMessages;


public class SalesforceConnectionUtils {
	
	public static final int MAX_UPDATED_OBJECTS_IDS= 2000;
	
	private static Class<?> PKG = SalesforceInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	public static final String LIB_VERION="16.0";
	
	public static final String TARGET_DEFAULT_URL= "https://www.salesforce.com/services/Soap/u/16.0";
	
	/**
	 * The records filter description
	 */
	public final static String recordsFilterDesc[] = {
			BaseMessages.getString(PKG, "SalesforceInputMeta.recordsFilter.All"),
			BaseMessages.getString(PKG, "SalesforceInputMeta.recordsFilter.Updated"),
			BaseMessages.getString(PKG, "SalesforceInputMeta.recordsFilter.Deleted")};
	
	/**
	 * The records filter type codes
	 */
	public final static String recordsFilterCode[] = { "all", "updated", "deleted" };

	public final static int RECORDS_FILTER_ALL = 0;

	public final static int RECORDS_FILTER_UPDATED = 1;

	public final static int RECORDS_FILTER_DELETED = 2;
	
	
	public static String getRecordsFilterDesc(int i) {
		if (i < 0 || i >= recordsFilterDesc.length)
			return recordsFilterDesc[0];
		return recordsFilterDesc[i];
	}
	public static int getRecordsFilterByDesc(String tt) {
		if (tt == null)
			return 0;
	
		for (int i = 0; i < recordsFilterDesc.length; i++) {
			if (recordsFilterDesc[i].equalsIgnoreCase(tt))
				return i;
		}
		// If this fails, try to match using the code.
		return getRecordsFilterByCode(tt);
	}

	public static int getRecordsFilterByCode(String tt) {
		if (tt == null)
			return 0;
	
		for (int i = 0; i < recordsFilterCode.length; i++) {
			if (recordsFilterCode[i].equalsIgnoreCase(tt))
				return i;
		}
		return 0;
	}
	public static String getRecordsFilterCode(int i) {
		if (i < 0 || i >= recordsFilterCode.length)
			return recordsFilterCode[0];
		return recordsFilterCode[i];
	}

}
