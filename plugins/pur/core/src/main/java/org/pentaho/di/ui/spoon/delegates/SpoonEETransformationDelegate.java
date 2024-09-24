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
package org.pentaho.di.ui.spoon.delegates;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.repository.pur.services.ILockService;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.xul.swt.tab.TabItem;

public class SpoonEETransformationDelegate extends SpoonTransformationDelegate implements java.io.Serializable {

  private static final long serialVersionUID = -2067024540694335010L; /* EESOURCE: UPDATE SERIALVERUID */

  ILockService service;

  public SpoonEETransformationDelegate( Spoon spoon ) {
    super( spoon );
    Repository repository = spoon.getRepository();
    try {
      if ( repository.hasService( ILockService.class ) ) {
        service = (ILockService) repository.getService( ILockService.class );
      } else {
        throw new IllegalStateException();
      }
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public void addTransGraph( TransMeta transMeta ) {
    super.addTransGraph( transMeta );
    TabMapEntry tabEntry = spoon.delegates.tabs.findTabMapEntry( transMeta );
    if ( tabEntry != null ) {
      TabItem tabItem = tabEntry.getTabItem();
      try {
        if ( ( service != null ) && ( transMeta.getObjectId() != null )
            && ( service.getTransformationLock( transMeta.getObjectId() ) != null ) ) {
          tabItem.setImage( GUIResource.getInstance().getImageLocked() );
        }
      } catch ( Exception e ) {
        throw new RuntimeException( e );
      }
    }
  }

}
