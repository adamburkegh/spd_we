package au.edu.qut.pm.stochastic;

import java.util.Iterator;
import java.util.Map;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree.NodeType;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2AcceptingPetriNet;

import gnu.trove.map.hash.TIntObjectHashMap;
import qut.pm.spm.ppt.ProbProcessTree;
import qut.pm.spm.ppt.ProbProcessTreeFactory;
import qut.pm.spm.ppt.ProbProcessTreeNode;

/**
 * Stateful converter that keeps track of node through different representations.
 *
 * Example workflow
 * 		      PTree                   PetriNet             SPN
 * discovery ------> convertPTtoPN() --------> estimation -----> convertSPNToPPT = PPT 
 *                     
 * 
 * @author burkeat
 *
 */
public class ProcessTreeConverter {

	private EfficientTree tree;
	private AcceptingPetriNet net;
	private StochasticNet snet;
	private TIntObjectHashMap<Transition> treeNodeToTransition;
	private Map<DirectedGraphElement, DirectedGraphElement> netMap;

	
	public AcceptingPetriNet convertProcessTreeToPetriNet(EfficientTree tree) throws Exception{
		treeNodeToTransition = new TIntObjectHashMap<Transition>(10, 0.5f, -1);
		this.tree = tree;
		net = EfficientTree2AcceptingPetriNet.convert(tree, treeNodeToTransition);
		return net;
	}
	
	public ProbProcessTree convertStochasticPetriNetToProbProcessTree(
										StochasticNetDescriptor snet) 
	{
		int root = tree.getRoot();
		ProbProcessTree result = convertNode(root);		
		return result;
	}
	
	private ProbProcessTree convertNode(int node) {
		ProbProcessTreeNode operatorNode = null;
		Transition ptTran = null;
		double weight = 0d;
		NodeType nodeType = tree.getNodeType(node);
		switch(nodeType) {
		case activity:
			ptTran = treeNodeToTransition.get(node);
			weight = ((TimedTransition)netMap.get(ptTran)).getWeight();
			return ProbProcessTreeFactory.createLeaf(ptTran.getLabel() , 
												     weight);
		case concurrent:
			operatorNode = ProbProcessTreeFactory.createConcurrency();
			break;
		case loop:
			return convertLoop(node);			
		case sequence:
			operatorNode = ProbProcessTreeFactory.createSequence();
			break;
		case tau:
			ptTran = treeNodeToTransition.get(node);
			weight = ((TimedTransition)netMap.get(ptTran)).getWeight();
			return ProbProcessTreeFactory.createSilent(weight);
		case xor:
			operatorNode = ProbProcessTreeFactory.createChoice();
			break;
		default:
			throw new RuntimeException("Operator type " + nodeType + " not supported for node:" + node );		
		}
		// sequence, choice, conc
		Iterable<Integer> children = tree.getChildren(node);
		for (Integer childIndex: children) {
			ProbProcessTree child = convertNode(childIndex);
			operatorNode.addChild(child);
		}
		return operatorNode;
	}

	private ProbProcessTree convertLoop(int node) {
		Iterable<Integer> children = tree.getChildren(node);
		Iterator<Integer> iter = children.iterator();
		Integer firstChild = iter.next();
		ProbProcessTree doNode = convertNode(firstChild);
		ProbProcessTreeNode redoChoiceNode = ProbProcessTreeFactory.createChoice();
		while(iter.hasNext()) {
			ProbProcessTree child = convertNode(iter.next());
			redoChoiceNode.addChild( child );
		}
		ProbProcessTreeNode seqNode =  ProbProcessTreeFactory.createSequence();
		seqNode.addChild(doNode);
		seqNode.addChild(redoChoiceNode);
		ProbProcessTreeNode loopNode = ProbProcessTreeFactory.createLoop(seqNode.getWeight()+1.0);
		loopNode.addChild(seqNode);
		ProbProcessTreeNode topSeq = ProbProcessTreeFactory.createSequence();
		ProbProcessTree doNodeTop = convertNode(firstChild);
		topSeq.addChild(loopNode);
		topSeq.addChild(doNodeTop);
		return topSeq;
	}

	public void setNet(RetraceableStochasticNetCloner snc) {
		this.snet = snc;
		this.netMap = snc.getMapping();		
	}

	public StochasticNet getNet() {
		return snet;
	}
	
}
