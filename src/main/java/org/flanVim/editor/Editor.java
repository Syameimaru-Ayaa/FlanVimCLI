package org.flanVim.editor;


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

    public void append(String text) {
        content.append(text);
        modified = true;
    }

    public void insert(int position, String text) {
        if (position >= 0 && position <= content.length()) {
            content.insert(position, text);
            modified = true;
        }
    }

    public void delete(int start, int length) {
        if (start >= 0 && start + length <= content.length()) {
            content.delete(start, start + length);
            modified = true;
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

}
