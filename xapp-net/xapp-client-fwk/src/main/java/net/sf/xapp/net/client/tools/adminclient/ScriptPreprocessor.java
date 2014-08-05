/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.tools.adminclient;

import net.sf.xapp.application.editor.EditorManager;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ClassModel;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Preprocessing included expanding variables and "scripts"
 */
public class ScriptPreprocessor
{
    private TestData m_testData;
    public static final Pattern ARG_PATTERN = Pattern.compile("\\$\\{[\\w\\.\\[\\]+%]*}");
    private Pattern OFFSET_PATTERN = Pattern.compile("[+]\\d+");
    private Pattern MOD_PATTERN = Pattern.compile("[%]\\d+");
    private String[] m_latestParams;
    private int m_iterationIndex;
    private InsideLoopResolver INSIDE_LOOP = new InsideLoopResolver();
    private OutsideLoopResolver OUTSIDE_LOOP = new OutsideLoopResolver();
    private ArgResolver m_argResolver = OUTSIDE_LOOP;
    private Map<String, Integer> m_seqMap = new HashMap<String, Integer>();
    public static final String LOOP_REGEXP = "^\\d+\\s*x\\s*.*";
    private long m_startTime;
    private long m_stopTime;
    private Processor processor;

    public ScriptPreprocessor(TestData testData)
    {
        m_testData = testData;
    }

    /**
     * runs a (potentially multiline) script
     * @param script
     */
    public void runScript(String script)
    {
        String[] lines = script.split("\n");
        for (String line : lines)
        {
            if (line.startsWith("#"))
            {
                continue;
            }
            exec(line);
        }
    }

    public void exec(String message)
    {
        //expand vars
        m_argResolver = OUTSIDE_LOOP;
        message = expandVariables(message);

        if (message.matches(LOOP_REGEXP)) //example: 100 x (command)
        {

            int times = Integer.parseInt(message.split("\\s")[0]);
            message = message.substring(message.indexOf('x') + 1).trim();

            m_argResolver = INSIDE_LOOP;
            for (int i = 0; i < times; i++)
            {
                m_iterationIndex = i;
                processInternal(expandVariables(message));
            }
        }
        else
        {
            processInternal(message);
        }
    }

    private void processInternal(String message)
    {
        String[] args = message.split("\\s");

        String head = args[0];
        Script script = getScript(head);
        if (script != null) //command to run another script
        {
            m_latestParams = args;
            runScript(script.getContent());
            return;
        }
        else if (head.equals("run"))
        {
            ScriptChoice sc = new ScriptChoice();
            ClassDatabase<?> cdb = m_testData.m_cm.getClassDatabase();
            ClassModel cm = cdb.getClassModel(ScriptChoice.class);
            boolean ok = EditorManager.getInstance().edit(null, cm, sc);
            if (ok)
            {
                runScript(sc.m_script.getContent());
            }
            return;
        }
        else if (head.equals("reload"))
        {
            System.out.println("reloading test data!");
            m_testData = TestData.load();
            return;
        }
        else if (head.equals("start_clock"))
        {
            System.out.println("starting clock!");
            m_startTime = System.currentTimeMillis();
            return;
        }
        else if (head.equals("stop_clock"))
        {
            m_stopTime = System.currentTimeMillis();
            System.out.println("stop clock! time = " + (m_stopTime - m_startTime));
            return;
        }
        System.out.println(message);
        processor.exec(message);
    }

    private Script getScript(String cmdName)
    {
        return m_testData!=null ? m_testData.getScript(cmdName) : null;
    }

    protected String expandVariables(String message)
    {
        Matcher matcher = ARG_PATTERN.matcher(message);
        String result = "";
        int cursor = 0;
        while (matcher.find())
        {
            result += message.substring(cursor, matcher.start());
            String arg = matcher.group().substring(2, matcher.group().length() - 1);
            result += m_argResolver.resolveArg(arg);
            cursor = matcher.end();
        }
        result += message.substring(cursor, message.length());
        message = result;
        return message;
    }

    private int applyModifications(String arg, int i)
    {
        Matcher matcher = MOD_PATTERN.matcher(arg);
        while (matcher.find())
        {
            int mod = Integer.parseInt(matcher.group().substring(1));
            i %= mod;
        }
        matcher = OFFSET_PATTERN.matcher(arg);
        while (matcher.find())
        {
            int offset = Integer.parseInt(matcher.group().substring(1));
            i += offset;
        }
        return i;
    }

    private String getKey(String arg)
    {
        String[] chunks = arg.split("[\\[\\]]");
        String seqKey = "";
        if (chunks.length > 1)
        {
            seqKey = chunks[1];
        }
        return seqKey;
    }

    private int incrementSeq(String seqKey)
    {
        Integer seq = m_seqMap.get(seqKey);
        if (seq == null)
        {
            seq = 0;
        }
        m_seqMap.put(seqKey, seq + 1);
        return seq;
    }

    public void setProcessor(Processor processor)
    {
        this.processor = processor;
    }

    private interface ArgResolver
    {
        String resolveArg(String arg);
    }

    private class InsideLoopResolver implements ArgResolver
    {
        public String resolveArg(String arg)
        {
            if (arg.startsWith("index"))
            {
                return String.valueOf(applyModifications(arg, m_iterationIndex));
            }
            else
            {
                throw new RuntimeException("Unresolved arg: " + arg);
            }
        }
    }

    private class OutsideLoopResolver implements ArgResolver
    {
        public String resolveArg(String arg)
        {
            if (arg.startsWith("next.int"))
            {
                String seqKey = getKey(arg);
                int i = incrementSeq(seqKey);
                i = applyModifications(arg, i);
                return String.valueOf(i);
            }
            else if (arg.startsWith("param"))
            {
                int i = Integer.parseInt(getKey(arg));
                return i < m_latestParams.length ? m_latestParams[i] : null;
            }
            else if (arg.startsWith("index"))//ignore
            {
                return "${" + arg + "}";
            }
            else
            {
                throw new RuntimeException("Unresolved arg: " + arg);
            }
        }
    }
}