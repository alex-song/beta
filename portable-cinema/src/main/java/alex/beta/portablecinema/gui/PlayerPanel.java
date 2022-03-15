package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.pojo.FileInfo;
import alex.beta.portablecinema.video.Player;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class PlayerPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(PlayerPanel.class);
    private static final int SMALL_STEP = 20;
    private static final int BIG_STEP = 120;
    private static final String SLIDER_TOOLTIP_TEMPLATE = "<html>A - 退后%s秒<br/>W - 退后%s秒<br/>D - 前进%s秒<br/>S - 前进%s秒</html>";
    private final PortableCinemaConfig config;
    BufferedImage screenshot;
    private Player player;
    private FileInfo fileInfo;
    private JLabel screenshotLabel;
    private JFormattedTextField timeField;
    private JButton jumpToBtn;
    private JButton captureBtn;

    public PlayerPanel(PortableCinemaConfig config, FileInfo fileInfo, int width, int height) {
        super(new BorderLayout());
        this.config = config;
        this.fileInfo = fileInfo;
        this.setSize(width, height);
        this.setPreferredSize(new Dimension(width, height));
        this.setBorder(null);
        createUIComponents();
        new PlayerWorker(0).execute();

        if (fileInfo != null) {
            registerKeyboardAction(ae -> fastForward(SMALL_STEP), KeyStroke.getKeyStroke("D"), JComponent.WHEN_IN_FOCUSED_WINDOW);
            registerKeyboardAction(ae -> fastForward(BIG_STEP), KeyStroke.getKeyStroke("S"), JComponent.WHEN_IN_FOCUSED_WINDOW);
            registerKeyboardAction(ae -> fastForward(-1 * SMALL_STEP), KeyStroke.getKeyStroke("A"), JComponent.WHEN_IN_FOCUSED_WINDOW);
            registerKeyboardAction(ae -> fastForward(-1 * BIG_STEP), KeyStroke.getKeyStroke("W"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
    }

    private static String getFormattedDuration(int duration) {
        if (duration < 60) {
            return "00:00:" + getString(getDurationSecondsPart(duration));
        } else if (duration < 3600) {
            return "00:" + getString(getDurationMinsPart(duration)) + ":" + getString((getDurationSecondsPart(duration)));
        } else {
            return getString(getDurationHoursPart(duration)) + ":" + getString(getDurationMinsPart(duration)) + ":" + getString((getDurationSecondsPart(duration)));
        }
    }

    private static String getString(int t) {
        if (t < 0) {
            return "00";
        } else if (t < 10) {
            return "0" + t;
        } else
            return String.valueOf(t);
    }

    private static int getDurationHoursPart(int duration) {
        return duration / 3600;
    }

    private static int getDurationMinsPart(int duration) {
        return (duration % 3600) / 60;
    }

    private static int getDurationSecondsPart(int duration) {
        return duration % 60;
    }

    private static KeyListener newEnterKeyListener(@NonNull JButton clickBtn) {
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

    private void fastForward(int delta) {
        if (fileInfo != null) {
            int currentSeconds = normalizeTimeFieldValue();
            int newSeconds = currentSeconds + delta;
            newSeconds = (int) Math.max(Math.min(newSeconds, fileInfo.getDuration()), 0);
            if (newSeconds != currentSeconds) {
                timeField.setText(getFormattedDuration(newSeconds));
                if (jumpToBtn.isEnabled())
                    jumpToBtn.doClick();
            }
        }
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
        JSlider slider = new JSlider();
        slider.setMinimum(0);
        if (fileInfo != null && fileInfo.getDuration() < Integer.MAX_VALUE)
            slider.setMaximum((int) fileInfo.getDuration());
        slider.setValue(0);
        slider.setMajorTickSpacing(3600);
        slider.setMinorTickSpacing(600);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        if (fileInfo != null)
            slider.setToolTipText(String.format(SLIDER_TOOLTIP_TEMPLATE, SMALL_STEP, BIG_STEP, SMALL_STEP, BIG_STEP));
        Hashtable<Integer, JLabel> position = new Hashtable<>();
        position.put(0, new JLabel("0:00:00"));
        position.put(3600, new JLabel("1:00:00"));
        position.put(2 * 3600, new JLabel("2:00:00"));
        position.put(3 * 3600, new JLabel("3:00:00"));
        position.put(4 * 3600, new JLabel("4:00:00"));
        position.put(5 * 3600, new JLabel("5:00:00"));
        position.put(6 * 3600, new JLabel("6:00:00"));
        slider.setLabelTable(position);

        slider.addChangeListener(e -> {
            if (!slider.getValueIsAdjusting()) {
                timeField.setText(getFormattedDuration(slider.getValue()));
                new PlayerWorker(slider.getValue()).execute();
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

        jumpToBtn = new JButton("跳转");
        jumpToBtn.addActionListener(e -> slider.setValue(normalizeTimeFieldValue()));
        jumpToBtn.setEnabled(fileInfo != null && fileInfo.getDuration() > 0);
        gbc.gridx = 2;
        gbc.gridy = 0;
        controlPanel.add(jumpToBtn, gbc);

        captureBtn = new JButton("保存截图");
        captureBtn.setEnabled(fileInfo != null && fileInfo.getDuration() > 0);
        captureBtn.addActionListener(e -> {
            if (fileInfo != null && screenshot != null) {
                File folder = new File(fileInfo.getPath());
                long timestamp = -1;
                try {
                    int dotIndex = fileInfo.getName().lastIndexOf('.');
                    String videoFileName = (dotIndex < 0 ? fileInfo.getName() : fileInfo.getName().substring(0, dotIndex));
                    if (isBlank(videoFileName)) videoFileName = "pc-screenshot";
                    File screenshotFile = new File(folder, videoFileName + "-" + timeField.getText().replace(":", "") + ".png");
                    if (screenshotFile.exists() && JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(this,
                            "文件已存在，要覆盖吗？\n" + screenshotFile.getCanonicalPath(), "保存截图", JOptionPane.YES_NO_OPTION)) {
                        return;
                    }
                    timestamp = folder.lastModified();
                    ImageIO.write(screenshot, "png", screenshotFile);
                    if (logger.isInfoEnabled())
                        logger.info("Screenshot is saved, {}", screenshotFile.getCanonicalPath());
                } catch (Exception ex) {
                    logger.error("Fail to save the screenshot", ex);
                    JOptionPane.showMessageDialog(this, "保存截图失败", "保存截图", JOptionPane.INFORMATION_MESSAGE, null);
                } finally {
                    if (timestamp > -1) folder.setLastModified(timestamp);
                }
            }
        });
        gbc.gridx = 3;
        gbc.gridy = 0;
        controlPanel.add(captureBtn, gbc);

        add(controlPanel, BorderLayout.SOUTH);
        add(new JScrollPane(screenshotLabel), BorderLayout.CENTER);

        timeField.addKeyListener(newEnterKeyListener(jumpToBtn));
        jumpToBtn.addKeyListener(newEnterKeyListener(jumpToBtn));
    }

    private int normalizeTimeFieldValue() {
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
        return (int) Math.max(Math.min(FileInfo.toSeconds(h, m, s), fileInfo.getDuration()), 0);
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
                screenshotLabel.setIcon(new StretchIcon(screenshot, true));
                captureBtn.setEnabled(screenshot != null);
            }
        }
    }
}
