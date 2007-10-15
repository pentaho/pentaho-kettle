package org.pentaho.di.core.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogWriter;


public class StringSearcher
{
    public static final void findMetaData(Object object, int level, List<StringSearchResult> stringList, Object parentObject, Object grandParentObject)
    {
        // System.out.println(Const.rightPad(" ", level)+"Finding strings in "+object.toString());
        
        if (level>5) return;
        
        Class<? extends Object> baseClass = object.getClass();
        Field[] fields = baseClass.getDeclaredFields();
        for (int i=0;i<fields.length;i++)
        {
            Field field = fields[i];
            
            boolean processThisOne = true;
            
            if ( (field.getModifiers()&Modifier.FINAL ) > 0) processThisOne=false;
            if ( (field.getModifiers()&Modifier.STATIC) > 0) processThisOne=false;
            if ( field.toString().indexOf("org.pentaho.di")<0 ) processThisOne=false; // Stay in this code-base.
            
            if (processThisOne)
            {
                try
                {
                    Object obj = field.get(object);
                    if (obj!=null)
                    {
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
                        if (obj instanceof Object[])
                        {
                            for (int j=0;j<((Object[])obj).length;j++) findMetaData( ((Object[])obj)[j], level+1, stringList, parentObject, grandParentObject);
                        }
                        else {
                            	findMetaData(obj, level+1, stringList, parentObject, grandParentObject);
                        }
                    }
                }
                catch(IllegalAccessException e)
                {
                    // OK, it's private, let's see if we can go there later on using getters and setters...
                    // fileName becomes: getFileName();
                     
                    Method method = findMethod(baseClass, field.getName());
                    if (method!=null)
                    {
                        String fullMethod = baseClass.getName()+"."+method.getName()+"()";
                            
                        // OK, how do we get the value now?
                        try
                        {
                            // System.out.println(Const.rightPad(" ", level)+"  Invoking method: "+fullMethod+", on object: "+object.toString());
                            Object string = method.invoke(object, (Object[])null);
                            if (string!=null)
                            {
                                if (string instanceof String)
                                {
                                    stringList.add(new StringSearchResult((String)string, parentObject, grandParentObject, field.getName()));
                                    // System.out.println(Const.rightPad(" ", level)+"  "+field.getName()+" : method "+fullMethod+" --> "+((String)string));
                                }
                                else
                                if (string instanceof String[])
                                {
                                    String[] array = (String[])string;
                                    for (int x=0;x<array.length;x++) 
                                    {
                                        if (array[x]!=null)
                                        {
                                            stringList.add(new StringSearchResult(array[x], parentObject, grandParentObject, field.getName()+" #"+(x+1)));
                                            /// System.out.println(Const.rightPad(" ", level)+"  "+field.getName()+" : method "+fullMethod+" --> String #"+x+" = "+array[x]);
                                        }
                                    }
                                }
                                else
                                if (string instanceof Boolean)
                                {
                                    // OK, let's add the String
                                    stringList.add(new StringSearchResult(((Boolean)string).toString(), parentObject, grandParentObject, field.getName()+" (Boolean)"));                       
                                }
                                else
                                if (string instanceof Condition)
                                {
                                	stringList.add(new StringSearchResult(((Condition)string).toString(), parentObject, grandParentObject, field.getName()+" (Condition)"));
                                }
                                else
                                if (string instanceof Object[])
                                {
                                    for (int j=0;j<((Object[])string).length;j++) findMetaData( ((Object[])string)[j], level+1, stringList, parentObject, grandParentObject);
                                }
                                else
                                {
                                    findMetaData(string, level+1, stringList, parentObject, grandParentObject);
                                }
                            }
                        }
                        catch(Exception ex)
                        {
                            LogWriter.getInstance().logDebug("StringSearcher", Const.rightPad(" ", level)+"    Unable to get access to method "+fullMethod+" : "+e.toString());
                        }
                    }
                }
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

