package com.jluzon.billboards.objects

import java.io.File

object PathFinder {
	private val path: String = {
		val f = new File(System.getProperty("java.class.path"));
		val dir = f.getAbsoluteFile().getParentFile().toString().split(";")(0);
		withCorrectEndSlash(dir);
}

def getPath: String = {
		path;
}

private def withCorrectEndSlash(str: String): String = {
		if(str.last == '\\' || str.last == '/') {
			return str;
		}
		if(str.contains("\\")) {
			return str + '\\';
		}
		else return str + '/';
}
}