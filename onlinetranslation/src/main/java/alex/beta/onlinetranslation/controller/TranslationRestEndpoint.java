/**
 * <p>
 * File Name: TranslationRestEndpoint.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/15 下午3:32
 * </p>
 */
package alex.beta.onlinetranslation.controller;

import alex.beta.onlinetranslation.model.TranslationResult;
import alex.beta.onlinetranslation.model.TranslationStatus;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.UUID;

/**
 * @author alexsong
 * @version ${project.version}
 */

@Profile("dev")
@Controller
@RestController
@Validated
@Api(value = "Online Translation API")
public class TranslationRestEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(TranslationRestEndpoint.class);

    @GetMapping("/")
    void home(HttpServletResponse response) throws IOException {
        response.sendRedirect("api-spec/index.html");
    }

    @ApiOperation(value = "Submit a translation request")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Translation request is successfully submitted.", response = String.class),
            @ApiResponse(code = 201, message = "Translation request is successfully submitted.", response = String.class),
            @ApiResponse(code = 500, message = "Internal error, that failed to submit the translation request.", response = String.class)
    })
    @PostMapping(value = "/translate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity translate(
            @ApiParam(value = "Content to translate.", required = true)
            @NotNull
            @RequestBody String content,
            @ApiParam(value = "Target language to translate.")
            @Valid
            @RequestParam(value = "toLanguage", required = false, defaultValue = "en") String toLanguage) {
        validateToLanguage(toLanguage);
        //TODO
        String uuid = UUID.randomUUID().toString();
        TranslationResult result = new TranslationResult(uuid, TranslationStatus.SUBMITTED, content, toLanguage);
        return ResponseEntity.ok(result);
    }

    private void validateToLanguage(String toLanguage) {
        //TODO
    }
}
