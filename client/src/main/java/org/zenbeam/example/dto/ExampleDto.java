package org.zenbeam.example.dto;

import org.zenbeam.example.entity.ExampleA;

public class ExampleDto {

    private String exampleAId;

    private String exampleAName;

    private Long exampleBId;

    private String exampleBName;

    private ExampleA exampleA;

    public String getExampleAId() {
        return exampleAId;
    }

    public void setExampleAId(String exampleAId) {
        this.exampleAId = exampleAId;
    }

    public String getExampleAName() {
        return exampleAName;
    }

    public void setExampleAName(String exampleAName) {
        this.exampleAName = exampleAName;
    }

    public Long getExampleBId() {
        return exampleBId;
    }

    public void setExampleBId(Long exampleBId) {
        this.exampleBId = exampleBId;
    }

    public String getExampleBName() {
        return exampleBName;
    }

    public void setExampleBName(String exampleBName) {
        this.exampleBName = exampleBName;
    }

    public ExampleA getExampleA() {
        return exampleA;
    }

    public void setExampleA(ExampleA exampleA) {
        this.exampleA = exampleA;
    }
}
