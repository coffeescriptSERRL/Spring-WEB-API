package com.lob.springboot_api.entity;

import java.io.Serializable;

public class CreateForm implements Serializable {

    private String Id;
    private String name;
    private String Organ;

    public String getId() {
        return Id;
    }

    public String getName() {
        return name;
    }

    public String getOrgan() {
        return Organ;
    }

    public void setId(String id) {
        Id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrgan(String organ) {
        Organ = organ;
    }
}