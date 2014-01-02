package com.jluzon.billboards.logger

import java.io.PrintWriter
import java.io.File
import java.util.Date

object Logger {
	private final val FILENAME = "log.txt";
	private final val FILE = new File(FILENAME);
	private final val WRITER = new PrintWriter(FILE);

	private def printToFile(message: String) {
		WRITER.append(new Date() + " " +  message);
		WRITER.flush();
	}
	def error(message: String) {
		printToFile("ERROR: " + message);
	}
	def debug(message: String) {
		printToFile("DEBUG: " + message);
	}
	def info(message: String) {
		printToFile("INFO: " + message);
	}
	def warning(message: String) {
		printToFile("WARNING: " + message);
	}
}