<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:tei="http://www.tei-c.org/ns/1.0" >
    <xsl:strip-space elements="*"/>
    <xsl:output method="text" indent="no"/>
    
    <xsl:template match="text()">
        <xsl:variable name="a" select="replace(., '[\n]\s*$', ' ')"/>
        <xsl:variable name="b" select="replace($a, '^[\n]\s*', '')"/>
        <xsl:variable name="c" select="replace($b, '\n+', '')"/>
        <xsl:variable name="d" select="replace($c, '\s+', ' ')"/>
        <xsl:value-of select="$d"/>
    </xsl:template>

</xsl:stylesheet>