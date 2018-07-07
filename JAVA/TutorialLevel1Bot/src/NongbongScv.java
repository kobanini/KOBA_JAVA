

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;

/// �� ���α׷� ����
public class NongbongScv {
	//����� ����
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
			//�̳׶� Ŭ�� 8ȸ �̸� - 8ȸ°�� ���� �̳׶� Ŭ������ ����
			//System.out.println("mineral Right_Click ==>>> " + NongBong_Chk + " times !!!!!!!!!!!!!");
			int mineral_chk = 0;
			if(NongBong_Chk  == 0){
				// ����� ����� scv�� �̳׶� �ϲ� -2
				NongBong_Cnt = WorkerManager.Instance().getNumMineralWorkers() -2;
				//��� �����϶� �̳׶� / ���� / ���� ���ֿ� ���� ������ �����´�.
				for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
					NongBong_InfoWorker = worker;
				}
				depot = WorkerManager.Instance().getWorkerData().getWorkerDepot(NongBong_InfoWorker) ;
				//System.out.println("depot ==>>>>>>>> " + depot);
				mineralPatches = WorkerManager.Instance().getWorkerData().getMineralPatchesNearDepot(depot);
				//System.out.println(" �̳׶� ���� =====>>>>  " + mineralPatches.size());
				
				int radius = 320;
				for (Unit unit : MyBotModule.Broodwar.getAllUnits())
				{
					if ((unit.getType() == UnitType.Resource_Mineral_Field) && unit.getDistance(depot) < radius){
						if(mineral_chk == 0){
							//���� �̳׶� �߰�
							NongBong_FirstMineral = unit;
						}
						//��Ʈ �̳׶��� ���϶����� �־���.
						NongBong_LastMineral = unit;
						mineral_chk++;
					}
					if ((unit.getType() == UnitType.Resource_Vespene_Geyser) && unit.getDistance(depot) < radius)
					{
						NongBong_Gas = unit;
					}
				}
				NongBong_Attack = depot;
				
				System.out.println("��� �������� ��Ҵ�.");
				//mineral_chk ������ ���⼭ �ϲ� ������ üũ������ ��Ȱ��
				
			}
			mineral_chk = 0;
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				//System.out.println("getNumMineralWorkers() ==>>> " + WorkerManager.Instance().getNumMineralWorkers());
				//��ü �̳׶� �ϲ� -2 ��ŭ�� ����� �������
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
			//�̳׶� �ѹ� �� Ŭ��
			System.out.println("NongBong_Chk == 8 &&  Last Mineral Click !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				//������ �̳׶��� �����ߴ� NongBongM ���� �ϲ۵� ���
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.NongBongM){
					//���� �̳׶��� �ݴ��� �̳׶��� ����.
					System.out.println("goto Mineral Last!!!!!!!!!!!! ==>>>>>>> " + worker.getID());
					CommandUtil.rightClick(worker, NongBong_LastMineral);
					WorkerManager.Instance().getWorkerData().setWorkerJob(worker, WorkerData.WorkerJob.NongBongG, (Unit)null);
				}

			}
			NongBong_Chk ++;
		}else if(NongBong_Chk == 9){
			//����Ŭ��
			System.out.println("NongBong_Chk == 9 &&  Gas Click !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				//������ �̳׶��� �����ߴ� NongBongG ���� �ϲ۵� ���
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.NongBongG){
					//���� �̳׶��� �ݴ��� �̳׶��� ����.
					System.out.println("goto Gas!!!!!!!!!!!! ==>>>>>>> " + worker.getID());
					CommandUtil.rightClick(worker, NongBong_Gas);
					WorkerManager.Instance().getWorkerData().setWorkerJob(worker, WorkerData.WorkerJob.NongBongGS, (Unit)null);
				}

			}
			NongBong_Chk ++;
		}else if(NongBong_Chk == 10){
			//���� ����Ʈ ��Ŭ��
			System.out.println("NongBong_Chk == 10 &&  Gas Shift Click !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				//������ ������ �����ߴ� NongBongGS ���� �ϲ۵� ���
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.NongBongGS){
					//���� �̳׶��� �ݴ��� �̳׶��� ����.
					//CommandUtil.rightClick(worker, NongBong_Gas);
					System.out.println("goto Gas Shift!!!!!!!!!!!! ==>>>>>>> " + worker.getID());
					worker.rightClick(NongBong_Gas, true);
					WorkerManager.Instance().getWorkerData().setWorkerJob(worker, WorkerData.WorkerJob.NongBongAT, (Unit)null);
				}

			}
			NongBong_Chk ++;
		}else if(NongBong_Chk == 11){
			//���� ����Ʈ ��Ŭ��
			System.out.println("NongBong_Chk == 11 &&  Attack Shift Click !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				//������ ������ ����Ʈ �����ߴ� NongBongAT ���� �ϲ۵� ���
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.NongBongAT){
					//���� �̳׶��� �ݴ��� �̳׶��� ����.
					//CommandUtil.rightClick(worker, NongBong_Gas);
					System.out.println("Attack Shift!!!!!!!!!!!! ==>>>>>>> " + worker.getID());
					worker.attack(NongBong_At_Unit, true);
					//WorkerManager.Instance().getWorkerData().setWorkerJob(worker, WorkerData.WorkerJob.NongBongAT, (Unit)null);
				}

			}
			NongBong_Chk = 1000;
		}else if(NongBong_Chk == 1000){
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				//������ ������ ����Ʈ �����ߴ� NongBongAT ���� �ϲ۵� ���
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