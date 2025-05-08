package telran.monitoring;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.json.JSONArray;
import org.json.JSONObject;

public class ConverterService {
	private LinkedHashMap<String, JSONArray> jsonObjects;
	static private Iterator<Row> rowsIterator;
	private LinkedHashMap<String, Consumer<Cell>> handlers;
	private String[] titlesArr;
	private ConverterService(LinkedHashMap<String, JSONArray> jsonObjects,
			LinkedHashMap<String, Consumer<Cell>> handlers, String[] titlesArr ) {
		this.jsonObjects = jsonObjects;
		this.handlers = handlers;
		this.titlesArr = titlesArr;
	}

	private static Logger logger = ExcelConverterAppl.logger;
	private static ConverterService converterService = null;
	public static synchronized ConverterService
	of(Iterator<Row> rowsIterator) throws Exception {
		Row titles = rowsIterator.next();
		ConverterService.rowsIterator = rowsIterator;
		if (converterService == null) {
			LinkedHashMap<String, JSONArray> jsonObjects = new LinkedHashMap<>();
			LinkedHashMap<String, Consumer<Cell>> handlers = ConverterSchema.getHandlers(jsonObjects);
			logger.debug("handlers: {}", handlers.keySet());
			String []titlesArr = getTitlesArray(titles);
			logger.info("titles: {}", Arrays.toString(titlesArr));
			checkSchema(handlers, titlesArr);
			converterService = new ConverterService(jsonObjects, handlers, titlesArr);

		}
		return converterService;
	}

	public String getFinalJSON() throws Exception{
		while(rowsIterator.hasNext()) {
			Row row = rowsIterator.next();
			rowProcessing(row, titlesArr);
		}
		return toJSON();
	}

	private String toJSON() {
		JSONArray jsonArray = new JSONArray();
		
		jsonObjects.forEach((k, v) -> {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(k, v);
			jsonArray.put(jsonObj);
		});
		return jsonArray.toString();
	}

	private void rowProcessing(Row row, String[] titlesArray) throws Exception{
		Iterator<Cell> cellsIterator = row.cellIterator();
		while(cellsIterator.hasNext()) {
			cellProcessing(cellsIterator.next(), titlesArray);
		}
		
	}

	private void cellProcessing(Cell cell, String[] titlesArray) throws Exception{
		logger.trace(getMessage(cell, "processing"));
		try {
			int index = cell.getColumnIndex();
			String titleName = titlesArray[index];
			String key = getKeyTitle(titleName);
			Consumer<Cell> handler = handlers.get(key);
			if(handler == null) {
				throw new Exception(titleName + " Not found in Schema");
			}
			handler.accept(cell);
		} catch (Exception e) {
			throw new Exception(getMessage(cell, "Error") + e);
		}
		
	}

	private String getMessage(Cell cell, String status) {
		return String.format("row: %d, column: %c, %s ", cell.getRowIndex() + 1,
				'A' + cell.getColumnIndex(), status);
	}

	private static void checkSchema(LinkedHashMap<String, Consumer<Cell>> handlers, String[] titlesArr) throws Exception {
		Set<String> titleNames = handlers.keySet();
		int schemaNumberOfColumns = titleNames.size();
		if (schemaNumberOfColumns != titlesArr.length) {
			throw new Exception(String.format("Excel: %d columns; Schema: %d columns", titlesArr.length));
		}
		for(String title: titlesArr) {
			String keyTitle = getKeyTitle(title);
			if(!handlers.containsKey(keyTitle)) {
				throw new Exception(String.format("title %s doesn't exist in Schema", title));
			}
		}

	}

	private static String getKeyTitle(String title) {
		
		return title.toLowerCase().replaceAll("[\\s,]+", "_");
	}

	private static String[] getTitlesArray(Row titles) {
		Iterator<Cell> it = titles.cellIterator();

		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, 0), false).map(Cell::getStringCellValue)
				.map(str -> str.strip().replaceAll("\n", " ")).toArray(String[]::new);
	}

}
