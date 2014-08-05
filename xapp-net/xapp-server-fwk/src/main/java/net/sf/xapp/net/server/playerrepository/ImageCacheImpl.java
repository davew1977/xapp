package net.sf.xapp.net.server.playerrepository;

import ngpoker.common.types.ImageData;
import ngpoker.common.types.PlayerId;
import net.sf.xapp.net.server.util.FileUtils;
import net.sf.xapp.net.server.util.SimpleCache;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

public class ImageCacheImpl extends SimpleCache<PlayerId, ImageData> implements ImageCache
{
    private final ImageData defaultImage;
    private final File imageDir;
    private static final int MAX_ENTRIES = 100;

    public ImageCacheImpl(String imageDir) throws URISyntaxException
    {
        super(MAX_ENTRIES);
        this.imageDir = new File(imageDir);
        this.imageDir.mkdirs();

        defaultImage = new ImageData(FileUtils.readStream(
                ImageCacheImpl.class.getResourceAsStream("/default_profile_image.jpg")));
    }


    @Override
    public ImageData load(PlayerId playerId)
    {
        ImageData image = get(playerId);
        if (image == null)
        {
            File file = imageFileName(playerId);
            if(file.exists())
            {
                List<Byte> data = FileUtils.readFile(file);
                image = new ImageData(data);
            }
            else
            {
                image = defaultImage;
            }
            put(playerId, image);
        }
        return image;
    }

    @Override
    public void save(PlayerId playerId, ImageData imageData)
    {
        FileUtils.writeFile(imageFileName(playerId), imageData.getData());
        if(containsKey(playerId))
        {
            put(playerId, imageData);
        }
    }

    private File imageFileName(PlayerId playerId)
    {
        return new File(imageDir, playerId.getValue() + ".jpg");
    }
}
