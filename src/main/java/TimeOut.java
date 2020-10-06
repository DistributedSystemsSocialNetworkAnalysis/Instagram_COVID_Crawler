
public class TimeOut implements Runnable {
	int searchTime = 10800000; // 3 ore = 10800000
	
	public void run() {
		try {
			Thread.sleep(searchTime); 
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
