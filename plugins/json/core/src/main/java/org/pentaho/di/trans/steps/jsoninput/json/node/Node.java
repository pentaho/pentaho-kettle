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


package org.pentaho.di.trans.steps.jsoninput.json.node;

import java.util.List;

/**
 * Created by bmorrise on 8/7/18.
 */
public abstract class Node {

  protected String key;

  protected Node( String key ) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public void setKey( String key ) {
    this.key = key;
  }

  public abstract void dedupe();
  public abstract String getType();
  public abstract List<Node> getChildren();
}
