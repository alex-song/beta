package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.pojo.FileInfo;
import alex.beta.portablecinema.video.Player;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import static java.awt.Image.SCALE_SMOOTH;

public class PlayerPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(PlayerPanel.class);

    private final PortableCinemaConfig config;

    private FileInfo fileInfo;

    private JLabel screenshotLabel;

    BufferedImage screenshot;

    private JSlider slider;

    private JFormattedTextField timeField;

    private Player player;

    public PlayerPanel(PortableCinemaConfig config, FileInfo fileInfo, int width, int height) {
        super(new BorderLayout());
        this.config = config;
        this.fileInfo = fileInfo;
        this.setSize(width, height);
        this.setPreferredSize(new Dimension(width, height));
        this.setBorder(null);
        createUIComponents();
        new PlayerWorker(0).execute();
    }

    public void close() {
        try {
            if (screenshot != null && screenshot.getGraphics() != null) {
                screenshot.getGraphics().dispose();
                screenshot = null;
            }
            if (player != null) {
                player.close();
                player = null;
            }
        } catch (Exception ex) {
            logger.warn("Error when closing PlayerPanel", ex);
        }
    }

    private void createUIComponents() {
        screenshotLabel = new JLabel();
        screenshotLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        screenshotLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        screenshotLabel.setHorizontalAlignment(SwingConstants.CENTER);
        screenshotLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel controlPanel = new JPanel(new GridBagLayout());
        slider = new JSlider();
        slider.setMinimum(0);
        if (fileInfo != null && fileInfo.getDuration() < Integer.MAX_VALUE)
            slider.setMaximum((int) fileInfo.getDuration());
        slider.setValue(0);
        slider.setMajorTickSpacing(3600);
        slider.setMinorTickSpacing(600);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        Hashtable<Integer, JLabel> position = new Hashtable<>();
        position.put(0, new JLabel("0:00:00"));
        position.put(3600, new JLabel("1:00:00"));
        position.put(2 * 3600, new JLabel("2:00:00"));
        position.put(3 * 3600, new JLabel("3:00:00"));
        position.put(4 * 3600, new JLabel("4:00:00"));
        position.put(5 * 3600, new JLabel("5:00:00"));
        position.put(6 * 3600, new JLabel("6:00:00"));
        slider.setLabelTable(position);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (!slider.getValueIsAdjusting()) {
                    timeField.setText(getFormattedDuration(slider.getValue()));
                    new PlayerWorker(slider.getValue()).execute();
                }
            }

            private String getFormattedDuration(int duration) {
                if (duration < 60) {
                    return "00:00:" + getString(getDurationSecondsPart(duration));
                } else if (duration < 3600) {
                    return "00:" + getString(getDurationMinsPart(duration)) + ":" + getString((getDurationSecondsPart(duration)));
                } else {
                    return getString(getDurationHoursPart(duration)) + ":" + getString(getDurationMinsPart(duration)) + ":" + getString((getDurationSecondsPart(duration)));
                }
            }

            private String getString(int t) {
                if (t < 0) {
                    return "00";
                } else if (t < 10) {
                    return "0" + t;
                } else
                    return String.valueOf(t);
            }

            private int getDurationHoursPart(int duration) {
                return duration / 3600;
            }

            private int getDurationMinsPart(int duration) {
                return (duration % 3600) / 60;
            }

            private int getDurationSecondsPart(int duration) {
                return duration % 60;
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(slider, gbc);

        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter("##:##:##") {
                @Override
                public boolean getAllowsInvalid() {
                    return false;
                }

                @Override
                public boolean getCommitsOnValidEdit() {
                    /**
                     * If you want the value to be committed on each keystroke instead of focus lost
                     */
                    return true;
                }

                @Override
                public char getPlaceholderCharacter() {
                    return '0';
                }
            };
        } catch (ParseException pe) {
            //
        }
        timeField = new JFormattedTextField(formatter);
        timeField.setPreferredSize(new Dimension(80, 24));
        timeField.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        controlPanel.add(timeField, gbc);

        JButton jumpToBtn = new JButton("跳转");
        jumpToBtn.addActionListener(e -> {
            StringTokenizer st = new StringTokenizer(timeField.getText(), ":");
            int h = 0;
            int m = 0;
            int s = 0;
            if (st.countTokens() == 3) {
                h = Integer.parseInt(st.nextToken().trim());
                m = Integer.parseInt(st.nextToken().trim());
                s = Integer.parseInt(st.nextToken().trim());
            } else if (st.countTokens() == 2) {
                m = Integer.parseInt(st.nextToken().trim());
                s = Integer.parseInt(st.nextToken().trim());
            } else if (st.countTokens() == 1) {
                s = Integer.parseInt(st.nextToken().trim());
            }
            slider.setValue((int) FileInfo.toSeconds(h, m, s));
        });
        gbc.gridx = 2;
        gbc.gridy = 0;
        controlPanel.add(jumpToBtn, gbc);

        add(controlPanel, BorderLayout.SOUTH);
        add(new JScrollPane(screenshotLabel), BorderLayout.CENTER);

        timeField.addKeyListener(newEnterKeyListener(jumpToBtn));
        jumpToBtn.addKeyListener(newEnterKeyListener(jumpToBtn));
    }

    public synchronized Player getPlayer() {
        if (player == null) {
            try {
                player = Player.getInstance(config, fileInfo).read();
            } catch (IOException ioe) {
                logger.error("Failed to read video [{}/{}]", fileInfo.getPath(), fileInfo.getName(), ioe);
                return null;
            }
        }
        return player;
    }

    private KeyListener newEnterKeyListener(@NonNull JButton clickBtn) {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (clickBtn.isEnabled())
                        clickBtn.doClick();
                    e.consume();
                } else
                    super.keyPressed(e);
            }
        };
    }

    String getCurrentImg() {
        return this.timeField.getText();
    }

    byte[] getCurrentImageData() {
        if (screenshot == null) {
            return null;
        } else {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(screenshot, "png", baos);
                baos.flush();
                return baos.toByteArray();
            } catch (IOException ex) {
                logger.error("Failed to convert screenshot to png", ex);
                return null;
            }
        }
    }

    public class PlayerWorker extends SwingWorker<Void, BufferedImage> {
        private final int seconds;

        public PlayerWorker(int seconds) {
            this.seconds = seconds;
        }

        @Override
        protected Void doInBackground() {
            getPlayer();
            BufferedImage bi;
            if (player != null && (bi = player.captureScreen(seconds)) != null) {
                publish(bi);
            }
            return null;
        }

        @Override
        protected void process(List<BufferedImage> chunks) {
            if (chunks != null && !chunks.isEmpty()) {
                // dispose previous image
                if (screenshot != null && screenshot.getGraphics() != null) {
                    screenshot.getGraphics().dispose();
                }
                // publish new
                screenshot = chunks.get(chunks.size() - 1);
                screenshotLabel.setIcon(new ImageIcon(fitScreen(screenshot)));
            }
        }

        private Image fitScreen(BufferedImage image) {
            int screenWidth = getWidth() - 20;
            int screenHeight = getHeight() - 60;
            boolean respectWidth = ((1.0 * image.getWidth() / image.getHeight()) > (1.0 * screenWidth / screenHeight));
            if (image.getWidth() <= screenWidth && image.getHeight() <= screenHeight) {
                return image;
            } else if (respectWidth) {
                return image.getScaledInstance(screenWidth, -1, SCALE_SMOOTH);
            } else {
                return image.getScaledInstance(-1, screenHeight, SCALE_SMOOTH);
            }
        }
    }
}
