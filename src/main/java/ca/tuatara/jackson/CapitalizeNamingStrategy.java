package ca.tuatara.jackson;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;

public class CapitalizeNamingStrategy extends PropertyNamingStrategy {
    private static final long serialVersionUID = 1L;

    @Override
    public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
        return StringUtils.capitalize(defaultName);
    }

    @Override
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return StringUtils.capitalize(defaultName);
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return StringUtils.capitalize(defaultName);
    }
}
