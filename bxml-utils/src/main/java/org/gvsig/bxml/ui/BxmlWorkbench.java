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
package org.gvsig.bxml.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.gvsig.bxml.stream.EventType;
import org.gvsig.bxml.util.ProgressListener;
import org.gvsig.bxml.util.ProgressListenerAdapter;

/**
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class BxmlWorkbench {

    public static interface Worker {
        /**
         * 
         * @param progressListener
         * @throws IllegalStateException
         *             if the options are not correctly set
         * @throws IOException
         *             if an error occurs while performing the encode/decode process
         */
        public void execute(ProgressListener progressListener) throws IllegalStateException,
                IOException;
    }

    public static enum Status {
        WORKING, READY;
    }

    private JTabbedPane tabbedPane;

    private JButton executeButton;

    private JButton cancelButton;

    private StatusUi statusUi;

    private ProgressListenerAdapter progressListener;

    private void cancel() {
        progressListener.setCancelled(true);
    }

    private void execute() {
        progressListener = new ProgressListenerAdapter() {
            long attributeCount = 0;

            long elementCount = 0;

            final Runnable attributesRunnable = new Runnable() {
                public void run() {
                    statusUi.setEncodedAttributesCount(attributeCount);
                }
            };

            final Runnable elementsRunnable = new Runnable() {
                public void run() {
                    statusUi.setEncodedElementsCount(elementCount);
                }
            };

            @Override
            public void end() {
                SwingUtilities.invokeLater(attributesRunnable);
                SwingUtilities.invokeLater(elementsRunnable);
            }

            @Override
            public void event(final EventType event) {
                if (EventType.ATTRIBUTE == event) {
                    attributeCount++;
                    updateUi(attributeCount, attributesRunnable);
                } else if (EventType.END_ELEMENT == event) {
                    elementCount++;
                    updateUi(elementCount, elementsRunnable);
                }
            }

            private void updateUi(final long count, final Runnable runnable) {
                if (count < 1E3) {
                    SwingUtilities.invokeLater(runnable);
                } else if (count < 1E5 && count % 100 == 0) {
                    SwingUtilities.invokeLater(runnable);
                } else if (count < 1E6 && count % 200 == 0) {
                    SwingUtilities.invokeLater(runnable);
                } else if (count < 1E7 && count % 400 == 0) {
                    SwingUtilities.invokeLater(runnable);
                } else if (count % 500 == 0) {
                    SwingUtilities.invokeLater(runnable);
                }
            }
        };

        Runnable workerRunnable = new Runnable() {
            public void run() {
                final Component selectedComponent = tabbedPane.getSelectedComponent();
                final Worker worker = (Worker) selectedComponent;
                try {
                    cancelButton.setEnabled(true);
                    executeButton.setEnabled(false);

                    statusUi.setStatus(Status.WORKING);
                    long t = System.currentTimeMillis();

                    worker.execute(progressListener);

                    t = System.currentTimeMillis() - t;
                    JOptionPane.showMessageDialog(selectedComponent, "Process finished in " + t
                            + "ms", "Error trying to execute operation",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(selectedComponent, e.getMessage(),
                            "Error trying to execute operation", JOptionPane.ERROR_MESSAGE);
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(selectedComponent, e.getMessage(),
                            "Error executing process", JOptionPane.ERROR_MESSAGE);
                } finally {
                    statusUi.setStatus(Status.READY);
                    cancelButton.setEnabled(false);
                    executeButton.setEnabled(true);
                }
            }
        };

        Thread workThread = new Thread(workerRunnable, "BxmlWorkbench process");
        workThread.start();
    }

    /**
     * Opens up the BxmlWorkbench UI in a new JFrame
     */
    private void run() {
        final JFrame frame = new JFrame("BXML Workbench");
        frame.setName("BXML Workbench");
        // frame.setResizable(false);
        final BxmlEncodePanel encodePanel = new BxmlEncodePanel();
        final BxmlDecodePanel decodePanel = new BxmlDecodePanel();

        tabbedPane = new JTabbedPane();
        tabbedPane.add("Encode BXML", encodePanel);
        tabbedPane.add("Decode BXML", decodePanel);

        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BorderLayout());

        JComponent northPanel = createNorthPanel();
        contentPane.add(northPanel, BorderLayout.NORTH);
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                frame.dispose();
                java.lang.System.exit(0);
            }
        });

        frame.add(createSouthPanel(tabbedPane), BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }

    private Component createSouthPanel(final JTabbedPane tabbedPane) {
        JPanel southPanel = new JPanel(new BorderLayout());

        FlowLayout lyt = new FlowLayout(FlowLayout.RIGHT);
        lyt.setHgap(20);
        final JPanel panel = new JPanel(lyt);

        executeButton = new JButton("Execute");
        executeButton.setDefaultCapable(true);
        panel.add(executeButton);

        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(false);
        panel.add(cancelButton);

        southPanel.add(panel, BorderLayout.CENTER);

        executeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                execute();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });

        this.statusUi = new StatusUi();
        southPanel.add(this.statusUi, BorderLayout.SOUTH);

        return southPanel;
    }

    /**
     * Status bar panel to provide visual feedback over the running process
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    private static class StatusUi extends JPanel {
        private static final long serialVersionUID = 1L;

        private static final NumberFormat NUMBER_FORMATTER = NumberFormat.getIntegerInstance();
        static {
            NUMBER_FORMATTER.setGroupingUsed(true);
        }

        private JProgressBar progressBar;

        private JLabel atttibutesLabel, elementsLabel;

        public StatusUi() {
            this.progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setVisible(false);
            this.setLayout(new BorderLayout());
            this.add(progressBar, BorderLayout.CENTER);
            this.setBorder(BorderFactory.createLoweredBevelBorder());
            JPanel panelLabels = new JPanel(new FlowLayout());
            panelLabels.add(new JLabel("Elements: "));
            panelLabels.add((elementsLabel = new JLabel("0")));
            panelLabels.add(new JLabel(", Attributes: "));
            panelLabels.add((atttibutesLabel = new JLabel("0")));
            this.add(panelLabels, BorderLayout.WEST);
        }

        public void setStatus(final Status status) {
            final boolean working = status == Status.WORKING;
            this.progressBar.setVisible(working);
            this.progressBar.setIndeterminate(working);
        }

        public void setEncodedAttributesCount(long count) {
            atttibutesLabel.setText(NUMBER_FORMATTER.format(count));
            atttibutesLabel.repaint();
        }

        public void setEncodedElementsCount(long count) {
            elementsLabel.setText(NUMBER_FORMATTER.format(count));
            elementsLabel.repaint();
        }
    }

    private static JComponent createNorthPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setHgap(10);
        borderLayout.setVgap(10);
        panel.setLayout(borderLayout);

        JLabel title = new JLabel("BXML Workbench");
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panel.add(title, BorderLayout.NORTH);

        JLabel message = new JLabel();
        message.setBorder(BorderFactory.createEmptyBorder(5, 25, 20, 10));
        message.setText("Use this dialog to handle conversion between BXML and XML");
        panel.add(message, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Launches the application
     * 
     * @param argv
     */
    public static void main(String[] argv) {
        BxmlWorkbench workBench = new BxmlWorkbench();
        workBench.run();
    }

}
