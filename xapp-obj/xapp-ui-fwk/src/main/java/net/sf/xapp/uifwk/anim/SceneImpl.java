/*
 *
 * Date: 2011-feb-24
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk.anim;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.xapp.uifwk.ReflectionUtils;
import net.sf.xapp.uifwk.XPane;

public class SceneImpl implements Scene, ActionListener
{
    private static final int FPS = 70;
    public String name = "";

    private Map<String, Integer> marks;
    private List<Sequence> sequences;
    private Timer timer;
    private boolean running;
    private int position;
    private int maxPosition;
    private boolean started;
    private Runnable finishedCallback;

    public SceneImpl()
    {
        marks = new HashMap<String, Integer>();
        sequences = new ArrayList<Sequence>();
        started = false;
        //add new seqence
        newSequence();
    }

    @Override
    public Scene newSequence()
    {
        sequences.add(new Sequence());
        position = 0;
        return this;
    }

    @Override
    public Scene loop()
    {
        currentSequence().loop = true;
        return this;
    }


    private Sequence currentSequence()
    {
        return sequences.get(sequences.size() - 1);
    }

    @Override
    public Scene pause(int duration)
    {
        addTask(new TaskTuple(new Function(), duration, Effect.NONE));
        return this;
    }

    @Override
    public Scene moveTo(Component comp, int x, int y, int duration, Effect effect)
    {
        addTask(new TaskTuple(new Mover(comp, null, new Point(x, y)), duration, effect));
        return this;
    }

    @Override
    public Scene move(XPane comp, int startX, int startY, int endX, int endY, int duration, Effect effect)
    {
        addTask(new TaskTuple(new Mover(comp, new Point(startX, startY), new Point(endX, endY)), duration, effect));
        return this;
    }

    @Override
    public Scene rotate(XPane comp, float startAngle, float endAngle, int duration, Effect effect)
    {
        addTask(new TaskTuple(new Rotator(comp, startAngle, endAngle), duration, effect));
        return this;
    }

    @Override
    public Scene fadeTo(XPane comp, float alpha, int duration)
    {
        addTask(new TaskTuple(new Fader(comp, -1, alpha), duration, Effect.NONE));
        return this;
    }

    @Override
    public Scene fade(XPane comp, float startAlpha, float endAlpha, int duration)
    {
        addTask(new TaskTuple(new Fader(comp, startAlpha, endAlpha), duration, Effect.NONE));
        return this;
    }

    public Scene fadeIn(XPane comp)
    {
        return fade(comp, 0, 1, 250);
    }

    public Scene fadeOut(XPane comp)
    {
        return fade(comp, 1, 0, 250);
    }

    @Override
    public Scene addFuntion(Function function, int duration, Effect effect, Object... args)
    {
        addTask(new TaskTuple(function, duration, effect, args));
        return this;
    }

    @Override
    public Scene addFuntion(final Object target, final String method, final Object... args)
    {
        ReflectionUtils.checkMethodExists(target.getClass(), method, args);
        addFuntion(new Function()
        {
            @Override
            public void run()
            {
                ReflectionUtils.call(target, method, args );
            }
        }, 0, Effect.NONE);
        return this;
    }

    @Override
    public Scene show(final XPane comp)
    {
        Function f = new Function()
        {
            @Override
            public void run()
            {
                comp.setVisible(true);
                comp.setDefaultAlpha(1f);
            }
        };
        addTask(new TaskTuple(f, 0, Effect.NONE));
        return this;
    }

    @Override
    public Scene hide(final XPane comp)
    {
        Function f = new Function()
        {
            @Override
            public void run()
            {
                comp.setVisible(false);
            }
        };
        addTask(new TaskTuple(f, 0, Effect.NONE));
        return this;
    }

    @Override
    public Scene resize(XPane comp, float startScale, float endScale, int duration, Effect effect, Point anchor)
    {
        addTask(new TaskTuple(new Resizer(comp, startScale, startScale, endScale, endScale, anchor.x, anchor.y), duration, effect));
        return this;
    }

    @Override
    public Scene resizeXY(XPane comp, float startScaleX, float startScaleY, float endScaleX, float endScaleY, int duration, Effect effect, Point anchor)
    {
        addTask(new TaskTuple(new Resizer(comp, startScaleX, startScaleY, endScaleX, endScaleY, anchor.x, anchor.y), duration, effect));
        return this;
    }

    @Override
    public Scene run(Task task, int duration, Effect effect)
    {
        addTask(new TaskTuple(task, duration, effect));
        return this;
    }

    @Override
    public Scene start()
    {
        if(!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    start();
                }
            });
        }
        else
        {
            if (running) return this;
            running = true;
            started = true;
            timer = new Timer(1000 / FPS, this);
            timer.start();
            long time = System.currentTimeMillis();
            for (Sequence sequence : sequences)
            {
                sequence.start(time);
            }
        }
        return this;
    }

    private void tick()
    {
        boolean keepAlive = false;
        long time = System.currentTimeMillis();
        for (Sequence sequence : sequences)
        {
            keepAlive |= sequence.tick(time);
        }
        if (!keepAlive)
        {
            stop();
        }
    }

    @Override
    public Scene stop()
    {
        if (!running) return this;

        running = false;

//            m_timer.stop();
//		    m_timer = null;
        releaseTimer();

        for (Sequence sequence : sequences)
        {
            sequence.stop();
        }
        if (finishedCallback != null) finishedCallback.run();
        return this;
    }

    @Override
    public Scene end()
    {
        if (!started)
        {
            start(); //if scene has never been started then start it
        }
        if (!running) return this; //if not running then there's no need to end it!

        running = false; //this flag will prevent it being ended twice
        releaseTimer();

        for (Sequence sequence : sequences)
        {
            sequence.end();
        }
        return this;
    }

    private void releaseTimer()
    {
        if (timer != null)
        {
            timer.stop();
            timer.removeActionListener(this);
            timer = null;
        }
    }

    @Override
    public Scene mark(String name)
    {
        marks.put(name, position);
        return this;
    }

    @Override
    public Scene newSequenceFrom(String name)
    {
        newSequence().pause(marks.get(name));
        return this;
    }

    @Override
    public Scene mergeScene(Scene s)
    {
        SceneImpl scene = (SceneImpl) s;
        assert !scene.running : "cannot merge a running scene";
        assert !scene.started : "cannot merge a scene that's been started";
        assert !running : "cannot merge into a running scene";
        assert !started : "cannot merge into a scene that's been started";

        for (int i = 0; i < scene.sequences.size(); i++)
        {
            Sequence seq = scene.sequences.get(i);
            seq.tasks.add(0, new TaskTuple(new Function(), position, Effect.NONE));
            sequences.add(seq);
        }
        maxPosition = Math.max(maxPosition, position + s.getTotalDuration());
        newSequence();
        return this;
    }

    @Override
    public int getTotalDuration()
    {
        return maxPosition;
    }

    public void setFinishedCallback(Runnable finishedCallback)
    {
        this.finishedCallback = finishedCallback;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        tick();
    }

    private void addTask(TaskTuple tt)
    {
        currentSequence().tasks.add(tt);
        position += tt.duration;
        maxPosition = Math.max(maxPosition, position);
    }
}
