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

import alex.beta.onlinetranslation.models.TranslationResult;
import alex.beta.onlinetranslation.persistence.Translation;
import alex.beta.onlinetranslation.persistence.TranslationRepository;
import alex.beta.onlinetranslation.persistence.TranslationStatus;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private TranslationRepository translationRepository;

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

    @GetMapping("/")
    void home(HttpServletResponse response) throws IOException {
        response.sendRedirect("api-spec/index.html");
    }

    @ApiOperation(value = "Submit a translation request")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Translation request is successfully submitted.", response = String.class),
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
        if (logger.isInfoEnabled()) {
            logger.info("Receive translation request.\ntoLanguage : {},\ntext: \n{}", toLanguage, content);
        }
        String validatedToLanguage = validateToLanguage(toLanguage);
        if (logger.isDebugEnabled()) {
            logger.debug("toLanguage : {}", validatedToLanguage);
        }
        Translation translation = translationRepository.saveAndFlush(new Translation(TranslationStatus.SUBMITTED, "auto", validatedToLanguage, content));
        TranslationResult result = new TranslationResult(translation);
        if (logger.isInfoEnabled()) {
            logger.info("Translation request is successfully submitted.\n{}", result);
        }
        return ResponseEntity.ok(result);
    }

    private String validateToLanguage(String toLanguage) {
        String lang = Objects.requireNonNull(toLanguage).trim().toLowerCase();
        if (availableLanguages.contains(lang)) {
            return lang;
        } else {
            String msg = messageSource.getMessage("TranslationRestEndpoint.unsupportedToLanguage",
                    new Object[]{toLanguage, availableLanguages.stream().collect(Collectors.joining("\', \'"))},
                    LocaleContextHolder.getLocale());
            logger.warn(msg);
            throw new IllegalArgumentException(msg);
        }
    }
}
