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



package org.pentaho.di.trans.steps.jsoninput.json;

/**
 * Configuration options for JsonSampler
 *
 * Created by bmorrise on 7/27/18.
 */
public class Configuration {

  private static final int DEFAULT_LINES = 100;
  private int lines = DEFAULT_LINES;
  private boolean dedupe = true;

  public int getLines() {
    return lines;
  }

  public void setLines( int lines ) {
    this.lines = lines;
  }

  public boolean isDedupe() {
    return dedupe;
  }

  public void setDedupe( boolean dedupe ) {
    this.dedupe = dedupe;
  }
}
