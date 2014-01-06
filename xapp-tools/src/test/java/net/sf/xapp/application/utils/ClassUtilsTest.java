/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.application.utils;

import net.sf.xapp.application.core.C;
import net.sf.xapp.application.core.A;
import net.sf.xapp.application.core.B;
import net.sf.xapp.utils.ClassUtils;
import net.sf.xapp.annotations.objectmodelling.TrackKeyChanges;
import junit.framework.TestCase;


public class ClassUtilsTest extends TestCase
{
    public void testGetCommonClass()
    {
       assertEquals(A.class, ClassUtils.getCommonClass(A.class, B.class));
       assertEquals(A.class, ClassUtils.getCommonClass(B.class, A.class));
       assertEquals(A.class, ClassUtils.getCommonClass(B.class, C.class));
       assertEquals(A.class, ClassUtils.getCommonClass(C.class, B.class));
       assertEquals(A.class, ClassUtils.getCommonClass(A.class, C.class));
       assertEquals(A.class, ClassUtils.getCommonClass(C.class, A.class));
       assertEquals(C.class, ClassUtils.getCommonClass(C.class, C.class));
       assertEquals(B.class, ClassUtils.getCommonClass(B.class, B.class));
       assertEquals(A.class, ClassUtils.getCommonClass(A.class, A.class));
    }

    public void testHasAnnotationInHeirarchy()
    {
        assertEquals(true, ClassUtils.hasAnnotationInHeirarchy(TrackKeyChanges.class, X.class));
        assertEquals(false, ClassUtils.hasAnnotationInHeirarchy(TrackKeyChanges.class, A.class));
        assertEquals(true, ClassUtils.hasAnnotationInHeirarchy(TrackKeyChanges.class, Y.class));
        assertEquals(true, ClassUtils.hasAnnotationInHeirarchy(TrackKeyChanges.class, J.class));
        assertEquals(true, ClassUtils.hasAnnotationInHeirarchy(TrackKeyChanges.class, Z.class));
        assertEquals(true, ClassUtils.hasAnnotationInHeirarchy(TrackKeyChanges.class, K.class));
        assertEquals(true, ClassUtils.hasAnnotationInHeirarchy(TrackKeyChanges.class, L.class));
    }

    @TrackKeyChanges
    interface X {}

    interface Y extends X {}

    class J implements Y{}

    @TrackKeyChanges
    class Z{}

    class K extends Z{}

    class L extends K{}
}
