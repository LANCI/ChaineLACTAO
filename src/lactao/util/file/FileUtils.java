package lactao.util.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;

public class FileUtils {

	public static String read(String fileName, String encoding) throws IOException {
		StringBuffer sb = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName),
				"Cp1252"));
		String s;
		while ((s = in.readLine()) != null) {
			sb.append(s);
			sb.append("\r\n");
		}
		in.close();
		return sb.toString();
	}

	public static String read(String fileName) throws IOException {
		return read(fileName, "Cp1252");
	}

	public static String read(File file) throws IOException {
		return read(file.getPath(), "Cp1252");
	}

	public static String read(File file, String encoding) throws IOException {
		return read(file.getPath(), encoding);
	}

	public static void write(String text, String fileName, String encoding) throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName), encoding)));
		out.print(text);
		out.close();
	}

	public static void write(String text, String fileName) throws IOException {
		write(text, fileName, "Cp1252");
	}

	public static void copyFile(File in, File out) throws IOException {
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			throw e;
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

	public static void copyFile(String in, String out) throws IOException {
		copyFile(new File(in), new File(out));
	}

}
