/*
 *
 * Date: 2010-sep-10
 * Author: davidw
 *
 */
package net.sf.xapp.codegen;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.codegen.mixins.GenericMixIn;
import net.sf.xapp.codegen.model.Field;
import net.sf.xapp.codegen.model.FieldMatcher;
import net.sf.xapp.codegen.model.LobbyType;
import net.sf.xapp.codegen.model.Model;

import java.util.ArrayList;
import java.util.List;

public class LobbyStorableTypeGenerator
{
    private final GeneratorContext generatorContext;

    public LobbyStorableTypeGenerator(GeneratorContext generatorContext)
    {
        this.generatorContext = generatorContext;
    }

    public List<CodeFile> generate(Model model)
    {
        List<CodeFile> cfs = new ArrayList<CodeFile>();
        //generate an enum for all error codes
        for (LobbyType lobbyType : model.lobbyTypes())
        {
            if (!lobbyType.isAbstract() && lobbyType.shouldGenerate())
            {
                cfs.add(generateStorableType(lobbyType));
            }
        }
        return cfs;
    }

    public CodeFile generateStorableType(LobbyType lobbyType)
    {
        CodeFile cf = generatorContext.createJavaFile(lobbyType);
        new GenericMixIn(lobbyType.getPackageName()).mixIn(lobbyType.getName() + "Type", cf);
        cf.addImplements("StorableType");
        cf.addImport("net.sf.xapp.server.framework.memdb.StorableType");
        cf.addImport("java.util.*");
        cf.method("getPropertyNames", "List<String>");
        StringBuilder sb = new StringBuilder();
        List<Field> fieldList = lobbyType.fields(FieldMatcher.NON_UNIQUE_READ_ONLY);
        for (int i = 0; i < fieldList.size(); i++)
        {
            Field field = fieldList.get(i);
            sb.append('"').append(field.getName()).append('"');
            if (i < fieldList.size() - 1)
            {
                sb.append(',');
            }
        }
        cf.line("return Arrays.asList(%s)", sb);
        return cf;
    }
}
