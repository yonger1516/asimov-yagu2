package com.seven.asimov.it.utils.sysmonitor;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExcelReport {

    private static final String TAG = ExcelReport.class.getSimpleName();
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private File file;
    private FileOutputStream stream;
    private SystemInfo systemInfo;
    private Context context;
    private HSSFWorkbook wb;
    private int rowIndex = 0;
    private String packageName;

    public ExcelReport(Context context, String packageName, String reportName, String path) throws IOException,
            PackageManager.NameNotFoundException {
        this.systemInfo = SystemInfo.INSTANCE;
        this.context = context;
        this.packageName = packageName;
        this.file = new File(path + reportName + "Report.xls");
        createTitleSheet();
    }

    private void createTitleSheet() throws IOException, PackageManager.NameNotFoundException {
        if (file.exists()) file.delete();
        wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Device info");
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Device information");

        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Manufacturer");
        row.createCell(1).setCellValue(systemInfo.getManufacturer());

        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Model");
        row.createCell(1).setCellValue(systemInfo.getModel());

        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Android version");
        row.createCell(1).setCellValue(systemInfo.getAndroidVersion());

        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Network interface");
        row.createCell(1).setCellValue(systemInfo.getNetworkInterface(context));

        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("OC version");
        row.createCell(1).setCellValue(systemInfo.getOCVersion(context, packageName));
        saveFile();
        rowIndex = 0;
    }

    public void createNewSheet(List<AppInfo> appInfoList, String sheetName, List<String> columnList) {
        Log.i(TAG, "ReportPoi  sheetName = " + sheetName + " appInfoList.size() = " + appInfoList.size());
        readExistWorkBook();
        Log.i(TAG, "" + wb.getNumberOfSheets());
        Sheet sheet = wb.createSheet(sheetName);
        Row row = sheet.createRow(rowIndex++);
        for (int i = 0; i < columnList.size(); i++) {
            row.createCell(i, Cell.CELL_TYPE_BLANK).setCellValue(columnList.get(i));
        }
        Date date = new Date();
        for (AppInfo appInfo : appInfoList) {
            int index = 0;
            row = sheet.createRow(rowIndex++);
            date.setTime(appInfo.getTime());
            row.createCell(index++, Cell.CELL_TYPE_STRING).setCellValue(dateFormat.format(date));

            row.createCell(index++, Cell.CELL_TYPE_NUMERIC).setCellValue(formatFloat(appInfo.getCpuTotal(), 2));
            row.createCell(index++, Cell.CELL_TYPE_NUMERIC).setCellValue(formatFloat(appInfo.getAvailableMemory(), 3));
            row.createCell(index++, Cell.CELL_TYPE_NUMERIC).setCellValue(formatFloat(appInfo.getTotalMemory(), 3));
            row.createCell(index++, Cell.CELL_TYPE_NUMERIC).setCellValue(appInfo.getTotalRAM());
            row.createCell(index++, Cell.CELL_TYPE_NUMERIC).setCellValue(appInfo.getUsedRAM());

            if (appInfo instanceof AppInfoOC) {
                AppInfoOC aiOC = (AppInfoOC) appInfo;
                row.createCell(index++, Cell.CELL_TYPE_NUMERIC).setCellValue(formatFloat(aiOC.getCpuController(), 2));
                row.createCell(index++, Cell.CELL_TYPE_NUMERIC).setCellValue(aiOC.getMemController());
                row.createCell(index++, Cell.CELL_TYPE_NUMERIC).setCellValue(formatFloat(aiOC.getCpuEngine(), 2));
                row.createCell(index++, Cell.CELL_TYPE_NUMERIC).setCellValue(aiOC.getMemEngine());
                for (DispatcherInfo dispatcherInfo : aiOC.getDispatcherList()) {
                    row.createCell(index++, Cell.CELL_TYPE_NUMERIC).setCellValue(formatFloat(dispatcherInfo.getCpuUsage(), 2));
                    row.createCell(index++, Cell.CELL_TYPE_NUMERIC).setCellValue(dispatcherInfo.getMemoryUsage());
                }
            }
        }
        Log.i(TAG, "" + wb.getNumberOfSheets());
        saveFile();
        rowIndex = 0;
    }

    private boolean isSheetExists(String name) {
        for (int i = 1; i < wb.getNumberOfSheets(); i++) {
            if (wb.getSheetAt(i).getSheetName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private void readExistWorkBook() {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            wb = new HSSFWorkbook(is);
        } catch (FileNotFoundException fne) {
            Log.e(TAG, ExceptionUtils.getStackTrace(fne));
        } catch (IOException ioe) {
            Log.e(TAG, ExceptionUtils.getStackTrace(ioe));
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                }
            }
        }
    }

    private String formatFloat(float number, int digitsAfterPoint) {
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        decimalFormat.setMaximumFractionDigits(digitsAfterPoint);
        return decimalFormat.format(number);
    }

    public void saveFile() {
        try {
            stream = new FileOutputStream(file);
            wb.write(stream);
        } catch (FileNotFoundException fne) {
            Log.e(TAG, ExceptionUtils.getStackTrace(fne));
        } catch (IOException ioe) {
            Log.e(TAG, ExceptionUtils.getStackTrace(ioe));
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                Log.e(TAG, ExceptionUtils.getStackTrace(e));
            }
        }
    }
}
