package com.ankit;


import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;



public class BusinessRuleStreamParser {



    private static final Logger logger = LoggerFactory.getLogger(BusinessRuleStreamParser.class);

    private static final char[] SUPPORTED_IMPORT_RULE_FILE_SEPARATORS = {',', ';'};
    private static final String CSV_EXTENSION = "csv";
    private static final String XLSX_EXTENSION = "xlsx";
    private static final String XLSM_EXTENSION = "xlsm";
    private static final String XLS_EXTENSION = "xls";
    private static final String[] EMPTY = {};



    public String parse(InputStream rulesInputStream, String extension, List<String> workSheetNames, boolean hasHeaders) {
        logger.info("Parse input stream, extension " + extension);
        switch (extension) {
            case XLSX_EXTENSION:
            case XLS_EXTENSION:
            case XLSM_EXTENSION:
                return parseXlsInputStream(rulesInputStream, workSheetNames, hasHeaders);
            default:
                return "";

        }
    }

    public List<String> getWorkSheetNames(InputStream rulesInputStream) {
        try (Workbook workbook = WorkbookFactory.create(rulesInputStream)) {
            return IntStream.range(0, workbook.getNumberOfSheets())
                    .mapToObj(workbook::getSheetAt)
                    .map(Sheet::getSheetName)
                    .collect(Collectors.toList());
        } catch (InvalidFormatException | EncryptedDocumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    private String parseXlsInputStream(InputStream rulesInputStream, List<String> workSheetNames, boolean hasHeaders) {

        ZipSecureFile.setMinInflateRatio(-1.0d);
        try (Workbook workbook = WorkbookFactory.create(rulesInputStream)) {
            int numberOfSheets = workbook.getNumberOfSheets();
            List<String[]> data;
            for (int n = 0; n < numberOfSheets; n++) {
                Sheet sheet = workbook.getSheetAt(n);
                String sheetName = sheet.getSheetName();
                if (!workSheetNames.contains(sheetName)) {
                    continue;
                }
                DataFormatter dataFormatter = new DataFormatter();

                int rows = sheet.getLastRowNum() + 1;
                logger.info("Sheet {} has {} physical rows from number {} to number {}, will use {} rows", sheetName,
                        sheet.getPhysicalNumberOfRows(), sheet.getFirstRowNum(), sheet.getLastRowNum(), rows);
                data = new ArrayList<>();
                for (int i = 0; i < rows; i++) {
                    Row row = sheet.getRow(i);
                    String[] rule;
                    if (row != null) {
                        short firstCellNum = row.getFirstCellNum();
                        short lastCellNum = row.getLastCellNum();
                        rule = new String[lastCellNum];
                        for (int j = 0; j < rule.length; j++) {
                            Cell cell = row.getCell(j);
                            if (cell != null) {
                                rule[j] = dataFormatter.formatCellValue(cell);
                            }
                        }
                        logger.info("Row {} has {} cells from number {} to number {}, used only first {} cells", i, row.getPhysicalNumberOfCells(),
                                firstCellNum, lastCellNum, rule.length);
                    } else {
                        rule = EMPTY;
                        logger.warn("Worksheet {} row {} is absent, will be ignored", sheetName, (i + 1));
                    }
                    data.add(rule);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Done";
    }


}
