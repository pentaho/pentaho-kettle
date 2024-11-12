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


package org.pentaho.di.trans.steps.avro.input;

import org.pentaho.di.trans.steps.file.BaseFileInputFiles;

/**
 * Base class for format's input file - env added.
 * 
 * @author <alexander_buloichik@epam.com>
 */
public class FormatInputFile extends BaseFileInputFiles {

  public String[] environment = {};

  /**
   * we need to reallocate {@link #environment} too since it can have other length
   */
  @Override
  public void normalizeAllocation( int length ) {
    super.normalizeAllocation( length );
    environment = normalizeAllocation( environment, length );
  }

}
