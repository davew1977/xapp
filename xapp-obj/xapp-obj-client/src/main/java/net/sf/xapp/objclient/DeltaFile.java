package net.sf.xapp.objclient;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.utils.FileUtils;
import net.sf.xapp.utils.ReflectionUtils;
import net.sf.xapp.utils.ant.AntFacade;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates a set of deltas and handles their persisted state
 */
public class DeltaFile implements MessageHandler {
    private final File file;
    private long baseRevision = -1;
    private final List<Delta> deltas;
    private OutputStreamWriter writer;


    public DeltaFile(File offlineFile) {
        this.file = offlineFile;
        deltas = new ArrayList<>();
        if (offlineFile.exists()) {
            String offlineData = FileUtils.readFile(offlineFile, Charset.forName("UTF-8"));
            if (!offlineData.isEmpty()) {
                String[] lines = offlineData.split("\n");
                baseRevision = Long.parseLong(lines[0]);
                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i];
                    deltas.add(new Delta().deserialize(line));
                }
            }
        }
    }

    public void reset(long baseRevision) {
        delete();
        this.baseRevision = baseRevision;
        write(baseRevision + "");
    }

    @Override
    public Object handleMessage(InMessage inMessage) {
        assert baseRevision != -1;
        Delta delta = new Delta(inMessage, System.currentTimeMillis());
        write(delta.serialize());
        deltas.add(delta);
        return null;
    }

    public long getBaseRevision() {
        return baseRevision;
    }

    public List<Delta> getDeltas() {
        return deltas;
    }

    public boolean isEmpty() {
        return deltas.isEmpty();
    }

    private OutputStreamWriter getWriter() {
        if (writer == null) {
            try {
                writer = new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return writer;
    }

    private void write(String line) {
        try {
            getWriter().write(line + "\n");
            getWriter().flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeWriter() {
        try {
            getWriter().flush();
            getWriter().close();
            writer = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        closeWriter();
    }

    public long getLastRevision() {
        if(isEmpty()) {
            return baseRevision;
        } else {
            InMessage message = deltas.get(deltas.size() - 1).getMessage();
            return (Long) ReflectionUtils.call(message, "getRev");
        }
    }

    public void delete() {
        closeWriter();
        new AntFacade().deleteFile(file);
        deltas.clear();
        baseRevision = -1;
    }

    public File getFile() {
        return file;
    }

    public int size() {
        return deltas.size();
    }
}
