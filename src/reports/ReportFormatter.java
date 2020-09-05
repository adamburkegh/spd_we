package reports;

public class ReportFormatter{

	protected StringBuffer reportBuffer = new StringBuffer();
			
	public void startReport() {
	}
	
	public void reportStartLine() {
	}

	public void reportCellBreak() {
		reportBuffer.append(" | ");
	}
	
	public void reportNewline() {
		reportBuffer.append(SPNDiscoverReporter.LINE_SEP);
	}
	
	public void reportHeader(String ...cells ) {
		reportCells(cells);
	}
	
	public void reportCells(String ... cells ) {
		for (String cell: cells) {
			reportBuffer.append(cell);
			reportCellBreak();
		}		
	}
	
	public void reportCells(long ... cells) {
		for (long cell: cells) {
			reportCells(String.valueOf(cell) );
		}
	}

	
	public void endReport(){
		
	}
	
	public String format() {
		return reportBuffer.toString();
	}

			
}