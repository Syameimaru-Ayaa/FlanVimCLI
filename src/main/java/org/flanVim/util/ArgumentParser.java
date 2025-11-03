package org.flanVim.util;

import java.util.ArrayList;
import java.util.List;

/**
 * ArgumentParser - 智能参数解析器
 * 支持双引号包裹的字符串和转义字符
 * 
 * 示例：
 * - insert 1:1 "hello world"  → ["insert", "1:1", "hello world"]
 * - insert 1:1 "say \"hi\""   → ["insert", "1:1", "say \"hi\""]
 * - insert 1:1 "line1\nline2" → ["insert", "1:1", "line1\nline2"]
 */
public class ArgumentParser {
    
    /**
     * 解析模式
     */
    public enum ParseMode {
        /**
         * 严格模式：引号后必须是空格或结束，否则报错
         * 例如: "abc"def → 报错
         */
        STRICT,
        
        /**
         * 宽松模式：引号后的非空格字符会被忽略（直到下一个空格）
         * 例如: "abc"def → ["abc"] (忽略 def)
         */
        LENIENT
    }
    
    private static ParseMode defaultMode = ParseMode.STRICT;
    
    /**
     * 设置默认解析模式
     */
    public static void setDefaultMode(ParseMode mode) {
        defaultMode = mode;
    }
    
    /**
     * 解析命令行输入（使用默认模式）
     */
    public static String[] parse(String input) {
        return parse(input, defaultMode);
    }
    
    /**
     * 解析命令行输入，支持引号和转义字符
     * 
     * @param input 用户输入的命令字符串
     * @param mode 解析模式（严格/宽松）
     * @return 解析后的参数数组
     */
    public static String[] parse(String input, ParseMode mode) {
        if (input == null || input.trim().isEmpty()) {
            return new String[0];
        }
        
        List<String> args = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        boolean inQuotes = false;
        boolean escaping = false;
        boolean skipUntilSpace = false;  // 用于宽松模式：跳过引号后的非空格字符
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            // 宽松模式：跳过引号后的非法字符
            if (skipUntilSpace) {
                if (Character.isWhitespace(c)) {
                    skipUntilSpace = false;
                    // 保存当前参数
                    if (currentArg.length() > 0) {
                        args.add(currentArg.toString());
                        currentArg = new StringBuilder();
                    }
                }
                continue;  // 跳过当前字符
            }
            
            if (escaping) {
                // 处理转义字符
                switch (c) {
                    case 'n':
                        currentArg.append('\n');
                        break;
                    case 't':
                        currentArg.append('\t');
                        break;
                    case 'r':
                        currentArg.append('\r');
                        break;
                    case '\\':
                        currentArg.append('\\');
                        break;
                    case '"':
                        currentArg.append('"');
                        break;
                    default:
                        // 未知转义序列，保留反斜杠
                        currentArg.append('\\').append(c);
                }
                escaping = false;
            } else if (c == '\\') {
                // 开始转义
                escaping = true;
            } else if (c == '"') {
                if (inQuotes) {
                    // 遇到右引号：结束引号
                    inQuotes = false;
                    
                    // 检查引号后的字符
                    if (i + 1 < input.length()) {
                        char nextChar = input.charAt(i + 1);
                        if (!Character.isWhitespace(nextChar)) {
                            if (mode == ParseMode.STRICT) {
                                // 严格模式：报错
                                throw new IllegalArgumentException(
                                    String.format("Invalid syntax at position %d: character '%c' after closing quote. " +
                                                "Expected whitespace or end of input. Input: %s", 
                                                i + 1, nextChar, input)
                                );
                            } else {
                                // 宽松模式：忽略后续字符直到空格
                                skipUntilSpace = true;
                            }
                        }
                    }
                } else {
                    // 遇到左引号：开始引号
                    inQuotes = true;
                }
            } else if (Character.isWhitespace(c) && !inQuotes) {
                // 遇到空格且不在引号内，结束当前参数
                if (currentArg.length() > 0) {
                    args.add(currentArg.toString());
                    currentArg = new StringBuilder();
                }
            } else {
                // 普通字符
                currentArg.append(c);
            }
        }
        
        // 添加最后一个参数
        if (currentArg.length() > 0) {
            args.add(currentArg.toString());
        }
        
        // 检查是否有未闭合的引号
        if (inQuotes) {
            throw new IllegalArgumentException("Unclosed quote in command: " + input);
        }
        
        if (escaping) {
            throw new IllegalArgumentException("Trailing escape character in command: " + input);
        }
        
        return args.toArray(new String[0]);
    }
    
    /**
     * 测试方法（可选）
     */
    public static void main(String[] args) {
        // 测试用例
        String[] testCases = {
            "insert 1:1 hello",                    // 简单参数
            "insert 1:1 \"hello world\"",          // 带空格
            "insert 1:1 \" hello\"",               // 以空格开头
            "insert 1:1 \"  multiple  spaces  \"", // 多个空格
            "insert 1:1 \"say \\\"hi\\\"\"",       // 转义引号
            "insert 1:1 \"line1\\nline2\"",        // 换行符
            "insert 1:1 \"tab\\there\"",           // 制表符
            "insert 1:1 \"backslash\\\\\"",        // 反斜杠
            "append \"\"",                         // 空字符串
            "replace 1:1 5 \"new text\""           // replace 命令
        };
        
        for (String testCase : testCases) {
            System.out.println("Input: " + testCase);
            try {
                String[] result = parse(testCase);
                System.out.print("Output: [");
                for (int i = 0; i < result.length; i++) {
                    System.out.print("\"" + result[i] + "\"");
                    if (i < result.length - 1) System.out.print(", ");
                }
                System.out.println("]");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
            System.out.println();
        }
    }
}
