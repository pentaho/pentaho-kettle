/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.ui.trans.steps.hadoopexit;

import org.pentaho.di.trans.steps.hadoopexit.HadoopExitMeta;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class HadoopExitMetaMapper extends XulEventSourceAdapter {
  public static String OUT_KEY_FIELDNAME = "out-key-fieldname";
  public static String OUT_VALUE_FIELDNAME = "out-value-fieldname";
  
  protected String outKeyFieldname;
  protected String outValueFieldname;
  
  
  public void setOutKeyFieldname(String arg) {
    String previousVal = outKeyFieldname;
    outKeyFieldname = arg;
    firePropertyChange(OUT_KEY_FIELDNAME, previousVal, outKeyFieldname);
  }
  
  public String getOutKeyFieldname() {
    return outKeyFieldname;
  }
  
  public void setOutValueFieldname(String arg) {
    String previousVal = outValueFieldname;
    outValueFieldname = arg;
    firePropertyChange(OUT_VALUE_FIELDNAME, previousVal, outValueFieldname);
  }
  
  public String getOutValueFieldname() {
    return outValueFieldname;
  }
  
  /**
   * Load data into the MetaMapper from the HadoopExitMeta
   * @param meta
   */
  public void loadMeta(HadoopExitMeta meta) {
    setOutKeyFieldname(meta.getOutKeyFieldname());
    setOutValueFieldname(meta.getOutValueFieldname());
  }
  
  /**
   * Save data from the MetaMapper into the HadoopExitMeta
   * @param meta
   */
  public void saveMeta(HadoopExitMeta meta) {
    // Set outKey
    if(meta.getOutKeyFieldname() == null && getOutKeyFieldname() != null) {
      meta.setOutKeyFieldname(getOutKeyFieldname());
      meta.setChanged();
    } else if(meta.getOutKeyFieldname() != null && !meta.getOutKeyFieldname().equals(getOutKeyFieldname())) {
      meta.setOutKeyFieldname(getOutKeyFieldname());
      meta.setChanged();
    }
    
    // Set outValue
    if(meta.getOutValueFieldname() == null && getOutValueFieldname() != null) {
      meta.setOutValueFieldname(getOutValueFieldname());
      meta.setChanged();
    } else if(meta.getOutValueFieldname() != null && !meta.getOutValueFieldname().equals(getOutValueFieldname())) {
      meta.setOutValueFieldname(getOutValueFieldname());
      meta.setChanged();
    }
  }
}
