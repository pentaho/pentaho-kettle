/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.ui.trans.steps.salesforceinput;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.salesforce.SalesforceConnectionUtils;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceInputField;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceInputMeta;

import static org.pentaho.di.trans.steps.salesforce.SalesforceConnectionUtils.RECORDS_FILTER_ALL;
import static org.pentaho.di.trans.steps.salesforce.SalesforceConnectionUtils.RECORDS_FILTER_DELETED;
import static org.pentaho.di.trans.steps.salesforce.SalesforceConnectionUtils.RECORDS_FILTER_UPDATED;
import static org.pentaho.di.trans.steps.salesforceinput.SalesforceInputField.TYPE_TRIM_BOTH;
import static org.pentaho.di.trans.steps.salesforceinput.SalesforceInputField.TYPE_TRIM_LEFT;
import static org.pentaho.di.trans.steps.salesforceinput.SalesforceInputField.TYPE_TRIM_NONE;
import static org.pentaho.di.trans.steps.salesforceinput.SalesforceInputField.TYPE_TRIM_RIGHT;

public class SalesforceInputMetaInjectionTest extends BaseMetadataInjectionTest<SalesforceInputMeta> {
  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new SalesforceInputMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "SALESFORCE_URL", () ->  meta.getTargetURL() );
    check( "SALESFORCE_USERNAME", () ->  meta.getUsername() );
    check( "SALESFORCE_PASSWORD", () ->  meta.getPassword() );
    check( "TIME_OUT", () ->  meta.getTimeout() );
    check( "USE_COMPRESSION", () ->  meta.isCompression() );
    check( "MODULE", () ->  meta.getModule() );
    check( "INCLUDE_SQL_IN_OUTPUT", () ->  meta.includeSQL() );
    check( "SQL_FIELDNAME", () ->  meta.getSQLField() );
    check( "INCLUDE_TIMESTAMP_IN_OUTPUT", () ->  meta.includeTimestamp() );
    check( "TIMESTAMP_FIELDNAME", () ->  meta.getTimestampField() );
    check( "INCLUDE_URL_IN_OUTPUT", () ->  meta.includeTargetURL() );
    check( "URL_FIELDNAME", () ->  meta.getTargetURLField() );
    check( "INCLUDE_MODULE_IN_OUTPUT", () ->  meta.includeModule() );
    check( "MODULE_FIELDNAME", () ->  meta.getModuleField() );
    check( "INCLUDE_DELETION_DATE_IN_OUTPUT", () ->  meta.includeDeletionDate() );
    check( "DELETION_DATE_FIELDNAME", () ->  meta.getDeletionDateField() );
    check( "INCLUDE_ROWNUM_IN_OUTPUT", () ->  meta.includeRowNumber() );
    check( "ROWNUM_FIELDNAME", () ->  meta.getRowNumberField() );
    check( "QUERY_CONDITION", () ->  meta.getCondition() );
    check( "LIMIT", () ->  meta.getRowLimit() );
    check( "USE_SPECIFIED_QUERY", () ->  meta.isSpecifyQuery() );
    check( "SPECIFY_QUERY", () ->  meta.getQuery() );
    check( "END_DATE", () ->  meta.getReadTo() );
    check( "START_DATE", () ->  meta.getReadFrom() );
    check( "QUERY_ALL", () ->  meta.isQueryAll() );
    checkStringToInt( "RETRIEVE", () ->  meta.getRecordsFilter(),
      SalesforceConnectionUtils.recordsFilterCode,
      new int[]{ RECORDS_FILTER_ALL, RECORDS_FILTER_UPDATED, RECORDS_FILTER_DELETED } );
    check( "NAME", () ->  meta.getInputFields()[0].getName() );
    check( "FIELD", () ->  meta.getInputFields()[0].getField() );
    check( "LENGTH", () ->  meta.getInputFields()[0].getLength() );
    check( "FORMAT", () ->  meta.getInputFields()[0].getFormat() );
    check( "PRECISION", () ->  meta.getInputFields()[0].getPrecision() );
    check( "CURRENCY", () ->  meta.getInputFields()[0].getCurrencySymbol() );
    check( "DECIMAL", () ->  meta.getInputFields()[0].getDecimalSymbol() );
    check( "GROUP", () ->  meta.getInputFields()[0].getGroupSymbol() );
    check( "REPEAT", () ->  meta.getInputFields()[0].isRepeated() );
    check( "ISIDLOOKUP", () ->  meta.getInputFields()[0].isIdLookup() );
    checkStringToInt( "TRIM_TYPE", () ->  meta.getInputFields()[0].getTrimType(),
      SalesforceInputField.trimTypeCode,
      new int[]{ TYPE_TRIM_NONE, TYPE_TRIM_LEFT, TYPE_TRIM_RIGHT, TYPE_TRIM_BOTH });
    int[] types = new int[]{
      ValueMetaInterface.TYPE_NONE,
      ValueMetaInterface.TYPE_NUMBER,
      ValueMetaInterface.TYPE_STRING,
      ValueMetaInterface.TYPE_DATE,
      ValueMetaInterface.TYPE_BOOLEAN,
      ValueMetaInterface.TYPE_INTEGER,
      ValueMetaInterface.TYPE_BIGNUMBER,
      ValueMetaInterface.TYPE_SERIALIZABLE,
      ValueMetaInterface.TYPE_BINARY,
      ValueMetaInterface.TYPE_TIMESTAMP,
      ValueMetaInterface.TYPE_INET
    };
    ValueMetaString valueMeta = new ValueMetaString();
    checkStringToInt("TYPE", () ->  meta.getInputFields()[0].getType(),
      valueMeta.typeCodes, types );
  }
}
