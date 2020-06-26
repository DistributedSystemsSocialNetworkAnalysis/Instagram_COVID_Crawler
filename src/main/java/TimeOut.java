
public class TimeOut implements Runnable {
	int searchTime = 18000000; // 30 minuti
	
	@Override
	public void run() {
		try {
			Thread.sleep(18000000); 
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
