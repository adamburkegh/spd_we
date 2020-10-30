package org.processmining.stochasticweightestimation.plugins;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.ClassifierChooser;

import au.edu.qut.pm.spn_estimator.ActivityPairLHEstimator;
import au.edu.qut.pm.spn_estimator.ActivityPairRHEstimator;
import au.edu.qut.pm.spn_estimator.AlignmentEstimator;
import au.edu.qut.pm.spn_estimator.BillClintonEstimator;
import au.edu.qut.pm.spn_estimator.FrequencyEstimator;
import au.edu.qut.pm.spn_estimator.LogSourcedWeightEstimator;
import au.edu.qut.pm.spn_estimator.MeanScaledActivityPairRHEstimator;
import au.edu.qut.xes.helpers.DelimitedTraceToXESConverter;

/**
 *  
 * @author burkeat
 *
 */
public class EstimatorPluginConfiguration extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private static final String[] ESTIMATOR_LABELS;
	private static final LogSourcedWeightEstimator[] ESTIMATORS;

	private static final int DEFAULT_ESTIMATOR = 3;
	static {
		ESTIMATOR_LABELS = new String[6];
		ESTIMATOR_LABELS[0] = "Frequency Estimator";
		ESTIMATOR_LABELS[1] = "LH Activity-Pair Estimator";
		ESTIMATOR_LABELS[2] = "RH Activity-Pair Estimator";
		ESTIMATOR_LABELS[3] = "Scaled RH Activity-Pair Estimator";
		ESTIMATOR_LABELS[4] = "Fork Distributed Estimator";
		ESTIMATOR_LABELS[5] = "Alignment Estimator";
		ESTIMATORS = new LogSourcedWeightEstimator[6];
		ESTIMATORS[0] = new FrequencyEstimator();
		ESTIMATORS[1] = new ActivityPairLHEstimator();
		ESTIMATORS[2] = new ActivityPairRHEstimator();
		ESTIMATORS[3] = new MeanScaledActivityPairRHEstimator();
		ESTIMATORS[4] = new BillClintonEstimator();
		ESTIMATORS[5] = new AlignmentEstimator();
	}
	
	private JComboBox<String> estimatorComboBox ;

	public EstimatorPluginConfiguration(XLog log) {
		super(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		final JLabel estimatorLabel = new JLabel("Weight Estimator");
		constraints.gridx = 0; constraints.gridy = 0; constraints.ipadx = 10; constraints.anchor = GridBagConstraints.LINE_START;
		add(estimatorLabel, constraints);
		estimatorComboBox = new JComboBox<String>(ESTIMATOR_LABELS);
		constraints.gridx = 2; constraints.gridy = 0; constraints.anchor = GridBagConstraints.LINE_END;
		add(estimatorComboBox, constraints);
		final JLabel classifierLabel = new JLabel("Event Classifier");
		constraints.gridx = 0; constraints.gridy = 1; constraints.ipadx = 10; constraints.anchor = GridBagConstraints.LINE_START;
		add(classifierLabel, constraints);
		ClassifierChooser classifierChooser = new ClassifierChooser(log);
		constraints.gridx = 2; constraints.gridy = 1; constraints.anchor = GridBagConstraints.LINE_END;
		add(classifierChooser, constraints);
	}

	public LogSourcedWeightEstimator getEstimator() {
		int selection = estimatorComboBox.getSelectedIndex();
		if (selection >= 0) {
			return ESTIMATORS[selection];
		}
		return ESTIMATORS[DEFAULT_ESTIMATOR]; 
	}


	// Test method
	private static void createAndShowGUI() {
		JFrame frame = new JFrame("Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Create and set up the content pane.
		XLog log = new DelimitedTraceToXESConverter().convertTextArgs("a b","b c");
        JComponent newContentPane = new EstimatorPluginConfiguration(log);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
         //Display the window.
        frame.pack();
        frame.setVisible(true);
	}

	// Test method	
	public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}

	
}
