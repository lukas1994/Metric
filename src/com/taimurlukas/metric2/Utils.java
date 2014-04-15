package com.taimurlukas.metric2;

public class Utils {
	// x decimal places => decimalPlaces = 10^x
	static String googleScoreToScore(int gScore, int decimalPlaces) {
		String s = String.valueOf(gScore / decimalPlaces) + ".";
		String rest = String.valueOf(gScore % decimalPlaces);
		for (int i = 0; i < String.valueOf(decimalPlaces).length() - rest.length() - 1; i++)
			s += "0";
		s += rest;
		return s.substring(0, s.length()-1) + "%";
	}
}
