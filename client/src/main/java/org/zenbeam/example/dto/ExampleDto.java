package org.zenbeam.example.dto;

import org.zenbeam.example.entity.ExampleB;

public class ExampleDto {

    private String exampleAId;

    private String exampleAName;

    private Long exampleBId;

    private String exampleBName;

    private ExampleB exampleB;

    private Long exampleNumber;

    private String exampleCId;

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

    public ExampleB getExampleB() {
        return exampleB;
    }

    public void setExampleB(ExampleB exampleB) {
        this.exampleB = exampleB;
    }

    public Long getExampleNumber() {
        return exampleNumber;
    }

    public void setExampleNumber(Long exampleNumber) {
        this.exampleNumber = exampleNumber;
    }

    public String getExampleCId() {
        return exampleCId;
    }

    public void setExampleCId(String exampleCId) {
        this.exampleCId = exampleCId;
    }
}
