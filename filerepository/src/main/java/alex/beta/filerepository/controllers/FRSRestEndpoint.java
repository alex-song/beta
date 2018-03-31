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

import alex.beta.filerepository.persistence.entity.FileInfo;
import alex.beta.filerepository.persistence.repository.FileInfoRepository;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

    private FileInfoRepository frsRepository;

    @Autowired
    public FRSRestEndpoint(MessageSource messageSource, FileInfoRepository frsRepository) {
        this.messageSource = messageSource;
        this.frsRepository = frsRepository;
    }

    @GetMapping("/")
    void home(HttpServletResponse response) throws IOException {
        response.sendRedirect("api-spec/index.html");
    }

    @ApiOperation(value = "Test")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Translation request is found."),
            @ApiResponse(code = 404, message = "Translation request is not found.")
    })
    @GetMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity translate() {
        frsRepository.save(FileInfo.builder().id(1L).name("name").description("description").build());
        return ResponseEntity.ok("ok");
    }
}
