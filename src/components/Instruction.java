package components;

public class Instruction {
	
	public InstructionType type;
	public int address;
	public int opcode;
	public int r1;
	public int r2;
	public int imm;
	public int r3;
	public int shamt;
	public int value;
	public int id;
	
	
	public Instruction(InstructionType type, int opcode, int r1, int r2, int r3, int shamt, int id) {
		this.type = type;
		this.opcode = opcode;
		this.r1 = r1;
		this.r2 = r2;
		this.r3 = r3;
		this.shamt = shamt;
		this.id = id;
	}
	
	public Instruction(InstructionType type, int opcode, int r1, int r2, int imm, int id) {
		this.type = type;
		this.opcode = opcode;
		this.r1 = r1;
		this.r2 = r2;
		this.imm = imm;
		this.id = id;
	}
	public Instruction(InstructionType type, int opcode, int address, int id) {
		this.type = type;
		this.opcode = opcode;
		this.address = address;
		this.id = id;
	}

	
	public void setValue(int value) {
		this.value = value;
	}
	
	public String toString() {
		String res = "\n";
		res += "id : " + id;
		res += "\nopcode : " + opcode;
		res += "\n instruction type : " + type;
		if(type == InstructionType.J)
			res += "\n address: " + address;
		else {
			res += "\nr1 : " + r1;
			res += "\nr2 : " + r2;
			if(type == InstructionType.I)
				res += "\n immediate : " + imm;
			else {
				res+= "\n r3 : " + r3;
				res += "\nshamt : " + shamt;
			}
		}
		return res;
	}

}
