package be.ibridge.kettle.core;

import java.io.UnsupportedEncodingException;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

public class XMLTransfer extends ByteArrayTransfer
{
    private static final String    MYTYPENAME = "KETTLE_XML_TRANSFER";

    private static final int       MYTYPEID   = registerType(MYTYPENAME);

    private static XMLTransfer _instance  = new XMLTransfer();

    public static XMLTransfer getInstance()
    {
        return _instance;
    }

    public void javaToNative(Object object, TransferData transferData)
    {
        if (!checkMyType(object) || !isSupportedType(transferData)) {
            DND.error(DND.ERROR_INVALID_DATA);
          }
        
        try
        {
            byte[] buffer = ((DragAndDropContainer)object).getXML().getBytes(Const.XML_ENCODING);

            super.javaToNative(buffer, transferData);
        }
        catch (UnsupportedEncodingException e)
        {
            LogWriter.getInstance().logError(toString(), "XML Encoding [" + Const.XML_ENCODING + "] is not supported on this system!");
            return;
        }
        catch (Exception e)
        {
            LogWriter.getInstance().logError(toString(), "Unexpected error trying to put a string onto the XML Transfer type: " + e.toString());
            LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
            return;
        }
    }
    
    boolean checkMyType(Object object)
    {
        if (object == null || !(object instanceof DragAndDropContainer) ) { return false; }
        
        return true;
    }

    public Object nativeToJava(TransferData transferData)
    {
        if (isSupportedType(transferData))
        {
            try
            {
                byte[] buffer = (byte[]) super.nativeToJava(transferData);
                return new DragAndDropContainer(new String(buffer, Const.XML_ENCODING));
            }
            catch (UnsupportedEncodingException e)
            {
                LogWriter.getInstance().logError(toString(), "XML Encoding [" + Const.XML_ENCODING + "] is not supported on this system!");
                return null;
            }
            catch (Exception e)
            {
                LogWriter.getInstance().logError(toString(), "Unexpected error trying to read a drag and drop container from the XML Transfer type: " + e.toString());
                LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
                return null;
            }
        }
        return null;
    }

    protected String[] getTypeNames()
    {
        return new String[] { MYTYPENAME };
    }

    protected int[] getTypeIds()
    {
        return new int[] { MYTYPEID };
    }
}
