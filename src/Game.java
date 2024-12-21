import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.TimerTask;
import java.util.Timer;
import javax.swing.*;

public class Game extends JPanel implements KeyListener {
    // Constants
    public static final int DIRECTION_RIGHT = 4;
    public static final int DIRECTION_UP = 3;
    public static final int DIRECTION_LEFT = 2;
    public static final int DIRECTION_DOWN = 1;

    // Game state
    private boolean inGame = false;
    public int score = 0;
    private int lives = 3;
    public int fps = 30;
    private Timer timer;

    // Size constants
    public int oneBlockSize = 20;
    private double wallSpaceWidth = oneBlockSize / 1.2;
    private double wallOffset = (oneBlockSize - wallSpaceWidth) / 2;

    // Game sprites
    public Image blinky;
    public Image pinky;
    public Image inky;
    public Image clyde;
    public Image pacmanRightImage;
    private Font Emulogic;

    // Game objects
    private HashSet<Ghost> ghosts;
    private Pacman pacman;

    // Map layout
    // 0 = Skip, 1 = Wall, 2 = Food, 3 = Pacman
    // 4 = Blinky (Red), 5 = Pinky (Pink), 6 = Inky (Cyan), 7 = Clyde (Orange)
    public int[][] map = {
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1},
        {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1},
        {1, 2, 2, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2, 2, 2, 1},
        {1, 1, 1, 1, 1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1},
        {0, 0, 0, 0, 1, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 0, 0, 0, 0},
        {1, 1, 1, 1, 1, 2, 1, 2, 1, 1, 4, 1, 1, 2, 1, 2, 1, 1, 1, 1, 1},
        {1, 2, 2, 2, 2, 2, 2, 2, 1, 5, 6, 7, 1, 2, 2, 2, 2, 2, 2, 2, 1},
        {1, 1, 1, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 1, 1, 1, 1, 1},
        {0, 0, 0, 0, 1, 2, 1, 2, 1, 1, 1, 1, 1, 2, 1, 2, 1, 0, 0, 0, 0},
        {0, 0, 0, 0, 1, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 0, 0, 0, 0},
        {1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1, 1},
        {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1},
        {1, 2, 2, 2, 1, 2, 2, 2, 2, 2, 3, 2, 2, 2, 2, 2, 1, 2, 2, 2, 1},
        {1, 1, 2, 2, 1, 2, 1, 2, 1, 1, 1, 1, 1, 2, 1, 2, 1, 2, 2, 1, 1},
        {1, 2, 2, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2, 2, 2, 1},
        {1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1},
        {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };

    public Point[] randomTargetsForGhosts = {
        new Point(oneBlockSize, oneBlockSize),
        new Point(oneBlockSize, (map.length - 2) * oneBlockSize),
        new Point((map[0].length - 2) * oneBlockSize, oneBlockSize),
        new Point((map[0].length - 2) * oneBlockSize, (map.length - 2) * oneBlockSize)
    };

    public Game() {
        setPreferredSize(new Dimension(420, 500));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        loadImages();
        loadCustomFont();
        restartPacmanandGhosts();
        gameLoop();
    }

    private void loadImages() {
        try {
            blinky = new ImageIcon(getClass().getResource("./resource/ghosts/blinky.png")).getImage();
            pinky = new ImageIcon(getClass().getResource("./resource/ghosts/pinky.png")).getImage();
            inky = new ImageIcon(getClass().getResource("./resource/ghosts/inky.png")).getImage();
            clyde = new ImageIcon(getClass().getResource("./resource/ghosts/clyde.png")).getImage();
            pacmanRightImage = new ImageIcon(getClass().getResource("./resource/pacman/pacmanRight.png")).getImage();
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }

    private void loadCustomFont() {
        try {
            Emulogic = Font.createFont(Font.TRUETYPE_FONT, 
            getClass().getResourceAsStream("/resource/font/Emulogic-zrEw.ttf"))
            .deriveFont(12f);
            GraphicsEnvironment ge = 
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Emulogic);
        } catch (IOException | FontFormatException e) {
            System.err.println("Error loading custom font: " + e.getMessage());
            Emulogic = new Font("Arial", Font.PLAIN, 16);
        }
    }

    private void gameLoop() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                update();
                repaint();
            }
        }, 0, 1000 / fps);
    }

    private boolean allFoodsEaten() {
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[0].length; c++) {
                if (map[r][c] == 2) {
                    return false;
                }
            }
        }
        return true;
    }

    private void restoreFood() {
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[0].length; c++) {
                if (map[r][c] == 8) {
                    map[r][c] = 2;
                }
            }
        }
    }

    private void update() {
        if (inGame) {
            pacman.moveProcess();
            pacman.eat(map);
            Ghost.updateGhosts(ghosts);

            if (pacman.checkGhostCollision(ghosts)) {
                lives--;
                if (lives == 0) {
                    inGame = false;
                    score = 0;
                    lives = 3;
                    restoreFood();
                }
                restartPacmanandGhosts();
            }
            
            if (allFoodsEaten()) {
                inGame = false;
                score = 0;
                lives = 3;
                restartPacmanandGhosts();
                restoreFood();
            }
        }
    }

    private void restartPacmanandGhosts() {
        createNewPacman();
        createGhosts();
    }

    public void createRect(Graphics2D g2d, double x, double y, double width, double height, Color color) {
            g2d.setColor(color);
            g2d.fill(new Rectangle2D.Double(x, y, width, height));
    }

    public void createNewPacman() {
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[0].length; c++) {
                if (map[r][c] == 3) { //pacman
                    pacman = new Pacman(
                        this,
                        c * oneBlockSize,
                        r * oneBlockSize,
                        oneBlockSize,
                        oneBlockSize,
                        oneBlockSize / 5
                    );
                }
            }
        }
    }

    public void createGhosts() {
        ghosts = new HashSet<Ghost>();
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[0].length; c++) {
                if (map[r][c] == 4) { //red ghost
                    Ghost ghost = new Ghost(
                        this,
                        pacman,
                        blinky, 
                        c * oneBlockSize, 
                        r * oneBlockSize, 
                        oneBlockSize, 
                        oneBlockSize,
                        pacman.speed / 2,
                        6
                    );
                    ghosts.add(ghost);
                }
                else if (map[r][c] == 5) { //pink ghost
                    Ghost ghost = new Ghost(
                        this,
                        pacman,
                        pinky, 
                        c * oneBlockSize, 
                        r * oneBlockSize, 
                        oneBlockSize, 
                        oneBlockSize,
                        pacman.speed / 2,
                        6
                    );
                    ghosts.add(ghost);
                }
                else if (map[r][c] == 6) { //cyan ghost
                    Ghost ghost = new Ghost(
                        this,
                        pacman,
                        inky, 
                        c * oneBlockSize, 
                        r * oneBlockSize, 
                        oneBlockSize, 
                        oneBlockSize,
                        pacman.speed / 2,
                        6
                    );
                    ghosts.add(ghost);
                }
                else if (map[r][c] == 7) { //orange ghost
                    Ghost ghost = new Ghost(
                        this,
                        pacman,
                        clyde, 
                        c * oneBlockSize, 
                        r * oneBlockSize, 
                        oneBlockSize, 
                        oneBlockSize,
                        pacman.speed /2,
                        6
                    );
                    ghosts.add(ghost);
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        drawWalls(g2d);
        drawFoods(g2d);
        pacman.draw(g2d);
        drawGhost(g2d);
        drawScore(g2d);
        drawRemainingLives(g2d);

        if (!inGame) {
            drawIntro(g2d);
        }
    }

    private void drawIntro(Graphics2D g2d) {
        g2d.setColor(Color.yellow);
        g2d.setFont(Emulogic);
        g2d.drawString(
            "Ready!", 
            (oneBlockSize * map[0].length) / 2 - 30, 
            (oneBlockSize * map.length) / 2 + 45
        );
    }

    private void drawGhost(Graphics2D g2d) {
        for (Ghost ghost : ghosts) {
            ghost.draw(g2d);
        }
    }

    private void drawWalls(Graphics2D g2d) {
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[0].length; c++) {
                if (map[r][c] == 1) {
                    createRect(
                        g2d,
                        c * oneBlockSize,
                        r * oneBlockSize,
                        oneBlockSize,
                        oneBlockSize,
                        Color.decode("#342DCA")
                    );
                    if (c > 0 && map[r][c - 1] == 1) {
                        createRect(
                            g2d,
                            c * oneBlockSize,
                            r * oneBlockSize + wallOffset,
                            wallSpaceWidth + wallOffset,
                            wallSpaceWidth,
                            Color.BLACK
                        );
                    }

                    if (c < map[0].length - 1 && map[r][c + 1] == 1) {
                        createRect(
                            g2d,
                            c * oneBlockSize + wallOffset,
                            r * oneBlockSize + wallOffset,
                            wallSpaceWidth + wallOffset,
                            wallSpaceWidth,
                            Color.BLACK
                        );
                    }

                    if (r < map.length - 1 && map[r + 1][c] == 1) {
                        createRect(
                            g2d,
                            c * oneBlockSize + wallOffset,
                            r * oneBlockSize + wallOffset,
                            wallSpaceWidth,
                            wallSpaceWidth + wallOffset,
                            Color.BLACK
                        );
                    }

                    if (r > 0 && map[r - 1][c] == 1) {
                        createRect(
                            g2d,
                            c * oneBlockSize + wallOffset,
                            r * oneBlockSize,
                            wallSpaceWidth,
                            wallSpaceWidth + wallOffset,
                            Color.BLACK
                        );
                    }
                }
            }
        }
    };

    private void drawFoods(Graphics2D g2d) {
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[0].length; c++) {
                if (map[r][c] == 2) {
                    createRect(
                        g2d, 
                        c * oneBlockSize + 9,
                        r * oneBlockSize + 9,
                        3, 
                        3, 
                        Color.decode("#FEB897")
                    );
                }
            }
        }
    };

    private void drawScore(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(Emulogic);
        g2d.drawString(
            "Score:" + score, 
            300, 
            oneBlockSize * (map.length + 1)
        );
    }

    private void drawRemainingLives(Graphics2D g2d) {
        for (int i = 0; i < lives; i++) {
            g2d.drawImage(
                pacmanRightImage,
                9 + i * oneBlockSize,
                oneBlockSize * map.length + 2,
                oneBlockSize,
                oneBlockSize,
                null
            );
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyReleased(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (!inGame) {
            if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
                inGame = true;
            }
        } else {
            switch (key) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    pacman.nextDirection = DIRECTION_LEFT;
                    break;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    pacman.nextDirection = DIRECTION_UP;
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    pacman.nextDirection = DIRECTION_RIGHT;
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    pacman.nextDirection = DIRECTION_DOWN;
                    break;
                case KeyEvent.VK_ESCAPE:
                    inGame = false;
                    restartPacmanandGhosts();
                    break;
            }
        }
    }
}