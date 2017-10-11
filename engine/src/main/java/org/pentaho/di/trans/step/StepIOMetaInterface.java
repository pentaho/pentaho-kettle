/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.step;

import java.util.List;

import org.pentaho.di.trans.step.errorhandling.StreamInterface;

public interface StepIOMetaInterface {

  public boolean isInputAcceptor();

  public boolean isOutputProducer();

  public boolean isInputOptional();

  public boolean isSortedDataRequired();

  public List<StreamInterface> getInfoStreams();

  public List<StreamInterface> getTargetStreams();

  public String[] getInfoStepnames();

  public String[] getTargetStepnames();

  /**
   * Replace the info steps with the supplied source steps.
   *
   * @param infoSteps
   */
  public void setInfoSteps( StepMeta[] infoSteps );

  /**
   * Add a stream to the steps I/O interface
   *
   * @param stream
   *          The stream to add
   */
  public void addStream( StreamInterface stream );

  /**
   * Set the general info stream description
   *
   * @param string
   *          the info streams description
   */
  public void setGeneralInfoDescription( String string );

  /**
   * Set the general target stream description
   *
   * @param string
   *          the target streams description
   */
  public void setGeneralTargetDescription( String string );

  /**
   * @return the generalTargetDescription
   */
  public String getGeneralTargetDescription();

  /**
   * @return the generalInfoDescription
   */
  public String getGeneralInfoDescription();

  /**
   * @return true if the output targets of this step are dynamic (variable)
   */
  public boolean isOutputDynamic();

  /**
   * @param outputDynamic
   *          set to true if the output targets of this step are dynamic (variable)
   */
  public void setOutputDynamic( boolean outputDynamic );

  /**
   * @return true if the input info sources of this step are dynamic (variable)
   */
  public boolean isInputDynamic();

  /**
   * @param inputDynamic
   *          set to true if the input info sources of this step are dynamic (variable)
   */
  public void setInputDynamic( boolean inputDynamic );

  public StreamInterface findTargetStream( StepMeta targetStep );

  public StreamInterface findInfoStream( StepMeta infoStep );
}
