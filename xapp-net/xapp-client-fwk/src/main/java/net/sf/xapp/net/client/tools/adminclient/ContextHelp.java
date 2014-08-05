/*
 *
 * Date: 2010-mar-04
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.tools.adminclient;

import ng.Global;
import ngpoker.codegen.model.ComplexType;
import ngpoker.codegen.model.EnumType;
import ngpoker.codegen.model.Field;
import ngpoker.codegen.model.Message;

import java.util.ArrayList;
import java.util.List;

public class ContextHelp {
    private List<Message> messages = new ArrayList<Message>();
    private TestData m_testData = TestData.load();

    public ContextHelp() {
        messages = Global.allMessages();
    }

    public HelpTuple getHelp(String line) {
        if (line == null) {
            line = "";
        }
        HelpTuple h = new HelpTuple();
        line = line.trim();

        if (line.matches(ScriptPreprocessor.LOOP_REGEXP)) {
            int index = line.indexOf('x') + 1;
            line = line.substring(index).trim();
            h.m_insertIndex = index;
        }
        String[] chunks = line.split("\\s+", 2);

        String command = chunks[0];

        if (chunks.length == 1) {
            for (Message in : messages) {
                if (in.uniqueObjectKey().startsWith(command)) {
                    InsertionSuggestion is = new InsertionSuggestion();
                    is.m_template = in.uniqueObjectKey() + ",[" + createDummyValue(in, is.m_paramHelp) + "]";
                    h.m_insertionSuggestions.add(is);
                    if (in.getDescription() != null) {
                        is.m_tooltip = "<html>" + in.getDescription().replace("\n", "<br>") + "</html>";
                    }
                }
            }
            for (Script script : m_testData.scripts) {
                if (script.getName().startsWith(command)) {
                    InsertionSuggestion is = new InsertionSuggestion();
                    List<String> paramMeta = script.getParamMeta();
                    is.m_template = script.getName();
                    for (int i = 0; i < paramMeta.size(); i++) {
                        String s = paramMeta.get(i);
                        is.m_template += " $" + i;
                        is.m_paramHelp.add(s);
                    }
                    is.m_template += " $" + paramMeta.size();
                    h.m_insertionSuggestions.add(is);
                    if (script.getDescription() != null) {
                        is.m_tooltip = "<html>" + script.getDescription().replace("\n", "<br>") + "</html>";
                    }

                }

            }
        }
        return h;
    }

    private String createDummyValue(ComplexType in, List<String> paramHelp) {
        StringBuilder sb = new StringBuilder();
        int varcount = createDummyValue(sb, in, 0, paramHelp);
        sb.append("$" + varcount);
        return sb.toString();
    }

    private int createDummyValue(StringBuilder sb, ComplexType complexType, int varCount, List<String> paramHelp) {

        List<Field> fieldList = complexType.resolveFields(true);
        for (Field field : fieldList) {
            if (field.isCollection()) {
                sb.append("[");
            }
            if(field.isMap()) {
                sb.append("[");
                sb.append("$" + varCount++);
                paramHelp.add(field.getType() + " Key");

                sb.append(",");
            }
            if (field.getType() instanceof ComplexType) {
                if (field.isReference()) {
                    sb.append("$" + varCount++);
                    paramHelp.add(field.getType() + " Key" + (field.isList() ? "(list)" : ""));

                }
                else {
                    sb.append("[");
                    varCount = createDummyValue(sb, (ComplexType) field.getType(), varCount, paramHelp);
                    sb.append("]");
                }
            }
            else {
                sb.append("$" + varCount++);
                String enumValues = "";
                if (field.getType() instanceof EnumType) {
                    EnumType enumType = (EnumType) field.getType();
                    enumValues = String.valueOf(enumType.getValues());
                }
                paramHelp.add(field.getType() + " " + field.getName() + (field.isList() ? "(list)" : "") +
                        enumValues);
            }
            if(field.isMap()) {
                sb.append("]");
            }
            if (field.isCollection()) {
                sb.append("]");
            }
            sb.append(", ");

        }
        return varCount;
    }

    public static class HelpTuple {
        public List<InsertionSuggestion> m_insertionSuggestions = new ArrayList<InsertionSuggestion>();
        public int m_insertIndex;
    }

    public static class InsertionSuggestion {
        public String m_template;
        public List<String> m_paramHelp = new ArrayList<String>();
        public String m_tooltip;

        @Override
        public String toString() {
            return m_template;
        }
    }
}