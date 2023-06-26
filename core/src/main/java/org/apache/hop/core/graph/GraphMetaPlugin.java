package org.apache.hop.core.graph;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation signals to the plugin system that the class is a graph metadata plugin.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GraphMetaPlugin {

    String type();
    String typeDescription();
    String classLoaderGroup() default "";
    String documentationUrl() default "";
}
