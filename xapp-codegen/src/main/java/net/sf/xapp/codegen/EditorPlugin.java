/*
 *
 * Date: 2010-jun-02
 * Author: davidw
 *
 */
package net.sf.xapp.codegen;

import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.codegen.editor.GeneratorPlugin;
import net.sf.xapp.codegen.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditorPlugin implements GeneratorPlugin
{
    private final Generator generator;

    public EditorPlugin()
    {
        generator = new Generator();
    }

    @Override
    public List<CodeFile> generate(Model model, Node node)
    {
        ArrayList<CodeFile> files = new ArrayList<CodeFile>();
        if (node.wrappedObject() instanceof Api)
        {
            Api api = node.wrappedObject();
            List<TransientApi> transientApiList = api.deriveApis();
            return generator.genApiClasses(transientApiList);
        }
        else if (node.wrappedObject() instanceof Message)
        {
            Message type = node.wrappedObject();
            return Arrays.asList(generator.genInMessageClass((Api) node.getParent().wrappedObject(), type));

        }
        else if (node.wrappedObject() instanceof ComplexType)
        {
            ComplexType type = node.wrappedObject();
            files.add(generator.genDomainClass(type));
            /*if (type.isAbstract())
            {
                files.add(generator.genTypeEnum(model, type));
            }*/
        }
        else if (node.wrappedObject() instanceof Model)
        {
            List<TransientApi> transientApiList = model.deriveApis();
            files.add(generator.generateAllErrorEnum(model, transientApiList));
            files.add(generator.generateAllApiEnum(model, transientApiList));
            files.add(generator.generateUniqueMessageEnum(model, transientApiList));
            files.add(generator.generateUniqueObjectEnum(model));
            files.add(generator.generateLobbyPropertyEnum(model));
            return files;
        }
        if(node.wrappedObject() instanceof EnumType)
        {
            EnumType enumType = node.wrappedObject();
            files.add(generator.genEnum(enumType));
        }
        if(node.wrappedObject() instanceof Entity)
        {
            Entity entity = node.wrappedObject();
            if (entity.isObservable())
            {
                files.addAll(generator.genApiClasses(model.observableApis(entity)));
            }
        }
        if(node.wrappedObject() instanceof LobbyType)
        {
            LobbyType lobbyType = node.wrappedObject();
            if (!lobbyType.isAbstract())
            {
                files.add(generator.generateStorableType(lobbyType));
            }
        }
        if(node.isA(Module.class)) {
            Module module = node.wrappedObject();
        }
        return files;
    }
}
