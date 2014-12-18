package org.zenbeam.example;

import org.zenbeam.Projection;
import org.zenbeam.Projections;
import org.zenbeam.ZenBeamer;
import org.zenbeam.example.dto.ExampleDto;
import org.zenbeam.example.entity.ExampleA;

@ZenBeamer
public interface Mapper {


     @Projections({
         @Projection(source = "exampleB.id", target = "exampleB.id"),
         @Projection(source = "exampleB.id", target = "exampleBId"),
         @Projection(source = "id", target = "exampleAId"),
         @Projection(source = "name", target = "exampleAName"),
         @Projection(source = "exampleB.name", target = "exampleBName")
     })
     public void fromExampleAToExampleDto(ExampleA source, ExampleDto target);


}
