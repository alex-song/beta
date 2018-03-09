/**
 * @File: SwaggerConfig.java
 * @Project: onlinetranslation
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * @Date: 2018/2/15 下午3:24
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation;

import alex.beta.onlinetranslation.controllers.TranslationRestEndpoint;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @version ${project.version}
 * @Description
 */

@Profile({"dev", "nas"})
@EnableSwagger2
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = {
        TranslationRestEndpoint.class
})
public class SwaggerConfig {
    @Value("${info.app.version}")
    String appVersion;

    @Value("${info.app.name}")
    String appName;

    @Value("${info.app.description}")
    String appDescription;

    @Value("${spring.profiles.active}")
    String profile;

    //Add URL mapping for static swagger2 ui files under src/main/resource/static/api-spec
    @Bean
    WebMvcConfigurer configurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/api-spec/**").
                        addResourceLocations("classpath:/api-spec/");
            }
        };
    }

    @Bean
    public Docket customImplementation() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .paths(PathSelectors.any())
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .build()
                .directModelSubstitute(LocalDate.class, java.sql.Date.class)
                .directModelSubstitute(LocalDateTime.class, java.util.Date.class)
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(appName)
                .description(appDescription)
                .contact(new Contact("Alex Online Translation", "https://songlp.ddns.net", "song_liping@hotmail.com"))
                .version(profile + " " + appVersion)
                .build();
    }
}
