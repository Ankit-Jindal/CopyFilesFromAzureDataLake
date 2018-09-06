package com.ankit.excel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.RichTextString;

/**
 * Using Apache POI API read Microsoft Excel (.xls) file and convert into CSV file with Java API.
 * Using Java API read Comma Separated Values(.csv) file and convert into XLS file with Apache POI API.
 * <p>
 * Microsoft Excel file is converted to CSV for all type of columns data.
 * </p>
 * <pre>
 * DataConvertionUtil.csvToEXCEL(csvFileName,excelFileName);
 * DataConvertionUtil.excelToCSV(excelFileName,csvFileName);
 * </pre>
 * @version 1.0, 13/July/2012
 * @author Stephen Babu.P
 *
 * See http://stephenbabu-p.blogspot.com/2012/07/convert-excel-file-to-csv-using-apache.html
 */
public class DataConvertionUtil
{
    /***
     * Date format used to convert excel cell date value
     */
    private static final String OUTPUT_DATE_FORMAT= "yyyy-MM-dd";
    /**
     * Comma separated characters
     */
    private static final String CVS_SEPERATOR_CHAR=",";
    /**
     * New line character for CSV file
     */
    private static final String NEW_LINE_CHARACTER="\r\n";

    /**
     * Convert CSV file to Excel file
     * @param csvFileName
     * @param excelFileName
     * @throws Exception
     */
    public static void csvToEXCEL(String csvFileName,String excelFileName) throws Exception{
        checkValidFile(csvFileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFileName)));
        HSSFWorkbook myWorkBook = new HSSFWorkbook();
        FileOutputStream writer = new FileOutputStream(new File(excelFileName) );
        HSSFSheet mySheet = myWorkBook.createSheet();
        String line= "";
        int rowNo=0;
        while ( (line=reader.readLine()) != null ){
            String[] columns = line.split(CVS_SEPERATOR_CHAR);
            HSSFRow myRow =mySheet.createRow(rowNo);
            for (int i=0;i<columns.length;i++){
                HSSFCell myCell = myRow.createCell(i);
                myCell.setCellValue(columns[i]);
            }
            rowNo++;
        }
        myWorkBook.write(writer);
        writer.close();
    }

    /**
     * Convert the Excel file data into CSV file
     * @param excelFileName
     * @param csvFileName
     * @throws Exception
     */
    public static void excelToCSV(String excelFileName,String csvFileName) throws Exception{

       // DataFormatter df = new DataFormatter()

        checkValidFile(csvFileName);
        HSSFWorkbook myWorkBook = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(excelFileName)));
        HSSFSheet mySheet = myWorkBook.getSheetAt(0);
        Iterator  rowIter =  mySheet.rowIterator();
        String csvData="";
        while (rowIter.hasNext()) {
            HSSFRow myRow = (HSSFRow) rowIter.next();
            for ( int i=0;i<myRow.getLastCellNum();i++){
                String csvCellData = getCellData(myRow.getCell(i)).replaceAll(",","") + CVS_SEPERATOR_CHAR;
                csvData += csvCellData;
                //csvData += getCellData(myRow.getCell(i));
            }
            if (csvData.charAt(csvData.length() - 1) == ',') csvData = csvData.substring(0,csvData.length() - 1 );
            csvData+=NEW_LINE_CHARACTER;
        }
        //1,Eldon Base for stackable storage shelf  platinum,Muhammed MacIntyre,3,-213.25,38.93999999999999772626324556767940521240234375,35,Nunavut,Storage & Organization,0.800000000000000044408920985006261616945266723632812
        //1	Eldon Base for stackable storage shelf, platinum	Muhammed MacIntyre	3	-213.25	38.94	35	Nunavut	Storage & Organization	0.8
        writeCSV(csvFileName, csvData);
    }
    /**
     * Write the string into a text file
     * @param csvFileName
     * @param csvData
     * @throws Exception
     */
    private static void writeCSV(String csvFileName,String csvData) throws Exception{
        FileOutputStream writer = new FileOutputStream(csvFileName);
        writer.write(csvData.getBytes());
        writer.close();
    }
    /**
     * Get cell value based on the excel column data type
     * @param myCell
     * @return
     */
    private static String getCellData( HSSFCell myCell) throws Exception{
        String cellData="";
        System.out.println(myCell);
        if ( myCell== null){
            cellData += CVS_SEPERATOR_CHAR;;
        }else{
            switch(myCell.getCellType() ){
                case  HSSFCell.CELL_TYPE_STRING  :
                case  HSSFCell.CELL_TYPE_BOOLEAN  :
                    RichTextString richString = myCell.getRichStringCellValue();
                    richString.clearFormatting();
                    String clearStr = richString.getString().replaceAll(","," ");
                    cellData +=  clearStr + CVS_SEPERATOR_CHAR;
                    break;
                case HSSFCell.CELL_TYPE_NUMERIC :
                    cellData += getNumericValue(myCell);
                    break;
                case  HSSFCell.CELL_TYPE_FORMULA :
                    cellData +=  getFormulaValue(myCell);
                default:
                    cellData += CVS_SEPERATOR_CHAR;;
            }
        }
        return cellData;
    }
    /**
     * Get the formula value from a cell
     * @param myCell
     * @return
     * @throws Exception
     */
    private static String getFormulaValue(HSSFCell myCell) throws Exception{
        String cellData="";
        if ( myCell.getCachedFormulaResultType() == HSSFCell.CELL_TYPE_STRING  || myCell.getCellType () ==HSSFCell.CELL_TYPE_BOOLEAN) {
            cellData +=  myCell.getRichStringCellValue ()+CVS_SEPERATOR_CHAR;
        }else  if ( myCell.getCachedFormulaResultType() == HSSFCell.CELL_TYPE_NUMERIC ) {
            cellData += getNumericValue(myCell)+CVS_SEPERATOR_CHAR;
        }
        return cellData;
    }
    /**
     * Get the date or number value from a cell
     * @param myCell
     * @return
     * @throws Exception
     */
    private static String getNumericValue(HSSFCell myCell) throws Exception {
        String cellData="";
        if ( HSSFDateUtil.isCellDateFormatted(myCell) ){
            cellData += new SimpleDateFormat(OUTPUT_DATE_FORMAT).format(myCell.getDateCellValue()) +CVS_SEPERATOR_CHAR;
        }else{
            cellData += new BigDecimal(myCell.getNumericCellValue()).toPlainString()+CVS_SEPERATOR_CHAR ;
/*            Double num = myCell.getNumericCellValue();
            //num.
            cellData += String.valueOf(myCell.getNumericCellValue()) + CVS_SEPERATOR_CHAR ;*/
        }
        return cellData;
    }
    private static void checkValidFile(String fileName){
        boolean valid=true;
        try{
            File f = new File(fileName);
            if ( !f.exists() || f.isDirectory() ){
                File parent = f.getParentFile();
                if(!parent.exists() && parent.mkdirs()) {
                    System.out.println(f.createNewFile() + " create file " + f.getName());
                }
            }
        }catch(Exception e){
            valid=false;
        }
        if ( !valid){
            System.out.println("File doesn't exist: " + fileName);
            System.exit(0);
        }
    }

    public static void main(String[] args) throws Exception
    {
        String excelfileName1="/Users/ankitkumar/Documents/Project Documents/astra/excel_sampleData/SampleXLSFile_38kb.xls";
        String csvFileName1="/Users/ankitkumar/Documents/Project Documents/astra/excelToCsvOut/SampleXLSFile_38kb.csv";
        String excelfileName2="D:\\stephen\\files\\excel-file2.xls";
        String csvFileName2="D:\\stephen\\files\\csv-file2.csv";
        excelToCSV(excelfileName1,csvFileName1);
        //csvToEXCEL(csvFileName2,excelfileName2);
    }
}