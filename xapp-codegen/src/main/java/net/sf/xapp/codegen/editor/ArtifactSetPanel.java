package net.sf.xapp.codegen.editor;

import net.sf.xapp.codegen.model.Artifact;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: davidw
 * Date: 2/14/14
 * Time: 3:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class ArtifactSetPanel extends JTable{
    private List<Artifact> artifacts;
    public ArtifactSetPanel(final Editor editor) {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()==2) {
                    int row = rowAtPoint(e.getPoint());
                    Artifact artifact = artifacts.get(row);
                    editor.getAppContainer().expand(artifact);
                }
            }
        });
    }

    public void init(final List<Artifact> artifacts) {
        this.artifacts = artifacts ;
        setModel(new ArtifactTableModel());

    }

    private class ArtifactTableModel extends AbstractTableModel {
        private String[] columnNames = new String[]{"type", "module", "package", "name"};

        @Override
        public int getRowCount() {
            return artifacts.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Artifact artifact = artifacts.get(rowIndex);
            switch(columnIndex) {
                case 0:
                    return artifact.getClass().getSimpleName();
                case 1:
                    return artifact.getModule().getName();
                case 2:
                    return artifact.getPackageName();
                case 3:
                    return artifact.getName();

            }
            return null;
        }
    }
}
