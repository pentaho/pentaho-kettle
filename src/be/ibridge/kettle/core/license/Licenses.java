package be.ibridge.kettle.core.license;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;

/**
 * Signleton class that contains the licenses.
 * All licenses are loaded on first request. 
 * The different products and dialogs can question this class for the existence of a proper license.
 * 
 * @author Matt
 *
 */
public class Licenses
{
    public static final String PRODUCT_ALL                = "All";

    public static final String PRODUCT_SPOON              = "Spoon";
    public static final String PRODUCT_PAN                = "Pan";
    public static final String PRODUCT_CHEF               = "Chef";
    public static final String PRODUCT_KITCHEN            = "Kitchen";
    public static final String PRODUCT_MENU               = "Menu";
    public static final String PRODUCT_PLATE              = "Plate";

    public static final String PRODUCT_SPOON_ENTERPRISE   = "SpoonEnterprise";
    public static final String PRODUCT_PAN_ENTERPRISE     = "PanEnterprise";
    public static final String PRODUCT_CHEF_ENTERPRISE    = "ChefEnterprise";
    public static final String PRODUCT_KITCHEN_ENTERPRISE = "KitchenEnterprise";
    
    
    
    private static final String STRING_MAC_ADDRESS = "MACAddress";
    private static final String STRING_USERNAME    = "Username";
    private static final String STRING_COMPANY     = "Company";
    private static final String STRING_PRODUCTS    = "Products";
    private static final String STRING_SIGNATURE   = "Signature";



    private static Licenses licenses;
    
    private List license;
    
    /**
     * Default constructor: loads all licenses from .licence file
     *
     */
    private Licenses()
    {
        LogWriter log = LogWriter.getInstance();
        license = new ArrayList();
        
        try
        {
            // What file to load?
            String filename = getLicensesFilename();
            
            Properties properties = new Properties();
            properties.load(new FileInputStream(new File(filename)));
            
            // Get the values from the properties file...
            int nrLicenses = getNrLicenses(properties);
            for (int i=0;i<nrLicenses;i++)
            {
                String userName   = getUsername(properties, i+1);
                String company    = getCompany(properties, i+1);
                String products   = getProducts(properties, i+1);
                String macAddress = getMACAddress(properties, i+1);
                String licenseCode= getSignature(properties, i+1);
                
                License lic = new License(userName, company, products, macAddress, licenseCode);
                log.logBasic("Kettle", "License found: "+lic.toString());
                
                license.add( lic );
            }
        }
        catch(IOException e)
        {
            log.logError("Kettle", "No license file present!");
        }
    }
    
    public void storeLicenses()
    {
        Properties properties = new Properties();
        for (int i=0;i<license.size();i++)
        {
            License lic = (License) license.get(i);
            setUsername(properties, lic.getUserName(), i+1);
            setCompany(properties, lic.getCompany(), i+1);
            setProducts(properties, lic.getProducts(), i+1);
            setMACAddress(properties, lic.getMacAddress(), i+1);
            setSignature(properties, lic.getLicenseCode(), i+1);
        }
        
        // Save the file to disk!
        String filename = getLicensesFilename();
        File file = new File(filename);
        file.renameTo(new File("old"+filename));
        
        try
        {
            properties.store(new FileOutputStream(new File(filename)), "Kettle licences");
        }
        catch (FileNotFoundException e)
        {
            LogWriter.getInstance().logError("Licenses", "License file not found : "+e.toString());
        }
        catch (IOException e)
        {
            LogWriter.getInstance().logError("Licenses", "Unable to save license file : "+e.toString());
        }
    }
    
    public String getLicensesFilename()
    {
        return Const.getKettleDirectory()+Const.FILE_SEPARATOR+".licence";
    }
    
    public static final Licenses getInstance()
    {
        if (licenses!=null) return licenses;
        
        licenses = new Licenses();
        
        return licenses;
    }
    
    private int getNrLicenses(Properties properties)
    {
        int nr = 1;
        
        String lic=properties.getProperty(STRING_SIGNATURE+nr);
        while (lic!=null)
        {
            nr++;
            lic=properties.getProperty(STRING_SIGNATURE+nr);
        }
        
        return nr-1;
    }

    private void setSignature(Properties properties, String sig, int i)
    {
        properties.setProperty(STRING_SIGNATURE+i, sig);
    }
    
    private String getSignature(Properties properties, int i)
    {
        return properties.getProperty(STRING_SIGNATURE+i, "");
    }

    private void setUsername(Properties properties, String usr, int i)
    {
        properties.setProperty(STRING_USERNAME+i, usr);
    }
    
    private String getUsername(Properties properties, int i)
    {
        return properties.getProperty(STRING_USERNAME+i, "");
    }

    private void setCompany(Properties properties, String com, int i)
    {
        properties.setProperty(STRING_COMPANY+i, com);
    }
    
    private String getCompany(Properties properties, int i)
    {
        return properties.getProperty(STRING_COMPANY+i, "");
    }

    private void setProducts(Properties properties, String prd, int i)
    {
        properties.setProperty(STRING_PRODUCTS+i, prd);
    }
    
    private String getProducts(Properties properties, int i)
    {
        return properties.getProperty(STRING_PRODUCTS+i, "");
    }

    private void setMACAddress(Properties properties, String mac, int i)
    {
        properties.setProperty(STRING_MAC_ADDRESS+i, mac);
    }
    
    private String getMACAddress(Properties properties, int i)
    {
        return properties.getProperty(STRING_MAC_ADDRESS+i, "");
    }

    /**
     * Check if a license is available for the specified product.
     * 
     * @param product The product to check against.
     * @return The license number or -1 if no license is available for the specified product
     */
    public int checkLicense(String product)
    {
        // System.out.println("Checking license for product ["+product+"]");
        String mac = Const.getMACAddress();
        
        for (int i=0;i<license.size();i++)
        {
            License lic = (License) license.get(i);
            
            // System.out.println("Checking license #"+i+" --> "+lic);
            if (lic.checkLicense(mac, product)) return i;
        }
        
        // System.out.println("NO LICENSE FOUND for product ["+product+"]");
        return -1;
    }

    /**
     * @return The number of available licenses
     */
    public int getNrLicenses()
    {
        return license.size();
    }
    
    /**
     * @param nr The license nr
     * @return a license from the available licenses 
     */
    public License getLicense(int nr)
    {
        return (License) license.get(nr);
    }
    
    /**
     * Add a license to the list of licenses
     * @param lic The License to add
     */
    public void addLicense(License lic)
    {
        license.add(lic);
    }
    
    public License findLicense(String licenseCode)
    {
        for (int i=0;i<license.size();i++)
        {
            License lic = (License)license.get(i);
            if (lic.checkLicenseCode(licenseCode)) return lic;
        }
        return null;
    }    
 }
