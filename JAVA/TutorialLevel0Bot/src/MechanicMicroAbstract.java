

import java.util.List;

import bwapi.Unit;

public abstract class MechanicMicroAbstract {
	abstract public void prepareMechanic(SquadOrder order, List<UnitInfo> enemiesInfo);
	abstract public void executeMechanicMicro(Unit unit);
}
