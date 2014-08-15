/*
 *
 * Date: 2010-feb-12
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.tools.adminclient;

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.api.SimpleApplication;
import net.sf.xapp.application.editor.text.TextEditor;
import net.sf.xapp.net.common.framework.PrettyPrinter;

import java.util.List;

public class ViewerApplication extends SimpleApplication
{
    private TextEditor m_editor;
    private ScriptWidget m_scriptWidget;

    public void init(ApplicationContainer applicationContainer)
    {
        super.init(applicationContainer);
        m_editor = new TextEditor()
        {
            public void handleNewText(int offs, String newText, TextEditor.Line linePreEdit, List<TextEditor.Line> lineOrLinesPostEdit)
            {

            }

            @Override
            public void handleTextRemoved(int offs, int len, Line lineAffected, String removedText)
            {

            }
        };
        m_scriptWidget = new ScriptWidget();
        m_scriptWidget.init(null);
        m_scriptWidget.setEditable(false);
    }

    public boolean nodeSelected(Node node)
    {
        if(node.wrappedObject() instanceof PrettyPrinter)
        {
            PrettyPrinter prettyPrinter = node.wrappedObject();
            m_editor.setText(prettyPrinter.expandToString());
            getAppContainer().setUserPanel(m_editor);
        }
        else if(node.wrappedObject() instanceof Script)
        {
            Script script = node.wrappedObject();
            m_scriptWidget.setValue(script.getContent(), null);
            getAppContainer().setUserPanel(m_scriptWidget.getTextEditor());
        }
        return false;
    }
}