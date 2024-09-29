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

package org.pentaho.di.engine.api.remote;

import org.pentaho.di.engine.api.model.Hop;
import org.pentaho.di.engine.api.model.Operation;

import java.io.Serializable;
import java.util.*;

/**
 * Created by ccaspanello on 8/14/17.
 */
public class TestOperation implements Operation {

  private final String id;
  private final String key;
  private Map<String, Serializable> config;
  private List<Operation> from;
  private List<Operation> to;
  private List<Hop> hopsIn;
  private List<Hop> hopsOut;

  public TestOperation( String id ) {
    this.id = id;
    this.key = id + UUID.randomUUID();
    this.config = new HashMap<>();
    this.from = new ArrayList<>();
    this.to = new ArrayList<>();
    this.hopsIn = new ArrayList<>();
    this.hopsOut = new ArrayList<>();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override public Map<String, Serializable> getConfig() {
    return config;
  }

  @Override public void setConfig( String key, Serializable value ) {
    this.config.put( key, value );
  }

  @Override public List<Operation> getFrom() {
    return from;
  }

  @Override public List<Operation> getTo() {
    return to;
  }

  @Override public List<Hop> getHopsIn() {
    return hopsIn;
  }

  @Override public List<Hop> getHopsOut() {
    return hopsOut;
  }

  @Override public void setConfig( Map<String, Serializable> config ) {
    this.config = config;
  }

  public void setFrom( List<Operation> from ) {
    this.from = from;
  }

  public void setTo( List<Operation> to ) {
    this.to = to;
  }
}
