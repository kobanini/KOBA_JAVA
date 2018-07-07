public class LagObserver {

	private static int groupsize = 1; // 1 ... 24
	public static int groupsize() {
		return groupsize;
	}

	private static final boolean ADJUST_ON = true;
	
	private static final long MILLISEC_MAX_COAST = 50;
//	private static final long MILLISEC_MIN_COAST = 30;
	
	private static int groupMaxSize = 48; // max : 2ÃÊ µô·¹ÀÌ
	private static int groupMinSize = 1;
	
	private static final int OBSERVER_CAPACITY = 15 * 24; // 15ÃÊ°£ delay°¡ ¾ø¾î¼­ groupsize¸¦ ³·Ãá´Ù.
	private static long[] observedTime = new long[OBSERVER_CAPACITY];
	
	private static final String LAG_RELIEVE_ADJUSTMENT = "Lag Relieve Adjustment: LELEL - %d ... (%s)";

	private long startTime;
	
	public LagObserver() {
	}
	
	public void start() {
		this.startTime = System.currentTimeMillis();
	}
	
	public void observe() {
		observedTime[MyBotModule.Broodwar.getFrameCount() % OBSERVER_CAPACITY] = System.currentTimeMillis() - this.startTime;
		this.startTime = System.currentTimeMillis();
		this.adjustment();
	}
	
	public void adjustment() {
		if (ADJUST_ON) {
			long cost = observedTime[MyBotModule.Broodwar.getFrameCount() % OBSERVER_CAPACITY];
			
			if (cost > MILLISEC_MAX_COAST) {
				if (groupsize < groupMaxSize) {
					groupsize++;
				}
			} else {
				if (MyBotModule.Broodwar.self().supplyUsed() > 300) {
					groupMinSize = 6;
				} else {
					groupMinSize = 1;
				}
				
				if (groupsize > groupMinSize) {
					boolean exceedTimeExist = false;
					for (long t : observedTime) {
						if (t >= MILLISEC_MAX_COAST) {
							exceedTimeExist = true;
							break;
						}
					}
					if (!exceedTimeExist) {
						groupsize--;
					}
				}
			}
		}
	}

}
