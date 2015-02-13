package org.zenbeam.converter;

public class StringLongObjectConverter implements ObjectConverter<String, Long> {


    @Override
    public Long convert(String source) {

        if (source != null && !source.isEmpty()) {
            return Long.valueOf(source);
        } else {

            return null;
        }

    }

    @Override
    public Class<String> getSourceClass() {
        return String.class;
    }

    @Override
    public Class<Long> getTargetClass() {
        return Long.class;
    }


}
