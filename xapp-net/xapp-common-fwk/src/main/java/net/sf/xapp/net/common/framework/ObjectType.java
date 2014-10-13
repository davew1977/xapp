package net.sf.xapp.net.common.framework;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public interface ObjectType {
    TransportObject create();

    int getId();
    String name();
}
