package org.flanVim.command.workspace;

import org.flanVim.command.Command;
import org.flanVim.command.Undoable;
import org.flanVim.editor.Editor;
import org.flanVim.workspace.WorkSpace;

import java.io.File;
import java.io.IOException;

public class LoadCommand implements Command, Undoable {
    private WorkSpace workSpace;
    private String filePath;
    private String fullPath; // 存储转换后的完整路径
    private String previousActiveFile;
    private boolean wasNewEditor;
    private boolean wasNewFile; // 标记文件是否是新创建的

    public LoadCommand(WorkSpace workSpace, String filePath) {
        this.workSpace = workSpace;
        this.filePath = filePath;
        this.wasNewEditor = false;
        this.wasNewFile = false;
    }

    @Override
    public boolean execute() {
        // 保存当前活动文件名用于撤销
        previousActiveFile = workSpace.getActiveFileName();
        
        // 构建完整的文件路径
        fullPath = filePath;
        if (workSpace.getWorkSpacePath() != null) {
            // 如果工作区已初始化，将相对路径转换为基于工作区的绝对路径
            File file = new File(filePath);
            if (!file.isAbsolute()) {
                // 如果是相对路径，拼接工作区路径
                fullPath = new File(workSpace.getWorkSpacePath(), filePath).getAbsolutePath();
            }
        }
        
        File file = new File(fullPath);
        
        // 检查文件是否已在工作区打开
        Editor existingEditor = workSpace.getEditor(fullPath);
        if (existingEditor != null) {
            // 文件已打开，直接切换为活动文件
            workSpace.setActiveEditor(fullPath);
            System.out.println("Switched to already opened file: " + fullPath);
            return true;
        }
        
        // 创建 Editor（如果文件存在会自动加载内容，否则创建空 Editor）
        Editor editor;
        try {
            // 如果文件不存在，先创建文件
            if (!file.exists()) {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                file.createNewFile();
                wasNewFile = true;
                System.out.println("File does not exist. Created new file: " + fullPath);
            }
            
            // 创建 Editor（会自动加载文件内容）
            editor = new Editor(fullPath);
            
            if (editor.isWithLog()) {
                System.out.println("Log mode enabled for this file.");
            }
            if (!wasNewFile) {
                System.out.println("Loaded file: " + fullPath);
            }
        } catch (IOException e) {
            System.out.println("Error loading file: " + e.getMessage());
            return false;
        }
        
        // 添加到工作区并设为活动文件
        workSpace.addEditor(fullPath, editor);
        workSpace.setActiveEditor(fullPath);
        wasNewEditor = true;
        
        return true;
    }

    @Override
    public void undo() {
        if (wasNewEditor) {
            // 如果是新创建的编辑器，移除它
            workSpace.removeEditor(fullPath);
            System.out.println("Undo: Removed editor for " + fullPath);
            
            // 如果文件是新创建的，删除物理文件
            if (wasNewFile) {
                File file = new File(fullPath);
                if (file.exists() && file.delete()) {
                    System.out.println("Undo: Deleted file " + fullPath);
                }
            }
        }
        
        // 恢复之前的活动文件
        if (previousActiveFile != null) {
            workSpace.setActiveEditor(previousActiveFile);
            System.out.println("Restored active file to: " + previousActiveFile);
        }
    }

    @Override
    public void redo() {
        execute();
    }
}