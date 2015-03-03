package org.zenbeam.example;

import org.zenbeam.Projection;
import org.zenbeam.Projections;
import org.zenbeam.ZenBeamer;
import org.zenbeam.example.dto.ExampleDto;
import org.zenbeam.example.entity.ExampleA;

@ZenBeamer
public interface Mapper2 {


    @Projections({
            @Projection(source= "exampleBName", target="exampleBList[0].name")
            /*
            @Projection(source = "exampleCId", target = "exampleB.exampleC.id", instantiateNewIfNotNull = true),
            @Projection(source = "exampleAId", target = "id"),
            @Projection(source = "exampleNumber", target = "exampleNumber"),
            */
    })
    public void fromExampleDtoToExampleA(ExampleDto source, ExampleA target);


}
