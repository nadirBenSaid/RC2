import java.io.*;
import java.util.Scanner;

class Rc2{
	private int[] inputFile = null;
	private int[] outputFile = null;
	private byte[] secretKey = null;
	private boolean encrypting = true;
	private int counter = 0;
	private short keyBitLimit = 0;
	private int[] expandedKey = null;
	private int[][] blocks = null;
	private static final int[] SVECTOR = new int[] {1,2,3,5};
	private static final int[] TABLEPI = new int[] {217,120,249,196,25,221,181,237,40,233,253,121,74,160,216,157,198,126,55,131,43,118,83,142,98,76,100,136,68,139,251,162,23,154,89,245,135,179,79,19,97,69,109,141,9,129,125,50,189,143,64,235,134,183,123,11,240,149,33,34,92,107,78,130,84,214,101,147,206,96,178,28,115,86,192,20,167,140,241,220,18,117,202,31,59,190,228,209,66,61,212,48,163,60,182,38,111,191,14,218,70,105,7,87,39,242,29,155,188,148,67,3,248,17,199,246,144,239,62,231,6,195,213,47,200,102,30,215,8,232,234,222,128,82,238,247,132,170,114,172,53,77,106,42,150,26,210,113,90,21,73,116,75,159,208,94,4,24,164,236,194,224,65,110,15,81,203,204,36,145,175,80,161,244,112,57,153,124,58,133,35,184,180,122,252,2,54,91,37,85,151,49,45,93,250,152,227,138,146,174,5,223,41,16,103,108,186,201,211,0,230,207,225,158,168,44,99,22,1,63,88,226,137,169,13,56,52,27,171,51,255,176,187,72,12,95,185,177,205,46,197,243,219,71,229,165,156,119,10,166,32,104,254,127,193,173};
	private void setInputFile(){
		Scanner scanner = new Scanner(System.in);
		System.out.println("");
		System.out.println("Step 1: Enter path to input file:");
		String filePath = scanner.nextLine();
		File file = new File(filePath);
		FileInputStream stream = null;
		byte[] fileContent = null;
		try {
			stream = new FileInputStream(file);
			fileContent = new byte[(int)file.length()];
			stream.read(fileContent);
		}catch (FileNotFoundException e) {
			System.out.println("File not found" + e);
		}catch (IOException ioe) {
			System.out.println("Exception while reading file " + ioe);
		}finally {
			try {
				if (stream != null) {
					stream.close();
				}
			}
			catch (IOException ioe) {
				System.out.println("Error while closing stream: " + ioe);
			}
		}
		int[] tmp = null;
		if (this.encrypting) {
			tmp = new int[fileContent.length];
			for (int x=0; x<fileContent.length; x++) {
				tmp[x] = fileContent[x];	
			}
		}else{
			tmp = new int[fileContent.length/4];
			for (int x=0; x<fileContent.length/4; x++) {
				tmp[x] = ((fileContent[4*x]*16777216)&0xff000000);
				tmp[x] = tmp[x] + ((fileContent[4*x+1]*65536)&0x00ff0000);
				tmp[x] = tmp[x] + ((fileContent[4*x+2]*256)&0x00ff00);
				tmp[x] = tmp[x] + (fileContent[4*x+3]&0x00ff);
			}
		}
		this.inputFile = tmp;
	}
	private void writeOutputFile(){
		Scanner scanner = new Scanner(System.in);
		System.out.println("");
		System.out.println("Step 3: Enter path to save output file:");
		String filePath = scanner.nextLine();
		File file = new File(filePath);
		DataOutputStream stream = null;
		int[] output = this.outputFile;
		try {
		 stream = new DataOutputStream(new FileOutputStream(file));
			 for (int x=0; x<output.length; x++) {
			 	if (output[x]!=0x0000) {
			 		if (this.encrypting) {
			 			stream.writeInt(output[x]);
			 		}else{
			 			stream.writeByte(output[x]);
			 		}
			 	}
			 }
		}catch (FileNotFoundException e) {
			System.out.println("File not found" + e);
		}catch (IOException ioe) {
			System.out.println("Exception while reading file " + ioe);
		}finally {
			try {
				if (stream != null) {
					stream.close();
				}
			}
			catch (IOException ioe) {
				System.out.println("Error while closing stream: " + ioe);
			}
		}
	}
	private void setSecretKey(){
		boolean loop = true;
		Scanner scanner = new Scanner(System.in);
		while (loop) {
			System.out.println("");
			System.out.println("Step 2: Enter your secret key [Between 1-128 Characters]:");
			String key = scanner.nextLine();
			if (key.length()<128) {
				System.out.println("");
				System.out.println("**Note: Remember this key as it is used to both encrypt and decrypt your files.");
				this.secretKey = key.getBytes();
				this.keyBitLimit = (short) ((this.secretKey.length*8)-7);
				loop=false;
			}else {
				System.out.println("");
				System.out.println("Error: key size not fitting the conditions, it should be between 1-128 Characters.");
			}	
		}
	}
	private void setExpandedKey(){
		int[] exKey = new int[128];
		short keySize = (short) this.secretKey.length;
		short keyBitLimit = this.keyBitLimit;
		short keyByteLimit =(short) ((keyBitLimit+7)/8);
		short keyMask = (short) (255%(2^(8+keyBitLimit-8*keyByteLimit)));
		for (int x=0; x<keySize; x++){
			exKey[x] = this.secretKey[x];
		}
		for (int x=keySize; x<128; x++) {
			exKey[x] = TABLEPI[(exKey[x-1]+exKey[x-keySize])%256];
		}
		exKey[128-keyBitLimit] = TABLEPI[exKey[128-keyByteLimit]&keyMask];
		exKey[128-keyBitLimit] = exKey[128-keyBitLimit]&65535;
		for (int x=127-keyByteLimit; x==0; x--) {
			exKey[x] = TABLEPI[exKey[x+1]^exKey[x+keyByteLimit]];
		}
		this.expandedKey = exKey;
	}
	private int[] bytesToWords(int[] input){
		int numOfWords = (input.length%2==0)?(input.length/2):((input.length+1)/2);
		int[][] words = new int[numOfWords][2];
		for (int x=0; x<numOfWords; x++) {
			for (int y=0;y<2 ;y++) {
				words[x][y]=((2*x+y)<input.length)?(input[2*x+y]):0;
			}
		}
		int[] finalWords = new int[numOfWords];
		for (int x=0; x<numOfWords; x++) {
			finalWords[x]= 256*words[x][0]+words[x][1];
		}
		return finalWords;
	}
	private int[][] wordsToBlocks(int[] words){
		int numOfBlocks =(words.length%4==0)?(words.length/4):((words.length+3)/4);
		int[][] blocks = new int[numOfBlocks][4];
		for (int x=0; x<numOfBlocks; x++) {
			for (int y=0;y<4 ;y++) {
					blocks[x][y]=((4*x+y)<words.length)?(words[4*x+y]):0;
			}
		}
		return blocks;
	}
	private int[] blocksToWords(int[][] blocks){
		int[] words = new int[4*blocks.length];
		for (int x=0; x<blocks.length; x++) {
			for (int y=0; y<4; y++) {
				words[4*x+y] = blocks[x][y];
			}
		}
		return words;
	}
	private int[] wordsToBytes(int[] words){
		int[] output = new int[2*words.length];
		for (int x=0; x<words.length; x++) {
			output[2*x] = words[x]/256;
			output[2*x+1] = words[x]&0x00ff;	
		}
		return output;
	}
	private int[] mixing(int[] words, int[] key){
		for (int x=0; x<4; x++) {
			words[x] = words[x] + key[this.counter] + (words[(x+4-1)%4]&words[(x+4-2)%4]) + ((~words[(x+4-1)%4])&words[(x+4-3)%4]);
			words[x] = Integer.rotateLeft(words[x], SVECTOR[x]);
			this.counter++;
		}
		return words;
	}
	private int[] mashing(int[] words, int[] key){
		for (int x=0; x<4; x++) {
			words[x] = words[x] + key[words[(x+4-1)%4]&63];
		}
		return words;
	}
	private int[] rmixing(int[] words, int[] key){
		for (int x=3; x>=0; x--) {
			words[x] = Integer.rotateRight(words[x], SVECTOR[x]);
			words[x] = words[x] - key[this.counter] - (words[(x+4-1)%4]&words[(x+4-2)%4]) - ((~words[(x+4-1)%4])&words[(x+4-3)%4]);
			this.counter--;
		}
		return words;
	}
	private int[] rmashing(int[] words, int[] key){
		for (int x=3; x>=0; x--) {
			words[x] = words[x] - key[words[(x+4-1)%4]&63];
		}
		return words;
	}
	private int[][] encrypt(){
		int[][] blocks = this.blocks;
		int[] key = this.bytesToWords(this.expandedKey);
		for (int x=0; x<blocks.length; x++) {
			this.counter = 0;
			for (int y=0; y<5; y++) {
				blocks[x] = this.mixing(blocks[x],key);
			}
			blocks[x] = this.mashing(blocks[x],key);
			for (int y=0; y<6; y++) {
				blocks[x] = this.mixing(blocks[x],key);
			}
			blocks[x] = this.mashing(blocks[x],key);
			for (int y=0; y<5; y++) {
				blocks[x] = this.mixing(blocks[x],key);
			}
		}
		return blocks;
	}
	private int[][] decrypt(){
		int[][] blocks = this.blocks;
		int[] key = this.bytesToWords(this.expandedKey);
		for (int x=0; x<blocks.length; x++) {
			this.counter = 63;
			for (int y=0; y<5; y++) {
				blocks[x] = this.rmixing(blocks[x],key);
			}
			blocks[x] = this.rmashing(blocks[x],key);
			for (int y=0; y<6; y++) {
				blocks[x] = this.rmixing(blocks[x],key);
			}
			blocks[x] = this.rmashing(blocks[x],key);
			for (int y=0; y<5; y++) {
				blocks[x] = this.rmixing(blocks[x],key);
			}
		}
		return blocks;
	}
	public void init(byte choice){
		if (choice == 1) {
			this.setInputFile();
			this.setSecretKey();
			this.setExpandedKey();
			this.blocks = this.wordsToBlocks(this.bytesToWords(this.inputFile));
			this.outputFile = this.blocksToWords(this.encrypt());
			this.writeOutputFile();
		}
		if (choice == 2){
			this.encrypting = false;
			this.setInputFile();
			this.setSecretKey();
			this.setExpandedKey();
			this.blocks = this.wordsToBlocks(this.inputFile);
			this.outputFile = this.wordsToBytes(this.blocksToWords(this.decrypt()));
			this.writeOutputFile();
		}
	}
}