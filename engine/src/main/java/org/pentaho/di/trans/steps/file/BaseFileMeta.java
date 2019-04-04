/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.file;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * A base parent class for all file based metas.
 */
public abstract class BaseFileMeta extends BaseStepMeta implements StepMetaInterface {


  /**
   * Whether to push the output into the output of a servlet with the executeTrans Carte/DI-Server servlet
   */
  protected boolean servletOutput;

  /**
   * @param showSamples determines whether the paths being returned are dummy samples or the "template" representation.
   *                    The samples woulc include things like sample step number, partition number etc (
   *                    filename_0_1.txt) , while the non-sample path would include a token (
   *                    filename_&lt;step&gt;_&lt;partition&gt;.&lt;extension&gt;)
   * @return An array of file paths
   */
  public abstract String[] getFilePaths( final boolean showSamples );

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean passDataToServletOutput() {
    return servletOutput;
  }

  /**
   * Returns true if the given step writes to a file, and false otherwise.
   *
   * @return true if the given step writes to a file, and false otherwise
   */
  public boolean writesToFile() {
    return !passDataToServletOutput();
  }
}
