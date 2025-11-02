package org.flanVim.workspace;

import org.flanVim.command.CommandHistory;
import org.flanVim.editor.Editor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WorkSpace: 管理所有打开的编辑器和当前活动的编辑器
 */
public class WorkSpace {
    private Map<String, Editor> editors = new HashMap<>();
    private String workSpacePath = null;
    private Editor activeEditor = null;
    private String activeFileName = null;
    private CommandHistory commandHistory = new CommandHistory();

    // public WorkSpace(String workSpacePath) {
    //     this.workSpacePath = workSpacePath;
    // }
    public WorkSpace() {}

    public Editor getActiveEditor() {
        return activeEditor;
    }

    public void setActiveEditor(String fileName) {
        this.activeEditor = editors.get(fileName);
        this.activeFileName = fileName;
        
        // 更新访问时间
        if (this.activeEditor != null) {
            this.activeEditor.updateAccessTime();
        }
    }

    public void addEditor(String fileName, Editor editor) {
        editors.put(fileName, editor);
        if (activeEditor == null) {
            setActiveEditor(fileName);
        }
    }

    public Editor getEditor(String fileName) {
        return editors.get(fileName);
    }

    public boolean hasEditor(String fileName) {
        return editors.containsKey(fileName);
    }

    public void removeEditor(String fileName) {
        editors.remove(fileName);
        // 如果删除的是活动编辑器，需要清空或切换到另一个
        if (fileName.equals(activeFileName)) {
            activeEditor = null;
            activeFileName = null;
        }
    }

    public boolean hasActiveEditor() {
        return activeEditor != null;
    }

    public String getActiveFileName() {
        return activeFileName;
    }

    public CommandHistory getCommandHistory() {
        return commandHistory;
    }

    public String getWorkSpacePath() {
        return workSpacePath;
    }

    public void setWorkSpacePath(String workSpacePath) {
        this.workSpacePath = workSpacePath;
    }

    public Map<String, Editor> getAllEditors() {
        return editors;
    }

    /**
     * 将绝对路径转换为相对于 workSpace 的相对路径
     * @param absolutePath 绝对路径
     * @return 相对路径，如果无法转换则返回原路径
     */
    public String getRelativePath(String absolutePath) {
        if (workSpacePath == null || absolutePath == null) {
            return absolutePath;
        }
        
        try {
            Path workSpacePathObj = Paths.get(workSpacePath).toAbsolutePath().normalize();
            Path absolutePathObj = Paths.get(absolutePath).toAbsolutePath().normalize();
            Path relativePath = workSpacePathObj.relativize(absolutePathObj);
            return relativePath.toString();
        } catch (IllegalArgumentException e) {
            // 如果路径不在同一个根目录下，无法计算相对路径
            return absolutePath;
        }
    }

    /**
     * 将相对路径转换为绝对路径
     * @param relativePath 相对路径（相对于 workSpace）
     * @return 绝对路径
     */
    public String getAbsolutePath(String relativePath) {
        if (workSpacePath == null || relativePath == null) {
            return relativePath;
        }
        
        Path path = Paths.get(relativePath);
        // 如果已经是绝对路径，直接返回
        if (path.isAbsolute()) {
            return relativePath;
        }
        
        // 否则，基于 workSpacePath 解析相对路径
        return Paths.get(workSpacePath, relativePath).toAbsolutePath().normalize().toString();
    }

    /**
     * 获取最近使用的文件（排除指定文件）
     * @param excludeFileName 要排除的文件名（通常是当前要关闭的文件）
     * @return 最近使用的文件名，如果没有其他文件则返回 null
     */
    public String getMostRecentlyUsedFile(String excludeFileName) {
        String mostRecentFile = null;
        LocalDateTime mostRecentTime = null;
        
        for (Map.Entry<String, Editor> entry : editors.entrySet()) {
            String fileName = entry.getKey();
            Editor editor = entry.getValue();
            
            // 跳过要排除的文件
            if (fileName.equals(excludeFileName)) {
                continue;
            }
            
            // 找到访问时间最晚的文件
            LocalDateTime accessTime = editor.getLastAccessTime();
            if (mostRecentTime == null || accessTime.isAfter(mostRecentTime)) {
                mostRecentTime = accessTime;
                mostRecentFile = fileName;
            }
        }
        
        return mostRecentFile;
    }

    /**
     * 执行命令并自动管理命令历史
     * 只有成功执行的可撤销命令才会被添加到历史记录中
     * 这样可以避免失败的命令占用内存
     * 
     * @param command 要执行的命令
     * @return 命令执行结果（true 表示成功，false 表示失败）
     */
    public boolean executeCommand(org.flanVim.command.Command command) {
        boolean success = false;
        try {
            success = command.execute();
        } catch (Exception e) {
            System.out.println("Error executing command: " + e.getMessage());
            success = false;  // 确保异常时返回 false
        }
        
        // 只有命令执行成功且支持撤销时，才添加到历史记录
        // 这样可以避免失败的命令占用内存
        if (success && command instanceof org.flanVim.command.Undoable) {
            commandHistory.addCommand(command);
        }
        
        return success;
    }
}
