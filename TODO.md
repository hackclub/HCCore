# TODO

order by priority

## 🔴 High

- [x] `onCommand` in `LocCommand.java` on line 103-106: Array out of bounds: missing `return true;` after error message when `args.length < 3`
- [x] `Constructor` in `MileAdv.java` on line 24-28: Severe performance issue: checks statistic on every PlayerMoveEvent without checking if advancement already granted
- [x] `Constructor` in `AstraAdv.java` on line 25-31: Severe performance issue: checks location on every PlayerMoveEvent without checking if advancement already granted
- [x] `Constructor` in `MusicophileAdv.java` on line 29-92: Data loss across sessions: disc collection stored in entity metadata which is not persisted
- [x] `onlinePlayers` in `DataManager.java` on line 16: Thread safety issue: HashMap accessed from async thread in `onPrePlayerLogin`, needs ConcurrentHashMap
- [ ] `onCommand` in `LocCommand.java` on line 110-111: Wrong recipient: second message sent to sender instead of recipient

## 🟡 Medium
- [ ] `onCommand` in `LocCommand.java` on line 115: Potential NPE: `sendLocation.getWorld()` can return null if world is unloaded
- [x] `musicDiscs` in `MusicophileAdv.java` on line 19-26: Incomplete feature: missing newer music discs (MUSIC_DISC_RELIC, MUSIC_DISC_CREATOR, etc.) making advancement impossible to complete
- [ ] `onAsyncPlayerChat` in `PlayerListener.java` on line 80: Potential NPE: `getData()` can return null if event fires before `onJoin()`
- [ ] `onCommand` in `ColorCommand.java` on line 63: Wrong message type: reset message says "chat" instead of "name"
- [ ] `setNickname` in `PlayerData.java` on line 113: Edge case: `oldName` could be null if both nickname and offlinePlayer.getName() are null
- [x] `Constructor` in `MusicophileAdv.java` on line 29-92: Inefficient design: registers 30 event handlers (15 discs × 2 events) instead of 2 handlers
- [x] `load` in `PlayerData.java` on line 162: Resource leak: FileReader never closed
- [x] `save` in `PlayerData.java` on line 180: Resource leak: FileWriter not using try-with-resources

## 🟢 Low
- [x] `toSIPrefix` in `StatsCommand.java` on line 135: Unreachable code: final `return null;` is unreachable, all cases covered
- [ ] `onPacketSending` in `NameChangeListener.java` on line 29: Minor performance: creates new ArrayList for every packet
- [ ] `handleInventoryClick/handleItemPickup` in `MusicophileAdv.java` on line 35-36, 73-74: Unchecked cast warning: potential ClassCastException and IndexOutOfBoundsException with metadata
- [ ] `unregisterPlayer` in `DataManager.java` on line 73: Null pointer exception: `getTeam()` can return null but is called without null check before `unregister()`
- [ ] `onPrePlayerLogin` in `PlayerListener.java` on line 116-123: Race condition: creates PlayerData for offline player that gets discarded when `registerPlayer()` creates new instance, even though its only used to check for slack id



note: a lot of this stuff is for redundancy
