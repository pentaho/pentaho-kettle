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


package org.pentaho.di.ui.spoon;

import java.util.Set;
import java.util.Map;

public interface SpoonUiExtenderPluginInterface {

  public Map<Class<?>, Set<String>> respondsTo();

  public void uiEvent( Object subject, String event );
}
