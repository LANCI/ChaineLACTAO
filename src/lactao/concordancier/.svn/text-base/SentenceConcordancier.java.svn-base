package lactao.concordancier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SentenceConcordancier extends Concordancier {
	private static final String sentenceRE = "([^.!?\\r\\n]+[.!?\\r\\n]+\\s*)";

	private Matcher matcherBackward;
	private Matcher matcherForward;

	public SentenceConcordancier() {
		super();
	}

	public SentenceConcordancier(String text, int nbSentencesBefore, int nbSentencesAfter) {
		super(text, nbSentencesBefore, nbSentencesAfter);
		resetMatchers();
	}

	private void resetMatchers() {
		matcherBackward = Pattern.compile(
				"(?<=[.!?\\r\\n])" + sentenceRE + "{" + nbUnitsBefore + "}", Pattern.MULTILINE)
				.matcher(text);
		matcherForward = Pattern.compile(sentenceRE + "{" + nbUnitsAfter + "}", Pattern.MULTILINE)
				.matcher(text);
	}

	@Override
	public void setNbUnitsAfter(int nbUnitsAfter) {
		super.setNbUnitsAfter(nbUnitsAfter);
		resetMatchers();
	}

	@Override
	public void setNbUnitsBefore(int nbUnitsBefore) {
		super.setNbUnitsBefore(nbUnitsBefore);
		resetMatchers();
	}

	@Override
	public void setText(String text) {
		super.setText(text);
		resetMatchers();
	}

	private String getSentencesForward(int startPos) {
		if (startPos <= 0 || startPos >= text.length())
			return "";
		matcherForward.region(startPos, text.length());
		if (matcherForward.find())
			return matcherForward.group();
		else
			return (text.substring(startPos));
	}

	private String getSentencesBackward(int startPos) {

		if (startPos <= 0 || startPos >= text.length())
			return "";

		int pos = startPos;
		do {
			--pos;
			matcherBackward.region(pos, startPos);
			if (matcherBackward.find())
				return matcherBackward.group();
		} while (pos > 0);

		// never found, return whole text up to endPos
		return (text.substring(0, startPos));
	}

	public List<String> getContexts(String target, boolean leftWordBoundary, boolean rightWordBoundary) {
		return getContexts(target, leftWordBoundary, rightWordBoundary, false);
	}

	public List<String> getContexts(String target, boolean leftWordBoundary, boolean rightWordBoundary, boolean capitalizeMatch) {
		List<String> contexts = new ArrayList<String>();
		String lb = leftWordBoundary ? "\\b" : "";
		String rb = rightWordBoundary ? "\\b" : "";
		String p = "([^.!?\\r\\n]*)(" + lb + Pattern.quote(target) + rb
				+ ")([^.!?\\r\\n]*[.!?\\r\\n]+\\s*)";
		Matcher m = Pattern.compile(p,
				Pattern.MULTILINE + Pattern.UNICODE_CASE + Pattern.CASE_INSENSITIVE).matcher(text);

		while (m.find()) {
			String group = m.group(1)
					+ (capitalizeMatch ? m.group(2).toUpperCase(Locale.FRENCH) : m.group(2))
					+ m.group(3);
			String s = getSentencesBackward(m.start()) + group + getSentencesForward(m.end());
			contexts.add(s.trim());
		}
		return contexts;
	}
	
	public List<String> getContexts(Matcher m) {
		List<String> contexts = new ArrayList<String>();

		while (m.find()) {
			final String group = m.group(1) + m.group(2).toUpperCase(Locale.FRENCH) + m.group(3);
			final String s = getSentencesBackward(m.start()) + group + getSentencesForward(m.end());
			contexts.add(s.trim());
		}
		return contexts;
	}

}
