

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;

/// 봇 프로그램 설정
public class NongbongScv {
	//농봉용 변수
	public int NongBong_Chk = 0;
	public int NongBong_Cnt = 0;
	public Unit NongBong_FirstMineral = null;
	public Unit NongBong_LastMineral = null;
	public Unit NongBong_Gas = null;
	public Unit NongBong_Attack = null;
	public Unit NongBong_InfoWorker = null;
	public Unit depot = null;
	public Unit NongBong_At_Unit = null;
	public List<Unit> mineralPatches = null;

	private static NongbongScv instance = new NongbongScv();
	
	public static NongbongScv Instance() {
		return instance;
	}
	

	public void executeNongBong() {
		if (MyBotModule.Broodwar.getFrameCount() % 3 != 0) {
			//System.out.println("tot_mineral_self: "+ tot_mineral_self);
			return;
		}
		
		if(NongBong_Chk < 8){
			//미네랄 클릭 8회 미만 - 8회째는 다음 미네랄 클릭으로 간다
			//System.out.println("mineral Right_Click ==>>> " + NongBong_Chk + " times !!!!!!!!!!!!!");
			int mineral_chk = 0;
			if(NongBong_Chk  == 0){
				// 농봉에 사용할 scv는 미네랄 일꾼 -2
				NongBong_Cnt = WorkerManager.Instance().getNumMineralWorkers() -2;
				//농봉 시작일때 미네랄 / 가스 / 어택 유닛에 대한 정보를 가져온다.
				for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
					NongBong_InfoWorker = worker;
				}
				depot = WorkerManager.Instance().getWorkerData().getWorkerDepot(NongBong_InfoWorker) ;
				//System.out.println("depot ==>>>>>>>> " + depot);
				mineralPatches = WorkerManager.Instance().getWorkerData().getMineralPatchesNearDepot(depot);
				//System.out.println(" 미네랄 개수 =====>>>>  " + mineralPatches.size());
				
				int radius = 320;
				for (Unit unit : MyBotModule.Broodwar.getAllUnits())
				{
					if ((unit.getType() == UnitType.Resource_Mineral_Field) && unit.getDistance(depot) < radius){
						if(mineral_chk == 0){
							//최초 미네랄 발견
							NongBong_FirstMineral = unit;
						}
						//라스트 미네랄은 보일때마다 넣어줌.
						NongBong_LastMineral = unit;
						mineral_chk++;
					}
					if ((unit.getType() == UnitType.Resource_Vespene_Geyser) && unit.getDistance(depot) < radius)
					{
						NongBong_Gas = unit;
					}
				}
				NongBong_Attack = depot;
				
				System.out.println("농봉 변수까지 담았다.");
				//mineral_chk 변수는 여기서 일꾼 마리수 체크용으로 재활용
				
			}
			mineral_chk = 0;
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				//System.out.println("getNumMineralWorkers() ==>>> " + WorkerManager.Instance().getNumMineralWorkers());
				//전체 미네랄 일꾼 -2 만큼만 농봉에 사용하자
				if(NongBong_Cnt > mineral_chk){
					if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Minerals && NongBong_Chk == 0){
						//System.out.println("goto Minerals!!!!!!!!!!!! ==>>>>>>> " + worker.getID());
						CommandUtil.rightClick(worker, NongBong_FirstMineral);
						WorkerManager.Instance().getWorkerData().setWorkerJob(worker, WorkerData.WorkerJob.NongBongM, (Unit)null);
						mineral_chk ++;
					}
					if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.NongBongM && NongBong_Chk != 0){
						//System.out.println("goto Minerals NongBongM!!!!!!!!!!!! ==>>>>>>> " + worker.getID());
						CommandUtil.rightClick(worker, NongBong_FirstMineral);
						mineral_chk ++;
					}
				}
			}
			NongBong_Chk ++;
		}else if(NongBong_Chk == 8){
			//미네랄 한번 더 클릭
			System.out.println("NongBong_Chk == 8 &&  Last Mineral Click !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				//이전에 미네랄로 무브했던 NongBongM 상태 일꾼들 대상
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.NongBongM){
					//최초 미네랄과 반대쪽 미네랄로 간다.
					System.out.println("goto Mineral Last!!!!!!!!!!!! ==>>>>>>> " + worker.getID());
					CommandUtil.rightClick(worker, NongBong_LastMineral);
					WorkerManager.Instance().getWorkerData().setWorkerJob(worker, WorkerData.WorkerJob.NongBongG, (Unit)null);
				}

			}
			NongBong_Chk ++;
		}else if(NongBong_Chk == 9){
			//가스클릭
			System.out.println("NongBong_Chk == 9 &&  Gas Click !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				//이전에 미네랄로 무브했던 NongBongG 상태 일꾼들 대상
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.NongBongG){
					//최초 미네랄과 반대쪽 미네랄로 간다.
					System.out.println("goto Gas!!!!!!!!!!!! ==>>>>>>> " + worker.getID());
					CommandUtil.rightClick(worker, NongBong_Gas);
					WorkerManager.Instance().getWorkerData().setWorkerJob(worker, WorkerData.WorkerJob.NongBongGS, (Unit)null);
				}

			}
			NongBong_Chk ++;
		}else if(NongBong_Chk == 10){
			//가스 쉬프트 우클릭
			System.out.println("NongBong_Chk == 10 &&  Gas Shift Click !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				//이전에 가스로 무브했던 NongBongGS 상태 일꾼들 대상
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.NongBongGS){
					//최초 미네랄과 반대쪽 미네랄로 간다.
					//CommandUtil.rightClick(worker, NongBong_Gas);
					System.out.println("goto Gas Shift!!!!!!!!!!!! ==>>>>>>> " + worker.getID());
					worker.rightClick(NongBong_Gas, true);
					WorkerManager.Instance().getWorkerData().setWorkerJob(worker, WorkerData.WorkerJob.NongBongAT, (Unit)null);
				}

			}
			NongBong_Chk ++;
		}else if(NongBong_Chk == 11){
			//어택 시프트 우클릭
			System.out.println("NongBong_Chk == 11 &&  Attack Shift Click !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				//이전에 가스로 시프트 무브했던 NongBongAT 상태 일꾼들 대상
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.NongBongAT){
					//최초 미네랄과 반대쪽 미네랄로 간다.
					//CommandUtil.rightClick(worker, NongBong_Gas);
					System.out.println("Attack Shift!!!!!!!!!!!! ==>>>>>>> " + worker.getID());
					worker.attack(NongBong_At_Unit, true);
					//WorkerManager.Instance().getWorkerData().setWorkerJob(worker, WorkerData.WorkerJob.NongBongAT, (Unit)null);
				}

			}
			NongBong_Chk = 1000;
		}else if(NongBong_Chk == 1000){
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				//이전에 가스로 시프트 무브했던 NongBongAT 상태 일꾼들 대상
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.NongBongAT){
					System.out.println("NongBongAt ======>>> " + worker.getID());
				}

			}
			NongBong_Chk = 100000;
		}
	}
	
	public void executeMarineIntoBunker() {
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			//System.out.println("tot_mineral_self: "+ tot_mineral_self);
			return;
		}
		
		int haveMarine = 0;
		int haveBunker = 0;
		
		Unit theBunker = null;
		for (Unit unit : MyBotModule.Broodwar.getAllUnits()){
			if(unit.getType() == UnitType.Terran_Bunker){
				haveBunker ++;
				theBunker = unit;
			}
			
			if(unit.getType() == UnitType.Terran_Marine){
				haveMarine ++;
				if(haveBunker > 0 && haveMarine >0){
					theBunker.load(unit);
				}
			}
			
		}
		
	}
	
}