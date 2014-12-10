package org.zenbeam.example.entity;

public class ExampleA extends BaseExample {

    private String name;

    private ExampleB exampleB;

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
}
