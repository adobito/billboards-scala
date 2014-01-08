package com.jluzon.billboards.logger

import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import com.jluzon.billboards.objects.PathFinder
import java.io.BufferedWriter
import java.util.Date

object Logger {
	private final val PATH = PathFinder.getPath;
	private final val FILENAME = "log.txt";
	private final val FILE = new File(PATH + FILENAME);
	private final val WRITER = new BufferedWriter(new FileWriter(FILE,true));
	private var printlineEnabled = false;

	private def printToFile(message: String) {
		WRITER.append(new Date() + " " +  message +"\r\n");
		if(printlineEnabled)  {
			println(message);
		}
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
	def enablePrintln() {
		printlineEnabled = true;
	}
	def disablePrintln() {
		printlineEnabled = false;
	}
}