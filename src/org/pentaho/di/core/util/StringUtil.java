package org.pentaho.di.core.util;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.variables.KettleVariables;

/**
 * A collection of utilities to manipulate strings.
 * 
 * @author wdeclerc
 */
public class StringUtil
{
	public static final String UNIX_OPEN = "${";

	public static final String UNIX_CLOSE = "}";

	public static final String WINDOWS_OPEN = "%%";

	public static final String WINDOWS_CLOSE = "%%";

	private static final Pattern ALL_LETTERS = Pattern.compile("([a-z])");

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
		return substitute(aString, variablesValues, open, close, 0);
	}

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
	 * @param recursion
	 *            the number of recursion (internal counter to avoid endless
	 *            loops)
	 * @return the string with the substitution applied.
	 */
	public static String substitute(String aString, Map variablesValues, String open, String close,
			int recursion)
	{
		if (aString == null)
			return null;

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
				} else
				{
					// check for another variable inside this value
					int another = ((String) value).indexOf(open); // check
					// here
					// first for
					// speed
					if (another > -1)
					{
						if (recursion > 50) // for safety: avoid recursive
						// endless loops with stack overflow
						{
							throw new RuntimeException("Endless loop detected for substitution of variable: "
									+ (String) value);
						}
						value = substitute((String) value, variablesValues, open, close, ++recursion);
					}
				}
				buffer.append(rest.substring(0, i));
				buffer.append(value);
				rest = rest.substring(j + close.length());
			} else
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
	 * Substitutes variables in <code>aString</code> with Kettle system and
	 * local environment values.
	 * 
	 * @param aString
	 *            the string on which to apply the substitution.
	 * @return the string with the substitution applied.
	 */
	public static final String environmentSubstitute(String aString)
	{
		if (aString == null || aString.length() == 0)
			return aString;

		KettleVariables vars = KettleVariables.getInstance();

		Properties systemProperties = new Properties();
		systemProperties.putAll(System.getProperties());
		systemProperties.putAll(vars.getProperties()); // overwrite with
		// local vars

		return environmentSubstitute(aString, systemProperties);
	}

	/**
	 * Substitutes variables in <code>aString</code> with the environment
	 * values in the system properties
	 * 
	 * @param aString
	 *            the string on which to apply the substitution.
	 * @param systemProperties
	 *            the system properties to use
	 * @return the string with the substitution applied.
	 */
	private static final String environmentSubstitute(String aString, Properties systemProperties)
	{
		aString = substituteWindows(aString, systemProperties);
		aString = substituteUnix(aString, systemProperties);
		return aString;
	}

	/**
	 * Replaces environment variables in an array of strings.
	 * <p>
	 * See also: environmentSubstitute(String string)
	 * 
	 * @param string
	 *            The array of strings that wants its variables to be replaced.
	 * @return the array with the environment variables replaced.
	 */
	public static final String[] environmentSubstitute(String string[])
	{
		KettleVariables vars = KettleVariables.getInstance();

		Properties systemProperties = new Properties();
		systemProperties.putAll(System.getProperties());
		systemProperties.putAll(vars.getProperties()); // overwrite with
		// local vars

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
	 * @param variables
	 *            a map containg the variable values. The keys are the variable
	 *            names, the values are the variable values.
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
	 * @param variables
	 *            a map containg the variable values. The keys are the variable
	 *            names, the values are the variable values.
	 * @return the string with the substitution applied.
	 */
	public static String substituteWindows(String aString, Map variables)
	{
		return substitute(aString, variables, WINDOWS_OPEN, WINDOWS_CLOSE);
	}

	/**
	 * Search the string and report back on the variables used
	 * 
	 * @param aString
	 *            The string to search
	 * @param open
	 *            the open or "start of variable" characters ${ or %%
	 * @param close
	 *            the close or "end of variable" characters } or %%
	 * @param list
	 *            the list of variables to add to
	 * @param includeSystemVariables
	 *            also check for system variables.
	 */
	private static void getUsedVariables(String aString, String open, String close, List list,
			boolean includeSystemVariables)
	{
		if (aString == null)
			return;

		int p = 0;
		while (p < aString.length())
		{
			// OK, we found something... : start of Unix variable
			if (aString.substring(p).startsWith(open))
			{
				// See if it's closed...
				int from = p + open.length();
				int to = aString.indexOf(close, from + 1);

				if (to >= 0)
				{
					String variable = aString.substring(from, to);

					if (Const.indexOfString(variable, list) < 0)
					{
						// Optionally filter out set environment variables
						if (includeSystemVariables || System.getProperty(variable) == null)
						{
							list.add(variable);
						}
					}
					// OK, continue
					p = to + close.length();
				}
			}
			p++;
		}
	}

	public static void getUsedVariables(String aString, List list, boolean includeSystemVariables)
	{
		getUsedVariables(aString, UNIX_OPEN, UNIX_CLOSE, list, includeSystemVariables);
		getUsedVariables(aString, WINDOWS_OPEN, WINDOWS_CLOSE, list, includeSystemVariables);
	}

	/**
	 * Return the value of a Kettle or system variable. (in that order or
	 * occurence)
	 * 
	 * @param variable
	 *            the variable to look for, without the $ or % variable
	 *            specification, just the name.
	 * @param defaultValue
	 *            the default value in case nothing was found.
	 * @return the value for that (kettle) variable
	 */
	public static final String getVariable(String variable, String defaultValue)
	{
		KettleVariables vars = KettleVariables.getInstance();

		Properties systemProperties = new Properties();
		systemProperties.putAll(System.getProperties());
		systemProperties.putAll(vars.getProperties()); // overwrite with
		// local vars

		return systemProperties.getProperty(variable, defaultValue);
	}

	public static final String generateRandomString(int length, String prefix, String postfix,
			boolean uppercase)
	{
		StringBuffer buffer = new StringBuffer();

		if (!Const.isEmpty(prefix))
			buffer.append(prefix);

		for (int i = 0; i < length; i++)
		{
			int c = 'a' + (int) (Math.random() * 26);
			buffer.append((char) c);
		}
		if (!Const.isEmpty(postfix))
			buffer.append(postfix);

		if (uppercase)
			return buffer.toString().toUpperCase();

		return buffer.toString();
	}

	public static String initCap(String st)
	{
		if (st == null || st.trim().length() == 0)
			return "";

		StringBuffer sb = new StringBuffer();
		Matcher m = ALL_LETTERS.matcher(st);
		if (m.find(0))
			m.appendReplacement(sb, m.group(1).toUpperCase());

		st = m.appendTail(sb).toString();

		return st;
	}

	public static double str2num(String pattern, String decimal, String grouping, String currency,
			String value) throws KettleValueException
	{
		// 0 : pattern
		// 1 : Decimal separator
		// 2 : Grouping separator
		// 3 : Currency symbol

		NumberFormat nf = NumberFormat.getInstance();
		DecimalFormat df = (DecimalFormat) nf;
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();

		if (!Const.isEmpty(pattern))
			df.applyPattern(pattern);
		if (!Const.isEmpty(decimal))
			dfs.setDecimalSeparator(decimal.charAt(0));
		if (!Const.isEmpty(grouping))
			dfs.setGroupingSeparator(grouping.charAt(0));
		if (!Const.isEmpty(currency))
			dfs.setCurrencySymbol(currency);
		try
		{
			df.setDecimalFormatSymbols(dfs);
			return df.parse(value).doubleValue();
		} catch (Exception e)
		{
			String message = "Couldn't convert string to number " + e.toString();
			if (!isEmpty(pattern))
				message += " pattern=" + pattern;
			if (!isEmpty(decimal))
				message += " decimal=" + decimal;
			if (!isEmpty(grouping))
				message += " grouping=" + grouping.charAt(0);
			if (!isEmpty(currency))
				message += " currency=" + currency;
			throw new KettleValueException(message);
		}
	}

	/**
	 * Check if the string supplied is empty. A String is empty when it is null
	 * or when the length is 0
	 * 
	 * @param string
	 *            The string to check
	 * @return true if the string supplied is empty
	 */
	public static final boolean isEmpty(String string)
	{
		return string == null || string.length() == 0;
	}

	/**
	 * Check if the stringBuffer supplied is empty. A StringBuffer is empty when
	 * it is null or when the length is 0
	 * 
	 * @param string
	 *            The stringBuffer to check
	 * @return true if the stringBuffer supplied is empty
	 */
	public static final boolean isEmpty(StringBuffer string)
	{
		return string == null || string.length() == 0;
	}

	public static Date str2dat(String arg0, String arg1, String val) throws KettleValueException
	{

		// System.out.println("Convert string ["+string+"] to date using pattern
		// '"+arg0+"'");

		SimpleDateFormat df = new SimpleDateFormat();

		DateFormatSymbols dfs = new DateFormatSymbols();
		if (arg1 != null)
			dfs.setLocalPatternChars(arg1);
		if (arg0 != null)
			df.applyPattern(arg0);

		try
		{
			return df.parse(val);

		} catch (Exception e)
		{

			throw new KettleValueException("TO_DATE Couldn't convert String to Date" + e.toString());
		}

	}

}
