package org.flanVim.command.workspace;

import org.flanVim.command.Command;

import java.io.File;

public class DirTreeCommand implements Command {
    private String directoryPath;

    public DirTreeCommand(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    @Override
    public boolean execute() {
        File directory = new File(directoryPath);
        
        if (!directory.exists()) {
            System.out.println("错误: 目录不存在 - " + directoryPath);
            return false;
        }
        
        if (!directory.isDirectory()) {
            System.out.println("错误: 指定的路径不是目录 - " + directoryPath);
            return false;
        }
        
        System.out.println(directoryPath);
        printDirectoryTree(directory, "");
        
        return true;
    }
    
    /**
     * 递归打印目录树
     * @param file 当前文件或目录
     * @param prefix 前缀字符串（用于绘制树状结构）
     */
    private void printDirectoryTree(File file, String prefix) {
        File[] children = file.listFiles();
        
        if (children == null || children.length == 0) {
            return;
        }
        
        for (int i = 0; i < children.length; i++) {
            File child = children[i];
            boolean isLastChild = (i == children.length - 1);
            
            // 打印当前文件/目录
            System.out.print(prefix);
            System.out.print(isLastChild ? "└── " : "├── ");
            System.out.println(child.getName());
            
            // 如果是目录，递归打印子目录
            if (child.isDirectory()) {
                String newPrefix = prefix + (isLastChild ? "    " : "│   ");
                printDirectoryTree(child, newPrefix);
            }
        }
    }
}