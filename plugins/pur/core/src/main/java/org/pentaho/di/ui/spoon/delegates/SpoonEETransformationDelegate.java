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
