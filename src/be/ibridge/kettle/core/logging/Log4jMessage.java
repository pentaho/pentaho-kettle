package be.ibridge.kettle.core.logging;

/**
 * Contains a Logging message with a message and a subject
 * @author Matt
 *
 */
public class Log4jMessage
{
    private String message;
    private String subject;
    
    public Log4jMessage(String message, String subject)
    {
        this.message = message;
        this.subject = subject;
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

    
}
