package org.gvsig.bxml.geoserver;

import java.io.IOException;
import java.util.logging.Logger;

import org.geotools.gml3.GML;
import org.geotools.util.logging.Logging;
import org.gvsig.bxml.geoserver.AttributeEncoders.AttributeEncoderExecutor;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.opengis.feature.Attribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;

public class SimpleFeatureEncoder {

    private static final Logger LOGGER = Logging.getLogger(SimpleFeatureEncoder.class);

    private final Gml3Encoder gmlEncoder;

    private final EncoderConfig config;

    // lazily created so we have the actual Feature namespace as the default for attribtues that
    // have no ns declared (quite common in geotools SimpleFeatures)
    private AttributeEncoders encoders;

    public SimpleFeatureEncoder(EncoderConfig config, Gml3Encoder gmlEncoder) {
        this.config = config;
        this.gmlEncoder = gmlEncoder;
    }

    public void encode(final SimpleFeature feature, final BxmlStreamWriter encoder) throws IOException {
        // final FeatureType fType = fc.getSchema();
        // final XSDSchema schema = configuration.schema();
        // TODO: traverse gathering properties in the schema defined order...
        // by now using the FeatureType declared order

        final SimpleFeatureType featureType = feature.getFeatureType();
        final boolean featureBounding = config.isFeatureBounding();

        AttributeDescriptor descriptor;
        Name featureName;
        String featureNamespaceURI, featureLocalName;
        {
            descriptor = feature.getDescriptor();
            if (descriptor == null) {
                featureName = feature.getFeatureType().getName();
            } else {
                featureName = descriptor.getName();
            }
            featureNamespaceURI = featureName.getNamespaceURI();
            featureLocalName = featureName.getLocalPart();

            encoder.writeStartElement(featureNamespaceURI, featureLocalName);
        }
        encoder.writeStartAttribute(GML.id);
        encoder.writeValue(feature.getIdentifier().getID());
        encoder.writeEndAttributes();

        if (encoders == null) {
            encoders = new AttributeEncoders(gmlEncoder, featureType, featureNamespaceURI);
        }

        if (featureBounding) {
            final AttributeEncoderExecutor boundedByEncoder = encoders.getBoundedByEncoder();
            boundedByEncoder.encode(feature, encoder);
        }

        AttributeEncoderExecutor attEncoder;
        AttributeDescriptor attDescriptor;
        for (Property prop : feature.getProperties()) {
            if (prop instanceof Attribute) {
                attDescriptor = ((Attribute) prop).getDescriptor();
                attEncoder = encoders.getEncoder(attDescriptor);
                attEncoder.encode(feature, encoder);
            } else {
                LOGGER.info("Ignoring property " + prop.getName()
                        + " as it is not an Attribute instance");
            }
        }

        encoder.writeEndElement();
    }

}
