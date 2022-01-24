package alex.beta.portablecinema.command;

import alex.beta.portablecinema.DatabaseAdapter;
import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.database.DatabaseException;
import alex.beta.portablecinema.pojo.FileInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jxl.Workbook;
import jxl.write.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExportCommand extends Command<String> {

    private String fileName;

    /**
     * @param config
     * @return Export file path
     */
    @Override
    public String execute(PortableCinemaConfig config) {
        if (fileName.toLowerCase().endsWith(".xls") || fileName.toLowerCase().endsWith(".xlsx")) {
            return exportXls(config);
        } else {
            return exportJson(!fileName.toLowerCase().endsWith(".json"));
        }
    }

    private String exportXls(PortableCinemaConfig config) {
        String filePath = null;
        try {
            File xlsFile = new File(fileName);
            filePath = xlsFile.getCanonicalPath();
            WritableWorkbook workbook = Workbook.createWorkbook(xlsFile);
            WritableFont labelFont = new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD);
            WritableCellFormat labelCellFormat = new WritableCellFormat(labelFont);

            WritableSheet configSheet = workbook.createSheet("配置信息", 0);
            configSheet.addCell(new Label(0, 0, "根目录", labelCellFormat));
            configSheet.setColumnView(0, 30);
            configSheet.addCell(new Label(1, 0, config.getRootFolderPath()));
            configSheet.addCell(new Label(0, 1, "目录中数据库文件名", labelCellFormat));
            configSheet.addCell(new Label(1, 1, config.getDbFileName()));
            configSheet.addCell(new Label(0, 2, "视频文件后缀", labelCellFormat));
            configSheet.addCell(new Label(1, 2, config.getVideoFileExtensions()));
            configSheet.addCell(new Label(0, 3, "图片文件后缀", labelCellFormat));
            configSheet.addCell(new Label(1, 3, config.getImageFileExtensions()));
            configSheet.addCell(new Label(0, 4, "视频文件大小限制（最小）", labelCellFormat));
            configSheet.addCell(new Label(1, 4, String.valueOf(config.getVideoFileSizeThreshold())));
            configSheet.addCell(new Label(0, 5, "图片文件大小限制（最小）", labelCellFormat));
            configSheet.addCell(new Label(1, 5, String.valueOf(config.getImageFileSizeThreshold())));
            configSheet.addCell(new Label(0, 6, "忽略文件名", labelCellFormat));
            configSheet.addCell(new Label(1, 6, config.getSkipNameStartsWith()));
            configSheet.addCell(new Label(0, 7, "字典文件名", labelCellFormat));
            configSheet.addCell(new Label(1, 7, config.getGlossaryFileName()));

            WritableSheet fileSheet = workbook.createSheet("影片信息", 1);
            fileSheet.addCell(new Label(0, 0, "名称", labelCellFormat));
            fileSheet.setColumnView(0, 100);
            fileSheet.addCell(new Label(1, 0, "路径", labelCellFormat));
            fileSheet.setColumnView(1, 100);
            fileSheet.addCell(new Label(2, 0, "标签", labelCellFormat));
            fileSheet.setColumnView(2, 100);
            fileSheet.addCell(new Label(3, 0, "影片时长（秒）", labelCellFormat));
            fileSheet.setColumnView(3, 20);
            fileSheet.addCell(new Label(4, 0, "最后修改时间", labelCellFormat));
            fileSheet.setColumnView(4, 20);
            fileSheet.addCell(new Label(5, 0, "文件大小", labelCellFormat));
            fileSheet.setColumnView(5, 20);
            fileSheet.addCell(new Label(6, 0, "分辨率-宽", labelCellFormat));
            fileSheet.setColumnView(6, 20);
            fileSheet.addCell(new Label(7, 0, "分辨率-高", labelCellFormat));
            fileSheet.setColumnView(7, 20);
            fileSheet.addCell(new Label(8, 0, "封面图片", labelCellFormat));
            fileSheet.setColumnView(8, 100);
            fileSheet.addCell(new Label(9, 0, "封底图片", labelCellFormat));
            fileSheet.setColumnView(9, 100);

            DatabaseAdapter databaseAdapter = DatabaseAdapter.getAdapter(DatabaseAdapter.Type.H2_IN_MEMORY);
            FileInfo[] infos = databaseAdapter.findByTags();
            for (int i = 0; i < infos.length; i++) {
                fileSheet.addCell(new Label(0, i + 1, infos[i].getName()));
                fileSheet.addCell(new Label(1, i + 1, infos[i].getPath()));
                fileSheet.addCell(new Label(2, i + 1, StringUtils.join(infos[i].getTags(), ", ")));
                fileSheet.addCell(new Label(3, i + 1, String.valueOf(infos[i].getDuration())));
                if (infos[i].getLastModifiedOn() != null) {
                    fileSheet.addCell(new Label(4, i + 1, DateFormatUtils.format(infos[i].getLastModifiedOn(), PortableCinemaConfig.DATE_FORMATTER)));
                }
                fileSheet.addCell(new Label(5, i + 1, String.valueOf(infos[i].getSize())));
                if (infos[i].getResolution() != null) {
                    fileSheet.addCell(new Label(6, i + 1, String.valueOf(infos[i].getResolution().getWidth())));
                    fileSheet.addCell(new Label(7, i + 1, String.valueOf(infos[i].getResolution().getHeight())));
                }
                fileSheet.addCell(new Label(8, i + 1, infos[i].getCover1()));
                fileSheet.addCell(new Label(9, i + 1, infos[i].getCover2()));
            }

            workbook.write();
            workbook.close();
        } catch (DatabaseException ex0) {
            logger.error("Failed to query database to export file infos", ex0);
        } catch (WriteException ex1) {
            logger.error("Failed to write spreadsheet of file infos", ex1);
        } catch (IOException ex2) {
            logger.error("Failed to write file infos into file [{}]", fileName, ex2);
        }
        return filePath;
    }

    private String exportJson(boolean appendExtension) {
        String filePath = null;
        String realFileName = fileName + (appendExtension ? ".json" : "");
        File jsonFile = new File(realFileName);
        try (FileWriter writer = new FileWriter(jsonFile)) {
            filePath = jsonFile.getCanonicalPath();
            DatabaseAdapter databaseAdapter = DatabaseAdapter.getAdapter(DatabaseAdapter.Type.H2_IN_MEMORY);
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().setDateFormat(PortableCinemaConfig.DATE_FORMATTER).create();
            gson.toJson(databaseAdapter.findByTags(), writer);
            writer.flush();
        } catch (DatabaseException ex1) {
            logger.error("Failed to query database to export file infos", ex1);
        } catch (IOException ex2) {
            logger.error("Failed to write file info into file [{}]", realFileName, ex2);
        }
        return filePath;
    }
}
