/**
 * @File: FRSRestEndpoint.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/25 下午1:21
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.controllers;

import alex.beta.filerepository.ContentValidationException;
import alex.beta.filerepository.QuotaExceededException;
import alex.beta.filerepository.models.FRSErrorModel;
import alex.beta.filerepository.models.FileInfoModel;
import alex.beta.filerepository.models.FileStoreModel;
import alex.beta.filerepository.persistence.repository.FileInfoRepository;
import alex.beta.filerepository.services.FileRepositoryService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @version ${project.version}
 * @Description
 */
@Controller
@RestController
@Validated
@Api(value = "Alex File Repository Service API")
public class FRSRestEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(FRSRestEndpoint.class);

    private MessageSource messageSource;

    private FileRepositoryService fileRepositoryService;

    @Autowired
    private FileInfoRepository fileInfoRepository;

    @Autowired
    public FRSRestEndpoint(MessageSource messageSource, FileRepositoryService fileRepositoryService) {
        this.messageSource = messageSource;
        this.fileRepositoryService = fileRepositoryService;
    }

    @GetMapping("/")
    void home(HttpServletResponse response) throws IOException {
        response.sendRedirect("api-spec/index.html");
    }

    @ApiOperation(value = "Add file into repository")
    @ApiResponses({
            @ApiResponse(code = 200, message = "File is uploaded successfully.", response = FileInfoModel.class),
            @ApiResponse(code = 400, message = "Cannot read file, or content validation error.", response = FRSErrorModel.class),
            @ApiResponse(code = 507, message = "Insufficient quota.", response = FRSErrorModel.class)
    })
    @PostMapping(value = "/repository", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity add(
            @ApiParam(value = "File to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @ApiParam(value = "Application ID", required = true)
            @RequestParam(value = "appid") String appid,
            @ApiParam(value = "File name")
            @RequestParam(value = "name", required = false) String name,
            @ApiParam(value = "File description")
            @RequestParam(value = "description", required = false) String description,
            @ApiParam(value = "Content MD5 value")
            @RequestParam(value = "md5", required = false) String md5,
            @ApiParam(value = "Time this file is going to be expired")
            @RequestParam(value = "expiredDate", required = false) LocalDateTime expiredDate) {
        try {
            FileInfoModel model = fileRepositoryService.add(appid,
                    StringUtils.isEmpty(name) ? file.getOriginalFilename() : name, description,
                    file.getContentType(), expiredDate, md5, file.getBytes());
            if (logger.isDebugEnabled()) {
                logger.debug("File is uploaded successfully{}{}", System.lineSeparator(), model);
            }
            return ResponseEntity.ok(model);
        } catch (IOException ex) {
            logger.error("Cannot read file", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new FRSErrorModel("FRSRestEndpoint.FileReadError",
                            buildResponseErrorMessage("FRSRestEndpoint.FileReadError")));
        } catch (ContentValidationException ex) {
            logger.error("Content validation error", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new FRSErrorModel("FRSRestEndpoint.MD5Error",
                            buildResponseErrorMessage("FRSRestEndpoint.MD5Error", ex.getExpected(), ex.getActual())));
        } catch (QuotaExceededException ex) {
            logger.error("No enough quota", ex);
            return ResponseEntity.status(HttpStatus.INSUFFICIENT_STORAGE).body(
                    new FRSErrorModel("FRSRestEndpoint.InsufficientQuota",
                            buildResponseErrorMessage("FRSRestEndpoint.InsufficientQuota", ex.getAppid(), ex.getMax(), ex.getUsed(), ex.getPoints())));
        }

    }

    @ApiOperation(value = "Get file information")
    @ApiResponses({
            @ApiResponse(code = 200, message = "File is found.", response = FileInfoModel.class),
            @ApiResponse(code = 404, message = "File not found.")
    })
    @GetMapping(value = "/repository/{fileInfoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity file(
            @ApiParam(value = "ID of file", required = true)
            @PathVariable(value = "fileInfoId") String fileInfoId) {
        FileInfoModel model = fileRepositoryService.get(fileInfoId);
        if (model == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(model);
        }
    }

    @ApiOperation(value = "Get file content")
    @ApiResponses({
            @ApiResponse(code = 200, message = "File is found.", response = Resource.class),
            @ApiResponse(code = 404, message = "File not found.")
    })
    @GetMapping(value = "/repository/{fileInfoId}/file")
    public ResponseEntity download(
            @ApiParam(value = "ID of file", required = true)
            @PathVariable(value = "fileInfoId") String fileInfoId) {
        FileStoreModel model = fileRepositoryService.getFile(fileInfoId);
        if (model == null) {
            return ResponseEntity.notFound().build();
        } else {
            ByteArrayResource resource = new ByteArrayResource(model.getContent());
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            return ResponseEntity.ok().headers(headers).contentLength(model.getContent().length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        }
    }

    @ApiOperation(value = "Delete file")
    @ApiResponses({
            @ApiResponse(code = 200, message = "File is deleted.", response = FileInfoModel.class),
            @ApiResponse(code = 404, message = "File not found.")
    })
    @DeleteMapping(value = "/repository/{fileInfoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity delete(
            @ApiParam(value = "ID of file", required = true)
            @PathVariable(value = "fileInfoId") String fileInfoId) {
        FileInfoModel model = fileRepositoryService.delete(fileInfoId);
        if (model == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(model);
        }
    }


    @ApiOperation(value = "Test")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Translation request is found."),
            @ApiResponse(code = 404, message = "Translation request is not found.")
    })
    @GetMapping(value = "/testDelete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity testDelete() throws Exception {
        //fileRepositoryService.add("aabbcc", "aabbcc", "", null, null, null, null);
        //fileRepositoryService.add("AaBbCc", "aabbcc", "", null, null, null, null);
        //fileRepositoryService.add("aabbcc", "bbc", "", null, null, null, null);

        List<FileInfoModel> fims = fileRepositoryService.page("AaBbCc", "AAB", 0);

        System.out.println(fims);

        List<FileInfoModel> fims1 = fileRepositoryService.page("AaBbCc", "AAB", 1);

        System.out.println(fims1);

        return ResponseEntity.ok("ok");
    }

    //----------- private methods -----------

    private String buildResponseErrorMessage(String errorCode, Object... paramters) {
        String msg = messageSource.getMessage(errorCode, paramters, LocaleContextHolder.getLocale());
        logger.warn("{} - {}", errorCode, msg);
        return msg;
    }
}
