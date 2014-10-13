package net.sf.xapp.objclient.ui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import net.sf.xapp.net.common.types.UserId;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class Helper {
    private static Map<UserId, Image> profileImageCache = new HashMap<UserId, Image>();

    public static final Color[] colors = new Color[]{
            new Color(153, 153, 255, 180),
            new Color(255, 153, 255, 180),
            new Color(255, 204, 153, 180),
            new Color(153, 255, 153, 180),
            new Color(204, 204, 204, 180),
            new Color(255, 255, 102, 180),
            new Color(255, 102, 255, 180),
    };
    public static Font defaultFont = Font.decode("Tahoma-PLAIN-10");

    public static Image getProfileImage(UserId userId) {
        Image image = profileImageCache.get(userId);
        if(image == null) {
            image = new ImageIcon(new ImageIcon(Helper.class.getResource("/images/"+userId.getValue()+".jpg")).getImage().getScaledInstance(25,25, Image.SCALE_SMOOTH)).getImage();
            profileImageCache.put(userId, image);
        }
        return image;
    }
}
