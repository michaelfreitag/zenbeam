package org.zenbeam.converter;

public interface Converter<S, T> {

    public T convert(S source);

    public Class<S> getSourceClass();

    public Class<T> getTargetClass();

}
