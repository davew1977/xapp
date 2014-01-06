/*
 *
 * Date: 2009-dec-20
 * Author: davidw
 *
 */
package net.sf.xapp.application.editor.widgets;


import net.sf.xapp.application.utils.SwingUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

public class ComboChooser<T> extends JComboBox implements ItemListener
{
    private ComboChooserClient<T> m_listener;
    private JComponent m_parent;
    private FocusListener m_focusAdaptor;
    private MyDocumentListener m_myDocumentListener;
    private int m_width;
    private int m_height;

    public ComboChooser(int width, int height)
    {
        m_width = width;
        m_height = height;
    }

    public void init(int x, int y, final JComponent parent, Collection<T> initialValues, T initialValue, ComboChooserClient<T> listener)
    {
        m_listener = listener;
        m_parent = parent;
        setEditor(new MyComboBoxEditor(getEditor()));
        SwingUtils.setFont(this, parent.getFont());
        setBounds(x, y, m_width, m_height);
        parent.add(this);
        requestFocus();
        addItemListener(this);


        final JTextField editorTF = (JTextField) getEditor().getEditorComponent();
        //editorTF.setText(String.valueOf(initialValue));
        m_myDocumentListener = new MyDocumentListener(editorTF);
        editorTF.getDocument().addDocumentListener(m_myDocumentListener);
        editorTF.addKeyListener(new MyKeyAdapter());
        m_focusAdaptor = new MyFocusAdapter();
        parent.addFocusListener(m_focusAdaptor);
        parent.revalidate();
        parent.repaint();

        setEditable(listener.isEditable());
        setModel(initialValues);
        setSelectedItem(initialValue);
        new Thread(new Runnable()  //hack because the damn popup won't open without a delay
        {
            public void run()
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                setPopupVisible(true);
                System.out.println(isPopupVisible());
            }
        }).start();
    }

    private void setModel(Collection<T> initialValues)
    {
        setModel(new DefaultComboBoxModel(new Vector<Object>(initialValues)));
    }

    private void removeMe()
    {
        m_parent.removeFocusListener(m_focusAdaptor);
        m_parent.remove(this);
        m_parent.revalidate();
        m_parent.repaint();
        m_parent.requestFocus();
        m_listener.comboRemoved();
    }

    public void itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
            m_listener.selectionChanged((T) e.getItem());
        }
    }

    private class MyDocumentListener implements DocumentListener
    {
        boolean m_switching;
        private final JTextField m_editorTF;

        public MyDocumentListener(JTextField editorTF)
        {
            m_editorTF = editorTF;
        }

        public void insertUpdate(DocumentEvent e)
        {
            update(e);
        }

        public void removeUpdate(DocumentEvent e)
        {
            update(e);
        }

        public void changedUpdate(DocumentEvent e)
        {
            update(e);
        }

        private void update(DocumentEvent e)
        {
            if (!m_switching && e.getLength() == 1) //key typed
            {
                m_switching = true;
                final String updatedText = m_editorTF.getText();
                final List<T> newValues = m_listener.filterValues(updatedText);
                if (newValues != null)
                {
                    if (newValues.isEmpty()) //remove combo
                    {
                        m_listener.itemChosen((T) updatedText);
                        removeMe();
                        m_switching = false;
                    }
                    else
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                setModel(newValues);
                                setSelectedItem(updatedText);
                                setPopupVisible(true);
                                m_switching = false;
                            }
                        });
                    }
                }
                else
                {
                    m_switching = false;
                }
            }
        }
    }

    private class MyComboBoxEditor implements ComboBoxEditor
    {
        private final ComboBoxEditor m_oldEditor;

        public MyComboBoxEditor(ComboBoxEditor oldEditor)
        {
            m_oldEditor = oldEditor;
        }

        public Component getEditorComponent()
        {
            return m_oldEditor.getEditorComponent();
        }

        public void setItem(Object anObject)
        {
            m_myDocumentListener.m_switching = true;
            m_oldEditor.setItem(anObject);
            m_myDocumentListener.m_switching = false;
        }

        public Object getItem()
        {
            return m_oldEditor.getItem();
        }

        public void selectAll()
        {
            m_oldEditor.selectAll();
        }

        public void addActionListener(ActionListener l)
        {
            m_oldEditor.addActionListener(l);
        }

        public void removeActionListener(ActionListener l)
        {
            m_oldEditor.removeActionListener(l);
        }
    }

    private class MyKeyAdapter extends KeyAdapter
    {
        public void keyTyped(KeyEvent e)
        {
            System.out.println(e.getKeyChar());
            if (e.getKeyChar() == KeyEvent.VK_ENTER && !isPopupVisible())
            {
                removeMe();
                m_listener.itemChosen((T) getSelectedItem());
                return;
            }
            if (e.getKeyChar() == KeyEvent.VK_ESCAPE)
            {
                removeMe();
                return;
            }
        }
    }

    private class MyFocusAdapter extends FocusAdapter
    {
        public void focusGained(FocusEvent e)
        {
            removeMe();
        }
    }
}