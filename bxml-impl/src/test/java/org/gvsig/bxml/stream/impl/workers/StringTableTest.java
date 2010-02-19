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
package org.gvsig.bxml.stream.impl.workers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class StringTableTest {

    private StringTable st;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        st = new StringTable();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        st = null;
    }

    /**
     * Test method for
     * {@link org.gvsig.bxml.stream.impl.workers.StringTable#add(java.lang.CharSequence)}.
     */
    @Test
    public void testAdd() {
        assertEquals(0, st.add("abc", 10));
        assertEquals(1, st.add("123", 20));
        assertEquals(0, st.add("abc", 30));
    }

    /**
     * Test method for {@link org.gvsig.bxml.stream.impl.workers.StringTable#get(long)}.
     */
    @Test
    public void testGetByIndex() {
        assertEquals(0, st.add("abc", 10));
        assertEquals(1, st.add("1234", 20));
        assertEquals(2, st.add("xyz", 30));

        assertEquals("1234", st.get(1));
        assertEquals("abc", st.get(0));
        try {
            assertNull(st.get(3));
            fail("expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }
    }

    /**
     * Test method for
     * {@link org.gvsig.bxml.stream.impl.workers.StringTable#get(java.lang.CharSequence)}.
     */
    @Test
    public void testGetCharSequence() {
        assertEquals(0, st.add("abc", 10));
        assertEquals(1, st.add(new StringBuilder("1234"), 20));
        assertEquals(2, st.add(new StringBuffer("xyz"), 30));

        assertEquals(2, st.get("xyz"));
        assertEquals(1, st.get(new StringBuffer("1234")));
        assertEquals(0, st.get(new StringBuilder("abc")));
        assertEquals(-1, st.get("no way"));
    }

}
