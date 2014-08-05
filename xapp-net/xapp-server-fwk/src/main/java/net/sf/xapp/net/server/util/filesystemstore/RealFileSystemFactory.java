/*
 *
 * Date: 2010-feb-26
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.util.filesystemstore;

import net.sf.xapp.net.server.clustering.NodeInfo;

public class RealFileSystemFactory implements FileSystemFactory
{
    @Override
    public FileSystem create(NodeInfo nodeInfo, String partitionId)
    {
        return new DirSizeLimitFileSystemDecorator(
                new RealFileSystem(nodeInfo.getBackupDir(), nodeInfo.getNodeAlias(), partitionId));
    }
}
