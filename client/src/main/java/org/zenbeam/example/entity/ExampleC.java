package org.zenbeam.example.entity;

import java.util.List;

public class ExampleC extends BaseExample {

    private String name;

    private List<ExampleA> exampleAList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ExampleA> getExampleAList() {
        return exampleAList;
    }

    public void setExampleAList(List<ExampleA> exampleAList) {
        this.exampleAList = exampleAList;
    }
}
