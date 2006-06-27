package be.ibridge.kettle.core.util;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.KettleVariables;

/**
 * A collection of utilities to manipulate strings.
 * 
 * @author wdeclerc
 * 
 */
public class StringUtil
{
	public static final String UNIX_OPEN = "${";

	public static final String UNIX_CLOSE = "}";

	public static final String WINDOWS_OPEN = "%%";

	public static final String WINDOWS_CLOSE = "%%";

	/**
	 * Substitutes variables in <code>aString</code>. Variable names are
	 * delimited by open and close strings. The values are retrieved from the
	 * given map.
	 * 
	 * @param aString
	 *            the string on which to apply the substitution.
	 * @param variablesValues
	 *            a map containg the variable values. The keys are the variable
	 *            names, the values are the variable values.
	 * @param open
	 *            the open delimiter for variables.
	 * @param close
	 *            the close delimiter for variables.
	 * @return the string with the substitution applied.
	 */
	public static String substitute(String aString, Map variablesValues, String open, String close)
	{
		if (aString==null) return null;
		
		StringBuffer buffer = new StringBuffer();

		String rest = aString;
        
		// search for opening string
		int i = rest.indexOf(open);
		while (i > -1)
		{
			int j = rest.indexOf(close, i + open.length());
			// search for closing string
			if (j > -1)
			{
				String varName = rest.substring(i + open.length(), j);
				Object value = variablesValues.get(varName);
				if (value == null)
				{
					value = open + varName + close;
				}
				buffer.append(rest.substring(0, i));
				buffer.append(value);
				rest = rest.substring(j + close.length());
			}
			else
			{
				// no closing tag found; end the search
				buffer.append(rest);
				rest = "";
			}
			// keep searching
			i = rest.indexOf(open);
		}
		buffer.append(rest);
		return buffer.toString();
	}

	/**
	 * Substitutes variables in <code>aString</code> with Kettle system and local environment values.
	 * 
	 * @param aString the string on which to apply the substitution.
	 * @return the string with the substitution applied.
	 */
	public static final String environmentSubstitute(String aString)
	{
        KettleVariables vars = KettleVariables.getInstance();
        
        Properties systemProperties = new Properties();
        systemProperties.putAll( System.getProperties() );
        systemProperties.putAll( vars.getProperties() ); // overwrite with local vars

        return environmentSubstitute(aString, systemProperties);
	}

    /**
     * Substitutes variables in <code>aString</code> with the environment values in the system properties
     * 
     * @param aString the string on which to apply the substitution.
     * @param systemProperties the system properties to use
     * @return the string with the substitution applied.

     */
	private static final String environmentSubstitute(String aString, Properties systemProperties)
    {
        aString = substituteWindows(aString, systemProperties);
        aString = substituteUnix(aString, systemProperties);
        return aString;
    }

    /**
	 * Replaces environment variables in an array of strings.<p>
	 * See also: environmentSubstitute(String string)
	 * @param string The array of strings that wants its variables to be replaced.
	 * @return the array with the environment variables replaced.
	 */
	public static final String[] environmentSubstitute(String string[])
	{
        KettleVariables vars = KettleVariables.getInstance();
        
        Properties systemProperties = new Properties();
        systemProperties.putAll( System.getProperties() );
        systemProperties.putAll( vars.getProperties() ); // overwrite with local vars
        
        String retval[] = new String[string.length];
		for (int i = 0; i < string.length; i++)
		{
			retval[i] = environmentSubstitute(string[i], systemProperties);
		}
		return retval;
	}

	/**
	 * Substitutes variables in <code>aString</code>. Variables are of the
	 * form "${<variable name>}", following the usin convention. The values are
	 * retrieved from the given map.
	 * 
	 * @param aString
	 *            the string on which to apply the substitution.
	 * @param variablesValues
	 *            a map containg the variable values. The keys are the variable
	 *            names, the values are the variable values.
	 * @param open
	 *            the open delimiter for variables.
	 * @param close
	 *            the close delimiter for variables.
	 * @return the string with the substitution applied.
	 */
	public static String substituteUnix(String aString, Map variables)
	{
		return substitute(aString, variables, UNIX_OPEN, UNIX_CLOSE);
	}

	/**
	 * Substitutes variables in <code>aString</code>. Variables are of the
	 * form "%%<variable name>%%", following the windows convention. The values
	 * are retrieved from the given map.
	 * 
	 * @param aString
	 *            the string on which to apply the substitution.
	 * @param variablesValues
	 *            a map containg the variable values. The keys are the variable
	 *            names, the values are the variable values.
	 * @param open
	 *            the open delimiter for variables.
	 * @param close
	 *            the close delimiter for variables.
	 * @return the string with the substitution applied.
	 */
	public static String substituteWindows(String aString, Map variables)
	{
		return substitute(aString, variables, WINDOWS_OPEN, WINDOWS_CLOSE);
	}
    
    
    /**
     * Search the string and report back on the variables used
     * @param aString The string to search
     * @param list the list of variables to add to
     */
    private static void getUsedVariables(String aString, String open, String close, List list)
    {
        if (aString==null) return;
        
        int p=0;
        while (p<aString.length())
        {
            // OK, we found something... : start of Unix variable
            if (aString.substring(p).startsWith(open))
            {
                // See if it's closed...
                int from = p+open.length();
                int to = aString.indexOf(close, from+1);
                String variable = aString.substring(from, to);
                
                if (Const.indexOfString(variable, list)<0) 
                {
                	if (System.getProperty(variable)==null) // Filter out set environment variables
                	{
                		list.add(variable);
                	}
                }
                
                // OK, continue
                p=to;
            }
            p++;
        }
    }
    
    public static void getUsedVariables(String aString, List list)
    {
        getUsedVariables(aString, UNIX_OPEN, UNIX_CLOSE, list);
        getUsedVariables(aString, WINDOWS_OPEN, WINDOWS_CLOSE, list);
    }
    
    /**
     * Return the value of a Kettle or system variable. (in that order or occurence)
     * 
     * @param aString the variable to look for, without the $ or % variable specification, just the name.
     * @param defaultValue the default value in case nothing was found.
     * @return the value for that (kettle) variable
     */
    public static final String getVariable(String variable, String defaultValue)
    {
        KettleVariables vars = KettleVariables.getInstance();
        
        Properties systemProperties = new Properties();
        systemProperties.putAll( System.getProperties() );
        systemProperties.putAll( vars.getProperties() ); // overwrite with local vars

        return systemProperties.getProperty(variable, defaultValue);
    }
}

