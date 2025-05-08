package telran.monitoring;

import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.*;

public record ColumnDefinition(String title, Consumer<Cell> rowHandler) {

}
