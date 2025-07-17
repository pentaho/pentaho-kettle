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

package org.pentaho.di.repository.pur;

import java.io.Serializable;
import java.util.Date;

import org.pentaho.di.repository.ObjectRevision;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement( name = "revision" )
@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = { "versionId", "creationDate", "login", "comment" } )
public class PurObjectRevision implements ObjectRevision, java.io.Serializable {

  private static final long serialVersionUID = -7857510728831225268L; /* EESOURCE: UPDATE SERIALVERUID */

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private String comment;

  private Date creationDate;

  private String login;

  @XmlElement( type = String.class )
  private Serializable versionId;

  // ~ Constructors ====================================================================================================

  public PurObjectRevision() {
    super();
    this.versionId = null;
    this.login = null;
    // defensive copy
    this.creationDate = ( creationDate != null ? new Date( creationDate.getTime() ) : null );
    this.comment = null;
  }

  public PurObjectRevision( final Serializable versionId, final String login, final Date creationDate,
      final String comment ) {
    super();
    this.versionId = versionId;
    this.login = login;
    // defensive copy
    this.creationDate = ( creationDate != null ? new Date( creationDate.getTime() ) : null );
    this.comment = comment;
  }

  // ~ Methods =========================================================================================================

  public String getComment() {
    return comment;
  }

  public Date getCreationDate() {
    // defensive copy
    return creationDate != null ? new Date( creationDate.getTime() ) : null;
  }

  public String getLogin() {
    return login;
  }

  public String getName() {
    return versionId != null ? versionId.toString() : null;
  }

}
