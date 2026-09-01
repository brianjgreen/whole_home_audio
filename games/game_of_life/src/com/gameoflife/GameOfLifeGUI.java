package com.gameoflife;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class GameOfLifeGUI {

    private static final Color DARK = new Color(30, 30, 38);
    private static final Color DARK_ALT = new Color(38, 38, 48);
    private static final Color ACCENT = new Color(0, 255, 120);
    private static final Color TEXT = new Color(230, 230, 235);

    private final Grid grid;
    private final GridPanel gridPanel;
    private final Timer timer;
    private final PatternCatalog catalog = new PatternCatalog();
    private JComboBox<String> patternSelector;
    private JToggleButton playPauseButton;
    private JLabel generationLabel;
    private JLabel populationLabel;
    private JSlider speedSlider;

    private boolean running;

    public GameOfLifeGUI() {
        grid = Grid.glider();

        JFrame frame = new JFrame("Conway's Game of Life");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        gridPanel = new GridPanel(grid);
        frame.add(gridPanel, BorderLayout.CENTER);

        frame.add(buildToolbar(), BorderLayout.NORTH);
        frame.add(buildStatusBar(), BorderLayout.SOUTH);

        timer = new Timer(100, e -> step());
        timer.setCoalesce(true);

        bindPatterns();
        bindKeyboard(frame.getRootPane());

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setBackground(DARK);

        playPauseButton = new JToggleButton("▶  Play");
        playPauseButton.setBackground(new Color(70, 200, 110));
        playPauseButton.setOpaque(true);
        playPauseButton.addActionListener(e -> toggleRun());

        JButton stepButton = styledButton("⏭  Step");
        stepButton.addActionListener(e -> step());

        JButton randomButton = styledButton("🎲  Random");
        randomButton.addActionListener(e -> randomize());

        JButton clearButton = styledButton("🗑  Clear");
        clearButton.addActionListener(e -> {
            grid.clear();
            running = false;
            updatePlayPause();
            gridPanel.setGrid(grid);
            updateStatus();
        });

        patternSelector = new JComboBox<>();
        patternSelector.setBackground(DARK_ALT);
        patternSelector.setForeground(TEXT);
        patternSelector.addActionListener(e -> loadPattern());

        speedSlider = new JSlider(1, 50, 10);
        speedSlider.setBackground(DARK);
        speedSlider.setToolTipText("Speed");
        speedSlider.addChangeListener(e -> {
            int value = speedSlider.getValue();
            timer.setDelay(1000 / value);
        });

        JLabel speedLabel = new JLabel("Speed");
        speedLabel.setForeground(TEXT);

        toolbar.add(playPauseButton);
        toolbar.add(stepButton);
        toolbar.add(randomButton);
        toolbar.add(clearButton);
        toolbar.add(new Separator());
        JLabel patternLabel = new JLabel(" Pattern:");
        patternLabel.setForeground(TEXT);
        toolbar.add(patternLabel);
        toolbar.add(patternSelector);
        toolbar.add(new Separator());
        toolbar.add(speedLabel);
        toolbar.add(speedSlider);

        return toolbar;
    }

    private JLabel styledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        return label;
    }

    private JButton styledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(DARK_ALT);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        return button;
    }

    private JPanel buildStatusBar() {
        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 6));
        status.setBackground(DARK_ALT);

        generationLabel = styledLabel("Generation: 0");
        populationLabel = styledLabel("Population: 0");
        generationLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        populationLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));

        JLabel hint = styledLabel("Click / drag cells to edit");
        hint.setForeground(new Color(140, 140, 150));

        status.add(generationLabel);
        status.add(populationLabel);
        status.add(hint);
        return status;
    }

    private void bindPatterns() {
        for (String name : catalog.names()) {
            patternSelector.addItem(name);
        }
        patternSelector.setSelectedItem("Glider");
    }

    private void loadPattern() {
        String name = (String) patternSelector.getSelectedItem();
        if (name == null) return;
        grid.copyFrom(catalog.create(name));
        running = false;
        updatePlayPause();
        gridPanel.setGrid(grid);
        updateStatus();
    }

    private void randomize() {
        grid.randomize(0.30);
        running = false;
        updatePlayPause();
        gridPanel.setGrid(grid);
        updateStatus();
    }

    private void toggleRun() {
        running = !running;
        updatePlayPause();
    }

    private void updatePlayPause() {
        if (running) {
            playPauseButton.setText("⏸  Pause");
            playPauseButton.setBackground(new Color(230, 180, 60));
            timer.start();
        } else {
            playPauseButton.setText("▶  Play");
            playPauseButton.setBackground(new Color(70, 200, 110));
            timer.stop();
        }
    }

    private void step() {
        grid.copyFrom(grid.nextGeneration());
        gridPanel.setGrid(grid);
        updateStatus();
    }

    private void updateStatus() {
        generationLabel.setText("Generation: " + grid.generation());
        populationLabel.setText("Population: " + grid.population());
    }

    private void bindKeyboard(JComponent root) {
        root.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "togglePlay");
        root.getActionMap().put("togglePlay", new javax.swing.AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                toggleRun();
            }
        });
        root.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "stepOne");
        root.getActionMap().put("stepOne", new javax.swing.AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                step();
            }
        });
    }

    private static class Separator extends JLabel {
        Separator() {
            super("│");
            setForeground(new Color(100, 100, 110));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameOfLifeGUI::new);
    }
}
