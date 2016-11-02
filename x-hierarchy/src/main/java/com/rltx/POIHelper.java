package com.rltx;

import com.rltx.method.SqlXmlConfigMethod;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.Region;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.lang.reflect.Method;
import java.util.*;

public class POIHelper {

    private static HSSFCellStyle style;
    private static HSSFCellStyle style1;
    private static HSSFCellStyle style2;

    public static HSSFWorkbook generateCallerHierarchyWorkbook(List<SqlXmlConfigMethod> methodList, Map<CtExecutableReference, List<CtExecutableReference>> calleeList, Map<CtExecutableReference, List<CtExecutableReference>> callerList, Map<CtTypeReference, Set<CtTypeReference>> classHierarchy) {

        // 第一步，创建一个webbook，对应一个Excel文件
        HSSFWorkbook wb = new HSSFWorkbook();
        // 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
        HSSFSheet sheet = wb.createSheet("已订车列表");
        sheet.setColumnWidth((short) 0, (short) 5000);
        sheet.setColumnWidth((short) 1, (short) 10000);
        sheet.setColumnWidth((short) 2, (short) 20000);
//        sheet.setColumnWidth((short) 2, (short) 8000);
//        sheet.setColumnWidth((short) 3, (short) 4000);
//        sheet.setColumnWidth((short) 4, (short) 5000);
//        sheet.setColumnWidth((short) 5, (short) 5000);
// 第四步，创建单元格，并设置值表头 设置表头居中
        style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_LEFT); // create dynamic style

        style1 = wb.createCellStyle();
        style1.setAlignment(HSSFCellStyle.ALIGN_LEFT); // create title style
        style1.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
        style1.setFillForegroundColor(HSSFColor.WHITE.index);
        style1.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        HSSFFont headerFont = wb.createFont();
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setBoldweight((short) 13);
        headerFont.setFontName("宋体");
        headerFont.setColor(HSSFColor.BLACK.index);
        style1.setFont(headerFont);
        style1.setWrapText(true);
        setBorderStyle(style1);

        style2 = wb.createCellStyle();
        style2.setAlignment(HSSFCellStyle.ALIGN_LEFT); // create header style
        style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
        style2.setFillForegroundColor(HSSFColor.WHITE.index);
        style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        HSSFFont headerFont1 = wb.createFont();
        headerFont1.setFontHeightInPoints((short) 12);
        headerFont1.setFontName("宋体");
        headerFont1.setColor(HSSFColor.BLACK.index);
        style2.setFont(headerFont1);
        style2.setWrapText(true);
        setBorderStyle(style2);

        sheet.addMergedRegion(new Region(0, (short) 0, 0, (short) 5));
        wb.setSheetName(0, "调用分析", HSSFWorkbook.ENCODING_UTF_16); // 设置sheet中文编码；
        HSSFRow row = sheet.createRow(0);
        row.setHeight((short) 500);
//      row1.setHeight((short) 400);
        HSSFCell cell = row.createCell((short) 0);
        cell.setEncoding(HSSFWorkbook.ENCODING_UTF_16);
        cell.setCellValue("调用分析");
        cell.setCellStyle(style1);
// 填充表格

        int currentRow = 1;
        for (SqlXmlConfigMethod configMethod : methodList) {
            String methodName = configMethod.getMethodName();
            MethodCallHierarchyBuilder methodCallHierarchyBuilder = MethodCallHierarchyBuilder.forMethodName(methodName, calleeList, callerList, classHierarchy);
            String callerHierarchy = methodCallHierarchyBuilder.getCallerHierarchy();

            row = sheet.createRow(currentRow);
            cell = row.createCell((short) 0);
            cell.setCellStyle(style2);
            cell.setCellValue(configMethod.getKeyword());
            cell = row.createCell((short) 1);
            cell.setCellStyle(style2);
            int i = configMethod.getSql().indexOf(configMethod.getKeyword());
            int beginIndex = i - 20 > 0 ? i - 20 : 0;
            int endIndex = i + configMethod.getKeyword().length() + 20 < configMethod.getSql().length() ? i + configMethod.getKeyword().length() + 20 : configMethod.getSql().length();
            cell.setCellValue(configMethod.getSql().substring(beginIndex, endIndex));
            cell = row.createCell((short) 2);
            cell.setCellStyle(style2);
            cell.setCellValue(callerHierarchy);
            currentRow++;
        }

//        cell = row1.createCell((short) 0);
//        cell.setCellStyle(style);
//        cell.setEncoding(HSSFWorkbook.ENCODING_UTF_16);
//        cell.setCellValue("序号");
//        cell.setCellStyle(style2);
//        cell.setEncoding(HSSFWorkbook.ENCODING_UTF_16);
//        cell = row1.createCell((short) 1);
//        cell.setEncoding(HSSFWorkbook.ENCODING_UTF_16);
//        cell.setCellValue("车牌号");
//        cell.setCellStyle(style2);
//        cell = row1.createCell((short) 2);
//        cell.setEncoding(HSSFWorkbook.ENCODING_UTF_16);
//        cell.setCellValue("车型");
//        cell.setCellStyle(style2);
//        cell = row1.createCell((short) 3);
//        cell.setEncoding(HSSFWorkbook.ENCODING_UTF_16);
//        cell.setCellValue("联系人");
//        cell.setCellStyle(style2);
//        cell = row1.createCell((short) 4);
//        cell.setEncoding(HSSFWorkbook.ENCODING_UTF_16);
//        cell.setCellValue("手机号码");
//        cell.setCellStyle(style2);
//        cell = row1.createCell((short) 5);
//        cell.setEncoding(HSSFWorkbook.ENCODING_UTF_16);
//        cell.setCellValue("手机号码");
//        cell.setCellStyle(style2);

        return wb;
    }

    private static void fillSheet(CellIndex cellIndex, int currentColum, CtExecutableReference ctExecutableReference, HSSFSheet sheet, Set<CtExecutableReference> alreadyVisited, Map<CtExecutableReference, List<CtExecutableReference>> callerList, Map<CtTypeReference, Set<CtTypeReference>> classHierarchy) {
//        int rowCount = getRowCount(ctExecutableReference);
//        int columnCount = getColumnCount(ctExecutableReference);
        if (alreadyVisited.contains(ctExecutableReference)) {
            return;
        }
        alreadyVisited.add(ctExecutableReference);

//        sheet.addMergedRegion(new Region(startRow, (short) ((short) startRow + rowCount), startColum, (short) ((short) startColum + columnCount)));
        HSSFRow row = sheet.createRow(cellIndex.currentRow);
        HSSFCell cell = row.createCell((short) currentColum);
        cell.setCellStyle(style2);
        cell.setCellValue(ctExecutableReference.getDeclaringType().getQualifiedName() + "." + ctExecutableReference.getSimpleName());
        List<CtExecutableReference> callerListForMethod = callerList.get(ctExecutableReference);
        if (callerListForMethod == null) {
            Set superInterfaces = ctExecutableReference.getDeclaringType().getSuperInterfaces();
            for (Object o : superInterfaces) {
                CtTypeReference superclass = (CtTypeReference) o;
                Collection<CtExecutableReference> declaredExecutables = superclass.getDeclaredExecutables();
                Iterator iterator = declaredExecutables.iterator();
                while (iterator.hasNext()) {
                    CtExecutableReference superRef = (CtExecutableReference) iterator.next();
                    CtExecutableReference overridingExecutable = superRef.getOverridingExecutable(ctExecutableReference.getDeclaringType());
                    if (overridingExecutable.equals(ctExecutableReference)) {
                        callerListForMethod = callerList.get(superRef);
                    }
                    if (callerListForMethod != null) {
                        break;
                    }
                }

            }

            if (callerListForMethod == null) {
                return;
            }

        }
        for (CtExecutableReference eachReference : callerListForMethod) {
            if (!(eachReference.toString().contains("xtailor.controller") || eachReference.toString().contains("xtailor.service") || eachReference.toString().contains("xtailor.dao"))) {
                continue;
            }

            fillSheet(cellIndex, currentColum + 1, eachReference, sheet, alreadyVisited, callerList, classHierarchy);
            Set<CtTypeReference> subclasses = classHierarchy.get(eachReference.getDeclaringType());
            if (subclasses != null) {
                for (CtTypeReference subclass : subclasses) {
                    CtExecutableReference reference = eachReference.getOverridingExecutable(subclass);
                    if (reference != null) {
                        fillSheet(cellIndex, currentColum, eachReference, sheet, alreadyVisited, callerList, classHierarchy);
                    }
                }
            }


        }
        cellIndex.setCurrentRow(cellIndex.getCurrentRow() + 1);

    }


    private static void setBorderStyle(HSSFCellStyle style) {
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        // 设置边框颜色
        style.setTopBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setBottomBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setLeftBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setRightBorderColor(HSSFColor.GREY_40_PERCENT.index);
    }

    private static int getRowCount(CtExecutableReference executableReference) {
        return 1;
    }

    private static int getColumnCount(CtExecutableReference executableReference) {
        return 1;
    }

    public static class CellIndex {
        private int currentRow;

        public int getCurrentRow() {
            return currentRow;
        }

        public void setCurrentRow(int currentRow) {
            this.currentRow = currentRow;
        }

    }
}
