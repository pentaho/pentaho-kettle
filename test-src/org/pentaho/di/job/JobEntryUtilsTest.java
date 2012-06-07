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

package org.pentaho.di.job;

import org.junit.Test;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;

import static org.junit.Assert.*;

/**
 * User: RFellows
 * Date: 6/7/12
 */
public class JobEntryUtilsTest {

  @Test
  public void asBoolean() {
    VariableSpace variableSpace = new Variables();

    assertFalse(JobEntryUtils.asBoolean("not-true", variableSpace));
    assertFalse(JobEntryUtils.asBoolean(Boolean.FALSE.toString(), variableSpace));
    assertTrue(JobEntryUtils.asBoolean(Boolean.TRUE.toString(), variableSpace));

    // No variable set, should attempt convert ${booleanValue} as is
    assertFalse(JobEntryUtils.asBoolean("${booleanValue}", variableSpace));

    variableSpace.setVariable("booleanValue", Boolean.TRUE.toString());
    assertTrue(JobEntryUtils.asBoolean("${booleanValue}", variableSpace));

    variableSpace.setVariable("booleanValue", Boolean.FALSE.toString());
    assertFalse(JobEntryUtils.asBoolean("${booleanValue}", variableSpace));
  }

  @Test
  public void asLong() {
    VariableSpace variableSpace = new Variables();

    assertNull(JobEntryUtils.asLong(null, variableSpace));
    assertEquals(Long.valueOf("10", 10), JobEntryUtils.asLong("10", variableSpace));

    variableSpace.setVariable("long", "150");
    assertEquals(Long.valueOf("150", 10), JobEntryUtils.asLong("${long}", variableSpace));

    try {
      JobEntryUtils.asLong("NaN", variableSpace);
      fail("expected number format exception");
    } catch (NumberFormatException ex) {
      // we're good
    }
  }}
