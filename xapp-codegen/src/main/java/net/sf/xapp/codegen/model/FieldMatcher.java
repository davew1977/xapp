/*
 *
 * Date: 2010-jun-14
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

public enum FieldMatcher
{
    OPTIONAL
            {
                @Override
                public boolean matches(Field f)
                {
                    return f.isOptional();
                }
            },
    READ_ONLY
            {
                @Override
                public boolean matches(Field f)
                {
                    return f.isReadOnly();
                }
            },
    WRITEABLE
            {
                @Override
                public boolean matches(Field f)
                {
                    return f.isWritable();
                }
            },
    COLLECTION
            {
                @Override
                public boolean matches(Field f)
                {
                    return f.isList() || f.isMap();
                }
            },
    LIST
            {
                @Override
                public boolean matches(Field f)
                {
                    return f.isList();
                }
            },
    MAP
            {
                @Override
                public boolean matches(Field f)
                {
                    return f.isMap();
                }
            },
    NON_LIST_READ_ONLY
            {
                @Override
                public boolean matches(Field f)
                {
                    return READ_ONLY.matches(f) && !LIST.matches(f);
                }
            },
    NON_UNIQUE_READ_ONLY
            {
                @Override
                public boolean matches(Field f)
                {
                    return READ_ONLY.matches(f) && !f.isUnique() && f.isMandatory();
                }
            };

    public abstract boolean matches(Field f);
}
