
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import bwapi.Color;
import bwapi.Order;
import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.BaseLocation;
/// 상황을 판단하여, 정찰, 빌드, 공격, 방어 등을 수행하도록 총괄 지휘를 하는 class <br>
/// InformationManager 에 있는 정보들로부터 상황을 판단하고, <br>
/// BuildManager 의 buildQueue에 빌드 (건물 건설 / 유닛 훈련 / 테크 리서치 / 업그레이드) 명령을 입력합니다.<br>
/// 정찰, 빌드, 공격, 방어 등을 수행하는 코드가 들어가는 class
public class StrategyManager {

	private static StrategyManager instance = new StrategyManager();

	private CommandUtil commandUtil = new CommandUtil();
	
	private boolean isInitialBuildOrderFinished;
	
	public boolean isInitialBuildOrderFinished() {
		return isInitialBuildOrderFinished;
	}

		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가를 위한 변수 및 메소드 선언
		/// 한 게임에 대한 기록을 저장하는 자료구조
		private class GameRecord {
			String mapName;
			String enemyName;
			String enemyRace;
			String enemyRealRace;
			String myName;
			String myRace;
			int gameFrameCount = 0;
			int myWinCount = 0;
			int myLoseCount = 0;
		}
		/// 과거 전체 게임들의 기록을 저장하는 자료구조
		ArrayList<GameRecord> gameRecordList = new ArrayList<GameRecord>();
	// BasicBot 1.1 Patch End //////////////////////////////////////////////////
	
	public int vultureratio = 0;
	public int tankratio = 0;
	public int goliathratio = 0;
	public int wgt = 1;
	private int InitFaccnt = 0;
	public boolean EXOK = false;
	public boolean LiftChecker = false;
	
	public enum Strategys { 
		zergBasic
		,zergBasic_HydraWave
		,zergBasic_GiftSet
		,zergBasic_HydraMutal
		,zergBasic_LingHydra
		,zergBasic_LingLurker
		,zergBasic_LingMutal
		,zergBasic_LingUltra
		,zergBasic_Mutal
		,zergBasic_MutalMany
		,zergBasic_Ultra
		,protossBasic
		,protossBasic_Carrier
		,protossBasic_DoublePhoto
		,terranBasic
		,terranBasic_Bionic
		,terranBasic_Mechanic
		,terranBasic_MechanicWithWraith
		,terranBasic_MechanicAfter
		,terranBasic_BattleCruiser	
		,AttackIsland
//		,terranBasic_ReverseRush
		} //기본 전략 나열
	public enum StrategysException { 
		zergException_FastLurker
		,zergException_Guardian
		,zergException_NongBong
		,zergException_OnLyLing
		,zergException_PrepareLurker
		,zergException_ReverseRush
		,zergException_HighTech
		,protossException_CarrierMany
		,protossException_Dark
		,protossException_Reaver
		,protossException_Scout
		,protossException_Shuttle
		,protossException_ShuttleMix
		,protossException_ReadyToZealot
		,protossException_ZealotPush
		,protossException_ReadyToDragoon
		,protossException_DragoonPush
		,protossException_PhotonRush
		,protossException_DoubleNexus
		,protossException_Arbiter
		,terranException_CheeseRush
		,terranException_NuClear
		,terranException_WraithCloak
		,Init} //예외 전략 나열, 예외가 아닐때는 무조건 Init 으로 

	/// static singleton 객체를 리턴합니다
	public static StrategyManager Instance() {
		return instance;
	}

	private Strategys CurrentStrategyBasic = null;
	private StrategysException CurrentStrategyException = null;
	public Strategys LastStrategyBasic = null;
	public StrategysException LastStrategyException = null;
	
	public int Attackpoint=0;
	public int MyunitPoint = 0;
	public int ExpansionPoint = 0;
	public int UnitPoint = 0;
	public int CombatStartCase =0;
	public int WraithTime =0;
	public int nomorewraithcnt=0;
	public int CombatTime=0;
	
	public StrategyManager() {
		isInitialBuildOrderFinished = false;
		CurrentStrategyBasic = Strategys.protossBasic_Carrier;
		CurrentStrategyException = StrategysException.Init;
		setCombatUnitRatio();
	}
	
	public void setCurrentStrategyBasic(Strategys strategy) {
		if(CurrentStrategyBasic != strategy){
			LastStrategyBasic = CurrentStrategyBasic; 
			CurrentStrategyBasic = strategy;
			setCombatUnitRatio();
		}
	}
	public void setCurrentStrategyException(StrategysException strategy) {
		if(CurrentStrategyException != strategy){
			LastStrategyException = CurrentStrategyException;
			CurrentStrategyException = strategy;
			setCombatUnitRatio();
		}
	}
	public Strategys getCurrentStrategyBasic() {
		return CurrentStrategyBasic;
	}
	public StrategysException getCurrentStrategyException() {
		return CurrentStrategyException;
	}
	
	public Strategys getLastStrategyBasic() {
		return LastStrategyBasic;
	}
	public StrategysException getLastStrategyException() {
		return LastStrategyException;
	}	

	/// 경기가 시작될 때 일회적으로 전략 초기 세팅 관련 로직을 실행합니다
	public void onStart() {
		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가
		// 과거 게임 기록을 로딩합니다
		//loadGameRecordList();
		// BasicBot 1.1 Patch End //////////////////////////////////////////////////
		
//		StrategyManager.Instance().setCurrentStrategyBasic(Strategys.zergBasic);
//		StrategyManager.Instance().setCurrentStrategyException(StrategysException.Init);
		InitialBuild.Instance().setInitialBuildOrder();	
		AnalyzeStrategy.Instance().AnalyzeEnemyStrategyInit();
		AnalyzeStrategy.Instance().AnalyzeEnemyStrategy();
		
		InitFaccnt = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory);
	}

	///  경기가 종료될 때 일회적으로 전략 결과 정리 관련 로직을 실행합니다
	public void onEnd(boolean isWinner) {
		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가
		// 과거 게임 기록 + 이번 게임 기록을 저장합니다
		//saveGameRecordList(isWinner);
		// BasicBot 1.1 Patch End //////////////////////////////////////////////////		
	}
	
	/// 경기 진행 중 매 프레임마다 경기 전략 관련 로직을 실행합니다
	public void update() {
		
		
		if(EXOK == false && MyBotModule.Broodwar.getFrameCount()%2 == 0){
			executeFirstex();
		}
		
//		lag.estimate("1");
		
		//TODO 전략은 자주 확인할 필요 없다, 1초에 한번 하지만@@!@!@ 초반에는 자주 확인해야된다 아래
		if ((MyBotModule.Broodwar.getFrameCount() < 13000 && MyBotModule.Broodwar.getFrameCount() % 5 == 0)
				||(MyBotModule.Broodwar.getFrameCount() >= 13000 && MyBotModule.Broodwar.getFrameCount() % 23 == 0)) {
			AnalyzeStrategy.Instance().AnalyzeEnemyStrategy();
		}
		
//		lag.estimate("2");
		MyBotModule.Broodwar.drawTextScreen(350, 100, "isInit=" + isInitialBuildOrderFinished);
		
		if (!isInitialBuildOrderFinished && BuildManager.Instance().buildQueue.isEmpty()) {
			if(isInitialBuildOrderFinished == false){
			}
			isInitialBuildOrderFinished = true;
		}
		
//		lag.estimate("3");

		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 29 == 0 && MyBotModule.Broodwar.getFrameCount() > 4500) {
			executeSupplyManagement();
		}
		if (MyBotModule.Broodwar.getFrameCount() % 43 == 0) {
			executeExpansion();
			executeResearch();
		}
		
//		lag.estimate("4");
		
		if(isInitialBuildOrderFinished == true){
			if(MyBotModule.Broodwar.getFrameCount() % 53 == 0) {
				executeUpgrade();
			}
		}
//		lag.estimate("5");
		
		if (MyBotModule.Broodwar.getFrameCount() % 31 == 0){// info 의 멀티 체크가 31 에 돈다 
			executeCombat();
		}
//		lag.estimate("6");
		
		if (isInitialBuildOrderFinished == false) {
			if (MyBotModule.Broodwar.getFrameCount() % 23 == 0){
				executeAddBuildingInit();
			}
		}else if (MyBotModule.Broodwar.getFrameCount() % 113 == 0) { //5초에 한번 팩토리 추가 여부 결정
			executeAddFactory();
		}
		
//		lag.estimate("7");
		
		if (MyBotModule.Broodwar.getFrameCount() % 5 == 0 && MyBotModule.Broodwar.getFrameCount() > 2500){
			executeWorkerTraining();
			
			executeCombatUnitTrainingBlocked();

			if (isInitialBuildOrderFinished == true) {
				executeCombatUnitTraining();
			}

		}
//		lag.estimate("8");
		if(MyBotModule.Broodwar.getFrameCount() % 239 == 0) {
			executeSustainUnits();
		}
		if(MyBotModule.Broodwar.getFrameCount() < 10000 ){
			if (MyBotModule.Broodwar.getFrameCount() % 29 == 0) {
				executeFly();
			}
		}else{
			if (MyBotModule.Broodwar.getFrameCount() % 281 == 0) {
				executeFly();
			}
		}
		
//		lag.estimate("9");
		
		if ((MyBotModule.Broodwar.getFrameCount() < 13000 && MyBotModule.Broodwar.getFrameCount() % 5 == 0)
				||(MyBotModule.Broodwar.getFrameCount() >= 13000 && MyBotModule.Broodwar.getFrameCount() % 23 == 0)) { //Analyze 와 동일하게
			RespondToStrategy.Instance().update();//다른 유닛 생성에 비해 제일 마지막에 돌아야 한다. highqueue 이용하면 제일 앞에 있을 것이므로
			//AnalyzeStrategy.Instance().AnalyzeEnemyStrategy();
		}
//		lag.estimate("10");
	}
	
	public static int least(double a, double b, double c, int checker){
		
		int ret=0;
		if(a>b){
			if(b>c){
				ret = 3;
			}else{
				ret = 2;
			}
		}else{
			if(a>c){
				ret = 3;
			}else{
				ret = 1;
			}
		}
		if(ret==1){
			if(a==b&&checker!=3){
				ret = checker;
			}else if(a==c&&checker!=2){
				ret = checker;
			}
		}else if(ret==2){
			if(b==a&&checker!=3){
				ret = checker;
			}else if(b==c&&checker!=1){
				ret = checker;
			}
		}else if(ret==3){
			if(c==a&&checker!=2){
				ret = checker;
			}else if(c==b&&checker!=1){
				ret = checker;
			}
		}
		return ret;
	}

	public static UnitType chooseunit(int ratea, int rateb, int ratec, int wgt, int tota, int totb, int totc){
		
		if( wgt < 1 || wgt > 3){
			wgt = 1;
		}
		double tempa = 0;
		double tempb = 0;
		double tempc = 0;
		if(ratea == 0){
			tempa = 99999999;	
		}else{
			tempa = 1.0/ratea*tota;
		}
		if(rateb == 0){
			tempb = 99999999;	
		}else{
			tempb = 1.0/rateb*totb;
		}
		if(ratec == 0){
			tempc = 99999999;	
		}else{
			tempc = 1.0/ratec*totc;
		}
		int num = least(tempa,tempb,tempc,wgt);
		if(num == 3){//1:벌쳐, 2:시즈, 3:골리앗
			return UnitType.Terran_Goliath;
		}else if(num == 2){
			return UnitType.Terran_Siege_Tank_Tank_Mode;
		}else{
			return UnitType.Terran_Vulture;
		}
	}
	
	private int GetCurrentTot(UnitType checkunit) {
		return BuildManager.Instance().buildQueue.getItemCount(checkunit) + 
				 MyBotModule.Broodwar.self().allUnitCount(checkunit);
		
//		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
//			if (unit.getType() == UnitType.Terran_Factory) {
//				if (unit.isTraining() &&  unit.get) {
//					cnt += unit.getTrainingQueue().size();//TODO trainingqueue 에 모가 있는지를 알수가 없다.
//				}
//			}
//		}
	}
	
	private int GetCurrentTotBlocked(UnitType checkunit) {
		int cnt = MyBotModule.Broodwar.self().allUnitCount(checkunit);
//		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
//			if (unit.getType() == UnitType.Terran_Factory && unit.isTraining()) {
//				if(unit.getTrainingQueue().size() > 0 && unit.getTrainingQueue().get(0) == checkunit){
//					cnt ++;
//				}
//			}
//		}
		return cnt;
	}
	
	// 일꾼 계속 추가 생산
	public void executeWorkerTraining() {

		if(MyBotModule.Broodwar.self().supplyTotal() - MyBotModule.Broodwar.self().supplyUsed() < 2){
			return;
		}
		
		if(EXOK==false){
			if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2){
				Unit checkCC =null;
				for(Unit unit : MyBotModule.Broodwar.self().getUnits()){
					
					if(unit.getType() != UnitType.Terran_Command_Center){
						continue;
					}
					if(unit.getTilePosition().getX() == BlockingEntrance.Instance().startingX 
						&& unit.getTilePosition().getY() == BlockingEntrance.Instance().startingY ){
						continue;
					}else {
						checkCC =unit;
						break;
					}
				}
				
				if(checkCC != null){
					BaseLocation temp = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
					if(checkCC.getTilePosition().getX() == temp.getTilePosition().getX()
							&& checkCC.getTilePosition().getY() == temp.getTilePosition().getY()){
							
					}else{
						BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
						BuildOrderItem checkItem = null; 
			
						if (!tempbuildQueue.isEmpty()) {
							checkItem= tempbuildQueue.getHighestPriorityItem();
							while(true){
								if(tempbuildQueue.canGetNextItem() == true){
									tempbuildQueue.canGetNextItem();
								}else{
									break;
								}
								tempbuildQueue.PointToNextItem();
								checkItem = tempbuildQueue.getItem();
								
								if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_SCV){
									tempbuildQueue.removeCurrentItem();
								}
							}
						}
						return;
					}
				}
			}
		}
		
		int tot_mineral_self = 0 ;
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType() == UnitType.Terran_Command_Center){
				int minerals = WorkerManager.Instance().getWorkerData().getMineralsNearDepot(unit);
				if(minerals > 0){
					if(unit.isCompleted() == false){
						minerals= minerals * unit.getHitPoints()/1500;
					}
					tot_mineral_self += minerals;
				}
			}
			
 		}

		if (MyBotModule.Broodwar.self().minerals() >= 50) {
			int maxworkerCount = tot_mineral_self * 2 + 8 * MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center);
			int workerCount = MyBotModule.Broodwar.self().allUnitCount(InformationManager.Instance().getWorkerType()); // workerCount = 현재 일꾼 수 + 생산중인 일꾼 수
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType().isResourceDepot()) {
					if (unit.isTraining()) {
						workerCount += unit.getTrainingQueue().size();
					}
				}
			}
			
			int nomorescv = 65;
			if(InformationManager.Instance().enemyRace == Race.Terran){
				nomorescv = 60;
			}
			//System.out.println("maxworkerCount: " + maxworkerCount);
			if (workerCount < nomorescv && workerCount < maxworkerCount) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType().isResourceDepot() && unit.isCompleted() && unit.isTraining() == false) {
						
						BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
						BuildOrderItem checkItem = null; 

						if (!tempbuildQueue.isEmpty()) {
							checkItem= tempbuildQueue.getHighestPriorityItem();
							while(true){
								if(checkItem.blocking == true){
									break;
								}
								if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_SCV){
									return;
								}
//								if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType().isAddon()){
//									return;
//								}
								if(tempbuildQueue.canSkipCurrentItem() == true){
									tempbuildQueue.skipCurrentItem();
								}else{
									break;
								}
								checkItem = tempbuildQueue.getItem();
							}
							if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_SCV){
								return;
							}
						}
						//System.out.println("checkItem: " + checkItem.metaType.getName());
						if (checkItem == null){
							BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
						}else if(checkItem.metaType.isUnit()){
							if(checkItem.metaType.getUnitType() == UnitType.Terran_Comsat_Station){
								return;
							}else if(checkItem.metaType.getUnitType() != UnitType.Terran_SCV){
								if(workerCount < 4){
									BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
								}else{
									int checkgas = checkItem.metaType.getUnitType().gasPrice() - MyBotModule.Broodwar.self().gas();
									if(checkgas < 0){
										checkgas = 0;
									}
									if(MyBotModule.Broodwar.self().minerals() > checkItem.metaType.getUnitType().mineralPrice()+50 - checkgas) {
										BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	//부족한 인구수 충원
	public void executeSupplyManagement() {
		
		BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
		BuildOrderItem checkItem = null; 

		if (!tempbuildQueue.isEmpty()) {
			checkItem= tempbuildQueue.getHighestPriorityItem();
			while(true){
				if(checkItem.blocking == true){
					break;
				}
//				if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType().isAddon()){
//					return;
//				}
				if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Missile_Turret){
					return;
				}
				if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Supply_Depot){
					return;
				}
				if(tempbuildQueue.canSkipCurrentItem() == true){
					tempbuildQueue.skipCurrentItem();
				}else{
					break;
				}
				checkItem = tempbuildQueue.getItem();
			}
			if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Supply_Depot){
				return;
			}
		}
		
		// 게임에서는 서플라이 값이 200까지 있지만, BWAPI 에서는 서플라이 값이 400까지 있다
		// 저글링 1마리가 게임에서는 서플라이를 0.5 차지하지만, BWAPI 에서는 서플라이를 1 차지한다
		if (MyBotModule.Broodwar.self().supplyTotal() < 400) {

			// 서플라이가 다 꽉찼을때 새 서플라이를 지으면 지연이 많이 일어나므로, supplyMargin (게임에서의 서플라이 마진 값의 2배)만큼 부족해지면 새 서플라이를 짓도록 한다
			// 이렇게 값을 정해놓으면, 게임 초반부에는 서플라이를 너무 일찍 짓고, 게임 후반부에는 서플라이를 너무 늦게 짓게 된다
			int supplyMargin = 4;
			boolean barrackflag = false;
			boolean factoryflag = false;

			if(factoryflag==false){
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType() == UnitType.Terran_Factory  && unit.isCompleted()) {
						factoryflag = true;
					}
					if (unit.getType() == UnitType.Terran_Barracks && unit.isCompleted()) {
						barrackflag = true;
					}
				}
			}
			
			int Faccnt=0;
			int CCcnt=0;
			int facFullOperating =0;
			for (Unit unit : MyBotModule.Broodwar.self().getUnits())
			{
				if (unit == null) continue;
				if (unit.getType().isResourceDepot() && unit.isCompleted()){
					CCcnt++;
				}
				if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted()){
					Faccnt ++;
					if(unit.isTraining() == true){
						facFullOperating++;
					}
				}
			}
						
			if(CCcnt == 1){//TODO 이거 현재는 faccnt cccnt 기준 안 먹는다. 기준 다시 잡아야됨
				if(factoryflag==false && barrackflag==true){
					supplyMargin = 5;
				}else if(factoryflag==true){
					supplyMargin = 6+4*Faccnt+facFullOperating*2;
				}
			}else{ //if((MyBotModule.Broodwar.getFrameCount()>=6000 && MyBotModule.Broodwar.getFrameCount()<10000) || (Faccnt > 3 && CCcnt == 2)){
				supplyMargin = 11+4*Faccnt+facFullOperating*2;
			}
			
			// currentSupplyShortage 를 계산한다
			int currentSupplyShortage = MyBotModule.Broodwar.self().supplyUsed() + supplyMargin + 1 - MyBotModule.Broodwar.self().supplyTotal();

			if (currentSupplyShortage > 0) {
				// 생산/건설 중인 Supply를 센다
				int onBuildingSupplyCount = 0;
				// 저그 종족이 아닌 경우, 건설중인 Protoss_Pylon, Terran_Supply_Depot 를 센다. Nexus, Command Center 등 건물은 세지 않는다
				onBuildingSupplyCount += ConstructionManager.Instance().getConstructionQueueItemCount(
						InformationManager.Instance().getBasicSupplyProviderUnitType(), null)
						* InformationManager.Instance().getBasicSupplyProviderUnitType().supplyProvided();

				if (currentSupplyShortage > onBuildingSupplyCount) {
					
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Supply_Depot,BuildOrderItem.SeedPositionStrategy.NextSupplePoint, false);
//					
////					boolean isToEnqueue = true;
////					if (!BuildManager.Instance().buildQueue.isEmpty()) {
////						BuildOrderItem currentItem = BuildManager.Instance().buildQueue.getHighestPriorityItem();
////						if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == InformationManager.Instance().getBasicSupplyProviderUnitType()) 
////						{
////							isToEnqueue = false;
////						}
////					}
//					if(InformationManager.Instance().getMainBaseSuppleLimit() <= MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Supply_Depot)){
////							MyBotModule.Broodwar.printf("currentSupplyShortage: " + currentSupplyShortage);
////							MyBotModule.Broodwar.printf("onBuildingSupplyCount: " + onBuildingSupplyCount);
////							MyBotModule.Broodwar.printf("supplyMargin: " + supplyMargin);
////							MyBotModule.Broodwar.printf("supplyTotal: " + MyBotModule.Broodwar.self().supplyTotal());
//						
//					}else{
////							MyBotModule.Broodwar.printf("currentSupplyShortage: " + currentSupplyShortage);
////							MyBotModule.Broodwar.printf("onBuildingSupplyCount: " + onBuildingSupplyCount);
////							MyBotModule.Broodwar.printf("supplyMargin: " + supplyMargin);
////							MyBotModule.Broodwar.printf("supplyTotal: " + MyBotModule.Broodwar.self().supplyTotal());
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Supply_Depot,BuildOrderItem.SeedPositionStrategy.NextSupplePoint, true);
//					}
				}
			}
		}
	}

	public void executeAddBuildingInit() {
		//팩토리
		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory, null) 
				+ MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Factory)
				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null)
				< InitFaccnt) {
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Factory,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		}
		//가스
		if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Refinery, null) 
				+ MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Refinery)
				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Refinery, null) == 0){
			if(InformationManager.Instance().isGasRushed() == false){
				int barrack=0;
				int supple=0;
				int scv=0;
				for (Unit unit : MyBotModule.Broodwar.self().getUnits())
				{
					if (unit == null) continue;
					if (unit.getType() == UnitType.Terran_Barracks){
						barrack++;
					}
					if (unit.getType() == UnitType.Terran_Supply_Depot  && unit.isCompleted()){
						supple++;
					}
					if (unit.getType() == UnitType.Terran_SCV && unit.isCompleted()){
						scv++;
					}
		 		}
				if(barrack > 0 && supple > 0 && scv>10){
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Refinery,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				}
			}
		}
	}
	
	public void executeSustainUnits() {
		
		boolean aca = false;
		boolean acaComplete = false;
		boolean engineering = false;
		boolean star = false;
		boolean starComplete = false;
		boolean science = false;
		boolean scienceComplete = false;
		boolean physiclab = false;
		boolean armory = false;
		boolean vessel = false;
		int barrackcnt = 0;
		int marinecnt = 0;
		int vulturecnt= 0;
		int wraithcnt = 0;
//		int valkyriecnt = 0;
//		int battlecnt =0;
		int engineeringcnt = 0;
		Unit starportUnit = null;
		Unit barrackUnit = null;
		Unit engineeringUnit = null;
		boolean  controltower = false;
		int CC =0;
	
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			
			//컴샛 start
			if(unit.getType() == UnitType.Terran_Academy){
				aca = true;
				if(unit.isCompleted()){
					acaComplete = true;
				}
			}
			//컴샛 end
			
			
			//engineering start
			if(unit.getType() == UnitType.Terran_Armory){
				armory = true;
			}
			//engineering end
			
			//scienceVessel start
			if(unit.getType() == UnitType.Terran_Starport){
				star = true;
				if(unit.isCompleted()){
					starportUnit = unit;
					starComplete = true;
				}
			}
			if(unit.getType() == UnitType.Terran_Control_Tower &&unit.isCompleted()){
				controltower = true;
			}
			if(unit.getType() == UnitType.Terran_Science_Facility){
				science = true;
				if(unit.isCompleted()){
					scienceComplete = true;
				}
			}
			if(unit.getType() == UnitType.Terran_Science_Vessel){
				vessel = true;
			}
			//scienceVessel end 	
			
			//battle start
//			if(unit.getType() == UnitType.Terran_Physics_Lab && unit.isCompleted()){
//				physiclab = true;
//			}
//			if(unit.getType() == UnitType.Terran_Battlecruiser && unit.isCompleted()){
//				battlecnt ++;
//			}
			//battle end
			
			//marine for fast zergling and zealot start
			if(unit.getType() == UnitType.Terran_Marine && unit.isCompleted()){
				marinecnt ++;
			}
			
			if(unit.getType() == UnitType.Terran_Vulture && unit.isCompleted()){
				vulturecnt ++;
			}
			//marine for fast zergling and zealot end
			
			//wraith for TvT start
			if(unit.getType() == UnitType.Terran_Wraith && unit.isCompleted()){
				wraithcnt ++;
			}
			//wraith for TvT end
			
			//barrack start
			if(unit.getType() == UnitType.Terran_Barracks && unit.isCompleted()){
				barrackcnt++;
				barrackUnit = unit;
			}
			//barrack end
			if(unit.getType() == UnitType.Terran_Engineering_Bay){
				
				engineering = true;
				if(unit.isCompleted()){
					engineeringUnit = unit;
					engineeringcnt++;
				}
			}
			
			
			//가스 start
			if (unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted() ){
				
				Unit geyserAround = null;
				
				boolean refineryAlreadyBuilt = false; 
				for (Unit unitsAround: MyBotModule.Broodwar.getUnitsInRadius(unit.getPosition(), 300)){
					if (unitsAround == null) continue;
					if(unitsAround.getType() == UnitType.Terran_Refinery 
							|| unitsAround.getType() == UnitType.Zerg_Extractor 
							|| unitsAround.getType() == UnitType.Protoss_Assimilator){
						refineryAlreadyBuilt = true;
						break;
					}			
					if(unitsAround.getType() == UnitType.Resource_Vespene_Geyser){
						geyserAround = unitsAround;
					}
				}
				
				if(isInitialBuildOrderFinished == false && geyserAround != null){
					if(InformationManager.Instance().getMyfirstGas() !=null){
						if(geyserAround.getPosition().equals(InformationManager.Instance().getMyfirstGas().getPosition())){
							continue;
						}
					}
				}
				
				if(refineryAlreadyBuilt ==false){
					TilePosition findGeyser = ConstructionPlaceFinder.Instance().getRefineryPositionNear(unit.getTilePosition());
					if(findGeyser != null){
						if (findGeyser.getDistance(unit.getTilePosition())*32 > 300){
							continue;
						}
						if(WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 9){
							if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Refinery) == 0) {
								BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery, findGeyser, false);
							}
						}
					}
				}
				
				if(WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 8){
					CC++;
				}
			}
			//가스 end
		}
		
		//컴샛 start
		if(aca == false){
			if(CC>=2 || MyBotModule.Broodwar.getFrameCount() > 15000){
				if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Academy) == 0 
						&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Academy) == 0
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Academy, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Academy, false);
				}
			}
		}
		if(acaComplete){
			if(EXOK==false){
				if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2){
					Unit checkCC =null;
					for(Unit checkunit : MyBotModule.Broodwar.self().getUnits()){
						
						if(checkunit.getType() != UnitType.Terran_Command_Center){
							continue;
						}
						if(checkunit.getTilePosition().getX() == BlockingEntrance.Instance().startingX 
							&& checkunit.getTilePosition().getY() == BlockingEntrance.Instance().startingY ){
							continue;
						}else {
							checkCC =checkunit;
							break;
						}
					}
					
					if(checkCC != null){
						BaseLocation temp = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
						if(checkCC.getTilePosition().getX() == temp.getTilePosition().getX()
								&& checkCC.getTilePosition().getY() == temp.getTilePosition().getY()){
								
						}else{
							BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
							BuildOrderItem checkItem = null; 
				
							if (!tempbuildQueue.isEmpty()) {
								checkItem= tempbuildQueue.getHighestPriorityItem();
								while(true){
									if(tempbuildQueue.canGetNextItem() == true){
										tempbuildQueue.canGetNextItem();
									}else{
										break;
									}
									tempbuildQueue.PointToNextItem();
									checkItem = tempbuildQueue.getItem();
									
									if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_SCV){
										tempbuildQueue.removeCurrentItem();
									}
								}
							}
							return;
						}
					}
				}
			}
			for (Unit unit : MyBotModule.Broodwar.self().getUnits())
			{
				if(unit == null) continue;
				if(unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted() && unit.canBuildAddon()
						&& MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Comsat_Station) < 4){
					
					if(MyBotModule.Broodwar.self().minerals() > 50 && MyBotModule.Broodwar.self().gas() > 50){
						if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Comsat_Station, null) 
								+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Comsat_Station, null) == 0){
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Comsat_Station, true);
							break;
						}
					}
				}
	 		}
		}
		//컴샛 end
		
		
		//barrack start
		if(isInitialBuildOrderFinished == true && (barrackcnt == 0 || (barrackcnt == 1 && barrackUnit.getHitPoints() < UnitType.Terran_Barracks.maxHitPoints()/2)) ){
			if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Barracks) == 0
					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Barracks, null) == 0) {
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, false);
			}
		}
		//barrack end
		
		//engineering start
		if(isInitialBuildOrderFinished == true && (engineeringcnt == 0 || (engineeringcnt == 1 && engineeringUnit.getHitPoints() < UnitType.Terran_Engineering_Bay.maxHitPoints()/2))
				&& MyBotModule.Broodwar.getFrameCount() > 11000){
			if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) == 0
					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0) {
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Engineering_Bay, false);
			}
		}
		//engineering end
		
		//engineering start1
		if(engineering == false){
			if(CC>=2 || MyBotModule.Broodwar.getFrameCount() > 17000){
				if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) == 0 
						&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) == 0
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Engineering_Bay, false);
				}
			}
		}
		//engineering end2
		
		//scienceVessel start
		if((RespondToStrategy.Instance().need_vessel == true && CC>=2) || CC>=3){
			if(star == false){
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) == 0
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Starport, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport, false);
				}
			}
			if(starComplete){
				//컨트롤 타워가 없다면
				if(starportUnit != null && starportUnit.canBuildAddon()){
					if(MyBotModule.Broodwar.self().minerals() > 50 && MyBotModule.Broodwar.self().gas() > 50){
						if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower, null) 
								+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Control_Tower, null) == 0){
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Control_Tower, true);
						}
					}
				}
			}
			if(science == false && starComplete){
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Science_Facility) == 0
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Science_Facility, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Facility, false);
				}
			}
			if(starComplete && scienceComplete && controltower){
				if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Science_Vessel) < RespondToStrategy.Instance().max_vessel){
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Science_Vessel, null) 
							+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Science_Vessel, null) == 0){
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Vessel, false);
					}
				}
			}
		}
		//scienceVessel end
		
		//armory start1
		if(armory == false){
			if(CC>=2 || MyBotModule.Broodwar.getFrameCount() > 15000){
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) == 0
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory, false);
				}
			}
		}
		//armory end2

		
		
		
		
		//battlecruiser start
//		System.out.println("need_battlecruiser: " + RespondToStrategy.Instance().need_battlecruiser);
//		System.out.println("CC: " + CC);
//		System.out.println("max_battlecruiser: " + RespondToStrategy.Instance().max_battlecruiser);
//		System.out.println("battlecnt: " + battlecnt);
		
//		if(RespondToStrategy.Instance().need_battlecruiser == true && CC>=3 && RespondToStrategy.Instance().max_battlecruiser > battlecnt){
//			
//			nomorewraithcnt = 100;
//			if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Starport) < 4){
//				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) == 0
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Starport, null) < 4) {
//					if(Config.BroodwarDebugYN){
//					MyBotModule.Broodwar.printf("make starport for battle");
//					}
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport, false);
//				}
//			}
//			if(starComplete){
//				//컨트롤 타워가 없다면
//				if(starportUnit != null && starportUnit.getAddon() == null){
//					if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Control_Tower) < 4){
//						if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower) == 0
//								&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Control_Tower, null) < 4) {
//							if(Config.BroodwarDebugYN){
//							MyBotModule.Broodwar.printf("make starport for battle");
//							}
//							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Control_Tower, false);
//						}
//					}
//				}
//			}
//			if(science == false && starComplete){
//				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Science_Facility) == 0
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Science_Facility, null) == 0) {
//					if(Config.BroodwarDebugYN){
//					MyBotModule.Broodwar.printf("make scienceFacil since we have many CC");
//					}
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Facility, false);
//				}
//			}
//			if(scienceComplete){
//				if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Physics_Lab) < 1){
//					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Physics_Lab, null) 
//							+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Physics_Lab, null) == 0){
//						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Physics_Lab, false);
//					}
//				}else if(physiclab){
//					if(MyBotModule.Broodwar.self().hasResearched(TechType.Yamato_Gun) == false && MyBotModule.Broodwar.self().isResearching(TechType.Yamato_Gun) ==false){
//						if(BuildManager.Instance().buildQueue.getItemCount(TechType.Yamato_Gun) == 0){
//							BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Yamato_Gun, false);
//						}
//					}
//						
//				}
//			}
//			
//			if(scienceComplete && physiclab){
//				if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Battlecruiser) < RespondToStrategy.Instance().max_battlecruiser){
//					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Battlecruiser, null) == 0) {
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Battlecruiser, false);
//					}
//				}
//			}
//		}
		//battlecruiser end
				
				
		//wraith for TvT start
		if(RespondToStrategy.Instance().max_wraith > wraithcnt && nomorewraithcnt <= RespondToStrategy.Instance().max_wraith){
			
			if(CC>=2){
				if(star == false){
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) == 0
							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Starport, null) == 0) {
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport, false);
					}
				}
			}
			if(starComplete){
				if(starportUnit.isTraining() == false && (MyBotModule.Broodwar.getFrameCount() - WraithTime > 2400 || wraithcnt < 1)){ //TODO && wraithcnt <= needwraith){
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Wraith, null) == 0) {
						WraithTime = MyBotModule.Broodwar.getFrameCount();
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Wraith, false);
						nomorewraithcnt++;
					}
				}
			}
		}
		//wraith for TvT end
		
//		//valkyrie for TvT start
//		if(RespondToStrategy.Instance().max_valkyrie > valkyriecnt && CC >2){
//			if(star == false){
//				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) == 0
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Starport, null) == 0) {
//					if(Config.BroodwarDebugYN){
//					MyBotModule.Broodwar.printf("make starport  for valkyrie");
//					}
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport, false);
//				}
//			}
//			if(armory == false){
//				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) == 0
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0) {
//					if(Config.BroodwarDebugYN){
//					MyBotModule.Broodwar.printf("make armory for valkyrie");
//					}
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory, false);
//				}
//			}
//			if(starComplete){
//				//컨트롤 타워가 없다면
//				if(starportUnit != null && starportUnit.getAddon() == null){
//					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower) == 0
//							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Control_Tower, null) == 0) {
//						if(Config.BroodwarDebugYN){
//						MyBotModule.Broodwar.printf("make Terran_Control_Tower  for valkyrie");
//						}
//						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Control_Tower, true);
//					}
//				}
//			}
//			if(starComplete && starportUnit.getAddon() != null && starportUnit.getAddon().isCompleted() == true){
//				if(starportUnit.isTraining() == false){// && valkyriecnt < needwraithvalkyriecnt){ //TODO 
//					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Valkyrie, null) == 0) {
//						if(Config.BroodwarDebugYN){
//						MyBotModule.Broodwar.printf("make valkyrie");
//						}
//						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Valkyrie, false);
//					}
//				}
//			}
//		}
		//valkyrie for TvT end
		
		
		
	}
	
	public void executeAddFactory() {
		
		int CCcnt = 0;
		int maxFaccnt = 0;
		int Faccnt = 0;
		int FaccntForMachineShop = 0;
		int MachineShopcnt = 0;
		boolean facFullOperating = true;

		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType().isResourceDepot() && unit.isCompleted()){
				CCcnt++;
			}
			if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted()){
				Faccnt ++;
				FaccntForMachineShop++;
				if (BuildManager.Instance().isBuildableTile(unit.getTilePosition().getX()+4, unit.getTilePosition().getY()+1) == false
						||BuildManager.Instance().isBuildableTile(unit.getTilePosition().getX()+5, unit.getTilePosition().getY()+1) == false
						||BuildManager.Instance().isBuildableTile(unit.getTilePosition().getX()+4, unit.getTilePosition().getY()+2) == false
						||BuildManager.Instance().isBuildableTile(unit.getTilePosition().getX()+5, unit.getTilePosition().getY()+2) == false)
				{
					FaccntForMachineShop--;
				}
			}
			if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted()){
				if(unit.isTraining() == false){
					facFullOperating = false;
				}
			}
			if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted() == false){
				facFullOperating = false;
			}
			if (unit.getType() == UnitType.Terran_Machine_Shop){
				MachineShopcnt ++;
			}
 		}
		
		if(CCcnt <= 1){
			if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
				maxFaccnt = 4;
			}else{
				maxFaccnt = 3;
			}
		}else if(CCcnt == 2){
			if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
				maxFaccnt = 6;
			}else{
				maxFaccnt = 6;
			}
		}else if(CCcnt >= 3){
			maxFaccnt = 9;
		}
		
		Faccnt += ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null);
				
		
		int additonalmin = 0;
		int additonalgas = 0;
		if(facFullOperating == false){
			additonalmin = (Faccnt-1)*40;
			additonalgas = (Faccnt-1)*20;
		}
		
		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory, null) == 0 ) {
			if(Faccnt == 0){
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Factory,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			}else if(Faccnt < maxFaccnt){
				if(MyBotModule.Broodwar.self().minerals() > 200 + additonalmin && MyBotModule.Broodwar.self().gas() > 100 + additonalgas){
					if(Faccnt == 2){
						if(getFacUnits()>24 && facFullOperating){
							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
						}
					}else if(Faccnt <= 6){
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
					}else{
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,BuildOrderItem.SeedPositionStrategy.LastBuilingPoint, false);
					}
				}
			}
		}
		
		if(MachineShopcnt < FaccntForMachineShop){
			for (Unit unit : MyBotModule.Broodwar.self().getUnits())
			{
				if (unit == null) continue;
				if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted()&& unit.isCompleted() &&unit.getAddon() ==null &&unit.canBuildAddon() ){
					int addition = 3;
					if( MyBotModule.Broodwar.self().gas() > 300){
						
						int tot_vulture = GetCurrentTotBlocked(UnitType.Terran_Vulture);
						int tot_tank = GetCurrentTotBlocked(UnitType.Terran_Siege_Tank_Tank_Mode) + GetCurrentTotBlocked(UnitType.Terran_Siege_Tank_Siege_Mode);
						int tot_goliath = GetCurrentTotBlocked(UnitType.Terran_Goliath);
						UnitType selected = chooseunit(vultureratio, tankratio, goliathratio, wgt, tot_vulture, tot_tank, tot_goliath);
						
						if(selected == UnitType.Terran_Siege_Tank_Tank_Mode){
							addition =0;
						}
					}
					
					if(MachineShopcnt + addition < FaccntForMachineShop){
						if(MyBotModule.Broodwar.self().minerals() > 50 && MyBotModule.Broodwar.self().gas() > 50){
							if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Machine_Shop, null) == 0
									&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Machine_Shop, null) == 0) {
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Machine_Shop, true);
								break;
							}
						}
					}
				}
			}
		}
	}
	
	public void setCombatUnitRatio(){
		vultureratio = 0;
		tankratio = 0;
		goliathratio = 0;
		wgt = 1;
		
		//config setting 가지고 오기
		if(CurrentStrategyException.toString() == "Init"){
			vultureratio = Config.vultureratio[CurrentStrategyBasic.ordinal()];
			tankratio = Config.tankratio[CurrentStrategyBasic.ordinal()];
			goliathratio = Config.goliathratio[CurrentStrategyBasic.ordinal()];
			wgt = Config.wgt[CurrentStrategyBasic.ordinal()];
		}else{
			vultureratio = Config.vultureratioexception[CurrentStrategyException.ordinal()];
			tankratio = Config.tankratioexception[CurrentStrategyException.ordinal()];
			goliathratio = Config.goliathratioexception[CurrentStrategyException.ordinal()];
			wgt = Config.wgtexception[CurrentStrategyException.ordinal()];
			
			if(vultureratio == 0 && tankratio == 0 && goliathratio == 0){
				vultureratio = Config.vultureratio[CurrentStrategyBasic.ordinal()];
				tankratio = Config.tankratio[CurrentStrategyBasic.ordinal()];
				goliathratio = Config.goliathratio[CurrentStrategyBasic.ordinal()];
				wgt = Config.wgt[CurrentStrategyBasic.ordinal()];
			}
		}
	}
	public int getFacUnits(){
		
		int tot=0;
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType() == UnitType.Terran_Vulture && unit.isCompleted() ){
				tot++;
			}
			if (unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode && unit.isCompleted() ){
				tot++;
			}
			if (unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode && unit.isCompleted() ){
				tot++;
			}
			if (unit.getType() == UnitType.Terran_Goliath && unit.isCompleted() ){
				tot++;
			}
 		}
		return tot*4; //인구수 기준이므로
	}

	public void executeCombatUnitTrainingBlocked() {
		
		BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
		BuildOrderItem currentItem = null; 
		Boolean goliathInTheQueue = false;
		Boolean tankInTheQueue = false;
		Boolean isfacexists = false;
		
		if(MyBotModule.Broodwar.self().supplyTotal() - MyBotModule.Broodwar.self().supplyUsed() < 4){
//			System.out.println("executeCombatUnitTrainingBlocked "+ new Exception().getStackTrace()[0].getLineNumber());
			return;
		}
		
		if (!tempbuildQueue.isEmpty()) {
			currentItem= tempbuildQueue.getHighestPriorityItem();
			while(true){
				
				
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Goliath){
					System.out.println("executeCombatUnitTrainingBlocked " + currentItem.metaType.getUnitType() + " "
					+ new Exception().getStackTrace()[0].getLineNumber());
					goliathInTheQueue = true;
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Siege_Tank_Tank_Mode){
					System.out.println("executeCombatUnitTrainingBlocked " + currentItem.metaType.getUnitType() + " "
					+ new Exception().getStackTrace()[0].getLineNumber());
					tankInTheQueue = true;
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Supply_Depot){
					System.out.println("executeCombatUnitTrainingBlocked " + currentItem.metaType.getUnitType() + " "
							+ new Exception().getStackTrace()[0].getLineNumber());
					return;
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType().isAddon()){
					System.out.println("executeCombatUnitTrainingBlocked " + currentItem.metaType.getUnitType() + " "
							+ new Exception().getStackTrace()[0].getLineNumber());
					return;
				}
//				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Missile_Turret){
//					System.out.println("executeCombatUnitTrainingBlocked " + currentItem.metaType.getUnitType() + " "
//							+ new Exception().getStackTrace()[0].getLineNumber());
//					return;
//				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Vulture){
					System.out.println("executeCombatUnitTrainingBlocked " + currentItem.metaType.getUnitType() + " "
							+ new Exception().getStackTrace()[0].getLineNumber());
					return;
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_SCV){
					System.out.println("executeCombatUnitTrainingBlocked " + currentItem.metaType.getUnitType() + " "
							+ new Exception().getStackTrace()[0].getLineNumber());
					return;
				}
				if(currentItem.blocking == true){
					break;
				}
				if(tempbuildQueue.canSkipCurrentItem() == true){
					tempbuildQueue.skipCurrentItem();
				}else{
					break;
				}
				currentItem = tempbuildQueue.getItem();
			}
		}else{
			System.out.println("executeCombatUnitTrainingBlocked isEmpty "+ new Exception().getStackTrace()[0].getLineNumber());
			return;
		}
		
		boolean isarmoryexists = false;
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType() == UnitType.Terran_Armory && unit.isCompleted()){
				isarmoryexists = true;
			}
			if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted()){
				isfacexists = true;
			}
		}
		if(isfacexists == false){
			System.out.println("executeCombatUnitTrainingBlocked "+ new Exception().getStackTrace()[0].getLineNumber());
			return;
		}
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted() && unit.isTraining() == false){
				
				if(unit.isConstructing() == true){
					continue;
				}
				
				//TODO else 가 들어가야할까. addon 있는놈일때는 신겨 안쓰게끔?
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Machine_Shop && unit.getAddon() == null ){
					continue;
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Siege_Tank_Tank_Mode){
					if(unit.getAddon() != null && unit.getAddon().isCompleted() != true){
						continue;
					}
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Goliath){
					if(isarmoryexists){
						break;
					}
				}
				
				boolean eventually_vulture = true;
				
				int tot_vulture = GetCurrentTotBlocked(UnitType.Terran_Vulture);
				int tot_tank = GetCurrentTotBlocked(UnitType.Terran_Siege_Tank_Tank_Mode) + GetCurrentTotBlocked(UnitType.Terran_Siege_Tank_Siege_Mode);
				int tot_goliath = GetCurrentTotBlocked(UnitType.Terran_Goliath);
				
				UnitType selected = null; 
				
				selected = chooseunit(vultureratio, tankratio, goliathratio, wgt, tot_vulture, tot_tank, tot_goliath);
				
				
				int minNeed = selected.mineralPrice();
//System.out.println("MyBotModule.Broodwar.self().supplyUsed()="+MyBotModule.Broodwar.self().supplyUsed());				
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType()!=selected){
					if(selected == UnitType.Terran_Siege_Tank_Tank_Mode && tankInTheQueue == false){
						if(unit.getAddon() != null && unit.getAddon().isCompleted() == true){
							if(currentItem.metaType.mineralPrice()+minNeed < MyBotModule.Broodwar.self().minerals() &&
									currentItem.metaType.gasPrice()+selected.gasPrice() < MyBotModule.Broodwar.self().gas() && MyBotModule.Broodwar.self().supplyUsed() <= 392){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(selected,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
								eventually_vulture = false;
							}
						}
					}else if(selected == UnitType.Terran_Goliath && goliathInTheQueue == false){
						if(isarmoryexists){
							if(currentItem.metaType.mineralPrice()+minNeed < MyBotModule.Broodwar.self().minerals() &&
									currentItem.metaType.gasPrice()+selected.gasPrice() < MyBotModule.Broodwar.self().gas() && MyBotModule.Broodwar.self().supplyUsed() <= 392){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(selected,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
								eventually_vulture = false;
							}
						}
					}
				}
				
				if(eventually_vulture){
					if(MyBotModule.Broodwar.self().gas() < 250){
						minNeed = 75;
					}
					
					if(currentItem.metaType.mineralPrice()+minNeed < MyBotModule.Broodwar.self().minerals() && MyBotModule.Broodwar.self().supplyUsed() <= 392){
						if((unit.isConstructing() == true) || ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Machine_Shop, null) != 0){
							continue;
						}
//						if(selected == UnitType.Terran_Goliath && isarmoryexists == false){
//							continue;
//						}
						if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture) == 0){
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Vulture,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
						}
					}
				}
			}
 		}
	}
	
	public void executeCombatUnitTraining() {

//		if(CurrentStrategyBasic == Strategys.terranBasic_BattleCruiser
//				&& MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Goliath) > 14
//				&& MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
//				+ MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)> 14){
//			return;
//		}
		int tot_vulture = GetCurrentTot(UnitType.Terran_Vulture);
		int tot_tank = GetCurrentTot(UnitType.Terran_Siege_Tank_Tank_Mode) + GetCurrentTot(UnitType.Terran_Siege_Tank_Siege_Mode);
		int tot_goliath = GetCurrentTot(UnitType.Terran_Goliath);
		
		UnitType selected = null; 
		int currentinbuildqueuecnt = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture, null) +
				BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Siege_Tank_Tank_Mode, null) +
				BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath, null);

		if(currentinbuildqueuecnt == 0){
			selected = chooseunit(vultureratio, tankratio, goliathratio, wgt, tot_vulture, tot_tank, tot_goliath);
			
			
			if(selected.mineralPrice() < MyBotModule.Broodwar.self().minerals() &&	selected.gasPrice() < MyBotModule.Broodwar.self().gas() && MyBotModule.Broodwar.self().supplyUsed() <= 392){
				BuildManager.Instance().buildQueue.queueAsLowestPriority(selected,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
			}
		}
	}
	
	public int getTotDeadCombatUnits(){
		
		int res =0;
		
		int totmarine = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Marine);
		int tottank = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
				+MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) ;
		int totgoliath = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Goliath);
		int totvulture = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Vulture);
		int totwraith = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Wraith);
		int totvessel = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Science_Vessel);
		
		
		res = tottank + totgoliath + totvulture + totwraith + totvessel;
		res = res*2 + totmarine;
		
		return res*2;//스타에서는 두배
	}
	
	public int getTotKilledCombatUnits(){
		
		int res = 0;
		
		if (InformationManager.Instance().enemyRace == Race.Terran) {
			int totbio = 0;
			
			int tottank = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
					+MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) ;
			int totgoliath = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Goliath);
			int totvulture = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Vulture);
			int totwraith = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Wraith);
			int totvessel = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Science_Vessel);
			totbio = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Terran_Marine)
					+ MyBotModule.Broodwar.self().killedUnitCount(UnitType.Terran_Firebat)
					+ MyBotModule.Broodwar.self().killedUnitCount(UnitType.Terran_Medic);

			res = tottank + totgoliath + totvulture + totwraith + totvessel;
			res = res*2 + totbio;
			
			return res*2;
			
		}else if (InformationManager.Instance().enemyRace == Race.Zerg) {
			
			int totzerling = 0;
			int tothydra = 0;
			int totmutal = 0;
			int totoverload = 0;
			int totlurker=0;
			
			//tot = MyBotModule.Broodwar.self().killedUnitCount(UnitType.AllUnits);
			totzerling = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Zergling);
			tothydra = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Hydralisk);
			totlurker = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Lurker) 
					+ MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Lurker_Egg);
			totmutal = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Mutalisk);
			totoverload = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Overlord);
			int totguardian = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Guardian);
			int totultra = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Ultralisk);
			int totsunken = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Sunken_Colony);

			res = totzerling + + totsunken * 2+ tothydra*2 + totmutal*4 + totoverload*3 + totlurker*5 + totguardian*8 + totultra*10;
			return res; //저그는 저글링 때문에 이미 2배 함
			
		}else if (InformationManager.Instance().enemyRace == Race.Protoss) {
			
//			tot = MyBotModule.Broodwar.self().killedUnitCount();
			
			int totzealot = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Protoss_Zealot);
			int totdragoon = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Protoss_Dragoon);
			int totarchon = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Protoss_Archon);
			int totphoto = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Protoss_Photon_Cannon);
			int tothigh = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Protoss_High_Templar);
			int totdark = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Protoss_Dark_Templar);
			int totcarrier = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Protoss_Carrier);

			res = (totzealot+totdragoon+totphoto+tothigh+totdark)*2 + totarchon*4 + totcarrier * 8;
			
			return res*2;
		}else{
			return 0;
		}
	}

	public boolean enemyExspansioning(){
		//int cnt = MyBotModule.Broodwar.enemy().incompleteUnitCount(InformationManager.Instance().getBasicResourceDepotBuildingType(InformationManager.Instance().enemyRace));
		List<UnitInfo> enemyCC = null;
		enemyCC = InformationManager.Instance().getEnemyUnits(InformationManager.Instance().getBasicResourceDepotBuildingType(InformationManager.Instance().enemyRace));
		
		for (UnitInfo target : enemyCC) {
			if(target.isCompleted()){
				return true;
			}
		}
		return false;
	}
	public int selfExspansioning(){
		
		int cnt=0;
		for(Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType() == UnitType.Terran_Command_Center){
				if(unit.isCompleted() == false){
					cnt++;
				}else if(WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) < 7){
					cnt++;
				}
			}
		}
		return cnt;
	}
	
	
	//TODO 상대방 신규 멀티를 찾았을때 공격 여부 한번더 돌려야함(상대 멀티 진행 여부 판단해야되므로
	public void executeCombat() {
		
		int unitPoint = 0;
		int expansionPoint = 0;
		int totPoint = 0;
		
		int selfbasecnt = 0;
		int enemybasecnt = 0;
		
		//boolean allaware = InformationManager.Instance().isReceivingEveryMultiInfo();
		
		//TODO 아군은 돌아가기 시작하고 scv 특정 수 이상 붙은 컴맨드만 쳐야 하지 않나?
		selfbasecnt = InformationManager.Instance().getOccupiedBaseLocationsCnt(InformationManager.Instance().selfPlayer);
		enemybasecnt = InformationManager.Instance().getOccupiedBaseLocationsCnt(InformationManager.Instance().enemyPlayer);
		//TODO상대가 다른곳에 파일론만 지으면.. 문제 되는데..
		//CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_INSIDE);
		
		if(enemybasecnt == 0){
			return;
		}
		
		int myunitPoint = getFacUnits();
	
		
		//공통 기본 로직, 팩토리 유닛 50마리 기준 유닛 Kill Death 상황에 따라 변경.
		
		int deadcombatunit = getTotDeadCombatUnits();
		int killedcombatunit = getTotKilledCombatUnits();
		int totworkerdead = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_SCV)*2;
		int totworkerkilled = MyBotModule.Broodwar.self().killedUnitCount(InformationManager.Instance().getWorkerType(InformationManager.Instance().enemyRace))*2;
		int totaldeadunit = MyBotModule.Broodwar.self().deadUnitCount();
		int totalkilledunit = MyBotModule.Broodwar.self().deadUnitCount();
		
		
		
		//if(selfbasecnt > enemybasecnt){ //약 시작 ~ 15분까지
		if(MyBotModule.Broodwar.getFrameCount() < 20000){ //약 시작 ~ 15분까지
			unitPoint += (totworkerkilled - totworkerdead) * (-MyBotModule.Broodwar.getFrameCount()/40000.0*3.0 + 3.0);
			unitPoint += (killedcombatunit - deadcombatunit);
			
		}else if(MyBotModule.Broodwar.getFrameCount() < 40000){ //약 15분 ~ 28분까지  // 여기서부턴 시간보다.... 현재 전체 규모수가 중요할듯?
			unitPoint += (totworkerkilled - totworkerdead) * (-MyBotModule.Broodwar.getFrameCount()/40000.0*3.0 + 3.0);
			unitPoint += (killedcombatunit - deadcombatunit)* (-MyBotModule.Broodwar.getFrameCount()/20000.0 + 2.0);
		}
		
		
		if(selfbasecnt == enemybasecnt && selfExspansioning() ==0 && enemyExspansioning() == false){ //베이스가 동일할때
			if (InformationManager.Instance().enemyRace == Race.Zerg || InformationManager.Instance().enemyRace == Race.Protoss) {
				expansionPoint += 5;
			}
		}else if(selfbasecnt > enemybasecnt){//우리가 베이스가 많으면
			if(selfbasecnt - enemybasecnt == 1){
				if(selfExspansioning() > 0){
					if(enemyExspansioning() == false){
						expansionPoint -= 15;
					}else{ 
						expansionPoint += 10;
					}
				}else if(selfExspansioning() == 0){
					if(enemyExspansioning() == true){
						expansionPoint += 40;
					}
					if(enemyExspansioning() == false){
						expansionPoint += 20;
					}
				}
			}
			if(selfbasecnt - enemybasecnt > 1 && selfbasecnt - enemybasecnt > selfExspansioning()){
				expansionPoint += 20+ 20*(selfbasecnt - enemybasecnt);
			}
		}else if(selfbasecnt < enemybasecnt){//우리가 베이스가 적으면
			if(enemybasecnt - selfbasecnt == 1){
				if(selfExspansioning() > 0){ 
					if(enemyExspansioning() == false){
						expansionPoint -= 20;
						if (InformationManager.Instance().enemyRace == Race.Zerg || InformationManager.Instance().enemyRace == Race.Protoss) {
							expansionPoint += 5;
						}
					}else{
						expansionPoint -= 10;
					}
				}else{
					if(enemyExspansioning() == true){
						expansionPoint += 10;
					}
				}
			}else{
				expansionPoint -= 20;
			}
		}
		
		//종족별 예외 상황
		if (InformationManager.Instance().enemyRace == Race.Terran) {
			if( myunitPoint > 80 && killedcombatunit-deadcombatunit > myunitPoint/4 ){//죽인수 - 죽은수 가  현재 내 유닛의 일정 비율이 넘으면 가산점
				unitPoint += 20;
			}
		}
		if (InformationManager.Instance().enemyRace == Race.Protoss) {
			if( myunitPoint > 80 && killedcombatunit-deadcombatunit > myunitPoint/3 ){//죽인수 - 죽은수 가  현재 내 유닛의 일정 비율이 넘으면 가산점
				unitPoint += 20;
			}
			
			unitPoint += InformationManager.Instance().getNumUnits(UnitType.Protoss_Photon_Cannon,InformationManager.Instance().enemyPlayer)*4;
		}
				
		if (InformationManager.Instance().enemyRace == Race.Zerg) {
			if( myunitPoint > 80 && killedcombatunit-deadcombatunit > myunitPoint/3 ){//죽인수 - 죽은수 가  현재 내 유닛의 일정 비율이 넘으면 가산점
				unitPoint += 20;
			}
			unitPoint += InformationManager.Instance().getNumUnits(UnitType.Zerg_Sunken_Colony,InformationManager.Instance().enemyPlayer)*4;
			//triple hatchery
		}

		//내 팩토리 유닛 인구수 만큼 추가
		totPoint = 	myunitPoint + expansionPoint + unitPoint;
		
		
	
		if(InformationManager.Instance().enemyRace == Race.Terran){
			int plus=0;
			if( CurrentStrategyBasic == StrategyManager.Strategys.terranBasic_Bionic){
				plus = 2;
			}
			
			if(unitPoint > 10 &&  MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)+MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) >= 4+plus){
				CombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
			}else if(unitPoint > 0 &&  MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)+MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) >= 5+plus){
				CombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
			}else if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)+MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) >= 6+plus){
				CombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
			}
			
			int CC = MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center);
			if(CombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY && unitPoint <0){
				if( CC == 1 && myunitPoint + unitPoint < 0){
					CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
				}
				if( CC == 2 && myunitPoint + unitPoint/2 < 0){
					CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
				}
				if( CC > 3 && myunitPoint < 20){
					CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
				}
			}
			if(CC > 4){
				CC= 4;
			}
			if((myunitPoint > 250 - CC*10 || MyBotModule.Broodwar.self().supplyUsed() > 392 )){
				CombatManager.Instance().pushSiegeLine = true;
				CombatStartCase = 1;
			}
			
			if(CombatStartCase == 1 && myunitPoint < 90){
				CombatManager.Instance().pushSiegeLine = false;
			}
			
			if(myunitPoint > 100 && unitPoint > 40){
				CombatManager.Instance().pushSiegeLine = true;
				CombatStartCase = 2;
			}
			if(CombatStartCase == 2 && unitPoint < 10){
				CombatManager.Instance().pushSiegeLine = false;
			}
			
			
		}else{
			//공통 예외 상황
			if((myunitPoint > 170 || MyBotModule.Broodwar.self().supplyUsed() > 392 )&& CombatManager.Instance().getCombatStrategy() != CombatStrategy.ATTACK_ENEMY){//팩토리 유닛  130 이상 또는 서플 196 이상 사용시(스타 내부에서는 2배)
	
				CombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
				CombatStartCase = 1;
			}
			
			
			if(totPoint > 120 && CombatManager.Instance().getCombatStrategy() != CombatStrategy.ATTACK_ENEMY && myunitPoint > 80){// 팩토리 유닛이 30마리(즉 스타 인구수 200 일때)
				
				if (InformationManager.Instance().enemyRace == Race.Zerg && InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk,InformationManager.Instance().enemyPlayer) > 6) {
					if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk,InformationManager.Instance().enemyPlayer) <
					MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Goliath)){
						CombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
					}
					//triple hatchery
				}else{
					CombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
				}
				CombatStartCase = 2;
			}
			
			if((LastStrategyBasic == StrategyManager.Strategys.protossBasic_DoublePhoto || CurrentStrategyBasic == StrategyManager.Strategys.protossBasic_DoublePhoto) 
					&& CombatManager.Instance().getCombatStrategy() != CombatStrategy.ATTACK_ENEMY
					&& MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
					+MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) >= 1) {
				CombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
				CombatStartCase=5;
				CombatManager.Instance().setDetailStrategy(CombatStrategyDetail.NO_CHECK_NO_GUERILLA, 500*24);
				CombatManager.Instance().setDetailStrategy(CombatStrategyDetail.NO_WAITING_CHOKE, 500*24);
				
				if(CombatTime ==0 ){
					CombatTime = MyBotModule.Broodwar.getFrameCount() + 5000;
				}
			}
			
			if(CombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY){
				if(CombatStartCase == 1 && myunitPoint < 30){
					CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
				}
				if(CombatStartCase == 2){
					
					if (InformationManager.Instance().enemyRace == Race.Zerg && InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk,InformationManager.Instance().enemyPlayer) > 6) {
						if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk,InformationManager.Instance().enemyPlayer) >
						MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Goliath)){
							CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
						}
						//triple hatchery
						if(totPoint < 50){
							CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
						}
					}else
					if(totPoint < 50){
						CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
					}
				}
				if(CombatStartCase == 5){
					
					if(CombatTime < MyBotModule.Broodwar.getFrameCount() && ((myunitPoint < 20 && unitPoint <20) || unitPoint < -10)){
					
						CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
						if(CurrentStrategyBasic == StrategyManager.Strategys.protossBasic_DoublePhoto){
							setCurrentStrategyBasic(CurrentStrategyBasic.protossBasic);
						}
					}
					
					if(InformationManager.Instance().getNumUnits(UnitType.Protoss_Zealot,InformationManager.Instance().enemyPlayer)
							+ MyBotModule.Broodwar.enemy().deadUnitCount(UnitType.Protoss_Zealot)
							+ InformationManager.Instance().getNumUnits(UnitType.Protoss_Dragoon,InformationManager.Instance().enemyPlayer)
							+ MyBotModule.Broodwar.enemy().deadUnitCount(UnitType.Protoss_Dragoon) > 20
							){
						if(totPoint < 50){
							CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
							if(CurrentStrategyBasic == StrategyManager.Strategys.protossBasic_DoublePhoto){
								setCurrentStrategyBasic(CurrentStrategyBasic.protossBasic);
							}
						}
					}
				}
	//			if(CombatStartCase == 3 && (myunitPoint < 8 || unitPoint < -20) ){
	//				if(Config.BroodwarDebugYN){
	//					MyBotModule.Broodwar.printf("Retreat3");
	//				}
	//				CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
	//			}
	//			if(CombatStartCase == 4 && myunitPoint < 60){
	//				if(Config.BroodwarDebugYN){
	//					MyBotModule.Broodwar.printf("Retreat4");
	//				}
	//				LastStrategyException = StrategyManager.StrategysException.Init;
	//				CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
	//			}
				
				if (InformationManager.Instance().enemyRace == Race.Protoss) {
					if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Science_Vessel) ==0	&& MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Comsat_Station) ==0){
						for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
							if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing) && unit.getPosition().isValid() && unit.isFlying() ==false) {
								MyBotModule.Broodwar.printf("dark and no comsat or vessel");
								CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
							}
						}
					}else if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Comsat_Station) > 0){
						boolean energy = false;
						boolean dark = false;
						for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
							if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing) && unit.getPosition().isValid() && unit.isFlying() ==false) {
								dark = true;
							}
						}
						if(dark){
							for(Unit myunit : MyBotModule.Broodwar.self().getUnits()){
								if(myunit.getType() == UnitType.Terran_Comsat_Station && myunit.isCompleted() && myunit.getEnergy() > 50){
									energy = true;
								}
							}
							if(energy == false){
								CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
							}
						}
					}
				}
				
				if( CombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY && expansionPoint >= 0 && myunitPoint > 120	&& unitPoint + totPoint*0.1 > 55){
					if(InformationManager.Instance().enemyRace == Race.Protoss && InformationManager.Instance().getNumUnits(UnitType.Protoss_Photon_Cannon, InformationManager.Instance().enemyPlayer) <=3) {
						CombatManager.Instance().setDetailStrategy(CombatStrategyDetail.ATTACK_NO_MERCY, 500*24);
					}
					if(InformationManager.Instance().enemyRace == Race.Zerg && InformationManager.Instance().getNumUnits(UnitType.Zerg_Sunken_Colony, InformationManager.Instance().enemyPlayer) <=3) {
						CombatManager.Instance().setDetailStrategy(CombatStrategyDetail.ATTACK_NO_MERCY, 500*24);
					}
					
				}
				
				if(CombatManager.Instance().getCombatStrategy() == CombatStrategy.DEFENCE_CHOKEPOINT || (CombatManager.Instance().getDetailStrategyFrame(CombatStrategyDetail.ATTACK_NO_MERCY) > 0 && (expansionPoint < 0 || (myunitPoint < 40 || unitPoint < 10)))){
					CombatManager.Instance().setDetailStrategy(CombatStrategyDetail.ATTACK_NO_MERCY, 0);
				}
			}
		}
		
		MyunitPoint = myunitPoint;
		ExpansionPoint = expansionPoint;
		UnitPoint = unitPoint;
		Attackpoint = totPoint;
	}
	
	public void executeUpgrade() {

		Unit armory = null;
		for(Unit unit : MyBotModule.Broodwar.self().getUnits()){
			if(unit.getType() == UnitType.Terran_Armory){
				armory = unit;
			}
		}
		if(armory == null){
			return;
		}

		boolean standard = false;
		if(getFacUnits() > 42 || (UnitPoint > 10 && getFacUnits() > 30)){
			standard = true;
		}
		if(UnitPoint < -10 && getFacUnits() < 60){
			standard = false;
		}
		//Fac Unit 18 마리 이상 되면 1단계 업그레이드 시도
		if(MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) ==0 && standard  && armory.canUpgrade(UpgradeType.Terran_Vehicle_Weapons) 
				&& MyBotModule.Broodwar.self().minerals()> 100 && MyBotModule.Broodwar.self().gas()> 100){
			if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Weapons) == 0) {
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Weapons, false);
			}
		}else if(MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) ==0 && standard && armory.canUpgrade(UpgradeType.Terran_Vehicle_Plating)
				&& MyBotModule.Broodwar.self().minerals()> 100 && MyBotModule.Broodwar.self().gas()> 100){
			if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Plating) == 0) {
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Plating, false);
			}
		}
		//Fac Unit 30 마리 이상, 일정 이상의 자원 2단계
		else if(MyBotModule.Broodwar.self().minerals()> 250 && MyBotModule.Broodwar.self().gas()> 225 ){
			if((MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) >= 2 && getFacUnits() > 140)
					||(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) >= 3 && getFacUnits() > 80)){
			
				if(MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) == 1 && armory.canUpgrade(UpgradeType.Terran_Vehicle_Weapons)){
					if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Weapons) == 0) {
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Weapons, false);
					}
				}else if(MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) == 1 && armory.canUpgrade(UpgradeType.Terran_Vehicle_Plating)){
					if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Plating) == 0) {
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Plating, false);
					}
				}else if(MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) == 2 && armory.canUpgrade(UpgradeType.Terran_Vehicle_Weapons)){//3단계
					if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Weapons) == 0) {
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Weapons, false);
					}
				}else if(MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) == 2 && armory.canUpgrade(UpgradeType.Terran_Vehicle_Plating)){
					if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Plating) == 0) {
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Plating, false);
					}
				}
			}
 		}
	}
	
	public void executeResearch() {
		
		boolean VS = (MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Ion_Thrusters) == 1 ? true : false)
				||(MyBotModule.Broodwar.self().isUpgrading(UpgradeType.Ion_Thrusters) ? true : false);
		boolean VM = (MyBotModule.Broodwar.self().hasResearched(TechType.Spider_Mines))
				||(MyBotModule.Broodwar.self().isResearching(TechType.Spider_Mines));
		boolean TS = (MyBotModule.Broodwar.self().hasResearched(TechType.Tank_Siege_Mode))
				||(MyBotModule.Broodwar.self().isResearching(TechType.Tank_Siege_Mode));
		boolean GR = (MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Charon_Boosters) == 1 ? true : false)
				||(MyBotModule.Broodwar.self().isUpgrading(UpgradeType.Charon_Boosters) ? true : false);
		
		if(VS&&VM&&TS&&GR) return; // 4개 모두 완료이면
		
		int currentResearched =0;
		if(VS){	currentResearched++;}
		if(VM){	currentResearched++;}
		if(TS){	currentResearched++;}
		if(GR){	currentResearched++;}
		
		if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) < 2 
				&& currentResearched >= 2 
				&& getFacUnits() < 32 
				&& !(MyBotModule.Broodwar.self().minerals()> 300 && MyBotModule.Broodwar.self().gas()> 250)) return;
		
		MetaType vultureSpeed = new MetaType(UpgradeType.Ion_Thrusters);
		MetaType vultureMine = new MetaType(TechType.Spider_Mines);
		MetaType TankSiegeMode = new MetaType(TechType.Tank_Siege_Mode);
		MetaType GoliathRange = new MetaType(UpgradeType.Charon_Boosters);
		
		MetaType vsZerg[] = new MetaType[]{vultureMine, GoliathRange, TankSiegeMode, vultureSpeed};
		boolean vsZergbool[] = new boolean[]{VM, GR, TS, VS};
		MetaType vsZergHydra[] = new MetaType[]{vultureMine, TankSiegeMode, GoliathRange, vultureSpeed};
		boolean vsZergHydrabool[] = new boolean[]{VM, TS, GR, VS};
		MetaType vsZergLurker[] = new MetaType[]{TankSiegeMode, GoliathRange, vultureMine, vultureSpeed};
		boolean vsZergLurkerbool[] = new boolean[]{TS, GR, VM, VS};
		MetaType vsTerran[] = new MetaType[]{vultureMine, TankSiegeMode, vultureSpeed, GoliathRange};
		boolean vsTerranbool[] = new boolean[]{VM, TS, VS, GR};
		MetaType vsTerranBio[] = new MetaType[]{vultureSpeed, TankSiegeMode, vultureMine, GoliathRange};
		boolean vsTerranBiobool[] = new boolean[]{VS, TS, VM, GR};
//		MetaType vsProtoss[] = new MetaType[]{vultureMine, vultureSpeed, TankSiegeMode, GoliathRange};
//		boolean vsProtossbool[] = new boolean[]{VM, VS, TS, GR};
		MetaType vsProtoss[] = new MetaType[]{TankSiegeMode, vultureMine, vultureSpeed, GoliathRange};
		boolean vsProtossbool[] = new boolean[]{TS, VM, VS, GR};
		MetaType vsProtossZealot[] = new MetaType[]{vultureSpeed, vultureMine, TankSiegeMode, GoliathRange};
		boolean vsProtossZealotbool[] = new boolean[]{VS, VM, TS, GR};
		MetaType vsProtossDragoon[] = new MetaType[]{TankSiegeMode, vultureMine, vultureSpeed, GoliathRange};
		boolean vsProtossDragoonbool[] = new boolean[]{TS, VM, VS, GR};
		MetaType vsProtossDouble[] = new MetaType[]{vultureMine, TankSiegeMode, vultureSpeed, GoliathRange};
		boolean vsProtossDoublebool[] = new boolean[]{VM, TS, VS, GR};
		MetaType vsProtossBasic_DoublePhoto[] = new MetaType[]{TankSiegeMode, vultureSpeed, vultureMine, GoliathRange};
		boolean vsProtossBasic_DoublePhotobool[] = new boolean[]{TS, VS, VM, GR};
		
		
		MetaType[] Current = null;
		boolean[] Currentbool = null;
		boolean air = true;
		boolean terranBio = false;
		
		if(InformationManager.Instance().enemyRace == Race.Protoss){
			Current = vsProtoss;
			Currentbool = vsProtossbool;
			if(CurrentStrategyException == StrategyManager.StrategysException.protossException_ZealotPush
					|| (CurrentStrategyException == StrategyManager.StrategysException.Init
					&& LastStrategyException == StrategyManager.StrategysException.protossException_ZealotPush)){
				Current = vsProtossZealot;
				Currentbool = vsProtossZealotbool;
			}
//			if(CurrentStrategyException == StrategyManager.StrategysException.protossException_PhotonRush
//					|| (CurrentStrategyException == StrategyManager.StrategysException.Init
//					    && LastStrategyException == StrategyManager.StrategysException.protossException_PhotonRush)){
//				Current = vsProtossPhoto;
//				Currentbool = vsProtossPhotobool;
//			}
			if((CurrentStrategyException == StrategyManager.StrategysException.protossException_DragoonPush
					|| (CurrentStrategyException == StrategyManager.StrategysException.Init
					&& LastStrategyException == StrategyManager.StrategysException.protossException_DragoonPush))
				|| (CurrentStrategyException == StrategyManager.StrategysException.protossException_PhotonRush
				|| (CurrentStrategyException == StrategyManager.StrategysException.Init
			    && LastStrategyException == StrategyManager.StrategysException.protossException_PhotonRush))
				){
				Current = vsProtossDragoon;
				Currentbool = vsProtossDragoonbool;
			}
			if(CurrentStrategyException == StrategyManager.StrategysException.protossException_DoubleNexus
					|| (CurrentStrategyException == StrategyManager.StrategysException.Init
					    && LastStrategyException == StrategyManager.StrategysException.protossException_DoubleNexus)){
				Current = vsProtossDouble;
				Currentbool = vsProtossDoublebool;
			}
			
			if(CurrentStrategyBasic == StrategyManager.Strategys.protossBasic_DoublePhoto){
				Current = vsProtossBasic_DoublePhoto;
				Currentbool = vsProtossBasic_DoublePhotobool;
			}
			
			
			
			air = false;
			for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()){
				if(unit.getType() == UnitType.Protoss_Stargate
						|| unit.getType() == UnitType.Protoss_Arbiter
						|| unit.getType() == UnitType.Protoss_Carrier
						|| unit.getType() == UnitType.Protoss_Corsair
						|| unit.getType() == UnitType.Protoss_Scout
						|| unit.getType() == UnitType.Protoss_Arbiter_Tribunal
						|| unit.getType() == UnitType.Protoss_Fleet_Beacon
						|| unit.getType() == UnitType.Protoss_Shuttle
						){
					air = true;
				}
			}
		}else if(InformationManager.Instance().enemyRace == Race.Terran){
			Current = vsTerran;
			Currentbool = vsTerranbool;
			if(CurrentStrategyBasic == Strategys.terranBasic_Bionic){
				Current = vsTerranBio;
				Currentbool = vsTerranBiobool;
				terranBio = true;
			}
			air = false;
			for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()){
				if(unit.getType() == UnitType.Terran_Starport
						|| unit.getType() == UnitType.Terran_Science_Facility
						|| unit.getType() == UnitType.Terran_Dropship
						|| unit.getType() == UnitType.Terran_Science_Vessel
						|| unit.getType() == UnitType.Terran_Wraith
						|| unit.getType() == UnitType.Terran_Battlecruiser
						|| unit.getType() == UnitType.Terran_Physics_Lab
						|| unit.getType() == UnitType.Terran_Control_Tower
						){
					air = true;
				}
			}
		}else {
			Current = vsZerg;
			Currentbool = vsZergbool;
			for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()){
				if(unit.getType() == UnitType.Zerg_Mutalisk
						|| unit.getType() == UnitType.Zerg_Lair
						|| unit.getType() == UnitType.Zerg_Spire
						|| unit.getType() == UnitType.Zerg_Scourge
						|| unit.getType() == UnitType.Zerg_Guardian
						|| unit.getType() == UnitType.Zerg_Devourer
						){
					air = true;
				}
			}
			if(CurrentStrategyBasic == Strategys.zergBasic_HydraWave
					|| CurrentStrategyBasic == Strategys.zergBasic_LingHydra
					){
				Current = vsZergHydra;
				Currentbool = vsZergHydrabool;
			}
			
			if(CurrentStrategyException == StrategysException.zergException_PrepareLurker){
				Current = vsZergLurker;
				Currentbool = vsZergLurkerbool;
			}
		}
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			
			if (unit.getType() == UnitType.Terran_Machine_Shop && unit.isCompleted() && unit.canUpgrade()){
				if(Currentbool == null) return;
				for(int i = 0; i<4; i++){
					if(Currentbool[i] == true){
						continue;
					}else{
						if(i==3 && Current[3].getUpgradeType() == UpgradeType.Charon_Boosters){
							if(!air && !(MyBotModule.Broodwar.self().minerals()> 200 && MyBotModule.Broodwar.self().gas()> 150)){
								continue;
							}
						}
						if(terranBio && i==2 && Current[2].getTechType() == TechType.Spider_Mines){
							if((getFacUnits() > 48 && MyBotModule.Broodwar.self().minerals()> 200 && MyBotModule.Broodwar.self().gas()> 150) || getFacUnits() > 100 ){
								
							}else{
								continue;
							}
						}
						if(BuildManager.Instance().buildQueue.getItemCount(Current[i]) == 0) {
							UpgradeType tempU = null;
							if(Current[i].isUpgrade()){
								boolean booster = false;
								for (Unit unitcheck : MyBotModule.Broodwar.self().getUnits())
								{
									if(unitcheck.getType() == UnitType.Terran_Armory && unitcheck.isCompleted()){
										booster = true;
									}
								}
								tempU = Current[i].getUpgradeType();
								if(tempU == UpgradeType.Charon_Boosters && booster == false){
									 return;
								}
							}
							if(currentResearched<=2){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(Current[i], true);
							}else{
								BuildManager.Instance().buildQueue.queueAsLowestPriority(Current[i], false);
							}
						}
						break;
					}
				}
			}
		}
	}
	
	public int getValidMineralsForExspansionNearDepot(Unit depot)
	{
		if (depot == null) { return 0; }

		int mineralsNearDepot = 0;

		for (Unit unit : MyBotModule.Broodwar.getAllUnits())
		{
			if ((unit.getType() == UnitType.Resource_Mineral_Field) && unit.getDistance(depot) < 450 && unit.getResources() > 200)
			{
				mineralsNearDepot++;
			}
		}

		return mineralsNearDepot;
	}
	public void executeExpansion() {
		
		if (MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) >= 2) {
			if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) > 0) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit == null)
						continue;
					
					// 멀티이후 Command_Center 근처에 일정양의 미사일터렛을 건설한다.
					if (unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted()) {
						int build_turret_cnt = 0;
						List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(unit.getPosition(), 8 * 32);
						build_turret_cnt = 0;
						for (Unit unit2 : turretInRegion) {
							if (unit2.getType() == UnitType.Terran_Missile_Turret) {
								build_turret_cnt++;
							}
						}
						
						if (build_turret_cnt < 2) {
							if (BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret,
									unit.getPosition().toTilePosition(), 10) < 1
									&& ConstructionManager.Instance().getConstructionQueueItemCountNear(
											UnitType.Terran_Missile_Turret, unit.getPosition().toTilePosition(),
											10) == 0) {
								System.out.println("executeExpansion=(" + unit.getTilePosition().getX() + ","
										+ unit.getTilePosition().getY() + ") "
										+ new Exception().getStackTrace()[0].getLineNumber());

								//첫번째 터렛 강제로 해보고 건설이 안되는 위치이면 자동위치 건설로
								TilePosition nearTilePosition = new TilePosition(unit.getTilePosition().getX() + 4,
										unit.getTilePosition().getY() - 1);
								if (MyBotModule.Broodwar.canBuildHere(nearTilePosition, UnitType.Terran_Missile_Turret)) {
									BuildManager.Instance().buildQueue.queueAsHighestPriority(
											UnitType.Terran_Missile_Turret, nearTilePosition, true, true);// 강제건설
									System.out.println("executeExpansion=(" + unit.getTilePosition().getX() + ","
											+ unit.getTilePosition().getY() + ") "
											+ new Exception().getStackTrace()[0].getLineNumber());
								} else {
									BuildManager.Instance().buildQueue.queueAsHighestPriority(
											UnitType.Terran_Missile_Turret, nearTilePosition, true);//자동위치건설
									System.out.println("executeExpansion=(" + unit.getTilePosition().getX() + ","
											+ unit.getTilePosition().getY() + ") "
											+ new Exception().getStackTrace()[0].getLineNumber());
								}

//								//두번째 터렛 강제로 해보고 건설이 안되는 위치이면 자동위치 건설로
//								TilePosition nearTilePosition2 = new TilePosition(unit.getTilePosition().getX() - 1,
//										unit.getTilePosition().getY() - 2);
//								if (MyBotModule.Broodwar.canBuildHere(nearTilePosition2, UnitType.Terran_Missile_Turret)) {
//									BuildManager.Instance().buildQueue.queueAsHighestPriority(
//											UnitType.Terran_Missile_Turret, nearTilePosition2, true, true);// 강제건설
//									System.out.println("executeExpansion=(" + unit.getTilePosition().getX() + ","
//											+ unit.getTilePosition().getY() + ") "
//											+ new Exception().getStackTrace()[0].getLineNumber());
//								} else {
//									BuildManager.Instance().buildQueue.queueAsHighestPriority(
//											UnitType.Terran_Missile_Turret, nearTilePosition2, true);//자동위치건설
//									System.out.println("executeExpansion=(" + unit.getTilePosition().getX() + ","
//											+ unit.getTilePosition().getY() + ") "
//											+ new Exception().getStackTrace()[0].getLineNumber());
//								}

								//세번째 터렛 강제로 해보고 건설이 안되는 위치이면 자동위치 건설로
								TilePosition nearTilePosition3 = new TilePosition(unit.getTilePosition().getX(),
										unit.getTilePosition().getY() + 3);
								if (MyBotModule.Broodwar.canBuildHere(nearTilePosition3, UnitType.Terran_Missile_Turret)) {
									BuildManager.Instance().buildQueue.queueAsHighestPriority(
											UnitType.Terran_Missile_Turret, nearTilePosition3, true, true);// 강제건설
									System.out.println("executeExpansion=(" + unit.getTilePosition().getX() + ","
											+ unit.getTilePosition().getY() + ") "
											+ new Exception().getStackTrace()[0].getLineNumber());
								} else {
									BuildManager.Instance().buildQueue.queueAsHighestPriority(
											UnitType.Terran_Missile_Turret, nearTilePosition3, true);//자동위치건설
									System.out.println("executeExpansion=(" + unit.getTilePosition().getX() + ","
											+ unit.getTilePosition().getY() + ") "
											+ new Exception().getStackTrace()[0].getLineNumber());
								}
							}
						}
					}
				}
			}
		}
		
		if(MyBotModule.Broodwar.self().incompleteUnitCount(UnitType.Terran_Command_Center)>0){
			return;
		}
		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
				+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) != 0) {
			return;
		}
		
		int MaxCCcount = 4;
		int CCcnt =0 ;
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted() ){
				if(getValidMineralsForExspansionNearDepot(unit) > 6){
					CCcnt++;
				}

				//멀티이후 Command_Center 근처에 일정양의 미사일터렛을 건설한다.
				if (MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) >= 2) {
					if (BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret,
							unit.getTilePosition(), 180)
							+ ConstructionManager.Instance().getConstructionQueueItemCountNear(
									UnitType.Terran_Missile_Turret, unit.getTilePosition(), 180) == 0) {
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,
								unit.getTilePosition(), false);
					}
				}
			}
 		}
		if(CCcnt >= MaxCCcount){
			return;
		}
		
		int RealCCcnt = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center);
		
		//앞마당 전
		if(RealCCcnt == 1){//TODO 이거 손봐야된다... 만약 위로 띄어서 해야한다면?? 본진에 지어진거 카운트 안되는 상황에서 앞마당에 지어버리겟네
			if (isInitialBuildOrderFinished == false) {
				return;
			}
			
			if((MyBotModule.Broodwar.self().minerals() > 200 && getFacUnits() > 40 && UnitPoint > 15) || (getFacUnits() > 60 && Attackpoint > 60 && ExpansionPoint > 0)){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
				}
			}
			if( MyBotModule.Broodwar.getFrameCount() > 9000 && MyBotModule.Broodwar.self().minerals() > 400){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
				}
			}if( MyBotModule.Broodwar.getFrameCount() > 12000 && getFacUnits() > 40){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
				}
			}
			if( MyBotModule.Broodwar.getFrameCount() > 80000 && getFacUnits() > 80){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
				}
			}
			if((MyBotModule.Broodwar.self().minerals() > 600 && getFacUnits() > 40)){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
				}
			}
			
			
		}
		
		//앞마당 이후
		if(RealCCcnt >= 2){
			
			// 돈이 600 넘고 아군 유닛이 많으면 멀티하기
			if( MyBotModule.Broodwar.self().minerals() > 600 && getFacUnits() > 120){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
			//공격시 돈 250 넘으면 멀티하기
			if(CombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY && MyBotModule.Broodwar.self().minerals() > 250){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
			//800 넘으면 멀티하기
			if(MyBotModule.Broodwar.self().minerals() > 800){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
			
			//500 넘고 유리하면
			if( MyBotModule.Broodwar.self().minerals() > 500 && getFacUnits() > 50 && Attackpoint > 30){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
			//200 넘고 유리하면
			if( MyBotModule.Broodwar.self().minerals() > 200 && getFacUnits() > 80 && Attackpoint > 40 && ExpansionPoint >= 0){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
			//공격시 돈 250 넘으면 멀티하기
			
			
			if(MyBotModule.Broodwar.getFrameCount() > 14000){
				
				
				if( getFacUnits() > 80 
						&& MyBotModule.Broodwar.self().deadUnitCount() - MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Vulture)< 12){
					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
							+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
					}
				}
			}
						
			int temp =0;
			for(Unit units : MyBotModule.Broodwar.self().getUnits()){
				if(units.getType() == UnitType.Terran_Command_Center && units.isCompleted()){
					temp += WorkerManager.Instance().getWorkerData().getMineralsSumNearDepot(units);
				}
			}
			if(temp < 8000 && CombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
			if(getFacUnits() > 160){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
		}
	}

	private void executeFirstex(){
		if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2){
			Unit checkCC=null;
			BaseLocation temp = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
			for(Unit unit : MyBotModule.Broodwar.self().getUnits()){
				
				if(unit.getType() != UnitType.Terran_Command_Center){
					continue;
				}
				if(unit.getTilePosition().getX() == BlockingEntrance.Instance().startingX 
					&& unit.getTilePosition().getY() == BlockingEntrance.Instance().startingY ){
					continue;
				}else {
					checkCC =unit;
					break;
				}
			}
			if(checkCC != null){
				if(checkCC.isLifted() == false){
					if(checkCC.getTilePosition().getX() != temp.getTilePosition().getX() || checkCC.getTilePosition().getY() != temp.getTilePosition().getY()){
						checkCC.lift();
					}
				}else{
					checkCC.land(new TilePosition(temp.getTilePosition().getX(),temp.getTilePosition().getY()));
				}
				if(checkCC.isLifted() == false && checkCC.getTilePosition().getX() == temp.getTilePosition().getX() && checkCC.getTilePosition().getY() == temp.getTilePosition().getY()){
					EXOK = true;
				}
			}
		}
	}
	 
	private void executeFly() {
		  
		if (MyBotModule.Broodwar.getFrameCount() > 12000) {
			if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) > 1) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.isLifted() == false && (unit.getType() == UnitType.Terran_Barracks	|| unit.getType() == UnitType.Terran_Engineering_Bay) && unit.isCompleted()) {

						unit.lift();
						LiftChecker = true;
						BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
						BuildOrderItem checkItem = null;

						if (!tempbuildQueue.isEmpty()) {
							checkItem = tempbuildQueue.getHighestPriorityItem();
							while (true) {
								if (tempbuildQueue.canGetNextItem() == true) {
									tempbuildQueue.canGetNextItem();
								} else {
									break;
								}
								tempbuildQueue.PointToNextItem();
								checkItem = tempbuildQueue.getItem();

								if (checkItem.metaType.isUnit()
										&& checkItem.metaType.getUnitType() == UnitType.Terran_Marine) {
									tempbuildQueue.removeCurrentItem();
								}
							}
						}
					}
					if (InformationManager.Instance().enemyRace != Race.Zerg) {
						if (unit.getType() == UnitType.Terran_Marine) {
							CommandUtil.move(unit, InformationManager.Instance()
									.getSecondChokePoint(InformationManager.Instance().selfPlayer).getPoint());
						}
					}
				}
			}
		}

		else {
			if (InformationManager.Instance().enemyRace == Race.Terran) {
				
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.isLifted() == true && (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Engineering_Bay) && unit.isCompleted()) {
						if (unit.isLifted()) {
							if (unit.canLand(new TilePosition(BlockingEntrance.Instance().barrackX,	BlockingEntrance.Instance().barrackY))) {
								unit.land(new TilePosition(BlockingEntrance.Instance().barrackX, BlockingEntrance.Instance().barrackY));
								LiftChecker = false;
							}else{
								unit.land(unit.getTilePosition());
								LiftChecker = false;
							}
						}
					}
				}
				Boolean lift = false;
				if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture)
						> InformationManager.Instance().getNumUnits(UnitType.Terran_Vulture,InformationManager.Instance().enemyPlayer)){
					lift = true;
				}	
				if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
						+ MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)> 2) {
					
					lift = true;
				}
				if (lift) {
					for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
						if (unit.isLifted() == false
								&& (unit.getType() == UnitType.Terran_Barracks
										|| unit.getType() == UnitType.Terran_Engineering_Bay)
								&& unit.isCompleted()) {
							unit.lift();
							LiftChecker = true;
							BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
							BuildOrderItem checkItem = null;

							if (!tempbuildQueue.isEmpty()) {
								checkItem = tempbuildQueue.getHighestPriorityItem();
								while (true) {
									if (tempbuildQueue.canGetNextItem() == true) {
										tempbuildQueue.canGetNextItem();
									} else {
										break;
									}
									tempbuildQueue.PointToNextItem();
									checkItem = tempbuildQueue.getItem();

									if (checkItem.metaType.isUnit()
											&& checkItem.metaType.getUnitType() == UnitType.Terran_Marine) {
										tempbuildQueue.removeCurrentItem();
									}
								}
							}
						}
					}
				}
			}
			if (InformationManager.Instance().enemyRace == Race.Protoss) {

				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.isLifted() == true && (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Engineering_Bay) && unit.isCompleted()) {
						if (unit.isLifted()) {
							if (unit.canLand(new TilePosition(BlockingEntrance.Instance().barrackX,	BlockingEntrance.Instance().barrackY))) {
								unit.land(new TilePosition(BlockingEntrance.Instance().barrackX, BlockingEntrance.Instance().barrackY));
								LiftChecker = false;
							}else{
								unit.land(unit.getTilePosition());
								LiftChecker = false;
							}
						}
					}
				}

				int dragooncnt = 0;
				int zealotcnt = 0;

//				if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) >= 1
//						&& MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 1) {

				bwapi.Position checker = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer).getPoint();
				List<Unit> eniemies = MapGrid.Instance().getUnitsNear(checker, 500, false, true, null);

				Boolean lift = false;
				for (Unit enemy : eniemies) {

					if (enemy.getType() == UnitType.Protoss_Dragoon) {
						dragooncnt++;
					}
					if (enemy.getType() == UnitType.Protoss_Zealot) {
						zealotcnt++;
					}
				}
				
				if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
						+ MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) >= 3
						&& dragooncnt + zealotcnt == 0) {
					lift = true;
				}

				if (CurrentStrategyException != StrategysException.protossException_DragoonPush
						&& CurrentStrategyException != StrategysException.protossException_ZealotPush
						&& CurrentStrategyException != StrategysException.protossException_ReadyToZealot
						&& CurrentStrategyException != StrategysException.protossException_ReadyToDragoon) {
					lift = true;
				}

//				if (MyBotModule.Broodwar.getFrameCount() > 8000) {
//					if (CurrentStrategyException == StrategysException.protossException_ReadyToZealot
//							&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Zealot,
//									InformationManager.Instance().enemyPlayer) < 4) {
//						lift = true;
//					}
//					if (CurrentStrategyException == StrategysException.protossException_ReadyToDragoon
//							&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Dragoon,
//									InformationManager.Instance().enemyPlayer) < 3) {
//						lift = true;
//					}
//				}
				if (CurrentStrategyException == StrategysException.protossException_DragoonPush
						|| CurrentStrategyException == StrategysException.protossException_ZealotPush){
					lift = false;
				}
				if (zealotcnt + dragooncnt > 0) {
					lift = false;
				}

				if (zealotcnt + dragooncnt > 8) {
					if ((CurrentStrategyException == StrategysException.protossException_DragoonPush
							|| CurrentStrategyException == StrategysException.protossException_ReadyToDragoon
							|| CurrentStrategyException == StrategysException.protossException_ZealotPush
							|| CurrentStrategyException == StrategysException.protossException_ReadyToZealot)
							&& dragooncnt + zealotcnt > 0
							&& (InformationManager.Instance().getNumUnits(UnitType.Terran_Siege_Tank_Siege_Mode,
									InformationManager.Instance().selfPlayer)
									+ InformationManager.Instance().getNumUnits(UnitType.Terran_Siege_Tank_Tank_Mode,
											InformationManager.Instance().selfPlayer)) >= dragooncnt
							&& (InformationManager.Instance().getNumUnits(UnitType.Terran_Vulture,
									InformationManager.Instance().selfPlayer)) > zealotcnt) {
						lift = true;
					}
				}
				if (dragooncnt + zealotcnt == 0) {
					lift = true;
				}
				
				if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2) {
					lift = true;
				}
				if (lift) {
					for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
						if (unit.isLifted() == false
								&& (unit.getType() == UnitType.Terran_Barracks
										|| unit.getType() == UnitType.Terran_Engineering_Bay)
								&& unit.isCompleted()) {
							unit.lift();
							LiftChecker = true;
							BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
							BuildOrderItem checkItem = null;

							if (!tempbuildQueue.isEmpty()) {
								checkItem = tempbuildQueue.getHighestPriorityItem();
								while (true) {
									if (tempbuildQueue.canGetNextItem() == true) {
										tempbuildQueue.canGetNextItem();
									} else {
										break;
									}
									tempbuildQueue.PointToNextItem();
									checkItem = tempbuildQueue.getItem();

									if (checkItem.metaType.isUnit()
											&& checkItem.metaType.getUnitType() == UnitType.Terran_Marine) {
										tempbuildQueue.removeCurrentItem();
									}
								}
							}
						}
						// if (InformationManager.Instance().enemyRace !=
						// Race.Zerg) {
						// if(unit.getType() == UnitType.Terran_Marine){
						// CommandUtil.attackMove(unit,
						// InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer).getPoint());
						// }
						// }
					}
				}
			}
		}
	}
//	private void executeFly() {
//
//		
//		if(MyBotModule.Broodwar.getFrameCount() > 10000){
//			if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) > 1){
//				for (Unit unit : MyBotModule.Broodwar.self().getUnits())
//				{
//					if (unit.isLifted() == false && (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Engineering_Bay)&& unit.isCompleted()){
//						
//						
//						unit.lift();
//						LiftChecker = true;
//						BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//						BuildOrderItem checkItem = null; 
//			
//						if (!tempbuildQueue.isEmpty()) {
//							checkItem= tempbuildQueue.getHighestPriorityItem();
//							while(true){
//								if(tempbuildQueue.canGetNextItem() == true){
//									tempbuildQueue.canGetNextItem();
//								}else{
//									break;
//								}
//								tempbuildQueue.PointToNextItem();
//								checkItem = tempbuildQueue.getItem();
//								
//								if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Marine){
//									tempbuildQueue.removeCurrentItem();
//								}
//							}
//						}
//					}
//					if (InformationManager.Instance().enemyRace != Race.Zerg) {
//						if(unit.getType() == UnitType.Terran_Marine){
//							CommandUtil.move(unit, InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer).getPoint());
//						}
//					} 
//		 		}
//			}
//		}else{
//			
//			if (InformationManager.Instance().enemyRace != Race.Zerg) {
//				int dragooncnt = 0;
//				int zealotcnt = 0;
//				
//				if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) >= 1 && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 1){
//					
//					bwapi.Position checker = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer).getPoint();
//					List<Unit> eniemies = MapGrid.Instance().getUnitsNear(checker, 500, false, true, null);
//					
//					for(Unit enemy : eniemies){
//						
//						if(enemy.getType() == UnitType.Protoss_Dragoon){
//							dragooncnt++;
//						}
//						if(enemy.getType() == UnitType.Protoss_Zealot){
//							zealotcnt++;
//						}
//						
//					}
//					
//					if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) + MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) >= 3&& dragooncnt+zealotcnt == 0){
//						for (Unit unit : MyBotModule.Broodwar.self().getUnits())
//						{
//							if (unit.isLifted() == false && (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Engineering_Bay)&& unit.isCompleted()){
//								unit.lift();
//								LiftChecker = true;
//								BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//								BuildOrderItem checkItem = null; 
//					
//								if (!tempbuildQueue.isEmpty()) {
//									checkItem= tempbuildQueue.getHighestPriorityItem();
//									while(true){
//										if(tempbuildQueue.canGetNextItem() == true){
//											tempbuildQueue.canGetNextItem();
//										}else{
//											break;
//										}
//										tempbuildQueue.PointToNextItem();
//										checkItem = tempbuildQueue.getItem();
//										
//										if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Marine){
//											tempbuildQueue.removeCurrentItem();
//										}
//									}
//								}
//							}
//							if (InformationManager.Instance().enemyRace != Race.Zerg) {
//								if(unit.getType() == UnitType.Terran_Marine){
//									CommandUtil.attackMove(unit, InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer).getPoint());
//								}
//							} 
//				 		}
//					}else if(CurrentStrategyException == StrategysException.protossException_DragoonPush){
//						
//					}else if(CurrentStrategyException == StrategysException.protossException_ZealotPush){
//						
//					}else if(CurrentStrategyException == StrategysException.protossException_ReadyToZealot && InformationManager.Instance().getNumUnits(UnitType.Protoss_Zealot, InformationManager.Instance().enemyPlayer) > 3){
//							
//					}else if(CurrentStrategyException == StrategysException.protossException_ReadyToDragoon && InformationManager.Instance().getNumUnits(UnitType.Protoss_Dragoon, InformationManager.Instance().enemyPlayer) > 2){
//						
//					}
//					else if((CurrentStrategyException == StrategysException.protossException_DragoonPush || CurrentStrategyException == StrategysException.protossException_ReadyToDragoon)&& dragooncnt+zealotcnt > 0 
//							&& (InformationManager.Instance().getNumUnits(UnitType.Terran_Siege_Tank_Siege_Mode,InformationManager.Instance().selfPlayer)
//								+ InformationManager.Instance().getNumUnits(UnitType.Terran_Siege_Tank_Siege_Mode,InformationManager.Instance().selfPlayer)) <= dragooncnt
//							){
//						
//					}else if((CurrentStrategyException == StrategysException.protossException_ZealotPush || CurrentStrategyException == StrategysException.protossException_ReadyToZealot)&& dragooncnt+zealotcnt > 0 
//							&& (InformationManager.Instance().getNumUnits(UnitType.Terran_Vulture,InformationManager.Instance().selfPlayer)
//								< zealotcnt)){
//							
//					}else{
//						for (Unit unit : MyBotModule.Broodwar.self().getUnits())
//						{
//							if (unit.isLifted() == false && (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Engineering_Bay)&& unit.isCompleted()){
//								unit.lift();
//								LiftChecker = true;
//								BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//								BuildOrderItem checkItem = null; 
//					
//								if (!tempbuildQueue.isEmpty()) {
//									checkItem= tempbuildQueue.getHighestPriorityItem();
//									while(true){
//										if(tempbuildQueue.canGetNextItem() == true){
//											tempbuildQueue.canGetNextItem();
//										}else{
//											break;
//										}
//										tempbuildQueue.PointToNextItem();
//										checkItem = tempbuildQueue.getItem();
//										
//										if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Marine){
//											tempbuildQueue.removeCurrentItem();
//										}
//									}
//								}
//							}
//							if (InformationManager.Instance().enemyRace != Race.Zerg) {
//								if(unit.getType() == UnitType.Terran_Marine){
//									CommandUtil.attackMove(unit, InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer).getPoint());
//								}
//							} 
//				 		}
//					}
//				}
//			}else{
//				if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) ==2){
//					for (Unit unit : MyBotModule.Broodwar.self().getUnits())
//					{
//						if (unit.isLifted() == false && (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Engineering_Bay)&& unit.isCompleted()){
//							unit.lift();
//							LiftChecker = true;
//							BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//							BuildOrderItem checkItem = null; 
//				
//							if (!tempbuildQueue.isEmpty()) {
//								checkItem= tempbuildQueue.getHighestPriorityItem();
//								while(true){
//									if(tempbuildQueue.canGetNextItem() == true){
//										tempbuildQueue.canGetNextItem();
//									}else{
//										break;
//									}
//									tempbuildQueue.PointToNextItem();
//									checkItem = tempbuildQueue.getItem();
//									
//									if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Marine){
//										tempbuildQueue.removeCurrentItem();
//									}
//								}
//							}
//						}
//			 		}
//				}
//			}
//		}
//	}
}