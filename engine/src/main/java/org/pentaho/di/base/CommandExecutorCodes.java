/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.base;

import java.util.Arrays;

/**
 * @link https://help.pentaho.com/Documentation/8.0/Products/Data_Integration/Command_Line_Tools
 */
public class CommandExecutorCodes {

  /**
   * @link https://help.pentaho.com/Documentation/8.0/Products/Data_Integration/Command_Line_Tools#Pan_Status_Codes
  */
  public enum Pan {

    SUCCESS ( 0, "The transformation ran without a problem" ),
    ERRORS_DURING_PROCESSING ( 1, "Errors occurred during processing" ),
    UNEXPECTED_ERROR ( 2, "An unexpected error occurred during loading / running of the transformation" ),
    UNABLE_TO_PREP_INIT_TRANS ( 3, "Unable to prepare and initialize this transformation" ),
    KETTLE_VERSION_PRINT ( 6, "Kettle Version printing" ),
    COULD_NOT_LOAD_TRANS ( 7, "The transformation couldn't be loaded from XML or the Repository" ),
    ERROR_LOADING_STEPS_PLUGINS ( 8, "Error loading steps or plugins (error in loading one of the plugins mostly)" ),
    CMD_LINE_PRINT ( 9, "Command line usage printing" );

    private int code;
    private String description;

    Pan( int code, String description ) {
      setCode( code );
      setDescription( description );
    }

    public int getCode() {
      return code;
    }

    public void setCode( int code ) {
      this.code = code;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription( String description ) {
      this.description = description;
    }

    public static Pan getByCode( final int code ) {
      return Arrays.asList( Pan.values() ).stream()
          .filter( pan -> pan.getCode() == code )
          .findAny().orElse( null );
    }

    public static boolean isFailedExecution( final int code ) {
      return Pan.UNEXPECTED_ERROR.getCode() == code
          || Pan.UNABLE_TO_PREP_INIT_TRANS.getCode() == code
          || Pan.COULD_NOT_LOAD_TRANS.getCode() == code
          || Pan.ERROR_LOADING_STEPS_PLUGINS.getCode() == code;
    }
  }

  /**
   * @link https://help.pentaho.com/Documentation/8.0/Products/Data_Integration/Command_Line_Tools#Kitchen_Status_Codes
  */
  public enum Kitchen {

    SUCCESS ( 0, "The job ran without a problem" ),
    ERRORS_DURING_PROCESSING ( 1, "Errors occurred during processing" ),
    UNEXPECTED_ERROR ( 2, "An unexpected error occurred during loading or running of the job" ),
    KETTLE_VERSION_PRINT ( 6, "Kettle Version printing" ),
    COULD_NOT_LOAD_JOB ( 7, "The job couldn't be loaded from XML or the Repository" ),
    ERROR_LOADING_STEPS_PLUGINS ( 8, "Error loading steps or plugins (error in loading one of the plugins mostly)" ),
    CMD_LINE_PRINT ( 9, "Command line usage printing" );

    private int code;
    private String description;

    Kitchen( int code, String description ) {
      setCode( code );
      setDescription( description );
    }

    public int getCode() {
      return code;
    }

    public void setCode( int code ) {
      this.code = code;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription( String description ) {
      this.description = description;
    }

    public static Kitchen getByCode( final int code ) {
      return Arrays.asList( Kitchen.values() ).stream()
          .filter( kitchen -> kitchen.getCode() == code )
          .findAny().orElse( null );
    }

    public static boolean isFailedExecution( final int code ) {
      return Kitchen.UNEXPECTED_ERROR.getCode() == code
          || Kitchen.COULD_NOT_LOAD_JOB.getCode() == code
          || Kitchen.ERROR_LOADING_STEPS_PLUGINS.getCode() == code;
    }
  }
}
