package telran.monitoring;

import java.io.*;
import java.util.Iterator;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;

public class ExcelConverterAppl {
	public static final String LOGGER_NAME = "converter_logger";
	public static final String LOGGER_LEVEL_ENV_VARIABLE = "LOGGER_LEVEL";
	private static final String DEFAULT_PATH = "protocol-pma2.xlsx";
	private static final String DEFAULT_OUTPUT_PATH = "protocol.json";
	public static Logger logger = LogManager.getLogger(LOGGER_NAME);
	public static void main(String[] args)  {
		try {
			String levelStr = System.getenv(LOGGER_LEVEL_ENV_VARIABLE);
			if (levelStr == null) {
				levelStr = "INFO";
			}
			
			Configurator.setLevel(LOGGER_NAME, Level.valueOf(levelStr));
			String workbookPath = args.length > 0 ? args[0] : DEFAULT_PATH;
			int sheetNumber = args.length > 1 ? Integer.parseInt(args[1]) : 0;
			FileInputStream file = new FileInputStream(new File(workbookPath));
			// Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);
			logger.info("workbook : {}", workbookPath);
			// Get a sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(sheetNumber);
			logger.info("sheet: " + sheet.getSheetName());
			Iterator<Row> rowIterator = sheet.iterator();
			ConverterService converterService = ConverterService.of(rowIterator);
			String json = converterService.getFinalJSON();
			PrintStream output = new PrintStream(DEFAULT_OUTPUT_PATH);
			output.println(json);
			output.close();
			file.close();
			workbook.close();
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} 
	}

}
