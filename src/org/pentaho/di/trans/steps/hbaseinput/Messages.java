/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.di.trans.steps.hbaseinput;

import org.pentaho.di.i18n.BaseMessages;

public class Messages
{
	public static final Class<Messages> PKG = Messages.class;

	public static String getString(String key)
	{
		return BaseMessages.getString(PKG, key);
	}

	public static String getString(String key, String param1)
	{
		return BaseMessages.getString(PKG, key, param1);
	}

	public static String getString(String key, String param1, String param2)
	{
		return BaseMessages.getString(PKG, key, param1, param2);
	}

	public static String getString(String key, String param1, String param2, String param3)
	{
		return BaseMessages.getString(PKG, key, param1, param2, param3);
	}

	public static String getString(String key, String param1, String param2, String param3, String param4)
	{
		return BaseMessages.getString(PKG, key, param1, param2, param3, param4);
	}

	public static String getString(String key, String param1, String param2, String param3, String param4, String param5)
	{
		return BaseMessages.getString(PKG, key, param1, param2, param3, param4, param5);
	}

	public static String getString(String key, String param1, String param2, String param3, String param4, String param5, String param6)
	{
		return BaseMessages.getString(PKG, key, param1, param2, param3, param4, param5, param6);
	}
}