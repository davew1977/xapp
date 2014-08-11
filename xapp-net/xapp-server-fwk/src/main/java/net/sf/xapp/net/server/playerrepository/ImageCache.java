package net.sf.xapp.net.server.playerrepository;

import ngpoker.common.types.ImageData;
import net.sf.xapp.net.common.types.UserId;

public interface ImageCache
{
    ImageData load(UserId userId);
    void save(UserId userId, ImageData imageData);
}
