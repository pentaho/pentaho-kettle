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

/*
 *    ReservoirSamplingData.java
 *    Copyright 2007 Pentaho Corporation.  All rights reserved. 
 *
 */

package org.pentaho.di.trans.steps.reservoirsampling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Holds temporary data (i.e. sampled rows). Implements
 * the reservoir sampling algorithm "R" by Jeffrey Scott
 * Vitter. <p>
 * For more information see:<br><br>
 *
 * Vitter, J. S. Random Sampling with a Reservoir. ACM
 * Transactions on Mathematical Software, Vol. 11, No. 1,
 * March 1985. Pages 37-57.
 *
 * @author Mark Hall (mhall{[at]}pentaho.org)
 * @version 1.0
 */
public class ReservoirSamplingData extends BaseStepData 
  implements StepDataInterface {

  // the output data format
  protected RowMetaInterface m_outputRowMeta;

  // holds the sampled rows
  protected List<Object[]> m_sample = null;

  // the size of the sample
  protected int m_k;

  // the current row number
  protected int m_currentRow;
  
  // random number generator
  protected Random m_random;
  
  // state of processing
  protected PROC_MODE m_state;
  
  public enum PROC_MODE {SAMPLING, PASSTHROUGH, DISABLED};

  /**
   * Set the meta data for the output format
   *
   * @param rmi a <code>RowMetaInterface</code> value
   */
  public void setOutputRowMeta(RowMetaInterface rmi) {
    m_outputRowMeta = rmi;
  }
  
  /**
   * Get the output meta data
   *
   * @return a <code>RowMetaInterface</code> value
   */
  public RowMetaInterface getOutputRowMeta() {
    return m_outputRowMeta;
  }

  /**
   * Gets the sample as an array of rows
   *
   * @return the sampled rows
   */
  public List<Object[]> getSample() {
    return m_sample;
  }

  /**
   * Initialize this data object
   *
   * @param sampleSize the number of rows to sample
   * @param seed the seed for the random number generator
   */
  public void initialize(int sampleSize, int seed) {
    m_k = sampleSize;
    
    if(m_k == 0){
      m_state = PROC_MODE.PASSTHROUGH;
    } else if(m_k < 0){
      m_state = PROC_MODE.DISABLED;
    } else if (m_k > 0){
      m_state = PROC_MODE.SAMPLING;
    }
    
    m_sample = (m_k > 0) ? new ArrayList<Object[]>(m_k) : new ArrayList<Object[]>();
    m_currentRow = 0;
    m_random = new Random(seed);
    
    // throw away the first 100 random numbers
    for (int i = 0; i < 100; i++) {
      m_random.nextDouble();
    }
  }
  
  /**
   *  Determine the current operational state of
   *  the Reservoir Sampling step.
   *  Sampling, PassThrough(Do not wait until
   *  end, pass through on the fly), Disabled.
   *  
   *  @return current operational state
   */
  public PROC_MODE getProcessingMode(){
    return m_state;
  }
  
  /**
   * 
   * Set this component to sample, pass through or
   * be disabled
   * 
   * @param state member of PROC_MODE enumeration
   * indicating the desired operational state
   */
  public void setProcessingMode(PROC_MODE state){
    this.m_state = state;
  }

  /**
   * Here is where the action happens. Sampling
   * is done using the "R" algorithm of Jeffrey
   * Scott Vitter.
   *
   * @param row an incoming row
   * 
   */
  public void processRow(Object [] row) {
    if (m_currentRow < m_k) {
      // Fill sample size with first available data
      setElement(m_sample, m_currentRow, row);
    } else if (m_k > 0) { 
      // Replace random positions within the sample     
      double r = m_random.nextDouble();
      if (r < ((double)m_k / (double)m_currentRow)) {
        r = m_random.nextDouble();
        int replace = (int)((double)m_k * r);
        setElement(m_sample, replace, row);
      }
    }
    m_currentRow++;
  }
  
  //brute force way of filling list when item index is  out of range,
  //should be ported to a commons or some library call or something
  //that works well with the "R" randomizing algorithm
  private void setElement(List<Object[]> list, int idx, Object item) {
    final int size = list.size();
    if (size <= idx) {
      int buff = (size == 0) ? 100 : size * 2;
      for(int i = 0; i < buff; i++) {
        list.add(null);
      }		  
    }
    list.set(idx, (Object [])item);
  
  }
  
  public void cleanUp() {
    m_sample = null;
  }
}