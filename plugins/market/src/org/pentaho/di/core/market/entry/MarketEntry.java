/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.market.entry;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.market.SupportLevel;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.w3c.dom.Node;

/**
 * This class defines a single PDI market entry. It defines the type of plugin,
 * the version, where to download the archive package and so on.
 * 
 * @author matt
 */
public class MarketEntry implements XMLInterface {
  public static String XML_TAG = "market_entry"; 
  
  private String id;
  private MarketEntryType type;
  private String name;
  private String version;
  private String author;
  private String description;
  private String documentationUrl;
  private String sourceUrl;
  private String forumUrl;
  private String casesUrl;
  private String packageUrl;
  private String licenseName;
  private String licenseText;
  private SupportLevel supportLevel;
  private String supportMessage;
  private String supportOrganization;
  private String supportUrl;
  private String minPdiVersion;
  private String maxPdiVersion;
  
  private transient boolean installed;
  private transient String installedBranch;
  private transient String installedVersion;
  private transient String installedBuildId;  
  
  private transient String marketPlaceName;

  public MarketEntry() {
  }
  
  /**
   * @param id
   * @param type;
   * @param name
   * @param version
   * @param author
   * @param description
   * @param documentationUrl
   * @param sourceUrl
   * @param forumUrl
   * @param casesUrl
   * @param packageUrl
   * @param licenseName
   * @param licenseText
   * @param supportLevel
   * @param supportMessage
   * @param supportOrganization
   * @param supportUrl
   */
  public MarketEntry(String id, MarketEntryType type, String name, String version, String author, String description, String documentationUrl, String sourceUrl, String forumUrl, String casesUrl, String packageUrl, String licenseName, String licenseText, SupportLevel supportLevel, String supportMessage,
      String supportOrganization, String supportUrl, String minPdiVersion, String maxPdiVersion) throws KettleException {
    this();
    this.id = id;
    this.type = type;
    this.name = name;
    this.version = version;
    this.author = author;
    this.description = description;
    this.documentationUrl = documentationUrl;
    this.sourceUrl = sourceUrl;
    this.forumUrl = forumUrl;
    this.casesUrl = casesUrl;
    this.packageUrl = packageUrl;
    this.licenseName = licenseName;
    this.licenseText = licenseText;
    this.supportLevel = supportLevel;
    this.supportMessage = supportMessage;
    this.supportOrganization = supportOrganization;
    this.supportUrl = supportUrl;
    this.minPdiVersion = minPdiVersion;
    this.maxPdiVersion = maxPdiVersion;
    
    if (type==null) {
      throw new KettleException("The market entry type can't be null");
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MarketEntry)) return false;
    if (obj == this) return true;
    
    return ((MarketEntry)obj).getId().equals(id);
  }
  
  @Override
  public int hashCode() {
    return id.hashCode();
  }
  @Override
  public String getXML() throws KettleException {
    StringBuilder xml = new StringBuilder();
    
    xml.append(XMLHandler.openTag(XML_TAG));
    xml.append(XMLHandler.addTagValue("id", id, false));
    xml.append(XMLHandler.addTagValue("type", type.toString(), false));
    xml.append(XMLHandler.addTagValue("name", name, false));
    xml.append(XMLHandler.addTagValue("description", description, false));
    
    // for now, support a single version
    
    xml.append(XMLHandler.openTag("versions"));
    xml.append(XMLHandler.openTag("version"));
    xml.append(XMLHandler.addTagValue("version", version, false));
    xml.append(XMLHandler.addTagValue("min_parent_version", minPdiVersion, false));
    xml.append(XMLHandler.addTagValue("max_parent_version", maxPdiVersion, false));
    xml.append(XMLHandler.addTagValue("package_url", packageUrl, false));
    xml.append(XMLHandler.addTagValue("source_url", sourceUrl, false));
    xml.append(XMLHandler.closeTag("version"));
    xml.append(XMLHandler.closeTag("versions"));
    
    xml.append(XMLHandler.addTagValue("author", author, false));
    xml.append(XMLHandler.addTagValue("documentation_url", documentationUrl, false));
    xml.append(XMLHandler.addTagValue("forum_url", forumUrl, false));
    xml.append(XMLHandler.addTagValue("cases_url", casesUrl, false));
    xml.append(XMLHandler.addTagValue("license_name", licenseName, false));
    xml.append(XMLHandler.addTagValue("license_text", licenseText, false));
    xml.append(XMLHandler.addTagValue("support_level", supportLevel.toString(), false));
    xml.append(XMLHandler.addTagValue("support_message", supportMessage, false));
    xml.append(XMLHandler.addTagValue("support_organization", supportOrganization, false));
    xml.append(XMLHandler.addTagValue("support_url", supportUrl, false));
    xml.append(XMLHandler.closeTag(XML_TAG));
    return xml.toString();
  }
  
  public MarketEntry(Node node) {
    this();
    id = XMLHandler.getTagValue(node, "id");
    type = MarketEntryType.getMarketEntryType(XMLHandler.getTagValue(node, "type"));
    name = XMLHandler.getTagValue(node, "name");
    description = XMLHandler.getTagValue(node, "description");
    author = XMLHandler.getTagValue(node, "author");

    // for now read the first version from the metadata
    
    List<Node> versionsNodes = XMLHandler.getNodes(node, "versions");
    for (Node versionsNode : versionsNodes) {
      List<Node> versionNodes = XMLHandler.getNodes(versionsNode, "version");
      for (Node versionNode : versionNodes) {
        version = XMLHandler.getTagValue(versionNode, "version");
        packageUrl = XMLHandler.getTagValue(versionNode, "package_url");
        sourceUrl = XMLHandler.getTagValue(versionNode, "source_url");
        minPdiVersion = XMLHandler.getTagValue(versionNode, "min_parent_version");
        maxPdiVersion = XMLHandler.getTagValue(versionNode, "max_parent_version");
        break;
      }
      break;
    }
    
    documentationUrl = XMLHandler.getTagValue(node, "documentation_url");
    forumUrl = XMLHandler.getTagValue(node, "forum_url");
    casesUrl = XMLHandler.getTagValue(node, "cases_url");
    licenseName = XMLHandler.getTagValue(node, "license_name");
    licenseText = XMLHandler.getTagValue(node, "license_text");
    supportLevel = SupportLevel.getSupportLevel(XMLHandler.getTagValue(node, "support_level"));
    supportMessage = XMLHandler.getTagValue(node, "support_message");
    supportOrganization = XMLHandler.getTagValue(node, "support_organization");
    supportUrl = XMLHandler.getTagValue(node, "support_url");
  }
  
  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * @param version the version to set
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * @return the author
   */
  public String getAuthor() {
    return author;
  }

  /**
   * @param author the author to set
   */
  public void setAuthor(String author) {
    this.author = author;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the documentationUrl
   */
  public String getDocumentationUrl() {
    return documentationUrl;
  }

  /**
   * @param documentationUrl the documentationUrl to set
   */
  public void setDocumentationUrl(String documentationUrl) {
    this.documentationUrl = documentationUrl;
  }

  /**
   * @return the packageUrl
   */
  public String getPackageUrl() {
    return packageUrl;
  }

  /**
   * @param packageUrl the packageUrl to set
   */
  public void setPackageUrl(String packageUrl) {
    this.packageUrl = packageUrl;
  }

  /**
   * @return the licenseName
   */
  public String getLicenseName() {
    return licenseName;
  }

  /**
   * @param licenseName the licenseName to set
   */
  public void setLicenseName(String licenseName) {
    this.licenseName = licenseName;
  }

  /**
   * @return the licenseText
   */
  public String getLicenseText() {
    return licenseText;
  }

  /**
   * @param licenseText the licenseText to set
   */
  public void setLicenseText(String licenseText) {
    this.licenseText = licenseText;
  }

  /**
   * @return the supportLevel
   */
  public SupportLevel getSupportLevel() {
    return supportLevel;
  }

  /**
   * @param supportLevel the supportLevel to set
   */
  public void setSupportLevel(SupportLevel supportLevel) {
    this.supportLevel = supportLevel;
  }

  /**
   * @return the supportMessage
   */
  public String getSupportMessage() {
    return supportMessage;
  }

  /**
   * @param supportMessage the supportMessage to set
   */
  public void setSupportMessage(String supportMessage) {
    this.supportMessage = supportMessage;
  }

  /**
   * @return the supportOrganization
   */
  public String getSupportOrganization() {
    return supportOrganization;
  }

  /**
   * @param supportOrganization the supportOrganization to set
   */
  public void setSupportOrganization(String supportOrganization) {
    this.supportOrganization = supportOrganization;
  }

  /**
   * @return the supportUrl
   */
  public String getSupportUrl() {
    return supportUrl;
  }

  /**
   * @param supportUrl the supportUrl to set
   */
  public void setSupportUrl(String supportUrl) {
    this.supportUrl = supportUrl;
  }

  /**
   * @return the type
   */
  public MarketEntryType getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(MarketEntryType type) {
    this.type = type;
  }

  /**
   * @return the sourceUrl
   */
  public String getSourceUrl() {
    return sourceUrl;
  }

  /**
   * @param sourceUrl the sourceUrl to set
   */
  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  /**
   * @return the forumUrl
   */
  public String getForumUrl() {
    return forumUrl;
  }

  /**
   * @param forumUrl the forumUrl to set
   */
  public void setForumUrl(String forumUrl) {
    this.forumUrl = forumUrl;
  }

  /**
   * @return the casesUrl
   */
  public String getCasesUrl() {
    return casesUrl;
  }

  /**
   * @param casesUrl the casesUrl to set
   */
  public void setCasesUrl(String casesUrl) {
    this.casesUrl = casesUrl;
  }

  /**
   * @return the marketPlaceName
   */
  public String getMarketPlaceName() {
    return marketPlaceName;
  }

  /**
   * @param marketPlaceName the marketPlaceName to set
   */
  public void setMarketPlaceName(String marketPlaceName) {
    this.marketPlaceName = marketPlaceName;
  }

  /**
   * @return the minPdiVersion
   */
  public String getMinPdiVersion() {
    return minPdiVersion;
  }

  /**
   * @param minPdiVersion the minPdiVersion to set
   */
  public void setMinPdiVersion(String minPdiVersion) {
    this.minPdiVersion = minPdiVersion;
  }

  /**
   * @return the maxPdiVersion
   */
  public String getMaxPdiVersion() {
    return maxPdiVersion;
  }

  /**
   * @param maxPdiVersion the maxPdiVersion to set
   */
  public void setMaxPdiVersion(String maxPdiVersion) {
    this.maxPdiVersion = maxPdiVersion;
  }
  
  public boolean isInstalled() {
    return installed;
  }

  public void setInstalled(boolean installed) {
      this.installed = installed;
  }
  
  public String getInstalledBranch() {
      return installedBranch;
  }
  
  public void setInstalledBranch(String installedBranch) {
      this.installedBranch = installedBranch;
  }


  /**
   * @return the installedBuildId
   */
  public String getInstalledBuildId() {
      return installedBuildId;
  }

  /**
   * @param installedBuildId the installedBuildId to set
   */
  public void setInstalledBuildId(String installedBuildId) {
      this.installedBuildId = installedBuildId;
  }

  public String getInstalledVersion() {
    return installedVersion;
  }
  
  public void setInstalledVersion(String installedVersion) {
      this.installedVersion = installedVersion;
  }
  
}
