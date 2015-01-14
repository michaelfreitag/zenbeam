package org.zenbeam.example.entity;

public class ExampleB extends BaseExample {

    private String name;

    private ExampleC exampleC;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExampleC getExampleC() {
        return exampleC;
    }

    public void setExampleC(ExampleC exampleC) {
        this.exampleC = exampleC;
    }
}
