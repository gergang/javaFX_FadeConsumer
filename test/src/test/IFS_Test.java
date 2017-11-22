package test;

import java.io.BufferedReader;
import java.io.IOException;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSFileReader;

public class IFS_Test {

	public static void main(String[] args) {
		
		
		
		try {
			AS400 system = new AS400("sys_1");
			IFSFile file = new IFSFile(system, "/home/TEST_SYS_2");
			BufferedReader reader;
			reader = new BufferedReader(new IFSFileReader(file));
			// Read the first line of the file, converting characters.
			String line1 = reader.readLine();
			// Display the String that was read.
			System.out.println(line1);
			// Close the reader.
			reader.close();
		} catch (AS400SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}

}
