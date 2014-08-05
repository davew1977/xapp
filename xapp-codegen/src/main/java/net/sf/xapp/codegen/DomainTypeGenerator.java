/*
 *
 * Date: 2011-feb-09
 * Author: davidw
 *
 */
package net.sf.xapp.codegen;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.codegen.mixins.*;
import net.sf.xapp.codegen.model.*;

import java.util.Collection;

import static java.lang.String.format;

public class DomainTypeGenerator {
    private final GenContext genContext;

    public DomainTypeGenerator(GenContext genContext) {
        this.genContext = genContext;
    }

    public CodeFile genDomainClass(ComplexType complexType) {
        CodeFile ct = genContext.createJavaFile(complexType);
        ct.addImport(NGPGenerator.FWK_PACKAGE_NAME() + ".*");
        ct.addImport(complexType.model().getCorePackageName() + ".MessageTypeEnum");
        ct.addImport(complexType.model().getCorePackageName() + ".ObjectTypeEnum");
        ct.addImport("net.sf.xapp.net.common.types.*");
        String utilPackageName = "net.sf.xapp.net.common.util";
        ct.addImport(utilPackageName + ".StringUtils");
        ct.addImport("java.util.List");
        ct.addImport("java.util.ArrayList");
        ct.addImport("java.util.Collection");
        ct.addImport("java.util.Map");
        ct.addImport("java.util.LinkedHashMap");
        ct.addImport("java.util.LinkedHashSet");
        new GenericMixIn(complexType.getPackageName()).mixIn(complexType, ct);
        new JavaBeanMixIn().mixIn(complexType, ct);
        Collection<String> otherImports = complexType.resolvePackages();
        for (String otherImport : otherImports) {
            if (otherImport != null && !otherImport.equals("java.lang")) {
                ct.addImport(format("%s.*", otherImport));
            }
        }
        if (!complexType.isAbstract()) {
            new CustomSerializationMixIn().mixIn(complexType, ct);
            if (!genContext.isLight()) {
                new ToStringMixin().mixIn(complexType, ct);
                new StringSerializationMixIn(utilPackageName).mixIn(complexType, ct);
                new PrettyPrintMixIn("expandToString").mixIn(complexType, ct);
            }
            if (complexType instanceof ValueObject) {
                ValueObject valueObject = (ValueObject) complexType;
                new EqualsAndHashcodeMixIn().mixIn(valueObject, ct);
            }
            if (complexType instanceof Entity) {
                Entity entity = (Entity) complexType;
                if (entity.isObservable()) {
                    new ObserverAPIMixin(false).mixIn(entity, ct);
                    new ObserverAPIMixin(true).mixIn(entity, ct); //implement listener contract too
                    new ObservableMixin().mixIn(entity, ct);
                    ct.addImplements(entity.getName() + "Update");
                    ct.addImplements(entity.getName() + "Listener");

                }
                ct.addImplements("Entity");
            }

            boolean response = complexType instanceof ResponseMessage;
            if (response || !(complexType instanceof Message)) {
                ct.method("type", "ObjectType");
                ct.line("return %sTypeEnum.%s", response ? "Message" : "Object", complexType.uniqueObjectKey());
            }

            new ReferenceAwareMixin().mixIn(complexType, ct);
        }
        if (complexType instanceof LobbyType) {
            new StorableMixIn().mixIn(complexType, ct);
        }

        if (complexType.model().isXappPluginEnabled()) {
            new XappMixIn().mixIn(complexType, ct);
        }
        return ct;
    }
}
