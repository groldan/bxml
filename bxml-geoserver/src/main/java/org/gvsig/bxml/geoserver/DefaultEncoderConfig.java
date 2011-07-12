package org.gvsig.bxml.geoserver;

import java.util.Map;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.wfs.GMLInfo;
import org.geoserver.wfs.GMLInfo.SrsNameStyle;
import org.geoserver.wfs.WFSInfo;
import org.geoserver.wfs.WFSInfo.Version;
import org.geotools.xml.Configuration;

public class DefaultEncoderConfig implements EncoderConfig {

    private final GeoServer gs;

    private final Configuration wfsConfiguration;

    private final Version version;

    private boolean featureBounding;

    private SrsNameStyle srsNameStyle;

    public DefaultEncoderConfig(final GeoServer geoserverConfig,
            final Configuration wfsConfiguration, final Version version) {
        this.gs = geoserverConfig;
        this.wfsConfiguration = wfsConfiguration;
        this.version = version;
        this.featureBounding = gs.getService(WFSInfo.class).isFeatureBounding();
        Map<Version, GMLInfo> gml = gs.getService(WFSInfo.class).getGML();
        GMLInfo info = gml.get(this.version);
        this.srsNameStyle = info.getSrsNameStyle();
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
        return featureBounding;
    }

    public SrsNameStyle getSrsNameStyle() {
        return srsNameStyle;
    }

}
