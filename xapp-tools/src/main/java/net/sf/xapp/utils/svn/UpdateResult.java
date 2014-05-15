/*
 *
 * Date: 2009-dec-14
 * Author: davidw
 *
 */
package net.sf.xapp.utils.svn;

import java.util.List;
import java.util.ArrayList;

public class UpdateResult
{
    private long rev;
    private List<Conflict> conflicts;

    public UpdateResult()
    {
        conflicts = new ArrayList<Conflict>();
    }

    public void setRev(long rev)
    {
        this.rev = rev;
    }

    public long getRev()
    {
        return rev;
    }

    public List<Conflict> getConflicts()
    {
        return conflicts;
    }

    public boolean isConflict()
    {
        return !conflicts.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("rev: "+rev +"\n");
        for (Conflict m_conflict : conflicts) {
           sb.append(m_conflict);
        }
        return sb.toString();
    }
}
