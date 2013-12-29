import java.io.PrintWriter
import java.io.File

object Logger {
	private final val FILENAME = "log.txt";
	private final val FILE = new File(FILENAME);
	private final val WRITER = new PrintWriter(FILE);

	def printToFile(message: String) {
		WRITER.write(message);
	}
	def error(message: String) {
		printToFile("ERROR: " + message);
	}
	def debug(message: String) {
		printToFile("DEBUG: " + message);
	}
	def verbose(message: String) {
		printToFile("VERBOSE: " + message);
	}
	def warning(message: String) {
		printToFile("WARNING: " + message);
	}
}