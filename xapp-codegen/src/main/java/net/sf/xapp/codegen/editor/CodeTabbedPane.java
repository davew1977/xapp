/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.editor;

import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.application.utils.codegen.CodeFile;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CodeTabbedPane extends JTabbedPane
{
    private List<CodeEditor> m_codeEditors;

    public CodeTabbedPane()
    {
        m_codeEditors = new ArrayList<CodeEditor>();
        setFont(SwingUtils.DEFAULT_FONT);
    }

    public void init(List<CodeFile> files)
    {
        removeAll();
        for (int i = 0; i < files.size(); i++)
        {
            CodeFile codeFile = files.get(i);
            final CodeEditor codeEditor = getEditor(i);
            codeEditor.setText(codeFile.generateString());
            JScrollPane jsp = codeEditor.scrollPane();
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    codeEditor.scrollRectToVisible(new Rectangle(0,0,10,10));
                }
            });
            addTab(codeFile.getFileName(), jsp);
        }
    }

    private CodeEditor getEditor(int i)
    {
        if(m_codeEditors.size()<=i)
        {
            m_codeEditors.add(new CodeEditor());
        }
        return m_codeEditors.get(i);
    }
}