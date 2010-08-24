package org.pentaho.di.core.database;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;

public class HiveDatabaseMeta 
       extends BaseDatabaseMeta implements 
       DatabaseInterface {

    private final static String JAR_FILE = "hive-jdbc-0.7.0.jar";
    
    
    @Override
    public int[] getAccessTypeList() {
        return new int[] {DatabaseMeta.TYPE_ACCESS_NATIVE};
    }

    @Override
    public String getAddColumnStatement(String tablename, ValueMetaInterface v,
            String tk, boolean useAutoinc, String pk, boolean semicolon) {

        return "ALTER TABLE "+tablename+" ADD "+getFieldDefinition(v, tk, pk, useAutoinc, true, false);

    }

    @Override
    public String getDriverClass() {
        
        //  !!!  We will probably have to change this if we are providing our own driver,
        //  i.e., before our code is committed to the Hadoop Hive project.
        return "org.apache.hadoop.hive.jdbc.HiveDriver";
    }

    /**
     * This method assumes that Hive has no concept of primary 
     * and technical keys and auto increment columns.  We are 
     * ignoring the tk, pk and useAutoinc parameters.
     */
    @Override
    public String getFieldDefinition(ValueMetaInterface v, String tk,
            String pk, boolean useAutoinc, boolean addFieldname, boolean addCr) {

       
        String retval="";
        
        String fieldname = v.getName();
        int    length    = v.getLength();
        int    precision = v.getPrecision();
        
        if (addFieldname)  {
            retval+=fieldname+" ";
        }
        
        int    type      = v.getType();
        switch(type) {
        
            case ValueMetaInterface.TYPE_BOOLEAN:
                retval+="BOOLEAN";
                break;
        
            //  Hive does not support DATE
            case ValueMetaInterface.TYPE_DATE:
                retval+="STRING";
                break;
                
            case  ValueMetaInterface.TYPE_STRING:
                retval+="STRING";
                break;
           
            case ValueMetaInterface.TYPE_NUMBER    :
            case ValueMetaInterface.TYPE_INTEGER   : 
            case ValueMetaInterface.TYPE_BIGNUMBER : 
                // Integer values...
                if (precision==0) {
                    if (length>9) {
                        if (length<19) {
                            // can hold signed values between -9223372036854775808 and 9223372036854775807
                            // 18 significant digits
                            retval+="BIGINT";
                        }
                        else {
                            retval+="FLOAT";
                        }
                    }
                    else {
                        retval+="INT";
                    }
                }
                // Floating point values...
                else {  
                    if (length>15) {
                        retval+="FLOAT";
                    }
                    else {
                        // A double-precision floating-point number is accurate to approximately 15 decimal places.
                        // http://mysql.mirrors-r-us.net/doc/refman/5.1/en/numeric-type-overview.html 
                            retval+="DOUBLE";
                    }
                }
                   
                break;
            }

        return retval;
    }

    @Override
    public String getModifyColumnStatement(String tablename,
            ValueMetaInterface v, String tk, boolean useAutoinc, String pk,
            boolean semicolon) {

            return "ALTER TABLE "+tablename+" MODIFY "+getFieldDefinition(v, tk, pk, useAutoinc, true, false);
    }

    @Override
    public String getURL(String hostname, String port, String databaseName)
            throws KettleDatabaseException {

        return "jdbc:hive://"+hostname+":"+port+"/"+databaseName;
    }

    @Override
    public String[] getUsedLibraries() {

        return new String[] { JAR_FILE };
    }
    
    /**
     * Build the SQL to count the number of rows in the passed table.
     * @param tableName
     * @return
     */
    @Override
    public String getSelectCountStatement(String tableName) {
        return "select count(1) from "+tableName;
    }

    
    @Override
    public String generateColumnAlias(int columnIndex, String suggestedName) {
      // Column aliases are currently not supported in Hive.  The default column alias
      // generated is in the format '_col##' where ## = column index.  Use this format
      // so the result can be mapped back correctly.
      return "_col" + String.valueOf(columnIndex); //$NON-NLS-1$
    }
}
