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

/**
 * This class contains options that control the testing of VFS Connection.
 */
public class VFSConnectionTestOptions {

  private boolean rootPathIgnored;

  public VFSConnectionTestOptions() {
  }

  public VFSConnectionTestOptions( boolean rootPathIgnored ) {
    this.rootPathIgnored = rootPathIgnored;
  }

  /**
   * Indicates if the root path should be ignored when testing the connection.
   * @return {@code true}, if the root path should be ignored; {@code false}, otherwise.
   */
  public boolean isRootPathIgnored() {
    return rootPathIgnored;
  }


  /**
   * Sets if the root path should be ignored when testing the connection.
   * @param rootPathIgnored The root path ignored flag.
   * {@code true} to ignore the root path; {@code false}, otherwise.
   */
  public void setRootPathIgnored( boolean rootPathIgnored ) {
    this.rootPathIgnored = rootPathIgnored;
  }
}
