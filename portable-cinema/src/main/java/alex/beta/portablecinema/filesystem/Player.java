package alex.beta.portablecinema.filesystem;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.pojo.FileInfo;
import com.google.common.io.Files;
import com.xuggle.ferry.JNIMemoryManager;
import com.xuggle.xuggler.*;
import lombok.NonNull;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class Player implements AutoCloseable {

    public static final Pattern SCREENSHOT_NAME_PATTERN = Pattern.compile(".*-\\d{6}$");

    private static final Logger logger = LoggerFactory.getLogger(Player.class);

    private final PortableCinemaConfig config;
    private FileInfo fileInfo;
    private RandomAccessFile videoFile;
    private IContainer container;
    private int videoStreamId = -1;
    private IStreamCoder videoCoder;

    private Player(PortableCinemaConfig config, FileInfo fileInfo) {
        this.config = config;
        this.fileInfo = fileInfo;
    }

    public static Player getInstance(@NonNull PortableCinemaConfig config, @NonNull FileInfo fileInfo) {
        return new Player(config, fileInfo);
    }

    private static String getFormattedSeconds(int seconds) {
        if (seconds < 0 || seconds > 99 * 3600 + 59 * 60 + 59) { // oversize
            return null;
        } else if (seconds < 60) {
            return "0000" + getString(getDurationSecondsPart(seconds));
        } else if (seconds < 3600) {
            return "00" + getString(getDurationMinsPart(seconds)) + getString((getDurationSecondsPart(seconds)));
        } else {
            return getString(getDurationHoursPart(seconds)) + getString(getDurationMinsPart(seconds)) + getString((getDurationSecondsPart(seconds)));
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

    public synchronized Player read() throws IOException {
        container = IContainer.make();
        videoFile = new RandomAccessFile(new File(fileInfo.getPath(), fileInfo.getName()), "r");
        if (container.open(videoFile, IContainer.Type.READ, null) >= 0) {
            int numOfStreams = container.getNumStreams();
            for (int i = 0; i < numOfStreams; i++) {
                IStreamCoder coder = container.getStream(i).getStreamCoder();
                if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                    videoStreamId = i;
                    videoCoder = coder;
                    break;
                } else {
                    coder.close();
                }
            }
        } else {
            logger.warn("Failed to open video file [{}/{}]", fileInfo.getPath(), fileInfo.getName());
        }
        return this;
    }

    /**
     * Caution: This method is ONLY for FileScan
     *
     * @return
     */
    public synchronized boolean isDecodable() {
        if (container == null) {
            throw new UnsupportedOperationException("Container is not initialized, do read() first.");
        }
        try {
            fileInfo.setDecodeError(videoCoder == null || videoCoder.open(null, null) < 0);
            return fileInfo.isDecodeError();
        } finally {
            if (videoCoder != null) {
                videoCoder.close();
            }
        }
    }

    /**
     * overwrite = false
     *
     * @return
     */
    public FileInfo.Resolution getResolution() {
        return this.getResolution(false);
    }

    /**
     * @param overwrite Overwrite the resolution in file info, if this flag is true and video is able to decode
     * @return Resolution of given video file
     */
    public FileInfo.Resolution getResolution(boolean overwrite) {
        if (videoCoder != null) {
            if (overwrite) {
                fileInfo.setResolution(new FileInfo.Resolution(videoCoder.getWidth(), videoCoder.getHeight()));
                return fileInfo.getResolution();
            } else {
                return new FileInfo.Resolution(videoCoder.getWidth(), videoCoder.getHeight());
            }
        } else {
            if (overwrite && fileInfo.getResolution() == null) {
                fileInfo.setResolution(new FileInfo.Resolution(0, 0));
            }
            return fileInfo.getResolution();
        }
    }

    /**
     * overwrite = false
     *
     * @return
     */
    public long getDuration() {
        return this.getDuration(false);
    }

    /**
     * @param overwrite Overwrite the duration in file info, if this flag is true and video is able to decode
     * @return Duration (in seconds) of given video file
     */
    public long getDuration(boolean overwrite) {
        if (videoCoder != null) {
            if (overwrite) {
                fileInfo.setDuration(container.getDuration() / 1000 / 1000);
                return fileInfo.getDuration();
            } else {
                return container.getDuration() / 1000 / 1000;
            }
        } else {
            // Cannot decode video file, and return the duration from file info
            return fileInfo.getDuration();
        }
    }

    /**
     * @param seconds
     * @return Screenshot at given timestamp
     * null, if the video is not decodable, or cannot capture screen
     */
    @SuppressWarnings("deprecation")
    public synchronized BufferedImage captureScreen(int seconds) {
        if (videoCoder == null || videoCoder.open(null, null) < 0) {
            logger.warn("Cannot open video decoder for container: {}/{}", fileInfo.getPath(), fileInfo.getName());
            return null;
        }
        try {
            BufferedImage screenshot = null;
            IVideoResampler resampler = null;
            if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {
                resampler = IVideoResampler.make(videoCoder.getWidth(),
                        videoCoder.getHeight(),
                        IPixelFormat.Type.BGR24,
                        videoCoder.getWidth(),
                        videoCoder.getHeight(),
                        videoCoder.getPixelType());
                if (resampler == null) {
                    logger.error("Cannot create color space resampler for [{}/{}]", fileInfo.getPath(), fileInfo.getName());
                    return null;
                }
            }
            IPacket packet = IPacket.make();
            IRational timeBase = container.getStream(videoStreamId).getTimeBase();
            long timeStampOffset = ((long) timeBase.getDenominator() / timeBase.getNumerator()) * seconds;
            long target = container.getStartTime() + timeStampOffset;
            container.seekKeyFrame(videoStreamId, target, 0);
            boolean isFinished = false;
            while (container.readNextPacket(packet) >= 0 && !isFinished) {
                if (packet.getStreamIndex() == videoStreamId) {
                    IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(), videoCoder.getWidth(), videoCoder.getHeight());
                    int offset = 0;
                    while (!isFinished && offset < packet.getSize()) {
                        int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
                        if (bytesDecoded < 0) {
                            logger.warn("Get no data decoding video in one packet");
                            return null;
                        }
                        offset += bytesDecoded;
                        if (picture.isComplete()) {
                            IVideoPicture newPic = picture;
                            if (resampler != null) {
                                newPic = IVideoPicture.make(resampler.getOutputPixelFormat(), picture.getWidth(), picture.getHeight());
                                if (resampler.resample(newPic, picture) < 0) {
                                    logger.error("Cannot resample video from [{}/{}]", fileInfo.getPath(), fileInfo.getName());
                                    return null;
                                }
                            }
                            if (newPic.getPixelType() != IPixelFormat.Type.BGR24) {
                                logger.error("Cannot decode video as BGR 24 bit data in [{}/{}]", fileInfo.getPath(), fileInfo.getName());
                                return null;
                            }
                            screenshot = Utils.videoPictureToImage(newPic);
                            isFinished = true;
                        }
                    }
                }
            }
            return screenshot;
        } finally {
            videoCoder.close();
        }
    }

    @Override
    public synchronized void close() {
        try {
            if (videoCoder != null) {
                videoCoder.close();
                videoCoder = null;
            }
            if (container != null) {
                container.close();
                container = null;
            }
            if (videoFile != null) {
                videoFile.close();
                videoFile = null;
            }
            if (JNIMemoryManager.getMgr() != null)
                JNIMemoryManager.getMgr().gc();
        } catch (Exception ex) {
            logger.error("Error when closing player", ex);
        }
    }

    public boolean saveImage(RenderedImage image, File imageFile) throws IOException {
        if (image == null || imageFile == null)
            return false;

        try (FileOutputStream output = new FileOutputStream(imageFile)) {
            ImageIO.write(image, config.getScreenshotResolution().getFormatName(), output);
            output.flush();
            return true;
        }
    }

    private boolean saveImage(RenderedImage image, String append) throws IOException {
        if (isBlank(append))
            append = DateFormatUtils.format(new Date(), "hhmmss");
        File folder = new File(fileInfo.getPath());
        long timestamp = -1;
        try {
            String videoFileName = Files.getNameWithoutExtension(fileInfo.getName());
            if (isBlank(videoFileName)) videoFileName = "PC-screenshot";
            File screenshotFile = new File(folder, videoFileName + "-" + append + "." + config.getScreenshotResolution().getSuffix());
            timestamp = folder.lastModified();
            boolean flag = this.saveImage(image, screenshotFile);
            if (flag && logger.isInfoEnabled())
                logger.info("Screenshot is saved, {}", screenshotFile.getCanonicalPath());
            return flag;
        } finally {
            if (timestamp > -1) folder.setLastModified(timestamp);
        }
    }

    public boolean saveImage(RenderedImage image, int seconds) throws IOException {
        return saveImage(image, getFormattedSeconds(seconds));
    }
}
