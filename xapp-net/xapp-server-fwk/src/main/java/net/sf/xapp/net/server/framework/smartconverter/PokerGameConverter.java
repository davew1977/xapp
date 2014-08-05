/*
 *
 * Date: 2011-feb-18
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework.smartconverter;

import ngpoker.Version;
import ngpoker.codegen.model.ComplexType;
import ngpoker.common.framework.LispObj;
import net.sf.xapp.net.server.util.filesystemstore.FileContent;
import net.sf.xapp.net.server.util.filesystemstore.FileSystem;
import net.sf.xapp.net.server.util.filesystemstore.RealFileSystem;

import java.util.List;

public class PokerGameConverter extends AbstractConverter
{
    protected PokerGameConverter()
    {
        super("2", "2");
    }

    @Override
    public ConvertResult<LispObj> convert(LispObj obj, ComplexType type)
    {
        ConvertMeta convertMeta = ConversionHelper.analyze(type, obj);
        if (convertMeta.needsConversion())
        {
            if(type.getName().equals("PublicPlayer"))
            {
               obj.insert("false",14);
            }
        }
        return new ConvertResult<LispObj>(convertMeta.needsConversion(), obj);
    }



    public static void main(String[] args)
    {
        FileSystem fileSystem = new RealFileSystem("C:/dev/svn-stuff/ng-poker-copy/src/product/testdata/cashgame");

        PokerGameConverter pokerGameConverter = new PokerGameConverter();

        List<String> files = fileSystem.filePaths();
        for (String file : files)
        {
            if(file.endsWith("snapshot"))
            {
                System.out.println(file);
                FileContent fileContent = fileSystem.readFile(file);
                FileContent converted = pokerGameConverter.convert(fileContent).getTarget();
                fileSystem.createFile(file, Version.VERSION + "," + converted.getRootType(), converted.firstLine());
            }
        }

    }
}
