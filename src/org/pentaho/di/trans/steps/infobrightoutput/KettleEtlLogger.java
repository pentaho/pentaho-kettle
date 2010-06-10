/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.trans.steps.infobrightoutput;

import org.pentaho.di.trans.step.BaseStep;

import com.infobright.logging.EtlLogger;

/**
 * Adapter between Infobright EtlLogger and a Kettle BaseStep.
 *
 * @author geoffrey.falk@infobright.com
 */
public class KettleEtlLogger implements EtlLogger {

  private BaseStep step;
  
  public KettleEtlLogger(BaseStep step) {
    this.step = step;
  }
  
  //@Override
  public void debug(String s) {
    step.logDebug(s);
  }

  //@Override
  public void error(String s, Throwable cause) {
    step.logError(s + ": " + cause.getMessage());
  }

  //@Override
  public void error(String s) {
    step.logError(s);
  }

  //@Override
  public void info(String s) {
    step.logBasic(s);
  }

  //@Override
  public void trace(String s) {
    step.logRowlevel(s);
  }

  //@Override
  public void warn(String s) {
    step.logMinimal(s);
  }

  //@Override
  public void fatal(String s) {
    step.logError(s); // Kettle BaseStep does not have FATAL level 
  }

}
