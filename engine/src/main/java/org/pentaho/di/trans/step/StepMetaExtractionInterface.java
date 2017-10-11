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

import org.pentaho.di.core.exception.KettleException;

/**
 * This interface allows an external program to inject metadata using a standard flat set of metadata attributes.
 *
 * @author matt
 *
 */
public interface StepMetaExtractionInterface {
  /**
   * @return A list of step injection metadata entries. In case the data type of the entry is NONE (0) you will get at
   *         least one entry in the details section. You can use this list
   */
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException;

  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> metadata ) throws KettleException;
}
