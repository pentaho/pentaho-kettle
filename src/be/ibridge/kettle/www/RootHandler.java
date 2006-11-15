package be.ibridge.kettle.www;

import java.io.IOException;

import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.ResourceHandler;

import be.ibridge.kettle.core.LogWriter;

public class RootHandler extends ResourceHandler
{
    private static final long serialVersionUID = 5525862902153097714L;
    
    private static LogWriter log = LogWriter.getInstance();

    public RootHandler()
    {
    }

    public void handle(String pathInContext, String pathParams, HttpRequest request, HttpResponse response) throws HttpException, IOException
    {
        if (!isStarted()) return;

        if (log.isDebug())
        {
            log.logDebug("Root Handler", "Access to root web page requested");
        }

        super.handle(pathInContext, pathParams, request, response);
    }

}
