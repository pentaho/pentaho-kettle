package be.ibridge.kettle.core;

import java.io.UnsupportedEncodingException;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

public class XMLTransferType extends ByteArrayTransfer
{
    private static final String    MYTYPENAME = "KETTLE_XML_TRANSFER";

    private static final int       MYTYPEID   = registerType(MYTYPENAME);

    private static XMLTransferType _instance  = new XMLTransferType();

    public static XMLTransferType getInstance()
    {
        return _instance;
    }

    public void javaToNative(Object object, TransferData transferData)
    {
        if (object == null || !(object instanceof String)) return;

        if (isSupportedType(transferData))
        {
            try
            {
                byte[] buffer = ((String)object).getBytes(Const.XML_ENCODING);

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
    }

    public Object nativeToJava(TransferData transferData)
    {
        if (isSupportedType(transferData))
        {
            try
            {
                byte[] buffer = (byte[]) super.nativeToJava(transferData);
                return (Object)(new String(buffer, Const.XML_ENCODING));
            }
            catch (UnsupportedEncodingException e)
            {
                LogWriter.getInstance().logError(toString(), "XML Encoding [" + Const.XML_ENCODING + "] is not supported on this system!");
                return null;
            }
            catch (Exception e)
            {
                LogWriter.getInstance().logError(toString(), "Unexpected error trying to read a string from the XML Transfer type: " + e.toString());
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
