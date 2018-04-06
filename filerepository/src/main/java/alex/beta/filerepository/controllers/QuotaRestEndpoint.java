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

import alex.beta.filerepository.services.QuotaService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

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

    //TODO
}
