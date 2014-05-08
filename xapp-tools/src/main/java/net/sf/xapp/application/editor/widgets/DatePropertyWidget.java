/*
 *
 * Date: 2010-nov-17
 * Author: davidw
 *
 */
package net.sf.xapp.application.editor.widgets;

import com.toedter.calendar.JDateChooser;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import javax.swing.*;
import java.util.Date;

public class DatePropertyWidget extends AbstractPropertyWidget<Date>
{
    private JDateChooser dateChooser;

    @Override
    public JComponent getComponent()
    {
        if(dateChooser==null)
        {
            dateChooser = new JDateChooser();
        }
        return dateChooser;
    }

    @Override
    public Date getValue()
    {
        return dateChooser.getDate();
    }

    @Override
    public void setValue(Date value, ObjectMeta target)
    {
        dateChooser.setDate(value);
    }

    @Override
    public void setEditable(boolean editable)
    {
        dateChooser.setEnabled(editable);
    }

    public static void main(String[] args)
    {
        SwingUtils.showInFrame(new DatePropertyWidget().getComponent());
    }
}
