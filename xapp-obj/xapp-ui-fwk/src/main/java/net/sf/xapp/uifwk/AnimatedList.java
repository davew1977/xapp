package net.sf.xapp.uifwk;


import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.xapp.uifwk.anim.Effect;
import net.sf.xapp.uifwk.anim.Scene;

public class AnimatedList<E extends Comparable> extends XPane
{
    private AnimatedListRenderer renderer;
    private Map<E, ListItemSprite<E>> spriteMap;
    private List<E> data;
    final int width;
    final int rowHeight;


    public AnimatedList(int width, int rowHeight)
    {
        this.width = width;
        this.rowHeight = rowHeight;
        data = new ArrayList<E>();
        spriteMap = new HashMap<E, ListItemSprite<E>>();
        renderer = new DefaulAnimatedListRenderer();
        setSize(width, 0);
    }

    public void setRenderer(AnimatedListRenderer renderer)
    {
        this.renderer = renderer;
    }

    public void init(List<E> items)
    {
        data.clear();
        spriteMap.clear();
        removeAll();
        for (E item : items)
        {
            add(item);
        }
    }

    public void animateShow()
    {
        Scene s = newScene();
        s.fadeIn(this);
        for (int i = 0; i < data.size(); i++)
        {
            ListItemSprite<E> sprite = spriteMap.get(data.get(i));
            s.move(sprite, 0,0, 0,i * rowHeight,1000, Effect.DECELERATE);
            s.newSequence();
        }
        s.start();
    }

    public void reposition()
    {
        for (int i = 0; i < data.size(); i++)
        {
            ListItemSprite<E> sprite = spriteMap.get(data.get(i));
            sprite.setLocation(0, i*rowHeight);
        }
    }

    public void add(E item)
    {
        ListItemSprite<E> lis = new ListItemSprite<E>(item);
        add(lis);
        data.add(item);
        spriteMap.put(item, lis);
        Collections.sort(data);
        resize();
    }

    public Scene remove(E item)
    {
        int index = data.indexOf(item);
        data.remove(item);
        ListItemSprite<E> removedSprite = spriteMap.remove(item);
        Scene scene = newScene();
        scene.fadeOut(removedSprite).addFuntion(this, "removeRow", removedSprite);
        for (int i = index; i < data.size(); i++)
        {
            E row = data.get(i);
            ListItemSprite<E> sprite = spriteMap.get(row);
            scene.newSequence().moveTo(sprite, 0, sprite.getLocation().y - rowHeight, 500, Effect.BOTH);
        }
        scene.addFuntion(this, "setSize", getWidth(), data.size() * rowHeight);
        return scene;
    }

    public void removeRow(ListItemSprite<E> row)
    {
        remove(row);
    }

    @Override
    public void setDefaultAlpha(float defaultAlpha)
    {
        super.setDefaultAlpha(defaultAlpha);
        for (ListItemSprite<E> sprite : spriteMap.values())
        {
            sprite.setDefaultAlpha(defaultAlpha);
        }
    }

    public void sort()
    {
        List<E> before = new ArrayList<E>(data);
        Collections.sort(data);
        List<E> after = data;
        if(before.equals(after)) {
            repaint();
            return;
        }

        Scene scene = newScene();

        for (int i = 0; i < data.size(); i++)
        {
            E item = after.get(i);
            if (before.indexOf(item) != i)
            {
                ListItemSprite<E> sprite = spriteMap.get(item);
                scene.newSequence().moveTo(sprite, 0, i * rowHeight, 1000, Effect.BOTH);
            }
        }
        scene.start();

    }

    private void resize()
    {
        setSize(getWidth(), data.size() * rowHeight);
    }

    public void animateHide()
    {
        newScene().fadeOut(this).start();
    }


    private class ListItemSprite<E> extends XPane
    {
        private E data;

        private ListItemSprite(E data)
        {
            this.data = data;
            setSize(width, rowHeight);
        }

        @Override
        protected void paintPane(Graphics2D g)
        {
            renderer.render(g, this, data);
        }
    }

    static class DefaulAnimatedListRenderer implements AnimatedListRenderer
    {
        @Override
        public void render(Graphics2D g, XPane comp, Object data)
        {
            g.setPaint(new GradientPaint(0, 0, Color.gray, 0, 10, Color.yellow));
            int h = comp.getHeight();
            g.fillRoundRect(0, 0, comp.getWidth(), h, h, h);
            g.setColor(Color.black);
            g.drawString(data.toString(), 15, 16);
        }
    }

    static class Person implements Comparable<Person>
    {
        String name;
        Integer age;

        Person(String name, Integer age)
        {
            this.name = name;
            this.age = age;
        }

        @Override
        public int compareTo(Person o)
        {
            return age.compareTo(o.age);
        }

        @Override
        public String toString()
        {
            return name + ": " + age;
        }
    }
}
