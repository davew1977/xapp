package net.sf.xapp.examples.school;

import net.sf.xapp.objserver.types.AddConflict;
import net.sf.xapp.objserver.types.DeleteConflict;
import net.sf.xapp.objserver.types.MoveConflict;
import net.sf.xapp.objserver.types.PropConflict;

import java.util.List;

/**
 * Created by oldDave on 23/12/2014.
 */
public class Conflicts {
    List<PropConflict> propConflicts;
    List<DeleteConflict> deleteConflicts;
    List<MoveConflict> moveConflicts;
    List<AddConflict> addConflicts;

    public Conflicts(List<PropConflict> propConflicts, List<DeleteConflict> deleteConflicts, List<MoveConflict> moveConflicts, List<AddConflict> addConflicts) {
        this.propConflicts = propConflicts;
        this.deleteConflicts = deleteConflicts;
        this.moveConflicts = moveConflicts;
        this.addConflicts = addConflicts;
    }
}
