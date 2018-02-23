/**
 * <p>
 * File Name: CustomizedModelPropertyBuilderPlugin.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/23 下午8:38
 * </p>
 */
package alex.beta.onlinetranslation.plugins;

import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.google.common.base.Optional;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.ModelPropertyBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;

/**
 * 因为Swagger UI上对Model的验证有问题，不接受allowEmptyValue，所以做了这个Plugin。
 * 当这个属性是false时（默认值），强制把这个属性置空，避免这个属性在meta里输出。
 *
 * https://stackoverflow.com/questions/48379238/springfox-hide-allowemptyvalue-when-field-annotated-with-apimodelproperty
 *
 * @author alexsong
 * @version ${project.version}
 */
@Component
public class CustomizedModelPropertyBuilderPlugin implements ModelPropertyBuilderPlugin {
    @Override
    public boolean supports(final DocumentationType arg0) {
        return true;
    }

    @Override
    public void apply(final ModelPropertyContext context) {
        final ModelPropertyBuilder builder = context.getBuilder();

        final Optional<BeanPropertyDefinition> beanPropDef = context.getBeanPropertyDefinition();
        final BeanPropertyDefinition beanDef = beanPropDef.isPresent() ? beanPropDef.get() : null;
        if (beanDef == null) {
            return;
        }
        final AnnotatedMethod method = beanDef.getGetter();
        if (method == null) {
            return;
        }

        final ApiModelProperty apiModelProperty = method.getAnnotation(ApiModelProperty.class);
        if (apiModelProperty == null) {
            return;
        }
        if (!apiModelProperty.allowEmptyValue()) {
            builder.allowEmptyValue(null);
        }
    }
}