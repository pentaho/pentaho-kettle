/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.repository;

import org.xml.sax.SAXParseException;

public interface RepositoryElementReadListener {
  public boolean transformationElementRead( String xml, RepositoryImportFeedbackInterface feedback );

  public boolean jobElementRead( String xml, RepositoryImportFeedbackInterface feedback );

  public void fatalXmlErrorEncountered( SAXParseException e );
}
