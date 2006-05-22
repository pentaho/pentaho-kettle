package be.ibridge.kettle.i18n;

import java.util.MissingResourceException;

public class BaseMessages
{
	public static String getString(String packageName, String key)
	{
		try
		{
			return be.ibridge.kettle.i18n.GlobalMessages.getString(packageName, key);
		}
		catch (MissingResourceException e)
		{
			return GlobalMessages.getSystemString(key);
		}
	}

	public static String getString(String packageName, String key, String param1)
	{
		try
		{
			return be.ibridge.kettle.i18n.GlobalMessages.getString(packageName, key, param1);
		}
		catch (MissingResourceException e)
		{
			return GlobalMessages.getSystemString(key, param1);
		}
	}

	public static String getString(String packageName, String key, String param1, String param2)
	{
		try
		{
			return be.ibridge.kettle.i18n.GlobalMessages.getString(packageName, key, param1, param2);
		}
		catch (MissingResourceException e)
		{
			return GlobalMessages.getSystemString(key, param1, param2);
		}
	}

	public static String getString(String packageName, String key, String param1, String param2, String param3)
	{
		try
		{
			return be.ibridge.kettle.i18n.GlobalMessages.getString(packageName, key, param1, param2, param3);
		}
		catch (MissingResourceException e)
		{
			return GlobalMessages.getSystemString(key, param1, param2, param3);
		}
	}

	public static String getString(String packageName, String key, String param1, String param2, String param3, String param4)
	{
		try
		{
			return be.ibridge.kettle.i18n.GlobalMessages.getString(packageName, key, param1, param2, param3, param4);
		}
		catch (MissingResourceException e)
		{
			return GlobalMessages.getSystemString(key, param1, param2, param3, param4);
		}
	}

	public static String getString(String packageName, String key, String param1, String param2, String param3, String param4, String param5)
	{
		try
		{
			return be.ibridge.kettle.i18n.GlobalMessages.getString(packageName, key, param1, param2, param3, param4, param5);
		}
		catch (MissingResourceException e)
		{
			return GlobalMessages.getSystemString(key, param1, param2, param3, param4, param5);
		}
	}

	public static String getString(String packageName, String key, String param1, String param2, String param3, String param4, String param5, String param6)
	{
		try
		{
			return be.ibridge.kettle.i18n.GlobalMessages.getString(packageName, key, param1, param2, param3, param4, param5, param6);
		}
		catch (MissingResourceException e)
		{
			return GlobalMessages.getSystemString(key, param1, param2, param3, param4, param5, param6);
		}
	}

}
