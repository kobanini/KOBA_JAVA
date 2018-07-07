import java.util.Iterator;
import java.util.Map;

import bwapi.Color;
import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;

/// �ϲ� ���ֵ��� ���¸� �����ϰ� ��Ʈ���ϴ� class
public class WorkerManager {

	/// �� Worker �� ���� WorkerJob ��Ȳ�� �����ϴ� �ڷᱸ�� ��ü
	private WorkerData workerData = new WorkerData();
	
	private CommandUtil commandUtil = new CommandUtil();
	
	/// �ϲ� �� �Ѹ��� Repair Worker �� ���ؼ�, ��ü ���� ����� �ϳ��� ������� �����մϴ�
	private Unit currentRepairWorker = null;
	
	private static WorkerManager instance = new WorkerManager();
	
	/// static singleton ��ü�� �����մϴ�
	public static WorkerManager Instance() {
		return instance;
	}
	
	/// �ϲ� ���ֵ��� ���¸� �����ϴ� workerData ��ü�� ������Ʈ�ϰ�, �ϲ� ���ֵ��� �ڿ� ä�� �� �ӹ� ������ �ϵ��� �մϴ�
	public void update() {

		// 1�ʿ� 1���� �����Ѵ�
		//if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) return;
		if(MyBotModule.Broodwar.getFrameCount() < 8000){
			if (MyBotModule.Broodwar.getFrameCount() % 3 == 0){
			
				updateWorkerStatus();
				handleGasWorkers();
				handleIdleWorkers();
			}
			if (MyBotModule.Broodwar.getFrameCount() % 3 == 0){//TODO 5,7 �� ����� ����.
				//�̳׶� �� , �ϲ� ���ġ ����� ������ updatework() �ּ�
				updatework();
			}
			if (MyBotModule.Broodwar.getFrameCount() % 5 == 0){
				//cc���ġ�� cc�� �������� �ݺ��� ����. (max�� 3���� �����ϴ�.)
				handleMoveWorkers();
				handleCombatWorkers();
				handleRepairWorkers();
			}
		}else {
			if (MyBotModule.Broodwar.getFrameCount() % 19 == 0){
				
				updateWorkerStatus();
				handleGasWorkers();
				handleIdleWorkers();
			}
			if (MyBotModule.Broodwar.getFrameCount() % 3 == 0){//TODO 5,7 �� ����� ����.
				//�̳׶� �� , �ϲ� ���ġ ����� ������ updatework() �ּ�
				updatework();
			}
			if (MyBotModule.Broodwar.getFrameCount() % 19 == 0){
				//cc���ġ�� cc�� �������� �ݺ��� ����. (max�� 3���� �����ϴ�.)
				handleMoveWorkers();
				handleCombatWorkers();
				handleRepairWorkers();
			}
		}
	}
	
	public void updateWorkerStatus() 
	{
		// Drone �� �Ǽ��� ���� isConstructing = true ���·� �Ǽ���ұ��� �̵��� ��, 
		// ��� getBuildType() == none �� �Ǿ��ٰ�, isConstructing = true, isMorphing = true �� �� ��, �Ǽ��� �����Ѵ�

		// for each of our Workers
		for (Unit worker : workerData.getWorkers())
		{
			if (!worker.isCompleted())
			{
				continue;
			}
			// ���ӻ󿡼� worker�� isIdle ���°� �Ǿ����� (���� ź���߰ų�, ���� �ӹ��� ���� ���), WorkerData �� Idle �� ���� ��, handleGasWorkers, handleIdleWorkers ��� �� �ӹ��� �����Ѵ� 
			if ( worker.isIdle() )
			{
				// workerData ���� Build / Move / Scout �� �ӹ������� ���, worker �� �� �ӹ� ���� ���� (�ӹ� �Ϸ� ��) �� �Ͻ������� isIdle ���°� �� �� �ִ� 
				if ((workerData.getWorkerJob(worker) != WorkerData.WorkerJob.Build)
					&& (workerData.getWorkerJob(worker) != WorkerData.WorkerJob.Move)
					&& (workerData.getWorkerJob(worker) != WorkerData.WorkerJob.Scout))  
				{
					
					workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit)null);
				}
			}

			// if its job is gas
			if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Gas)
			{
				Unit refinery = workerData.getWorkerResource(worker);

				// if the refinery doesn't exist anymore (�ı��Ǿ��� ���)
				if (refinery == null || !refinery.exists() ||	refinery.getHitPoints() <= 0 || worker.isGatheringMinerals())
				{
					workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit)null);
				}
			}

			// if its job is repair
			if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Repair)
			{
				Unit repairTargetUnit = workerData.getWorkerRepairUnit(worker);
							
				// ����� �ı��Ǿ��ų�, ������ �� ���� ���
				if (repairTargetUnit == null || !repairTargetUnit.exists() || repairTargetUnit.getHitPoints() <= 0 
						|| repairTargetUnit.getHitPoints() == repairTargetUnit.getType().maxHitPoints())
				{
					workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit)null);
				}
			}
			
			//1.3 �߰� �ǹ����� ������ �ϲ� ������ 20�����̸� idle
			// if its job is Build
//			if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Build)
//			{
//				// ����� �ı��Ǿ��ų�, ������ �� ���� ���
//				//1.3 �ϲ� �������� 20�����ϋ� idle ����
//				if(worker.getHitPoints() < 20)
//				{
////					worker.cancelConstruction();
//					
//					
//					worker.haltConstruction();
//					workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit)null);
//				}
//			}
			
			//1.3 �߰�  �����ϰ� ������ �ϲ� ������ 20�����̸� idle
			// if its job is Build
			if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Combat)
			{
				// ����� �ı��Ǿ��ų�, ������ �� ���� ���
				//1.3 �ϲ� �������� 20�����ϋ� idle ����
				if(worker.getHitPoints() <= 16)
				{
					Squad temp = CombatManager.Instance().squadData.getUnitSquad(worker);
//					worker.cancelConstruction();
					if(temp!=null){
						temp.removeUnit(worker);
					}
					workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit)null);
				}
			}
		}
	}


	public void handleGasWorkers()
	{
		int numResourceAssigned = 0;
		if(MyBotModule.Broodwar.getFrameCount() < 8000){
			for (Unit unit : MyBotModule.Broodwar.self().getUnits())
			{
				if (unit.getType().isResourceDepot() && unit.isCompleted() )
				{
					numResourceAssigned = workerData.getNumAssignedWorkers(unit);
				}
			}
		}
		// for each unit we have
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			// refinery �� �Ǽ� completed �Ǿ�����,
			//se-min.park ���� ��ó 300���� �ȿ� cc������ �ϲ� �Ⱥ������� �߰� 
			if (unit.getType().isRefinery() && unit.isCompleted() )
			{
				int closestDist = 500;
				boolean existNearRefinery = false;
				for (Unit depot : WorkerManager.Instance().getWorkerData().getDepots()){
					int dist = unit.getDistance(depot);
					if (dist < closestDist) {
						existNearRefinery = true;
					}
				}
				if(!existNearRefinery)
					return;
				
				int numRefAssigned = workerData.getNumAssignedWorkers(unit);
				
				
				//�̳׶� �ϲ۰� ���� �ϲ۰��� �뷱��
				if(MyBotModule.Broodwar.getFrameCount() < 8000){
//					System.out.println("numResourceAssigned: " + numResourceAssigned + ", numRefAssigned: " + numRefAssigned);
					if(numResourceAssigned <= 7){
						for (int i = 0; i<(7 - numResourceAssigned); ++i){				
							for (Iterator<Unit> it = workerData.workers.iterator(); it.hasNext(); ) {
								Unit worker = it.next();
								if(worker.isCarryingGas()){
									continue;
								}
								if (workerData.workerRefineryMap.containsKey(worker.getID())) {
									if (workerData.workerRefineryMap.get(worker.getID()).getID() == unit.getID()){
										workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, unit);
									}
								}
							}
						}
						
					}else{
						int correction = numResourceAssigned - 7;
						for (int i = 0; i<(Config.WorkersPerRefinery - numRefAssigned) && i < correction; ++i){				
							Unit gasWorker = chooseGasWorkerFromMineralWorkers(unit);
							if (gasWorker != null && !gasWorker.isCarryingGas())
							{
								workerData.setWorkerJob(gasWorker, WorkerData.WorkerJob.Gas, unit);
							}
						}
					}
				}else{
					
					int workerforgas = Config.WorkersPerRefinery;
					if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Refinery) >= 3){
						if(unit.getResources() < 8){
							workerforgas = 0;
						}
					}
					
					if(workerforgas - numRefAssigned < 0){
						for (Iterator<Unit> it = workerData.workers.iterator(); it.hasNext(); ) {
							Unit worker = it.next();
							if(worker.isCarryingGas()){
								continue;
							}
							if (workerData.workerRefineryMap.containsKey(worker.getID())) {
								if (workerData.workerRefineryMap.get(worker.getID()).getID() == unit.getID()){
									workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, unit);
									return;
								}
							}	
						}
					}
					for (int i = 0; i<(workerforgas - numRefAssigned); ++i){				
						Unit gasWorker = chooseGasWorkerFromMineralWorkers(unit);
						if (gasWorker != null && !gasWorker.isCarryingGas())
						{
							workerData.setWorkerJob(gasWorker, WorkerData.WorkerJob.Gas, unit);
						}
					}
				}
					
			}
		}
	}
	/// Idle �ϲ��� Mineral �ϲ����� ����ϴ�
	public void handleIdleWorkers() 
    {
        if(MyBotModule.Broodwar.getFrameCount() > 10000){
            int k=0;
            // for each of our workers
            for (Unit worker : workerData.getWorkers())
            {
                if (worker == null) continue;
                // if worker's job is idle 
                if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Idle || workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Default )
                {
                    // send it to the nearest mineral patch
                    setMineralWorker(worker);
                    k++;
                }
                if(k>0){
                    break;
                }
            }
        }else{
            for (Unit worker : workerData.getWorkers())
            {
                if (worker == null) continue;
                // if worker's job is idle 
                if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Idle || workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Default )
                {
                    // send it to the nearest mineral patch
                    setMineralWorker(worker);
                }
            }
        }
    }

	private void updatework() {
		
		for (Unit worker : workerData.getWorkers())
		{
			if(workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Scout){
				continue;
			}
			if(workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Combat){
				continue;
			}
			if (!worker.isCompleted())
			{
				continue;
			}
			//workerMineralMap = new HashMap<Integer, Unit>();
			
			if(worker.isGatheringMinerals()){
				/*
				 * se-min.park �ϲ����ġ
				 */
				int maxSCV = 0;
				int lowSCV = 10000;
				int sCvCnt = 0;	
				
				
				Unit tempMineral = workerData.workerMineralAssignment.get(worker.getID());
				if(tempMineral == null)
					continue;
				int planGetMineral = tempMineral.getID();
				
				int realGetMineral = 0;
				if(worker.getOrderTarget() != null){
					realGetMineral = worker.getOrderTarget().getID();
				}
				if(worker.isCarryingMinerals() == true || worker.getOrderTarget()  == null){
					continue;
				}
				if(planGetMineral != realGetMineral){
					worker.gather(tempMineral);
					realGetMineral = worker.getOrderTarget().getID();
				}
			}
		}	
	}


	public void handleMoveWorkers()
	{
		// for each of our workers
		for (Unit worker : workerData.getWorkers())
		{
			if (worker == null) continue;
			// if it is a move worker
			if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Move)
			{
				WorkerMoveData data = workerData.getWorkerMoveData(worker);

				// �������� ������ ��� �̵� ����� �����Ѵ�
				if (worker.getPosition().getDistance(data.getPosition()) < 4) {
					setIdleWorker(worker);
				}
				else {
					commandUtil.move(worker, data.getPosition());
				}
			}
		}
	}

	// bad micro for combat workers
	public void handleCombatWorkers()
	{
		for (Unit worker : workerData.getWorkers())
		{
			if (worker == null) continue;

			if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Combat)
			{
//				MyBotModule.Broodwar.drawCircleMap(worker.getPosition().getX(), worker.getPosition().getY(), 4, Color.Yellow, true);
				Unit target = getClosestEnemyUnitFromWorker(worker);

				if (target != null)
				{
					commandUtil.attackUnit(worker, target);
				}
			}
		}
	}


	public void handleRepairWorkers()
	{
		if (MyBotModule.Broodwar.self().getRace() != Race.Terran)
		{
			return;
		}
		
		//�̳׶��� ��� ��� ��ġ�� �ϹǷ� mineral < 5 �����϶� ����.
		if(MyBotModule.Broodwar.self().minerals() <5 ){
			return;
		}
		
		int repairWorkCnt = workerData.workerRepairMap.size();
		
		int repairmax = 3;
		if(CombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY){
			repairmax = 6;
		}
		if(repairWorkCnt > repairmax){
			return;
		}
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			
			// �ǹ��� ��� �ƹ��� �־ ������ ����. �ϲ� �Ѹ��� ������� ����
			// ������ �ǹ� ���� ����.
			if (unit.getType().isBuilding() && unit.isCompleted() == true && unit.getHitPoints() < unit.getType().maxHitPoints()*0.9)
			{
				if(InformationManager.Instance().enemyRace == Race.Terran && unit.isFlying()){
					continue;
				}
				Unit repairWorker = chooseRepairWorkerClosestTo(unit, 0);
				
//				if((InformationManager.Instance().enemyRace == Race.Protoss || InformationManager.Instance().enemyRace == Race.Terran)){
//					if(unit.isFlying() == true){
//						continue;
//					}
////					if(unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Supply_Depot){
////						if(unit.getHitPoints() > unit.getType().maxHitPoints()* 0.9){
////							continue;
////						}
////					}
//				}
				if(MyBotModule.Broodwar.getFrameCount() <= 12000){
					if((unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Supply_Depot) && unit.getHitPoints() > unit.getType().maxHitPoints()* 0.9){
						setRepairWorker(repairWorker, unit);
						repairWorkCnt = workerData.workerRepairMap.size();
					}
				} 
				if(unit.getType() == UnitType.Terran_Bunker){
						setRepairWorker(repairWorker, unit);
						repairWorkCnt = workerData.workerRepairMap.size();
				}else{
					setRepairWorker(repairWorker, unit);
					repairWorkCnt = workerData.workerRepairMap.size();
					continue;
				}
			}
			// ��ī�� ���� (SCV, ������ũ, ���̾� ��)�� ��� ��ó�� SCV�� �ִ� ��� ����. �ϲ� �Ѹ��� ������� ����
			else if (unit.getType().isMechanical() && unit.isCompleted() == true && unit.getHitPoints() < unit.getType().maxHitPoints())
			{
				repairWorkCnt = workerData.workerRepairMap.size();
				
				// SCV �� ���� ��󿡼� ����. ���� ���ָ� �����ϵ��� �Ѵ�
				if (unit.getType() == UnitType.Terran_Goliath 
						|| unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode 
						|| unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode
						|| unit.getType() == UnitType.Terran_Science_Vessel){
					Unit repairWorker = chooseRepairWorkerClosestTo(unit, 0);
					setRepairWorker(repairWorker, unit);
					repairWorkCnt = workerData.workerRepairMap.size();
				}
			}
			
			
			if(repairWorkCnt >= repairmax){
				break;
			}
		}
	}
	

	/// position ���� ���� ����� Mineral Ȥ�� Idle Ȥ�� Move �ϲ� ���ֵ� �߿��� Repair �ӹ��� ������ �ϲ� ������ ���ؼ� �����մϴ�
	public Unit chooseRepairWorkerClosestTo(Unit unit, int maxRange)
	{
		Position p = unit.getPosition();
		if (!p.isValid()) return null;

	    Unit closestWorker = null;
	    double closestDist = 100000000;

	    
	  //if (currentRepairWorker != null && currentRepairWorker.exists() && currentRepairWorker.getHitPoints() > 0 && unit.getType().isBuilding())
	    if(MyBotModule.Broodwar.getFrameCount() <= 12000){
	    	if(currentRepairWorker != null && currentRepairWorker.exists() && currentRepairWorker.getHitPoints() > 0 && unit.getType().isBuilding() 
	    			&& (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Supply_Depot || unit.getType() == UnitType.Terran_Bunker) == false){
	    		return currentRepairWorker;
	    	}
	    }else if (currentRepairWorker != null && currentRepairWorker.exists() && currentRepairWorker.getHitPoints() > 0 && unit.getType().isBuilding() 
	    		&& unit.getType() != UnitType.Terran_Bunker){
	    	return currentRepairWorker;
	    }

	    // for each of our workers
		for (Unit worker : workerData.getWorkers())
		{
			if (worker == null)
			{
				continue;
			}

			if (worker.isCompleted() 
				&& (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Minerals || workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Idle || workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Move))
			{
				double dist = worker.getDistance(p);

				if(worker.isCarryingMinerals() || worker.isCarryingGas())
					continue;
				if (closestWorker == null || (dist < closestDist ))
	            {
					closestWorker = worker;
	                dist = closestDist;
	            }
			}
		}

		if (currentRepairWorker == null || currentRepairWorker.exists() == false || currentRepairWorker.getHitPoints() <= 0) {
			currentRepairWorker = closestWorker;
		}

		return closestWorker;
	}

	/// �ش� �ϲ� ���� unit �� WorkerJob ���� Mineral �� �����մϴ�
	public void setMineralWorker(Unit unit)
	{
		if (unit == null) return;

		// check if there is a mineral available to send the worker to
		/// �ش� �ϲ� ���� unit ���κ��� ���� ����� ResourceDepot �ǹ��� �����մϴ�
		Unit depot = getClosestResourceDepotFromWorker(unit);
		// if there is a valid ResourceDepot (Command Center, Nexus, Hatchery)
		if (depot != null)
		{
			// update workerData with the new job
			workerData.setWorkerJob(unit, WorkerData.WorkerJob.Minerals, depot);
		}
	}
	
	/*
	 * se-min.park ��Ƽ �ϲ� ��й� ���� �߰�.
	 */
	/// �ش� �ϲ� ���� unit �� WorkerJob ���� Mineral �� �����մϴ�
		public void setMineralWorker(Unit unit,Unit depot)
		{
			if (unit == null) return;

			// check if there is a mineral available to send the worker to
			/// �ش� �ϲ� ���� unit ���κ��� ���� ����� ResourceDepot �ǹ��� �����մϴ�
			// if there is a valid ResourceDepot (Command Center, Nexus, Hatchery)
			if (depot != null && depot.isCompleted())
			{
				// update workerData with the new job
				workerData.setWorkerJob(unit, WorkerData.WorkerJob.Minerals, depot);
			}
		}
		
	/// target ���κ��� ���� ����� Mineral �ϲ� ������ �����մϴ�
	public Unit getClosestMineralWorkerTo(Position target)
	{
		Unit closestUnit = null;
		double closestDist = 100000000;

		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit.isCompleted()
				&& unit.getHitPoints() > 0
				&& unit.exists()
				&& unit.getType().isWorker()
				&& WorkerManager.Instance().isMineralWorker(unit))
			{
				double dist = unit.getDistance(target);
				if (closestUnit == null || dist < closestDist)
				{
					closestUnit = unit;
					closestDist = dist;
				}
			}
		}

		return closestUnit;
	}

	/// �ش� �ϲ� ���� unit ���κ��� ���� ����� ResourceDepot �ǹ��� �����մϴ�

	public Unit getClosestResourceDepotFromWorker(Unit worker)
	{
		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// ��Ƽ ������ �ϲ� ���� ���뷱���� �� �Ͼ���� ���� ����
		if (worker == null) return null;

		Unit closestDepot = null;
		double closestDistance = 1000000000;

		// �ϼ���, ���߿� ������ �ʰ� ���� �������ִ�, ResourceDepot Ȥ�� Lair �� Hive�� �������� Hatchery �߿���
		// ù°�� �̳׶� �ϲۼ��� �� �������� ��
		// ��°�� ����� ���� ã�´�
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;

			if (unit.getType().isResourceDepot() && unit.isCompleted()&& unit.isLifted() == false)
			{
				if (workerData.depotHasEnoughMineralWorkers(unit) == false) {
					if(isCheckEnemy(unit) == true){
						continue;
					}
					double distance = unit.getDistance(worker);
					if (closestDistance > distance) {
						closestDepot = unit;
						closestDistance = distance;
					}
				}
			}
		}
		// ��� ResourceDepot �� �� �ϲۼ��� �� ���ְų�, �ϼ��� ResourceDepot �� �ϳ��� ���� �Ǽ����̶��, 
		// ResourceDepot ������ �̳׶��� �����ִ� �� �߿��� ����� ���� ���õǵ��� �Ѵ�
		if (closestDepot == null) {
			for (Unit unit : MyBotModule.Broodwar.self().getUnits())
			{
				if (unit == null) continue;

				if (unit.getType().isResourceDepot())
				{
					if (workerData.getMineralsNearDepot(unit) > 0) {
						double distance = unit.getDistance(worker);
						if (closestDistance > distance) {
							closestDepot = unit;
							closestDistance = distance;
						}
					}
				}
			}
		}
		// ��� ResourceDepot ������ �̳׶��� �ϳ��� ���ٸ�, �ϲۿ��� ���� ����� ���� �����Ѵ�  
		if (closestDepot == null) {
			for (Unit unit : MyBotModule.Broodwar.self().getUnits())
			{
				if (unit == null) continue;
				if (unit.getType().isResourceDepot())
				{
					double distance = unit.getDistance(worker);
					if (closestDistance > distance) {
						closestDepot = unit;
						closestDistance = distance;
					}
				}
			}			
		}
		
		
		
		return closestDepot;
		// BasicBot 1.1 Patch End //////////////////////////////////////////////////

	} 

//	/// �ش� ������ ���������� �մ��� �Ǵ�
//	public boolean isCheckEnemy(Unit depot)
//	{
//		int unitCnt = 0;
//		BaseLocation myBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
//		//�����϶��� ������ false
//		if (myBaseLocation == null || depot.getDistance(myBaseLocation.getPosition()) < 5 * Config.TILE_SIZE)
//			return false;
//		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits())
//		{
//			if(unit.isVisible() && unit.getDistance(depot) < 300 && unit.getType().canAttack()){
//				unitCnt++;
////				commandUtil.move(currentScoutUnit, firstBuilding.getPosition());
////				if(unitCnt > 8){
//				if(unitCnt > 3){
//					return true;
//				}
//			}
//		}
//		return false;
//	}
	
	
	public boolean isCheckEnemy(Unit depot)
	{
		if (depot.getTilePosition().getX() == BlockingEntrance.Instance().startingX 
				&& depot.getTilePosition().getY() == BlockingEntrance.Instance().startingY ){
				return false;
			}
			
		for (Unit enemy : MapGrid.Instance().getUnitsNear(depot.getPosition(), 300, false, true, null))
		{
			if(enemy.getType().canAttack()){
				return true;
			}
		}
		return false;
	}
	
	/// �ش� �ϲ� ���� unit �� WorkerJob ���� Idle �� �����մϴ�
	public void setIdleWorker(Unit unit)
	{
		if (unit == null) return;
	
		workerData.setWorkerJob(unit, WorkerData.WorkerJob.Idle, (Unit)null);
	}

	/// Mineral �ϲ� ���ֵ� �߿��� Gas �ӹ��� ������ �ϲ� ������ ���ؼ� �����մϴ�<br>
	/// Idle �ϲ��� Build, Repair, Scout �� �ٸ� �ӹ��� ���� ���ԵǾ�� �ϱ� ������ Mineral �ϲ� �߿����� ���մϴ�
	public Unit chooseGasWorkerFromMineralWorkers(Unit refinery)
	{
		if (refinery == null) return null;

		Unit closestWorker = null;
		double closestDistance = 100000000;

		for (Unit unit : workerData.getWorkers())
		{
			if (unit == null) continue;
			
			if (unit.isCompleted() && workerData.getWorkerJob(unit) == WorkerData.WorkerJob.Minerals && !unit.isCarryingMinerals())
			{
				double distance = unit.getDistance(refinery);
				if (closestWorker == null || (distance < closestDistance && unit.isCarryingMinerals() == false))
				{
					if(unit.isCarryingGas() == true){
						distance = 0;
					}
					closestWorker = unit;
					closestDistance = distance;
				}
			}
		}

		return closestWorker;
	}

	public void setConstructionWorker(Unit worker, UnitType buildingType)
	{
		if (worker == null) return;

		workerData.setWorkerJob(worker, WorkerData.WorkerJob.Build, buildingType);
	}

	/// buildingPosition ���� ���� ����� Move Ȥ�� Idle Ȥ�� Mineral �ϲ� ���ֵ� �߿��� Construction �ӹ��� ������ �ϲ� ������ ���ؼ� �����մϴ�<br>
	/// Move / Idle Worker �߿��� ���� �����ϰ�, ������ Mineral Worker �߿��� �����մϴ�<br>
	/// �ϲ� ������ 2�� �̻��̸�, avoidWorkerID �� �ش��ϴ� worker �� �������� �ʵ��� �մϴ�<br>
	/// if setJobAsConstructionWorker is true (default), it will be flagged as a builder unit<br>
	/// if setJobAsConstructionWorker is false, we just want to see which worker will build a building
	public Unit chooseConstuctionWorkerClosestTo(UnitType buildingType, TilePosition buildingPosition, boolean setJobAsConstructionWorker, int avoidWorkerID)
	{
		// variables to hold the closest worker of each type to the building
		Unit closestMovingWorker = null;
		Unit closestMiningWorker = null;
		double closestMovingWorkerDistance = 100000000;
		double closestMiningWorkerDistance = 100000000;

		// look through each worker that had moved there first
		for (Unit unit : workerData.getWorkers())
		{
			if (unit == null) continue;

			// worker �� 2�� �̻��̸�, avoidWorkerID �� ���Ѵ�
			if (workerData.getWorkers().size() >= 2 && avoidWorkerID != 0 && unit.getID() == avoidWorkerID) continue;

			
			
			// Move / Idle Worker
			if (unit.isCompleted() && (workerData.getWorkerJob(unit) == WorkerData.WorkerJob.Move || workerData.getWorkerJob(unit) == WorkerData.WorkerJob.Idle))
			{
				
				
				// if it is a new closest distance, set the pointer
				double distance = unit.getDistance(buildingPosition.toPosition());
				//1.3 worker ������ 20�����ϸ� �ٽ� ������Ѵ�.
				if(unit.getHitPoints() < 20){
					distance += 200;
				}
				
				if (closestMovingWorker == null || (distance < closestMovingWorkerDistance && unit.isCarryingMinerals() == false && unit.isCarryingGas() == false ))
				{
					if (BWTA.isConnected(unit.getTilePosition(), buildingPosition)) {
						closestMovingWorker = unit;
						closestMovingWorkerDistance = distance;
					}
				}
			}
			

			// Move / Idle Worker �� ������, �ٸ� Worker �߿��� �����Ѵ�
			/*
			 * se-min.park ������ �̳׶����� �Ǽ����� ��ġ�� ������� gas ��� �ִ� �ϲ��� �����(�������� isGatheringGas false ó�� �Ǿ��������� ��...�׷��� Gas �ϲۿ��� �Ȼ��°ɷ� ����) 
			 */
			if (unit.isCompleted() 
				&& (workerData.getWorkerJob(unit) != WorkerData.WorkerJob.Move 
				&& workerData.getWorkerJob(unit) != WorkerData.WorkerJob.Idle 
				&& workerData.getWorkerJob(unit) != WorkerData.WorkerJob.Gas 
				&& workerData.getWorkerJob(unit) != WorkerData.WorkerJob.Build
				&& workerData.getWorkerJob(unit) != WorkerData.WorkerJob.Scout
				&& workerData.getWorkerJob(unit) != WorkerData.WorkerJob.Combat
				))
			{
				// if it is a new closest distance, set the pointer
				double distance = unit.getDistance(buildingPosition.toPosition());
				if (closestMiningWorker == null || (distance < closestMiningWorkerDistance && unit.isCarryingMinerals() == false && unit.isCarryingGas() == false ))
				{
					if (BWTA.isConnected(unit.getTilePosition(), buildingPosition)) {
						closestMiningWorker = unit;
						closestMiningWorkerDistance = distance;
					}
				}
			}
		}
		//System.out.println("closestMovingWorker ; " + closestMovingWorker.getID() + " closestMiningWorker L : " + closestMiningWorker.getID());
		Unit chosenWorker = closestMovingWorker != null ? closestMovingWorker : closestMiningWorker;

		// if the worker exists (one may not have been found in rare cases)
		// �̳׶� or ���� �ȿű�� �ַ� ����
		if (chosenWorker != null && setJobAsConstructionWorker)
		{
			workerData.setWorkerJob(chosenWorker, WorkerData.WorkerJob.Build, buildingType);
		}

		return chosenWorker;
	}
	

	/// Mineral Ȥ�� Idle �ϲ� ���ֵ� �߿��� Scout �ӹ��� ������ �ϲ� ������ ���ؼ� �����մϴ�
	public Unit getScoutWorker()
	{
	    // for each of our workers
		for (Unit worker : workerData.getWorkers())
		{
			if (worker == null)
			{
				continue;
			}
			if(worker.isCarryingMinerals()){
        		continue;
        	}
			// if it is a scout worker
	        if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Scout) 
			{
				return worker;
			}
		}

	    return null;
	}

	// sets a worker as a scout
	public void setScoutWorker(Unit worker)
	{
		if (worker == null) return;

		workerData.setWorkerJob(worker, WorkerData.WorkerJob.Scout, (Unit)null);
	}

	
	// get a worker which will move to a current location
	/// position ���� ���� ����� Mineral Ȥ�� Idle �ϲ� ���ֵ� �߿��� Move �ӹ��� ������ �ϲ� ������ ���ؼ� �����մϴ�
	public Unit chooseMoveWorkerClosestTo(Position p)
	{
		// set up the pointer
		Unit closestWorker = null;
		double closestDistance = 100000000;

		// for each worker we currently have
		for (Unit unit : workerData.getWorkers())
		{
			if (unit == null) continue;

			// only consider it if it's a mineral worker
			if (unit.isCompleted() && workerData.getWorkerJob(unit) == WorkerData.WorkerJob.Minerals)
			{
				// if it is a new closest distance, set the pointer
				double distance = unit.getDistance(p);
				if (closestWorker == null || (distance < closestDistance && unit.isCarryingMinerals() == false && unit.isCarryingGas() == false ) ){
					if(unit.isCarryingMinerals()){
						continue;
					}
					closestWorker = unit;
					closestDistance = distance;
				}
			}
		}

		// return the worker
		return closestWorker;
	}

	/// position ���� ���� ����� Mineral Ȥ�� Idle �ϲ� ���ֵ� �߿��� Move �ӹ��� ������ �ϲ� ������ ���ؼ� �����մϴ�??????
	public void setMoveWorker(Unit worker, int mineralsNeeded, int gasNeeded, Position p)
	{
		// set up the pointer
		Unit closestWorker = null;
		double closestDistance = 100000000;

		// for each worker we currently have
		for (Unit unit : workerData.getWorkers())
		{
			if (unit == null) continue;
			
			// only consider it if it's a mineral worker or idle worker
			if (unit.isCompleted() && (workerData.getWorkerJob(unit) == WorkerData.WorkerJob.Minerals || workerData.getWorkerJob(unit) == WorkerData.WorkerJob.Idle)&& !unit.isCarryingMinerals())
			{
				// if it is a new closest distance, set the pointer
				double distance = unit.getDistance(p);
				if (closestWorker == null || distance < closestDistance)
				{
					closestWorker = unit;
					closestDistance = distance;
				}
			}
		}

		if (closestWorker != null)
		{
			workerData.setWorkerJob(closestWorker, WorkerData.WorkerJob.Move, new WorkerMoveData(mineralsNeeded, gasNeeded, p));
		}
		else
		{
			//MyBotModule.Broodwar.printf("Error, no worker found");
		}
	}


	/// �ش� �ϲ� �������κ��� ���� ����� ���� ������ �����մϴ�
	public Unit getClosestEnemyUnitFromWorker(Unit worker)
	{
		if (worker == null) return null;

		Unit closestUnit = null;
		double closestDist = 10000;

		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits())
		{
			double dist = unit.getDistance(worker);

			//if ((dist < 400) && (closestUnit == null || (dist < closestDist)))
			if ((dist < 1000) && (closestUnit == null || (dist < closestDist)))
			{
				closestUnit = unit;
				closestDist = dist;
			}
		}

		return closestUnit;
	}

	/// �ش� �ϲ� ���ֿ��� Combat �ӹ��� �ο��մϴ�
	public void setCombatWorker(Unit worker)
	{
		if (worker == null) return;

		workerData.setWorkerJob(worker, WorkerData.WorkerJob.Combat, (Unit)null);
	}

	/// ��� Combat �ϲ� ���ֿ� ���� �ӹ��� �����մϴ�
	public void stopCombat()
	{
		for (Unit worker : workerData.getWorkers())
		{
			if (worker == null) continue;

			if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Combat)
			{
				setMineralWorker(worker);
			}
		}
	}
	
	public void setRepairWorker(Unit worker, Unit unitToRepair)
	{
		workerData.setWorkerJob(worker, WorkerData.WorkerJob.Repair, unitToRepair);
	}

	public void stopRepairing(Unit worker)
	{
		workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit)null);
	}

	/// �ϲ� ���ֵ��� ���¸� �����ϴ� workerData ��ü�� ������Ʈ�մϴ�
	public void onUnitMorph(Unit unit)
	{
		if (unit == null) return;

		if (unit.getType().isBuilding() && unit.getPlayer() == MyBotModule.Broodwar.self() && unit.getPlayer().getRace() == Race.Zerg)
		{
			// �ش� worker �� workerData ���� �����Ѵ�
			workerData.workerDestroyed(unit);
			rebalanceWorkers();
		}
	}

	
	/// �ϲ� ���ֵ��� ���¸� �����ϴ� workerData ��ü�� ������Ʈ�մϴ�
	public void onUnitShow(Unit unit)
	{
		if (unit == null) return;

		// add the depot if it exists
		if (unit.getType().isResourceDepot() && unit.getPlayer() == MyBotModule.Broodwar.self())
		{
			workerData.addDepot(unit);
		}

		// add the worker
		if (unit.getType().isWorker() && unit.getPlayer() == MyBotModule.Broodwar.self() && unit.getHitPoints() >= 0)
		{
			workerData.addWorker(unit);
		}

		if (unit.getType().isResourceDepot() && unit.getPlayer() == MyBotModule.Broodwar.self())
		{
			rebalanceWorkers();
		}
	}
	
	// onUnitComplete �޼ҵ� �߰�
	/// �ϲ� ���ֵ��� ���¸� �����ϴ� workerData ��ü�� ������Ʈ�մϴ�	
	/// Terran_SCV, Protoss_Probe ���� �Ʒ��� ������ ź���� ���, 	
	/// Zerg_Drone ������ ź���ϴ� ���,	
	/// Zerg_Drone ������ �ǹ��� Morph �� ������ �ǹ��� �ϼ��Ǵ� ���,	
	/// Zerg_Drone ������ Zerg_Extractor �ǹ����� Morph �� ��ҽ��Ѽ� Zerg_Drone ������ ���Ӱ� ź���ϴ� ���	
	/// ȣ��˴ϴ�	
	public void onUnitComplete(Unit unit)	
	{		
		if (unit == null) 
			return;		
		// ResourceDepot �ǹ��� �ű� �����Ǹ�, �ڷᱸ�� �߰� ó���� �� ��, rebalanceWorkers �� �Ѵ�		
		if (unit.getType().isResourceDepot() && unit.getPlayer() == MyBotModule.Broodwar.self())	
		{			
			workerData.addDepot(unit);
			rebalanceWorkers();
		}		
		// �ϲ��� �ű� �����Ǹ�, �ڷᱸ�� �߰� ó���� �Ѵ�. 		
		if (unit.getType().isWorker() && unit.getPlayer() == MyBotModule.Broodwar.self() && unit.getHitPoints() >= 0)
		{			
			workerData.addWorker(unit);
			rebalanceWorkers();
		}
	}
	// ���ϰ��ִ� resource depot �� ����� ���� mineral worker ���� �����Ǿ� �ִٸ�, idle ���·� �����
	// idle worker ���� mineral job �� �ο��� ��, mineral worker �� ������ resource depot ���� �̵��ϰ� �ȴ�  
	public void rebalanceWorkers()
	{
		for (Unit worker : workerData.getWorkers())
		{
			if (workerData.getWorkerJob(worker) != WorkerData.WorkerJob.Minerals)
			{
				continue;
			}

			Unit depot = workerData.getWorkerDepot(worker);
			
			if (depot != null && workerData.depotHasEnoughMineralWorkers(depot))
			{
				workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit)null);
			}
			else if (depot == null)
			{
				workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit)null);
			}
		}
	}

	/// �ϲ� ���ֵ��� ���¸� �����ϴ� workerData ��ü�� ������Ʈ�մϴ�

	public void onUnitDestroy(Unit unit) 
	{
		if (unit == null) return;
		// ResourceDepot �ǹ��� �ı��Ǹ�, �ڷᱸ�� ���� ó���� �� ��, �ϲ۵��� Idle ���·� ����� rebalanceWorkers �� ȿ���� ���� �Ѵ�
		if (unit.getType().isResourceDepot() && unit.getPlayer() == MyBotModule.Broodwar.self())
		{
			workerData.removeDepot(unit);
		}
		// �ϲ��� ������, �ڷᱸ�� ���� ó���� �� ��, rebalanceWorkers �� �Ѵ�
		if (unit.getType().isWorker() && unit.getPlayer() == MyBotModule.Broodwar.self()) 
		{
			workerData.workerDestroyed(unit);
			rebalanceWorkers();
		}
		// �̳׶��� �� ä���ϸ� rebalanceWorkers�� �Ѵ�
		if (unit.getType() == UnitType.Resource_Mineral_Field)
		{
			rebalanceWorkers();
		}
	}

	public boolean isMineralWorker(Unit worker)
	{
		if (worker == null) return false;

		return workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Minerals || workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Idle;
	}
	
	public boolean isCombatWorker(Unit worker)
	{
		if (worker == null) return false;

		return workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Combat || workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Idle;
	}

	public boolean isScoutWorker(Unit worker)
	{
		if (worker == null) return false;

		return (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Scout);
	}

	public boolean isConstructionWorker(Unit worker)
	{
		if (worker == null) return false;

		return (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Build);
	}

	public int getNumMineralWorkers() 
	{
		return workerData.getNumMineralWorkers();	
	}
	
	//������� �ϲ� ���� ���� ����
	public int getNumNongBongMWorkers() 
	{
		return workerData.getNumNongBongMWorkers();	
	}
	
	public int getNumNongBongGWorkers() 
	{
		return workerData.getNumNongBongGWorkers();	
	}
	
	public int getNumNongBongGSWorkers() 
	{
		return workerData.getNumNongBongGSWorkers();	
	}

	/// idle ������ �ϲ� ���� unit �� ���ڸ� �����մϴ�
	public int getNumIdleWorkers() 
	{
		return workerData.getNumIdleWorkers();	
	}

	public int getNumGasWorkers() 
	{
		return workerData.getNumGasWorkers();
	}

	/// �ϲ� ���ֵ��� ���¸� �����ϴ� workerData ��ü�� �����մϴ�
	public WorkerData getWorkerData()
	{
		return workerData;
	}
}