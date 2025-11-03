package org.flanVim.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.flanVim.command.Command;
import org.flanVim.command.CommandHistory;

/**
 * Editor: 一个打开的文本的包装类
 * 
 * 每个 Editor 维护自己的命令历史栈，实现文件级别的 undo/redo
 */
public class Editor {
    private StringBuilder content;
    private String filePath;
    private boolean modified = false;
    private boolean withLog = false;
    private LocalDateTime lastAccessTime;  // 最后访问时间
    private CommandHistory history = new CommandHistory();  // 每个 Editor 独立的历史栈

    /**
     * 从文件路径创建 Editor（如果文件存在则加载内容，否则创建空 Editor）
     * @param filePath 文件路径
     * @throws IOException 如果读取文件时发生错误
     */
    public Editor(String filePath) throws IOException {
        this.filePath = filePath;
        this.lastAccessTime = LocalDateTime.now();
        
        File file = new File(filePath);
        if (file.exists()) {
            // 文件存在，加载内容
            loadFromFile(file);
            this.modified = false;  // 刚加载的文件未修改
        } else {
            // 文件不存在，创建空 Editor
            this.content = new StringBuilder();
            this.modified = true;  // 新文件标记为已修改（需要保存）
        }
    }

    /**
     * 创建空的 Editor（用于测试或特殊场景）
     * @param filePath 文件路径
     * @param createEmpty 必须传 true，用于区分构造函数
     */
    public Editor(String filePath, boolean createEmpty) {
        this.filePath = filePath;
        this.content = new StringBuilder();
        this.lastAccessTime = LocalDateTime.now();
        this.modified = true;
    }

    /**
     * 从文件加载内容
     */
    private void loadFromFile(File file) throws IOException {
        // 先读取第一行检查是否启用日志模式
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();
            if (firstLine != null && firstLine.equals("# log")) {
                this.withLog = true;
            }
        }
        
        // 读取完整内容
        String fileContent = Files.readString(Paths.get(file.getAbsolutePath()));
        this.content = new StringBuilder(fileContent);
    }

    private void markModified() {
        this.modified = true;
    }

    public void append(String text) {
        if (content.length() > 0) {
            content.append("\n");
        }
        content.append(text);
        markModified();
    }

    public void insert(int line, int column, String text) throws IllegalArgumentException {
        validateLineColumn(line, column);
        
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Text to insert cannot be null or empty");
        }
        
        try {
            int position = getPositionFromLineColumn(line, column);
            content.insert(position, text);
            markModified();
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Insert position out of bounds at line " + line + 
                                             ", column " + column + ": " + e.getMessage(), e);
        }
    }

    public void delete(int line, int column, int length) throws IllegalArgumentException {
        validateLineColumn(line, column);
        validateLength(length);
        
        try {
            int position = getPositionFromLineColumn(line, column);
            if (position + length > content.length()) {
                throw new IllegalArgumentException(
                    "Delete range [" + position + ", " + (position + length) + 
                    ") exceeds content length " + content.length() + 
                    " at line " + line + ", column " + column
                );
            }
            content.delete(position, position + length);
            markModified();
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Delete position out of bounds at line " + line + 
                                             ", column " + column + ": " + e.getMessage(), e);
        }
    }

    /**
     * 获取指定位置的字符串
     * @param line 行号（从 1 开始）
     * @param column 列号（从 1 开始）
     * @param length 要获取的字符数
     * @return 指定位置的字符串
     * @throws IllegalArgumentException 如果参数无效或范围超出边界
     */
    public String getStringFromLineColumn(int line, int column, int length) 
            throws IllegalArgumentException {
        validateLineColumn(line, column);
        validateLength(length);
        
        try {
            int position = getPositionFromLineColumn(line, column);
            if (position + length > content.length()) {
                throw new IllegalArgumentException(
                    "Range [" + position + ", " + (position + length) + 
                    ") exceeds content length " + content.length() + 
                    " at line " + line + ", column " + column
                );
            }
            return content.substring(position, position + length);
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Position out of bounds at line " + line + 
                                             ", column " + column + ": " + e.getMessage(), e);
        }
    }

    /**
     * 将行号和列号转换为字符位置
     * @param line 行号（从1开始）
     * @param column 列号（从1开始）
     * @return 字符位置索引
     * @throws IllegalArgumentException 如果行号或列号无效
     */
    private int getPositionFromLineColumn(int line, int column) throws IllegalArgumentException {
        String[] lines = content.toString().split("\n", -1);
        if (line < 1 || line > lines.length) {
            throw new IllegalArgumentException(
                "Line number " + line + " out of range [1, " + lines.length + "]"
            );
        }
        
        int position = 0;
        // 累加前面所有行的长度
        for (int i = 0; i < line - 1; i++) {
            position += lines[i].length() + 1; // +1 for '\n'
        }
        
        // 加上当前行的列偏移
        if (column < 1 || column > lines[line - 1].length() + 1) {
            throw new IllegalArgumentException(
                "Column number " + column + " out of range [1, " + 
                (lines[line - 1].length() + 1) + "] at line " + line
            );
        }
        position += column - 1;
        
        return position;
    }

    /**
     * 验证行号和列号的有效性
     * @param line 行号
     * @param column 列号
     * @throws IllegalArgumentException 如果行号或列号无效
     */
    private void validateLineColumn(int line, int column) throws IllegalArgumentException {
        if (line < 1) {
            throw new IllegalArgumentException("Line number must be >= 1, got: " + line);
        }
        if (column < 1) {
            throw new IllegalArgumentException("Column number must be >= 1, got: " + column);
        }
    }

    /**
     * 验证长度参数的有效性
     * @param length 长度
     * @throws IllegalArgumentException 如果长度无效
     */
    private void validateLength(int length) throws IllegalArgumentException {
        if (length < 0) {
            throw new IllegalArgumentException("Length must be >= 0, got: " + length);
        }
    }

    public List<String> getLines(int startLine, int endLine) {
        String[] lines = content.toString().split("\n", -1);
        
        // 宽容处理
        if (startLine < 1) startLine = 1;
        if (endLine > lines.length) endLine = lines.length;

        if (startLine > endLine) {
            int t = startLine;
            startLine = endLine;
            endLine = t;
        }
        List<String> result = new ArrayList<>();
        for (int i = startLine - 1; i < endLine; i++) {
            result.add(lines[i]);
        }
        return result;
    }

    /**
     * 给append用的未包装版本
     * @param start
     * @param end
     */
    public void delete(int start, int end) {
        try {
            content.delete(start, end);
            markModified();
        } catch (StringIndexOutOfBoundsException e) {
            System.err.println("Delete failed: " + e.getMessage());
            throw e;
        }
    }

    public String getContent() {
        return content.toString();
    }

    public String getFilePath() {
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

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public void updateAccessTime() {
        this.lastAccessTime = LocalDateTime.now();
    }

    // ==================== 历史管理方法 ====================
    
    public void addToHistory(Command cmd) {
        history.addCommand(cmd);
    }
    
    public void undo() {
        history.undo();
    }

    public void redo() {
        history.redo();
    }
    

    public boolean hasUndo() {
        return history.getUndoSize() > 0;
    }
    
    public boolean hasRedo() {
        return history.getRedoSize() > 0;
    }
    
    public CommandHistory getHistory() {
        return history;
    }

}
