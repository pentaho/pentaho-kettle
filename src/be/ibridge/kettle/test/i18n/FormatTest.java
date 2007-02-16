package be.ibridge.kettle.test.i18n;

import java.text.MessageFormat;
import java.util.Locale;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.i18n.LanguageChoice;

public class FormatTest
{
    public static void main(String[] args)
    {
        // String message = "Because of an error in ''{0}'', this step can''t continue : ''{1}''.";
        String message = "Because of an error this step can''t continue.";
        Object params[] = new Object[] { 
                "getLookup()", 
                "Some error message"+Const.CR+"With Line Breaks"+Const.CR+"And another line" };
        
        System.out.println("formatted message: "+MessageFormat.format(message, params));
        
        LanguageChoice.getInstance().setDefaultLocale(Locale.US);
        LanguageChoice.getInstance().setFailoverLocale(Locale.US);
        
        System.out.println();
        System.out.println("----------------------------------------------------------------------");
        System.out.println("test1: "+Messages.getString("FormatTest.1"));
        System.out.println("test2: "+Messages.getString("FormatTest.2"));
        System.out.println("test3: "+Messages.getString("FormatTest.3", "XXX"));
        System.out.println("test4: "+Messages.getString("FormatTest.4", "XXX"));
        System.out.println("test5: "+Messages.getString("FormatTest.5", "XXX", "YYY"));
        System.out.println("test6: "+Messages.getString("FormatTest.6", "XXX", "YYY"));
    }
}
