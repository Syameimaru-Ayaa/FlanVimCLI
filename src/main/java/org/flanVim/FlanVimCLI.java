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
             FlanVimCLI.DirTreeCmd.class,
             FlanVimCLI.EditorListCmd.class,
             FlanVimCLI.SaveCmd.class,
             FlanVimCLI.EditCmd.class,
             FlanVimCLI.CloseCmd.class,
             FlanVimCLI.ExitCmd.class
         })
public class FlanVimCLI implements Runnable {

    // 共享的 WorkSpace
    static WorkSpace workSpace = new WorkSpace();
    static Scanner scanner = new Scanner(System.in);  // 共享的 Scanner

    @Override
    public void run() {
        System.out.println("Use --help to see available commands.");
    }


    /**
     * 比较特殊，文档没做要求，所以也没做相关命令抽象
     */
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
            workSpace.executeCommand(cmd);
        }
    }

    @Command(name = "editor-list", description = "List all open editors")
    static class EditorListCmd implements Runnable {
        @Override
        public void run() {
            EditorListCommand cmd = new EditorListCommand(workSpace);
            workSpace.executeCommand(cmd);
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
            workSpace.executeCommand(cmd);  // workspace自动管理历史
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
            workSpace.executeCommand(cmd);  // 自动管理历史
        }
    }

    @Command(name = "save", description = "Save the active file")
    static class SaveCmd implements Runnable {
        @Option(names = {"--all", "-a"}, description = "Save all files")
        private boolean saveAll;

        @Parameters(index = "0..*", description = "File name(s)", arity = "0..*")
        private String[] fileNames;

        @Override
        public void run() {
            SaveCommand cmd;
            
            if (saveAll) {
                // 保存所有文件
                cmd = new SaveCommand(workSpace, true);
            } else if (fileNames != null && fileNames.length > 0) {
                // 保存指定的一个或多个文件
                cmd = new SaveCommand(workSpace, Arrays.asList(fileNames));
            } else {
                // 保存当前活动文件
                if (!workSpace.hasActiveEditor()) {
                    System.out.println("Error: No active editor.");
                    return;
                }
                cmd = new SaveCommand(workSpace);
            }
            
            workSpace.executeCommand(cmd); 
        }
    }

    @Command(name = "show", description = "Display file content")
    static class ShowCmd implements Runnable {
        @Override
        public void run() {
            //TODO
            
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
                System.out.println("Redo last command");
            }
        }
    }

    @Command(name = "edit", description = "Switch to another opened file")
    static class EditCmd implements Runnable {
        @Parameters(index = "0", description = "File name to switch to")
        private String fileName;

        @Override
        public void run() {
            EditCommand cmd = new EditCommand(workSpace, fileName);
            workSpace.executeCommand(cmd);
        }
    }

    @Command(name = "close", description = "Close the active or specified file")
    static class CloseCmd implements Runnable {
        @Parameters(index = "0", description = "File name to close (optional)", arity = "0..1")
        private String fileName;

        @Override
        public void run() {
            CloseCommand cmd;
            if (fileName != null) {
                cmd = new CloseCommand(workSpace, fileName, scanner);
            } else {
                cmd = new CloseCommand(workSpace, scanner);
            }
            workSpace.executeCommand(cmd);
        }
    }

    @Command(name = "exit", description = "Exit FlanVimCLI")
    static class ExitCmd implements Runnable {
        @Override
        public void run() {
            ExitCommand cmd = new ExitCommand(workSpace, scanner);
            workSpace.executeCommand(cmd);
        }
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new FlanVimCLI());
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to FlanVimCLI! Type 'exit' to quit.");
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            // if ("exit".equalsIgnoreCase(input)) {
            //     System.out.println("Exiting FlanVimCLI...");
            //     break;
            // }
            try {
                commandLine.execute(input.split("\\s+"));
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                scanner.close();
                break;
            }
        }
    }
}
