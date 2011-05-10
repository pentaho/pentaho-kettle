/* Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
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

package org.pentaho.hbase.mapping;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

/**
 * Interface to something that can produce a connection to
 * HBase
 * 
 * @author Mark Hall (mhall{[at]}penthao{[dot]}com)
 * @version $Revision$
 *
 */
public interface ConfigurationProducer {
  
  
  /**
   * Get a configuration object encapsulating connection information
   * for HBase
   * 
   * @return a configuration object
   * @throws IOException if the connection can't be supplied for some reason
   */
  Configuration getHBaseConnection() throws IOException;
}
