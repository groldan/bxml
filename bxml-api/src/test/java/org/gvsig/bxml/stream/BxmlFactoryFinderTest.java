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
package org.gvsig.bxml.stream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class BxmlFactoryFinderTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // nothing to do
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        // nothing to do
    }

    /**
     * Test method for {@link org.gvsig.bxml.stream.BxmlFactoryFinder#newInputFactory()}.
     */
    @Test
    public void testNewInputFactory() {
        BxmlInputFactory newInputFactory = BxmlFactoryFinder.newInputFactory();
        assertNotNull(newInputFactory);
        newInputFactory = BxmlFactoryFinder.unwrap(newInputFactory);
        assertTrue(newInputFactory instanceof FakeBxmlInputFacotry);

        BxmlInputFactory anotherInputFactory = BxmlFactoryFinder.newInputFactory();
        anotherInputFactory = BxmlFactoryFinder.unwrap(anotherInputFactory);
        assertTrue(anotherInputFactory instanceof FakeBxmlInputFacotry);

        // BxmlInputFactories are stateful, so not to be cached by the factory system
        assertNotSame(newInputFactory, anotherInputFactory);
    }

    /**
     * Test method for {@link org.gvsig.bxml.stream.BxmlFactoryFinder#newOutputFactory()}.
     */
    @Test
    public void testNewOutputFactory() {
        BxmlOutputFactory newOutputFactory = BxmlFactoryFinder.newOutputFactory();
        assertNotNull(newOutputFactory);
        newOutputFactory = BxmlFactoryFinder.unwrap(newOutputFactory);
        assertTrue(newOutputFactory instanceof FakeBxmlOutputFactory);

        BxmlOutputFactory anotherOutputFactory = BxmlFactoryFinder.newOutputFactory();
        anotherOutputFactory = BxmlFactoryFinder.unwrap(anotherOutputFactory);

        assertTrue(anotherOutputFactory instanceof FakeBxmlOutputFactory);

        // BxmlOutputFactories are stateful, so not to be cached by the factory system
        assertNotSame(newOutputFactory, anotherOutputFactory);
    }

}
