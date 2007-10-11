package be.ibridge.kettle.core;

import java.io.InputStream;
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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import be.ibridge.kettle.core.exception.KettleStepLoaderException;
import be.ibridge.kettle.core.util.ImageUtil;
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
    private ManagedColor colorPentaho;
    private ManagedColor colorLightPentaho;

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
    private Image     imageKettleLogo;
    private Image     imageLogoSmall;
    private Image     imageBanner;
    private Image     imageBol;
    private Image     imageArrow;
    private Image     imageCredits;
    private Image     imageStart;
    private Image     imageDummy;
    private Image     imageStartSmall;
    private Image     imageDummySmall;
    private Image     imageSpoon;
    private Image     imageChef;
    private Image     imagePentaho;
    private Image     imageVariable;
    private Image     imageSpoonGraph;
    private Image     imageChefGraph;
    
    private Image     imageEditOptionButton;
    private Image     imageResetOptionButton;

    private ManagedFont fontBold;
    
    private boolean   usingLightMode;

    /**
     * GUIResource also contains the clipboard as it has to be allocated only once!
     * I don't want to put it in a separate singleton just for this one member.
     */
    private static Clipboard clipboard;

    private GUIResource(Display display)
    {
        this.display = display;
        
        usingLightMode = !Const.isWindows() && !Const.isLinux() && !Const.isOSX();
        
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
        
        colorBackground   = new ManagedColor(display, props.getBackgroundRGB() );
        colorGraph        = new ManagedColor(display, props.getGraphColorRGB() );
        colorTab          = new ManagedColor(display, props.getTabColorRGB()   );
        
        colorRed          = new ManagedColor(display, 255,   0,   0 );
        colorGreen        = new ManagedColor(display,   0, 255,   0 );
        colorBlue         = new ManagedColor(display,   0,   0, 255 );
        colorGray         = new ManagedColor(display, 100, 100, 100 );
        colorYellow       = new ManagedColor(display, 255, 255,   0 );
        colorMagenta      = new ManagedColor(display, 255,   0, 255);
        colorOrange       = new ManagedColor(display, 255, 165,   0 );

        colorWhite        = new ManagedColor(display, 255, 255, 255 );
        colorDemoGray     = new ManagedColor(display, 240, 240, 240 );
        colorLightGray    = new ManagedColor(display, 225, 225, 225 );
        colorDarkGray     = new ManagedColor(display, 100, 100, 100 );
        colorBlack        = new ManagedColor(display,   0,   0,   0 );

        colorDirectory    = new ManagedColor(display,   0,   0, 255 );
        // colorPentaho    = new ManagedColor(display, 239, 128,  51 ); // Orange
        colorPentaho      = new ManagedColor(display, 188, 198,  82 );
        colorLightPentaho = new ManagedColor(display, 238, 248, 152 );
        
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
        colorBackground  .dispose();
        colorGraph       .dispose();
        colorTab         .dispose();
        
        colorRed         .dispose();
        colorGreen       .dispose();
        colorBlue        .dispose();
        colorGray        .dispose();
        colorYellow      .dispose();
        colorMagenta     .dispose();
        colorOrange      .dispose();

        colorWhite       .dispose();
        colorDemoGray    .dispose();
        colorLightGray   .dispose();
        colorDarkGray    .dispose();
        colorBlack       .dispose();
        
        colorDirectory   .dispose();
        colorPentaho     .dispose();
        colorLightPentaho.dispose();
        
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
            imageLogoSmall   .dispose();
            if (imageKettleLogo!=null) imageKettleLogo  .dispose();
            if (imageBanner!=null)     imageBanner      .dispose();
            if (imageCredits!=null)    imageCredits     .dispose();
            imageBol         .dispose();
            imageArrow       .dispose();
            imageStart       .dispose();
            imageDummy       .dispose();
            imageStartSmall  .dispose();
            imageDummySmall  .dispose();
            imageSpoon       .dispose();
            imageChef        .dispose();
            imagePentaho     .dispose();
            imageVariable    .dispose();
            imageSpoonGraph  .dispose();
            imageChefGraph   .dispose();
            
            disposeImage(imageEditOptionButton);
            disposeImage(imageResetOptionButton);
     
            // big images
            disposeImages(imagesSteps.values());

            // Small images
            disposeImages(imagesStepsSmall.values());
        }
    }
    
    private void disposeImages(Collection c)
    {
        for (Iterator iter = c.iterator(); iter.hasNext();)
            disposeImage((Image) iter.next());
    }

    private void disposeImage(Image image)
    {
        if (image != null && !image.isDisposed())
            image.dispose();
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
                    log.logError("Kettle", "Unable to find required step image file or image format not supported (e.g. interlaced) [" + filename + " : "+e.toString());
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
    	// General images that we always need...
    	//
        imageHop         = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "HOP.png"));
        imageConnection  = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "CNC.png"));
        imageBol         = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "BOL.png"));
        
        if (!usingLightMode)
        {
            imageKettleLogo  = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "logo_kettle_lrg.png"));
        	imageBanner      = ImageUtil.makeImageTransparent(display, new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "bg_banner.png")), new RGB(255,255,255));
            imageCredits     = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "credits.png"));
        }
        imageStart       = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "STR.png"));
        imageDummy       = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "DUM.png"));
        imageSpoon       = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "spoon32.png"));
        imageChef        = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "chef.png"));
        imagePentaho     = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "PentahoLogo.png"));
        imageVariable    = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "variable.png"));
        imageEditOptionButton  = loadImage(Const.IMAGE_DIRECTORY + "edit_option.png");
        imageResetOptionButton = loadImage(Const.IMAGE_DIRECTORY + "reset_option.png");
        
        imageStartSmall  = new Image(display, 16, 16);
        GC gc = new GC(imageStartSmall);
        gc.drawImage(imageStart, 0, 0, 32, 32, 0, 0, 16, 16);
        gc.dispose();
        imageDummySmall  = new Image(display, 16, 16);
        gc = new GC(imageDummySmall);
        gc.drawImage(imageDummy, 0, 0, 32, 32, 0, 0, 16, 16);
        gc.dispose();
        
        // Makes transparent images "on the fly"
        //
        imageSpoonGraph = ImageUtil.makeImageTransparent(display, new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "spoongraph.png")), new RGB(255,255,255));
        imageChefGraph  = ImageUtil.makeImageTransparent(display, new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "chefgraph.png")), new RGB(255,255,255));
        imageLogoSmall  = ImageUtil.makeImageTransparent(display, new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "kettle_logo_small.png")), new RGB(255,255,255));
        imageArrow      = ImageUtil.makeImageTransparent(display, new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "arrow.png")), new RGB(255,255,255));
    }

    /**
     * Loading an image, checking whether it exists before
     * 
     * @param filename A package-like name to the image as resource. E.g.
     *            <code>/be/ibridge/kettle/images/SCR.png</code>
     * @return The image or <code>null</code>
     */
    private Image loadImage(String filename)
    {
        InputStream is = getClass().getResourceAsStream(filename);
        if (is != null)
        {
            return new Image(display, is);
        }
        else
        {
            log.logBasic(GUIResource.class.toString(), "Did not find image '" + filename + "'");
            return null;
        }
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
     * @return Returns the imageSpoon.
     */
    public Image getImageSpoon()
    {
        return imageSpoon;
    }

    /**
     * @return Returns the image Pentaho.
     */
    public Image getImagePentaho()
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

    public Font getFontBold()
    {
        return fontBold.getFont();
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
    
    public Image getImageChefGraph()
    {
        return imageChefGraph;
    }

    public Image getEditOptionButton()
    {
        return imageEditOptionButton;
    }
    
    public Image getResetOptionButton()
    {
        return imageResetOptionButton;
    }

    /**
     * @return the imageArrow
     */
    public Image getImageArrow()
    {
        return imageArrow;
    }

    /**
     * @param imageArrow the imageArrow to set
     */
    public void setImageArrow(Image imageArrow)
    {
        this.imageArrow = imageArrow;
    }

    /**
     * @return the imageDummySmall
     */
    public Image getImageDummySmall()
    {
        return imageDummySmall;
    }

    /**
     * @param imageDummySmall the imageDummySmall to set
     */
    public void setImageDummySmall(Image imageDummySmall)
    {
        this.imageDummySmall = imageDummySmall;
    }

    /**
     * @return the imageStartSmall
     */
    public Image getImageStartSmall()
    {
        return imageStartSmall;
    }

    /**
     * @param imageStartSmall the imageStartSmall to set
     */
    public void setImageStartSmall(Image imageStartSmall)
    {
        this.imageStartSmall = imageStartSmall;
    }

    /**
     * @return the imageBanner
     */
    public Image getImageBanner()
    {
        return imageBanner;
    }

    /**
     * @param imageBanner the imageBanner to set
     */
    public void setImageBanner(Image imageBanner)
    {
        this.imageBanner = imageBanner;
    }

    /**
     * @return the imageKettleLogo
     */
    public Image getImageKettleLogo()
    {
        return imageKettleLogo;
    }

    /**
     * @param imageKettleLogo the imageKettleLogo to set
     */
    public void setImageKettleLogo(Image imageKettleLogo)
    {
        this.imageKettleLogo = imageKettleLogo;
    }

    /**
     * @return the colorPentaho
     */
    public Color getColorPentaho()
    {
        return colorPentaho.getColor();
    }

    /**
     * @return the imageLogoSmall
     */
    public Image getImageLogoSmall()
    {
        return imageLogoSmall;
    }

    /**
     * @param imageLogoSmall the imageLogoSmall to set
     */
    public void setImageLogoSmall(Image imageLogoSmall)
    {
        this.imageLogoSmall = imageLogoSmall;
    }

    /**
     * @return the colorLightPentaho
     */
    public Color getColorLightPentaho()
    {
        return colorLightPentaho.getColor();
    }

	/**
	 * @return the lightMode
	 */
	public boolean isUsingLightMode() {
		return usingLightMode;
	}
}
