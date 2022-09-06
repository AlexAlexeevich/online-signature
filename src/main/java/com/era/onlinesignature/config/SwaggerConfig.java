package com.era.onlinesignature.config;

import io.swagger.models.auth.In;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    public static final String AUTHORIZATION_HEADER = "Authorization";


    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .securitySchemes(Arrays.asList(new ApiKey("Bearer", HttpHeaders.AUTHORIZATION, In.HEADER.name())))
//                .directModelSubstitute(LocalDate.class, String.class)
//                .useDefaultResponseMessages(false)
                //.ignoredParameterTypes(UserPrincipal.class)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.era.onlinesignature.controller"))
                .paths(PathSelectors.any())
                //.build().apiInfo(metaData());
                .build();
    }

        private ApiInfo metaData() {
            return new ApiInfoBuilder()
                .title("Online-signature api")
                .description("\"Online-signature Backend API\"")
                .version("1.0.0")
                .license("OOO ERA DEVELOPMENT")
                .licenseUrl("http://eradevelopment.org/")
                .contact(new Contact("Baranov Alexei", "http://eradevelopment.org/", "joe@sanitas.ru"))
                .build();
    }
}
