package ru.idealplm.specification.util;

import java.util.Date;

public class GeneralUtils {
	
	static public enum Month {
		JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEP, OCT, NOV, DEC, ßÍÂ, ÔÅÂ, ÌÀĞ, ÀÏĞ, ÌÀÉ, ÈŞÍ, ÈŞË, ÀÂÃ, ÑÅÍ, ÎÊÒ, ÍÎß, ÄÅÊ
	}

	// Ïàğñèò äàòó è âîçâğàùàåò å¸ â ôîğìàòå ÄÄ.ÌÌ.ÃÃ
		public static String parseDateFromTC(String tcDate) {
			//19 Èşë 2016 18:11
			String out = ""; // was " ", not ""
			if (tcDate.length() > 0) {
				String stDate = tcDate.substring(0, 2);
				out = stDate;

				String stMo = tcDate.substring(3, 6).toUpperCase();

				System.out.println("Date:"+tcDate);
				System.out.println("Looking for:"+stMo);
				Month mo = Month.valueOf(stMo);
				switch (mo) {
				case ßÍÂ:
				case JAN:
					out += ".01.";
					break;
				case ÔÅÂ:
				case FEB:
					out += ".02.";
					break;
				case ÌÀĞ:
				case MAR:
					out += ".03.";
					break;
				case ÀÏĞ:
				case APR:
					out += ".04.";
					break;
				case ÌÀÉ:
				case MAY:
					out += ".05.";
					break;
				case ÈŞÍ:
				case JUN:
					out += ".06.";
					break;
				case ÈŞË:
				case JUL:
					out += ".07.";
					break;
				case ÀÂÃ:
				case AUG:
					out += ".08.";
					break;
				case ÑÅÍ:
				case SEP:
					out += ".09.";
					break;
				case ÎÊÒ:
				case OCT:
					out += ".10.";
					break;
				case ÍÎß:
				case NOV:
					out += ".11.";
					break;
				case ÄÅÊ:
				case DEC:
					out += ".12.";
					break;
				}
				String year = tcDate.substring(9, 11);
				out += year;
			}
			return out;
		}
		
		public static String parseDateToTC(String date){
			//Date:DateTime {7/14/2016}
			String result = "";
			String day = date.substring(date.indexOf("{"), date.indexOf("/"));
			result+=day;
			int month = Integer.parseInt(date.substring(date.indexOf("/"),date.lastIndexOf("/")));
			result+=month;
			String year = date.substring(date.lastIndexOf("/"));
			result+=year;
			
			return result;
		}
		
		public static Date getDateFormSimpleString(String s_date){
			int year = Integer.parseInt(s_date.substring(s_date.lastIndexOf(".")+1, s_date.length()))+100;
			int month = Integer.parseInt(s_date.substring(s_date.indexOf(".")+1, s_date.lastIndexOf(".")));
			int date = Integer.parseInt(s_date.substring(0, s_date.indexOf(".")));
			return new Date(year, month, date);
		}
}
