/*
 * !
 *  * This program is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 *  * Foundation.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License along with this
 *  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *  * or from the Free Software Foundation, Inc.,
 *  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  * See the GNU Lesser General Public License for more details.
 *  *
 *  * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 *
 */

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
