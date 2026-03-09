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

  public SpoonEETransformationDelegate( Spoon spoon ) {
    super( spoon );
  }

  /**
   * Retrieves the current {@link ILockService} from the repository. This must be fetched each time rather than
   * cached, because after a session timeout and reconnection the repository creates new service instances backed
   * by fresh web-service stubs. Holding a stale reference would cause
   * {@code "close method has already been invoked"} errors.
   *
   * @return the lock service, or {@code null} if unavailable
   */
  private ILockService getLockService() {
    Repository repository = spoon.getRepository();
    if ( repository == null ) {
      return null;
    }
    try {
      if ( repository.hasService( ILockService.class ) ) {
        return (ILockService) repository.getService( ILockService.class );
      }
    } catch ( KettleException e ) {
      log.logDebug( "Unable to retrieve ILockService from repository", e );
    }
    return null;
  }

  @Override
  public void addTransGraph( TransMeta transMeta ) {
    super.addTransGraph( transMeta );
    TabMapEntry tabEntry = spoon.delegates.tabs.findTabMapEntry( transMeta );
    if ( tabEntry != null ) {
      TabItem tabItem = tabEntry.getTabItem();
      try {
        ILockService service = getLockService();
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
