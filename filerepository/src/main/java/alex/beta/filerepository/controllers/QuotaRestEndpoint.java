/**
 * @File: QuotaRestEndpoint.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/5 下午4:45
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.controllers;

import alex.beta.filerepository.models.QuotaModel;
import alex.beta.filerepository.persistence.entity.Quota;
import alex.beta.filerepository.services.QuotaService;
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

import java.util.List;

/**
 * @version ${project.version}
 * @Description
 */

@Controller
@RestController
@Validated
@Api(value = "Alex File Repository Service Quota API")
public class QuotaRestEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(QuotaRestEndpoint.class);

    private MessageSource messageSource;

    private QuotaService quotaService;

    @Autowired
    public QuotaRestEndpoint(MessageSource messageSource, QuotaService quotaService) {
        this.messageSource = messageSource;
        this.quotaService = quotaService;
    }

    //TODO: create new quota, update existing quota, reset all, reset one
    @ApiOperation(value = "Get quota")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Quota is found.", response = QuotaModel.class),
            @ApiResponse(code = 404, message = "Quota not found.")
    })
    @GetMapping(value = "/quota/{appid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity get(
            @ApiParam(value = "ID of app", required = true)
            @PathVariable(value = "appid") String appid) {
        QuotaModel model = quotaService.findByAppidIgnoreCase(appid);
        if (model == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(model);
        }
    }

    @ApiOperation(value = "Get used quota")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Quota is found.", response = QuotaModel.class),
            @ApiResponse(code = 404, message = "Quota not found.")
    })
    @GetMapping(value = "/quota/{appid}/usedQuota", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getUsedQuota(
            @ApiParam(value = "ID of app", required = true)
            @PathVariable(value = "appid") String appid) {
        long usedQuota = quotaService.getUsedQuota(appid);
        if (usedQuota == Long.MIN_VALUE) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(new QuotaModel(Quota.builder().appid(appid).usedQuota(usedQuota).build()));
        }
    }

    @ApiOperation(value = "Get max quota")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Quota is found.", response = QuotaModel.class),
            @ApiResponse(code = 404, message = "Quota not found.")
    })
    @GetMapping(value = "/quota/{appid}/maxQuota", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getMaxQuota(
            @ApiParam(value = "ID of app", required = true)
            @PathVariable(value = "appid") String appid) {
        long maxQuota = quotaService.getMaxQuota(appid);
        if (maxQuota == Long.MIN_VALUE) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(new QuotaModel(Quota.builder().appid(appid).maxQuota(maxQuota).build()));
        }
    }

    @ApiOperation(value = "Get all quotas")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Quota is found.", response = List.class),
    })
    @GetMapping(value = "/quota/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity all() {
        return ResponseEntity.ok(quotaService.findAll());
    }
}
