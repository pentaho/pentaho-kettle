/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.mondrianinput;
/*
// $Id: $
// This software is subject to the terms of the Common Public License
// Agreement, available at the following URL:
// http://www.opensource.org/licenses/cpl.html.
// Copyright (C) 2007-2007 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;

import mondrian.olap.Axis;
import mondrian.olap.Cell;
import mondrian.olap.Connection;
import mondrian.olap.DriverManager;
import mondrian.olap.Hierarchy;
import mondrian.olap.Member;
import mondrian.olap.Position;
import mondrian.olap.Query;
import mondrian.olap.Result;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.DBCacheEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;

/**
 * <code>Mondrian Helper class</code> ...
 *
 * @author jhyde
 * @since Mar 12, 2007
 */
public class MondrianHelper {
	
	private DatabaseMeta databaseMeta;
	private String catalog;
	private String queryString;
	
	private RowMetaInterface outputRowMeta;
	private Result result;
	private Query query;
	private VariableSpace space;
	
    private List<List<Object>> rows;
    private List<String> headings;
	private Connection connection;

	public MondrianHelper(DatabaseMeta databaseMeta, String catalog, String queryString, VariableSpace space) {
		this.databaseMeta = databaseMeta;
		this.catalog = catalog;
		this.queryString = queryString;
		this.space = space;
	}
	
    public void openQuery() throws KettleDatabaseException {
        
    	String connectString = "Provider=mondrian;" +
            "Jdbc='"+space.environmentSubstitute(databaseMeta.getURL())+"';"+
    		"Catalog='"+space.environmentSubstitute(catalog)+"';"+
            "JdbcDrivers="+space.environmentSubstitute(databaseMeta.getDriverClass())+";";
    	if (!Const.isEmpty(databaseMeta.getUsername())) {
    		connectString+="JdbcUser="+space.environmentSubstitute(databaseMeta.getUsername())+";";
    	}
    	if (!Const.isEmpty(databaseMeta.getPassword())) {
    		connectString+="JdbcPassword="+space.environmentSubstitute(databaseMeta.getPassword())+";";
    	}

    	connection = DriverManager.getConnection(connectString, null);
        query = connection.parseQuery(queryString);
        result = connection.execute(query);
    }
    
    public void close() {
    	if (result!=null) result.close();
    	if (query!=null) query.clone();
    	if (connection!=null) connection.close();
    }

    /**
     * Outputs one row per tuple on the rows axis.
     * 
     * @throws KettleDatabaseException
     *           in case some or other error occurs
     */
    public void createRectangularOutput() throws KettleDatabaseException {
  
      final Axis[] axes = result.getAxes();
      if (axes.length != 2) {
        throw new KettleDatabaseException("Tabular output only supported for 2-dimensional results");
      }
      headings = new ArrayList<String>();
      rows = new ArrayList<List<Object>>();
  
      final Axis rowsAxis = axes[1];
      final Axis columnsAxis = axes[0];
  
      int rowOrdinal = -1;
      int[] coords = { 0, 0 };
      for (Position rowPos : rowsAxis.getPositions()) {
        ++rowOrdinal;
        coords[1] = rowOrdinal;
        if (rowOrdinal == 0) {
          // Generate headings on the first row. Note that if there are
          // zero rows, we don't have enough metadata to generate
          // headings.
  
          // First headings are for the members on the rows axis.
          for (Member rowMember : rowPos) {
            headings.add(rowMember.getHierarchy().getUniqueName());
          }
  
          // Rest of the headings are for the members on the columns axis.
          // If there are more than one member at each postition,
          // concatenate the unique names.
          for (Position columnPos : columnsAxis.getPositions()) {
            String heading = "";
            for (Member columnMember : columnPos) {
              if (!heading.equals("")) {
                heading += ", ";
              }
              heading += columnMember.getUniqueName();
            }
            headings.add(heading);
          }
        }
  
        List<Object> rowValues = new ArrayList<Object>();
  
        // The first row values describe the members on the rows axis.
        for (Member rowMember : rowPos) {
          rowValues.add(rowMember.getUniqueName());
        }
  
        // Rest of the row values are the raw cell values.
        // NOTE: Could also/instead output formatted cell values here.
        // NOTE: Could also output all properties of each cell.
        for (int columnOrdinal = 0; columnOrdinal < columnsAxis.getPositions().size(); ++columnOrdinal) {
          coords[0] = columnOrdinal;
          final Cell cell = result.getCell(coords);
          rowValues.add(cell.getValue());
        }
  
        rows.add(rowValues);
      }
  
      outputRowMeta = new RowMeta();
  
      // Scan the rows for the data types. Important case: if we get null on a
      // column, keep scanning until we find one line that has an actual value
      if (rows.size() > 0) {
        int columnCount = rows.get(0).size();
        HashMap<Integer, ValueMetaInterface> valueMetaHash = new HashMap<Integer, ValueMetaInterface>();
  
        for (int i = 0; i < rows.size(); i++) {
  
          List<Object> rowValues = rows.get(i);
  
          for (int c = 0; c < rowValues.size(); c++) {
  
            if (valueMetaHash.containsKey(new Integer(c))) {
              continue; // we have this value already
            }
  
            ValueMetaInterface valueMeta = new ValueMeta(headings.get(c));
            Object valueData = rowValues.get(c);
  
            if (valueData == null) {
              continue; // skip this row and look for the metadata in a new one
            }
  
            if (valueData instanceof String) {
              valueMeta.setType(ValueMetaInterface.TYPE_STRING);
            } else if (valueData instanceof Date) {
              valueMeta.setType(ValueMetaInterface.TYPE_DATE);
            } else if (valueData instanceof Boolean) {
              valueMeta.setType(ValueMetaInterface.TYPE_BOOLEAN);
            } else if (valueData instanceof Integer) {
              valueMeta.setType(ValueMetaInterface.TYPE_INTEGER);
              valueData = Long.valueOf(((Integer) valueData).longValue());
            } else if (valueData instanceof Short) {
              valueMeta.setType(ValueMetaInterface.TYPE_INTEGER);
              valueData = Long.valueOf(((Short) valueData).longValue());
            } else if (valueData instanceof Byte) {
              valueMeta.setType(ValueMetaInterface.TYPE_INTEGER);
              valueData = Long.valueOf(((Byte) valueData).longValue());
            } else if (valueData instanceof Long) {
              valueMeta.setType(ValueMetaInterface.TYPE_INTEGER);
            } else if (valueData instanceof Double) {
              valueMeta.setType(ValueMetaInterface.TYPE_NUMBER);
            } else if (valueData instanceof Float) {
              valueMeta.setType(ValueMetaInterface.TYPE_NUMBER);
              valueData = Double.valueOf(((Float) valueData).doubleValue());
            } else if (valueData instanceof BigDecimal) {
              valueMeta.setType(ValueMetaInterface.TYPE_BIGNUMBER);
            } else {
              throw new KettleDatabaseException("Unhandled data type found '" + valueData.getClass().toString() + "'");
            }
  
            valueMetaHash.put(c, valueMeta);
          }
  
          if (valueMetaHash.size() == columnCount) {
            break; // we're done
          }
        }
  
        // Build the list of valueMetas
        List<ValueMetaInterface> valueMetaList = new ArrayList<ValueMetaInterface>();
  
        for (int c = 0; c < columnCount; c++) {
          if (valueMetaHash.containsKey(new Integer(c))) {
            valueMetaList.add(valueMetaHash.get(new Integer(c)));
          } else {
            // If the entire column is null, assume the missing data as String.
            // Irrelevant, anyway
            ValueMetaInterface valueMeta = new ValueMeta(headings.get(c), ValueMetaInterface.TYPE_STRING);
            valueMetaList.add(valueMeta);
          }
  
        }
  
        outputRowMeta.setValueMetaList(valueMetaList);
      }
      // Now that we painstakingly found the meta data that comes out of the
      // Mondrian database, cache it please...
      //
      DBCacheEntry cacheEntry = new DBCacheEntry(databaseMeta.getName(), queryString);
      DBCache.getInstance().put(cacheEntry, outputRowMeta);
    }
    
    /**
     * Retrieve the rows from the opened query.
     * Also create a description of the flattened output of the query.
     * This call populated rowMetaInterface and rows
     * The query needs to be opened beforehand.
     * @throws KettleDatabaseException in case something goes wrong
     * 
     * TODO: this is not quite working for our purposes.
     */
    public void createFlattenedOutput() throws KettleDatabaseException {
    	
    	final Axis[] axes = result.getAxes();
        rows = new ArrayList<List<Object>>();
        headings = new ArrayList<String>();

        // Compute headings. Each heading is a hierarchy name. If there are say
        // 2 members on the columns, and 3 members on the rows axis, then there
        // will be 5 headings.
        //
        for (Axis axis : axes) {
            final List<Position> positions = axis.getPositions();
            if (positions.isEmpty()) {
                // Result set is empty. There is no data to print, and we cannot
                // even deduce column headings.
                return;
            }
            for (Member member : positions.get(0)) {
            	Hierarchy hierarchy = member.getHierarchy();
                headings.add(hierarchy.getUniqueName());
            }
        }

        int[] coords = new int[axes.length];
        outputFlattenedRecurse(result, rows, new ArrayList<Object>(), coords, 0);

        outputRowMeta = new RowMeta();

        // Just scan the first row to see what data types we received...
        //
        for (int i=0 ; i<rows.size() && i<1 ; i++) {
        	
        	List<Object> rowValues = rows.get(i);
        	
            for (int c=0 ;c<rowValues.size();c++) {
        		ValueMetaInterface valueMeta = new ValueMeta(headings.get(c));
            	Object             valueData = rowValues.get(c);

                if (valueData instanceof String) {
                  valueMeta.setType(ValueMetaInterface.TYPE_STRING);
                } else if (valueData instanceof Date) {
                  valueMeta.setType(ValueMetaInterface.TYPE_DATE);
                } else if (valueData instanceof Boolean) {
                  valueMeta.setType(ValueMetaInterface.TYPE_BOOLEAN);
                } else if (valueData instanceof Integer) {
                  valueMeta.setType(ValueMetaInterface.TYPE_INTEGER);
                  valueData = Long.valueOf(((Integer) valueData).longValue());
                } else if (valueData instanceof Short) {
                  valueMeta.setType(ValueMetaInterface.TYPE_INTEGER);
                  valueData = Long.valueOf(((Short) valueData).longValue());
                } else if (valueData instanceof Byte) {
                  valueMeta.setType(ValueMetaInterface.TYPE_INTEGER);
                  valueData = Long.valueOf(((Byte) valueData).longValue());
                } else if (valueData instanceof Long) {
                  valueMeta.setType(ValueMetaInterface.TYPE_INTEGER);
                } else if (valueData instanceof Double) {
                  valueMeta.setType(ValueMetaInterface.TYPE_NUMBER);
                } else if (valueData instanceof Float) {
                  valueMeta.setType(ValueMetaInterface.TYPE_NUMBER);
                  valueData = Double.valueOf(((Float) valueData).doubleValue());
                } else if (valueData instanceof BigDecimal) {
                  valueMeta.setType(ValueMetaInterface.TYPE_BIGNUMBER);
                } else {
                  throw new KettleDatabaseException("Unhandled data type found '" + valueData.getClass().toString() + "'");
                }
            	
            	outputRowMeta.addValueMeta(valueMeta);
            }
        }
        
        // Now that we painstakingly found the metadata that comes out of the Mondrian database, cache it please...
        //
        DBCacheEntry cacheEntry = new DBCacheEntry(databaseMeta.getName(), queryString);
        DBCache.getInstance().put(cacheEntry, outputRowMeta);
    }
    
    public RowMetaInterface getCachedRowMeta() {
    	DBCacheEntry cacheEntry = new DBCacheEntry(databaseMeta.getName(), queryString);
        return DBCache.getInstance().get(cacheEntry);
    }

    private static void outputFlattenedRecurse(
        Result result,
        List<List<Object>> rows,
        List<Object> rowValues,
        int[] coords,
        int axisOrdinal)
    {
        final Axis[] axes = result.getAxes();
        if (axisOrdinal == axes.length) {
            final Cell cell = result.getCell(coords);
            // Output the raw (unformatted) value of the cell.
            // NOTE: We could output other properties of the cell here, such as its
            // formatted value, too.
            rowValues.add(cell.getValue());

            // Add a copy of the completed row to the list of rows.
            rows.add(new ArrayList<Object>(rowValues));
        } else {
            final Axis axis = axes[axisOrdinal];
            int k = -1;
            int saveLength = rowValues.size();
            for (Position position : axis.getPositions()) {
                coords[axisOrdinal] = ++k;
                for (Member member : position) {
                    rowValues.add(member.getUniqueName());
                }
                outputFlattenedRecurse(
                    result, rows, rowValues, coords, axisOrdinal + 1);
                while (rowValues.size() > saveLength) {
                    rowValues.remove(rowValues.size() - 1);
                }
            }
        }
    }

	/**
	 * @return the databaseMeta
	 */
	public DatabaseMeta getDatabaseMeta() {
		return databaseMeta;
	}

	/**
	 * @param databaseMeta the databaseMeta to set
	 */
	public void setDatabaseMeta(DatabaseMeta databaseMeta) {
		this.databaseMeta = databaseMeta;
	}

	/**
	 * @return the catalog
	 */
	public String getCatalog() {
		return catalog;
	}

	/**
	 * @param catalog the catalog to set
	 */
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	/**
	 * @return the queryString
	 */
	public String getQueryString() {
		return queryString;
	}

	/**
	 * @param queryString the queryString to set
	 */
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	/**
	 * @return the outputRowMeta
	 */
	public RowMetaInterface getOutputRowMeta() {
		return outputRowMeta;
	}

	/**
	 * @return the result
	 */
	public Result getResult() {
		return result;
	}

	/**
	 * @return the query
	 */
	public Query getQuery() {
		return query;
	}

	/**
	 * @return the rows
	 */
	public List<List<Object>> getRows() {
		return rows;
	}

	/**
	 * @return the headings
	 */
	public List<String> getHeadings() {
		return headings;
	}
}

// End Foo.java
