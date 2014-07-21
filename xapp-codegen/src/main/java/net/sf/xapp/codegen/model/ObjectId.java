package net.sf.xapp.codegen.model;

import net.sf.xapp.annotations.application.NotEditable;

/**
 * Encapsulates ...
 */
public class ObjectId implements Comparable<ObjectId>{
    private String name;
    private int id;

    public ObjectId(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public ObjectId() {
    }
    @NotEditable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotEditable
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id + ": " + name;
    }

    @Override
    public int compareTo(ObjectId o) {
        Integer id1 = id;
        return id1.compareTo(o.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ObjectId messageId = (ObjectId) o;

        if (id != messageId.id) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
