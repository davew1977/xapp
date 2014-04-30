package net.sf.xapp.annotations.objectmodelling;

/**
 * annotation for a property whose type is Map. If this annotation is omitted the key property will be the property
 * marked as {@link @Key}
 */
public @interface MapKey {
    String keyProperty();
}
