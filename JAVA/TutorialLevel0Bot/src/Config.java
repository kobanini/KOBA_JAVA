
import java.util.Set;

import bwapi.Color;
import bwapi.UnitType;

/// 봇 프로그램 설정
public class Config {
	
	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
	// 봇 이름 및 파일 경로 기본값 변경

	/// 봇 이름
	public static final String BotName = "NoNameBot";
	/// 봇 개발자 이름
	public static final String BotAuthors = "NoName";
	
	
	
	/// 로그 파일 이름
	public static String LogFilename = BotName + "_LastGameLog.dat";
	/// 읽기 파일 경로
	public static String ReadDirectory = "bwapi-data\\read\\";
	/// 쓰기 파일 경로
	public static String WriteDirectory = "bwapi-data\\write\\";		

	// BasicBot 1.1 Patch End //////////////////////////////////////////////////	

	
	

	/// 로컬에서 게임을 실행할 때 게임스피드 (코드 제출 후 서버에서 게임을 실행할 때는 서버 설정을 사용함)<br>
	/// Speedups for automated play, sets the number of milliseconds bwapi spends in each frame<br>
	/// Fastest: 42 ms/frame.  1초에 24 frame. 일반적으로 1초에 24frame을 기준 게임속도로 합니다<br>
	/// Normal: 67 ms/frame. 1초에 15 frame<br>
	/// As fast as possible : 0 ms/frame. CPU가 할수있는 가장 빠른 속도.
	public static int SetLocalSpeed = 12;
	
	/// 로컬에서 게임을 실행할 때 FrameSkip (코드 제출 후 서버에서 게임을 실행할 때는 서버 설정을 사용함)<br>
	/// frameskip을 늘리면 화면 표시도 업데이트 안하므로 훨씬 빠릅니다
    public static int SetFrameSkip = 0;
    
    /// 로컬에서 게임을 실행할 때 사용자 키보드/마우스 입력 허용 여부 (코드 제출 후 서버에서 게임을 실행할 때는 서버 설정을 사용함)	
    public static boolean EnableUserInput = true;
    
    /// 로컬에서 게임을 실행할 때 전체 지도를 다 보이게 할 것인지 여부 (코드 제출 후 서버에서 게임을 실행할 때는 서버 설정을 사용함)    
	public static boolean EnableCompleteMapInformation = false;

	
	/// MapGrid 에서 한 개 GridCell 의 size
	public static int MAP_GRID_SIZE = 32;
	
	/// StarCraft 및 BWAPI 에서 1 Tile = 32 * 32 Point (Pixel) 입니다<br>
	/// Position 은 Point (Pixel) 단위이고, TilePosition 은 Tile 단위입니다 
	public static int TILE_SIZE = 32;

	/// 각각의 Refinery 마다 투입할 일꾼 최대 숫자
	public static int WorkersPerRefinery = 3;
	/// 건물과 건물간 띄울 최소한의 간격 - 일반적인 건물의 경우
	
	public static int BuildingSpacing = 1;
	/// 건물과 건물간 띄울 최소한의 간격 - ResourceDepot 건물의 경우 (Nexus, Hatchery, Command Center)
	public static int BuildingResourceDepotSpacing = 1;
	/// 건물과 건물간 띄울 최소한의 간격 - Protoss_Pylon 건물의 경우 - 게임 초기에
	public static int BuildingPylonEarlyStageSpacing = 4;
	/// 건물과 건물간 띄울 최소한의 간격 - Protoss_Pylon 건물의 경우 - 게임 초기 이후에
	public static int BuildingPylonSpacing = 2;
	/// 건물과 건물간 띄울 최소한의 간격 - Terran_Supply_Depot 건물의 경우
	public static int BuildingSupplyDepotSpacing = 1;
	/// 건물과 건물간 띄울 최소한의 간격 - 방어 건물의 경우 (포톤캐논. 성큰콜로니. 스포어콜로니. 터렛. 벙커)
	public static int BuildingDefenseTowerSpacing = 1;
	
	
	/*public static int BuildingSpacing = 0;
	/// 건물과 건물간 띄울 최소한의 간격 - ResourceDepot 건물의 경우 (Nexus, Hatchery, Command Center)
	public static int BuildingResourceDepotSpacing = 0;
	/// 건물과 건물간 띄울 최소한의 간격 - Protoss_Pylon 건물의 경우 - 게임 초기에
	public static int BuildingPylonEarlyStageSpacing = 4;
	/// 건물과 건물간 띄울 최소한의 간격 - Protoss_Pylon 건물의 경우 - 게임 초기 이후에
	public static int BuildingPylonSpacing = 2;
	/// 건물과 건물간 띄울 최소한의 간격 - Terran_Supply_Depot 건물의 경우
	public static int BuildingSupplyDepotSpacing = 0;
	/// 건물과 건물간 띄울 최소한의 간격 - 방어 건물의 경우 (포톤캐논. 성큰콜로니. 스포어콜로니. 터렛. 벙커)
	public static int BuildingDefenseTowerSpacing = 0;*/
	
	

	/// 화면 표시 여부 - 게임 정보
	public static boolean DrawGameInfo = true;
	
	/// 화면 표시 여부 - 미네랄, 가스
	public static boolean DrawResourceInfo = false;
	/// 화면 표시 여부 - 지도
	public static boolean DrawBWTAInfo = true;
	/// 화면 표시 여부 - 바둑판
	public static boolean DrawMapGrid = false;

	/// 화면 표시 여부 - 유닛 HitPoint
	public static boolean DrawUnitHealthBars = true;
	/// 화면 표시 여부 - 유닛 통계
	public static boolean DrawEnemyUnitInfo = true;
	/// 화면 표시 여부 - 유닛 ~ Target 간 직선
	public static boolean DrawUnitTargetInfo = true;

	/// 화면 표시 여부 - 빌드 큐
	public static boolean DrawProductionInfo = true;

	/// 화면 표시 여부 - 건물 Construction 상황
	public static boolean DrawBuildingInfo = false;
	/// 화면 표시 여부 - 건물 ConstructionPlace 예약 상황
	public static boolean DrawReservedBuildingTiles = true;
	
	/// 화면 표시 여부 - 정찰 상태
	public static boolean DrawScoutInfo = true;
	/// 화면 표시 여부 - 일꾼 목록
	public static boolean DrawWorkerInfo = false;
	
	/// 화면 표시 여부 - 마우스 커서	
	public static boolean DrawMouseCursorInfo = true;
	
	

	public static final Color ColorLineTarget = Color.White;
	public static final Color ColorLineMineral = Color.Cyan;
	public static final Color ColorUnitNearEnemy = Color.Red;
	public static final Color ColorUnitNotNearEnemy = Color.Green;	

	//전략 0  zergBasic
	//전략 1  protossBasic
	//전략 2  terranBasic                              //0  1 2  3 4 5  6  7 8  9  0 1 2 3 4  5 6 7 8 9 0 
	public static final int[] vultureratio = new int[] {0, 1,0 ,1,1,2 ,1 ,5,1 ,0 ,5,5,1,3,0,10,5,0,0 ,5,0}; //기본전략 벌쳐 비, 예 vultureratio[0] 은 BasicvsZerg 에서의 비율
	public static final int[] tankratio    = new int[] {2, 5,2 ,5,4,4 ,2 ,6,1 ,1 ,6,5,2,3,2 ,5,8,7,9 ,1,0}; //기본전략 탱크 비
	public static final int[] goliathratio = new int[] {10,2,10,6,3,4 ,10,2,9 ,12,1,0,8,0,0 ,0,1,1,1 ,1,1}; //기본전략 골리앗 비
	public static final int[] wgt          = new int[] {1, 2,1 ,3,1,1 ,1 ,1,3 ,3 ,1,2,3,1,1 ,1,2,3,3 ,2,3}; //기본전략 우선순위 1벌쳐, 2탱크, 3골리앗

															  // 0 1  2  3 4 5 6 7 8 9 0 1 2  3 4 5 6 7 8 9 0 1 2 3 
	public static final int[] vultureratioexception = new int[] {1,0 ,0 ,7,0,1,0,9,9,8,7,7,10,6,6,1,1,0,0,0,0,0,0,0}; //예외전략 벌쳐 비, 예 vultureratio[0] 은 
	public static final int[] tankratioexception    = new int[] {4,2 ,2 ,3,0,3,0,3,3,3,3,3,2 ,1,1,3,4,0,0,0,0,0,0,0}; //예외전략 탱크 비
	public static final int[] goliathratioexception = new int[] {1,10,10,2,0,3,0,0,0,1,2,2,0 ,0,0,0,0,0,0,0,0,0,0,0}; //예외전략 골리앗 비
	public static final int[] wgtexception          = new int[] {2,1 ,1 ,1,1,2,1,1,1,1,1,1,1 ,1,1,2,2,0,0,0,0,0,0,0}; //예외전략 우선순위 1벌쳐, 2탱크, 3골리앗	
	
//	public static final int[] vultureratioexception = new int[] {6,0 ,0 ,7,0 ,6,2,9,9,8,7,7,10,8,3,1,0,0,0,0,0,0,0,0}; //예외전략 벌쳐 비, 예 vultureratio[0] 은 
//	public static final int[] tankratioexception    = new int[] {0,2 ,2 ,3,2 ,3,2,3,3,3,3,3,2 ,2,3,1,0,0,0,0,0,0,0,0}; //예외전략 탱크 비
//	public static final int[] goliathratioexception = new int[] {6,10,10,2,10,3,8,0,0,1,2,2,0 ,2,7,1,0,0,0,0,0,0,0,0}; //예외전략 골리앗 비
//	public static final int[] wgtexception          = new int[] {1,1 ,1 ,1,1 ,1,1,1,1,1,1,1,1 ,1,1,1,0,0,0,0,0,0,0,0}; //예외전략 우선순위 1벌쳐, 2탱크, 3골리앗	
	
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