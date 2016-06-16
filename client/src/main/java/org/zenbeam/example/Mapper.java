package org.zenbeam.example;

import org.zenbeam.Projection;
import org.zenbeam.Projections;
import org.zenbeam.ZenBeamer;
import org.zenbeam.example.dto.ExampleDto;
import org.zenbeam.example.entity.ExampleA;
import org.zenbeam.example.service.EntityService;

@ZenBeamer
public interface Mapper {

     /*
     @Projections({
             //@Projection(source="exampleB.exampleC.exampleAList[0].name", target = "exampleAName")
         //@Projection(source = "name", target = "exampleAName"),
        // @Projection(source = "id", target = "exampleAId"),
         //@Projection(source = "exampleNumber", target = "exampleNumber"),
         //@Projection(source = "exampleB.id", target = "exampleB.id"),
         //@Projection(source = "exampleB.exampleC.id", target = "exampleB.exampleC.id"),
         //@Projection(source = "exampleB.id", target = "exampleBId"),
         //@Projection(source = "name", target = "exampleAName"),
         //@Projection(source = "exampleB.name", target = "exampleBName")
     })
     public void fromExampleAToExampleDto(ExampleA source, ExampleDto target);
     */

    @Projections({
            @Projection(source = "exampleCId", target = "exampleB.exampleC.id", instantiateNewIfNotNull = true, entityService = EntityService.class),
            @Projection(source = "exampleCUuid", target = "exampleB.exampleC.uuid", profile = "test"),
            /*
            @Projection(source = "exampleAId", target = "id")
            @Projection(source= "exampleAName", target="exampleB.exampleC.exampleAList[0].name")
            @Projection(source= "exampleBName", target="exampleBList[0].name")
            @Projection(source = "exampleNumber", target = "exampleNumber"),
            */
    })
    public void fromExampleDtoToExampleA(ExampleDto source, ExampleA target);


}
