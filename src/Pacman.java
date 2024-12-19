import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.Timer;

public class Pacman {
    private Game game;
    private int x;
    private int y;
    private int width;
    private int height;
    public int speed;
    private int direction;
    public int nextDirection;

    private int currentFrame;
    private int frameCount;
    private BufferedImage[] pacmanFrames;
    private Timer timer;
    private int[][] map;

    public Pacman(
        Game game,
        int x,
        int y,
        int width,
        int height,
        int speed
    ) {
        this.game = game;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.direction = Game.DIRECTION_RIGHT;
        this.nextDirection = Game.DIRECTION_RIGHT;
        this.currentFrame = 1;
        this.frameCount = 7;
        this.map = game.map;

        pacmanFrames = loadFrames("./resource/pacman/animations.gif", frameCount, width, height);

        timer = new Timer(100, e -> changeAnimation());
        timer.start();
    }

    private BufferedImage[] loadFrames(String path, int count, int width, int height) {
        BufferedImage[] result = new BufferedImage[count];
        try {
            BufferedImage gif = ImageIO.read(getClass().getResource(path));
            for (int i = 0; i < count; i++) {
                result[i] = gif.getSubimage(i * width, 0, width, height);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void moveProcess() {
        changeDirectionIfPossible();
        this.moveForwards();
        if (this.checkCollision(map)) {
            this.moveBackwards();
        }
    }

    public void eat(int[][] map) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (
                    map[i][j] == 2 &&
                    this.getMapX() == j &&
                    this.getMapY() == i
                ) {
                    map[i][j] = 8; // eaten food
                    game.score++;
                }
            }
        }
    }

    private void moveBackwards() {
        switch (direction) {
            case Game.DIRECTION_RIGHT: 
                x -= speed; 
                break;
            case Game.DIRECTION_UP: 
                y += speed; 
                break;
            case Game.DIRECTION_LEFT: 
                x += speed; 
                break;
            case Game.DIRECTION_DOWN: 
                y -= speed; 
                break;
        }
    }

    private void moveForwards() {
        switch (direction) {
            case Game.DIRECTION_RIGHT: 
                x += speed; 
                break;
            case Game.DIRECTION_UP: 
                y -= speed; 
                break;
            case Game.DIRECTION_LEFT: 
                x -= speed; 
                break;
            case Game.DIRECTION_DOWN:   
                y += speed; 
                break;
        }
    }

    public boolean checkCollision(int[][] map) {
        if (
            map[getMapY()][getMapX()] == 1 ||
            map[getMapYRightSide()][getMapX()] == 1 ||
            map[getMapY()][getMapXRightSide()] == 1 ||
            map[getMapYRightSide()][getMapXRightSide()] == 1
        ) {
            return true;
        }
        return false; 

    }

    public boolean checkGhostCollision(HashSet<Ghost> ghosts) {
        for (Ghost ghost : ghosts) {
            if (
                ghost.getMapX() == this.getMapX() &&
                ghost.getMapY() == this.getMapY()
            ) {
                return true;
            }
        }
        return false;
    }

    public void changeDirectionIfPossible() {
        if (direction == nextDirection) return;
        int tempDirection = direction;
        direction = nextDirection;
        moveForwards();
        if (checkCollision(game.map)) {
            moveBackwards();
            direction = tempDirection;
        } else {
            moveBackwards();
        }
    }

    public void setNextDirection(int dir) {
        this.nextDirection = dir;
    }

    public void changeAnimation() {
        currentFrame = currentFrame == frameCount ? 1 : currentFrame + 1;
    }

    public int getMapX() {
        return (int) (x / game.oneBlockSize);
    }

    public int getMapY() {
        return (int) (y / game.oneBlockSize);
    }

    public int getMapXRightSide() {
        return (int) ((x + 0.9999 * game.oneBlockSize) / game.oneBlockSize);
    }

    public int getMapYRightSide() {
        return (int) ((y + 0.9999 * game.oneBlockSize) / game.oneBlockSize);
    }

    public void draw(Graphics2D g2d) {
        AffineTransform original = g2d.getTransform();
        g2d.translate(
            x + width / 2, 
            y + height / 2);
        g2d.rotate((this.direction * 90 * Math.PI) / 180);
        g2d.drawImage(
            pacmanFrames[currentFrame - 1], 
            -width / 2, 
            -height / 2, 
            width, 
            height, 
            null);
        g2d.setTransform(original);
    }
}