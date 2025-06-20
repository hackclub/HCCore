package com.hackclub.hccore.commands.general;

import com.hackclub.hccore.DataManager;
import com.hackclub.hccore.HCCorePlugin;

public abstract class AbstractCommand {
  public final HCCorePlugin plugin = HCCorePlugin.getInstance();
  public final DataManager dataManager = plugin.getDataManager();
}
