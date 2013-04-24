<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:tei="http://www.tei-c.org/ns/1.0">
    <xsl:output method="text" indent="no" />
    <xsl:variable name="new-line" select="'&#10;'" />
    <xsl:variable name="display-linebreak" select="'&#10;'" />

    <!-- exclusions -->
    <xsl:template match="tei:teiHeader" />
    <xsl:template match="tei:front" />
    <xsl:template match="tei:note" />

    <!-- linebreaks -->
    <xsl:template
        match="tei:pb|tei:opener|tei:body|tei:floatingText|tei:castGroup|tei:broadcast|tei:dateline|tei:actor|tei:castList|tei:head|tei:argument|tei:text|tei:sp|tei:prologue|tei:addrLine|tei:table|tei:finalRubric|tei:row|tei:salute|tei:epilogue|tei:explicit|tei:biblStruct|tei:lg|tei:view|tei:lb|tei:epigraph|tei:entryFree|tei:trailer|tei:set|tei:postscript|tei:div|tei:stage|tei:closer|tei:byline|tei:graph|tei:def|tei:colophon|tei:performance|tei:headLabel|tei:div7|tei:div6|tei:div5|tei:div4|tei:div3|tei:div2|tei:activity|tei:div1|tei:titlePage|tei:signed|tei:camera|tei:back|tei:caption|tei:l|tei:tech|tei:catchwords|tei:headItem|tei:castItem|tei:bibl|tei:address|tei:entry|tei:p|tei:ab|tei:divGen">
        <xsl:call-template name="line-break" />
    </xsl:template>


    <xsl:template match="text()">
        <xsl:variable name="a" select="replace(., '[\n]\s*$', ' ')" />
        <xsl:variable name="b" select="replace($a, '^[\n]\s*', ' ')" />
        <xsl:variable name="c" select="replace($b, '\n+', '')" />
        <xsl:variable name="d" select="replace($c, '\s+', ' ')" />
        <xsl:value-of select="$d" />
    </xsl:template>

    <xsl:template name="line-break">
        <xsl:apply-templates />
        <xsl:value-of select="$display-linebreak" />
    </xsl:template>
</xsl:stylesheet>