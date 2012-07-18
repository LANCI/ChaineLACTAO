package lactao.corpuses;

import java.sql.Date;

public class Corpus {
	public String source;
	public Date startDate;
	public Date endDate;
	public String label;

	public Corpus(String source, Date startDate, Date endDate, String label) {
		this.source = source;
		this.startDate = startDate;
		this.endDate = endDate;
		this.label = label;
	}
}
