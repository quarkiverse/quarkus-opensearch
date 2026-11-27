package io.quarkiverse.opensearch.it;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Fruit {

    public String id;
    public String name;
    public String color;

    public Fruit() {
    }

    public Fruit(String id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }
}
