import java.util.Scanner;

class Rc2Cipher{
	public static void main(String[] args) {
		Rc2 cipher = new Rc2();
		Scanner scanner = new Scanner(System.in);
		byte choice = 0;
		boolean loop = true;
		while (loop){
			System.out.println("");
			System.out.println("Welcome to RC2 Cipher:");
			System.out.println("");
			System.out.println("1-Encrypt a file.");
			System.out.println("2-Decrypt an encrypted File.");
			System.out.println("");
			System.out.print("[1/2]:	");
			choice = scanner.nextByte();
			if (choice == 1 || choice ==2 ) {
				cipher.init(choice);
				loop=false;
			}
		}
	}
}