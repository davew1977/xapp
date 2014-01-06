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
    private long m_rev;
    private List<Conflict> m_conflicts;

    public UpdateResult()
    {
        m_conflicts = new ArrayList<Conflict>();
    }

    public void setRev(long rev)
    {
        m_rev = rev;
    }

    public long getRev()
    {
        return m_rev;
    }

    public List<Conflict> getConflicts()
    {
        return m_conflicts;
    }

    public boolean isConflict()
    {
        return !m_conflicts.isEmpty();
    }
}
