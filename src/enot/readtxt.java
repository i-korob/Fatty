package enot;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class readtxt {

	public static int readFile(String message){

		String fileName = message;
		int answer = 0;
		try (Scanner scanner = new Scanner(new File(fileName))) {
			scanner.useDelimiter("[\n\r]");
			while (scanner.hasNext()){
				//System.out.println(scanner.nextLine());
				answer = memory.IncomigMessage(scanner.next().replaceAll("[\n\r]", ""));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return answer;
	}
	
}
