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

package org.pentaho.di.core.database.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.pentaho.di.core.database.DataSourceNamingException;
import org.pentaho.di.core.database.DataSourceProviderInterface;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Provides default implementation for looking data sources up in
 * JNDI.
 * 
 * @author mbatchel
 *
 */

public class DatabaseUtil implements DataSourceProviderInterface
{
  private static Class<?> PKG = Database.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  /**
   * Implementation of DatasourceProviderInterface.
   * 
   */
  public DataSource getNamedDataSource(String datasourceName) throws DataSourceNamingException {
    try {
      return DatabaseUtil.getDataSourceFromJndi(datasourceName);
    } catch (NamingException ex) {
      throw new DataSourceNamingException(ex);
    }
  }
  
  private static Map<String,DataSource> FoundDS = Collections.synchronizedMap(new HashMap<String,DataSource>());
  
  /**
   * Since JNDI is supported different ways in different app servers, it's
   * nearly impossible to have a ubiquitous way to look up a datasource. This
   * method is intended to hide all the lookups that may be required to find a
   * jndi name.
   * 
   * @param dsName
   *            The Datasource name
   * @return DataSource if there is one bound in JNDI
   * @throws NamingException
   */
  private static DataSource getDataSourceFromJndi(String dsName) throws NamingException
  {
    Object foundDs = FoundDS.get(dsName);
    if (foundDs != null)
    {
      return (DataSource) foundDs;
    }
    InitialContext ctx = new InitialContext();
    Object lkup = null;
    DataSource rtn = null;
    NamingException firstNe = null;
    // First, try what they ask for...
    try
    {
      lkup = ctx.lookup(dsName);
      if (lkup != null)
      {
        rtn = (DataSource) lkup;
        FoundDS.put(dsName, rtn);
        return rtn;
      }
    } catch (NamingException ignored)
    {
      firstNe = ignored;
    }
    try
    {
      // Needed this for Jboss
      lkup = ctx.lookup("java:" + dsName); //$NON-NLS-1$
      if (lkup != null)
      {
        rtn = (DataSource) lkup;
        FoundDS.put(dsName, rtn);
        return rtn;
      }
    } catch (NamingException ignored)
    {
    }
    try
    {
      // Tomcat
      lkup = ctx.lookup("java:comp/env/jdbc/" + dsName); //$NON-NLS-1$
      if (lkup != null)
      {
        rtn = (DataSource) lkup;
        FoundDS.put(dsName, rtn);
        return rtn;
      }
    } catch (NamingException ignored)
    {
    }
    try
    {
      // Others?
      lkup = ctx.lookup("jdbc/" + dsName); //$NON-NLS-1$
      if (lkup != null)
      {
        rtn = (DataSource) lkup;
        FoundDS.put(dsName, rtn);
        return rtn;
      }
    } catch (NamingException ignored)
    {
    }
    if (firstNe != null)
    {
      throw firstNe;
    }
    throw new NamingException(BaseMessages.getString(PKG, "DatabaseUtil.DSNotFound", dsName)); //$NON-NLS-1$
  }
}
