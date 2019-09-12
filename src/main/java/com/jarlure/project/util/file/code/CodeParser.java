package com.jarlure.project.util.file.code;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeParser {

    //当前项目的.java文件存放路径。如果不正确，请手动在你的项目中重新配置该路径
    public static String SOURCE_CODE_ROOT_DIRECTORY = "src/main/java";
    //类（全）名的正则表达式，例如：int、String、java.util.regex.Matcher
    private static final String CLASS_NAME_REGEX = "\\w+([.]\\w+)*";
    //类声明的正则表达式，例如：Object[]、java.util.ArrayList<String[]>、HashMap<K,V>
    private static final String CLASS_DECLARATION_REGEX = CLASS_NAME_REGEX + "(<\\w+([.]\\w+)*(\\[\\])?(\\s*,\\s*\\w+([.]\\w+)*(\\[\\])?)?>)?(\\[\\])?";
    //泛型声明的正则表达式，例如：<T>、<? extends ArrayList<String[]>>
    private static final String GENERIC_DECLARATION_REGEX = "<[\\w\\?]+(\\s+extends\\s+" + CLASS_DECLARATION_REGEX + ")?(\\s*,\\s*[\\w\\?]+(\\s+extends\\s+" + CLASS_DECLARATION_REGEX + ")?)?>";

    /**
     * 获取去掉包名后的类名。
     *
     * @param className 类全名。例如：com.jarlure.project.util.file.code.CodeParser
     * @return 去掉包名后的类名。例如：CodeParser
     */
    public static String getSimpleName(String className) {
        int index = className.lastIndexOf(".");
        if (index == -1) return className;
        else return className.substring(index + 1);
    }

    /**
     * 将驼峰式命名规则的变量名变成大写字母+下划线的常量名。注意：该方法无法正确识别例如my3D这种变量名，会将其变为MY3_D。
     * 这是算法本身造成的。如果你有更巧妙的转换算法，请务必告之于我。
     *
     * @param varyName 变量名。要求驼峰式命名规则。例如：varyName
     * @return 常量名。例如：VARY_NAME
     */
    public static String toConstantName(String varyName) {
        if (Character.isUpperCase(varyName.charAt(0))) throw new IllegalArgumentException("传入参数必须遵守驼峰式命名规则，第一个字母小写");
        StringBuilder result = new StringBuilder(varyName.length());
        for (char c : varyName.toCharArray()) {
            if (Character.isLowerCase(c)) result.append(Character.toUpperCase(c));
            else if (Character.isUpperCase(c)) result.append('_').append(c);
            else result.append(c);
        }
        return result.toString();
    }

    /**
     * 判断字符串中是否包含有该字段。该方法常用于通过判断变量名中是否包含有某些字段识别其作用。例如变量string可能是类型
     * 为String的一个类对象。
     *
     * @param str   字符串。例如：DesktopBackground
     * @param param 字段。例如：background
     * @return 如果字符串包含有该字段（该字段的首字母大小写不影响检查）返回true，例如：DesktopBackground中含有
     * background或Background；否则返回false
     */
    public static boolean contains(String str, String param) {
        if (param == null || param.isEmpty()) return false;
        char initial = param.charAt(0);
        if (Character.isLetter(initial)) {
            StringBuilder builder = new StringBuilder(param.length() + 4);
            builder.append('[');
            builder.append(Character.toUpperCase(initial));
            builder.append(Character.toLowerCase(initial));
            builder.append(']');
            builder.append(param, 1, param.length());
            String regex = builder.toString();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(str);
            return matcher.find();
        } else return str.contains(param);
    }

    /**
     * 判断字符串中是否以该字段结尾。该方法常用于通过判断变量名中是否包含有某些字段识别其作用。例如变量string可能是类型
     * 为String的一个类对象。
     *
     * @param str   字符串。例如：DesktopBackground
     * @param param 字段。例如：background
     * @return 如果字符串包含有该字段（该字段的首字母大小写不影响检查）返回true，例如：DesktopBackground中含有
     * background或Background；否则返回false
     */
    public static boolean endsWith(String str, String param) {
        if (param == null || param.isEmpty()) return false;
        char initial = param.charAt(0);
        if (Character.isLetter(initial)) {
            StringBuilder builder = new StringBuilder(param.length());
            if (str.length() == param.length()) {
                builder.append(Character.toLowerCase(initial));
            } else {
                builder.append(Character.toUpperCase(initial));
            }
            builder.append(param, 1, param.length());
            return str.endsWith(builder.toString());
        } else return str.endsWith(param);
    }

    /**
     * 通过给定的类寻找它对应的Java文件
     *
     * @param clazz 给定的类
     * @return 它对应的Java文件
     */
    public static File findJavaFileByClass(Class clazz) {
        String className = clazz.getName();
        StringBuilder builder = new StringBuilder();
        for (String str : className.split("[.]")) {
            builder.append('\\').append(str);
        }
        builder.append(".java");
        String classPath = builder.toString();

        File root = new File(System.getProperty("user.dir"));
        Set<File> list = getJavaFile(root, null);
        for (File file : list) {
            if (file.getAbsolutePath().endsWith(classPath)) {
                return file;
            }
        }
        return null;
    }

    /**
     * 获取给定文件夹下所有Java文件。包括子文件夹、孙子文件夹、曾孙子文件夹……子子孙孙文件夹下的Java文件
     *
     * @param file  文件夹
     * @param store 用于存放找到的Java文件。可以为null
     * @return 定文件夹下所有Java文件
     */
    public static Set<File> getJavaFile(File file, Set<File> store) {
        if (store == null) store = new HashSet<>();
        if (!file.exists()) return store;
        if (file.isFile()) {
            if (file.getName().endsWith(".java")) store.add(file);
            return store;
        }
        File[] list = file.listFiles();
        if (list == null) return store;
        for (File subFile : list) getJavaFile(subFile, store);
        return store;
    }

    /**
     * 根据给定的类全名生成Java文件路径。
     *
     * @param className 类全名。例如：com.jarlure.project.util.file.code.CodeParser
     * @return Java文件路径。例如：src/main/java/com/jarlure/project/util/file/code/CodeParser.java
     */
    public static String toJavaFilePath(String className) {
        if (!className.matches(CLASS_NAME_REGEX)) throw new IllegalArgumentException("类名非法！className=" + className);
        StringBuilder path = new StringBuilder();
        path.append(SOURCE_CODE_ROOT_DIRECTORY);
        for (String str : className.split("[.]")) {
            path.append('/').append(str);
        }
        path.append(".java");
        return path.toString();
    }

    /**
     * 过滤掉单行注释。
     *
     * @param line 代码行。例如： String str = "Hello World!";//这是一行代码
     * @return 去掉单行注释后的代码行。例如：String str = "Hello World!";
     */
    public static String filterSingleLineCommentSymbol(String line) {
        if (line == null || line.isEmpty()) return line;
        int index = line.indexOf("//");
        if (index == -1) return line;
        if (isInParenthesesSymbolPair(line, index, index + 2)) {
            return line.substring(0, index + 2) + filterSingleLineCommentSymbol(line.substring(index + 2));
        }
        return line.substring(0, index);
    }

    /**
     * 判断该代码行是否是包声明
     *
     * @param line 代码行。例如：package com.jarlure.project.util.file.code;
     * @return true如果该代码行是包声明；false如果不是
     */
    public static boolean isPackageLine(String line) {
        String regex = "\\s*package\\s+\\w+(\\.\\w+)*\\s*;\\s*";
        return line.matches(regex);
    }

    /**
     * 将包声明代码行分解成包声明字段。
     *
     * @param line 包声明代码行。例如：package com.jarlure.project.util.file.code;
     * @return 包声明字段。例如：数组[0]="package"、数组[1]="com.jarlure.project.util.file.code"、数组[2]=";"
     */
    public static String[] splitPackageLine(String line) {
        String packageKeyword = "\\s*package\\s+";
        String packageName = CLASS_NAME_REGEX;
        String semicolon = "\\s*;\\s*";
        return split(line, packageKeyword, packageName, semicolon);
    }

    /**
     * 判断该代码行是否是导入声明
     *
     * @param line 代码行。例如：import java.io.File;
     * @return true如果该代码行是导入声明；false如果不是
     */
    public static boolean isImportLine(String line) {
        String regex = "\\s*import\\s+(static\\s+)?\\w+(\\.\\w+)*(\\.\\*)?\\s*;\\s*";
        return line.matches(regex);
    }

    /**
     * 将导入声明代码行分解成导入声明字段。
     *
     * @param line 代码行。例如：import java.io.File;
     * @return 导入声明字段。例如：数组[0]="import"、数组[1]="java.io.File"、数组[2]=";"
     */
    public static String[] splitImportLine(String line) {
        String importKeyWord = "\\s*import\\s+";
        String staticKeyWord = "(static\\s+)?";
        String importName = CLASS_NAME_REGEX + "(\\.\\*)?";
        String semicolon = "\\s*;\\s*";
        return split(line, importKeyWord, staticKeyWord, importName, semicolon);
    }

    /**
     * 判断该代码行是否是类声明
     *
     * @param line 代码行。例如：public interface Screen extends AppState {
     * @return true如果该代码行是类声明；false如果不是
     */
    public static boolean isClassLine(String line) {
        String regex = "(public|protected|private|static|final|abstract|\\s)*" +
                "(class|interface|enum)\\s+" +
                "\\w+" +//类名
                "(\\s*" + GENERIC_DECLARATION_REGEX + ")?" +//泛型
                "(\\s+extends\\s+" + CLASS_DECLARATION_REGEX + ")?" +//类继承
                "(\\s+implements\\s+" + CLASS_DECLARATION_REGEX + "(\\s*,\\s*" + CLASS_DECLARATION_REGEX + ")*" + ")?" +//接口实现
                "\\s*\\{?;?}?;?";
        return line.matches(regex);
    }

    /**
     * 将类声明代码行分解成类声明字段。
     *
     * @param line 代码行。例如：public interface Screen extends AppState {
     * @return 类声明字段。例如：数组[0]="public"、数组[1]="interface"、数组[2]="Screen"、数组[3]="extends"、
     * 数组[4]="AppState"、数组[5]="{"
     */
    public static String[] splitClassLine(String line) {
        String relativeKeyword = "(public|protected|private|static|final|abstract|\\s)*";
        String classKeyword = "(class|interface|enum)\\s+";
        String className = "\\w+";
        String genericDeclaration = "(\\s*" + GENERIC_DECLARATION_REGEX + ")?";
        String extendsDeclaration = "(\\s+extends\\s+" + CLASS_DECLARATION_REGEX + ")?";
        String implementsDeclaration = "(\\s+implements\\s+" + CLASS_DECLARATION_REGEX + "(\\s*,\\s*" + CLASS_DECLARATION_REGEX + ")*" + ")?";
        String semicolon = "\\s*\\{?;?}?;?";
        return split(line, relativeKeyword, classKeyword, className, genericDeclaration, extendsDeclaration, implementsDeclaration, semicolon);
    }

    /**
     * 判断该代码行是否是类构造方法声明
     *
     * @param line 代码行。例如：public String(String original) {
     * @return true如果该代码是类构造方法声明；false如果不是
     */
    public static boolean isConstructMethod(String line) {
        String regex = "^(?!\\s*return)^(?!\\s*new)^(?!\\s*else)^(?!\\s*if)^(?!\\s*if)^(?!\\s*default)" +
                "\\s*(public|protected|private)?" +
                "\\s+\\w+" +//方法名
                "\\s*\\(\\s*([,]?\\s*" + CLASS_DECLARATION_REGEX + "(\\s*[.]{3})?\\s+\\w+(\\[\\])?)*\\s*\\)" +//参数类型和参数名
                "\\s*\\{?}?";
        return line.matches(regex);
    }

    /**
     * 获得构造方法名。（应该没什么实际用途）
     *
     * @param line 构造方法代码行
     * @return 构造方法名
     */
    public static String getConstructName(String line) {
        String regex = "^(?!\\s*return)^(?!\\s*new)^(?!\\s*else)^(?!\\s*if)^(?!\\s*if)^(?!\\s*default)" +
                "\\s*(public|protected|private)?\\s+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) return "";
        line = line.substring(matcher.end());
        regex = "\\w+";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(line);
        if (!matcher.find()) return "";
        line = line.substring(matcher.start(), matcher.end());
        return line;
    }

    /**
     * 判断该代码行是否是类方法或成员方法声明
     *
     * @param line 代码行。例如：public static boolean isMethodLine(String line) {
     * @return true如果该代码行是类方法或成员方法声明；false如果不是
     */
    public static boolean isMethodLine(String line) {
        String regex = "^(?!\\s*return)^(?!\\s*new)^(?!\\s*else)^(?!\\s*if)" +
                "(default|public|protected|private|static|final|abstract|synchronized|" + GENERIC_DECLARATION_REGEX + "|\\s)*" +
                "(?!public)(?!protected)(?!private)" +//过滤掉构造方法
                CLASS_DECLARATION_REGEX +//返回类型
                "\\s+\\w+" +//方法名
                "\\s*\\(\\s*([,]?\\s*" + CLASS_DECLARATION_REGEX + "(\\s*[.]{3})?\\s+\\w+(\\[\\])?)*\\s*\\)" +//参数类型和参数名
                "(\\s*throws\\s*\\w+)?" +
                "\\s*\\{?;?}?;?";
        return line.matches(regex);
    }

    /**
     * 获得方法名。
     *
     * @param line 类方法或成员方法声明。例如：public static String getMethodName(String line) {
     * @return 方法名。例如：getMethodName
     */
    public static String getMethodName(String line) {
        String regex = "^(?!\\s*return)^(?!\\s*new)^(?!\\s*else)^(?!\\s*if)" +
                "(default|public|protected|private|static|final|abstract|synchronized|" + GENERIC_DECLARATION_REGEX + "|\\s)*" +
                CLASS_DECLARATION_REGEX;//返回类型
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) return "";
        line = line.substring(matcher.end());
        regex = "\\w+";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(line);
        if (!matcher.find()) return "";
        line = line.substring(matcher.start(), matcher.end());
        return line;
    }

    /**
     * 判断给定代码行是否以分号结尾。
     *
     * @param line 代码行。例如：return -1 != line.lastIndexOf(";");
     * @return true如果给定代码行以分号结尾；false如果不是
     */
    public static boolean isCodeLine(String line) {
        return -1 != line.lastIndexOf(";");
    }

    /**
     * 判断给定代码行是否以左花括号结尾。
     *
     * @param line 代码行。例如：public static boolean isCodeBlock(String line) {
     * @return true如果给定代码行以左花括号结尾；false如果不是
     */
    public static boolean isCodeBlock(String line) {
        int index = line.lastIndexOf("{");
        if (-1 == index) return false;
        return -1 == line.indexOf("}", index);
    }

    /**
     * 判断给定代码行是否是空代码块
     *
     * @param line 代码行。例如：public doSomething(){}
     * @return true如果给定代码行是空代码块；false如果不是
     */
    public static boolean isEmptyCodeBlock(String line) {
        int index = line.lastIndexOf("{");
        if (-1 == index) return false;
        index = line.indexOf("}", index);
        if (-1 == index) return false;
        return '{' == line.charAt(index - 1);
    }

    /**
     * 判断代码行中的一段是否被多行注释注释
     *
     * @param line       代码行。例如：public doSomething()/*$skip*反斜杠{}
     * @param startIndex 开始索引值（包含）
     * @param endIndex   结束索引值（不包含）
     * @return true如果给定的这段被多行注释注释;false如果不是
     */
    public static boolean isInAsteriskSymbolPair(String line, int startIndex, int endIndex) {
        String regex = "[*][^*]*[*]";
        return isInSymbolPair(line, regex, startIndex, endIndex);
    }

    /**
     * 判断代码行中的一段是否处于双引号内
     *
     * @param line       代码行。例如：System.out.println("执行了doSomething()");
     * @param startIndex 开始索引值（包含）
     * @param endIndex   结束索引值（不包含）
     * @return true如果给定的这段处于双引号内；false如果不处于
     */
    public static boolean isInDoubleQuotesSymbolPair(String line, int startIndex, int endIndex) {
        String regex = "[\"][^\"]*[\"]";
        return isInSymbolPair(line, regex, startIndex, endIndex);
    }

    /**
     * 判断代码行中的一段是否处于小括号内
     *
     * @param line       代码行。例如：System.out.println("执行了doSomething()");
     * @param startIndex 开始索引值（包含）
     * @param endIndex   结束索引值（不包含）
     * @return true如果给定的这段处于小括号内；false如果不处于
     */
    public static boolean isInParenthesesSymbolPair(String line, int startIndex, int endIndex) {
        String regex = "[(][^()]*[)]";
        return isInSymbolPair(line, regex, startIndex, endIndex);
    }

    /**
     * 判断代码行中的一段是否处于方括号内
     *
     * @param line       代码行。例如：String[] array="Hello World!".split(" ");
     * @param startIndex 开始索引值（包含）
     * @param endIndex   结束索引值（不包含）
     * @return true如果给定的这段处于方括号内；false如果不处于
     */
    public static boolean isInSquareBracketsSymbolPair(String line, int startIndex, int endIndex) {
        String regex = "\\[[^\\[\\]]*\\]";
        return isInSymbolPair(line, regex, startIndex, endIndex);
    }

    /**
     * 判断代码行中的一段是否处于尖括号内
     *
     * @param line       代码行。例如：List<String> list;
     * @param startIndex 开始索引值（包含）
     * @param endIndex   结束索引值（不包含）
     * @return true如果给定的这段处于尖括号内；false如果不处于
     */
    public static boolean isInAngleBracketsSymbolPair(String line, int startIndex, int endIndex) {
        String regex = "[<][^<>]*[>]";
        return isInSymbolPair(line, regex, startIndex, endIndex);
    }

    /**
     * 判断代码行中的一段是否处于花括号内
     *
     * @param line       代码行。例如：public doSomething(){System.out.println("执行了doSomething()");}
     * @param startIndex 开始索引值（包含）
     * @param endIndex   结束索引值（不包含）
     * @return true如果给定的这段处于花括号内；false如果不处于
     */
    public static boolean isInBraceSymbolPair(String line, int startIndex, int endIndex) {
        String regex = "[{][^{}]*[}]";
        return isInSymbolPair(line, regex, startIndex, endIndex);
    }

    private static boolean isInSymbolPair(String line, String regex, int startIndex, int endIndex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            if (matcher.end() < startIndex) continue;
            if (matcher.start() < startIndex && endIndex < matcher.end()) return true;
            if (matcher.start() < endIndex) break;
        }
        return false;
    }

    private static String[] split(String line, String... regexArray) {
        String[] result;

        Pattern pattern = Pattern.compile(regexArray[0]);
        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) return null;
        result = new String[regexArray.length];
        result[0] = line.substring(matcher.start(), matcher.end());
        for (int i = 1, start = 0; i < regexArray.length; i++) {
            pattern = Pattern.compile(regexArray[i]);
            matcher.usePattern(pattern);
            if (!matcher.find(start)) return null;
            result[i] = line.substring(matcher.start(), matcher.end());
            start = matcher.end();
        }
        return result;
    }

}
