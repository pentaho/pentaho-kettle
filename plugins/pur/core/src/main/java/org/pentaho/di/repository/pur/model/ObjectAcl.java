/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.repository.pur.model;

import java.util.List;

import org.pentaho.di.repository.ObjectRecipient;

public interface ObjectAcl {

  public List<ObjectAce> getAces();

  public ObjectRecipient getOwner();

  public boolean isEntriesInheriting();

  public void setAces( List<ObjectAce> aces );

  public void setOwner( ObjectRecipient owner );

  public void setEntriesInheriting( boolean entriesInheriting );
}
