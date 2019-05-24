package it.mountaineering.gadria.ring.memory.process;

import it.mountaineering.gadria.ring.memory.bean.FileWithCreationTime;
import it.mountaineering.gadria.ring.memory.util.DiskSpaceManager;

public class VlcLauncherBackupSaveVideoTask implements Runnable {

	DiskSpaceManager diskSPaceManager;
	FileWithCreationTime fileWithCreationTime;

	public VlcLauncherBackupSaveVideoTask(DiskSpaceManager diskSPaceManager, FileWithCreationTime fileWithCreationTime) {
		this.diskSPaceManager = diskSPaceManager;
		this.fileWithCreationTime = fileWithCreationTime;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		diskSPaceManager.addLatestFile(fileWithCreationTime);
	}

}
