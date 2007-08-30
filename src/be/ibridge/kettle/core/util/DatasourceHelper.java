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
 * Created Aug 30, 2005 
 * @author mbatchel
 */
package be.ibridge.kettle.core.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import be.ibridge.kettle.core.Messages;

public class DatasourceHelper {

  private static Map FoundDS = Collections.synchronizedMap(new HashMap());

  /**
   * This method clears the JNDI DS cache.  The need exists because after a JNDI
   * connection edit the old DS must be removed from the cache.
   *
   */
  public static void clearCache() {
    if (FoundDS != null) {
      FoundDS.clear();
    }
  }

  /**
   * This method clears the JNDI DS cache.  The need exists because after a JNDI
   * connection edit the old DS must be removed from the cache.
   *
   */
  public static void clearDataSource(String dsName) {
    if (FoundDS != null) {
      FoundDS.remove(dsName);
    }
  }

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
  public static DataSource getDataSourceFromJndi(String dsName) throws NamingException {
    Object foundDs = FoundDS.get(dsName);
    if (foundDs != null) {
      return (DataSource) foundDs;
    }
    InitialContext ctx = new InitialContext();
    Object lkup = null;
    DataSource rtn = null;
    NamingException firstNe = null;
    // First, try what they ask for...
    try {
      lkup = ctx.lookup(dsName);
      if (lkup != null) {
        rtn = (DataSource) lkup;
        FoundDS.put(dsName, rtn);
        return rtn;
      }
    } catch (NamingException ignored) {
      firstNe = ignored;
    }
    try {
      // Needed this for Jboss
      lkup = ctx.lookup("java:" + dsName); //$NON-NLS-1$
      if (lkup != null) {
        rtn = (DataSource) lkup;
        FoundDS.put(dsName, rtn);
        return rtn;
      }
    } catch (NamingException ignored) {
    }
    try {
      // Tomcat
      lkup = ctx.lookup("java:comp/env/jdbc/" + dsName); //$NON-NLS-1$
      if (lkup != null) {
        rtn = (DataSource) lkup;
        FoundDS.put(dsName, rtn);
        return rtn;
      }
    } catch (NamingException ignored) {
    }
    try {
      // Others?
      lkup = ctx.lookup("jdbc/" + dsName); //$NON-NLS-1$
      if (lkup != null) {
        rtn = (DataSource) lkup;
        FoundDS.put(dsName, rtn);
        return rtn;
      }
    } catch (NamingException ignored) {
    }
    if (firstNe != null) {
      throw firstNe;
    }
    throw new NamingException(Messages.getString("DatasourceHelper.ERROR_0001_INVALID_DATASOURCE", dsName)); //$NON-NLS-1$
  }

  /**
   * Since JNDI is supported different ways in different app servers, it's
   * nearly impossible to have a ubiquitous way to look up a datasource. This
   * method is intended to hide all the lookups that may be required to find a
   * jndi name, and return the actual bound name.
   * 
   * @param dsName
   *            The Datasource name (like SampleData)
   * @return The bound DS name if it is bound in JNDI (like "jdbc/SampleData")
   * @throws NamingException
   */
  public static String getDSBoundName(String dsName) throws NamingException {
    InitialContext ctx = new InitialContext();
    Object lkup = null;
    NamingException firstNe = null;
    String rtn = dsName;
    // First, try what they ask for...
    try {
      lkup = ctx.lookup(rtn);
      if (lkup != null) {
        return rtn;
      }
    } catch (NamingException ignored) {
      firstNe = ignored;
    }
    try {
      // Needed this for Jboss
      rtn = "java:" + dsName; //$NON-NLS-1$
      lkup = ctx.lookup(rtn);
      if (lkup != null) {
        return rtn;
      }
    } catch (NamingException ignored) {
    }
    try {
      // Tomcat
      rtn = "java:comp/env/jdbc/" + dsName; //$NON-NLS-1$
      lkup = ctx.lookup(rtn);
      if (lkup != null) {
        return rtn;
      }
    } catch (NamingException ignored) {
    }
    try {
      // Others?
      rtn = "jdbc/" + dsName; //$NON-NLS-1$
      lkup = ctx.lookup(rtn);
      if (lkup != null) {
        return rtn;
      }
    } catch (NamingException ignored) {
    }
    if (firstNe != null) {
      throw firstNe;
    }
    throw new NamingException(Messages.getString("DatasourceHelper.ERROR_0001_INVALID_DATASOURCE", dsName)); //$NON-NLS-1$

  }
}
