package org.flanVim;

import org.flanVim.command.editorspace.*;
import org.flanVim.command.workspace.*;
import org.flanVim.editor.Editor;
import org.flanVim.workspace.WorkSpace;
import org.flanVim.util.ArgumentParser;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Scanner;
import java.util.Arrays;

@Command(name = "FlanVimCLI", version = "FlanVimCLI 1.0", mixinStandardHelpOptions = true,
         subcommands = {
             FlanVimCLI.AppendCmd.class,
             FlanVimCLI.DeleteCmd.class,
             FlanVimCLI.InsertCmd.class,
             FlanVimCLI.ReplaceCmd.class,
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
        @Parameters(index = "0", description = "Text to append (use quotes for text with spaces)")
        private String text;

        @Override
        public void run() {
            if (!workSpace.hasActiveEditor()) {
                System.out.println("Error: No active editor. Use 'init <file>' first.");
                return;
            }

            // text 已由 ArgumentParser 正确解析（支持空格、转义等）
            AppendCommand cmd = new AppendCommand(workSpace.getActiveEditor(), text != null ? text : "");
            workSpace.executeCommand(cmd);  // workspace自动管理历史
        }
    }

    @Command(name = "delete", description = "Delete text in the active file")
    static class DeleteCmd implements Runnable {
        @Parameters(index = "0", description = "the start position <line:col> at which delete begin ", arity = "1")
        private String position;

        @Parameters(index = "1", description = "length of text to be deleted", arity = "1")
        private int length;

        @Override
        public void run() {
            if (!workSpace.hasActiveEditor()) {
                System.out.println("Error: No active editor. Use 'init/load <file>' first.");
                return;
            }

            Editor editor = workSpace.getActiveEditor();

            String[] parts = position.split(":");
            if(parts.length != 2) {
                System.out.println("Invalid range format. Use [int:int].");
                return;
            }
            try{
                int line = Integer.parseInt(parts[0]);
                int colume = Integer.parseInt(parts[1]);
                DeleteCommand cmd = new DeleteCommand(editor, line, colume, length);
                workSpace.executeCommand(cmd);
            } catch (NumberFormatException e) {
                System.out.println("Invalid range format. Use [int:int].");
                return;
            }
        }
    }

    @Command(name = "insert", description = "Insert text into the active file")
    static class InsertCmd implements Runnable {
        @Parameters(index = "0", description = "the start position <line:col> at which insert begin ", arity = "1")
        private String position;

        @Parameters(index = "1", description = "Text to insert (use quotes for text with spaces)")
        private String text;

        @Override
        public void run() {
            if (!workSpace.hasActiveEditor()) {
                System.out.println("Error: No active editor. Use 'init/load <file>' first.");
                return;
            }

            Editor editor = workSpace.getActiveEditor();

            String[] parts = position.split(":");
            if(parts.length != 2) {
                System.out.println("Invalid range format. Use [int:int].");
                return;
            }
            try{
                int line = Integer.parseInt(parts[0]);
                int colume = Integer.parseInt(parts[1]);
                // text 已由 ArgumentParser 正确解析（支持空格、转义等）
                InsertCommand cmd = new InsertCommand(editor, line, colume, text != null ? text : "");
                workSpace.executeCommand(cmd);
            } catch (NumberFormatException e) {
                System.out.println("Invalid range format. Use [int:int].");
                return;
            }
        }
    }

    @Command(name = "replace", description = "Replace text in the active file")
    static class ReplaceCmd implements Runnable {
        @Parameters(index = "0", description = "the start position <line:col> at which replace begin ", arity = "1")
        private String position;

        @Parameters(index = "1", description = "length of text to be replaced", arity = "1")
        private int length;

        @Parameters(index = "2", description = "Text to replace with (use quotes for text with spaces)")
        private String text;

        @Override
        public void run() {
            if (!workSpace.hasActiveEditor()) {
                System.out.println("Error: No active editor. Use 'init <file>' first.");
                return;
            }

            Editor editor = workSpace.getActiveEditor();

            String[] parts = position.split(":");
            if(parts.length != 2) {
                System.out.println("Invalid range format. Use [int:int].");
                return;
            }
            try{
                int line = Integer.parseInt(parts[0]);
                int colume = Integer.parseInt(parts[1]);
                // 使用新的构造函数，text 已由 ArgumentParser 正确解析（支持空格）
                ReplaceCommand cmd = new ReplaceCommand(editor, line, colume, length, text != null ? text : "");
                workSpace.executeCommand(cmd);
            } catch (NumberFormatException e) {
                System.out.println("Invalid range format. Use [int:int].");
                return;
            }
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
            Editor editor = new Editor(fileName, true);  // 创建空 Editor
            if (withLog) {
                editor.setWithLog(true);
            }
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
        @Parameters(index = "0", description = "[startLine:endLine]", arity = "0..1")
        private String range;

        @Override
        public void run() {
            if (!workSpace.hasActiveEditor()) {
                System.out.println("Error: No active editor. Use 'init/load <file>' first.");
                return;
            }
            Editor editor = workSpace.getActiveEditor();
            ShowCommand cmd;
            if (range == null) {
                cmd = new ShowCommand(editor);
            } else {
                // 解析范围
                String[] parts = range.split(":");
                if(parts.length != 2) {
                    System.out.println("Invalid range format. Use [int:int].");
                    return;
                }
                try{
                    int startLine = Integer.parseInt(parts[0]);
                    int endLine = Integer.parseInt(parts[1]);
                    cmd = new ShowCommand(editor, startLine, endLine);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid range format. Use [int:int].");
                    return;
                }
            }
            workSpace.executeCommand(cmd);
        }
    }

    @Command(name = "undo", description = "Undo the last command")
    static class UndoCmd implements Runnable {
        @Option(names = {"--workspace", "-w"}, description = "Force undo workspace-level command instead of editor command")
        private boolean workspace = false;

        @Override
        public void run() {
            if (workspace) {
                // 强制撤销 WorkSpace 层的命令
                workSpace.undoWorkspace();
            } else {
                // 智能撤销: 优先当前 Editor,否则 WorkSpace
                workSpace.undo();
            }
        }
    }

    @Command(name = "redo", description = "Redo the last undone command")
    static class RedoCmd implements Runnable {
        @Option(names = {"--workspace", "-w"}, description = "Force redo workspace-level command instead of editor command")
        private boolean workspace = false;

        @Override
        public void run() {
            if (workspace) {
                // 强制重做 WorkSpace 层的命令
                workSpace.redoWorkspace();
            } else {
                // 智能重做: 优先当前 Editor,否则 WorkSpace
                workSpace.redo();
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
            
            try {
                // 使用智能参数解析器，支持引号和转义字符
                String[] parsedArgs = ArgumentParser.parse(input);
                commandLine.execute(parsedArgs);
            } catch (IllegalArgumentException e) {
                // 参数解析错误（如未闭合的引号）
                System.out.println("Parse Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                scanner.close();
                break;
            }
        }
    }
}
