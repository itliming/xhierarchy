package com.rltx.parser;


import com.rltx.method.SqlXmlConfigMethod;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class IBatisConfigParser {

    private static Map sqlMap;
    private static Map resultMap;
    private static String methodTypes = "insert update delete select";

    static {
        sqlMap = new HashMap();
        resultMap = new HashMap();

    }

    /**
     * 根据关键字获取方法列表
     *
     * @param keyword   关键字
     * @param documents 文件列表
     * @return 方法列表
     */
    public static List<SqlXmlConfigMethod> parse(String keyword, List<Document> documents, String classPath) throws MalformedURLException, ClassNotFoundException {
        List<SqlXmlConfigMethod> sqlXmlConfigMethods = new ArrayList<>();
        for (Document document : documents) {
            Element rootElement = document.getRootElement();
            String className = rootElement.attribute("namespace").getValue();
            System.out.println("name:" + className);
            File file = new File(classPath);
            URL url = file.toURI().toURL();
            URLClassLoader loader = new URLClassLoader(new URL[]{url});
            Class clazz = loader.loadClass(className);
            int nodeCount = rootElement.nodeCount();
            for (int i = 0; i < nodeCount; i++) {
                Node node = rootElement.node(i);
                if (node instanceof Element) {
                    Element element = (Element) node;
                    StringBuilder sb = new StringBuilder();
                    // 获取Element内容
                    getNodeContent(sb, element);
                    if (!sb.toString().contains(keyword)) {
                        continue;
                    }
                    String id = element.attribute("id").getValue();
                    String methodType = node.getName();
                    // 如果是方法
                    if (methodTypes.contains(methodType)) {
                        SqlXmlConfigMethod sqlXmlConfigMethod = new SqlXmlConfigMethod();
                        Method[] clazzMethods = clazz.getMethods();
                        for (Method clazzMethod : clazzMethods) {
                            // 重名方法会有问题 todo
                            if (id.equals(clazzMethod.getName())) {
                                sqlXmlConfigMethod.setKeyword(keyword);
                                sqlXmlConfigMethod.setMethod(clazzMethod);
                                sqlXmlConfigMethod.setMethodName(className + "." + id);
                                sqlXmlConfigMethod.setSql(sb.toString().trim().replace("\n", "").replace("\r", "").replace("\t", ""));
                                // 添加方法
                                sqlXmlConfigMethods.add(sqlXmlConfigMethod);
                                break;
                            }
                        }

                    } else if (methodType.equals("resultMap")) {
                        String type = element.attribute("type").getValue();
                        resultMap.put(id, type);
                    } else if (methodType.equals("sql")) {
                        sqlMap.put(id, sb.toString());
                    }
//                    System.out.println(methodType + ":" + id + ":" + sb.toString());
                }

            }
        }
        return sqlXmlConfigMethods;
    }

    private static void getNodeContent(StringBuilder sb, Node node) {
        if (node instanceof Element) {
            Element element = (Element) node;
            sb.append(node.getStringValue());
        }
    }


}
