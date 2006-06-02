package be.ibridge.kettle.core.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author wdeclerc
 */
public class StringUtilTest extends TestCase
{
	public void testSubstituteUnix()
	{
		Map variables = new HashMap();

		variables.put("x", "val1");
		variables.put("y", "val2");
		variables.put("xx", "val1b");
		variables.put("yy", "val2b");

		String result = StringUtil.substituteUnix("aa${x}bb${y}cc", variables);
		assertEquals("aaval1bbval2cc", result);

		result = StringUtil.substituteUnix("aa${xx}bb${yy}cc", variables);
		assertEquals("aaval1bbbval2bcc", result);

		result = StringUtil.substituteUnix("${xx}bb${yy}cc", variables);
		assertEquals("val1bbbval2bcc", result);

		result = StringUtil.substituteUnix("${xx}bb${yy}", variables);
		assertEquals("val1bbbval2b", result);

		result = StringUtil.substituteUnix("${xx}${yy}", variables);
		assertEquals("val1bval2b", result);

		result = StringUtil.substituteUnix("${x}${y}", variables);
		assertEquals("val1val2", result);

		result = StringUtil.substituteUnix("${x}${y", variables);
		assertEquals("val1${y", result);
	}

	public void testSubstituteWindows()
	{
		Map variables = new HashMap();

		variables.put("x", "val1");
		variables.put("y", "val2");
		variables.put("xx", "val1b");
		variables.put("yy", "val2b");

		String result = StringUtil.substituteWindows("aa%%x%%bb%%y%%cc",
				variables);
		assertEquals("aaval1bbval2cc", result);

		result = StringUtil.substituteWindows("aa%%xx%%bb%%yy%%cc", variables);
		assertEquals("aaval1bbbval2bcc", result);

		result = StringUtil.substituteWindows("%%xx%%bb%%yy%%cc", variables);
		assertEquals("val1bbbval2bcc", result);

		result = StringUtil.substituteWindows("%%xx%%bb%%yy%%", variables);
		assertEquals("val1bbbval2b", result);

		result = StringUtil.substituteWindows("%%xx%%%%yy%%", variables);
		assertEquals("val1bval2b", result);

		result = StringUtil.substituteWindows("%%x%%%%y%%", variables);
		assertEquals("val1val2", result);

		result = StringUtil.substituteWindows("%%x%%%%y", variables);
		assertEquals("val1%%y", result);
	}
}
