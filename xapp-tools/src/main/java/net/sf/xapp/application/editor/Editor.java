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
package net.sf.xapp.application.editor;

import javax.swing.*;
import java.awt.*;

public interface Editor
{
    void setGuiListener(EditorListener editorListener);

    JFrame getMainFrame();

    JDialog getMainDialog(Frame owner, boolean modal);

    void updateFields();

    void setEditableContext(EditableContext editableContext);

    void setCloseOnSave(boolean closeOnSave);

    Window getMainWindow();

    /**
     * when editor is used as a modal dialog, record the whether it was cancelled or not
     * @return true if the close/cancel button was pressed
     */
    boolean wasCancelled();

}
