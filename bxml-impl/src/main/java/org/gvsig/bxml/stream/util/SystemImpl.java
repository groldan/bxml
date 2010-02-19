package org.gvsig.bxml.stream.util;

import java.io.PrintStream;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class SystemImpl implements System {

    /**
     * @see System#defaultCharset()
     */
    public Charset defaultCharset() {
        return Charset.defaultCharset();
    }

    /**
     * @see System#nativeOrder()
     */
    public ByteOrder nativeOrder() {
        return ByteOrder.nativeOrder();
    }

    /**
     * @see System#stdOut()
     */
    public PrintStream stdOut() {
        return java.lang.System.err;
    }

}
