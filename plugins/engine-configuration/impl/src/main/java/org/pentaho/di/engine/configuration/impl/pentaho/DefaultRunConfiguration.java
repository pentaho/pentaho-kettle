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


package org.pentaho.di.engine.configuration.impl.pentaho;

import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationUI;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

/**
 * Created by bmorrise on 3/15/17.
 */
@MetaStoreElementType(
  name = "Default Run Configuration",
  description = "Defines a default run configuration" )
public class DefaultRunConfiguration implements RunConfiguration {

  public static final String TYPE = "Pentaho";

  @MetaStoreAttribute
  private boolean local = true;

  @MetaStoreAttribute
  private boolean remote = false;

  @MetaStoreAttribute
  private boolean clustered = false;

  @MetaStoreAttribute
  private boolean pentaho = false;

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

  public boolean isPentaho() {
    return pentaho;
  }

  public void setPentaho( boolean pentaho ) {
    this.pentaho = pentaho;
  }

  @Override public RunConfigurationUI getUI() {
    return new DefaultRunConfigurationUI( this );
  }
}
