package lactao.concordancier;

import java.util.List;

public abstract class Concordancier {
	protected String text = "";
	protected int nbUnitsBefore = 0;
	protected int nbUnitsAfter = 0;

	public Concordancier() {
	};

	public Concordancier(String text, int nbUnitsBefore, int nbUnitsAfter) {
		this.text = text;
		this.nbUnitsBefore = nbUnitsBefore;
		this.nbUnitsAfter = nbUnitsAfter;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getNbUnitsBefore() {
		return nbUnitsBefore;
	}

	public void setNbUnitsBefore(int nbUnitsBefore) {
		this.nbUnitsBefore = nbUnitsBefore;
	}

	public int getNbUnitsAfter() {
		return nbUnitsAfter;
	}

	public void setNbUnitsAfter(int nbUnitsAfter) {
		this.nbUnitsAfter = nbUnitsAfter;
	}

	public abstract List<String> getContexts(String target, boolean leftWordBoundary, boolean rightWordBoundary);
	public abstract List<String> getContexts(String target, boolean leftWordBoundary, boolean rightWordBoundary, boolean capitalizeMatch);
}
