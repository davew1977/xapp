package net.sf.xapp.codegen.model;

import java.util.TreeSet;

/**
 * Encapsulates ...
 */
public class IntIdManager {
    int startId;
    TreeSet<Integer> ids = new TreeSet<Integer>();

    public IntIdManager(int startId) {
        this.startId = startId;
    }

    public int next() {
        int newId = startId;
        for (Integer id : ids) {
            if (id > newId) {
                break;
            }
            newId++;
        }

        ids.add(newId);
        return newId;
    }

    public void replace(int i) {
        boolean alreadyThere = !ids.add(i);
        if (alreadyThere) {
            throw new IllegalArgumentException("int was already there! " + i);
        }
    }
}
