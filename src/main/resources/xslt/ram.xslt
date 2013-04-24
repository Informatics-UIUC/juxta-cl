<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:tei="http://www.tei-c.org/ns/1.0" >
    <xsl:strip-space elements="*"/>
    <xsl:output method="text" indent="no"/>
    <xsl:variable name="new-line" select="'&#10;'" />
    <xsl:variable name="display-linebreak" select="'&#10;'" />

    <!--global-exclusions-->
    <xsl:template match="back"/>
    <xsl:template match="ramheader"/>
    <xsl:template match="front"/>
    <xsl:template match="add"/>

    <!--breaks-->
    <xsl:template match="title|divheader|lg|p|l|lb|epage|closer">
        <xsl:call-template name="line-break"/>
    </xsl:template>

    
    <xsl:template match="text()">
        <xsl:variable name="a" select="replace(., '[\n]\s*$', ' ')"/>
        <xsl:variable name="b" select="replace($a, '^[\n]\s*', '')"/>
        <xsl:variable name="c" select="replace($b, '\n+', '')"/>
        <xsl:variable name="d" select="replace($c, '\s+', ' ')"/>
        <xsl:value-of select="$d"/>
    </xsl:template>
    
    <xsl:template name="line-break">
        <xsl:apply-templates/>
        <xsl:value-of select="$display-linebreak"/>
    </xsl:template>
</xsl:stylesheet>