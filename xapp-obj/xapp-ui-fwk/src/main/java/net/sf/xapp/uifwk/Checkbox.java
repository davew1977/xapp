/*
 *
 * Date: 2010-okt-14
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;




import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Checkbox extends JCheckBox
{
    private Color background = Color.white;
    /**
     * @param text
     */
    public Checkbox(String text)
    {
        super();
        setText(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFont(new Font("Tahoma", Font.BOLD, 10));
        setForeground(Color.BLACK);
        setFocusable(false);
        setWidth(70);
    }

    private void setWidth(int width)
    {
        setSize(new Dimension(width,20));
        setPreferredSize(new Dimension(width,20));
    }

    public Checkbox width(int width)
    {
        setWidth(width);
        return this;
    }

    public Checkbox background(Color color)
    {
        background  = color;
        return this;
    }

    public Checkbox foreground(Color color)
    {
        setForeground(color);
        return this;
    }

    /**
     *
     */
    public void paintComponent(Graphics g)
    {
        //NGButton.paintButtonBgr(g, this, background);
        super.paintComponent(g);
    }

    public JPanel getButtonsPanel()
    {
        JPanel panel = new JPanel();
        panel.setBackground(Color.BLUE);
        Checkbox standardButton = new Checkbox("Bet");
        Checkbox rollOverButton = new Checkbox("Raise");
        Checkbox disabledButton = new Checkbox("Call");
        Checkbox pressedButton = new Checkbox("Fold");
        Checkbox checkButton = new Checkbox("Check");
        panel.add(standardButton);
        panel.add(rollOverButton);
        panel.add(disabledButton);
        panel.add(pressedButton);
        panel.add(checkButton);
        return panel;
    }

    public void addListener(final Object listener, final String method)
    {
        ReflectionUtils.checkMethodExists(listener.getClass(), method, true);
        addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ReflectionUtils.call(listener, method, isSelected());
            }
        });
    }
}
