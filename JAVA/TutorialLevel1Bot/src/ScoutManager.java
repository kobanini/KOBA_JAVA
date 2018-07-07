
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import bwapi.Color;
import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;

/// ���� �ʹݿ� �ϲ� ���� �߿��� ���� ������ �ϳ� �����ϰ�, ���� ������ �̵����� ������ �����ϴ� class<br>
/// ������ BaseLocation ��ġ�� �˾Ƴ��� �ͱ����� ���ߵǾ��ֽ��ϴ�
public class ScoutManager{

	private Unit currentScoutUnit;
	private int currentScoutStatus;
	public enum ScoutStatus {
		NoScout,						///< ���� ������ �������� ����
		MovingToAnotherBaseLocation,	///< ������ BaseLocation �� �̹߰ߵ� ���¿��� ���� ������ �̵���Ű�� �ִ� ����
		MoveAroundEnemyBaseLocation   	///< ������ BaseLocation �� �߰ߵ� ���¿��� ���� ������ �̵���Ű�� �ִ� ����
	};
	private BaseLocation currentScoutTargetBaseLocation = null;
	private Vector<Position> enemyBaseRegionVertices = new Vector<Position>();
	private int currentScoutFreeToVertexIndex = -1;
	private Position currentScoutTargetPosition = Position.None;

	private CommandUtil commandUtil = new CommandUtil();
	private MapTools mapTools = new MapTools();
	
	private static ScoutManager instance = new ScoutManager();
	
	private int preScoutHP = 0;
	private boolean scoutUnderAttack = false;
	private boolean gasRush = false; //����
	private boolean finishGasRush = false; //����
	private boolean distrubMineral = false; //�̳׶� �ռ���
	private boolean distrubFlag = false; //�̳׶� �ռ��� �ϴ� ���ݹ��� �ռ��� ���� 
	private boolean fleeFlag  = false; //�������� �߽߰� ���� �������� ���� 
	private boolean fleeLongEnemyFlag  = false; //���� , �巡�� �Ǻ� ���� 
	private boolean idleFlag = false;  //���� ���� 
	private boolean cyberFlag = false;  //�����佺 ��� �ǹ� �ϼ��ƴ��� �Ǻ� ����. 
	private boolean scoutFlag = false;  //��ī�� �ѹ��� ������ ���� ���� 
	private boolean gasExpscoutFlag = false;  //�� ���� ���� �߰��ߴ��� �Ǻ� ���� 
	private boolean twoBarrack = false;  //�� ���� ���� �߰��ߴ��� �Ǻ� ���� 
	
	
	private List<Unit> units = new ArrayList<>();
	public List<Unit> getUnits() {
		return units;
	}
	
	/// static singleton ��ü�� �����մϴ�
	public static ScoutManager Instance() {
		return instance;
	} 

	/// ���� ������ �����ϰ�, ���� ���¸� ������Ʈ�ϰ�, ���� ������ �̵���ŵ�ϴ�
	public void update()
	{
		// 1�ʿ� 6���� �����մϴ�
		if (MyBotModule.Broodwar.getFrameCount() % 4 == 0){
		
			// scoutUnit �� �����ϰ�, scoutUnit �� �̵��� ��Ʈ����.
			if(scoutFlag == false)
				assignScoutIfNeeded();
			if(fleeFlag == false){
				moveScoutUnit();
			}else{
				if(fleeLongEnemyFlag == false){
					updateFleeUnit();
				}else{
					updateSecondFleeUnit();
				}
				if(idleFlag){
					WorkerManager.Instance().setIdleWorker(currentScoutUnit);
					currentScoutUnit = null;
					return;
				}else{
					followPerimeter();
				}
//				distrubMineral();
//				if (distrubMineral && MyBotModule.Broodwar.getFrameCount() % 20 == 0) updateScoutUnit();
				
			}
		}

		// �����, scoutUnit �� �̵��� ���� �߰ߵ� ������ ó���ϴ� ���� InformationManager.update() ���� ������
	}

	//�� �������� �ո�Ƽ �������� �Ȱ��� ���ϴ� �Լ�
	private void updateFleeUnit() {
		// TODO Auto-generated method stub
		if(currentScoutUnit == null){
			return;
		}
		if(currentScoutUnit != null){
			enemyLongInRadius();
		}
	}
	
	//�� �ո��翡�� �������� �������� ������ ���ϴ� �Լ� 
	private void updateSecondFleeUnit() {
		// TODO Auto-generated method stub
		if(currentScoutUnit == null){
			return;
		}
		Position enemyFirstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.enemy()).getPosition();
		if(enemyFirstExpansionLocation == null){
			return;
		}
		if(currentScoutUnit.getDistance(enemyFirstExpansionLocation) > 300){
			return;
		}
			
		if(currentScoutUnit != null){
			if(enemySecondLongInRadius()){
				idleFlag = true;
			}
		}
	}
	
	private void updateScoutUnit() {
		// TODO Auto-generated method stub
		if(currentScoutUnit == null){
			return;
		}
		if(currentScoutUnit != null){
			if(currentScoutUnit.isGatheringMinerals()){
				currentScoutUnit.stop();
			}
			
		}
	}

	private void distrubMineral() {
		// TODO Auto-generated method stub
		int scoutHP = currentScoutUnit.getHitPoints() + currentScoutUnit.getShields();
		if(scoutHP < preScoutHP){
			distrubMineral = false;
			distrubFlag = false;
			return;
		}
		if(!currentScoutUnit.isGatheringMinerals()){
			if(currentScoutUnit != null){
				//�� ��ġ ��ã����
				BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
				if(enemyBaseLocation == null){
					followPerimeter(); 
				}else{
					if(!currentScoutUnit.isGatheringMinerals()){
						followPerimeter(); 
					}
				}
			}
		}
		preScoutHP = scoutHP;
	}

	/// ���� ������ �ʿ��ϸ� ���� �����մϴ�
	public void assignScoutIfNeeded()
	{
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());

		if (enemyBaseLocation == null)
		{
			if (currentScoutUnit == null || currentScoutUnit.exists() == false || currentScoutUnit.getHitPoints() <= 0)
			{
				currentScoutUnit = null;
				currentScoutStatus = ScoutStatus.NoScout.ordinal();

				// first building (Pylon / Supply Depot / Spawning Pool) �� �Ǽ� ������ ��, ���� �����̿� �ִ� Worker �� ������������ �����Ѵ�
				Unit firstBuilding = null;

				for (Unit unit : MyBotModule.Broodwar.self().getUnits())
				{
					if (InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters) {
						if (unit.getType().isBuilding() == true && unit.getType() == UnitType.Terran_Supply_Depot)
						{
							firstBuilding = unit;
							break;
						}
					}else{
						if (unit.getType().isBuilding() == true && unit.getType() == UnitType.Terran_Barracks)
						{
							firstBuilding = unit;
							break;
						}
					}
				}

				if (firstBuilding != null)
				{
					if(InformationManager.Instance().enemyRace == Race.Zerg){
						if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_SCV) < 14){
							return;
						}
					}
					// grab the closest worker to the first building to send to scout
					//Unit unit = WorkerManager.Instance().getClosestMineralWorkerTo(firstBuilding.getPosition());
					/*	se-min.park �߰�
					 *  ���� �ּ� �ս�
					 */
					Unit unit = WorkerManager.Instance().chooseMoveWorkerClosestTo(firstBuilding.getPosition());

					// if we find a worker (which we should) add it to the scout units
					// ���� ���� �ϲ��� ������, �ƹ��͵� ���� �ʴ´�
//					if (unit != null && !unit.isCarryingMinerals())
//					if (unit != null)
					{
						// set unit as scout unit
						currentScoutUnit = unit;
						if(currentScoutUnit!= null){
							if(currentScoutUnit.isCarryingMinerals())
								return;
						}
						WorkerManager.Instance().setScoutWorker(currentScoutUnit);
						scoutFlag = true;	
						// �����, �ϲ��� ���� �ӹ��� �����Ϸ���, ������ ���� �ϸ� �ȴ�
						//WorkerManager::Instance().setIdleWorker(currentScoutUnit);
					}
				}
			}
		}
	}


	/// ���� ������ �̵���ŵ�ϴ�
	// ���� MainBaseLocation ��ġ�� �𸣴� ��Ȳ�̸�, StartLocation �鿡 ���� �Ʊ��� MainBaseLocation���� ����� �ͺ��� ������� ����
	// ���� MainBaseLocation ��ġ�� �ƴ� ��Ȳ�̸�, �ش� BaseLocation �� �ִ� Region�� �����ڸ��� ���� ��� �̵��� (���� ������ ����������) 
	public void moveScoutUnit()
	{
		if (currentScoutUnit == null || currentScoutUnit.exists() == false || currentScoutUnit.getHitPoints() <= 0 )
		{
			currentScoutUnit = null;
			currentScoutStatus = ScoutStatus.NoScout.ordinal();
			return;
		}
		int scoutHP = currentScoutUnit.getHitPoints() + currentScoutUnit.getShields();
		BaseLocation myBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
		//�����϶��� ������ false
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		//BaseLocation myBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
		
		//�Ʊ� ������ �� �ǹ��� ������ �н�(���� ������ �츮 ������ ���� �������� ���°�찡 ����.
		if (enemyBaseLocation != null && enemyBaseLocation.getDistance(myBaseLocation.getPosition()) < 5 * Config.TILE_SIZE)
			enemyBaseLocation = null;
		
		int scoutDistanceThreshold = 30;
		
		//�� ��ġ ��ã����
		if (enemyBaseLocation == null)
		{
			
			
			
			// currentScoutTargetBaseLocation �� null �̰ų� ���� ������ currentScoutTargetBaseLocation �� ���������� 
			// �Ʊ� MainBaseLocation ���κ��� ���� ����� ������ BaseLocation �� ���ο� ���� ��� currentScoutTargetBaseLocation ���� ��Ƽ� �̵�
			if (currentScoutTargetBaseLocation == null || currentScoutUnit.getDistance(currentScoutTargetBaseLocation.getPosition()) < 5 * Config.TILE_SIZE) 
			{
				currentScoutStatus = ScoutStatus.MovingToAnotherBaseLocation.ordinal();
 
				double closestDistance = 1000000000;
				double tempDistance = 0;
				BaseLocation closestBaseLocation = null;
				for (BaseLocation startLocation : BWTA.getStartLocations())
				{
					// if we haven't explored it yet (�湮�߾��� ���� �ٽ� ���� �ʿ� ����)
					if (MyBotModule.Broodwar.isExplored(startLocation.getTilePosition()) == false)
					{
						// GroundDistance �� �������� ���� ����� ������ ����
						tempDistance = (double)(InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getGroundDistance(startLocation) + 0.5);

						if (tempDistance > 0 && tempDistance < closestDistance) {
							closestBaseLocation = startLocation;
							closestDistance = tempDistance;
						}
					}
				}

				
				if (MyBotModule.Broodwar.isExplored(new TilePosition(64,64)) == false){
				    CommandUtil.move(currentScoutUnit, new TilePosition(64,64).toPosition());
				    currentScoutTargetBaseLocation = closestBaseLocation;
				}else
					
				if (closestBaseLocation != null) {
					// assign a scout to go scout it
					CommandUtil.move(currentScoutUnit, closestBaseLocation.getPosition());
					currentScoutTargetBaseLocation = closestBaseLocation;
				}else{
					//TF ���� ���� �ϲ� �ȳ����� ���� �ذ�� �߰� -> Ȯ�� �ʿ�
					CommandUtil.move(currentScoutUnit, currentScoutTargetBaseLocation.getPosition());
				}
			}else{//BasicBot1.2
				if (MyBotModule.Broodwar.isExplored(new TilePosition(64,64)) == false){
				    CommandUtil.move(currentScoutUnit, new TilePosition(64,64).toPosition());
				}else{
					CommandUtil.move(currentScoutUnit, currentScoutTargetBaseLocation.getPosition());
				}
			}
		}
		// if we know where the enemy region is
		else 
		{
			int scoutDistanceToEnemy = mapTools.getGroundDistance(currentScoutUnit.getPosition(), enemyBaseLocation.getPosition());
	        boolean scoutInRangeOfenemy = scoutDistanceToEnemy <= scoutDistanceThreshold;
			// if scout is exist, move scout into enemy region
			if (currentScoutUnit != null) {
				currentScoutTargetBaseLocation = enemyBaseLocation;
				
				if (MyBotModule.Broodwar.isExplored(currentScoutTargetBaseLocation.getTilePosition()) == false) {
					currentScoutStatus = ScoutStatus.MovingToAnotherBaseLocation.ordinal();
					currentScoutTargetPosition = currentScoutTargetBaseLocation.getPosition();
					CommandUtil.move(currentScoutUnit, currentScoutTargetPosition);
					
				}
				else {
					//���� ������ ���ݹް� ������
					if(scoutHP < preScoutHP){
						scoutUnderAttack = true;
						fleeFlag = true;
//						distrubFlag = true;
					}
//					if(enemyInRadius()){
//						fleeFlag = true;
//						return;
//					}
					enemyLongInRadius();
					if(fleeLongEnemyFlag == true || fleeFlag == true){
						return;
					}
					//���� ������ ���ݹ��� �ʰ� �ְ� �����ȿ� ���� ������.
					if (!currentScoutUnit.isUnderAttack() && !enemyWorkerInRadius())
			        {
				        scoutUnderAttack = false;
			        }
					// if the scout is in the enemy region
					/*if (scoutInRangeOfenemy)
					{	
						
						 * se-min.park
						 * Buf_Fix 1.3
						 * ���� �׽�Ʈ�� ����� �ϲ� �������� �ʰ� �ٷ� ���� ������ ���� ��Ź��.
						 * ���� Ȯ�� �ʿ�.  �����Ϸ��� scoutUnderAttack = true; ���� 
						 
						scoutUnderAttack = true;
						// if the worker scout is not under attack
						if (!scoutUnderAttack){
							
								// get the closest enemy worker
								Unit closestWorker = enemyWorkerToHarass();
								
								// if there is a worker nearby, harass it
								if(closestWorker != null && (currentScoutUnit.getDistance(closestWorker) < 800))
								{
				                    //scoutStatus = "Harass enemy worker";
									currentScoutFreeToVertexIndex = -1;
									
									//currentScoutUnit.attack(closestWorker);
									 if(distrubFlag){
										 preScoutHP = scoutHP;
//										 distrubMineral = true;
//										 followPerimeter();
//										 commandUtil.move(currentScoutUnit,closestWorker.getPosition());
										 return;
					                 }else
					                	 CommandUtil.attackUnit(currentScoutUnit, closestWorker);
									 scoutUnderAttack= true;
									 
									//Micro::SmartAttackUnit(_workerScout, closestWorker);
								}
								// otherwise keep moving to the enemy region
								else
								{
//									System.out.println("Following perimeter");
				                   // _scoutStatus = "Following perimeter";
				                    followPerimeter();  
				                }
						}else{
//							System.out.println("Under attack inside, fleeing1");
			                   // _scoutStatus = "Under attack inside, fleeing";
			                    followPerimeter();  
			                   
		                }
						
					}// if the scout is not in the enemy region
					else if (scoutUnderAttack)
					{
//						System.out.println("Under attack inside, fleeing");
			            //_scoutStatus = "Under attack inside, fleeing";

			            followPerimeter();
					}
					else
					{
			            //_scoutStatus = "Enemy region known, going there";

						// move to the enemy region
							followPerimeter();
			        }*/
					followPerimeter();
					
				}
			}	
		}
				preScoutHP = scoutHP;
	}
	
	public void followPerimeter()
	{
	    Position fleeTo = getScoutFleePositionFromEnemyRegionVertices();

//        MyBotModule.Broodwar.drawCircleMap(fleeTo, 5, Color.Red, true);
	    
		commandUtil.move(currentScoutUnit, fleeTo);
	}
	// Choose an enemy worker to harass, or none.
	public Unit enemyWorkerToHarass()
	{
		Unit enemyWorker = null;
		double maxDist = 0;
		double closestDist = 100000000;

		Unit geyser = getEnemyGeyser();
		
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits())
		{
			if (unit.getType().isWorker() && unit.isConstructing())
			{
				return unit;
			}
		}

		// Only harass terran SCVs that are building, no other workers.
		// Conclusion: Doesn't help. Scout dies if counterattacked at all.
		// return nullptr;

		// for each enemy worker
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits())
		{
			if (unit.getType().isWorker())
			{
//				double dist = unit.getDistance(geyser);
				double dist = unit.getDistance(currentScoutUnit.getPosition());
				if (enemyWorker == null || (dist < closestDist))
	            {
					enemyWorker = unit;
	            }
				/*
				if (dist < 1000 && dist > maxDist)
				{
					maxDist = dist;
					enemyWorker = unit;
				}*/
			}
		}

		return enemyWorker;
	}
	
	// If there is exactly 1 geyser in the enemy base, return it.
	// If there's 0 we can't steal it, and if >1 then it's no use to steal it.
	public Unit getEnemyGeyser()
	{
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);

		//List<Unit> geysers =  enemyBaseLocation.getGeysers();
		int geyserCnt = 0;
		Unit geyserOne = null;
		for (Unit geyser : enemyBaseLocation.getGeysers())
		{
			geyserCnt += 1;
			if(geyserCnt > 1){
				geyserCnt = -1;
				geyserOne = null;
				break;
			}
			geyserOne = geyser; 
			
		}
		if (geyserCnt == 1)
		{
			return geyserOne;
		}else{
			return null;
		}
	}
	
	//�����ȿ� �ϲ� �ִ��� ������ �Ǻ�
	public boolean enemyWorkerInRadius()
	{
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits())
		{
			if (unit.getType().isWorker() && (unit.getDistance(currentScoutUnit) < 300))
			{
				return true;
			}
		}
		return false;
	}
	//�����ȿ� ���� ���� �ִ��� ������ �Ǻ�
	public boolean enemyInRadius()
	{
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits())
		{
			if (!unit.getType().isWorker() && unit.getType().canAttack() && (unit.getDistance(currentScoutUnit) < 300))
			{
				return true;
			}
		}

		return false;
	}
	//�����ȿ� ���Ÿ� ���� ���� �ִ��� ������ �Ǻ�(����/�巡��)
	public void enemyLongInRadius()
	{
		//�� ���� �ô��� ���ô��� Ȯ���ϱ� ���� ����
		//�Ʒ� �Լ��� �ְ� �;����� �Ʒ��� �Ǽ��� unit�� ������ ���� �Ǻ��� �ȵ�.
		//�跰 2���� ������ ���� ����
		int barrackCnt = 0;
		
		if(gasExpscoutFlag == false){
			for (Unit unit : MyBotModule.Broodwar.enemy().getUnits())
			{
				if(unit.getType().isRefinery()){
					if(MyBotModule.Broodwar.isExplored(unit.getTilePosition())){
						gasExpscoutFlag = true;
					}
					break;
				}
			}
		}
		Iterator<Integer> it = InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getUnitAndUnitInfoMap().keySet().iterator();
//		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits())
		while (it.hasNext()) {
			UnitInfo ui= InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getUnitAndUnitInfoMap().get(it.next());
			if(ui.getType() == UnitType.Protoss_Cybernetics_Core && ui.isCompleted()){
				cyberFlag = true;
			}
			if (!ui.getType().isWorker() && ui.getType().canAttack() && (ui.getUnit().getDistance(currentScoutUnit) < 300))
			{
				fleeFlag = true;
			}
			if (!ui.getType().isWorker() && 
				((ui.getType() == UnitType.Terran_Barracks && ui.getUnit().isTraining())
				|| (cyberFlag = true && ui.getType() == UnitType.Protoss_Gateway &&  ui.getUnit().isTraining())
				|| ui.getType() == UnitType.Terran_Marine 
				|| ui.getType() == UnitType.Protoss_Dragoon
				|| (ui.getType() == UnitType.Protoss_Photon_Cannon && ui.isCompleted() && ui.getUnit().getDistance(currentScoutUnit) < ui.getType().groundWeapon().maxRange()) 
				|| (ui.getType() == UnitType.Terran_Bunker && ui.isCompleted() && ui.getUnit().getDistance(currentScoutUnit) < ui.getType().groundWeapon().maxRange())
				|| (ui.getType() == UnitType.Zerg_Sunken_Colony && ui.isCompleted() && ui.getUnit().getDistance(currentScoutUnit) < ui.getType().groundWeapon().maxRange()))
				&& (ui.getUnit().getDistance(currentScoutUnit) < 600)) 
			{
				fleeFlag = true;
				fleeLongEnemyFlag = true;
			}
			if(ui.getType() == UnitType.Terran_Barracks){
				barrackCnt++;
			}
			if(barrackCnt >= 2){
				twoBarrack = true;
			}
			
		}
		//������ �������� �ո������� ���� ���� ���
		if(gasExpscoutFlag == false){
			fleeLongEnemyFlag = false;
		}
		
		//���� ������ ���跰�̸� ���� �����°ɷ� ����
		if(twoBarrack == true){
			fleeLongEnemyFlag = true;
		}
		
		if(fleeLongEnemyFlag == true){
			enemyBaseRegionVertices = new Vector<Position>();;
			currentScoutFreeToVertexIndex = -1;
		}
	}
	//�����ȿ� ���Ÿ� ���� ���� �ִ��� ������ �Ǻ�(����/�巡��)
	public boolean enemySecondLongInRadius()
	{
		Iterator<Integer> it = InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getUnitAndUnitInfoMap().keySet().iterator();
//		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits())
		while (it.hasNext()) {
			UnitInfo ui= InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getUnitAndUnitInfoMap().get(it.next());
			if (!ui.getType().isWorker() && 
					(		 ui.getType() == UnitType.Terran_Marine 
							|| ui.getType() == UnitType.Protoss_Dragoon
							|| (ui.getType() == UnitType.Protoss_Photon_Cannon && ui.isCompleted() && ui.getUnit().getDistance(currentScoutUnit) < ui.getType().groundWeapon().maxRange()) 
							|| (ui.getType() == UnitType.Terran_Bunker && ui.isCompleted() && ui.getUnit().getDistance(currentScoutUnit) < ui.getType().groundWeapon().maxRange())
							|| (ui.getType() == UnitType.Zerg_Sunken_Colony && ui.isCompleted() && ui.getUnit().getDistance(currentScoutUnit) < ui.getType().groundWeapon().maxRange()))
							&& (ui.getUnit().getDistance(currentScoutUnit) < 400)) 
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	public Position getScoutFleePositionFromEnemyRegionVertices()
	{
		// calculate enemy region vertices if we haven't yet
		if (enemyBaseRegionVertices.isEmpty()) {
			if(fleeLongEnemyFlag == false){
				calculateEnemyRegionVertices();
			}else{
				calculateEnemySecnodRegionVertices();
			}
		}

		if (enemyBaseRegionVertices.isEmpty()) {
			return MyBotModule.Broodwar.self().getStartLocation().toPosition();
		}

		// if this is the first flee, we will not have a previous perimeter index
		if (currentScoutFreeToVertexIndex == -1)
		{
			// so return the closest position in the polygon
			int closestPolygonIndex = getClosestVertexIndex(currentScoutUnit);

			if (closestPolygonIndex == -1)
			{
				return MyBotModule.Broodwar.self().getStartLocation().toPosition();
			}
			else
			{
				// set the current index so we know how to iterate if we are still fleeing later
				currentScoutFreeToVertexIndex = closestPolygonIndex;
				return enemyBaseRegionVertices.get(closestPolygonIndex);
			}
		}
		// if we are still fleeing from the previous frame, get the next location if we are close enough
		else
		{
			double distanceFromCurrentVertex = enemyBaseRegionVertices.get(currentScoutFreeToVertexIndex).getDistance(currentScoutUnit.getPosition());

			// keep going to the next vertex in the perimeter until we get to one we're far enough from to issue another move command
			int limit =0;
			while (distanceFromCurrentVertex < 128)
			{
				limit++;
				currentScoutFreeToVertexIndex = (currentScoutFreeToVertexIndex + 1) % enemyBaseRegionVertices.size();
				distanceFromCurrentVertex = enemyBaseRegionVertices.get(currentScoutFreeToVertexIndex).getDistance(currentScoutUnit.getPosition());
				
				if(enemyBaseRegionVertices.size() < limit){
					break;
				}
			}

			return enemyBaseRegionVertices.get(currentScoutFreeToVertexIndex);
		}
	}
	
	// Enemy MainBaseLocation �� �ִ� Region �� �����ڸ���  enemyBaseRegionVertices �� �����Ѵ�
		// Region �� ��� �ǹ��� Eliminate ��Ű�� ���� ���� Ž�� ���� �ۼ��� ������ �� �ִ�
	public void calculateEnemySecnodRegionVertices()
	{
		Position enemyFirstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.enemy()).getPosition();
		if (enemyFirstExpansionLocation == null) {
			return;
		}
		// check each tile position
		enemyBaseRegionVertices.add(new Position(enemyFirstExpansionLocation.getX(), enemyFirstExpansionLocation.getY()));
		
		Position enemySecondChokePoint = InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.enemy()).getPoint();
		enemyBaseRegionVertices.add(new Position(enemySecondChokePoint.getX(), enemySecondChokePoint.getY()));
		
		if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.FightingSpririts
				|| InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.LostTemple){
			enemyBaseRegionVertices.add(new Position(enemySecondChokePoint.getX()+(enemySecondChokePoint.getX()-enemyFirstExpansionLocation.getX())
					,  enemySecondChokePoint.getY()+(enemySecondChokePoint.getY()- enemyFirstExpansionLocation.getY())));
		}

	}

	// Enemy MainBaseLocation �� �ִ� Region �� �����ڸ���  enemyBaseRegionVertices �� �����Ѵ�
	// Region �� ��� �ǹ��� Eliminate ��Ű�� ���� ���� Ž�� ���� �ۼ��� ������ �� �ִ�
	public void calculateEnemyRegionVertices()
	{
		BaseLocation enemyBaseLocation = null;
		enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());

		if (enemyBaseLocation == null) {
			return;
		}

		
		Region enemyRegion = enemyBaseLocation.getRegion();
		if (enemyRegion == null) {
			return;
		}
		final Position basePosition = MyBotModule.Broodwar.self().getStartLocation().toPosition();
		final Vector<TilePosition> closestTobase = MapTools.Instance().getClosestTilesTo(basePosition);
		Set<Position> unsortedVertices = new HashSet<Position>();

		// check each tile position
		for (int i = 0; i < closestTobase.size(); ++i)
		{
			final TilePosition tp = closestTobase.get(i);
			
			if (BWTA.getRegion(tp) != enemyRegion)
			{
				continue;
			}

			// a tile is 'surrounded' if
			// 1) in all 4 directions there's a tile position in the current region
			// 2) in all 4 directions there's a buildable tile
			boolean surrounded = true;
			if (BWTA.getRegion(new TilePosition(tp.getX() + 1, tp.getY())) != enemyRegion || !MyBotModule.Broodwar.isBuildable(new TilePosition(tp.getX() + 1, tp.getY()))
					|| BWTA.getRegion(new TilePosition(tp.getX(), tp.getY() + 1)) != enemyRegion || !MyBotModule.Broodwar.isBuildable(new TilePosition(tp.getX(), tp.getY() + 1))
					|| BWTA.getRegion(new TilePosition(tp.getX() - 1, tp.getY())) != enemyRegion || !MyBotModule.Broodwar.isBuildable(new TilePosition(tp.getX() - 1, tp.getY()))
					|| BWTA.getRegion(new TilePosition(tp.getX(), tp.getY() - 1)) != enemyRegion || !MyBotModule.Broodwar.isBuildable(new TilePosition(tp.getX(), tp.getY() - 1)))
			{
				surrounded = false;
			}

			// push the tiles that aren't surrounded 
			// Region�� �����ڸ� Ÿ�ϵ鸸 �߰��Ѵ�
			if (!surrounded && MyBotModule.Broodwar.isBuildable(tp))
			{
				if (Config.DrawScoutInfo)
				{
					int x1 = tp.getX() * 32 + 2;
					int y1 = tp.getY() * 32 + 2;
					int x2 = (tp.getX() + 1) * 32 - 2;
					int y2 = (tp.getY() + 1) * 32 - 2;
//					MyBotModule.Broodwar.drawTextMap(x1 + 3, y1 + 2, "" + MapTools.Instance().getGroundDistance(tp.toPosition(), basePosition));
//					MyBotModule.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Green, false);
				}

				unsortedVertices.add(new Position(tp.toPosition().getX() + 16, tp.toPosition().getY() + 16));
			}
		}

		Vector<Position> sortedVertices = new Vector<Position>();
		Position current = unsortedVertices.iterator().next();
		enemyBaseRegionVertices.add(current);
		unsortedVertices.remove(current);

		// while we still have unsorted vertices left, find the closest one remaining to current
		while (!unsortedVertices.isEmpty())
		{
			double bestDist = 1000000;
			Position bestPos = null;

			for (final Position pos : unsortedVertices)
			{
				double dist = pos.getDistance(current);

				if (dist < bestDist)
				{
					bestDist = dist;
					bestPos = pos;
				}
			}

			current = bestPos;
			sortedVertices.add(bestPos);
			unsortedVertices.remove(bestPos);
		}

		// let's close loops on a threshold, eliminating death grooves
		int distanceThreshold = 100;

		while (true)
		{
			// find the largest index difference whose distance is less than the threshold
			int maxFarthest = 0;
			int maxFarthestStart = 0;
			int maxFarthestEnd = 0;

			// for each starting vertex
			for (int i = 0; i < (int)sortedVertices.size(); ++i)
			{
				int farthest = 0;
				int farthestIndex = 0;

				// only test half way around because we'll find the other one on the way back
				for (int j= 1; j < sortedVertices.size() / 2; ++j)
				{
					int jindex = (i + j) % sortedVertices.size();

					if (sortedVertices.get(i).getDistance(sortedVertices.get(jindex)) < distanceThreshold)
					{
						farthest = j;
						farthestIndex = jindex;
					}
				}

				if (farthest > maxFarthest)
				{
					maxFarthest = farthest;
					maxFarthestStart = i;
					maxFarthestEnd = farthestIndex;
				}
			}

			// stop when we have no long chains within the threshold
			if (maxFarthest < 4)
			{
				break;
			}

			double dist = sortedVertices.get(maxFarthestStart).getDistance(sortedVertices.get(maxFarthestEnd));

			Vector<Position> temp = new Vector<Position>();

			for (int s = maxFarthestEnd; s != maxFarthestStart; s = (s + 1) % sortedVertices.size())
			{
				
				temp.add(sortedVertices.get(s));
			}

			sortedVertices = temp;
		}

		enemyBaseRegionVertices = sortedVertices;
	}

	public int getClosestVertexIndex(Unit unit)
	{
		int closestIndex = -1;
		double closestDistance = 10000000;

		for (int i = 0; i < enemyBaseRegionVertices.size(); ++i)
		{
			double dist = unit.getDistance(enemyBaseRegionVertices.get(i));
			if (dist < closestDistance)
			{
				closestDistance = dist;
				closestIndex = i;
			}
		}

		return closestIndex;
	}
	
	/// ���� ������ �����մϴ�
	public Unit getScoutUnit()
	{
		return currentScoutUnit;
	}

	// ���� ���¸� �����մϴ�
	public int getScoutStatus()
	{
		return currentScoutStatus;
	}

	/// ���� ������ �̵� ��ǥ BaseLocation �� �����մϴ�
	public BaseLocation getScoutTargetBaseLocation()
	{
		return currentScoutTargetBaseLocation;
	}

	/// ������ Main Base Location �� �ִ� Region �� ��輱�� �ش��ϴ� Vertex ���� ����� �����մϴ�
	public Vector<Position> getEnemyRegionVertices()
	{
		return enemyBaseRegionVertices;
	}
}