package be.ibridge.kettle.www;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;

public class AddTransServlet extends HttpServlet
{
    private static final long serialVersionUID = -6850701762586992604L;
    private static LogWriter log = LogWriter.getInstance();
    
    public static final String CONTEXT_PATH = "/kettle/addTrans";
    
    private TransformationMap transformationMap;
    
    public AddTransServlet(TransformationMap transformationMap)
    {
        this.transformationMap = transformationMap;
    }
    
    public void init(ServletConfig servletConfig) throws ServletException
    {
        super.init(servletConfig);
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doPost(request, response);
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (!request.getServletPath().equals(CONTEXT_PATH)) return;

        if (log.isDebug()) log.logDebug(toString(), "Addition of transformation requested");

        boolean useXML = "Y".equalsIgnoreCase( request.getParameter("xml") );
        
        if (useXML)
        {
            response.setContentType("text/xml");
        }
        else
        {
            response.setContentType("text/html");
        }
        
        OutputStream os = response.getOutputStream(); // to write to the browser/client
        InputStream is = request.getInputStream(); // read from the client
        
        PrintStream out = new PrintStream(os);

        if (useXML)
        {
            out.print(XMLHandler.getXMLHeader());
        }
        else
        {
            out.println("<HTML>");
            out.println("<HEAD><TITLE>Add transformation</TITLE></HEAD>");
            out.println("<BODY>");
        }

        try
        {
            // First read the complete transformation in memory from the inputStream
            int c;
            StringBuffer xml = new StringBuffer();
            while ( (c=is.read())!=-1)
            {
                xml.append((char)c);
            }
            
            // Parse the XML, create a transformation
            //
            Document doc = XMLHandler.loadXMLString(xml.toString());
            Node transNode = XMLHandler.getSubNode(doc, "transformation");
            TransMeta transMeta = new TransMeta(transNode);
            
            // Create the transformation and store in the list...
            //
            Trans trans = new Trans(log, transMeta);
            
            Trans oldOne = transformationMap.getTransformation(trans.getName());
            if ( oldOne!=null && !oldOne.isFinished())
            {
                if ( oldOne.isRunning() || oldOne.isPreparing() || oldOne.isInitializing() )
                {
                    throw new Exception("A transformation with the same name exists and is not idle."+Const.CR+"Please stop the transformation first.");
                }
            }
            
            transformationMap.addTransformation(transMeta.getName(), trans);

            String message;
            if (oldOne!=null)
            {
                message = "Transformation '"+trans.getName()+"' was replaced in the list.";
            }
            else
            {
                message = "Transformation '"+trans.getName()+"' was added to the list.";
            }
            if (useXML)
            {
                out.println(new WebResult(WebResult.STRING_OK, message));
            }
            else
            {
                out.println("<H1>"+message+"</H1>");
                out.println("<p><a href=\"/kettle/transStatus?name="+trans.getName()+"\">Go to the transformation status page</a><p>");
            }
        }
        catch (Exception ex)
        {
            if (useXML)
            {
                out.println(new WebResult(WebResult.STRING_ERROR, Const.getStackTracker(ex)));
            }
            else
            {
                out.println("<p>");
                out.println("<pre>");
                ex.printStackTrace(out);
                out.println("</pre>");
            }
        }

        if (!useXML)
        {
            out.println("<p>");
            out.println("</BODY>");
            out.println("</HTML>");
        }

        out.flush();

        Request baseRequest = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
        baseRequest.setHandled(true);
    }
    
    public String toString()
    {
        return "Add Transformation";
    }

}
