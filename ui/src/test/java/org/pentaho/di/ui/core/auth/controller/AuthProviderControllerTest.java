/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.core.auth.controller;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.auth.AuthHarness;
import org.pentaho.di.ui.core.auth.AuthProviderDialog;
import org.pentaho.di.ui.core.auth.model.NamedProvider;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.swt.SwtBindingFactory;

import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;



/**
 * Created by gmoran on 3/17/14.
 */
public class AuthProviderControllerTest {

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

  private List<NamedProvider> providers;
  AuthProviderController controller;

  @Before
  public void setup() {

    BindingFactory bf = new SwtBindingFactory();
    controller = new AuthProviderController(  );
    controller.setResourceBundle( resourceBundle );
    providers = AuthHarness.getProviders( bf );

  }

  @Test
  public void testAddProviders() {

    controller.addProviders( providers );
    assertEquals( 8, controller.getModel().getModelObjects().size() );

  }

  @Test
  public void testAddNew() {

    controller.addNew();
    controller.addNew();
    controller.addNew();

    assertEquals( 3, controller.getModel().getModelObjects().size() );

  }

  @Test
  public void testRemove() {

    controller.addProviders( providers );
    controller.getModel().setSelectedItem( controller.getModel().getModelObjects().get( 0 ) );
    controller.remove();

    assertEquals( 7, controller.getModel().getModelObjects().size() );
  }


}
