package org.pentaho.di.i18n;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.pentaho.di.core.Const;

public class LanguageChoice
{
    private static final String STRING_FAILOVER_LOCALE        = "LocaleFailover";
    private static final String STRING_DEFAULT_LOCALE         = "LocaleDefault";
    
    
    
    private static LanguageChoice choice;
    
    private Locale defaultLocale;
    private Locale failoverLocale;
    
    
    private LanguageChoice()
    {
        try
        {
            loadSettings();
        }
        catch(IOException e)  // Can't load settings: set the default
        {
            defaultLocale = Const.DEFAULT_LOCALE;
            failoverLocale = Locale.US;
            
            if (defaultLocale.getLanguage().equals(Locale.GERMAN.getLanguage()))
            {
                defaultLocale = Locale.US;
            }
        }
    }
    
    public static final LanguageChoice getInstance()
    {
        if (choice!=null) return choice;
        
        choice = new LanguageChoice();
        
        // System.out.println("Loaded language choices: default="+choice.defaultLocale.toString()+", failover="+choice.failoverLocale.toString());
        
        return choice;
    }

    /**
     * @return Returns the defaultLocale.
     */
    public Locale getDefaultLocale()
    {
        return defaultLocale;
    }

    /**
     * @param defaultLocale The defaultLocale to set.
     */
    public void setDefaultLocale(Locale defaultLocale)
    {
        this.defaultLocale = defaultLocale;
    }

    /**
     * @return Returns the failoverLocale.
     */
    public Locale getFailoverLocale()
    {
        return failoverLocale;
    }

    /**
     * @param failoverLocale The failoverLocale to set.
     */
    public void setFailoverLocale(Locale failoverLocale)
    {
        this.failoverLocale = failoverLocale;
    }
    
    private void loadSettings() throws IOException
    {
        Properties properties = new Properties();
        properties.load(new FileInputStream(getSettingsFilename()));
        defaultLocale  = new Locale( properties.getProperty(STRING_DEFAULT_LOCALE,  Const.DEFAULT_LOCALE.toString()) );
        failoverLocale = new Locale( properties.getProperty(STRING_FAILOVER_LOCALE, "en_US") );
    }
    
    public void saveSettings()
    {
        try
        {
            Properties properties = new Properties();
            properties.setProperty(STRING_DEFAULT_LOCALE, defaultLocale.toString());
            properties.setProperty(STRING_FAILOVER_LOCALE, failoverLocale.toString());
            properties.store(new FileOutputStream(getSettingsFilename()), "Language Choice");
        }
        catch(IOException e)
        {
            ;
        }
    }
    
    public String getSettingsFilename()
    {
        return Const.getKettleDirectory()+Const.FILE_SEPARATOR+".languageChoice";
    }
}
