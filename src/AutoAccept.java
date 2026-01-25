import javax.swing.*;
import java.awt.*;

public class AutoAccept extends JFrame {
    public final int BUTTON_WIDTH = 100;
    public final int BUTTON_HEIGHT = 35;
    private final JButton startButton;
    private final JButton stopButton;
    private static final double MATCH_THRESHOLD = 0.8;
    private static final String TEMPLATE_IMAGE_PATH = "accept_button.png"; // Your button image

    static {
        nu.pattern.OpenCV.loadLocally();
    }

    public AutoAccept() {
        setTitle("CS2 Matchmaking Auto-Accept");
        setSize(400, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

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

    private void handleStart() {
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        // Add your auto-accept logic here
        System.out.println("Auto-accept started");
    }

    private void handleStop() {
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        // Stop auto-accept logic here
        System.out.println("Auto-accept stopped");
    }

    static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            AutoAccept gui = new AutoAccept();
            gui.setVisible(true);
        });
    }
}