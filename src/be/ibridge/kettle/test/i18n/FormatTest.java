package be.ibridge.kettle.test.i18n;

import java.text.MessageFormat;

import be.ibridge.kettle.core.Const;

public class FormatTest
{
    public static void main(String[] args)
    {
        String message = "Because of an error in [{0}], this step can''t continue : {1}.";
        Object params[] = new Object[] { 
                "[getLookup()]", 
                "Some error message"+Const.CR+"With Line Breaks"+Const.CR+"And another line" };
        
        System.out.println("formatted message: "+MessageFormat.format(message, params));
    }
}
