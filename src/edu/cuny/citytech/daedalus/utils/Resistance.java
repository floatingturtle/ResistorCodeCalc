/**
 * A static utility class to perform operations related to resistances.
 * 
 * @author Jason Warren
 * 
 * 
 */

package edu.cuny.citytech.daedalus.utils;

import java.math.BigDecimal;
import java.math.MathContext;

public class Resistance {

	private static final int KILO = 1000;
	private static final int MEGA = 1000000;
	private static final char OMEGA = '\u03A9';
	private static final String MULTIPLIERS = "[kM]?";
	private static final String ONE_TO_NINE = "^[1-9](([.][0-9])0*)?"+ MULTIPLIERS + OMEGA +"?$";
	private static final String GT_EQ_TEN = "^[1-9]\\d0*(\\.0)?0*" + MULTIPLIERS + OMEGA + "?$";
	private static final String LESS_THAN_ONE = "^0?\\.0*[1-9]\\d?0*" 
												+ MULTIPLIERS + OMEGA +"?$";
	private static final MathContext PRECISION = new MathContext(2);
	

	/**
	 * Parses a resistance string and returns the plain number resistance value that corresponds
	 * with the string.
	 * 
	 * 
	 * @param resistance
	 *            A string representing the resistance value. This may be purely
	 *            numerical or contain SI prefixes (ie. 33k). The final
	 *            character in the string can be the char '\u03A9' to indicate
	 *            units of ohms, or it may be omitted.
	 * 
	 * @return The value of the resistance or -1 if the string does not
	 *         represent a valid value. note that this function does NOT check to see if
	 *         resistance is in the valid range of a 3 band color code (.1ohm to 99Gohm).
	 *         The best way to do that is by calling getMultiplier() and checking whether it is
	 *         > -2 and < 10.
	 */
	public static BigDecimal parse(String resistance) {

		BigDecimal rValue;
		BigDecimal multiplier;

		if(isValid(resistance)){
			resistance = removeOmega(resistance);
			multiplier = valueOfMultiplier(resistance);
			resistance = removeMultiplier(resistance);
			rValue = new BigDecimal(resistance);
		}
		
		else{
			rValue = new BigDecimal(-1);
			multiplier = new BigDecimal(1);
		}

		return rValue.multiply(multiplier, PRECISION);
	}
	
	/**
	 * Determines if the string is one of the preferred values per decade.
	 * 
	 * @param resistance	The string whose value is to be checked against the standard.
	 * 						This string can be in engineering notation.
	 * 
	 * @return				TRUE if the first two non-zero significant digits of resistance
	 * 						are 10, 12, 15, 18, 22, 27, 33, 39, 47, 56, 68, 82 or resistance = 1
	 */
	public static boolean isStandard(String resistance){
		boolean isStandard = false;
		String s;
		char first;
		char second;
		
		if(isValid(resistance)){
			first = Character.forDigit(getFirstSigDigit(resistance),10);
			second = Character.forDigit(getSecondSigDigit(resistance), 10);
			s = new String(new char[]{first,second});
			if(		s.equals("10") || s.equals("12") || s.equals("15") || s.equals("18") ||
					s.equals("22") || s.equals("27") || s.equals("33") || s.equals("39") ||
					s.equals("47") || s.equals("56") || s.equals("68") || s.equals("82")){
				isStandard = true;
			}
		}
		
		return isStandard;
	}

	/**
	 * Removes the multiplier from the end of the string.
	 * 
	 * @param resistance	The resistance string. The final character in this string must
	 * 						be 'k' or 'M'.
	 * 
	 * @return				The resistance string without the trailing 'k' or 'M'.
	 */
	private static String removeMultiplier(String resistance) {
		int length = resistance.length();
		char c = resistance.charAt(length-1);
		if(c == 'k' || c == 'M'){
			resistance = resistance.substring(0,length-1);
		}
		return resistance;
	}

	
	/**
	 * Returns the value of the trailing multiplier.
	 * 
	 * @param resistance	Resistance string with a trailing 'k' or 'M'
	 * @return				1,000 if the last character in resistance == 'k' or
	 * 						1,000,000 if the last character in resistance == 'M'
	 */
	private static BigDecimal valueOfMultiplier(String resistance) {
		BigDecimal multiplier = new BigDecimal(1);
		int length = resistance.length();
		if(resistance.charAt(length-1) == 'k'){
			multiplier = new BigDecimal(KILO);
		}
		
		else if(resistance.charAt(length-1) == 'M'){
			multiplier = new BigDecimal(MEGA);
		}
		
		return multiplier;
	}

	/**
	 * Removes the last character in the string if the last character == '\u03A9'
	 * @param characters
	 * @return
	 */
	private static String removeOmega(String characters) {

		int lengthWithoutOmega;

		if (characters.charAt(characters.length() - 1) == OMEGA) {
			lengthWithoutOmega = characters.length() - 1;
		}

		else {
			lengthWithoutOmega = characters.length();
		}
		return characters.substring(0, lengthWithoutOmega);
	}
	
	/**
	 * Determines if resistance can be represented using two significant digits and a power of
	 * 10.
	 * 
	 * @param resistance	Any string to be checked against.
	 * @return				TRUE if resistance matches regexpression:
	 * 						"^[1-9](([.][0-9])0*)?[kM]?\u03A9?$
	 * 						|^[1-9]\\d0*(\\.0)?0*[kM]?\u03A9?$
	 * 						|^0?\\.0*[1-9]\\d?0*[kM]?\u03A9?$";
	 * 
	 */
	public static boolean isValid(String resistance){
		
		boolean matches = resistance.matches(ONE_TO_NINE) || resistance.matches(GT_EQ_TEN) ||
		resistance.matches(LESS_THAN_ONE);
		
		return matches;
	}
	
	/**
	 * Determines the exponent 'x' such that resistance / (10^x) results in two significant
	 * digits.
	 * 
	 * @param resistance	The numerical input string.
	 * @return				The exponent such that resistance / 10^x results in two sig figures
	 */
	public static int getMultiplier(String resistance) {
		int exponent = -3;
		BigDecimal resistanceValue = Resistance.parse(resistance);
		BigDecimal divisor = new BigDecimal(Math.pow(10, exponent));
		while (resistanceValue.divide(divisor,PRECISION).compareTo(new BigDecimal(100)) >= 0) {
			exponent++;
			divisor = new BigDecimal(Math.pow(10, exponent));
		}
		
		return exponent;
	}

	/**
	 * Returns the first significant digit of resistance.
	 * 
	 * @param resistance	The numerical resistance which to take the digit from.
	 * @return				The first significant digit of resistance.
	 */
	public static int getFirstSigDigit(String resistance) {
		int firstSignificantDigit;
		int exponent = getMultiplier(resistance);
		BigDecimal resistanceValue = Resistance.parse(resistance);
		BigDecimal divisor = new BigDecimal(Math.pow(10, exponent + 1));
		firstSignificantDigit = (resistanceValue.divide(divisor,PRECISION)).intValue();
		
		return firstSignificantDigit;
	}

	/**
	 * Returns the second significant digit of resistance.
	 * 
	 * @param resistance	The numerical resistance which to take the digit from.
	 * @return				The second significant digit of resistance.
	 */
	public static int getSecondSigDigit(String resistance) {
		int firstSignificantDigit = getFirstSigDigit(resistance);
		int secondSignificantDigit;
		int exponent = getMultiplier(resistance);
		BigDecimal resistanceValue = Resistance.parse(resistance);
		BigDecimal divisor = new BigDecimal(Math.pow(10, exponent));
		divisor = new BigDecimal(Math.pow(10,  exponent));
		secondSignificantDigit = resistanceValue.divide(divisor,PRECISION).subtract(
				new BigDecimal(10 * firstSignificantDigit)).intValue();
		
		return secondSignificantDigit;
	}
}
