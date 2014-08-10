/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.editor;

import net.sf.xapp.application.api.*;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.codegen.Generator;
import net.sf.xapp.objectmodelling.core.PropertyChange;
import net.sf.xapp.codegen.AntFacade;
import net.sf.xapp.codegen.ChangeMeta;
import net.sf.xapp.codegen.model.*;
import net.sf.xapp.utils.CollectionsUtils;
import net.sf.xapp.utils.Filter;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Editor extends SimpleApplication<Model> implements SpecialTreeGraphics<Model> {
    public static final ImageIcon READ_ONLY_FIELD = new ImageIcon(Editor.class.getResource("/read_only_field.png"), "");
    public static final ImageIcon READ_WRITE_FIELD = new ImageIcon(Editor.class.getResource("/read_write_field.png"), "");
    public static final ImageIcon ENTITY = new ImageIcon(Editor.class.getResource("/complex_type.png"), "");
    public static final ImageIcon VALUE_OBJECT = new ImageIcon(Editor.class.getResource("/value_object.png"), "");
    public static final ImageIcon LOBBY_TYPE = new ImageIcon(Editor.class.getResource("/lobby_type.png"), "");
    public static final ImageIcon PRIMITIVE_TYPE = new ImageIcon(Editor.class.getResource("/primitive_type.png"), "");
    public static final ImageIcon ENUM_TYPE = new ImageIcon(Editor.class.getResource("/enum_type.png"), "");
    public static final ImageIcon API = new ImageIcon(Editor.class.getResource("/api.png"), "");
    public static final ImageIcon MESSAGE = new ImageIcon(Editor.class.getResource("/message.png"), "");

    private GeneratorPlugin m_generatorPlugin;
    private CodeTabbedPane m_codeTabbedPane;

    private Set<Node> changedNodes = new HashSet<Node>();
    private Set<Node> addedNodes = new HashSet<Node>();
    private Set<Node> removedNodes = new HashSet<Node>();
    private boolean apiOrMessageRenamed;

    private Generator generator;
    private ArtifactSetPanel artifactSetPanel;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public Editor(GeneratorPlugin generatorPlugin, Generator generator) {
        this.generator = generator;
        SwingUtils.DEFAULT_FONT = Font.decode("Tahoma-11");
        m_generatorPlugin = generatorPlugin;
        m_codeTabbedPane = new CodeTabbedPane();
        artifactSetPanel = new ArtifactSetPanel(this);
    }

    public void init(final ApplicationContainer<Model> applicationContainer) {
        super.init(applicationContainer);

        applicationContainer.addBeforeHook(DefaultAction.SAVE, new ApplicationContainer.Hook() {
            public void execute() {
                List<String> errors = model().validate();
                if (!errors.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("You have error(s):\n");
                    for (String error : errors) {
                        sb.append("\t").append(error).append("\n");
                    }
                    sb.append("Proceed?");
                    boolean proceed = SwingUtils.askUser(applicationContainer.getMainFrame(), sb.toString());
                    if (!proceed) {
                        throw new RuntimeException("save cancelled due to validation errors: " + errors);
                    }
                }

                model().updateMessageIds();
                model().updateObjectIds();
            }
        });

        JButton cleanButton = new JButton("Clean");
        cleanButton.setFont(Font.decode("Tahoma-BOLD-10"));
        cleanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Module> modules = CollectionsUtils.filter(model().getModules(), new Filter<Module>() {
                    @Override
                    public boolean matches(Module module) {
                        return module.getOutDir() != null;
                    }
                });
                StringBuilder sb = new StringBuilder("Clear the following directories?\n");
                for (Module module : modules) {
                    sb.append("\t").append(module.getOutDir()).append("\n");
                }
                if (SwingUtils.askUser(applicationContainer.getMainFrame(), sb.toString())) {
                    AntFacade antFacade = new AntFacade();
                    for (Module module : modules) {
                        antFacade.deleteDir(module.outDir());
                    }
                }
            }
        });
        applicationContainer.getToolBar().add(cleanButton);

        final List<CodeFile> filesAtStart = new ArrayList<CodeFile>();
        applicationContainer.addAfterHook(Application.DefaultAction.SAVE,
                new AfterSaveHook(applicationContainer, filesAtStart));
        //run on worker thread:
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Model originalModel = Model.loadModel(generator.getGeneratorContext());
                originalModel.setAllArtifactsChanged(true);
                filesAtStart.addAll(generator.generate(originalModel));
                System.out.println("will generate " + filesAtStart.size() + " files");
            }
        });


        SearchPane searchPane = new SearchPane(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Artifact> artifacts = (List<Artifact>) e.getSource();
                artifactSetPanel.init(artifacts);
                if (userPanel != artifactSetPanel) {
                    setUserPanel(artifactSetPanel);
                }
            }
        }, model());
        getAppContainer().getToolBar().add(searchPane);
    }

    public SpecialTreeGraphics createSpecialTreeGraphics() {
        return this;
    }

    public List<Command> getCommands(final Node node) {
        List<Command> commands = super.getCommands(node);
        if (node.wrappedObject() instanceof Field) {
            final Field field = (Field) node.wrappedObject();

            commands.add(new AbstractCommand("Toggle Access", "", "control T") {
                public void execute(Object params) {
                    field.setAccess(field.getAccess().next());
                }
            });
            commands.add(new AbstractCommand("Goto Type", "", "control G") {
                @Override
                public void execute(Object params) {
                    appContainer.expand(field.getType());
                }
            });
        } else if (node.isA(Module.class)) {
            final Module module = node.wrappedObject();
            commands.add(new AbstractCommand("Clean Module Code", "", "alt C") {
                @Override
                public void execute(Object params) {
                    if (SwingUtils.askUser(getAppContainer().getMainFrame(), "Delete " + module.getOutDir() + "?")) {
                        new AntFacade().deleteDir(module.outDir());
                    }
                }
            });
            commands.add(new AbstractCommand("Generate All Module Code", "", "alt G") {
                @Override
                public void execute(Object params) {
                    generator.generateAndWrite(model(), Arrays.asList(module), true);
                    model().setAllArtifactsChanged(false);
                }
            });
        } else if (node.wrappedObject() != null) {
            commands.add(new AbstractCommand("Touch", "", "alt T") {
                @Override
                public void execute(Object params) {
                    nodeUpdated(node, new HashMap<String, PropertyChange>());
                }
            });
        }

        return commands;
    }

    public boolean nodeSelected(Node node) {
        List<CodeFile> files = m_generatorPlugin.generate(model(), node);
        if (files != null) {
            m_codeTabbedPane.init(files);
            setUserPanel(m_codeTabbedPane, false);
        }
        return false;
    }

    @Override
    public void nodeUpdated(Node node, Map<String, PropertyChange> changes) {
        if (changes.containsKey("Name") && (node.isA(Message.class) || node.isA(Api.class))) {
            apiOrMessageRenamed = true;
        }
        markChanged(node);
        markHeirarchyChanged(node);
    }

    @Override
    public void nodeAdded(Node node) {
        addedNodes.add(node);
        markChanged(node);
        markHeirarchyChanged(node);

        if(node.isA(Api.class)) {
            Api api = node.wrappedObject();
        }
    }


    @Override
    public void nodeAboutToBeRemoved(Node node, boolean wasCut) {
        removedNodes.add(node);
        markHeirarchyChanged(node);
    }

    private void markHeirarchyChanged(Node node) {
        while ((node = node.getParent()) != null) {
            markChanged(node);
        }
    }

    private void markChanged(Node objectNode) {
        if (objectNode.isA(Artifact.class)) {
            objectNode.<Artifact>wrappedObject().setChangedInSession(true);
        }
        changedNodes.add(objectNode);
    }

    public ImageIcon getNodeImage(Node node) {
        Object o = node.wrappedObject();
        if (o instanceof Field) {
            Field field = (Field) o;
            return field.getAccess() == Access.READ_ONLY ? READ_ONLY_FIELD : READ_WRITE_FIELD;
        } else if (o instanceof Message) {
            return MESSAGE;
        } else if (o instanceof ValueObject) {
            return VALUE_OBJECT;
        } else if (o instanceof LobbyType) {
            return LOBBY_TYPE;
        } else if (o instanceof Entity) {
            return ENTITY;
        } else if (o instanceof PrimitiveType) {
            return PRIMITIVE_TYPE;
        } else if (o instanceof EnumType) {
            return ENUM_TYPE;
        } else if (o instanceof Api) {
            return API;
        }
        return null;
    }

    public void decorateCell(Node node, Graphics gr) {
        if (changedNodes.contains(node)) {
            highlight(gr, Color.red);
        }
        if (addedNodes.contains(node)) {
            highlight(gr, Color.blue);
        }
    }

    private void highlight(Graphics gr, Color color) {
        Graphics2D g = (Graphics2D) gr;
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
        g.setColor(color);
        g.fill(g.getClipBounds());
    }

    public String getTooltip(Node node) {
        return null;
    }

    public void prepareRenderer(Node currentNode, DefaultTreeCellRenderer cellRenderer) {

    }

    public ChangeMeta changedItems() {
        Set<Artifact> added = new HashSet<Artifact>();
        for (Node addedNode : addedNodes) {
            //only include nodes that will cause one or more class files to be generated in their own right
            if (addedNode.isA(Artifact.class)) {
                added.add(addedNode.<Artifact>wrappedObject());
            }
        }
        Set<Artifact> removed = new HashSet<Artifact>();
        for (Node removedNode : removedNodes) {
            //only include nodes that will cause one or more class files to be generated in their own right
            if (removedNode.isA(Artifact.class)) {
                removed.add(removedNode.<Artifact>wrappedObject());
            }
        }
        return new ChangeMeta(added, removed, apiOrMessageRenamed);
    }

    public void clearAllChangeInfo() {
        changedNodes.clear();
        addedNodes.clear();
        removedNodes.clear();
        apiOrMessageRenamed = false;
        model().setAllArtifactsChanged(false);
        appContainer.getMainTree().repaint();
    }

    private class AfterSaveHook implements ApplicationContainer.Hook {
        private final ApplicationContainer applicationContainer;
        private final List<CodeFile> filesAtStart;

        public AfterSaveHook(ApplicationContainer applicationContainer, List<CodeFile> filesAtStart) {
            this.applicationContainer = applicationContainer;
            this.filesAtStart = filesAtStart;
        }

        public void execute() {
            String[] options = {"All", "Changed", "None"};
            int option = JOptionPane.showOptionDialog(applicationContainer.getMainFrame(),
                    "Generate Code?", "generate",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, "Changed");

            Model model = model();
            if (option == 0) {
                model.setAllArtifactsChanged(true);
                generator.generateAndWrite(model, true);
            } else if (option == 1) {
                generator.generateAndWrite(model, false);
            }

            if (option == 1 || option == 0) {
                postGenerate(model);
            }
        }

        private void postGenerate(Model model) {
            //detect removed files
            model.setAllArtifactsChanged(true);
            List<CodeFile> files = generator.generate(model);
            filesAtStart.removeAll(files);
            if (!filesAtStart.isEmpty()) {
                String message = "Remove the following files?\n";
                for (CodeFile codeFile : filesAtStart) {
                    message += "\t" + codeFile.getFullPath() + "\n";
                }
                boolean removeFiles = SwingUtils.askUser(applicationContainer.getMainFrame(), message);
                if (removeFiles) {
                    AntFacade ant = new AntFacade();
                    for (CodeFile codeFile : filesAtStart) {
                        File file = new File(codeFile.getFullPath());
                        ant.deleteFile(file);
                        System.out.println("deleted: " + file);
                    }
                }
            }
            filesAtStart.clear();
            filesAtStart.addAll(files);
            clearAllChangeInfo();
        }
    }
}