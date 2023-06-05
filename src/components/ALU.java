package components;

public class ALU {
	
	int res;
	boolean zeroFlag;

	public ALU() {
		// TODO Auto-generated constructor stub
	}
	
	public void performOperation(int opcode, int x, int y) {
		/*
		 * 0 -> add
		 * 1 -> sub
		 * 2 -> xor
		 * 3 -> multiply
		 * 4 -> and
		 * 5 -> shif left
		 * 6 -> shift right
		 * */
		
		switch(opcode) {
		case 0: res = x + y;break;
		case 1: res = x -y; break;
		case 2: res = x ^ y; break;
		case 3: res = x * y; break;
		case 4: res = x & y; break;
		case 5: res = x << y; break;
		case 6: res = x >>> y; break;
		}
		
		System.out.println("alu operation: x=" + x + ", y= "+ y +", code="+ opcode+ ", res=" + res);
		
		if(res==0)
			zeroFlag = true;
		else zeroFlag = false;
		
	}
	
	public boolean getFlag()
	{
		return zeroFlag;
	}
	
	public int getResult() {
		return res;
	}
	
}
