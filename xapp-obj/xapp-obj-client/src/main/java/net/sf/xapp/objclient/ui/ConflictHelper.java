package net.sf.xapp.objclient.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.apis.objmanager.to.UpdateObject;
import net.sf.xapp.objserver.types.*;
import net.sf.xapp.uifwk.XButton;
import net.sf.xapp.uifwk.XPane;
import net.sf.xapp.utils.StringUtils;

import static java.lang.String.format;

/**
 */
public class ConflictHelper {
    public static JFrame showConflicts(List<Delta> offlineDeltas, List<PropConflict> propConflicts, List<DeleteConflict> deleteConflicts, List<MoveConflict> moveConflicts, List<AddConflict> addConflicts, Object listener) {
        //local cdb for context on deleted objects
        JTextArea textarea = new JTextArea();
        textarea.setFont(Font.decode("monospaced-12"));
        textarea.setWrapStyleWord(true);
        textarea.setLineWrap(false);
        JScrollPane jsp = new JScrollPane(textarea);
        int width = 700;
        jsp.setPreferredSize(new Dimension(width,400));

        StringBuilder sb = new StringBuilder();
        if(!propConflicts.isEmpty()) {
            title(sb, "Property Conflicts (you tried to change an object property that was changed on the server)");
            for (PropConflict propConflict : propConflicts) {
                ObjInfo obj = propConflict.getObj();
                sb.append(format("%s\n", toString(obj)));
                PropChange mine = propConflict.getMine();
                PropChange theirs = propConflict.getTheirs();
                sb.append(format("\ttheirs: \"%s\" changed from \"%s\" to \"%s\"\n", theirs.getProperty(), theirs.getOldValue(), theirs.getNewValue()));
                sb.append(format("\tyours:  \"%s\" changed from \"%s\" to \"%s\"\n", mine.getProperty(), mine.getOldValue(), mine.getNewValue()));
                sb.append("\n");
            }
            sb.append("\n");
        }

        if(!moveConflicts.isEmpty()) {
            title(sb, "Move Conflicts (you tried to move an object that has been moved on the server)");
            for (MoveConflict moveConflict : moveConflicts) {
                sb.append(format("%s\n", toString(moveConflict.getMovedObj())));
            }
            sb.append("\n");
        }
        if(!deleteConflicts.isEmpty()) {
            title(sb, "Delete Conflicts (you modified an object that no longer exists on the server)");
            for (DeleteConflict deleteConflict : deleteConflicts) {
                sb.append(format("%s\n", deleteConflict.getMissingObjId()));
                for (Integer deltaIndex : deleteConflict.getMyDeltaIndexes()) {
                    Delta delta = offlineDeltas.get(deltaIndex);
                    sb.append(format("\t%s\n", delta.getMessage().serialize()));
                }
            }
            sb.append("\n");
        }
        if(!addConflicts.isEmpty()) {
            title(sb, "Add Conflicts (you added an object in a slot which was taken by the server");
            for (AddConflict addConflict : addConflicts) {
                sb.append(format("%s\n", addConflict.getMissingObjId()));
                for (Integer deltaIndex : addConflict.getMyDeltaIndexes()) {
                    Delta delta = offlineDeltas.get(deltaIndex);
                    sb.append(format("\t%s\n", delta.getMessage().serialize()));
                }
            }
            sb.append("\n");
        }
        textarea.setText(sb.toString());

        XPane pane = new XPane();
        pane.setSize(width, 30);
        XButton discardButton = new XButton("Cancel").size(100,20).location(width /2 - 160, 5).
                addListener(listener, "decision", (Object) null);
        XButton mineButton = new XButton("Accept Mine").size(100,20).location(width /2 - 50, 5).
                addListener(listener, "decision", ConflictResolution.FORCE_ALL_MINE);
        XButton theirsButton = new XButton("Accept Theirs").size(100,20).location(width / 2 + 60, 5).
                addListener(listener, "decision", ConflictResolution.FORCE_ALL_THEIRS);
        pane.add(discardButton);
        pane.add(mineButton);
        pane.add(theirsButton);

        Box b = new Box(BoxLayout.PAGE_AXIS);
        b.add(jsp);
        b.add(pane);
        JFrame frame = SwingUtils.createFrame(b);
        frame.setLocationRelativeTo(JOptionPane.getRootFrame());
        frame.setVisible(true);
        return frame;
    }

    private static String toString(ObjInfo obj) {
        return format("%s : %s : %s", obj.getId(), obj.getType().getSimpleName(), obj.getName());
    }

    private static void title(StringBuilder sb, String title) {
        sb.append(title).append("\n");
        sb.append(StringUtils.line(title.length(), '=')).append("\n").append("\n");
    }

    public static void main(String[] args) {

        List<Delta> offlineDeltas = Arrays.asList(delta(updateObj()), delta(updateObj()), delta(updateObj()), delta(updateObj()));
        List<PropConflict> propConflicts = Arrays.asList(new PropConflict(0,0L,propChange(), propChange(), objInfo()));
        List<DeleteConflict> deleteConflicts = Arrays.asList(new DeleteConflict(Arrays.asList(1,2), 0L, 884L));
        List<MoveConflict> moveConflicts = Arrays.asList(new MoveConflict(0L, 2, objInfo()));
        List<AddConflict> addConflicts = Arrays.asList(new AddConflict(Arrays.asList(2,3), 0L, 6363L));
        showConflicts(offlineDeltas, propConflicts, deleteConflicts, moveConflicts, addConflicts, null);

    }

    private static ObjInfo objInfo() {
        return new ObjInfo(45L, "faveHat", ChatPane.class);
    }

    private static Delta delta(InMessage<ObjUpdate, Void> message) {
        double v = Math.random() * 10000000.0;
        return new Delta(message, System.currentTimeMillis() - 10000000 + (int) v);
    }

    private static UpdateObject updateObj() {
        return new UpdateObject("s1", new UserId("102"), Arrays.asList(new PropChangeSet(324L, Arrays.asList(propChange()))));
    }

    private static PropChange propChange() {
        return new PropChange("name", "frank", "jane");
    }
}
