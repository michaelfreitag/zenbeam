package org.zenbeam.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyConverter {


    private Map<Class, Map<Class, ObjectConverter>> converters = new HashMap<Class, Map<Class, ObjectConverter>>();

    public PropertyConverter() {

        registerConverter(new StringLongObjectConverter());
        registerConverter(new LongStringObjectConverter());

    }

    private void registerConverter(ObjectConverter converter) {

        Map<Class, ObjectConverter> availableConverters = converters.get(converter.getSourceClass());

        if (availableConverters == null) {
            availableConverters = new HashMap<Class, ObjectConverter>();
            converters.put(converter.getSourceClass(), availableConverters);
        }

        availableConverters.put(converter.getTargetClass(), converter);

    }


    public <T> T convert(Class<T> targetClazz, Object o) {

        T result = null;

        if (o != null) {
            //check if converter available
            Map<Class, ObjectConverter> availableConverters = converters.get(o.getClass());

            if (availableConverters != null) {

                ObjectConverter c = availableConverters.get(targetClazz);
                if (c != null) {
                    result = (T)c.convert(o);
                }

            }

        }

        return result;
    }

    public <T> List<T> convertToList(Class<T> listType, Object o) {

        List<T> result = new ArrayList<>();

        if (listType.isAssignableFrom(o.getClass())) {
            result.add((T)o);
        } else {
            result.add(convert(listType, o));
        }

        return result;

    }


}
