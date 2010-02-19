package org.gvsig.bxml.stream.util;

import java.io.PrintStream;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * This interface abstracts out the lookup operation for platform default settings needed for the
 * project.
 * <p>
 * By abstracting out these lookup operations and making use of a {@code System} instance in the
 * parts of the code where platform or default VM settings are needed, we allow to mock up those
 * settings on the unit tests.
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public interface System {

    /**
     * Returns the byte order of the platform where the VM is running.
     * 
     * @return the "endianess" of the hardware platform, as per {@link ByteOrder#nativeOrder()}
     */
    public ByteOrder nativeOrder();

    /**
     * @return {@link Charset#defaultCharset()}
     */
    public Charset defaultCharset();

    /**
     * Returns the standard output stream
     * 
     * @return by default may be {@code java.lang.System.out}
     */
    public PrintStream stdOut();
}
