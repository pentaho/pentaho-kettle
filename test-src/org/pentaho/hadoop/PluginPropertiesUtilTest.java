/*
 * ******************************************************************************
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */

package org.pentaho.hadoop;

import static org.junit.Assert.*;

import org.junit.Test;

public class PluginPropertiesUtilTest {

  @Test
  public void getVersion() {
    // This test will only success if using classes produced by the ant build
    PluginPropertiesUtil util = new PluginPropertiesUtil();
    assertNotNull("Version was not swapped out during build process. This will ALWAYS fail unless classes were built with ant script.", util.getVersion());
  }

}
