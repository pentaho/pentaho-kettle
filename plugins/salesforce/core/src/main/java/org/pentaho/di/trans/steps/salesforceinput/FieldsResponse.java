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


package org.pentaho.di.trans.steps.salesforceinput;

import java.util.List;
import java.util.Set;

public class FieldsResponse {

  public List<FieldDTO> fieldDTOList;
  public Set<String> fieldNames;

  public FieldsResponse( List<FieldDTO> fieldDTOList, Set<String> fieldNames ) {
    this.fieldDTOList = fieldDTOList;
    this.fieldNames = fieldNames;
  }
}
