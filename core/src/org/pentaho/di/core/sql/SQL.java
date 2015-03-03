/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.sql;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.jdbc.FoundClause;
import org.pentaho.di.core.jdbc.ThinUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;

public class SQL {
  private String sqlString;

  private RowMetaInterface rowMeta;

  private String serviceClause;
  private String namespace;
  private String serviceName;
  private String serviceAlias;

  private String selectClause;
  private SQLFields selectFields;

  private String whereClause;
  private SQLCondition whereCondition;

  private String groupClause;
  private SQLFields groupFields;

  private String havingClause;
  private SQLCondition havingCondition;

  private String orderClause;
  private SQLFields orderFields;

  private String limitClause;
  private SQLLimit limitValues;

  /**
   * Create a new SQL object by parsing the supplied SQL string. This is a simple implementation with only one table
   * allows
   * 
   * @param sqlString
   *          the SQL string to parse
   * @param serviceName
   *          the name of the service this SQL references
   * @param rowMeta
   *          the row layout of the service
   * @throws KettleSQLException
   *           in case there is a SQL parsing error
   */
  public SQL( String sqlString ) throws KettleSQLException {
    this.sqlString = sqlString;

    splitSql( sqlString );
  }

  private void splitSql( String sql ) throws KettleSQLException {
    // First get the major blocks...
    /*
     * SELECT A, B, C FROM Step
     * 
     * SELECT A, B, C FROM Step WHERE D > 6 AND E = 'abcd'
     * 
     * SELECT A, B, C FROM Step ORDER BY B, A, C
     * 
     * SELECT A, B, sum(C) FROM Step WHERE D > 6 AND E = 'abcd' GROUP BY A, B HAVING sum(C) > 100 ORDER BY sum(C) DESC
     */
    //
    FoundClause foundClause = ThinUtil.findClauseWithRest( sql, "SELECT", "FROM" );
    selectClause = foundClause.getClause();
    if ( foundClause.getRest() == null ) {
      return;
    }
    foundClause = ThinUtil.findClauseWithRest( foundClause.getRest(), "FROM", "WHERE", "GROUP BY", "ORDER BY", "LIMIT" );
    serviceClause = foundClause.getClause();
    parseServiceClause();
    if ( foundClause.getRest() == null ) {
      return;
    }
    foundClause = ThinUtil.findClauseWithRest( foundClause.getRest(), "WHERE", "GROUP BY", "ORDER BY", "LIMIT" );
    whereClause = foundClause.getClause();
    if ( foundClause.getRest() == null ) {
      return;
    }
    foundClause = ThinUtil.findClauseWithRest( foundClause.getRest(), "GROUP BY", "HAVING", "ORDER BY", "LIMIT" );
    groupClause = foundClause.getClause();
    if ( foundClause.getRest() == null ) {
      return;
    }
    foundClause = ThinUtil.findClauseWithRest( foundClause.getRest(), "HAVING", "ORDER BY", "LIMIT" );
    havingClause = foundClause.getClause();
    if ( foundClause.getRest() == null ) {
      return;
    }
    foundClause = ThinUtil.findClauseWithRest( foundClause.getRest(), "ORDER BY", "LIMIT" );
    orderClause = foundClause.getClause();
    if ( foundClause.getRest() == null ) {
      return;
    }
    foundClause = ThinUtil.findClauseWithRest( foundClause.getRest(), "LIMIT" );
    limitClause = foundClause.getClause();
  }

  private void parseServiceClause() throws KettleSQLException {
    if ( Const.isEmpty( serviceClause ) ) {
      serviceName = "dual";
      return;
    }

    List<String> parts = ThinUtil.splitClause( serviceClause, ' ', '"' );
    if ( parts.size() >= 1 ) {
      // The service name is in the first part/
      // However, it can be in format Namespace.Service (Schema.Table)
      //
      List<String> list = ThinUtil.splitClause( parts.get( 0 ), '.', '"' );
      if ( list.size() == 1 ) {
        namespace = null;
        serviceName = ThinUtil.stripQuotes( list.get( 0 ), '"' );
      }
      if ( list.size() == 2 ) {
        namespace = ThinUtil.stripQuotes( list.get( 0 ), '"' );
        serviceName = ThinUtil.stripQuotes( list.get( 1 ), '"' );
      }
      if ( list.size() > 2 ) {
        throw new KettleSQLException( "Too many parts detected in table name specification [" + serviceClause + "]" );
      }
    }

    if ( parts.size() == 2 ) {
      serviceAlias = ThinUtil.stripQuotes( parts.get( 1 ), '"' );
    }
    if ( parts.size() == 3 ) {

      if ( parts.get( 1 ).equalsIgnoreCase( "AS" ) ) {
        serviceAlias = ThinUtil.stripQuotes( parts.get( 2 ), '"' );
      } else {
        throw new KettleSQLException( "AS expected in from clause: " + serviceClause );
      }
    }
    if ( parts.size() > 3 ) {
      StringBuilder builder = new StringBuilder();
      builder.append( "Found " );
      builder.append( parts.size() );
      builder.append( " parts for the FROM clause when only a table name and optionally an alias is supported: " );
      builder.append( serviceClause );
      throw new KettleSQLException( builder.toString() );
    }

    serviceAlias = Const.NVL( serviceAlias, serviceName );
  }

  public void parse( RowMetaInterface rowMeta ) throws KettleSQLException {

    // Now do the actual parsing and interpreting of the SQL, map it to the service row metadata

    this.rowMeta = rowMeta;

    selectFields = new SQLFields( serviceAlias, rowMeta, selectClause );
    if ( !Const.isEmpty( whereClause ) ) {
      whereCondition = new SQLCondition( serviceAlias, whereClause, rowMeta );
    }
    if ( !Const.isEmpty( groupClause ) ) {
      groupFields = new SQLFields( serviceAlias, rowMeta, groupClause );
    } else {
      groupFields = new SQLFields( serviceAlias, new RowMeta(), null );
    }
    if ( !Const.isEmpty( havingClause ) ) {
      havingCondition = new SQLCondition( serviceAlias, havingClause, rowMeta, selectFields );
    }
    if ( !Const.isEmpty( orderClause ) ) {
      orderFields = new SQLFields( serviceAlias, rowMeta, orderClause, true, selectFields );
    }
    if ( !Const.isEmpty( limitClause ) ) {
      limitValues = new SQLLimit( limitClause );
    }
  }

  public String getSqlString() {
    return sqlString;
  }

  public RowMetaInterface getRowMeta() {
    return rowMeta;
  }

  public String getServiceName() {
    return serviceName;
  }

  /**
   * @return the selectClause
   */
  public String getSelectClause() {
    return selectClause;
  }

  /**
   * @param selectClause
   *          the selectClause to set
   */
  public void setSelectClause( String selectClause ) {
    this.selectClause = selectClause;
  }

  /**
   * @return the whereClause
   */
  public String getWhereClause() {
    return whereClause;
  }

  /**
   * @param whereClause
   *          the whereClause to set
   */
  public void setWhereClause( String whereClause ) {
    this.whereClause = whereClause;
  }

  /**
   * @return the groupClause
   */
  public String getGroupClause() {
    return groupClause;
  }

  /**
   * @param groupClause
   *          the groupClause to set
   */
  public void setGroupClause( String groupClause ) {
    this.groupClause = groupClause;
  }

  /**
   * @return the havingClause
   */
  public String getHavingClause() {
    return havingClause;
  }

  /**
   * @param havingClause
   *          the havingClause to set
   */
  public void setHavingClause( String havingClause ) {
    this.havingClause = havingClause;
  }

  /**
   * @return the orderClause
   */
  public String getOrderClause() {
    return orderClause;
  }

  /**
   * @param orderClause
   *          the orderClause to set
   */
  public void setOrderClause( String orderClause ) {
    this.orderClause = orderClause;
  }

  /**
   * @return the limitClause
   */
  public String getLimitClause() {
    return limitClause;
  }

  /**
   * @param limitClause the orderClause to set
   */
  public void setLimitClause( String limitClause ) {
    this.limitClause = limitClause;
  }

  /**
   *
   * @return the limitValues
   */
  public SQLLimit getLimitValues() {
    return limitValues;
  }

  /**
   *
   * @param limitValues the limitValues to set
   *
   */
  public void setLimitValues( SQLLimit limitValues ) {
    this.limitValues = limitValues;
  }

  /**
   * @param sqlString
   *          the sql string to set
   */
  public void setSqlString( String sqlString ) {
    this.sqlString = sqlString;
  }

  /**
   * @return the selectFields
   */
  public SQLFields getSelectFields() {
    return selectFields;
  }

  /**
   * @param selectFields
   *          the selectFields to set
   */
  public void setSelectFields( SQLFields selectFields ) {
    this.selectFields = selectFields;
  }

  /**
   * @return the groupFields
   */
  public SQLFields getGroupFields() {
    return groupFields;
  }

  /**
   * @param groupFields
   *          the groupFields to set
   */
  public void setGroupFields( SQLFields groupFields ) {
    this.groupFields = groupFields;
  }

  /**
   * @return the orderFields
   */
  public SQLFields getOrderFields() {
    return orderFields;
  }

  /**
   * @param orderFields
   *          the orderFields to set
   */
  public void setOrderFields( SQLFields orderFields ) {
    this.orderFields = orderFields;
  }

  /**
   * @return the whereCondition
   */
  public SQLCondition getWhereCondition() {
    return whereCondition;
  }

  /**
   * @param whereCondition
   *          the whereCondition to set
   */
  public void setWhereCondition( SQLCondition whereCondition ) {
    this.whereCondition = whereCondition;
  }

  /**
   * @param rowMeta
   *          the rowMeta to set
   */
  public void setRowMeta( RowMetaInterface rowMeta ) {
    this.rowMeta = rowMeta;
  }

  /**
   * @param serviceName
   *          the serviceName to set
   */
  public void setServiceName( String serviceName ) {
    this.serviceName = serviceName;
  }

  /**
   * @return the havingCondition
   */
  public SQLCondition getHavingCondition() {
    return havingCondition;
  }

  /**
   * @param havingCondition
   *          the havingCondition to set
   */
  public void setHavingCondition( SQLCondition havingCondition ) {
    this.havingCondition = havingCondition;
  }

  /**
   * @return the namespace
   */
  public String getNamespace() {
    return namespace;
  }

}
