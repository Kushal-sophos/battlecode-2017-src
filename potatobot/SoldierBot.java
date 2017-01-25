package potatobot;
import battlecode.common.*;

public class SoldierBot extends Globals
{
	public static void loop()throws GameActionException
	{
		movingDirection = here.directionTo(theirInitialArchons[0]);
		while (true)
		{
			header();

			// Look for broadcasted archons
			int enemyArchons = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[0]);
			MapLocation closestArchonLocation = null;
			float closestArchonDistance = 500000;
			if (enemyArchons > 0)
			{
				for (int i = 2; i <= enemyArchons * 2; i += 2)
				{
					int hashedLocation = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[i]);
					if (hashedLocation == -1)
					{
						continue;
					}
					else
					{
						MapLocation unhashedLocation = unhashIt(hashedLocation);
						float enemyDistance = here.distanceTo(unhashedLocation);
						if (enemyDistance < closestArchonDistance)
						{
							closestArchonDistance = enemyDistance;
							closestArchonLocation = unhashedLocation;
						}
					}
				}
			}
			if (closestArchonLocation != null)
			{
				movingDirection = here.directionTo(closestArchonLocation);
			}
			
			// Check all nearby enemies
			for (RobotInfo enemy : enemies)
			{
				RobotType enemyType = enemy.getType();
				MapLocation enemyLocation = enemy.getLocation();
				if (enemyType == RobotType.LUMBERJACK && here.distanceTo(enemyLocation) - myType.bodyRadius < 3.5f)
				{
					movingDirection = enemyLocation.directionTo(here);
					break;
				}
				else
				{
					movingDirection = here.directionTo(enemyLocation);
					break;
				}
			}
			
			
			// Look for broadcasted gardeners
			int enemyGardeners = rc.readBroadcast(ENEMY_GARDENERS_CHANNELS[0]);
			MapLocation closestGardenerLocation = null;
			float closestGardenerDistance = 500000;
			if (enemyGardeners > 0)
			{
				for (int i = 2; i <= enemyGardeners * 2; i += 2)
				{
					int hashedLocation = rc.readBroadcast(ENEMY_GARDENERS_CHANNELS[i]);
					if (hashedLocation == -1)
					{
						continue;
					}
					else
					{
						MapLocation unhashedLocation = unhashIt(hashedLocation);
						float enemyDistance = here.distanceTo(unhashedLocation);
						if (enemyDistance < closestGardenerDistance)
						{
							closestGardenerDistance = enemyDistance;
							closestGardenerLocation = unhashedLocation;
						}
					}
				}
			}
			if (closestGardenerLocation != null)
			{
				movingDirection = here.directionTo(closestGardenerLocation);
			}
			
			// Defend your closest farmer
			int soldiers = robotCount[RobotType.SOLDIER.ordinal()];
			int farmers = robotCount[farmerIndex];
			if (soldiers <= farmers)
			{
				int allyFarmLocations = rc.readBroadcast(FARM_LOCATIONS_CHANNELS[0]);
				MapLocation closestFarmLocation = null;
				float closestFarmDistance = 500000;
				if (allyFarmLocations > 0)
				{
					for (int i = 1; i <= allyFarmLocations; i++)
					{
						int hashedLocation = rc.readBroadcast(FARM_LOCATIONS_CHANNELS[i]);
						if (hashedLocation == -1)
						{
							continue;
						}
						else
						{
							MapLocation unhashedLocation = unhashIt(hashedLocation);
							float farmDistance = here.distanceTo(unhashedLocation);
							if (farmDistance < closestFarmDistance)
							{
								closestFarmDistance = farmDistance;
								closestFarmLocation = unhashedLocation;
							}
						}
					}
				}
				if (closestFarmLocation != null)
				{
					movingDirection = here.directionTo(closestFarmLocation);
				}
			}
			
			// movingDirection decided, now tryToMove
			else if (!tryToMove(movingDirection))
			{
				movingDirection = randomDirection();
			}
			shootClosestEnemy();
			
			footer();
		}
	}
	
	public static boolean shootClosestEnemy()throws GameActionException
	{
		for (RobotInfo enemy : enemies)
		{
			if (here.distanceTo(enemy.getLocation()) <= 4 || (enemies.length > 4 && enemies.length * 3 > allies.length * 2))
			{
				if (tryPentadShot(enemy))
				{
					return true;
				}
			}
			else if (here.distanceTo(enemy.getLocation()) <= 6 || (enemies.length > 3 && enemies.length * 3 > allies.length * 2))
			{
				if (tryTriadShot(enemy))
				{
					return true;
				}
			}
			else
			{
				if (trySingleShot(enemy))
				{
					return true;
				}
			}
		}
		return false;
	}
}