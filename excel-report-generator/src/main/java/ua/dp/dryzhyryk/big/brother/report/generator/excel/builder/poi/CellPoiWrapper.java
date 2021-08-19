package ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.poi;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.Style;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.CellWrapper;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.RowWrapper;

import java.util.Map;

public class CellPoiWrapper implements CellWrapper {
    private final RowPoiWrapper row;
    private final XSSFCell cell;
    private final Map<Style, CellStyle> styles;

    public CellPoiWrapper(RowPoiWrapper row, XSSFCell cell, Map<Style, CellStyle> styles) {
        this.row = row;
        this.cell = cell;
        this.styles = styles;
    }

    @Override
    public CellWrapper withStyle(Style cellStyle) {
        cell.setCellStyle(styles.get(cellStyle));
        return this;
    }

    @Override
    public CellWrapper withValue(String cellValue) {
        cell.setCellValue(cellValue);
        return this;
    }

    @Override
    public CellWrapper withValue(double cellValue) {
        cell.setCellValue(cellValue);
        return this;
    }

    @Override
    public CellWrapper withComment(String cellComment) {
        if (null == cellComment) {
            return this;
        }
        Workbook workbook = row.sheetWrapper.workbook;
        Sheet sheet = row.sheetWrapper.sheet;

        Comment comment = addComment(workbook, sheet, cell, "big-brother", cellComment);
        cell.setCellComment(comment);

        return this;
    }


    private Comment addComment(Workbook workbook, Sheet sheet, Cell cell, String author, String commentText) {
        CreationHelper factory = workbook.getCreationHelper();
        //get an existing cell or create it otherwise:
        ClientAnchor anchor = factory.createClientAnchor();
        //i found it useful to show the comment box at the bottom right corner
        //		anchor.setCol1(cell.getColumnIndex() + 1); //the box of the comment starts at this given column...
        //		anchor.setCol2(cell.getColumnIndex() + 3); //...and ends at that given column
        //		anchor.setRow1(cell.getRowIndex() + 1); //one row below the cell...
        //		anchor.setRow2(cell.getRowIndex() + 5); //...and 4 rows high

        Drawing drawing = sheet.createDrawingPatriarch();
        Comment comment = drawing.createCellComment(anchor);
        //set the comment text and author
        comment.setString(factory.createRichTextString(commentText));
        comment.setAuthor(author);

        return comment;
    }

    @Override
    public RowWrapper buildCell() {
        return row;
    }
}
