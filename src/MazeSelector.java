import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("unused")
public class MazeSelector extends JFrame {
    private int selectedMaze = 1;
    
    @SuppressWarnings("unused")
    public MazeSelector() {
        setTitle("Pacman Maze Selector");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 150);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        ButtonGroup group = new ButtonGroup();
        JRadioButton maze1 = new JRadioButton("Maze 1 - Classic Layout", true);
        JRadioButton maze2 = new JRadioButton("Maze 2 - Corner Ghosts Layout");
        
        group.add(maze1);
        group.add(maze2);
        
        maze1.addActionListener(e -> selectedMaze = 1);
        maze2.addActionListener(e -> selectedMaze = 2);
        
        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(e -> {
            dispose();
            JFrame window = new JFrame("Pacman");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.add(new Game(selectedMaze));
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
        
        panel.add(Box.createVerticalStrut(20));
        panel.add(maze1);
        panel.add(maze2);
        panel.add(Box.createVerticalStrut(20));
        panel.add(startButton);
        
        add(panel);
    }
}