package reports;

import java.text.DecimalFormat;
import java.util.Set;
import java.util.TreeSet;

import au.edu.qut.pm.spn_discover.Measure;
import au.edu.qut.pm.spn_discover.RunState;
import au.edu.qut.pm.spn_discover.StochasticNetLogMiner;

public class SPNDiscoveryPaperReporter extends SPNDiscoverReporter{

	private static final Set<Measure> INCLUDED_MEASURES;
	
	static {
		INCLUDED_MEASURES = new TreeSet<>();
		registerMeasures();
	}

	private static void registerMeasures() {
		INCLUDED_MEASURES.add(Measure.MODEL_EDGE_COUNT);
		INCLUDED_MEASURES.add(Measure.MODEL_ENTITY_COUNT);
		INCLUDED_MEASURES.add(Measure.ENTROPY_PRECISION);
		INCLUDED_MEASURES.add(Measure.ENTROPY_RECALL);
		INCLUDED_MEASURES.add(Measure.EARTH_MOVERS_LIGHT_COVERAGE);
	}
	
	public SPNDiscoveryPaperReporter(String reportDir) {
		super(reportDir, new LatexTableFormatter(), System.out );
	}

	protected void outputReports() {
		reportFormatter.startReport();
		final int extraCols = 5;
		String[] headers = new String[INCLUDED_MEASURES.size() + extraCols];
		headers[0] = "Artifact Creator"; 
		headers[1] = "Miner algo";
		headers[2] = "Miner t"; headers[3] = "Estimator t";
		headers[4] = "Log"; 
		int i=extraCols;
		for (Measure measure: INCLUDED_MEASURES) {
			headers[i] = measure.getText();
			i++;
		}
		reportFormatter.reportHeader(headers);
		for ( ReportStats reportStats: statsDb.keySet() ) {
			if (reportStats.minerAlgo.equals(reportStats.artifactCreatorId) ) {					
				StochasticNetLogMiner minerAlgo = MINER_MAP.get( reportStats.minerAlgo );
				if (minerAlgo != null && !minerAlgo.isStochasticNetProducer()) {
					continue;
				}
			}
			reportFormatter.reportStartLine();
			reportFormatter.reportCells(
					rewriteArtifactCreator( reportStats.getArtifactCreator() ), 
					reportStats.minerAlgo 
					);		
			reportFormatter.reportCells(reportStats.minerDuration, 
					reportStats.estimatorDuration);
			reportFormatter.reportCells(rewriteLogName(reportStats.getInputLogFileName()) );
			Set<Measure> runMeasures = reportStats.getAllMeasures().keySet();
			for (Measure measure: INCLUDED_MEASURES) {
				if (runMeasures.contains(measure)) {
					DecimalFormat dFormat = new DecimalFormat("#.####");
					Number value = reportStats.getAllMeasures().get(measure);
					reportFormatter.reportCells(dFormat.format(value));
				}else {
					RunState state = reportStats.getState();
					if ( RunState.FAILED.equals( state ) || RunState.RUNNING.equals( state )) {
						reportFormatter.reportCells(state.toString());
					}else {
						reportFormatter.reportCells("    ");
					}
				}
			}
			reportFormatter.reportNewline();
		}
		reportFormatter.endReport();
		printStream.println(reportFormatter.format());
	}

	
}
