/*
 * $Header: ConfigurableStreamLogger.java
 * $Revision:
 * $Date: 06.07.2009 11:04:51
 *
 * ==========================================================
 * COPYRIGHTS
 * ==========================================================
 * Copyright (c) 2009 Aschauer EDV.  All rights reserved. 
 * This software was developed by Aschauer EDV and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Terafast 
 * PDI Plugin. The Initial Developer is Aschauer EDV.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/

package org.pentaho.di.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;

/**
 * @author <a href="mailto:michael.gugerell@aschauer-edv.at">Michael Gugerell(asc145)</a>
 *
 * Provides the ability to specify the desired logLevel on which the StreamLogger should write.
 */
public class ConfigurableStreamLogger implements Runnable {

    private InputStream is;
    private String type;
    private LogLevel logLevel;
    private LogChannelInterface log;
        
    /**
     * @param in
     *        the InputStream
     * @param logLevel
     *        the logLevel. Refer to org.pentaho.di.core.logging.LogWriter for constants
     * @param type
     *        the label for logger entries.
     */
    public ConfigurableStreamLogger(LogChannelInterface logChannel, final InputStream in, final LogLevel logLevel, final String type) {
    	this.log = logChannel;
        this.is = in;
        this.type = type;
        this.logLevel = logLevel;
    }
    
    /** (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try
        {
            InputStreamReader isr = new InputStreamReader(this.is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null)
            {
              switch(this.logLevel) {
              case MINIMAL: log.logMinimal(this.type, line); break;
              case BASIC: log.logBasic(this.type, line); break;
              case DETAILED: log.logDetailed(this.type, line); break;
              case DEBUG: log.logDebug(this.type, line); break;
              case ROWLEVEL: log.logRowlevel(this.type, line); break;
              case ERROR: log.logError(this.type, line); break; 
              }
            }
        }
        catch (IOException ioe)
        {
            if (log.isError()) {
                log.logError(this.type, Const.getStackTracker(ioe));
            }
        }
    }

}
