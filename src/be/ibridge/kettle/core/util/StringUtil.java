package be.ibridge.kettle.core.util;

import java.util.Map;
import java.util.Properties;

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
	 * Substitutes variables in <code>aString</code> with Kettle system
	 * environment values.
	 * 
	 * @param aString
	 *            the string on which to apply the substitution.
	 * @return the string with the substitution applied.
	 */
	public static final String environmentSubstitute(String aString)
	{
		Properties systemProperties = System.getProperties();
		aString = substituteWindows(aString, systemProperties);
		aString = substituteUnix(aString, systemProperties);
		return aString;
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
}
