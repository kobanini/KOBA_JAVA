public class Main {

	/// �� ���α׷��� �����մϴ�<br>
	/// <br>
	/// Eclipse �޴� -> Run -> Run Configurations...<br> 
	/// -> Arguments �� -> Working Directory �� Other : C����̺���StarCraft �� �����ϸ�<br>
	/// �� ���α׷��� �� �����Ǿ��ִ� �� ���� �м� ĳ�������� Ȱ���ϰ� �Ǿ<br>
	/// �� ���α׷� ���� �� �� ���� �м��� �ҿ�Ǵ� �����̸� ���� �� �ֽ��ϴ�.
    public static void main(String[] args) {
    	try{
            new MyBotModule().run();   		
    	}
    	catch(Exception e) {
    		System.out.println(e.toString());
    		e.printStackTrace();
    	}
    }
}