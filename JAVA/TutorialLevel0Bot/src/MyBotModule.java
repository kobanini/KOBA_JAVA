/*
+----------------------------------------------------------------------+
| BuildServerCode                                                             |
+----------------------------------------------------------------------+
| Samsung SDS - 2017 Algorithm Contest                                 |
+----------------------------------------------------------------------+
|                                                                      |
+----------------------------------------------------------------------+
| Author: Tekseon Shin  <tekseon.shin@gmail.com>                       |
| Author: Duckhwan Kim  <duckhwan1982.kim@gmail.com>                   |
+----------------------------------------------------------------------+
*/

/*
+----------------------------------------------------------------------+
| UAlbertaBot                                                          |
+----------------------------------------------------------------------+
| University of Alberta - AIIDE StarCraft Competition                  |
+----------------------------------------------------------------------+
|                                                                      |
+----------------------------------------------------------------------+
| Author: David Churchill <dave.churchill@gmail.com>                   |
+----------------------------------------------------------------------+
*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.Flag.Enum;
import bwta.BWTA;


/// MyBotModule �� �����α׷��� �⺻���� ���� ������ ������ class �μ�, ��Ÿũ����Ʈ ��� ���� �߻��ϴ� �̺�Ʈ���� GameCommander class �ν��Ͻ����� �����մϴ�.<br>
///
/// MyBotModule class�� ������ ���� ����,<br>
/// ���� �����α׷� ������ GameCommander class �� �����ϴ� ���·� �����ϵ��� �մϴ�.<br>
/// @see GameCommander
///
/// <br><br>
/// �˰��� ������ȸ �� �����ϰ� ȿ������ ��� ���� Main, MyBotModule, UXManager ������ �����ڵ��� �����ϴ� �ҽ��ڵ带 �����ϰ� ��� �� �����մϴ� <br>
///
/// �˰��� ������ȸ ���弭���� ����ϴ� Main, MyBotModule, UXManager ������ ���������� MyBotModule �� �ݿ��Ͽ����ϴ� <br>
/// ���� �˰��� ������ȸ ���弭�������� �ڵ带 �Ϻ� �����ؼ� �����ϰ� �� �� �ֽ��ϴ� <br>
///
/// �˰��� ������ȸ ���弭���� ����ϴ� Main �� MyBotModule �� �����Ű�� ����� �����մϴ�. <br>
/// �˰��� ������ȸ ���弭���� ����ϴ� MyBotModule �� GameCommander ���� �̺�Ʈ�� �����ϴ� ����� �����ϸ�, ���� �ӵ� ���� ���� �ľ�, ���� ���º� ��Ȳ �ľ� ���� ���� ������ ���� �й��Ű�ų� ���� �����Ű�� �ൿ�� �����մϴ�. <br>
/// �˰��� ������ȸ ���弭���� ����ϴ� UX Manager �� �˰��� ������ȸ �, ���� ���� � �ʿ��� �ּ����� ���븸 ȭ�鿡 ǥ���մϴ�. <br>
/// �� ���ϵ��� InformationManager �� �ٸ� ���ϵ�� Dependency�� ������ ���ߵǾ��� ������, <br>
/// �����ڵ��� InformationManager �� �ٸ� ���ϵ��� �����Ӱ� �����Ͻ� �� �ֽ��ϴ�. 
/// 
public class MyBotModule extends DefaultBWListener {

	/// BWAPI �� �ش��ϴ� ���� ��ü
	private Mirror mirror = new Mirror();
	
	/// ��Ÿũ����Ʈ ��� ��Ȳ ��ü�� ���� ��Ȳ �ľ� �� �׼� ������ �����ϴ� ��ü  <br>
	/// C���� MyBotModule.Broodwar �� �ش��մϴ�
	public static Game Broodwar;

	/// ���� �����α׷�
	/// @see GameCommander			
	private GameCommander gameCommander;

	/// ���� ���ǵ� 
	/// ��ʸ�Ʈ Ŭ���̾�Ʈ ���࿣������ ó�������� Ȥ�ó� ���� ����ó��
	private int numLocalSpeed = 20;

	/// frameskip
	/// ��ʸ�Ʈ Ŭ���̾�Ʈ ���࿣������ ó�������� Ȥ�ó� ���� ����ó��
	private int numFrameSkip = 0;

	// BasicBot 1.2 Patch Start ////////////////////////////////////////////////
	/// �й� ������ ������ä ������ ������Ű�� �ִ� ������ ��
	private int maxDurationForLostCondition = 200;
	// BasicBot 1.2 Patch End //////////////////////////////////////////////////
	
	private boolean isExceptionLostConditionSatisfied = false;	/// Exception ���� ���� �й� üũ ���
	private int exceptionLostConditionSatisfiedFrame = 0;		/// Exception �й� ������ ���۵� ������ ����
	private int maxDurationForExceptionLostCondition = 20;		/// Exception �й� ������ ������ä ������ ������Ű�� �ִ� ������ ��
	
	private boolean isToCheckGameLostCondition = true;			/// �ڵ� �й� üũ ���� ����
	private boolean isGameLostConditionSatisfied = false;		/// �ڵ� �й� üũ ���
	private int gameLostConditionSatisfiedFrame = 0;			/// �ڵ� �й� ������ ���۵� ������ ����
		
	private boolean isToCheckTimeOut = true;					/// Ÿ�� �ƿ� üũ ���� ����
	private int timeOutConditionSatisfiedFrame = 0;				/// Ÿ�� �ƿ� ������ ���۵� ������ ����
	private boolean isTimeOutConditionSatisfied = false;		/// Ÿ�� �ƿ� üũ ���
	private ArrayList<Integer> timerLimits = new ArrayList<Integer>();			///< Ÿ�� �ƿ� �Ѱ�ð� (ms/frame)
	private ArrayList<Integer> timerLimitsBound = new ArrayList<Integer>();		///< Ÿ�� �ƿ� �ʰ��Ѱ�Ƚ��
	private ArrayList<Integer> timerLimitsExceeded = new ArrayList<Integer>();	///< Ÿ�� �ƿ� �ʰ�Ƚ��
	private long[] timeStartedAtFrame = new long[100000];		///< �ش� �������� ������ �ð�
	private long[] timeElapsedAtFrame = new long[100000];		///< �ش� �����ӿ��� ����� �ð� (ms)		

	private boolean isToTestTimeOut = false;					///< Ÿ�� �ƿ� üũ �׽�Ʈ ���� ����
	private int timeOverTestDuration = 0;
	private int timeOverTestFrameCountLimit = 0;
	private int timeOverTestFrameCount = 0;						///< Ÿ�� �ƿ� üũ �׽�Ʈ ���� 
	
	public void run() {
		mirror.getModule().setEventListener(this);
		mirror.startGame();
	}

	/// ��Ⱑ ���۵� �� ��ȸ������ �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onStart() {

		Broodwar = mirror.getGame();
		
		gameCommander = new GameCommander();

		if (Broodwar.isReplay()) {
			return;
		}

		initializeLostConditionVariables();

		/// ��ü �� ���� ��� ���� : ����
		/// ��ʸ�Ʈ Ŭ���̾�Ʈ ���࿣�������� ���� ó�������� Ȥ�ó� ���� ����ó��
		boolean isToEnableCompleteMapInformation = false;

		/// UserInput ��� ���� : ����
		/// ��ʸ�Ʈ Ŭ���̾�Ʈ ���࿣�������� ���� ó�������� Ȥ�ó� ���� ����ó��
		// TODO : �׽�Ʈ���� true, ���� ���ÿ����� false �� ����
		boolean isToEnableUserInput = true;

		if(isToEnableCompleteMapInformation){
			Broodwar.enableFlag(Enum.CompleteMapInformation.getValue());
		}

		if(isToEnableUserInput){
			Broodwar.enableFlag(Enum.UserInput.getValue());
		}

		Broodwar.setCommandOptimizationLevel(1);

		// Speedups for automated play, sets the number of milliseconds bwapi spends in each frame
		// Fastest: 42 ms/frame.  1�ʿ� 24 frame. �Ϲ������� 1�ʿ� 24frame�� ���� ���Ӽӵ��� �Ѵ�
		// Normal: 67 ms/frame. 1�ʿ� 15 frame
		// As fast as possible : 0 ms/frame. CPU�� �Ҽ��ִ� ���� ���� �ӵ�. 
		Broodwar.setLocalSpeed(numLocalSpeed);
		// frameskip�� �ø��� ȭ�� ǥ�õ� ������Ʈ ���ϹǷ� �ξ� ������
		Broodwar.setFrameSkip(numFrameSkip);

		System.out.println("Map analyzing started");
		BWTA.readMap();
		BWTA.analyze();
		BWTA.buildChokeNodes();
		System.out.println("Map analyzing finished");

		gameCommander.onStart();
	}

	///  ��Ⱑ ����� �� ��ȸ������ �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onEnd(boolean isWinner) {
		if (isWinner){
			System.out.println("I won the game");
		} else {
			System.out.println("I lost the game");
		}

		gameCommander.onEnd(isWinner);
		
        System.out.println("Match ended");
        
		// BasicBot 1.2 Patch Start ////////////////////////////////////////////////
		// ����ó��
    	TournamentModuleState state = new TournamentModuleState(
			(int)(System.currentTimeMillis() - timeStartedAtFrame[10])
		);
    	state.ended(isWinner);
    	state.update(timerLimitsExceeded);
    	state.write();

        Broodwar.sendText("Game End");
        System.exit(0);
		// BasicBot 1.2 Patch End //////////////////////////////////////////////////				
	}

	/// ��� ���� �� �� �����Ӹ��� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onFrame() {

		if (Broodwar.isReplay()) {
			return;
		}

		// timeStartedAtFrame �� �����Ѵ�
		if (timeStartedAtFrame[Broodwar.getFrameCount()] == 0) {
			timeStartedAtFrame[Broodwar.getFrameCount()] = System.currentTimeMillis();
		}
			
		// Ÿ�Ӿƿ� üũ �޸� ������ ����
		if ((int)timeStartedAtFrame.length < Broodwar.getFrameCount() + 10)
		{
			timeStartedAtFrame = Arrays.copyOf(timeStartedAtFrame, timeStartedAtFrame.length+10000);
			timeElapsedAtFrame = Arrays.copyOf(timeElapsedAtFrame, timeElapsedAtFrame.length+10000);
		}

		// Pause ���¿����� timeStartedAtFrame �� ��� �����ؼ�, timeElapsedAtFrame �� ����� ���ǵ��� �Ѵ�
		if (Broodwar.isPaused()) {
			timeStartedAtFrame[Broodwar.getFrameCount()] = System.currentTimeMillis();
		}
		else {
			
			try {
				gameCommander.onFrame();
			} 
			catch (Exception e) {

				// BasicBot 1.2 Patch Start ////////////////////////////////////////////////
				// Exception �� �ݺ������� �߻��ص� �� �ѹ��� ǥ���ϵ��� ����				
				if (isExceptionLostConditionSatisfied == false) {
				
					Broodwar.sendText("[Error Stack Trace]");
					System.out.println("[Error Stack Trace]");
					for (int i = 0; i < e.getStackTrace().length && i < 11; i++) {
						StackTraceElement ste = e.getStackTrace()[i];
						Broodwar.sendText(ste.toString());
						System.out.println(ste.toString());
					}
					Broodwar.sendText("GG");
	
					isExceptionLostConditionSatisfied = true;
					
					exceptionLostConditionSatisfiedFrame = Broodwar.getFrameCount();
				}				
				// BasicBot 1.2 Patch End //////////////////////////////////////////////////				
			}
			
			if (isExceptionLostConditionSatisfied) {
				MyBotModule.Broodwar.drawTextScreen(250, 100, "I lost because of EXCEPTION");

				if (MyBotModule.Broodwar.getFrameCount() - exceptionLostConditionSatisfiedFrame >= maxDurationForLostCondition) {
					MyBotModule.Broodwar.leaveGame();
				}
			}
	    }
		
		// BasicBot 1.2 Patch Start ////////////////////////////////////////////////
		// �� ���� ��� : 360 ������ (�ʴ� 24������ * 15 ��)���� �ѹ���
		if (Broodwar.getFrameCount() % 360 == 0)
		{
			TournamentModuleState state = new TournamentModuleState(
				(int)(System.currentTimeMillis() - timeStartedAtFrame[10]));
			state.update(timerLimitsExceeded);
			state.write();
		}
		// BasicBot 1.2 Patch End //////////////////////////////////////////////////

		// ȭ�� ��� �� ����� �Է� ó��
		UXManager.Instance().update();

		checkLostConditions();
	}

	/// ����(�ǹ�/��������/��������)�� Create �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onUnitCreate(Unit unit){
		if (!Broodwar.isReplay()) {
			if (timeStartedAtFrame[Broodwar.getFrameCount()] == 0) {
				timeStartedAtFrame[Broodwar.getFrameCount()] = System.currentTimeMillis();
			}
			
			gameCommander.onUnitCreate(unit);
		} 
	}

	///  ����(�ǹ�/��������/��������)�� Destroy �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onUnitDestroy(Unit unit){
		if (!Broodwar.isReplay()) {
			if (timeStartedAtFrame[Broodwar.getFrameCount()] == 0) {
				timeStartedAtFrame[Broodwar.getFrameCount()] = System.currentTimeMillis();
			}
			
			gameCommander.onUnitDestroy(unit);
		}
	}

	/// ����(�ǹ�/��������/��������)�� Morph �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�<br>
	/// Zerg ������ ������ �ǹ� �Ǽ��̳� ��������/�������� ���꿡�� ���� ��κ� Morph ���·� ����˴ϴ�
	@Override
	public void onUnitMorph(Unit unit){
		if (!Broodwar.isReplay()) {
			if (timeStartedAtFrame[Broodwar.getFrameCount()] == 0) {
				timeStartedAtFrame[Broodwar.getFrameCount()] = System.currentTimeMillis();
			}
			
			gameCommander.onUnitMorph(unit);
		} 
	}
	
	/// ����(�ǹ�/��������/��������)�� �ϴ� �� (�ǹ� �Ǽ�, ���׷��̵�, �������� �Ʒ� ��)�� ������ �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onUnitComplete(Unit unit){
		if (!Broodwar.isReplay()) {
			if (timeStartedAtFrame[Broodwar.getFrameCount()] == 0) {
				timeStartedAtFrame[Broodwar.getFrameCount()] = System.currentTimeMillis();
			}
			
			gameCommander.onUnitComplete(unit);
		}
	}

	/// ����(�ǹ�/��������/��������)�� �Ҽ� �÷��̾ �ٲ� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�<br>
	/// Gas Geyser�� � �÷��̾ Refinery �ǹ��� �Ǽ����� ��, Refinery �ǹ��� �ı��Ǿ��� ��, Protoss ���� Dark Archon �� Mind Control �� ���� �Ҽ� �÷��̾ �ٲ� �� �߻��մϴ�
	@Override
	public void onUnitRenegade(Unit unit){
		if (!Broodwar.isReplay()) {
			if (timeStartedAtFrame[Broodwar.getFrameCount()] == 0) {
				timeStartedAtFrame[Broodwar.getFrameCount()] = System.currentTimeMillis();
			}
			
			gameCommander.onUnitRenegade(unit);
		}
	}

	/// ����(�ǹ�/��������/��������)�� Discover �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�<br>
	/// �Ʊ� ������ Create �Ǿ��� �� ��簡, ���� ������ Discover �Ǿ��� �� �߻��մϴ�
	@Override
	public void onUnitDiscover(Unit unit){
		if (!Broodwar.isReplay()) {
			if (timeStartedAtFrame[Broodwar.getFrameCount()] == 0) {
				timeStartedAtFrame[Broodwar.getFrameCount()] = System.currentTimeMillis();
			}
			
			gameCommander.onUnitDiscover(unit);
		}
	}

	/// ����(�ǹ�/��������/��������)�� Evade �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�<br>
	/// ������ Destroy �� �� �߻��մϴ�
	@Override
	public void onUnitEvade(Unit unit){
		if (!Broodwar.isReplay()) {
			if (timeStartedAtFrame[Broodwar.getFrameCount()] == 0) {
				timeStartedAtFrame[Broodwar.getFrameCount()] = System.currentTimeMillis();
			}
			
			gameCommander.onUnitEvade(unit);
		}
	}

	/// ����(�ǹ�/��������/��������)�� Show �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�<br>
	/// �Ʊ� ������ Create �Ǿ��� �� ��簡, ���� ������ Discover �Ǿ��� �� �߻��մϴ�
	@Override
	public void onUnitShow(Unit unit){
		if (!Broodwar.isReplay()) {
			if (timeStartedAtFrame[Broodwar.getFrameCount()] == 0) {
				timeStartedAtFrame[Broodwar.getFrameCount()] = System.currentTimeMillis();
			}
			
			gameCommander.onUnitShow(unit);
		}
	}

	/// ����(�ǹ�/��������/��������)�� Hide �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�<br>
	/// ���̴� ������ Hide �� �� �߻��մϴ�
	@Override
	public void onUnitHide(Unit unit){
		if (!Broodwar.isReplay()) {
			if (timeStartedAtFrame[Broodwar.getFrameCount()] == 0) {
				timeStartedAtFrame[Broodwar.getFrameCount()] = System.currentTimeMillis();
			}
			
			gameCommander.onUnitHide(unit);
		}
	}

	/// �ٹ̻��� �߻簡 �����Ǿ��� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onNukeDetect(Position target){
		if (!Broodwar.isReplay()) {
			if (timeStartedAtFrame[Broodwar.getFrameCount()] == 0) {
				timeStartedAtFrame[Broodwar.getFrameCount()] = System.currentTimeMillis();
			}

			// ���弭�������� ���� ����
			//gameCommander.onNukeDetect(target);
		}
	}

	/// �ٸ� �÷��̾ ����� ������ �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onPlayerLeft(Player player){
		if (!Broodwar.isReplay()) {
			if (timeStartedAtFrame[Broodwar.getFrameCount()] == 0) {
				timeStartedAtFrame[Broodwar.getFrameCount()] = System.currentTimeMillis();
			}
			
			// ���弭�������� ���� ����
			//gameCommander.onPlayerLeft(player);
		}
	}

	/// ������ ������ �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onSaveGame(String gameName){
		if (!Broodwar.isReplay()) {
			if (timeStartedAtFrame[Broodwar.getFrameCount()] == 0) {
				timeStartedAtFrame[Broodwar.getFrameCount()] = System.currentTimeMillis();
			}

			// ���弭�������� ���� ����
			//gameCommander.onSaveGame(gameName);
		}
	}
	
	/// �ؽ�Ʈ�� �Է� �� ���͸� �Ͽ� �ٸ� �÷��̾�鿡�� �ؽ�Ʈ�� �����Ϸ� �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onSendText(String text){		
		
		if (timeStartedAtFrame[Broodwar.getFrameCount()] == 0) {
			timeStartedAtFrame[Broodwar.getFrameCount()] = System.currentTimeMillis();
		}
		
		ParseTextCommand(text);
		
		gameCommander.onSendText(text);

		// Display the text to the game
		Broodwar.sendText(text);
	}

	/// �ٸ� �÷��̾�κ��� �ؽ�Ʈ�� ���޹޾��� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onReceiveText(Player player, String text){
		if (timeStartedAtFrame[Broodwar.getFrameCount()] == 0) {
			timeStartedAtFrame[Broodwar.getFrameCount()] = System.currentTimeMillis();
		}
		
		Broodwar.printf(player.getName() + " said \"" + text + "\"");

		gameCommander.onReceiveText(player, text);
	}
	
	private void initializeLostConditionVariables(){
		
		timerLimits.add(55);
		timerLimitsBound.add(320);
		timerLimitsExceeded.add(0);

		timerLimits.add(1000);
		timerLimitsBound.add(10);
		timerLimitsExceeded.add(0);

		timerLimits.add(10000);
		timerLimitsBound.add(2);
		timerLimitsExceeded.add(0);
		
	    parseConfigFile("bwapi-data\\tm_settings.ini");
	}
	

	private void parseConfigFile(String filename) {
	    File file = new File(filename);
	    
	    if (file.exists()) {
	  		timerLimits.clear();
	  		timerLimitsBound.clear();
	  		timerLimitsExceeded.clear();
	  		
	  		try (BufferedReader br = new BufferedReader(new FileReader(file));) {
		        String line = null;
		  	    while ((line = br.readLine()) != null) {
			  	      String[] split = line.split(" ");
			  	      
			  	      if ("LocalSpeed".equals(split[0])) {
			  	    	  numLocalSpeed = Integer.parseInt(split[1]);
			  	      } 
			  	      else if ("FrameSkip".equals(split[0])) {
			  	    	  numFrameSkip = Integer.parseInt(split[1]);
			  	      } 
			  	      else if ("Timeout".equals(split[0])) {
			  	    	  timerLimits.add(Integer.parseInt(split[1]));
			  	    	  timerLimitsBound.add(Integer.parseInt(split[2]));
			  	    	  timerLimitsExceeded.add(0);
			  	      } 
			  	      else if ("MaxDurationForLostCondition".equals(split[0])) {
			  	    	  maxDurationForLostCondition = Integer.parseInt(split[1]);
			  	      } 
			  	      else {
			  	    	  MyBotModule.Broodwar.drawTextScreen(250, 100, "Invalid Option in Tournament Module Settings: " + split[0]);
			  	      }
		  	    }
	  		} 
	  		catch (Exception e) {
	  			e.printStackTrace();
	  		}
	    } 
	    else {
	      MyBotModule.Broodwar.drawTextScreen(250, 100, "Tournament Module Settings File Not Found, Using Defaults " + file.getPath());
	    }
	}
	
	/// ����ڰ� �Է��� text �� parse �ؼ� ó���մϴ�
	public void ParseTextCommand(String commandString)
	{
		// Make sure to use %s and pass the text as a parameter,
		// otherwise you may run into problems when you use the %(percent) character!
		Player self = Broodwar.self();

		if ("afap".equals(commandString)) {
			Broodwar.setLocalSpeed(0);
			Broodwar.setFrameSkip(0);
		} else if ("fast".equals(commandString)) {
			Broodwar.setLocalSpeed(24);
			Broodwar.setFrameSkip(0);
		} else if ("slow".equals(commandString)) {
			Broodwar.setLocalSpeed(42);
			Broodwar.setFrameSkip(0);
		} else if ("endthegame".equals(commandString)) {
			// Not needed if using setGUI(false).
			Broodwar.setGUI(false);
		}

		// 1�� ������ ó���� 10�� �Ѱ� �ɸ����� �ϴ� ���� 1�� �غ���
		else if ("delay12000_1".equals(commandString)) {
			isToTestTimeOut = true;
			timeOverTestFrameCount = 0;
			timeOverTestDuration = 12000;
			timeOverTestFrameCountLimit = 1;
		}
		// 1�� ������ ó���� 10�� �Ѱ� �ɸ����� �ϴ� ���� 2�� �غ���
		else if ("delay12000_2".equals(commandString)) {
			isToTestTimeOut = true;
			timeOverTestFrameCount = 0;
			timeOverTestDuration = 12000;
			timeOverTestFrameCountLimit = 2;
		}
		// 1�� ������ ó���� 1�� �Ѱ� �ɸ����� �ϴ� ���� 9�� �غ���
		else if ("delay1200_9".equals(commandString)) {
			isToTestTimeOut = true;
			timeOverTestFrameCount = 0;
			timeOverTestDuration = 1200;
			timeOverTestFrameCountLimit = 9;
		}
		// 1�� ������ ó���� 1�� �Ѱ� �ɸ����� �ϴ� ���� 12�� �غ���
		else if ("delay1200_12".equals(commandString)) {
			isToTestTimeOut = true;
			timeOverTestFrameCount = 0;
			timeOverTestDuration = 1200;
			timeOverTestFrameCountLimit = 12;
		}
		// 1�� ������ ó���� 55 millisecond �Ѱ� �ɸ����� �ϴ� ���� 310�� �غ���
		else if ("delay70_310".equals(commandString)) {
			isToTestTimeOut = true;
			timeOverTestFrameCount = 0;
			timeOverTestDuration = 70;
			timeOverTestFrameCountLimit = 310;
		}
		// 1�� ������ ó���� 55 millisecond �Ѱ� �ɸ����� �ϴ� ���� 330�� �̻� �غ���
		else if ("delay70_330".equals(commandString)) {
			isToTestTimeOut = true;
			timeOverTestFrameCount = 0;
			timeOverTestDuration = 70;
			timeOverTestFrameCountLimit = 330;
		}
	}

	private void checkLostConditions() {
		
		// �й����� üũ
		if (isToCheckGameLostCondition) {
			checkLostConditionAndLeaveGame();
		}

		// Ÿ�Ӿƿ� �׽�Ʈ
		if (isToTestTimeOut) {
			doTimeOutDelay();
		}

		if (isToCheckTimeOut) {
			// Ÿ�Ӿƿ� üũ
			checkTimeOutConditionAndLeaveGame();
		}
	}

	// ���� �ڵ� �й����� : ����ɷ��� ���� �ǹ��� �ϳ��� ���� && ���ݴɷ��� ����/�������ִ� �ǹ��� �ϳ��� ���� && ����/����/Ư���ɷ��� ���� ��ǹ� ������ �ϳ��� ����
	// ��ʸ�Ʈ �������� ������ ���ǹ��ϰ� ���ѽð����� �÷��̽�Ű�� ��찡 ������ �ϱ� ������
	//
	// TODO (���� �߰����� ����) : '�ϲ��� ������ Ŀ�ǵ弾�͵� ���� ���� �̳׶��� ���� ������ �̳׶��� �ϳ��� ���� ���' ó�� ���� �¸��� �̲� ���ɼ��� ���������� ���� ���� ������ �߰� üũ
	public void checkLostConditionAndLeaveGame()
	{
		int canProduceBuildingCount = 0;
		int canAttackBuildingCount = 0;
		int canDoSomeThingNonBuildingUnitCount = 0;
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit.getType().isBuilding()) {
					
				// ���� ���� �ǹ��� �ϳ��� ������ ���� ���� ����.
				if (unit.getType().canProduce()) {
					canProduceBuildingCount++;
					break;
				}

				// ���� ���� �ǹ��� �ϳ��� ������ ���� ���� ����. ũ���ݷδϴ� ����� ���ݴɷ��� �������� ������, ���� ���ݴɷ��� ���� �� �ִ� �ǹ��̹Ƿ� ī��Ʈ�� ����
				if (unit.getType().canAttack() || unit.getType() == UnitType.Zerg_Creep_Colony) {
					canAttackBuildingCount++;
					break;
				}
			}
			else {
				// ���� �ɷ��� ���� �����̳� ���� �ɷ��� ���� ����, Ư�� �ɷ��� ���� ������ �ϳ��� ������ ���� ���� ����
				// ��, ���, ��, �����Ϸ�, ���̾𽺺���, ��ũ��ĭ ���� ���� �¸��� �̲� ���ɼ��� �����̶� ����
				// ġ��, ����, ������ �ɷ¸� �ִ� ���ָ� ������ ���� ����.
				// ��, �޵�, �����, �����ε�, ������, ��Ʋ�� �����ϸ�, ���� �¸��� �̲� �ɷ��� ����
				if (unit.getType().canAttack() || unit.getType().canProduce() 
					|| (unit.getType().isSpellcaster() && unit.getType() != UnitType.Terran_Medic) 
					|| unit.getType() == UnitType.Zerg_Larva
					|| unit.getType() == UnitType.Zerg_Egg || unit.getType() == UnitType.Zerg_Lurker_Egg || unit.getType() == UnitType.Zerg_Cocoon ) 
				{
					canDoSomeThingNonBuildingUnitCount++;
					break;
				}
			}
		}

		//MyBotModule.Broodwar.drawTextScreen(250, 120, "canProduce Building Count        : " + canProduceBuildingCount);
		//MyBotModule.Broodwar.drawTextScreen(250, 130, "canAttack Building Count         : " + canAttackBuildingCount);
		//MyBotModule.Broodwar.drawTextScreen(250, 140, "canDoSomeThing NonBuilding Count : " + canDoSomeThingNonBuildingUnitCount);

		// BasicBot 1.2 Patch Start ////////////////////////////////////////////////
		// �ڵ� �й����� ������ ���°� �����ð� ��ӵǾ��� �� �޽��� SendText
		
		// �ڵ� �й����� �����ϰ� �� ������ ���
		if (canDoSomeThingNonBuildingUnitCount == 0 && canProduceBuildingCount == 0 && canAttackBuildingCount == 0 ) {

			if (isGameLostConditionSatisfied == false) {
				MyBotModule.Broodwar.sendText("I HAVE NO UNIT TO DEFEAT ENEMY PLAYER");

				isGameLostConditionSatisfied = true;
				gameLostConditionSatisfiedFrame = MyBotModule.Broodwar.getFrameCount();
			}
			
		}
		// �ڵ� �й����� ����� �Ǹ� ����
		else if (canDoSomeThingNonBuildingUnitCount != 0 || canProduceBuildingCount != 0 || canAttackBuildingCount != 0) {

			if (isGameLostConditionSatisfied == true) {
				isGameLostConditionSatisfied = false;
				gameLostConditionSatisfiedFrame = 0;
			}
		}

		// �ڵ� �й����� ���� ��Ȳ�� �����ð� ���� ���ӵǾ����� ���� �й�� ó��
		if (isGameLostConditionSatisfied) {

			MyBotModule.Broodwar.drawTextScreen(250, 100, "I lost because I HAVE NO UNIT TO DEFEAT ENEMY PLAYER");
			MyBotModule.Broodwar.drawTextScreen(250, 115, "I will leave game in " 
					+ (maxDurationForLostCondition - (MyBotModule.Broodwar.getFrameCount() - gameLostConditionSatisfiedFrame)) 
					+ " frames");

			// �ڵ� �й����� �������� 100 ������ �Ǿ����� �޽��� ǥ��
			if (MyBotModule.Broodwar.getFrameCount() - gameLostConditionSatisfiedFrame == 100) {
				int count = 0;
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit != null && unit.exists() ) {
						MyBotModule.Broodwar.sendText("Alive Unit : " + unit.getType().toString() + " " + unit.getID() + " at " + unit.getTilePosition());
					}
					count ++;
					if (count == 10) break;
				}
				MyBotModule.Broodwar.sendText("Alive Unit Number : " + MyBotModule.Broodwar.self().getUnits().size());

				MyBotModule.Broodwar.sendText("I lost because I HAVE NO UNIT TO DEFEAT ENEMY PLAYER");
				MyBotModule.Broodwar.sendText("GG");
				System.out.println("I lost because I HAVE NO UNIT TO DEFEAT ENEMY PLAYER");

			}
			
			if (MyBotModule.Broodwar.getFrameCount() - gameLostConditionSatisfiedFrame >= maxDurationForLostCondition) {
				MyBotModule.Broodwar.leaveGame();
			}
		}
		// BasicBot 1.2 Patch End //////////////////////////////////////////////////
	}



	void doTimeOutDelay() {
		if (timeOverTestFrameCount < timeOverTestFrameCountLimit) {

			// 10 �����Ӹ��� 1���� Ÿ�� �����̸� �����Ѵ�
			if (Broodwar.getFrameCount() % 10 == 0) {
				long startTime = System.currentTimeMillis();
				while (System.currentTimeMillis() - startTime < timeOverTestDuration) {
				}
				timeOverTestFrameCount++;
			}
		}
		else {
			isToTestTimeOut = false;
		}	
	}

	/// Ÿ�� �ƿ� ������ üũ�Ͽ�, ���� ���� �� GG �����ϰ� ������ �����ϴ�
	void checkTimeOutConditionAndLeaveGame(){
		
		// BasicBot 1.2 Patch Start ////////////////////////////////////////////////
		// �����Ӻ� �ҿ�ð� ������ �ּ� �߰�

		if (Broodwar.getFrameCount() >= 10) {

			// ���� �����ӿ����� �ҿ�ð� ���
			timeElapsedAtFrame[Broodwar.getFrameCount()] = System.currentTimeMillis()
					- timeStartedAtFrame[Broodwar.getFrameCount()];

			long timeElapsedAtLastFrame = timeElapsedAtFrame[Broodwar.getFrameCount() - 1];

			// (������ F-1 ���� �ҿ��ߴ� �ð�)  = (������ F - 1 �� ������ �ð�) - (������ F -1 �� ���� �ð�) ���� �ϸ� 
			// onFrame ���� ������ �ð��� ����� �� ���� �������� ���۵Ǳ� ������ StarCraft �� ó���ϴµ� �ð��� �� �ɸ��� ������ ���� ����Ȯ��
			// �׷���, �ϴ� �� ����� �ּ�.

//			timeElapsedAtFrame[Broodwar.getFrameCount() - 1] =
//					timeEndAtFrame[Broodwar.getFrameCount() - 1]
//					- timeStartedAtFrame[Broodwar.getFrameCount() - 1];
//
//			long timeElapsedAtLastFrame = timeElapsedAtFrame[Broodwar.getFrameCount()-1];

			// (������ F-2 ���� �ҿ��ߴ� �ð�)  = (������ F - 1 �� ���� �ð�) - (������ F -2 �� ���� �ð�) ���� �ϸ� �ȵ�.
			// ���� ���� ��å������ ���� ������ �������µ�,  ������ ����ȭ ������ ���� �ð��� �������� �����ؼ� �Բ� Ÿ�Ӿƿ��� ī��Ʈ�Ǳ� ����.
//			timeElapsedAtFrame[Broodwar.getFrameCount() - 2] = 
//					timeStartedAtFrame[Broodwar.getFrameCount() - 1]
//					- timeStartedAtFrame[Broodwar.getFrameCount() - 2];
//			long timeElapsedAtLastFrame = timeElapsedAtFrame[Broodwar.getFrameCount() - 2];

			// ���� �ð� ǥ��
			Broodwar.drawTextScreen(260, 5, "FrameCount :");
			Broodwar.drawTextScreen(340, 5, ""+ Broodwar.getFrameCount());
			Broodwar.drawTextScreen(370, 5, "("
					+(int)(Broodwar.getFrameCount() / (23.8 * 60))
					+":"
					+(int)((int)(Broodwar.getFrameCount() / 23.8) % 60)
					+")"
			);
							
			// Ÿ�Ӿƿ� üũ ��Ȳ�� ȭ�鿡 ǥ��
			int y = 15;
			for (int t = 0; t < timerLimits.size(); ++t)
			{
				Broodwar.drawTextScreen(260, y, "> "+timerLimits.get(t)+" ms : "+timerLimitsExceeded.get(t)+" / "+timerLimitsBound.get(t));
				y += 10;
			}

			/*
			// �� �����Ӻ� �ҿ�ð� ����� ȭ�鿡 ǥ��
			y = 100;
			for (int i = Broodwar.getFrameCount() - 9; i < Broodwar.getFrameCount(); ++i)
			{
				Broodwar.drawTextScreen(260, y, "["+i+"] : "+timeStartedAtFrame[i]+" "+timeElapsedAtFrame[i]+" ms");
				y += 10;
			}

			// �ִ� ���� �ð��� �ҿ��� �������� ȭ�鿡 ǥ��
			y = 220;
			int maxI = 0;
			for (int i = 0; i < Broodwar.getFrameCount(); ++i)
			{
				if (timeElapsedAtFrame[maxI] < timeElapsedAtFrame[i]) {
					maxI = i;
				}
			}
			Broodwar.drawTextScreen(260, y, "["+maxI+"] : "+timeStartedAtFrame[maxI]+" "+timeElapsedAtFrame[maxI]+" ms");
			*/

			// Ÿ�Ӿƿ� üũ �� ���� ����
			for (int t = 0; t < timerLimits.size(); ++t)
			{
				if (timeElapsedAtLastFrame > timerLimits.get(t))
				{
					timerLimitsExceeded.set(t, timerLimitsExceeded.get(t)+1);

					if (timerLimitsExceeded.get(t).equals(timerLimitsBound.get(t)))
					{
						isTimeOutConditionSatisfied = true;
						timeOutConditionSatisfiedFrame = Broodwar.getFrameCount();
						
						Broodwar.sendText("I lost because of TIMEOUT ("+timerLimitsBound.get(t)+" frames exceed "+timerLimits.get(t)+" ms/frame)");
						System.out.println("I lost because of TIMEOUT ("+timerLimitsBound.get(t)+" frames exceed "+timerLimits.get(t)+" ms/frame)");
						Broodwar.sendText("GG");
					}
				}
			}
			
			// �ڵ� �й����� ���� ��Ȳ�� �����ð� ���� ���ӵǾ����� ���� �й�� ó��
			if (isTimeOutConditionSatisfied) {

				MyBotModule.Broodwar.drawTextScreen(250, 100, "I lost because of TIMEOUT");

				if (MyBotModule.Broodwar.getFrameCount() - timeOutConditionSatisfiedFrame >= maxDurationForLostCondition) {
					MyBotModule.Broodwar.leaveGame();
				}
			}
		}

	}

	// BasicBot 1.2 Patch Start ////////////////////////////////////////////////
	/// �� ���α׷��� ���� : ���� ����ִ��� üũ�ϱ� ���� File Write �뵵
	class TournamentModuleState {

		public String tournamentModuleStateFileName; ///< �� ���α׷��� ���¸� ���� ���� �̸�		

		public String selfName;
		public String enemyName;
		public String mapName;

		int frameCount;
		int selfScore;
		int enemyScore;
		int gameElapsedTime;
		int gameOver;
		int gameTimeUp;

		int isDefeated;
		int isVictorious;

		ArrayList<Integer> timerLimitsExceededInTournamentModuleState;

		TournamentModuleState(int _gameElapsedTime)
		{
			tournamentModuleStateFileName = "gameStateOfBot.txt";

			selfName = Broodwar.self().getName();
			enemyName = Broodwar.enemy().getName();
			mapName = Broodwar.mapFileName();

			frameCount = Broodwar.getFrameCount();
			selfScore = Broodwar.self().getKillScore()
				+ Broodwar.self().getBuildingScore()
				+ Broodwar.self().getRazingScore()
				+ Broodwar.self().gatheredMinerals()
				+ Broodwar.self().gatheredGas();

			enemyScore = 0;

			gameOver = 0;
			gameTimeUp = 0;

			gameElapsedTime = _gameElapsedTime;

			isDefeated = -1;
			isVictorious = -1;
		}

		void update(ArrayList<Integer> _timerLimitsExceeded)
		{
			frameCount = Broodwar.getFrameCount();
			selfScore = Broodwar.self().getKillScore()
				//+ Broodwar.self().getBuildingScore()
				+ Broodwar.self().getRazingScore();
			//+ Broodwar.self().gatheredMinerals()
			//+ Broodwar.self().gatheredGas();

			timerLimitsExceededInTournamentModuleState = new ArrayList<Integer>();
			for (int t = 0; t < _timerLimitsExceeded.size(); ++t)
			{
				timerLimitsExceededInTournamentModuleState.add(_timerLimitsExceeded.get(t));
			}
		}

		void ended(boolean isWinner)
		{
			gameOver = 1;
			if (isWinner) {
				isDefeated = 0;
				isVictorious = 1;
			}
			else {
				isDefeated = 1;
				isVictorious = 0;
			}
		}

		boolean write()
		{
			gameTimeUp = Broodwar.getFrameCount() > 64800 ? 1 : 0;

			boolean result = false;
			BufferedWriter bw = null;
			try{
				bw = new BufferedWriter(new FileWriter(tournamentModuleStateFileName, false));
				
				bw.write(""+selfName); bw.newLine();
				bw.write(""+enemyName); bw.newLine();
				bw.write(""+mapName); bw.newLine();
				bw.write(""+frameCount); bw.newLine();
				bw.write(""+selfScore); bw.newLine();
				bw.write(""+enemyScore); bw.newLine();
				bw.write(""+gameElapsedTime); bw.newLine();
				bw.write(""+isDefeated); bw.newLine();
				bw.write(""+isVictorious); bw.newLine();
				bw.write(""+gameOver); bw.newLine();
				bw.write(""+gameTimeUp); bw.newLine();

				for (int i = 0; i<timerLimitsExceededInTournamentModuleState.size(); ++i)
				{
					bw.write(""+timerLimitsExceededInTournamentModuleState.get(i)); bw.newLine();
				}

				bw.flush();
				result = true;				
			}
			catch(Exception e) {
			}
			finally {
				try{
					if (bw != null) {
						bw.close();
					}
				}
				catch(Exception ex) {				
				}
			}

			return result;
		}
	}
	
	
}