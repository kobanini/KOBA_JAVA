
import java.util.Set;

import bwapi.Color;
import bwapi.UnitType;

/// �� ���α׷� ����
public class Config {
	
	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
	// �� �̸� �� ���� ��� �⺻�� ����

	/// �� �̸�
	public static final String BotName = "NoNameBot";
	/// �� ������ �̸�
	public static final String BotAuthors = "NoName";
	
	
	
	/// �α� ���� �̸�
	public static String LogFilename = BotName + "_LastGameLog.dat";
	/// �б� ���� ���
	public static String ReadDirectory = "bwapi-data\\read\\";
	/// ���� ���� ���
	public static String WriteDirectory = "bwapi-data\\write\\";		

	// BasicBot 1.1 Patch End //////////////////////////////////////////////////	

	
	

	/// ���ÿ��� ������ ������ �� ���ӽ��ǵ� (�ڵ� ���� �� �������� ������ ������ ���� ���� ������ �����)<br>
	/// Speedups for automated play, sets the number of milliseconds bwapi spends in each frame<br>
	/// Fastest: 42 ms/frame.  1�ʿ� 24 frame. �Ϲ������� 1�ʿ� 24frame�� ���� ���Ӽӵ��� �մϴ�<br>
	/// Normal: 67 ms/frame. 1�ʿ� 15 frame<br>
	/// As fast as possible : 0 ms/frame. CPU�� �Ҽ��ִ� ���� ���� �ӵ�.
	public static int SetLocalSpeed = 12;
	
	/// ���ÿ��� ������ ������ �� FrameSkip (�ڵ� ���� �� �������� ������ ������ ���� ���� ������ �����)<br>
	/// frameskip�� �ø��� ȭ�� ǥ�õ� ������Ʈ ���ϹǷ� �ξ� �����ϴ�
    public static int SetFrameSkip = 0;
    
    /// ���ÿ��� ������ ������ �� ����� Ű����/���콺 �Է� ��� ���� (�ڵ� ���� �� �������� ������ ������ ���� ���� ������ �����)	
    public static boolean EnableUserInput = true;
    
    /// ���ÿ��� ������ ������ �� ��ü ������ �� ���̰� �� ������ ���� (�ڵ� ���� �� �������� ������ ������ ���� ���� ������ �����)    
	public static boolean EnableCompleteMapInformation = false;

	
	/// MapGrid ���� �� �� GridCell �� size
	public static int MAP_GRID_SIZE = 32;
	
	/// StarCraft �� BWAPI ���� 1 Tile = 32 * 32 Point (Pixel) �Դϴ�<br>
	/// Position �� Point (Pixel) �����̰�, TilePosition �� Tile �����Դϴ� 
	public static int TILE_SIZE = 32;

	/// ������ Refinery ���� ������ �ϲ� �ִ� ����
	public static int WorkersPerRefinery = 3;
	/// �ǹ��� �ǹ��� ��� �ּ����� ���� - �Ϲ����� �ǹ��� ���
	
	public static int BuildingSpacing = 1;
	/// �ǹ��� �ǹ��� ��� �ּ����� ���� - ResourceDepot �ǹ��� ��� (Nexus, Hatchery, Command Center)
	public static int BuildingResourceDepotSpacing = 1;
	/// �ǹ��� �ǹ��� ��� �ּ����� ���� - Protoss_Pylon �ǹ��� ��� - ���� �ʱ⿡
	public static int BuildingPylonEarlyStageSpacing = 4;
	/// �ǹ��� �ǹ��� ��� �ּ����� ���� - Protoss_Pylon �ǹ��� ��� - ���� �ʱ� ���Ŀ�
	public static int BuildingPylonSpacing = 2;
	/// �ǹ��� �ǹ��� ��� �ּ����� ���� - Terran_Supply_Depot �ǹ��� ���
	public static int BuildingSupplyDepotSpacing = 1;
	/// �ǹ��� �ǹ��� ��� �ּ����� ���� - ��� �ǹ��� ��� (����ĳ��. ��ū�ݷδ�. �������ݷδ�. �ͷ�. ��Ŀ)
	public static int BuildingDefenseTowerSpacing = 1;
	
	
	/*public static int BuildingSpacing = 0;
	/// �ǹ��� �ǹ��� ��� �ּ����� ���� - ResourceDepot �ǹ��� ��� (Nexus, Hatchery, Command Center)
	public static int BuildingResourceDepotSpacing = 0;
	/// �ǹ��� �ǹ��� ��� �ּ����� ���� - Protoss_Pylon �ǹ��� ��� - ���� �ʱ⿡
	public static int BuildingPylonEarlyStageSpacing = 4;
	/// �ǹ��� �ǹ��� ��� �ּ����� ���� - Protoss_Pylon �ǹ��� ��� - ���� �ʱ� ���Ŀ�
	public static int BuildingPylonSpacing = 2;
	/// �ǹ��� �ǹ��� ��� �ּ����� ���� - Terran_Supply_Depot �ǹ��� ���
	public static int BuildingSupplyDepotSpacing = 0;
	/// �ǹ��� �ǹ��� ��� �ּ����� ���� - ��� �ǹ��� ��� (����ĳ��. ��ū�ݷδ�. �������ݷδ�. �ͷ�. ��Ŀ)
	public static int BuildingDefenseTowerSpacing = 0;*/
	
	

	/// ȭ�� ǥ�� ���� - ���� ����
	public static boolean DrawGameInfo = true;
	
	/// ȭ�� ǥ�� ���� - �̳׶�, ����
	public static boolean DrawResourceInfo = false;
	/// ȭ�� ǥ�� ���� - ����
	public static boolean DrawBWTAInfo = true;
	/// ȭ�� ǥ�� ���� - �ٵ���
	public static boolean DrawMapGrid = false;

	/// ȭ�� ǥ�� ���� - ���� HitPoint
	public static boolean DrawUnitHealthBars = true;
	/// ȭ�� ǥ�� ���� - ���� ���
	public static boolean DrawEnemyUnitInfo = true;
	/// ȭ�� ǥ�� ���� - ���� ~ Target �� ����
	public static boolean DrawUnitTargetInfo = true;

	/// ȭ�� ǥ�� ���� - ���� ť
	public static boolean DrawProductionInfo = true;

	/// ȭ�� ǥ�� ���� - �ǹ� Construction ��Ȳ
	public static boolean DrawBuildingInfo = false;
	/// ȭ�� ǥ�� ���� - �ǹ� ConstructionPlace ���� ��Ȳ
	public static boolean DrawReservedBuildingTiles = true;
	
	/// ȭ�� ǥ�� ���� - ���� ����
	public static boolean DrawScoutInfo = true;
	/// ȭ�� ǥ�� ���� - �ϲ� ���
	public static boolean DrawWorkerInfo = false;
	
	/// ȭ�� ǥ�� ���� - ���콺 Ŀ��	
	public static boolean DrawMouseCursorInfo = true;
	
	

	public static final Color ColorLineTarget = Color.White;
	public static final Color ColorLineMineral = Color.Cyan;
	public static final Color ColorUnitNearEnemy = Color.Red;
	public static final Color ColorUnitNotNearEnemy = Color.Green;	

	//���� 0  zergBasic
	//���� 1  protossBasic
	//���� 2  terranBasic                              //0  1 2  3 4 5  6  7 8  9  0 1 2 3 4  5 6 7 8 9 0 
	public static final int[] vultureratio = new int[] {0, 1,0 ,1,1,2 ,1 ,5,1 ,0 ,5,5,1,3,0,10,5,0,0 ,5,0}; //�⺻���� ���� ��, �� vultureratio[0] �� BasicvsZerg ������ ����
	public static final int[] tankratio    = new int[] {2, 5,2 ,5,4,4 ,2 ,6,1 ,1 ,6,5,2,3,2 ,5,8,7,9 ,1,0}; //�⺻���� ��ũ ��
	public static final int[] goliathratio = new int[] {10,2,10,6,3,4 ,10,2,9 ,12,1,0,8,0,0 ,0,1,1,1 ,1,1}; //�⺻���� �񸮾� ��
	public static final int[] wgt          = new int[] {1, 2,1 ,3,1,1 ,1 ,1,3 ,3 ,1,2,3,1,1 ,1,2,3,3 ,2,3}; //�⺻���� �켱���� 1����, 2��ũ, 3�񸮾�

															  // 0 1  2  3 4 5 6 7 8 9 0 1 2  3 4 5 6 7 8 9 0 1 2 3 
	public static final int[] vultureratioexception = new int[] {1,0 ,0 ,7,0,1,0,9,9,8,7,7,10,6,6,1,1,0,0,0,0,0,0,0}; //�������� ���� ��, �� vultureratio[0] �� 
	public static final int[] tankratioexception    = new int[] {4,2 ,2 ,3,0,3,0,3,3,3,3,3,2 ,1,1,3,4,0,0,0,0,0,0,0}; //�������� ��ũ ��
	public static final int[] goliathratioexception = new int[] {1,10,10,2,0,3,0,0,0,1,2,2,0 ,0,0,0,0,0,0,0,0,0,0,0}; //�������� �񸮾� ��
	public static final int[] wgtexception          = new int[] {2,1 ,1 ,1,1,2,1,1,1,1,1,1,1 ,1,1,2,2,0,0,0,0,0,0,0}; //�������� �켱���� 1����, 2��ũ, 3�񸮾�	
	
//	public static final int[] vultureratioexception = new int[] {6,0 ,0 ,7,0 ,6,2,9,9,8,7,7,10,8,3,1,0,0,0,0,0,0,0,0}; //�������� ���� ��, �� vultureratio[0] �� 
//	public static final int[] tankratioexception    = new int[] {0,2 ,2 ,3,2 ,3,2,3,3,3,3,3,2 ,2,3,1,0,0,0,0,0,0,0,0}; //�������� ��ũ ��
//	public static final int[] goliathratioexception = new int[] {6,10,10,2,10,3,8,0,0,1,2,2,0 ,2,7,1,0,0,0,0,0,0,0,0}; //�������� �񸮾� ��
//	public static final int[] wgtexception          = new int[] {1,1 ,1 ,1,1 ,1,1,1,1,1,1,1,1 ,1,1,1,0,0,0,0,0,0,0,0}; //�������� �켱���� 1����, 2��ũ, 3�񸮾�	
	
//0	zergBasic
//	,zergBasic_HydraWave
//	,zergBasic_GiftSet
//	,zergBasic_HydraMutal
//	,zergBasic_LingHydra
//5	,zergBasic_LingLurker
//	,zergBasic_LingMutal
//	,zergBasic_LingUltra
//	,zergBasic_Mutal
//	,zergBasic_MutalMany
//10	,zergBasic_Ultra
//	,protossBasic
//	,protossBasic_Carrier
//	,protossBasic_DoublePhoto
//14,terranBasic
//	,terranBasic_Bionic
//	,terranBasic_Mechanic
//	,terranBasic_MechanicWithWraith
//	,terranBasic_MechanicAfter
//	,terranBasic_BattleCruiser	
//20,AttackIsland
	
	
//0	zergException_FastLurker
//	,zergException_Guardian
//	,zergException_NongBong
//	,zergException_OnLyLing
//	,zergException_PrepareLurker
//5	,zergException_ReverseRush
//	,zergException_HighTech
//	,protossException_CarrierMany
//	,protossException_Dark
//	,protossException_Reaver
//10	,protossException_Scout
//	,protossException_Shuttle
//	,protossException_ShuttleMix
//	,protossException_ReadyToZealot
//	,protossException_ZealotPush
//15	,protossException_ReadyToDragoon
//	,protossException_DragoonPush
//	,protossException_PhotonRush
//	,protossException_DoubleNexus
//	,protossException_Arbiter
//20	,terranException_CheeseRush
//	,terranException_NuClear
//	,terranException_Wraith
//	,Init
}