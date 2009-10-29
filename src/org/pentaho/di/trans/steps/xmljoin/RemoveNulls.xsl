<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <xsl:apply-templates mode="filter-empty-elements" select="."/>
</xsl:template>

<xsl:template name="f-e-e" match="*" mode="filter-empty-elements">
  <!-- Filter for nodes with attributes, text or descendants who have attributes or text or attributes -->
  <xsl:if test="(descendant::*/text() or descendant::*/@*) or normalize-space(text()) or @*">
    <!-- Extract the current element's name to create a copy in the output -->
    <xsl:variable name="elName" select="local-name()"/>
    <!-- Create the copy of the element -->
    <xsl:element name="{$elName}">
      <!-- Create a copy of all attributes in this element -->
      <xsl:for-each select="@*">
        <!-- Extract the current attribute's name to create a copy in the output -->
        <xsl:variable name="attName" select="local-name()"/>
        <!-- Create the copy of the attribute -->
        <xsl:attribute name="{$attName}"><xsl:value-of select="string(.)"/></xsl:attribute>
      </xsl:for-each>

      <!-- Add the text element to the current element -->
      <xsl:value-of select="normalize-space(text())"/>
      <!-- Add and filter all child elements of this element -->
      <xsl:for-each select="child::*">
        <xsl:call-template name="f-e-e"/>
      </xsl:for-each>
    </xsl:element>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>