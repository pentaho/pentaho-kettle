/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.hadoop.shim.spi;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.pentaho.hadoop.shim.ShimVersion;
import org.pentaho.hadoop.shim.api.Configuration;

public class MockPigShim implements PigShim {

  @Override
  public ShimVersion getVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isLocalExecutionSupported() {
    return false;
  }

  @Override
  public void configure(Properties properties, Configuration configuration) {
  }

  @Override
  public String substituteParameters(URL pigScript, List<String> paramList) throws Exception {
    return null;
  }

  @Override
  public int[] executeScript(String pigScript, ExecutionMode mode, Properties properties) throws Exception {
    return null;
  }

}
