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

public class App extends JFrame {
    public static App Instance;
    public List<Ant> ants = new ArrayList<>();
    private final int[][] antPositions; // [antIndex][0]=row, [antIndex][1]=col
    private final Set<String> occupied = new HashSet<>();
    private final BoardPanel board;
    private final int rows;
    private final int cols;
    private int MAX_ANTS = 100;

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
        InitializeAnts(antCount);
        StartWorldLoop();
    }

    private void InitializeAnts(int antCount) {
        Random rand = new Random();
        for (int i = 0; i < antCount; i++) {
            int r, c;
            do {
                r = rand.nextInt(rows);
                c = rand.nextInt(cols);
            } while (occupied.contains(r+","+c));
            SpawnAnt(c, r);   
        }
        board.repaint();
    }

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

    public void RemoveAnt(Ant ant) {
        if (!ants.contains(ant)) {
            System.out.println("Invalid ant.");
            return;
        }
        int index = ants.indexOf(ant);
        occupied.remove(antPositions[index][0]+","+antPositions[index][1]);
        // Shift remaining ants
        for (int i = index; i < ants.size() - 1; i++) {
            ants.set(i, ants.get(i + 1));
            antPositions[i] = antPositions[i + 1];
        }
        ants.remove(ants.size() - 1);
    }

    private void StartWorldLoop() {
        Timer timer = new Timer(500, _ -> WorldTick());
        timer.start();
    }

    private void WorldTick() {
        Random rand = new Random();
        int[][] directions = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
        };
        for (int i = 0; i < ants.size(); i++) {
            System.out.println("Ant " + i + " at (" + antPositions[i][0] + "," + antPositions[i][1] + "), age: " + ants.get(i).GetAge());
            ants.get(i).IncrementAge();
            if (ants.get(i).IsDead()) {
                continue;
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
                occupied.remove(antPositions[i][0]+","+antPositions[i][1]);
                antPositions[i][0] = move[0];
                antPositions[i][1] = move[1];
                occupied.add(move[0]+","+move[1]);
            }
        }
        System.out.println("tick end");
        board.repaint();
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
}
