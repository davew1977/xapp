package net.sf.xapp.examples.school.model;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class Pet {
    private Animal animal;
    private String name;

    public Animal getAnimal() {
        return animal;
    }

    public void setAnimal(Animal animal) {
        this.animal = animal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " the " + animal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pet pet = (Pet) o;

        if (animal != pet.animal) return false;
        if (name != null ? !name.equals(pet.name) : pet.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = animal != null ? animal.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
