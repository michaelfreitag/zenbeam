package org.zenbeam.converter;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class StringLongObjectConverterTest {

    private StringLongObjectConverter converter = new StringLongObjectConverter();

    @Test
    public void testConvert() {

        //given
        String s = "1";

        //when
        Long number = converter.convert(s);

        //then
        assertThat(number, instanceOf(Long.class));
        assertThat(number, equalTo(new Long(1)));

    }

}
