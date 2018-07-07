
import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

/// ���� �����α׷��� ��ü�� �Ǵ� class<br>
/// ��Ÿũ����Ʈ ��� ���� �߻��ϴ� �̺�Ʈ���� �����ϰ� ó���ǵ��� �ش� Manager ��ü���� �̺�Ʈ�� �����ϴ� ������ Controller ������ �մϴ�
public class GameCommander {

	/// ������ �÷��� : ��� Manager �� ������ ����Ű���� �˱����� �÷���
	private boolean isToFindError = false;
	private LagObserver logObserver = new LagObserver();
	public boolean scoutFlag = false;
	
	private static GameCommander instance = new GameCommander();
	
	/// static singleton ��ü�� �����մϴ�
	public static GameCommander Instance() {
		return instance;
	}
	
	/// ��Ⱑ ���۵� �� ��ȸ������ �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onStart() 
	{
		TilePosition startLocation = MyBotModule.Broodwar.self().getStartLocation();
		if (startLocation == TilePosition.None || startLocation == TilePosition.Unknown) {
			return;
		}
		StrategyManager.Instance().onStart();
	}

	/// ��Ⱑ ����� �� ��ȸ������ �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onEnd(boolean isWinner)
	{
		StrategyManager.Instance().onEnd(isWinner);
	}

	/// ��� ���� �� �� �����Ӹ��� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onFrame()
	{
		
		if (MyBotModule.Broodwar.isPaused()
			|| MyBotModule.Broodwar.self() == null || MyBotModule.Broodwar.self().isDefeated() || MyBotModule.Broodwar.self().leftGame()
			|| MyBotModule.Broodwar.enemy() == null || MyBotModule.Broodwar.enemy().isDefeated() || MyBotModule.Broodwar.enemy().leftGame()) 
		{
			return;
		}

		try {
			logObserver.start();
			if ( isToFindError) System.out.print("(a");

			// �Ʊ� ���̽� ��ġ. ���� ���̽� ��ġ. �� ���ֵ��� �������� ���� Map �ڷᱸ���� ����/������Ʈ
			InformationManager.Instance().update();
			if ( isToFindError) System.out.print("b");
		
			// �� ������ ��ġ�� ��ü MapGrid �ڷᱸ���� ����
			MapGrid.Instance().update();
			if ( isToFindError) System.out.print("c");

			// economy and base managers
			// �ϲ� ���ֿ� ���� ��� (�ڿ� ä��, �̵� ����) ���� �� ����
			WorkerManager.Instance().update();
			if ( isToFindError) System.out.print("d");
			
			// �������ť�� �����ϸ�, ��������� ���� ���� ����(���� �Ʒ�, ��ũ ���׷��̵� ��)�� �����Ѵ�.
			BuildManager.Instance().update();
			if ( isToFindError) System.out.print("e");

			// ������� �� �ǹ� ���忡 ���ؼ���, �ϲ����� ����, ��ġ����, �Ǽ� �ǽ�, �ߴܵ� �ǹ� ���� �簳�� �����Ѵ�
			ConstructionManager.Instance().update();
			if ( isToFindError) System.out.print("f");

			// ���� �ʱ� ���� ���� ���� �� ���� ���� ��Ʈ���� �����Ѵ�
			ScoutManager.Instance().update();
			if ( isToFindError) System.out.print("g");

			// ������ �Ǵ� �� ���� ��Ʈ��
			StrategyManager.Instance().update();
			if ( isToFindError) System.out.print("h");
			
			CombatManager.Instance().update();
			if ( isToFindError) System.out.print("i)");
			
			logObserver.observe();

		} catch (Exception e) {
			e.printStackTrace();
		}		
	}


	/// ����(�ǹ�/��������/��������)�� Create �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onUnitCreate(Unit unit) { 
		InformationManager.Instance().onUnitCreate(unit);
		
		if(unit.getType() == UnitType.Terran_Command_Center  && unit.getPlayer() == InformationManager.Instance().selfPlayer ){
			ConstructionPlaceFinder.Instance().setTilesToAvoidCCAddon(unit);
		}
		if((unit.getType() == UnitType.Terran_Factory||unit.getType() == UnitType.Terran_Starport ||unit.getType() == UnitType.Terran_Science_Facility)  && unit.getPlayer() == InformationManager.Instance().selfPlayer ){
			ConstructionPlaceFinder.Instance().setTilesToAvoidFac(unit);
		}
	}

	///  ����(�ǹ�/��������/��������)�� Destroy �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onUnitDestroy(Unit unit) {
		// ResourceDepot �� Worker �� ���� ó��
		WorkerManager.Instance().onUnitDestroy(unit);

		InformationManager.Instance().onUnitDestroy(unit); 
	}
	
	/// ����(�ǹ�/��������/��������)�� Morph �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�<br>
	/// Zerg ������ ������ �ǹ� �Ǽ��̳� ��������/�������� ���꿡�� ���� ��κ� Morph ���·� ����˴ϴ�
	public void onUnitMorph(Unit unit) { 
		InformationManager.Instance().onUnitMorph(unit);

		// Zerg ���� Worker �� Morph �� ���� ó��
		//WorkerManager.Instance().onUnitMorph(unit);
	}

	/// ����(�ǹ�/��������/��������)�� �Ҽ� �÷��̾ �ٲ� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�<br>
	/// Gas Geyser�� � �÷��̾ Refinery �ǹ��� �Ǽ����� ��, Refinery �ǹ��� �ı��Ǿ��� ��, Protoss ���� Dark Archon �� Mind Control �� ���� �Ҽ� �÷��̾ �ٲ� �� �߻��մϴ�
	public void onUnitRenegade(Unit unit) {
		// Vespene_Geyser (���� ����) �� �������� �Ǽ��� ���� ���
		//MyBotModule.Broodwar.sendText("A %s [%p] has renegaded. It is now owned by %s", unit.getType().c_str(), unit, unit.getPlayer().getName().c_str());

		InformationManager.Instance().onUnitRenegade(unit);
	}
	
	// �ϲ� ź��/�ı� � ���� ������Ʈ ���� ���� ���� : onUnitShow �� �ƴ϶� onUnitComplete ���� ó���ϵ��� ����
	/// ����(�ǹ�/��������/��������)�� �ϴ� �� (�ǹ� �Ǽ�, ���׷��̵�, �������� �Ʒ� ��)�� ������ �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onUnitComplete(Unit unit)
	{
		InformationManager.Instance().onUnitComplete(unit);
		// ResourceDepot �� Worker �� ���� ó��
		WorkerManager.Instance().onUnitComplete(unit);
		
		if(unit.getType() == UnitType.Terran_Command_Center  && unit.getPlayer() == InformationManager.Instance().selfPlayer ){
			
			if(CombatManager.Instance().getClosestMineral(unit)!=null){
				unit.setRallyPoint(CombatManager.Instance().getClosestMineral(unit));
			}
		}
		
		if(unit.getType() == UnitType.Terran_Barracks  && unit.getPlayer() == InformationManager.Instance().selfPlayer ){
			
			if (InformationManager.Instance().enemyRace == Race.Protoss || InformationManager.Instance().enemyRace == Race.Terran) {
				unit.lift();
//				for (Unit myUnit : MyBotModule.Broodwar.self().getUnits())
//				{
					
//					if (myUnit.getType() == UnitType.Terran_Command_Center && myUnit.isCompleted())
//					{
//						unit.setRallyPoint(new Position( (unit.getX()*7 + myUnit.getX())/8, (unit.getY()*7 + myUnit.getY())/8));
//					}
//				}
			}
			else{
				for (Unit myUnit : MyBotModule.Broodwar.self().getUnits())
				{
					if (myUnit.getType() == UnitType.Terran_Command_Center && myUnit.isCompleted())
					{
						if(CombatManager.Instance().getBestPosition(myUnit)==null){
						}else{
							unit.setRallyPoint(CombatManager.Instance().getBestPosition(myUnit));
						}
					}
				}
			}
		}
		
		if(unit.getType() == UnitType.Terran_Bunker && unit.getPlayer() == InformationManager.Instance().selfPlayer ){
			for (Unit myUnit : MyBotModule.Broodwar.self().getUnits())
			{
				if ((myUnit.getType() == UnitType.Terran_Marine) && myUnit.isCompleted())
				{
					CommandUtil.attackMove(myUnit, unit.getPosition());
				}
				if(myUnit.getType() == UnitType.Terran_Barracks && unit.isCompleted()){
					myUnit.setRallyPoint(unit.getPosition());
				}
			}
		}
		
		
		
		
	}

	/// ����(�ǹ�/��������/��������)�� Discover �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�<br>
	/// �Ʊ� ������ Create �Ǿ��� �� ��簡, ���� ������ Discover �Ǿ��� �� �߻��մϴ�
	public void onUnitDiscover(Unit unit) {
	}

	/// ����(�ǹ�/��������/��������)�� Evade �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�<br>
	/// ������ Destroy �� �� �߻��մϴ�
	public void onUnitEvade(Unit unit) {
	}	

	// �ϲ� ź��/�ı� � ���� ������Ʈ ���� ���� ���� : onUnitShow �� �ƴ϶� onUnitComplete ���� ó���ϵ��� ����
	/// ����(�ǹ�/��������/��������)�� Show �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�<br>
	/// �Ʊ� ������ Create �Ǿ��� �� ��簡, ���� ������ Discover �Ǿ��� �� �߻��մϴ�
	public void onUnitShow(Unit unit) { 
		InformationManager.Instance().onUnitShow(unit); 
		
		// ResourceDepot �� Worker �� ���� ó��
		//WorkerManager.Instance().onUnitShow(unit);
	}

	/// ����(�ǹ�/��������/��������)�� Hide �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�<br>
	/// ���̴� ������ Hide �� �� �߻��մϴ�
	public void onUnitHide(Unit unit) {
		InformationManager.Instance().onUnitHide(unit); 
	}

	// onNukeDetect, onPlayerLeft, onSaveGame �̺�Ʈ�� ó���� �� �ֵ��� �޼ҵ� �߰�

	/// �ٹ̻��� �߻簡 �����Ǿ��� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onNukeDetect(Position target){
	}

	/// �ٸ� �÷��̾ ����� ������ �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onPlayerLeft(Player player){
	}

	/// ������ ������ �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onSaveGame(String gameName){
	}		

	/// �ؽ�Ʈ�� �Է� �� ���͸� �Ͽ� �ٸ� �÷��̾�鿡�� �ؽ�Ʈ�� �����Ϸ� �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onSendText(String text){
	}

	/// �ٸ� �÷��̾�κ��� �ؽ�Ʈ�� ���޹޾��� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onReceiveText(Player player, String text){
	}

	public boolean getScoutFlag(){ 
		return scoutFlag;
	}

}