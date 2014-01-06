/*
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 *
 * The contents of this file may be used under the terms of the GNU Lesser
 * General Public License Version 2.1 or later.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */
package net.sf.xapp.application.utils.html;

import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.utils.XappException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides support for displaying html that has been generated for quick and dirty guis in the right-hand pane
 */
public class BrowserView extends JEditorPane
{
    private BrowserViewListener m_listener;

    public BrowserView()
    {
        this(new BrowserViewListener.NullBrowserViewListener());
    }

    public BrowserView(BrowserViewListener listener)
    {
        super("text/html", "<html></html>");
        m_listener = listener;
        setBorder(BorderFactory.createEtchedBorder());
        setFont(SwingUtils.DEFAULT_FONT);
        setEditable(false);
        setOpaque(false);

        addHyperlinkListener(new HyperlinkListener()
        {
            public void hyperlinkUpdate(HyperlinkEvent hle)
            {
                try
                {
                    if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType()))
                    {
                        if (hle instanceof FormSubmitEvent)
                        {
                            handleFormSubmit(hle);
                        }
                        else
                        {
                            handleLinkPressed(hle);
                        }
                    }
                }
                catch (Throwable e)
                {
                    m_listener.handleUncaughtException(e);
                }
            }
        });

        MyHTMLEditorKit htmlEditorKit = new MyHTMLEditorKit();
        htmlEditorKit.setAutoFormSubmission(false);
        setEditorKit(htmlEditorKit);

    }

    private void handleLinkPressed(HyperlinkEvent hle)
    {
        String url = hle.getDescription();
        m_listener.linkPressed(url);
        if (url.startsWith("http://") || url.startsWith("https://"))
        {
            try
            {
                Desktop.getDesktop().browse(new URI(url));
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            catch (URISyntaxException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleFormSubmit(HyperlinkEvent hle)
    {
        FormSubmitEvent event = (FormSubmitEvent) hle;
        Map<String, String> props = new HashMap<String, String>();
        String data = event.getData();
        System.out.println(data);
        if (data != null)
        {
            String[] propsStr = data.split("\\&");
            for (String s : propsStr)
            {
                String[] args = s.split("=");
                props.put(args[0], args.length > 1 ? args[1] : null);
            }
        }
        m_listener.formSubmitted(props);
    }

    public void setListener(BrowserViewListener listener)
    {
        m_listener = listener;
    }

    public void setHTML(final String content)
    {

        /*new Thread(new Runnable()
        {
            public void run()
            {
                try
                {

                    read(new StringReader(content), "text/html");

                }
                catch (IOException e)
                {
                    throw new DJWastorException(e);
                }
                m_listener.htmlRendered();
            }
        }).start();*/
        try
        {
            read(new StringReader(content), "text/html");
        }
        catch (IOException e)
        {
            throw new XappException(e);
        }
        m_listener.htmlRendered();
    }

    public void setHTML(HTML html)
    {
        setHTML(html.htmlDoc());
    }


    private class MyHTMLEditorKit extends HTMLEditorKit
    {

        @Override
        public Document createDefaultDocument()
        {
            HTMLDocument doc = (HTMLDocument) super.createDefaultDocument();
            doc.setBase(getClass().getResource("/images/"));
            return doc;
        }

        @Override
        public ViewFactory getViewFactory()
        {
            return new HTMLFactory()
            {
                @Override
                public View create(Element elem)
                {
                    View view = super.create(elem);
                    final Element element = view.getElement();
                    if (view instanceof ImageView)
                    {
                        view = new ImageView(element)
                        {
                            @Override
                            public URL getImageURL()
                            {
                                String src = (String) getElement().getAttributes().
                                        getAttribute(javax.swing.text.html.HTML.Attribute.SRC);
                                String res = "/images/" + src;
                                URL resource = getClass().getResource(res);
                                return resource;
                            }
                        };
                    }
                    else if (view instanceof FormView)
                    {
                        view = new FormView(element)
                        {
                            protected Component createComponent()
                            {
                                Component comp = super.createComponent();
                                AttributeSet attributeSet = element.getAttributes();
                                final String compId = String.valueOf(attributeSet.getAttribute(javax.swing.text.html.HTML.getAttributeKey("name")));
                                if (comp instanceof JComboBox)
                                {
                                    final JComboBox jComboBox = (JComboBox) comp;
                                    //System.out.println(id);
                                    jComboBox.addItemListener(new ItemListener()
                                    {
                                        public void itemStateChanged(ItemEvent e)
                                        {
                                            if (e.getStateChange() == ItemEvent.SELECTED)
                                            {
                                                m_listener.comboItemChanged(compId,
                                                        String.valueOf(jComboBox.getSelectedItem()));
                                            }

                                        }
                                    });
                                }
                                else if (comp instanceof JTextField)
                                {
                                    final JTextField jTextField = (JTextField) comp;
                                    jTextField.getDocument().addDocumentListener(new DocumentListener()
                                    {
                                        public void insertUpdate(DocumentEvent e)
                                        {
                                            update(e);
                                        }

                                        private void update(DocumentEvent e)
                                        {
                                            m_listener.textFieldChanged(compId, jTextField.getText());
                                        }

                                        public void removeUpdate(DocumentEvent e)
                                        {
                                            update(e);
                                        }

                                        public void changedUpdate(DocumentEvent e)
                                        {
                                            update(e);
                                        }
                                    });

                                }
                                else if (comp instanceof JCheckBox)
                                {
                                     final JCheckBox jCheckBox = (JCheckBox) comp;
                                    //System.out.println(id);
                                    jCheckBox.addItemListener(new ItemListener()
                                    {
                                        public void itemStateChanged(ItemEvent e)
                                        {
                                            int stateChange = e.getStateChange();
                                            if (stateChange == ItemEvent.SELECTED || stateChange == ItemEvent.DESELECTED)
                                            {
                                                m_listener.checkBoxChanged(compId, stateChange == ItemEvent.SELECTED );

                                            }
                                            
                                        }
                                    });
                                }

                                return comp;
                            }
                        };
                    }
                    return view;
                }


            };
        }


    }
}
