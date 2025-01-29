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

    SUCCESS( 0, "The transformation ran without a problem" ),
    ERRORS_DURING_PROCESSING( 1, "Errors occurred during processing" ),
    UNEXPECTED_ERROR( 2, "An unexpected error occurred during loading / running of the transformation" ),
    UNABLE_TO_PREP_INIT_TRANS( 3, "Unable to prepare and initialize this transformation" ),
    KETTLE_VERSION_PRINT( 6, "Kettle Version printing" ),
    COULD_NOT_LOAD_TRANS( 7, "The transformation couldn't be loaded from XML or the Repository" ),
    ERROR_LOADING_STEPS_PLUGINS( 8, "Error loading steps or plugins (error in loading one of the plugins mostly)" ),
    CMD_LINE_PRINT( 9, "Command line usage printing" );


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

    SUCCESS( 0, "The job ran without a problem" ),
    ERRORS_DURING_PROCESSING( 1, "Errors occurred during processing" ),
    UNEXPECTED_ERROR( 2, "An unexpected error occurred during loading or running of the job" ),
    KETTLE_VERSION_PRINT( 6, "Kettle Version printing" ),
    COULD_NOT_LOAD_JOB( 7, "The job couldn't be loaded from XML or the Repository" ),
    ERROR_LOADING_STEPS_PLUGINS( 8, "Error loading steps or plugins (error in loading one of the plugins mostly)" ),
    CMD_LINE_PRINT( 9, "Command line usage printing" );

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
