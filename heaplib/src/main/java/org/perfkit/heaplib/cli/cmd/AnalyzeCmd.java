package org.perfkit.heaplib.cli.cmd;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.googlecode.lanterna.graphics.PropertyTheme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import org.gridkit.jvmtool.cli.CommandLauncher;
import org.gridkit.jvmtool.cli.CommandLauncher.CmdRef;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.perfkit.heaplib.cli.ui.TreeViewBox;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class AnalyzeCmd implements CmdRef {
    @Override
    public String getCommandName() {
        return "analyze";
    }

    @Override
    public Runnable newCommand(CommandLauncher host) {
        return new AnalyzeRunner(host);
    }

    @Parameters(commandDescription = "HProf analyze")
    public static class AnalyzeRunner implements Runnable {

        @ParametersDelegate
        private final CommandLauncher host;

        @ParametersDelegate
        private HeapProvider heapProvider = new HeapProvider();

        public AnalyzeRunner(CommandLauncher host) {
            this.host = host;
        }

        @Override
        public void run() {
            try {
                Heap heap = heapProvider.openHeap(host);
                List<Instance> dominators = new ArrayList<>(getDominatorRoots(heap));
                tui(dominators);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                throw host.fail("Heap dump processing error", e);
            }

        }

        private Set<Instance> getDominatorRoots(Heap heap) {
            List<Instance> searchInstances = heap.getBiggestObjectsByRetainedSize(1000);
            Set<Instance> dominators = new HashSet(searchInstances);
            Set<Instance> removed = new HashSet();

            for (Instance instance : searchInstances) {
                if (dominators.contains(instance)) {
                    Instance dom = instance;
                    long retainedSize = instance.getRetainedSize();

                    while (!instance.isGCRoot()) {
                        instance = instance.getNearestGCRootPointer();
                        if (dominators.contains(instance) && instance.getRetainedSize() >= retainedSize) {
                            dominators.remove(dom);
                            removed.add(dom);
                            dom = instance;
                            retainedSize = instance.getRetainedSize();
                        }
                        if (removed.contains(instance)) {
                            dominators.remove(dom);
                            removed.add(dom);
                            break;
                        }
                    }
                }
            }
            return dominators;
        }

        private void tui(List<Instance> roots) {
            DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
            //terminalFactory.setForceTextTerminal(true);
            TerminalScreen screen = null;

            try {
                screen = terminalFactory.createScreen();
                screen.startScreen();

                final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);
                textGUI.setTheme(new PropertyTheme(loadPropTheme("keep-theme.properties")));
                final Window window = new BasicWindow("Dominators");
                window.setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS));
                TreeViewBox treeViewBox = new TreeViewBox(roots);

                window.setComponent(treeViewBox);
                textGUI.addWindowAndWait(window);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (screen != null) {
                    try {
                        screen.stopScreen();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private static Properties loadPropTheme(String resourceFileName) {
            Properties properties = new Properties();

            try {
                ClassLoader classLoader = AbstractTextGUI.class.getClassLoader();
                InputStream resourceAsStream = classLoader.getResourceAsStream(resourceFileName);
                if (resourceAsStream == null) {
                    resourceAsStream = new FileInputStream("src/main/resources/" + resourceFileName);
                }

                properties.load((InputStream)resourceAsStream);
                ((InputStream)resourceAsStream).close();
                return properties;
            } catch (IOException var4) {
                return null;
            }
        }
    }
}
