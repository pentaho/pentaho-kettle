/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
