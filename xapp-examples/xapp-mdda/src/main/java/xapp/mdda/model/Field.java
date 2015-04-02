/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package xapp.mdda.model;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.Reference;
import net.sf.xapp.annotations.objectmodelling.Transient;
import net.sf.xapp.application.utils.codegen.AbstractCodeFile;
import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.sf.xapp.utils.StringUtils.depluralise;

public class Field implements Cloneable
{
    private String name;
    private Type type;
    private Type mapKeyType;
    private String mapKeyField;
    private List<FieldTag> tags;
    private String defaultValue;

    public Field()
    {
    }

    public Field(String name, Type type)
    {
        this.name = name;
        this.type = type;
    }

    public Field(String name, String dummyTypeName, Class<? extends Type> aClass)
    {
        this.name = name;
        type = ReflectionUtils.newInstance(aClass, dummyTypeName);
    }

    @Key
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Reference
    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }


    @Transient
    public boolean isCollection() {
        return isMap() || isList();
    }
    @Transient
    public boolean isMap() {

        return mapKeyType != null || mapKeyField != null;
    }

    public boolean isAnyOf(FieldTag... tags) {
        for (FieldTag tag : tags) {
            if(is(tag)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAllOf(FieldTag... tags) {
        for (FieldTag tag : tags) {
            if(!is(tag)) {
                return false;
            }
        }
        return true;
    }

    public boolean is(FieldTag tag) {
        return tags.contains(tag);
    }

    public boolean isList()
    {
        return is(FieldTag.LIST);
    }

    public String toString()
    {
        return (isSet() ? String.format("Set<%s>", type) :
                isList() ? type + "[]":
                        isMap() ? String.format("Map<%s,%s>",mapKeyType, type) :
                                type) + " " + name;
    }

    @Transient
    public boolean isOptional()
    {
        return is(FieldTag.OPTIONAL);
    }


    @Transient
    public boolean isReadOnly()
    {
        return is(FieldTag.READ_ONLY);
    }

    @Transient
    public boolean isWritable()
    {
        return is(FieldTag.READ_WRITE);
    }

    @Transient
    public boolean isMandatory()
    {
        return !isOptional();
    }


    public Field clone() throws CloneNotSupportedException
    {
        return (Field) super.clone();
    }

    @Transient
    public boolean isTransient()
    {
        return is(FieldTag.TRANSIENT);
    }


    public boolean isUnique()
    {
        return is(FieldTag.UNIQUE);
    }


    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    @Transient
    public boolean isCollectionOrComplex()
    {
        return isComplex() || isCollection();
    }

    @Transient
    public boolean isComplex()
    {
        return getType() instanceof ComplexType;
    }

    @Transient
    public boolean isSet()
    {
        return is(FieldTag.SET);
    }

    @Transient
    public boolean isReference() {
        return is(FieldTag.REFERENCE);
    }

    @Transient
    public boolean isKey() {
        return is(FieldTag.KEY);
    }

    @Transient
    public boolean isContainsReferences() {
        return is(FieldTag.REFERENCE_CONTAINER);
    }

    public Type getMapKeyType() {
        return mapKeyType;
    }

    public void setMapKeyType(Type mapKeyType) {
        this.mapKeyType = mapKeyType;
    }

    public String getMapKeyField() {
        return mapKeyField;
    }

    public void setMapKeyField(String mapKeyField) {
        this.mapKeyField = mapKeyField;
    }

    public List<FieldTag> getTags() {
        return tags;
    }

    public void setTags(List<FieldTag> tags) {
        this.tags = tags;
    }

    public List<String> validate(List<String> errors) {

        if(isMap() && isList()) {
            errors.add("cannot be both a map and a list");
        }
        if(mapKeyField != null && mapKeyType != null) {
            errors.add("EITHER MapKeyField OR MapKeyType should be set, not both");

        }
        if(mapKeyField != null) {
            if(! (type instanceof ComplexType)) {
                errors.add("MapKeyField set but type is not complex");
                return errors;
            }
            ComplexType complexType = (ComplexType) type;
            if(!complexType.hasField(mapKeyField)) {
                errors.add("field type does not have MapKeyField");
            }
        }
        return errors;
    }
}