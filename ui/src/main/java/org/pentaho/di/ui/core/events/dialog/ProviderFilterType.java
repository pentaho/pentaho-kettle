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

package org.pentaho.di.ui.core.events.dialog;

/**
 * Provider Filter options for providers that are desired to be included.
 */
public enum ProviderFilterType {
  ALL_PROVIDERS, DEFAULT, CLUSTERS, LOCAL, REPOSITORY, VFS, RECENTS;

  @Override
  public String toString() {
    return name().toLowerCase();
  }

  public static String[] getDefaults() {
    return new String[] { LOCAL.toString(), VFS.toString(), CLUSTERS.toString() };
  }
}
