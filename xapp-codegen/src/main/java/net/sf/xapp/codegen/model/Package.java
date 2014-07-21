/*
 *
 * Date: 2010-jun-10
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

import net.sf.xapp.annotations.application.Container;

import java.util.ArrayList;
import java.util.List;


@Container(listProperty = "types")
public class Package implements Cloneable
{
    private Module module;
    private String name;
    private String packageName;
    private List<Type> types = new ArrayList<Type>();

    public void init() {
        assert module!=null;
        init(module);
    }

    public void init(Module module)
    {
        this.module = module;
        for (Type type : types)
        {
            type.setPackageName(packageName);
            type.setModule(module);
        }
    }

    public String getPackageName()
    {
        return packageName;
    }

    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Type> getTypes()
    {
        return types;
    }

    public void setTypes(List<Type> types)
    {
        this.types = types;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public void validate(List<String> errors)
    {
        for (Type type : types)
        {
            errors.addAll(type.validate());
        }
    }

    public List<ComplexType> complexTypes()
    {
        return types(ComplexType.class);
    }

    public List<EnumType> enumTypes()
    {
        return types(EnumType.class);
    }

    public List<Entity> entities()
    {
        return types(Entity.class);
    }

    public List<LobbyType> lobbyTypes()
    {
        return types(LobbyType.class);
    }


    public <T> List<T> types(Class<T> cl)
    {
        return types(types, cl);
    }

    public static <T> List<T> types(List<? extends Type> types, Class<T> cl)
    {
        ArrayList<T> results = new ArrayList<T>();
        for (Type type : types)
        {
            if (cl==null || cl.isInstance(type))
            {
                results.add((T) type);
            }
        }
        return results;
    }

    @Override
    public Package clone() throws CloneNotSupportedException {
        Package clone = (Package) super.clone();
        clone.types = new ArrayList<Type>();
        return clone;
    }
}
