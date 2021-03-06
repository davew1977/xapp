package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.objectmodelling.api.InspectionType;
import net.sf.xapp.testmodels.Category;
import net.sf.xapp.testmodels.Site;
import net.sf.xapp.testmodels.SiteModel;
import net.sf.xapp.testmodels.SpecialCategory;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;

/**
 */
public class TreeContextTest {
    private ObjectMeta<Site> siteObjMeta;

    @Test
    public void testTreeFunctions() {
        ClassModelManager<SiteModel> cdb = new ClassModelManager<SiteModel>(SiteModel.class, InspectionType.FIELD);
        ObjectMeta<SiteModel> rootObjMeta = cdb.getRootUnmarshaller().unmarshalURL("classpath:///net/sf/xapp/testmodels/sitemodel.xml");

        SiteModel siteModel = rootObjMeta.getInstance();
        siteObjMeta = rootObjMeta.getObjMeta(Site.class, "peppa-k");
        assertEquals("peppa-k", siteObjMeta.getKey());
        Map<String, ObjectMeta> all = siteObjMeta.all(Category.class);
        assertEquals(7, all.size());

        ObjectMeta<Category> trousersObjMeta = catOM("clothes/trousers");
        assertEquals("trousers", trousersObjMeta.getKey());
        assertEquals("peppa-k/clothes/trousers", trousersObjMeta.getGlobalKey());
        assertEquals("peppa-k.clothes.trousers", trousersObjMeta.getGlobalKey(PathSeparator.DOT));
        ObjectMeta<Category> offerObjMeta = catOM("clothes/trousers/short/on offer!");
        Category offer = offerObjMeta.getInstance();
        assertEquals("peppa-k/clothes/trousers/short/on offer!", offerObjMeta.getGlobalKey());
        ObjectMeta<SpecialCategory> offer2 = siteObjMeta.find(SpecialCategory.class, "clothes/trousers/short/on offer!");
        assertEquals("peppa-k/clothes/trousers/short/on offer!", offer2.getGlobalKey());

        assertEquals("short/on offer!", NamespacePath.fullPath(trousersObjMeta, offerObjMeta));
        assertEquals("clothes/trousers/short/on offer!", NamespacePath.fullPath(siteObjMeta, offerObjMeta));
        assertEquals("peppa-k/clothes/trousers/short/on offer!", NamespacePath.fullPath(rootObjMeta, offerObjMeta));


        //test tree functions
        Category shortCat = offer.parent();
        Category trousers = trousersObjMeta.getInstance();
        Category clothes = trousers.parent();
        List<Category> offerPath = asList(clothes, trousers, shortCat, offer);
        assertEquals(offerPath, offer.path());
        assertEquals(trousers.children(), trousers.getSubCategories());

        Category longCat = cat("clothes/trousers/long");
        Category leggings = cat("clothes/leggings");
        Category shirts = cat("clothes/shirts");
        assertEquals(asList(clothes, trousers, shortCat, offer, longCat, leggings, shirts), clothes.enumerate(Category.class));
        assertEquals(singletonList(offer), clothes.enumerate(SpecialCategory.class));

        assertEquals("clothes.shirts", shirts.pathKey());
        assertEquals("clothes.trousers.short.on offer!", offer.pathKey());

        assertEquals(leggings, clothes.getChild("leggings"));
        assertEquals(offer, shortCat.getChild("on offer!"));

        assertTrue(clothes.isRoot());
        assertFalse(clothes.isLeaf());
        assertFalse(trousers.isRoot());
        assertFalse(trousers.isLeaf());
        assertFalse(offer.isRoot());
        assertTrue(offer.isLeaf());


    }

    private ObjectMeta<Category> catOM(String path) {
        return siteObjMeta.find(Category.class, path);
    }

    private Category cat(String path) {
        return catOM(path).getInstance();
    }

}
