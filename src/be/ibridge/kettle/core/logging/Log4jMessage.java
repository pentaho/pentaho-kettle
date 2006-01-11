package be.ibridge.kettle.core.logging;

import be.ibridge.kettle.core.LogWriter;

/**
 * Contains a Logging message with a message and a subject
 * @author Matt
 *
 */
public class Log4jMessage
{
    private String message;
    private String subject;
    private int    level;
    
    public Log4jMessage(String message, String subject, int level)
    {
        this.message = message;
        this.subject = subject;
        this.level   = level;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public int getLevel()
    {
        return level;
    }
    
    public void setLevel(int level)
    {
        this.level = level;
    }
    
    public boolean isError()
    {
        return level==LogWriter.LOG_LEVEL_ERROR;
    }
}
