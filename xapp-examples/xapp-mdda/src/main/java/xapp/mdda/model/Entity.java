/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package xapp.mdda.model;

import net.sf.xapp.annotations.objectmodelling.Transient;

import java.util.List;

public class Entity extends ComplexType
{
    private boolean rootType;
    private ObservableMeta observableMeta;

    public boolean isRootType()
    {
        return rootType;
    }

    public void setRootType(boolean rootType)
    {
        this.rootType = rootType;
    }

    @Transient
    public boolean isObservable()
    {
        return observableMeta != null;
    }

    /*public void setObservable(boolean observable)
    {
        this.observable = observable;
        if(observable) {
            observableMeta = new ObservableMeta();
        }
    }*/

    @Override
    public List<String> validate() {

        List<String> errors = super.validate();

        /*for (Field field : resolveFields(true))
        {
        }*/
        Field keyField = keyField();
        if(keyField != null) {
            if(!keyField.getName().equals("key") || !keyField.getType().getName().equals("String")) {
                errors.add(className() + ": Currently, primary key fields must be named 'key' and be of type String");
            }
        }
        return errors;
    }

    public ObservableMeta getObservableMeta() {
        return observableMeta;
    }

    public void setObservableMeta(ObservableMeta observableMeta) {
        this.observableMeta = observableMeta;
    }

    public Field fieldByName(String s)
    {
        for (Field field : resolveFields(true))
        {
            if(field.getName().equals(s))
            {
                return field;
            }
        }
        return null;
    }

}