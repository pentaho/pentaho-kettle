/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.trans;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.repository.Repository;

public class DefaultTransFactory implements TransFactory {

  /**
   * Creates a {@link Trans} Object for the registered Factory type
   * @param parent
   * @param rep
   * @param name
   * @param dirname
   * @param filename
   * @param parentTransMeta
   * @return Trans
   */
  @Override
  public Trans create( TransMeta parent, Repository rep, String name, String dirname, String filename,
    TransMeta parentTransMeta ) throws KettleException {
    return new Trans( parent, rep, name, dirname, filename, parentTransMeta );
  }

  /**
   * Creates a {@link Trans} Object for the registered Factory type
   * @param transMeta
   * @param log
   * @return Trans
   */
  @Override
  public Trans create( TransMeta transMeta, LoggingObjectInterface log )
    throws KettleException {
    return new Trans( transMeta, log );
  }
}
