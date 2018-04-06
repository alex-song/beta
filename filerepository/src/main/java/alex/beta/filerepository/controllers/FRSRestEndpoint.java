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

import alex.beta.filerepository.models.FileInfoModel;
import alex.beta.filerepository.persistence.repository.FileInfoRepository;
import alex.beta.filerepository.services.FileRepositoryService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
            @ApiResponse(code = 500, message = "Internal server error, when saving file into repository."),
            @ApiResponse(code = 403, message = "No permission to add file.")
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
            @RequestParam(value = "md5", required = false) String md5
    ) throws Exception {
        FileInfoModel model = fileRepositoryService.add(appid,
                StringUtils.isEmpty(name) ? file.getOriginalFilename() : name, description,
                file.getContentType(), null, md5, file.getBytes());

        return ResponseEntity.ok(model);

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
}
