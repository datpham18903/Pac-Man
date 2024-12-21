import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            // Launch the maze selector to choose the maze type
            MazeSelector mazeSelector = new MazeSelector();
            mazeSelector.setVisible(true);  // Show maze selection screen
        });
    }

    public static void startGame(int selectedMaze) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pac Man");
            frame.setSize(500, 500);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Pass the selected maze to Game
            Game game = new Game(selectedMaze);
            frame.add(game);
            frame.pack();
            game.requestFocus();
            frame.setVisible(true);
        });
    }
}