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


package org.pentaho.di.connections.vfs;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang.StringUtils;
import org.pentaho.metastore.persist.MetaStoreAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class to put common fields for all types of VFS Connection Details to avoid code duplication
 */
public abstract class BaseVFSConnectionDetails implements VFSConnectionDetails {

  @NonNull
  @MetaStoreAttribute
  private List<String> baRoles = new ArrayList<>();

  @MetaStoreAttribute
  private String rootPath;

  @NonNull
  @Override
  public List<String> getBaRoles() {
    return baRoles;
  }

  @Override
  public Map<String, String> getProperties() {
    Map<String, String> props = new HashMap<>();
    fillProperties( props );
    return props;
  }

  /**
   * Gets if the VFS connection supports root path or not.
   * @returns {@code true} if VFS connection supports root path; {@code false} otherwise.
   * @default {@code true}
   */
  @Override
  public boolean isRootPathSupported() {
    return true;
  }

  @Override
  public String getRootPath() {
    return rootPath;
  }

  public void setRootPath( String rootPath ) {
    this.rootPath = StringUtils.isEmpty( rootPath ) ? null : rootPath;
  }

  /**
   * Adds base/default properties to properties of connection instance.
   * <p>
   * @param props The properties map
   */
  protected void fillProperties( Map<String, String> props ) {
    props.put( "baRoles", getBaRoles().toString() );
    props.put( "rootPath", getRootPath() );
  }
}
