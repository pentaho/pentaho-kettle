/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.ui.spoon;

public class XulMessages implements org.pentaho.xul.Messages
{

	public String getString(String key)
	{
		return Messages.getString(key);
	}

	public String getString(String key, String param1)
	{
		return Messages.getString(key, param1);
	}

	public String getString(String key, String param1, String param2)
	{
		return Messages.getString(key, param1, param2);
	}

	public String getString(String key, String param1, String param2, String param3)
	{
		return Messages.getString(key, param1, param2, param3);
	}

	public String getString(String key, String param1, String param2, String param3, String param4)
	{
		return Messages.getString(key, param1, param2, param3, param4);
	}

	public String getString(String key, String param1, String param2, String param3, String param4, String param5)
	{
		return Messages.getString(key, param1, param2, param3, param4, param5);
	}

	public String getString(String key, String param1, String param2, String param3, String param4, String param5, String param6)
	{
		return Messages.getString(key, param1, param2, param3, param4, param5, param6);
	}
}