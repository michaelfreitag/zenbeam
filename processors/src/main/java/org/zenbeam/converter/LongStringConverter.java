package org.zenbeam.converter;

public class LongStringConverter implements Converter<Long, String> {


    @Override
    public String convert(Long source) {

        if (source != null) {
            return source.toString();
        } else {
            return null;
        }

    }

    @Override
    public Class<Long> getSourceClass() {
        return Long.class;
    }

    @Override
    public Class<String> getTargetClass() {
        return String.class;
    }


}
