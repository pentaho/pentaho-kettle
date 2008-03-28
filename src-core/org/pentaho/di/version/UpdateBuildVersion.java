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
package org.pentaho.di.version;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.pentaho.di.core.Const;

/**
 * Updated : 2008/03/28 : get the build number from build-res/version.properties 
 * 
 * @author Matt Casters (mcasters@pentaho.org)
 *
 */
public class UpdateBuildVersion
{   
    public static final String BUILD_VERSION_FILE = "build-res/version.properties";
    public static final String BUILD_VERSION_PROPERTY = "release.build.number";

	public static void main(String[] args) throws IOException
    {
        BuildVersion buildVersion = BuildVersion.getInstance();
        
        // Get the build number from build-res/version.properties...
        //
        Properties properties = new Properties();
        FileInputStream in = new FileInputStream(new File(BUILD_VERSION_FILE));
        properties.load(in);
        in.close();
        
        buildVersion.setVersion( Const.toInt(properties.getProperty(BUILD_VERSION_PROPERTY), buildVersion.getVersion()) );
        buildVersion.setBuildDate(new Date());
        buildVersion.save();
    }
}
