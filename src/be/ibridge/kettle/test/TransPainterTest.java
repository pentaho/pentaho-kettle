package be.ibridge.kettle.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.dialog.ShowImageDialog;
import be.ibridge.kettle.spoon.TransPainter;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.TransMeta;

public class TransPainterTest
{
    private static final String TESTNAME = "TransPainterTest";
    private static LogWriter log = LogWriter.getInstance(LogWriter.LOG_LEVEL_DETAILED);

    private static final String testTransformationFile = "testfiles/testTransformation.xml"; 
    private static final String imageFilename          = "testfiles/testTransformation.bmp";
       
    public TransPainterTest()
    {
        
    }
    
    /**
     * Test the creation, working and destruction of a repository.
     * @throws Exception in case something goes wrong (unacceptable ;-))
     */
    public void testTransPainter() throws Exception
    {
       // Load a test transformation from an XML file... 
       log.logBasic(TESTNAME, "Load test transformation from file "+testTransformationFile);
       TransMeta transMeta = new TransMeta(testTransformationFile); 

       // OK, now create an image.
       Display display = new Display();  // Create a new display
       Props.init(display, Props.TYPE_PROPERTIES_SPOON);  // things to remember...
       
       Point area = transMeta.getMaximum();
 
       TransPainter transPainter = new TransPainter(transMeta, area, null, null, null, null, null);
       Image image = transPainter.getTransformationImage();

       Shell shell = new Shell(display);
       ShowImageDialog sid = new ShowImageDialog(shell, image);
       sid.open();
       
       ImageLoader imageLoader = new ImageLoader();
       ImageData imageData = image.getImageData();
       System.out.println("Image data type : "+imageData.type);
       imageLoader.data = new ImageData[] { imageData } ;
       imageLoader.save(imageFilename, SWT.IMAGE_BMP);
    }

    /**
    * @param args
    */
    public static void main(String[] args) throws Exception
    {
       StepLoader.getInstance().read();

       new TransPainterTest().testTransPainter();
    }

}
