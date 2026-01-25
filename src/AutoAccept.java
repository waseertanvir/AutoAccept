import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Timer;
import java.util.TimerTask;

/**
 * TODO(Tanvir Waseer): Use SLF4J for logging instead of system output stream.
 */
public class AutoAccept extends JFrame {
    public static final int NUMBER_OF_CLICKS = 5;
    public final int BUTTON_WIDTH = 100;
    public final int BUTTON_HEIGHT = 35;
    private static JButton startButton;
    private static JButton stopButton;
    private Timer screenshotTimer;
    private Robot robot;
    private Mat templateImage;
    private static final double MATCH_THRESHOLD = 0.8;
    private static final String TEMPLATE_IMAGE_PATH = "cs2_accept_button.png";

    static {
        nu.pattern.OpenCV.loadLocally();
    }

    public AutoAccept() {
        setTitle("CS2 Matchmaking Auto-Accept");
        setSize(400, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        try {
            robot = new Robot();
        } catch (final AWTException e) {
            e.printStackTrace();
            System.exit(1);
        }

        loadAcceptButtonImage();

        JLabel messageLabel = new JLabel(
                "<html><div style='text-align: center;'>" +
                        "Automatically accept Counter-Strike 2 competitive matchmaking.<br>" +
                        "No more waiting or missing the accept button." +
                        "</div></html>"
        );
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        add(messageLabel, BorderLayout.CENTER);

        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        startButton = new JButton("Start");
        startButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        startButton.addActionListener(e -> handleStart());

        stopButton = new JButton("Stop");
        stopButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> handleStop());

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadAcceptButtonImage() {
        templateImage = Imgcodecs.imread(TEMPLATE_IMAGE_PATH);
        if (templateImage.empty()) {
            JOptionPane.showMessageDialog(this,
                    "Template image not found: " + TEMPLATE_IMAGE_PATH,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            System.exit(0);
        }
    }

    private void handleStart() {
        System.out.println("Auto-accept started");

        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        screenshotTimer = new Timer();
        screenshotTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkForButton();
            }
        }, 0, 5000);
    }

    private void handleStop() {
        System.out.println("Auto-accept stopped");

        startButton.setEnabled(true);
        stopButton.setEnabled(false);

        if (screenshotTimer != null) {
            screenshotTimer.cancel();
            screenshotTimer = null;
        }
    }

    private void checkForButton() {
        try {
            // Capture screenshot
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenshot = robot.createScreenCapture(screenRect);

            // Save screenshot temporarily
            File tempFile = new File("temp_screenshot.png");
            ImageIO.write(screenshot, "png", tempFile);

            // Load screenshot as OpenCV Mat
            Mat screenMat = Imgcodecs.imread(tempFile.getAbsolutePath());

            // Perform template matching
            Point matchLocation = findTemplate(screenMat, templateImage);

            if (matchLocation != null) {
                int clickX = matchLocation.x + templateImage.cols() / 2;
                int clickY = matchLocation.y + templateImage.rows() / 2;

                robot.mouseMove(clickX, clickY);

                for (int i = 0; i < NUMBER_OF_CLICKS; i++) {
                    robot.delay(100);
                    robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
                    robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
                }

                System.out.println("Button clicked at: " + clickX + ", " + clickY);
            }

            tempFile.delete();
            screenMat.release();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private Point findTemplate(final Mat source, final Mat template) {
        final Mat result = new Mat();

        Imgproc.matchTemplate(source, template, result, Imgproc.TM_CCOEFF_NORMED);

        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

        result.release();

        // Check if match is good enough
        if (mmr.maxVal >= MATCH_THRESHOLD) {
            final int x = (int) mmr.maxLoc.x;
            final int y = (int) mmr.maxLoc.y;
            return new Point(x, y);
        }

        return null;
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            AutoAccept gui = new AutoAccept();
            gui.setVisible(true);
        });
    }
}