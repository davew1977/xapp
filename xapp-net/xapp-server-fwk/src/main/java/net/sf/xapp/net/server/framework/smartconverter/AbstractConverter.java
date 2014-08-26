/*
 *
 * Date: 2011-feb-18
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework.smartconverter;

import net.sf.xapp.Global;
import net.sf.xapp.codegen.model.ComplexType;
import net.sf.xapp.codegen.model.Model;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.net.common.framework.LispObj;
import net.sf.xapp.net.common.framework.TransportObject;
import net.sf.xapp.net.server.util.filesystemstore.FileContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConverter implements Converter
{
    private final String srcVersion;
    private final String targetVersion;

    private static final Logger log = LoggerFactory.getLogger(AbstractConverter.class);
    private SmartConverter smartConverter;

    protected AbstractConverter(String srcVersion, String targetVersion)
    {
        this.srcVersion = srcVersion;
        this.targetVersion = targetVersion;
        smartConverter = new SmartConverter(false);
    }


    @Override
    public ConvertResult<LispObj> convert(LispObj obj, ComplexType type)
    {
        ConvertMeta convertMeta = ConversionHelper.analyze(type, obj);
        if (convertMeta.needsConversion())
        {
            log.info(convertMeta.toString());
        }

        return new ConvertResult<LispObj>(false, obj);
    }

    @Override
    public String getTargetVersion()
    {
        return targetVersion;
    }

    @Override
    public String getSourceVersion()
    {
        return srcVersion;
    }

    @Override
    public ConvertResult<FileContent> convert(FileContent oldContent)
    {
        List<String> lines = new ArrayList<String>();
        ConvertResult<LispObj> result = smartConverter.convert(oldContent.getLines().get(0), oldContent.getRootType(), this);
        boolean converted = result.isConverted();
        String newLine = result.getTarget().serialize();
        lines.add(newLine);

        for (int i = 1; i < oldContent.getLines().size(); i++)
        {
            String line = oldContent.getLines().get(i);
            String[] args = line.split(",", 2);
            Class<? extends TransportObject> aClass = Global.create(args[0]).getClass();
            result = smartConverter.convert(args[1], aClass.getName(), this);
            converted |= result.isConverted();
            newLine = result.getTarget().serialize();
            if(newLine!=null)
            {
                lines.add(args[0]+","+newLine);
            }
        }
        return new ConvertResult<FileContent>(converted, new FileContent(oldContent.getSrcFile(), getTargetVersion(), oldContent.getBaseSeqNo(), oldContent.getRootType(), lines));
    }

    public static Model loadModel()
    {
        return Unmarshaller.load(Model.class, "classpath:///domain-model.xml");
    }
}
