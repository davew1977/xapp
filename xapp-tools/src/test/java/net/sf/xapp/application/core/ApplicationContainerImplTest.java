/*
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 *
 */
package net.sf.xapp.application.core;

import net.sf.xapp.application.api.Node;
import junit.framework.TestCase;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class ApplicationContainerImplTest extends TestCase {

    public void testGetCommonClassFromNodeList() throws Exception {
        assertEquals(A.class, ApplicationContainerImpl.getCommonClass(createNodeList(A.class, B.class, C.class)));
        assertEquals(A.class, ApplicationContainerImpl.getCommonClass(createNodeList(B.class, C.class, A.class)));
        assertEquals(A.class, ApplicationContainerImpl.getCommonClass(createNodeList(B.class, C.class)));
        assertEquals(B.class, ApplicationContainerImpl.getCommonClass(createNodeList(B.class)));
        assertEquals(C.class, ApplicationContainerImpl.getCommonClass(createNodeList(C.class, C.class)));
        assertNull(ApplicationContainerImpl.getCommonClass(createNodeList()));
    }

    private List<Node> createNodeList(Class... classList) throws Exception {
        List<Node> nodes = new ArrayList<Node>();
        for (Class aClass : classList) {
            Node node = mock(Node.class);
            when(node.wrappedObject()).thenReturn("ok");
            when(node.wrappedObjectClass()).thenReturn(aClass);
            nodes.add(node);
        }
        return nodes;
    }
}
