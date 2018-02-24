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
package alex.beta.onlinetranslation.controllers;

import alex.beta.onlinetranslation.models.TranslationErrorModel;
import alex.beta.onlinetranslation.models.TranslationModel;
import alex.beta.onlinetranslation.services.TranslationService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author alexsong
 * @version ${project.version}
 */

@Controller
@RestController
@Validated
@Api(value = "Alex Online Translation API")
public class TranslationRestEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(TranslationRestEndpoint.class);
    private static final List<String> availableLanguages = Arrays.asList(
            "zh",
            "en",
            "yue",
            "wyw",
            "jp",
            "kor",
            "fra",
            "spa",
            "th",
            "ara",
            "ru",
            "pt",
            "de",
            "it",
            "el",
            "nl",
            "pl",
            "bul",
            "est",
            "dan",
            "fin",
            "cs",
            "rom",
            "slo",
            "swe",
            "hu",
            "cht",
            "vie");

    private MessageSource messageSource;

    private TranslationService translationService;

    @Autowired
    public TranslationRestEndpoint(MessageSource messageSource, TranslationService translationService) {
        this.messageSource = messageSource;
        this.translationService = translationService;
    }

    @GetMapping("/")
    void home(HttpServletResponse response) throws IOException {
        response.sendRedirect("api-spec/index.html");
    }

    @ApiOperation(value = "Submit a translation request. Max. 2000 characters.", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Translation request is successfully submitted.", response = TranslationModel.class),
            @ApiResponse(code = 400, message = "Invalid parameters.", response = TranslationErrorModel.class)
    })
    @PostMapping(value = "/translate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity translate(
            @ApiParam(value = "Content to translate.", required = true)
            @NotNull
            @RequestBody String content,
            @ApiParam(value = "Target language to translate.", required = true)
            @Valid
            @RequestParam(value = "toLanguage", defaultValue = "zh") String toLanguage) {
        if (logger.isInfoEnabled()) {
            logger.info("Receive translation request.\ntoLanguage : {}, text: {}", toLanguage, content);
        }

        if (content.trim().isEmpty()) {
            return ResponseEntity.ok(TranslationModel.NOTHING_TO_TRANSLATE);
        } else if (content.length() > 2000) {
            return ResponseEntity.badRequest().body(
                    new TranslationErrorModel("TranslationRestEndpoint.ContentOversize",
                            buildResponseErrorMessage("TranslationRestEndpoint.ContentOversize",
                                    toLanguage, String.valueOf(content.length())))
            );
        }

        String lang = Objects.requireNonNull(toLanguage).trim().toLowerCase();
        if (!availableLanguages.contains(lang)) {
            return ResponseEntity.badRequest().body(
                    new TranslationErrorModel("TranslationRestEndpoint.UnsupportedLanguage",
                            buildResponseErrorMessage("TranslationRestEndpoint.UnsupportedLanguage",
                                    toLanguage, availableLanguages.stream().collect(Collectors.joining("\', \'"))))
            );
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("toLanguage : {}", lang);
            }
            TranslationModel result = translationService.submit("auto", lang, content);
            if (logger.isInfoEnabled()) {
                logger.info("Translation request is successfully submitted.\n{}", result);
            }
            return ResponseEntity.ok(result);
        }
    }

    @ApiOperation(value = "Get status of a translation request")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Translation request is found.", response = TranslationModel.class),
            @ApiResponse(code = 404, message = "Translation request is not found.", response = TranslationErrorModel.class)
    })
    @GetMapping(value = "/translate/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity translate(
            @ApiParam(value = "uuid of a translation request.", required = true)
            @PathVariable(value = "uuid") String uuid) {
        if (logger.isDebugEnabled()) {
            logger.debug("Get translation according to uuid  {}", uuid);
        }
        TranslationModel translation = translationService.getTranslation(uuid);

        if (translation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new TranslationErrorModel("TranslationRestEndpoint.NotFound",
                            buildResponseErrorMessage("TranslationRestEndpoint.NotFound")));
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Translation is found.\n{}", translation);
            }
            return ResponseEntity.ok(translation);
        }
    }

    //----------- private methods -----------

    private String buildResponseErrorMessage(String errorCode, String... paramters) {
        String msg = messageSource.getMessage(errorCode, paramters, LocaleContextHolder.getLocale());
        logger.warn("{} - {}", errorCode, msg);
        return msg;
    }
}