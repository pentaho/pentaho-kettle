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

package org.pentaho.di.ui.core.auth;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.auth.controller.AuthProviderController;
import org.pentaho.di.ui.core.auth.model.NamedProvider;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.swt.SwtBindingFactory;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by gmoran on 3/19/14.
 */
public class AuthProviderDialog {

  private static String XUL_FILE = "org/pentaho/di/ui/core/auth/xul/authManager.xul";

  private LogChannelInterface log;

  private AuthProviderController controller = new AuthProviderController();
  private XulDomContainer container;

  private static final Class<?> CLZ = AuthProviderDialog.class;
  private ResourceBundle resourceBundle = new ResourceBundle() {

    @Override
    public Enumeration<String> getKeys() {
      return null;
    }

    @Override
    protected Object handleGetObject( String key ) {
      return BaseMessages.getString( CLZ, key );
    }

  };

  public AuthProviderDialog( Shell shell ) {

    log = new LogChannel( resourceBundle.getString( "log.name" ) );

    try {

      SwtXulLoader xulLoader = new SwtXulLoader();
      xulLoader.setOuterContext( shell );
      container = xulLoader.loadXul( XUL_FILE, resourceBundle );

      final XulRunner runner = new SwtXulRunner();
      runner.addContainer( container );

      BindingFactory bf = new SwtBindingFactory();
      bf.setDocument( container.getDocumentRoot() );

      controller.setBindingFactory( bf );
      controller.setResourceBundle( resourceBundle );
      container.addEventHandler( controller );

      try {
        runner.initialize();
      } catch ( XulException e ) {
        SpoonFactory.getInstance().messageBox( e.getLocalizedMessage(),
            resourceBundle.getString( "error.on_initialization" ), false, Const.ERROR );
        log.logError( resourceBundle.getString( "error.on_initialization" ), e );
      }
    } catch ( XulException e ) {
      log.logError( resourceBundle.getString( "error.on_initialization" ), e );
    }
  }

  public void show() {
    controller.open();
  }

  public void addProviders( List<NamedProvider> providers ) {
    controller.addProviders( providers );
  }

  public BindingFactory getBindingFactory() {
    return controller.getBindingFactory();
  }

}
