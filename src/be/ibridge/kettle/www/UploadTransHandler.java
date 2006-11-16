package be.ibridge.kettle.www;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;

public class UploadTransHandler extends AbstractHandler
{
    private static final long       serialVersionUID = -6290587735180984638L;

    public static final String CONTEXT_PATH = "/kettle/uploadTrans";

    private static LogWriter        log              = LogWriter.getInstance();

    private final TransformationMap transformationMap;

    private final KettleFileFactory kettleFileFactory;

    public UploadTransHandler(TransformationMap transformationMap) throws Exception
    {
        this.transformationMap = transformationMap;
        this.kettleFileFactory = new KettleFileFactory(new File("fileUploads"), "kettleFile.xml");
    }

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
    {
        if (!request.getContextPath().equals(CONTEXT_PATH)) return;
        if (!isStarted()) return;

        if (log.isDebug()) log.logDebug(toString(), "Uploading Kettle transformation...");

        ByteArrayOutputStream buf = new ByteArrayOutputStream(2048);
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(buf));
        response.setContentType("text/html");

        File file = null;
        try
        {
            file = getTransformationFile(request);
            Document doc = XMLHandler.loadXMLFile(file);
            Node transNode = XMLHandler.getSubNode(doc, "transformation");
            TransMeta transMeta = new TransMeta(transNode);
            Trans trans = new Trans(log, transMeta);
            transformationMap.addTransformation(trans.getName(), trans);

            response.setContentType("text/html");
            writer.write("<html><title>Uploaded a Kettle transformation</title>");
            writer.write("<body>");
            writer.write("<h1>Uploaded of the Kettle transformation was succesful.</h1>");
            writer.write("<p><a href=\"/kettle/status\">Back to the status page</a><p>");
            writer.write("</body>");
            writer.write("</html>");

        }
        catch (Exception e)
        {
            log.logError(toString(), "The uploading of a transformation failed:" + e.toString());
            log.logError(toString(), Const.getStackTracker(e));
            
            response.setContentType("text/html");
            writer.write("<html><title>Uploaded of Kettle transformation failed</title>");
            writer.write("<body>");
            writer.write("<h1>The upload of the Kettle transformation failed!</h1>");
            writer.write("<pre>");
            writer.write(Const.getStackTracker(e));
            writer.write("</pre>");
            writer.write("<p><a href=\"/kettle/status\">Back to the status page</a><p>");
            writer.write("</body>");
            writer.write("</html>");

        }

        writer.flush();
        OutputStream out = response.getOutputStream();
        buf.writeTo(out);

        Request baseRequest = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
        baseRequest.setHandled(true);
     }

    private File getTransformationFile(HttpServletRequest request) throws Exception
    {
        kettleFileFactory.clear();
        FileUpload upload = new FileUpload(kettleFileFactory);
        upload.setHeaderEncoding(request.getCharacterEncoding());

        List fileItems = null;
        try
        {
            fileItems = upload.parseRequest(new WrappedHttpRequest(request));
        }
        catch (FileUploadException e)
        {
            throw new Exception("There was a problem during upload of a kettle file", e);
        }
        if (fileItems == null) { throw new Exception("No files arrived at the server"); }

        if (fileItems.size() > 1)
        {
            for (int i = 0; i < fileItems.size(); i++)
            {
                FileItem fileItem = (FileItem) fileItems.get(i);
                fileItem.delete();
            }
            throw new Exception("Only one file was expected to arrive at the server, but " + fileItems.size() + " were found.");
        }
        return ((SimpleFileItem) fileItems.get(0)).getFile();
    }

}
