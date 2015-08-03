package net.sf.xapp.codegen.model;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.application.utils.codegen.JavaFile;
import net.sf.xapp.utils.StringUtils;
import net.sf.xapp.codegen.mixins.GenericMixIn;
import net.sf.xapp.codegen.mixins.ObserverAPIMixin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by dwebber
 */
public class Module extends DirMeta {
    private String outDir;


    public Model model() {
        return (Model) objMeta.root().getInstance();
    }

    public String getOutDir() {
        return outDir;
    }

    public void setOutDir(String outDir) {
        this.outDir = outDir;
    }

    public List<Api> allApis() {
        return all(Api.class);
    }

    public List<Entity> allEntities() {
        return all(Entity.class);
    }

    public File outDir() {
        return new File(getOutDir());
    }

    public List<TransientApi> deriveApis(Map<String, Type> typeLookup) {
        ArrayList<TransientApi> results = new ArrayList<TransientApi>();
        for (Api api : allApis())
        {
            results.addAll(api.deriveApis());
        }
        for (Entity entity : allEntities()) {

            if (entity.isObservable())
            {
                results.addAll(observableApis(entity, typeLookup));
            }
        }
        return results;
    }


    public static List<TransientApi> observableApis(Entity entity, Map<String, Type> typeLookup)
    {
        ArrayList<TransientApi> results = new ArrayList<TransientApi>();
        TransientApi updaterApi = ObserverAPIMixin.createApi(createObserverUpdater(entity), typeLookup, TransientApiType.DEFAULT);
        updaterApi.setPackageName(entity.getPackageName());
        updaterApi.setChangedInSession(entity.isChangedInSession());
        updaterApi.setEntityKeyType(new PrimitiveType("String"));
        updaterApi.setHideEntityKey(true);
        updaterApi.init(entity.getModule());
        results.add(updaterApi);
        TransientApi listenerApi = ObserverAPIMixin.createApi(createObserverListener(entity), typeLookup, TransientApiType.ENTITY);
        listenerApi.setEntityKeyType(new PrimitiveType("String"));
        listenerApi.setHideEntityKey(true);
        listenerApi.setPackageName(entity.getPackageName());
        listenerApi.setClientVisible(true);
        listenerApi.setChangedInSession(entity.isChangedInSession());
        listenerApi.init(entity.getModule());
        results.addAll(listenerApi.deriveApis());
        return results;
    }


    public static CodeFile createObserverListener(Entity e)
    {
        CodeFile cf = new JavaFile(null, false);
        cf.setInterface();
        new GenericMixIn(e.getPackageName()).mixIn(e.getName() + "Listener", cf);
        new ObserverAPIMixin(true).mixIn(e, cf);
        return cf;
    }

    public static CodeFile createObserverUpdater(Entity e)
    {
        CodeFile cf = new JavaFile(null, false);
        cf.setInterface();
        new GenericMixIn(e.getPackageName()).mixIn(e.getName() + "Update", cf);
        new ObserverAPIMixin(false).mixIn(e, cf);
        return cf;
    }

    public String messageTypeEnumName() {
        String[] s = getName().split("-");
        StringBuilder sb = new StringBuilder();
        for (String c : s) {
            sb.append(StringUtils.capitalizeFirst(c));
        }
        sb.append("MsgTypeEnum");
        return sb.toString();
    }

    public void validate(ArrayList<String> errors) {
        for (Artifact artifact : all(Artifact.class)) {
            artifact.validate(errors);
        }
    }
}
