/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.GlobalMessageUtil;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.tags.SwtDeck;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Singleton Object controlling SpoonPerspectives.
 *
 * A Perspective is an optional Spoon mode that can be added by a SpoonPlugin. Perspectives take over the look of the
 * entire application by replacing the main UI area.
 *
 * @author nbaker
 *
 */
public class SpoonPerspectiveManager {

  private static Class<?> PKG = SpoonPerspectiveManager.class;

  private static SpoonPerspectiveManager instance = new SpoonPerspectiveManager();

  private final Map<Class<? extends SpoonPerspective>, SpoonPerspective> perspectives;

  private final Map<SpoonPerspective, PerspectiveManager> perspectiveManagerMap;

  private final LinkedHashSet<SpoonPerspective> orderedPerspectives;

  private XulDeck deck;

  private SpoonPerspective activePerspective;

  private XulDomContainer domContainer;

  private boolean forcePerspective = false;

  private String startupPerspective = null;

  private final LogChannelInterface log = new LogChannel( this );

  private String[] defaultDisabled = new String[] { "schedulerPerspective" };

  public String getStartupPerspective() {
    return startupPerspective;
  }

  public void setStartupPerspective( String startupPerspective ) {
    this.startupPerspective = startupPerspective;
  }

  Map<SpoonPerspective, PerspectiveManager> getPerspectiveManagerMap() {
    return Collections.unmodifiableMap( perspectiveManagerMap );
  }

  protected static class SpoonPerspectiveComparator implements Comparator<SpoonPerspective> {
    public int compare( SpoonPerspective o1, SpoonPerspective o2 ) {
      return o1.getId().compareTo( o2.getId() );
    }
  }

  static class PerspectiveManager {
    private final SpoonPerspective per;

    private final XulVbox box;

    private final XulToolbar mainToolbar;
    private final List<PerspectiveData> perspectiveList;
    private final String name;
    private boolean initialized;

    public PerspectiveManager( SpoonPerspective per, XulVbox box, XulToolbar mainToolbar,
                               List<PerspectiveData> perspectiveList, String name ) {
      super();
      this.per = per;
      this.box = box;
      this.mainToolbar = mainToolbar;
      this.perspectiveList = perspectiveList;
      this.name = name;
      initialized = false;
    }

    public void initializeIfNeeded() {
      if ( !initialized ) {
        performInit();
        initialized = true;
      }
    }

    void performInit() {
      per.getUI().setParent( (Composite) box.getManagedObject() );
      per.getUI().layout();
      ( (Composite) mainToolbar.getManagedObject() ).layout( true, true );
    }

    /**
     * Sets hidden to true for {@code perspectiveName} from {@code perspectiveList}
     */
    void setPerspectiveHidden( final String perspectiveName, boolean hidden ) {
      for ( PerspectiveData perspectiveData : perspectiveList ) {
        if ( perspectiveData.getName().equals( perspectiveName ) ) {
          perspectiveData.setHidden( hidden );
        }
      }
    }
  }

  private SpoonPerspectiveManager() {
    perspectives = new LinkedHashMap<Class<? extends SpoonPerspective>, SpoonPerspective>();
    perspectiveManagerMap = new HashMap<SpoonPerspective, PerspectiveManager>();
    orderedPerspectives = new LinkedHashSet<SpoonPerspective>();
  }

  /**
   * Returns the single instance of this class.
   *
   * @return SpoonPerspectiveManager instance.
   */
  public static SpoonPerspectiveManager getInstance() {
    if ( Const.isRunningOnWebspoonMode() ) {
      try {
        Class webSpoonUtils = Class.forName( "org.pentaho.di.webspoon.WebSpoonUtils" );
        Method getUISession = webSpoonUtils.getDeclaredMethod( "getUISession" );
        Class singletonUtil = Class.forName( "org.eclipse.rap.rwt.SingletonUtil" );
        Method getUniqueInstance = Arrays.stream( singletonUtil.getDeclaredMethods() )
                .filter( method -> method.getName().equals( "getUniqueInstance" ) && method.toGenericString().contains( "UISession" ) )
                .collect( Collectors.toList() ).get( 0 );
        return (SpoonPerspectiveManager) getUniqueInstance.invoke( null, SpoonPerspectiveManager.class, getUISession.invoke( null ) );
      } catch ( ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e ) {
        e.printStackTrace();
        return null;
      }
    } else {
      return instance;
    }
  }

  /**
   * Sets the deck used by the Perspective Manager to display Perspectives in.
   *
   * @param deck
   */
  public void setDeck( XulDeck deck ) {
    this.deck = deck;
  }

  /**
   * Receives the main XUL document comprising the menuing system and main layout of Spoon. Perspectives are able to
   * modify these areas when activated. Any other areas need to be modified via a SpoonPlugin.
   *
   * @param doc
   */
  public void setXulDoc( XulDomContainer doc ) {
    this.domContainer = doc;
  }

  /**
   * Adds a SpoonPerspective making it available to be activated later.
   *
   * @param perspective
   */
  public void addPerspective( SpoonPerspective perspective ) {
    if ( activePerspective == null ) {
      activePerspective = perspective;
    }
    perspectives.put( perspective.getClass(), perspective );
    orderedPerspectives.add( perspective );
    if ( domContainer != null ) {
      initialize();
    }
  }

  /**
   * Changes perspective visibility due to {@code hidePerspective} value.
   * If perspective exists already, and we want to make it visible, no new perspective will be added.
   *
   */
  private void changePerspectiveVisibility( final String perspectiveId, boolean hidePerspective ) {
    PerspectiveManager perspectiveManager;

    for ( SpoonPerspective sp : getPerspectiveManagerMap().keySet() ) {
      if ( sp.getId().equals( perspectiveId ) ) {
        perspectiveManager = getPerspectiveManagerMap().get( sp );
        perspectiveManager.setPerspectiveHidden( sp.getDisplayName( Locale.getDefault() ), hidePerspective );
        return;
      }
    }

    getLogger().logError( "Perspective with " + perspectiveId + " is not found." );
  }

  /**
   * Shows perspective with {@code perspectiveId} if it is not shown yet.
   */
  public void showPerspective( final String perspectiveId ) {
    changePerspectiveVisibility( perspectiveId, false );
  }

  /**
   * Hides perspective with {@code perspectiveId}.
   */
  public void hidePerspective( final String perspectiveId ) {
    changePerspectiveVisibility( perspectiveId, true );
  }

  /**
   * Returns an unmodifiable List of perspectives in no set order.
   *
   * @return
   */
  public List<SpoonPerspective> getPerspectives() {
    return Collections.unmodifiableList( new ArrayList<SpoonPerspective>( orderedPerspectives ) );
  }

  private void unloadPerspective( SpoonPerspective per ) {
    per.setActive( false );
    List<XulOverlay> overlays = per.getOverlays();
    if ( overlays != null ) {
      for ( XulOverlay overlay : overlays ) {
        try {
          domContainer.removeOverlay( overlay.getOverlayUri() );
        } catch ( XulException e ) {
          log.logError( "Error unload perspective", e );
        }
      }
    }
    getSpoon().enableMenus();
  }

  /**
   * Activates the given instance of the class literal passed in. Activating a perspective first deactivates the current
   * perspective removing any overlays its applied to the UI. It then switches the main deck to display the perspective
   * UI and applies the optional overlays to the main Spoon XUL container.
   *
   * @param clazz
   *          SpoonPerspective class literal
   * @throws KettleException
   *           throws a KettleException if no perspective is found for the given parameter
   */
  public void activatePerspective( Class<? extends SpoonPerspective> clazz ) throws KettleException {

    if ( this.forcePerspective ) {
      // we are currently prevented from switching perspectives
      return;
    }
    SpoonPerspective sp = perspectives.get( clazz );
    if ( sp == null ) {
      throw new KettleException( "Could not locate perspective by class: " + clazz );
    }
    PerspectiveManager perspectiveManager = getPerspectiveManagerMap().get( sp );
    if ( perspectiveManager != null ) {
      perspectiveManager.initializeIfNeeded();
    }
    unloadPerspective( activePerspective );
    activePerspective = sp;

    List<XulOverlay> overlays = sp.getOverlays();
    if ( overlays != null ) {
      for ( XulOverlay overlay : overlays ) {
        try {
          ResourceBundle res = null;
          if ( overlay.getResourceBundleUri() != null ) {
            try {
              res = GlobalMessageUtil.getBundle( overlay.getResourceBundleUri(), SpoonPerspectiveManager.class );
            } catch ( MissingResourceException ignored ) {
              // Ignore errors
            }
          } else {
            try {
              res = GlobalMessageUtil.getBundle( overlay.getOverlayUri().replace( ".xul", ".properties" ),
                SpoonPerspectiveManager.class );
            } catch ( MissingResourceException ignored ) {
              // Ignore errors
            }
          }
          if ( res == null ) {
            res = new XulSpoonResourceBundle( sp.getClass() );
          }
          domContainer.loadOverlay( overlay.getOverlayUri(), res );
        } catch ( XulException e ) {
          log.logError( "Error activate perspective", e );
        }
      }
    }

    List<XulEventHandler> theXulEventHandlers = sp.getEventHandlers();
    if ( theXulEventHandlers != null ) {
      for ( XulEventHandler handler : theXulEventHandlers ) {
        domContainer.addEventHandler( handler );
      }
    }

    sp.setActive( true );
    if ( sp.equals( activePerspective ) ) {
      deck.setSelectedIndex( deck.getChildNodes().indexOf( deck.getElementById( "perspective-" + sp.getId() ) ) );
      getSpoon().enableMenus();
    }
  }

  /**
   * Returns the current active perspective.
   *
   * @return active SpoonPerspective
   */
  public SpoonPerspective getActivePerspective() {
    return activePerspective;
  }

  /**
   * Returns whether this perspective manager is prevented from switching perspectives
   */
  public boolean isForcePerspective() {
    return forcePerspective;
  }

  /**
   * Sets whether this perspective manager is prevented from switching perspectives. This is used when a startup
   * perspective is requested on the command line parameter to prevent other perpsectives from openeing.
   */
  public void setForcePerspective( boolean forcePerspective ) {
    this.forcePerspective = forcePerspective;
  }

  public void removePerspective( SpoonPerspective per ) {
    perspectives.remove( per );
    orderedPerspectives.remove( per );
    Document document = domContainer.getDocumentRoot();

    XulComponent comp = document.getElementById( "perspective-" + per.getId() );
    comp.getParent().removeChild( comp );

    comp = document.getElementById( "perspective-btn-" + per.getId() );
    comp.getParent().removeChild( comp );
    XulToolbar mainToolbar = (XulToolbar) domContainer.getDocumentRoot().getElementById( "main-toolbar" );
    ( (Composite) mainToolbar.getManagedObject() ).layout( true, true );

    deck.setSelectedIndex( 0 );

  }

  private List<SpoonPerspective> installedPerspectives = new ArrayList<SpoonPerspective>();

  public void initialize() {
    XulToolbar mainToolbar = (XulToolbar) domContainer.getDocumentRoot().getElementById( "main-toolbar" );
    SwtDeck deck = (SwtDeck) domContainer.getDocumentRoot().getElementById( "canvas-deck" );

    int y = 0;
    int perspectiveIdx = 0;
    Class<? extends SpoonPerspective> perClass = null;

    List<SpoonPerspective> perspectives = getPerspectives();
    if ( this.startupPerspective != null ) {
      for ( int i = 0; i < perspectives.size(); i++ ) {
        if ( perspectives.get( i ).getId().equals( this.startupPerspective ) ) {
          perspectiveIdx = i;
          break;
        }
      }
    }

    final List<PerspectiveData> perspectiveList = new ArrayList<>();

    final ToolBar swtToolbar = (ToolBar) mainToolbar.getManagedObject();
    final Shell shell = swtToolbar.getShell();
    final ToolItem perspectiveButton = new ToolItem( swtToolbar, SWT.DROP_DOWN, 7 );

    perspectiveButton.setImage( GUIResource.getInstance().getImage( "ui/images/perspective_changer.svg" ) );
    perspectiveButton.setToolTipText( BaseMessages.getString( PKG, "Spoon.Menu.View.Perspectives" ) );
    perspectiveButton.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent e ) {
        Menu menu = new Menu( shell );
        for ( final PerspectiveData perspectiveData : perspectiveList ) {
          MenuItem item = new MenuItem( menu, SWT.CHECK );
          if ( perspectiveData.isHidden() ) {
            item.setEnabled( false );
          }
          if ( activePerspective.getId().equals( perspectiveData.getId() ) ) {
            item.setSelection( true );
          }
          item.setText( perspectiveData.getName() );
          item.addSelectionListener( new SelectionAdapter() {
            @Override public void widgetSelected( SelectionEvent selectionEvent ) {
              Spoon.getInstance().loadPerspective( perspectiveData.getId() );
              swtToolbar.forceFocus();
            }
          } );
        }
        ToolItem item = (ToolItem) e.widget;
        Rectangle rect = item.getBounds();
        Point pt = item.getParent().toDisplay( new Point( rect.x, rect.y + rect.height ) );

        menu.setLocation( pt.x, pt.y );
        menu.setVisible( true );

      }
    } );

    for ( final SpoonPerspective per : getPerspectives() ) {
      if ( installedPerspectives.contains( per ) ) {
        y++;
        continue;
      }
      String name = per.getDisplayName( LanguageChoice.getInstance().getDefaultLocale() );
      PerspectiveData perspectiveData = new PerspectiveData( name, per.getId() );
      if ( Arrays.asList( defaultDisabled ).contains( per.getId() ) ) {
        perspectiveData.setHidden( true );
      }
      perspectiveList.add( perspectiveData );

      XulVbox box = deck.createVBoxCard();
      box.setId( "perspective-" + per.getId() );
      box.setFlex( 1 );
      deck.addChild( box );

      PerspectiveManager perspectiveManager =
          new PerspectiveManager( per, box, mainToolbar, perspectiveList, name );
      perspectiveManagerMap.put( per, perspectiveManager );
      // Need to force init for main perspective even if it won't be shown
      if ( perspectiveIdx == y || y == 0 ) {
        if ( perspectiveIdx == y ) {
          // we have a startup perspective. Hold onto the class
          perClass = per.getClass();
        }

        // force init
        perspectiveManager.initializeIfNeeded();
      }
      y++;
      installedPerspectives.add( per );
    }
    deck.setSelectedIndex( perspectiveIdx );
    if ( perClass != null ) {
      // activate the startup perspective
      try {
        activatePerspective( perClass );
        // stop other perspectives from opening
        SpoonPerspectiveManager.getInstance().setForcePerspective( true );
      } catch ( KettleException e ) {
        // TODO Auto-generated catch block
      }
    }
  }

  static class PerspectiveData {
    private String name;
    private String id;
    private boolean hidden = false;

    public PerspectiveData( String name, String id ) {
      this.name = name;
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public String getId() {
      return id;
    }

    public boolean isHidden() {
      return hidden;
    }

    public void setHidden( boolean hidden ) {
      this.hidden = hidden;
    }
  }

  /**
   * For testing
   */
  Spoon getSpoon() {
    return Spoon.getInstance();
  }
  /**
   * For testing
   */
  LogChannelInterface getLogger() {
    return log;
  }
}
