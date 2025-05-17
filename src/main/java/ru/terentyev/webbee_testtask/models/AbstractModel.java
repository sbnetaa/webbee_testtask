package ru.terentyev.webbee_testtask.models;

import java.util.UUID;

public abstract class AbstractModel {

    protected UUID id;

    public AbstractModel() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
