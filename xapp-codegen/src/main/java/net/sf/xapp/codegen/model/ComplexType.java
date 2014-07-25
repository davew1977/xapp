/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

import net.sf.xapp.annotations.application.Container;
import net.sf.xapp.annotations.objectmodelling.NamespaceFor;
import net.sf.xapp.annotations.objectmodelling.Reference;
import net.sf.xapp.annotations.objectmodelling.Transient;
import net.sf.xapp.annotations.objectmodelling.ValidImplementations;
import net.sf.xapp.utils.Filter;

import java.util.*;

import static java.lang.String.format;
import static net.sf.xapp.utils.CollectionsUtils.filter;
import static net.sf.xapp.utils.CollectionsUtils.zeroOrOne;
import static net.sf.xapp.codegen.model.FieldMatcher.NON_LIST_READ_ONLY;

@Container(listProperty = "fields")
@ValidImplementations({Entity.class, ValueObject.class, LobbyType.class})
@NamespaceFor(Field.class)
public abstract class ComplexType extends AbstractType
{
    private List<Field> m_fields = new ArrayList<Field>();
    private ComplexType m_superType;
    private boolean m_abstract;
    private List<ComplexType> subTypes = new ArrayList<ComplexType>();

    public ComplexType()
    {
    }

    public ComplexType(String name)
    {
        super(name);
    }

    public List<Field> getFields()
    {
        return m_fields;
    }

    public void setFields(List<Field> fields)
    {
        m_fields = fields;
    }

    @Reference
    public ComplexType getSuperType()
    {
        return m_superType;
    }

    public void setSuperType(ComplexType superType)
    {
        m_superType = superType;
    }

    /**
     * @return all the fields in the hierarchy
     */
    public List<Field> resolveFields(boolean includeSuperFields)
    {
        return resolveFieldsBasic(includeSuperFields);
    }

    public List<Field> resolveFieldsBasic(boolean includeSuperFields)
    {
        ArrayList<Field> fields = new ArrayList<Field>();
        fields.addAll(getFields());
        if(includeSuperFields && getSuperType()!=null)
        {
            fields.addAll(getSuperType().resolveFields(includeSuperFields));
        }
        return fields;
    }

    public Set<Type> listTypes()
    {
        LinkedHashSet<Type> s = new LinkedHashSet<Type>();
        for (Field field : resolveFields(true))
        {
            if(field.isList())
            {
                s.add(field.getType());
            }
        }
        return s;
    }

    public Field fieldWithProperty(String propKey, String propValue) {
        for (Field field : resolveFields(true)) {
            if(field.checkProp(propKey, propValue)) {
                return field;
            }
        }
        return null;
    }

    public Set<Type[]> mapTypes()
    {
        LinkedHashSet<Type[]> s = new LinkedHashSet<Type[]>();
        for (Field field : resolveFields(true))
        {
            if(field.isMap())
            {
                s.add(new Type[] {field.mapKeyType(), field.getType()});
            }
        }
        return s;
    }

    public boolean isAbstract()
    {
        return m_abstract;
    }

    public void setAbstract(boolean anAbstract)
    {
        m_abstract = anAbstract;
    }


    public List<Field> fields(FieldMatcher matcher)
    {
        return Field.filter(resolveFields(true), matcher);
    }

    public List<String> validate()
    {
        ArrayList<String> errors = new ArrayList<String>();
        if(getSuperType()!=null)
        {
            if(!getSuperType().isAbstract())
            {
                errors.add(format("Supertype %s of %s is not marked 'abstract'", getSuperType(), this));
            }
        }
        for (Field field : resolveFields(true))
        {
            List<String> subErrors = field.validate();
            for (String subError : subErrors) {
                errors.add(format("%s.%s %s", this, field, subError));
            }
            //todo check that keys in maps are primitive, enums or value objects
        }
        return errors;
    }

    public ComplexType clone() throws CloneNotSupportedException
    {
        ComplexType type = (ComplexType) super.clone();
        type.m_fields = new ArrayList<Field>();
        for (Field field : m_fields)
        {
            type.m_fields.add(field.clone());
        }
        return type;
    }

    public Collection<Type> fieldTypes()
    {
        LinkedHashSet<Type> s = new LinkedHashSet<Type>();
        for (Field field : m_fields)
        {
            s.add(field.getType());
        }
        return s;
    }

    public boolean hasSuper()
    {
        return getSuperType()!=null;
    }

    public void addField(Field field)
    {
        getFields().add(field);
    }

    public List<FieldSet> constructors()
    {
        List<FieldSet> fieldSets = new ArrayList<FieldSet>();
        fieldSets.add(new FieldSet(fields(NON_LIST_READ_ONLY)));
        return fieldSets;
    }

    public List<FieldSet> deepAnalyze()
    {
        List<FieldSet> fs = new ArrayList<FieldSet>();
        deepAnalyze(fs, new ArrayList<Field>());
        return fs;

    }
    public void deepAnalyze(List<FieldSet> fieldSets, List<Field> prependFields)
    {
        for (Field field : m_fields)
        {
            Type ft = field.getType();
            if(field.isWritable())
            {
                List<Field> fields = new ArrayList<Field>(prependFields);
                FieldSet fieldSet = new FieldSet(fields);
                fieldSet.add(field);
                fieldSets.add(fieldSet);
            }
            if (!field.isReference()) {
                if(ft instanceof ComplexType)
                {
                    ComplexType ct = (ComplexType) ft;
                    List<Field> fields = new ArrayList<Field>(prependFields);
                    fields.add(field);
                    ct.deepAnalyze(fieldSets, fields);
                }
            }
        }
    }

    public Collection<String> resolvePackages()
    {
        LinkedHashSet<String> h = new LinkedHashSet<String>();
        for (Type type : fieldTypes())
        {
            h.add(type.getPackageName());
        }
        return h;
    }

    public Type baseType()
    {
        if(m_superType!=null)
        {
            return m_superType.baseType();
        }
        return this;
    }

    public void addSubType(ComplexType complexType)
    {
        subTypes.add(complexType);
    }

    public boolean hasSubTypes()
    {
        return !subTypes.isEmpty();
    }

    @Transient
    public List<ComplexType> getSubTypes() {
        return subTypes;
    }

    public String uniqueObjectKey() {
        return getPackageName().replace(".", "_") + "_" + getName();
    }

    public Field keyField() {
        return zeroOrOne(filter(resolveFields(true), new Filter<Field>() {
            @Override
            public boolean matches(Field field) {
                return field.isKey();
            }
        }));
    }

    public boolean containsEntities() {
        for (Field field : resolveFields(true)) {
            if(field.containsEntities()) {
                return true;
            }
        }
        return false;
    }

    public boolean containsReferencedEntities() {
        Set<Entity> refTypes = referencedTypes();
        Set<Entity> entityTypes = entityTypes();
        return !Collections.disjoint(refTypes, entityTypes);
    }

    public boolean containsRefs() {
        for (Field field : resolveFields(true)) {
            if(field.isReference()) {
                return true;
            }
            if(field.getType() instanceof ComplexType) {
                ComplexType complexType = (ComplexType) field.getType();
                if(complexType.containsRefs()){
                    return true;
                }
            }
        }
        return false;
    }

    public Set<Entity> referencedTypes() { //todo fix, recursion
        Set<Entity> result = new HashSet<Entity>();
        for (Field field : resolveFields(true)) {
            result.addAll(field.referencedTypes());
        }
        return result;
    }

    public Set<Entity> entityTypes() {
        Set<Entity> result = new HashSet<Entity>();
        for (Field field : resolveFields(true)) {
            result.addAll(field.entityTypes());
        }
        return result;
    }

    public Field field(String fieldName) {

        for (Field field : resolveFields(true))
        {
            if(field.getName().equals(fieldName))
            {
                return field;
            }
        }
        return null;
    }


    public boolean hasField(String s)
    {
        return field(s) != null;
    }
}