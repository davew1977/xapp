package net.sf.xapp.net.server.framework.persistendb.converters;

import net.sf.xapp.net.server.clustering.NodeInfoImpl;
import ngpoker.codegen.model.ComplexType;
import ngpoker.common.framework.LispObj;
import net.sf.xapp.net.server.framework.smartconverter.AbstractConverter;
import net.sf.xapp.net.server.framework.smartconverter.ConvertResult;
import net.sf.xapp.net.server.util.filesystemstore.*;

public class Converter2to3 extends AbstractConverter
{
    public Converter2to3()
    {
        super("2", "3");
    }

    @Override
    public ConvertResult<LispObj> convert(LispObj obj, ComplexType type)
    {
        if(type.getName().equals("UserEntity"))
        {
            obj.add(new LispObj("[0,0]"));
            obj.add("0");
            obj.add("0");
            obj.add("0");
        }
        return new ConvertResult<LispObj>(true, obj);
    }


    public static void main(String[] args)
    {
        FileSystemFactory fsf= new RealFileSystemFactory();
        FileSystem fileSystem = fsf.create(new NodeInfoImpl(0, "C:\\dev\\svn-stuff\\ng-poker-copy\\src\\product\\_NG_BACKUP"), "users");

        FileContent fileContent = fileSystem.readFileFromKey("davew1977");

        new Converter2to3().convert(fileContent);
    }
}
