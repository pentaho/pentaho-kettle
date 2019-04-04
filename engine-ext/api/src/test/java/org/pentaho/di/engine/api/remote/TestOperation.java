/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */
package org.pentaho.di.engine.api.remote;

import org.pentaho.di.engine.api.model.Hop;
import org.pentaho.di.engine.api.model.Operation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ccaspanello on 8/14/17.
 */
public class TestOperation implements Operation {

  private final String id;
  private Map<String, Serializable> config;
  private List<Operation> from;
  private List<Operation> to;
  private List<Hop> hopsIn;
  private List<Hop> hopsOut;

  public TestOperation( String id ) {
    this.id = id;
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
