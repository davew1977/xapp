package net.sf.xapp.net.server.framework.persistendb;

import ngpoker.codegen.model.ComplexType;
import net.sf.xapp.net.common.framework.LispObj;
import ngpoker.common.types.Country;
import net.sf.xapp.net.server.framework.smartconverter.AbstractConverter;
import net.sf.xapp.net.server.framework.smartconverter.ConversionHelper;
import net.sf.xapp.net.server.framework.smartconverter.ConvertMeta;
import net.sf.xapp.net.server.framework.smartconverter.ConvertResult;
import net.sf.xapp.net.server.util.filesystemstore.FileContent;
import org.apache.log4j.Logger;

public class DiffDetectorConverter extends AbstractConverter implements FileContentConverter
{
    private final Logger log = Logger.getLogger(getClass());
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
                log.error(convertMeta);
                throw new RuntimeException();
            }
            log.info(type + " needed converting");
        }

        if (type.getName().equals("UserInfo"))
        {
            convertOldCountryName(convertMeta, obj);
        }
        return new ConvertResult<LispObj>(convertMeta.needsConversion(), obj);
    }

    private void convertOldCountryName(ConvertMeta convertMeta, LispObj obj)
    {
        String old = (String) obj.itemAt(4);
        String newC = null;

        if (old.equals("australia"))
        {
            newC = Country.Australia.name();
        }
        else if (old.equals("brazil"))
        {
            newC = Country.Brazil.name();
        }
        else if (old.equals("china"))
        {
            newC = Country.China.name();
        }
        else if (old.equals("denmark"))
        {
            newC = Country.Denmark.name();
        }
        else if (old.equals("equador"))
        {
            newC = Country.Ecuador.name();
        }
        else if (old.equals("france"))
        {
            newC = Country.France.name();
        }
        else if (old.equals("germany"))
        {
            newC = Country.Germany.name();
        }
        else if (old.equals("honduras"))
        {
            newC = Country.Honduras.name();
        }
        else if (old.equals("ireland"))
        {
            newC = Country.Ireland.name();
        }
        else if (old.equals("italy"))
        {
            newC = Country.Italy.name();
        }
        else if (old.equals("jamaica"))
        {
            newC = Country.Jamaica.name();
        }
        else if (old.equals("kuwait"))
        {
            newC = Country.Kuwait.name();
        }
        else if (old.equals("laos"))
        {
            newC = Country.Laos.name();
        }
        else if (old.equals("marocco"))
        {
            newC = Country.Morocco.name();
        }
        else if (old.equals("nigeria"))
        {
            newC = Country.Nigeria.name();
        }
        else if (old.equals("oman"))
        {
            newC = Country.Oman.name();
        }
        else if (old.equals("portugal"))
        {
            newC = Country.Portugal.name();
        }
        else if (old.equals("qata"))
        {
            newC = Country.Qatar.name();
        }
        else if (old.equals("romania"))
        {
            newC = Country.Romania.name();
        }
        else if (old.equals("spain"))
        {
            newC = Country.Spain.name();
        }
        else if (old.equals("sweden"))
        {
            newC = Country.Sweden.name();
        }
        else if (old.equals("taiwan"))
        {
            newC = Country.Taiwan.name();
        }
        else if (old.equals("uganda"))
        {
            newC = Country.Uganda.name();
        }
        else if (old.equals("uk"))
        {
            newC = Country.United_Kingdom.name();
        }
        else if (old.equals("us"))
        {
            newC = Country.United_States.name();
        }
        else if (old.equals("venezuela"))
        {
            newC = Country.Venezuela.name();
        }
        else if (old.equals("zimbabwe"))
        {
            newC = Country.Zimbabwe.name();
        }
        if(newC!=null)
        {
            convertMeta.addError("country name not found");
            obj.set(newC, 4);
        }
    }
}
