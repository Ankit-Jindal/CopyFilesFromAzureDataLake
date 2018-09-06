package com.ankit.excelasstream;

import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ExcelStreamReader {
    public static void main(String[] args) {
        String xlsxFile = "/Users/ankitkumar/Documents/Project Documents/astra/data_vishwa/BiGEXCEL/MT_Table2.xlsx";
        InputStream is = null;
        try {
            is = new FileInputStream(new File(xlsxFile));
/*
            Workbook workbook = StreamingReader.builder()
                    .rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
                    .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
                    .open(is);
*/
            Workbook workbook = StreamingReader.builder()
                    .rowCacheSize(500)    // number of rows to keep in memory (defaults to 10)
                    .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
                    .open(is);
            for (Sheet sheet : workbook){
                System.out.println(sheet.getSheetName());
                for (Row r : sheet) {
                    for (Cell c : r) {
                        System.out.println(c.getStringCellValue());
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
