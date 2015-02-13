package org.zenbeam.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class FieldInfoUtilsTest {

    @Test
    public void testPropertyPath() {

        //given
        String property = "a.b.c.d";

        //when
        String field = FieldInfoUtils.getFirstFieldName(property);

        //then
        assertThat(field, equalTo("a"));

    }

    @Test
    public void testProperty() {

        //given
        String property = "a";

        //when
        String field = FieldInfoUtils.getFirstFieldName(property);

        //then
        assertThat(field, equalTo("a"));

    }


    @Test
    public void testArrayProperty() {

        //given
        String property = "a[]";

        //when
        String field = FieldInfoUtils.getFirstFieldName(property);

        //then
        assertThat(field, equalTo("a"));

    }



}
