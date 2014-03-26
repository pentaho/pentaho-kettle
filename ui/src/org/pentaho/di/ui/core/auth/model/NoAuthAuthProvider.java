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

package org.pentaho.di.ui.core.auth.model;

import java.util.List;

import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;

public class NoAuthAuthProvider extends AbstractAuthProvider {
  public NoAuthAuthProvider( BindingFactory bf ) {
    super( bf );
  }

  public String getPrincipal() {
    return null;
  }

  public String getProviderDescription() {
    return "NoAuth";
  }

  @Override
  protected void addBindings( List<Binding> bindings, BindingFactory bf ) {

  }

  @Override
  public void bind() {

  }
}
