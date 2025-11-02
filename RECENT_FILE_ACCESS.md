# 最近使用文件功能实现说明

## 🎯 需求分析

### 原始需求
> 关闭后，如果还有其他打开的文件，切换到**最近使用的文件**
> 
> "最近使用"定义：最后一次通过 `load` 或 `edit` 命令切换到的文件。

### 之前的问题 ❌
```java
// 错误实现：只是获取 HashMap 的第一个元素
String nextFile = allEditors.keySet().iterator().next();
```
- ❌ HashMap 的顺序不确定
- ❌ 不是最近使用的文件
- ❌ 无法满足需求

---

## 💡 解决方案：时间戳追踪

### 设计思路
在 `Editor` 中添加时间戳字段，每次访问时更新，从而实现精确的访问时间追踪。

### 架构优势
✅ **符合单一职责**：Editor 管理自己的访问时间  
✅ **易于扩展**：支持未来的功能（如"最近打开的文件列表"）  
✅ **性能良好**：O(n) 时间复杂度查找最近文件  
✅ **简单直观**：使用系统时间戳，无需额外维护

---

## 🏗️ 实现细节

### 1. Editor 类改进

#### 添加时间戳字段
```java
public class Editor {
    private long lastAccessTime;  // 最后访问时间戳
    
    public Editor(String filePath) {
        this.filePath = filePath;
        this.content = new StringBuilder();
        this.lastAccessTime = System.currentTimeMillis();  // 初始化
    }
}
```

#### 添加访问时间管理方法
```java
/**
 * 获取最后访问时间
 */
public long getLastAccessTime() {
    return lastAccessTime;
}

/**
 * 更新最后访问时间
 */
public void updateAccessTime() {
    this.lastAccessTime = System.currentTimeMillis();
}
```

---

### 2. WorkSpace 类改进

#### 自动更新访问时间
```java
public void setActiveEditor(String fileName) {
    this.activeEditor = editors.get(fileName);
    this.activeFileName = fileName;
    
    // 自动更新访问时间 ✅
    if (this.activeEditor != null) {
        this.activeEditor.updateAccessTime();
    }
}
```

每次通过 `load` 或 `edit` 切换文件时，`setActiveEditor` 会被调用，自动更新时间戳。

#### 获取最近使用的文件
```java
/**
 * 获取最近使用的文件（排除指定文件）
 * @param excludeFileName 要排除的文件名
 * @return 最近使用的文件名，如果没有其他文件则返回 null
 */
public String getMostRecentlyUsedFile(String excludeFileName) {
    String mostRecentFile = null;
    long mostRecentTime = 0;
    
    for (Map.Entry<String, Editor> entry : editors.entrySet()) {
        String fileName = entry.getKey();
        Editor editor = entry.getValue();
        
        // 跳过要排除的文件
        if (fileName.equals(excludeFileName)) {
            continue;
        }
        
        // 找到访问时间最晚的文件
        if (editor.getLastAccessTime() > mostRecentTime) {
            mostRecentTime = editor.getLastAccessTime();
            mostRecentFile = fileName;
        }
    }
    
    return mostRecentFile;
}
```

---

### 3. CloseCommand 改进

#### 使用最近文件逻辑
```java
// 关闭文件后，切换到最近使用的文件
if (wasActiveFile) {
    String mostRecentFile = workSpace.getMostRecentlyUsedFile(fileToClose);
    if (mostRecentFile != null) {
        workSpace.setActiveEditor(mostRecentFile);  // ✅ 正确的切换
        System.out.println("Switched to: " + mostRecentFile);
    } else {
        System.out.println("No more files open.");
    }
}
```

---

## 📊 完整测试用例

### 测试场景：验证最近使用逻辑

```bash
# 1. 加载多个文件
> load a.txt
Loaded file: E:\project\a.txt
# 时间戳：T1

> load b.txt  
Loaded file: E:\project\b.txt
# 时间戳：T2 (T2 > T1)

> load c.txt
Loaded file: E:\project\c.txt
# 时间戳：T3 (T3 > T2 > T1)

# 2. 切换到 a.txt
> edit a.txt
Switched to: E:\project\a.txt
# 时间戳：T4 (T4 > T3 > T2 > T1)
# 现在 a.txt 是最近使用的文件！

# 3. 切换到 c.txt
> edit c.txt
Switched to: E:\project\c.txt
# 时间戳：T5 (T5 > T4 > T3 > T2 > T1)
# 现在访问时间排序：c.txt (T5) > a.txt (T4) > b.txt (T2)

# 4. 关闭当前文件 c.txt
> close
Closed: E:\project\c.txt
Switched to: E:\project\a.txt  # ✅ 切换到 a.txt（第二近使用）
# 而不是 b.txt ！

# 5. 再次关闭当前文件 a.txt
> close
Closed: E:\project\a.txt
Switched to: E:\project\b.txt  # ✅ 只剩下 b.txt 了

# 6. 验证撤销
> undo
Undo: Reopened E:\project\a.txt
Restored as active file: E:\project\a.txt
```

---

## 🎯 时间线可视化

```
操作流程：
1. load a.txt    → a.txt 访问时间 = T1
2. load b.txt    → b.txt 访问时间 = T2
3. load c.txt    → c.txt 访问时间 = T3
4. edit a.txt    → a.txt 访问时间 = T4 (更新！)
5. edit c.txt    → c.txt 访问时间 = T5 (更新！)

当前访问时间排序：
c.txt (T5 最新) > a.txt (T4 第二) > b.txt (T2 最旧)

关闭 c.txt 后：
应该切换到 a.txt ✅（最近使用）
而不是 b.txt ❌（最旧）
```

---

## 🚀 未来扩展可能性

有了时间戳功能，可以轻松实现：

### 1. 最近打开文件列表
```java
public List<String> getRecentFiles(int limit) {
    return editors.entrySet().stream()
        .sorted((e1, e2) -> Long.compare(
            e2.getValue().getLastAccessTime(),
            e1.getValue().getLastAccessTime()
        ))
        .limit(limit)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
}
```

### 2. 文件访问统计
```java
public void showFileStatistics() {
    for (Map.Entry<String, Editor> entry : editors.entrySet()) {
        long accessTime = entry.getValue().getLastAccessTime();
        Date date = new Date(accessTime);
        System.out.println(entry.getKey() + " - Last accessed: " + date);
    }
}
```

### 3. 自动清理长时间未访问的文件
```java
public void closeStaleFiles(long maxIdleTime) {
    long now = System.currentTimeMillis();
    editors.entrySet().removeIf(entry -> 
        now - entry.getValue().getLastAccessTime() > maxIdleTime
    );
}
```

---

## ✅ 总结

### 实现的功能
- ✅ 精确追踪文件访问时间
- ✅ 关闭文件时切换到最近使用的文件
- ✅ 支持撤销/重做
- ✅ 自动更新访问时间（无需手动调用）

### 设计优势
- ✅ 符合单一职责原则
- ✅ 易于测试和维护
- ✅ 支持未来扩展
- ✅ 性能良好（O(n) 查找）

### 代码改动
- `Editor.java`: 添加 `lastAccessTime` 字段和相关方法
- `WorkSpace.java`: `setActiveEditor()` 自动更新时间 + 新增 `getMostRecentlyUsedFile()`
- `CloseCommand.java`: 使用最近文件逻辑
