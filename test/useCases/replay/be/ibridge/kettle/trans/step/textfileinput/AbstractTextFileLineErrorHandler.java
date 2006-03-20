package be.ibridge.kettle.trans.step.textfileinput;

import java.text.SimpleDateFormat;

public class AbstractTextFileLineErrorHandler
{
    public static SimpleDateFormat createDateFormat()
    {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
    }

}
