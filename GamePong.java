// Importing certain Java librarires that help us run this game
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;

public class GamePong extends JPanel implements ActionListener, KeyListener {

    
    final int WIDTH = 800, HEIGHT = 600;
    final int BASE_PADDLE_WIDTH = 15, BASE_PADDLE_HEIGHT = 70;
    // game window and paddle size
    
    int paddleWidth = BASE_PADDLE_WIDTH, paddleHeight = BASE_PADDLE_HEIGHT;
    int ballSize = 40;
    int playerScore = 0, opponentScore = 0;
   // game state varibales

    int ballX, ballY, ballDX = 8, ballDY = 8;
    int ball2X, ball2Y, ball2DX, ball2DY;

    // ball speeds

    int playerY = HEIGHT / 2 - paddleHeight / 2;
    int opponentY = HEIGHT / 2 - paddleHeight / 2;
    // player positions


    boolean upPressed = false, downPressed = false;
    boolean wPressed = false, sPressed = false;
    boolean paused = false, inMenu = true;
    int paddleSpeed = 10;
    boolean multiBallActive = false;

    Timer timer;
    Random rand = new Random();
   // randomness for the certain powerups

   // for the paddles and game skins
    ArrayList<Image> paddleSkins = new ArrayList<>();
    ArrayList<String> paddleNames = new ArrayList<>();
    Image[] ballSkins;
    String[] ballNames = { "Iceball", "Slimeball", "SoccerBall", "CaptainAmerica" };

    // tracking the skin selections
    int paddleIndexP1 = 0;
    int paddleIndexP2 = 0;
    int currentBallIndex = 0;

    boolean hasObstacle = false;
    Rectangle obstacle;
    // obstacle

    // sets up the panel and starts the game
    public GamePong() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        loadAssets();
        timer = new Timer(1000 / 60, this);
        timer.start();
        resetBall();
    }
// puts the paddles and balls into memory
    final void loadAssets() {
        // makes solid coloures you can use
        Color[] colors = { Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.ORANGE, Color.CYAN, Color.PINK, Color.LIGHT_GRAY, Color.WHITE };
        for (Color color : colors) {
            BufferedImage img = new BufferedImage(20, 100, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(color);
            g.fillRect(0, 0, 20, 100);
            g.dispose();
            paddleSkins.add(img);
            paddleNames.add("Default");
        }
        // loading the custom paddles and ball skins we made
        try {
            addCustomPaddle("RedLightSaber_paddle.png", "Red Saber", true);
            addCustomPaddle("GreenLightSaber_paddle.png", "Green Saber", true);
            addCustomPaddle("MagentaLightSaber_paddle.png", "Magenta Saber", true);
            addCustomPaddle("TealLightSaber_paddle.png", "Teal Saber", true);
            addCustomPaddle("OrangeLightSaber_paddle.png", "Orange Saber", true);
            addCustomPaddle("WhiteLightSaber_paddle.png", "White Saber", true);
            addCustomPaddle("YellowLightSaber_paddle.png", "Yellow Saber", true);
            addCustomPaddle("PurpleLightSaber_paddle.png", "Purple Saber", true);
            addCustomPaddle("DarkblueLightSaber_paddle.png", "Darkblue Saber", true);

            ballSkins = new Image[] {
                loadCircular("assets/Iceball_ball.png"),
                loadCircular("assets/Slimeball.png"),
                loadCircular("assets/SoccerBall_ball.png"),
                loadCircular("assets/CaptainAmerica_paddle.png")
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // cropping the images of the paddles and the balls to ensure that they are the propter size for the game
    void addCustomPaddle(String fileName, String name, boolean rotate) throws IOException {
        BufferedImage img = ImageIO.read(new File("assets/" + fileName));
        int cropX = img.getWidth() / 4;
        int cropY = img.getHeight() / 5;
        int cropWidth = img.getWidth() / 2;
        int cropHeight = Math.min(paddleHeight, img.getHeight() - cropY);
        if (cropX + cropWidth > img.getWidth()) cropWidth = img.getWidth() - cropX;
        if (cropY + cropHeight > img.getHeight()) cropHeight = img.getHeight() - cropY;
        if (cropWidth <= 0 || cropHeight <= 0) return;
        BufferedImage cropped = img.getSubimage(cropX, cropY, cropWidth, cropHeight);
        if (rotate) cropped = rotateImage(cropped, Math.PI / 2);
        Image scaled = cropped.getScaledInstance(paddleWidth, paddleHeight, Image.SCALE_SMOOTH);
        paddleSkins.add(scaled);
        paddleNames.add(name);
    }
  // rotating the paddle so its in the proper orientation
    BufferedImage rotateImage(BufferedImage src, double angle) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage result = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate(h / 2.0, w / 2.0);
        at.rotate(angle);
        at.translate(-w / 2.0, -h / 2.0);
        g2.drawImage(src, at, null);
        g2.dispose();
        return result;
    }
    // making the ball images into a circle
    Image loadCircular(String path) throws IOException {
        BufferedImage img = ImageIO.read(new File(path));
        BufferedImage circle = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circle.createGraphics();
        g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, img.getWidth(), img.getHeight()));
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        return circle;
    }
    // resetting the ball and adding all of the random potential powerups/effetcs
        void resetBall() {
        currentBallIndex = rand.nextInt(ballSkins.length);
        ballX = WIDTH / 2 - ballSize / 2;
        ballY = HEIGHT / 2 - ballSize / 2;

        // speed of ball varies by skin here
        switch (ballNames[currentBallIndex]) {
            case "Slimeball":
                ballDX = 13 * (rand.nextBoolean() ? 1 : -1);
                ballDY = 13 * (rand.nextBoolean() ? 1 : -1);
                break;
            case "Iceball":
                ballDX = 8 * (rand.nextBoolean() ? 1 : -1);
                ballDY = 8 * (rand.nextBoolean() ? 1 : -1);
                break;
            default:
                ballDX = 10 * (rand.nextBoolean() ? 1 : -1);
                ballDY = 10 * (rand.nextBoolean() ? 1 : -1);
        }
       // second ball for mulitball mode
        ball2X = ballX;
        ball2Y = ballY;
        ball2DX = -ballDX;
        ball2DY = -ballDY;

        // random obstacle will occur
        hasObstacle = rand.nextInt(100) < 25;
        if (hasObstacle) {
            int obsWidth = 30, obsHeight = 120;
            int x = WIDTH / 2 - obsWidth / 2;
            int y = rand.nextInt(HEIGHT - obsHeight);
            obstacle = new Rectangle(x, y, obsWidth, obsHeight);
        }
        // making the specific random chance of powerup happening after a goal
        int chance = rand.nextInt(100);
        if (chance < 50) {
            int effect = rand.nextInt(3);
            ballSize = (effect == 0) ? 60 : (effect == 1 ? 20 : 40);
            multiBallActive = (effect == 2);
        } else {
            ballSize = 40;
            multiBallActive = false;
        }
    }
     //drawing
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (inMenu) {
            // the menu screen 
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Press ENTER to Start", 260, 180);
            g.drawString("Press 1 to switch Player 1 skin", 230, 220);
            g.drawString("Press 2 to switch Player 2 skin", 230, 260);
           
            return;
        }

        if (paused) {
            // the pause screen
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Paused", WIDTH / 2 - 70, HEIGHT / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.setColor(Color.WHITE);
           
            return;
        }
        // drawing the paddles and the skins for the game
        g.drawImage(paddleSkins.get(paddleIndexP1), WIDTH - paddleWidth, playerY, paddleWidth, paddleHeight, null);
        g.drawImage(paddleSkins.get(paddleIndexP2), 0, opponentY, paddleWidth, paddleHeight, null);
        g.drawImage(ballSkins[currentBallIndex], ballX, ballY, ballSize, ballSize, null);
       // multiball drawing
        if (multiBallActive)
            g.drawImage(ballSkins[currentBallIndex], ball2X, ball2Y, ballSize, ballSize, null);
        
        // obstacle drawing
            if (hasObstacle && obstacle != null) {
            g.setColor(Color.GRAY);
            g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
        }
        // the middle line
        g.setColor(Color.WHITE);
        for (int i = 0; i < HEIGHT; i += 30)
            g.fillRect(WIDTH / 2 - 1, i, 2, 15);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString(String.valueOf(opponentScore), WIDTH / 2 - 60, 50);
        g.drawString(String.valueOf(playerScore), WIDTH / 2 + 30, 50);
    }
        // Main game loop: update positions, handle collisions, and check scoring
    public void actionPerformed(ActionEvent e) {
        if (paused || inMenu) return;
        // moving the paddles
        if (upPressed && playerY > 0) playerY -= paddleSpeed;
        if (downPressed && playerY + paddleHeight < HEIGHT) playerY += paddleSpeed;
        if (wPressed && opponentY > 0) opponentY -= paddleSpeed;
        if (sPressed && opponentY + paddleHeight < HEIGHT) opponentY += paddleSpeed;
        // moving the balls
        ballX += ballDX;
        ballY += ballDY;
        if (multiBallActive) {
            ball2X += ball2DX;
            ball2Y += ball2DY;
        }
        //hitboxes
        Rectangle ball = new Rectangle(ballX, ballY, ballSize, ballSize);
        Rectangle ball2 = new Rectangle(ball2X, ball2Y, ballSize, ballSize);
        Rectangle player = new Rectangle(WIDTH - paddleWidth, playerY, paddleWidth, paddleHeight);
        Rectangle opponent = new Rectangle(0, opponentY, paddleWidth, paddleHeight);

        // boucning off the top and the bottom of the screen
        if (ballY <= 0 || ballY + ballSize >= HEIGHT) ballDY *= -1;
        if (ball.intersects(player) && ballDX > 0) ballDX *= -1;
        if (ball.intersects(opponent) && ballDX < 0) ballDX *= -1;

        if (multiBallActive) {
            if (ball2Y <= 0 || ball2Y + ballSize >= HEIGHT) ball2DY *= -1;
            if (ball2.intersects(player) && ball2DX > 0) ball2DX *= -1;
            if (ball2.intersects(opponent) && ball2DX < 0) ball2DX *= -1;
        }
        // obstacle collisions
        if (hasObstacle && obstacle != null && ball.intersects(obstacle)) ballDX *= -1;
        if (multiBallActive && hasObstacle && obstacle != null && ball2.intersects(obstacle)) ball2DX *= -1;
        // scoring
        if (ballX <= 0 || ball2X <= 0) { playerScore++; resetBall(); }
        else if (ballX + ballSize >= WIDTH || ball2X + ballSize >= WIDTH) { opponentScore++; resetBall(); }

        repaint();
    }
    // method for our key presses
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_R) resetBall();
        if (code == KeyEvent.VK_UP) upPressed = true;
        if (code == KeyEvent.VK_DOWN) downPressed = true;
        if (code == KeyEvent.VK_W) wPressed = true;
        if (code == KeyEvent.VK_S) sPressed = true;
        if (code == KeyEvent.VK_P) paused = !paused;
        if (code == KeyEvent.VK_ENTER && inMenu) inMenu = false;
        if ((!inMenu || paused) && code == KeyEvent.VK_1) paddleIndexP1 = (paddleIndexP1 + 1) % paddleSkins.size();
        if ((!inMenu || paused) && code == KeyEvent.VK_2) paddleIndexP2 = (paddleIndexP2 + 1) % paddleSkins.size();
    }
// method for our key released
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP) upPressed = false;
        if (code == KeyEvent.VK_DOWN) downPressed = false;
        if (code == KeyEvent.VK_W) wPressed = false;
        if (code == KeyEvent.VK_S) sPressed = false;
    }

    public void keyTyped(KeyEvent e) {}

// being able to properly launch the game
    public static void main(String[] args) {
        JFrame frame = new JFrame("Pong Game");
        GamePong game = new GamePong();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(game);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
