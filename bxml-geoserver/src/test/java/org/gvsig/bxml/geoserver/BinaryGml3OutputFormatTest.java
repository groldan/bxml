package org.gvsig.bxml.geoserver;

import static org.geoserver.data.test.MockData.PRIMITIVEGEOFEATURE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.geoserver.wfs.WFSTestSupport;
import org.geoserver.wfs.xml.v1_1_0.WFS;
import org.gvsig.bxml.adapt.parsers.DocumentBuilderImpl;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.util.StreamUtil;

public class BinaryGml3OutputFormatTest extends WFSTestSupport {

    public void testFullRequest() throws Exception {
        MockHttpServletResponse resp = getAsServletResponse("wfs?request=GetFeature&typeName=sf:PrimitiveGeoFeature&outputFormat=BinaryGML3&");
        ByteArrayInputStream binaryInputStream = super.getBinaryInputStream(resp);
        InputStream copyStream = StreamUtil.copyStream(binaryInputStream);
        // FileOutputStream fileOutputStream = new FileOutputStream("testFullRequest.bxml");
        // fileOutputStream.write(StreamUtil.getStreamAsByteArray(copyStream));
        // fileOutputStream.flush();
        // fileOutputStream.close();
        // copyStream.reset();

        Document dom = parseBinaryXML(copyStream);
        print(dom);

        // check the mime type
        assertEquals("text/x-bxml; subtype=gml/3.1.1", resp.getContentType());

        assertEquals(WFS.NAMESPACE, dom.getDocumentElement().getNamespaceURI());
        assertEquals(WFS.FEATURECOLLECTION.getLocalPart(), dom.getDocumentElement().getLocalName());
        assertEquals("5", dom.getDocumentElement()
                .getAttributeNS(WFS.NAMESPACE, "numberOfFeatures"));
        assertFalse("".equals(dom.getDocumentElement().getAttributeNS(WFS.NAMESPACE, "timeStamp")));

        NodeList features = dom.getElementsByTagNameNS(PRIMITIVEGEOFEATURE.getNamespaceURI(),
                PRIMITIVEGEOFEATURE.getLocalPart());
        assertEquals(5, features.getLength());
    }

    /**
     * Parses a BinaryXML stream to a {@link Document DOM}.
     * 
     * @param binaryInputStream
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private Document parseBinaryXML(final InputStream binaryInputStream) throws IOException,
            ParserConfigurationException, SAXException {
        DocumentBuilder docBuilder = new DocumentBuilderImpl();
        Document dom = docBuilder.parse(binaryInputStream);
        return dom;
    }

}
