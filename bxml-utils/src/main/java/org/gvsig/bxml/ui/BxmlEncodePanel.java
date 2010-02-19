package org.gvsig.bxml.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.gvsig.bxml.adapt.sax.XmlToBxmlSaxConverter;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.gvsig.bxml.stream.EncodingOptions;
import org.gvsig.bxml.util.ProgressListener;

public class BxmlEncodePanel extends JPanel implements BxmlWorkbench.Worker {

    private static final Logger LOGGER = Logger.getLogger(BxmlEncodePanel.class.getPackage()
            .getName());

    private final BxmlPanel panel;

    /**
     * Opens up the BxmlWorkbench UI in a new JFrame
     */
    public BxmlEncodePanel() {
        panel = new BxmlPanel();
        this.add(panel);
    }

    /**
     * @see org.gvsig.bxml.ui.BxmlWorkbench.Worker#execute()
     */
    public void execute(final ProgressListener progressListener) {
        final EncodingOptions encodingOptions = panel.getEncodingOptions();
        final boolean encodeGmlPosListAsDoubles = panel.encodeGmlPosListAsDoubles();
        InputStream source;
        try {
            source = panel.getSourceFile();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(panel, e.getMessage(), "Error obtaining input source",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        File target = panel.getTargetFile();
        try {
            XmlToBxmlSaxConverter converter;
            converter = new XmlToBxmlSaxConverter(encodingOptions);

            converter.convert(source, target, progressListener, encodeGmlPosListAsDoubles);
            JOptionPane.showMessageDialog(panel, "Done!", "BXML encoding result",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
            String title = "Error encoding xml file";
            int type = JOptionPane.ERROR_MESSAGE;
            JOptionPane.showMessageDialog(panel, message, title, type);
        }
    }

    /**
     * BXML Workbench Swing user interface
     * <p>
     * Provides the following facilities to get the encoding options from the calling code and to
     * control the behavior of the execute operation and progress listening:
     * <ul>
     * <li>{@link #getSourceFile()} the input stream for the XML document to encode
     * <li>{@link #getTargetFile()} the output file for the BXML encoded document
     * <li>{@link #getEncodingOptions()} the {@link BxmlStreamWriter} encoding options
     * </ul>
     * </p>
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    private static class BxmlPanel extends JPanel {
        private static final long serialVersionUID = 7559435310240391376L;

        private EncodingOptions encodingOptions;

        private boolean encodeGmlPosListAsDoubles;

        private JTextField targetFile;

        private JTextField sourceFile;

        public BxmlPanel() {
            encodingOptions = new EncodingOptions();
            createGui();
        }

        public InputStream getSourceFile() throws IOException {
            final String text = sourceFile.getText();
            File file = new File(text);
            if (file.exists()) {
                return new FileInputStream(file);
            }
            URL url = new URL(text);
            InputStream openStream = url.openStream();
            return new BufferedInputStream(openStream);
        }

        public File getTargetFile() {
            File file = new File(targetFile.getText());
            return file;
        }

        public EncodingOptions getEncodingOptions() {
            return encodingOptions.clone();
        }

        public boolean encodeGmlPosListAsDoubles() {
            return encodeGmlPosListAsDoubles;
        }

        private void createGui() {
            setLayout(new BorderLayout());
            JComponent content = createContent();
            this.add(content, BorderLayout.CENTER);
        }

        private JComponent createContent() {
            BorderLayout borderLayout = new BorderLayout();
            JPanel content = new JPanel(borderLayout);
            JComponent sourceComponent = createSourceComponent();
            JComponent targetComponent = createTargetComponent();
            content.add(sourceComponent, BorderLayout.NORTH);
            content.add(targetComponent, BorderLayout.CENTER);
            return content;
        }

        private JComponent createSourceComponent() {
            JPanel panel = new JPanel();
            BorderLayout borderLayout = new BorderLayout();
            borderLayout.setHgap(5);
            panel.setLayout(borderLayout);
            panel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), "Source"));
            panel.add(new JLabel("Source XML file:"), BorderLayout.WEST);
            sourceFile = new JTextField();
            
            panel.add(sourceFile, BorderLayout.CENTER);
            JButton button = new JButton("...");
            panel.add(button, BorderLayout.EAST);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String fileName = selectFile();
                    if (fileName != null) {
                        sourceFile.setText(fileName);
                        String target = targetFile.getText().trim();
                        if (target.length() == 0) {
                            targetFile.setText(fileName + ".bxml");
                        }
                    }
                }
            });
            return panel;
        }

        private String selectFile() {
            JFileChooser chooser = new JFileChooser();
            String dir = java.lang.System.getProperty("user.dir");
            File currDir = new File(dir);
            chooser.setCurrentDirectory(currDir);
            int returnVal = chooser.showOpenDialog(this);
            String fileName = null;
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                fileName = chooser.getSelectedFile().getAbsolutePath();
            }
            return fileName;
        }

        private JComponent createTargetComponent() {
            JPanel panel = new JPanel();
            BorderLayout borderLayout = new BorderLayout();
            borderLayout.setHgap(5);
            panel.setLayout(borderLayout);
            panel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), "Target"));
            {
                JPanel filePanel = new JPanel(new BorderLayout());
                filePanel.add(new JLabel("Target BXML file: "), BorderLayout.WEST);
                targetFile = new JTextField();
                filePanel.add(targetFile, BorderLayout.CENTER);
                JButton button = new JButton("...");
                filePanel.add(button, BorderLayout.EAST);
                panel.add(filePanel, BorderLayout.NORTH);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String fileName = selectFile();
                        if (fileName != null) {
                            targetFile.setText(fileName);
                        }
                    }
                });
            }
            GridLayout gridLayout = new GridLayout(0, 2);
            JPanel optionsPanel = new JPanel(gridLayout);
            panel.add(optionsPanel, BorderLayout.CENTER);

            optionsPanel.add(new JLabel("GML: "));
            optionsPanel.add(createGmlOptionsGui());

            optionsPanel.add(new JLabel("Byte order: "));
            optionsPanel.add(createByteOrderOptionsGui());

            optionsPanel.add(new JLabel("Characters encoding: "));
            optionsPanel.add(createCharsetOptionsGui());

            optionsPanel.add(new JLabel("Compress content: "));
            optionsPanel.add(createCompressionOptionsGui());
            return panel;
        }

        private JComponent createGmlOptionsGui() {
            final JCheckBox checkBox = new JCheckBox("Encode gml:posList as double[]");
            String ttip = "Check this option to encode gml:postList content as "
                    + "double array instead of leaving them as plain Strings";
            checkBox.setToolTipText(ttip);
            checkBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    encodeGmlPosListAsDoubles = checkBox.isSelected();
                }
            });
            return checkBox;
        }

        private JComponent createCompressionOptionsGui() {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setToolTipText("Not yet supported");
            checkBox.setEnabled(false);
            return checkBox;
        }

        private JComponent createCharsetOptionsGui() {
            SortedMap<String, Charset> availableCharsets = new TreeMap<String, Charset>(Charset
                    .availableCharsets());
            List<Charset> highRateCharsets = new ArrayList<Charset>();
            highRateCharsets.add(availableCharsets.remove("UTF-8"));
            highRateCharsets.add(availableCharsets.remove("US-ASCII"));
            highRateCharsets.add(availableCharsets.remove("ISO-8859-1"));
            highRateCharsets.add(availableCharsets.remove("UTF-16"));
            highRateCharsets.add(availableCharsets.remove("UTF-16LE"));
            highRateCharsets.add(availableCharsets.remove("UTF-16BE"));

            final JComboBox combo = new JComboBox();
            for (Charset chset : highRateCharsets) {
                combo.addItem(chset);
            }
            for (Charset chset : availableCharsets.values()) {
                combo.addItem(chset);
            }
            combo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Charset selectedItem = (Charset) combo.getSelectedItem();
                    encodingOptions.setCharactersEncoding(selectedItem);
                    LOGGER.info("charset: " + encodingOptions.getCharactersEncoding());
                }
            });
            return combo;
        }

        private JComponent createByteOrderOptionsGui() {
            ButtonGroup group = new ButtonGroup();
            final ByteOrder defaultByteOrder = encodingOptions.getByteOrder();
            final JRadioButton le = new JRadioButton("Little Endian");
            final JRadioButton be = new JRadioButton("Big Endian");
            JRadioButton defaultOption = ByteOrder.BIG_ENDIAN == defaultByteOrder ? be : le;
            defaultOption.setText(defaultOption.getText() + " (platform value)");
            defaultOption.setSelected(true);
            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ByteOrder byteOrder = le.isSelected() ? ByteOrder.LITTLE_ENDIAN
                            : ByteOrder.BIG_ENDIAN;
                    encodingOptions.setByteOrder(byteOrder);
                    LOGGER.info("byte order: " + encodingOptions.getByteOrder());
                }
            };
            le.addActionListener(actionListener);
            be.addActionListener(actionListener);
            group.add(le);
            group.add(be);

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(le);
            panel.add(be);
            return panel;
        }
    }

}
