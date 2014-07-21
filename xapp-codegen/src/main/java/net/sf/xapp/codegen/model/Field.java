/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

import net.sf.xapp.annotations.application.EditorWidget;
import net.sf.xapp.annotations.marshalling.FormattedText;
import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.Reference;
import net.sf.xapp.annotations.objectmodelling.Transient;
import net.sf.xapp.application.editor.widgets.FreeTextPropertyWidget;
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
    private String m_name;
    private Type m_type;
    private Type mapKeyType;
    private String mapKeyField;
    private boolean m_list;
    private boolean preventDuplicates;
    private boolean m_optional;
    private Access m_access = Access.READ_ONLY;
    private boolean m_transient;
    private boolean m_unique;
    private boolean reference;
    private boolean key;
    private String defaultValue;
    private String properties;

    public Field()
    {
    }

    public Field(String name, Type type)
    {
        m_name = name;
        m_type = type;
    }

    public Field(String name, String dummyTypeName, Class<? extends Type> aClass)
    {
        m_name = name;
        m_type = ReflectionUtils.newInstance(aClass, dummyTypeName);
    }

    @Key
    public String getName()
    {
        return m_name;
    }

    public void setName(String name)
    {
        m_name = name;
    }

    @Reference
    public Type getType()
    {
        return m_type;
    }

    public void setType(Type type)
    {
        m_type = type;
    }

    @Reference
    public Type getMapKeyType()
    {
        return mapKeyType;
    }

    public void setMapKeyType(Type mapKeyType)
    {
        this.mapKeyType = mapKeyType;
    }

    public String getMapKeyField() {
        return mapKeyField;
    }

    public void setMapKeyField(String mapKeyField) {
        this.mapKeyField = mapKeyField;
    }

    public boolean isPreventDuplicates()
    {
        return preventDuplicates;
    }

    public void setPreventDuplicates(boolean preventDuplicates)
    {
        this.preventDuplicates = preventDuplicates;
    }

    @Transient
    public boolean isCollection() {
        return isMap() || isList();
    }
    @Transient
    public boolean isMap() {

        return mapKeyType != null || mapKeyField != null;
    }

    public boolean isList()
    {
        return m_list;
    }

    public void setList(boolean list)
    {
        m_list = list;
    }

    public String toString()
    {
        return (isSet() ? String.format("Set<%s>", m_type) :
                isList() ? m_type + "[]":
                        isMap() ? String.format("Map<%s,%s>",mapKeyType, m_type) :
                                m_type ) + " " + m_name;
    }

    public boolean isOptional()
    {
        return m_optional;
    }

    public void setOptional(boolean optional)
    {
        m_optional = optional;
    }

    public Access getAccess()
    {
        return m_access;
    }

    public void setAccess(Access access)
    {
        m_access = access;
    }

    @Transient
    public boolean isReadOnly()
    {
        return m_access== Access.READ_ONLY;
    }

    @Transient
    public boolean isWritable()
    {
        return m_access== Access.READ_WRITE;
    }

    @Transient
    public boolean isMandatory()
    {
        return !isOptional();
    }

    @EditorWidget(FreeTextPropertyWidget.class)
    @FormattedText
    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public Field clone() throws CloneNotSupportedException
    {
        return (Field) super.clone();
    }

    public boolean isTransient()
    {
        return m_transient;
    }

    public void setTransient(boolean aTransient)
    {
        m_transient = aTransient;
    }

    public static List<Field> filter(List<Field> fields, FieldMatcher matcher)
    {

        ArrayList<Field> f = new ArrayList<Field>();
        for (Field field : fields)
        {
            if(matcher.matches(field))
            {
                f.add(field);
            }
        }
        return f;
    }

    public String typeName()
    {
        return getType().getName();
    }

    public String genIndexVarName()
    {
        return isCollection() ? depluralise(getName()) + (isList() ? "Index" : "Key"): "";
    }

    public boolean isUnique()
    {
        return m_unique;
    }

    public void setUnique(boolean unique)
    {
        m_unique = unique;
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

    public String genIndexTypeName()
    {
        return isList() ? "Integer" : mapKeyType().getName();
    }

    public Type mapKeyType() {
        return getMapKeyType() !=null ? mapKeyType : mapKeyField().getType();
    }

    private Field mapKeyField() {
        return ((ComplexType)m_type).field(mapKeyField);
    }

    @Transient
    public boolean isSet()
    {
        return isList() && isPreventDuplicates();
    }

    public boolean isReference() {
        return reference;
    }

    public void setReference(boolean reference) {
        this.reference = reference;
    }

    public boolean isKey() {
        return key;
    }

    public void setKey(boolean key) {
        this.key = key;
    }

    public PropertyMap properties() {
        return new PropertyMap(properties);
    }

    public boolean is(String propKey) {
        return checkProp(propKey, "true");
    }
    public boolean checkProp(String propKey, String propValue) {
        String prop = properties().get(propKey);
        return prop !=null && prop.equals(propValue);
    }

    public String get(String propName) {
        return properties().get(propName);
    }

    public String accessorName() {
        return AbstractCodeFile.getterName(getType().getName(), getName());
    }

    public String modifierName() {
        return AbstractCodeFile.setterName(getName());
    }

    public boolean containsReferences() {
        if(m_type instanceof ComplexType) {
            ComplexType complexType = (ComplexType) m_type;
            return complexType.containsRefs();
        }
        return false;
    }

    public String loopStart(CodeFile ct) {
        String var = getName();
        if(isCollection()) {
            var = "item";
            ct.startBlock("for(%s %s : %s%s)", typeName(), var, getName(), isMap() ? ".values()" : "");
        }
        return var;
    }

    public void loopEnd(CodeFile ct) {
        if(isCollection()) {
            ct.endBlock();
        }
    }

    public boolean containsEntities() {
        if(m_type instanceof ComplexType) {
            ComplexType complexType = (ComplexType) m_type;
            return isEntityDeclaration()  || complexType.containsEntities();
        }
        return false;
    }

    public boolean isEntityDeclaration() {
        return m_type instanceof Entity && !isReference();
    }

    public boolean isReferencedEntityDeclaration(ComplexType parentType) {
        return isEntityDeclaration() && parentType.referencedTypes().contains((Entity) m_type);
    }

    public Set<Entity> entityTypes() {
        Set<Entity> result = new HashSet<Entity>();
        if(isEntityDeclaration()) {
            result.add((Entity) m_type);
        }
        if(containsEntities()) {
            result.addAll(((ComplexType) m_type).entityTypes());
        }
        return result;
    }

    public Set<Entity> referencedTypes() {
        Set<Entity> result = new HashSet<Entity>();
        if(isReference()) {
            result.add((Entity) m_type);
        } else if(containsReferences()) {
            result.addAll(((ComplexType) m_type).referencedTypes());
        }

        return result;
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<String>();

        if(isMap() && isList()) {
            errors.add("cannot be both a map and a list");
        }
        if(mapKeyField != null && mapKeyType != null) {
            errors.add("EITHER MapKeyField OR MapKeyType should be set, not both");

        }
        if(mapKeyField != null) {
            if(! (m_type instanceof ComplexType)) {
                errors.add("MapKeyField set but type is not complex");
                return errors;
            }
            ComplexType complexType = (ComplexType) m_type;
            if(!complexType.hasField(mapKeyField)) {
                errors.add("field type does not have MapKeyField");
            }
        }
        return errors;
    }
}