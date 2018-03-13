import java.util.*;
import java.lang.Math;

/**
 * Fill in the implementation details of the class DecisionTree using this file.
 * Any methods or secondary classes that you want are fine but we will only
 * interact with those methods in the DecisionTree framework.
 * 
 * You must add code for the 1 member and 4 methods specified below.
 * 
 * See DecisionTree for a description of default methods.
 */
public class DecisionTreeImpl extends DecisionTree {
	private DecTreeNode root;
	// ordered list of class labels
	private List<String> labels;
	// ordered list of attributes
	private List<String> attributes;
	// map to ordered discrete values taken by attributes
	private Map<String, List<String>> attributeValues;
	// map for getting the index
	private HashMap<String, Integer> label_inv;
	private HashMap<String, Integer> attr_inv;

	/**
	 * Answers static questions about decision trees.
	 */
	DecisionTreeImpl() {
		// no code necessary this is void purposefully
	}

	/**
	 * Build a decision tree given only a training set.
	 * 
	 * @param train:
	 *            the training set
	 */
	DecisionTreeImpl(DataSet train) {

		this.labels = train.labels;
		this.attributes = train.attributes;
		this.attributeValues = train.attributeValues;
		root = buildTree(train.instances, this.attributes,null, majorityLabel(train.instances));
		// TODO: Homework requirement, learn the decision tree here
		// Get the list of instances via train.instances
		// You should write a recursive helper function to build the tree
		//
		// this.labels contains the possible labels for an instance
		// this.attributes contains the whole set of attribute names
		// train.instances contains the list of instances
	}
	
	public DecTreeNode buildTree(List<Instance> examples, List<String> attributes1,
			String defaultLab, String parentAttr) {
		if (examples.isEmpty()) {
			return new DecTreeNode(defaultLab, null, parentAttr, true);
		} else if (sameLabel(examples)) {
			return new DecTreeNode(examples.get(0).label, null, parentAttr, true);
		} else if (attributes1.isEmpty()) {
			return new DecTreeNode(majorityLabel(examples), null, parentAttr, true);
		}

		int bestAttr = 0;
		double bestGain = 0.0;

		for (int i = 0; i < attributes1.size(); i++) {
			double infoGain = 0.0;
			infoGain = InfoGain(examples,attributes1.get(i));
			if (infoGain > bestGain) {
				bestGain = infoGain;
				bestAttr = i;
			}
		}
		//Calculating the best infoGain

		String q = attributes1.get(bestAttr);
		DecTreeNode tree = new DecTreeNode(null, q, parentAttr, false);
		int numQ = this.getAttributeIndex(q);
		List<String> ca = new ArrayList<String>(attributes1);
		ca.remove(q);
		for (String vVal : attributeValues.get(q)) {
			List<Instance> v_ex = new ArrayList<Instance>();
			for (Instance example : examples) {
				if (example.attributes.get(numQ).equals(vVal)) {
					v_ex.add(example);
				}
			}
			DecTreeNode subtree;
			subtree = buildTree(v_ex, ca, majorityLabel(examples), vVal);
			
			tree.addChild(subtree);
		}

		return tree;
	}

	boolean sameLabel(List<Instance> instances) {
		// Suggested helper function
		// returns if all the instances have the same label
		// labels are in instances.get(i).label
		// TODO
		for (int i = 0; i < instances.size(); i++) {
			if (!instances.get(i).label.equals(instances.get(0).label)) {
				return false;
			}
		}
		return true;
	}

	String majorityLabel(List<Instance> instances) {
		// Suggested helper function
		// returns the majority label of a list of examples
		// TODO
		int bestCounter = 0;
		int index = 0;
		for (int i = 0; i < this.labels.size(); i++) {
			int counter = 0;
			for (int j = 0; j < instances.size(); j++) {
				if (instances.get(j).label.equals(this.labels.get(i))) {
					counter++;
				}
			}
			if (counter > bestCounter) {
				index = i;
				bestCounter = counter;
			}
		}
		return this.labels.get(index);

	}

	double entropy(List<Instance> instances) {
		double entropy = 0.0;
		int nsize = this.labels.size();
		//
		//ArrayList<Integer> list = new ArrayList<Integer>(Collections.nCopies(nsize, 0));
		int [] list = new int[nsize];
		
		for (int i = 0; i < nsize; i++) {
			for (int j = 0; j < instances.size(); j++) {
				if (instances.get(j).label.equals(this.labels.get(i))) {
					//list.set(i, list.get(i) + 1);
					list[i]++;
				}
			}
		}
		ArrayList<Double> proportion = new ArrayList<Double>(Collections.nCopies(nsize, 0.0));
		
		for (int i = 0; i < this.labels.size(); i++) {
			proportion.set(i, (double) list[i] / instances.size());
		}
		for (int i = 0; i < this.labels.size(); i++) {
			if (proportion.get(i).equals(0)) {
				entropy = entropy + 0;

			} else {
				entropy = entropy - proportion.get(i) * Math.log(proportion.get(i)) / Math.log(2);
			}
		}
		// Suggested helper function
		// returns the Entropy of a list of examples
		// TODO
		return entropy;
	}

	double conditionalEntropy(List<Instance> instances, String attr) {
		
		List<String> AttrList = attributeValues.get(attr);
		int[][] List = new int[AttrList.size()][this.labels.size()];
		// How to create the conditional Entropy?

		for (int i = 0; i < instances.size(); i++) {
			String tag = instances.get(i).attributes.get(getAttributeIndex(attr));
			for (int j = 0; j < AttrList.size(); j++) {
				if (tag.equals(AttrList.get(j))) {
					for (int k = 0; k < this.labels.size(); k++) {
						if (instances.get(i).label.equals(this.labels.get(k))) {
							List[j][k]++;
						}
					}
				}
			}
		}
		int[] rowSum = new int[AttrList.size()];
		for (int i = 0; i < AttrList.size(); i++) {
			for (int j = 0; j < this.labels.size(); j++) {
				rowSum[i] = rowSum[i] + List[i][j];
			}
		}
		// Store the proportion of attribute
		double[][] proportion = new double[AttrList.size()][this.labels.size()];
		// Store probability of each attribute(Storing 1 will be enough)
		double condEntropy = 0.0;
		for (int i = 0; i < AttrList.size(); i++) {
			double entropy = 0.0;
			for (int j = 0; j < this.labels.size(); j++) {
				if (rowSum[i] != 0) {
					proportion[i][j] = ((double) List[i][j]) / rowSum[i];
				} else {
					proportion[i][j] = 0.0;
				}
				if (proportion[i][j] != 0.0) {
					entropy = entropy
							- proportion[i][j] * (Math.log(proportion[i][j])) / Math.log(2);
				}

			}
			double probab = (double) rowSum[i] / instances.size();
			condEntropy = condEntropy + probab * entropy;

		}
		
		return condEntropy;
	}

	double InfoGain(List<Instance> instances, String attr) {
		// Suggested helper function
		// returns the info gain of a list of examples, given the attribute attr
		return entropy(instances) - conditionalEntropy(instances, attr);
	}

	@Override
	public String classify(Instance instance) {
		// TODO: Homework requirement
		// The tree is already built, when this function is called
		// this.root will contain the learnt decision tree.
		// write a recusive helper function, to return the predicted label of
		// instance

		return treeSearch(instance, this.root);
	}

	public String treeSearch(Instance instance, DecTreeNode node) {
		if (node.terminal) {
			return node.label;
		}
		int attrIndex = this.getAttributeIndex(node.attribute);
		String attr = "";
		for (int i = 0; i < node.children.size(); i++) {
			if (instance.attributes.get(attrIndex)
					.equals(node.children.get(i).parentAttributeValue)) {
				attr = treeSearch(instance, node.children.get(i));
			}
		}
		return attr;
	}

	@Override
	public void rootInfoGain(DataSet train) {
		this.labels = train.labels;
		this.attributes = train.attributes;
		this.attributeValues = train.attributeValues;
		for (int i = 0; i < this.attributes.size(); i++) {
			double infoGain1 = 0.0;
			infoGain1 = InfoGain(train.instances, this.attributes.get(i));
			System.out.format(this.attributes.get(i) +" "+ "%.5f\n", infoGain1);
		}
		// TODO: Homework requirement
		// Print the Info Gain for using each attribute at the root node
		// The decision tree may not exist when this funcion is called.
		// But you just need to calculate the info gain with each attribute,
		// on the entire training set.
	}

	@Override
	public void printAccuracy(DataSet test) {
		// TODO: Homework requirement
		// Print the accuracy on the test set.
		// The tree is already built, when this function is called
		// You need to call function classify, and compare the predicted labels.
		// List of instances: test.instances
		// getting the real label: test.instances.get(i).label
		double total = test.instances.size();
		double correct = 0;
		for (int i = 0; i < test.instances.size(); i++) {
			if (test.instances.get(i).label.equals(classify(test.instances.get(i)))) {
				correct++;
			}
		}
		System.out.format("%.5f\n", (correct / total));
	}

	@Override
	/**
	 * Print the decision tree in the specified format Do not modify
	 */
	public void print() {

		printTreeNode(root, null, 0);
	}

	/**
	 * Prints the subtree of the node with each line prefixed by 4 * k spaces.
	 * Do not modify
	 */
	public void printTreeNode(DecTreeNode p, DecTreeNode parent, int k) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < k; i++) {
			sb.append("    ");
		}
		String value;
		if (parent == null) {
			value = "ROOT";
		} else {
			int attributeValueIndex = this.getAttributeValueIndex(parent.attribute,
					p.parentAttributeValue);
			value = attributeValues.get(parent.attribute).get(attributeValueIndex);
		}
		sb.append(value);
		if (p.terminal) {
			sb.append(" (" + p.label + ")");
			System.out.println(sb.toString());
		} else {
			sb.append(" {" + p.attribute + "?}");
			System.out.println(sb.toString());
			for (DecTreeNode child : p.children) {
				printTreeNode(child, p, k + 1);
			}
		}
	}

	/**
	 * Helper function to get the index of the label in labels list
	 */
	private int getLabelIndex(String label) {
		if (label_inv == null) {
			this.label_inv = new HashMap<String, Integer>();
			for (int i = 0; i < labels.size(); i++) {
				label_inv.put(labels.get(i), i);
			}
		}
		return label_inv.get(label);
	}

	/**
	 * Helper function to get the index of the attribute in attributes list
	 */
	private int getAttributeIndex(String attr) {
		if (attr_inv == null) {
			this.attr_inv = new HashMap<String, Integer>();
			for (int i = 0; i < attributes.size(); i++) {
				attr_inv.put(attributes.get(i), i);
			}
		}
		return attr_inv.get(attr);
	}

	/**
	 * Helper function to get the index of the attributeValue in the list for
	 * the attribute key in the attributeValues map
	 */
	private int getAttributeValueIndex(String attr, String value) {
		for (int i = 0; i < attributeValues.get(attr).size(); i++) {
			if (value.equals(attributeValues.get(attr).get(i))) {
				return i;
			}
		}
		return -1;
	}
}
