import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
// import java.util.function.Function;

public class RotatingCube extends JPanel implements ActionListener {
    private final Timer timer;
    private double angleX = 0, angleY = 0, angleZ = 0;

    // 8 vertices of a unit cube centered at origin
    private final double[][] vertices = {
        {-1, -1, -1}, { 1, -1, -1}, { 1,  1, -1}, {-1,  1, -1},
        {-1, -1,  1}, { 1, -1,  1}, { 1,  1,  1}, {-1,  1,  1}
    };

    // 12 edges connecting vertex pairs
    //private final int[][] edges = {
    //    {0,1},{1,2},{2,3},{3,0},
    //    {4,5},{5,6},{6,7},{7,4},
    //    {0,4},{1,5},{2,6},{3,7}
    //};

    // 6 faces (4 vertices each) for filled rendering
    private final int[][] faces = {
        {0,1,2,3}, {5,4,7,6}, {1,5,6,2},
        {4,0,3,7}, {3,2,6,7}, {4,5,1,0}
    };

    private final Color[] faceColors = {
        new Color(220, 50, 50, 180),
        new Color(50, 180, 50, 180),
        new Color(50, 50, 220, 180),
        new Color(200, 200, 50, 180),
        new Color(200, 50, 200, 180),
        new Color(50, 200, 200, 180)
    };

    public RotatingCube() {
        setBackground(Color.BLACK);
        timer = new Timer(16, this); // ~60 FPS
        timer.start();
    }

    private double[] rotate(double[] p, double ax, double ay, double az) {
        double x = p[0], y = p[1], z = p[2];

        // Rotate X
        double y1 = y * Math.cos(ax) - z * Math.sin(ax);
        double z1 = y * Math.sin(ax) + z * Math.cos(ax);

        // Rotate Y
        double x2 = x * Math.cos(ay) + z1 * Math.sin(ay);
        double z2 = -x * Math.sin(ay) + z1 * Math.cos(ay);

        // Rotate Z
        double x3 = x2 * Math.cos(az) - y1 * Math.sin(az);
        double y3 = x2 * Math.sin(az) + y1 * Math.cos(az);

        return new double[]{x3, y3, z2};
    }

    private int[] project(double[] p, int cx, int cy, int scale, double dist) {
        double factor = dist / (dist + p[2]);
        return new int[]{(int)(p[0] * scale * factor) + cx, (int)(p[1] * scale * factor) + cy};
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int cx = w / 2, cy = h / 2;
        int scale = Math.min(w, h) / 6;
        double dist = 5.0;

        // Transform all vertices
        double[][] transformed = new double[8][3];
        for (int i = 0; i < 8; i++) {
            transformed[i] = rotate(vertices[i], angleX, angleY, angleZ);
        }

        // Sort faces by average Z (painter's algorithm)
        Integer[] faceOrder = {0, 1, 2, 3, 4, 5};
        java.util.Arrays.sort(faceOrder, (a, b) -> {
            double za = 0, zb = 0;
            for (int v : faces[a]) za += transformed[v][2];
            for (int v : faces[b]) zb += transformed[v][2];
            return Double.compare(za, zb);
        });

        // Draw faces back-to-front
        for (int fi : faceOrder) {
            int[] face = faces[fi];
            int[] xs = new int[face.length];
            int[] ys = new int[face.length];
            for (int i = 0; i < face.length; i++) {
                int[] pt = project(transformed[face[i]], cx, cy, scale, dist);
                xs[i] = pt[0];
                ys[i] = pt[1];
            }
            g2.setColor(faceColors[fi]);
            g2.fillPolygon(xs, ys, face.length);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawPolygon(xs, ys, face.length);
        }

        // Title
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.drawString("3D Rotating Cube  [Java " + Runtime.version().feature() + "]", 15, 25);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        angleX += 0.02;
        angleY += 0.03;
        angleZ += 0.01;
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Rotating Cube");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(true);
            frame.add(new RotatingCube());
            frame.setSize(700, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
