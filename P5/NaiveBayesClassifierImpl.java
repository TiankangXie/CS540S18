import java.util.HashMap;
import java.util.Map;
import java.lang.Math;

/**
 * Your implementation of a naive bayes classifier. Please implement all four
 * methods.
 */

public class NaiveBayesClassifierImpl implements NaiveBayesClassifier {
	private Instance[] m_trainingData;
	private int m_v;
	private double m_delta;
	public int m_sports_count, m_business_count;
	public int m_sports_word_count, m_business_word_count;
	private HashMap<String, Integer> m_map[] = new HashMap[2];

	/**
	 * Trains the classifier with the provided training data and vocabulary size
	 */
	@Override
	public void train(Instance[] trainingData, int v) {
		// TODO : Implement
		// For all the words in the documents, count the number of occurrences.
		// Save in HashMap
		// e.g.
		// m_map[0].get("catch") should return the number of "catch" es, in the
		// documents labeled sports
		// Hint: m_map[0].get("asdasd") would return null, when the word has not
		// appeared before.
		// Use m_map[0].put(word,1) to put the first count in.
		// Use m_map[0].replace(word, count+1) to update the value
		m_trainingData = trainingData;
		m_v = v;
		m_map[0] = new HashMap<>();
		m_map[1] = new HashMap<>();
		documents_per_label_count(m_trainingData);
		for(Instance insta:trainingData) {
			if(insta.label.equals(Label.SPORTS)) {
				for(String wds:insta.words) {
					if(m_map[0].containsKey(wds)) {
						int tmp1 = m_map[0].get(wds);
						m_map[0].put(wds, tmp1+1);
					}else {
						m_map[0].put(wds, 1);
					}
				}
			}
			else if(insta.label.equals(Label.BUSINESS)) {
				for(String wds:insta.words) {
					if(m_map[1].containsKey(wds)) {
						int tmp2 = m_map[1].get(wds);
						m_map[1].put(wds, tmp2+1);
					}else {
						m_map[1].put(wds, 1);
					}
				}
			}
		}
		
	}

	/*
	 * Counts the number of documents for each label
	 */
	public void documents_per_label_count(Instance[] trainingData) {
		m_sports_count = 0;
		m_business_count = 0;
		for (Instance inst : trainingData) {
			if (inst.label.equals(Label.BUSINESS)) {
				m_business_count++;
			} else if (inst.label.equals(Label.SPORTS)) {
				m_sports_count++;
			}
		}
	}

	/*
	 * Prints the number of documents for each label
	 */
	public void print_documents_per_label_count() {
		System.out.println("SPORTS=" + m_sports_count);
		System.out.println("BUSINESS=" + m_business_count);
	}

	/*
	 * Counts the total number of words for each label
	 */
	public void words_per_label_count(Instance[] trainingData) {
		// TODO : Implement
		m_sports_word_count = 0;
		m_business_word_count = 0;
		for (Instance inst : trainingData) {
			if (inst.label.equals(Label.SPORTS)) {
				m_sports_word_count += inst.words.length;
			} else if (inst.label.equals(Label.BUSINESS)) {
				m_business_word_count += inst.words.length;
			}
		}
	}

	

	/*
	 * Prints out the number of words for each label
	 */
	public void print_words_per_label_count() {
		System.out.println("SPORTS=" + m_sports_word_count);
		System.out.println("BUSINESS=" + m_business_word_count);
	}

	/**
	 * Returns the prior probability of the label parameter, i.e. P(SPORTS) or
	 * P(BUSINESS)
	 */
	@Override
	public double p_l(Label label) {
		// TODO : Implement
		// Calculate the probability for the label. No smoothing here.
		// Just the number of label counts divided by the number of documents.
		double ret = (double) m_sports_count / (m_sports_count + m_business_count);
		if (label.equals(Label.SPORTS)) {
			return ret;
		} else {
			return (1 - ret);
		}
	}

	/**
	 * Returns the smoothed conditional probability of the word given the label,
	 * i.e. P(word|SPORTS) or P(word|BUSINESS)
	 */
	@Override
	public double p_w_given_l(String word, Label label) {
		double ret = 0;
		// TODO : Implement
		m_delta = 0.00001;
		// Calculate the probability with Laplace smoothing for word in
		// class(label)
		int sportsTotal = 0;
		int businessTotal = 0;
		// Word token of HAM
		for (Integer value : m_map[0].values()) {
			sportsTotal += value;
		}
		for (Integer value : m_map[1].values()) {
			businessTotal += value;
		}

		if (label.equals(Label.SPORTS)) {
			if (m_map[0].containsKey(word)) {
				Integer clSports = m_map[0].get(word);
				ret = (clSports + m_delta) / (m_v * m_delta + sportsTotal);
			} else {
				ret = (0 + m_delta) / (m_v * m_delta + sportsTotal);
			}
		}

		else if (label.equals(Label.BUSINESS)) {
			if (m_map[1].containsKey(word)) {
				Integer clBusiness = m_map[1].get(word);
				ret = (clBusiness + m_delta) / (m_v * m_delta + businessTotal);
			} else {
				ret = (0 + m_delta) / (m_v * m_delta + businessTotal);
			}

		}

		return ret;
	}

	/**
	 * Classifies an array of words as either SPORTS or BUSINESS.
	 */
	@Override
	public ClassifyResult classify(String[] words) {
		// TODO : Implement
		// Sum up the log probabilities for each word in the input data, and the
		// probability of the label
		// Set the label to the class with larger log probability
		ClassifyResult ret = new ClassifyResult();
		ret.label = Label.SPORTS;
		ret.log_prob_sports = 0;
		ret.log_prob_business = 0;
		
		ret.log_prob_sports = Math.log(p_l(Label.SPORTS));
		for(String wds:words) {
			ret.log_prob_sports += Math.log(p_w_given_l(wds,Label.SPORTS));
		}
		ret.log_prob_business = Math.log(p_l(Label.BUSINESS));
		for(String wds:words) {
			ret.log_prob_business += Math.log(p_w_given_l(wds,Label.BUSINESS));
		}
		if(ret.log_prob_business>ret.log_prob_sports) {
			ret.label = Label.BUSINESS;
		}
		return ret;
	}

	/*
	 * Constructs the confusion matrix
	 */
	@Override
	public ConfusionMatrix calculate_confusion_matrix(Instance[] testData) {
		// TODO : Implement
		// Count the true positives, true negatives, false positives, false
		// negatives
		int TP, FP, FN, TN;
		TP = 0;
		FP = 0;
		FN = 0;
		TN = 0;
		for (Instance insta : testData) {
			ClassifyResult clres = classify(insta.words);
			if (insta.label.equals(Label.BUSINESS)) {
				if (clres.label.equals(Label.BUSINESS)) {
					TN++;
				} else {
					FP++;
				}

			} else if (insta.label.equals(Label.SPORTS)) {
				if (clres.label.equals(Label.SPORTS)) {
					TP++;
				} else {
					FN++;
				}
			}
		}
		return new ConfusionMatrix(TP, FP, FN, TN);
	}

}
