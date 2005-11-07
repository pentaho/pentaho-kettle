package be.ibridge.kettle.core.license;

import be.ibridge.kettle.core.Encr;

/** 
 * This class contains the properties of a license
 * 
 * @author Matt
 */
public class License
{
    private String userName;
    private String company;
    private String products;
    private String macAddress;
    private String licenseCode;
    
    public License(String userName, String company, String products, String macAddress, String licenseCode)
    {
        this.userName    = userName;
        this.company     = company;
        this.products    = products;
        this.macAddress  = macAddress;
        this.licenseCode = licenseCode;
    }
    
    /**
     * @return Returns the company.
     */
    public String getCompany()
    {
        return company;
    }

    /**
     * @param company The company to set.
     */
    public void setCompany(String company)
    {
        this.company = company;
    }

    /**
     * @return Returns the licenseCode.
     */
    public String getLicenseCode()
    {
        return licenseCode;
    }

    /**
     * @param licenseCode The licenseCode to set.
     */
    public void setLicenseCode(String licenseCode)
    {
        this.licenseCode = licenseCode;
    }

    /**
     * @return Returns the macAddress.
     */
    public String getMacAddress()
    {
        return macAddress;
    }

    /**
     * @param macAddress The macAddress to set.
     */
    public void setMacAddress(String macAddress)
    {
        this.macAddress = macAddress;
    }

    /**
     * @return Returns the products.
     */
    public String getProducts()
    {
        return products;
    }

    /**
     * @param products The products to set.
     */
    public void setProducts(String products)
    {
        this.products = products;
    }

    /**
     * @return Returns the userName.
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * @param userName The userName to set.
     */
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    /**
     * Check the license using a MAC address and a product
     * @param mac The network card MAC address
     * @param product The product to check against.
     * @return True if this license if valid for the product and MAC address
     */
    public boolean checkLicense(String mac, String product)
    {
        String correctsignature = getSignature();
        
        // System.out.println("correctsignature="+correctsignature+", signature="+signature);
        
        if (licenseCode!=null && 
            licenseCode.length()>0 &&
            licenseCode.equalsIgnoreCase(correctsignature) &&
            ( products.toUpperCase().indexOf(product.toUpperCase())>=0 || products.equalsIgnoreCase(Licenses.PRODUCT_ALL) )
            ) return true; 
        
        return false;

    }
    
    private String getSignature()
    {
        Encr enc = new Encr();
        if (enc.init()) // Build keypairs etc.
        {
            // Build it again...
            return enc.buildSignature(macAddress, userName, company, products);
        }
        else
        {
            return "";
        }
    }

    /**
     * Check the 
     * @param code
     * @return true if the supplied license code is valid
     */
    public boolean checkLicenseCode(String code)
    {
        if (code==null || code.length()==0) return false;
        
        String oldCode = licenseCode; // Save licenseCode in case code is wrong  
        
        licenseCode = getSignature();
        String shortSignature = Encr.getSignatureShort(licenseCode); 

        if (code.equalsIgnoreCase(shortSignature))
        {
            // Correct code: leave the licenseCode!
            return true;
        }
        else
        {
            licenseCode = oldCode;
            return false;
        }
    }
    
    public String toString()
    {
        return "User ["+userName+"] Company ["+company+"] Products ["+products+"] MAC ["+macAddress+"] code ["+Encr.getSignatureShort(licenseCode)+"]";
    }

    
}
