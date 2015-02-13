package org.zenbeam.converter;

public interface ObjectConverter<S, T> {

    public T convert(S source);

    public Class<S> getSourceClass();

    public Class<T> getTargetClass();

}
