package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import components.ALU;
import components.Instruction;
import components.InstructionType;
import components.Memory;
import components.RegisterFile;
import components.SignExtender;

public class Processor {

	int clock;
	RegisterFile registerFile;
	Memory memory;
	ALU alu;
	int decodeCycle;
	int executeCycle;
	SignExtender signExtender;

	boolean accessingMemory;// no fetch and mem at the same time
	Object[] pipeline; // can store int b4 decoding or Instruction object after decoding

	public Processor() {

		clock = 1;
		registerFile = new RegisterFile();
		memory = new Memory();
		signExtender = new SignExtender();
		alu = new ALU();
		pipeline = new Object[5]; // space for instruction at each stage
	}

	public void run() {
		System.out.println("\n\nclock cycle " + clock);
		if (clock % 2 == 1)
			if (!fetch())
				pipeline[0] = null;
		if (pipeline[1] != null)
			decode();
		if (pipeline[2] != null)
			execute();
		if (!accessingMemory && pipeline[3] != null)
			memoryAccess();
		if (pipeline[4] != null) {
			writeback();
			pipeline[4] = null;
		}

		if (!accessingMemory && pipeline[3] != null) {
			pipeline[4] = pipeline[3];
			pipeline[3] = null;
		}

		if (executeCycle == 2) {
			pipeline[3] = pipeline[2];
			pipeline[2] = null;
			executeCycle = 0;
		}

		if (decodeCycle == 2) {
			pipeline[2] = pipeline[1];
			pipeline[1] = null;
			decodeCycle = 0;
		}

		if (clock % 2 == 1) {
			pipeline[1] = pipeline[0];
			pipeline[0] = null;
		}

		accessingMemory = false;

		clock++;
		
		boolean flag = true;
		if(registerFile.getPC() < 1024) {
		for (int i = 0; i < pipeline.length; i++)
			if (pipeline[i] != null) {
				flag = false;
				run();
			}
		}
		
		if(flag) {
			System.out.println(registerFile.toString());
			System.out.println(memory.toString());
		}
	}

	private boolean fetch() {

		accessingMemory = true;
		int instruction = memory.readInstruction(registerFile.getPC()); // get from memory
		if (instruction == 0)
			return false;
		System.out.println("\nfetching instruction at pc: " + registerFile.getPC() + " " + instruction);
		pipeline[0] = instruction;
		registerFile.updatePC();
		return true;
	}

	private void decode() {
		System.out.println("\ndecoding instruction " + pipeline[1]);

		if (decodeCycle == 1) {
			decodeCycle++;
			return;
		}

		int instruction = (int) pipeline[1];
		Instruction decodedInstruction;
		InstructionType type;

		int opcode = (instruction & 0b11110000000000000000000000000000) >>> 28;
		switch (opcode) {
		case 0:
		case 1:
		case 2:
		case 5:
		case 8:
		case 9:
			type = InstructionType.R;
			break;
		case 4:
		case 3:
		case 6:
		case 10:
		case 11:
			type = InstructionType.I;
			break;
		case 7:
			type = InstructionType.J;
			break;
		default:
			System.out.println("no such instruction");
			return;
		}

		if (type == InstructionType.J) {
			int address = (instruction & 0b00001111111111111111111111111111);
			// address = signExtender.extend(false, address);
			decodedInstruction = new Instruction(type, opcode, address, registerFile.getPC());

		} else {

			int r1 = (instruction & 0b00001111100000000000000000000000) >>> 23;
			int r2 = (instruction & 0b00000000011111000000000000000000) >>> 18;

			if (type == InstructionType.I) {
				int imm = (instruction & 0b00000000000000111111111111111111);
				imm = signExtender.extend(true, imm);
				decodedInstruction = new Instruction(type, opcode, r1, r2, imm, registerFile.getPC());
			} else {

				int r3 = (instruction & 0b00000000000000111110000000000000) >>> 13;
				int shamt = (instruction & 0b00000000000000000001111111111111);
				decodedInstruction = new Instruction(type, opcode, r1, r2, r3, shamt, registerFile.getPC());
			}

		}

		pipeline[1] = decodedInstruction;

		decodeCycle++;

	}

	public void execute() {
		Instruction instr = (Instruction) pipeline[2];
		System.out.println("\nexecuting instruction with id : " + instr.id + " and opcode : " + instr.opcode);

		if (executeCycle == 0) {
			executeCycle++;
			return;
		}

		executeCycle++;

		if (instr.type == InstructionType.J) {
			registerFile.updatePC((registerFile.getPC() & 0b11110000000000000000000000000000) + instr.address);
			pipeline[0] = null;
			pipeline[1] = null;
			return;
		}

		if (instr.type == InstructionType.I) {
			if (instr.opcode == 3) {
				//movi
				instr.setValue(instr.imm);
				return;
			}

			if (instr.opcode == 4) {
				// jump if equal
				alu.performOperation(0, registerFile.getRegister(instr.r1), registerFile.getRegister(instr.r2));
				if (alu.getFlag()) {
					registerFile.updatePC(registerFile.getPC() + 1 + instr.imm);
					pipeline[0] = null;
					pipeline[1] = null;

				}
				return;
			}
			if (instr.opcode == 6) {
				// xorI
				alu.performOperation(2, registerFile.getRegister(instr.r2), instr.imm);
				instr.setValue(alu.getResult());
				return;
			}
			if (instr.opcode == 10) {
				// movr
				alu.performOperation(0, registerFile.getRegister(instr.r2), instr.imm);
				instr.setValue(alu.getResult());
				return;
			}

			if (instr.opcode == 11) {
				// movm
				alu.performOperation(0, registerFile.getRegister(instr.r2), instr.imm);
				instr.setValue(alu.getResult());
				return;
			}

		}

		if (instr.type == InstructionType.R) {
			if (instr.opcode == 0) {
				// ADD
				alu.performOperation(0, registerFile.getRegister(instr.r2), registerFile.getRegister(instr.r3));
				instr.setValue(alu.getResult());
				return;
			}

			if (instr.opcode == 1) {
				// SUB
				alu.performOperation(1, registerFile.getRegister(instr.r2), registerFile.getRegister(instr.r3));
				instr.setValue(alu.getResult());
				return;
			}
			if (instr.opcode == 2) {
				alu.performOperation(3, registerFile.getRegister(instr.r2), registerFile.getRegister(instr.r3));
				instr.setValue(alu.getResult());
				return;
			}
			if (instr.opcode == 5) {
				alu.performOperation(4, registerFile.getRegister(instr.r2), registerFile.getRegister(instr.r3));
				instr.setValue(alu.getResult());
				return;
			}
			if (instr.opcode == 8) {
				alu.performOperation(5, registerFile.getRegister(instr.r2), instr.shamt);
				instr.setValue(alu.getResult());
				return;

			}
			if (instr.opcode == 9) {
				alu.performOperation(6, registerFile.getRegister(instr.r2), instr.shamt);

				return;
			}
		}

	}

	public void memoryAccess() {

		Instruction instr = (Instruction) pipeline[3];

		switch (instr.opcode) {
		case 10: {
			System.out.println("\n memory access, loading address " + instr.value);
			instr.setValue(memory.readData(instr.value));
			break;
		}
		case 11: {
			System.out.println("\n memory access, storing address " + instr.value);
			memory.storeData(instr.value, registerFile.getRegister(instr.r1));
			break;
		}
		default:
			System.out.println("\n memory access stage of instruction : " + instr.id);
			return;
		}

	}

	public void writeback() {
		Instruction instr = (Instruction) pipeline[4];
		switch (instr.opcode) {
		case 10:
		case 9:
		case 8:
		case 6:
		case 5:
		case 3:
		case 2:
		case 1:
		case 0:
			System.out.println("\nWriteback stage of instruction " + instr.id + " with writeback register " + instr.r1);
			registerFile.updateRegister(instr.r1, instr.value);
			break;
		default:
			System.out.println("\nWriteback stage of instruction " + instr.id);
			return;

		}
	}

	public void loadInstructions(File file) throws IOException {
		int instrAddress = 0;
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = reader.readLine()) != null) {

			if (instrAddress == 1024) {
				System.out.println("instruction memory full");
				break;
			}

			line = line.toLowerCase();
			String[] str = line.split(" ");
			int opcode = 0;
			InstructionType type = null;
			switch (str[0]) {
			case "add":
				opcode = 0;
				type = InstructionType.R;
				break;
			case "sub":
				opcode = 0b00010000000000000000000000000000;
				type = InstructionType.R;
				break;
			case "mul":
				opcode = 0b00100000000000000000000000000000;
				type = InstructionType.R;
				break;
			case "movi":
				opcode = 0b00110000000000000000000000000000;
				type = InstructionType.I;
				break;
			case "jeq":
				opcode = 0b01000000000000000000000000000000;
				type = InstructionType.I;
				break;
			case "and":
				opcode = 0b01010000000000000000000000000000;
				type = InstructionType.R;
				break;
			case "xori":
				opcode = 0b01100000000000000000000000000000;
				type = InstructionType.I;
				break;
			case "jmp":
				opcode = 0b01110000000000000000000000000000;
				type = InstructionType.J;
				break;
			case "lsl":
				opcode = 0b10000000000000000000000000000000;
				type = InstructionType.R;
				break;
			case "lsr":
				opcode = 0b10010000000000000000000000000000;
				type = InstructionType.R;
				break;
			case "movr":
				opcode = 0b10100000000000000000000000000000;
				type = InstructionType.I;
				break;
			case "movm":
				opcode = 0b10110000000000000000000000000000;
				type = InstructionType.I;
				break;
			default:
				System.out.println(str[0] + " no such opcode");
				return;
			}

			if (type == InstructionType.J) {
				int address = Integer.parseInt(str[1]);
				// todo:check hex or int, validate
				memory.loadInstruction(opcode + address, instrAddress);
				instrAddress++;
				continue;
			}

			int r1 = Integer.parseInt(str[1].substring(1));
			 {
				if (!validateRegister(r1)) {
					System.out.println("invalid register");
					return;
				}

				// 0b10110000100000000000000000000001
				r1 = r1 << 23;
			}

			int r2;
			if (opcode == 0b00110000000000000000000000000000) {
				r2 = 0;
			} else {

				r2 = Integer.parseInt(str[2].substring(1));

				if (!validateRegister(r2)) {
					System.out.println("invalid register");
					return;
				}

				r2 = r2 << 18;

			}

			if (type == InstructionType.I) {
				// movi
				int imm;
				if (opcode == 0b00110000000000000000000000000000)
					imm = Integer.parseInt(str[2]);
				else
					imm = Integer.parseInt(str[3]);
				imm = imm & 0b00000000000000111111111111111111;
				memory.loadInstruction(opcode + r1 + r2 + imm, instrAddress);
				instrAddress++;
				continue;
			}

			int r3;
			int shamt;
			if (opcode == 0b10000000000000000000000000000000 || opcode == 0b10010000000000000000000000000000) {
				r3 = 0;
				shamt = Integer.parseInt(str[3]);
			} else {

				r3 = Integer.parseInt(str[3].substring(1));

				if (!validateRegister(r3)) {
					System.out.println("invalid register");
					return;
				}
				r3 = r3 << 13;

				shamt = 0;
			}

			memory.loadInstruction(opcode + r1 + r2 + r3 + shamt, instrAddress);
			instrAddress++;

		}
	}

	private boolean validateRegister(int r1) {
		// TODO Auto-generated method stub
		return r1 < 33 && r1 >= 0;
	}

	public static void main(String[] args) {
		Processor p = new Processor();
		String programName = "program.txt";
		File program = new File("src/resources/" + programName);

		try {
			p.loadInstructions(program);
		} catch (IOException e) {
			System.out.println("problem in loading program");
		}
		// parse el file
		// save in instruct

		p.run();
	}
}
