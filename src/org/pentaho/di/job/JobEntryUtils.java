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

import org.pentaho.di.core.variables.VariableSpace;

/**
 * User: RFellows
 * Date: 6/7/12
 */
public class JobEntryUtils {

  /**
   * @return {@code true} if {@link Boolean#parseBoolean(String)} returns {@code true} for {@link #isBlockingExecution()}
   */
  /**
   * Determine if the string equates to {@link Boolean#TRUE} after performing a variable substitution.
   *
   * @param s             String-encoded boolean value or variable expression
   * @param variableSpace Context for variables so we can substitute {@code s}
   * @return the value returned by {@link Boolean#parseBoolean(String) Boolean.parseBoolean(s)} after substitution
   */
  public static boolean asBoolean(String s, VariableSpace variableSpace) {
    String value = variableSpace.environmentSubstitute(s);
    return Boolean.parseBoolean(value);
  }

  /**
   * Parse the string as a {@link Long} after variable substitution.
   *
   * @param s             String-encoded {@link Long} value or variable expression that should resolve to a {@link Long} value
   * @param variableSpace Context for variables so we can substitute {@code s}
   * @return the value returned by {@link Long#parseLong(String, int) Long.parseLong(s, 10)} after substitution
   */
  public static Long asLong(String s, VariableSpace variableSpace) {
    String value = variableSpace.environmentSubstitute(s);
    return value == null ? null : Long.valueOf(value, 10);
  }

}
