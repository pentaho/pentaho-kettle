/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016-2018 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
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

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.rap.rwt.scripting.ClientListener;

public class WebSpoonClientListener extends ClientListener {
  public static WebSpoonClientListener getInstance() {
    return SingletonUtil.getSessionInstance( WebSpoonClientListener.class );
  }

  private WebSpoonClientListener() {
    super( getText() );
  }

  private static String getText() {
    String canvasScript = null;
    try {
      canvasScript = IOUtils.toString( WebSpoonClientListener.class.getResourceAsStream( "canvas.js" ) );
    } catch ( IOException e1 ) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    return canvasScript;
  }
}
