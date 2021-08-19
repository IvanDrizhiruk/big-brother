package ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.poi;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.CellCommentWrapper;

public class CellPoiCommentWrapper implements CellCommentWrapper {

	protected final Comment comment;

	public CellPoiCommentWrapper(String commentText) {

		Workbook workbook;
		Sheet sheet;

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
		//		comment.setAuthor(author);

		this.comment = comment;
	}

//
//	private void addComment(Workbook workbook, Sheet sheet, Cell cell, String author, String commentText) {
//		CreationHelper factory = workbook.getCreationHelper();
//		//get an existing cell or create it otherwise:
//		ClientAnchor anchor = factory.createClientAnchor();
//		//i found it useful to show the comment box at the bottom right corner
//		//		anchor.setCol1(cell.getColumnIndex() + 1); //the box of the comment starts at this given column...
//		//		anchor.setCol2(cell.getColumnIndex() + 3); //...and ends at that given column
//		//		anchor.setRow1(cell.getRowIndex() + 1); //one row below the cell...
//		//		anchor.setRow2(cell.getRowIndex() + 5); //...and 4 rows high
//
//		Drawing drawing = sheet.createDrawingPatriarch();
//		Comment comment = drawing.createCellComment(anchor);
//		//set the comment text and author
//		comment.setString(factory.createRichTextString(commentText));
//		//		comment.setAuthor(author);
//
//		cell.setCellComment(comment);
//	}
}
