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


package org.pentaho.di.repository;

import java.util.Date;

/**
 * A revision is simply a name, a commit comment and a date
 *
 * @author matt
 *
 */
public interface ObjectRevision {

  /**
   * @return The internal name or number of the revision
   */
  public String getName();

  /**
   * @return The creation date of the revision
   */
  public Date getCreationDate();

  /**
   * @return The revision comment
   */
  public String getComment();

  /**
   * @return The user that caused the revision
   */
  public String getLogin();

}
