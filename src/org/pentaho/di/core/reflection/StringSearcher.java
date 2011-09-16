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
package org.pentaho.di.core.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;


public class StringSearcher
{
	private static final String LOCAL_PACKAGE = "org.pentaho.di";

	private static final String[] JAVA_PACKAGES = new String[] { "java.util", };

	
	private static List<String> stepPluginPackages;
	private static List<String> jobEntryPluginPackages;
	
    public static final void findMetaData(Object object, int level, List<StringSearchResult> stringList, Object parentObject, Object grandParentObject)
    {
        // System.out.println(Const.rightPad(" ", level)+"Finding strings in "+object.toString());
        
        if (level>5) return;
        
        PluginRegistry registry = PluginRegistry.getInstance();
        
        if (stepPluginPackages==null) 
        {
        	stepPluginPackages = registry.getPluginPackages(StepPluginType.class);  
        }
        if (jobEntryPluginPackages==null) 
        {
        	jobEntryPluginPackages = registry.getPluginPackages(JobEntryPluginType.class);  
        }
        
        Class<? extends Object> baseClass = object.getClass();
        Field[] fields = baseClass.getDeclaredFields();
        for (int i=0;i<fields.length;i++)
        {
            Field field = fields[i];
            
            boolean processThisOne = true;
            
            if ( (field.getModifiers()&Modifier.FINAL ) > 0) processThisOne=false;
            if ( (field.getModifiers()&Modifier.STATIC) > 0) processThisOne=false;

        	// Investigate only if we're dealing with a sanctioned package.
            // A sanctioned package is either the local package (org.pentaho.di) or
            // a package of one of the plugins.
            //
            boolean sanctionedPackage = false;
            if (field.toString().indexOf(LOCAL_PACKAGE)>=0) sanctionedPackage=true;
            for (int x=0;x<JAVA_PACKAGES.length && !sanctionedPackage;x++)
            {
            	if (field.toString().indexOf(JAVA_PACKAGES[x])>=0)
            	{
            		sanctionedPackage=true;
            	}
            }
            for (int x=0;x<stepPluginPackages.size() && !sanctionedPackage;x++)
            {
            	if (field.toString().indexOf(stepPluginPackages.get(x))>=0) sanctionedPackage=true;
            }
            for (int x=0;x<jobEntryPluginPackages.size() && !sanctionedPackage;x++)
            {
            	if (field.toString().indexOf(jobEntryPluginPackages.get(x))>=0) sanctionedPackage=true;
            }
            if ( !sanctionedPackage ) processThisOne=false; // Stay in the sanctioned code-base.
            
            // Dig into the metadata from here...
            //
            if (processThisOne)
            {
                try
                {
                    Object obj = field.get(object);
                    if (obj!=null)
                    {
                    	stringSearchInObject(obj, level, stringList, parentObject, grandParentObject, field);
                    }
                }
                catch(IllegalAccessException e)
                {
                // OK, it's private, let's see if we can go there later on using
                // getters and setters...
                // fileName becomes: getFileName();
                // OK, how do we get the value now?
                try {
      
                  Method method = findMethod(baseClass, field.getName());
                  if (method != null) {
                    // String fullMethod =
                    // baseClass.getName()+"."+method.getName()+"()";
      
                    Object string = method.invoke(object, (Object[]) null);
                    if (string != null) {
                      stringSearchInObject(string, level, stringList, parentObject, grandParentObject, field);
                    }
                  }
                } catch (Throwable ex) {
                  // Ignore this error silently. If we can't access the method there
                  // is nothing you can do about it.
                }
              }
            }
        }        
    }

	private static void stringSearchInObject(Object obj, int level, List<StringSearchResult> stringList, Object parentObject, Object grandParentObject, Field field) {
        if (obj instanceof String)
        {
            // OK, let's add the String
            stringList.add(new StringSearchResult((String)obj, parentObject, grandParentObject, field.getName()));                       
        }
        else
        if (obj instanceof String[])
        {
            String[] array = (String[])obj;
            for (int x=0;x<array.length;x++) 
            {
                if (array[x]!=null)
                {
                    stringList.add(new StringSearchResult(array[x], parentObject, grandParentObject, field.getName()+" #"+(x+1)));
                }
            }
        }
        else
        if (obj instanceof Boolean)
        {
            // OK, let's add the String
            stringList.add(new StringSearchResult(((Boolean)obj).toString(), parentObject, grandParentObject, field.getName()+" (Boolean)"));                       
        }
        else
        if (obj instanceof Condition)
        {
        	stringList.add(new StringSearchResult(((Condition)obj).toString(), parentObject, grandParentObject, field.getName()+" (Condition)"));
        }
        else
        if (obj instanceof DatabaseInterface)
        {
        	// Make sure we read the attributes.  This is not picked up by default. (getDeclaredFields doesn't pick up inherited fields)
        	//
        	DatabaseInterface databaseInterface = (DatabaseInterface) obj;
        	findMapMetaData(databaseInterface.getAttributes(), level+1, stringList, parentObject, grandParentObject, field);
        	findMetaData(obj, level+1, stringList, parentObject, grandParentObject);
        }
        else
        if (obj instanceof Map)
        {
        	findMapMetaData((Map<?,?>)obj, level, stringList, parentObject, grandParentObject, field);
        }
        else
        if (obj instanceof Object[])
        {
            for (int j=0;j<((Object[])obj).length;j++) findMetaData( ((Object[])obj)[j], level+1, stringList, parentObject, grandParentObject);
        }
        else 
        {
            findMetaData(obj, level+1, stringList, parentObject, grandParentObject);
        }
	}

	private static void findMapMetaData(Map<?,?> map, int level, List<StringSearchResult> stringList, Object parentObject, Object grandParentObject, Field field) {
		
    	for (Object key : map.keySet())
    	{
    		Object value = map.get( key );
    		if (key!=null)
    		{
    			stringList.add(new StringSearchResult(key.toString(), parentObject, grandParentObject, field.getName()+" (Map key)"));
    		}
    		if (value!=null)
    		{
    			stringList.add(new StringSearchResult(value.toString(), parentObject, grandParentObject, field.getName()+" (Map value)"));
    		}
    	}
	}

	private static Method findMethod(Class<? extends Object> baseClass, String name)
    {
        // baseClass.getMethod(methodName[m], null);
        Method[] methods = baseClass.getDeclaredMethods();
        Method method = null;

        // getName()
        if (method==null)
        {
            String getter = constructGetter(name);
            method = searchGetter(getter, baseClass, methods);
        }

        // isName()
        if (method==null)
        {
            String getter = constructIsGetter(name);
            method = searchGetter(getter, baseClass, methods);
        }

        // name()
        if (method==null)
        {
            String getter = name;
            method = searchGetter(getter, baseClass, methods);
        }
        
        return method;

    }


  
    private static Method searchGetter(String getter, Class<?> baseClass, Method[] methods)
    {
        Method method =null;
        try
        {
            method=baseClass.getMethod(getter);

        }
        catch(Exception e)
        {
            // Nope try case insensitive.
            for (int i=0;i<methods.length;i++)
            {
                String methodName = methods[i].getName(); 
                if (methodName.equalsIgnoreCase(getter))
                {
                    return methods[i];
                }
            }
            
        }
        
        return method;
    }

    public static final String constructGetter(String name)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("get");
        buf.append(name.substring(0,1).toUpperCase());
        buf.append(name.substring(1));
        
        return buf.toString();
    }
    
    public static final String constructIsGetter(String name)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("is");
        buf.append(name.substring(0,1).toUpperCase());
        buf.append(name.substring(1));
        
        return buf.toString();
    }
    
}

