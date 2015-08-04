/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

import net.sf.xapp.annotations.application.Container;
import net.sf.xapp.annotations.application.Hide;
import net.sf.xapp.annotations.objectmodelling.PostInit;
import net.sf.xapp.annotations.objectmodelling.Reference;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ClassModelManager;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.utils.CollectionsUtils;
import net.sf.xapp.utils.Filter;
import net.sf.xapp.codegen.GeneratorContext;
import net.sf.xapp.codegen.mixins.GenHelper;

import java.util.*;
import java.util.regex.Pattern;

@Container(listProperty = "Modules")
public class Model {
    public ClassDatabase<Model> cdb;
    private String m_version;
    private String m_name;
    private List<Module> modules = new ArrayList<Module>();
    private List<ObjectId> messageIds = new ArrayList<ObjectId>();
    private List<ObjectId> objectIds = new ArrayList<ObjectId>();
    private int messageIdStart;
    private int objectIdStart;
    private String corePackageName;
    private boolean xappPluginEnabled;
    private boolean generateSetters;
    private Module baseModule;
    private List<String> generalErrors = new ArrayList<String>();

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public boolean isGenerateSetters() {
        return generateSetters;
    }

    public void setGenerateSetters(boolean generateSetters) {
        this.generateSetters = generateSetters;
    }

    public String getVersion() {
        return m_version;
    }

    public void setVersion(String version) {
        m_version = version;
    }

    public String getCorePackageName() {
        return corePackageName;
    }

    public void setCorePackageName(String corePackageName) {
        this.corePackageName = corePackageName;
    }

    @Hide
    public List<ObjectId> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(List<ObjectId> messageIds) {
        this.messageIds = messageIds;
    }

    @Hide
    public List<ObjectId> getObjectIds() {
        return objectIds;
    }

    public void setObjectIds(List<ObjectId> objectIds) {
        this.objectIds = objectIds;
    }

    public List<String> validate() {
        ArrayList<String> errors = new ArrayList<String>();
        for (Module module : modules) {
            module.validate(errors);
        }
        return errors;
    }

    public List<Api> allApis() {
        List<Api> result = new ArrayList<Api>();
        for (Module module : modules) {
            result.addAll(module.allApis());
        }
        return result;
    }

    @PostInit
    public void init(ObjectMeta<Model> objectMeta) {
        for (ComplexType complexType : complexTypes()) {
            if (complexType.getSuperType() != null) {
                complexType.getSuperType().addSubType(complexType);
            }
        }
        updateMessageIds();
        updateObjectIds();
    }

    public Set<ComplexType> findSubTypes(ComplexType superType) {
        LinkedHashSet<ComplexType> subTypes = new LinkedHashSet<ComplexType>();
        List<ComplexType> complexTypes = complexTypes();
        for (ComplexType ct : complexTypes) {
            if (superType.equals(ct.getSuperType())) {
                subTypes.add(ct);
            }
        }
        return subTypes;
    }

    public List<ComplexType> concreteTypes() {
        return CollectionsUtils.filter(complexTypes(), new Filter<ComplexType>() {
            @Override
            public boolean matches(ComplexType complexType) {
                return !complexType.isAbstract();
            }
        });
    }

    public List<ComplexType> complexTypes() {
        return all(ComplexType.class);
    }

    public Map<String, Type> createTypeLookUp() {
        Map<String, Type> m = new HashMap<String, Type>();
        for (Type type: all(Type.class)) {
            m.put(type.getName(), type);
            //add possible list mappings
            m.put(GenHelper.listTypeDecl(type), type);
            //add extra mapping for primitives
            if (type instanceof PrimitiveType) {
                PrimitiveType primitiveType = (PrimitiveType) type;
                if (primitiveType.getJavaMapping() != null) {
                    m.put(primitiveType.getJavaMapping(), type);
                }
            }
        }
        return m;
    }

    public List<TransientApi> deriveApis() {
        Map<String, Type> typeLookup = createTypeLookUp();
        ArrayList<TransientApi> results = new ArrayList<TransientApi>();
        for (Module module : modules) {
            results.addAll(module.deriveApis(typeLookup));
        }
        return results;
    }

    public List<TransientApi> observableApis(Entity entity) {
        return Module.observableApis(entity, createTypeLookUp());
    }

    public Collection<LobbyType> lobbyTypes() {
        return all(LobbyType.class);
    }

    public Map<String, ComplexType> deriveAllTypes() {
        Map<String, ComplexType> types = new HashMap<String, ComplexType>();
        for (ComplexType complexType : complexTypes()) {
            ComplexType put = types.put(complexType.className(), complexType);
            assert put == null;
        }
        for (TransientApi api : deriveApis()) {
            for (Message message : api.getMessages()) {
                Message m = new Message();
                m.setModule(api.getModule());
                m.setApi(api);
                m.setName(message.getName());
                m.setPackageName(message.getPackageName());
                m.setFields(message.resolveFields(true));
                m.setSuperType(message.getSuperType());

                ComplexType put = types.put(message.className(), m);
                assert put == null;
            }
        }
        return types;
    }

    public void setAllArtifactsChanged(boolean b) {
        for (Artifact artifact : all(Artifact.class)) {
            artifact.setChangedInSession(b);
        }
    }

    public Map<String, ObjectId> updateMessageIds() {

        return updateIds(messageIds, getMessageIdStart(), deriveAllMessages());
    }

    public List<Message> deriveAllMessages() {
        List<TransientApi> apis = deriveApis();
        List<Message> messages = new ArrayList<Message>();
        for (TransientApi api : apis) {
            messages.addAll(api.allMessagesAndResponses());
        }
        return messages;
    }

    public Map<String, ObjectId> updateObjectIds() {
        return updateIds(objectIds, getObjectIdStart(), concreteTypes());
    }

    private static Map<String, ObjectId> updateIds(List<ObjectId> objectIds, int objIdStart, List<? extends ComplexType> types) {
        IntIdManager ids = new IntIdManager(objIdStart);
        Map<String, ObjectId> objectIdMap = createObjectIdMap(objectIds);
        for (ObjectId objectId : objectIdMap.values()) {
            ids.replace(objectId.getId());
        }
        objectIds.clear();
        for (ComplexType complexType : types) {
            String key = complexType.uniqueObjectKey();
            ObjectId objectId = objectIdMap.remove(key);
            if (objectId == null) {
                objectId = new ObjectId(key, ids.next());
                objectIds.add(objectId);
            } else {
                objectIds.add(objectId);
            }
        }
        for (ObjectId objectId : objectIdMap.values()) {
            objectIds.remove(objectId);
        }
        Collections.sort(objectIds);
        return createObjectIdMap(objectIds);
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    private static Map<String, ObjectId> createObjectIdMap(List<ObjectId> objectIds) {
        Map<String, ObjectId> objectIdMap = new HashMap<String, ObjectId>();
        for (ObjectId objectId : objectIds) {
            objectIdMap.put(objectId.getName(), objectId);
        }
        return objectIdMap;
    }

    public static Model loadModel(GeneratorContext generatorContext) {
        ClassDatabase<Model> cdb = new ClassModelManager<Model>(Model.class);
        Model model = cdb.getRootUnmarshaller().unmarshalURL(generatorContext.getModelFilePath()).getInstance();
        model.cdb = cdb;
        return model;
    }

    public List<Artifact> search(String text) {

        Pattern pattern = Pattern.compile(".*" + text + ".*", Pattern.CASE_INSENSITIVE);
        List<Artifact> result = new ArrayList<Artifact>();
        for (Artifact artifact : all(Artifact.class)) {
            if (pattern.matcher(artifact.getName()).matches()) {
                result.add(artifact);
            }
        }
        return result;
    }

    public <A > List<A> all(Class<A> type) {
        List<A> result = new ArrayList<A>();
        for (Module module : modules) {
            result.addAll(module.all(type));
        }
        return result;
    }

    public <A > List<A> all(Filter filter) {
        List<A> result = new ArrayList<A>();
        for (Module module : modules) {
            result.addAll((List)module.all(filter));
        }
        return result;
    }

    @Reference
    public Module getBaseModule() {
        return baseModule;
    }

    public void setBaseModule(Module baseModule) {
        this.baseModule = baseModule;
    }

    public int getMessageIdStart() {
        return messageIdStart;
    }

    public void setMessageIdStart(int messageIdStart) {
        this.messageIdStart = messageIdStart;
    }

    public int getObjectIdStart() {
        return objectIdStart;
    }

    public void setObjectIdStart(int objectIdStart) {
        this.objectIdStart = objectIdStart;
    }

    public boolean isXappPluginEnabled() {
        return xappPluginEnabled;
    }

    public void setXappPluginEnabled(boolean xappPluginEnabled) {
        this.xappPluginEnabled = xappPluginEnabled;
    }

    public List<String> getGeneralErrors() {
        return generalErrors;
    }

    public void setGeneralErrors(List<String> generalErrors) {
        this.generalErrors = generalErrors;
    }
}