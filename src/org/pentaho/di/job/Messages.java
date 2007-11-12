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
package org.pentaho.di.job;

import java.util.MissingResourceException;
import org.pentaho.di.i18n.BaseMessages;

public class Messages {
	public static final String packageName = Messages.class.getPackage().getName();
    
    
	public static String getString(String key) {
		try {
			return BaseMessages.getString(packageName, key);
		} catch (MissingResourceException e) {
            return Messages.getString(key);
		}
	}
    
    public static String getString(String key, String param1) {
        try {
            return BaseMessages.getString(packageName, key, param1);
        } catch (MissingResourceException e) {
            return Messages.getString(key, param1);
        }
    }

    public static String getString(String key, String param1, String param2) {
        try {
            return BaseMessages.getString(packageName, key, param1, param2);
        } catch (MissingResourceException e) {
            return Messages.getString(key, param1, param2);
        }
    }

    public static String getString(String key, String param1, String param2, String param3) {
        try {
            return BaseMessages.getString(packageName, key, param1, param2, param3);
        } catch (MissingResourceException e) {
            return Messages.getString(key, param1, param2);
        }
    }
    
    public static String getString(String key, String param1, String param2, String param3,String param4) {
        try {
            return BaseMessages.getString(packageName, key, param1, param2, param3,param4);
        } catch (MissingResourceException e) {
            return Messages.getString(key, param1, param2);
        }
    }
    
    public static String getString(String key, String param1, String param2, String param3,String param4,String param5) {
        try {
            return BaseMessages.getString(packageName, key, param1, param2, param3,param4,param5);
        } catch (MissingResourceException e) {
        
            return Messages.getString(key, param1, param2);
        }
    }

}
