
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;

import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.UnitType;
import bwapi.UpgradeType;

/// ���� ���� ��� �ڷᱸ�� class
public class BuildOrderQueue {
	
	private enum SeedPositionStrategy { MainBaseLocation, MainBaseBackYard, FirstChokePoint, FirstExpansionLocation, SecondChokePoint, SeedPositionSpecified 
		,NextExpansionPoint ,NextSupplePoint ,LastBuilingPoint ,getLastBuilingFinalLocation};
	private SeedPositionStrategy seedLocationStrategy;

	private int highestPriority;
	private int lowestPriority;
	private int defaultPrioritySpacing;

	/// iteration �� �ϱ� ���� ����<br>
	/// highest priority �� BuildOrderItem ���κ��� ��� skip �ߴ°�. 
	private int numSkippedItems;
	
	/// BuildOrderItem ���� Double Ended Queue �ڷᱸ���� �����մϴ�<br>
	/// lowest priority �� BuildOrderItem�� front ��, highest priority �� BuildOrderItem �� back �� ��ġ�ϰ� �մϴ�
	private Deque<BuildOrderItem> queue = new ArrayDeque<BuildOrderItem>();
	
	public BuildOrderQueue()
	{
		highestPriority = 0; 
		lowestPriority = 0;
		defaultPrioritySpacing = 10;
		numSkippedItems = 0;
	}

	/// clears the entire build order queue
	public void clearAll() 
	{
		// clear the queue
		queue.clear();

		// reset the priorities
		highestPriority = 0;
		lowestPriority = 0;
	}

	/// returns the highest priority item
	public BuildOrderItem getHighestPriorityItem() 
	{
		// reset the number of skipped items to zero
		numSkippedItems = 0;

		// the queue will be sorted with the highest priority at the back
		// C ������ highest �� back �� ������, JAVA ������ highest �� fist �� �ִ� 
		return queue.getFirst();   //  queue.back(); C++
	}

	/// returns the highest priority item
	public BuildOrderItem getItem() 
	{
		// TODO : assert ����
		//assert(queue.size() - 1 - numSkippedItems >= 0);

		// the queue will be sorted with the highest priority at the back
		Object[] tempArr = queue.toArray();
		
		//return (BuildOrderItem)tempArr[queue.size() - 1 - numSkippedItems];
		return (BuildOrderItem)tempArr[numSkippedItems];
	}

	public boolean canGetNextItem() 
	{
		// does the queue have more elements
		boolean bigEnough = queue.size() > (int)(1 + numSkippedItems);

		if (!bigEnough) 
		{
			return false;
		}

		// this tells us if we can skip
		return true;
	}
	public void PointToNextItem() 
	{
		// make sure we can skip
		if (canGetNextItem()) {
			// skip it
			numSkippedItems++;
		}
	}
	
	/// BuildOrderQueue�� �ش� type �� Item �� �� �� �����ϴ��� �����Ѵ�. queryTilePosition �� �Է��� ���, �ǹ��� ���ؼ� �߰� Ž���Ѵ�
	public int getItemCount(MetaType queryType, TilePosition queryTilePosition)
	{
		// queryTilePosition �� �Է��� ���, �Ÿ��� maxRange. Ÿ�ϴ���
		int maxRange = 16;
		
		int itemCount = 0;

		int reps = queue.size();
		
		Object[] tempArr = queue.toArray();
		
		// for each unit in the queue
		for (int i = 0; i<reps; i++) {
					
			final MetaType item = ((BuildOrderItem)tempArr[queue.size() - 1 - i]).metaType;
			TilePosition itemPosition = ((BuildOrderItem)tempArr[queue.size() - 1 - i]).seedLocation;
			Point seedPositionPoint = null;
			if(queryTilePosition != null)
			{
				seedPositionPoint = new Point(queryTilePosition.getX(), queryTilePosition.getY());
			}
			else
			{
				queryTilePosition = TilePosition.None;
			}

			if (queryType.isUnit() && item.isUnit()) {
				//if (item.getUnitType().getID() == queryType.getUnitType().getID()) {
				if (item.getUnitType() == queryType.getUnitType()) 
				{
					if (queryType.getUnitType().isBuilding() && queryTilePosition != TilePosition.None)
					{
						if (itemPosition.getDistance(new TilePosition((int)seedPositionPoint.getX(), (int)seedPositionPoint.getY())) <= maxRange){
							itemCount++;
						}
					}
					else 
					{
						itemCount++;
					}
				}
			}
			else if (queryType.isTech() && item.isTech()) {
				//if (item.getTechType().getID() == queryType.getTechType().getID()) {
				if (item.getTechType() == queryType.getTechType()) {
					itemCount++;
				}
			}
			else if (queryType.isUpgrade() && item.isUpgrade()) {
				//if (type.getUpgradeType().getID() == queryType.getUpgradeType().getID()) {
				if (item.getUpgradeType() == queryType.getUpgradeType()) {
					itemCount++;
				}
			}
		}
		return itemCount;
	}

	
	public int getItemCountNear(UnitType unitType, TilePosition queryTilePosition, int range)
	{
		return getItemCountNear(new MetaType(unitType), queryTilePosition, range);
	}
	
	
	public int getItemCountNear(MetaType queryType, TilePosition queryTilePosition, int range)
	{
		// queryTilePosition �� �Է��� ���, �Ÿ��� maxRange. Ÿ�ϴ���
		int maxRange = range;
		
		int itemCount = 0;

		int reps = queue.size();
		
		Object[] tempArr = queue.toArray();
		
		// for each unit in the queue
		for (int i = 0; i<reps; i++) {
					
			final MetaType item = ((BuildOrderItem)tempArr[queue.size() - 1 - i]).metaType;
			TilePosition itemPosition = ((BuildOrderItem)tempArr[queue.size() - 1 - i]).seedLocation;
			Point seedPositionPoint = null;
			if(queryTilePosition != null)
			{
				seedPositionPoint = new Point(queryTilePosition.getX(), queryTilePosition.getY());
			}

			if (queryType.isUnit() && item.isUnit()) {
				//if (item.getUnitType().getID() == queryType.getUnitType().getID()) {
				if (item.getUnitType() == queryType.getUnitType()) 
				{
					if (queryType.getUnitType().isBuilding())
					{
						if(seedPositionPoint != null && itemPosition != null)
						{
							//System.out.println("distance : " + itemPosition.getDistance(new TilePosition((int)seedPositionPoint.getX(), (int)seedPositionPoint.getY())));
							if (itemPosition.getDistance(new TilePosition((int)seedPositionPoint.getX(), (int)seedPositionPoint.getY())) <= maxRange){
								itemCount++;
							}
						}
					}
				}
			}
			else if (queryType.isTech() && item.isTech()) {
				//if (item.getTechType().getID() == queryType.getTechType().getID()) {
				if (item.getTechType() == queryType.getTechType()) {
					itemCount++;
				}
			}
			else if (queryType.isUpgrade() && item.isUpgrade()) {
				//if (type.getUpgradeType().getID() == queryType.getUpgradeType().getID()) {
				if (item.getUpgradeType() == queryType.getUpgradeType()) {
					itemCount++;
				}
			}
		}
		return itemCount;
	}
	
	/// BuildOrderQueue�� �ش� type �� Item �� �� �� �����ϴ��� �����Ѵ�. queryTilePosition �� �Է��� ���, �ǹ��� ���ؼ� �߰� Ž���Ѵ�
	public int getItemCount(MetaType queryType)
	{
		return getItemCount(queryType, null);
	}
	
	/// BuildOrderQueue�� �ش� type �� Item �� �� �� �����ϴ��� �����Ѵ�. queryTilePosition �� �Է��� ���, �ǹ��� ���ؼ� �߰� Ž���Ѵ�
	public int getItemCount(UnitType unitType, TilePosition queryTilePosition)
	{
		return getItemCount(new MetaType(unitType), queryTilePosition);
	}

	public int getItemCount(UnitType unitType)
	{
		return getItemCount(new MetaType(unitType), null);
	}

	public int getItemCount(TechType techType)
	{
		return getItemCount(new MetaType(techType), null);
	}

	public int getItemCount(UpgradeType upgradeType)
	{
		return getItemCount(new MetaType(upgradeType), null);
	}

	/// increments skippedItems
	public void skipCurrentItem()
	{
		// make sure we can skip
		if (canSkipCurrentItem()) {
			// skip it
			numSkippedItems++;
		}
	}

	public boolean canSkipCurrentItem() 
	{
		// does the queue have more elements
		boolean bigEnough = queue.size() > (int)(1 + numSkippedItems);

		if (!bigEnough) 
		{
			return false;
		}

		// is the current highest priority item not blocking a skip
		Object[] tempArr = queue.toArray();
		//boolean highestNotBlocking = !((BuildOrderItem)tempArr[queue.size() - 1 - numSkippedItems]).blocking;
		boolean highestNotBlocking = !((BuildOrderItem)tempArr[numSkippedItems]).blocking;

		// this tells us if we can skip
		return highestNotBlocking;
	}

	/// queues something with a given priority
	public void queueItem(BuildOrderItem b) 
	{
		// if the queue is empty, set the highest and lowest priorities
		if (queue.isEmpty()) 
		{
			highestPriority = b.priority;
			lowestPriority = b.priority;
		}

		// push the item into the queue
		if (b.priority <= lowestPriority) 
		{
			queue.addLast(b); // C++ :  queue.push_front(b);
		}
		else
		{
			queue.addFirst(b); // C++ :  queue.push_back(b);
		}

		// if the item is somewhere in the middle, we have to sort again
		if ((queue.size() > 1) && (b.priority < highestPriority) && (b.priority > lowestPriority)) 
		{
			// sort the list in ascending order, putting highest priority at the top
			// C++ std::sort(queue.begin(), queue.end());
			Object[] tempArr = queue.toArray();
			Arrays.sort(tempArr);
			queue.clear();
			for(int i=0 ; i<tempArr.length ; i++){
				queue.add((BuildOrderItem)tempArr[i]);
			}
		}

		// update the highest or lowest if it is beaten
		highestPriority = (b.priority > highestPriority) ? b.priority : highestPriority;
		lowestPriority  = (b.priority < lowestPriority)  ? b.priority : lowestPriority;
	}

	/// ��������� ���� ���� �켱������ buildQueue �� �߰��Ѵ�. blocking (�ٸ� ���� �������� �ʰ�, �̰��� ���� �����ϰ� �� ������ ��ٸ��� ���) �⺻���� true �̴�
	public void queueAsHighestPriority(MetaType metaType, boolean blocking, int producerID)
	{
		// the new priority will be higher
		int newPriority = highestPriority + defaultPrioritySpacing;

		// queue the item
		queueItem(new BuildOrderItem(metaType, newPriority, blocking, producerID));
	}

	public void queueAsHighestPriority(MetaType metaType, boolean blocking)
	{
		queueAsHighestPriority(metaType, blocking, -1);
	}

	public void queueAsHighestPriority(MetaType metaType, BuildOrderItem.SeedPositionStrategy seedPositionStrategy, boolean blocking){
		int newPriority = highestPriority + defaultPrioritySpacing;
		queueItem(new BuildOrderItem(metaType, seedPositionStrategy, newPriority, blocking, -1));
	}
	
	public void queueAsHighestPriority(MetaType metaType){
		queueAsHighestPriority(metaType, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
	}

	public void queueAsHighestPriority(MetaType metaType, BuildOrderItem.SeedPositionStrategy seedPositionStrategy){
		queueAsHighestPriority(metaType, seedPositionStrategy, true);
	}
	
	public void queueAsHighestPriority(UnitType unitType, BuildOrderItem.SeedPositionStrategy seedPositionStrategy, boolean blocking){
		int newPriority = highestPriority + defaultPrioritySpacing;
		queueItem(new BuildOrderItem(new MetaType(unitType), seedPositionStrategy, newPriority, blocking, -1));
	}
	
	public void queueAsHighestPriority(MetaType metaType, TilePosition seedPosition, boolean blocking)
	{
		int newPriority = highestPriority + defaultPrioritySpacing;
		queueItem(new BuildOrderItem(metaType, seedPosition, newPriority, blocking, -1));
	}
	
	public void queueAsHighestPriority(UnitType unitType, TilePosition seedPosition, boolean blocking, boolean forcedType)
	{
		int newPriority = highestPriority + defaultPrioritySpacing;
		queueItem(new BuildOrderItem(new MetaType(unitType), seedPosition, newPriority, blocking, -1, forcedType));
	}
	
	public void queueAsHighestPriority(UnitType unitType, TilePosition seedPosition, boolean blocking)
	{
		int newPriority = highestPriority + defaultPrioritySpacing;
		queueItem(new BuildOrderItem(new MetaType(unitType), seedPosition, newPriority, blocking, -1));
	}

	public void queueAsHighestPriority(UnitType unitType, boolean blocking, int producerID){
		int newPriority = highestPriority + defaultPrioritySpacing;
		queueItem(new BuildOrderItem(new MetaType(unitType), newPriority, blocking, producerID));
	}
	
	public void queueAsHighestPriority(UnitType unitType, boolean blocking){
		queueAsHighestPriority(unitType, blocking, -1);
	}

	public void queueAsHighestPriority(TechType techType, boolean blocking, int producerID){
		int newPriority = highestPriority + defaultPrioritySpacing;
		queueItem(new BuildOrderItem(new MetaType(techType), newPriority, blocking, producerID));
	}
	
	public void queueAsHighestPriority(TechType techType, boolean blocking){
		queueAsHighestPriority(techType, blocking, -1);
	}

	public void queueAsHighestPriority(UpgradeType upgradeType, boolean blocking, int producerID){
		int newPriority = highestPriority + defaultPrioritySpacing;
		queueItem(new BuildOrderItem(new MetaType(upgradeType), newPriority, blocking, producerID));
	}

	public void queueAsHighestPriority(UpgradeType upgradeType, boolean blocking){
		queueAsHighestPriority(upgradeType, blocking, -1);
	}

	/// ��������� ���� ���� �켱������ buildQueue �� �߰��Ѵ�. blocking (�ٸ� ���� �������� �ʰ�, �̰��� ���� �����ϰ� �� ������ ��ٸ��� ���) �⺻���� true �̴�
	public void queueAsLowestPriority(MetaType metaType, boolean blocking, int producerID)
	{
		// the new priority will be higher
		int newPriority = lowestPriority - defaultPrioritySpacing;
		if (newPriority < 0) {
			newPriority = 0;
		}

		// queue the item
		queueItem(new BuildOrderItem(metaType, newPriority, blocking, producerID));
	}

	public void queueAsLowestPriority(MetaType metaType, boolean blocking)
	{
		queueAsLowestPriority(metaType, blocking, -1);
	}

	public void queueAsLowestPriority(MetaType metaType, BuildOrderItem.SeedPositionStrategy seedPositionStrategy, boolean blocking){
		queueItem(new BuildOrderItem(metaType, seedPositionStrategy, 0, blocking, -1));
	}
	
	public void queueAsLowestPriority(UnitType unitType, BuildOrderItem.SeedPositionStrategy seedPositionStrategy, boolean blocking){
		queueItem(new BuildOrderItem(new MetaType(unitType), seedPositionStrategy, 0, blocking, -1));
	}

	public void queueAsLowestPriority(UnitType unitType, BuildOrderItem.SeedPositionStrategy seedPositionStrategy){
		queueAsLowestPriority(unitType, seedPositionStrategy, true);
	}

	public void queueAsLowestPriority(UnitType unitType){
		queueAsLowestPriority(unitType, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
	}

	public void queueAsLowestPriority(MetaType metaType, TilePosition seedPosition, boolean blocking)
	{
		queueItem(new BuildOrderItem(metaType, seedPosition, 0, blocking, -1));
	}

	public void queueAsLowestPriority(UnitType unitType, TilePosition seedPosition, boolean blocking, boolean forcedType)
	{
		queueItem(new BuildOrderItem(new MetaType(unitType), seedPosition, 0, blocking, -1, forcedType));
	}
	
	public void queueAsLowestPriority(UnitType unitType, TilePosition seedPosition, boolean blocking)
	{
		queueItem(new BuildOrderItem(new MetaType(unitType), seedPosition, 0, blocking, -1));
	}

	public void queueAsLowestPriority(UnitType unitType, boolean blocking, int producerID){
		queueItem(new BuildOrderItem(new MetaType(unitType), 0, blocking, producerID));
	}

	public void queueAsLowestPriority(UnitType unitType, boolean blocking){
		queueAsLowestPriority(unitType, blocking, -1);
	}
	
	public void queueAsLowestPriority(TechType techType, boolean blocking, int producerID){
		queueItem(new BuildOrderItem(new MetaType(techType), 0, blocking, producerID));
	}

	public void queueAsLowestPriority(TechType techType, boolean blocking){
		queueAsLowestPriority(techType, blocking, -1);
	}

	public void queueAsLowestPriority(TechType techType){
		queueAsLowestPriority(techType, true);
	}

	public void queueAsLowestPriority(UpgradeType upgradeType, boolean blocking, int producerID){
		queueItem(new BuildOrderItem(new MetaType(upgradeType), 0, blocking, producerID));
	}

	public void queueAsLowestPriority(UpgradeType upgradeType, boolean blocking){
		queueAsLowestPriority(upgradeType, blocking, -1);
	}

	public void queueAsLowestPriority(UpgradeType upgradeType){
		queueAsLowestPriority(upgradeType, true);
	}

	/// removes the highest priority item
	public void removeHighestPriorityItem() 
	{
		// remove the back element of the vector
		// queue.pop_back();
		queue.removeFirst();

		// if the list is not empty, set the highest accordingly
		// highestPriority = queue.isEmpty() ? 0 : queue.back().priority;
		highestPriority = queue.isEmpty() ? 0 : queue.getLast().priority;
		lowestPriority  = queue.isEmpty() ? 0 : lowestPriority;
	}

//	public BuildOrderItem getCurrentItem() 
//	{
//		// remove the back element of the vector
//		// C++ : queue.erase(queue.begin() + queue.size() - 1 - numSkippedItems);
//
//		Object[] tempArr = queue.toArray();
//		BuildOrderItem currentItem = (BuildOrderItem)tempArr[numSkippedItems];
//		
//		return currentItem;
//	}
	/// skippedItems ������ item �� �����մϴ�
	public void removeCurrentItem() 
	{
		// remove the back element of the vector
		// C++ : queue.erase(queue.begin() + queue.size() - 1 - numSkippedItems);

		Object[] tempArr = queue.toArray();
		BuildOrderItem currentItem = (BuildOrderItem)tempArr[numSkippedItems];
		queue.remove(currentItem);
		
		//assert((int)(queue.size()) < size);

		// if the list is not empty, set the highest accordingly
		// C++ : highestPriority = queue.isEmpty() ? 0 : queue.back().priority;
		highestPriority = queue.isEmpty() ? 0 : queue.getFirst().priority;
		lowestPriority  = queue.isEmpty() ? 0 : lowestPriority;
	}

	/// returns the size of the queue
	public int size() 
	{
		return queue.size();
	}

	public boolean isEmpty()
	{
		return (queue.size() == 0);
	}

	/// overload the bracket operator for ease of use
	public BuildOrderItem operator(int i)
	{
		Object[] tempArr = queue.toArray();
		return (BuildOrderItem)tempArr[i];
	}

	public Deque<BuildOrderItem> getQueue()
	{
		return queue;
	}
}