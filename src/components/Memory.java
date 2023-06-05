package components;

import java.util.Arrays;

public class Memory {

	int indexBits;
	int instructionBits;
	int instructionBase;
	int dataBase;
	int[] memory;
	int size;

	public Memory() {
		// TODO Auto-generated constructor stub
		indexBits = 11;
		instructionBits = 10;
		instructionBase = 0;
		dataBase = 1024;
		size = (int) Math.pow(2, indexBits);
		memory = new int[size];
	}

	public int readInstruction(int index) {
		return memory[index];
	}

	public int readData(int index) {
		return memory[index];
	}

	public void storeData(int index, int value) {
		if (index>= size) {
			System.out.println("out of memory bounds");
			return;
		}
		memory[index] = value;
		System.out.println("updating data at index " + (index ) + " to " + value);
	}

	public void loadInstruction(int instruction, int index) {
		memory[index] = instruction;
	}

	public String toString() {
		return "Memory:\n" + Arrays.toString(memory);
	}

}
