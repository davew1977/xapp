/*
 *
 * Date: 2010-okt-14
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;





import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicPasswordFieldUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class XPasswordField extends JPasswordField
{
    /**
     * @param text
     */
    public XPasswordField(String text)
    {
        super();

        setOpaque(false);
        setText(text);
        setFont(new Font("Tahoma", Font.BOLD, 10));
        setForeground(Color.BLACK);
        setWidth(70);
        setUI(new BasicPasswordFieldUI()
        {
            @Override
            public void update(Graphics g, JComponent c)
            {
                XTextFieldUI._paintBgrd(g, XPasswordField.this);
                super.update(g, c);
            }
        });
        setBorder(new EmptyBorder(0, 10, 0, 10));
    }

    private void setWidth(int width)
    {
        setSize(new Dimension(width, 20));
        setPreferredSize(new Dimension(width, 20));
    }

    public XPasswordField width(int width)
    {
        setWidth(width);
        return this;
    }

    public XPasswordField bold(boolean bold)
    {
        Font f = getFont();
        setFont(f.deriveFont(bold ? Font.BOLD : Font.PLAIN));
        return this;
    }

    public XPasswordField foreground(Color color)
    {
        setForeground(color);
        return this;
    }

    public XPasswordField location(int x, int y)
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

    public static void main(String[] args)
    {
        XPane p = new XPane();
        p.setSize(100, 100);
        p.add(new XPasswordField("type").location(10, 30).width(70));
        SwingUtils.showInFrame(p);
    }

    public boolean isEmpty()
    {
        return getPassword() == null || getPassword().length==0;
    }

}
