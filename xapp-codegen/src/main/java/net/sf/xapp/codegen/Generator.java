/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen;

import net.sf.xapp.application.api.Launcher;
import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.codegen.editor.Editor;
import net.sf.xapp.codegen.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Generator
{
    private final GeneratorContext generatorContext;
    private final TypeGenerator typeGenerator;
    private final MessageGenerator messageGenerator;
    private final VersionFileGenerator versionFileGenerator;
    private final UniqueMessageEnumGenerator uniqueMessageEnumGenerator;
    private final ApiGenerator apiGenerator;
    private final DomainTypeGenerator domainTypeGenerator;
    private final TypeEnumGenerator typeEnumGenerator;
    private final EnumGenerator enumGenerator;
    private final ModelEnumGenerator modelEnumGenerator;
    private final LobbyStorableTypeGenerator lobbyStorableTypeGenerator;
    private final LobbyPropertyEnumGenerator lobbyPropertyEnumGenerator;

    public Generator()
    {
        generatorContext = new GeneratorContext(Boolean.getBoolean("light"));
        generatorContext.setModelFilePath(System.getProperty("model.file.path"));
        domainTypeGenerator = new DomainTypeGenerator(generatorContext);
        typeEnumGenerator = new TypeEnumGenerator(generatorContext);
        enumGenerator = new EnumGenerator(generatorContext);
        typeGenerator = new TypeGenerator(generatorContext, domainTypeGenerator, typeEnumGenerator, enumGenerator);
        versionFileGenerator = new VersionFileGenerator(generatorContext);
        messageGenerator = new MessageGenerator(generatorContext);
        uniqueMessageEnumGenerator = new UniqueMessageEnumGenerator(generatorContext);
        apiGenerator = new ApiGenerator(generatorContext);
        modelEnumGenerator = new ModelEnumGenerator(enumGenerator);
        lobbyStorableTypeGenerator = new LobbyStorableTypeGenerator(generatorContext);
        lobbyPropertyEnumGenerator = new LobbyPropertyEnumGenerator(generatorContext);
    }

    public Model loadModel() {
        return Model.loadModel(generatorContext);
    }

    public static void main(String[] args)
    {
        System.out.println(Arrays.asList(args));
        Generator generator = new Generator();
        generator.run(args);
    }

    public GeneratorContext getGeneratorContext() {
        return generatorContext;
    }

    public void run(String[] args)
    {
        if (args.length > 0 && args[0].equals("edit"))
        {
            Launcher.run(Model.class, new Editor(new EditorPlugin(), this), generatorContext.getModelFilePath());
        }
        else if (args.length > 0 && !args[0].equals("${arg}")) {
            Model model = Model.loadModel(generatorContext);
            for (String arg : args)
            {
                model.cdb.getInstance(Type.class, arg).setChangedInSession(true);
            }
            generateAndWrite(model, false);
        }
        else
        {
            Model model = Model.loadModel(generatorContext);
            generateAndWrite(model, true);
        }
    }

    public void generateAndWrite(Model model, boolean generateAll) {
        generateAndWrite(model, model.getModules(), generateAll);
    }

    public void generateAndWrite(Model model, List<Module> modules, boolean generateAll)
    {
        if(generateAll) {
            model.setAllArtifactsChanged(true);
        }
        generatorContext.setActiveModules(modules);
        writeFiles(generate(model));
    }

    public List<CodeFile> generate(Model model)
    {
        List<CodeFile> files = new ArrayList<CodeFile>();


        files.addAll(typeGenerator.generateTypes(model));

        List<TransientApi> apis = model.deriveApis();
        files.add(modelEnumGenerator.generateAllErrorEnum(model, apis));
        files.add(lobbyPropertyEnumGenerator.generate(model));

        files.add(modelEnumGenerator.generateAllApiEnum(model, apis));
        //files.add(ClientMessageParserGenerator.generate(apis));

        files.addAll(lobbyStorableTypeGenerator.generate(model));

        for (TransientApi api : apis)
        {
            if (api.isChangedInSession())
            {
                files.addAll(apiGenerator.genApiClasses(api));
                files.addAll(messageGenerator.genMessageClasses(api));
            }
        }

        files.add(versionFileGenerator.genVersionFile(model));

        files.add(uniqueMessageEnumGenerator.generate(model, apis));
        files.add(uniqueMessageEnumGenerator.generateObjectTypeEnum(model));

        return files;
    }


    public static void writeFiles(List<CodeFile> files)
    {
        String filter = System.getProperty("filter");
        for (CodeFile file : files)
        {
            if (filter==null || file.getName().matches(filter)) {
                file.generate();
            }
        }
    }

    static String FWK_PACKAGE_NAME()
    {
        return "net.sf.xapp.net.common.framework";
    }

    public List<CodeFile> genApiClasses(List<TransientApi> transientApiList)
    {
        return apiGenerator.genApiClasses(transientApiList);
    }

    public CodeFile genDomainClass(ComplexType type)
    {
        return domainTypeGenerator.genDomainClass(type);
    }

    public CodeFile genInMessageClass(Api api, Message type)
    {
        return messageGenerator.genInMessageClass(api, type);
    }

    public CodeFile genTypeEnum(Model model, ComplexType type)
    {
        return typeEnumGenerator.genTypeEnum(model, type);
    }

    public CodeFile genEnum(EnumType enumType)
    {
        return enumGenerator.genEnum(enumType);
    }

    public CodeFile generateAllErrorEnum(Model model, List<TransientApi> transientApiList)
    {
        return modelEnumGenerator.generateAllErrorEnum(model, transientApiList);
    }

    public CodeFile generateAllApiEnum(Model model, List<TransientApi> transientApiList)
    {
        return modelEnumGenerator.generateAllApiEnum(model, transientApiList);
    }

    public CodeFile generateStorableType(LobbyType lobbyType)
    {
        return lobbyStorableTypeGenerator.generateStorableType(lobbyType);
    }

    public CodeFile generateUniqueMessageEnum(Model model, List<TransientApi> transientApiList)
    {
        return uniqueMessageEnumGenerator.generate(model,  transientApiList);
    }

    public CodeFile generateLobbyPropertyEnum(Model model)
    {
        return lobbyPropertyEnumGenerator.generate(model);
    }

    public CodeFile generateUniqueObjectEnum(Model model) {
        return uniqueMessageEnumGenerator.generateObjectTypeEnum(model);
    }
}