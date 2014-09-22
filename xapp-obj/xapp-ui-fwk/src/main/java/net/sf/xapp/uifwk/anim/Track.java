package net.sf.xapp.uifwk.anim;

import java.util.LinkedList;

public class Track
{
    private LinkedList<Scene> scenes;

    public Track()
    {
        scenes = new LinkedList<Scene>();
    }

    public void queueScene(final SceneImpl scene)
    {
        scene.setFinishedCallback(new Runnable()
        {
            @Override
            public void run()
            {
                scenes.removeFirst();
                if(!scenes.isEmpty())
                {
                    scenes.getFirst().start();
                }
            }
        });
        if(scenes.isEmpty())
        {
            scene.start();
        }
        scenes.add(scene);
    }

    public void endAll()
    {
        while(!scenes.isEmpty())
        {
            scenes.removeFirst().end();
        }
    }
}
