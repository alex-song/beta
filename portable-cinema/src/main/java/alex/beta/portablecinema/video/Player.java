package alex.beta.portablecinema.video;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.pojo.FileInfo;
import com.xuggle.xuggler.*;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Player implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Player.class);
    private FileInfo fileInfo;
    private RandomAccessFile videoFile;
    private IContainer container;
    private int videoStreamId = -1;
    private IStreamCoder videoCoder;

    private Player(PortableCinemaConfig config, FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public static Player getInstance(@NonNull PortableCinemaConfig config, @NonNull FileInfo fileInfo) {
        return new Player(config, fileInfo);
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
            logger.warn("Fail to open video file [{}/{}]", fileInfo.getPath(), fileInfo.getName());
        }
        return this;
    }

    /**
     * force = false
     *
     * @return
     */
    public FileInfo.Resolution getResolution() {
        return this.getResolution(false);
    }

    /**
     * @param force decode the video again, if this flag is true, regardless the existing resolution
     * @return Resolution of given video file
     */
    public FileInfo.Resolution getResolution(boolean force) {
        if (videoCoder == null) {
            if (force && fileInfo.getResolution() == null) {
                fileInfo.setResolution(new FileInfo.Resolution());
            }
        } else if (fileInfo.getResolution() == null || force) {
            fileInfo.setResolution(new FileInfo.Resolution(videoCoder.getWidth(), videoCoder.getHeight()));
        }
        return fileInfo.getResolution();
    }

    /**
     * force = false
     *
     * @return
     */
    public long getDuration() {
        return this.getDuration(false);
    }

    /**
     * @param force decode the video again, if this flag is true, regardless the existing duration
     * @return Duration (in seconds) of given video file
     */
    public long getDuration(boolean force) {
        if (videoCoder != null && (fileInfo.getDuration() <= 0 || force)) {
            fileInfo.setDuration(container.getDuration() / 1000 / 1000);
        }
        return fileInfo.getDuration();
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
            logger.debug("TimeStampOffset {}", timeStampOffset);
            long target = container.getStartTime() + timeStampOffset;
            container.seekKeyFrame(videoStreamId, target, 0);
            boolean isFinished = false;
            while (container.readNextPacket(packet) >= 0 && !isFinished) {
                if (packet.getStreamIndex() == videoStreamId) {
                    IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(), videoCoder.getWidth(), videoCoder.getHeight());
                    int offset = 0;
                    while (!isFinished && offset < packet.getSize()) {
                        int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
                        if (bytesDecoded < 0) logger.warn("Get no data decoding video in one packet");
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
                            if (false)
                                try {
                                    ImageIO.write(screenshot, "png", new File("player-" + System.currentTimeMillis() + ".png"));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            isFinished = true;
                        }
                    }
                }
            }
            return screenshot;
        } finally {
            if (videoCoder != null) videoCoder.close();
        }
    }

    @Override
    public void close() {
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
        } catch (Exception ex) {
            logger.error("Error when closing player", ex);
        }
    }

    public static void main(String[] args) throws Exception {
        FileInfo fi = new FileInfo();
        fi.setName("SKYHD-016.mp4");
        fi.setPath("/Users/alexsong/Development/my_workspace/beta/portable-cinema/sample/Sample 1/Sample 13");
        Player p = Player.getInstance(new PortableCinemaConfig(), fi).read();
        p.captureScreen(0);
        p.captureScreen(60 * 30);
        p.captureScreen(60 * 60);
    }
}
