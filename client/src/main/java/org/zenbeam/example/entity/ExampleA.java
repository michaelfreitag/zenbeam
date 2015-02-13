package org.zenbeam.example.entity;

import java.util.List;

public class ExampleA extends BaseExample {

    private String name;

    private ExampleB exampleB;

    private List<ExampleB> exampleBList;

    private String exampleNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExampleB getExampleB() {
        return exampleB;
    }

    public void setExampleB(ExampleB exampleB) {
        this.exampleB = exampleB;
    }

    public List<ExampleB> getExampleBList() {
        return exampleBList;
    }

    public void setExampleBList(List<ExampleB> exampleBList) {
        this.exampleBList = exampleBList;
    }

    public String getExampleNumber() {
        return exampleNumber;
    }

    public void setExampleNumber(String exampleNumber) {
        this.exampleNumber = exampleNumber;
    }
}
