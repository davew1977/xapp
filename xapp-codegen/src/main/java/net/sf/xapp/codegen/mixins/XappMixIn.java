/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.mixins;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.codegen.GenContext;
import net.sf.xapp.codegen.model.ComplexType;
import net.sf.xapp.codegen.model.Entity;
import net.sf.xapp.codegen.model.Field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class XappMixIn implements MixIn<ComplexType>
{
    public void mixIn(ComplexType complexType, CodeFile ct)
    {
        ct.addImport("net.sf.xapp.annotations.objectmodelling.*");
        ct.addImport("net.sf.xapp.annotations.marshalling.*");
        ct.addImport("net.sf.xapp.annotations.application.*");

        addValidImplementations(complexType, ct);

        addPrimaryKey(complexType, ct);

        processFields(complexType, ct);
    }

    private void processFields(ComplexType entity, CodeFile ct) {
        List<Field> fields = entity.resolveFields(false);
        for (Field field : fields) {
            if (field.isReference()) {
                if (field.isList()) {
                    ct.getMethod(field.accessorName()).addAnnotation("@ContainsReferences");
                } else {
                    ct.getMethod(field.accessorName()).addAnnotation("@Reference");
                }
            }

            if(field.is("formattedText")){
                ct.getMethod(field.accessorName()).addAnnotation("@FormattedText");
            }
            String editorWidget = field.get("editorWidget");
            if(editorWidget !=null) {
                ct.getMethod(field.accessorName()).addAnnotation("@EditorWidget(%s.class)", editorWidget);
            }
        }
    }

    private void addPrimaryKey(ComplexType entity, CodeFile ct) {
        Field primaryKey = entity.keyField();
        if (primaryKey != null && entity instanceof Entity) {
            ct.getMethod(primaryKey.accessorName()).addAnnotation("@Key");
        } else {
            //System.out.println("warning: entity does not have primary key: " + entity);
        }
    }

    private void addValidImplementations(ComplexType entity, CodeFile ct) {
        if(entity.isAbstract()) {
            List<ComplexType> subTypes = entity.getSubTypes();
            StringBuilder args = new StringBuilder();
            for (int i = 0; i < subTypes.size(); i++) {
                ComplexType complexType = subTypes.get(i);
                args.append(complexType.getName()).append(".class");
                args.append(i<subTypes.size()-1 ? ", " : "");
            }

            ct.addAnnotation("@ValidImplementations({%s})", args.toString());
        }
    }

    public Collection<CodeFile> generate(GenContext genContext, ComplexType complexType) {
        Collection<CodeFile> result = new ArrayList<CodeFile>();
        if(complexType instanceof Entity && ((Entity) complexType).isObservable()) {
            result.add(new XappListenerGenerator(genContext).generate((Entity) complexType));
        }
        return result;
    }
}