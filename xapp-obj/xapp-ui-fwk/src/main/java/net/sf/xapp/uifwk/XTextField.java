/*
 *
 * Date: 2010-okt-14
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;




import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class XTextField extends JTextField
{
    private Color background = Color.white;

    /**
     * @param text
     */
    public XTextField(String text)
    {
        super();

        setOpaque(false);
        setText(text);
        setFont(new Font("Tahoma", Font.BOLD, 10));
        setForeground(Color.BLACK);
        setWidth(70);
        setUI(new XTextFieldUI(this));
        setBorder(new EmptyBorder(0,5,0, 5));
    }

    private void setWidth(int width)
    {
        setSize(new Dimension(width, 20));
        setPreferredSize(new Dimension(width, 20));
    }

    public XTextField size(int width, int height)
    {
        setSize(new Dimension(width, height));
        setPreferredSize(new Dimension(width, height));
        return this;
    }

    public XTextField width(int width)
    {
        setWidth(width);
        return this;
    }

    public XTextField fontSize(float size)
    {
        Font f = getFont();
        setFont(f.deriveFont(size));
        return this;
    }

    public XTextField bold(boolean bold)
    {
        Font f = getFont();
        setFont(f.deriveFont(bold ? Font.BOLD : Font.PLAIN));
        return this;
    }

    public XTextField background(Color color)
    {
        background = color;
        return this;
    }

    public XTextField foreground(Color color)
    {
        setForeground(color);
        return this;
    }

    public XTextField location(int x, int y)
    {
        setLocation(x, y);
        return this;
    }

    public void addListener(final Object listener, final String method, final Object... args)
    {
        ReflectionUtils.checkMethodExists(listener.getClass(), method, args);
        addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ReflectionUtils.call(listener, method, args);
            }
        });
    }

    public void addCaretListener(final Object listener, final String method, final Object... args)
    {
        ReflectionUtils.checkMethodExists(listener.getClass(), method, args.length==0 ? new Object[]{""} : args);
        addCaretListener(new CaretListener()
        {
            @Override
            public void caretUpdate(CaretEvent e)
            {
                ReflectionUtils.call(listener, method, args.length==0 ? new Object[]{getText()} : args);
            }
        });
    }

    public boolean isEmpty()
    {
        return getText()==null || getText().equals("");
    }

}
