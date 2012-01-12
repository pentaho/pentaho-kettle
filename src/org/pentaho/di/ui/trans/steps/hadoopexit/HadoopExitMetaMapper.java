/*******************************************************************************
 *
 * Pentaho Big Data
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
