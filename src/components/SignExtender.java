package components;

public class SignExtender {

	public SignExtender() {
		// TODO Auto-generated constructor stub
	}
	
	public int extend(boolean flag, int value) {
		if(flag) {
			//immediate
			int check = (value & 0b000000000100000000000000000);
			if(check == Math.pow(2,17))
				return (0b11111111111111000000000000000000) + value;
			else 
				return value;
		}else {
			//address
			int check = (value & 0b00001000000000000000000000000000);
			if(check == Math.pow(2,27))
				return (0b11110000000000000000000000000000) + value;
			else 
				return value;
		}
	}
	
}
