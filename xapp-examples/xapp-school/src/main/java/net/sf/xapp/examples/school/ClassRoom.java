package net.sf.xapp.examples.school;

import net.sf.xapp.annotations.application.Container;
import net.sf.xapp.annotations.objectmodelling.ContainsReferences;
import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.NamespaceFor;
import net.sf.xapp.annotations.objectmodelling.Reference;

import java.util.List;
import java.util.Set;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
@NamespaceFor(FileMeta.class)
public class ClassRoom implements FileSystem{
    private String name;
    private Teacher teacher;
    private List<Pupil> pupils;
    private DirMeta homeDir = new DirMeta("docs");
    private Set<Pet> pets;

    @Key
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Reference
    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    @ContainsReferences
    public List<Pupil> getPupils() {
        return pupils;
    }

    public void setPupils(List<Pupil> pupils) {
        this.pupils = pupils;
    }

    public DirMeta getHomeDir() {
        return homeDir;
    }

    public void setHomeDir(DirMeta homeDir) {
        this.homeDir = homeDir;
    }

    public Set<Pet> getPets() {
        return pets;
    }

    public void setPets(Set<Pet> pets) {
        this.pets = pets;
    }

    @Override
    public String toString() {
        return name;
    }
}
