package org.gvsig.bxml.geoserver;

import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.wfs.GMLInfo.SrsNameStyle;
import org.geotools.xml.Configuration;

/**
 * Abstracts out the service configuration options needed by the {@link BinaryGml3OutputFormat
 * encoder}
 * 
 * @author Gabriel Roldan
 * 
 */
public interface EncoderConfig {

    /**
     * @return the xml configuration for the WFS
     */
    public Configuration getConfiguration();

    FeatureTypeInfo getFeatureTypeByName(String namespaceURI, String localPart);

    /*
     * WFSInfo.getCharset
     */
    String getCharSet();

    /*
     * wfsInfo.isF...
     */
    boolean isFeatureBounding();

    SrsNameStyle getSrsNameStyle();

}
