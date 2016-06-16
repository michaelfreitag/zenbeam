package org.zenbeam.util;

import org.junit.Test;
import org.zenbeam.model.FieldCommand;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class FieldCommandUtilsTest {

    @Test
    public void testCountDots() {

        //given
        String propertyPath = "a.b.c.d";

        //when
        int count = FieldCommandUtils.countDots(propertyPath);

        //then
        assertThat(count, equalTo(3));

    }

    @Test
    public void testGetCommandsAsStringSortedByFieldDepth() {

        //given
        List<FieldCommand> commands = new ArrayList<FieldCommand>();
        FieldCommand fc1 = new FieldCommand("a", "{}", false, null);
        FieldCommand fc2 = new FieldCommand("a.a", "{}", false, null);
        FieldCommand fc3 = new FieldCommand("b", "{}", false, null);
        FieldCommand fc4 = new FieldCommand("a.b.c.d", "{}", false, null);
        FieldCommand fc5 = new FieldCommand("a.b", "{}", false, null);

        commands.add(fc4);
        commands.add(fc3);
        commands.add(fc2);
        commands.add(fc1);
        commands.add(fc5);

        //when
        FieldCommandUtils.sortFieldCommandListByFieldDepth(commands);

        //then
        assertThat(commands.get(0).getFieldKey(), equalTo("a"));
        assertThat(commands.get(4).getFieldKey(), equalTo("a.b.c.d"));

    }

    @Test
    public void testRemoveDuplicates() {

        //given
        List<FieldCommand> commands = new ArrayList<FieldCommand>();
        FieldCommand fc1 = new FieldCommand("a", "{}", false, null);
        FieldCommand fc2 = new FieldCommand("b", "{}", false, null);
        FieldCommand fc3 = new FieldCommand("a", "{}", false, null);
        FieldCommand fc4 = new FieldCommand("b", "{;}", false, null);

        commands.add(fc1);
        commands.add(fc2);
        commands.add(fc3);
        commands.add(fc4);


        //when
        FieldCommandUtils.removeDuplicates(commands);

        //then
        assertThat(commands.size(), equalTo(2));

    }


}
