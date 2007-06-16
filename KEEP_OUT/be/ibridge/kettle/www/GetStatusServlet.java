package be.ibridge.kettle.www;

import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.trans.Trans;

public class GetStatusServlet extends HttpServlet
{
    private static final long serialVersionUID = 3634806745372015720L;
    
    public static final String CONTEXT_PATH = "/kettle/status";
    
    
    private static LogWriter log = LogWriter.getInstance();
    private TransformationMap transformationMap;
    
    public GetStatusServlet(TransformationMap transformationMap)
    {
        this.transformationMap = transformationMap;
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (!request.getContextPath().equals(CONTEXT_PATH)) return;
        
        if (log.isDebug()) log.logDebug(toString(), "Status requested");
        response.setStatus(HttpServletResponse.SC_OK);
        
        boolean useXML = "Y".equalsIgnoreCase( request.getParameter("xml") );

        if (useXML)
        {
            response.setContentType("text/xml");
            response.setCharacterEncoding(Const.XML_ENCODING);
        }
        else
        {
            response.setContentType("text/html");
        }
        
        PrintStream out = new PrintStream(response.getOutputStream());
        if (useXML)
        {
            out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
            SlaveServerStatus serverStatus = new SlaveServerStatus();
            serverStatus.setStatusDescription("Online");
            
            String[] transNames = transformationMap.getTransformationNames();
            for (int i=0;i<transNames.length;i++)
            {
                String name   = transNames[i]; 
                Trans  trans  = transformationMap.getTransformation(name);
                String status = trans.getStatus();
                
                serverStatus.getTransStatusList().add( new SlaveServerTransStatus(name, status) );
            }
            
            out.println(serverStatus.getXML());
        }
        else
        {    
            out.println("<HTML>");
            out.println("<HEAD><TITLE>Kettle slave server status</TITLE></HEAD>");
            out.println("<BODY>");
            out.println("<H1>Status</H1>");
    
            out.println("<table border=\"1\">");
            out.print("<tr> <th>Transformation name</th> <th>Status</th> </tr>");
    
            try
            {
                String[] transNames = transformationMap.getTransformationNames();
                for (int i=0;i<transNames.length;i++)
                {
                    String name   = transNames[i]; 
                    Trans  trans  = transformationMap.getTransformation(name);
                    String status = trans.getStatus();
                    
                    out.print("<tr>");
                    out.print("<td><a href=\"/kettle/transStatus?name="+name+"\">"+name+"</a></td>");
                    out.print("<td>"+status+"</td>");
                    out.print("</tr>");
                }
                out.print("</table>");
            }
            catch (Exception ex)
            {
                out.println("<p>");
                out.println("<pre>");
                ex.printStackTrace(out);
                out.println("</pre>");
            }
    
            out.println("<p>");
            out.println("</BODY>");
            out.println("</HTML>");
        }        
    }

    public String toString()
    {
        return "Status Handler";
    }
}
