package org.zenbeam.converter;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class LongStringConverterTest {

    private LongStringConverter converter = new LongStringConverter();

    @Test
    public void testConvert() {

        //given
        Long l = 10L;

        //when
        String s = converter.convert(l);

        //then
        assertThat(s, instanceOf(String.class));
        assertThat(s, equalTo("10"));

    }

}