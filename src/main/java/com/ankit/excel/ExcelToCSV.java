package com.ankit.excel;

import com.ankit.BusinessRuleStreamParser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelToCSV {

    //static String xlsxFile = "";



    public static void main(String[] args) {
        Workbook wb = null;
       // String xlsxFile = "/Users/ankitkumar/Documents/projects/astra/data/excel_data_tmp";
        String xlsxFile = "/Users/ankitkumar/Documents/Project Documents/astra/data_vishwa/BiGEXCEL/MT_Table2.xlsx";
         String baseOutPath = "/Users/ankitkumar/Documents/Project Documents/astra/excelToCsvOutBiGEXCEL";
         //String xlsxFile="/Users/ankitkumar/Documents/Project Documents/astra/excel_sampleData/SampleXLSFile_38kb.xls";
         String csvFile="/Users/ankitkumar/Documents/Project Documents/astra/excelToCsvOut/SampleXLSFile_38kb2.csv";

        BusinessRuleStreamParser businessRuleStreamParser = new BusinessRuleStreamParser();
        try (FileInputStream fis = new FileInputStream(xlsxFile)) {
            String extension = xlsxFile.substring(xlsxFile.lastIndexOf(".") + 1);
             businessRuleStreamParser.parse(fis, extension, Arrays.asList(""), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File excelDirFile = new File(xlsxFile);
        Set<String> excelFilePaths = new HashSet<>();
        if(excelDirFile.isDirectory() && excelDirFile.exists()) {
            File[] files = excelDirFile.listFiles();
            for (File file: files) {
                if(file.isFile() && file.getName().contains("xls")) {
                    excelFilePaths.add(xlsxFile + "/" + file.getName());
                }
            }
        }

        for ( String excelPath : excelFilePaths) {
            excelExcelWorbookToCsv(excelPath, baseOutPath);
        }


        excelExcelWorbookToCsv(xlsxFile, baseOutPath);

    }

    private static  void excelExcelWorbookToCsv(String excelPath , String csvBbaseDir) {

        String xlsxFile = excelPath;
        Workbook wb = null;


        try {

            FileInputStream excelInputStream = new FileInputStream(excelPath);
            //Workbook wb = WorkbookFactory.create(excelInputStream);
            //wb = WorkbookFactory.create(new File(excelPath));
            try {
                wb = new HSSFWorkbook(new FileInputStream(new File(xlsxFile)));
            } catch (OfficeXmlFileException EXCP){
                try {
                    ZipSecureFile.setMinInflateRatio(-1.0d);
                    OPCPackage pkg = OPCPackage.open(excelInputStream);
                    //wb = new XSSFWorkbook(pkg);
                    wb = new XSSFWorkbook(pkg);
                    // wb = new SXSSFWorkbook(xssfWb, 100);
                } catch (Exception exc) {
                    exc.printStackTrace();
                }

            }
            Iterator<Sheet> allSheets = wb.sheetIterator();
            while (allSheets.hasNext()) {
                Sheet sheet = allSheets.next();
                String sheetName = sheet.getSheetName();
                String csvFile = csvBbaseDir + "/" + sheetName + ".csv";

                FormulaEvaluator fe = null;
                fe = wb.getCreationHelper().createFormulaEvaluator();
                DataFormatter formatter = new DataFormatter();
                PrintStream out = new PrintStream(new FileOutputStream(csvFile),
                        true, "UTF-8");
                byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
                // out.write(bom);
                StringBuilder sb = new StringBuilder();

                for (int r = 0, rn = sheet.getLastRowNum(); r <= rn; r++) {
                    sb.setLength(0);
                    Row row = sheet.getRow(r);
                    if (row == null) {
                        out.println(',');
                        sb.append(',');
                        continue;
                    }
                    boolean firstCell = true;
                    for (int c = 0, cn = row.getLastCellNum(); c < cn; c++) {
                        Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        if (!firstCell) {
                            out.print(',');
                            sb.append(',');
                        }
                        if (cell != null) {
                            if (fe != null) cell = fe.evaluateInCell(cell);
                            String value = formatter.formatCellValue(cell);
                            if (cell.getCellTypeEnum() == CellType.FORMULA) {
                                value = "=" + value;
                            }
                            out.print(encodeValue(value));
                            sb.append(encodeValue(value));
                        }
                        firstCell = false;
                    }
                    System.out.println(sb.toString());

                    out.println();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

     private static Pattern rxquote = Pattern.compile("\"");

        private static String encodeValue(String value) {
        boolean needQuotes = false;
        if (value.indexOf(',') != -1 || value.indexOf('"') != -1 ||
                value.indexOf('\n') != -1 || value.indexOf('\r') != -1)
            needQuotes = true;
        Matcher m = rxquote.matcher(value);
        if (m.find()) needQuotes = true;
        value = m.replaceAll("\"\"");
        if (needQuotes) return "\"" + value + "\"";
        else return value;
    }
}
