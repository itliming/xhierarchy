package com.rltx;


import com.rltx.method.CallHierarchyMethod;
import com.rltx.method.SqlXmlConfigMethod;
import com.rltx.parser.IBatisConfigParser;
import com.rltx.parser.MethodCallHierarchyParser;
import com.rltx.parser.XmlDocumentParser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import sun.misc.JarFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.*;

public class CallHierarchyMain {


    public static void main(String[] args) throws Exception {

        String ibatisConfigPath = "F:\\Ronglian\\code\\app\\xtailor-web-app\\target\\classes\\mybatis";
        String keyword = "mobile_app";
        String classPath = null;
        Properties properties = System.getProperties();
        Set<Map.Entry<Object, Object>> entries = properties.entrySet();
        for (Map.Entry entry : entries) {
            if (entry.getKey().equals("java.class.path")) {
                classPath = (String) entry.getValue();
                break;
            }
        }

        String jarFilePath = getJarFilePath("F:\\Ronglian\\code\\app\\xtailor-web-app\\target\\xtailor\\WEB-INF\\lib");

        classPath = classPath.concat(";").concat(jarFilePath).concat("F:\\Ronglian\\code\\app\\xtailor-web-app\\target\\classes");
//        String classPath = "F:\\Ronglian\\code\\app\\xtailor-web-app\\target\\classes";
        System.out.println(classPath);
        List<String> sourceFolders = new ArrayList<>();
        sourceFolders.add("F:\\Ronglian\\code\\app\\xtailor-web-app\\src\\main\\java");
//        String methodName = "com.wl.xtailor.controller.PatchController.createPatch";
//        String methodName = "com.wl.xtailor.service.IMobileAppPatchService.uploadPatch";
        String methodName = "com.wl.xtailor.dao.MobileAppPatchDao.save";
        ShowMethodCallHierarchy showMethodCallHierarchy = new ShowMethodCallHierarchy(classPath, sourceFolders, methodName, System.out);
        showMethodCallHierarchy.scan();
//        showMethodCallHierarchy.printMethodCalleeHierarchy();
//        showMethodCallHierarchy.printMethodCallerHierarchy();

        Map<CtExecutableReference, List<CtExecutableReference>> calleeList = showMethodCallHierarchy.getCalleeList();
        Map<CtExecutableReference, List<CtExecutableReference>> callerList = showMethodCallHierarchy.getCallerList();
        Map<CtTypeReference, Set<CtTypeReference>> classHierarchy = showMethodCallHierarchy.getClassHierarchy();

        List<Document> documents = XmlDocumentParser.parse(ibatisConfigPath);
        List<SqlXmlConfigMethod> methodList = IBatisConfigParser.parse(keyword, documents, "F:\\Ronglian\\code\\app\\xtailor-web-app\\target\\classes");
        HSSFWorkbook hssfWorkbook = POIHelper.generateCallerHierarchyWorkbook(methodList, calleeList, callerList, classHierarchy);
        OutputStream outputStream = new FileOutputStream("F:\\caller.xls");
        hssfWorkbook.write(outputStream);
        outputStream.close();
    }

    private static String getJarFilePath(String path) {
        StringBuilder sb = new StringBuilder();

        File file = new File(path);
        File[] files = file.listFiles(new JarFilter());
        for (File jarFile : files) {
            sb.append(jarFile.getAbsolutePath()).append(";");
        }
        return sb.toString();
    }

}

