package org.gvsig.bxml.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.gvsig.bxml.util.BxmlDecoder;
import org.gvsig.bxml.util.ProgressListener;

public class BxmlDecodePanel extends JPanel implements BxmlWorkbench.Worker {
    private static final long serialVersionUID = 7798005029989397321L;

    private JTextField sourceFileField;

    private JTextField targetFileField;

    public BxmlDecodePanel() {
        createUI();
    }

    /**
     * @see org.gvsig.bxml.ui.BxmlWorkbench.Worker#execute()
     */
    public void execute(final ProgressListener progressListener) throws IllegalStateException,
            IOException {
        File sourceFile = getSourceFile();
        if (sourceFile.getPath() == null || sourceFile.getPath().trim().length() == 0) {
            throw new IllegalStateException("Source file must be specified");
        }
        if (!sourceFile.exists()) {
            throw new IllegalStateException("File " + sourceFile.getPath() + " does not exist");
        }
        File targetFile = getTargetFile();
        if (targetFile.getPath() == null || targetFile.getPath().trim().length() == 0) {
            throw new IllegalStateException("Target file must be specified");
        }
        if (targetFile.exists()) {
            final int override = JOptionPane.showConfirmDialog(this, "File " + targetFile.getPath()
                    + " already exists. Override?", "Confirm file override",
                    JOptionPane.OK_CANCEL_OPTION);
            if (JOptionPane.OK_OPTION != override) {
                return;
            }
        }

        BxmlDecoder decoder = new BxmlDecoder();
        ReadableByteChannel source = new FileInputStream(sourceFile).getChannel();
        WritableByteChannel target = new FileOutputStream(targetFile).getChannel();
        try {
            decoder.decode(source, target, progressListener);
        } finally {
            source.close();
            target.close();
        }
    }

    private File getSourceFile() {
        return getFile(this.sourceFileField);
    }

    private File getTargetFile() {
        return getFile(this.targetFileField);
    }

    private File getFile(final JTextField field) {
        return new File(field.getText().trim());
    }

    private void createUI() {
        setLayout(new GridLayout(0, 1));

        add(createSourceFileComponent());
        add(cerateTargetFileComponent());
        add(createOptionsPanel());
        add(createExecutePanel());
    }

    private Component createSourceFileComponent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Source"));
        panel.add(new JLabel("Source Binary XML file:"), BorderLayout.WEST);
        sourceFileField = new JTextField();
        panel.add(sourceFileField, BorderLayout.CENTER);
        JButton button = new JButton("...");
        panel.add(button, BorderLayout.EAST);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final String fileName = selectFile();
                if (fileName != null) {
                    sourceFileField.setText(fileName);
                    String target = targetFileField.getText().trim();
                    if (target.length() == 0) {
                        File source = new File(fileName);
                        File dir = source.getParentFile();
                        String name = source.getName();
                        if (name.endsWith(".xml.bxml")) {
                            name = name.replaceAll("\\.bxml", "");
                        } else if (name.endsWith(".bxml")) {
                            name = name.replaceAll("\\.bxml", "\\.xml");
                        } else {
                            name = name + ".xml";
                        }
                        String targetFileName = new File(dir, name).getPath();
                        targetFileField.setText(targetFileName);
                    }
                }
            }
        });
        return panel;
    }

    private Component cerateTargetFileComponent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Target"));
        panel.add(new JLabel("Target XML file:"), BorderLayout.WEST);
        targetFileField = new JTextField();
        panel.add(targetFileField, BorderLayout.CENTER);

        JButton button = new JButton("...");
        panel.add(button, BorderLayout.EAST);

        panel.add(button, BorderLayout.EAST);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String fileName = selectFile();
                if (fileName != null) {
                    targetFileField.setText(fileName);
                }
            }
        });
        return panel;
    }

    private Component createOptionsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 0));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Options"));
        return panel;
    }

    private Component createExecutePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 0));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Execute"));
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
}
