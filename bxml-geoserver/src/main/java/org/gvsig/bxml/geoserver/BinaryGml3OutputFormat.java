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
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDSchema;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.ows.util.ResponseUtils;
import org.geoserver.platform.Operation;
import org.geoserver.platform.ServiceException;
import org.geoserver.wfs.WFSException;
import org.geoserver.wfs.WFSGetFeatureOutputFormat;
import org.geoserver.wfs.request.FeatureCollectionResponse;
import org.geoserver.wfs.request.GetFeatureRequest;
import org.geoserver.wfs.request.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.type.DateUtil;
import org.geotools.gml3.GML;
import org.geotools.xml.Configuration;
import org.geotools.xml.XSD;
import org.gvsig.bxml.stream.BxmlFactoryFinder;
import org.gvsig.bxml.stream.BxmlOutputFactory;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.gvsig.bxml.stream.EncodingOptions;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

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

    private final Gml3Encoder gmlEncoder;

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
        this.gmlEncoder = new Gml3Encoder(config);
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
     */
    @Override
    public void write(final FeatureCollectionResponse results, final OutputStream output,
            final Operation getFeature) throws ServiceException, IOException {

        @SuppressWarnings("rawtypes")
        final List<FeatureCollection> featureCollections = results.getFeature();
        // round up the info objects for each feature collection

        final Map<String, Set<FeatureTypeInfo>> ns2metas = new HashMap<String, Set<FeatureTypeInfo>>();

        final GetFeatureRequest request;
        final String baseUrl;

        {
            Object[] parameters = getFeature.getParameters();
            request = GetFeatureRequest.adapt(parameters[0]);
            baseUrl = request.getBaseURL();
        }

        for (int fcIndex = 0; fcIndex < featureCollections.size(); fcIndex++) {
            // get the query for this featureCollection
            final Query query = request.getQueries().get(fcIndex);

            // may have multiple type names in each query, so add them all
            for (QName name : query.getTypeNames()) {
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

        encoder.writeStartDocument();

        encoder.setSchemaLocation(org.geoserver.wfs.xml.v1_1_0.WFS.NAMESPACE,
                ResponseUtils.buildSchemaURL(baseUrl, "schemas/wfs/1.1.0/wfs.xsd"));

        // declare application schema namespaces
        for (Entry<String, Set<FeatureTypeInfo>> entry : ns2metas.entrySet()) {

            String namespaceURI = (String) entry.getKey();
            Set<FeatureTypeInfo> metas = entry.getValue();

            StringBuffer typeNames = new StringBuffer();

            for (Iterator<FeatureTypeInfo> m = metas.iterator(); m.hasNext();) {
                FeatureTypeInfo meta = (FeatureTypeInfo) m.next();
                String prefixedName = meta.prefixedName();
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
    private void encode(final FeatureCollectionResponse results, final BxmlStreamWriter encoder)
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

        for (@SuppressWarnings("rawtypes")
        FeatureCollection fc : results.getFeatures()) {
            encode(fc, encoder);
        }

        encoder.writeEndElement(); // gml:featureMemebers
        encoder.writeEndElement(); // wfs:FeatureCollection

    }

    private void declareNamespaces(final BxmlStreamWriter encoder) throws IOException {
        final Configuration wfsConfiguration = config.getConfiguration();
        final XSD xsd = wfsConfiguration.getXSD();
        final XSDSchema schema = xsd.getSchema();
        // write out all the namespace prefix value mappings
        final Map<String, String> namePrefixToNamespaceMap = schema.getQNamePrefixToNamespaceMap();

        final String defaultNamespaceUri = schema.getTargetNamespace();
        encoder.writeDefaultNamespace(defaultNamespaceUri);

        for (Entry<String, String> entry : namePrefixToNamespaceMap.entrySet()) {
            final String pre = entry.getKey();
            final String ns = entry.getValue();

            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(ns)) {
                continue;
            }

            encoder.writeNamespace(pre, ns);
        }
    }

    /**
     * Encodes a GeoTools FeatureCollection by first building a set of per attribute encoder
     * executors (a sort of execution chain) and then applying the encoding chain for the feature
     * attributes.
     * 
     * @param fc
     * @param writer
     * @throws IOException
     */
    void encode(final FeatureCollection<SimpleFeatureType, SimpleFeature> fc,
            final BxmlStreamWriter writer) throws IOException {

        final SimpleFeatureEncoder sfEncoder = new SimpleFeatureEncoder(gmlEncoder);
        final FeatureIterator<SimpleFeature> features = fc.features();
        try {
            SimpleFeature feature;
            while (features.hasNext()) {
                feature = features.next();
                sfEncoder.encode(feature, writer);
            }
        } finally {
            features.close();
        }
    }
}
