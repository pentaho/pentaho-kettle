package org.pentaho.di.trans.steps.getvariable;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransformationTestCase;

public class GetVariableMetaTest extends TransformationTestCase {

	public GetVariableMetaTest() throws KettleException {
		super();
	}

	@Test
	public void testClone1() {
		GetVariableMeta gvm  = new GetVariableMeta();
		gvm.allocate(1);
		String[] fieldName   = {"Test"};
		int[] fieldType      = {ValueMeta.getType("Number")};
		String[] varString   = {"${testVariable}"};
		int[] fieldLength    = {1};
		int[] fieldPrecision = {2};
		String[] currency    = {"$"};
		String[] decimal     = {"."};
		String[] group       = {"TestGroup"};
		int[] trimType       = {ValueMeta.getTrimTypeByDesc("none")};
		
		gvm.setFieldName(fieldName);
		gvm.setFieldType(fieldType);
		gvm.setVariableString(varString);
		gvm.setFieldLength(fieldLength);
		gvm.setFieldPrecision(fieldPrecision);
		gvm.setCurrency(currency);
		gvm.setDecimal(decimal);
		gvm.setGroup(group);
		gvm.setTrimType(trimType);
		
		GetVariableMeta clone = (GetVariableMeta)gvm.clone();
		assertEquals(clone.getFieldName()[0],"Test");
		assertEquals(clone.getFieldType()[0],ValueMetaInterface.TYPE_NUMBER);
		assertEquals(clone.getVariableString()[0],"${testVariable}");
		assertEquals(clone.getFieldLength()[0],1);
		assertEquals(clone.getFieldPrecision()[0],2);
		assertEquals(clone.getCurrency()[0],"$");
		assertEquals(clone.getDecimal()[0],".");
		assertEquals(clone.getGroup()[0],"TestGroup");
		assertEquals(clone.getTrimType()[0],ValueMetaInterface.TRIM_TYPE_NONE);
	}

	@Test
	public void testGetVariableMeta1() {
		GetVariableMeta gvm = new GetVariableMeta();
		assertNotNull(gvm);
		assertNull(gvm.getFieldName());
		assertNull(gvm.getVariableString());
		assertNull(gvm.getFieldFormat());
		assertNull(gvm.getFieldType());
		assertNull(gvm.getFieldLength());
		assertNull(gvm.getFieldPrecision());
		assertNull(gvm.getCurrency());
		assertNull(gvm.getDecimal());
		assertNull(gvm.getGroup());
		assertNull(gvm.getTrimType());
	}

	@Test
	public void testAllocate1() {
		GetVariableMeta gvm = new GetVariableMeta();
		gvm.allocate(1);
		assertNotNull(gvm.getFieldName());
		assertNotNull(gvm.getVariableString());
		assertNotNull(gvm.getFieldFormat());
		assertNotNull(gvm.getFieldType());
		assertNotNull(gvm.getFieldLength());
		assertNotNull(gvm.getFieldPrecision());
		assertNotNull(gvm.getCurrency());
		assertNotNull(gvm.getDecimal());
		assertNotNull(gvm.getGroup());
		assertNotNull(gvm.getTrimType());
	}

}
