package net.sf.xapp.net.server.framework.persistendb;

import net.sf.xapp.codegen.model.ComplexType;
import net.sf.xapp.net.common.framework.LispObj;
import net.sf.xapp.net.server.framework.smartconverter.AbstractConverter;
import net.sf.xapp.net.server.framework.smartconverter.ConversionHelper;
import net.sf.xapp.net.server.framework.smartconverter.ConvertMeta;
import net.sf.xapp.net.server.framework.smartconverter.ConvertResult;
import net.sf.xapp.net.server.util.filesystemstore.FileContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffDetectorConverter extends AbstractConverter implements FileContentConverter
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final boolean suppressConversion;

    public DiffDetectorConverter(boolean suppressConversion)
    {
        super("", "");
        this.suppressConversion = suppressConversion;
    }

    @Override
    public ConvertResult<FileContent> convert(FileContent oldContent)
    {
        if (suppressConversion)
        {
            return new ConvertResult<FileContent>(false, oldContent);
        }
        return super.convert(oldContent);
    }

    @Override
    public ConvertResult<LispObj> convert(LispObj obj, ComplexType type)
    {
        ConvertMeta convertMeta = ConversionHelper.analyze(type, obj);
        if (convertMeta.needsConversion())
        {
            if(type.getName().equals("Store")) {
                obj.add(new LispObj("[]"));
            }
            else
            {
                log.error(convertMeta.toString());
                throw new RuntimeException();
            }
            log.info(type + " needed converting");
        }


        return new ConvertResult<LispObj>(convertMeta.needsConversion(), obj);
    }

}
