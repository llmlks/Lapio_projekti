//Laila Melkas 40212358

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class CW{
	public static void main(String[] args) throws Exception {
		BufferedReader fh =
				new BufferedReader(new FileReader("iot.txt"));
		//First line contains the language names
		String s = fh.readLine(); 
		List<String> langs =
				new ArrayList<>(Arrays.asList(s.split("\t")));
		langs.remove(0);	//Throw away the first word - "week"
		TreeMap<String,HashMap<String,Integer>> iot = new TreeMap<>();
		while ((s=fh.readLine())!=null)
		{
			String [] wrds = s.split("\t");
			HashMap<String,Integer> interest = new HashMap<>();
			for(int i=0;i<langs.size();i++)
				interest.put(langs.get(i), Integer.parseInt(wrds[i+1]));
			iot.put(wrds[0], interest);
		}
		fh.close();
		HashMap<Integer,HashMap<String,HashMap<String,Integer>>>
			regionsByYear = new HashMap<>();
		for (int i=2004;i<2016;i++)
		{
			BufferedReader fh1 =
					new BufferedReader(new FileReader(i+".txt"));
			String s1 = fh1.readLine(); //Throw away the first line
			HashMap<String,HashMap<String,Integer>> year = new HashMap<>();
			while ((s1=fh1.readLine())!=null)
			{
				String [] wrds = s1.split("\t");
				HashMap<String,Integer>langMap = new HashMap<>();
				for(int j=1;j<wrds.length;j++){
					langMap.put(langs.get(j-1), Integer.parseInt(wrds[j]));
				}
				year.put(wrds[0],langMap);
			}
			regionsByYear.put(i,year);
			fh1.close();
		}
		
		// Data structures available are: langs, iot, regionsByYear
		// langs is a list of languages
		// iot is interest over time - a map from week to a map from languages
		// regionsByYear maps years onto a map of regions onto a map of languages

		
		//Set of all regions
		Set<String> countries = new HashSet<>();
		for (int year : regionsByYear.keySet()) {
			for (String country : regionsByYear.get(year).keySet()) {
				countries.add(country);
			}
		}
		
		//11) Which are the top 5 regions that demonstrated significant growth of 
		//interests in programming languages in general? 
		
		//I calculated both the overall change in interest comparing the first and 
		//last years as well as the change on average, which is slightly different 
		//due to all countries not having data for all of the years

		//Map from countries to a list of the years they appear in
		Map<String, ArrayList<Integer>> yearsOfCountries = new HashMap<>();
		for (String country : countries) {
			ArrayList<Integer> yoc = new ArrayList<>();
			for (int year : regionsByYear.keySet())
				if (regionsByYear.get(year).keySet().contains(country))
					yoc.add(year);
			Collections.sort(yoc);
			yearsOfCountries.put(country, yoc);
		}
		
		//A map from country to a map from pairs of consecutive years
		//to change in interest in programming languages in those years
		HashMap<String, TreeMap<String, Integer>> yearlyChangeByCountry = 
				new HashMap<>();
		
		//Loop over countries for the changes in interest
		for (String country : countries) {
			//List of the years the country has data for
			ArrayList<Integer> yoc = yearsOfCountries.get(country);
			
			//Maps the pair of consecutive years to the change that occurred 
			//between them
			TreeMap<String, Integer> yearlyChange = new TreeMap<>();
			
			//Loop over years to get the change that occurred between each pair 
			//of consecutive years
			for (int i = 0; i < yoc.size() - 1; i++) {
				
				String changeTime = yoc.get(i) + "-" + yoc.get(i + 1);
				
				int interest1 = 0;//interest in the first year
				for (String l : langs) {
					interest1 += 
						regionsByYear.get(yoc.get(i)).get(country).get(l);
				}
				
				int interest2 = 0;//interest in the second year
				for (String l : langs) {
					interest2 += 
						regionsByYear.get(yoc.get(i + 1)).get(country).get(l);
				}				
				int change = interest2 - interest1;
				yearlyChange.put(changeTime, change);
			}
			yearlyChangeByCountry.put(country, yearlyChange);
		}
		
		//Maps the average of the yearly changes to country
		TreeMap<Double, String> avrgChangeByCountry = new TreeMap<>();
		
		//Loop over countries to calculate the average of the yearly changes
		for (String country : yearlyChangeByCountry.keySet()) {
			double amount = 0;
			double all = 0;
			for (String time : yearlyChangeByCountry.get(country).keySet()) {
				all += yearlyChangeByCountry.get(country).get(time);
				amount++;
			}
			double ave = all/amount;
			if (ave >= 0 || ave < 0)
				avrgChangeByCountry.put(ave, country);
		}
		
		//Maps countries to overall change in interest
		TreeMap<String, Integer> overallChange = new TreeMap<>();
		for (String country : countries) {
			List<Integer> yoc = yearsOfCountries.get(country);
			int interest1 = 0;
			int firstYear = yoc.get(0);
			for (String l : regionsByYear.get(firstYear).get(country).keySet()) {
				interest1 += regionsByYear.get(firstYear).get(country).get(l);
			}
			int interest2 = 0;
			int lastYear = yoc.get(yoc.size() - 1);
			for (String l : regionsByYear.get(lastYear).get(country).keySet()) {
				interest2 += regionsByYear.get(lastYear).get(country).get(l);
			}				
			overallChange.put(country, interest2 - interest1);
		}
		
		//Creating and sorting a list of values in overallChange
		List<Integer> overAll = new ArrayList<>(overallChange.values());
		Collections.sort(overAll);
		
		System.out.println("11) Which are the top 5 regions that demonstrated "
				+ "significant growth of interests in programming languages in"
				+ " general?");
		System.out.printf("%45s\n","Based on overall change");
		System.out.printf("%20s\t%20s\t%20s\n", "Country", "Overall change", 
				"Change on Average");

		List<String> cs = new ArrayList<>();

		//Loop to find the top 5 countries in order by the overall change
		for (int i = 0; i < 5; i++) {
			String country = "";
			
			//Loop over countries to find the one whose overall change value 
			//matches the element with index i in the list overAll
			for (String country1 : overallChange.keySet())
				
				if (overallChange.get(country1) == overAll.get(overAll.size() - i - 1) 
							&& !cs.contains(country1)) {
					country = country1;
					cs.add(country1);//List to help prevent duplicates
					break;//Stop looking once the country's been found
				}
			
			//Loop to find the change on average for the country
			double average = 0;
			for (double a : avrgChangeByCountry.keySet())
				if (avrgChangeByCountry.get(a).equals(country))
					average = a;
			
			System.out.printf("%20s\t%10d\t%17.2f\n", country, 
					overAll.get(overAll.size() - i - 1), average);
		}
		
		System.out.printf("\n%50s\n" ,"Based on average yearly change");
		System.out.printf("%20s\t%20s\t%20s\n", "Country", "Overall change", 
				"Change on Average");

		//Loop to find the top 5 countries in order by the average change
		double average = avrgChangeByCountry.lastKey();
		for (int i = 0; i < 5; i++) {
			if (i > 0)
				average = avrgChangeByCountry.lowerKey(average);
			String country = avrgChangeByCountry.get(average);

			System.out.printf("%20s\t%10d\t%17.2f\n", country, 
					overallChange.get(country), average);
		}				
		System.out.println("");

		//12) Which programming language set the record for losing the most 
		//interests over a 12 months period? When did this happen?
		
		//Here I interpreted 12 months as 52 weeks

		int deepestDecline = 0;
		String weekAndDecline = "";	
		//Loop over languages
		for (String lang : langs) {
			String wk = iot.firstKey();
			
			//Loop over weeks to check the decline from each week to 12 months 
			//(52 weeks) ahead
			for (int j = 0; j < iot.keySet().size() - 52; j++) {
				if (j > 0)
					wk = iot.higherKey(wk);
				int first = iot.get(wk).get(lang);
				String wk2 = wk;
				for (int i = 0; i < 52; i++){
					wk2 = iot.higherKey(wk2);
				}
				int decline = iot.get(wk2).get(lang) - first;
				
				//Check if the integer decline is smaller than the one stored
				//in deepestDecline
				if (decline < deepestDecline) {
					deepestDecline = decline;
					weekAndDecline = lang + " had the deepest decline over 12"
							+ " months ("+ deepestDecline + "), and it " +
							"happened between weeks " + wk + " and " + wk2;
				}
			}
		}
		
		System.out.println("12) Which programming language set the record "
				+ "for losing the most interests over a "
				+ "12 months period? When did this happen?");
		System.out.println(weekAndDecline);
		System.out.println("");
		
		//13) Languages popular at University may be higher in September and 
		//October - this means the Sept/Oct average is higher than the whole 
		//year average. Show this trend for the language Java
		
		//I interpreted the week which contained the date 01.09 to be the 
		//first week of the academic year, even when that date fell on the 
		//weekend. Same for the last week of October, so whichever week
		//31.10 was on
		
		//A list of the weeks academic years start in
		ArrayList<String> yearlyStart = new ArrayList<>();
		
		//Maps for each year the starting week of September to the last week
		//of October
		TreeMap<String, String> septToOct = new TreeMap<>();
		for (String wk : iot.keySet()) {
			String [] date = wk.split("-");
			for (int i = 0; i < date.length; i++) {
				date[i] = date[i].trim();
			}
			if ((date[1].equals("09") && date[2].equals("01")) 
					|| (date[1].equals("08") 
					&& Integer.parseInt(date[2]) >= 26))
				yearlyStart.add(wk);
			if (date[1].equals("10") && Integer.parseInt(date[2]) >= 25)
				septToOct.put(yearlyStart.get(yearlyStart.size() - 1), wk);
		}
		System.out.println("13) Languages popular at University may be higher"
				+ " in September and October. "
				+ "Show this trend for the language Java.\n");
		System.out.printf("%7s\t%11s\t%8s\n", "Ac Year", "Sep/Oct", "All Year");

		//Maps the starting week of each academic year to the weekly average
		//interest in Java in September and October
		HashMap<String, Double> soAve = new HashMap<>();
		
		//Loop to calculate the weekly average interest in Java Sept to Oct
		for (String wk : septToOct.keySet()) {
			double sepoct = 0;
			int seoc = 0;
			String wk2 = wk;
			while (!wk.equals(septToOct.get(wk2))) {
				sepoct += iot.get(wk).get("java");
				seoc++;
				wk = iot.higherKey(wk);
			}
			soAve.put(wk2, sepoct / seoc);
		}
		
		//Loops over academic years to calculate the weekly average of 
		//interest in Java for each year
		for (int i = 0; i < yearlyStart.size(); i++) {
			int nextYear = 
					(Integer.parseInt(yearlyStart.get(i).substring(2,4)) + 1);
			String year = yearlyStart.get(i).substring(0,4) + "/" + nextYear;
			if (nextYear < 10)
				year = yearlyStart.get(i).substring(0,4) + "/0" + nextYear;
			int acc = 0;
			double all = 0;
			String wk = yearlyStart.get(i).trim();
			String wk2 = wk; //Variable to store the start of the academic year
			if (i < yearlyStart.size() - 1) {
				//Loop to add up all the weekly interests in Java and increment 
				//the accumulator accordingly
				while (!wk.equals(yearlyStart.get(i + 1))) {
					all += iot.get(wk).get("java");
					acc++;
					wk = iot.higherKey(wk);
				} 
			} else //Loop for the last year, as it isn't whole
				while (!wk.equals(iot.lastKey())) {
					all += iot.get(wk).get("java");
					acc++;
					wk = iot.higherKey(wk);
				} 				
			double aveYear = all / acc; //Average for the year

			System.out.printf("%7s\t%10.2f\t%3.2f\n", year, soAve.get(wk2), 
					aveYear);
		}
		
		System.out.println("");
		
		//14) Which pair of languages is most strongly correlated week by week? 
		
		//Map from languages to the interest by the week
		Map<String, ArrayList<Integer>> listedInterest = new HashMap<>();
		
		for (String lang : langs) {
			ArrayList<Integer> interests = new ArrayList<>();
			for (String wk : iot.keySet())
				interests.add(iot.get(wk).get(lang));
			listedInterest.put(lang, interests);
		}
		
		//Map from each pair of languages to their weekly correlation
		TreeMap<String, Double> pearsons = new TreeMap<>();
		
		//Looping over languages in pairs, so that each pair only gets
		//looped over once
		for (int i = 0; i < langs.size(); i++) {
			ArrayList<Integer> first = listedInterest.get(langs.get(i));
			int size = first.size();
			for (int j = langs.size() - 1; j > i; j--) {
				ArrayList<Integer> second = listedInterest.get(langs.get(j));
				
				//Sum of the products of the interests per week
				double sumofprod = 0;
				
				//Sums of weekly interests for both languages
				double sumoffirst = 0;
				double sumofsecond = 0;
				
				//Loop over the weeks to get the 3 variables above
				for (int wk = 0; wk < size; wk++) {
					sumofprod += first.get(wk)*second.get(wk);
					sumoffirst += first.get(wk)*first.get(wk);
					sumofsecond += second.get(wk)*second.get(wk);
				}
				
				//Formula for calculating Pearson's product-moment coefficient
				double pearson = (sumofprod - (size * mean(first) * 
						mean(second))) / (Math.sqrt(sumoffirst - size * 
						mean(first) * mean(first)) * Math.sqrt(sumofsecond - 
						size * mean(second) * mean(second)));
				String output = langs.get(i) + " and " + langs.get(j) + 
						" (" + pearson + ")";
				pearsons.put(output, pearson);
			}
		}
		
		System.out.println("14) Which pair of languages is most strongly "
				+ "correlated week by week?");
		double corr = 0;
		String output = "";
		
		//Loop over the pearson's coefficients for each language pair to find
		//the most strongly correlating pair
		for (String o : pearsons.keySet())
			if (pearsons.get(o) > corr) {
				corr = pearsons.get(o);
				output = o;
			}
		System.out.println("The two most strongly correlating languages are " 
			+ output + "\n");
			
		//15) Which language had the deepest continuous decline and when? 

		//I found the deepest (not longest) continuous decline, counting in
		//weeks when the change was non-positive (i.e. also when change was 0)
		
		//A list of weeks
		List<String> weeklist = 
				Collections.list(Collections.enumeration(iot.keySet()));
		
		//A map from languages to a map from the deepest decline to the weeks
		//between which that happened 
		Map<String, TreeMap<Integer, String>> declineByLangs = new HashMap<>();
		
		for (String l : langs) {
			
			//A map from the deepest decline to the weeks between which that happened
			TreeMap<Integer, String> declineTime = new TreeMap<>();
			
			//Looping over the indexes of the list of weeks
			for (int i = 0; i < weeklist.size() - 1; i++) {
				int decline = 0;
				int change = 0;
				int j = i;
				
				//Adding the difference between consecutive weeks (if non-positive)
				//to the variable decline
				while (change <= 0) {
					decline += change;
					change  = iot.get(weeklist.get(j + 1)).get(l) - 
							iot.get(weeklist.get(j)).get(l);
					j++;
					
					//Break if next iteration would be out of index
					if (j >= weeklist.size() - 1) 
						break;
				}
				
				String startend = " between " + weeklist.get(i) + 
							" and " + weeklist.get(j - 1);
				if (!declineTime.containsKey(decline))
					declineTime.put(decline, startend);
			}
			declineByLangs.put(l, declineTime);
		}
		
		//Loop to find the deepest decline
		int steep = 0;		
		for (String l : declineByLangs.keySet())
			if (steep > declineByLangs.get(l).firstKey()) {
				steep = declineByLangs.get(l).firstKey();
				output = l + " had the deepest continuous decline (" + steep + ")"
						+ "" + declineByLangs.get(l).get(steep);
			}
		System.out.println("15) Which language had the deepest continuous "
				+ "decline and when?");
		System.out.println(output);
	}
	
	
	//Method for calculating the average of a list of integers
	public static double mean(ArrayList<Integer> al) {
		double sum = 0;
		for (int i : al) {
			sum += i;
		}
		return sum / al.size();
	}
}


//OUTPUT:

//11) Which are the top 5 regions that demonstrated significant growth of interests in programming languages in general?
//                      Based on overall change
//             Country	      Overall change	   Change on Average
//               China	       394	            35.82
//          Bangladesh	        34	             3.40
//             Bolivia	        26	             2.36
//            Ethiopia	        20	             2.86
//               Nepal	        20	             3.33

//                    Based on average yearly change
//             Country	      Overall change	   Change on Average
//               China	       394	            35.82
//          Bangladesh	        34	             3.40
//               Nepal	        20	             3.33
//            Ethiopia	        20	             2.86
//             Bolivia	        26	             2.36

//12) Which programming language set the record for losing the most interests over a 12 months period? When did this happen?
//java had the deepest decline over 12 months (-20), and it happened between weeks 2004-02-29 - 2004-03-06 and 2005-02-27 - 2005-03-05

//13) Languages popular at University may be higher in September and October. Show this trend for the language Java.

//Ac Year	    Sep/Oct	All Year
//2004/05	     85.11	78.12
//2005/06	     68.44	63.10
//2006/07	     56.89	55.21
//2007/08	     51.56	48.34
//2008/09	     45.63	42.62
//2009/10	     39.63	37.25
//2010/11	     35.11	33.96
//2011/12	     34.67	33.65
//2012/13	     35.00	34.42
//2013/14	     34.75	34.00
//2014/15	     32.88	30.94

//14) Which pair of languages is most strongly correlated week by week?
//The two most strongly correlating languages are java and c++ (0.9849537649134632)

//15) Which language had the deepest continuous decline and when?
//java had the deepest continuous decline (-16) between 2007-11-18 - 2007-11-24 and 2007-12-30 - 2008-01-05

