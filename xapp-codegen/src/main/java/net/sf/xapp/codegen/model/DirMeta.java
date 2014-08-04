package net.sf.xapp.codegen.model;

import net.sf.xapp.annotations.application.Container;
import net.sf.xapp.annotations.objectmodelling.NamespaceFor;
import net.sf.xapp.utils.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: davidw
 * Date: 5/8/14
 * Time: 7:25 AM
 * To change this template use File | Settings | File Templates.
 */
@NamespaceFor(FileMeta.class)
@Container(listProperty = "Files")
public class DirMeta extends FileMeta {
    private List<FileMeta> files;

    public DirMeta(String name) {
        super(name);
    }

    public DirMeta() {
    }

    public List<FileMeta> getFiles() {
        return files;
    }

    public void setFiles(List<FileMeta> files) {
        this.files = files;
    }

    @Override
    public DirMeta clone() throws CloneNotSupportedException {
        DirMeta dirMeta = (DirMeta) super.clone();
        if(files != null) {
            dirMeta.files  = new ArrayList<FileMeta>();
            for (FileMeta file : files) {
                String oldName = file.getName();
                FileMeta clone = file.clone();
                clone.setName(oldName);
                dirMeta.files.add(clone);
            }
        }
        return dirMeta;
    }

    public <T> List<T> all(final Class<T> typeFilter) {
        return all(new Filter<T>() {
            @Override
            public boolean matches(T fileMeta) {
                return typeFilter.isInstance(fileMeta);
            }
        });
    }

    public <T> List<T> all(Filter filter) {
        List result = new ArrayList();
        for (FileMeta file : files) {
            if(file instanceof DirMeta) {
                result.addAll(((DirMeta) file).all(filter));
            } else if(filter.matches(file)) {
                result.add(file);
            }
        }
        if (filter.matches(this)) {
            result.add(this);
        }
        return (List<T>) result;
    }
}
