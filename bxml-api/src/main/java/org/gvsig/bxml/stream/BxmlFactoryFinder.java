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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Performs a {@link BxmlInputFactory} and {@link BxmlOutputFactory} lookup using the default SPI
 * (Service Provider Interface) mechanism in the Java platform.
 * <p>
 * Registered implementations of these factories are looked up in the classpath under the {@code
 * META-INF/services/org.gvsig.bxml.stream.BxmlInputFactory} and {@code
 * META-INF/services/org.gvsig.bxml.stream.BxmlOutputFactory} files, whose content shall be the
 * fully qualified class name of the factory implementations. More than one implementation may be
 * registered at the same file by stating one implementation class name per line.
 * </p>
 * <p>
 * Factory implementations shall have a default empty constructor.
 * </p>
 * <p>
 * <h2>DBCP</h2>
 * This factory finder enforces implementations to respect the {@link BxmlStreamReader} and
 * {@link BxmlStreamWriter} contracts. There are a couple techniques to implement Design By Contract
 * in the Java language, given it does not provides DBCP as a language characteristic. Some of those
 * techniques use AOP aspects, other rely on direct bytecode instrumentation, and others use a
 * simple wrapper decorator. All of them has their pros and cons. Right now we're forcing contract
 * validation by using a decorator that performs pre and post condition checks. The decorators have
 * been highly tunned to impose a minimum performance penalty, and we're quite happy with the
 * results. In any case, de default behaviour is to force contract checking, but it can be avoided
 * by using setting the system property <b>{@code "org.gvsig.bxml.avoidContractEnforcement"}</b>.
 * For example:
 * 
 * <pre>
 * &lt;code&gt;
 * java -Dorg.gvsig.bxml.avoidContractEnforcement ...
 * &lt;/code&gt;
 * </pre>
 * 
 * will avoid creating a contract enforcement decorator for the available {@link BxmlInputFactory}
 * and {@link BxmlOutputFactory} found by this factory finder.
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 * @since 1.0
 */
public final class BxmlFactoryFinder {

    private static final String SYTEM_PROPERTY = "org.gvsig.bxml.avoidContractEnforcement";

    /**
     * Returns an iterator over the available {@link BxmlInputFactory BXML input factories} found in
     * the classpath using the SPI mechanism.
     * <p>
     * The returned objects shall be newly created instances ready to be used.
     * </p>
     * 
     * @return an iterator of instances of the available input factory implementations
     */
    @SuppressWarnings("unchecked")
    private static Iterator<BxmlInputFactory> availableInputFactories() {
        Iterator<BxmlInputFactory> providers = sun.misc.Service.providers(BxmlInputFactory.class);
        return providers;
    }

    /**
     * Returns an iterator over the available {@link BxmlOutputFactory BXML output factories} found
     * in the classpath using the SPI mechanism.
     * <p>
     * The returned objects shall be newly created instances ready to be used.
     * </p>
     * 
     * @return an iterator of instances of the available output factory implementations
     */
    @SuppressWarnings("unchecked")
    private static Iterator<BxmlOutputFactory> availableOutputFactories() {
        Iterator<BxmlOutputFactory> providers = sun.misc.Service.providers(BxmlOutputFactory.class);
        return providers;
    }

    /**
     * Returns the first {@link BxmlInputFactory} implementation found using the SPI mechanism, or
     * raises a {@link NoSuchElementException} if none is found.
     * 
     * @return a BxmlInputFactory implementation found through the SPI mechanism
     * @throws NoSuchElementException
     *             if no implementation is found on the classpath using the SPI mechanism
     */
    public static BxmlInputFactory newInputFactory() {
        Iterator<?> providers = availableInputFactories();
        if (providers.hasNext()) {
            return wrap((BxmlInputFactory) providers.next());
        }
        /*
         * REVISIT: the SPI mechanism does not work in gvSIG!! (classloader hell?)
         */
        try {
            BxmlInputFactory defaultImpl = (BxmlInputFactory) Class.forName(
                    "org.gvsig.bxml.stream.impl.DefaultBxmlInputFactory").newInstance();
            return wrap(defaultImpl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new NoSuchElementException("Found no BxmlInputFactory");
    }

    /**
     * Returns the a {@link BxmlInputFactory} implementation looked up using the SPI mechanism, and
     * giving preference to the implementation denoted by the {@code prefferredImplClassName}, if
     * any.
     * <p>
     * If no {@code BxmlInputFactory} implemementation named {@code prefferredImplClassName} is
     * found in the classpath, then this method falls back to return the first implementation found,
     * just like the method {@link #newInputFactory()}.
     * </p>
     * 
     * @param prefferredImplClassName
     *            the qualified class name for the prefferred BxmlInputFactory implementation to be
     *            returned
     * @return a BxmlInputFactory implementation found using the SPI mechanism, preferrably the one
     *         denoted by the {@code prefferredImplClassName} class name.
     * @throws NoSuchElementException
     *             if no implementation is found on the classpath using the SPI mechanism, whether
     *             is is or not the implementation named {@code prefferredImplClassName}
     */
    public static BxmlInputFactory newInputFactory(final String prefferredImplClassName) {
        Class<?> prefferredImplClass = null;
        try {
            prefferredImplClass = Class.forName(prefferredImplClassName);
        } catch (ClassNotFoundException e) {
            // fall back to default lookup
            return newInputFactory();
        }

        Iterator<BxmlInputFactory> providers = availableInputFactories();
        while (providers.hasNext()) {
            BxmlInputFactory next = providers.next();
            if (prefferredImplClass.isInstance(next)) {
                return wrap(next);
            }
        }
        // fall back to default lookup
        return newInputFactory();
    }

    /**
     * Returns the first {@link BxmlOutputFactory} implementation found using the SPI mechanism, or
     * raises a {@link NoSuchElementException} if none is found.
     * 
     * @return a BxmlOutputFactory implementation found using the SPI mechanism
     */
    public static BxmlOutputFactory newOutputFactory() {
        Iterator<BxmlOutputFactory> providers = availableOutputFactories();
        if (providers.hasNext()) {
            return wrap(providers.next());
        }
        /*
         * REVISIT: the SPI mechanism does not work in gvSIG!! (classloader hell?)
         */
        try {
            BxmlOutputFactory defaultImpl = (BxmlOutputFactory) Class.forName(
                    "org.gvsig.bxml.stream.impl.DefaultBxmlOutputFactory").newInstance();
            return wrap(defaultImpl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new NoSuchElementException("Found no BxmlOutputFactory");
    }

    /**
     * Returns the a {@link BxmlOutputFactory} implementation looked up using the SPI mechanism, and
     * giving preference to the implementation denoted by the {@code prefferredImplClassName}, if
     * any.
     * <p>
     * If no {@code BxmlOutputFactory} implemementation named {@code prefferredImplClassName} is
     * found in the classpath, then this method falls back to return the first implementation found,
     * just like the method {@link #newOutputFactory()}.
     * </p>
     * 
     * @param prefferredImplClassName
     *            the qualified class name for the prefferred BxmlOutputFactory implementation to be
     *            returned.
     * @return a BxmlOutputFactory implementation found using the SPI mechanism, preferrably the one
     *         denoted by the {@code prefferredImplClassName} class name.
     * @throws NoSuchElementException
     *             if no implementation is found on the classpath using the SPI mechanism, whether
     *             is is or not the implementation named {@code prefferredImplClassName}.
     */
    public static BxmlOutputFactory newOutputFactory(final String prefferredImplClassName) {
        Class<?> prefferredImplClass = null;
        try {
            prefferredImplClass = Class.forName(prefferredImplClassName);
        } catch (ClassNotFoundException e) {
            // fall back to default lookup
            return newOutputFactory();
        }

        Iterator<BxmlOutputFactory> providers = availableOutputFactories();
        while (providers.hasNext()) {
            BxmlOutputFactory next = providers.next();
            if (prefferredImplClass.isInstance(next)) {
                return wrap(next);
            }
        }
        // fall back to default lookup
        return newOutputFactory();
    }

    private static BxmlOutputFactory wrap(final BxmlOutputFactory implementation) {
        if (null != System.getProperty(SYTEM_PROPERTY)) {
            return implementation;
        }
        return new ContractCheckingOutputFactoryFactoryDecorator(implementation);
    }

    private static BxmlInputFactory wrap(final BxmlInputFactory implementation) {
        if (null != System.getProperty(SYTEM_PROPERTY)) {
            return implementation;
        }
        return new ContractCheckingInputFactoryDecorator(implementation);
    }

    private static class ContractCheckingOutputFactoryFactoryDecorator implements BxmlOutputFactory {

        private final BxmlOutputFactory factory;

        public ContractCheckingOutputFactoryFactoryDecorator(BxmlOutputFactory impl) {
            this.factory = impl;
        }

        private BxmlStreamWriter wrap(final BxmlStreamWriter impl) {
            return new BxmlStreamWriter_Contract(impl);
        }

        public BxmlStreamWriter createSerializer(File outputFile) throws IOException {
            return wrap(factory.createSerializer(outputFile));
        }

        public BxmlStreamWriter createSerializer(OutputStream output) throws IOException {
            return wrap(factory.createSerializer(output));
        }

        public BxmlStreamWriter createSerializer(WritableByteChannel output) throws IOException {
            return wrap(factory.createSerializer(output));
        }

        public EncodingOptions getEncodingOptions() {
            return factory.getEncodingOptions();
        }

        public void setEncodingOptions(EncodingOptions encodingOptions) {
            factory.setEncodingOptions(encodingOptions);
        }

    }

    private static class ContractCheckingInputFactoryDecorator implements BxmlInputFactory {

        private BxmlInputFactory in;

        private ContractCheckingInputFactoryDecorator(BxmlInputFactory in) {
            this.in = in;
        }

        private BxmlStreamReader wrap(BxmlStreamReader impl) {
            return new BxmlStreamReader_Contract(impl);
        }

        public BxmlStreamReader createScanner(InputStream input) throws IOException {
            return wrap(in.createScanner(input));
        }

        public BxmlStreamReader createScanner(File bxmlFile) throws IOException {
            return wrap(in.createScanner(bxmlFile));
        }

        public BxmlStreamReader createScanner(URL bxmlResource) throws IOException {
            return wrap(in.createScanner(bxmlResource));
        }

        public boolean isNamespaceAware() {
            return in.isNamespaceAware();
        }

        public void setNamespaceAware(boolean namespaceAware) {
            in.setNamespaceAware(namespaceAware);
        }

        public BxmlStreamReader createScanner(ReadableByteChannel bxmlSource) throws IOException {
            return wrap(in.createScanner(bxmlSource));
        }
    }

    /**
     * Warning, this method is to easy unit testing this class only!
     * 
     * @param inputFactory
     * @return
     */
    static BxmlInputFactory unwrap(BxmlInputFactory inputFactory) {
        if (!(inputFactory instanceof ContractCheckingInputFactoryDecorator)) {
            throw new IllegalArgumentException();
        }
        return ((ContractCheckingInputFactoryDecorator) inputFactory).in;
    }

    /**
     * Warning, this method is to easy unit testing this class only!
     * 
     * @param outputFactory
     * @return
     */
    static BxmlOutputFactory unwrap(BxmlOutputFactory outputFactory) {
        if (!(outputFactory instanceof ContractCheckingOutputFactoryFactoryDecorator)) {
            throw new IllegalArgumentException();
        }
        return ((ContractCheckingOutputFactoryFactoryDecorator) outputFactory).factory;
    }
}
