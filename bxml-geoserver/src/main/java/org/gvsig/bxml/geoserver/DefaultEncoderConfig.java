package org.gvsig.bxml.geoserver;

import java.util.Map;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.ows.util.RequestUtils;
import org.geoserver.ows.util.ResponseUtils;
import org.geoserver.wfs.GMLInfo;
import org.geoserver.wfs.WFSInfo;
import org.geoserver.wfs.GMLInfo.SrsNameStyle;
import org.geoserver.wfs.WFSInfo.Version;
import org.geotools.xml.Configuration;

public class DefaultEncoderConfig implements EncoderConfig {

    private final GeoServer gs;

    private final Configuration wfsConfiguration;

    private final Version version;

    public DefaultEncoderConfig(final GeoServer geoserverConfig,
            final Configuration wfsConfiguration, final Version version) {
        this.gs = geoserverConfig;
        this.wfsConfiguration = wfsConfiguration;
        this.version = version;
    }

    public String getCharSet() {
        return gs.getGlobal().getCharset();
    }

    public Configuration getConfiguration() {
        return wfsConfiguration;
    }

    public FeatureTypeInfo getFeatureTypeByName(String namespaceURI, String localPart) {
        Catalog catalog = gs.getCatalog();
        return catalog.getFeatureTypeByName(namespaceURI, localPart);
    }

    public boolean isFeatureBounding() {
        return gs.getService(WFSInfo.class).isFeatureBounding();
    }

    public SrsNameStyle getSrsNameStyle() {
        Map<Version, GMLInfo> gml = gs.getService(WFSInfo.class).getGML();
        GMLInfo info = gml.get(this.version);
        return info.getSrsNameStyle();
    }

}
