package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.objectmodelling.api.InspectionType;
import net.sf.xapp.testmodels.Category;
import net.sf.xapp.testmodels.Site;
import net.sf.xapp.testmodels.SiteModel;
import net.sf.xapp.testmodels.SpecialCategory;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 */
public class TreeContextTest {
    @Test
    public void testTreeFunctions() {
        ClassModelManager<SiteModel> cdb = new ClassModelManager<SiteModel>(SiteModel.class, InspectionType.FIELD);
        ObjectMeta<SiteModel> rootObjMeta = cdb.getRootUnmarshaller().unmarshalURL("classpath:///net/sf/xapp/testmodels/sitemodel.xml");

        SiteModel siteModel = rootObjMeta.getInstance();
        ObjectMeta<Site> siteObjMeta = rootObjMeta.getObjMeta(Site.class, "peppa-k");
        assertEquals("peppa-k", siteObjMeta.getKey());
        Map<String, ObjectMeta> all = siteObjMeta.all(Category.class);
        assertEquals(7, all.size());

        ObjectMeta<Category> trousersObjMeta = siteObjMeta.find(Category.class, "clothes/trousers");
        assertEquals("trousers", trousersObjMeta.getKey());
        assertEquals("peppa-k/clothes/trousers", trousersObjMeta.getGlobalKey());
        assertEquals("peppa-k.clothes.trousers", trousersObjMeta.getGlobalKey("."));
        ObjectMeta<Category> offerObjMeta = siteObjMeta.find(Category.class, "clothes/trousers/short/on offer!");
        Category offer = offerObjMeta.getInstance();
        assertEquals("peppa-k/clothes/trousers/short/on offer!", offerObjMeta.getGlobalKey());
        ObjectMeta<SpecialCategory> offer2 = siteObjMeta.find(SpecialCategory.class, "clothes/trousers/short/on offer!");
        assertEquals("peppa-k/clothes/trousers/short/on offer!", offer2.getGlobalKey());

        assertEquals("short/on offer!", NamespacePath.fullPath(trousersObjMeta, offerObjMeta));
        assertEquals("clothes/trousers/short/on offer!", NamespacePath.fullPath(siteObjMeta, offerObjMeta));
        assertEquals("peppa-k/clothes/trousers/short/on offer!", NamespacePath.fullPath(rootObjMeta, offerObjMeta));


        //test tree functions
        //todo
        Category shortCat = offer.parent();
        Category trousers = trousersObjMeta.getInstance();
        Category clothes = trousers.parent();
        List<Category> offerPath = Arrays.asList(clothes, trousers, shortCat, offer);
        assertEquals(offerPath, offer.path());
        assertEquals(trousers.children(), trousers.getSubCategories());

    }

}
