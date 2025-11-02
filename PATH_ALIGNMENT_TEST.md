# 路径对齐测试

## LoadCommand vs SaveCommand 路径处理

### LoadCommand 路径转换逻辑
```java
fullPath = filePath;
if (workSpace.getWorkSpacePath() != null) {
    File file = new File(filePath);
    if (!file.isAbsolute()) {
        fullPath = new File(workSpace.getWorkSpacePath(), filePath).getAbsolutePath();
    }
}
// 存储时使用: workSpace.addEditor(fullPath, editor)
```

### SaveCommand 路径转换逻辑
```java
private String resolveFilePath(String filePath) {
    if (workSpace.getWorkSpacePath() != null) {
        File file = new File(filePath);
        if (!file.isAbsolute()) {
            return new File(workSpace.getWorkSpacePath(), filePath).getAbsolutePath();
        }
    }
    return new File(filePath).getAbsolutePath();
}
```

### 结论
✅ **路径处理已对齐**
- LoadCommand 存储**绝对路径**到 WorkSpace
- SaveCommand 将相对路径转换为**绝对路径**后查找

## 测试场景

### 场景 1: 相对路径
```bash
> inispace E:\myproject
> load test.txt                    # 存储为 E:\myproject\test.txt
> append Hello World
> save test.txt                     # 转换为 E:\myproject\test.txt ✅
```

### 场景 2: 绝对路径
```bash
> load E:\other\file.txt           # 存储为 E:\other\file.txt
> append Content
> save E:\other\file.txt           # 直接使用 E:\other\file.txt ✅
```

### 场景 3: 多文件保存（新功能）
```bash
> load a.txt
> load b.txt
> load c.txt
> append Content to a
> append Content to b
> append Content to c
> save a.txt b.txt c.txt           # 批量保存 ✅
Saved: E:\myproject\a.txt
Saved: E:\myproject\b.txt
Saved: E:\myproject\c.txt
Saved 3 of 3 file(s).
```

### 场景 4: 混合路径
```bash
> load a.txt                       # 相对路径
> load E:\absolute\b.txt          # 绝对路径
> save a.txt E:\absolute\b.txt    # 混合保存 ✅
```

## 新增功能

### 多文件保存支持
- ✅ 支持空格分隔的多个文件名
- ✅ 自动转换相对路径到绝对路径
- ✅ 显示保存成功/失败的文件数量
- ✅ 与 LoadCommand 路径处理完全对齐

### 使用示例
```bash
# 保存单个文件
> save test.txt

# 保存多个文件
> save file1.txt file2.txt file3.txt

# 保存所有文件
> save --all
> save -a

# 保存当前活动文件
> save
```
