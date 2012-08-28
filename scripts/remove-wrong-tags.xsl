<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!--Identity transform copies all items by default -->
  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
   </xsl:template>

   <!--Empty template to match on b elements and prevent it from being copied to output -->
   <xsl:template match="orderEntry">
     <xsl:if test="not(library/CLASSES/root[contains(@url, 'ertp-') or contains(@url, 'scala-2.9.1')])">
       <xsl:copy>
         <xsl:apply-templates select="@*|node()"/>
       </xsl:copy>
     </xsl:if>
   </xsl:template>

</xsl:stylesheet>
