/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.version;

import java.net.JarURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.xml.XMLHandler;

import java.text.SimpleDateFormat;


/**
 * Singleton class to allow us to see on which date & time the kettle3.jar was built.
 * 
 * @author Matt
 * @since 2006-aug-12
 */
public class BuildVersion
{
	public static final String REFERENCE_FILE = "/kettle-steps.xml";
	private static final String JAR_BUILD_DATE_FORMAT = "yyyy-MM-dd HH.mm.ss";
	
	private static BuildVersion buildVersion;
    
    /**
     * @return the instance of the BuildVersion singleton
     */
    public static final BuildVersion getInstance()
    {
        if (buildVersion!=null) return buildVersion;
        
        buildVersion = new BuildVersion();
        
        return buildVersion;
    }
    
    private String version;
    private String revision;
    private String buildDate;
    private String buildUser;
    
    private BuildVersion()
    {
        try
        {
        	URL url = this.getClass().getResource(REFERENCE_FILE);
        	JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
        	Manifest manifest = jarConnection.getManifest();

        	version = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        	revision = manifest.getMainAttributes().getValue(Attributes.Name.SPECIFICATION_VERSION);
        	buildDate = manifest.getMainAttributes().getValue("Compile-Timestamp");
        	buildUser = manifest.getMainAttributes().getValue("Compile-User");
        }
        catch(Exception e) {
          // System.out.println("Unable to read version information from manifest : not running from jar files (Igored)");
          
          version = Const.VERSION;
          revision = "";
          buildDate = XMLHandler.date2string(new Date());
          buildUser = "";
        }
    }

    /**
     * @return the buildDate
     */
    public String getBuildDate()
    {
        return buildDate;
    }
    
    public Date getBuildDateAsLocalDate(){

    	SimpleDateFormat sdf = new SimpleDateFormat(JAR_BUILD_DATE_FORMAT);
		try {
			Date d = sdf.parse(buildDate);
			return d;
			// ignore failure, retry using standard format
		} catch (ParseException e) {};
		
		sdf = new SimpleDateFormat(ValueMeta.DEFAULT_DATE_FORMAT_MASK);
		try {
			Date d = sdf.parse(buildDate);
			return d;
			// ignore failure and return null
		} catch (ParseException e) {};
		
		return null;
    	
    }

    /**
     * @param buildDate the buildDate to set
     */
    public void setBuildDate(String buildDate)
    {
        this.buildDate = buildDate;
    }

    /**
     * @return the version
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * @param revision the version to set
     */
    public void setVersion(String version)
    {
        this.version = version;
    }
    
	/**
	 * @return the revision
	 */
	public String getRevision() {
		return revision;
	}

	/**
	 * @param revision the revision to set
	 */
	public void setRevision(String revision) {
		this.revision = revision;
	}

	/**
	 * @return the buildUser
	 */
	public String getBuildUser() {
		return buildUser;
	}

	/**
	 * @param buildUser the buildUser to set
	 */
	public void setBuildUser(String buildUser) {
		this.buildUser = buildUser;
	}
}
