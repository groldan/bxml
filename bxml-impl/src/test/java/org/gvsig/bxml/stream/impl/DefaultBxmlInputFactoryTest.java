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
package org.gvsig.bxml.stream.impl;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.io.BxmlInputStream;
import org.gvsig.bxml.stream.io.Header;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test suite for {@link DefaultBxmlInputFactory}
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class DefaultBxmlInputFactoryTest {

    DefaultBxmlInputFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new DefaultBxmlInputFactory();
    }

    @After
    public void tearDown() throws Exception {
        factory = null;
    }

    @Test
    public void testCreateScanner() throws IllegalArgumentException, IOException {
        try {
            factory.createScanner((BxmlInputStream) null);
            fail("Expected NPE");
        } catch (NullPointerException e) {
            assertTrue(true);
        }
        BxmlInputStream mockStream = createNiceMock(BxmlInputStream.class);
        expect(mockStream.getHeader()).andReturn(Header.DEFAULT);
        replay(mockStream);
        BxmlStreamReader scanner = factory.createScanner(mockStream);
        verify(mockStream);
        assertNotNull(scanner);
    }

    @Test
    public void testSetNamespaceAware() throws IllegalArgumentException, IOException {
        BxmlInputStream mockStream = createNiceMock(BxmlInputStream.class);
        expect(mockStream.getHeader()).andReturn(Header.DEFAULT);
        replay(mockStream);

        BxmlStreamReader reader = factory.createScanner(mockStream);
        assertFalse(reader.isNamespaceAware());

        mockStream = createNiceMock(BxmlInputStream.class);
        expect(mockStream.getHeader()).andReturn(Header.DEFAULT);
        replay(mockStream);

        factory.setNamespaceAware(true);
        reader = factory.createScanner(mockStream);
        assertTrue(reader.isNamespaceAware());
    }
}
