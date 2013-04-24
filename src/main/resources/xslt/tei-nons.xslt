<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
    <xsl:strip-space elements="*"/>
    <xsl:output method="text" indent="no"/>
    <xsl:variable name="new-line" select="'&#10;'" />
    <xsl:variable name="display-linebreak" select="'&#10;'" />

    <!--global-exclusions-->
    <xsl:template match="note"/>
    <xsl:template match="milestone"/>
    <xsl:template match="teiHeader"/>
    <xsl:template match="facsimile"/>
    <xsl:template match="front"/>

    <!--breaks-->
    <xsl:template match="pb|opener|body|floatingText|castGroup|broadcast|dateline|actor|castList|head|argument|text|sp|prologue|addrLine|table|finalRubric|row|salute|epilogue|explicit|biblStruct|lg|view|lb|epigraph|entryFree|trailer|set|postscript|div|stage|closer|byline|graph|def|colophon|performance|headLabel|div7|div6|div5|div4|div3|div2|activity|div1|titlePage|signed|camera|back|caption|l|tech|catchwords|headItem|castItem|bibl|address|entry|p|ab|divGen">
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