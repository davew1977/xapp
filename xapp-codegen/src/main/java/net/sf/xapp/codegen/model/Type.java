/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.ValidImplementations;

import java.util.List;

@ValidImplementations({PrimitiveType.class, EnumType.class, Entity.class, ValueObject.class, LobbyType.class})
public interface Type extends Cloneable, Artifact
{
    @Key
    String getName();

    List<String> validate();

    String getPackageName();

    void setPackageName(String aPackage);

}