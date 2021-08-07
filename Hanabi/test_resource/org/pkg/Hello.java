package org.pkg;

public class Hello {
	
	public static void main(String[] args) {
		System.out.println(foo(1, 2));
		
		System.out.println(devide(2, 3));

		tryCatch();
	}
	
	public static int foo(int a, int b) {
		int result = 0;

		if(a > 0) {
			result = a + b;
		} else {
			result = a - b;
		}
		return result;
	}

	public static int devide(int a, int b){
		return a / b;
	}

	public static void tryCatch(){
		int a = 100;
		int b = 99;
		try{
			int c = a / b;
		} catch (Exception e){
			//
			e.printStackTrace();
		} finally{
			b++;
		}
	}
}

