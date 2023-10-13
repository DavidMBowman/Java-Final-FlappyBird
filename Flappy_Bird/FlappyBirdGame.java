import java.util.ArrayList;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class FlappyBirdGame extends Application {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 600;
    private int score = 0;
    private boolean isGameOver = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Flappy Bird");
        Pane root = new Pane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);

        Bird bird = new Bird(WIDTH / 4, HEIGHT / 2, this);
        List<Pipe> pipes = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            pipes.add(new Pipe(WIDTH + i * 350, this)); // Create initial pipes with spacing
        }

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.clearRect(0, 0, WIDTH, HEIGHT);

                bird.update();
                bird.draw(gc);

                for (Pipe pipe : pipes) {
                    pipe.update();
                    pipe.draw(gc);

                    if (!isGameOver) {
                        if (bird.checkCollision(pipe)) {
                            isGameOver = true; // Bird collided with a pipe, game over
                        } else if (pipe.passedBird(bird)) {
                            score += 1; // Bird passed a pipe, increment score
                        }
                    }
                }

                if (bird.getY() > HEIGHT) {
                    isGameOver = true; // Bird fell off the screen, game over
                }

                if (isGameOver) {
                    drawGameOver(gc); // Display game over message
                }

                drawScore(gc); // Display the current score
            }
        };

        timer.start();

        scene.setOnMouseClicked(e -> {
            if (isGameOver) {
                restartGame(bird, pipes, gc); // Restart the game when clicking after game over
            } else {
                bird.jump(); // Bird jumps on mouse click
            }
        });

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE && !isGameOver) {
                bird.jump(); // Bird jumps when the spacebar is pressed
            } else if (event.getCode() == KeyCode.ENTER && isGameOver) {
                restartGame(bird, pipes, gc); // Restart the game on Enter key press
            }
        });

        primaryStage.show();
    }

    public int getHeight() {
        return HEIGHT;
    }

    public int getWidth() {
        return WIDTH;
    }

    private void drawScore(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillText("Score: " + score, 10, 20);
    }

    private void drawGameOver(GraphicsContext gc) {
        gc.setFill(Color.RED);
        gc.setFont(Font.font(48));
        gc.fillText("Game Over", WIDTH / 2 - 100, HEIGHT / 2 - 10);
        gc.setFont(Font.font(24));
        gc.fillText("Press Enter to Restart", WIDTH / 2 - 110, HEIGHT / 2 + 30);
    }

    private void restartGame(Bird bird, List<Pipe> pipes, GraphicsContext gc) {
        score = 0;
        isGameOver = false;
        bird.reset(WIDTH / 4, HEIGHT / 2); // Reset the bird's position
        pipes.clear(); // Clear existing pipes

        for (int i = 0; i < 2; i++) {
            pipes.add(new Pipe(WIDTH + i * 350, this)); // Create new pipes with spacing
        }

        gc.clearRect(0, 0, WIDTH, HEIGHT); // Clear the canvas
    }
}

// Abstract base class for game objects
abstract class GameObject {
    protected double x;
    protected double y;
    protected FlappyBirdGame game;

    public GameObject(double x, double y, FlappyBirdGame game) {
        this.x = x;
        this.y = y;
        this.game = game;
    }

    public abstract void update(); // Update the object's state
    public abstract void draw(GraphicsContext gc); // Draw the object on the canvas

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}

class Bird extends GameObject {
    private static final int BIRD_SIZE = 30;
    private static final double GRAVITY = 0.025; // Adjusted gravity for slower fall
    private double velocity;

    public Bird(double x, double y, FlappyBirdGame game) {
        super(x, y, game);
        this.velocity = 0;
    }

    @Override
    public void update() {
        velocity += GRAVITY;
        y += velocity;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.RED);
        gc.fillOval(x, y, BIRD_SIZE, BIRD_SIZE);
    }

    public void jump() {
        velocity = -2.5; // Adjusted jump height
    }

    public boolean checkCollision(Pipe pipe) {
        return x + BIRD_SIZE > pipe.getX() && x < pipe.getX() + Pipe.PIPE_WIDTH
                && (y < pipe.getY() || y + BIRD_SIZE > pipe.getY() + Pipe.PIPE_SPACING);
    }

    public void reset(double x, double y) {
        this.x = x;
        this.y = y;
        this.velocity = 0;
    }
}

class Pipe extends GameObject {
    public static final int PIPE_WIDTH = 100;
    public static final int PIPE_SPACING = 350; // Adjusted spacing
    public static final int PIPE_SPEED = 2;

    public Pipe(double x, FlappyBirdGame game) {
        super(x, 0, game);
        this.y = Math.random() * (game.getHeight() - PIPE_SPACING);
    }

    @Override
    public void update() {
        x -= PIPE_SPEED;
        if (x < -PIPE_WIDTH) {
            x = game.getWidth();
            y = Math.random() * (game.getHeight() - PIPE_SPACING);
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.GREEN);
        gc.fillRect(x, 0, PIPE_WIDTH, y);
        gc.fillRect(x, y + PIPE_SPACING, PIPE_WIDTH, game.getHeight() - y - PIPE_SPACING);
    }

    public boolean passedBird(Bird bird) {
        return x + PIPE_WIDTH < bird.getX();
    }

    public void reset(double x) {
        this.x = x;
        this.y = Math.random() * (game.getHeight() - PIPE_SPACING);
    }
}
