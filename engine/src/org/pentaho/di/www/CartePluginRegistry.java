/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.www;

import java.util.HashMap;
import java.util.Map;

/**
 * This class basically serves as an immutable map for the CarteServlet.  This
 * allows concurrent access without synchronization as per:
 * http://jeremymanson.blogspot.com/2008/04/immutability-in-java.html
 * 
 * @author Bryan Rosander
 */
public class CartePluginRegistry {
  private final Map<String, CartePluginInterface> plugins;
  
  public CartePluginRegistry(Map<String, CartePluginInterface> pluginMap) {
    plugins = new HashMap<String, CartePluginInterface>(pluginMap);
  }
  
  public CartePluginInterface getCartePlugin(String key) {
    return plugins.get(key);
  }
}
