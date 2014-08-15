package net.sf.xapp.net.server.framework.persistendb;

import net.sf.xapp.net.server.framework.persistendb.converters.Converter2to3;
import net.sf.xapp.net.server.framework.smartconverter.ConvertResult;
import net.sf.xapp.net.server.framework.smartconverter.Converter;
import net.sf.xapp.net.server.util.filesystemstore.FileContent;
import xapp.Version;

import java.util.HashMap;
import java.util.Map;

public class StrictFileContentConverter implements FileContentConverter
{
    private Map<ConverterKey, Converter> converters;

    public StrictFileContentConverter()
    {
        converters = new HashMap<ConverterKey, Converter>();

        converters.put(new ConverterKey("2","3"), new Converter2to3());
    }

    @Override
    public ConvertResult<FileContent> convert(FileContent oldContent)
    {
        if(oldContent.getVersion().equals(Version.VERSION))
        {
            return new ConvertResult<FileContent>(false, oldContent);
        }
        return converters.get(new ConverterKey(oldContent.getVersion(), Version.VERSION)).convert(oldContent);
    }

    private static class ConverterKey
    {
        final String src;
        final String target;

        private ConverterKey(String src, String target)
        {
            this.src = src;
            this.target = target;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConverterKey that = (ConverterKey) o;

            if (!src.equals(that.src)) return false;
            if (!target.equals(that.target)) return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = src.hashCode();
            result = 31 * result + target.hashCode();
            return result;
        }
    }
}
