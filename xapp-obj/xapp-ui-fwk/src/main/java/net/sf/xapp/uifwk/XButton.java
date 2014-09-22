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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class XButton extends JButton
{
    private Color background = Color.white;
    private boolean hover;

    /**
     * @param text
     */
    public XButton(String text)
    {
        super();
        setText(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFont(new Font("Tahoma", Font.BOLD, 10));
        setForeground(Color.BLACK);
        setFocusable(false);
        _setSize(70, 20);
        setMargin(new Insets(0, 0, 0, 0));
        setBorder(null);
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                hover = false;
                repaint();
            }
        });
    }

    public void _setSize(int width, int height)
    {
        setSize(new Dimension(width,height));
        setPreferredSize(new Dimension(width,height));
        setMinimumSize(new Dimension(width,height));
        setMaximumSize(new Dimension(width,height));
    }

    public XButton width(int width)
    {
        _setSize(width, getSize().height);
        return this;
    }

    public XButton location(int x, int y)
    {
        setLocation(x,y);
        return this;
    }

    public XButton size(int w, int h)
    {
        _setSize(w, h);
        return this;
    }

    public XButton fontSize(float size)
    {
        setFont(getFont().deriveFont(Font.BOLD, size));
        return this;
    }

    public XButton background(Color color)
    {
        background  = color;
        return this;
    }

    public XButton foreground(Color color)
    {
        setForeground(color);
        return this;
    }

    /**
     *
     */
    public void paintComponent(Graphics gr)
    {
        paintButtonBgr(gr, this, background);
        super.paintComponent(gr);
    }

    public static void paintButtonBgr(Graphics gr, AbstractButton comp, Color background)
    {
        Graphics2D g = (Graphics2D) gr;
        int h = comp.getHeight();
        int w = comp.getSize().width;
        ButtonModel model = comp.getModel();
        /*Color color = model.isPressed() ? pressedColor:
                        model.isRollover() ? rollOverColor : normalColor;*/
        if ((model.isPressed() || model.isRollover()) && model.isEnabled())
        {
            float alpha = model.isPressed() ? 0.4f: 0.6f;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }
        Color c1 = Color.gray;
        Color c2 = model.isEnabled() ? background : Color.lightGray;
        if(comp instanceof XButton && ((XButton) comp).hover && model.isEnabled()) {
            Color tmp = c1;
            c1 = c2;
            c2 = tmp;
        }
        GradientPaint gp = new GradientPaint(0,0, c1, 0,10, c2);
        g.setPaint(gp);
        int arcw = h/2;
        if(model.isPressed())
        {
            g.fill(new RoundRectangle2D.Float(0, 0, w, h, arcw, arcw));
        }
        else
        {
            g.fill(new RoundRectangle2D.Float(1, 1, w-2, h-2, arcw, arcw));
        }
    }

    public static void main(String args[])
    {
        JFrame frame = new JFrame("Custom Buttons Demo");
        frame.setLayout(new FlowLayout());
        XButton standardButton = new XButton("Standard Button");
        frame.add(standardButton.getButtonsPanel());
        frame.getContentPane().setBackground(Color.WHITE);
        frame.setBackground(Color.WHITE);
        frame.setSize(700, 85);
        frame.setVisible(true);
    }

    public JPanel getButtonsPanel()
    {
        JPanel panel = new JPanel();
        panel.setBackground(Color.BLUE);
        XButton standardButton = new XButton("Bet");
        XButton rollOverButton = new XButton("Raise");
        XButton disabledButton = new XButton("Call");
        XButton pressedButton = new XButton("Fold");
        XButton checkButton = new XButton("Check");
        panel.add(standardButton);
        panel.add(rollOverButton);
        panel.add(disabledButton);
        panel.add(pressedButton);
        panel.add(checkButton);
        return panel;
    }

    public XButton addListener(final Callback callback)
    {
        addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                callback.call();
            }
        });
        return this;
    }
    public XButton addListener(final Object listener, final String method, final Object... args)
    {
        return addListener(new Callback(method, listener, args));
    }
}
