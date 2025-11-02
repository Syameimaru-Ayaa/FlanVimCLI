package org.flanVim.command.workspace;

import org.flanVim.command.Command;
import org.flanVim.command.Undoable;
import org.flanVim.editor.Editor;
import org.flanVim.workspace.WorkSpace;

import java.io.IOException;
import java.util.Scanner;

/**
 * close - 关闭⽂件 
 * close [file]
 * 功能：关闭当前活动⽂件或指定⽂件。
 * ⾏为:
 * ⽂件已修改且未保存：提示"⽂件已修改，是否保存? (y/n)"
 * ⽤户输⼊ y：保存⽂件后关闭
 * ⽤户输⼊ n：直接关闭不保存
 * 关闭后，如果还有其他打开的⽂件，切换到最近使⽤的⽂件
 */
public class CloseCommand implements Command, Undoable {
    private WorkSpace workSpace;
    private String fileName;
    private String previousActiveFileName;
    private Editor closedEditor;
    private String closedFileName;
    private boolean wasActiveFile;
    private Scanner scanner;

    /**
     * 关闭当前活动文件
     */
    public CloseCommand(WorkSpace workSpace, Scanner scanner) {
        this.workSpace = workSpace;
        this.fileName = null;  // null 表示关闭当前活动文件
        this.scanner = scanner;
    }

    /**
     * 关闭指定文件
     */
    public CloseCommand(WorkSpace workSpace, String fileName, Scanner scanner) {
        this.workSpace = workSpace;
        this.fileName = fileName;
        this.scanner = scanner;
    }

    @Override
    public boolean execute() {
        // 确定要关闭的文件
        String fileToClose;
        if (fileName == null) {
            // 关闭当前活动文件
            if (!workSpace.hasActiveEditor()) {
                System.out.println("Error: No active editor to close.");
                return false;
            }
            fileToClose = workSpace.getActiveFileName();
        } else {
            fileToClose = fileName;
        }

        // 检查文件是否已打开
        Editor editor = workSpace.getEditor(fileToClose);
        if (editor == null) {
            System.out.println("Error: File not opened: " + fileToClose);
            return false;
        }

        // 保存状态用于撤销
        previousActiveFileName = workSpace.getActiveFileName();
        closedEditor = editor;
        closedFileName = fileToClose;
        wasActiveFile = fileToClose.equals(previousActiveFileName);

        // 处理保存提示和关闭文件（不切换活动编辑器）
        if (!handleSavePromptAndClose(editor)) {
            return false;
        }

        // 如果关闭的是当前活动文件，切换到最近使用的文件
        if (wasActiveFile) {
            String mostRecentFile = workSpace.getMostRecentlyUsedFile(fileToClose);
            if (mostRecentFile != null) {
                workSpace.setActiveEditor(mostRecentFile);
                System.out.println("Switched to: " + workSpace.getRelativePath(mostRecentFile));
            } else {
                System.out.println("No more files open.");
            }
        }

        return true;
    }

    /**
     * 处理保存提示和关闭文件的核心逻辑
     * 此方法不包含切换活动编辑器的逻辑，可被 ExitCommand 复用
     * 
     * @param editor 要关闭的编辑器
     * @return 是否成功关闭（用户选择不保存也算成功）
     */
    public boolean handleSavePromptAndClose(Editor editor) {
        String fileName = editor.getFilePath();
        
        // 检查文件是否已修改
        if (editor.isModified()) {
            while(true) {
                System.out.print("File <" + fileName + "> has been modified, save or not? (y/n): ");
                String response = scanner.nextLine().trim().toLowerCase();
                if (response.equals("y") || response.equals("yes")) {
                    // 保存文件
                    try {
                        editor.save();
                        System.out.println("File saved: " + workSpace.getRelativePath(fileName));
                    } catch (IOException e) {
                        System.out.println("Error saving file: " + e.getMessage());
                        System.out.println("File not closed.");
                        return false;
                    }
                    break;
                } else if (!response.equals("n") && !response.equals("no")) {
                    System.out.println("Invalid input. Print (y/n).");
                    continue;
                }
                //为n
                break;
            }
        }

        // 关闭文件
        workSpace.removeEditor(fileName);
        System.out.println("Closed: " + workSpace.getRelativePath(fileName));
        return true;
    }

    @Override
    public void undo() {
        // 恢复关闭的文件
        if (closedEditor != null && closedFileName != null) {
            workSpace.addEditor(closedFileName, closedEditor);
            System.out.println("Undo: Reopened " + workSpace.getRelativePath(closedFileName));

            // 如果关闭的是活动文件，恢复为活动文件
            if (wasActiveFile) {
                workSpace.setActiveEditor(closedFileName);
                System.out.println("Restored as active file: " + workSpace.getRelativePath(closedFileName));
            } else if (previousActiveFileName != null) {
                // 恢复之前的活动文件
                workSpace.setActiveEditor(previousActiveFileName);
            }
        }
    }

    @Override
    public void redo() {
        // 重做时不再提示用户，直接关闭
        if (closedFileName != null) {
            workSpace.removeEditor(closedFileName);
            System.out.println("Redo: Closed " + workSpace.getRelativePath(closedFileName));

            if (wasActiveFile) {
                String mostRecentFile = workSpace.getMostRecentlyUsedFile(closedFileName);
                if (mostRecentFile != null) {
                    workSpace.setActiveEditor(mostRecentFile);
                    System.out.println("Switched to: " + workSpace.getRelativePath(mostRecentFile));
                }
            }
        }
    }
}
