import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

//Eventually have a packet file with all this information as fields
public class WEAPacketDecoder {
	
	public void print(File file) {
		InputStream in;
		byte[] TS_Header = new byte[4];
		byte[] WEA_Header = new byte[8];
		byte[] WEA_INFO = new byte[4];
		byte[] WEA_MSGID = new byte[4];
		byte[] FIPS_LENGTH = new byte[2];
		byte[] FIPS_CODE = new byte[3];
		byte[] CONTENT = new byte[1];
		byte[] MSG_LENGTH = new byte[2];
		byte[] ASCII = new byte[1];
		byte[] CRC32 = new byte[4];
		
		byte[] SUBSEQUENT_HEADERS = new byte[12];
		
		String packetMessage = "";
		int bytesRead = 0;

		try {
			
			//Setup byte reader
			in = new FileInputStream(file);
			
			// Read 4 byte packet header
			in.read(TS_Header);

			// sync byte
			System.out.printf("Transport Stream Header\n-----------------------\nSync byte: %02X \n", TS_Header[0]);

			// TEI, PUSI, Priority (1,1,1)
			System.out.printf("TEI: %02X\n", TS_Header[1] >> 7 & 0b1);
			System.out.printf("PUSI: %02X\n", TS_Header[1] >> 6 & 0b1);
			System.out.printf("Priority: %02X\n", TS_Header[1] >> 5 & 0b1);

			// Get pid 13 bits
			System.out.printf("PID: 0x%02X%02X\n", TS_Header[1] >> 3, TS_Header[2]);

			// 8 TS_Header (2,2,4) Transport, Adaptation Field Control, Continuity Counter
			System.out.printf("TSC: %02X\n", TS_Header[3] >> 6 & 0b11);
			System.out.printf("AFC: %02X\n", TS_Header[3] >> 4 & 0b11);
			System.out.printf("CC: %02X\n", TS_Header[3] & 0b1111);

			// Read 8 byte WEA header
			in.read(WEA_Header);
			System.out.printf("\n\nWEA Header\n----------\nMessageID: %02X %02X %02X %02X\n", WEA_Header[0],
					WEA_Header[1], WEA_Header[2], WEA_Header[3]);
			System.out.printf("FragmentID: %02X %02X\n", WEA_Header[4], WEA_Header[5]);
			System.out.printf("Last FragmentID: %02X %02X\n", WEA_Header[6], WEA_Header[7]);
			System.out.println("Total packets: " + (int)((int)WEA_Header[6] + (int)WEA_Header[7]));
			
			// Read 4 bytes for WEA information
			in.read(WEA_INFO);
			System.out.printf("\n\nWEA INFO\n--------\nProtocol Version: %02X\n", WEA_INFO[0] >> 4 & 0b1111);
			System.out.printf("Priority Class: %02X\n", WEA_INFO[0] & 0b1111);
			System.out.printf("Message Type: %02X\n", WEA_INFO[1]);
			System.out.printf("Message Length: %02X\n", WEA_INFO[2]);
			System.out.printf("Reserved: %02X\n", WEA_INFO[3]);
			
			// Read 4 bytes for WEA MessageID
			in.read(WEA_MSGID);
			System.out.printf("\n\nWEA MSGID\n--------\nWEA MessageID: %02X %02X %02X %02X\n", WEA_MSGID[0], WEA_MSGID[1], WEA_MSGID[2], WEA_MSGID[3]);
			
			// Read 2 bytes for WEA MessageID
			in.read(FIPS_LENGTH);
			System.out.printf("\n\nFIPS\n--------\nFIPS Length: %02X %02X\n", FIPS_LENGTH[0],  FIPS_LENGTH[1]);
			
			//Read 3 bytes for FIPS codes if FIPS length larger than zero
			if(FIPS_LENGTH[0] << 8 + FIPS_LENGTH[1] > 0) {
				System.out.println("WEVE GOT FIPS CODES");
			}
			
			// Read 1 bytes for WEA MessageID
			in.read(CONTENT);
			System.out.printf("\n\nCONTENT\n--------\nCoding: %02X\n", CONTENT[0] >> 4 & 0b1111);
			System.out.printf("Type: %02X\n", CONTENT[0] & 0b1111);
			
			// Read 1 bytes for Message Length
			in.read(MSG_LENGTH);
			System.out.printf("\n\nMESSAGE\n--------\nLength: %02X %02X\n", MSG_LENGTH[0], MSG_LENGTH[1]);
			
			//Total number of bytes read up to now
			bytesRead = 25;
			
			//Capture remaining bytes
			while(bytesRead < 188) {
				in.read(ASCII);
				bytesRead++;
				packetMessage += (char)ASCII[0];
			}
			
			//Read subsequent packets
			for(int i = 0; i < ((int)WEA_Header[6] + (int)WEA_Header[7]); i++) {
				
				//Throw away header info for packets after 1st
				//MPEG is 4 bytes, WEA is 8
				in.read(SUBSEQUENT_HEADERS);
				
				//reset bytes read (4+8)
				bytesRead = 12;
				
				//Read more ASCII XML
				while(bytesRead < 188) {
					in.read(ASCII);
					bytesRead++;
					
					//00 Signifies end of XML message
					if(ASCII[0] == 0){
						bytesRead = 188;
						in.read(CRC32);
						System.out.printf("\n\nEND OF TRANSMISSION\n-------\nCRC32: %02X %02X %02X %02X\n\n", CRC32[0], CRC32[1], CRC32[2], CRC32[3]);
					}
					
					packetMessage += (char)ASCII[0];
				}
			}
			
			System.out.println("XML Message\n-----------\n" + packetMessage);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
