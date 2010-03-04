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

package org.pentaho.di.core.plugins;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;

import org.pentaho.di.i18n.BaseMessages;

public class KettleURLClassLoader extends URLClassLoader
{
	private static Class<?> PKG = KettleURLClassLoader.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private String name;
    
    public KettleURLClassLoader(URL[] url, ClassLoader classLoader)
    {
        super(url, classLoader);
    }
    
    public KettleURLClassLoader(URL[] url, ClassLoader classLoader, String name)
    {
        this(url, classLoader);
        this.name = name;
    }
    
    public String toString()
    {
        return super.toString()+" : "+name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    /*
        Cglib doe's not creates custom class loader (to access package methotds and classes ) it uses reflection to invoke "defineClass", 
        but you can call protected method in subclass without problems:
    */
    public Class<?> loadClass(String name, ProtectionDomain protectionDomain) 
    {
        Class<?> loaded = findLoadedClass(name);
        if (loaded == null)
        {
            // Get the jar, load the bytes from the jar file, construct class from scratch as in snippet below...

            /*
            
            loaded = super.findClass(name);
            
            URL url = super.findResource(newName);
            
            InputStream clis = getResourceAsStream(newName);
            
            */
           
            String newName = name.replace('.','/');
            InputStream is = super.getResourceAsStream(newName);
            byte[] driverBytes = toBytes( is );
            
            loaded = super.defineClass(name, driverBytes, 0, driverBytes.length, protectionDomain);

        }
        return loaded;
    }
    
    private byte[] toBytes(InputStream is)
    {
        byte[] retval = new byte[0];
        try
        {
            int a = is.available();
	        while (a>0)
	        {
	            byte[] buffer = new byte[a];
	            is.read(buffer);
	            
	            byte[] newretval = new byte[retval.length+a];
	            
	            for (int i=0;i<retval.length;i++) newretval[i] = retval[i]; // old part
	            for (int i=0;i<a;i++) newretval[retval.length+i] = buffer[i]; // new part
	            
	            retval = newretval;
	            
	            a = is.available(); // see what's left
	        }
            return retval; 
        }
        catch(Exception e)
        {
            System.out.println(BaseMessages.getString(PKG, "KettleURLClassLoader.Exception.UnableToReadClass")+e.toString()); //$NON-NLS-1$
            return null;
        }
    }
}
