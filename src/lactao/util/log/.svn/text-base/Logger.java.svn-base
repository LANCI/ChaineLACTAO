package lactao.util.log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {

	long count = 0;

	PrintWriter out = null;

	public Logger(String fileName) throws IOException {
		out = new PrintWriter(new FileWriter(fileName));
	}

	public void log(String s) {
		out.println(s);
		System.out.println(s);
		count++;
		if (count % 5 == 0)
			out.flush();
	}

	public void close() throws IOException {
		out.close();
	}
}
