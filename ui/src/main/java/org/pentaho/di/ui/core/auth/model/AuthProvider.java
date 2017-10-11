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

package org.pentaho.di.ui.core.auth.model;

import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.BindingException;

import java.lang.reflect.InvocationTargetException;

public abstract interface AuthProvider extends Cloneable {
  public abstract String getPrincipal();

  public abstract void setPrincipal( String paramString );

  public abstract String getProviderDescription();

  public abstract String getOverlay();

  public void bind() throws BindingException, XulException, InvocationTargetException;

  public void unbind();

  public AuthProvider clone() throws CloneNotSupportedException;

  public void fireBindingsChanged() throws XulException, InvocationTargetException;
}
