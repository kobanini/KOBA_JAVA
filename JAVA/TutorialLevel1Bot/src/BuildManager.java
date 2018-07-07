import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bwapi.Pair;
import bwapi.Position;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitCommandType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;

/// ����(�ǹ� �Ǽ� / ���� �Ʒ� / ��ũ ����ġ / ���׷��̵�) ����� ���������� �����ϱ� ���� ���� ť�� �����ϰ�, ���� ť�� �ִ� ����� �ϳ��� �����ϴ� class<br>
/// ���� ��� �� �ǹ� �Ǽ� ����� ConstructionManager�� �����մϴ�
/// @see ConstructionManager
public class BuildManager {

	/// BuildOrderItem ���� ����� �����ϴ� buildQueue 
	public BuildOrderQueue buildQueue = new BuildOrderQueue();

	private static BuildManager instance = new BuildManager();

	public Boolean MainBaseLocationFull;
	public Boolean FirstChokePointFull;
	public Boolean FirstExpansionLocationFull;
	public Boolean SecondChokePointFull;
	public Boolean FisrtSupplePointFull;
	
	/// static singleton ��ü�� �����մϴ�
	public static BuildManager Instance() {
		return instance;
	}
	
	public BuildManager() {
		MainBaseLocationFull = false;
		FirstChokePointFull = false;
		FirstExpansionLocationFull = false;
		SecondChokePointFull = false;
		FisrtSupplePointFull = false;
		
	}

	/// buildQueue �� ���� Dead lock �� ������ �����ϰ�, ���� �켱������ ���� BuildOrderItem �� ����ǵ��� �õ��մϴ�
	public void update() {
		
		
		// 1��(24������)�� 4�� ������ �����ص� ����ϴ�
		if (MyBotModule.Broodwar.getFrameCount() % 7 != 0){
			return;
		}

		if (buildQueue.isEmpty()) {
			return;
		}

		
		
		// Dead Lock �߿� �ռ� �ǹ��� ���� ��� �߰��Ѵ�.
		checkBuildOrderQueueDeadlockAndInsert();
		// Dead Lock �� üũ�ؼ� �����Ѵ�
		
		checkBuildOrderQueueDeadlockAndAndFixIt();
		
		// Dead Lock ������ Empty �� �� �ִ�
		if (buildQueue.isEmpty()) {
			return;
		}

		// the current item to be used
		BuildOrderItem currentItem = buildQueue.getHighestPriorityItem();

		//System.out.println("current HighestPriorityItem is " + currentItem.metaType.getName());

		// while there is still something left in the buildQueue
		while (!buildQueue.isEmpty()) 
		{
			boolean isOkToRemoveQueue = true;

			// seedPosition �� �����Ѵ�
			Position seedPosition = null;
			if (currentItem.seedLocation != TilePosition.None && currentItem.seedLocation != TilePosition.Invalid 
					&& currentItem.seedLocation != TilePosition.Unknown && currentItem.seedLocation.isValid()) 
			{
				seedPosition = currentItem.seedLocation.toPosition();
				System.out.println("currentItem.seedLocation=" + currentItem.seedLocation + "seedPosition="
						+ seedPosition + " " + new Exception().getStackTrace()[0].getLineNumber());
			}
			else 
			{
				seedPosition = getSeedPositionFromSeedLocationStrategy(currentItem.seedLocationStrategy);
				System.out.println("currentItem.seedLocation=" + currentItem.seedLocation + "seedPosition="
						+ seedPosition + " " + new Exception().getStackTrace()[0].getLineNumber());
			}
			
			// this is the unit which can produce the currentItem
			Unit producer = getProducer(currentItem.metaType, seedPosition, currentItem.producerID);
					
			/*
			 * if (currentItem.metaType.isUnit() &&
			 * currentItem.metaType.getUnitType().isBuilding()) { if (producer
			 * != null) { System.out.println("Build " +
			 * currentItem.metaType.getName() + " producer : " +
			 * producer.getType() + " ID : " + producer.getID()); } else {
			 * System.out.println("Build " + currentItem.metaType.getName() +
			 * " producer null"); } }
			 */

			boolean canMake = false;

			// �ǹ��� ����� �ִ� ����(�ϲ�)�̳�, ������ ����� �ִ� ����(�ǹ� or ����)�� ������
			if (producer != null) {

				// check to see if we can make it right now
				// ���� �ش� ������ �Ǽ�/���� �� �� �ִ����� ���� �ڿ�, ���ö���, ��ũ Ʈ��, producer ���� ����
				// �Ǵ��Ѵ�
				canMake = canMakeNow(producer, currentItem.metaType);

				/*
				 * if (currentItem.metaType.isUnit() &&
				 * currentItem.metaType.getUnitType().isBuilding() ) { std::cout
				 * + "Build " + currentItem.metaType.getName() +
				 * " canMakeNow : " + canMake + std::endl; }
				 */
			}

//			// SCV ���� ���� (�̳׶� 1���� 2�� SCV)
//			if (currentItem.metaType.getUnitType() == UnitType.Terran_SCV) {
//				int numScv = InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumUnits("Terran_SCV");
//				int numCmdCenter = InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumUnits("Terran_Command_Center");
//				if (numCmdCenter * 20 < numScv) {
////					System.out.println(currentItem.metaType.getUnitType()+"numver is too many"+" numCmdCenter="+numCmdCenter+" numScv="+numScv);
//					canMake = false;
//				}
//			}
			
			// if we can make the current item, create it
			if (producer != null && canMake == true) {
				MetaType t = currentItem.metaType;

				if (t.isUnit()) {
					if (t.getUnitType().isBuilding()) {

						// �׶� Addon �ǹ��� ��� (Addon �ǹ��� ������ �ִ����� getProducer �Լ����� �̹� üũ�Ϸ�)
						// ��ǹ��� Addon �ǹ� ���� ������ canBuildAddon = true,
						// isConstructing = false, canCommand = true �̴ٰ�
						// Addon �ǹ��� ���� �����ϸ� canBuildAddon = false,
						// isConstructing = true, canCommand = true �� �ǰ� (Addon
						// �ǹ� �Ǽ� ��Ҵ� �����ϳ� Train �� Ŀ�ǵ�� �Ұ���)
						// �ϼ��Ǹ� canBuildAddon = false, isConstructing = false ��
						// �ȴ�
						if (t.getUnitType().isAddon()) {

							producer.buildAddon(t.getUnitType());
							// �׶� Addon �ǹ��� ��� ���������� buildAddon ����� ������ SCV�� ��ǹ�
							// ��ó�� ���� �� �ѵ��� buildAddon ����� ��ҵǴ� ��찡 �־
							// ��ǹ��� isConstructing = true ���·� �ٲ� ���� Ȯ���� ��
							// buildQueue ���� �����ؾ��Ѵ�
							if (producer.isConstructing() == false) {
								isOkToRemoveQueue = false;
							}
						}
						// �׿� ��κ� �ǹ��� ���
						else {
							// ConstructionPlaceFinder �� ���� �Ǽ� ���� ��ġ
							// desiredPosition �� �˾Ƴ���
							// ConstructionManager �� ConstructionTask Queue�� �߰���
							// �ؼ� desiredPosition �� �Ǽ��� �ϰ� �Ѵ�.
							// ConstructionManager �� �Ǽ� ���߿� �ش� ��ġ�� �Ǽ��� ��������� �ٽ�
							// ConstructionPlaceFinder �� ���� �Ǽ� ���� ��ġ��
							// desiredPosition �������� ã�� ���̴�
//if(currentItem.seedLocation==null)
//{
//	System.out.println(currentItem.seedLocationStrategy);
//}
//else
//{
//	System.out.print("currentItem.seedLocation("+currentItem.seedLocation.getX()+","+currentItem.seedLocation.getY()+")");
//	System.out.println(currentItem.seedLocationStrategy);	
//}

							TilePosition desiredPosition = getDesiredPosition(t.getUnitType(), currentItem.seedLocation,currentItem.seedLocationStrategy);
							if(currentItem.forcedType == true)
							{
								System.out.println("desiredPosition is forced mode "+currentItem.metaType.getUnitType());
								desiredPosition = currentItem.seedLocation;
							}
							
							// std::cout << "BuildManager " +
							// currentItem.metaType.getUnitType().getName().c_str()
							// + " desiredPosition " + desiredPosition.x + "," +
							// desiredPosition.y + std::endl;

							if (desiredPosition != TilePosition.None) {
								// Send the construction task to the
								// construction manager
//System.out.println("t.getUnitType()="+t.getUnitType()+"("+desiredPosition.getX()+" "+desiredPosition.getY()+")");
								ConstructionManager.Instance().addConstructionTask(t.getUnitType(), desiredPosition, currentItem.forcedType);
							} else {
								// �ǹ� ���� ��ġ�� ���� ����, Protoss_Pylon �� ���ų�, Creep
								// �� ���ų�, Refinery �� �̹� �� �������ְų�, ���� ���� ������ ������
								// ���� ����ε�,
								// ��κ��� ��� Pylon �̳� Hatchery�� �������� �ִ� ���̹Ƿ�, ����
								// frame �� �ǹ� ���� ������ �ٽ� Ž���ϵ��� �Ѵ�.
								System.out.println("There is no place to construct " + currentItem.metaType.getUnitType()+ " strategy " + currentItem.seedLocationStrategy);
								if (currentItem.seedLocation != null)
									System.out.println(" seedPosition " + currentItem.seedLocation.getX() + ","+ currentItem.seedLocation.getY());
								if (desiredPosition != null)
									System.out.println(" desiredPosition " + desiredPosition.getX() + ","+ desiredPosition.getY());
								
								isOkToRemoveQueue = false;
							}
						}
					}
					// �������� / ���������� ���
					else {
//System.out.println("t.getUnitType()="+t.getUnitType()+" "+new Exception().getStackTrace()[0].getLineNumber());	
						producer.train(t.getUnitType());
//						if(producer.isTraining() == false){
//							isOkToRemoveQueue = false;
//						}
					}
				}
				// if we're dealing with a tech research
				else if (t.isTech()) {
					producer.research(t.getTechType());
				} else if (t.isUpgrade()) {
					producer.upgrade(t.getUpgradeType());
				}
				// remove it from the buildQueue
				if (isOkToRemoveQueue) {
//					System.out.println("here I am!!! Killing: " + buildQueue.getItem().metaType.getName());
					buildQueue.removeCurrentItem();
				}
				
				// don't actually loop around in here
				break;
			}
			// otherwise, if we can skip the current item
			else if (buildQueue.canSkipCurrentItem()) {
//System.out.println("canMake="+canMake+" "+"currentItem.metaType.getUnitType()"+currentItem.metaType.getUnitType()+new Exception().getStackTrace()[0].getLineNumber());
				// skip it and get the next one
				buildQueue.skipCurrentItem();
				currentItem = buildQueue.getItem();
			} else {
//System.out.println("canMake="+canMake+" "+"currentItem.metaType.getUnitType()"+currentItem.metaType.getUnitType()+new Exception().getStackTrace()[0].getLineNumber());
				// so break out
				break;
			}
		}
	}

	/// �ش� MetaType �� build �� �� �ִ� producer �� ã�� ��ȯ�մϴ�
	/// @param t �����Ϸ��� ����� Ÿ��
	/// @param closestTo �Ķ��Ÿ �Է� �� producer �ĺ��� �� �ش� position ���� ���� ����� producer �� �����մϴ�
	/// @param producerID �Ķ��Ÿ �Է� �� �ش� ID�� unit �� producer �ĺ��� �� �� �ֽ��ϴ�
	public Unit getProducer(MetaType t, Position closestTo, int producerID) {
		// get the type of unit that builds this
		UnitType producerType = t.whatBuilds();

		// make a set of all candidate producers
		List<Unit> candidateProducers = new ArrayList<Unit>();
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit == null)
				continue;

			// reasons a unit can not train the desired type
			if (unit.getType() != producerType) {
				continue;
			}
			if (!unit.exists()) {
				continue;
			}
			if (!unit.isCompleted()) {
				continue;
			}
			if (unit.isTraining()) {
				continue;
			}
			if (!unit.isPowered()) {
				continue;
			}
			// if unit is lifted, unit should land first
			if (unit.isLifted()) {
				continue;
			}
			
			if (unit.isUpgrading() || unit.isResearching()) {
				continue;
			}
			
			if (producerID != -1 && unit.getID() != producerID)	{ 
				continue; 
			}
			if ((producerType == UnitType.Terran_Factory || producerType == UnitType.Terran_Starport || producerType == UnitType.Terran_Science_Facility || producerType == UnitType.Terran_Command_Center) && unit.getType() == producerType && unit.isConstructing() == true ) {
				continue;
			}

			
			
			if (t.isUnit()) {
				// if the type dd an addon and the producer doesn't have
				// one
				// C++ : typedef std::pair<BWAPI::UnitType, int> ReqPair;
				Pair<UnitType, Integer> ReqPair = null;

				Map<UnitType, Integer> requiredUnitsMap = t.getUnitType().requiredUnits();
				if (requiredUnitsMap != null) {
					Iterator<UnitType> it = requiredUnitsMap.keySet().iterator();

					// for (final Pair<UnitType, Integer> pair :
					// t.getUnitType().requiredUnits())
					while (it.hasNext()) {
						UnitType requiredType = it.next();
						if (requiredType.isAddon()) {
							if (unit.getAddon() == null || (unit.getAddon().getType() != requiredType)) {
								continue;
							}
						}
					}
				}

				// if the type is an addon
				if (t.getUnitType().isAddon()) {
					// if the unit already has an addon, it can't make one
					if (unit.getAddon() != null) {
						continue;
					}

					// ��ǹ��� �Ǽ��ǰ� �ִ� �߿��� isCompleted = false, isConstructing =
					// true, canBuildAddon = false �̴ٰ�
					// �Ǽ��� �ϼ��� �� �� �����ӵ����� isCompleted = true ������, canBuildAddon
					// = false �� ��찡 �ִ�
					if (!unit.canBuildAddon()) {
						continue;
					}

					// if we just told this unit to build an addon, then it will
					// not be building another one
					// this deals with the frame-delay of telling a unit to
					// build an addon and it actually starting to build
					if (unit.getLastCommand().getUnitCommandType() == UnitCommandType.Build_Addon // C++
																									// :
																									// unit.getLastCommand().getType()
							&& (MyBotModule.Broodwar.getFrameCount() - unit.getLastCommandFrame() < 10)) {
						continue;
					}

					boolean isBlocked = false;

					// if the unit doesn't have space to build an addon, it
					// can't make one
					TilePosition addonPosition = new TilePosition(
							unit.getTilePosition().getX() + unit.getType().tileWidth(),
							unit.getTilePosition().getY() + unit.getType().tileHeight() - t.getUnitType().tileHeight());

					for (int i = 0; i < t.getUnitType().tileWidth(); ++i) {
						for (int j = 0; j < t.getUnitType().tileHeight(); ++j) {
							TilePosition tilePos = new TilePosition(addonPosition.getX() + i, addonPosition.getY() + j);

							// if the map won't let you build here, we can't
							// build it.
							// �� Ÿ�� ��ü�� �Ǽ� �Ұ����� Ÿ���� ��� + ���� �ǹ��� �ش� Ÿ�Ͽ� �̹� �ִ°��
							if (!MyBotModule.Broodwar.isBuildable(tilePos, true)) {
								isBlocked = true;
							}

							// if there are any units on the addon tile, we
							// can't build it
							// �Ʊ� ������ Addon ���� ��ġ�� �־ ������. (���� ������ Addon ���� ��ġ��
							// ������ �Ǽ� �ȵǴ����� ���� ��Ȯ����)
							for (Unit u : MyBotModule.Broodwar.getUnitsOnTile(tilePos.getX(), tilePos.getY())) {
								//System.out.println("Construct " + t.getName() + " beside " + unit.getType() + "("
								//		+ unit.getID() + ")" + ", units on Addon Tile " + tilePos.getX() + ","
								//		+ tilePos.getY() + " is " + u.getType() + "(ID : " + u.getID() + " Player : "
								//		+ u.getPlayer().getName() + ")");
								if (u.getPlayer() != InformationManager.Instance().selfPlayer) {
									isBlocked = false;
								}
							}
						}
					}

					if (isBlocked) {
						continue;
					}
				}
			}

			// if we haven't cut it, add it to the set of candidates
			candidateProducers.add(unit); // C++ :
											// candidateProducers.insert(unit);

		}

		return getClosestUnitToPosition(candidateProducers, closestTo);
	}

	/// �ش� MetaType �� build �� �� �ִ� producer �� ã�� ��ȯ�մϴ�
	public Unit getProducer(MetaType t, Position closestTo) {
		return getProducer(t, closestTo, -1);
	}

	/// �ش� MetaType �� build �� �� �ִ� producer �� ã�� ��ȯ�մϴ�
	public Unit getProducer(MetaType t) {
		return getProducer(t, Position.None, -1);
	}

	/*
	/// �ش� MetaType �� build �� �� �ִ�, getProducer ���ϰ��� �ٸ� producer �� ã�� ��ȯ�մϴ�<br>
	/// �����佺 ���� ���� �� Protoss_Archon / Protoss_Dark_Archon �� ������ �� ����մϴ�
	public Unit getAnotherProducer(Unit producer, Position closestTo) {
		if (producer == null)
			return null;

		Unit closestUnit = null;

		List<Unit> candidateProducers = new ArrayList<Unit>();
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit == null) {
				continue;
			}
			if (unit.getType() != producer.getType()) {
				continue;
			}
			if (unit.getID() == producer.getID()) {
				continue;
			}
			if (!unit.isCompleted()) {
				continue;
			}
			if (unit.isTraining()) {
				continue;
			}
			if (!unit.exists()) {
				continue;
			}
			if (unit.getHitPoints() + unit.getEnergy() <= 0) {
				continue;
			}

			candidateProducers.add(unit); // C++ :
											// candidateProducers.insert(unit);
		}

		return getClosestUnitToPosition(candidateProducers, closestTo);
	}
	*/
	
	public Unit getClosestUnitToPosition(final List<Unit> units, Position closestTo) {
		if (units.size() == 0) {
			return null;
		}

		// if we don't care where the unit is return the first one we have
		if (closestTo == Position.None || closestTo == Position.Invalid || closestTo == Position.Unknown || closestTo.isValid() == false) {
			return units.get(0); // C++ : return units.begin();
		}

		Unit closestUnit = null;
		double minDist = 1000000000;

		for (Unit unit : units) {
			if (unit == null)
				continue;

			double distance = unit.getDistance(closestTo);
			if (closestUnit == null || distance < minDist) {
				closestUnit = unit;
				minDist = distance;
			}
		}

		return closestUnit;
	}

	// ���� �ش� ������ �Ǽ�/���� �� �� �ִ����� ���� �ڿ�, ���ö���, ��ũ Ʈ��, producer ���� ���� �Ǵ��Ѵ�<br>
	// �ش� ������ �ǹ��� ��� �ǹ� ���� ��ġ�� ���� ���� (Ž���߾��� Ÿ������, �Ǽ� ������ Ÿ������, ������ Pylon�� �ִ���,<br>
	// Creep�� �ִ� ������ ��) �� �Ǵ����� �ʴ´�
	public boolean canMakeNow(Unit producer, MetaType t) {
		if (producer == null) {
			return false;
		}

		boolean canMake = hasEnoughResources(t);

		if (canMake) {
			if (t.isUnit()) {
				// MyBotModule.Broodwar.canMake : Checks all the requirements
				// include resources, supply, technology tree, availability, and
				// required units
				canMake = MyBotModule.Broodwar.canMake(t.getUnitType(), producer);
			} else if (t.isTech()) {
				canMake = MyBotModule.Broodwar.canResearch(t.getTechType(), producer);
			} else if (t.isUpgrade()) {
				canMake = MyBotModule.Broodwar.canUpgrade(t.getUpgradeType(), producer);
			}
		}

		//TODO ��������?
		if(producer.getType() == UnitType.Terran_Factory || producer.getType() == UnitType.Terran_Starport || producer.getType() == UnitType.Terran_Science_Facility || producer.getType() == UnitType.Terran_Command_Center){
			if(producer.canBuildAddon()==false && producer.isConstructing() == true){
				canMake = false;
			}
		}
		// �׶� Addon �ǹ��� ��� (Addon �ǹ��� ������ �ִ����� getProducer �Լ����� �̹� üũ�Ϸ�)
		// ��ǹ��� Addon �ǹ� ���� ������ canBuildAddon = true,
		// isConstructing = false, canCommand = true �̴ٰ�
		// Addon �ǹ��� ���� �����ϸ� canBuildAddon = false,
		// isConstructing = true, canCommand = true �� �ǰ� (Addon
		// �ǹ� �Ǽ� ��Ҵ� �����ϳ� Train �� Ŀ�ǵ�� �Ұ���)
		// �ϼ��Ǹ� canBuildAddon = false, isConstructing = false ��
		// �ȴ�
			// �׶� Addon �ǹ��� ��� ���������� buildAddon ����� ������ SCV�� ��ǹ�
			// ��ó�� ���� �� �ѵ��� buildAddon ����� ��ҵǴ� ��찡 �־
			// ��ǹ��� isConstructing = true ���·� �ٲ� ���� Ȯ���� ��
			// buildQueue ���� �����ؾ��Ѵ�
		
		return canMake;
	}

	// �Ǽ� ���� ��ġ�� ã�´�<br>
	// seedLocationStrategy �� SeedPositionSpecified �� ��쿡�� �� ��ó�� ã�ƺ���,<br>
	// SeedPositionSpecified �� �ƴ� ��쿡�� seedLocationStrategy �� ���ݾ� �ٲ㰡�� ��� ã�ƺ���.<br>
	// (MainBase . MainBase ���� . MainBase ��� . MainBase ����� �ո��� . MainBase ����� �ո����� ��� . Ž�� ����)
	public TilePosition getDesiredPosition(UnitType unitType, TilePosition seedPosition,BuildOrderItem.SeedPositionStrategy seedPositionStrategy) {
		
		switch (seedPositionStrategy) {
		case MainBaseLocation:
			if(MainBaseLocationFull == true){
				seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.LastBuilingPoint;//TODO ���� �˻� ��ġ
			}
			break;
//		case MainBaseBackYard:
//			//seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.FirstChokePoint;
//			break;
		case FirstChokePoint:
			if(FirstChokePointFull == true){
				seedPositionStrategy =  BuildOrderItem.SeedPositionStrategy.LastBuilingPoint;//TODO ���� �˻� ��ġ
			}
			break;
		case FirstExpansionLocation:
			if(FirstExpansionLocationFull == true){
				seedPositionStrategy =  BuildOrderItem.SeedPositionStrategy.LastBuilingPoint;//TODO ���� �˻� ��ġ
			}
			break;
		case SecondChokePoint:
			if(SecondChokePointFull == true){
				seedPositionStrategy =  BuildOrderItem.SeedPositionStrategy.LastBuilingPoint;//TODO ���� �˻� ��ġ
			}
			break;
		case NextExpansionPoint:
			break;
			
		case NextSupplePoint:
			if(FisrtSupplePointFull == true){
				if(MainBaseLocationFull == true){
					seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.LastBuilingPoint;
				}else{
					seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.MainBaseLocation;
				}
			}
			break;
			
		case LastBuilingPoint:
			seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.getLastBuilingFinalLocation;
			break;
			
		default:
			break;
		}
		
		TilePosition desiredPosition = ConstructionPlaceFinder.Instance().getBuildLocationWithSeedPositionAndStrategy(unitType, seedPosition, seedPositionStrategy);
		/*
		 * std::cout +
		 * "ConstructionPlaceFinder getBuildLocationWithSeedPositionAndStrategy "
		 * + unitType.getName().c_str() + " strategy " + seedPositionStrategy +
		 * " seedPosition " + seedPosition.x + "," + seedPosition.y +
		 * " desiredPosition " + desiredPosition.x + "," + desiredPosition.y +
		 * std::endl;
		 */

		boolean findAnotherPlace = true;
		// desiredPosition �� ã�� �� ���� ���
		while (desiredPosition == TilePosition.None) {

			switch (seedPositionStrategy) {
			case MainBaseLocation:
				seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.LastBuilingPoint;//TODO ���� �˻� ��ġ
				break;
			case MainBaseBackYard:
				seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.LastBuilingPoint;
				break;
			case FirstChokePoint:
				seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.LastBuilingPoint;
				break;
			case FirstExpansionLocation:
				seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.LastBuilingPoint;
				break;
			case SecondChokePoint:
				seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.LastBuilingPoint;
				break;
			case NextSupplePoint:
				seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.LastBuilingPoint;
				break;
			case LastBuilingPoint:
				seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.getLastBuilingFinalLocation;
				break;
			
			case NextExpansionPoint:
			case SeedPositionSpecified:
			case getLastBuilingFinalLocation:
			default:
				findAnotherPlace = false;
				break;
			}

			// �ٸ� ���� �� ã�ƺ���
			if (findAnotherPlace) {
				desiredPosition = ConstructionPlaceFinder.Instance().getBuildLocationWithSeedPositionAndStrategy(unitType, seedPosition, seedPositionStrategy);
				/*
				 * std::cout +
				 * "ConstructionPlaceFinder getBuildLocationWithSeedPositionAndStrategy "
				 * + unitType.getName().c_str() + " strategy " +
				 * seedPositionStrategy + " seedPosition " + seedPosition.x +
				 * "," + seedPosition.y + " desiredPosition " +
				 * desiredPosition.x + "," + desiredPosition.y + std::endl;
				 */
			}
			// @@@@@@ ���⼭ �����ϸ� ��? �ٸ� ���� �� ã�ƺ��� �ʰ�, ������
			else {
				break;
			}
		}

		return desiredPosition;
	}

	// ��밡�� �̳׶� = ���� ���� �̳׶� - ����ϱ�� ����Ǿ��ִ� �̳׶�
	public int getAvailableMinerals() {
		return MyBotModule.Broodwar.self().minerals() - ConstructionManager.Instance().getReservedMinerals();
	}

	// ��밡�� ���� = ���� ���� ���� - ����ϱ�� ����Ǿ��ִ� ����
	public int getAvailableGas() {
		return MyBotModule.Broodwar.self().gas() - ConstructionManager.Instance().getReservedGas();
	}

	// return whether or not we meet resources, including building reserves
	public boolean hasEnoughResources(MetaType type) {
		// return whether or not we meet the resources
		return (type.mineralPrice() <= getAvailableMinerals()) && (type.gasPrice() <= getAvailableGas());
	}

	// selects a unit of a given type
	public Unit selectUnitOfType(UnitType type, Position closestTo) {
		// if we have none of the unit type, return null right away
		if (MyBotModule.Broodwar.self().completedUnitCount(type) == 0) {
			return null;
		}

		Unit unit = null;

		// if we are concerned about the position of the unit, that takes
		// priority
		if (closestTo != Position.None) {
			double minDist = 1000000000;

			for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
				if (u.getType() == type) {
					double distance = u.getDistance(closestTo);
					if (unit == null || distance < minDist) {
						unit = u;
						minDist = distance;
					}
				}
			}

			// if it is a building and we are worried about selecting the unit
			// with the least
			// amount of training time remaining
		} else if (type.isBuilding()) {
			for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
				if (u.getType() == type && u.isCompleted() && !u.isTraining() && !u.isLifted() && u.isPowered()) {

					return u;
				}
			}
			// otherwise just return the first unit we come across
		} else {
			for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
				if (u.getType() == type && u.isCompleted() && u.getHitPoints() > 0 && !u.isLifted() && u.isPowered()) {
					return u;
				}
			}
		}

		// return what we've found so far
		return null;
	}

	/// BuildOrderItem ���� ����� �����ϴ� buildQueue �� �����մϴ�
	public BuildOrderQueue getBuildQueue() {
		return buildQueue;
	}

	/// seedPositionStrategy �� ���� ���ӻ�Ȳ�� �°� seedPosition ���� �ٲپ� �����մϴ�
	private Position getSeedPositionFromSeedLocationStrategy(BuildOrderItem.SeedPositionStrategy seedLocationStrategy) {
		Position seedPosition = null;
		Chokepoint tempChokePoint;
		BaseLocation tempBaseLocation;
		TilePosition tempTilePosition = null;
		Region tempBaseRegion;
		int vx, vy;
		double d, theta;
		int bx, by;

		switch (seedLocationStrategy) {
		case MainBaseLocation:
			tempBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
			if (tempBaseLocation != null) {
				seedPosition = tempBaseLocation.getPosition(); 
			}
			break;
		case MainBaseBackYard:
			tempBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
			tempChokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer);
			tempBaseRegion = BWTA.getRegion(tempBaseLocation.getPosition());

			//std::cout << "y";

			// (vx, vy) = BaseLocation �� ChokePoint �� ���� ���� = �Ÿ� d �� ���� t ����. ������ position
			// ��Ÿũ����Ʈ ��ǥ�� : ���������� ������ x �� ���� (��ī��Ʈ ��ǥ��� ����). �Ʒ��� ������ y�� ���� (y�ุ ��ī��Ʈ ��ǥ��� �ݴ�)
			// �ﰢ�Լ� ���� ��ī��Ʈ ��ǥ�迡�� ����ϹǷ�, vy�� ��ȣ �ݴ�� �ؼ� ���� t ���� ���� 

			// MainBaseLocation �� null �̰ų�, ChokePoint �� null �̸�, MainBaseLocation �������� ������ ���� �����Ѵ�
			if (tempBaseLocation != null && tempChokePoint != null) {
	
				// BaseLocation ���� ChokePoint ���� ���͸� ���Ѵ�
				vx = tempChokePoint.getCenter().getX() - tempBaseLocation.getPosition().getX();
				//std::cout << "vx : " << vx ;
				vy = (tempChokePoint.getCenter().getY() - tempBaseLocation.getPosition().getY()) * (-1);
				//std::cout << "vy : " << vy;
				d = Math.sqrt(vx * vx + vy * vy) * 0.5; // BaseLocation �� ChokePoint �� �Ÿ����� ���� ª�� �Ÿ��� ����. BaseLocation�� �ִ� Region�� ��κ� ���簢�� �����̱� ����
				//std::cout << "d : " << d;
				theta = Math.atan2(vy, vx + 0.0001); // ���� ����
				//std::cout << "t : " << t;
	
				// cos(t+90), sin(t+180) �� �ﰢ�Լ� Trigonometric functions of allied angles �� �̿�. y�࿡ ���ؼ��� �ݴ��ȣ�� ����
	
				// BaseLocation ���� ChokePoint �ݴ��� ������ Back Yard : ��ī��Ʈ ��ǥ�迡�� (cos(t+180) = -cos(t), sin(t+180) = -sin(t))
				bx = tempBaseLocation.getTilePosition().getX() - (int)(d * Math.cos(theta) / Config.TILE_SIZE);
				by = tempBaseLocation.getTilePosition().getY() + (int)(d * Math.sin(theta) / Config.TILE_SIZE);
				//std::cout << "i";
				tempTilePosition = new TilePosition(bx, by);
				// std::cout << "ConstructionPlaceFinder MainBaseBackYard tempTilePosition " << tempTilePosition.x << "," << tempTilePosition.y << std::endl;
				
				//std::cout << "k";
				// �ش� ������ ���� Region �� ���ϰ� Buildable �� Ÿ������ Ȯ��
				if (!tempTilePosition.isValid() || !MyBotModule.Broodwar.isBuildable(tempTilePosition.getX(), tempTilePosition.getY(), false) || tempBaseRegion != BWTA.getRegion(new Position(bx*Config.TILE_SIZE, by*Config.TILE_SIZE))) {
					//std::cout << "l";
	
					// BaseLocation ���� ChokePoint ���⿡ ���� ���������� 90�� ���� ������ Back Yard : ��ī��Ʈ ��ǥ�迡�� (cos(t-90) = sin(t),   sin(t-90) = - cos(t))
					bx = tempBaseLocation.getTilePosition().getX() + (int)(d * Math.sin(theta) / Config.TILE_SIZE);
					by = tempBaseLocation.getTilePosition().getY() + (int)(d * Math.cos(theta) / Config.TILE_SIZE);
					tempTilePosition = new TilePosition(bx, by);
					// std::cout << "ConstructionPlaceFinder MainBaseBackYard tempTilePosition " << tempTilePosition.x << "," << tempTilePosition.y << std::endl;
					//std::cout << "m";
	
					if (!tempTilePosition.isValid() || !MyBotModule.Broodwar.isBuildable(tempTilePosition.getX(), tempTilePosition.getY(), false)) {
						// BaseLocation ���� ChokePoint ���⿡ ���� �������� 90�� ���� ������ Back Yard : ��ī��Ʈ ��ǥ�迡�� (cos(t+90) = -sin(t),   sin(t+90) = cos(t))
						bx = tempBaseLocation.getTilePosition().getX() - (int)(d * Math.sin(theta) / Config.TILE_SIZE);
						by = tempBaseLocation.getTilePosition().getY() - (int)(d * Math.cos(theta) / Config.TILE_SIZE);
						tempTilePosition = new TilePosition(bx, by);
						// std::cout << "ConstructionPlaceFinder MainBaseBackYard tempTilePosition " << tempTilePosition.x << "," << tempTilePosition.y << std::endl;
	
						if (!tempTilePosition.isValid() || !MyBotModule.Broodwar.isBuildable(tempTilePosition.getX(), tempTilePosition.getY(), false) || tempBaseRegion != BWTA.getRegion(new Position(bx*Config.TILE_SIZE, by*Config.TILE_SIZE))) {
	
							// BaseLocation ���� ChokePoint ���� ���� ������ Back Yard : ��ī��Ʈ ��ǥ�迡�� (cos(t),   sin(t))
							bx = tempBaseLocation.getTilePosition().getX() + (int)(d * Math.cos(theta) / Config.TILE_SIZE);
							by = tempBaseLocation.getTilePosition().getY() - (int)(d * Math.sin(theta) / Config.TILE_SIZE);
							tempTilePosition = new TilePosition(bx, by);
							// std::cout << "ConstructionPlaceFinder MainBaseBackYard tempTilePosition " << tempTilePosition.x << "," << tempTilePosition.y << std::endl;
							//std::cout << "m";
						}
	
					}
				}
				//std::cout << "z";
				if (tempTilePosition.isValid() == false 
					|| MyBotModule.Broodwar.isBuildable(tempTilePosition.getX(), tempTilePosition.getY(), false) == false) {
					seedPosition = tempTilePosition.toPosition();
				}
				else {
					seedPosition = tempBaseLocation.getPosition();
				}
			}
			//std::cout << "w";
			// std::cout << "ConstructionPlaceFinder MainBaseBackYard desiredPosition " << desiredPosition.x << "," << desiredPosition.y << std::endl;
			break;

		case FirstExpansionLocation:
			tempBaseLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self());
			if (tempBaseLocation != null) {
				seedPosition = tempBaseLocation.getPosition();
			}
			break;

		case FirstChokePoint:
			tempChokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer);
			if (tempChokePoint != null) {
				seedPosition = tempChokePoint.getCenter();
			}
			break;

		case SecondChokePoint:
			tempChokePoint = InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.self());
			if (tempChokePoint != null) {
				seedPosition = tempChokePoint.getCenter();
			}
			break;
		default:
			break;
		}

		return seedPosition;
	}

	/// buildQueue �� Dead lock ���θ� �Ǵ��ϱ� ����, ���� �켱������ ���� BuildOrderItem �� producer �� �����ϰԵ� ������ ���θ� �����մϴ�
	public boolean isProducerWillExist(UnitType producerType) {
		boolean isProducerWillExist = true;

		if (MyBotModule.Broodwar.self().completedUnitCount(producerType) == 0
				&& MyBotModule.Broodwar.self().incompleteUnitCount(producerType) == 0) {
			// producer �� �ǹ� �� ��� : �ǹ��� �Ǽ� ������ �߰� �ľ�
			// ������� unitType = Addon �ǹ�. Lair. Hive. Greater Spire. Sunken
			// Colony. Spore Colony. �����佺 �� �׶��� �������� / ��������.
			if (producerType.isBuilding()) {
				if (ConstructionManager.Instance().getConstructionQueueItemCount(producerType, null) == 0) {
					isProducerWillExist = false;
				}
			}
			// producer �� �ǹ��� �ƴ� ��� : producer �� ������ �������� �߰� �ľ�
			// producerType : �ϲ�. Larva. Hydralisk, Mutalisk
			else {
					isProducerWillExist = false;
			}
		}

		return isProducerWillExist;
	}

	public void checkBuildOrderQueueDeadlockAndInsert() {
		
		BuildOrderQueue buildQueue = BuildManager.Instance().getBuildQueue();
		if (!buildQueue.isEmpty()) {
			BuildOrderItem currentItem = buildQueue.getHighestPriorityItem();

			// �ǹ��̳� ������ ���
			if (currentItem.metaType.isUnit()) {
				UnitType unitType = currentItem.metaType.getUnitType();//TODO ������ �ʿ��� �ǹ��̸鼭 ���� refinery �� ������ ���´�
				final Map<UnitType, Integer> requiredUnits = unitType.requiredUnits();

				Iterator<UnitType> it = requiredUnits.keySet().iterator();
				// ���� �ǹ�/������ �ִµ�
				if (requiredUnits.size() > 0) {
					while (it.hasNext()) {
						UnitType requiredUnitType = it.next(); // C++ : u.first;
						if (requiredUnitType != UnitType.None) {
							if (MyBotModule.Broodwar.self().completedUnitCount(requiredUnitType) == 0
									&& MyBotModule.Broodwar.self().incompleteUnitCount(requiredUnitType) == 0) {
								// ���� �ǹ��� �Ǽ� ���������� ������ �����
								if (requiredUnitType.isBuilding()) {
									if (BuildManager.Instance().buildQueue.getItemCount(requiredUnitType) == 0
											&& ConstructionManager.Instance().getConstructionQueueItemCount(requiredUnitType, null) == 0) {
//										int needcnt=0;
//										int requirecnt=0;
//								
//										for (Unit unit : MyBotModule.Broodwar.self().getUnits()){
//											if(unit.getType() == unitType && unit.isCompleted()){
//												needcnt++;
//											}
//											if(unit.getType() == requiredUnitType){
//												requirecnt++;
//											}
//										}
//										if(needcnt > requirecnt){		
											System.out.println("Inserting blocked unit: " + requiredUnitType);
											BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(requiredUnitType), true);
										//}
									}
								}
							}
						}
					}
				}
			}
		}
		
	}
	public void checkBuildOrderQueueDeadlockAndAndFixIt() {
		// ��������� ������ �� �ִ� ���������� ���� �Ǵ��Ѵ�
		// this will be true if any unit is on the first frame if it's training
		// time remaining
		// this can cause issues for the build order search system so don't plan
		// a search on these frames
		boolean canPlanBuildOrderNow = true;
		for (final Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit.getRemainingTrainTime() == 0) {
				continue;
			}

			UnitCommand unitCommand = unit.getLastCommand();
			if (unitCommand != null) {

				UnitCommandType unitCommandType = unitCommand.getUnitCommandType();
				if (unitCommandType != UnitCommandType.None) {
					if (unitCommand.getUnit() != null) {
						UnitType trainType = unitCommand.getUnit().getType();
						if (unit.getRemainingTrainTime() == trainType.buildTime()) {
							canPlanBuildOrderNow = false;
							break;
						}
					}
				}
			}

		}
		if (!canPlanBuildOrderNow) {
			return;
		}

		// BuildQueue �� HighestPriority �� �ִ� BuildQueueItem �� skip �Ұ����� ���ε�,
		// ���������� ������ �� ���ų�, ��)���� �����ε� ��� �Ұ����� ���, dead lock �� �߻��Ѵ�
		// ���� �ǹ��� BuildQueue�� �߰��س�����, �ش� BuildQueueItem �� �������� ���������� �Ǵ��ؾ� �Ѵ�
		BuildOrderQueue buildQueue = BuildManager.Instance().getBuildQueue();
		if (!buildQueue.isEmpty()) {
			BuildOrderItem currentItem = buildQueue.getHighestPriorityItem();

			// if (buildQueue.canSkipCurrentItem() == false)
			if (currentItem.blocking == true) {
				boolean isDeadlockCase = false;

				// producerType�� ���� �˾Ƴ���
				UnitType producerType = currentItem.metaType.whatBuilds();

				// �ǹ��̳� ������ ���
				if (currentItem.metaType.isUnit()) {
					UnitType unitType = currentItem.metaType.getUnitType();
					TechType requiredTechType = unitType.requiredTech();
					final Map<UnitType, Integer> requiredUnits = unitType.requiredUnits();

					/*
					 * std::cout + "To make " + unitType.getName() +
					 * ", producerType " + producerType.getName() +
					 * " completedUnitCount " +
					 * MyBotModule.Broodwar.self().completedUnitCount(
					 * producerType) + " incompleteUnitCount " +
					 * MyBotModule.Broodwar.self().incompleteUnitCount(
					 * producerType) + std::endl;
					 */

					// �ǹ��� �����ϴ� �����̳�, ������ �����ϴ� �ǹ��� �������� �ʰ�, �Ǽ� ���������� ������ dead
					// lock
					if (isProducerWillExist(producerType) == false) {
						isDeadlockCase = true;
					}

					// Refinery �ǹ��� ���, Refinery �� �Ǽ����� ���� Geyser�� �ִ� ��쿡�� ����
					if (!isDeadlockCase && unitType == InformationManager.Instance().getRefineryBuildingType()) {
						boolean hasAvailableGeyser = true;

						// Refinery�� ������ �� �ִ� ��Ҹ� ã�ƺ���
						TilePosition testLocation = getDesiredPosition(unitType, currentItem.seedLocation,
								currentItem.seedLocationStrategy);

						// Refinery �� �������� ��Ҹ� ã�� �� ������ dead lock
						if (testLocation == TilePosition.None || testLocation == TilePosition.Invalid
								|| testLocation.isValid() == false) {
							//System.out.println("Build Order Dead lock case . Cann't find place to construct " + unitType); // C++ : unitType.getName()
							hasAvailableGeyser = false;
						} else {
							// Refinery �� �������� ��ҿ� Refinery �� �̹� �Ǽ��Ǿ� �ִٸ� dead lock
							for (Unit u : MyBotModule.Broodwar.getUnitsOnTile(testLocation)) {
								if (u.getType().isRefinery() && u.exists()) {
									hasAvailableGeyser = false;
									break;
								}
							}
						}

						if (hasAvailableGeyser == false) {
							isDeadlockCase = true;
						}
					}

					// ���� ��� ����ġ�� �Ǿ����� �ʰ�, ����ġ �������� ������ dead lock
					if (!isDeadlockCase && requiredTechType != TechType.None) {
						if (MyBotModule.Broodwar.self().hasResearched(requiredTechType) == false) {
							if (MyBotModule.Broodwar.self().isResearching(requiredTechType) == false) {
								isDeadlockCase = true;
							}
						}
					}
					
					int getAddonPossibeCnt = 0;
					
					if (currentItem.metaType.getUnitType().isAddon()){ 
						UnitType ProducerType = currentItem.metaType.getUnitType().whatBuilds().first;
						
						for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
							if(ProducerType == unit.getType() && unit.isCompleted() ){
//								
								if (StrategyManager.Instance().isInitialBuildOrderFinished() == true) {
									if(unit.canBuildAddon() == false){
										continue;
									}
								}
								if(currentItem.metaType.getUnitType()  != UnitType.Terran_Comsat_Station){
									if (isBuildableTile(unit.getTilePosition().getX()+4, unit.getTilePosition().getY()+1) == false
											||isBuildableTile(unit.getTilePosition().getX()+5, unit.getTilePosition().getY()+1) == false
											||isBuildableTile(unit.getTilePosition().getX()+4, unit.getTilePosition().getY()+2) == false
											||isBuildableTile(unit.getTilePosition().getX()+5, unit.getTilePosition().getY()+2) == false)
									{
										//System.out.println("something is blocking addon place, so no cnt");
										continue;
									}
								}
								getAddonPossibeCnt++;
							}
						}
						if(getAddonPossibeCnt == 0){
//							System.out.println("deadlock because no place to addon");
							isDeadlockCase = true;
						}
					}
					
					Iterator<UnitType> it = requiredUnits.keySet().iterator();
					// ���� �ǹ�/������ �ִµ�
					if (!isDeadlockCase && requiredUnits.size() > 0) {
						// for (Unit u : it)
						while (it.hasNext()) {
							UnitType requiredUnitType = it.next(); // C++ : u.first;
							if (requiredUnitType != UnitType.None) {
								/*
								 * std::cout + "pre requiredUnitType " +
								 * requiredUnitType.getName() +
								 * " completedUnitCount " +
								 * MyBotModule.Broodwar.self().
								 * completedUnitCount(requiredUnitType) +
								 * " incompleteUnitCount " +
								 * MyBotModule.Broodwar.self().
								 * incompleteUnitCount(requiredUnitType) +
								 * std::endl;
								 */

								// ���� �ǹ� / ������ �������� �ʰ�, ���� �������� �ʰ�
								if (MyBotModule.Broodwar.self().completedUnitCount(requiredUnitType) == 0
										&& MyBotModule.Broodwar.self().incompleteUnitCount(requiredUnitType) == 0) {
									// ���� �ǹ��� �Ǽ� ���������� ������ dead lock
									if (requiredUnitType.isBuilding()) {
										if (ConstructionManager.Instance().getConstructionQueueItemCount(requiredUnitType, null) == 0) {
											isDeadlockCase = true;
										}
									}
								}
							}
						}
					}

					// �ǹ��� �ƴ� ����/���� ������ ���, ���ö��̰� 400 �� á���� dead lock
					if (!isDeadlockCase && !unitType.isBuilding() && MyBotModule.Broodwar.self().supplyTotal() == 400
							&& MyBotModule.Broodwar.self().supplyUsed() + unitType.supplyRequired() > 400) {
						isDeadlockCase = true;
					}

					// �ǹ��� �ƴ� ����/���� �����ε�, ���ö��̰� �����ϸ� dead lock ��Ȳ�� �Ǳ� ������, 
					// �� ���� ���带 ����ϱ⺸�ٴ�, StrategyManager ��� ���ö��� ���带 �߰������ν� Ǯ���� �Ѵ�
//					if (!isDeadlockCase && !unitType.isBuilding()
//							&& MyBotModule.Broodwar.self().supplyUsed() + unitType.supplyRequired() > MyBotModule.Broodwar.self().supplyTotal()) 
//					{
//						//isDeadlockCase = true;
//					}

					// Pylon �� �ش� ���� ������ ���� �������� �ϴµ�, Pylon �� �ش� ���� ������ ����, �����Ǿ������� ������ dead lock
//					if (!isDeadlockCase && unitType.isBuilding() && unitType.requiresPsi()
//							&& currentItem.seedLocationStrategy == BuildOrderItem.SeedPositionStrategy.SeedPositionSpecified) {
//
//						boolean hasFoundPylon = false;
//						List<Unit> ourUnits = MyBotModule.Broodwar
//								.getUnitsInRadius(currentItem.seedLocation.toPosition(), 4 * Config.TILE_SIZE);
//
//						for (Unit u : ourUnits) {
//							if (u.getPlayer() == MyBotModule.Broodwar.self() && u.getType() == UnitType.Protoss_Pylon) {
//								hasFoundPylon = true;
//							}
//						}
//
//						if (hasFoundPylon == false) {
//							isDeadlockCase = true;
//						}
//					}

					// Creep �� �ش� ���� ������ Hatchery�� Creep Colony ���� ���� ���� �������� �ϴµ�, �ش� ���� ������ �������� �ʰ� ������ dead lock
//					if (!isDeadlockCase && unitType.isBuilding() && unitType.requiresCreep()
//							&& currentItem.seedLocationStrategy == BuildOrderItem.SeedPositionStrategy.SeedPositionSpecified) {
//						boolean hasFoundCreepGenerator = false;
//						List<Unit> ourUnits = MyBotModule.Broodwar
//								.getUnitsInRadius(currentItem.seedLocation.toPosition(), 4 * Config.TILE_SIZE);
//
//						for (Unit u : ourUnits) {
//							if (u.getPlayer() == MyBotModule.Broodwar.self() && (u.getType() == UnitType.Zerg_Hatchery
//									|| u.getType() == UnitType.Zerg_Lair || u.getType() == UnitType.Zerg_Hive
//									|| u.getType() == UnitType.Zerg_Creep_Colony
//									|| u.getType() == UnitType.Zerg_Sunken_Colony
//									|| u.getType() == UnitType.Zerg_Spore_Colony)) {
//								hasFoundCreepGenerator = true;
//							}
//						}
//
//						if (hasFoundCreepGenerator == false) {
//							isDeadlockCase = true;
//						}
//					}

				}
				// ��ũ�� ���, �ش� ����ġ�� �̹� �߰ų�, �̹� �ϰ��ְų�, ����ġ�� �ϴ� �ǹ� �� ����ǹ��� ���������ʰ�
				// �Ǽ����������� ������ dead lock
				else if (currentItem.metaType.isTech()) {
					TechType techType = currentItem.metaType.getTechType();
					UnitType requiredUnitType = techType.requiredUnit();

					/*
					 * System.out.println("To research " + techType.toString() +
					 * ", hasResearched " +
					 * MyBotModule.Broodwar.self().hasResearched(techType) +
					 * ", isResearching " +
					 * MyBotModule.Broodwar.self().isResearching(techType) +
					 * ", producerType " + producerType.toString() +
					 * " completedUnitCount " +
					 * MyBotModule.Broodwar.self().completedUnitCount(
					 * producerType) + " incompleteUnitCount " +
					 * MyBotModule.Broodwar.self().incompleteUnitCount(
					 * producerType));
					 */

					if (MyBotModule.Broodwar.self().hasResearched(techType)
							|| MyBotModule.Broodwar.self().isResearching(techType)) {
						isDeadlockCase = true;
					} 
					else if (MyBotModule.Broodwar.self().completedUnitCount(producerType) == 0
							&& MyBotModule.Broodwar.self().incompleteUnitCount(producerType) == 0) 
					{
						if (ConstructionManager.Instance().getConstructionQueueItemCount(producerType, null) == 0) 
						{

							// ��ũ ����ġ�� producerType�� Addon �ǹ��� ���, Addon �ǹ� �Ǽ���
							// ��� ���������� ���۵Ǳ� �������� getUnits, completedUnitCount,
							// incompleteUnitCount ���� Ȯ���� �� ����
							// producerType�� producerType �ǹ��� ���� Addon �ǹ� �Ǽ���
							// ����� ���������� Ȯ���ؾ� �Ѵ�
							if (producerType.isAddon()) {

								boolean isAddonConstructing = false;

								UnitType producerTypeOfProducerType = producerType.whatBuilds().first;

								if (producerTypeOfProducerType != UnitType.None) {

									for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
										if (unit == null)
											continue;
										if (unit.getType() != producerTypeOfProducerType) {
											continue;
										}

										// ��ǹ��� �ϼ��Ǿ��ְ�, ��ǹ��� �ش� Addon �ǹ��� �Ǽ�������
										// Ȯ���Ѵ�
										if (unit.isCompleted() && unit.isConstructing()
												&& unit.getBuildType() == producerType) {
											isAddonConstructing = true;
											break;
										}
									}
								}

								if (isAddonConstructing == false) {
									isDeadlockCase = true;
								}
							} else {
								isDeadlockCase = true;
							}
						}
					} 
					else if (requiredUnitType != UnitType.None) {
						/*
						 * std::cout + "To research " + techType.getName() +
						 * ", requiredUnitType " + requiredUnitType.getName() +
						 * " completedUnitCount " +
						 * MyBotModule.Broodwar.self().completedUnitCount(
						 * requiredUnitType) + " incompleteUnitCount " +
						 * MyBotModule.Broodwar.self().incompleteUnitCount(
						 * requiredUnitType) + std::endl;
						 */

						if (MyBotModule.Broodwar.self().completedUnitCount(requiredUnitType) == 0
								&& MyBotModule.Broodwar.self().incompleteUnitCount(requiredUnitType) == 0) {
							if (ConstructionManager.Instance().getConstructionQueueItemCount(requiredUnitType,
									null) == 0) {
								isDeadlockCase = true;
							}
						}
					}
				}
				// ���׷��̵��� ���, �ش� ���׷��̵带 �̹� �߰ų�, �̹� �ϰ��ְų�, ���׷��̵带 �ϴ� �ǹ� �� ����ǹ���
				// ���������� �ʰ� �Ǽ����������� ������ dead lock
				else if (currentItem.metaType.isUpgrade()) {
					UpgradeType upgradeType = currentItem.metaType.getUpgradeType();
					int maxLevel = MyBotModule.Broodwar.self().getMaxUpgradeLevel(upgradeType);
					int currentLevel = MyBotModule.Broodwar.self().getUpgradeLevel(upgradeType);
					UnitType requiredUnitType = upgradeType.whatsRequired();

					/*
					 * std::cout + "To upgrade " + upgradeType.getName() +
					 * ", maxLevel " + maxLevel + ", currentLevel " +
					 * currentLevel + ", isUpgrading " +
					 * MyBotModule.Broodwar.self().isUpgrading(upgradeType) +
					 * ", producerType " + producerType.getName() +
					 * " completedUnitCount " +
					 * MyBotModule.Broodwar.self().completedUnitCount(
					 * producerType) + " incompleteUnitCount " +
					 * MyBotModule.Broodwar.self().incompleteUnitCount(
					 * producerType) + ", requiredUnitType " +
					 * requiredUnitType.getName() + std::endl;
					 */

					if (currentLevel >= maxLevel || MyBotModule.Broodwar.self().isUpgrading(upgradeType)) {
						isDeadlockCase = true;
					} else if (MyBotModule.Broodwar.self().completedUnitCount(producerType) == 0
							&& MyBotModule.Broodwar.self().incompleteUnitCount(producerType) == 0) {
						if (ConstructionManager.Instance().getConstructionQueueItemCount(producerType, null) == 0) {

							// ���׷��̵��� producerType�� Addon �ǹ��� ���, Addon �ǹ� �Ǽ���
							// ���۵Ǳ� �������� getUnits, completedUnitCount,
							// incompleteUnitCount ���� Ȯ���� �� ����
							// producerType�� producerType �ǹ��� ���� Addon �ǹ� �Ǽ���
							// ���۵Ǿ��������� Ȯ���ؾ� �Ѵ�
							if (producerType.isAddon()) {

								boolean isAddonConstructing = false;

								UnitType producerTypeOfProducerType = producerType.whatBuilds().first;

								if (producerTypeOfProducerType != UnitType.None) {

									for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
										if (unit == null)
											continue;
										if (unit.getType() != producerTypeOfProducerType) {
											continue;
										}
										// ��ǹ��� �ϼ��Ǿ��ְ�, ��ǹ��� �ش� Addon �ǹ��� �Ǽ�������
										// Ȯ���Ѵ�
										if (unit.isCompleted() && unit.isConstructing()
												&& unit.getBuildType() == producerType) {
											isAddonConstructing = true;
											break;
										}
									}
								}

								if (isAddonConstructing == false) {
									isDeadlockCase = true;
								}
							} else {
								isDeadlockCase = true;
							}
						}
					} else if (requiredUnitType != UnitType.None) {
						if (MyBotModule.Broodwar.self().completedUnitCount(requiredUnitType) == 0
								&& MyBotModule.Broodwar.self().incompleteUnitCount(requiredUnitType) == 0) {
							if (ConstructionManager.Instance().getConstructionQueueItemCount(requiredUnitType,
									null) == 0) {
								isDeadlockCase = true;
							}
						}
					}
				}

				if (!isDeadlockCase) {
					// producerID �� �����ߴµ�, �ش� ID �� ���� ������ �������� ������ dead lock
					if (currentItem.producerID != -1 ) {
						boolean isProducerAlive = false;
						for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
							if (unit != null && unit.getID() == currentItem.producerID && unit.exists() && unit.getHitPoints() > 0) {
								isProducerAlive = true;
								break;
							}
						}
						if (isProducerAlive == false) {
							isDeadlockCase = true;
						}
					}
				}

				if (isDeadlockCase) {
//					System.out.println(	"Build Order Dead lock case . remove BuildOrderItem " + currentItem.metaType.getName());

					buildQueue.removeCurrentItem();
				}

			}
		}
	}
	
	public final boolean isBuildableTile(int x, int y)
	{
		TilePosition tp = new TilePosition(x, y);
		if (!tp.isValid())
		{
			//System.out.println("Invalid");
			return false;
		}

		// �� ������ �Ӹ� �ƴ϶� ���� �����͸� ��� ����ؼ� isBuildable üũ
		//if (BWAPI::Broodwar->isBuildable(x, y) == false)
		if (MyBotModule.Broodwar.isBuildable(x, y, true) == false)
		{
			//System.out.println("not buildable at: " + x + ", " + y);
			return false;
		}

		// constructionWorker �̿��� �ٸ� ������ ������ false�� �����Ѵ�
		if(MyBotModule.Broodwar.getUnitsOnTile(x, y).size() > 0){
//			List<Unit> temp= MyBotModule.Broodwar.getUnitsOnTile(x, y);
//			for(Unit u : temp){
//				System.out.println("unit: "+ u.getType() + " at " + u.getPosition().toString());
//			}
			//System.out.println("there is unit");
			return false;
		}
		
		return true;
	}
};