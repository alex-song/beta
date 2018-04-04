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
import alex.beta.filerepository.persistence.entity.FileStore;
import alex.beta.filerepository.persistence.entity.Quota;
import alex.beta.filerepository.persistence.repository.FileInfoRepository;
import alex.beta.filerepository.persistence.repository.QuotaRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
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

    private FileInfoRepository frsRepository;

    @Autowired
    private QuotaRepository qr;

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
    @GetMapping(value = "/testAdd", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity testAdd() throws Exception {

        Quota q = qr.findAndIncreaseUsedQuotaByAppidIgnoreCase("aaa", 10);
        logger.warn(q.toString());


        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource res = resourcePatternResolver.getResource("classpath:application.yml");

        byte[] content = IOUtils.toByteArray(res.getInputStream());
        FileStore fs = FileStore.builder().content(content).md5(DigestUtils.md5DigestAsHex(content)).build();

        FileInfo fi = frsRepository.save(FileInfo.builder().name("name").description("description").size(content.length).fileStore(fs).build());
        System.out.println(fi.getId());
        return ResponseEntity.ok("ok");
    }

    @ApiOperation(value = "Test")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Translation request is found."),
            @ApiResponse(code = 404, message = "Translation request is not found.")
    })
    @GetMapping(value = "/testDelete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity testDelete() throws Exception {

        List<FileInfo> aaa = new ArrayList<>();
        aaa.add(FileInfo.builder().id("5ac0ed30ff11c10f9e38483e").build());

        frsRepository.delete(aaa);
        return ResponseEntity.ok("ok");
    }
}
