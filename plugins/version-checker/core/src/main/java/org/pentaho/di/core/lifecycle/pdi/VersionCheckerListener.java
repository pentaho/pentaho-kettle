/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2015 - 2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.lifecycle.pdi;

import org.pentaho.di.core.Props;
import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.gui.GUIOption;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginClassTypeMapping;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.versionchecker.BasicVersionCheckerProvider;
import org.pentaho.versionchecker.IVersionCheckErrorHandler;
import org.pentaho.versionchecker.IVersionCheckResultHandler;
import org.pentaho.versionchecker.VersionChecker;

@LifecyclePlugin( id = "VersionChecker", name = "Version checker" )
@PluginClassTypeMapping( classTypes = { GUIOption.class }, implementationClass = { VersionCheckerListener.class } )
public class VersionCheckerListener implements LifecycleListener, GUIOption<Boolean> {
  private static Class<?> PKG = VersionCheckerListener.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private static final String PDI_CHECK_VERSION_FLAG = "pdi.check.version.flag";

  private LifecycleException root;

  private static LogChannelInterface log = new LogChannel( "Version checker" );

  public static final String VERSION_CHECKER = "Version Checker";

  /**
   * Interface method: checks the version
   */
  public void onStart( final LifeEventHandler handler ) throws LifecycleException {
    // Should we run this listener?
    String prop = Props.getInstance().getProperty( PDI_CHECK_VERSION_FLAG );
    if ( prop != null && !Boolean.parseBoolean( prop ) ) {
      log.logBasic( "Skipping version check.", new Object[] {} );
      return;
    }

    try {
      // check to see if pentaho version checker is in the classpath
      //
      Class.forName( "org.pentaho.versionchecker.BasicVersionCheckerProvider" );
    } catch ( ClassNotFoundException e ) {
      return;
    }

    Runnable r = new Runnable() {
      public void run() {
        final BasicVersionCheckerProvider dataProvider = new BasicVersionCheckerProvider( Spoon.class );
        VersionChecker vc = new VersionChecker();
        vc.setDataProvider( dataProvider );
        vc.addResultHandler( new IVersionCheckResultHandler() {
          public void processResults( String result ) {
          }
        } );
        vc.addErrorHandler( new IVersionCheckErrorHandler() {
          public void handleException( Exception e ) {
            root = new LifecycleException( e, false );
          }
        } );

        vc.performCheck( false );

        if ( root != null )
          return;
      }
    };

    new Thread( r ).start();
  }

  public void onExit( LifeEventHandler handler ) throws LifecycleException {
    // nothing
  }

  public Boolean getLastValue() {
    // the default is true
    String prop = Props.getInstance().getProperty( PDI_CHECK_VERSION_FLAG );
    return prop == null ? true : new Boolean( prop );
  }

  public void setValue( Boolean val ) {
    // just save it back into kettle
    Props.getInstance().setProperty( PDI_CHECK_VERSION_FLAG, val.toString() );
  }

  public DisplayType getType() {
    return DisplayType.CHECK_BOX;
  }

  public String getLabelText() {
    return BaseMessages.getString( PKG, "versioncheck.label.text" );
  }

}
