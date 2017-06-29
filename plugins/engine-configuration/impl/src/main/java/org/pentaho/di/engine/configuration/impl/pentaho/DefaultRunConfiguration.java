/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.di.engine.configuration.impl.pentaho;

import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

/**
 * Created by bmorrise on 3/15/17.
 */
@MetaStoreElementType(
  name = "Default Run Configuration",
  description = "Defines a default run configuration" )
public class DefaultRunConfiguration implements RunConfiguration {

  public static String TYPE = "Pentaho";

  @MetaStoreAttribute
  private boolean local = true;

  @MetaStoreAttribute
  private boolean remote = false;

  @MetaStoreAttribute
  private boolean clustered = false;

  @MetaStoreAttribute
  private String server;

  @MetaStoreAttribute
  private boolean sendResources = false;

  @MetaStoreAttribute
  private boolean logRemoteExecutionLocally = false;

  @MetaStoreAttribute
  private boolean showTransformations = false;

  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  private String description;

  @MetaStoreAttribute
  private boolean readOnly;

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getType() {
    return TYPE;
  }

  public boolean isLocal() {
    return local;
  }

  public void setLocal( boolean local ) {
    this.local = local;
  }

  public boolean isClustered() {
    return clustered;
  }

  public void setClustered( boolean clustered ) {
    this.clustered = clustered;
  }

  public boolean isRemote() {
    return remote;
  }

  public void setRemote( boolean remote ) {
    this.remote = remote;
  }

  public String getServer() {
    return server;
  }

  public void setServer( String server ) {
    this.server = server;
  }

  public boolean isLogRemoteExecutionLocally() {
    return logRemoteExecutionLocally;
  }

  public void setLogRemoteExecutionLocally( boolean logRemoteExecutionLocally ) {
    this.logRemoteExecutionLocally = logRemoteExecutionLocally;
  }

  public boolean isShowTransformations() {
    return showTransformations;
  }

  public void setShowTransformations( boolean showTransformations ) {
    this.showTransformations = showTransformations;
  }

  @Override public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly( boolean readOnly ) {
    this.readOnly = readOnly;
  }

  public boolean isSendResources() {
    return sendResources;
  }

  public void setSendResources( boolean sendResources ) {
    this.sendResources = sendResources;
  }
}
