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
package org.pentaho.di.trans.step;

import org.pentaho.di.trans.Trans;

/**
 * This listener informs the audience of the various states of a step.
 * 
 * @author matt
 * 
 */
public interface StepListener {

  /**
   * This method is called when a step goes from being idle to being active.
   * 
   * @param trans
   * @param stepMeta
   * @param step
   */
  public void stepActive(Trans trans, StepMeta stepMeta, StepInterface step);

  /**
   * This method is called when a step goes from being active to being idle.
   * 
   * @param trans
   * @param stepMeta
   * @param step
   */
  public void stepIdle(Trans trans, StepMeta stepMeta, StepInterface step);

  /**
   * This method is called when a step completes all work and is finished.
   * 
   * @param trans
   * @param stepMeta
   * @param step
   */
  public void stepFinished(Trans trans, StepMeta stepMeta, StepInterface step);
}
