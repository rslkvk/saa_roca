package util;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Created by resul on 16/05/15.
 */
@Provider
public class DefaultContextResolver implements ContextResolver<ObjectMapper> {
    final ObjectMapper combinedObjectMapper;
    public DefaultContextResolver() {
        combinedObjectMapper = createCombinedObjectMapper();
    }
    @Override
    public ObjectMapper getContext(final Class<?> type) {
        return combinedObjectMapper;
    }
    private static ObjectMapper createCombinedObjectMapper() {
        return new ObjectMapper()
                .configure(SerializationFeature.WRAP_ROOT_VALUE, true)
                .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true)
                .setAnnotationIntrospector(createJaxbJacksonAnnotationIntrospector());
    }
    private static AnnotationIntrospector createJaxbJacksonAnnotationIntrospector() {
        final AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        final AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
        return AnnotationIntrospector.pair(jacksonIntrospector, jaxbIntrospector);
    }
}