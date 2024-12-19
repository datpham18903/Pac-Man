import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Stack;

public class Ghost {
    public Image image;
    public int x;
    public int y;
    public int width;
    public int height;
    public double speed;
    public int range;

    private int direction;
    private Object target;
    private int[][] map;
    private double randomDirectionTimer = 0;
    private double directionChangeInterval = 10.0;
    private Random random = new Random();

    private Game game;
    private Pacman pacman;

    public Ghost(
        Game game,
        Pacman pacman,
        Image image,
        int x,
        int y,
        int width,
        int height,
        double speed,
        int range
    ) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.range = range;

        this.direction = Game.DIRECTION_RIGHT;
        this.game = game;
        this.map = game.map;
        this.pacman = pacman;
    }

    private boolean isInRange() {
        int xDistance = Math.abs(pacman.getMapX() - getMapX());
        int yDistance = Math.abs(pacman.getMapY() - getMapY());
        return Math.sqrt(xDistance * xDistance + yDistance * yDistance) <= range;
    }

    private void moveProcess() {
        if (isInRange()) {
            target = pacman;
            changeDirectionIfPossible();
        } else {
            randomDirectionTimer += 1 / game.fps;
            if (randomDirectionTimer >= directionChangeInterval) {
                randomDirectionTimer = 0;
                direction = 1 + random.nextInt(4);
            }
        }

        moveForwards();
        if (checkCollisions(map)) {
            moveBackwards();
            if (!isInRange()) {
                direction = 1 + random.nextInt(4);
            }
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


    public boolean checkCollisions(int[][] map) {
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

    private void changeDirectionIfPossible() {
        int tempDirection = direction;
        int targetX, targetY;
        if (target instanceof Pacman) {
            targetX = pacman.getMapX();
            targetY = pacman.getMapY();
        } else {
            Point targetPoint = (Point) target;
            targetX = targetPoint.x / game.oneBlockSize;
            targetY = targetPoint.y / game.oneBlockSize;
        }

        int newDirection = calculateNewDirection(map, targetX, targetY);
        
        if (newDirection == -1) {
            direction = tempDirection;
            return;
        }
        
        direction = newDirection;

        if (
            getMapY() != getMapYRightSide() && 
            (direction == Game.DIRECTION_LEFT || 
            direction == Game.DIRECTION_RIGHT)
        ) {
            direction = Game.DIRECTION_UP;
        }
        
        if (
            getMapX() != getMapXRightSide() && 
            direction == Game.DIRECTION_UP
        ) {
            direction = Game.DIRECTION_LEFT;
        }

        moveForwards();
        if (checkCollisions(map)) {
            moveBackwards();
            direction = tempDirection;
        } else {
            moveBackwards();
        }
    }

    private int calculateNewDirection(int[][] map, int destX, int destY) {
        if (
            this.image == game.blinky
        ) {
            return calculateUCS(map, destX, destY);
        } else if (
            this.image == game.pinky
        ) {
            return calculateDFS(map, destX, destY);
        } else if (
            this.image == game.inky
        ) {
            return calculateAStar(map, destX, destY);
        } else if (
            this.image == game.clyde
        ) {
            return calculateBDFS(map, destX, destY);
        }
        return Game.DIRECTION_DOWN;
    }

    // For Blinky - Uniform Cost Search
    private int calculateUCS(int[][] map, int destX, int destY) {
        PriorityQueue<PathNode> queue = new PriorityQueue<>((a, b) -> a.cost - b.cost);
        boolean[][] visited = new boolean[map.length][map[0].length];
        
        queue.offer(new PathNode(getMapX(), getMapY(), 0, new ArrayList<>()));
        
        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            
            if (current.x == destX && current.y == destY) {
                return current.moves.isEmpty() ? Game.DIRECTION_RIGHT : current.moves.get(0);
            }
            
            if (visited[current.y][current.x]) continue;
            visited[current.y][current.x] = true;
            
            // Add neighbors with cost
            int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}}; // Up, Right, Down, Left
            int[] moveDirections = {Game.DIRECTION_UP, Game.DIRECTION_RIGHT, Game.DIRECTION_DOWN, Game.DIRECTION_LEFT};
            
            for (int i = 0; i < directions.length; i++) {
                int newX = current.x + directions[i][0];
                int newY = current.y + directions[i][1];
                
                if (isValidMove(newX, newY, map) && !visited[newY][newX]) {
                    List<Integer> newMoves = new ArrayList<>(current.moves);
                    if (newMoves.isEmpty()) {
                        newMoves.add(moveDirections[i]);
                    }
                    queue.offer(new PathNode(newX, newY, current.cost + 1, newMoves));
                }
            }
        }
        return Game.DIRECTION_RIGHT;
    }

    // For Pinky - Depth First Search
    private int calculateDFS(int[][] map, int destX, int destY) {
        Stack<PathNode> stack = new Stack<>();
        boolean[][] visited = new boolean[map.length][map[0].length];
        
        stack.push(new PathNode(getMapX(), getMapY(), 0, new ArrayList<>()));
        
        while (!stack.isEmpty()) {
            PathNode current = stack.pop();
            
            if (current.x == destX && current.y == destY) {
                return current.moves.isEmpty() ? Game.DIRECTION_RIGHT : current.moves.get(0);
            }
            
            if (visited[current.y][current.x]) continue;
            visited[current.y][current.x] = true;
            
            // Add neighbors in reverse order for DFS
            int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}}; // Left, Down, Right, Up
            int[] moveDirections = {Game.DIRECTION_LEFT, Game.DIRECTION_DOWN, Game.DIRECTION_RIGHT, Game.DIRECTION_UP};
            
            for (int i = 0; i < directions.length; i++) {
                int newX = current.x + directions[i][0];
                int newY = current.y + directions[i][1];
                
                if (isValidMove(newX, newY, map) && !visited[newY][newX]) {
                    List<Integer> newMoves = new ArrayList<>(current.moves);
                    if (newMoves.isEmpty()) {
                        newMoves.add(moveDirections[i]);
                    }
                    stack.push(new PathNode(newX, newY, 0, newMoves));
                }
            }
        }
        return Game.DIRECTION_RIGHT;
    }

    // For Inky - A* Search
    private int calculateAStar(int[][] map, int destX, int destY) {
        PriorityQueue<PathNode> openSet = new PriorityQueue<>((a, b) -> 
            (a.cost + manhattan(a.x, a.y, destX, destY)) - 
            (b.cost + manhattan(b.x, b.y, destX, destY)));
        boolean[][] visited = new boolean[map.length][map[0].length];
        
        openSet.offer(new PathNode(getMapX(), getMapY(), 0, new ArrayList<>()));
        
        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();
            
            if (current.x == destX && current.y == destY) {
                return current.moves.isEmpty() ? Game.DIRECTION_RIGHT : current.moves.get(0);
            }
            
            if (visited[current.y][current.x]) continue;
            visited[current.y][current.x] = true;
            
            int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}}; // Up, Right, Down, Left
            int[] moveDirections = {Game.DIRECTION_UP, Game.DIRECTION_RIGHT, Game.DIRECTION_DOWN, Game.DIRECTION_LEFT};
            
            for (int i = 0; i < directions.length; i++) {
                int newX = current.x + directions[i][0];
                int newY = current.y + directions[i][1];
                
                if (isValidMove(newX, newY, map) && !visited[newY][newX]) {
                    List<Integer> newMoves = new ArrayList<>(current.moves);
                    if (newMoves.isEmpty()) {
                        newMoves.add(moveDirections[i]);
                    }
                    int newCost = current.cost + 1;
                    openSet.offer(new PathNode(newX, newY, newCost, newMoves));
                }
            }
        }
        return Game.DIRECTION_RIGHT;
    }

    // For Clyde - Bidirectional DFS
    private int calculateBDFS(int[][] map, int destX, int destY) {
        Stack<PathNode> forwardStack = new Stack<>();
        Stack<PathNode> backwardStack = new Stack<>();
        boolean[][] visitedForward = new boolean[map.length][map[0].length];
        boolean[][] visitedBackward = new boolean[map.length][map[0].length];
        
        forwardStack.push(new PathNode(getMapX(), getMapY(), 0, new ArrayList<>()));
        backwardStack.push(new PathNode(destX, destY, 0, new ArrayList<>()));
        
        while (!forwardStack.isEmpty() && !backwardStack.isEmpty()) {
            // Forward search
            PathNode current = forwardStack.pop();
            if (visitedBackward[current.y][current.x]) {
                return current.moves.isEmpty() ? Game.DIRECTION_RIGHT : current.moves.get(0);
            }
            
            if (!visitedForward[current.y][current.x]) {
                visitedForward[current.y][current.x] = true;
                addNeighborsToDFS(current, map, visitedForward, forwardStack);
            }
            
            // Backward search
            current = backwardStack.pop();
            if (visitedForward[current.y][current.x]) {
                return current.moves.isEmpty() ? Game.DIRECTION_RIGHT : current.moves.get(0);
            }
            
            if (!visitedBackward[current.y][current.x]) {
                visitedBackward[current.y][current.x] = true;
                addNeighborsToDFS(current, map, visitedBackward, backwardStack);
            }
        }
        return Game.DIRECTION_RIGHT;
    }

    // Helper methods
    private boolean isValidMove(int x, int y, int[][] map) {
        return x >= 0 && x < map[0].length && y >= 0 && y < map.length && map[y][x] != 1;
    }

    private int manhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private void addNeighborsToDFS(PathNode current, int[][] map, boolean[][] visited, Stack<PathNode> stack) {
        int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}}; // Left, Down, Right, Up
        int[] moveDirections = {Game.DIRECTION_LEFT, Game.DIRECTION_DOWN, Game.DIRECTION_RIGHT, Game.DIRECTION_UP};
        
        for (int i = 0; i < directions.length; i++) {
            int newX = current.x + directions[i][0];
            int newY = current.y + directions[i][1];
            
            if (isValidMove(newX, newY, map) && !visited[newY][newX]) {
                List<Integer> newMoves = new ArrayList<>(current.moves);
                if (newMoves.isEmpty()) {
                    newMoves.add(moveDirections[i]);
                }
                stack.push(new PathNode(newX, newY, 0, newMoves));
            }
        }
    }

    private class PathNode {
        int x;
        int y;
        int cost;
        List<Integer> moves;

        PathNode(int x, int y, int cost, List<Integer> moves) {
            this.x = x;
            this.y = y;
            this.cost = cost;
            this.moves = moves;
        }
    }    
    
    public int getMapX() {
        return (int) (x / game.oneBlockSize);
    }

    public int getMapY() {
        return (int) (y / game.oneBlockSize);
    }

    public int getMapXRightSide() {
        return (int) Math.ceil((x + game.oneBlockSize - 1) / game.oneBlockSize);
    }

    public int getMapYRightSide() {
        return (int) Math.ceil((y + game.oneBlockSize - 1) / game.oneBlockSize);
    }


    public void draw(Graphics2D g2d) {
        g2d.drawImage(
            image,
            x,
            y,
            width,
            height,
            null
        );

        g2d.setColor(Color.RED);
        Ellipse2D rangeCircle = new Ellipse2D.Double(
            x + game.oneBlockSize / 2 - range * game.oneBlockSize,
            y + game.oneBlockSize / 2 - range * game.oneBlockSize,
            range * 2 * game.oneBlockSize,
            range * 2 * game.oneBlockSize
        );

        g2d.draw(rangeCircle);      
    }



    public static void updateGhosts(HashSet<Ghost> ghosts) {
        for (Ghost ghost : ghosts) {
            ghost.moveProcess();
        }
    }
}
