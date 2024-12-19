import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pac Man");
            frame.setSize(500, 500);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Game game = new Game();
            frame.add(game);
            frame.pack();
            game.requestFocus();
            frame.setVisible(true);
        });
    }
}