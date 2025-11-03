package org.flanVim.command.workspace;

import org.flanVim.command.Command;
import org.flanVim.editor.Editor;
import org.flanVim.workspace.WorkSpace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 我不觉得 
 * @SaveCommand 我不觉得save是需要Undoable的
 */
public class SaveCommand implements Command {
    private WorkSpace workSpace;
    private List<String> targetFiles; // 要保存的文件列表
    private boolean saveAll;

    /**
     * 保存当前活动文件
     */
    public SaveCommand(WorkSpace workSpace) {
        this.workSpace = workSpace;
        this.targetFiles = new ArrayList<>();
        this.saveAll = false;
    }

    /**
     * 保存指定的多个文件
     * @param fileNames 文件名列表
     */
    public SaveCommand(WorkSpace workSpace, List<String> fileNames) {
        this.workSpace = workSpace;
        this.targetFiles = fileNames != null ? new ArrayList<>(fileNames) : new ArrayList<>();
        this.saveAll = false;
    }

    /**
     * 保存指定文件或所有文件
     * @param fileName 文件名或 "all"，其实"all"的功能不想加了，但为了保证文档要求的功能可以实现
     */
    public SaveCommand(WorkSpace workSpace, String fileName) {
        this.workSpace = workSpace;
        this.saveAll = "all".equalsIgnoreCase(fileName);
        this.targetFiles = new ArrayList<>();
        if (!saveAll) {
            this.targetFiles.add(fileName);
        }
    }

    /**
     * 保存所有文件
     */
    public SaveCommand(WorkSpace workSpace, boolean saveAll) {
        this.workSpace = workSpace;
        this.saveAll = saveAll;
        this.targetFiles = new ArrayList<>();
    }

    @Override
    public boolean execute() {
        if (saveAll) {
            // 保存所有文件
            return saveAllFiles();
        } else if (!targetFiles.isEmpty()) {
            // 保存指定的文件列表
            return saveSpecificFiles(targetFiles);
        } else {
            // 保存当前活动文件
            return saveActiveFile();
        }
    }

    /**
     * 保存当前活动文件
     */
    private boolean saveActiveFile() {
        if (!workSpace.hasActiveEditor()) {
            System.out.println("Error: No active editor to save.");
            return false;
        }

        String fileName = workSpace.getActiveFileName();
        Editor editor = workSpace.getActiveEditor();
        
        return saveEditorToFile(fileName, editor);
    }

    /**
     * 保存指定的多个文件
     */
    private boolean saveSpecificFiles(List<String> fileNames) {
        boolean allSuccess = true;
        int savedCount = 0;

        for (String fileName : fileNames) {
            // 将相对路径转换为绝对路径（与 LoadCommand 对齐）
            String fullPath = resolveFilePath(fileName);
            
            Editor editor = workSpace.getEditor(fullPath);
            
            if (editor == null) {
                System.out.println("Error: File not found in workspace: " + fileName);
                allSuccess = false;
                continue;
            }

            if (saveEditorToFile(fullPath, editor)) {
                savedCount++;
            } else {
                allSuccess = false;
            }
        }

        if (fileNames.size() > 1) {
            System.out.println("Saved " + savedCount + " of " + fileNames.size() + " file(s).");
        }
        
        return allSuccess;
    }

    /**
     * 将相对路径转换为绝对路径（与 LoadCommand 保持一致）
     */
    private String resolveFilePath(String filePath) {
        if (workSpace.getWorkSpacePath() != null) {
            File file = new File(filePath);
            if (!file.isAbsolute()) {
                // 如果是相对路径，拼接工作区路径
                return new File(workSpace.getWorkSpacePath(), filePath).getAbsolutePath();
            }
        }
        return new File(filePath).getAbsolutePath();
    }

    /**
     * 保存所有文件
     */
    private boolean saveAllFiles() {
        Map<String, Editor> allEditors = workSpace.getAllEditors();
        
        if (allEditors.isEmpty()) {
            System.out.println("No files to save.");
            return true;
        }

        boolean allSuccess = true;
        int savedCount = 0;

        for (Map.Entry<String, Editor> entry : allEditors.entrySet()) {
            String fileName = entry.getKey();
            Editor editor = entry.getValue();

            if (editor.isModified()) {
                if (saveEditorToFile(fileName, editor)) {
                    savedCount++;
                } else {
                    allSuccess = false;
                }
            }
        }

        System.out.println("Saved " + savedCount + " file(s).");
        return allSuccess;
    }

    /**
     * 将编辑器内容保存到文件
     */
    private boolean saveEditorToFile(String fileName, Editor editor) {
        try {
            editor.save();
            System.out.println("Saved: " + fileName);
            return true;
        } catch (IOException e) {
            System.out.println("Error saving file: " + fileName);
            System.out.println("Reason: " + e.getMessage());
            return false;
        }
    }
}

