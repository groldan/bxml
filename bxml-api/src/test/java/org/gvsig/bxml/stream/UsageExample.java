package org.gvsig.bxml.stream;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * API usage example class.
 * <p>
 * This class is only meant to contain API usage examples that compiles, not meant to be ran at all.
 * The idea being to maintain the usage examples up to date with API changes for documentation
 * purposes.
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class UsageExample {

    /**
     * Exemplifies the archetypical usage of a {@link BxmlStreamReader}
     */
    public void sampleInputFactoryUsage() {
        // Obtain an input factory
        BxmlInputFactory inputFactory;
        try {
            inputFactory = BxmlFactoryFinder.newInputFactory();
        } catch (NoSuchElementException e) {
            throw new RuntimeException("No BxmlInputFactory implementation found");
        }

        // obtain a bxml stream reader for a bxml file
        BxmlStreamReader reader;
        try {
            reader = inputFactory.createScanner(new java.io.File("path/to/file"));
        } catch (IOException e) {
            throw new RuntimeException("Error creating BxmlStreamReader", e);
        }

        // dumb scan over the whole document
        try {
            while (reader.next() != EventType.END_DOCUMENT) {
                ;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Exemplifies the archetypical usage of a {@link BxmlStreamWriter}
     */
    public void sampleOutputFactoryUsage() {
        BxmlOutputFactory outputFactory;
        try {
            outputFactory = BxmlFactoryFinder.newOutputFactory();
        } catch (NoSuchElementException e) {
            throw new RuntimeException("No BxmlOutputFactory implementation found");
        }

        BxmlStreamWriter writer;
        try {
            writer = outputFactory.createSerializer(new java.io.File("path/to/file"));
        } catch (IOException e) {
            throw new RuntimeException("Error creating BxmlStreamWriter", e);
        }
        try {
            writer.writeStartDocument();
            // .... write elements
            writer.writeEndDocument();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
