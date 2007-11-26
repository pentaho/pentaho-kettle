/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Sep 16, 2007 
 * @author David Kincade
 */
package org.pentaho.di.core.lifecycle.pdi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.versionchecker.IVersionCheckDataProvider;

public class KettleVersionCheckDataProvider implements IVersionCheckDataProvider
{
	private String productID, versionMajor, versionMinor, versionBuild, versionRelease, versionMilestone;

	public KettleVersionCheckDataProvider() throws IOException
	{

		FileObject obj = KettleVFS.getFileObject("build-res/version.properties");

		Properties bundle = new Properties();
		InputStream in = null;

		try
		{
			in = obj.getURL().openStream();

			bundle.load(in);

			productID = bundle.getProperty("impl.productID"); //$NON-NLS-1$
			versionMajor = bundle.getProperty("release.major.number"); //$NON-NLS-1$
			versionMinor = bundle.getProperty("release.minor.number"); //$NON-NLS-1$
			versionBuild = bundle.getProperty("release.build.number"); //$NON-NLS-1$

			// The release milestone number has both the release number and the
			// milestone number
			String releaseMilestoneNumber = bundle.getProperty("release.milestone.number"); //$NON-NLS-1$
			if (releaseMilestoneNumber != null)
			{
				String[] parts = releaseMilestoneNumber.replace('-', '.').split("\\."); //$NON-NLS-1$
				if (parts.length > 0)
				{
					versionRelease = parts[0];
					if (parts.length > 1)
					{
						versionMilestone = parts[1];
					}
				}
			}
		} finally
		{
			try
			{
				if (in != null)
					in.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

	}

	public String getApplicationID()
	{
		return productID;
	}

	public String getApplicationVersion()
	{
		StringBuffer sb = new StringBuffer();
		if (versionMajor != null)
		{
			sb.append(versionMajor);
			if (versionMinor != null)
			{
				sb.append('.').append(versionMinor);
				if (versionRelease != null)
				{
					sb.append('.').append(versionRelease);
					if (versionMilestone != null)
					{
						sb.append('.').append(versionMilestone);
						if (versionBuild != null)
						{
							sb.append('.').append(versionBuild);
						}
					}
				}
			}
		}
		return sb.toString();
	}

	public String getBaseURL()
	{
		return null;
	}

	protected int computeOSMask()
	{
		try
		{
			String os = System.getProperty("os.name"); //$NON-NLS-1$
			if (os != null)
			{
				os = os.toLowerCase();
				if (os.indexOf("windows") >= 0) { //$NON-NLS-1$
					return DEPTH_WINDOWS_MASK;
				} else if (os.indexOf("mac") >= 0) { //$NON-NLS-1$
					return DEPTH_MAC_MASK;
				} else if (os.indexOf("linux") >= 0) { //$NON-NLS-1$
					return DEPTH_LINUX_MASK;
				} else
				{
					return DEPTH_ALL_MASK;
				}
			}
		} catch (Exception e)
		{
			// ignore any issues
		}
		return DEPTH_ALL_MASK;
	}

	/**
	 * generates the depth flags
	 */
	public int getDepth()
	{

		int depth = DEPTH_MINOR_MASK + DEPTH_GA_MASK + computeOSMask();
		return depth;
	}

	public Map getExtraInformation()
	{
		return null;
	}
}
