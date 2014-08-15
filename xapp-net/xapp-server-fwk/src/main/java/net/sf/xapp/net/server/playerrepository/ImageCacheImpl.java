package net.sf.xapp.net.server.playerrepository;

import net.sf.xapp.net.common.types.ImageData;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.server.util.FileUtils;
import net.sf.xapp.net.server.util.SimpleCache;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

public class ImageCacheImpl extends SimpleCache<UserId, ImageData> implements ImageCache
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
    public ImageData load(UserId userId)
    {
        ImageData image = get(userId);
        if (image == null)
        {
            File file = imageFileName(userId);
            if(file.exists())
            {
                List<Byte> data = FileUtils.readFile(file);
                image = new ImageData(data);
            }
            else
            {
                image = defaultImage;
            }
            put(userId, image);
        }
        return image;
    }

    @Override
    public void save(UserId userId, ImageData imageData)
    {
        FileUtils.writeFile(imageFileName(userId), imageData.getData());
        if(containsKey(userId))
        {
            put(userId, imageData);
        }
    }

    private File imageFileName(UserId userId)
    {
        return new File(imageDir, userId.getValue() + ".jpg");
    }
}
