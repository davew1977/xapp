package net.sf.xapp.net.common.framework;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public interface ObjectType {
    TransportObject create();

    int getId();
    String name();
}
