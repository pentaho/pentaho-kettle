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

package org.pentaho.di.ui.repository.pur.model;

import org.pentaho.ui.xul.XulEventSourceAdapter;

/**
 * @author rmansoor
 * 
 */
public class RepositoryConfigModel extends XulEventSourceAdapter implements java.io.Serializable {

  private static final long serialVersionUID = 1018425117620046943L; /* EESOURCE: UPDATE SERIALVERUID */
  private String url;
  private String id;
  private String name;
  private boolean modificationComments;

  /**
   * 
   */
  public RepositoryConfigModel() {
    // TODO Auto-generated constructor stub
  }

  public String getUrl() {
    return url;
  }

  public void setUrl( String url ) {
    String previousVal = this.url;
    this.url = url;
    this.firePropertyChange( "url", previousVal, url ); //$NON-NLS-1$
    checkIfModelValid();
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    String previousVal = this.name;
    this.name = name;
    this.firePropertyChange( "name", previousVal, name ); //$NON-NLS-1$
    checkIfModelValid();
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    String previousVal = this.id;
    this.id = id;
    this.firePropertyChange( "id", previousVal, id );//$NON-NLS-1$
    checkIfModelValid();
  }

  public boolean isModificationComments() {
    return modificationComments;
  }

  public void setModificationComments( boolean modificationComments ) {
    boolean previousVal = this.modificationComments;
    this.modificationComments = modificationComments;
    this.firePropertyChange( "modificationComments", previousVal, modificationComments );//$NON-NLS-1$
  }

  public void clear() {
    setUrl( "" );//$NON-NLS-1$
    setId( "" );//$NON-NLS-1$
    setName( "" );//$NON-NLS-1$
    setModificationComments( true );
  }

  public void checkIfModelValid() {
    this.firePropertyChange( "valid", null, isValid() );//$NON-NLS-1$
  }

  public boolean isValid() {
    return url != null && url.length() > 0 && id != null && id.length() > 0 && name != null && name.length() > 0;
  }
}
