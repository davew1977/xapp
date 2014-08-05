package net.sf.xapp.net.server.playerrepository;

import ngpoker.common.types.ImageData;
import ngpoker.common.types.PlayerId;

public interface ImageCache
{
    ImageData load(PlayerId playerId);
    void save(PlayerId playerId, ImageData imageData);
}
