package net.sf.xapp.codegen.model;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.NamespaceFor;
import net.sf.xapp.annotations.objectmodelling.TreeMeta;
import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.application.utils.codegen.JavaFile;
import net.sf.xapp.objectmodelling.core.Namespace;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.tree.Tree;
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
@NamespaceFor(FileMeta.class)
public class Module {
    private ObjectMeta objMeta;
    private String name;
    private String outDir;
    private Tree apiTree = new Tree("Apis");
    private List<Package> packages = new ArrayList<Package>();
    private DirMeta src;

    private void setObjMeta(ObjectMeta objMeta) {
        this.objMeta = objMeta;
    }

    @Key
    public String getName() {
        return name;
    }

    public Model model() {
        return (Model) objMeta.root().getInstance();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOutDir() {
        return outDir;
    }

    public void setOutDir(String outDir) {
        this.outDir = outDir;
    }

    public List<Package> getPackages() {
        return packages;
    }

    public void setPackages(List<Package> packages) {
        this.packages = packages;
    }

    public DirMeta getSrc() {
        return src;
    }

    public void setSrc(DirMeta src) {
        this.src = src;
    }

    @TreeMeta(leafTypes = {Tree.class, Api.class})
    public Tree getApiTree() {
        return apiTree;
    }

    public void setApiTree(Tree apiTree) {
        this.apiTree = apiTree;
    }

    @Override
    public String toString() {
        return name;
    }

    public List<Api> allApis() {
        return src.all(Api.class);
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
        for (Package aPackage : packages)
        {
            for (Entity entity : aPackage.entities())
            {
                if (entity.isObservable())
                {
                    results.addAll(observableApis(entity, typeLookup));
                }
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
        String[] s = name.split("-");
        StringBuilder sb = new StringBuilder();
        for (String c : s) {
            sb.append(StringUtils.capitalizeFirst(c));
        }
        sb.append("MsgTypeEnum");
        return sb.toString();
    }
}
