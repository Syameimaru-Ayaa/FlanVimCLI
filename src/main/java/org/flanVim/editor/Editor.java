package org.flanVim.editor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Editor: 一个打开的文本的包装类
 */
public class Editor {
    private StringBuilder content;
    private String filePath;
    private boolean modified = false;
    private boolean withLog = false;

    public Editor(String filePath) {
        this.filePath = filePath;
        this.content = new StringBuilder();
    }

    public Editor(String filePath, String initialContent) {
        this.filePath = filePath;
        this.content = new StringBuilder(initialContent);
        modified = true;
    }

    public Editor(String filePath, String initialContent, boolean withLog) {
        this.filePath = filePath;
        this.content = new StringBuilder(initialContent);
        this.withLog = withLog;
        modified = true;
    }

    /**
     * 标记文件为已修改
     */
    private void markModified() {
        this.modified = true;
    }

    /**
     * 在文件末尾追加一行文本（自动添加换行符）
     */
    public void append(String text) {
        if (content.length() > 0) {
            content.append("\n");
        }
        content.append(text);
        markModified();
    }

    public void insert(int position, String text) {
        if (position >= 0 && position <= content.length()) {
            content.insert(position, text);
            markModified();
        }
    }

    public void delete(int start, int length) {
        if (start >= 0 && start + length <= content.length()) {
            content.delete(start, start + length);
            markModified();
        }
    }

    public String getContent() {
        return content.toString();
    }

    public String getfilePath() {
        return filePath;
    }

    public int getContentLength() {
        return content.length();
    }

    public boolean isWithLog() {
        return withLog;
    }

    public void setWithLog(boolean withLog) {
        this.withLog = withLog;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * 保存文件内容到磁盘
     * @return true 如果保存成功，false 如果保存失败
     * @throws IOException 如果发生 I/O 错误
     */
    public boolean save() throws IOException {
        File file = new File(filePath);
        
        // 确保父目录存在
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Unable to create directory: " + parentDir.getAbsolutePath());
            }
        }

        // 检查文件是否可写
        if (file.exists() && !file.canWrite()) {
            throw new IOException("File is not writable: " + filePath);
        }

        // 写入文件
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content.toString());
        }

        // 清除已修改标记
        this.modified = false;
        
        return true;
    }

}
