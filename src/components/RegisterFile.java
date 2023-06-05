package components;

import java.util.Arrays;

public class RegisterFile {
	
	final int R0 = 0;
	int pc;
	int[] GPRegisters;
	public RegisterFile() {
		// TODO Auto-generated constructor stub
		GPRegisters = new int[31];
		pc= 0;
	}
	
	public void updatePC() {
		pc++;
		System.out.println("pc incremented");
	}
	
	public void updatePC(int value) {
		pc = value;
		System.out.println("pc updated to : " + value);
	}
	
	public int getPC() {
		return pc;
	}
	
	public void updateRegister(int index, int value) {
		if(index == 0)
			return;
		GPRegisters[index - 1] = value;
		System.out.println("Register " + (index) + " updated to :" + value);
	} 
	
	public int getRegister(int index) {
		if(index == 0)
			return R0;
		return GPRegisters[index - 1];
	}
	
	public int getRZero() {
		return R0;
	}
	
	public String toString() {
		return "Registers:\n" + Arrays.toString(GPRegisters);
	}

}
