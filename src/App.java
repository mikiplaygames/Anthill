/// Programowal: Mikolaj Gajewski 205813 MTR \\\
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.*;
public final class App extends JFrame {
    public static App Instance; // Singleton instance
    public List<Ant> ants = new ArrayList<>(); // All ants
    private final int[][] antPositions; // Ant positions
    private final Set<String> occupied = new HashSet<>(); // Occupied cells
    private final BoardPanel board; // Board panel
    private final int rows, cols; // Grid size
    private int MAX_ANTS = 100; // Max ants
    private JFrame antListFrame = null; // Ant list window

    // App constructor
    public App(int x, int y, int antCount) {
        setTitle("Mrowisko");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        rows = y;
        cols = x;
        MAX_ANTS = (x*y) / 2;
        antPositions = new int[MAX_ANTS][2];
        board = new BoardPanel(y, x, antPositions);
        setContentPane(board);
        pack();
        setLocationRelativeTo(null);
        InitializeAnts(antCount); // Place starting ants
        setVisible(true);
        ShowAntListWindow(); // Show ant list
        StartWorldLoop(); // Start simulation timer
    }

    // Randomly place the initial ants on the board
    private void InitializeAnts(int antCount) {
        Random rand = new Random();
        for (int i = 0; i < antCount; i++) {
            int r, c;
            do {
                r = rand.nextInt(rows);
                c = rand.nextInt(cols);
            } while (occupied.contains(r+","+c)); // Ensure no overlap
            SpawnAnt(c, r);
        }
        board.repaint();
    }

    // Add a new ant at (x, y) if possible
    public void SpawnAnt(int x, int y)
    {
        if (MAX_ANTS <= ants.size()) {
            System.out.println("Max ant count reached, cannot spawn more ants.");
            return;
        }
        antPositions[ants.size()][0] = y;
        antPositions[ants.size()][1] = x;
        Ant ant = new Ant();
        ants.add(ant);
        occupied.add(y+","+x);
    }

    // Safely remove an ant from the board
    public void RemoveAnt(Ant ant) {
        if (!ants.contains(ant)) {
            System.out.println("Invalid ant.");
            return;
        }
        int index = ants.indexOf(ant);
        occupied.remove(antPositions[index][0]+","+antPositions[index][1]);
        // Shift remaining ants and positions
        for (int i = index; i < ants.size() - 1; i++) {
            ants.set(i, ants.get(i + 1));
            antPositions[i] = antPositions[i + 1];
        }
        ants.remove(ants.size() - 1);
    }

    // Start the simulation tick timer (world loop)
    private void StartWorldLoop() {
        Timer timer = new Timer(500, _ -> WorldTick());
        timer.start();
    }

    // move ants, update state, render board
    private void WorldTick() {
        Random rand = new Random();
        int[][] directions = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
        };
        for (int i = 0; i < ants.size(); i++) {
            System.out.println("Ant " + i + " at (" + antPositions[i][0] + "," + antPositions[i][1] + "), age: " + ants.get(i).GetAge());
            ants.get(i).IncrementAge(); // Age the ant
            if (ants.get(i).IsDead()) {
                continue; // if dead, skip movement
            }
            int r = antPositions[i][0];
            int c = antPositions[i][1];
            List<int[]> moves = new ArrayList<>();
            for (int[] d : directions) {
                int nr = r + d[0];
                int nc = c + d[1];
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                    moves.add(new int[]{nr, nc});
                }
            }
            if (!moves.isEmpty()) {
                int[] move = moves.get(rand.nextInt(moves.size()));
                // If the target cell is occupied, try to spawn a new ant
                if (occupied.contains(move[0]+","+move[1])) {
                    if (!ants.get(i).CanCopulate())
                        continue;
                    int[] notOccupiedMove = moves.stream()
                        .filter(m -> !occupied.contains(m[0]+","+m[1]))
                        .findAny()
                        .orElse(null);
                    if (notOccupiedMove != null) {
                        SpawnAnt(notOccupiedMove[0], notOccupiedMove[1]);
                    }
                    continue;
                }
                // Move the ant to the new cell
                occupied.remove(antPositions[i][0]+","+antPositions[i][1]);
                antPositions[i][0] = move[0];
                antPositions[i][1] = move[1];
                occupied.add(move[0]+","+move[1]);
            }
        }
        System.out.println("tick end");
        board.repaint(); // Redraw the board
        ShowAntListWindow(); // Update the ant list window
    }

    // Show or update the window listing all ants and their ages
    public void ShowAntListWindow() {
        if (antListFrame == null) {
            antListFrame = new JFrame("Ant List");
            antListFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            antListFrame.setSize(300, 50 + 60 * ants.size());
            antListFrame.setLocationRelativeTo(this);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            antListFrame.setContentPane(new JScrollPane(panel));
            antListFrame.setVisible(true);
        }
        // Update the list contents
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (Ant ant : ants) {
            JPanel antPanel = new JPanel();
            antPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel imgLabel;
            BufferedImage img;
            if (ant.IsDead()) {
                img = board.getAntDeadImg();
            } else if (ant.CanCopulate()) {
                img = board.getAntBigImg();
            } else {
                img = board.getAntSmallImg();
            }
            if (img != null) {
                imgLabel = new JLabel(new ImageIcon(img.getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH)));
            } else {
                imgLabel = new JLabel();
            }
            antPanel.add(imgLabel);
            antPanel.add(new JLabel("Ant, age: " + ant.GetAge()));
            panel.add(antPanel);
        }
        JScrollPane scrollPane = new JScrollPane(panel);
        antListFrame.setContentPane(scrollPane);
        antListFrame.revalidate();
        antListFrame.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ShowInitWindow();
        });
    }

    private static void ShowInitWindow() {
        JFrame inputFrame = new JFrame("Set Grid Size");
        inputFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        inputFrame.setSize(250, 150);
        inputFrame.setLayout(new GridLayout(5, 2, 5, 5));
        inputFrame.setLocationRelativeTo(null);

        JLabel xLabel = new JLabel("X size:");
        JTextField xField = new JTextField("10");
        JLabel yLabel = new JLabel("Y size:");
        JTextField yField = new JTextField("10");
        JLabel antLabel = new JLabel("Ant count:");
        JTextField antField = new JTextField( "6");
        JLabel antAgeLabel = new JLabel("Ant max age:");
        JTextField antAgeField = new JTextField("50");
        JButton okButton = new JButton("OK");

        inputFrame.add(xLabel);
        inputFrame.add(xField);
        inputFrame.add(yLabel);
        inputFrame.add(yField);
        inputFrame.add(antLabel);
        inputFrame.add(antField);
        inputFrame.add(antAgeLabel);
        inputFrame.add(antAgeField);
        inputFrame.add(okButton);

        okButton.addActionListener(_ -> {
            try {
                int x = Integer.parseInt(xField.getText());
                int y = Integer.parseInt(yField.getText());
                int antCount = Integer.parseInt(antField.getText());
                Ant.MaxAge = Integer.parseInt(antAgeField.getText());
                if (x > 600 || y > 600 || antCount >= x*y || antCount <= 0 || Ant.MaxAge <= 0) {
                    JOptionPane.showMessageDialog(inputFrame, "Invalid input values. Please ensure:\n- X and Y are less than or equal to 600\n- Ant count is positive and less than the total grid size (X * Y).\n- Ant max age is positive.");
                    return;
                }
                inputFrame.dispose();
                Instance = new App(x, y, antCount);
                Instance.setVisible(true);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(inputFrame, "Please enter valid integers for X, Y and Ant count.");
            }
        });

        inputFrame.setVisible(true);
    }
}

// BoardPanel draws the grid and ants
class BoardPanel extends JPanel {
    private int rows, cols;
    private int cellSize = 40;
    private int[][] antPositions;
    private BufferedImage antBigImg, antSmallImg, antDeadImg;

    public BoardPanel(int rows, int cols, int[][] antPositions) {
        this.rows = rows;
        this.cols = cols;
        this.antPositions = antPositions;
        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
        try {
            antBigImg = ImageIO.read(new File("ant_big.png"));
            antSmallImg = ImageIO.read(new File("ant_small.png"));
            antDeadImg = ImageIO.read(new File("ant_dead.png"));
        } catch (IOException e) {
            System.err.println("Could not load ant images: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the grid
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = col * cellSize;
                int y = row * cellSize;
                g.setColor((row + col) % 2 == 0 ? Color.LIGHT_GRAY : Color.WHITE);
                g.fillRect(x, y, cellSize, cellSize);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, cellSize, cellSize);
            }
        }
        // Draw ants as images
        BufferedImage img;
        for (int i = 0; i < antPositions.length; i++) {
            int[] pos = antPositions[i];
            int x = pos[1] * cellSize;
            int y = pos[0] * cellSize;
            if (i < App.Instance.ants.size()) {
                Ant ant = App.Instance.ants.get(i);
                if (ant.IsDead()) {
                    img = antDeadImg;
                } else {
                    img = ant.CanCopulate() ? antBigImg : antSmallImg;
                }
                g.drawImage(img, x + cellSize/8, y + cellSize/8, 3*cellSize/4, 3*cellSize/4, null);
            }
        }
    }

    // Getters for ant images
    public BufferedImage getAntBigImg() { return antBigImg; }
    public BufferedImage getAntSmallImg() { return antSmallImg; }
    public BufferedImage getAntDeadImg() { return antDeadImg; }
}