
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.Region;

/// �ǹ� �Ǽ� Construction ��� ����� ����Ʈ�� �����ϰ�, �ǹ� �Ǽ� ����� �� ����ǵ��� ��Ʈ���ϴ� class
public class ConstructionManager {

	/// �Ǽ� �ʿ� �ڿ��� �̸� �����س���, <br>
	/// �Ǽ� ��� ��Ұ� �̰�ô ����� ��� �Ǽ� �ϲ��� �̵����� �ᱹ �Ǽ��� ���۵ǰ� �ϰ�, <br>
	/// �Ǽ� �ϲ��� ���߿� �״� ��� �ٸ� �Ǽ� �ϲ��� �����Ͽ� �Ǽ��� �����ϰ� �ϱ� ����<br>
	/// Construction Task ���� ����� constructionQueue �� �����մϴ�
	private Vector<ConstructionTask> constructionQueue = new Vector<ConstructionTask>();
	
	CommandUtil commandUtil = new CommandUtil();
	
	///< minerals reserved for planned buildings
	private int reservedMinerals = 0;
	
	///< gas reserved for planned buildings
	private int reservedGas = 0;
	
	private static ConstructionManager instance = new ConstructionManager();
	
	/// static singleton ��ü�� �����մϴ�
	public static ConstructionManager Instance() {
		return instance;
	}
	
	/// constructionQueue �� ConstructionTask �� �߰��մϴ�
	public void addConstructionTask(UnitType type, TilePosition desiredPosition, boolean forcedType)
	{
		if (type == UnitType.None || type == UnitType.Unknown) {
			return;
		}
		if (desiredPosition == TilePosition.None || desiredPosition == TilePosition.Invalid || desiredPosition == TilePosition.Unknown) {
			return;
		}

		ConstructionTask b = new ConstructionTask(type, desiredPosition, forcedType);
		b.setStatus(ConstructionTask.ConstructionStatus.Unassigned.ordinal());

		// reserve resources
		reservedMinerals += type.mineralPrice();
		reservedGas += type.gasPrice();

		constructionQueue.add(b); // C++ : constructionQueue.push_back(b);
	}

	/// constructionQueue ���� ConstructionTask �� ����մϴ�
	public void cancelConstructionTask(UnitType type, TilePosition desiredPosition)
	{
		reservedMinerals -= type.mineralPrice();
		reservedGas -= type.gasPrice();

		ConstructionTask b = new ConstructionTask(type, desiredPosition, false);
	    if (constructionQueue.contains(b))
	    {
	    	//System.out.println("Cancel Construction " + b.getType() + " at " + b.getDesiredPosition().getX() + "," + b.getDesiredPosition().getY());

			if (b.getConstructionWorker() != null) {
				WorkerManager.Instance().setIdleWorker(b.getConstructionWorker());
			}
			if (b.getFinalPosition() != null) {
				
				int width = b.getType().tileWidth();
				int height = b.getType().tileHeight();
				
				if (b.getType() == UnitType.Terran_Command_Center ||
						b.getType() == UnitType.Terran_Factory ||
						b.getType() == UnitType.Terran_Starport ||
						b.getType() == UnitType.Terran_Science_Facility)
					{
						width += 2;
				}
				ConstructionPlaceFinder.Instance().freeTiles(b.getFinalPosition(), width, height);
			}
	        constructionQueue.remove(b);
	    }
	}

	/// constructionQueue ���� �Ǽ� ���°� UnderConstruction �� ConstructionTask �������� �����մϴ�<br>
	/// �Ǽ��� �����߾��� ConstructionTask �̱� ������ _reservedMinerals, _reservedGas �� �ǵ帮�� �ʴ´�
	public void removeCompletedConstructionTasks(final Vector<ConstructionTask> toRemove)
	{
		for (ConstructionTask b : toRemove)
		{		
			if (constructionQueue.contains(b))
			{
			    constructionQueue.remove(b);
			}
		}
	}
	
	public void update()
	{
		// 1�ʿ� 4���� �����մϴ�
		if (MyBotModule.Broodwar.getFrameCount() % 6 != 0){
			return;
		}

		// constructionQueue �� ����ִ� ConstructionTask ���� 
		// Unassigned . Assigned (buildCommandGiven=false) . Assigned (buildCommandGiven=true) . UnderConstruction . (Finished) �� ���� ��ȭ�ȴ�

		/*
		System.out.println( "\nCurrent ConstructionTasks in constructionQueue");
	    for (ConstructionTask b : constructionQueue)
	    {
			System.out.print( b.getType() + " status " + b.getStatus() + ", desiredPosition " + b.getDesiredPosition().getX() + "," + b.getDesiredPosition().getY() );

			if (b.getConstructionWorker() != null) {
				System.out.println(" constructionWorker " + b.getConstructionWorker().getID() + " isConstructing " + b.getConstructionWorker().isConstructing());
			}
			else {
				System.out.println(" constructionWorker is null");
			}
			
			if (b.getFinalPosition() != null) {
				System.out.println(" finalPosition " + b.getFinalPosition().getX() + "," + b.getFinalPosition().getY());
			}
			else {
				System.out.println(" finalPosition null ");					
			}
	    }
	    */
	    
	    validateWorkersAndBuildings();  
	    //haltConstructionBuildings();
	    assignWorkersToUnassignedBuildings();       
	    checkForStartedConstruction();              
	    constructAssignedBuildings();               
	    checkForDeadTerranBuilders();               
	    checkForCompletedBuildings();           
		checkForDeadlockConstruction();			
		checkConstructionBuildings();
	}

	public void haltConstructionBuildings()
	{
		for (ConstructionTask b : constructionQueue)
		{
			// if a terran building whose worker died mid construction, 
			// send the right click command to the buildingUnit to resume construction			
			if (b.getStatus() == ConstructionTask.ConstructionStatus.UnderConstruction.ordinal()) {

				if (b.getBuildingUnit().isCompleted()) continue;
//				//�Ǽ����� �ϲ� ������ 20���Ϸ� idle �Ǿ����� ���⼱ ���� �ʾ� �ǹ� ��Ǽ� ���ϴ� ���� �߻�
//				//���� �Ҵ�� ��Ŀ null�� ����
//				if(WorkerManager.Instance().getWorkerData().getWorkerId(b.getConstructionWorker())){
//					b.setConstructionWorker(null);
//				}
				Unit worker = b.getConstructionWorker() ;
				if(worker!= null){
					if(worker.isUnderAttack() && worker.getHitPoints() < 20){
						worker.haltConstruction();
						WorkerManager.Instance().setIdleWorker(b.getConstructionWorker());
						b.setConstructionWorker(null);
					}
				}
			}
		}
	}

	/// �Ǽ� ���� ���� (������ �޾Ƽ�) �Ǽ��Ϸ��� �ǹ��� �ı��� ���, constructionQueue ���� �����մϴ�
	public void validateWorkersAndBuildings()
	{
		Vector<ConstructionTask> toRemove = new Vector<ConstructionTask>();

		for (ConstructionTask b : constructionQueue)
	    {
			if (b.getStatus() == ConstructionTask.ConstructionStatus.UnderConstruction.ordinal())
			{
				// �Ǽ� ���� ���� (������ �޾Ƽ�) �Ǽ��Ϸ��� �ǹ��� �ı��� ���, constructionQueue ���� �����մϴ�
				// �׷��� ������ (�Ƹ��� ������ ���������ִ�) ���� ��ġ�� �ٽ� �ǹ��� ������ �� ���̱� ����.
				if (b.getBuildingUnit() == null || !b.getBuildingUnit().getType().isBuilding() || b.getBuildingUnit().getHitPoints() <= 0 || !b.getBuildingUnit().exists())
				{
					//System.out.println("Construction Failed case . remove ConstructionTask " + b.getType());
					toRemove.add(b);

					if (b.getConstructionWorker() != null) {
						WorkerManager.Instance().setIdleWorker(b.getConstructionWorker());
					}
				}
			}
	    }

		removeCompletedConstructionTasks(toRemove);
	}

	/// �Ǽ� ������°� Unassigned �� ConstructionTask �� ���� �Ǽ� ��ġ �� �Ǽ� �ϲ��� �����ϰ�, �Ǽ� ������¸� Assigned �� �����մϴ�
	public void assignWorkersToUnassignedBuildings()
	{
		// for each building that doesn't have a builder, assign one
	    for (ConstructionTask b : constructionQueue)
	    {
	        if (b.getStatus() != ConstructionTask.ConstructionStatus.Unassigned.ordinal())
	        {
	            continue;
	        }

			//System.out.println( "find build place near desiredPosition " + b.desiredPosition.x + "," + b.desiredPosition.y );

			// �Ǽ� �ϲ��� Unassigned �� ���¿��� getBuildLocationNear �� �Ǽ��� ��ġ�� �ٽ� ���մϴ�. . Assigned 
			TilePosition testLocation = ConstructionPlaceFinder.Instance().getBuildLocationNear(b.getType(), b.getDesiredPosition());
			if(b.isForcedType() == true)
			{
				System.out.println("desiredPosition is forced mode "+b.getType());
				testLocation = b.getDesiredPosition();
			}
			
//			System.out.println( "ConstructionPlaceFinder Selected Location : " + testLocation.getX() + "," + testLocation.getY() );

			if (testLocation == TilePosition.None || testLocation == TilePosition.Invalid || testLocation.isValid() == false) {
				// ���� �ǹ� ���� ��Ҹ� ���� ã�� �� ���� �� ����, 
				// desiredPosition ������ �ٸ� �ǹ�/���ֵ��� �ְ� �Ǿ��ų�, Pylon �� �ı��Ǿ��ų�, Creep �� ������ ����̰�,
				// ��κ� �ٸ� �ǹ�/���ֵ��� �ְԵ� ����̹Ƿ� ���� frame ���� �ٽ� ���� ���� Ž���մϴ�
				continue;
			}

			//System.out.println("assignWorkersToUnassignedBuildings - chooseConstuctionWorkerClosest for " + b.getType() + " to worker near " + testLocation.getX() + "," + testLocation.getY());
			
	        // grab a worker unit from WorkerManager which is closest to this final position
			// �Ǽ��� ���ϴ� worker �� ��� construction worker �� ������ �� �ִ�. ������ �����Ǿ����� worker �� �ٽ� �������ϵ��� �մϴ�
			Unit workerToAssign = WorkerManager.Instance().chooseConstuctionWorkerClosestTo(b.getType(), testLocation, true, b.getLastConstructionWorkerID());
			
	        if (workerToAssign != null)
	        {
				//System.out.println("set ConstuctionWorker " + workerToAssign.getID());

				b.setConstructionWorker(workerToAssign);
				b.setFinalPosition(testLocation);
				b.setStatus(ConstructionTask.ConstructionStatus.Assigned.ordinal());

				int width = b.getType().tileWidth();
				int height = b.getType().tileHeight();
				
				if (b.getType() == UnitType.Terran_Command_Center ||
						b.getType() == UnitType.Terran_Factory ||
						b.getType() == UnitType.Terran_Starport ||
						b.getType() == UnitType.Terran_Science_Facility)
					{
						width += 2;
				}
				
				// reserve this building's space
				ConstructionPlaceFinder.Instance().reserveTiles(testLocation, width, height);
				b.setLastConstructionWorkerID(b.getConstructionWorker().getID());
	        }
	    }
	}

	/// �Ǽ� ������°� Assigned �� ConstructionTask �� ����,<br>
	/// �Ǽ��� ���۵Ǳ� ���� �ϲ��� �׾����� �Ǽ� ������¸� Unassigned �� �����ϰ�<br>
	/// �Ǽ� ��Ұ� unexplored �̸� �Ǽ� �ϲ��� �ش� ��ҷ� �̵���Ű��<br>
	/// �Ǽ� �ϲۿ��� build ����� �ȳ������� �Ǽ� �ϲۿ��� build ����� ������<br>
	/// �Ǽ� �ϲ��� �Ǽ��� �������� �ʴ� ���°� �Ǿ����� �Ǽ� �ϲ��� �����ϰ� �Ǽ� ������¸� Unassigned �� �����մϴ�
	public void constructAssignedBuildings()
	{
	    for (ConstructionTask b : constructionQueue)
	    {
	        if (b.getStatus() != ConstructionTask.ConstructionStatus.Assigned.ordinal())
	        {
	            continue;
	        }

			/*
			if (b.getConstructionWorker() == null) {
				System.out.println( b.getType() + " constructionWorker null" );
			}
			else {
				System.out.println( b.getType()
					+ " constructionWorker " + b.getConstructionWorker().getID()
					+ " exists " + b.getConstructionWorker().exists()
					+ " isIdle " + b.getConstructionWorker().isIdle()
					+ " isConstructing " + b.getConstructionWorker().isConstructing()
					+ " isMorphing " + b.getConstructionWorker().isMorphing() );
			}
			*/

			// �ϲۿ��� build ����� ������ ������ isConstructing = false �̴�
			// ���� Ž������ ���� ���� ���ؼ��� build ����� ���� �� ����
			// �ϲۿ��� build ����� ������, isConstructing = true ���°� �Ǿ� �̵��� �ϴٰ�
			// build �� ������ �� ���� ��Ȳ�̶�� �ǴܵǸ� isConstructing = false ���°� �ȴ�
			// build �� ������ �� ������, �����佺 / �׶� ������ ��� �ϲ��� build �� �����ϰ�
			// ���� ���� �ǹ� �� Extractor �ǹ��� �ƴ� �ٸ� �ǹ��� ��� �ϲ��� exists = true, isConstructing = true, isMorphing = true �� �ǰ�, �ϲ� ID �� �ǹ� ID�� �ȴ�
			// ���� ���� �ǹ� �� Extractor �ǹ��� ��� �ϲ��� exists = false, isConstructing = true, isMorphing = true �� �� ��, �ϲ� ID �� �ǹ� ID�� �ȴ�. 
			//                  Extractor �ǹ� ���带 ���߿� ����ϸ�, ���ο� ID �� ���� �ϲ��� �ȴ�

			// �ϲ��� Assigned �� ��, UnderConstruction ���·� �Ǳ� ��, �� �ϲ��� �̵� �߿� �ϲ��� ���� ���, �ǹ��� Unassigned ���·� �ǵ��� �ϲ��� �ٽ� Assign �ϵ��� �մϴ�		
			if (b.getConstructionWorker() == null || b.getConstructionWorker().exists() == false || b.getConstructionWorker().getHitPoints() <= 0)
			{
				// ���� ���� �ǹ� �� Extractor �ǹ��� ��� �ϲ��� exists = false ������ isConstructing = true �� �ǹǷ�, �ϲ��� ���� ��찡 �ƴϴ�
				if (b.getType() == UnitType.Zerg_Extractor && b.getConstructionWorker() != null && b.getConstructionWorker().isConstructing() == true) {
					continue;
				}

				//System.out.println( "unassign " + b.type.getName() + " worker " + b.constructionWorker.getID() + ", because it is not exists" );

				// Unassigned �� ���·� �ǵ�����
				WorkerManager.Instance().setIdleWorker(b.getConstructionWorker());

				int width = b.getType().tileWidth();
				int height = b.getType().tileHeight();
				
				if (b.getType() == UnitType.Terran_Command_Center ||
						b.getType() == UnitType.Terran_Factory ||
						b.getType() == UnitType.Terran_Starport ||
						b.getType() == UnitType.Terran_Science_Facility)
					{
						width += 2;
				}
				ConstructionPlaceFinder.Instance().freeTiles(b.getFinalPosition(), width, height);
				// free the previous location in reserved
				b.setConstructionWorker(null);
				b.setBuildCommandGiven(false);
				b.setFinalPosition(TilePosition.None);
				b.setStatus(ConstructionTask.ConstructionStatus.Unassigned.ordinal());
			}
			// if that worker is not currently constructing
			// �ϲ��� build command �� ������ isConstructing = true �� �ǰ� �Ǽ��� �ϱ����� �̵��ϴµ�,
			// isConstructing = false �� �Ǿ��ٴ� ����, build command �� ������ �� ���� ���ӿ��� �ش� �ӹ��� ��ҵǾ��ٴ� ���̴�
			else if (b.getConstructionWorker().isConstructing() == false)        
	        {
	            // if we haven't explored the build position, first we mush go there
				// �ѹ��� �Ȱ��� ������ build Ŀ�ǵ� ��ü�� ������ �� �����Ƿ�, �ϴ� �װ����� �̵��ϰ� �մϴ�
	            if (!isBuildingPositionExplored(b))
	            {
	            	CommandUtil.move(b.getConstructionWorker(),b.getFinalPosition().toPosition());
	            }
				else if (b.isBuildCommandGiven() == false)
	            {
					//System.out.println(b.getType() + " build commanded to " + b.getConstructionWorker().getID() + ", buildCommandGiven true " );
					
					// build command 
					b.getConstructionWorker().build(b.getType(), b.getFinalPosition());

					WorkerManager.Instance().setConstructionWorker(b.getConstructionWorker(), b.getType());

					// set the buildCommandGiven flag to true
					b.setBuildCommandGiven(true);
					b.setLastBuildCommandGivenFrame(MyBotModule.Broodwar.getFrameCount());
					b.setLastConstructionWorkerID(b.getConstructionWorker().getID());
	            }
				// if this is not the first time we've sent this guy to build this
				// �ϲۿ��� build command �� �־�����, ���߿� �ڿ��� �̴��ϰ� �Ǿ��ų�, �ش� ��ҿ� �ٸ� ���ֵ��� �־ �Ǽ��� ���� ���ϰ� �ǰų�, Pylon �̳� Creep �� ������ ��� ���� �߻��� �� �ִ�
				// �� ���, �ش� �ϲ��� build command �� �����ϰ�, �ǹ� ���¸� Unassigned �� �ٲ㼭, �ٽ� �ǹ� ��ġ�� ���ϰ�, �ٸ� �ϲ��� �����ϴ� ������ ó���մϴ�
				else
	            {
					if (MyBotModule.Broodwar.getFrameCount() - b.getLastBuildCommandGivenFrame() > 24) {

						//System.out.println(b.getType() + " (" + b.getFinalPosition().getX() + "," + b.getFinalPosition().getY() + ") buildCommandGiven . but now Unassigned" );

						// tell worker manager the unit we had is not needed now, since we might not be able
						// to get a valid location soon enough
						WorkerManager.Instance().setIdleWorker(b.getConstructionWorker());

						// free the previous location in reserved
						int width = b.getType().tileWidth();
						int height = b.getType().tileHeight();
						
						if (b.getType() == UnitType.Terran_Command_Center ||
								b.getType() == UnitType.Terran_Factory ||
								b.getType() == UnitType.Terran_Starport ||
								b.getType() == UnitType.Terran_Science_Facility)
							{
								width += 2;
						}
						ConstructionPlaceFinder.Instance().freeTiles(b.getFinalPosition(), width, height);

						// nullify its current builder unit
						b.setConstructionWorker(null);

						// nullify its current builder unit
						b.setFinalPosition(TilePosition.None);

						// reset the build command given flag
						b.setBuildCommandGiven(false);

						// add the building back to be assigned
						b.setStatus(ConstructionTask.ConstructionStatus.Unassigned.ordinal());
					}
				}
	        }
	    }
	}

	/// �Ǽ��� ���۵Ǹ�, �ش� ConstructionTask �� �Ǽ� ������¸� UnderConstruction ���� �����ϰ�<br>
	/// ���� �� �����佺 ������ ��� �Ǽ� �ϲ��� �����մϴ�
	public void checkForStartedConstruction()
	{				
		// for each building unit which is being constructed
	    for (Unit buildingThatStartedConstruction : MyBotModule.Broodwar.self().getUnits())
	    {
	        // filter out units which aren't buildings under construction
	        if (!buildingThatStartedConstruction.getType().isBuilding() || !buildingThatStartedConstruction.isBeingConstructed())
	        {
	            continue;
	        }
			
	        // check all our building status objects to see if we have a match and if we do, update it
	        for (ConstructionTask b : constructionQueue)
	        {
				if (b.getStatus() != ConstructionTask.ConstructionStatus.Assigned.ordinal())
	            {
	                continue;
	            }
	        
	            // check if the positions match.  Worker just started construction.
	            if (b.getFinalPosition().getX() == buildingThatStartedConstruction.getTilePosition().getX() && b.getFinalPosition().getY() == buildingThatStartedConstruction.getTilePosition().getY())
	            {
					//System.out.println( "Construction " + b.getType() + " started at " + b.getFinalPosition().getX() + "," + b.getFinalPosition().getY() );

	                // the resources should now be spent, so unreserve them
	                reservedMinerals -= buildingThatStartedConstruction.getType().mineralPrice();
	                reservedGas      -= buildingThatStartedConstruction.getType().gasPrice();

	                // flag it as started and set the buildingUnit
	                b.setUnderConstruction(true);

	                b.setBuildingUnit(buildingThatStartedConstruction);

	                // if we are zerg, make the buildingUnit null since it's morphed or destroyed
					// Extractor �� ��� destroyed �ǰ�, �׿� �ǹ��� ��� morphed �ȴ�
	                if (MyBotModule.Broodwar.self().getRace() == Race.Zerg)
	                {
	                	b.setConstructionWorker(null);
	                }
					// if we are protoss, give the worker back to worker manager
					else if (MyBotModule.Broodwar.self().getRace() == Race.Protoss)
	                {
	                    WorkerManager.Instance().setIdleWorker(b.getConstructionWorker());
	                    b.setConstructionWorker(null);
	                }

	                // free this space
	                int width = b.getType().tileWidth();
					int height = b.getType().tileHeight();
					
					if (b.getType() == UnitType.Terran_Command_Center ||
							b.getType() == UnitType.Terran_Factory ||
							b.getType() == UnitType.Terran_Starport ||
							b.getType() == UnitType.Terran_Science_Facility)
						{
							width += 2;
					}
					ConstructionPlaceFinder.Instance().freeTiles(b.getFinalPosition(), width, height);

					// put it in the under construction vector
	                b.setStatus(ConstructionTask.ConstructionStatus.UnderConstruction.ordinal());

					// only one building will match
	                break;
	            }
	        }
	    }
	}

	/// �׶��� ��� �Ǽ� ������°� UnderConstruction ������ �Ǽ� �ϲ��� ���� ���, �ٸ� �Ǽ� �ϲ��� �����ؼ� �Ǽ��� ����ǵ��� �մϴ�<br>
	/// �׶��� �Ǽ��� ������ ��, �Ǽ� ���߿� �ϲ��� ���� �� �ֽ��ϴ�. �� ���, �ǹ��� ���� �ٽ� �ٸ� SCV�� �Ҵ��մϴ�<br>
	/// �����, �����佺 / ���״� �Ǽ��� �����ϸ� �ϲ� �����͸� null �� ����� ������ (constructionWorker = null) �Ǽ� ���߿� ���� �ϲ��� �Ű澵 �ʿ� �����ϴ� 
	public void checkForDeadTerranBuilders()
	{
		if (MyBotModule.Broodwar.self().getRace() == Race.Terran) {

			if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_SCV) <= 0) return;
				
			// for each of our buildings under construction
			for (ConstructionTask b : constructionQueue)
			{
				// if a terran building whose worker died mid construction, 
				// send the right click command to the buildingUnit to resume construction			
				if (b.getStatus() == ConstructionTask.ConstructionStatus.UnderConstruction.ordinal()) {

					if (b.getBuildingUnit().isCompleted()) continue;
//					//�Ǽ����� �ϲ� ������ 20���Ϸ� idle �Ǿ����� ���⼱ ���� �ʾ� �ǹ� ��Ǽ� ���ϴ� ���� �߻�
//					//���� �Ҵ�� ��Ŀ null�� ����
//					if(WorkerManager.Instance().getWorkerData().getWorkerId(b.getConstructionWorker())){
//						b.setConstructionWorker(null);
//					}
					if (b.getConstructionWorker() == null || b.getConstructionWorker().exists() == false || b.getConstructionWorker().getHitPoints() <= 0 ){
				
						//System.out.println("checkForDeadTerranBuilders - chooseConstuctionWorkerClosest for " + b.getType() + " to worker near " + b.getFinalPosition().getX() + "," + b.getFinalPosition().getY());
						/*
						 * 1.3 �ʹ� ���� ���۸� �����϶� ��Ŀ ���� �跰���� �ߴܵȰǹ��� �ٽ� �����°ɷ� �߰�
						 */
//						if(( CombatManager.Instance().FastZerglingsInOurBase >0) 
//								&& !(b.getBuildingUnit().getType() == UnitType.Terran_Bunker || b.getBuildingUnit().getType() == UnitType.Terran_Barracks 
//								|| b.getBuildingUnit().getType() == UnitType.Terran_Supply_Depot || b.getBuildingUnit().getType() == UnitType.Terran_Factory)){
//							continue;
//						} 
						// grab a worker unit from WorkerManager which is closest to this final position	
						Unit workerToAssign = WorkerManager.Instance().chooseConstuctionWorkerClosestTo(b.getType(), b.getFinalPosition(), true, b.getLastConstructionWorkerID());
	
						if (workerToAssign != null)
						{
							//System.out.println("set ConstuctionWorker " + workerToAssign.getID());

							b.setConstructionWorker(workerToAssign);								
							CommandUtil.rightClick(b.getConstructionWorker(), b.getBuildingUnit());
							b.setBuildCommandGiven(true);
							b.setLastBuildCommandGivenFrame(MyBotModule.Broodwar.getFrameCount());
							b.setLastConstructionWorkerID(b.getConstructionWorker().getID());
						}
					}
				}
			}
		}
	}

	/// �Ǽ��� �Ϸ�� ConstructionTask �� �����ϰ�,<br>  
	/// �׶� ������ ��� �Ǽ� �ϲ��� �����մϴ�
	public void checkForCompletedBuildings()
	{
	    Vector<ConstructionTask> toRemove = new Vector<ConstructionTask>();

	    // for each of our buildings under construction
	    for (ConstructionTask b : constructionQueue)
	    {
	        if (b.getStatus() != ConstructionTask.ConstructionStatus.UnderConstruction.ordinal())
	        {
	            continue;       
	        }

	        // if the unit has completed
	        if (b.getBuildingUnit().isCompleted())
	        {
				//System.out.println("Construction " + b.getType() + " completed at " + b.getFinalPosition().getX() + "," + b.getFinalPosition().getY());
				
				// if we are terran, give the worker back to worker manager
	            if (MyBotModule.Broodwar.self().getRace() == Race.Terran)
	            {
	                WorkerManager.Instance().setIdleWorker(b.getConstructionWorker());
	            }

	            // remove this unit from the under construction vector
	            toRemove.add(b);
	        }
	    }

	    removeCompletedConstructionTasks(toRemove);
	}

	/// �Ǽ� ������� üũ�ϰ�, �ذ��մϴ�
	public void checkForDeadlockConstruction()
	{
		Vector<ConstructionTask> toCancel = new Vector<ConstructionTask>();
		for (ConstructionTask b : constructionQueue)
		{
			if (b.getStatus() != ConstructionTask.ConstructionStatus.UnderConstruction.ordinal())
			{
				// BuildManager�� �Ǵ������� Construction ���������� �������� ConstructionManager�� ConstructionQueue �� ���µ�, 
				// ���� �ǹ��� �ı��Ǽ� Construction�� ������ �� ���� �Ǿ��ų�,
				// �ϲ��� �� ����ϴ� �� ���ӻ�Ȳ�� �ٲ�, ��� ConstructionQueue �� �����ְ� �Ǵ� dead lock ��Ȳ�� �˴ϴ� 
				// ���� �ǹ��� BuildQueue�� �߰��س�����, �ش� ConstructionQueueItem �� �������� ���������� �Ǵ��ؾ� �մϴ�
				UnitType unitType = b.getType();
				UnitType producerType = b.getType().whatBuilds().first;
				final Map<UnitType,Integer> requiredUnits = unitType.requiredUnits();
				Region desiredPositionRegion = BWTA.getRegion(b.getDesiredPosition());

				boolean isDeadlockCase = false;

				// �ǹ��� �����ϴ� �����̳�, ������ �����ϴ� �ǹ��� �������� �ʰ�, �Ǽ� ���������� ������ dead lock
				if (BuildManager.Instance().isProducerWillExist(producerType) == false) {
					isDeadlockCase = true;
				}

				// Refinery �ǹ��� ���, �ǹ� ���� ��Ҹ� ã�� �� ���� �Ǿ��ų�, �ǹ� ���� �� �����Ŷ�� �Ǵ��ߴµ� �̹� Refinery �� �������ִ� ���, dead lock 
				if (!isDeadlockCase && unitType == InformationManager.Instance().getRefineryBuildingType())
				{
					boolean hasAvailableGeyser = true;

					TilePosition testLocation;
					if (b.getFinalPosition() != TilePosition.None && b.getFinalPosition() != TilePosition.Invalid && b.getFinalPosition().isValid()) {
						testLocation = b.getFinalPosition();
					}
					else {
						testLocation = ConstructionPlaceFinder.Instance().getBuildLocationNear(b.getType(), b.getDesiredPosition());
					}

					// Refinery �� �������� ��Ҹ� ã�� �� ������ dead lock
					if (testLocation == TilePosition.None || testLocation == TilePosition.Invalid || testLocation.isValid() == false) {
						//System.out.println("Construction Dead lock case . Cann't find place to construct " + b.getType());
						hasAvailableGeyser = false;
					}
					else {
						// Refinery �� �������� ��ҿ� Refinery �� �̹� �Ǽ��Ǿ� �ִٸ� dead lock 
						for (Unit u : MyBotModule.Broodwar.getUnitsOnTile(testLocation)) {
							if (u.getType().isRefinery() && u.exists() ) {
								hasAvailableGeyser = false;
								break;
							}
						}
						if (hasAvailableGeyser == false) {
							//System.out.println("Construction Dead lock case . Refinery Building was built already at " + testLocation.getX() + ", " + testLocation.getY());
						}
					}

					if (hasAvailableGeyser == false) {
						isDeadlockCase = true;
					}
				}

				// ������� Ȥ�� �������, �Ǽ� ��Ұ� �Ʊ� ���� Region �� �ƴϰ�, ������ ������ Region �� �Ǿ����� �Ϲ������δ� ���������� dead lock �� �ȴ� 
				// (����ĳ�� �����̰ų�, ���� ���� Region ��ó���� �׶� �ǹ� �Ǽ��ϴ� ��쿡�� ������������..)
				if (!isDeadlockCase
					&& !InformationManager.Instance().getOccupiedRegions(InformationManager.Instance().selfPlayer).contains(desiredPositionRegion)
					&& InformationManager.Instance().getOccupiedRegions(InformationManager.Instance().enemyPlayer).contains(desiredPositionRegion))
				{
					isDeadlockCase = true;
				}

				// ���� �ǹ�/������ �ִµ� 
				if (!isDeadlockCase && requiredUnits.size() > 0)
				{
					Iterator<UnitType> it = requiredUnits.keySet().iterator();
					while(it.hasNext())
					{
						UnitType requiredUnitType = it.next();

						if (requiredUnitType != UnitType.None) {

							// ���� �ǹ� / ������ �������� �ʰ�, ���� �������� �ʰ�
							if (MyBotModule.Broodwar.self().completedUnitCount(requiredUnitType) == 0
								&& MyBotModule.Broodwar.self().incompleteUnitCount(requiredUnitType) == 0)
							{
								// ���� �ǹ��� �Ǽ� ���������� ������ dead lock
								if (requiredUnitType.isBuilding())
								{
									if (ConstructionManager.Instance().getConstructionQueueItemCount(requiredUnitType, null) == 0) {
										isDeadlockCase = true;
									}
								}
							}
						}
					}
				}

				if (isDeadlockCase) {
					System.out.println("deadlock cancel at conQ:" + requiredUnits.toString());
					toCancel.add(b);
				}
			}
		}

		for (ConstructionTask i : toCancel)
		{
			cancelConstructionTask(i.getType(), i.getDesiredPosition());
		}
	}

	public void checkConstructionBuildings()
	{
		if (MyBotModule.Broodwar.self().getRace() != Race.Terran)
		{
			return;
		}
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			// �Ǽ����� �ǹ��� ��� ���� �ް� �ְ� �������� 100���̸� �Ǽ� ��� 
			if (unit.getType().isBuilding() && unit.isConstructing() && unit.isUnderAttack() && unit.getHitPoints() < 100 && unit.getHitPoints() < unit.getType().maxHitPoints()*0.1)
			{
				unit.cancelConstruction();
				cancelConstructionTask(unit.getType(), unit.getTilePosition());
			}
		}
	}
	// COMPLETED
	public boolean isEvolvedBuilding(UnitType type) 
	{
	    if (type == UnitType.Zerg_Sunken_Colony ||
	        type == UnitType.Zerg_Spore_Colony ||
	        type == UnitType.Zerg_Lair ||
	        type == UnitType.Zerg_Hive ||
	        type == UnitType.Zerg_Greater_Spire)
	    {
	        return true;
	    }
	    return false;
	}

	public boolean isBuildingPositionExplored(final ConstructionTask b)
	{
	    TilePosition tile = b.getFinalPosition();

	    // for each tile where the building will be built
	    for (int x=0; x<b.getType().tileWidth(); ++x)
	    {
	        for (int y=0; y<b.getType().tileHeight(); ++y)
	        {
	            if (!MyBotModule.Broodwar.isExplored(tile.getX()+ x,tile.getY() + y))
	            {
	                return false;
	            }
	        }
	    }

	    return true;
	}

	/// Construction �� ���� �����ص� Mineral ���ڸ� �����մϴ�
	public int getReservedMinerals() 
	{
	    return reservedMinerals;
	}

	/// Construction �� ���� �����ص� Gas ���ڸ� �����մϴ�
	public int getReservedGas() 
	{
	    return reservedGas;
	}

	/// constructionQueue �� ConstructionTask ������ �����մϴ�
	public Vector<UnitType> buildingsQueued()
	{
	    Vector<UnitType> buildingsQueued = new Vector<UnitType>();//TODO?? �������� �׳� =null by Zeus

	    for (final ConstructionTask b : constructionQueue)
	    {
	        if (b.getStatus() == ConstructionTask.ConstructionStatus.Unassigned.ordinal() || b.getStatus() == ConstructionTask.ConstructionStatus.Assigned.ordinal())
	        {
	            buildingsQueued.add(b.getType());
	        }
	    }

	    return buildingsQueued;
	}

	/// constructionQueue �� ConstructionTask ������ �����մϴ�<br>
	/// queryTilePosition �� �Է��� ���, ��ġ�� �Ÿ������� ����մϴ�
	public int getConstructionQueueItemCount(UnitType queryType, TilePosition queryTilePosition)
	{
		// queryTilePosition �� �Է��� ���, �Ÿ��� maxRange. Ÿ�ϴ���
		int maxRange = 16;

		Position queryTilePositionPoint = null;
		if(queryTilePosition == null)
		{
			queryTilePositionPoint = Position.None;
		}
		else
		{
			queryTilePositionPoint = queryTilePosition.toPosition();
		}

		int count = 0;
		for (ConstructionTask b : constructionQueue)
		{
			if (b.getType() == queryType)
			{
				if (queryType.isBuilding() && queryTilePosition != TilePosition.None)
				{
					if (queryTilePositionPoint.getDistance(b.getDesiredPosition().toPosition()) <= maxRange) {
						count++;
					}
				}
				else {
					count++;
				}
			}
		}

		return count;
	}
	
	public int getConstructionQueueItemCountNear(UnitType queryType, TilePosition queryTilePosition, int range)
	{
		// queryTilePosition �� �Է��� ���, �Ÿ��� maxRange. Ÿ�ϴ���
		int maxRange = range;

		TilePosition queryTilePositionPoint = null;
		queryTilePositionPoint = queryTilePosition.toPosition().toTilePosition();
		

		int count = 0;
		for (ConstructionTask b : constructionQueue)
		{
			if (b.getType() == queryType)
			{
				if (queryType.isBuilding())
				{
					if (queryTilePositionPoint.getDistance(b.getDesiredPosition().toPosition().toTilePosition()) <= maxRange) {
						count++;
					}
				}
			}
		}

		return count;
	}
	

	public Vector<ConstructionTask> getConstructionQueue()
	{
		return constructionQueue;
	}
}