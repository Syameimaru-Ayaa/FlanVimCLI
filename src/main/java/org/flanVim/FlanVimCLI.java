package org.flanVim;

import org.flanVim.command.editorspace.*;
import org.flanVim.command.workspace.*;
import org.flanVim.command.CommandHistory;
import org.flanVim.editor.Editor;
import org.flanVim.workspace.WorkSpace;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Scanner;
import java.util.Arrays;
import java.util.List;

@Command(name = "FlanVimCLI", version = "FlanVimCLI 1.0", mixinStandardHelpOptions = true,
         subcommands = {
             FlanVimCLI.AppendCmd.class,
             FlanVimCLI.InitCmd.class,
             FlanVimCLI.ShowCmd.class,
             FlanVimCLI.UndoCmd.class,
             FlanVimCLI.RedoCmd.class,
             FlanVimCLI.LoadCmd.class,
             FlanVimCLI.InitSpaceCmd.class,
             FlanVimCLI.DirTreeCmd.class
         })
public class FlanVimCLI implements Runnable {

    // 共享的 WorkSpace
    static WorkSpace workSpace = new WorkSpace();

    @Override
    public void run() {
        System.out.println("Use --help to see available commands.");
    }

    @Command(name = "inispace", description = "Initialize a new workspace")
    static class InitSpaceCmd implements Runnable {
        @Parameters(index = "0", description = "Workspace path")
        private String workSpacePath;

        @Override
        public void run() {
            if (workSpace.getWorkSpacePath() == null) {
                java.io.File dir = new java.io.File(workSpacePath);
                if (!dir.exists()) {
                    System.out.println("Error: Path does not exist: " + workSpacePath);
                    return;
                }
                if (!dir.isDirectory()) {
                    System.out.println("Error: Path is not a directory: " + workSpacePath);
                    return;
                }
                workSpace.setWorkSpacePath(workSpacePath);
                System.out.println("Initialized a new workspace: " + workSpacePath);
            } else {
                System.out.println("Workspace is already initialized.");
            }
        }
    }

    @Command(name = "dir-tree", description = "Display directory tree")
    static class DirTreeCmd implements Runnable {
        @Parameters(index = "0", description = "Directory path", arity = "0..1")
        private String directoryPath;

        @Override
        public void run() {
            if (directoryPath == null) {
                if (workSpace.getWorkSpacePath() == null) {
                    System.out.println("Error: Workspace is not initialized.");
                    return;
                }
                directoryPath = workSpace.getWorkSpacePath();
            }
            // Implement directory tree display logic here
            System.out.println("Displaying directory tree for workspace: " + workSpace.getWorkSpacePath());
            DirTreeCommand cmd = new DirTreeCommand(directoryPath);
            cmd.execute();
        }
    }

    // append "text" 命令
    @Command(name = "append", description = "Append text to the active file")
    static class AppendCmd implements Runnable {
        @Parameters(index = "0..*", description = "Text to append")
        private String[] text;

        @Override
        public void run() {
            if (!workSpace.hasActiveEditor()) {
                System.out.println("Error: No active editor. Use 'init <file>' first.");
                return;
            }

            List<String> args = text != null ? Arrays.asList(text) : List.of();
            AppendCommand cmd = new AppendCommand(workSpace.getActiveEditor(), args);
            cmd.execute();
        }
    }

    // init <file> [with-log]命令
    @Command(name = "init", description = "Create a new buffer")
    static class InitCmd implements Runnable {
        @Parameters(index = "0", description = "File name", arity = "1")
        private String fileName;

        @Option(names = {"--with-log", "-l"}, description = "Enable logging")
        private boolean withLog;

        @Override
        public void run() {
            Editor editor = new Editor(fileName);
            workSpace.addEditor(fileName, editor);
            System.out.println("Created new buffer: " + fileName);
        }
    }

    @Command(name = "load", description = "Load a file into the editor")
    static class LoadCmd implements Runnable {
        @Parameters(index = "0", description = "File Path to load")
        private String filePath;

        @Override
        public void run() {
            System.out.println("Loading file: " + filePath);
            LoadCommand cmd = new LoadCommand(workSpace, filePath);
            cmd.execute();
            workSpace.getCommandHistory().addCommand(cmd);
        }
    }

    @Command(name = "show", description = "Display file content")
    static class ShowCmd implements Runnable {
        @Override
        public void run() {
            if (!workSpace.hasActiveEditor()) {
                System.out.println("Error: No active editor.");
                return;
            }
            System.out.println("Content of " + workSpace.getActiveFileName() + ":");
            System.out.println(workSpace.getActiveEditor().getContent());
        }
    }

    @Command(name = "undo", description = "Undo the last command")
    static class UndoCmd implements Runnable {
        @Override
        public void run() {
            WorkSpace workSpace = FlanVimCLI.workSpace;
            CommandHistory history = workSpace.getCommandHistory();
            if (history != null) {
                history.undo();
            }
        }
    }

    @Command(name = "redo", description = "Redo the last undone command")
    static class RedoCmd implements Runnable {
        @Override
        public void run() {
            WorkSpace workSpace = FlanVimCLI.workSpace;
            CommandHistory history = workSpace.getCommandHistory();
            if (history != null) {
                history.redo();
            }
        }
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new FlanVimCLI());
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to FlanVimCLI! Type 'exit' to quit.");
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if ("exit".equalsIgnoreCase(input)) {
                System.out.println("Exiting FlanVimCLI...");
                break;
            }
            try {
                commandLine.execute(input.split("\\s+"));
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }
}
