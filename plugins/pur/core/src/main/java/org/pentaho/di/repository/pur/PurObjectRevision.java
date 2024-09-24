/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.repository.pur;

import java.io.Serializable;
import java.util.Date;

import org.pentaho.di.repository.ObjectRevision;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
