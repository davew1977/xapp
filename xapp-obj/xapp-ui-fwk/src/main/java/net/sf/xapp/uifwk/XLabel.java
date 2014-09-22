/*
 *
 * Date: 2010-okt-14
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;



import javax.swing.*;
import javax.swing.plaf.basic.BasicLabelUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class XLabel extends JLabel
{
    private Color hoverColor;
    private Color normalColor;
    private Callback onClick;

    /**
     * @param text
     */
    public XLabel(String text)
    {
        this(text, null);
    }
    public XLabel(String text, final Color hoverColor)
    {
        super(text);
        this.hoverColor = hoverColor;
        setFont(new Font("Tahoma", Font.BOLD, 10));
        foreground(Color.BLACK);
        setFocusable(false);
        setWidth(70);

        setUI(new BasicLabelUI()
        {
            @Override
            public void paint(Graphics g, JComponent c)
            {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                super.paint(g, c);
            }
        });

        if (hoverColor!=null)
        {
            addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseEntered(MouseEvent e)
                {
                    setForeground(hoverColor);
                }

                @Override
                public void mouseExited(MouseEvent e)
                {
                    foreground(normalColor);
                }

                @Override
                public void mousePressed(MouseEvent e)
                {
                    if(onClick!=null)
                    {
                        setForeground(Color.white);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e)
                {
                    if(onClick!=null)
                    {
                        setForeground(hoverColor);
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e)
                {
                    if(onClick!=null)
                    {
                        onClick.call();
                    }
                }
            });
        }
    }

    public void setOnClick(Callback onClick)
    {
        this.onClick = onClick;
    }

    private void setWidth(int width)
    {
        setSize(new Dimension(width, 20));
        setPreferredSize(new Dimension(width, 20));
    }

    public XLabel size(int width, int height)
    {
        setSize(new Dimension(width, height));
        setPreferredSize(new Dimension(width, height));
        return this;
    }

    public XLabel width(int width)
    {
        setWidth(width);
        return this;
    }

    public XLabel bold(boolean bold)
    {
        Font f = getFont();
        setFont(f.deriveFont(bold ? Font.BOLD : Font.PLAIN));
        return this;
    }

    public XLabel fontSize(float size)
    {
        Font f = getFont();
        setFont(f.deriveFont(size));
        return this;
    }

    public XLabel foreground(Color color)
    {
        setForeground(color);
        normalColor = color;
        return this;
    }

    public XLabel location(int x, int y)
    {
        setLocation(x, y);
        return this;
    }

    public JPanel getButtonsPanel()
    {
        JPanel panel = new JPanel();
        panel.setBackground(Color.BLUE);
        XLabel standardButton = new XLabel("Bet");
        XLabel rollOverButton = new XLabel("Raise");
        XLabel disabledButton = new XLabel("Call");
        XLabel pressedButton = new XLabel("Fold");
        XLabel checkButton = new XLabel("Check");
        panel.add(standardButton);
        panel.add(rollOverButton);
        panel.add(disabledButton);
        panel.add(pressedButton);
        panel.add(checkButton);
        return panel;
    }

    public XLabel opaque(boolean b)
    {
        setOpaque(b);
        return this;
    }
}
