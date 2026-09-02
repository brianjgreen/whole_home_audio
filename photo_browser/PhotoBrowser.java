import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PhotoBrowser extends JFrame {

    private static final int THUMB_SIZE = 150;
    private static final int THUMB_GAP = 8;
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "bmp", "tiff", "tif", "webp", "heic", "heif"
    );

    private final List<Path> allImages = new ArrayList<>();
    private final Set<Path> selectedImages = new LinkedHashSet<>();
    private final Map<Path, ImageIcon> thumbnailCache = new ConcurrentHashMap<>();
    private final ExecutorService thumbnailLoader = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
    );

    private JPanel gridPanel;
    private JScrollPane scrollPane;
    private JLabel statusLabel;
    private JLabel titleLabel;
    private Path currentDirectory;

    public PhotoBrowser() {
        super("Photo Browser");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setPreferredSize(new Dimension(1200, 800));
        initComponents();
        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));

        // Toolbar
        contentPanel.add(createToolbar(), BorderLayout.NORTH);

        // Grid
        gridPanel = new JPanel(new GridLayout(0, 4, THUMB_GAP, THUMB_GAP));
        gridPanel.setBackground(new Color(30, 30, 30));
        gridPanel.setBorder(new EmptyBorder(THUMB_GAP, THUMB_GAP, THUMB_GAP, THUMB_GAP));

        scrollPane = new JScrollPane(gridPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Status bar
        contentPanel.add(createStatusBar(), BorderLayout.SOUTH);

        setContentPane(contentPanel);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        toolbar.setBackground(new Color(45, 45, 45));
        toolbar.setBorder(new EmptyBorder(4, 8, 4, 8));

        JButton openBtn = makeButton("Open Folder", "Select a folder of images");
        openBtn.addActionListener(e -> openDirectory());
        toolbar.add(openBtn);

        toolbar.add(Box.createHorizontalStrut(12));

        JButton copyBtn = makeButton("Copy Selected", "Copy selected images to another folder");
        copyBtn.addActionListener(e -> copySelected());
        toolbar.add(copyBtn);

        JButton zipBtn = makeButton("Zip Selected", "Create a zip archive of selected images");
        zipBtn.addActionListener(e -> zipSelected());
        toolbar.add(zipBtn);

        toolbar.add(Box.createHorizontalStrut(12));

        JButton selectAllBtn = makeButton("Select All", null);
        selectAllBtn.addActionListener(e -> {
            selectedImages.addAll(allImages);
            refreshSelection();
        });
        toolbar.add(selectAllBtn);

        JButton deselectAllBtn = makeButton("Deselect All", null);
        deselectAllBtn.addActionListener(e -> {
            selectedImages.clear();
            refreshSelection();
        });
        toolbar.add(deselectAllBtn);

        toolbar.add(Box.createHorizontalGlue());

        titleLabel = new JLabel("No folder open");
        titleLabel.setForeground(new Color(180, 180, 180));
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        toolbar.add(titleLabel);

        return toolbar;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(35, 35, 35));
        statusBar.setBorder(new EmptyBorder(4, 10, 4, 10));

        statusLabel = new JLabel("Ready  |  Open a folder to browse images");
        statusLabel.setForeground(new Color(170, 170, 170));
        statusLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statusBar.add(statusLabel, BorderLayout.WEST);

        return statusBar;
    }

    private JButton makeButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btn.setBackground(new Color(60, 63, 65));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(80, 83, 85), 1),
            new EmptyBorder(6, 14, 6, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (tooltip != null) btn.setToolTipText(tooltip);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(75, 79, 82));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(60, 63, 65));
            }
        });
        return btn;
    }

    private void openDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Image Folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (currentDirectory != null) {
            chooser.setCurrentDirectory(currentDirectory.toFile());
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path selected = chooser.getSelectedFile().toPath();
            if (!Files.isDirectory(selected)) {
                selected = selected.getParent();
            }
            if (selected != null && Files.isDirectory(selected)) {
                currentDirectory = selected;
                loadImages(currentDirectory);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Could not determine a valid directory.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadImages(Path dir) {
        statusLabel.setText("Loading...");
        titleLabel.setText(dir.toString());

        thumbnailLoader.submit(() -> {
            try (var stream = Files.list(dir)) {
                List<Path> images = stream
                    .filter(p -> Files.isRegularFile(p))
                    .filter(p -> {
                        String ext = getExtension(p.getFileName().toString()).toLowerCase();
                        return IMAGE_EXTENSIONS.contains(ext);
                    })
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                    .toList();

                SwingUtilities.invokeLater(() -> {
                    allImages.clear();
                    allImages.addAll(images);
                    selectedImages.clear();
                    thumbnailCache.clear();
                    rebuildGrid();
                    statusLabel.setText(String.format(
                        "Loaded %d images  |  0 selected", allImages.size()
                    ));
                });

                // Pre-load thumbnails in background
                for (Path img : images) {
                    thumbnailLoader.submit(() -> loadThumbnail(img));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this,
                        "Error reading directory:\n" + ex.getClass().getSimpleName() + ": " + ex.getMessage()
                        + "\n\nPath: " + dir,
                        "Error", JOptionPane.ERROR_MESSAGE)
                );
            }
        });
    }

    private void loadThumbnail(Path path) {
        if (thumbnailCache.containsKey(path)) return;
        try {
            BufferedImage original = readImage(path);
            if (original == null) return;

            int w = original.getWidth();
            int h = original.getHeight();
            int size = THUMB_SIZE;
            double scale = Math.min((double) size / w, (double) size / h);
            int nw = (int) (w * scale);
            int nh = (int) (h * scale);

            BufferedImage thumb = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = thumb.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(original, 0, 0, nw, nh, null);
            g.dispose();

            thumbnailCache.put(path, new ImageIcon(thumb));
        } catch (Exception ignored) {
        }
    }

    private void rebuildGrid() {
        gridPanel.removeAll();
        gridPanel.setLayout(new GridLayout(0, calculateColumns(), THUMB_GAP, THUMB_GAP));

        for (Path image : allImages) {
            gridPanel.add(createThumbnailPanel(image));
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private int calculateColumns() {
        if (scrollPane == null || allImages.isEmpty()) return 4;
        int availableWidth = scrollPane.getViewport().getWidth() - 40;
        int colWidth = THUMB_SIZE + THUMB_GAP + 16;
        return Math.max(1, availableWidth / colWidth);
    }

    private JPanel createThumbnailPanel(Path imagePath) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(new Color(45, 45, 45));
        card.setBorder(new LineBorder(new Color(60, 60, 60), 1));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(THUMB_SIZE + 16, THUMB_SIZE + 40));

        // Thumbnail
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(THUMB_SIZE, THUMB_SIZE));

        ImageIcon cached = thumbnailCache.get(imagePath);
        if (cached != null) {
            imageLabel.setIcon(cached);
        } else {
            imageLabel.setText("...");
            imageLabel.setForeground(Color.GRAY);
            thumbnailLoader.submit(() -> {
                loadThumbnail(imagePath);
                SwingUtilities.invokeLater(() -> {
                    ImageIcon icon = thumbnailCache.get(imagePath);
                    if (icon != null) {
                        imageLabel.setIcon(icon);
                        imageLabel.setText(null);
                        card.repaint();
                    }
                });
            });
        }

        card.add(imageLabel, BorderLayout.CENTER);

        // Filename label
        JLabel nameLabel = new JLabel(
            truncate(imagePath.getFileName().toString(), 18),
            SwingConstants.CENTER
        );
        nameLabel.setForeground(new Color(160, 160, 160));
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        nameLabel.setBorder(new EmptyBorder(0, 2, 2, 2));
        card.add(nameLabel, BorderLayout.SOUTH);

        // Selection state
        if (selectedImages.contains(imagePath)) {
            selectCard(card);
        }

        // Click handler
        MouseAdapter clickHandler = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleSelection(imagePath, card);
            }
        };
        card.addMouseListener(clickHandler);
        imageLabel.addMouseListener(clickHandler);
        nameLabel.addMouseListener(clickHandler);

        // Right-click context menu
        JPopupMenu popup = new JPopupMenu();
        JMenuItem copyItem = new JMenuItem("Copy to...");
        copyItem.addActionListener(e -> copySelected());
        JMenuItem zipItem = new JMenuItem("Zip selected");
        zipItem.addActionListener(e -> zipSelected());
        JMenuItem openItem = new JMenuItem("Open file");
        openItem.addActionListener(e -> openFile(imagePath));
        popup.add(openItem);
        popup.addSeparator();
        popup.add(copyItem);
        popup.add(zipItem);

        MouseAdapter popupHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showPopup(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showPopup(e);
            }
            private void showPopup(MouseEvent e) {
                if (!selectedImages.contains(imagePath)) {
                    toggleSelection(imagePath, card);
                }
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        };
        card.addMouseListener(popupHandler);
        imageLabel.addMouseListener(popupHandler);

        return card;
    }

    private void toggleSelection(Path path, JPanel card) {
        if (selectedImages.contains(path)) {
            selectedImages.remove(path);
            deselectCard(card);
        } else {
            selectedImages.add(path);
            selectCard(card);
        }
        updateStatus();
    }

    private void selectCard(JPanel card) {
        card.setBorder(new LineBorder(new Color(70, 130, 220), 2));
        card.setBackground(new Color(35, 45, 60));
    }

    private void deselectCard(JPanel card) {
        card.setBorder(new LineBorder(new Color(60, 60, 60), 1));
        card.setBackground(new Color(45, 45, 45));
    }

    private void refreshSelection() {
        Component[] components = gridPanel.getComponents();
        for (int i = 0; i < components.length && i < allImages.size(); i++) {
            if (components[i] instanceof JPanel card) {
                if (selectedImages.contains(allImages.get(i))) {
                    selectCard(card);
                } else {
                    deselectCard(card);
                }
            }
        }
        updateStatus();
    }

    private void updateStatus() {
        int total = allImages.size();
        int sel = selectedImages.size();
        statusLabel.setText(String.format(
            "Total: %d images  |  Selected: %d", total, sel
        ));
    }

    // ---- Copy ----
    private void copySelected() {
        if (selectedImages.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No images selected.", "Nothing to copy", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Copy Selected Images To...");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (currentDirectory != null) {
            chooser.setCurrentDirectory(currentDirectory.toFile());
        }
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path destDir = chooser.getSelectedFile().toPath();
        statusLabel.setText("Copying...");
        // JButton source = (JButton) SwingUtilities.getWindowAncestor(gridPanel).getFocusOwner();

        thumbnailLoader.submit(() -> {
            int copied = 0;
            int errors = 0;
            for (Path src : selectedImages) {
                try {
                    Path dest = destDir.resolve(src.getFileName().toString());
                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                    copied++;
                } catch (IOException e) {
                    errors++;
                }
            }
            int finalCopied = copied;
            int finalErrors = errors;
            SwingUtilities.invokeLater(() -> {
                String msg = String.format("Copied %d image(s) to:\n%s", finalCopied, destDir);
                if (finalErrors > 0) {
                    msg += String.format("\n\n%d file(s) failed to copy.", finalErrors);
                }
                JOptionPane.showMessageDialog(this, msg, "Copy Complete",
                    finalErrors > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
                statusLabel.setText(String.format(
                    "Copied %d images  |  %d selected", finalCopied, selectedImages.size()
                ));
            });
        });
    }

    // ---- Zip ----
    private void zipSelected() {
        if (selectedImages.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No images selected.", "Nothing to zip", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Zip Archive As...");
        chooser.setSelectedFile(new File("selected_images.zip"));
        if (currentDirectory != null) {
            chooser.setCurrentDirectory(currentDirectory.toFile());
        }
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path zipPath = chooser.getSelectedFile().toPath();
        if (!zipPath.toString().endsWith(".zip")) {
            zipPath = Path.of(zipPath.toString() + ".zip");
        }

        final Path finalZipPath = zipPath;
        statusLabel.setText("Creating zip archive...");

        thumbnailLoader.submit(() -> {
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(finalZipPath))) {
                int count = 0;
                for (Path src : selectedImages) {
                    zos.putNextEntry(new ZipEntry(src.getFileName().toString()));
                    Files.copy(src, zos);
                    zos.closeEntry();
                    count++;
                }
                int finalCount = count;
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        String.format("Created zip with %d image(s):\n%s", finalCount, finalZipPath),
                        "Zip Created", JOptionPane.INFORMATION_MESSAGE);
                    statusLabel.setText(String.format(
                        "Zipped %d images to %s  |  %d selected",
                        finalCount, finalZipPath.getFileName(), selectedImages.size()
                    ));
                });
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this,
                        "Error creating zip: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE)
                );
            }
        });
    }

    private void openFile(Path path) {
        try {
            Desktop.getDesktop().open(path.toFile());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Cannot open file: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Read an image file into a BufferedImage. Falls back to the macOS
     * "sips" tool to decode HEIF/HEIC (e.g. iPhone Live Photos), which
     * ImageIO does not support.
     */
    private static BufferedImage readImage(Path path) {
        try {
            BufferedImage img = ImageIO.read(path.toFile());
            if (img != null) return img;

            String ext = getExtension(path.getFileName().toString()).toLowerCase();
            if (!"heic".equals(ext) && !"heif".equals(ext)) return null;

            Path tmp = Files.createTempFile("pb_heic_", ".jpg");
            try {
                Process p = new ProcessBuilder(
                    "sips", "-s", "format", "jpeg", "-Z", "512",
                    path.toString(), "--out", tmp.toString()
                ).redirectErrorStream(true).start();
                if (p.waitFor() != 0) return null;
                return ImageIO.read(tmp.toFile());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } finally {
                Files.deleteIfExists(tmp);
            }
        } catch (IOException e) {
            return null;
        }
    }

    private static String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1) : "";
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) return s;
        return s.substring(0, max - 3) + "...";
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Dark UI defaults
        UIManager.put("Panel.background", new Color(30, 30, 30));
        UIManager.put("OptionPane.background", new Color(45, 45, 45));
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("Button.background", new Color(60, 63, 65));
        UIManager.put("Button.foreground", Color.WHITE);

        SwingUtilities.invokeLater(() -> {
            PhotoBrowser browser = new PhotoBrowser();
            browser.setVisible(true);

            // Keyboard shortcuts
            browser.getRootPane().registerKeyboardAction(
                e -> browser.openDirectory(),
                KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW
            );
            browser.getRootPane().registerKeyboardAction(
                e -> browser.copySelected(),
                KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW
            );
            browser.getRootPane().registerKeyboardAction(
                e -> browser.zipSelected(),
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW
            );
            browser.getRootPane().registerKeyboardAction(
                e -> {
                    browser.selectedImages.clear();
                    browser.refreshSelection();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
            );
        });
    }
}
