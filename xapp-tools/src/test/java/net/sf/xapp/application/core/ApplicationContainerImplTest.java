/*
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 *
 */
package net.sf.xapp.application.core;

import net.sf.xapp.application.api.Node;
import junit.framework.TestCase;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import java.util.ArrayList;
import java.util.List;

public class ApplicationContainerImplTest extends TestCase
{

    public void testGetCommonClassFromNodeList() throws Exception
    {
        assertEquals(A.class, ApplicationContainerImpl.getCommonClass(createNodeList(A.class,B.class,C.class)));
        assertEquals(A.class, ApplicationContainerImpl.getCommonClass(createNodeList(B.class,C.class,A.class)));
        assertEquals(A.class, ApplicationContainerImpl.getCommonClass(createNodeList(B.class,C.class)));
        assertEquals(B.class, ApplicationContainerImpl.getCommonClass(createNodeList(B.class)));
        assertEquals(C.class, ApplicationContainerImpl.getCommonClass(createNodeList(C.class, C.class)));
        assertNull(ApplicationContainerImpl.getCommonClass(createNodeList()));
    }

    private List<Node> createNodeList(Class... classList) throws Exception
    {
        List<Node> nodes = new ArrayList<Node>();
        for (Class aClass : classList)
        {
             nodes.add(new DummyNode(aClass.newInstance()));
        }
        return nodes;
    }

    private static class DummyNode extends NodeImpl
    {
        public DummyNode(Object instance)
        {
            super(null, null, null, new ObjectNodeContextImpl(null, null, new ObjectMeta(null, instance, null), null));
        }
    }
}
