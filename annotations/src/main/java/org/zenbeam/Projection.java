package org.zenbeam;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Projection {

    String source();

    String target();

    boolean instantiateNewIfNotNull() default false;

    Class<? extends ProjectionEntityService> entityService() default ProjectionEntityService.class;

}
