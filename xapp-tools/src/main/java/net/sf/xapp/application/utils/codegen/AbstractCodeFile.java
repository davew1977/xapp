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
package net.sf.xapp.application.utils.codegen;

import net.sf.xapp.utils.FileUtils;
import net.sf.xapp.utils.StringUtils;
import net.sf.xapp.utils.SysOutXappLogger;
import net.sf.xapp.utils.XappLogger;

import java.io.File;
import java.util.*;

import static java.lang.String.format;

/**
 *
 */
public abstract class AbstractCodeFile implements CodeFile, EnumContext {
    public static final String VOID = "void";
    public static final String TAB = "    ";
    public static final int MAX_LINE_LENGTH = 120;
    public static XappLogger logger = new SysOutXappLogger();

    private boolean writeToFile;
    public String modifier = "public";
    protected boolean m_interface;
    protected String name;
    protected String mPackage;
    protected String superClass;
    protected LinkedHashSet<String> imports;
    protected List<String> implementedInterfaces;
    protected List<String> headerComments;
    protected List<String> classComments;
    protected Set<MethodImpl> methods;
    protected List<MethodImpl> constructors;
    protected List<Field> fields;
    protected MethodContext currentMethodContext;
    protected DocContext currentDocContext;
    public String FIELD_PREFIX = "";
    protected String currentIndent = "";
    protected File outPath;
    //modifiers for coming methods
    private boolean m_static;
    private boolean m_final;
    private boolean m_transient;
    private boolean m_abstract;
    private Collection<String> enumValues;
    private List<EnumContext> enumContexts;
    private List<ClassContext> innerClasses;
    private boolean abstractClass;
    private boolean staticClass;
    private boolean m_protected;
    private boolean m_private;
    private Object attachment;
    private String fileHeader = "";
    private Set<Integer> importBlankLines;
    private Map<CodeFileSection, String> docBlocks;
    private List<String> annotations;

    public AbstractCodeFile(File outPath, boolean writeToFile) {
        this.outPath = outPath;
        this.writeToFile = writeToFile;
        imports = new LinkedHashSet<String>();
        implementedInterfaces = new ArrayList<String>();
        methods = new LinkedHashSet<MethodImpl>();
        constructors = new ArrayList<MethodImpl>();
        fields = new ArrayList<Field>();
        classComments = new ArrayList<String>();
        enumContexts = new ArrayList<EnumContext>();
        enumValues = new LinkedHashSet<String>();
        innerClasses = new ArrayList<ClassContext>();
        importBlankLines = new LinkedHashSet<Integer>();
        docBlocks = new HashMap<CodeFileSection, String>();
        annotations = new ArrayList<String>();
        currentDocContext = this;
    }

    protected boolean isEnum() {
        return !enumValues.isEmpty() || !enumContexts.isEmpty();
    }

    @Override
    public CodeFile setStatic() {
        staticClass = true;
        return this;
    }

    public CodeFile setInterface() {
        m_interface = true;
        return this;
    }

    public CodeFile setAbstract() {
        abstractClass = true;
        return this;
    }

    public CodeFile setEnum(List<String> values) {
        enumValues = values;
        return this;
    }

    @Override
    public EnumContext newEnumValue(String name) {
        if (!enumValues.isEmpty()) {
            throw new RuntimeException("cannot use both enum value objects and strings");
        }
        JavaFile javaFile = new JavaFile(null, false); //Reusing this class
        javaFile.setName(name);
        enumContexts.add(javaFile);
        return javaFile;
    }

    @Override
    public CodeFile addSimpleEnumValue(String name) {
        enumValues.add(name);
        return this;
    }

    public void addDocLine(String line) {
        classComments.add(line);

    }

    public CodeFile setName(String name) {
        this.name = name;
        return this;
    }

    public CodeFile setPackage(String p) {
        mPackage = p;
        return this;
    }

    public CodeFile addImport(String i) {
        imports.add(i);
        return this;
    }

    public CodeFile setSuper(String s) {
        superClass = s;
        return this;
    }

    public CodeFile addImplements(String i) {
        implementedInterfaces.add(i);
        return this;
    }

    public CodeFile field(String type, String varname, String defaultValue) {
        return field(type, varname, Access.PRIVATE, defaultValue);
    }

    public CodeFile field(String type, String varname) {
        return field(type, varname, Access.PRIVATE, null);
    }

    public CodeFile constructor(String... params) {
        method(name, "", params);
        return this;
    }

    public CodeFile constructors(List<String> types, List<String> paramNames) {
        assert types.size() == paramNames.size();
        for (int i = 0; i < types.size(); i++) {
            String type = types.get(i);
            String paramName = paramNames.get(i);


        }
        return this;
    }

    public CodeFile defaultConstructor() {
        method(name, "");
        return this;
    }

    public CodeFile field(String type, String varname, Access access, String defaultValue) {
        Field field = createField();
        field.m_type = type;
        field.m_name = varname;
        field.m_modifier = access == Access.PUBLIC ? "public" : m_protected ? "protected" : "private";
        m_protected = false;
        field.m_defaultValue = defaultValue;
        field.m_static = m_static;
        field.m_final = m_final;
        field.m_transient = m_transient;
        field.m_prefix = FIELD_PREFIX;
        fields.add(field);
        if (access != Access.PRIVATE && access != Access.PUBLIC && access != Access.WRITE_ONLY) //generate getter
        {
            String methodName = getterName(type, varname);
            method(methodName, type);
            line("return " + FIELD_PREFIX + varname);
        }
        if (access == Access.READ_WRITE || access == Access.WRITE_ONLY) {
            method(setterName(varname), VOID, setterParams(type, varname));
            line((FIELD_PREFIX.equals("") ? "this." : FIELD_PREFIX) + varname + " = " + varname);
        }
        m_static = false;
        m_final = false;
        m_transient = false;
        m_private = false;
        currentDocContext = field;
        return this;
    }

    private String setterParams(String type, String varname) {
        return type + " " + varname;
    }

    public static String setterName(String varname) {
        return "set" + StringUtils.capitalizeFirst(varname);
    }

    public static String getterName(String type, String varname) {
        String prefix = type.equals("boolean") || type.equals("Boolean") ? "is" : "get";
        String methodName = prefix + StringUtils.capitalizeFirst(varname);
        return methodName;
    }

    public CodeFile field(String type, String varname, Access access) {
        return field(type, varname, access, null);
    }

    public CodeFile method(String name, String returnType, String... params) {
        return method2(name, returnType, null, params);
    }

    public Method getMethod(String name, String... params) {
        return getMethod(name, methods, params);
    }

    public Method getMethod(String name, Collection<MethodImpl> methods, String... params) {
        List<String> paramList = normalise(Arrays.asList(params));
        for (MethodImpl method : methods) {
            if (method.m_name.equals(name) &&
                    (params.length == 0 || method.m_params.equals(paramList))) {
                return method;
            }
        }
        String pl = paramList.toString();
        pl = pl.substring(1, pl.length() - 1);
        throw new RuntimeException("method " + name + "(" + pl + ") not found in " + getFileName());
    }

    public Field getField(String name) {
        for (Field field : fields) {
            if (field.m_name.equals(name)) {
                return field;
            }
        }
        return null;
    }

    public Method getAccessor(String fieldName) {
        return getMethod(getterName(getField(fieldName).m_type, fieldName));
    }

    public Method getModifier(String fieldName) {
        return getMethod(setterName(fieldName), setterParams(getField(fieldName).m_type, fieldName));
    }

    public Method getDefaultConstructor() {
        return getMethod(name, constructors);
    }

    public List<Method> getConstructors() {
        return (List) constructors;
    }


    public Collection<Method> getMethods() {
        return (Collection) methods;
    }

    public CodeFile method2(String name, String returnType, String exceptions, String... params) {
        MethodImpl method = createMethod();
        method.m_name = name;
        method.m_returnType = returnType;
        method.m_params = normalise(Arrays.asList(params));
        method.m_static = m_static;
        method.m_final = m_final;
        method.m_abstract = m_abstract;
        method.m_expections = exceptions;
        method.m_constructor = returnType.equals("") && name.equals(this.name);
        method.m_modifier = m_protected ? "protected" : m_private ? "private" : "public";
        method.attachment = this.attachment;
        if (method.m_constructor) {
            constructors.add(method);
        }
        else {
            methods.remove(method);
            methods.add(method);
        }
        currentMethodContext = method;
        m_static = false;
        m_final = false;
        m_abstract = false;
        currentDocContext = method;
        m_protected = false;
        m_private = false;
        return this;
    }

    /**
     * removes empty strings and splits double params
     */
    private static List<String> normalise(List<String> params) {
        List<String> result = new ArrayList<String>();
        for (String param : params) {
            if (StringUtils.isNullOrEmpty(param)) {
                continue;
            }
            String[] sepParams = param.split(",");
            for (String sepParam : sepParams) {
                result.add(sepParam.trim());
            }
        }
        return result;
    }

    protected abstract MethodImpl createMethod();

    protected abstract Field createField();

    public CodeFile line(String code, Object... args) {
        currentMethodContext.line(code, args);
        return this;
    }

    @Override
    public CodeFile setAutoSemiColon(boolean autoSemi) {
        currentMethodContext.setAutoSemiColon(autoSemi);
        return this;
    }

    @Override
    public CodeFile line() {
        return line("");
    }

    public CodeFile startBlock(String code, Object... args) {
        currentMethodContext.startBlock(code, args);
        return this;
    }

    public CodeFile annotate(String line, String... args) {
        currentDocContext.addAnnotation(line, args);
        return this;
    }

    public void addAnnotation(String line, String... args) {
        annotations.add(format(line, (Object[]) args));
    }

    public CodeFile endBlock() {
        currentMethodContext.endBlock();
        return this;
    }

    @Override
    public CodeFile endBlock(String code) {
        currentMethodContext.endBlock(code);
        return this;
    }

    public CodeFile docLine(String line, String... args) {
        currentDocContext.addDocLine(format(line, (Object[]) args));
        return this;
    }

    public CodeFile param(String type, String varname) {
        return param(type, varname, null);
    }

    public CodeFile param(String type, String varname, String doc) {
        return null;
    }

    public CodeFile iterate(String varname, boolean needIndex) {
        return null;
    }

    public void generate() {
        outputFile(genStringBuilder());
    }

    private StringBuilder genStringBuilder() {
        StringBuilder sb = new StringBuilder();
        doFileHeader(sb);
        doPackageDeclaration(sb);
        doImports(sb);
        doPossibleDocBlock(sb, CodeFileSection.IMPORTS);
        doClass(sb);
        return sb;
    }

    private void doPossibleDocBlock(StringBuilder sb, CodeFileSection section) {
        if (docBlocks.get(section) != null) {
            sb.append(docBlocks.get(section));
        }
    }

    private void doFileHeader(StringBuilder sb) {
        sb.append(fileHeader);
    }

    private void doClass(StringBuilder sb) {
        doClassDeclaration(sb);
        doEnum(sb);
        doFields(sb);
        doConstructors(sb);
        doMethods(currentIndent, m_interface, sb, new ArrayList<Method>(getMethods()));
        doInnerClasses(sb);
        doEndClass(sb);
    }

    public String generateString() {
        return genStringBuilder().toString();
    }

    private void doEnum(StringBuilder sb) {
        if (isEnum()) {
            String valueEndChar = !methods.isEmpty() ? ";" : "";
            if (enumContexts.isEmpty()) {
                List<String> enumValues = new ArrayList<String>(this.enumValues);
                for (int i = 0; i < enumValues.size(); i++) {
                    String enumValue = enumValues.get(i);
                    sb.append(currentIndent).append(enumValue).append(
                            i < enumValues.size() - 1 ? "," : valueEndChar).append("\n");
                }
                if (!enumValues.isEmpty()) {
                    sb.append("\n");
                }
            }
            else {
                for (int i = 0; i < enumContexts.size(); i++) {
                    EnumContext enumContext = enumContexts.get(i);
                    AbstractCodeFile ec = (AbstractCodeFile) enumContext;
                    sb.append(currentIndent).append(ec.name).append("\n");
                    sb.append(currentIndent).append("{\n");
                    indent();
                    doMethods(currentIndent, false, sb, new ArrayList<Method>(ec.getMethods()));
                    deIndent();
                    sb.append(currentIndent).append("}");
                    sb.append(i < enumContexts.size() - 1 ? "," : valueEndChar).append("\n");
                }
            }
        }
    }

    private void doInnerClasses(StringBuilder sb) {
        for (int i = 0; i < innerClasses.size(); i++) {
            AbstractCodeFile classContext = (AbstractCodeFile) innerClasses.get(i);
            classContext.currentIndent = currentIndent;
            classContext.doClass(sb);
            sb.append("\n");
        }
    }

    protected abstract void doEndClass(StringBuilder sb);

    protected abstract void doPackageDeclaration(StringBuilder sb);

    private void doClassDeclaration(StringBuilder sb) {
        //class comment
        appendDoc(currentIndent, classComments, sb);
        for (String annotation : annotations) {
            sb.append(currentIndent).append(annotation).append("\n");
        }
        sb.append(currentIndent).append(modifier);
        if (abstractClass) {
            sb.append(" abstract");
        }
        if (staticClass) {
            sb.append(" static");
        }
        String classType = m_interface ? "interface" : isEnum() ? "enum" : "class";
        sb.append(" ").append(classType).append(" ").append(name);
        if (superClass != null) {
            sb.append(" extends ").append(superClass);
        }
        if (!implementedInterfaces.isEmpty()) {
            sb.append(" implements");
            for (int i = 0; i < implementedInterfaces.size(); i++) {
                String implementedInterface = implementedInterfaces.get(i);
                sb.append(" ").append(implementedInterface);
                if (i < implementedInterfaces.size() - 1) {
                    sb.append(",");
                }
            }
        }
        sb.append(startBlockChars(currentIndent));
        indent();
    }

    private static void appendDoc(String indent, List<String> docLines, StringBuilder sb) {
        if (!docLines.isEmpty()) {
            sb.append(format("%s/**\n", indent));
            for (String classComment : docLines) {
                String space = classComment.isEmpty() ? "" : " ";
                sb.append(format("%s *%s%s\n", indent, space, classComment));
            }
            sb.append(format("%s */\n", indent));
        }
    }

    protected final void indent() {
        currentIndent += TAB;
    }

    protected final void deIndent() {
        currentIndent = currentIndent.substring(0, currentIndent.length() - TAB.length());
    }

    public static String tabs(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(TAB);
        }
        return sb.toString();
    }

    private void doImports(StringBuilder sb) {
        int i = 0;
        for (String s : imports) {
            if (importBlankLines.contains(i)) {
                sb.append("\n");
            }
            sb.append(currentIndent).append("import ").append(s).append(";\n");
            if (i == this.imports.size() - 1) {
                sb.append("\n");
            }
            i++;
        }
    }

    private void outputFile(StringBuilder sb) {
        if (Boolean.getBoolean("debug")) {
            logger.debug(sb.toString());
        }
        if (outPath == null) {
            throw new RuntimeException("output path not set");
        }
        if (writeToFile) {
            File dir = new File(getDirPath());
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File srcFile = new File(dir, getFileName());
            FileUtils.writeFile(sb.toString(), srcFile);
            logger.info("written " + srcFile);
        }
    }

    private String getDirPath() {
        return outPath + "/" + (mPackage != null ? mPackage.replace('.', '/') : "");
    }

    public String getFileName() {
        return name + "." + getFileSuffix();
    }

    public String getFullPath() {
        return getDirPath() + "/" + getFileName();
    }

    private void doConstructors(StringBuilder sb) {
        List<Method> constructors = getConstructors();
        doMethods(currentIndent, m_interface, sb, constructors);
        if (!constructors.isEmpty()) {
            sb.append("\n");
        }
    }

    private static void doMethods(String indent, boolean isInterface, StringBuilder sb, List<Method> methods) {
        for (int i = 0; i < methods.size(); i++) {
            MethodImpl method = (MethodImpl) methods.get(i);
            if (isInterface) {
                method.m_modifier = "";
            }
            appendDoc(indent, method.m_docLines, sb);
            for (String annotation : method.m_annotations) {
                sb.append(indent).append(annotation).append("\n");
            }
            sb.append(indent).append(method.generateSignature());
            if (isInterface || method.m_abstract) {
                sb.append(";\n");
            }
            else {
                sb.append(startBlockChars(indent));
                for (String codeLine : method.m_codeLines) {
                    sb.append(codeLine.equals("\n") ? "" : indent).append(codeLine);
                }
                sb.append(indent).append("}\n");
                sb.append("\n");
            }
        }
    }

    public static String startBlockChars(Object indent) {
        //sb.append("\n").append(indent).append("{\n");
        return " {\n";
    }

    private void doFields(StringBuilder sb) {
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            appendDoc(currentIndent, field.m_docLines, sb);
            for (String annotation : field.annotations) {
                sb.append(currentIndent).append(annotation).append("\n");
            }
            sb.append(currentIndent).append(field.generateDeclaration()).append("\n");
            sb.append("\n");
        }
    }

    public CodeFile _static() {
        m_static = true;
        return this;
    }

    public CodeFile _final() {
        m_final = true;
        return this;
    }

    public CodeFile _transient() {
        m_transient = true;
        return this;
    }

    public CodeFile _abstract() {
        m_abstract = true;
        return this;
    }

    @Override
    public CodeFile _protected() {
        m_protected = true;
        return this;
    }

    @Override
    public CodeFile _private() {
        m_private = true;
        return this;
    }

    public void clearFields() {
        fields.clear();
    }

    public void clearConstructors() {
        constructors.clear();
    }

    @Override
    public CodeFile attach(Object attachment) {
        this.attachment = attachment;
        return this;
    }

    @Override
    public ClassContext newInnerClass(String name) {
        JavaFile javaFile = new JavaFile(outPath, false);
        javaFile.setName(name);
        if (m_static) {
            javaFile.setStatic();
        }
        innerClasses.add(javaFile);
        return javaFile;
    }

    @Override
    public CodeFile setFileHeader(String content, Object... args) {
        this.fileHeader = format(content, args);
        return this;
    }

    @Override
    public CodeFile addImportBlankLine() {

        importBlankLines.add(imports.size());
        return this;
    }

    @Override
    public CodeFile insertDocBlockAfter(CodeFileSection section, String content, Object... args) {
        assert section == CodeFileSection.IMPORTS; //only support this for now
        docBlocks.put(section, format(content, args));
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getFullPath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractCodeFile that = (AbstractCodeFile) o;

        return getFullPath().equals(that.getFullPath());
    }

    @Override
    public int hashCode() {
        return getFullPath().hashCode();
    }

    public int getCurrentIndent() {
        return currentIndent.length();
    }

    protected abstract String getFileSuffix();
}