/* gvSIG. Sistem a de Información Geográfica de la Generalitat Valenciana
 *
 * Copyright (C) 2007 Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ibáñez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 9638 62 495
 *      gvsig@gva.es
 *      www.gvsig.gva.es
 */
package org.gvsig.bxml.adapt.sax;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.gvsig.bxml.adapt.sax.gml.Gml3SaxToBxmlContentHandler;
import org.gvsig.bxml.stream.BxmlOutputFactory;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.gvsig.bxml.stream.EncodingOptions;
import org.gvsig.bxml.stream.EventType;
import org.gvsig.bxml.stream.impl.DefaultBxmlOutputFactory;
import org.gvsig.bxml.ui.BxmlWorkbench;
import org.gvsig.bxml.util.ProgressListener;
import org.gvsig.bxml.util.ProgressListenerAdapter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * Utility class to encode an XML document in BXML using a SAX-to-BXML adapter content handler.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class XmlToBxmlSaxConverter {

    private static final BxmlOutputFactory bxmlFactory = new DefaultBxmlOutputFactory();

    private EncodingOptions encodingOptions;

    /**
     * 
     */
    public XmlToBxmlSaxConverter() {
        this(new EncodingOptions());
    }

    public XmlToBxmlSaxConverter(EncodingOptions encodingOptions) {
        this.encodingOptions = encodingOptions;
    }

    public void convert(final File xmlFile, final File bxmlOutputFile, final ProgressListener pl)
            throws Exception {
        InputStream in = new FileInputStream(xmlFile);
        convert(in, bxmlOutputFile, pl, false);
    }

    public void convert(final InputStream xmlFile, final File bxmlOutputFile,
            final ProgressListener pl, boolean encodeGmlPosList) throws Exception {
        final BxmlStreamWriter bxmlSerializer;
        bxmlFactory.setEncodingOptions(encodingOptions);
        bxmlSerializer = bxmlFactory.createSerializer(bxmlOutputFile);

        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(true);
        saxParserFactory.setValidating(false);
        SAXParser saxParser = saxParserFactory.newSAXParser();
        // make the parser not to complain about unreachable dtd's...
        saxParser.getXMLReader().setEntityResolver(new EmptyResolver());
        saxParser.getXMLReader().setDTDHandler(new DefaultHandler2());
        try {
            XmlToBxmlContentHandler encoderHandler;
            if (encodeGmlPosList) {
                encoderHandler = new Gml3SaxToBxmlContentHandler(bxmlSerializer);
                System.out.println("encoding GML postList as double[]");
            } else {
                encoderHandler = new XmlToBxmlContentHandler(bxmlSerializer);
            }
            ProgressXmlToBxmlHandler bxmlEncodingHandler;
            bxmlEncodingHandler = new ProgressXmlToBxmlHandler(bxmlSerializer);
            if (pl != null) {
                bxmlEncodingHandler.setProgressListener(pl);
            }
            saxParser.parse(xmlFile, encoderHandler);
        } finally {
            bxmlSerializer.close();
        }
    }

    /**
     * Resolves everything to an empty xml document, useful for skipping errors due to missing dtds
     * and the like. Stolen from GeoServerver's {@code
     * org.geoserver.test.GeoServerAbstractTestSupport}
     * 
     * @author Andrea Aime - TOPP
     */
    static class EmptyResolver implements org.xml.sax.EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId)
                throws org.xml.sax.SAXException, IOException {
            StringReader reader = new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            InputSource source = new InputSource(reader);
            source.setPublicId(publicId);
            source.setSystemId(systemId);

            return source;
        }
    }

    public static void main(String[] argv) {
        if (argv.length < 2) {
            java.lang.System.err.println("Usage: java " + XmlToBxmlSaxConverter.class.getName()
                    + " <input xml file> <output bxml file> [charset-name]");
            return;
        }
        File in = new File(argv[0]);
        File out = new File(argv[1]);

        String charsetName = "ISO-8859-1";
        if (argv.length > 2) {
            charsetName = argv[2];
        }
        Charset charset;
        try {
            charset = Charset.forName(charsetName);
        } catch (UnsupportedCharsetException e) {
            java.lang.System.err.println("Unsupported charset: " + charsetName);
            return;
        }
        EncodingOptions encodingOptions = new EncodingOptions();
        encodingOptions.setCharactersEncoding(charset);

        XmlToBxmlSaxConverter converter = new XmlToBxmlSaxConverter(encodingOptions);
        try {
            long start = java.lang.System.currentTimeMillis();
            converter.convert(in, out, null);
            long end = java.lang.System.currentTimeMillis();
            java.lang.System.out.println("BXML transform took " + (end - start) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Subclass of {@link XmlToBxmlContentHandler} that provides a callback mechanism to report
     * progress for the {@link BxmlWorkbench} ui.
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    private static class ProgressXmlToBxmlHandler extends XmlToBxmlContentHandler {

        public ProgressXmlToBxmlHandler(final BxmlStreamWriter bxmlSerializer) {
            super(bxmlSerializer);
        }

        private ProgressListener progressListener = new ProgressListenerAdapter();

        /**
         * @param pl
         *            non null progress listener
         */
        public void setProgressListener(ProgressListener pl) {
            if (pl == null) {
                throw new NullPointerException();
            }
            this.progressListener = pl;
        }

        /**
         * Defaults to parent behavior and calls {@code progressListener.elements} with the number
         * of elements encoded so far.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            progressListener.event(EventType.END_ELEMENT);
        }

        /**
         * Defaults to parent behavior and calls {@code progressListener.attributes} with the number
         * of attributes encoded so far.
         */
        @Override
        public void startAttribute(String nsUri, String attName, String value) throws IOException {
            super.startAttribute(nsUri, attName, value);
            progressListener.event(EventType.ATTRIBUTE);
        }
    }
}
