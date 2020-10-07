import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Script for converting DVBSRC files created with w_scan to ATSC format usable
 * with azap
 * 
 * usage: convertDVBSRCtoAZAP <inputfile> <outputfile>
 * 
 * @author Eric
 */
public class DVBtoZAP {

	// usage: convertDVBSRCtoAZAP <inputfile>
	// defaults dvb.conf as input file
	// output file is always channel.conf
	public static void main(String[] args) {

		File input;
		if(args.length > 0)
			input = new File(args[0]);
		else
			input = new File("dvb.conf");
		
		File output = new File("channels.conf");

		String name;
		String frequency;
		String type = "8VSB";
		String videoPID;
		String subChannel;
		String serviceID;

		try {
			BufferedReader br = new BufferedReader(new FileReader(input));
			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
			String currentLine = "";

			// Want to keep 1, 2 + 000, 6, last, 4 to the left of last
			while ((currentLine = br.readLine()) != null) {

				name = "";
				frequency = "";
				videoPID = "";
				subChannel = "";
				serviceID = "";

				// get name excludes ";(null)"
				int i = 0;
				while (currentLine.charAt(i) != ';')
					i++;

				// get name move to frequency
				name = currentLine.substring(0, i);
				name.replaceAll("\\s+","");
				i += 8;
				currentLine = currentLine.substring(i);
				i = 0;

				// get freqency adds 3 zeros
				while (currentLine.charAt(i) != ':')
					i++;

				// get frequency move to videoPID
				frequency = currentLine.substring(0, i) + "000";
				i += 9;
				currentLine = currentLine.substring(i);
				i = 0;

				// get videoPID
				while (currentLine.charAt(i) != ':')
					i++;

				// ignore =X values, we only want numbers before =
				if (currentLine.substring(0, i).contains("=")) {
					int e = 0;
					while (currentLine.charAt(e) != '=') {
						e++;
					}
					videoPID = currentLine.substring(0, e);
				} else
					videoPID = currentLine.substring(0, i);

				// move to end
				i = currentLine.length() - 1;

				// Find service id
				while (currentLine.charAt(i) != ':')
					i--;

				// Get service id
				serviceID = currentLine.substring(i + 1, currentLine.length());

				i -= 6;
				currentLine = currentLine.substring(0, i);
				i = currentLine.length() - 1;

				while (currentLine.charAt(i) != ':')
					i--;

				// Hope
				subChannel = currentLine.substring(i + 1, currentLine.length());

				// Write
				if (!videoPID.equals(""))
					bw.write(name + ":" + frequency + ":" + type + ":" + videoPID + ":" + serviceID + ":" + subChannel
							+ "\n");
			}

			br.close();
			bw.close();

		} catch (FileNotFoundException e) {
			System.out.println(input.toString() + " not found");
		} catch (IOException e) {
			System.out.println("File reading exception");
			e.printStackTrace();
		}
	}
}
