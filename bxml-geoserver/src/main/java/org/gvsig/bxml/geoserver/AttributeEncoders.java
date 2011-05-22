package org.gvsig.bxml.geoserver;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.feature.type.FeatureTypeFactoryImpl;
import org.geotools.gml3.GML;
import org.geotools.util.logging.Logging;
import org.gvsig.bxml.geoserver.Gml3Encoder.AttributeEncoder;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.FeatureTypeFactory;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.geometry.BoundingBox;

public class AttributeEncoders {

    private static final Logger LOGGER = Logging.getLogger("org.gvsig.bxml.geoserver");

    private final AttributeEncoderExecutor BOUNDED_BY_ENCODER;

    private final Map<Name, AttributeEncoderExecutor> executors;

    private final String defaultNamespaceURI;

    private final Gml3Encoder gmlEncoder;

    /**
     * 
     * @param featureType
     *            the type to build the initial set of encoders for
     * @param defaultNamespaceURI
     *            the namespace to use for attribtue names whose {@link AttributeDescriptor
     *            descriptor} name has no namespace
     */
    public AttributeEncoders(final Gml3Encoder gmlEncoder, final FeatureType featureType,
            final String defaultNamespaceURI) {
        this.gmlEncoder = gmlEncoder;
        this.defaultNamespaceURI = defaultNamespaceURI;
        this.executors = buildAttributeEncoderExecutors(featureType);
        this.BOUNDED_BY_ENCODER = new BoundedByEncoderExecutor(gmlEncoder);
    }

    public AttributeEncoderExecutor getBoundedByEncoder(){
        return BOUNDED_BY_ENCODER;
    }
    
    /**
     * @param attribute
     * @return
     */
    public AttributeEncoderExecutor getEncoder(final AttributeDescriptor attribute) {
        final Name name = attribute.getName();
        AttributeEncoderExecutor attEncoder = executors.get(name);
        if (attEncoder == null) {
            attEncoder = new DefaultAttributeEncoderExecutor(gmlEncoder, defaultNamespaceURI,
                    attribute);
            executors.put(name, attEncoder);
        }
        return attEncoder;
    }

    /**
     * Creates an ordered list of attribute encoder executors for a given feature type.
     * <p>
     * If the WFS configuration {@link EncoderConfig#isFeatureBounding() isFeatureBounding} property
     * is {@code true}, also appends to the list an attribute encoder for the feature bounds (ie,
     * gml:boundedBy).
     * </p>
     * 
     * @param featureType
     * @return
     */
    private Map<Name, AttributeEncoderExecutor> buildAttributeEncoderExecutors(
            final FeatureType featureType) {

        Map<Name, AttributeEncoderExecutor> executors;
        executors = new HashMap<Name, AttributeEncoderExecutor>();

        Collection<PropertyDescriptor> descriptors = featureType.getDescriptors();

        Name name;
        AttributeEncoderExecutor executor;

        for (PropertyDescriptor descriptor : descriptors) {
            name = descriptor.getName();
            if (descriptor instanceof AttributeDescriptor) {
                AttributeDescriptor attDescriptor = (AttributeDescriptor) descriptor;
                executor = new DefaultAttributeEncoderExecutor(gmlEncoder, defaultNamespaceURI,
                        attDescriptor);
                executors.put(name, executor);
            } else {
                LOGGER.info("Ignoring property " + name + " as it is not an AttributeDescriptor");
            }
        }

        return executors;
    }

    /**
     * An {@code AttributeEncoderExecutor} knows how to encode into GML 3.1.1 an specific attribute
     * fom a Feature.
     * <p>
     * It is meant to be used inside a feature encoding "chain" at the time of encoding a specific
     * geotools FeatureCollection. This way, the information needed to encode each attribute is
     * gathered once per FeatureCollection, except the attribute value itself.
     * </p>
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     * @see BinaryGml3OutputFormat#encode(FeatureCollection, BxmlStreamWriter)
     */
    public static interface AttributeEncoderExecutor {

        /**
         * Writes the start and end element events down to the encoder, relying in
         * {@link #encodeValue(Feature, BxmlStreamWriter)} to encode the element contents.
         * 
         * @param feature
         *            the feature from which to encode the attribute this executors especializes in.
         * @param encoder
         *            the binary xml encoder used to encode the feature attribute for this executor
         * @throws IOException
         */
        public abstract void encode(final SimpleFeature feature, final BxmlStreamWriter encoder)
                throws IOException;

    }

    /**
     * A private implementation for {@link AttributeEncoderExecutor} based on
     * {@link Gml3Encoder#getAttributeEncoder(Class)}
     * 
     * @author Gabriel Roldan
     * @version $Id$
     */
    private static class DefaultAttributeEncoderExecutor implements AttributeEncoderExecutor {

        protected final AttributeEncoder attributeValueEncoder;

        protected final String encodedMamespaceURI;

        protected final String localName;

        protected final Name attributeName;

        protected final AttributeDescriptor descriptor;

        protected final Gml3Encoder gmlEncoder;

        /**
         * 
         * @param defaultNamespaceURI
         * @param attributeName
         * @param binding
         */
        public DefaultAttributeEncoderExecutor(final Gml3Encoder gmlEncoder,
                final String defaultNamespaceURI, final AttributeDescriptor descriptor) {
            this.gmlEncoder = gmlEncoder;
            this.descriptor = descriptor;
            this.attributeName = descriptor.getName();

            this.encodedMamespaceURI = attributeName.getNamespaceURI() == null ? defaultNamespaceURI
                    : attributeName.getNamespaceURI();
            this.localName = attributeName.getLocalPart();
            AttributeType type = descriptor.getType();
            Class<?> binding = type.getBinding();
            attributeValueEncoder = Gml3Encoder.getAttributeEncoder(binding);
        }

        /**
         * @see AttributeEncoderExecutor#encode
         */
        public final void encode(final SimpleFeature feature, final BxmlStreamWriter encoder)
                throws IOException {
            // namespaceUri may be null
            encoder.writeStartElement(encodedMamespaceURI, localName);

            encodeValue(feature, encoder);

            encoder.writeEndElement();
        }

        /**
         * Encodes the content section of the xml element of the feature attribute for this encoding
         * executor.
         * <p>
         * The actual value encoding is done with an encoding binding for the java type of the
         * AttributeType for this encoder executor, from the
         * {@link Gml3Encoder#getAttributeEncoder(Class)} utility.
         * </p>
         * 
         * @param feature
         *            the feature from which to extract and encode the attribute value
         * @param encoder
         *            the encoder used to write the feature attribute with
         * @throws IOException
         * @see {@link Gml3Encoder#getAttributeEncoder(Class)}
         */
        protected void encodeValue(final SimpleFeature feature, final BxmlStreamWriter encoder)
                throws IOException {
            Object value = feature.getAttribute(attributeName);
            attributeValueEncoder.encode(gmlEncoder, value, descriptor, encoder);
        }
    }

    /**
     * This is an specialized attribute encoder for the {@code gml:boundedBy} feature property,
     * which it not gathered from a normal Feature attribute but from the feature's {@code bounds}
     * property.
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    static class BoundedByEncoderExecutor extends DefaultAttributeEncoderExecutor {
        /**
         * Fake attribute type used to encode the boundedBy feature property only if the
         * {@link #wfs} configuration is explicitly set to do that.
         * 
         * @see #encode(Feature, BxmlStreamWriter)
         */
        private static final AttributeDescriptor boundedByAttribute;

        static {
            FeatureTypeFactory ff = new FeatureTypeFactoryImpl();// CommonFactoryFinder.getFeatureTypeFactory(null);
            AttributeType boundedByAttributeType;
            Name boundedByTypeName = new NameImpl(GML.NAMESPACE, GML.BoundingShapeType
                    .getLocalPart());
            boundedByAttributeType = ff.createAttributeType(boundedByTypeName, BoundingBox.class,
                    false, false, null, null, null);
            Name name = new NameImpl(GML.boundedBy.getNamespaceURI(), GML.boundedBy.getLocalPart());
            boundedByAttribute = ff.createAttributeDescriptor(boundedByAttributeType, name, 0, 1,
                    true, null);
        }

        /**
         * Default constructor, uses the special {@code boundedByAttributeType} AttributeType
         */
        public BoundedByEncoderExecutor(final Gml3Encoder gmlEncoder) {
            super(gmlEncoder, GML.NAMESPACE, boundedByAttribute);
        }

        /**
         * Overrides to get the value object to encode from the {@code feature.getBounds()} property
         * instead of the normal {@code feature.getAttribute(xxx)} method.
         * 
         * @see DefaultAttributeEncoderExecutor#encodeValue
         */
        @Override
        protected void encodeValue(final SimpleFeature feature, final BxmlStreamWriter encoder)
                throws IOException {
            BoundingBox value = feature.getBounds();
            attributeValueEncoder.encode(gmlEncoder, value, boundedByAttribute, encoder);
        }
    }

}
