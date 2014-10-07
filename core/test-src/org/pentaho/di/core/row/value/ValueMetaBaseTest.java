package org.pentaho.di.core.row.value;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Time;
import java.sql.Types;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.NetezzaDatabaseMeta;
import org.pentaho.di.core.database.Vertica5DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;

public class ValueMetaBaseTest {

  private static final String TEST_NAME = "TEST_NAME";
  // Get PKG from class under test
  private static Class<?> PKG = ( new ValueMetaBase() {
    public Class<?> getPackage() {
      return PKG;
    }
  } ).getPackage();

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    PluginRegistry.addPluginType( ValueMetaPluginType.getInstance() );
    PluginRegistry.addPluginType( DatabasePluginType.getInstance() );
    PluginRegistry.init( true );
  }

  @Test
  public void testDefaultCtor() {
    ValueMetaBase base = new ValueMetaBase();
    assertNotNull( base );
    assertNull( base.getName() );
    assertEquals( base.getType(), ValueMetaInterface.TYPE_NONE );
  }

  @Test
  public void testCtorName() {
    ValueMetaBase base = new ValueMetaBase( "myValueMeta" );
    assertEquals( base.getName(), "myValueMeta" );
    assertEquals( base.getType(), ValueMetaInterface.TYPE_NONE );
    assertNotNull( base.getTypeDesc() );
  }

  @Test
  public void testCtorNameAndType() {
    ValueMetaBase base = new ValueMetaBase( "myStringType", ValueMetaInterface.TYPE_STRING );
    assertEquals( base.getName(), "myStringType" );
    assertEquals( base.getType(), ValueMetaInterface.TYPE_STRING );
    assertEquals( base.getTypeDesc(), "String" );
  }

  public void test4ArgCtor() {
    ValueMetaBase base =
      new ValueMetaBase( "Hello, is it me you're looking for?", ValueMetaInterface.TYPE_BOOLEAN, 4, 9 );
    assertEquals( base.getName(), "Hello, is it me you're looking for?" );
    assertEquals( base.getType(), ValueMetaInterface.TYPE_BOOLEAN );
    assertEquals( base.getLength(), 4 );
    assertEquals( base.getPrecision(), -1 );
    assertEquals( base.getStorageType(), ValueMetaInterface.STORAGE_TYPE_NORMAL );
  }

  /**
   * PDI-10877 Table input step returns no data when pulling a timestamp column from IBM Netezza
   *
   * @throws Exception
   */
  @Test
  public void testGetValueFromSqlTypeNetezza() throws Exception {
    ValueMetaBase obj = new ValueMetaBase();
    DatabaseInterface databaseInterface = new NetezzaDatabaseMeta();
    ResultSet resultSet = Mockito.mock( ResultSet.class );
    ResultSetMetaData metaData = Mockito.mock( ResultSetMetaData.class );
    Mockito.when( resultSet.getMetaData() ).thenReturn( metaData );

    Mockito.when( metaData.getColumnType( 1 ) ).thenReturn( Types.DATE );
    Mockito.when( metaData.getColumnType( 2 ) ).thenReturn( Types.TIME );

    obj.type = ValueMetaInterface.TYPE_DATE;
    // call to testing method
    obj.getValueFromResultSet( databaseInterface, resultSet, 0 );
    // for jdbc Date type getDate method called
    Mockito.verify( resultSet, Mockito.times( 1 ) ).getDate( Mockito.anyInt() );

    obj.getValueFromResultSet( databaseInterface, resultSet, 1 );
    // for jdbc Time type getTime method called
    Mockito.verify( resultSet, Mockito.times( 1 ) ).getTime( Mockito.anyInt() );
  }

  @Test
  public void testGetBinaryWithLength_WhenBinarySqlTypesOfVertica() throws Exception {
    final int binaryColumnIndex = 1;
    final int varbinaryColumnIndex = 2;
    final int expectedBinarylength = 1;
    final int expectedVarBinarylength = 80;

    ValueMetaBase obj = new ValueMetaBase();
    DatabaseMeta dbMeta = Mockito.spy( new DatabaseMeta() );
    DatabaseInterface databaseInterface = new Vertica5DatabaseMeta();
    dbMeta.setDatabaseInterface( databaseInterface );

    ResultSet resultSet = Mockito.mock( ResultSet.class );
    ResultSetMetaData metaData = Mockito.mock( ResultSetMetaData.class );

    Mockito.when( resultSet.getMetaData() ).thenReturn( metaData );
    Mockito.when( metaData.getColumnType( binaryColumnIndex ) ).thenReturn( Types.BINARY );
    Mockito.when( metaData.getPrecision( binaryColumnIndex ) ).thenReturn( expectedBinarylength );
    Mockito.when( metaData.getColumnDisplaySize( binaryColumnIndex ) ).thenReturn( expectedBinarylength * 2 );

    Mockito.when( metaData.getColumnType( varbinaryColumnIndex ) ).thenReturn( Types.BINARY );
    Mockito.when( metaData.getPrecision( varbinaryColumnIndex ) ).thenReturn( expectedVarBinarylength );
    Mockito.when( metaData.getColumnDisplaySize( varbinaryColumnIndex ) ).thenReturn( expectedVarBinarylength * 2 );

    // get value meta for binary type
    ValueMetaInterface binaryValueMeta =
      obj.getValueFromSQLType( dbMeta, TEST_NAME, metaData, binaryColumnIndex, false, false );
    assertNotNull( binaryValueMeta );
    assertTrue( TEST_NAME.equals( binaryValueMeta.getName() ) );
    assertTrue( ValueMetaInterface.TYPE_BINARY == binaryValueMeta.getType() );
    assertTrue( expectedBinarylength == binaryValueMeta.getLength() );
    assertFalse( binaryValueMeta.isLargeTextField() );

    // get value meta for varbinary type
    ValueMetaInterface varbinaryValueMeta =
      obj.getValueFromSQLType( dbMeta, TEST_NAME, metaData, varbinaryColumnIndex, false, false );
    assertNotNull( varbinaryValueMeta );
    assertTrue( TEST_NAME.equals( varbinaryValueMeta.getName() ) );
    assertTrue( ValueMetaInterface.TYPE_BINARY == varbinaryValueMeta.getType() );
    assertTrue( expectedVarBinarylength == varbinaryValueMeta.getLength() );
    assertFalse( varbinaryValueMeta.isLargeTextField() );

  }

  @Test
  public void testVerticaTimeType() throws Exception {
    // PDI-12244
    ResultSet resultSet = Mockito.mock( ResultSet.class );
    ResultSetMetaData metaData = Mockito.mock( ResultSetMetaData.class );
    ValueMetaInterface valueMetaInterface = Mockito.mock( ValueMetaInternetAddress.class );

    Mockito.when( resultSet.getMetaData() ).thenReturn( metaData );
    Mockito.when( metaData.getColumnType( 1 ) ).thenReturn( Types.TIME );
    Mockito.when( resultSet.getTime( 1 ) ).thenReturn( new Time( 0 ) );
    Mockito.when( valueMetaInterface.getOriginalColumnType() ).thenReturn( Types.TIME );
    Mockito.when( valueMetaInterface.getType() ).thenReturn( ValueMetaInterface.TYPE_DATE );

    DatabaseInterface databaseInterface = new Vertica5DatabaseMeta();
    Object ret = databaseInterface.getValueFromResultSet( resultSet, valueMetaInterface, 0 );
    assertEquals( new Time( 0 ), ret );
  }

  @Test
  public void testConvertStringToBoolean() {
    assertNull( ValueMetaBase.convertStringToBoolean( null ) );
    assertNull( ValueMetaBase.convertStringToBoolean( "" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "Y" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "y" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "Yes" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "YES" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "yES" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "TRUE" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "True" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "true" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "tRuE" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "Y" ) );
    assertFalse( ValueMetaBase.convertStringToBoolean( "N" ) );
    assertFalse( ValueMetaBase.convertStringToBoolean( "No" ) );
    assertFalse( ValueMetaBase.convertStringToBoolean( "no" ) );
    assertFalse( ValueMetaBase.convertStringToBoolean( "Yeah" ) );
    assertFalse( ValueMetaBase.convertStringToBoolean( "False" ) );
    assertFalse( ValueMetaBase.convertStringToBoolean( "NOT false" ) );
  }

  @Test
  public void testIsNumeric() {
    assertTrue( ValueMetaBase.isNumeric( ValueMetaInterface.TYPE_INTEGER ) );
    assertTrue( ValueMetaBase.isNumeric( ValueMetaInterface.TYPE_NUMBER ) );
    assertTrue( ValueMetaBase.isNumeric( ValueMetaInterface.TYPE_BIGNUMBER ) );
    assertFalse( ValueMetaBase.isNumeric( ValueMetaInterface.TYPE_INET ) );
    assertFalse( ValueMetaBase.isNumeric( ValueMetaInterface.TYPE_BOOLEAN ) );
    assertFalse( ValueMetaBase.isNumeric( ValueMetaInterface.TYPE_BINARY ) );
    assertFalse( ValueMetaBase.isNumeric( ValueMetaInterface.TYPE_DATE ) );
    assertFalse( ValueMetaBase.isNumeric( ValueMetaInterface.TYPE_STRING ) );
  }

  @Test
  public void testGetAllTypes() {
    assertArrayEquals( ValueMetaBase.getAllTypes(), ValueMetaFactory.getAllValueMetaNames() );
  }

  @Test
  public void testGetTrimTypeByCode() {
    assertEquals( ValueMetaBase.getTrimTypeByCode( "none" ), ValueMetaInterface.TRIM_TYPE_NONE );
    assertEquals( ValueMetaBase.getTrimTypeByCode( "left" ), ValueMetaInterface.TRIM_TYPE_LEFT );
    assertEquals( ValueMetaBase.getTrimTypeByCode( "right" ), ValueMetaInterface.TRIM_TYPE_RIGHT );
    assertEquals( ValueMetaBase.getTrimTypeByCode( "both" ), ValueMetaInterface.TRIM_TYPE_BOTH );
    assertEquals( ValueMetaBase.getTrimTypeByCode( null ), ValueMetaInterface.TRIM_TYPE_NONE );
    assertEquals( ValueMetaBase.getTrimTypeByCode( "" ), ValueMetaInterface.TRIM_TYPE_NONE );
    assertEquals( ValueMetaBase.getTrimTypeByCode( "fake" ), ValueMetaInterface.TRIM_TYPE_NONE );
  }

  @Test
  public void testGetTrimTypeCode() {
    assertEquals( ValueMetaBase.getTrimTypeCode( ValueMetaInterface.TRIM_TYPE_NONE ), "none" );
    assertEquals( ValueMetaBase.getTrimTypeCode( ValueMetaInterface.TRIM_TYPE_LEFT ), "left" );
    assertEquals( ValueMetaBase.getTrimTypeCode( ValueMetaInterface.TRIM_TYPE_RIGHT ), "right" );
    assertEquals( ValueMetaBase.getTrimTypeCode( ValueMetaInterface.TRIM_TYPE_BOTH ), "both" );
  }

  @Test
  public void testGetTrimTypeByDesc() {
    assertEquals( ValueMetaBase.getTrimTypeByDesc( BaseMessages.getString( PKG, "ValueMeta.TrimType.None" ) ),
      ValueMetaInterface.TRIM_TYPE_NONE );
    assertEquals( ValueMetaBase.getTrimTypeByDesc( BaseMessages.getString( PKG, "ValueMeta.TrimType.Left" ) ),
      ValueMetaInterface.TRIM_TYPE_LEFT );
    assertEquals( ValueMetaBase.getTrimTypeByDesc( BaseMessages.getString( PKG, "ValueMeta.TrimType.Right" ) ),
      ValueMetaInterface.TRIM_TYPE_RIGHT );
    assertEquals( ValueMetaBase.getTrimTypeByDesc( BaseMessages.getString( PKG, "ValueMeta.TrimType.Both" ) ),
      ValueMetaInterface.TRIM_TYPE_BOTH );
    assertEquals( ValueMetaBase.getTrimTypeByDesc( null ), ValueMetaInterface.TRIM_TYPE_NONE );
    assertEquals( ValueMetaBase.getTrimTypeByDesc( "" ), ValueMetaInterface.TRIM_TYPE_NONE );
    assertEquals( ValueMetaBase.getTrimTypeByDesc( "fake" ), ValueMetaInterface.TRIM_TYPE_NONE );
  }

  @Test
  public void testGetTrimTypeDesc() {
    assertEquals( ValueMetaBase.getTrimTypeDesc( ValueMetaInterface.TRIM_TYPE_NONE ), BaseMessages.getString( PKG,
      "ValueMeta.TrimType.None" ) );
    assertEquals( ValueMetaBase.getTrimTypeDesc( ValueMetaInterface.TRIM_TYPE_LEFT ), BaseMessages.getString( PKG,
      "ValueMeta.TrimType.Left" ) );
    assertEquals( ValueMetaBase.getTrimTypeDesc( ValueMetaInterface.TRIM_TYPE_RIGHT ), BaseMessages.getString( PKG,
      "ValueMeta.TrimType.Right" ) );
    assertEquals( ValueMetaBase.getTrimTypeDesc( ValueMetaInterface.TRIM_TYPE_BOTH ), BaseMessages.getString( PKG,
      "ValueMeta.TrimType.Both" ) );
    assertEquals( ValueMetaBase.getTrimTypeDesc( -1 ), BaseMessages.getString( PKG, "ValueMeta.TrimType.None" ) );
    assertEquals( ValueMetaBase.getTrimTypeDesc( 10000 ), BaseMessages.getString( PKG, "ValueMeta.TrimType.None" ) );
  }

  @Test
  public void testOrigin() {
    ValueMetaBase base = new ValueMetaBase();
    base.setOrigin( "myOrigin" );
    assertEquals( base.getOrigin(), "myOrigin" );
    base.setOrigin( null );
    assertNull( base.getOrigin() );
    base.setOrigin( "" );
    assertEquals( base.getOrigin(), "" );
  }

  @Test
  public void testName() {
    ValueMetaBase base = new ValueMetaBase();
    base.setName( "myName" );
    assertEquals( base.getName(), "myName" );
    base.setName( null );
    assertNull( base.getName() );
    base.setName( "" );
    assertEquals( base.getName(), "" );

  }

  @Test
  public void testLength() {
    ValueMetaBase base = new ValueMetaBase();
    base.setLength( 6 );
    assertEquals( base.getLength(), 6 );
    base.setLength( -1 );
    assertEquals( base.getLength(), -1 );
  }

  @Test
  public void testPrecision() {
    ValueMetaBase base = new ValueMetaBase();
    base.setPrecision( 6 );
    assertEquals( base.getPrecision(), 6 );
    base.setPrecision( -1 );
    assertEquals( base.getPrecision(), -1 );
  }

  @Test
  public void testCompareIntegers() throws KettleValueException {
    ValueMetaBase intMeta = new ValueMetaBase( "int", ValueMetaInterface.TYPE_INTEGER );
    Long int1 = new Long( 6223372036854775804L );
    Long int2 = new Long( -6223372036854775804L );
    assertEquals( 1, intMeta.compare( int1, int2 ) );
    assertEquals( -1, intMeta.compare( int2, int1 ) );
    assertEquals( 0, intMeta.compare( int1, int1 ) );
    assertEquals( 0, intMeta.compare( int2, int2 ) );

    int1 = new Long( 9223372036854775804L );
    int2 = new Long( -9223372036854775804L );
    assertEquals( 1, intMeta.compare( int1, int2 ) );
    assertEquals( -1, intMeta.compare( int2, int1 ) );
    assertEquals( 0, intMeta.compare( int1, int1 ) );
    assertEquals( 0, intMeta.compare( int2, int2 ) );

    int1 = new Long( 6223372036854775804L );
    int2 = new Long( -9223372036854775804L );
    assertEquals( 1, intMeta.compare( int1, int2 ) );
    assertEquals( -1, intMeta.compare( int2, int1 ) );
    assertEquals( 0, intMeta.compare( int1, int1 ) );

    int1 = new Long( 9223372036854775804L );
    int2 = new Long( -6223372036854775804L );
    assertEquals( 1, intMeta.compare( int1, int2 ) );
    assertEquals( -1, intMeta.compare( int2, int1 ) );
    assertEquals( 0, intMeta.compare( int1, int1 ) );

  }

  @Test
  public void testCompareDate() throws KettleValueException {
    ValueMetaBase dateMeta = new ValueMetaBase( "int", ValueMetaInterface.TYPE_DATE );
    Date date1 = new Date( 6223372036854775804L );
    Date date2 = new Date( -6223372036854775804L );
    assertEquals( 1, dateMeta.compare( date1, date2 ) );
    assertEquals( -1, dateMeta.compare( date2, date1 ) );
    assertEquals( 0, dateMeta.compare( date1, date1 ) );
  }

}
