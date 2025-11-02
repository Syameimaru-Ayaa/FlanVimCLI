package org.flanVim.command.workspace;

import org.flanVim.command.Command;
import org.flanVim.workspace.WorkSpace;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.flanVim.editor.Editor;

public class ExitCommand implements Command {

    private WorkSpace workSpace;
    private Scanner scanner;

    public ExitCommand(WorkSpace workSpace, Scanner scanner) {
        this.workSpace = workSpace;
        this.scanner = scanner;
    }


    /**
     * Must Close all editors one by one before exiting
     * @return
     */
    @Override
    public boolean execute() {
        System.out.println("Exiting FlanVimCLI...");
        
        // 获取所有编辑器的快照（避免 ConcurrentModificationException）
        Map<String, Editor> editors = workSpace.getAllEditors();
        
        // 如果没有打开的文件，直接退出
        if (editors.isEmpty()) {
            scanner.close();
            System.exit(0);
            return true;
        }
        
        // 创建编辑器列表副本（避免在遍历时修改 Map）
        List<Editor> editorList = new ArrayList<>(editors.values());
        
        // 只创建一个 CloseCommand 实例用于复用逻辑
        CloseCommand closeHelper = new CloseCommand(workSpace, scanner);
        
        // 逐个关闭所有编辑器（只处理保存提示，不切换活动编辑器）
        for (Editor editor : editorList) {
            // 使用 CloseCommand 的核心逻辑处理保存提示和关闭
            if (!closeHelper.handleSavePromptAndClose(editor)) {
                System.out.println("Failed to close editor: " + workSpace.getRelativePath(editor.getFilePath()));
                System.out.println("Exit Interrupted");
                return false;
            }
        }
        
        // 所有文件都已关闭，安全退出
        scanner.close();
        System.exit(0);
        return true;
    }
}