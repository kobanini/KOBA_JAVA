
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;

/// �Ǽ���ġ Ž���� ���� class
public class ConstructionPlaceFinder {

	/// �Ǽ���ġ Ž�� ���
	public enum ConstructionPlaceSearchMethod { 
		SpiralMethod,	///< ���������� ���ư��� Ž��
		SupplyDepotMethod, /// < ���ö��� ���� �޽��. ���� ���θ� ���ö��� ũ�⸸ŭ ���ؼ� ã��
		NewMethod 		///< ����
	};
	
	public int maxSupplyCntX = 3;
	public int maxSupplyCntY = 4;
	
	/// �ǹ� �Ǽ� ���� Ÿ���� �����س��� ���� 2���� �迭<br>
	/// TilePosition �����̱� ������ ���� 128*128 ����� �ȴ�<br>
	/// �����, �ǹ��� �̹� ������ Ÿ���� �������� �ʴ´�
	private boolean[][] reserveMap = new boolean[128][128];
	
	/// BaseLocation �� Mineral / Geyser ������ Ÿ�ϵ��� ��� �ڷᱸ��. ���⿡�� Addon �̿ܿ��� �ǹ��� ���� �ʵ��� �մϴ�	
	private Set<TilePosition> tilesToAvoid = new HashSet<TilePosition>();
	private Set<TilePosition> tilesToAvoidAbsolute = new HashSet<TilePosition>();
	//���ö��� ���� ����
	private Set<TilePosition> tilesToAvoidSupply = new HashSet<TilePosition>();
	
	private static ConstructionPlaceFinder instance = new ConstructionPlaceFinder();
	
	private static boolean isInitialized = false;
	
	/// static singleton ��ü�� �����մϴ�
	public static ConstructionPlaceFinder Instance() {
		if (isInitialized == false) {
			instance.setTilesToAvoid();
			instance.setTilesToAvoidForFirstGas();
			isInitialized = true;
		}
		return instance;
	}

	/// seedPosition �� seedPositionStrategy �Ķ���͸� Ȱ���ؼ� �ǹ� �Ǽ� ���� ��ġ�� Ž���ؼ� �����մϴ�<br>
	/// seedPosition �������� ������ ���� �����ϰų�, seedPositionStrategy �� ���� ���� �м���� �ش� ���� �������� ������ ���� �����մϴ�<br>
	/// seedPosition, seedPositionStrategy �� �Է����� ������, MainBaseLocation �������� ������ ���� �����մϴ�
	public final TilePosition getBuildLocationWithSeedPositionAndStrategy(UnitType buildingType, TilePosition seedPosition, BuildOrderItem.SeedPositionStrategy seedPositionStrategy)
	{
		TilePosition desiredPosition = TilePosition.None;

		// seedPosition �� �Է��� ��� �� ��ó���� ã�´�
		if (seedPosition != TilePosition.None  && seedPosition.isValid() )
		{
			
			//System.out.println("checking here");
			desiredPosition = getBuildLocationNear(buildingType, seedPosition, true, true);
			
		}
		// seedPosition �� �Է����� ���� ���
		else {
			//System.out.println("���������� �Է� ==>> (" + seedPositionStrategy + ")" );
			Chokepoint tempChokePoint;
			BaseLocation tempBaseLocation;
			TilePosition tempTilePosition = null;
//			Region tempBaseRegion;
//			int vx, vy;
//			double d, t;
//			int bx, by;
			
			switch (seedPositionStrategy) {

			case MainBaseLocation:
				
				tempTilePosition = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition();
				
				//���� 3�� ���ܻ�Ȳ
				if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
					
					if(BlockingEntrance.Instance().getStartingInt() == 3){		
						int temp=0;
						for(Unit unit : MyBotModule.Broodwar.self().getUnits()){
							if(unit.getType().isBuilding()){
								temp++;
							}
						}
						if(temp>=12){//TODO 12���� �ȵ��� ���̽��̸�... �Ʒ� ������ Ÿ��... ������ �ٸ��� �� ���´�
							tempTilePosition = new TilePosition(116,58);
						}
					}
				}
				
				desiredPosition = getBuildLocationNear(buildingType, tempTilePosition, true);
				
				if(desiredPosition == null){
					BuildManager.Instance().MainBaseLocationFull = true;
				}
				break;

			case FirstExpansionLocation:
				tempBaseLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self());
				if (tempBaseLocation != null) {
					desiredPosition = getBuildLocationNear(buildingType, tempBaseLocation.getTilePosition());
				}
				if(desiredPosition == null){
					BuildManager.Instance().FirstExpansionLocationFull   = true;
				}
				break;

			case FirstChokePoint:
				tempChokePoint = InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self());
				if (tempChokePoint != null) {
					desiredPosition = getBuildLocationNear(buildingType, tempChokePoint.getCenter().toTilePosition());
				}
				if(desiredPosition == null){
					BuildManager.Instance().FirstChokePointFull    = true;
				}
				break;

			case SecondChokePoint:
				tempChokePoint = InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.self());
				if (tempChokePoint != null) {
					desiredPosition = getBuildLocationNear(buildingType, tempChokePoint.getCenter().toTilePosition());
				}
				if(desiredPosition == null){
					BuildManager.Instance().SecondChokePointFull     = true;
				}
				break;
				
			case NextExpansionPoint: //TODO NextSupplePoint ���� �߰�����Ʈ�� �����ϳ�?
				tempBaseLocation = InformationManager.Instance().getNextExpansionLocation();
				if (tempBaseLocation != null) {
					desiredPosition = getBuildLocationNear(buildingType, tempBaseLocation.getTilePosition());
				}else{
					desiredPosition = getBuildLocationNear(buildingType, InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition());
				}
				break;
				
			case LastBuilingPoint: 
				tempTilePosition = InformationManager.Instance().getLastBuilingLocation();

				if (tempTilePosition != null) {
					if(buildingType == UnitType.Terran_Supply_Depot){
						if(BuildManager.Instance().FisrtSupplePointFull == true){
							tempTilePosition = BlockingEntrance.Instance().getSupplyPosition(tempTilePosition);
							desiredPosition = getBuildLocationNear(buildingType, tempTilePosition);
						
							break;
						}
					}
				
					desiredPosition = getBuildLocationNear(buildingType, tempTilePosition);
				}
//				else{
//					desiredPosition = getBuildLocationNear(buildingType, InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition());
//				}
				break;
				
			case NextSupplePoint: 
				
				if(buildingType == UnitType.Terran_Supply_Depot){
					if(BuildManager.Instance().FisrtSupplePointFull != true){
						tempTilePosition = BlockingEntrance.Instance().getSupplyPosition();
						desiredPosition = getBuildLocationNear(buildingType, tempTilePosition);
					
						if(desiredPosition == null){
							BuildManager.Instance().FisrtSupplePointFull = true;
						}
						break;
					}
				}else{
					tempTilePosition = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition();
				}
				break;
			
			case getLastBuilingFinalLocation: //�̳��� �������̴ϱ�.... NULL �ϼ��� ����.
			
				tempTilePosition = InformationManager.Instance().getLastBuilingFinalLocation();
				desiredPosition = getBuildLocationNear(buildingType, tempTilePosition);
			break;
					
			default:
				break;
			}
			
		}

		return desiredPosition;
	}

	/// desiredPosition ��ó���� �ǹ� �Ǽ� ���� ��ġ�� Ž���ؼ� �����մϴ�<br>
	/// desiredPosition �������� ������ ���� ã�� ��ȯ�մϴ�<br>
	/// desiredPosition �� valid �� ���� �ƴ϶��, desiredPosition �� MainBaseLocation �� �ؼ� ������ ã�´�<br>
	/// Returns a suitable TilePosition to build a given building type near specified TilePosition aroundTile.<br>
	/// Returns BWAPI::TilePositions::None, if suitable TilePosition is not exists (�ٸ� ���ֵ��� �ڸ��� �־, Pylon, Creep, �ǹ����� Ÿ�� ������ ���� ���� ��� ��)
	
	public final TilePosition getBuildLocationNear(UnitType buildingType, TilePosition desiredPosition, Boolean MethodFix)
	{
		return getBuildLocationNear(buildingType, desiredPosition, MethodFix, false);
	}
	public final TilePosition getBuildLocationNear(UnitType buildingType, TilePosition desiredPosition){
		return getBuildLocationNear(buildingType, desiredPosition, false);
	}
	public final TilePosition getBuildLocationNear(UnitType buildingType, TilePosition desiredPosition, Boolean MethodFix, Boolean spaceZero)
	{
		//System.out.println("getBuildLocationNear �Է�111 ==>> (" + desiredPosition.getX() + " , " +  desiredPosition.getY() + ")" );
		if (buildingType.isRefinery())
		{
			//std::cout << "getRefineryPositionNear "<< std::endl;

			return getRefineryPositionNear(desiredPosition);
		}

		if (desiredPosition == TilePosition.None || desiredPosition == TilePosition.Unknown || desiredPosition == TilePosition.Invalid || desiredPosition.isValid() == false)
		{
			desiredPosition = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition();
		}

		TilePosition testPosition = TilePosition.None;

		// TODO ���� : �Ǽ� ��ġ Ž�� ����� ConstructionPlaceSearchMethod::SpiralMethod �� �ϴµ�, �� ���� ����� �����غ� �����̴�
		int constructionPlaceSearchMethod = 0;
		
		if(buildingType == UnitType.Terran_Supply_Depot && MethodFix == false){
			constructionPlaceSearchMethod = ConstructionPlaceSearchMethod.SupplyDepotMethod.ordinal();
		}else{ 
			constructionPlaceSearchMethod = ConstructionPlaceSearchMethod.SpiralMethod.ordinal();	
		}
		
		// �Ϲ����� �ǹ��� ���ؼ��� �ǹ� ũ�⺸�� Config::Macro::BuildingSpacing ĭ ��ŭ �����¿�� �� �а� ���������� �ξ �� �ڸ��� �˻��Ѵ�
		int buildingGapSpace = Config.BuildingSpacing;

		// ResourceDepot (Nexus, Command Center, Hatchery),
		// Protoss_Pylon, Terran_Supply_Depot, 
		// Protoss_Photon_Cannon, Terran_Bunker, Terran_Missile_Turret, Zerg_Creep_Colony �� �ٸ� �ǹ� �ٷ� ���� �ٿ� ���� ��찡 �����Ƿ� 
		// buildingGapSpace�� �ٸ� Config ������ �����ϵ��� �Ѵ�
		if (buildingType.isResourceDepot()) {
			buildingGapSpace = Config.BuildingResourceDepotSpacing;
		}
		if(buildingType == UnitType.Terran_Barracks){
			buildingGapSpace = 0;
		}
		if(buildingType == UnitType.Terran_Factory){
			buildingGapSpace = 0;
		}
		if (buildingType == UnitType.Terran_Supply_Depot) {
			buildingGapSpace = Config.BuildingSupplyDepotSpacing;
			if(constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.SupplyDepotMethod.ordinal()){
				buildingGapSpace=0;
			}
			if(MethodFix == true){
				buildingGapSpace=1;
			}
			if(spaceZero == true){
				buildingGapSpace=0;
			}
		}
		
//		else if (buildingType == UnitType.Protoss_Photon_Cannon || buildingType == UnitType.Terran_Bunker 
//			|| buildingType == UnitType.Terran_Missile_Turret || buildingType == UnitType.Zerg_Creep_Colony) {
		else if (buildingType == UnitType.Terran_Missile_Turret) {
			buildingGapSpace = Config.BuildingDefenseTowerSpacing;
		}else if (buildingType == UnitType.Terran_Bunker) {
			buildingGapSpace = 0;
		}

		if (buildingType == UnitType.Terran_Missile_Turret) {
			while (buildingGapSpace >= 0) {

				testPosition = getBuildLocationNear(buildingType, desiredPosition, buildingGapSpace, constructionPlaceSearchMethod);

				// std::cout << "ConstructionPlaceFinder testPosition " << testPosition.x << "," << testPosition.y << std::endl;

				if (testPosition != TilePosition.None && testPosition != TilePosition.Invalid)
					return testPosition;
						
				// ã�� �� ���ٸ�, buildingGapSpace ���� �ٿ��� �ٽ� Ž���Ѵ�
				// buildingGapSpace ���� 1�̸� ���������� ���������� ��찡 ����  �����ϵ��� �Ѵ� 
				// 4 -> 3 -> 2 -> 0 -> Ž�� ����
				//      3 -> 2 -> 0 -> Ž�� ���� 
				//           1 -> 0 -> Ž�� ����
				if (buildingGapSpace > 2) {
					buildingGapSpace -= 1;
				}
				else if (buildingGapSpace == 2){
					buildingGapSpace = 0;
				}
				else if (buildingGapSpace == 1){
					buildingGapSpace = 0;
				}
				else {
					break;
				}
			}
		}else{
			testPosition = getBuildLocationNear(buildingType, desiredPosition, buildingGapSpace, constructionPlaceSearchMethod);
		}

		if (testPosition != TilePosition.None && testPosition != TilePosition.Invalid){
			return testPosition;
		}
		else{
			return TilePosition.None;
		}
	}

	/// �ش� buildingType �� �Ǽ��� �� �ִ� ��ġ�� desiredPosition ��ó���� Ž���ؼ� Ž������� �����մϴ�<br>
	/// buildingGapSpace�� �ݿ��ؼ� canBuildHereWithSpace �� ����ؼ� üũ<br>
	/// ��ã�´ٸ� BWAPI::TilePositions::None �� �����մϴ�<br>
	/// TODO ���� : �ǹ��� ��ȹ���� ������ �ִ� ���� ���� ���� ��� �ϴٺ���, ������ �ǹ� ���̿� ������ ��찡 �߻��� �� �ִµ�, �̸� �����ϴ� ����� �����غ� �����Դϴ�
	public final TilePosition getBuildLocationNear(UnitType buildingType, TilePosition desiredPosition, int buildingGapSpace, int constructionPlaceSearchMethod)
	{
		//System.out.println("getBuildLocationNear �Է�222 ==>> (" + desiredPosition.getX() + " , " +  desiredPosition.getY() + ")" );
		// std::cout << std::endl << "getBuildLocationNear " << buildingType.getName().c_str() << " " << desiredPosition.x << "," << desiredPosition.y 
		//	<< " gap " << buildingGapSpace << " method " << constructionPlaceSearchMethod << std::endl;

		//returns a valid build location near the desired tile position (x,y).
		TilePosition resultPosition = TilePosition.None;
		ConstructionTask b = new ConstructionTask(buildingType, desiredPosition);

		// maxRange �� �������� �ʰų�, maxRange �� 128���� �����ϸ� ���� ��ü�� �� Ž���ϴµ�, �ſ� �������Ӹ� �ƴ϶�, ��κ��� ��� ���ʿ��� Ž���� �ȴ�
		// maxRange �� 16 ~ 64�� �����ϴ�
		int maxRange = 35; // maxRange = BWAPI::Broodwar->mapWidth()/4;
		boolean isPossiblePlace = false;
			
		if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.SpiralMethod.ordinal())
		{
			// desiredPosition ���κ��� �����ؼ� spiral �ϰ� Ž���ϴ� ���
			// ó������ �Ʒ� ���� (0,1) -> ����������(1,0) -> ����(0,-1) -> ��������(-1,0) -> �Ʒ���(0,1) -> ..
			int currentX = desiredPosition.getX();
			int currentY = desiredPosition.getY();
			int spiralMaxLength = 1;
			int numSteps = 0;
			boolean isFirstStep = true;

			int spiralDirectionX = 0;
			int spiralDirectionY = 1;
			while (spiralMaxLength < maxRange)
			{
				if (currentX >= 0 && currentX < MyBotModule.Broodwar.mapWidth() && currentY >= 0 && currentY < MyBotModule.Broodwar.mapHeight()) {

					isPossiblePlace = canBuildHereWithSpace(new TilePosition(currentX, currentY), b, buildingGapSpace);

					if (isPossiblePlace) {
						resultPosition = new TilePosition(currentX, currentY);
						break;
					}
					//System.out.println(buildingType + " �� ����ȵ� ==>>>> ("+currentX+"/"+currentY+")");
					
				}

				currentX = currentX + spiralDirectionX;
				currentY = currentY + spiralDirectionY;
				numSteps++;
				
				// �ٸ� �������� ��ȯ�Ѵ�
				if (numSteps == spiralMaxLength)
				{
					numSteps = 0;

					if (!isFirstStep)
						spiralMaxLength++;

					isFirstStep = !isFirstStep;

					if (spiralDirectionX == 0)
					{
						spiralDirectionX = spiralDirectionY;
						spiralDirectionY = 0;
					}
					else
					{
						spiralDirectionY = -spiralDirectionX;
						spiralDirectionX = 0;
					}
				}
			}
//			if(resultPosition ==null){
//				System.out.println("chekcking resultPosition: " + currentX + ", "+ currentY);
//			}else{
//				System.out.println("chekcking resultPosition: " + resultPosition.toString());
//			}
		}
		else if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.SupplyDepotMethod.ordinal()) {
			//���ö��� ���� �� ����(4X4)
			// y����� ����. ���� �����ǿ��� X�� + 3 �Ѿ�� Y�� +2
			int currentX = desiredPosition.getX();
			int currentY = desiredPosition.getY();
			//System.out.println("**************input TilePosition ==>>>>  (" + currentX + " / " + currentY + ")");
			int depostSizeX = 3;
			int depostSizeY = 2;
			//4X4 �迭�� �Ǽ��� ���̹Ƿ� 4������ ������.
			
		
			/*for(int y_position = 0; y_position < maxSupplyCntY ; y_position ++){
				for(int x_position = 0; x_position < maxSupplyCntX ; x_position ++){
					if (currentX >= 0 && currentX < MyBotModule.Broodwar.mapWidth() && currentY >= 0 && currentY < MyBotModule.Broodwar.mapHeight()) {

						isPossiblePlace = canBuildHereWithSpace(new TilePosition(currentX, currentY), b, 0);

						if (isPossiblePlace) {
							resultPosition = new TilePosition(currentX, currentY);
							return resultPosition;
						}
						//System.out.println("is impossible place ==> (" + currentX + " / " + currentY + ")");
					}
					currentX = currentX + depostSizeX;
					//currentY = currentY + spiralDirectionY;
				}
				//X�ุ ���������� ��ã����� Y���� ������Ű�� X���� ���� ���������� ����
				currentX = desiredPosition.getX();
				currentY = currentY + depostSizeY;
			}*/
			for(int x_position= 0; x_position < maxSupplyCntX ; x_position ++){
				for(int y_position  = 0; y_position < maxSupplyCntY ; y_position ++){
					if (currentX >= 0 && currentX < MyBotModule.Broodwar.mapWidth() && currentY >= 0 && currentY < MyBotModule.Broodwar.mapHeight()) {

						isPossiblePlace = canBuildHereWithSpace(new TilePosition(currentX, currentY), b, 0);

						if (isPossiblePlace) {
							resultPosition = new TilePosition(currentX, currentY);
							break;
						}
						//System.out.println("is impossible place ==> (" + currentX + " / " + currentY + ")");
					}
					
					currentY = currentY + depostSizeY;
					//currentY = currentY + spiralDirectionY;
				}
				if (isPossiblePlace) {
					break;
				}
				
				currentY = desiredPosition.getY();
				currentX = currentX + depostSizeX;
			}
			//System.out.println("supply position ==>>>>>>>>>>>  (" +currentX + " , " +currentY + ")");
			
			
			/*while (maxSupplyCnt < 4)
			{
				if (currentX >= 0 && currentX < MyBotModule.Broodwar.mapWidth() && currentY >= 0 && currentY < MyBotModule.Broodwar.mapHeight()) {

					isPossiblePlace = canBuildHereWithSpace(new TilePosition(currentX, currentY), b, buildingGapSpace);

					if (isPossiblePlace) {
						resultPosition = new TilePosition(currentX, currentY);
						break;
					}
				}

				currentX = currentX + spiralDirectionX;
				currentY = currentY + spiralDirectionY;
				numSteps++;
				
				// �ٸ� �������� ��ȯ�Ѵ�
				if (numSteps == spiralMaxLength)
				{
					numSteps = 0;

					if (!isFirstStep)
						spiralMaxLength++;

					isFirstStep = !isFirstStep;

					if (spiralDirectionX == 0)
					{
						spiralDirectionX = spiralDirectionY;
						spiralDirectionY = 0;
					}
					else
					{
						spiralDirectionY = -spiralDirectionX;
						spiralDirectionX = 0;
					}
				}
			}*/
//			if(resultPosition ==null){
//				System.out.println("chekcking resultPosition2: " + currentX + ", "+ currentY);
//			}else{
//				System.out.println("chekcking resultPosition2: " + resultPosition.toString());
//			}
		}
		else if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.NewMethod.ordinal()) {
		}

		return resultPosition;
	}

	/// �ش� ��ġ�� �ǹ� �Ǽ��� �������� ���θ� buildingGapSpace ������ �����ؼ� �Ǵ��Ͽ� �����մϴ�<br>
	/// Broodwar �� canBuildHere, isBuildableTile, isReservedTile �� üũ�մϴ�
	public final boolean canBuildHereWithSpace(TilePosition position, final ConstructionTask b, int buildingGapSpace)
	{
		//System.out.println("canBuildHereWithSpace �Է�222 ==>> (" + position.getX() + " , " +  position.getY() + ") , buildingGapSpace ==>> " + buildingGapSpace );
		//if we can't build here, we of course can't build here with space
		if (!canBuildHere(position, b))
		{
			return false;
		}

		// height and width of the building
		int width = b.getType().tileWidth();
		int height = b.getType().tileHeight();

		// define the rectangle of the building spot
		// �ǹ� ũ�⺸�� �����¿�� �� ū �簢��
		int startx;
		int starty;
		int endx;
		int endy;

		//buildingGapSpace = 0;@@@@@@
		
		boolean horizontalOnly = false;

		// Refinery �� ��� GapSpace�� üũ�� �ʿ� ����
		if (b.getType().isRefinery())
		{
		}
		// Addon Ÿ���� �ǹ��� ��쿡��, �� Addon �ǹ� ���ʿ� whatBuilds �ǹ��� �ִ����� üũ�Ѵ�
		if (b.getType().isAddon())
		{
			final UnitType builderType = b.getType().whatBuilds().first;

			TilePosition builderTile = new TilePosition(position.getX() - builderType.tileWidth(), position.getY() + 2 - builderType.tileHeight());

			startx = builderTile.getX() - buildingGapSpace;
			starty = builderTile.getY() - buildingGapSpace;
			endx = position.getX() + width + buildingGapSpace;
			endy = position.getY() + height + buildingGapSpace;

			// builderTile�� Lifted �ǹ��� �ƴϰ� whatBuilds �ǹ��� �ƴ� �ǹ��� �ִ��� üũ
			for (int i = 0; i <= builderType.tileWidth(); ++i)
			{
				for (int j = 0; j <= builderType.tileHeight(); ++j)
				{
					for (Unit unit : MyBotModule.Broodwar.getUnitsOnTile(builderTile.getX() + i, builderTile.getY() + j))
					{
						if ((unit.getType() != builderType) && (!unit.isLifted()))
						{
							return false;
						}
					}
				}
			}
		}
		else 
		{
			//make sure we leave space for add-ons. These types of units can have addon:
			if (b.getType() == UnitType.Terran_Command_Center ||
				b.getType() == UnitType.Terran_Factory ||
				b.getType() == UnitType.Terran_Starport ||
				b.getType() == UnitType.Terran_Science_Facility)
			{
				width += 3;
			}

			// �����¿쿡 buildingGapSpace ��ŭ ������ ����
			if (horizontalOnly == false)
			{
				startx = position.getX() - buildingGapSpace;
				starty = position.getY() - buildingGapSpace;
				endx = position.getX() + width + buildingGapSpace;
				endy = position.getY() + height + buildingGapSpace;
			}
			// �¿�θ� buildingGapSpace ��ŭ ������ ����
			else {
				startx = position.getX() - buildingGapSpace;
				starty = position.getY();
				endx = position.getX() + width + buildingGapSpace;
				endy = position.getY() + height;
			}

			// �׶����� �ǹ��� ��� �ٸ� �ǹ��� Addon ������ Ȯ�����ֱ� ����, ���� 2ĭ�� �ݵ�� GapSpace�� �ǵ��� �Ѵ�
			/*if (b.getType().getRace() == Race.Terran) {
				if (buildingGapSpace < 2) {
					startx = position.getX() - 2;
					endx = position.getX() + width + buildingGapSpace;
				}
			}*/

			
			
			// �ǹ��� ������ ���� �� �ƴ϶� ������ buildingGapSpace �������� �� ����ִ���, �Ǽ������� Ÿ������, ����Ǿ��ִ°��� �ƴ���, TilesToAvoid �� �ش����� �ʴ��� üũ
			for (int x = startx; x < endx; x++)
			{
				for (int y = starty; y < endy; y++)
				{
					// if we can't build here, or space is reserved, we can't build here
					if (isBuildableTile(b, x, y) == false)
					{
						//System.out.println("not BuildableTile=========== ");
						return false;
					}

					if (isReservedTile(x, y)) {
						//System.out.println("isReservedTile=========== ");
						return false;
					}

					// ResourceDepot / Addon �ǹ��� �ƴ� �Ϲ� �ǹ��� ���, BaseLocation �� Geyser ���� Ÿ�� (TilesToAvoid) ���� �ǹ��� ���� �ʴ´�
					if (b.getType().isResourceDepot() == false && b.getType().isAddon() == false && b.getType() != UnitType.Terran_Bunker && b.getType() != UnitType.Terran_Missile_Turret) {
						if (isTilesToAvoid(x, y)) {
							return false;
						}
					}
					//���ö��� ������ ���ö��� �ܿ��� ������ ����.
					if (b.getType() != UnitType.Terran_Supply_Depot) {
						if (isTilesToAvoidSupply(x, y)) {
							return false;
						}
					}
					
					if (isTilesToAvoidAbsolute(x, y)) {
						return false;
					}
					
				}
			}
		}

		// if this rectangle doesn't fit on the map we can't build here
		if (startx < 0 || starty < 0 || endx > MyBotModule.Broodwar.mapWidth() || endx < position.getX() + width || endy > MyBotModule.Broodwar.mapHeight())
		{
			return false;
		}

		return true;
	}

	/// �ش� ��ġ�� �ǹ� �Ǽ��� �������� ���θ� �����մϴ� <br>
	/// Broodwar �� canBuildHere �� _reserveMap �� isOverlapsWithBaseLocation �� üũ
	public final boolean canBuildHere(TilePosition position, final ConstructionTask b)
	{
		/*if (!b.type.isRefinery() && !InformationManager::Instance().tileContainsUnit(position))
		{
		return false;
		}*/
		
		// This function checks for creep, power, and resource distance requirements in addition to the tiles' buildability and possible units obstructing the build location.
//		if (!MyBotModule.Broodwar.canBuildHere(position, b.getType(), b.getConstructionWorker()))
		if (!MyBotModule.Broodwar.canBuildHere(position, b.getType()))
		{
			return false;
		}
		
		// check the reserve map
		for (int x = position.getX() ; x < position.getX() + b.getType().tileWidth(); x++)
		{
			for (int y = position.getY() ; y < position.getY() + b.getType().tileHeight(); y++)
			{
				//if (reserveMap.get(x).get(y))
				if (reserveMap[x][y])
				{
					//System.out.println("here is reserveMap =============");
					return false;
				}
			}
		}

		// if it overlaps a base location return false
		// ResourceDepot �ǹ��� �ƴ� �ٸ� �ǹ��� BaseLocation ��ġ�� ���� ���ϵ��� �Ѵ�
		if (isOverlapsWithBaseLocation(position, b.getType()))
		{
			//System.out.println("here isOverlapsWithBaseLocation ============");
			return false;
		}

		return true;
	}

	/// seedPosition ��ó���� Refinery �ǹ� �Ǽ� ���� ��ġ�� Ž���ؼ� �����մϴ� <br>
	/// �������� ���� ���� ���� (Resource_Vespene_Geyser) �� ����Ǿ����� ���� ��(isReservedTile), �ٸ� ���� �ƴ� ��, �̹� Refinery �� �������������� �� ��<br> 
	/// seedPosition �� ���� ����� ���� �����մϴ�
	public final TilePosition getRefineryPositionNear(TilePosition seedPosition)
	{
		if (seedPosition == TilePosition.None || seedPosition == TilePosition.Unknown || seedPosition == TilePosition.Invalid || seedPosition.isValid() == false)
		{
			seedPosition = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition();
		}
		
		TilePosition closestGeyser = TilePosition.None;
		double minGeyserDistanceFromSeedPosition = 100000000;

		//TODO BASICBOT 1.1 ������ ���� ó����.. Ȯ���� ������.
//		for (Unit geyser : MyBotModule.Broodwar.getStaticGeysers())
//		{
//			// geyser->getPosition() �� �ϸ�, Unknown ���� ���� �� �ִ�.
//			// �ݵ�� geyser->getInitialPosition() �� ����ؾ� �Ѵ�
//			Position geyserPos = geyser.getInitialPosition();
//			TilePosition geyserTilePos = geyser.getInitialTilePosition();
//
//			// �̹� ����Ǿ��ִ°�
//			if (isReservedTile(geyserTilePos.getX(), geyserTilePos.getY())) {
//				continue;
//			}
//
//			// geyser->getType() �� �ϸ�, Unknown �̰ų�, Resource_Vespene_Geyser �̰ų�, Terran_Refinery �� ���� �ǹ����� ������, 
//			// �ǹ��� �ı��Ǿ �ڵ����� Resource_Vespene_Geyser �� ���ư��� �ʴ´�
//			// geyser ��ġ�� �ִ� ���ֵ鿡 ���� isRefinery() �� üũ�� �غ��� �Ѵ�
//
//			// seedPosition ���κ��� 16 TILE_SIZE �Ÿ� �̳��� �ִ°�
//			// Fighting Spirit ��ó�� seedPosition ���κ��� ������ �Ÿ� ���� geyser �� ������ ���� �� �ִ� ��� Refinery �ǹ��� ���� ���ؼ��� seedPosition �� ��Ȯ�ϰ� �Է��ؾ� �Ѵ�
//			double thisDistance = geyserTilePos.getDistance(seedPosition);
//			
//			if (thisDistance <= 16 && thisDistance < minGeyserDistanceFromSeedPosition)
//			{
//				minGeyserDistanceFromSeedPosition = thisDistance;
//				closestGeyser = geyser.getInitialTilePosition();
//			}
//		}
		
		// ��ü geyser �߿��� seedPosition ���κ��� 16 TILE_SIZE �Ÿ� �̳��� �ִ� ���� ã�´�
		for (Unit geyser : MyBotModule.Broodwar.getStaticGeysers())
		{
			// geyser->getPosition() �� �ϸ�, Unknown ���� ���� �� �ִ�.
			// �ݵ�� geyser->getInitialPosition() �� ����ؾ� �Ѵ�

			Position geyserPos = geyser.getInitialPosition();
			TilePosition geyserTilePos = geyser.getInitialTilePosition();

			//std::cout << " geyserTilePos " << geyserTilePos.x << "," << geyserTilePos.y << std::endl;

			// �̹� ����Ǿ��ִ°�
			if (isReservedTile(geyserTilePos.getX(), geyserTilePos.getY())) {
				continue;
			}

			// if it is not connected fron seedPosition, it is located in another island
			if (!BWTA.isConnected(seedPosition, geyserTilePos))
			{
				continue;
			}

			// �̹� ������ �ִ°�
			boolean refineryAlreadyBuilt = false;
			List<Unit> alreadyBuiltUnits = MyBotModule.Broodwar.getUnitsInRadius(geyserPos, 4 * Config.TILE_SIZE);
			for (Unit u : alreadyBuiltUnits) {
				if (u.getType().isRefinery() && u.exists()) {
					refineryAlreadyBuilt = true;
				}
			}

			//std::cout << " geyser TilePos is not reserved, is connected, is not refineryAlreadyBuilt" << std::endl;

			if (refineryAlreadyBuilt == false)
			{
				//double thisDistance = BWTA.getGroundDistance(geyserPos.toTilePosition(), seedPosition);

				double thisDistance = MapTools.Instance().getGroundDistance(geyserPos, seedPosition.toPosition());
				
				if (thisDistance < minGeyserDistanceFromSeedPosition)
				{
					//std::cout << " selected " << std::endl;

					minGeyserDistanceFromSeedPosition = thisDistance;
					closestGeyser = geyser.getInitialTilePosition();
				}
			}
		}
		return closestGeyser;
	}

	/// �ش� ��ġ�� BaseLocation �� ��ġ���� ���θ� �����մϴ�<br>
	/// BaseLocation ���� ResourceDepot �ǹ��� �Ǽ��ϰ�, �ٸ� �ǹ��� �Ǽ����� �ʱ� �����Դϴ�
	public final boolean isOverlapsWithBaseLocation(TilePosition tile, UnitType type)
	{
		// if it's a resource depot we don't care if it overlaps
		if (type.isResourceDepot() || type == UnitType.Terran_Barracks  || type == UnitType.Terran_Bunker)
		{
			return false;
		}

		// dimensions of the proposed location
		int tx1 = tile.getX();
		int ty1 = tile.getY();
		int tx2 = tx1 + type.tileWidth();
		int ty2 = ty1 + type.tileHeight();

		// for each base location
		for (BaseLocation base : BWTA.getBaseLocations())
		{
			// dimensions of the base location
			int bx1 = base.getTilePosition().getX();
			int by1 = base.getTilePosition().getY();
			int bx2 = bx1 + InformationManager.Instance().getBasicResourceDepotBuildingType().tileWidth();
			int by2 = by1 + InformationManager.Instance().getBasicResourceDepotBuildingType().tileHeight();

			// conditions for non-overlap are easy
			boolean noOverlap = (tx2 < bx1) || (tx1 > bx2) || (ty2 < by1) || (ty1 > by2);

			// if the reverse is true, return true
			if (!noOverlap)
			{
				return true;
			}
		}

		// otherwise there is no overlap
		return false;
	}

	/// �ǹ� �Ǽ� ���� Ÿ������ ���θ� �����մϴ�
	public final boolean isBuildableTile(final ConstructionTask b, int x, int y)
	{
		TilePosition tp = new TilePosition(x, y);
		if (!tp.isValid())
		{
			return false;
		}

		// �� ������ �Ӹ� �ƴ϶� ���� �����͸� ��� ����ؼ� isBuildable üũ
		//if (BWAPI::Broodwar->isBuildable(x, y) == false)
		if (MyBotModule.Broodwar.isBuildable(x, y, true) == false)
		{
			return false;
		}

		// constructionWorker �̿��� �ٸ� ������ ������ false�� �����Ѵ�
		for (Unit unit : MyBotModule.Broodwar.getUnitsOnTile(x, y))
		{
			if ((b.getConstructionWorker() != null) && (unit != b.getConstructionWorker()))
			{
				return false;
			}
		}

		return true;
	}

	/// �ǹ� �Ǽ� ���� Ÿ�Ϸ� �����ؼ�, �ٸ� �ǹ��� �ߺ��ؼ� ���� �ʵ��� �մϴ�
	public void reserveTiles(TilePosition position, int width, int height)
	{
		/*int rwidth = reserveMap.size();
		int rheight = reserveMap.get(0).size();
		for (int x = position.getX(); x < position.getX() + width && x < rwidth; x++)
		{
			for (int y = position.getY() ; y < position.getY() + height && y < rheight; y++)
			{
				reserveMap.get(x).set(y, true);
				// C++ : reserveMap[x][y] = true;
			}
		}*/
		int rwidth = reserveMap.length;
		int rheight = reserveMap[0].length;
		for (int x = position.getX(); x < position.getX() + width && x < rwidth; x++)
		{
			for (int y = position.getY() ; y < position.getY() + height && y < rheight; y++)
			{
				//reserveMap.get(x).set(y, true);
				reserveMap[x][y] = true;
				// C++ : reserveMap[x][y] = true;
			}
		}
	}
	
	/// �ǹ� �Ǽ� ���� Ÿ�Ϸ� �����ߴ� ���� �����մϴ�
	public void freeTiles(TilePosition position, int width, int height)
	{
		/*int rwidth = reserveMap.size();
		int rheight = reserveMap.get(0).size();

		for (int x = position.getX(); x < position.getX() + width && x < rwidth; x++)
		{
			for (int y = position.getY() ; y < position.getY() + height && y < rheight; y++)
			{
				reserveMap.get(x).set(y, false);
				// C++ : reserveMap[x][y] = false;
			}
		}*/
		int rwidth = reserveMap.length;
		int rheight = reserveMap[0].length;

		for (int x = position.getX(); x < position.getX() + width && x < rwidth; x++)
		{
			for (int y = position.getY() ; y < position.getY() + height && y < rheight; y++)
			{
				//reserveMap.get(x).set(y, false);
				reserveMap[x][y] = false;
				// C++ : reserveMap[x][y] = false;
			}
		}
	}

	// �ǹ� �Ǽ� ����Ǿ��ִ� Ÿ������ üũ
	public final boolean isReservedTile(int x, int y)
	{
		/*int rwidth = reserveMap.size();
		int rheight = reserveMap.get(0).size();
		if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
		{
			return false;
		}

		return reserveMap.get(x).get(y);*/
		int rwidth = reserveMap.length;
		int rheight = reserveMap[0].length;
		if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
		{
			return false;
		}

		return reserveMap[x][y];
	}

	/// reserveMap�� �����մϴ�
	public boolean[][] getReserveMap() {
		return reserveMap;
	}

	/// (x, y) �� BaseLocation �� Mineral / Geyser ������ Ÿ�Ͽ� �ش��ϴ��� ���θ� �����մϴ�
	public final boolean isTilesToAvoid(int x, int y)
	{
		for (TilePosition t : tilesToAvoid) {
			if (t.getX() == x && t.getY() == y) {
				return true;
			}
		}

		return false;
	}
	public final boolean isTilesToAvoidAbsolute(int x, int y)
	{
		for (TilePosition t : tilesToAvoidAbsolute) {
			if (t.getX() == x && t.getY() == y) {
				return true;
			}
		}

		return false;
	}
	
	/// (x, y) �� ���ö��� �����̶�� ������ ����.
	public final boolean isTilesToAvoidSupply(int x, int y)
	{
		for (TilePosition t : tilesToAvoidSupply) {
			if (t.getX() == x && t.getY() == y) {
				return true;
			}
		}

		return false;
	}

	/// BaseLocation �� Mineral / Geyser ������ Ÿ�ϵ��� ã�� _tilesToAvoid �� �����մϴ�<br>
	/// BaseLocation �� Geyser ����, ResourceDepot �ǹ��� Mineral ���� �������� �ǹ� �Ǽ� ��Ҹ� ���ϸ�<br> 
	/// �ϲ� ���ֵ��� ��ֹ��� �Ǿ �Ǽ� ���۵Ǳ���� �ð��� �����ɸ���, ������ �ǹ��� ��ֹ��� �Ǿ �ڿ� ä�� �ӵ��� �������� ������, �� ������ �ǹ��� ���� �ʴ� �������� �α� �����Դϴ�
	public void setTilesToAvoid()
	{
		// ResourceDepot �ǹ��� width = 4 Ÿ��, height = 3 Ÿ��
		// Geyser ��            width = 4 Ÿ��, height = 2 Ÿ��
		// Mineral ��           width = 2 Ÿ��, height = 1 Ÿ��

		for (BaseLocation base : BWTA.getBaseLocations())
		{
			// Island �� ��� �ǹ� ���� ������ ���������� ���� ������ �ǹ� ������ ������ ���� �ʴ´�
			if (base.isIsland()) continue;
			if (BWTA.isConnected(base.getTilePosition(), InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition()) == false) continue;

			// dimensions of the base location
			int bx0 = base.getTilePosition().getX();
			int by0 = base.getTilePosition().getY();
			int bx4 = base.getTilePosition().getX() + 4;
			int by3 = base.getTilePosition().getY() + 3;

			// BaseLocation �� Geyser ������ Ÿ���� BWTA::getShortestPath �� ����ؼ� ���� �� _tilesToAvoid �� �߰�
			for (Unit geyser : base.getGeysers())
			{
				TilePosition closeGeyserPosition = geyser.getInitialTilePosition();

				// dimensions of the closest geyser
				int gx0 = closeGeyserPosition.getX();
				int gy0 = closeGeyserPosition.getY();
				int gx4 = closeGeyserPosition.getX() + 4;
				int gy2 = closeGeyserPosition.getY() + 2;

				for (int i = bx0; i < bx4; i++) {
					for (int j = by0; j < by3; j++) {
						for (int k = gx0; k < gx4; k++) {
							for (int l = gy0; l < gy2; l++) {
								List<TilePosition> tileList = (List<TilePosition>) BWTA.getShortestPath(new TilePosition(i, j), new TilePosition(k, l));
								for (TilePosition t : tileList) {
									tilesToAvoid.add(t);									
								}
							}
						}
					}
				}

				/*
				// Geyser �� Base Location �� ������⿡ �ִ°��� ���� �ּ����� Ÿ�ϸ� �Ǵ��ؼ� tilesToAvoid �� �߰��ϴ� ����� �ִ�
				//
				//    11�ù���   12�ù���  1�ù���
				//
				//     9�ù���             3�ù���
				//
				//     7�ù���    6�ù���  5�ù���
				int whichPosition = 0;

				// dimensions of the tilesToAvoid
				int vx0 = 0;
				int vx1 = 0;
				int vy0 = 0;
				int vy1 = 0;

				// 11�� ����
				if (gx0 < bx0 && gy0 < by0) {
					vx0 = gx0 + 1; // Geyser �� �߾�
					vy0 = gy0;     // Geyser �� ���
					vx1 = bx0 + 3; // ResourceDepot �� �߾�
					vy1 = by0;     // ResourceDepot�� ���
				}
				// 9�� ����
				else if (gx0 < bx0 && gy0 <= by3) {
					vx0 = gx4; // Geyser �� �����ʳ�
					vy0 = gy0; // Geyser �� ���
					vx1 = bx0; // ResourceDepot �� ���ʳ�
					vy1 = gy2; // Geyser �� �ϴ� 
				}
				// 7�� ����
				else if (gx0 < bx0 && gy2 > by3) {
					vx0 = gx0 + 1; // Geyser �� ��� �߾�
					vy0 = by3;     // ResourceDepot �� �ϴ�
					vx1 = bx0 + 3; // ResourceDepot �� �ϴ� �߾�
					vy1 = gy0;     // Geyser �� ���
				}
				// 6�� ����
				else if (gx0 < bx4 && gy0 > by3) {
					vx0 = bx0 + 1; // ResourceDepot �� �ϴ� �߾�
					vy0 = by3;     // ResourceDepot �� �ϴ� 
					vx1 = gx0 + 3; // Geyser �� ��� �߾�
					vy1 = gy0;     // Geyser �� ���
				}
				// 12�� ����
				else if (gx0 < bx4 && gy0 < by0) {
					vx0 = gx0;     // Geyser �� �ϴ� ���ʳ�
					vy0 = gy2; 
					vx1 = gx0 + 3; // Geyser �� �߾�
					vy1 = by0;     // ResourceDepot �� ���
				}
				// 1�� ����
				else if (gx0 > bx0 && gy0 < by0) {
					vx0 = bx0 + 2; // ResourceDepot �� ��� �߾�
					vy0 = gy0 + 1; // Geyser �� �ϴ�
					vx1 = gx0 + 2; // Geyser �� �߾�
					vy1 = by0 + 1; // ResourceDepot �� ���
				}
				// 5�� ����
				else if (gx0 > bx0 && gy0 >= by3) {
					vx0 = bx0 + 2; // ResourceDepot �� �ϴ� �߾�
					vy0 = by0 + 2; // ResourceDepot �� �ϴ�
					vx1 = gx0 + 2; // Geyser �� �߾�
					vy1 = gy0 + 1; // Geyser �� �ϴ�
				}
				// 3�� ����
				else if (gx0 > bx0 && gy0 >= by0) {
					vx0 = bx4; // ResourceDepot �� �����ʳ�
					vy0 = gy0; // Geyser �� ���
					vx1 = gx0; // Geyser �� ���� ��
					vy1 = gy2; // Geyser �� �ϴ�
				}

				for (int i = vx0; i < vx1; i++) {
					for (int j = vy0; j < vy1; j++) {
						_tilesToAvoid.insert(BWAPI::TilePosition(i, j));
					}
				}
				*/

			}

			// BaseLocation �� Mineral ������ Ÿ���� BWTA::getShortestPath �� ����ؼ� ���� �� _tilesToAvoid �� �߰�
			for (Unit mineral : base.getMinerals())
			{
				TilePosition closeMineralPosition = mineral.getInitialTilePosition();

				// dimensions of the closest mineral
				int mx0 = closeMineralPosition.getX();
				int my0 = closeMineralPosition.getY();
				int mx2 = mx0 + 2;
				int my1 = my0 + 1;

				for (int i = bx0; i < bx4; i++) {
					for (int j = by0; j < by3; j++) {
						for (int k = mx0; k < mx2; k++) {
							List<TilePosition> tileList = (List<TilePosition>) BWTA.getShortestPath(new TilePosition(i, j), new TilePosition(k, my0));
							for (TilePosition t : tileList) {
								tilesToAvoid.add(t);								
							}
						}
					}
				}
				if(InformationManager.Instance().enemyRace != Race.Protoss) {
					
					int fromx = mineral.getTilePosition().getX()-2;
					int fromy = mineral.getTilePosition().getY()-2;
					
					for (int x = fromx; x > 0 && x < fromx + 6 && x < MyBotModule.Broodwar.mapWidth(); x++)
				        {
				            for (int y = fromy ; y > 0 && y < fromy + 6 && y < MyBotModule.Broodwar.mapHeight(); y++)
				            {
							TilePosition temp = new TilePosition(x,y);
							tilesToAvoid.add(temp);
						}
					}
				}
			}
		}
	}
	public void setTilesToAvoidSupply() {
		
		int supply_x = BlockingEntrance.Instance().getSupplyPosition().getX();
		int supply_y = BlockingEntrance.Instance().getSupplyPosition().getY();
		
		for(int x = 0; x < 9 ; x++){
			for(int y = 0; y < 8 ; y++){
				TilePosition t = new TilePosition(supply_x+x,supply_y+y);
				tilesToAvoidSupply.add(t);
				//System.out.println("supply region ==>>>>  ("+t.getX()+","+t.getY()+")");
			}
		}
	}

    public void setTilesToAvoidForFirstGas()
	{
		Unit firstgas = InformationManager.Instance().getMyfirstGas();
	
		int fromx = firstgas.getTilePosition().getX()-2;
		int fromy = firstgas.getTilePosition().getY()-2;
		
		for (int x = fromx; x > 0 && x < fromx + 8 && x < MyBotModule.Broodwar.mapWidth(); x++)
	        {
	            for (int y = fromy ; y > 0 && y < fromy + 6 && y < MyBotModule.Broodwar.mapHeight(); y++)
	            {
	            	if(fromx < x && x < fromx+5 && fromy < y && y < fromy+3){
						continue;
					}
				TilePosition temp = new TilePosition(x,y);
				tilesToAvoid.add(temp);
			}
		}
	}
	/// BaseLocation �� Mineral / Geyser ������ Ÿ�ϵ��� ����� �����մϴ�		
	public Set<TilePosition> getTilesToAvoid() {
		return tilesToAvoid;
	}

	public Set<TilePosition> getTilesToAvoidAbsolute() {
		return tilesToAvoidAbsolute;
	}
	
	public void setTilesToAvoidFac(Unit unit) {
		
		int fromx = unit.getTilePosition().getX()-1;
		int fromy = unit.getTilePosition().getY()-1;
		
		/*if(fromx<0){
			fromx=0;
		}
		if(fromy<0){
			fromy =0;
		}*/
		
		for (int x = fromx; x > 0 && x < fromx + 8 && x < MyBotModule.Broodwar.mapWidth(); x++)
	        {
	            for (int y = fromy ; y > 0 && y < fromy + 5 && y < MyBotModule.Broodwar.mapHeight(); y++)
	            {
				if((x==fromx + 6 || x==fromx + 7) && y == fromy){
					continue;
				}
				TilePosition temp = new TilePosition(x,y);
				tilesToAvoidAbsolute.add(temp);
			}
		}
	}
	
	public void setTilesToAvoidAddon(Unit unit) {
		
		int fromx = unit.getTilePosition().getX()+4;
		int fromy = unit.getTilePosition().getY()+1;
		
		for (int x = fromx; x < fromx + 2 && x < MyBotModule.Broodwar.mapWidth(); x++)
		{
			//���丮 �� �ǹ��� ���Ʒ��� ������� �ʿ䰡 ����
			for (int y = fromy ; y < fromy +  2&& y < MyBotModule.Broodwar.mapHeight(); y++)
			{
				TilePosition temp = new TilePosition(x,y);
				tilesToAvoidAbsolute.add(temp);
			}
		}
	}
	
	public void setTilesToAvoidCCAddon(Unit unit) {
		
		int fromx = unit.getTilePosition().getX()+4;
		int fromy = unit.getTilePosition().getY();
		
		for (int x = fromx; x < fromx + 3 && x < MyBotModule.Broodwar.mapWidth(); x++)
		{
			//���丮 �� �ǹ��� ���Ʒ��� ������� �ʿ䰡 ����
			for (int y = fromy ; y < fromy + 3 && y < MyBotModule.Broodwar.mapHeight(); y++)
			{
				TilePosition temp = new TilePosition(x,y);
				tilesToAvoidAbsolute.add(temp);
			}
		}
	}
}