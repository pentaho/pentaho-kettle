package org.pentaho.di.core.sql;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

public class SQLFieldsUnitTest {
  private RowMetaInterface serviceFields;

  @Before
  public void setup() {
    serviceFields = mock( RowMetaInterface.class );
  }

  @Test
  public void testParseWorksWithSpaces() throws KettleSQLException {
    String nospace = "nospace";
    String withSpaces = "with spaces";
    ValueMetaInterface nospaceVmi = mock( ValueMetaInterface.class );
    ValueMetaInterface spaceVmi = mock( ValueMetaInterface.class );

    when( serviceFields.searchValueMeta( nospace ) ).thenReturn( nospaceVmi );
    when( serviceFields.searchValueMeta( withSpaces ) ).thenReturn( spaceVmi );

    new SQLFields( "table", serviceFields, nospace + ", \"" + withSpaces + "\"" );
  }

  @Test
  public void testParseWorksWithSpacesStar() throws KettleSQLException {
    String nospace = "nospace";
    String withSpaces = "with spaces";
    ValueMetaInterface nospaceVmi = mock( ValueMetaInterface.class );
    ValueMetaInterface spaceVmi = mock( ValueMetaInterface.class );

    List<ValueMetaInterface> valueMetaList = Arrays.asList( nospaceVmi, spaceVmi );
    when( serviceFields.getValueMetaList() ).thenReturn( valueMetaList );
    when( nospaceVmi.getName() ).thenReturn( nospace );
    when( spaceVmi.getName() ).thenReturn( withSpaces );
    when( serviceFields.searchValueMeta( nospace ) ).thenReturn( nospaceVmi );
    when( serviceFields.searchValueMeta( withSpaces ) ).thenReturn( spaceVmi );

    new SQLFields( "table", serviceFields, "*" );
  }
}
