import java.io.*;
import java.util.*;

public class AddressTranslator {
	static List<Integer> lru = new LinkedList<Integer>();
	static List<Integer> lruPT = new LinkedList<Integer>();
	static List<Integer> fifo = new LinkedList<Integer>();
	static String inputFile = "InputFile.txt";
	static int addr;
	static int p_num;
	static int offset;
	static int f_num;
	static int value;
	static int phy_addr;
	static int tlb_miss = 0;
	static int page_fault = 0;
	static boolean isFull = false;
	static TLB tlb = new TLB();
	static PageTable pt = new PageTable();
	static PhysicalMemory pm = new PhysicalMemory();
	static BackStore bs = new BackStore();

	public static void main(String[] args) {
		// Use fifo Algorithm to the Page replace Mgt.
		// _fifo();
		// Use lru Algorithm to the Page replace Mgt.
		_lru();
	}

	static void _fifo() {
		try {
			Scanner sc = new Scanner(new File(inputFile));

			while (sc.hasNextInt()) {
				addr = sc.nextInt();
				addr = addr % 65536;
				offset = addr % 256;
				p_num = addr / 256;
				f_num = -1;
				f_num = tlb.get(p_num);
				if (f_num == -1) {
					tlb_miss++;
					f_num = pt.get(p_num);
					if (f_num == -1) {
						page_fault++;
						System.out.println("Page fault");
						Frame f = new Frame(bs.getData(p_num));
						// ///----------- FIFO IMPLEMENTATION -START
						fifo.add(p_num);
						if (isFull == true) {
							int delete = fifo.remove(0);
							pt.table[delete] = new PageTableItem(-1, false);
							tlb.table.remove(delete);
							pm.frames[pm.currentFreeFrame] = new Frame(f.data);
							f_num = pm.currentFreeFrame;
							pt.add(p_num, f_num);
							tlb.put(p_num, f_num);
							if (pm.currentFreeFrame == PhysicalMemory.frames.length) {
								pm.currentFreeFrame = 0;
							}
							phy_addr = f_num * 256 + offset;
							value = pm.getValue(f_num, offset);
							System.out.println(String.format("Virtual address: %s Physical address: %s Value: %s", addr,
									phy_addr, value));
							continue;
						} // ///----------- FIFO IMPLEMENTATION -END
						f_num = pm.addFrame(f);
						if (pm.currentFreeFrame == PhysicalMemory.frames.length) {
							isFull = true;
							pm.currentFreeFrame = 0;
						}
						pt.add(p_num, f_num);
						tlb.put(p_num, f_num);
					}
				}
				phy_addr = f_num * 256 + offset;
				value = pm.getValue(f_num, offset);
				System.out.println(
						String.format("Virtual address: %s Physical address: %s Value: %s", addr, phy_addr, value));
			}
			System.out.println(String.format("TLB miss: %s, Page Fault: %s", tlb_miss, page_fault));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	static void _lru() {
		try {
			Scanner sc = new Scanner(new File(inputFile));
			while (sc.hasNextInt()) {
				addr = sc.nextInt();
				addr = addr % 65536;
				offset = addr % 256;
				p_num = addr / 256;
				f_num = -1;
				f_num = tlb.get(p_num);
				if (f_num == -1) {
					tlb_miss++;
					f_num = pt.get(p_num);
					if (f_num == -1) {
						page_fault++;
						System.out.println("Page fault");
						Frame f = new Frame(bs.getData(p_num));
						// ///----------- LRU IMPLEMENTATION - START
						if (isFull == true) {
							pm.frames[lru.get(0)] = new Frame(f.data);
							f_num = lru.get(0);

							int delete = lruPT.get(0);
							pt.table[delete] = new PageTableItem(-1, false);
							tlb.table.remove(delete);

							pt.add(p_num, f_num);
							tlb.put(p_num, f_num);

							lru.remove(lru.indexOf(f_num));
							lruPT.remove(lruPT.indexOf(delete));
							lru.add(f_num);
							lruPT.add(p_num);

							phy_addr = f_num * 256 + offset;
							value = pm.getValue(f_num, offset);
							System.out.println(String.format("Virtual address: %s Physical address: %s Value: %s", addr,
									phy_addr, value));
							continue;
						}
//						if it 'isFull == false'
						f_num = pm.addFrame(f);
						if (pm.currentFreeFrame == PhysicalMemory.frames.length) {
							isFull = true;
						}
						pt.add(p_num, f_num);
						tlb.put(p_num, f_num);
					}
				}
//				these codes use lrtuPageTable Stack Structure to implement lru Stack
//				therefore the oldest referenced Page goes to the bottom part of index.
//				it means when frames are full the oldest one should be deleted as a victim.
				if (lruPT.contains(lruPT.indexOf(p_num))) {
					lruPT.remove(lruPT.indexOf(p_num));
					lruPT.add(p_num);
				} else {
					lruPT.add(p_num);
				}
//				these codes use lruFrametable Stack Structure to implement lru Stack
				if (lru.contains(lru.indexOf(f_num))) {
					lru.remove(lru.indexOf(f_num));
					lru.add(f_num);
				} else {
					lru.add(f_num);
				}
				///----------- LRU IMPLEMENTATION - END
				phy_addr = f_num * 256 + offset;
				value = pm.getValue(f_num, offset);
				System.out.println(
						String.format("Virtual address: %s Physical address: %s Value: %s", addr, phy_addr, value));
			}
			System.out.println(String.format("TLB miss: %s, Page Fault: %s", tlb_miss, page_fault));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
