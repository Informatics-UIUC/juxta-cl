<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:tei="http://www.tei-c.org/ns/1.0" >
    <xsl:strip-space elements="*"/>
    <xsl:output method="text" indent="no"/>
    <xsl:variable name="new-line" select="'&#10;'" />

    <xsl:template match="text()"/>

    <xsl:template match="wd/text()">
        <xsl:variable name="a" select="replace(., '[\n]\s*$', ' ')"/>
        <xsl:variable name="b" select="replace($a, '^[\n]\s*', '')"/>
        <xsl:variable name="c" select="replace($b, '\n+', '')"/>
        <xsl:variable name="d" select="replace($c, '\s+', ' ')"/>
        <xsl:variable name="e" select="concat($d, $new-line)"/>
        <xsl:value-of select="$e"/>
    </xsl:template>
    
</xsl:stylesheet>
