package be.ibridge.kettle.core;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import be.ibridge.kettle.core.exception.KettleStepLoaderException;
import be.ibridge.kettle.job.JobEntryLoader;
import be.ibridge.kettle.job.JobPlugin;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.StepPlugin;

/**
 * This is a singleton class that contains allocated Fonts, Colors, etc.
 * All colors etc. are allocated once and released once at the end of the program.
 * 
 * @author Matt
 * @since  27/10/2005
 *
 */
public class GUIResource
{
    private static LogWriter log = LogWriter.getInstance();

    private static GUIResource guiResource;
    
    private Display display;
    
    // 33 resources
    
    /* * * Colors * * */
    private ManagedColor colorBackground;
    private ManagedColor colorGraph;
    private ManagedColor colorTab;

    private ManagedColor colorRed;
    private ManagedColor colorGreen;
    private ManagedColor colorBlue;
    private ManagedColor colorOrange;
    private ManagedColor colorYellow;
    private ManagedColor colorMagenta;
    private ManagedColor colorBlack;
    private ManagedColor colorGray;
    private ManagedColor colorDarkGray;
    private ManagedColor colorLightGray;
    private ManagedColor colorDemoGray;
    private ManagedColor colorWhite;
    private ManagedColor colorDirectory;

    /* * * Fonts * * */
    private ManagedFont fontGraph;
    private ManagedFont fontNote;
    private ManagedFont fontFixed;
    private ManagedFont fontLarge;

    /* * * Images * * */
    private Hashtable imagesSteps;
    private Hashtable imagesStepsSmall;
    
    private Hashtable imagesJobentries;
    private Hashtable imagesJobentriesSmall;
    
    private Image     imageHop;
    private Image     imageConnection; 
    private Image     imageBol;
    private Image     imageKettle;
    private Image     imageCredits;
    private Image     imageStart;
    private Image     imageDummy;
    private Image     imageSpoon;
    private Image     imageChef;
    private Image     imagePentaho;
    private Image     imageVariable;
    private Image     imageSpoonGraph;
    
    private Image     imageSplash;

    private ManagedFont fontBold;

    /**
     * GUIResource also contains the clipboard as it has to be allocated only once!
     * I don't want to put it in a seperate singleton just for this one member.
     */
    private static Clipboard clipboard;

    private GUIResource(Display display)
    {
        this.display = display;
        
        getResources(false);
        
        display.addListener(SWT.Dispose, new Listener()
            {
                public void handleEvent(Event event)
                {
                    dispose(false);
                }
            }
        );
        
        clipboard = null;
    }
    
    public static final GUIResource getInstance()
    {
        if (guiResource!=null) return guiResource;
        guiResource = new GUIResource(Props.getInstance().getDisplay());
        return guiResource;
    }
        
    public void reload()
    {
        dispose(true);
        getResources(true);
    }
    
    private void getResources(boolean reload)
    {
        Props props = Props.getInstance();
        
        colorBackground = new ManagedColor(display, props.getBackgroundRGB() );
        colorGraph      = new ManagedColor(display, props.getGraphColorRGB() );
        colorTab        = new ManagedColor(display, props.getTabColorRGB()   );
        
        colorRed        = new ManagedColor(display, 255,   0,   0 );
        colorGreen      = new ManagedColor(display,   0, 255,   0 );
        colorBlue       = new ManagedColor(display,   0,   0, 255 );
        colorGray       = new ManagedColor(display, 100, 100, 100 );
        colorYellow     = new ManagedColor(display, 255, 255,   0 );
        colorMagenta    = new ManagedColor(display, 255,   0, 255);
        colorOrange     = new ManagedColor(display, 255, 165,   0 );

        colorWhite      = new ManagedColor(display, 255, 255, 255 );
        colorDemoGray   = new ManagedColor(display, 248, 248, 248 );
        colorLightGray  = new ManagedColor(display, 225, 225, 225 );
        colorDarkGray   = new ManagedColor(display, 100, 100, 100 );
        colorBlack      = new ManagedColor(display,   0,   0,   0 );

        colorDirectory  = new ManagedColor(display,   0,   0, 255 );
        
        // Load all images from files...
        if (!reload)
        {
            loadFonts();
            loadCommonImages();
            loadStepImages();
            loadJobEntryImages();
        }
    }
    
    private void dispose(boolean reload)
    {
        // Colors 
        colorBackground.dispose();
        colorGraph     .dispose();
        colorTab       .dispose();
        
        colorRed      .dispose();
        colorGreen    .dispose();
        colorBlue     .dispose();
        colorGray     .dispose();
        colorYellow   .dispose();
        colorMagenta  .dispose();
        colorOrange   .dispose();

        colorWhite    .dispose();
        colorDemoGray .dispose();
        colorLightGray.dispose();
        colorDarkGray .dispose();
        colorBlack    .dispose();
        
        colorDirectory.dispose();
        
        if (!reload) // display shutdown, clean up our mess
        {
            // Fonts
            fontGraph  .dispose();
            fontNote   .dispose();
            fontFixed  .dispose();
            fontLarge  .dispose();
            fontBold   .dispose();
            
            // Common images
            imageHop         .dispose();
            imageConnection  .dispose();
            imageBol         .dispose();
            imageKettle      .dispose();
            imageCredits     .dispose();
            imageStart       .dispose();
            imageDummy       .dispose();
            imageSpoon       .dispose();
            imageChef        .dispose();
            imageSplash      .dispose();
            imagePentaho     .dispose();
            imageVariable    .dispose();
            imageSpoonGraph  .dispose();
     
            // big images
            Collection images = imagesSteps.values();
            for (Iterator iter = images.iterator(); iter.hasNext();)
            {
                Image image = (Image) iter.next();
                if (image!=null && !image.isDisposed()) image.dispose();
            }
            
            // Small images
            Collection smallImages = imagesStepsSmall.values();
            for (Iterator iter = smallImages.iterator(); iter.hasNext();)
            {
                Image smallImage = (Image) iter.next();
                if (smallImage!=null && !smallImage.isDisposed()) smallImage.dispose();
            }
        }
    }
    
    /**
     * Load all step images from files. 
     *
     */
    private void loadStepImages()
    {
        imagesSteps      = new Hashtable();
        imagesStepsSmall = new Hashtable();

        ////
        //// STEP IMAGES TO LOAD
        ////
        StepLoader steploader = StepLoader.getInstance();
        StepPlugin steps[] = steploader.getStepsWithType(StepPlugin.TYPE_ALL);
        for (int i = 0; i < steps.length; i++)
        {
            Image image = null;
            Image small_image = null;

            if (steps[i].isNative())
            {
                String filename = steps[i].getIconFilename();
                try
                {
                    image = new Image(display, getClass().getResourceAsStream(filename));
                }
                catch(Exception e)
                {
                    log.logError("Kettle", "Unable to find required step image file ["+(Const.IMAGE_DIRECTORY + filename)+" : "+e.toString());
                    image = new Image(display, Const.ICON_SIZE, Const.ICON_SIZE);
                    GC gc = new GC(image);
                    gc.drawRectangle(0,0,Const.ICON_SIZE, Const.ICON_SIZE);
                    gc.drawLine(0,0,Const.ICON_SIZE, Const.ICON_SIZE);
                    gc.drawLine(Const.ICON_SIZE, 0, 0, Const.ICON_SIZE);
                    gc.dispose();
                }
            } 
            else
            {
                String filename = steps[i].getIconFilename();
                try
                {
                    image = new Image(display, filename);
                }
                catch(Exception e)
                {
                    log.logError("Kettle", "Unable to find required step image file ["+(Const.IMAGE_DIRECTORY + filename)+" : "+e.toString());
                    image = new Image(display, Const.ICON_SIZE, Const.ICON_SIZE);
                    GC gc = new GC(image);
                    gc.drawRectangle(0,0,Const.ICON_SIZE, Const.ICON_SIZE);
                    gc.drawLine(0,0,Const.ICON_SIZE, Const.ICON_SIZE);
                    gc.drawLine(Const.ICON_SIZE, 0, 0, Const.ICON_SIZE);
                    gc.dispose();
                }
            }

            // Calculate the smaller version of the image @ 16x16...
            // Perhaps we should make this configurable?
            //
            if (image != null)
            {
                int xsize = image.getBounds().width;
                int ysize = image.getBounds().height;
                small_image = new Image(display, 16, 16);
                GC gc = new GC(small_image);
                gc.drawImage(image, 0, 0, xsize, ysize, 0, 0, 16, 16);
                gc.dispose();
            }

            imagesSteps.put(steps[i].getID()[0], image);
            imagesStepsSmall.put(steps[i].getID()[0], small_image);
        }
    }
    
    private void loadFonts()
    {
        Props props = Props.getInstance();
        
        fontGraph   = new ManagedFont(display, props.getGraphFont());
        fontNote    = new ManagedFont(display, props.getNoteFont());
        fontFixed   = new ManagedFont(display, props.getFixedFont());

        // Create a large version of the graph font
        FontData largeFontData = props.getGraphFont();
        largeFontData.setHeight(largeFontData.getHeight()*3);
        fontLarge   = new ManagedFont(display, largeFontData);

        // Create a bold version of the default font to display shared objects in the trees 
        FontData boldFontData = props.getDefaultFont();
        boldFontData.setStyle(SWT.BOLD);
        fontBold = new ManagedFont(display, boldFontData);
    }
    
    private void loadCommonImages()
    {
        imageHop         = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "HOP.png"));
        imageConnection  = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "CNC.png"));
        imageBol         = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "BOL.png"));
        imageKettle      = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "kettle_logo.png"));
        imageCredits     = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "credits.png"));
        imageStart       = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "STR.png"));
        imageDummy       = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "DUM.png"));
        imageSpoon       = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "spoon32.png"));
        imageChef        = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "chef.png"));
        imageSplash      = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "kettle_splash.png"));
        imagePentaho     = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "PentahoLogo.png"));
        imageVariable    = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "variable.png"));
        imageSpoonGraph  = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "spoongraph.png"));
    }

    /**
     * Load all step images from files. 
     *
     */
    private void loadJobEntryImages()
    {
        imagesJobentries = new Hashtable();
        imagesJobentriesSmall = new Hashtable();
        
        ////
        //// JOB ENTRY IMAGES TO LOAD
        ////
        JobEntryLoader jobEntryLoader = JobEntryLoader.getInstance();
        if (!jobEntryLoader.isInitialized()) return; // Running in Spoon I guess...
        
        JobPlugin plugins[] = jobEntryLoader.getJobEntriesWithType(JobPlugin.TYPE_ALL);
        for (int i = 0; i < plugins.length; i++)
        {
            try
            {
                if (jobEntryLoader.getJobEntryClass(plugins[i]).getType()==JobEntryInterface.TYPE_JOBENTRY_SPECIAL) continue;
            }
            catch(KettleStepLoaderException e)
            {
                log.logError("Kettle", "Unable to create job entry from plugin ["+plugins[i]+"] : "+e.toString());
                continue;
            }
            
            Image image = null;
            Image small_image = null;

            if (plugins[i].isNative())
            {
                String filename = plugins[i].getIconFilename();
                try
                {
                    image = new Image(display, getClass().getResourceAsStream(filename));
                }
                catch(Exception e)
                {
                    log.logError("Kettle", "Unable to find required job entry image file ["+(Const.IMAGE_DIRECTORY + filename)+"] : "+e.toString());
                    image = new Image(display, Const.ICON_SIZE, Const.ICON_SIZE);
                    GC gc = new GC(image);
                    gc.drawRectangle(0,0,Const.ICON_SIZE, Const.ICON_SIZE);
                    gc.drawLine(0,0,Const.ICON_SIZE, Const.ICON_SIZE);
                    gc.drawLine(Const.ICON_SIZE, 0, 0, Const.ICON_SIZE);
                    gc.dispose();
                }
            } 
            else
            {
                String filename = plugins[i].getIconFilename();
                try
                {
                    image = new Image(display, filename);
                }
                catch(Exception e)
                {
                    log.logError("Kettle", "Unable to find required job entry image file ["+filename+"] : "+e.toString());
                    image = new Image(display, Const.ICON_SIZE, Const.ICON_SIZE);
                    GC gc = new GC(image);
                    gc.drawRectangle(0,0,Const.ICON_SIZE, Const.ICON_SIZE);
                    gc.drawLine(0,0,Const.ICON_SIZE, Const.ICON_SIZE);
                    gc.drawLine(Const.ICON_SIZE, 0, 0, Const.ICON_SIZE);
                    gc.dispose();
                }
            }

            // Calculate the smaller version of the image @ 16x16...
            // Perhaps we should make this configurable?
            //
            if (image != null)
            {
                int xsize = image.getBounds().width;
                int ysize = image.getBounds().height;
                small_image = new Image(display, 16, 16);
                GC gc = new GC(small_image);
                gc.drawImage(image, 0, 0, xsize, ysize, 0, 0, 16, 16);
                gc.dispose();
            }

            imagesJobentries.put(plugins[i].getID(), image);
            imagesJobentriesSmall.put(plugins[i].getID(), small_image);
        }
    }

    /**
     * @return Returns the colorBackground.
     */
    public Color getColorBackground()
    {
        return colorBackground.getColor();
    }

    /**
     * @return Returns the colorBlack.
     */
    public Color getColorBlack()
    {
        return colorBlack.getColor();
    }

    /**
     * @return Returns the colorBlue.
     */
    public Color getColorBlue()
    {
        return colorBlue.getColor();
    }

    /**
     * @return Returns the colorDarkGray.
     */
    public Color getColorDarkGray()
    {
        return colorDarkGray.getColor();
    }

    /**
     * @return Returns the colorDemoGray.
     */
    public Color getColorDemoGray()
    {
        return colorDemoGray.getColor();
    }

    /**
     * @return Returns the colorDirectory.
     */
    public Color getColorDirectory()
    {
        return colorDirectory.getColor();
    }

    /**
     * @return Returns the colorGraph.
     */
    public Color getColorGraph()
    {
        return colorGraph.getColor();
    }

    /**
     * @return Returns the colorGray.
     */
    public Color getColorGray()
    {
        return colorGray.getColor();
    }

    /**
     * @return Returns the colorGreen.
     */
    public Color getColorGreen()
    {
        return colorGreen.getColor();
    }

    /**
     * @return Returns the colorLightGray.
     */
    public Color getColorLightGray()
    {
        return colorLightGray.getColor();
    }

    /**
     * @return Returns the colorMagenta.
     */
    public Color getColorMagenta()
    {
        return colorMagenta.getColor();
    }

    /**
     * @return Returns the colorOrange.
     */
    public Color getColorOrange()
    {
        return colorOrange.getColor();
    }

    /**
     * @return Returns the colorRed.
     */
    public Color getColorRed()
    {
        return colorRed.getColor();
    }

    /**
     * @return Returns the colorTab.
     */
    public Color getColorTab()
    {
        return colorTab.getColor();
    }

    /**
     * @return Returns the colorWhite.
     */
    public Color getColorWhite()
    {
        return colorWhite.getColor();
    }

    /**
     * @return Returns the colorYellow.
     */
    public Color getColorYellow()
    {
        return colorYellow.getColor();
    }

    /**
     * @return Returns the display.
     */
    public Display getDisplay()
    {
        return display;
    }

    /**
     * @return Returns the fontFixed.
     */
    public Font getFontFixed()
    {
        return fontFixed.getFont();
    }

    /**
     * @return Returns the fontGraph.
     */
    public Font getFontGraph()
    {
        return fontGraph.getFont();
    }


    /**
     * @return Returns the fontNote.
     */
    public Font getFontNote()
    {
        return fontNote.getFont();
    }

    /**
     * @return Returns the imageBol.
     */
    public Image getImageBol()
    {
        return imageBol;
    }

    /**
     * @return Returns the imageConnection.
     */
    public Image getImageConnection()
    {
        return imageConnection;
    }

    /**
     * @return Returns the imageCredits.
     */
    public Image getImageCredits()
    {
        return imageCredits;
    }

    /**
     * @return Returns the imageDummy.
     */
    public Image getImageDummy()
    {
        return imageDummy;
    }

    /**
     * @return Returns the imageHop.
     */
    public Image getImageHop()
    {
        return imageHop;
    }

    /**
     * @return Returns the imageKettle.
     */
    public Image getImageKettle()
    {
        return imageKettle;
    }

    /**
     * @return Returns the imageSpoon.
     */
    public Image getImageSpoon()
    {
        return imageSpoon;
    }

    /**
     * @return Returns the image Pentaho.
     */
    public Image getImagePentaho ()
    {
        return imagePentaho;
    }

    /**
     * @return Returns the imagesSteps.
     */
    public Hashtable getImagesSteps()
    {
        return imagesSteps;
    }

    /**
     * @return Returns the imagesStepsSmall.
     */
    public Hashtable getImagesStepsSmall()
    {
        return imagesStepsSmall;
    }

    /**
     * @return Returns the imageStart.
     */
    public Image getImageStart()
    {
        return imageStart;
    }

    /**
     * @return Returns the imageSplash.
     */
    public Image getImageSplash()
    {
        return imageSplash;
    }

    /**
     * @return Returns the imagesJobentries.
     */
    public Hashtable getImagesJobentries()
    {
        return imagesJobentries;
    }

    /**
     * @param imagesJobentries The imagesJobentries to set.
     */
    public void setImagesJobentries(Hashtable imagesJobentries)
    {
        this.imagesJobentries = imagesJobentries;
    }

    /**
     * @return Returns the imagesJobentriesSmall.
     */
    public Hashtable getImagesJobentriesSmall()
    {
        return imagesJobentriesSmall;
    }

    /**
     * @param imagesJobentriesSmall The imagesJobentriesSmall to set.
     */
    public void setImagesJobentriesSmall(Hashtable imagesJobentriesSmall)
    {
        this.imagesJobentriesSmall = imagesJobentriesSmall;
    }

    /**
     * @return Returns the imageChef.
     */
    public Image getImageChef()
    {
        return imageChef;
    }

    /**
     * @param imageChef The imageChef to set.
     */
    public void setImageChef(Image imageChef)
    {
        this.imageChef = imageChef;
    }

    /**
     * @return the fontLarge
     */
    public Font getFontLarge()
    {
        return fontLarge.getFont();
    }
    
    /**
     * @return Returns the clipboard.
     */
    public Clipboard getNewClipboard()
    {
        if (clipboard!=null)
        {
            clipboard.dispose();
            clipboard=null;
        }
        clipboard=new Clipboard(display);
        
        return clipboard;
    }

    public void toClipboard(String cliptext)
    {
        if (cliptext==null) return;

        getNewClipboard();
        TextTransfer tran = TextTransfer.getInstance();
        clipboard.setContents(new String[] { cliptext }, new Transfer[] { tran });
    }
    
    public String fromClipboard()
    {
        getNewClipboard();
        TextTransfer tran = TextTransfer.getInstance();

        return (String)clipboard.getContents(tran);
    }

    public ManagedFont getFontBold()
    {
        return fontBold;
    }

    /**
     * @return the imageVariable
     */
    public Image getImageVariable()
    {
        return imageVariable;
    }

    public Image getImageSpoonGraph()
    {
        return imageSpoonGraph;
    }
}
