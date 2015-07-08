package fcmps.ui;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
/**
 * 將DscPageableSortableTable元件中的內容或是ResultSet匯出到Excel中.
 * @author 劉建華
 *
 */
public class JExportToExcel{
	private boolean autoAdjustWidth=true;
	private String[] DisplayColumns;
	File output;
//	private int	execExportType = 1;
	
	private String fileID;
	private String SheetName="Sheet1";
	private int[][] ItemType;
	private HSSFWorkbook wb;
	private HSSFSheet in_sheet =null;  //外部傳入的Sheet;

	private String formatFile=""; //範本檔
	
	/**
	 * 構造函數
	 * 構造完成後呼叫Export()方法完成匯出.
	 * 此構造函數用於其它類中自行匯出用.
	 */	
	public JExportToExcel(){
		
	}
	
	public void writeFile(java.io.OutputStream out) throws java.io.IOException{
        byte[] b = new byte[1024];
        int i = 0;
        FileInputStream fileIn = new java.io.FileInputStream(output);
        while ((i = fileIn.read(b)) > 0) {
          out.write(b, 0, i);               
        }		
	}
	
	public java.lang.String getContentType(){
//		return "application/msexcel";
		return "application%2Fmsexcel";
		
	}
	
	/**
	 * 設定範本檔 例如:ftbmd/FTBMD10R_3.xls
	 * @param formatFile
	 */
	public void setFormatFile(String formatFile){
		this.formatFile=formatFile;
	}
	
	/**
	 * 取消輸出的文件名稱
	 * @return
	 */
	public java.lang.String getFileName(){
		return output.getName();
	}
	
	public int getSize(){
		return -1;
	}
	
	/**
	 * 取得Excel 的 WorkBook
	 * @return
	 */
	public HSSFWorkbook getWorkbook(){
		if(wb==null){
			if(!formatFile.equals("")){ //有設定範本
				try{
					File formatFolder =new File(getClass().getClassLoader().getResource("conf/format").getFile());
					File format=new File(formatFolder,formatFile);
			        FileInputStream in = new FileInputStream(format);
			        wb = new HSSFWorkbook(in);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}else{
				wb = new HSSFWorkbook();
			}
		}
		return wb;
	}
	
	/**
	 * 設定 Excel 的 WorkBook
	 * @return
	 */
	public void setWorkbook(HSSFWorkbook wb){
		this.wb=wb;
	}
	
	public HSSFSheet getHSSFSheet(){
		return in_sheet;
	}
	
	public void setHSSFSheet(HSSFSheet sheet){
		in_sheet=sheet;
	}
	
	/**
	 * 取得標題行要顯示的標題
	 * @return
	 */
	public String[] getDisplayColumns(){
		return DisplayColumns;
	}
	
	/**
	 * 設定欄位顯示的標題,不設定直接用DscPageableSortableTable或ResultSet各欄位的標題
	 * @param s
	 */
	public void setDisplayColumns(String[] s){
		DisplayColumns=s;
	}

	/**
	 * 設定Excel中Sheet顯示的名稱
	 * @param SheetName
	 */
	public void setSheetName(String SheetName){
		this.SheetName=SheetName;
	}
	
	/**
	 * 設定欄位的數據類型,因為DscPageableSortableTable中的各欄位均為String,
	 * 故要手動設定
	 * @param ItemType
	 */
	public void setItemType(int[][] ItemType){
		this.ItemType=ItemType;
	}
	
	/**
	 * 設定文件的UUID號
	 * @param FileID
	 */
	public void setFileID(String FileID){
		this.fileID=FileID;
	}
	
	/**
	 * 取得文件的UUID號
	 * @return
	 */
	public String getFileID(){
		return fileID;
	}
	
    private void copyValue(HSSFCell cell1, HSSFCell cell2) {
        switch(cell1.getCellType()) {
            case HSSFCell.CELL_TYPE_BLANK:
                break;
            case HSSFCell.CELL_TYPE_BOOLEAN:
                cell2.setCellValue(cell1.getBooleanCellValue());
                break;
            case HSSFCell.CELL_TYPE_ERROR:
                cell2.setCellErrorValue(cell1.getErrorCellValue());
                break;
            case HSSFCell.CELL_TYPE_FORMULA:
                cell2.setCellFormula(cell1.getCellFormula());
                break;
            case HSSFCell.CELL_TYPE_NUMERIC:
                cell2.setCellNum(cell1.getCellNum());
                break;
            case HSSFCell.CELL_TYPE_STRING:
                cell2.setCellValue(cell1.getStringCellValue());
        }
    }
    
	public boolean isAutoAdjustWidth() {
		return autoAdjustWidth;
	}

	public void setAutoAdjustWidth(boolean autoAdjustWidth) {
		this.autoAdjustWidth = autoAdjustWidth;
	}
	
    public static void setCellValue(HSSFWorkbook wb, HSSFCell cell, Object value) {
        cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
        if (value == null)
            return;
        if (value instanceof String) {
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            cell.setEncoding(HSSFCell.ENCODING_UTF_16);
            cell.setCellValue((String)value);
        } else if (value instanceof Number) {
            cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            cell.setCellValue(((Number)value).doubleValue());
        } else if (value instanceof Calendar) {
            cell.setCellValue(HSSFDateUtil.getExcelDate(((Calendar)value).getTime()));
//            HSSFCellStyle style = wb.createCellStyle();
//            style.setDataFormat(dataFormat.getFormat(DATE_FORMAT));
//            cell.setCellStyle(style);
        } else if (value instanceof Date) {
        	SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd");
        	cell.setCellValue(sdf.format((Date)value));
//            cell.setCellValue(HSSFDateUtil.getExcelDate((Date)value));
//            HSSFCellStyle style = wb.createCellStyle();
//            style.setDataFormat(dataFormat.getFormat(DATE_FORMAT));
//            cell.setCellStyle(style);
        } else if (value instanceof Boolean) {
            cell.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
            cell.setCellValue(((Boolean)value).booleanValue());
        } else {
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            cell.setEncoding(HSSFCell.ENCODING_UTF_16);
            cell.setCellValue(value.toString());
            
        }
    }	
    
    public void setCellValue(HSSFWorkbook wb, HSSFCell cell, Object value,int Col) {
    	boolean iExists=false;
        cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
        if (value == null)
            return;
        
    	if(ItemType.length>0){
    		for(int i=0;i<ItemType.length;i++){
    			if(ItemType[i][0]==Col){
    				iExists=true;
    				int CellType=ItemType[i][1];
    				
    		        if (CellType==HSSFCell.CELL_TYPE_STRING) {
    		            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
    		            cell.setEncoding(HSSFCell.ENCODING_UTF_16);
    		            cell.setCellValue((String)value); 
    		        } else if (CellType==HSSFCell.CELL_TYPE_NUMERIC) {
    		            
    		            if(!value.equals("")){
    		            	cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
    		            	cell.setCellValue(((Number)Double.valueOf(String.valueOf(value))).doubleValue());
    		            }else{
        		            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
        		            cell.setEncoding(HSSFCell.ENCODING_UTF_16);
        		            cell.setCellValue((String)value);    		            	
    		            }    		            
    		        } else if (value instanceof Boolean) {
    		            cell.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
    		            cell.setCellValue(((Boolean)value).booleanValue());
    		        } else {
    		            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
    		            cell.setEncoding(HSSFCell.ENCODING_UTF_16);
    		            cell.setCellValue(value.toString());    		            
    		        } 
    		        break;
    			}
    		} 
    		if(!iExists){
    			setCellValue(wb,cell,value);
    		}
    	}else{
    		setCellValue(wb,cell,value);
    	}
        
    }

	
}
