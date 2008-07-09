/*************************************************************************************** 
 * Copyright (C) 2007 Samatar, Brahim.  All rights reserved. 
 * This software was developed by Samatar, Brahim and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar, Brahim.  
 * The Initial Developer is Samatar, Brahim.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/
package org.pentaho.di.trans.steps.ldapinput;

import org.pentaho.di.i18n.BaseMessages;

public class Messages
{
	public static final String packageName = Messages.class.getPackage().getName();

	public static String getString(String key)
	{
		return BaseMessages.getString(packageName, key);
	}

	public static String getString(String key, String param1)
	{
		return BaseMessages.getString(packageName, key, param1);
	}

	public static String getString(String key, String param1, String param2)
	{
		return BaseMessages.getString(packageName, key, param1, param2);
	}

	public static String getString(String key, String param1, String param2, String param3)
	{
		return BaseMessages.getString(packageName, key, param1, param2, param3);
	}

	public static String getString(String key, String param1, String param2, String param3, String param4)
	{
		return BaseMessages.getString(packageName, key, param1, param2, param3, param4);
	}

	public static String getString(String key, String param1, String param2, String param3, String param4, String param5)
	{
		return BaseMessages.getString(packageName, key, param1, param2, param3, param4, param5);
	}

	public static String getString(String key, String param1, String param2, String param3, String param4, String param5, String param6)
	{
		return BaseMessages.getString(packageName, key, param1, param2, param3, param4, param5, param6);
	}
}