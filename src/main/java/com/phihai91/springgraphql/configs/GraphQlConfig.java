package com.phihai91.springgraphql.configs;

import graphql.validation.rules.ValidationRules;
import graphql.validation.schemawiring.ValidationSchemaWiring;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
@Slf4j
public class GraphQlConfig {
    @Bean
    RuntimeWiringConfigurer runtimeWiringConfigurer() {
        // Adds all default validation rules in library
        ValidationRules possibleRules
                = ValidationRules.newValidationRules().build();
        // ValidationSchemaWiring implements SchemaDirectiveWiring
        ValidationSchemaWiring validationDirectiveWiring
                = new ValidationSchemaWiring(possibleRules);
        return wiringBuilder -> wiringBuilder
                .directiveWiring(validationDirectiveWiring);
    }

    @Bean
    GraphQlSourceBuilderCustomizer inspectionCustomizer() {
        return source -> source.inspectSchemaMappings(report -> log.info(report.toString()));
    }
}
