package org.zenbeam.converter;

import java.util.HashMap;
import java.util.Map;

public class PropertyConverter {


    private Map<Class, Map<Class, Converter>> converters = new HashMap<Class, Map<Class, Converter>>();

    public PropertyConverter() {

        registerConverter(new StringLongConverter());

    }

    private void registerConverter(Converter converter) {

        Map<Class, Converter> availableConverters = converters.get(converter.getSourceClass());

        if (availableConverters == null) {
            availableConverters = new HashMap<Class, Converter>();
            converters.put(converter.getSourceClass(), availableConverters);
        }

        availableConverters.put(converter.getTargetClass(), converter);

    }


    public <T> T convert(Class<T> targetClazz, Object o) {

        T result = null;

        if (o != null) {
            //check if converter available
            Map<Class, Converter> availableConverters = converters.get(o.getClass());

            if (availableConverters != null) {

                Converter c = availableConverters.get(targetClazz);
                if (c != null) {
                    result = (T)c.convert(o);
                }

            }

        }

        return result;
    }


}
