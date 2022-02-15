package alex.beta.portablecinema.command;

import alex.beta.portablecinema.DatabaseAdapter;
import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.database.DatabaseException;
import alex.beta.portablecinema.pojo.FileInfo;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
public class AnalyzeCommand extends Command<AnalyzeCommand.AnalyzeResult> {

    /**
     * @param config
     * @return
     */
    @Override
    public AnalyzeCommand.AnalyzeResult execute(PortableCinemaConfig config) {
        AnalyzeCommand.AnalyzeResult result = new AnalyzeCommand.AnalyzeResult();
        try {
            DatabaseAdapter databaseAdapter = DatabaseAdapter.getAdapter(DatabaseAdapter.Type.H2_IN_MEMORY);
            result.setTotalVideos(databaseAdapter.count());
            result.setTagsInUse(databaseAdapter.listTagsAndOrderByCountDesc(0, 10));
            Long[] fss = databaseAdapter.findBySameSize();
            if (fss != null && fss.length > 0) {
                for (Long fileSize : fss)
                    result.addSimilarVideos(fileSize, databaseAdapter.findBySize(fileSize));
            }
        } catch (DatabaseException ex) {
            logger.error("Failed to analyze file info", ex);
        }
        return result;
    }

    public static class AnalyzeResult {
        /**
         * 总电影数量
         */
        private int totalVideos = 0;

        /**
         * Tag及其被标签的电影数量（一个电影可以有多个标签）
         */
        private LinkedHashMap<String, Integer> tagsInUse = new LinkedHashMap<>();

        /**
         * 相同大小的电影
         */
        private List<FileInfo[]> similarVideos = new ArrayList<>();

        /**
         * 相同大小的电影大小
         */
        private List<Long> similarSizes = new ArrayList<>();

        public int getTotalVideos() {
            return totalVideos;
        }

        public void setTotalVideos(int totalVideos) {
            this.totalVideos = totalVideos;
        }

        public LinkedHashMap<String, Integer> getTagsInUse() {
            return tagsInUse;
        }

        public void setTagsInUse(LinkedHashMap<String, Integer> tagsInUse) {
            this.tagsInUse = tagsInUse;
        }

        public List<FileInfo[]> getSimilarVideos() {
            return similarVideos;
        }

        public List<Long> getSimilarSizes() {
            return similarSizes;
        }

        public void addSimilarVideos(long fileSize, FileInfo[] videos) {
            //Double check resolution of each file.
            //The files are different, if the resolution are different, though the file size are the same.
            FileInfo.Resolution r = videos[0].getResolution();
            List<FileInfo> tmp = new ArrayList<>();
            for (FileInfo video : videos)
                if (Objects.equals(video.getResolution(), r))
                    tmp.add(video);
                else if (video.getResolution() != null && r == null && video.getResolution().getWidth() == 0 && video.getResolution().getHeight() == 0) {
                    tmp.add(video);
                } else if (r != null && video.getResolution() == null && r.getWidth() == 0 && r.getHeight() == 0) {
                    tmp.add(video);
                }

            if (tmp.size() > 1) {
                similarSizes.add(fileSize);
                similarVideos.add(tmp.toArray(new FileInfo[]{}));
            }
        }
    }
}
