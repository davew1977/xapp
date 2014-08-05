package net.sf.xapp.net.server.framework.persistendb;

import net.sf.xapp.net.server.framework.smartconverter.ConvertResult;
import net.sf.xapp.net.server.util.filesystemstore.FileContent;

public interface FileContentConverter
{
    ConvertResult<FileContent> convert(FileContent oldContent);
}
