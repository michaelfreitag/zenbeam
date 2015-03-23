package org.zenbeam.example;

import org.zenbeam.ZenBeamer;

@ZenBeamer
public interface Mapper2 {

    /*
    @Projections({
            @Projection(source= "exampleBName", target="exampleBList[0].name")
            @Projection(source = "exampleCId", target = "exampleB.exampleC.id", instantiateNewIfNotNull = true),
            @Projection(source = "exampleAId", target = "id"),
            @Projection(source = "exampleNumber", target = "exampleNumber"),
    })

    public void fromExampleDtoToExampleA(ExampleDto source, ExampleA target);

    @Projections({
            @Projection(source="exampleBList[0].name", target = "exampleBName")
            @Projection(source = "exampleCId", target = "exampleB.exampleC.id", instantiateNewIfNotNull = true),
            @Projection(source = "exampleAId", target = "id"),
            @Projection(source = "exampleNumber", target = "exampleNumber"),
    })
    public void fromExampleAToExampleDto(ExampleA source, ExampleDto  target);

    */
}
