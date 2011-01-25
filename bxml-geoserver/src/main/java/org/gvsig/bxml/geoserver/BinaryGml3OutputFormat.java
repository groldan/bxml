package org.gvsig.bxml.geoserver;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.opengis.wfs.BaseRequestType;
import net.opengis.wfs.FeatureCollectionType;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.QueryType;

import org.eclipse.xsd.XSDSchema;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.ows.util.OwsUtils;
import org.geoserver.ows.util.ResponseUtils;
import org.geoserver.platform.Operation;
import org.geoserver.platform.ServiceException;
import org.geoserver.wfs.WFSException;
import org.geoserver.wfs.WFSGetFeatureOutputFormat;
import org.geoserver.wfs.GMLInfo.SrsNameStyle;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.type.DateUtil;
import org.geotools.gml3.GML;
import org.geotools.xml.Configuration;
import org.geotools.xml.XSD;
import org.gvsig.bxml.geoserver.AttributeEncoders.AttributeEncoderExecutor;
import org.gvsig.bxml.stream.BxmlFactoryFinder;
import org.gvsig.bxml.stream.BxmlOutputFactory;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.gvsig.bxml.stream.EncodingOptions;
import org.opengis.feature.Attribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;

/**
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public final class BinaryGml3OutputFormat extends WFSGetFeatureOutputFormat {

    /**
     * The output format name in WFS 1.0.0 style, where it should be a valid xml element name
     */
    private static final String WFS100_STYLE_OUTPUT_FORMAT = "BinaryGML3";

    /**
     * From BXML 0.0.8 Spec, Section 9.1 MIME & file types: "When used in a MIME type, the BXML
     * format should be identified by substituting the substring "xml" which normally identifies
     * textual XML format with "x-bxml"."
     */
    public static final String MIME_TYPE = "text/x-bxml; subtype=gml/3.1.1";

    public static final Set<String> OUTPUT_FORMATS = Collections
            .unmodifiableSet(new HashSet<String>(Arrays.asList(MIME_TYPE,
                    WFS100_STYLE_OUTPUT_FORMAT)));

    private final EncoderConfig config;

    private final Gml3EncodingUtils gmlEncoder;

    /**
     * Creates a new WFS output format encoder that generates GML 3.1.1 documents encoded as per the
     * OGC BinaryXML 0.0.8 Best Practices Document.
     * 
     * @param wfs
     * @param catalog
     * @param configuration
     */
    public BinaryGml3OutputFormat(final GeoServer gs, final EncoderConfig config) {
        super(gs, OUTPUT_FORMATS);
        this.config = config;
        SrsNameStyle srsNameStyle = config.getSrsNameStyle();
        this.gmlEncoder = new Gml3EncodingUtils(srsNameStyle);
    }

    /**
     * @see org.geoserver.wfs.WFSGetFeatureOutputFormat#getMimeType(java.lang.Object,
     *      org.geoserver.platform.Operation)
     */
    @Override
    public String getMimeType(Object value, Operation operation) {
        return MIME_TYPE;
    }

    /**
     * Returns the xml element name for use in WFS 1.0 style capabilities document in order to
     * advertise this output format.
     * <p>
     * This only makes sense
     * </p>
     * 
     * @return {@code "BinaryGML3"}
     * @see org.geoserver.wfs.WFSGetFeatureOutputFormat#getCapabilitiesElementName()
     */
    @Override
    public String getCapabilitiesElementName() {
        return WFS100_STYLE_OUTPUT_FORMAT;
    }

    /**
     * @param results
     * @param output
     * @param getFeature
     * @throws ServiceException
     * @throws IOException
     * @see org.geoserver.wfs.WFSGetFeatureOutputFormat#write(net.opengis.wfs.FeatureCollectionType,
     *      java.io.OutputStream, org.geoserver.platform.Operation)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void write(final FeatureCollectionType results, final OutputStream output,
            final Operation getFeature) throws ServiceException, IOException {

        final List featureCollections = results.getFeature();
        // round up the info objects for each feature collection

        final Map<String, Set<FeatureTypeInfo>> ns2metas = new HashMap<String, Set<FeatureTypeInfo>>();

        for (int fcIndex = 0; fcIndex < featureCollections.size(); fcIndex++) {
            // get the query for this featureCollection
            final GetFeatureType request;
            request = (GetFeatureType) OwsUtils.parameter(getFeature.getParameters(),
                    GetFeatureType.class);
            final QueryType queryType;
            queryType = (QueryType) request.getQuery().get(fcIndex);

            // may have multiple type names in each query, so add them all
            for (QName name : (List<QName>) queryType.getTypeName()) {
                // get a feature type name from the query
                final String namespaceURI = name.getNamespaceURI();
                final String localPart = name.getLocalPart();
                final FeatureTypeInfo meta = config.getFeatureTypeByName(namespaceURI, localPart);

                if (meta == null) {
                    throw new WFSException("Could not find feature type " + name
                            + " in the GeoServer catalog");
                }

                // add it to the map
                Set<FeatureTypeInfo> metas = ns2metas.get(namespaceURI);

                if (metas == null) {
                    metas = new HashSet<FeatureTypeInfo>();
                    ns2metas.put(namespaceURI, metas);
                }
                metas.add(meta);
            }
        }

        final BxmlStreamWriter encoder = createBxmlStreamWriter(output);

        // declare wfs schema location
        BaseRequestType gft = (BaseRequestType) getFeature.getParameters()[0];

        final String baseUrl = gft.getBaseUrl();

        encoder.writeStartDocument();

        encoder.setSchemaLocation(org.geoserver.wfs.xml.v1_1_0.WFS.NAMESPACE,
                ResponseUtils.buildSchemaURL(baseUrl, "schemas/wfs/1.1.0/wfs.xsd"));

        // declare application schema namespaces
        for (Iterator i = ns2metas.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();

            String namespaceURI = (String) entry.getKey();
            Set metas = (Set) entry.getValue();

            StringBuffer typeNames = new StringBuffer();

            for (Iterator m = metas.iterator(); m.hasNext();) {
                FeatureTypeInfo meta = (FeatureTypeInfo) m.next();
                String prefixedName = meta.getPrefixedName();
                typeNames.append(prefixedName);

                if (m.hasNext()) {
                    typeNames.append(",");
                }
            }

            // set the schema location
            String schemaLocation = ResponseUtils.appendQueryString(
                    ResponseUtils.buildSchemaURL(baseUrl, "wfs"),
                    "service=WFS&version=1.1.0&request=DescribeFeatureType&typeName="
                            + typeNames.toString());
            encoder.setSchemaLocation(namespaceURI, schemaLocation);
        }

        declareNamespaces(encoder);

        try {
            encode(results, encoder);

            encoder.writeEndDocument();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        try {
            encoder.flush();
            encoder.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * Creates a {@link BxmlStreamWriter} to encode the WFS GetFeature response with.
     * <p>
     * The encoder's character set will be the one configured in geoserver as the default output
     * character set.
     * </p>
     * 
     * @param output
     * @return
     * @throws IOException
     */
    private BxmlStreamWriter createBxmlStreamWriter(final OutputStream output) throws IOException {
        final BxmlStreamWriter writer;
        final BxmlOutputFactory bxmlOutputFactory = BxmlFactoryFinder.newOutputFactory();
        final EncodingOptions encodingOptions = new EncodingOptions();
        final String charset = config.getCharSet();
        encodingOptions.setCharactersEncoding(Charset.forName(charset));
        bxmlOutputFactory.setEncodingOptions(encodingOptions);
        writer = bxmlOutputFactory.createSerializer(output);
        return writer;
    }

    @SuppressWarnings("unchecked")
    private void encode(final FeatureCollectionType results, final BxmlStreamWriter encoder)
            throws IOException {

        final QName wfsFc = org.geoserver.wfs.xml.v1_1_0.WFS.FEATURECOLLECTION;

        encoder.writeStartElement(wfsFc); // wfs:FeatureCollection

        final BigInteger numberOfFeatures = results.getNumberOfFeatures();
        if (numberOfFeatures != null) {
            encoder.writeStartAttribute(XMLConstants.NULL_NS_URI, "numberOfFeatures");
            long featureCount = numberOfFeatures.longValue();
            encoder.writeValue(featureCount);
        }
        final Calendar timeStamp = results.getTimeStamp();
        if (timeStamp != null) {
            // write timestamp in ISO format
            String value = DateUtil.serializeDateTime(timeStamp.getTime());
            encoder.writeStartAttribute(XMLConstants.NULL_NS_URI, "timeStamp");
            encoder.writeValue(value);
        }
        /*
         * need to writeEndAttributes even if both numberOfFeatures and timeStamp were null, due to
         * the previous xmlns declarations
         */
        encoder.writeEndAttributes();

        encoder.writeStartElement(GML.featureMembers);

        FeatureCollection fc;
        for (Iterator it = results.getFeature().iterator(); it.hasNext();) {
            fc = (FeatureCollection) it.next();
            encode(fc, encoder);
        }

        encoder.writeEndElement(); // gml:featureMemebers
        encoder.writeEndElement(); // wfs:FeatureCollection

    }

    @SuppressWarnings("unchecked")
    private void declareNamespaces(final BxmlStreamWriter encoder) throws IOException {
        final Configuration wfsConfiguration = config.getConfiguration();
        final XSD xsd = wfsConfiguration.getXSD();
        final XSDSchema schema = xsd.getSchema();
        // write out all the namespace prefix value mappings
        final Map namePrefixToNamespaceMap = schema.getQNamePrefixToNamespaceMap();

        final String defaultNamespaceUri = schema.getTargetNamespace();
        encoder.setDefaultNamespace(defaultNamespaceUri);

        for (Iterator itr = namePrefixToNamespaceMap.entrySet().iterator(); itr.hasNext();) {
            final Map.Entry entry = (Map.Entry) itr.next();
            final String pre = (String) entry.getKey();
            final String ns = (String) entry.getValue();

            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(ns)) {
                continue;
            }

            encoder.writeNamespace(pre, ns);
        }
    }

    /**
     * Encodes a geotools FeatureCollection by first bulding a set of per attribute encoder
     * executors (a sort of execution chain) and then applying the encoding chaing for the feature
     * attributes.
     * 
     * @param fc
     * @param encoder
     * @throws IOException
     */
    void encode(final FeatureCollection<SimpleFeatureType, SimpleFeature> fc,
            final BxmlStreamWriter encoder) throws IOException {
        // final FeatureType fType = fc.getSchema();
        // final XSDSchema schema = configuration.schema();
        // TODO: traverse gathering properties in the schema defined order...
        // by now using the FeatureType declared order

        final SimpleFeatureType featureType = fc.getSchema();
        final boolean featureBounding = config.isFeatureBounding();
        // lazily create so we have the actual Feature namespace as the default for attribtues that
        // have no ns declared (quite common in geotools SimpleFeatures)
        AttributeEncoders encoders = null;

        final FeatureIterator<SimpleFeature> features = fc.features();
        try {
            SimpleFeature feature;
            AttributeDescriptor descriptor;
            Name featureName;
            String featureNamespaceURI, featureLocalName;
            while (features.hasNext()) {
                feature = features.next();
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
                    final AttributeEncoderExecutor boundedByEncoder = encoders
                            .getBoundedByEncoder();
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
        } finally {
            features.close();
        }
    }
}
