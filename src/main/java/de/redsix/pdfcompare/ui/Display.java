package de.redsix.pdfcompare.ui;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.*;

import de.redsix.pdfcompare.CompareResultWithExpectedAndActual;
import de.redsix.pdfcompare.PdfComparator;

public class Display {

    private ViewModel viewModel;

    public void init() {
        viewModel = new ViewModel(new CompareResultWithExpectedAndActual());

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        final BorderLayout borderLayout = new BorderLayout();
        frame.setLayout(borderLayout);
        frame.setMinimumSize(new Dimension(400, 200));
        frame.setTitle("----COMPARADOR DE PDF----");
        final Rectangle screenBounds = getDefaultScreenBounds();
        frame.setSize(Math.min(screenBounds.width, 1700), Math.min(screenBounds.height, 1000));
        frame.setLocation(screenBounds.x, screenBounds.y);
        frame.setBackground(Color.WHITE);
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);

        JToolBar toolBar = new JToolBar();
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        toolBar.setBackground(Color.WHITE);
        frame.add(toolBar, BorderLayout.PAGE_START);

        ImagePanel leftPanel = new ImagePanel(viewModel.getLeftImage());
        ImagePanel resultPanel = new ImagePanel(viewModel.getDiffImage());

        JScrollPane expectedScrollPane = new JScrollPane(leftPanel);
        expectedScrollPane.setMinimumSize(new Dimension(200, 200));
        JScrollPane actualScrollPane = new JScrollPane(resultPanel);
        actualScrollPane.setMinimumSize(new Dimension(200, 200));
        actualScrollPane.getViewport().addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(final ComponentEvent e) {
                resultPanel.setViewSize(e.getComponent().getSize());
                super.componentResized(e);
            }
        });

        expectedScrollPane.getVerticalScrollBar().setModel(actualScrollPane.getVerticalScrollBar().getModel());
        expectedScrollPane.getHorizontalScrollBar().setModel(actualScrollPane.getHorizontalScrollBar().getModel());
        expectedScrollPane.getViewport().addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(final ComponentEvent e) {
                leftPanel.setViewSize(e.getComponent().getSize());
                super.componentResized(e);
            }
        });

        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, expectedScrollPane, actualScrollPane);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.5);
        splitPane.setOneTouchExpandable(true);
        frame.add(splitPane, BorderLayout.CENTER);

        final JToggleButton expectedButton = new JToggleButton("Original");

        addToolBarButton(toolBar, "Buscar PDF", (event) -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showDialog(frame, "Abrir PDF original") == JFileChooser.APPROVE_OPTION) {
                final File expectedFile = fileChooser.getSelectedFile();
                if (fileChooser.showDialog(frame, "Abrir PDF comparable") == JFileChooser.APPROVE_OPTION) {
                    final File actualFile = fileChooser.getSelectedFile();
                    try {
                        System.out.println("---------- "+expectedFile.getAbsolutePath());
                        if (expectedFile.getAbsolutePath().toLowerCase().endsWith(".pdf")&&actualFile.getAbsolutePath().toLowerCase().endsWith(".pdf")) {
                            if (expectedFile.getAbsolutePath().equals(actualFile.getAbsolutePath())) {
                                JOptionPane.showMessageDialog(null, "NO PUEDES ABRIR EL MISMO PDF", "¡¡ERROR!!", JOptionPane.ERROR_MESSAGE);
                            } else {

                                frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                viewModel = new ViewModel(new PdfComparator<>(expectedFile, actualFile, new CompareResultWithExpectedAndActual()).compare());
                                leftPanel.setImage(viewModel.getLeftImage());
                                resultPanel.setImage(viewModel.getDiffImage());
                                frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                expectedButton.setSelected(true);
                            }
                        }else{
                          JOptionPane.showMessageDialog(null, "NO ES UN ARCHIVO PDF","¡¡ERROR!!", JOptionPane.ERROR_MESSAGE);
                        }

                    } catch (IOException ex) {
                        DisplayExceptionDialog(frame, ex);
                    }
                }
            }
            frame.repaint();
        });

        toolBar.addSeparator();

        addToolBarButton(toolBar, "Pagina -", (event) -> {
            if (viewModel.decreasePage()) {
                leftPanel.setImage(viewModel.getLeftImage());
                resultPanel.setImage(viewModel.getDiffImage());
            }
            frame.repaint();
        });

        addToolBarButton(toolBar, "Pagina +", (event) -> {
            if (viewModel.increasePage()) {
                leftPanel.setImage(viewModel.getLeftImage());
                resultPanel.setImage(viewModel.getDiffImage());

            }
            frame.repaint();
        });

        toolBar.addSeparator();

        final JToggleButton pageZoomButton = new JToggleButton("Zoom Pagina");
        pageZoomButton.setSelected(true);
        pageZoomButton.addActionListener((event) -> {
            leftPanel.zoomPage();
            resultPanel.zoomPage();
            frame.repaint();
        });

        addToolBarButton(toolBar, "Zoom -", (event) -> {
            pageZoomButton.setSelected(false);
            leftPanel.decreaseZoom();
            resultPanel.decreaseZoom();
            frame.repaint();
        });

        addToolBarButton(toolBar, "Zoom +", (event) -> {
            pageZoomButton.setSelected(false);
            leftPanel.increaseZoom();
            resultPanel.increaseZoom();
            frame.repaint();
        });

        toolBar.add(pageZoomButton);

        addToolBarButton(toolBar, "Zoom 100%", (event) -> {
            pageZoomButton.setSelected(false);
            leftPanel.zoom100();
            resultPanel.zoom100();
            frame.repaint();
        });

        toolBar.addSeparator();

        addToolBarButton(toolBar, "Centrar Split", (event) -> {
            splitPane.setDividerLocation(0.5);
            splitPane.revalidate();
            frame.repaint();
        });

        toolBar.addSeparator();

        final ButtonGroup buttonGroup = new ButtonGroup();
        expectedButton.setSelected(true);
        expectedButton.addActionListener((event) -> {
            viewModel.showExpected();
            leftPanel.setImage(viewModel.getLeftImage());
            System.err.println("--------------------------");
            frame.repaint();
        });
        toolBar.add(expectedButton);
        buttonGroup.add(expectedButton);

        final JToggleButton actualButton = new JToggleButton("Comparado");
        actualButton.addActionListener((event) -> {
            viewModel.showActual();
            leftPanel.setImage(viewModel.getLeftImage());
            frame.repaint();
        });

        toolBar.add(actualButton);
        buttonGroup.add(actualButton);

        frame.setVisible(true);

    }

    private static void DisplayExceptionDialog(final JFrame frame, final IOException ex) {
        final StringWriter stringWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(stringWriter));
        JTextArea textArea = new JTextArea(
                "Se produjo un error inesperado: " + ex.getMessage() + "\n\n" + stringWriter);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 500));
        JOptionPane.showMessageDialog(frame, scrollPane);
    }

    private static void addToolBarButton(final JToolBar toolBar, final String label, final ActionListener actionListener) {
        final JButton button = new JButton(label);
        button.addActionListener(actionListener);
        toolBar.add(button);
    }

    private static Rectangle getDefaultScreenBounds() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
    }
}
