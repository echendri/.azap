
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

public class TSInterpreter {
	public static void main(String[] args) {
		
		WEAPacketDecoder weaPD = new WEAPacketDecoder();
		File input;

		if(args.length > 0)
			input = new File(args[0]);
		else
			input = new File("WEA.bin");
		
		//Sleep until file has data
		while(input.length() == 0) {
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
			}
		}
		
		weaPD.print(input);
		
		//Empty file Maybe (Rewrite to use RandomAccessFile to avoid conflicts)
		PrintWriter pw;
		try {
			pw = new PrintWriter(input);
			pw.close();
		} catch (FileNotFoundException e) {
		}		
	}
}
