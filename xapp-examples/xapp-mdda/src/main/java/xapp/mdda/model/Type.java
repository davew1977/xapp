package xapp.mdda.model;

import net.sf.xapp.annotations.objectmodelling.ValidImplementations;

@ValidImplementations({PrimitiveType.class, EnumType.class, Entity.class, ValueObject.class, LobbyType.class})
public interface Type extends Cloneable, Artifact {

}
