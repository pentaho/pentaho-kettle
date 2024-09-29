/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.spoon;

import org.eclipse.swt.widgets.Composite;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.impl.XulEventHandler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.*;


public class SpoonPerspectiveManagerTest {
  private static final String PERSPECTIVE_ID = "perspective-id";
  private static final String PERSPECTIVE_NAME = "perspective-name";

  private Map<SpoonPerspective, SpoonPerspectiveManager.PerspectiveManager> perspectiveManagerMap;
  private static SpoonPerspectiveManager spoonPerspectiveManager;
  private SpoonPerspective perspective;

  @Before
  public void setUp() throws Exception {
    spoonPerspectiveManager = SpoonPerspectiveManager.getInstance();
    spoonPerspectiveManager = spy( spoonPerspectiveManager );

    perspective = new DummyPerspective();
    spoonPerspectiveManager.addPerspective( perspective );

    // emulate we have one perspective, that is not inited yet.
    perspectiveManagerMap = emulatePerspectiveManagerMap( perspective );

    doReturn( perspectiveManagerMap ).when( spoonPerspectiveManager ).getPerspectiveManagerMap();
    doReturn( mock( Spoon.class ) ).when( spoonPerspectiveManager ).getSpoon();
    spoonPerspectiveManager.setDeck( mock( XulDeck.class ) );
    doReturn( mock( LogChannelInterface.class ) ).when( spoonPerspectiveManager ).getLogger();
  }


  @Test
  public void perspectiveIsInitializedOnlyOnce() throws KettleException {
    SpoonPerspectiveManager.PerspectiveManager perspectiveManager = perspectiveManagerMap.get( perspective );

    spoonPerspectiveManager.activatePerspective( perspective.getClass() );
    // it's the first time this perspective gets active, so it should be initialized after this call
    verify( perspectiveManager ).performInit();

    spoonPerspectiveManager.activatePerspective( perspective.getClass() );
    // make sure that perspective was inited only after first activation
    verify( perspectiveManager ).performInit();
  }


  @Test
  public void hidePerspective() {
    SpoonPerspectiveManager.PerspectiveManager perspectiveManager = perspectiveManagerMap.get( perspective );
    spoonPerspectiveManager.hidePerspective( perspective.getId() );

    verify( perspectiveManager ).setPerspectiveHidden( PERSPECTIVE_NAME, true );
  }

  @Test
  public void showPerspective() {
    SpoonPerspectiveManager.PerspectiveManager perspectiveManager = perspectiveManagerMap.get( perspective );
    spoonPerspectiveManager.showPerspective( perspective.getId() );

    verify( perspectiveManager ).setPerspectiveHidden( PERSPECTIVE_NAME, false );
  }



  private Map<SpoonPerspective, SpoonPerspectiveManager.PerspectiveManager> emulatePerspectiveManagerMap(
    SpoonPerspective... perspectives ) {
    Map<SpoonPerspective, SpoonPerspectiveManager.PerspectiveManager> spoonPerspectiveManagerMap = new HashMap<>();

    for ( SpoonPerspective perspective : perspectives ) {
      spoonPerspectiveManagerMap.put( perspective, createPerspectiveManager( perspective ) );
    }
    return spoonPerspectiveManagerMap;
  }

  private SpoonPerspectiveManager.PerspectiveManager createPerspectiveManager( SpoonPerspective perspective ) {
    List<SpoonPerspectiveManager.PerspectiveData> perspectiveDatas = new ArrayList<SpoonPerspectiveManager.PerspectiveData>();
    perspectiveDatas.add( new SpoonPerspectiveManager.PerspectiveData( PERSPECTIVE_NAME, PERSPECTIVE_ID ) );
    SpoonPerspectiveManager.PerspectiveManager perspectiveManager =
      new SpoonPerspectiveManager.PerspectiveManager( perspective, mock( XulVbox.class ), mock( XulToolbar.class ),
        perspectiveDatas, perspective.getDisplayName( Locale.getDefault() ) );

    perspectiveManager = spy( perspectiveManager );
    doNothing().when( perspectiveManager ).performInit();

    return perspectiveManager;
  }


  private class DummyPerspective implements SpoonPerspective {

    @Override public String getId() {
      return PERSPECTIVE_ID;
    }

    @Override public Composite getUI() {
      return null;
    }

    @Override public String getDisplayName( Locale l ) {
      return PERSPECTIVE_NAME;
    }

    @Override public InputStream getPerspectiveIcon() {
      return null;
    }

    @Override public void setActive( boolean active ) {

    }

    @Override public List<XulOverlay> getOverlays() {
      return null;
    }

    @Override public List<XulEventHandler> getEventHandlers() {
      return null;
    }

    @Override public void addPerspectiveListener( SpoonPerspectiveListener listener ) {

    }

    @Override public EngineMetaInterface getActiveMeta() {
      return null;
    }
  }

}
