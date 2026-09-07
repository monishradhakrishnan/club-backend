package com.campus.app.config;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule; 
import org.springframework.context.annotation.Bean; 
import org.springframework.context.annotation.Configuration; 
@Configuration public class JacksonConfig 
{ @Bean public Hibernate5JakartaModule hibernate5JakartaModule() 
    { Hibernate5JakartaModule module = new Hibernate5JakartaModule(); 
        module.disable(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING); 
    return module; } } 