package org.pentaho.di.repository.kdr.delegates;

import org.junit.Test;

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;
import static org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryConnectionDelegate
  .createIdsWithValuesQuery;

/**
 */
public class KettleDatabaseRepositoryConnectionDelegateUnitTest {

  @Test
  public void createIdsWithsValueQuery() {
    final String table = "table";
    final String id = "id";
    final String lookup = "lookup";
    final String expectedTemplate = format( "select %s from %s where %s in ", id, table, lookup ) + "(%s)";

    assertTrue( format( expectedTemplate, "?" ).equalsIgnoreCase( createIdsWithValuesQuery(table, id, lookup, 1 ) ) );
    assertTrue( format( expectedTemplate, "?,?" ).equalsIgnoreCase( createIdsWithValuesQuery( table, id, lookup, 2 ) ) );
  }
}
