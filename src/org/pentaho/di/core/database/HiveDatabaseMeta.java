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

package org.pentaho.di.core.database;

import java.sql.Driver;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.plugins.DatabaseMetaPlugin;
import org.pentaho.di.core.row.ValueMetaInterface;

@DatabaseMetaPlugin(type="HIVE", typeDescription="Hadoop Hive")

public class HiveDatabaseMeta 
       extends BaseDatabaseMeta implements 
       DatabaseInterface {

   private final static String JAR_FILE = "hive-jdbc-0.6.0.jar";
    
    protected Integer driverMajorVersion;
    protected Integer driverMinorVersion;
    
    public HiveDatabaseMeta() throws Throwable {
    }
    
    /**
     * Package protected constructor for unit testing.
     * @param majorVersion The majorVersion to set for the driver
     * @param minorVersion The minorVersion to set for the driver
     * @throws Throwable
     */
    HiveDatabaseMeta(int majorVersion, int minorVersion) throws Throwable {
      driverMajorVersion = majorVersion;
      driverMinorVersion = minorVersion;
    }
    
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
      if(isDriverVersion(0,6)) {
        return suggestedName;
      } else {
        // For version 0.5 and prior:
        // Column aliases are currently not supported in Hive.  The default column alias
        // generated is in the format '_col##' where ## = column index.  Use this format
        // so the result can be mapped back correctly.
        return "_col" + String.valueOf(columnIndex); //$NON-NLS-1$
      }
    }
    
    protected synchronized void initDriverInfo() {
      Integer majorVersion = 0;
      Integer minorVersion = 0;
      
      try {
        // Load the driver version number
        Class<?> driverClass = Class.forName("org.apache.hadoop.hive.jdbc.HiveDriver"); //$NON-NLS-1$
        if(driverClass != null) {
          Driver driver = (Driver)driverClass.getConstructor().newInstance();
          majorVersion = driver.getMajorVersion(); 
          minorVersion = driver.getMinorVersion();
        }
      } catch (Exception e) {
        // Failed to load the driver version, leave at the defaults 
      }
      
      driverMajorVersion = majorVersion;
      driverMinorVersion = minorVersion;
    }
    
    /**
     * Check that the version of the driver being used is at least the driver you want.
     * If you do not care about the minor version, pass in a 0 (The assumption being that
     * the minor version will ALWAYS be 0 or greater)
     * 
     * @return  true: the version being used is equal to or newer than the one you requested
     *          false: the version being used is older than the one you requested
     */
    protected boolean isDriverVersion(int majorVersion, int minorVersion) {
      if (driverMajorVersion == null) {
        initDriverInfo();
      }
      
      if(majorVersion < driverMajorVersion) {
        // Driver major version is newer than the requested version  
        return true;
      } else if(majorVersion == driverMajorVersion) {
        // Driver major version is the same as requested, check the minor version
        if(minorVersion <= driverMinorVersion) {
          // Driver minor version is the same, or newer than requested
          return true;
        }
      }
      
      return false;
    }
}
