package be.ibridge.kettle.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;

public class EnvUtil
{
	private static LogWriter log = LogWriter.getInstance();

	/**
	 * Returns the properties from the users ketle home directory.
	 * 
	 * @param fileName
	 *            the relative name of the properties file in the users kettle
	 *            directory.
	 * @return the map of properties.
	 */
	public static Map readProperties(String fileName)
	{
		Properties props = new Properties();
		String kettlePropsFilename = Const.getKettleDirectory()
				+ Const.FILE_SEPARATOR + fileName;
		InputStream is = null;
		try
		{
			is = new FileInputStream(kettlePropsFilename);
			props.load(is);
		}
		catch (IOException ioe)
		{
			log
					.println(LogWriter.LOG_LEVEL_BASIC,
							"Unable to read \"<home>/.kettle/kettle.properties\" file: " + ioe.getMessage());
		}
		finally
		{
			if (is != null)
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					// ignore
				}
		}
		return props;
	}

	/**
	 * Adds the kettle properties the the global system properties.
	 */
	public static void environmentInit()
	{
		Map kettleProperties = EnvUtil.readProperties(Const.KETTLE_PROPERTIES);
		System.getProperties().putAll(kettleProperties);
	}
}
