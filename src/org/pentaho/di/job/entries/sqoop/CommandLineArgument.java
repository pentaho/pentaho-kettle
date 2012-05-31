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

package org.pentaho.di.job.entries.sqoop;

import java.lang.annotation.*;

/**
 * Marks a field as a command line argument.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CommandLineArgument {
  /**
   * @return the name of the command line argument (full name), e.g. --table
   */
  String name();

  /**
   * Optional String to be used when displaying this field in a list.
   *
   * @return the friendly display name to be shown to a user instead of the {@link #name()}
   */
  String displayName() default "";

  /**
   * @return description of the command line argument
   */
  String description() default "";

  /**
   * Arguments either have values to be included or represent a boolean setting/flag. This is to denote a flag
   *
   * @return true if this argument represents a flag or switch.
   */
  boolean flag() default false;
}
