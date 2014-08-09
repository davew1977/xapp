package net.sf.xapp.codegen.mixins;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.utils.CollectionsUtils;
import net.sf.xapp.utils.Filter;
import net.sf.xapp.codegen.GeneratorContext;
import net.sf.xapp.codegen.model.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static net.sf.xapp.utils.StringUtils.*;

/**
 * Created with IntelliJ IDEA.
 * User: davidw
 * Date: 4/22/14
 * Time: 7:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class XappListenerGenerator {
    private final GeneratorContext generatorContext;

    public XappListenerGenerator(GeneratorContext generatorContext) {
        this.generatorContext = generatorContext;
    }


    public CodeFile generate(Entity entity) {
        CodeFile ct = generatorContext.createJavaFile(entity);
        String cn = entity.getName();
        String updateType = cn + "Update";
        String vn = decapitaliseFirst(cn);
        String un = decapitaliseFirst(updateType);
        new GenericMixIn(entity.getPackageName()).mixIn("Xapp" + cn + "Listener", ct);

        ct.addImport("net.sf.xapp.objectmodelling.api.ClassDatabase");
        ct.addImport("net.sf.xapp.application.api.*");


        ct.setSuper(format("SimpleApplication<%s>", entity.getName()));
        ct._private().field("ClassDatabase", "cdb");
        ct._private().field(updateType, un);

        ct.constructor("ClassDatabase cdb", updateType + " " + un);
        ct.line("this.cdb = cdb");
        ct.line("this.%s = %s", un, un);

        ct.method("init", "void", "ApplicationContainer appContainer");
        ct.line("super.init(appContainer)");

        //node added method
        List<FieldSet> fieldSets = entity.deepAnalyze();
        List<FieldSet> complexFieldSets = CollectionsUtils.filter(fieldSets, new Filter<FieldSet>() {
            @Override
            public boolean matches(FieldSet fieldSet) {
                return fieldSet.last().isComplex();
            }
        });
        List<FieldSet> simpleFieldSets = CollectionsUtils.filter(fieldSets, new Filter<FieldSet>() {
            @Override
            public boolean matches(FieldSet fieldSet) {
                return !fieldSet.last().isComplex();
            }
        });
        ct.method("nodeAdded", "void", "Node node");
        {
            for (int i = 0; i < complexFieldSets.size(); i++) {
                FieldSet fieldSet = complexFieldSets.get(i);
                Field last = fieldSet.last();
                ct.addImport(last.getType().getPackageName() + ".*");
                String tn = last.typeName();
                ct.startBlock("%sif(node.isA(%s.class))", i > 0 ? "else " : "", tn);
                ct.line("%s obj = node.wrappedObject()", tn);
                List<String> params = new ArrayList<String>();
                List<Field> fields = fieldSet.significantFields();
                assert fields.size() == 1 || fields.size() == 2;
                if (fields.size() == 2) {
                    ct.line("%s parent = node.getParent().nearestWrappedObject()", fields.get(0).typeName());
                    params.add("parent.getKey()");
                }
                if (last.isMap()) {
                    ComplexType lastType = (ComplexType) last.getType();
                    params.add(format("obj.get%s()", capitalizeFirst(lastType.keyField().getName())));
                }
                params.add("obj");

                ct.line("%s.%s(%s)", un, MethodType.ADD.methodName(fieldSet, false), join(params, ", "));
                ct.endBlock();
            }
        }
        //node removed method
        ct.method("nodeRemoved", "void", "Node node", "boolean wasCut");
        {

            for (int i = 0; i < complexFieldSets.size(); i++) {
                FieldSet fieldSet = complexFieldSets.get(i);
                Field last = fieldSet.last();
                ct.addImport(last.getType().getPackageName() + ".*");
                String tn = last.typeName();
                ct.startBlock("%sif(node.isA(%s.class))", i > 0 ? "else " : "", tn);
                if (last.isMap()) {
                    ct.line("%s obj = node.wrappedObject()", tn);
                }
                List<String> params = new ArrayList<String>();
                List<Field> fields = fieldSet.significantFields();
                assert fields.size() == 1 || fields.size() == 2;
                if (fields.size() == 2) {
                    ct.line("%s parent = node.getParent().nearestWrappedObject()", fields.get(0).typeName());
                    params.add("parent.getKey()");
                }
                if (last.isMap()) {
                    ComplexType lastType = (ComplexType) last.getType();
                    params.add(format("obj.get%s()", capitalizeFirst(lastType.keyField().getName())));
                }
                else {
                    params.add("node.index()");
                }

                ct.line("%s.%s(%s)", un, MethodType.REMOVE.methodName(fieldSet, false), join(params, ", "));
                ct.endBlock();
            }
        }
        ct.addImport("net.sf.xapp.objectmodelling.core.PropertyChange");
        ct.addImport("java.util.Map");
        ct.method("nodeUpdated", "void", "Node node", "Map<String, PropertyChange> changes");
        {
            ct.startBlock("for (PropertyChange tuple : changes.values())");
            {
                for (FieldSet fieldSet : simpleFieldSets) {
                    System.out.println(fieldSet);
                    Field last = fieldSet.last();
                    Field penultimate = fieldSet.penultimate();
                    String tn = penultimate.typeName();
                    ct.startBlock("if(tuple.target instanceof %s && tuple.property.getName().equals(\"%s\"))",
                            tn,
                            capitalizeFirst(last.getName()));
                    if (!penultimate.isList()) {
                        ct.line("%s obj = (%s) tuple.target", tn, tn);
                    }
                    LinkedList<String> params = new LinkedList<String>();
                    List<Field> fields = new ArrayList<Field>(fieldSet.getFields());
                    for (int i = 0; i < fieldSet.getFields().size(); i++) {
                        Field field = fieldSet.getFields().get(fieldSet.getFields().size() - (i + 1));
                        Type type = field.getType();
                        if(i==0) {
                            params.add(format("(%s) tuple.newVal", last.typeName()));
                        } else if(i==1) {
                            if (field.isMap()) {
                                ComplexType complexType = (ComplexType) field.getType();
                                params.addFirst(format("obj.get%s()", capitalizeFirst(complexType.keyField().getName())));
                            }
                            else {
                                params.addFirst("node.index()");
                            }
                        } else if(i==2) {
                            ct.line("Node parentNode = node.getParent()");
                            if (field.isMap()) {
                                ct.line("%s parent = parentNode.nearestWrappedObject()", fields.get(0).typeName());
                                params.addFirst("parent.getKey()");
                            } else {
                                params.addFirst("parent.index()");
                            }
                        } else {
                            throw new RuntimeException("deeper nesting not supported " + fieldSet);
                        }
                    }
                    ct.line("%s.%s(%s)", un, MethodType.CHANGE.methodName(fieldSet, false), join(params, ", "));
                    ct.endBlock();
                }
            }
            ct.endBlock();
        }
        return ct;
    }
}
