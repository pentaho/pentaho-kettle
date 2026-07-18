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



package org.pentaho.di.core.svg;

import org.w3c.dom.Document;

/**
 * Container for SVG image.
 */
public class SvgImage {
  private final Document document;

  protected SvgImage( Document doc ) {
    this.document = doc;
  }

  public Document getDocument() {
    return document;
  }
}
