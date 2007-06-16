package be.ibridge.kettle.core.logging;

import org.apache.log4j.spi.LoggingEvent;

public interface Log4JLayoutInterface
{
    public String format(LoggingEvent event);
    public boolean ignoresThrowable();
    public void activateOptions();
    public boolean isTimeAdded();
    public void setTimeAdded(boolean addTime);
}
