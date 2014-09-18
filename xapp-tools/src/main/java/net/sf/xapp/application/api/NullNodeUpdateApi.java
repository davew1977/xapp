package net.sf.xapp.application.api;

import net.sf.xapp.objectmodelling.core.*;

import java.nio.charset.Charset;
import java.util.List;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class NullNodeUpdateApi implements NodeUpdateApi {
    @Override
    public void updateObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {

    }

    @Override
    public void updateObjects(List<ObjectMeta> objectMetas, List<PropertyUpdate> potentialUpdates) {

    }

    @Override
    public void createObject(ObjectLocation homeLocation, ClassModel type, ObjCreateCallback callback) {

    }

    @Override
    public void moveOrInsertObjMeta(ObjectLocation objectLocation, ObjectMeta objMeta) {

    }


    @Override
    public void initObject(Node parentNode, ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {

    }

    @Override
    public PropertyChange initObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {
        return null;
    }

    @Override
    public void deleteObject(ObjectMeta objectMeta) {

    }

    @Override
    public void moveInList(ObjectLocation objectLocation, ObjectMeta objectMeta, int delta) {

    }

    @Override
    public void changeType(ObjectMeta obj, ClassModel targetClassModel) {
    }

    @Override
    public void insertObject(ObjectLocation objectLocation, Object obj) {

    }

    @Override
    public ObjectMeta deserializeAndInsert(ObjectLocation objectLocation, ClassModel classModel, String xml, Charset charset) {
        return null;
    }

    @Override
    public void updateReferences(ObjectLocation objectLocation, List<ObjectMeta> refsToAdd, List<ObjectMeta> refsToRemove) {

    }
}
