/*
 *
 * Date: 2010-mar-04
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.tools.adminclient;

import net.sf.xapp.application.editor.widgets.ComboChooser;
import net.sf.xapp.application.editor.widgets.LiveTemplate;
import net.sf.xapp.application.editor.widgets.SimpleComboChooserClient;
import net.sf.xapp.application.utils.SwingUtils;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Text field with a history, activated by arrow up and arrow down
 */
public class CommandLineTF extends JTextField implements ActionListener
{
    private List<String> m_history;
    private int m_pointer;
    public static ContextHelp CONTEXT_HELP = new ContextHelp();
    private LiveTemplate m_currentLiveTemplate;
    private Popup currentPopup;

    public CommandLineTF(int cols)
    {
        super(cols);
        m_history = new ArrayList<String>();
        addActionListener(this);
        addAction("UP", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setText(nextEntry(-1));
            }
        });
        addAction("DOWN", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setText(nextEntry(1));
            }
        });
        addAction("control SPACE", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final String txt = getText();
                final ContextHelp.HelpTuple helpTuple = CONTEXT_HELP.getHelp(txt);
                if (!helpTuple.m_insertionSuggestions.isEmpty())
                {
                    Point pointAtIndex = getPointAtIndex(helpTuple.m_insertIndex);
                    final ComboChooser<ContextHelp.InsertionSuggestion> comboChooser = new ComboChooser<ContextHelp.InsertionSuggestion>(getSize().width, getSize().height);
                    comboChooser.init(pointAtIndex.x, pointAtIndex.y, CommandLineTF.this, helpTuple.m_insertionSuggestions, null, new SimpleComboChooserClient<ContextHelp.InsertionSuggestion>()
                    {
                        @Override
                        public void itemChosen(ContextHelp.InsertionSuggestion item)
                        {
                            LiveTemplate liveTemplate = new LiveTemplate(item.m_template, item.m_paramHelp);
                            liveTemplate.reset(helpTuple.m_insertIndex);
                            setText(txt.substring(0, helpTuple.m_insertIndex) + liveTemplate.getInsertion());
                            m_currentLiveTemplate = liveTemplate;
                            addAction("ENTER", new AbstractAction()
                            {
                                public void actionPerformed(ActionEvent e)
                                {
                                    if (m_currentLiveTemplate != null)
                                    {
                                        updateCaretForLiveTemplate();
                                        repaint();
                                    }
                                }
                            });
                            updateCaretForLiveTemplate();
                        }

                        @Override
                        public void selectionChanged(ContextHelp.InsertionSuggestion item)
                        {
                            showPopup(0,item.m_tooltip);
                        }
                    });
                }
            }
        });
        setDocument(new DefaultStyledDocument()
        {
            @Override
            public void insertString(int offs, String newText, AttributeSet a) throws BadLocationException
            {
                super.insertString(offs, newText, a);
                if (m_currentLiveTemplate != null)
                {
                    boolean stillValid = m_currentLiveTemplate.textInserted(offs, newText);
                    if (!stillValid)
                    {
                        killLiveTemplate();
                    }
                }
            }

            @Override
            public void remove(int offs, int len) throws BadLocationException
            {
                super.remove(offs, len);
                if (m_currentLiveTemplate != null)
                {
                    boolean stillValid = m_currentLiveTemplate.textRemoved(offs, len);
                    if (!stillValid)
                    {
                        killLiveTemplate();
                    }
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (m_currentLiveTemplate != null)
        {
            int[] args = m_currentLiveTemplate.indexRange();
            int startIndex = args[0];
            int len = args[1];
            g.setColor(Color.RED);
            Rectangle b = getBoundsAtIndex(startIndex);
            Rectangle bPrevious = b;
            g.drawLine(b.x, b.y, b.x, b.y + b.height - 1);
            for (int i = 1; i < len + 1; i++)
            {
                Rectangle b2 = getBoundsAtIndex(startIndex + i);
                if (i == len)
                {
                    //close old box
                    g.drawLine(b2.x, b2.y, b2.x, b2.y + b2.height - 1);
                }
                if (b2.y != bPrevious.y)
                {
                    //start new box
                    g.drawLine(bPrevious.x, bPrevious.y, bPrevious.x, bPrevious.y + bPrevious.height - 1);
                    g.drawLine(b2.x, b2.y, b2.x, b2.y + b2.height - 1);
                }
                else
                {
                    g.drawLine(bPrevious.x, bPrevious.y, b2.x, b2.y);
                    g.drawLine(bPrevious.x, bPrevious.y + bPrevious.height - 1, b2.x, b2.y + b2.height - 1);
                }
                bPrevious = b2;
            }
        }
    }

    private void updateCaretForLiveTemplate()
    {
        int newCaret = m_currentLiveTemplate.nextCaretIndex();
        setCaretPosition(newCaret);
        String paramHelp = m_currentLiveTemplate.currentParamHelp();
        if (paramHelp != null)
        {
            showPopup(newCaret, paramHelp);
        }
        if (!m_currentLiveTemplate.hasMore())
        {
            killLiveTemplate();
            removeAction("ENTER");
        }

    }

    private void showPopup(int newCaret, String paramHelp)
    {
        JToolTip toolTip = createToolTip();
        toolTip.setTipText(paramHelp);
        PopupFactory factory = PopupFactory.getSharedInstance();
        Rectangle b = getBoundsAtIndex(newCaret);
        if (currentPopup != null)
        {
            currentPopup.hide();
        }
        Point p = getLocationOnScreen();
        currentPopup = factory.getPopup(this, toolTip, p.x + b.x, p.y+b.y-20);
        currentPopup.show();
    }

    private void killLiveTemplate()
    {
        m_currentLiveTemplate = null;
        if (currentPopup!=null)
        {
            currentPopup.hide();
        }
    }

    private String nextEntry(int i)
    {
        if (m_pointer + i < 0 || m_pointer + i > m_history.size() - 1)
        {
            return null;
        }
        m_pointer += i;
        return m_history.get(m_pointer);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (getText() != null && !getText().equals(""))
        {
            m_history.remove(getText());
            m_history.add(getText());
        }
        m_pointer = m_history.size();
        setText(null);
    }

    public void addAction(String keyStroke, Action action)
    {
        KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
        getInputMap().put(ks, ks);
        getActionMap().put(ks, action);
    }

    public void removeAction(String keyStroke)
    {
        KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
        getInputMap().remove(ks);
        getActionMap().remove(ks);
    }

    public static void main(String[] args)
    {
        SwingUtils.showInFrame(new CommandLineTF(100));
    }

    public Point getPointAtIndex(int i)
    {
        return getBoundsAtIndex(i).getLocation();
    }

    public Rectangle getBoundsAtIndex(int i)
    {
        try
        {
            return modelToView(i);
        }
        catch (BadLocationException e)
        {
            throw new RuntimeException(e);
        }
    }
}