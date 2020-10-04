package project2;
import java.util.*; 
import java.io.*;

public class main {

	public static void main(String[] args) {
		Integer[] pm = new Integer[524288];	//physical memory
		Integer [][] demand_page = new Integer[1024][512];

		File output = new File("output.txt");
		FileWriter fw = null;

		try {
			fw = new FileWriter(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter print_to_file = new PrintWriter(fw);

		BufferedReader reader1 = null;	//reader1 to read file 1
		BufferedReader readerVA = null;	//reader2 to read file 2

		try {
			reader1 = new BufferedReader(new FileReader("init-dp.txt"));
			readerVA = new BufferedReader(new FileReader("input-dp.txt"));

			ArrayList<Integer> each_num = new ArrayList<Integer>();

			// Initialize free_frames and populate with 2 to 1023
			LinkedList<Integer> free_frames = new LinkedList<Integer>();
			for(int i = 2; i < 1024 ; i++) {
				free_frames.add(i);
			}
			// initialize demand_page to all 0
			for(int i = 0; i < 1024; i++) {
				for(int j = 0; j < 512; j++) {
					demand_page[i][j] = 0;
				}
			}

			//read line and add each number to each_num
			String command = reader1.readLine();
			for(String s: command.split(" ")) {
				each_num.add(Integer.parseInt(s));
			}

			//initialize Page Table
			while(each_num.isEmpty() == false) {
				Integer s = each_num.remove(0);
				Integer z = each_num.remove(0);
				Integer f = each_num.remove(0);
				//System.out.println("s,z,f are : " + s.toString() + " " + z.toString() + " " + f.toString());
				// if frame is positive then frame is taken
				if(f >= 0) {
					free_frames.removeFirstOccurrence(f);
					//System.out.println("Removed frame " + f.toString() + " from free frames");
				}
				pm[2 * s] = z;
				pm[2 * s + 1] = f;
				//System.out.println("PM Position " + (2*s) + " = " + z.toString());
				//System.out.println("PM Position " + (2*s+1) + "  = " + f.toString());
			}

			//read line and add each number to each_num
			command = reader1.readLine();
			for(String s: command.split(" ")) {
				each_num.add(Integer.parseInt(s));
			}

			//initialize Segment Table
			while(each_num.isEmpty() == false) {
				Integer s = each_num.remove(0);
				Integer p = each_num.remove(0);
				Integer f = each_num.remove(0);
				//System.out.println("s,p,f are : " + s.toString() + " " + p.toString() + " " + f.toString());	//test
				// if frame is positive then frame is taken
				if(f >= 0) {
					free_frames.removeFirstOccurrence(f);
					//System.out.println("Removed frame " + f.toString() + " from free frames");	//test
				}
				Integer pos = pm[s * 2 + 1];
				//System.out.println("POS is " + pos.toString());	//test
				if(pos < 0) {
					pos *= -1;
					demand_page[pos][p] = f;
					//System.out.println("demand page at row " + pos.toString() + ", and column: " + p.toString() + " is: " + f.toString());	//test
				}else {
					pm[pos * 512 + p] = f;
				}
			}

			//test
			//for(int i = 0; i < 524288; i++) {
				//if (pm[i] != null) {
					//System.out.println(i +" : " + pm[i] );
				//}
			//}

			//calculate PA
			while((command = readerVA.readLine()) != null) {
				for(String s: command.split(" ")) {
					each_num.add(Integer.parseInt(s));
				}
				while(each_num.isEmpty() == false) {
					Integer number = each_num.remove(0);
					Integer s = number>>18;
					Integer p = number>>9;
					p = p & 511;
					Integer w = number & 511;
					Integer pw = number & 262143;
					Integer pa = 0;

					//System.out.println("S, P, W , PW are: " + s.toString() + " " + p.toString() + " " + w.toString() + " " + pw.toString()); // test
					
					if(pw >= pm[2 * s]) {
						//System.out.println("-1");
						print_to_file.write("-1 ");
					}else {
						Integer page_table = pm[2 * s + 1];
						Integer page = 0;
						if((pm[2 * s + 1] * 512 + p) > 0) {
							page = pm[pm[2 * s + 1] * 512 + p];
						}
						
						//if Page Table and Page are resident
						if(page_table > 0 && page > 0) {
							pa = pm[pm[2 * s + 1] * 512 + p] * 512 + w;
							
							//System.out.println("Page Table and Page are both resident: PA = " + pa.toString());
							print_to_file.write(pa + " ");
						}
						//if Page Table is resident and Page NOT resident
						else if(page_table > 0 && page < 0) {
							// get first free frame and replace the number in pm[pm[2 * s + 1] * 512 + p] with free frame
							Integer frame = free_frames.removeFirst();
							//frame = 4;	//test
							pa = frame *512 + w;
							
							//System.out.println("Page Table is resident and Page is NOT resident: PA = " + pa.toString());
							print_to_file.write(pa + " ");
						}
						//if Page Table is NOT resident 
						else if(page_table < 0) {
							//get first free frame and replace with pm[2 * s + 1]'s number
							Integer frame = free_frames.removeFirst();
							//frame = 5; //test
							Integer get_disk = page_table * -1;
							Integer start = frame * 512;
							pm[2 * s + 1] = frame;	//replace negative number with free frame
							//filling pm with data from disk
							for(int i = 0; i < 512; i++) {
								//System.out.println("Getting data from disk inputting at memory: " + start + " with value: " + demand_page[get_disk][i]);	//test
								pm[start++] = demand_page[get_disk][i];
							}
							
							page = pm[frame * 512 + p];
							//if Page is resident
							if(page > 0 ) {
								pa = page * 512 + w;
								
								//System.out.println("Page Table is NOT resident and Page is resident: PA = " + pa.toString());
								print_to_file.write(pa + " ");
							}
							//if Page is NOT resident
							else {
								frame = free_frames.removeFirst();
								frame = 14;	//test
								pa = frame * 512 + w;
								
								//System.out.println("Page Table is NOT resident and Page is NOT resident: PA = " + pa.toString());
								print_to_file.write(pa + " ");
							}
							
						}
					}

				}


			}


		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader1.close();
				readerVA.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		print_to_file.close();
	}
}
