package org.pentaho.di.pan;


/**
 * @see https://help.pentaho.com/Documentation/8.0/Products/Data_Integration/Command_Line_Tools
 */
public enum PanReturnCode {

    SUCCESS ( 0, "The transformation ran without a problem" ),
    ERRORS_DURING_PROCESSING ( 1, "Errors occurred during processing" ),
    UNEXPECTED_ERROR ( 2, "An unexpected error occurred during loading / running of the transformation" ),
    UNABLE_TO_PREP_INIT_TRANS ( 3, "Unable to prepare and initialize this transformation" ),
    COULD_NOT_LOAD_TRANS ( 7, "The transformation couldn't be loaded from XML or the Repository" ),
    ERROR_LOADING_STEPS_PLUGINS ( 8, "Error loading steps or plugins (error in loading one of the plugins mostly)" ),
    CMD_LINE_PRINT ( 9, "Command line usage printing" );

    private int code;
    private String description;

    private PanReturnCode( int code, String description ) {
       setCode( code );
       setDescription( description );
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
