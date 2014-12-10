package net.sf.xapp.objclient;

import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.utils.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by oldDave on 04/12/2014.
 */
public class OfflineMeta {
    private long baseRevision;
    private List<Delta> deltas;


    public OfflineMeta(File offlineFile) {
        deltas = new ArrayList<Delta>();
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

    public long getBaseRevision() {
        return baseRevision;
    }

    public List<Delta> getDeltas() {
        return deltas;
    }

    public boolean isEmpty() {
        return deltas.isEmpty();
    }
}
