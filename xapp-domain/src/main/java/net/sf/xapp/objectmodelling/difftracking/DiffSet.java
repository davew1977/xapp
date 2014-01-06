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
package net.sf.xapp.objectmodelling.difftracking;

import net.sf.xapp.annotations.objectmodelling.ListType;
import net.sf.xapp.annotations.objectmodelling.Transient;
import net.sf.xapp.objectmodelling.core.Property;

import java.util.ArrayList;
import java.util.List;

public class DiffSet
{
    private List<PropertyDiff> m_propertyDiffs;
    private List<ComplexPropertyDiff> m_complexPropertyDiffs;
    private List<NewNodeDiff> m_newNodeDiffs;
    private List<RemovedNodeDiff> m_removedNodeDiffs;
    private List<ReferenceListDiff> m_referenceListDiffs;


    public DiffSet()
    {
        this(new ArrayList<PropertyDiff>(), new ArrayList<NewNodeDiff>(), new ArrayList<RemovedNodeDiff>(), new ArrayList<ReferenceListDiff>(), new ArrayList<ComplexPropertyDiff>());
    }


    public DiffSet(List<PropertyDiff> propertyDiffs, List<NewNodeDiff> newNodeDiffs, List<RemovedNodeDiff> removedNodeDiffs, List<ReferenceListDiff> referenceListDiffs, List<ComplexPropertyDiff> complexPropertyDiffs)
    {
        m_propertyDiffs = propertyDiffs;
        m_newNodeDiffs = newNodeDiffs;
        m_removedNodeDiffs = removedNodeDiffs;
        m_referenceListDiffs = referenceListDiffs;
        m_complexPropertyDiffs = complexPropertyDiffs;
    }

    @ListType(PropertyDiff.class)
    public List<PropertyDiff> getPropertyDiffs()
    {
        return m_propertyDiffs;
    }

    public void setPropertyDiffs(List<PropertyDiff> propertyDiffs)
    {
        m_propertyDiffs = propertyDiffs;
    }

    @ListType(NewNodeDiff.class)
    public List<NewNodeDiff> getNewNodeDiffs()
    {
        return m_newNodeDiffs;
    }

    public void setNewNodeDiffs(List<NewNodeDiff> newNodeDiffs)
    {
        m_newNodeDiffs = newNodeDiffs;
    }

    @ListType(RemovedNodeDiff.class)
    public List<RemovedNodeDiff> getRemovedNodeDiffs()
    {
        return m_removedNodeDiffs;
    }

    public void setRemovedNodeDiffs(List<RemovedNodeDiff> removedNodeDiffs)
    {
        m_removedNodeDiffs = removedNodeDiffs;
    }

    public void merge(DiffSet diffsetToMerge)
    {
        m_newNodeDiffs.addAll(diffsetToMerge.m_newNodeDiffs);
        m_propertyDiffs.addAll(diffsetToMerge.m_propertyDiffs);
        m_removedNodeDiffs.addAll(diffsetToMerge.m_removedNodeDiffs);
        m_referenceListDiffs.addAll(diffsetToMerge.m_referenceListDiffs);
        m_complexPropertyDiffs.addAll(diffsetToMerge.m_complexPropertyDiffs);
    }

    @ListType(ReferenceListDiff.class)
    public List<ReferenceListDiff> getReferenceListDiffs()
    {
        return m_referenceListDiffs;
    }

    public void setReferenceListDiffs(List<ReferenceListDiff> referenceListDiffs)
    {
        m_referenceListDiffs = referenceListDiffs;
    }

    @ListType(ComplexPropertyDiff.class)
    public List<ComplexPropertyDiff> getComplexPropertyDiffs()
    {
        return m_complexPropertyDiffs;
    }

    public void setComplexPropertyDiffs(List<ComplexPropertyDiff> complexPropertyDiffs)
    {
        m_complexPropertyDiffs = complexPropertyDiffs;
    }

    @Transient
    public boolean isEmpty()
    {
        return m_newNodeDiffs.isEmpty() && m_propertyDiffs.isEmpty() && m_referenceListDiffs.isEmpty() && m_removedNodeDiffs.isEmpty() && m_complexPropertyDiffs.isEmpty();
    }

    @Transient
    public boolean isNodeRemoved(PropertyDiff propertyDiff)
    {
        for (RemovedNodeDiff removedNodeDiff : m_removedNodeDiffs)
        {
            if (removedNodeDiff.getKey().equals(propertyDiff.getKey()) &&
                    removedNodeDiff.getNodeClass().equals(propertyDiff.getClazz()))
            {
                return true;
            }
        }
        return false;
    }

    public List<Conflict> findConflicts(DiffSet other)
    {
        List<Conflict> conflicts = new ArrayList<Conflict>();
        List<PropertyDiff> otherPropDiffs = other.getPropertyDiffs();
        for (PropertyDiff otherDiff : otherPropDiffs)
        {
            for (PropertyDiff thisDiff : m_propertyDiffs)
            {
                if (Property.objEquals(thisDiff.getKey(), otherDiff.getKey()) &&
                        thisDiff.getClazz().equals(otherDiff.getClazz()) &&
                        thisDiff.getProperty().equals(otherDiff.getProperty()) &&
                        !Property.objEquals(thisDiff.getNewValue(), otherDiff.getNewValue()))
                {
                    conflicts.add(new Conflict(thisDiff, otherDiff, ConflictType.SIMPLE_DIFF_DIFF));
                }
            }
        }
        List<ReferenceListDiff> otherReferenceListDiffs = other.getReferenceListDiffs();
        for (ReferenceListDiff otherDiff : otherReferenceListDiffs)
        {
            for (ReferenceListDiff thisDiff : m_referenceListDiffs)
            {
                if (Property.objEquals(thisDiff.getContainerKey(), otherDiff.getContainerKey()) &&
                        otherDiff.getContainerClass().equals(thisDiff.getContainerClass()) &&
                        otherDiff.getListProperty().equals(thisDiff.getListProperty()))
                {
                    conflicts.add(new Conflict(thisDiff, otherDiff, ConflictType.SIMPLE_REF_LIST));

                }
            }
        }
        List<ComplexPropertyDiff> complexPropertyDiffs = other.getComplexPropertyDiffs();
        for (ComplexPropertyDiff otherDiff : complexPropertyDiffs)
        {
            for (ComplexPropertyDiff thisDiff : m_complexPropertyDiffs)
            {

                if (Property.objEquals(otherDiff.getKey(), thisDiff.getKey()) &&
                        otherDiff.getClazz().equals(thisDiff.getClazz()) &&
                        otherDiff.getProperty().equals(thisDiff.getProperty()))
                {

                    if (thisDiff.isRemoved() && otherDiff.isRemoved())
                    {
                        //no conflict
                    }
                    else if(thisDiff.isNew() && otherDiff.isNew())
                    {
                        if(!thisDiff.getNewValue().equals(otherDiff.getNewValue()))
                        {
                            conflicts.add(new Conflict(thisDiff, otherDiff, ConflictType.COMPLEX_NEW_NEW));
                        }
                    }
                    else if(thisDiff.isNew() || otherDiff.isNew())
                    {
                        //Not Possible!
                        assert false;
                    }
                    else if(thisDiff.isRemoved() || otherDiff.isRemoved())
                    {
                        conflicts.add(new Conflict(thisDiff, otherDiff, thisDiff.isRemoved() ? ConflictType.COMPLEX_REMOVED_DIFF : ConflictType.COMPLEX_DIFF_REMOVED));
                    }
                    else //both have their own diffset
                    {
                        assert thisDiff.hasDiffSet() && otherDiff.hasDiffSet();
                        conflicts.add(new Conflict(thisDiff, otherDiff, ConflictType.COMPLEX_DIFF_DIFF, thisDiff.getDiffSet().findConflicts(otherDiff.getDiffSet())));
                    }
                }
            }
        }
        return conflicts;
    }


}
